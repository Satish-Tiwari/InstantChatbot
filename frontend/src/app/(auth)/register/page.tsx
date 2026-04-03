'use client';

import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerSchema, type RegisterSchema } from '@/lib/validations';
import { useRegister } from '@/hooks/useAuth';
import { cn } from '@/lib/utils';
import { Rocket, Loader2 } from 'lucide-react';

export default function RegisterPage() {
  const registerMutation = useRegister();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<RegisterSchema>({
    resolver: zodResolver(registerSchema),
    mode: 'onChange',
  });

  const onSubmit = (data: RegisterSchema) => {
    registerMutation.mutate(data);
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-6">
      <div className="glass p-8 w-full max-w-md animate-fade-in">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-gradient-brand flex items-center justify-center
                          mx-auto mb-4 shadow-[0_0_30px_rgba(102,126,234,0.3)]">
            <Rocket className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-2xl font-bold mb-1">Get Started</h1>
          <p className="text-sm text-gray-400">Create your Instant Chatbot account</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-400 mb-1.5">
              Full Name
            </label>
            <input
              id="name"
              type="text"
              autoComplete="name"
              {...register('name')}
              className={cn('input-field', errors.name && 'input-error')}
              placeholder="John Doe"
            />
            {errors.name && (
              <p className="mt-1.5 text-xs text-red-400">{errors.name.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-400 mb-1.5">
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              {...register('email')}
              className={cn('input-field', errors.email && 'input-error')}
              placeholder="you@example.com"
            />
            {errors.email && (
              <p className="mt-1.5 text-xs text-red-400">{errors.email.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-400 mb-1.5">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              {...register('password')}
              className={cn('input-field', errors.password && 'input-error')}
              placeholder="Min 6 characters"
            />
            {errors.password && (
              <p className="mt-1.5 text-xs text-red-400">{errors.password.message}</p>
            )}
          </div>

          <button
            type="submit"
            className="btn-brand w-full !py-3.5"
            disabled={registerMutation.isPending || !isValid}
          >
            {registerMutation.isPending ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              'Create Account'
            )}
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Already have an account?{' '}
          <Link href="/login" className="text-brand-400 font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
