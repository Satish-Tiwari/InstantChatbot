'use client';

import { useState } from 'react';
import { useProjects } from '@/hooks/useProjects';
import { ProjectCard } from '@/components/features/ProjectCard';
import { CreateProjectModal } from '@/components/features/CreateProjectModal';
import { Plus, Bot, Loader2 } from 'lucide-react';

export default function DashboardPage() {
  const { data: projects, isLoading, isError } = useProjects();
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <div className="max-w-7xl mx-auto px-6 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8 animate-fade-in">
        <div>
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-gray-400 mt-1">Manage your AI chatbot projects</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="btn-brand"
        >
          <Plus className="w-4 h-4" />
          New Project
        </button>
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="flex items-center justify-center py-32">
          <Loader2 className="w-10 h-10 text-brand-500 animate-spin" />
        </div>
      ) : isError ? (
        <div className="text-center py-32 animate-fade-in">
          <p className="text-red-400 text-lg mb-2">Failed to load projects</p>
          <p className="text-gray-500 text-sm">Check your connection and try again.</p>
        </div>
      ) : projects && projects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {projects.map((project, i) => (
            <div
              key={project.id}
              className="animate-fade-in"
              style={{ animationDelay: `${i * 60}ms` }}
            >
              <ProjectCard project={project} />
            </div>
          ))}
        </div>
      ) : (
        /* Empty State */
        <div className="text-center py-32 animate-fade-in">
          <div className="w-20 h-20 rounded-2xl bg-surface-50 flex items-center justify-center
                          mx-auto mb-6 text-gray-500">
            <Bot className="w-10 h-10" />
          </div>
          <h3 className="text-xl font-semibold text-gray-300 mb-2">No projects yet</h3>
          <p className="text-gray-500 mb-8 max-w-sm mx-auto">
            Create your first AI chatbot by entering a website URL.
          </p>
          <button
            onClick={() => setIsModalOpen(true)}
            className="btn-brand"
          >
            <Plus className="w-4 h-4" />
            Create Your First Project
          </button>
        </div>
      )}

      {/* Modal */}
      <CreateProjectModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </div>
  );
}
