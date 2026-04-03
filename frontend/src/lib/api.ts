import axios, { AxiosError, type AxiosInstance } from 'axios';
import type {
  AuthResponse,
  Project,
  ChatResponse,
  LoginFormValues,
  RegisterFormValues,
  CreateProjectFormValues,
} from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_URL,
      headers: { 'Content-Type': 'application/json' },
    });

    // Request interceptor — attach JWT
    this.client.interceptors.request.use((config) => {
      if (typeof window !== 'undefined') {
        const token = localStorage.getItem('chatgen_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
      return config;
    });

    // Response interceptor — handle 401
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401 && typeof window !== 'undefined') {
          localStorage.removeItem('chatgen_token');
          localStorage.removeItem('chatgen_user');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // ── Auth ──
  async register(data: RegisterFormValues): Promise<AuthResponse> {
    const res = await this.client.post<AuthResponse>('/api/auth/register', data);
    return res.data;
  }

  async login(data: LoginFormValues): Promise<AuthResponse> {
    const res = await this.client.post<AuthResponse>('/api/auth/login', data);
    return res.data;
  }

  async getMe(): Promise<{ id: number; email: string; name: string }> {
    const res = await this.client.get('/api/auth/me');
    return res.data;
  }

  // ── Projects ──
  async getProjects(): Promise<Project[]> {
    const res = await this.client.get<Project[]>('/api/projects');
    return res.data;
  }

  async getProject(id: number): Promise<Project> {
    const res = await this.client.get<Project>(`/api/projects/${id}`);
    return res.data;
  }

  async createProject(data: CreateProjectFormValues): Promise<Project> {
    const res = await this.client.post<Project>('/api/projects', data);
    return res.data;
  }

  // ── Crawl ──
  async startCrawl(projectId: number): Promise<{ message: string }> {
    const res = await this.client.post(`/api/projects/${projectId}/crawl`);
    return res.data;
  }

  async getProjectStatus(projectId: number): Promise<Project> {
    const res = await this.client.get<Project>(`/api/projects/${projectId}/status`);
    return res.data;
  }

  // ── Chat ──
  async sendMessage(projectId: number, message: string): Promise<ChatResponse> {
    const res = await this.client.post<ChatResponse>(
      `/api/projects/${projectId}/chat`,
      { message }
    );
    return res.data;
  }

  // ── Download ──
  async downloadBot(projectId: number): Promise<Blob> {
    const res = await this.client.get(`/api/projects/${projectId}/download`, {
      responseType: 'blob',
    });
    return res.data;
  }
}

export const api = new ApiClient();

/** Extract error message from Axios errors */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || error.message || 'An error occurred';
  }
  if (error instanceof Error) return error.message;
  return 'An unexpected error occurred';
}
