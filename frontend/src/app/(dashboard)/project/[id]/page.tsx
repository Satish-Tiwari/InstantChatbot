'use client';

import Link from 'next/link';
import { useProject, useStartCrawl, useDownloadBot } from '@/hooks/useProjects';
import { CrawlProgress } from '@/components/features/CrawlProgress';
import { ChatPreview } from '@/components/features/ChatPreview';
import { CrawlOverlay } from '@/components/features/CrawlOverlay';
import { useState, useEffect, useRef } from 'react';
import {
  ArrowLeft, Globe, Loader2, Download, Play,
  FileText, Layers, Clock, RefreshCw, Code2, Activity, ArrowUpRight,
} from 'lucide-react';
import { cn, getStatusColor, getStatusLabel, isProcessing, formatDate, formatRelativeTime } from '@/lib/utils';

interface PageProps {
  params: { id: string };
}

export default function ProjectDetailPage({ params }: PageProps) {
  const { id } = params;
  const projectId = parseInt(id, 10);

  const { data: project, isLoading, isError } = useProject(projectId);
  const [isOverlayOpen, setOverlayOpen] = useState(false);
  const hasAutoOpened = useRef(false);

  // Auto-open scanner if project is currently processing
  useEffect(() => {
    if (project && isProcessing(project.status) && !hasAutoOpened.current) {
      setOverlayOpen(true);
      hasAutoOpened.current = true;
    }
  }, [project]);
  const crawlMutation = useStartCrawl();
  const downloadMutation = useDownloadBot();

  const handleStartCrawl = (id: number) => {
    crawlMutation.mutate(id, {
      onSuccess: () => setOverlayOpen(true),
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-32">
        <Loader2 className="w-10 h-10 text-brand-500 animate-spin" />
      </div>
    );
  }

  if (isError || !project) {
    return (
      <div className="text-center py-32">
        <p className="text-red-400 text-lg mb-2">Project not found</p>
        <Link href="/dashboard" className="text-brand-400 text-sm hover:underline">
          ← Back to Dashboard
        </Link>
      </div>
    );
  }

  const processing = isProcessing(project.status);
  const isReady = project.status === 'READY';
  const canStartCrawl = project.status === 'PENDING' || project.status === 'FAILED';

  return (
    <div className="max-w-7xl mx-auto px-6 py-8">
      {/* Back link */}
      <Link
        href="/dashboard"
        className="inline-flex items-center gap-1.5 text-sm text-gray-400 hover:text-brand-400
                   transition-colors mb-6 animate-fade-in"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Dashboard
      </Link>

      {/* Header */}
      <div className="animate-fade-in mb-8">
        <div className="flex items-center gap-4 mb-2">
          <h1 className="text-3xl font-bold">{project.name}</h1>
          <span
            onClick={() => processing && setOverlayOpen(true)}
            className={cn(
              'inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold uppercase tracking-wide',
              processing && 'cursor-pointer hover:brightness-125 transition-all',
              getStatusColor(project.status)
            )}
          >
            {processing && <Loader2 className="w-3 h-3 animate-spin" />}
            {getStatusLabel(project.status)}
            {processing && <ArrowUpRight className="w-3 h-3 ml-0.5" />}
          </span>
        </div>
        <a
          href={project.websiteUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 text-sm text-gray-400 hover:text-brand-400 transition-colors"
        >
          <Globe className="w-3.5 h-3.5" />
          {project.websiteUrl}
        </a>
      </div>

      {/* Action buttons */}
      <div className="flex gap-3 mb-8 animate-fade-in" style={{ animationDelay: '100ms' }}>
        {canStartCrawl && (
          <button
            onClick={() => handleStartCrawl(projectId)}
            disabled={crawlMutation.isPending}
            className="btn-brand"
          >
            {crawlMutation.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Play className="w-4 h-4" />
            )}
            Start Crawling
          </button>
        )}

        {isReady && (
          <button
            onClick={() => downloadMutation.mutate(projectId)}
            disabled={downloadMutation.isPending}
            className="btn-brand"
          >
            {downloadMutation.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Download className="w-4 h-4" />
            )}
            Download Chatbot ZIP
          </button>
        )}

        {processing && (
          <button
            onClick={() => setOverlayOpen(true)}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-brand-500/10 
                       border border-brand-500/20 text-brand-400 font-semibold text-sm 
                       hover:bg-brand-500/15 transition-all active:scale-95"
          >
            <Activity className="w-4 h-4" />
            Open Live Scanner
          </button>
        )}
      </div>

      {/* Two-column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left column */}
        <div className="space-y-6">
          {/* Stats card */}
          <div className="glass p-6 animate-fade-in" style={{ animationDelay: '150ms' }}>
            <h4 className="font-semibold text-gray-100 mb-5">Project Stats</h4>
            <div className="grid grid-cols-2 gap-6">
              <StatBlock
                icon={<FileText className="w-4 h-4" />}
                value={project.pagesFound ?? 0}
                label="Pages Crawled"
              />
              <StatBlock
                icon={<Layers className="w-4 h-4" />}
                value={project.chunksCreated ?? 0}
                label="Text Chunks"
              />
              <StatBlock
                icon={<Clock className="w-4 h-4" />}
                value={formatDate(project.createdAt)}
                label="Created"
              />
              <StatBlock
                icon={<RefreshCw className="w-4 h-4" />}
                value={formatRelativeTime(project.updatedAt)}
                label="Last Update"
              />
            </div>
          </div>

          {/* Pipeline progress */}
          {(processing || project.crawlJob) && (
            <div className="animate-fade-in" style={{ animationDelay: '200ms' }}>
              <CrawlProgress crawlJob={project.crawlJob} status={project.status} />
            </div>
          )}

          {/* Integration guide */}
          {isReady && (
            <div className="glass p-6 animate-fade-in" style={{ animationDelay: '250ms' }}>
              <div className="flex items-center gap-2 mb-4">
                <Code2 className="w-4 h-4 text-brand-400" />
                <h4 className="font-semibold text-gray-100">Integration</h4>
              </div>
              <p className="text-sm text-gray-400 mb-4">
                Add this snippet to your website to embed the chatbot:
              </p>
              <pre className="p-4 rounded-xl bg-surface-100 border border-white/[0.06]
                              text-xs text-brand-400 font-mono overflow-x-auto">
{`<script src="chatbot-widget.js"></script>
<script>
  ChatWidget.init({
    serverUrl: 'YOUR_SERVER_URL'
  });
</script>`}
              </pre>
            </div>
          )}
        </div>

        {/* Right column — Chat */}
        <div className="animate-fade-in" style={{ animationDelay: '300ms' }}>
          <ChatPreview projectId={projectId} isReady={isReady} />
        </div>
      </div>

      {project && (
        <CrawlOverlay
          projectId={projectId}
          projectUrl={project.websiteUrl}
          projectName={project.name}
          isOpen={isOverlayOpen}
          onClose={() => setOverlayOpen(false)}
        />
      )}
    </div>
  );
}

function StatBlock({
  icon,
  value,
  label,
}: {
  icon: React.ReactNode;
  value: string | number;
  label: string;
}) {
  return (
    <div>
      <p className="text-xl font-bold text-brand-400">{value}</p>
      <p className="flex items-center gap-1.5 text-xs text-gray-500 uppercase tracking-wide mt-0.5">
        {icon} {label}
      </p>
    </div>
  );
}
