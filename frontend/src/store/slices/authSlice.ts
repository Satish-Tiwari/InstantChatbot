import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { User } from '@/types';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isHydrated: boolean;
}

const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  isHydrated: false,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{ user: User; token: string }>
    ) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;

      if (typeof window !== 'undefined') {
        localStorage.setItem('chatgen_token', action.payload.token);
        localStorage.setItem('chatgen_user', JSON.stringify(action.payload.user));
      }
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;

      if (typeof window !== 'undefined') {
        localStorage.removeItem('chatgen_token');
        localStorage.removeItem('chatgen_user');
      }
    },
    hydrate: (state) => {
      if (typeof window !== 'undefined') {
        const token = localStorage.getItem('chatgen_token');
        const userStr = localStorage.getItem('chatgen_user');

        if (token && userStr) {
          try {
            state.user = JSON.parse(userStr);
            state.token = token;
            state.isAuthenticated = true;
          } catch {
            state.isAuthenticated = false;
          }
        }
      }
      state.isHydrated = true;
    },
  },
});

export const { setCredentials, logout, hydrate } = authSlice.actions;
export default authSlice.reducer;
