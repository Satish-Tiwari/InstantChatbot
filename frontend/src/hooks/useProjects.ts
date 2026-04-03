import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { api, getErrorMessage } from '@/lib/api';
import type { CreateProjectFormValues, Project } from '@/types';

/** Query keys for cache management */
export const projectKeys = {
  all: ['projects'] as const,
  detail: (id: number) => ['projects', id] as const,
  status: (id: number) => ['projects', id, 'status'] as const,
};

/** Fetch all user projects */
export function useProjects() {
  return useQuery({
    queryKey: projectKeys.all,
    queryFn: () => api.getProjects(),
  });
}

/** Fetch a single project */
export function useProject(id: number) {
  return useQuery({
    queryKey: projectKeys.detail(id),
    queryFn: () => api.getProject(id),
    refetchInterval: (query) => {
      const project = query.state.data;
      // Auto-poll every 3 seconds while processing
      if (project && ['CRAWLING', 'PROCESSING', 'EMBEDDING', 'GENERATING'].includes(project.status)) {
        return 3000;
      }
      return false;
    },
  });
}

/** Create a new project */
export function useCreateProject() {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: (data: CreateProjectFormValues) => api.createProject(data),
    onSuccess: (project) => {
      queryClient.invalidateQueries({ queryKey: projectKeys.all });
      toast.success('Project created! 🎉');
      router.push(`/project/${project.id}`);
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}

/** Start crawling a project */
export function useStartCrawl() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (projectId: number) => api.startCrawl(projectId),
    onSuccess: (_, projectId) => {
      queryClient.invalidateQueries({ queryKey: projectKeys.detail(projectId) });
      toast.success('Crawl started! 🕸️');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}

/** Send a chat message */
export function useSendMessage(projectId: number) {
  return useMutation({
    mutationFn: (message: string) => api.sendMessage(projectId, message),
  });
}

/** Download bot ZIP */
export function useDownloadBot() {
  return useMutation({
    mutationFn: async (projectId: number) => {
      const blob = await api.downloadBot(projectId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `chatbot-${projectId}.zip`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    },
    onSuccess: () => {
      toast.success('Download started! 📦');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}
