'use client';

import { motion, AnimatePresence } from 'framer-motion';
import { AlertCircle, X } from 'lucide-react';
import { cn } from '@/lib/utils';
import { createPortal } from 'react-dom';
import { useState, useEffect } from 'react';

interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'danger' | 'info';
  isLoading?: boolean;
}

export function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'info',
  isLoading = false,
}: ConfirmDialogProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) return null;

  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 overflow-x-hidden overflow-y-auto outline-none focus:outline-none">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          />

          {/* Dialog Content */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            className="relative w-full max-w-xl bg-surface-200 border border-white/[0.08] rounded-[2.5rem] shadow-2xl overflow-hidden p-10 flex flex-col items-center text-center mx-auto"
          >
            <button
              onClick={onClose}
              className="absolute top-6 right-6 p-2 rounded-xl text-gray-500 hover:text-white hover:bg-white/5 transition-all"
            >
              <X className="w-5 h-5" />
            </button>

            <div className={cn(
              "w-20 h-20 rounded-3xl flex items-center justify-center mb-6 shadow-2xl relative group",
              variant === 'danger' ? "bg-red-500/10 text-red-500" : "bg-brand-500/10 text-brand-500"
            )}>
              <div className={cn(
                "absolute inset-0 blur-2xl opacity-40 rounded-full",
                variant === 'danger' ? "bg-red-500" : "bg-brand-500"
              )} />
              <AlertCircle className="w-10 h-10 relative z-10" />
            </div>

            <h3 className="text-3xl font-extrabold text-white mb-4 tracking-tight leading-tight">
              {title}
            </h3>
            <p className="text-gray-400 text-base leading-relaxed mb-10 max-w-[85%]">
              {message}
            </p>

            <div className="flex items-center gap-4 w-full max-w-md">
              <button
                onClick={onClose}
                disabled={isLoading}
                className="flex-1 px-8 py-3.5 rounded-2xl border border-white/10 text-gray-400 font-bold text-sm
                           hover:bg-white/5 hover:text-white transition-all disabled:opacity-50 active:scale-95"
              >
                {cancelText}
              </button>
              <button
                onClick={() => {
                  onConfirm();
                }}
                disabled={isLoading}
                className={cn(
                  "flex-1 px-8 py-3.5 rounded-2xl font-bold text-sm shadow-xl transition-all active:scale-95 disabled:opacity-50",
                  variant === 'danger'
                    ? "bg-red-500 text-white shadow-red-500/30 hover:bg-red-600"
                    : "bg-brand-500 text-white shadow-brand-500/30 hover:bg-brand-600"
                )}
              >
                {isLoading ? 'Processing...' : confirmText}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>,
    document.body
  );
}
