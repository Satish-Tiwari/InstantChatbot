'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createProjectSchema, type CreateProjectSchema } from '@/lib/validations';
import { useCreateProject } from '@/hooks/useProjects';
import { X, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

interface CreateProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function CreateProjectModal({ isOpen, onClose }: CreateProjectModalProps) {
  const createMutation = useCreateProject();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isValid },
  } = useForm<CreateProjectSchema>({
    resolver: zodResolver(createProjectSchema),
    mode: 'onChange',
  });

  const onSubmit = async (data: CreateProjectSchema) => {
    await createMutation.mutateAsync(data);
    reset();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm animate-fade-in"
      onClick={onClose}
    >
      <div
        className="glass p-8 w-full max-w-md animate-slide-up"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-100">New Project</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-200 transition-colors p-1"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form — React Hook Form + Zod */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          {/* Name */}
          <div>
            <label htmlFor="project-name" className="block text-sm font-medium text-gray-400 mb-1.5">
              Project Name
            </label>
            <input
              id="project-name"
              {...register('name')}
              className={cn('input-field', errors.name && 'input-error')}
              placeholder="My Website Chatbot"
            />
            {errors.name && (
              <p className="mt-1.5 text-xs text-red-400">{errors.name.message}</p>
            )}
          </div>

          {/* URL */}
          <div>
            <label htmlFor="project-url" className="block text-sm font-medium text-gray-400 mb-1.5">
              Website URL
            </label>
            <input
              id="project-url"
              {...register('websiteUrl')}
              className={cn('input-field', errors.websiteUrl && 'input-error')}
              placeholder="https://example.com"
            />
            {errors.websiteUrl ? (
              <p className="mt-1.5 text-xs text-red-400">{errors.websiteUrl.message}</p>
            ) : (
              <p className="mt-1.5 text-xs text-gray-500">
                We&apos;ll crawl this site to train your chatbot
              </p>
            )}
          </div>

          {/* Advanced AI Settings Toggle */}
          <div className="pt-2">
            <button
              type="button"
              onClick={() => {
                const el = document.getElementById('advanced-ai-settings');
                if (el) el.classList.toggle('hidden');
              }}
              className="text-sm font-medium text-brand-400 hover:text-brand-300 transition-colors flex items-center gap-1"
            >
              Advanced AI Settings (Optional)
            </button>
            <div id="advanced-ai-settings" className="hidden space-y-4 mt-4 p-4 rounded-xl border border-gray-800 bg-black/20">
              <p className="text-xs text-gray-500 mb-2">
                Provide your own API keys to avoid global rate limits and use specific models.
              </p>
              
              {/* OpenAI Key */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">OpenAI API Key</label>
                <input
                  {...register('customOpenAiApiKey')}
                  type="password"
                  className="input-field py-1.5 text-sm"
                  placeholder="sk-..."
                />
              </div>

              {/* Anthropic Key */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Anthropic API Key</label>
                <input
                  {...register('customAnthropicApiKey')}
                  type="password"
                  className="input-field py-1.5 text-sm"
                  placeholder="sk-ant-..."
                />
              </div>

              {/* Google Vertex */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Google Project ID</label>
                  <input
                    {...register('customGoogleProjectId')}
                    className="input-field py-1.5 text-sm"
                    placeholder="my-project-id"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Google Location</label>
                  <input
                    {...register('customGoogleLocation')}
                    className="input-field py-1.5 text-sm"
                    placeholder="us-central1"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-4 border-t border-gray-800/50">
            <button type="button" onClick={onClose} className="btn-ghost flex-1">
              Cancel
            </button>
            <button
              type="submit"
              className="btn-brand flex-1"
              disabled={createMutation.isPending || !isValid}
            >
              {createMutation.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                'Create Project'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
