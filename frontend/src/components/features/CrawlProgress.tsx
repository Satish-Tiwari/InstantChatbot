'use client';

import type { CrawlJob, ProjectStatus } from '@/types';
import { cn, isProcessing } from '@/lib/utils';
import { Loader2, Check, Globe, Cpu, Brain, Bot, CheckCircle2 } from 'lucide-react';

interface CrawlProgressProps {
  crawlJob: CrawlJob | null;
  status: ProjectStatus;
}

const STEPS = [
  { key: 'CRAWLING', label: 'Crawling Website', icon: Globe },
  { key: 'PROCESSING', label: 'Processing Content', icon: Cpu },
  { key: 'EMBEDDING', label: 'Creating Embeddings', icon: Brain },
  { key: 'GENERATING', label: 'Generating Chatbot', icon: Bot },
  { key: 'READY', label: 'Complete', icon: CheckCircle2 },
] as const;

export function CrawlProgress({ crawlJob, status }: CrawlProgressProps) {
  if (!crawlJob && !isProcessing(status)) return null;

  const currentIdx = STEPS.findIndex((s) => s.key === status);

  return (
    <div className="glass p-6">
      <h4 className="font-semibold text-gray-100 mb-5">Pipeline Progress</h4>

      <div className="space-y-4">
        {STEPS.map((step, idx) => {
          const Icon = step.icon;
          const isActive = idx === currentIdx;
          const isDone = idx < currentIdx || status === 'READY';
          const isPending = idx > currentIdx;

          return (
            <div
              key={step.key}
              className={cn(
                'flex items-center gap-4 transition-all duration-500',
                isPending && 'opacity-30'
              )}
            >
              {/* Step icon */}
              <div
                className={cn(
                  'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 transition-all duration-500',
                  isDone && 'bg-emerald-500/15 border-2 border-emerald-500 text-emerald-400',
                  isActive && 'bg-gradient-brand text-white shadow-[0_0_20px_rgba(102,126,234,0.4)]',
                  isPending && 'bg-white/5 border-2 border-white/[0.08] text-gray-500'
                )}
              >
                {isDone ? <Check className="w-4 h-4" /> : <Icon className="w-4 h-4" />}
              </div>

              {/* Step label + progress */}
              <div className="flex-1 min-w-0">
                <p
                  className={cn(
                    'text-sm font-medium transition-colors',
                    isDone && 'text-emerald-400',
                    isActive && 'text-gray-100',
                    isPending && 'text-gray-500'
                  )}
                >
                  {step.label}
                </p>
                {isActive && (
                  <div className="mt-2 h-1.5 bg-white/5 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-brand rounded-full animate-pulse-slow transition-all duration-1000"
                      style={{ width: isDone ? '100%' : '60%' }}
                    />
                  </div>
                )}

                {isActive && step.key === 'CRAWLING' && crawlJob?.currentUrl && (
                  <p className="mt-2 text-[10px] font-mono text-brand-400/80 truncate animate-fade-in max-w-md">
                    Crawling: {crawlJob.currentUrl}
                  </p>
                )}
              </div>

              {/* Spinner for active */}
              {isActive && <Loader2 className="w-5 h-5 text-brand-400 animate-spin flex-shrink-0" />}
            </div>
          );
        })}
      </div>

      {/* Stats row */}
      {crawlJob && (
        <div className="flex gap-8 mt-6 pt-4 border-t border-white/[0.06]">
          <StatMini label="Found" value={crawlJob.pagesFound} />
          <StatMini label="Processed" value={crawlJob.pagesProcessed} />
          <StatMini label="Chunks" value={crawlJob.chunksCreated} />
        </div>
      )}
    </div>
  );
}

function StatMini({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <p className="text-lg font-bold text-brand-400">{value}</p>
      <p className="text-xs text-gray-500 uppercase tracking-wide">{label}</p>
    </div>
  );
}
