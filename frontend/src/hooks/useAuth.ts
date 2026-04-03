import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { api, getErrorMessage } from '@/lib/api';
import { useAppDispatch } from '@/store/hooks';
import { setCredentials, logout as logoutAction } from '@/store/slices/authSlice';
import type { LoginFormValues, RegisterFormValues, User } from '@/types';

export function useLogin() {
  const dispatch = useAppDispatch();
  const router = useRouter();

  return useMutation({
    mutationFn: (data: LoginFormValues) => api.login(data),
    onSuccess: (data) => {
      const user: User = { id: data.userId, email: data.email, name: data.name };
      dispatch(setCredentials({ user, token: data.token }));
      toast.success('Welcome back! 👋');
      router.push('/dashboard');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}

export function useRegister() {
  const dispatch = useAppDispatch();
  const router = useRouter();

  return useMutation({
    mutationFn: (data: RegisterFormValues) => api.register(data),
    onSuccess: (data) => {
      const user: User = { id: data.userId, email: data.email, name: data.name };
      dispatch(setCredentials({ user, token: data.token }));
      toast.success('Account created! 🎉');
      router.push('/dashboard');
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}

export function useLogout() {
  const dispatch = useAppDispatch();
  const router = useRouter();

  return () => {
    dispatch(logoutAction());
    router.push('/login');
    toast.success('Logged out');
  };
}
