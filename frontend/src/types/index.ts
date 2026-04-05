// -----------------------------------------------
// Domain Types
// -----------------------------------------------

export type ProjectStatus =
  | 'PENDING'
  | 'CRAWLING'
  | 'PROCESSING'
  | 'EMBEDDING'
  | 'GENERATING'
  | 'READY'
  | 'PAUSED'
  | 'FAILED';

export type CrawlStatus = 'QUEUED' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface User {
  id: number;
  email: string;
  name: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  name: string;
  userId: number;
}

export interface CrawlJob {
  id: number;
  pagesFound: number;
  pagesProcessed: number;
  chunksCreated: number;
  status: CrawlStatus;
  errorMessage: string | null;
  currentUrl: string | null;
  startedAt: string | null;
  completedAt: string | null;
}

export interface Project {
  id: number;
  name: string;
  websiteUrl: string;
  status: ProjectStatus;
  pagesFound: number | null;
  chunksCreated: number | null;
  crawlJob: CrawlJob | null;
  downloadReady: boolean;
  createdAt: string;
  updatedAt: string;
  customAiProvider?: string;
  hasCustomOpenAiKey?: boolean;
  hasCustomAnthropicKey?: boolean;
  hasCustomGoogleKey?: boolean;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'bot';
  content: string;
  sources?: string[];
  timestamp: Date;
}

export interface ChatResponse {
  answer: string;
  sources: string[];
  confidence: number;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}

// -----------------------------------------------
// Form Schemas (used with React Hook Form + Zod)
// -----------------------------------------------

export interface LoginFormValues {
  email: string;
  password: string;
}

export interface RegisterFormValues {
  name: string;
  email: string;
  password: string;
}

export interface CreateProjectFormValues {
  name: string;
  websiteUrl: string;
  customAiProvider?: string;
  customOpenAiApiKey?: string;
  customAnthropicApiKey?: string;
  customGoogleProjectId?: string;
  customGoogleLocation?: string;
}

export interface ChatFormValues {
  message: string;
}
