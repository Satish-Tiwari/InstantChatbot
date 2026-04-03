'use client';

import Link from 'next/link';
import { useAppSelector } from '@/store/hooks';
import { useLogout } from '@/hooks/useAuth';
import { useEffect, useState } from 'react';
import { hydrate } from '@/store/slices/authSlice';
import { useAppDispatch } from '@/store/hooks';
import { LogOut, LayoutDashboard, Zap } from 'lucide-react';

export function Navbar() {
  const dispatch = useAppDispatch();
  const { isAuthenticated, user, isHydrated } = useAppSelector((s) => s.auth);
  const handleLogout = useLogout();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    dispatch(hydrate());
    setMounted(true);
  }, [dispatch]);

  if (!mounted) return null;

  return (
    <nav className="sticky top-0 z-50 border-b border-white/[0.06] bg-surface/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link
          href={isAuthenticated ? '/dashboard' : '/'}
          className="flex items-center gap-2.5 group"
        >
          <div className="w-8 h-8 rounded-lg bg-gradient-brand flex items-center justify-center
                          group-hover:shadow-[0_0_20px_rgba(102,126,234,0.4)] transition-shadow duration-300">
            <Zap className="w-4 h-4 text-white" />
          </div>
          <span className="text-xl font-bold bg-gradient-to-r from-brand-400 to-purple-400
                           bg-clip-text text-transparent">
            Instant Chatbot
          </span>
        </Link>

        {/* Actions */}
        <div className="flex items-center gap-3">
          {isHydrated && isAuthenticated ? (
            <>
              <Link
                href="/dashboard"
                className="btn-ghost !px-4 !py-2 text-sm"
              >
                <LayoutDashboard className="w-4 h-4" />
                Dashboard
              </Link>

              <div className="flex items-center gap-3 ml-2">
                <div className="w-8 h-8 rounded-full bg-gradient-brand flex items-center
                                justify-center text-white text-sm font-bold">
                  {user?.name?.charAt(0).toUpperCase()}
                </div>
                <span className="text-sm text-gray-400 hidden sm:block">
                  {user?.name}
                </span>
              </div>

              <button
                onClick={handleLogout}
                className="btn-ghost !px-3 !py-2 text-sm !text-gray-400
                           hover:!text-red-400 hover:!border-red-500/30"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn-ghost !px-4 !py-2 text-sm">
                Sign In
              </Link>
              <Link href="/register" className="btn-brand !px-4 !py-2 text-sm">
                Get Started
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
