import { z } from 'zod';

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters'),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters')
    .max(100, 'Password must be less than 100 characters'),
});

export const createProjectSchema = z.object({
  name: z
    .string()
    .min(1, 'Project name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(200, 'Name must be less than 200 characters'),
  websiteUrl: z
    .string()
    .min(1, 'Website URL is required')
    .url('Must be a valid URL')
    .refine((url) => url.startsWith('http://') || url.startsWith('https://'), {
      message: 'URL must start with http:// or https://',
    }),
});

export const chatSchema = z.object({
  message: z
    .string()
    .min(1, 'Message is required')
    .max(2000, 'Message must be less than 2000 characters'),
});

export type LoginSchema = z.infer<typeof loginSchema>;
export type RegisterSchema = z.infer<typeof registerSchema>;
export type CreateProjectSchema = z.infer<typeof createProjectSchema>;
export type ChatSchema = z.infer<typeof chatSchema>;
