package com.instantchatbot.service.ai;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service dedicated to sanitizing and extracting core textual content from raw HTML.
 * It identifies potential boilerplate or non-essential navigation elements and filters
 * them out to ensure high-quality data for the RAG pipeline.
 */
@Service
public class ContentCleanerService {

    private static final Logger log = LoggerFactory.getLogger(ContentCleanerService.class);

    private static final Set<String> REMOVE_TAGS = Set.of(
            "script", "style", "noscript", "iframe", "svg",
            "form", "button", "input", "select", "textarea"
    );

    private static final List<String> BOILERPLATE_PATTERNS = List.of(
            "nav", "navbar", "menu", "sidebar", "footer", "header",
            "cookie", "popup", "modal", "advertisement", "ad-",
            "social", "share", "comment", "related", "breadcrumb"
    );

    private static final Pattern EXCESSIVE_WHITESPACE = Pattern.compile("\\n{3,}");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile(" {2,}");

    /**
     * Parses raw HTML into a domain-specific CleanedContent record.
     *
     * @param html the raw HTML source of the page
     * @param url the source URL (for metadata)
     * @return a record contenant stripped and normalized text/headings
     */
    public CleanedContent clean(String html, String url) {
        Document doc = Jsoup.parse(html);

        // Remove unwanted tags
        for (String tag : REMOVE_TAGS) {
            doc.select(tag).remove();
        }

        // Remove boilerplate elements by class/id
        removeBoilerplate(doc);

        // Extract title
        String title = doc.title() != null ? doc.title().trim() : "";

        // Extract headings
        List<String> headings = new ArrayList<>();
        for (int level = 1; level <= 4; level++) {
            for (Element h : doc.select("h" + level)) {
                String text = h.text().trim();
                if (!text.isEmpty()) {
                    headings.add(text);
                }
            }
        }

        // Get main content area (prefer semantic tags)
        Element mainContent = doc.selectFirst("main");
        if (mainContent == null) mainContent = doc.selectFirst("article");
        if (mainContent == null) mainContent = doc.selectFirst("[role=main]");
        if (mainContent == null) mainContent = doc.selectFirst("#content");
        if (mainContent == null) mainContent = doc.selectFirst(".content");
        if (mainContent == null) mainContent = doc.body();

        // Extract text
        String text = mainContent != null ? mainContent.text() : doc.text();
        text = cleanText(text);

        if (text.length() < 50) {
            log.debug("Very short content ({} chars) from {}", text.length(), url);
        }

        return new CleanedContent(text, title, headings, url);
    }

    private void removeBoilerplate(Document doc) {
        for (Element el : doc.getAllElements()) {
            String classes = String.join(" ", el.classNames()).toLowerCase();
            String id = el.id().toLowerCase();
            String combined = classes + " " + id;

            boolean isBoilerplate = BOILERPLATE_PATTERNS.stream()
                    .anyMatch(combined::contains);

            if (isBoilerplate) {
                el.remove();
            }
        }
    }

    private String cleanText(String text) {
        text = EXCESSIVE_WHITESPACE.matcher(text).replaceAll("\n\n");
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");

        // Remove very short lines (likely noise)
        String result = Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(line -> line.length() > 2)
                .collect(Collectors.joining("\n"));

        return result.trim();
    }

    /**
     * Represents cleaned content extracted from a web page.
     */
    public record CleanedContent(String text, String title, List<String> headings, String url) {}
}
