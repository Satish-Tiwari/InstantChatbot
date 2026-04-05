import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/** Merge Tailwind classes with conflict resolution */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Format a date string to locale display */
export function formatDate(date: string | null | undefined): string {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

/** Format a date string to relative time */
export function formatRelativeTime(date: string | null | undefined): string {
  if (!date) return '-';
  const now = new Date();
  const d = new Date(date);
  const diffMs = now.getTime() - d.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return formatDate(date);
}

/** Status color mapping */
export function getStatusColor(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'text-amber-400 bg-amber-400/10',
    CRAWLING: 'text-blue-400 bg-blue-400/10',
    PROCESSING: 'text-blue-400 bg-blue-400/10',
    EMBEDDING: 'text-purple-400 bg-purple-400/10',
    GENERATING: 'text-purple-400 bg-purple-400/10',
    READY: 'text-emerald-400 bg-emerald-400/10',
    COMPLETED: 'text-emerald-400 bg-emerald-400/10',
    PAUSED: 'text-amber-400 bg-amber-400/10',
    CANCELLED: 'text-red-400 bg-red-400/10',
    FAILED: 'text-red-400 bg-red-400/10',
  };
  return map[status] || 'text-gray-400 bg-gray-400/10';
}

/** Status label mapping */
export function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'Pending',
    CRAWLING: 'Crawling...',
    PROCESSING: 'Processing',
    EMBEDDING: 'Embedding',
    GENERATING: 'Generating',
    READY: 'Ready',
    COMPLETED: 'Finished',
    PAUSED: 'Paused',
    CANCELLED: 'Stopped',
    FAILED: 'Failed',
  };
  return map[status] || status;
}

/** Check if status is a processing state */
export function isProcessing(status: string): boolean {
  return ['CRAWLING', 'PROCESSING', 'EMBEDDING', 'GENERATING'].includes(status);
}

/** Generate a random ID for chat messages */
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}
