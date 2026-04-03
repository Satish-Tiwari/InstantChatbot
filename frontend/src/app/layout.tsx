import type { Metadata } from 'next';
import { Toaster } from 'react-hot-toast';
import { ReduxProvider } from '@/providers/redux-provider';
import { QueryProvider } from '@/providers/query-provider';
import './globals.css';

export const metadata: Metadata = {
  title: 'Instant Chatbot — AI Website Assistant',
  description:
    'Transform any website into an AI-powered chatbot in minutes. Crawl, train, and deploy — no code required.',
  keywords: ['AI chatbot', 'website chatbot', 'RAG', 'chatbot generator', 'AI assistant'],
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen antialiased overflow-x-hidden">
        <ReduxProvider>
          <QueryProvider>
            {children}
            <Toaster
              position="top-right"
              toastOptions={{
                duration: 4000,
                style: {
                  background: 'rgba(20, 20, 50, 0.95)',
                  color: '#f0f0ff',
                  border: '1px solid rgba(100, 100, 180, 0.15)',
                  backdropFilter: 'blur(20px)',
                  borderRadius: '12px',
                  fontSize: '14px',
                },
              }}
            />
          </QueryProvider>
        </ReduxProvider>
      </body>
    </html>
  );
}
