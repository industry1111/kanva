import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User, OAuthCallbackRequest } from '../types/api';
import { authApi, setTokens, clearTokens, getToken } from '../services/api';

const OAUTH_STATE_KEY = 'kanva_oauth_state';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  loginWithOAuth: (provider: string) => Promise<void>;
  handleOAuthCallback: (provider: string, code: string, state: string) => Promise<{ success: boolean; message?: string }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = getToken();
      if (token) {
        try {
          const response = await authApi.getCurrentUser();
          if (response.success) {
            setUser(response.data);
            setIsLoading(false);
            return;
          } else {
            clearTokens();
          }
        } catch {
          clearTokens();
        }
      }

      // 개발 모드: 토큰 없으면 자동 로그인 시도
      if (import.meta.env.DEV) {
        try {
          const response = await authApi.devLogin();
          if (response.success) {
            setTokens(response.data.accessToken, response.data.refreshToken);
            setUser(response.data.user);
          }
        } catch {
          // dev-login 엔드포인트 없으면 무시 (prod 백엔드 연결 시)
        }
      }

      setIsLoading(false);
    };

    initAuth();
  }, []);

  const loginWithOAuth = async (provider: string) => {
    try {
      const response = await authApi.getOAuthLoginUrl(provider);
      if (response.success) {
        sessionStorage.setItem(OAUTH_STATE_KEY, response.data.state);
        window.location.href = response.data.url;
      }
    } catch (error) {
      console.error('OAuth 로그인 URL 조회 실패:', error);
    }
  };

  const handleOAuthCallback = async (provider: string, code: string, state: string) => {
    const savedState = sessionStorage.getItem(OAUTH_STATE_KEY);
    sessionStorage.removeItem(OAUTH_STATE_KEY);

    if (savedState !== state) {
      return { success: false, message: '유효하지 않은 인증 요청입니다.' };
    }

    try {
      const request: OAuthCallbackRequest = { code, state };
      const response = await authApi.oauthCallback(provider, request);
      if (response.success) {
        setTokens(response.data.accessToken, response.data.refreshToken);
        setUser(response.data.user);
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      return { success: false, message: 'OAuth 인증 처리 중 오류가 발생했습니다.' };
    }
  };

  const logout = () => {
    clearTokens();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        loginWithOAuth,
        handleOAuthCallback,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
