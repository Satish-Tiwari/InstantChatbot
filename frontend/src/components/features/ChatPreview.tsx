'use client';

import { useRef, useEffect, useState, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { chatSchema, type ChatSchema } from '@/lib/validations';
import { useSendMessage } from '@/hooks/useProjects';
import type { ChatMessage } from '@/types';
import { generateId } from '@/lib/utils';
import { Send, Loader2, MessageSquare } from 'lucide-react';

interface ChatPreviewProps {
  projectId: number;
  isReady: boolean;
}

export function ChatPreview({ projectId, isReady }: ChatPreviewProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: generateId(),
      role: 'bot',
      content: "Hi! I'm your AI assistant. Ask me anything about the website! 💬",
      timestamp: new Date(),
    },
  ]);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const sendMutation = useSendMessage(projectId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<ChatSchema>({
    resolver: zodResolver(chatSchema),
    mode: 'onChange',
  });

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(scrollToBottom, [messages, scrollToBottom]);

  const onSubmit = async (data: ChatSchema) => {
    if (!isReady) return;

    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content: data.message,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMsg]);
    reset();

    try {
      const response = await sendMutation.mutateAsync(data.message);
      const botMsg: ChatMessage = {
        id: generateId(),
        role: 'bot',
        content: response.answer,
        sources: response.sources,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, botMsg]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: generateId(),
          role: 'bot',
          content: 'Sorry, something went wrong. Please try again.',
          timestamp: new Date(),
        },
      ]);
    }
  };

  return (
    <div className="flex flex-col h-[500px] rounded-2xl overflow-hidden border border-white/[0.06]">
      {/* Header */}
      <div className="px-5 py-3.5 bg-gradient-brand flex items-center gap-2">
        <MessageSquare className="w-4 h-4 text-white" />
        <span className="text-white font-semibold text-sm">Chat Preview</span>
        {!isReady && (
          <span className="ml-auto text-xs text-white/60">Awaiting processing...</span>
        )}
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-surface-200">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            <div
              className={`max-w-[80%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed animate-fade-in ${
                msg.role === 'user'
                  ? 'bg-gradient-brand text-white rounded-br-md'
                  : 'glass text-gray-200 rounded-bl-md'
              }`}
            >
              <p>{msg.content}</p>
              {msg.sources && msg.sources.length > 0 && (
                <div className="mt-2 pt-2 border-t border-white/10 text-[11px] text-gray-400">
                  Sources: {msg.sources.join(', ')}
                </div>
              )}
            </div>
          </div>
        ))}

        {sendMutation.isPending && (
          <div className="flex justify-start">
            <div className="glass px-4 py-3 rounded-2xl rounded-bl-md">
              <div className="flex gap-1.5">
                <div className="w-2 h-2 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                <div className="w-2 h-2 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                <div className="w-2 h-2 bg-brand-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input — React Hook Form */}
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex gap-3 p-3 bg-surface border-t border-white/[0.06]"
      >
        <input
          {...register('message')}
          className="input-field !py-2.5 text-sm"
          placeholder={isReady ? 'Ask about the website...' : 'Chat available after processing...'}
          disabled={!isReady || sendMutation.isPending}
          autoComplete="off"
        />
        <button
          type="submit"
          className="btn-brand !px-4 !py-2.5 !rounded-xl"
          disabled={!isReady || sendMutation.isPending || !isValid}
        >
          {sendMutation.isPending ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Send className="w-4 h-4" />
          )}
        </button>
      </form>
    </div>
  );
}
