'use client';

import Link from 'next/link';
import type { Project } from '@/types';
import { cn, getStatusColor, getStatusLabel, isProcessing, formatDate } from '@/lib/utils';
import { Globe, FileText, Layers, Loader2 } from 'lucide-react';

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const processing = isProcessing(project.status);

  return (
    <Link href={`/project/${project.id}`}>
      <div className="glass glass-hover p-6 cursor-pointer group">
        {/* Header */}
        <div className="flex items-start justify-between mb-3">
          <h3 className="font-semibold text-lg text-gray-100 group-hover:text-brand-400
                         transition-colors line-clamp-1">
            {project.name}
          </h3>
          <span
            className={cn(
              'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wide',
              getStatusColor(project.status)
            )}
          >
            {processing && <Loader2 className="w-3 h-3 animate-spin" />}
            {getStatusLabel(project.status)}
          </span>
        </div>

        {/* URL */}
        <p className="flex items-center gap-2 text-sm text-gray-500 mb-4 truncate">
          <Globe className="w-3.5 h-3.5 flex-shrink-0" />
          {project.websiteUrl}
        </p>

        {/* Stats */}
        <div className="flex gap-6 pt-4 border-t border-white/[0.06]">
          <Stat
            icon={<FileText className="w-3.5 h-3.5" />}
            value={project.pagesFound ?? 0}
            label="Pages"
          />
          <Stat
            icon={<Layers className="w-3.5 h-3.5" />}
            value={project.chunksCreated ?? 0}
            label="Chunks"
          />
          <div className="ml-auto text-right">
            <p className="text-xs text-gray-500">Created</p>
            <p className="text-sm text-gray-400">{formatDate(project.createdAt)}</p>
          </div>
        </div>
      </div>
    </Link>
  );
}

function Stat({
  icon,
  value,
  label,
}: {
  icon: React.ReactNode;
  value: number;
  label: string;
}) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xl font-bold text-brand-400">{value}</span>
      <span className="flex items-center gap-1 text-xs text-gray-500 uppercase tracking-wide">
        {icon} {label}
      </span>
    </div>
  );
}
