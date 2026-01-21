import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User, LoginRequest, SignUpRequest } from '../types/api';
import { authApi, setTokens, clearTokens, getToken } from '../services/api';

// TODO: 임시 개발용 - 인증 없이 API 테스트를 위한 Mock User
const DEV_SKIP_AUTH = true;
const MOCK_USER: User = {
  id: 1,
  email: 'dev@example.com',
  name: '개발자',
  role: 'USER',
  createdAt: new Date().toISOString(),
};

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<{ success: boolean; message?: string }>;
  signUp: (request: SignUpRequest) => Promise<{ success: boolean; message?: string }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      // TODO: 임시 개발용 - 인증 스킵
      if (DEV_SKIP_AUTH) {
        setUser(MOCK_USER);
        setIsLoading(false);
        return;
      }

      const token = getToken();
      if (token) {
        try {
          const response = await authApi.getCurrentUser();
          if (response.success) {
            setUser(response.data);
          } else {
            clearTokens();
          }
        } catch {
          clearTokens();
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (request: LoginRequest) => {
    try {
      const response = await authApi.login(request);
      if (response.success) {
        setTokens(response.data.accessToken, response.data.refreshToken);
        setUser(response.data.user);
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      return { success: false, message: '로그인 중 오류가 발생했습니다.' };
    }
  };

  const signUp = async (request: SignUpRequest) => {
    try {
      const response = await authApi.signUp(request);
      if (response.success) {
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      return { success: false, message: '회원가입 중 오류가 발생했습니다.' };
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
        login,
        signUp,
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
