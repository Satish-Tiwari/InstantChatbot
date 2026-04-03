package com.instantchatbot.service.ai;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service providing web crawling capabilities using the Jsoup library.
 * It traverses internal links starting from a base URL, respecting depth limits,
 * extension filters, and optional sitemap.xml files.
 */
@Service
public class WebCrawlerService {

    private static final Logger log = LoggerFactory.getLogger(WebCrawlerService.class);

    private static final Set<String> SKIP_EXTENSIONS = Set.of(
            ".pdf", ".jpg", ".jpeg", ".png", ".gif", ".svg", ".ico",
            ".css", ".js", ".zip", ".mp4", ".mp3", ".woff", ".woff2", ".ttf"
    );

    @Value("${crawler.max-pages:50}")
    private int defaultMaxPages;

    @Value("${crawler.max-depth:3}")
    private int defaultMaxDepth;

    @Value("${crawler.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${crawler.user-agent:Instant ChatbotBot/1.0}")
    private String userAgent;

    /**
     * Initiates a crawl of a website to discover and retrieve page content.
     *
     * @param baseUrl the starting URL for the crawl
     * @param maxPages the maximum total count of unique pages to retrieve
     * @param maxDepth the maximum number of link-hops away from the base URL
     * @return a list of successfully retrieved CrawledPage records
     */
    public List<CrawledPage> crawl(String baseUrl, int maxPages, int maxDepth) {
        String normalizedBase = baseUrl.replaceAll("/+$", "");
        String domain = extractDomain(normalizedBase);

        Set<String> visited = ConcurrentHashMap.newKeySet();
        List<CrawledPage> pages = Collections.synchronizedList(new ArrayList<>());

        log.info("Starting crawl: {} (max_pages={}, max_depth={})", normalizedBase, maxPages, maxDepth);

        // Try sitemap first
        List<String> sitemapUrls = fetchSitemapUrls(normalizedBase);
        if (!sitemapUrls.isEmpty()) {
            log.info("Found {} URLs from sitemap.xml", sitemapUrls.size());
        }

        // Crawl from base URL
        crawlRecursive(normalizedBase, domain, 0, maxDepth, maxPages, visited, pages);

        // Also crawl sitemap URLs not yet visited
        for (String url : sitemapUrls) {
            if (pages.size() >= maxPages) break;
            if (!visited.contains(url.replaceAll("/+$", ""))) {
                crawlRecursive(url, domain, 1, maxDepth, maxPages, visited, pages);
            }
        }

        log.info("Crawl complete: {} pages collected", pages.size());
        return pages;
    }

    private void crawlRecursive(String url, String domain, int depth, int maxDepth,
                                 int maxPages, Set<String> visited, List<CrawledPage> pages) {
        String normalized = url.replaceAll("/+$", "").split("#")[0].split("\\?")[0];

        if (visited.contains(normalized) || pages.size() >= maxPages || depth > maxDepth) {
            return;
        }
        visited.add(normalized);

        try {
            Connection.Response response = Jsoup.connect(normalized)
                    .userAgent(userAgent)
                    .timeout(timeoutSeconds * 1000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute();

            if (response.statusCode() != 200) {
                log.debug("Non-200 status for {}: {}", normalized, response.statusCode());
                return;
            }

            String contentType = response.contentType();
            if (contentType != null && !contentType.contains("text/html")) {
                return;
            }

            Document doc = response.parse();
            String title = doc.title();

            pages.add(new CrawledPage(normalized, title, doc.html()));
            log.info("Crawled [{}/{}]: {}", pages.size(), maxPages, normalized);

            // Follow internal links
            if (depth < maxDepth && pages.size() < maxPages) {
                List<String> links = extractInternalLinks(doc, normalized, domain);
                for (String link : links) {
                    if (pages.size() >= maxPages) break;
                    crawlRecursive(link, domain, depth + 1, maxDepth, maxPages, visited, pages);
                }
            }

        } catch (IOException e) {
            log.warn("Error crawling {}: {}", normalized, e.getMessage());
        }
    }

    private List<String> extractInternalLinks(Document doc, String currentUrl, String domain) {
        List<String> links = new ArrayList<>();
        Elements anchors = doc.select("a[href]");

        for (Element a : anchors) {
            String href = a.attr("abs:href");
            if (href.isEmpty()) {
                // Resolve relative URLs
                href = a.absUrl("href");
                if (href.isEmpty()) continue;
            }

            // Skip non-page links
            if (href.startsWith("mailto:") || href.startsWith("tel:") ||
                href.startsWith("javascript:") || href.startsWith("#")) {
                continue;
            }

            try {
                URI uri = URI.create(href);
                String linkDomain = uri.getHost();
                if (linkDomain == null || !linkDomain.equals(domain)) continue;

                String path = uri.getPath() != null ? uri.getPath().toLowerCase() : "";
                boolean isSkipExt = SKIP_EXTENSIONS.stream().anyMatch(path::endsWith);
                if (isSkipExt) continue;

                // Clean the URL
                String clean = href.split("#")[0].split("\\?")[0].replaceAll("/+$", "");
                if (!clean.isEmpty()) {
                    links.add(clean);
                }
            } catch (Exception ignored) {
                // Invalid URI, skip
            }
        }

        return new ArrayList<>(new LinkedHashSet<>(links)); // deduplicate preserving order
    }

    private List<String> fetchSitemapUrls(String baseUrl) {
        List<String> urls = new ArrayList<>();
        String sitemapUrl = baseUrl + "/sitemap.xml";

        try {
            Document doc = Jsoup.connect(sitemapUrl)
                    .userAgent(userAgent)
                    .timeout(timeoutSeconds * 1000)
                    .ignoreContentType(true)
                    .get();

            Elements locs = doc.select("url > loc");
            for (Element loc : locs) {
                String url = loc.text().trim();
                if (!url.isEmpty()) {
                    urls.add(url);
                }
            }
        } catch (Exception e) {
            log.debug("No sitemap.xml found at {}: {}", sitemapUrl, e.getMessage());
        }

        return urls.subList(0, Math.min(urls.size(), defaultMaxPages));
    }

    private String extractDomain(String url) {
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Represents a single crawled web page.
     */
    public record CrawledPage(String url, String title, String html) {}
}
