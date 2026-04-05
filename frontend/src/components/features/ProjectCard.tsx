'use client';

import Link from 'next/link';
import type { Project } from '@/types';
import { cn, getStatusColor, getStatusLabel, isProcessing, formatDate } from '@/lib/utils';
import { Globe, FileText, Layers, Loader2, Trash2, AlertCircle } from 'lucide-react';
import { useDeleteProject } from '@/hooks/useProjects';
import { useState } from 'react';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';

interface ProjectCardProps {
  project: Project;
}
export function ProjectCard({ project }: ProjectCardProps) {
  const processing = isProcessing(project.status);
  const deleteMutation = useDeleteProject();
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const handleDelete = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsConfirmOpen(true);
  };

  const confirmDelete = () => {
    deleteMutation.mutate(project.id, {
      onSuccess: () => setIsConfirmOpen(false)
    });
  };

  return (
    <>
      <Link href={`/project/${project.id}`} className="block relative group">
        <div className="glass glass-hover p-6 cursor-pointer">
          {/* Header */}
          <div className="flex items-start justify-between mb-3">
            <div className="flex-1 min-w-0 pr-4">
              <h3 className="font-semibold text-lg text-gray-100 group-hover:text-brand-400
                             transition-colors line-clamp-1">
                {project.name}
              </h3>
              <span
                className={cn(
                  'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider mt-2',
                  getStatusColor(project.status)
                )}
              >
                {processing && <Loader2 className="w-3 h-3 animate-spin" />}
                {getStatusLabel(project.status)}
              </span>
              {(project.hasCustomOpenAiKey || project.hasCustomAnthropicKey || project.hasCustomGoogleKey) && (
                <span className="inline-flex items-center gap-1 ml-2 px-2 py-0.5 rounded bg-amber-500/10 text-amber-500 text-[9px] font-bold uppercase tracking-widest border border-amber-500/20">
                  Custom AI
                </span>
              )}
            </div>
            
            <button
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              className="p-2 rounded-xl bg-red-500/10 text-red-400 hover:bg-red-500/20 
                         transition-all active:scale-95 disabled:opacity-50"
              title="Delete Project"
            >
              <Trash2 className="w-4 h-4" />
            </button>
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

      <ConfirmDialog
        isOpen={isConfirmOpen}
        onClose={() => setIsConfirmOpen(false)}
        onConfirm={confirmDelete}
        title="Destroy Project Artifacts?"
        message={`Are you sure you want to delete "${project.name}"? This action is irreversible and will purge all collected knowledge, embeddings, and chat history.`}
        confirmText="Confirm Deletion"
        variant="danger"
        isLoading={deleteMutation.isPending}
      />
    </>
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
