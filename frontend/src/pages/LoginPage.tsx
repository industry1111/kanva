import { useState, type FormEvent } from 'react';
import { useAuth } from '../contexts/AuthContext';

type LoadingProvider = 'github' | 'slack' | null;
type AuthMode = 'login' | 'signup';

export default function LoginPage() {
  const { login, signUp, loginWithOAuth } = useAuth();
  const [mode, setMode] = useState<AuthMode>('login');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadingProvider, setLoadingProvider] = useState<LoadingProvider>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!name.trim() || !password) {
      setError('닉네임과 비밀번호를 입력해주세요.');
      return;
    }

    if (mode === 'signup') {
      if (name.trim().length < 2) {
        setError('닉네임은 2자 이상이어야 합니다.');
        return;
      }
      if (password.length < 8) {
        setError('비밀번호는 8자 이상이어야 합니다.');
        return;
      }
      if (password !== confirmPassword) {
        setError('비밀번호가 일치하지 않습니다.');
        return;
      }
    }

    setIsSubmitting(true);
    try {
      const result = mode === 'login'
        ? await login(name.trim(), password)
        : await signUp(name.trim(), password);

      if (!result.success) {
        setError(result.message || '오류가 발생했습니다.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleOAuthLogin = async (provider: 'github' | 'slack') => {
    setLoadingProvider(provider);
    await loginWithOAuth(provider);
  };

  const switchMode = () => {
    setMode(mode === 'login' ? 'signup' : 'login');
    setError('');
    setConfirmPassword('');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-[320px] border border-border">
        <h1 className="text-2xl font-bold text-center text-text m-0 tracking-tight">Kanva</h1>
        <p className="text-center text-text-secondary mt-1.5 mb-0 text-[13px]">Personal Productivity Management</p>

        <div className="h-px bg-border my-4" />

        <form onSubmit={handleSubmit} className="flex flex-col gap-2.5">
          <div className="flex flex-col gap-2">
            <input
              type="text"
              placeholder="닉네임"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full py-2 px-2.5 bg-white text-text border border-border rounded-lg text-[13px] outline-none box-border"
              disabled={isSubmitting}
              autoComplete="username"
            />
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full py-2 px-2.5 bg-white text-text border border-border rounded-lg text-[13px] outline-none box-border"
              disabled={isSubmitting}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
            {mode === 'signup' && (
              <input
                type="password"
                placeholder="비밀번호 확인"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full py-2 px-2.5 bg-white text-text border border-border rounded-lg text-[13px] outline-none box-border"
                disabled={isSubmitting}
                autoComplete="new-password"
              />
            )}
          </div>

          {error && <p className="text-danger text-[13px] mt-0.5 mb-0 text-center">{error}</p>}

          <button
            type="submit"
            className="w-full py-2 px-2.5 bg-primary text-white border-none rounded-lg text-[13px] font-semibold cursor-pointer transition-opacity"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? (mode === 'login' ? '로그인 중...' : '가입 중...')
              : (mode === 'login' ? '로그인' : '회원가입')
            }
          </button>

          <button
            type="button"
            className="bg-transparent border-none text-primary text-[13px] cursor-pointer p-0.5 text-center"
            onClick={switchMode}
            disabled={isSubmitting}
          >
            {mode === 'login'
              ? '계정이 없으신가요? 회원가입'
              : '이미 계정이 있으신가요? 로그인'
            }
          </button>
        </form>

        <div className="flex items-center gap-2.5 my-3.5">
          <div className="flex-1 h-px bg-border" />
          <span className="text-text-secondary text-xs">또는</span>
          <div className="flex-1 h-px bg-border" />
        </div>

        <div className="flex gap-2.5">
          <button
            className="flex-1 py-1.5 px-2.5 bg-white text-text border border-border rounded-lg text-[13px] font-semibold cursor-pointer flex items-center justify-center gap-1.5 transition-opacity"
            onClick={() => handleOAuthLogin('github')}
            disabled={loadingProvider !== null || isSubmitting}
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            {loadingProvider === 'github' ? '...' : 'GitHub'}
          </button>

          <button
            className="flex-1 py-1.5 px-2.5 bg-text text-white border-none rounded-lg text-[13px] font-semibold cursor-pointer flex items-center justify-center gap-1.5 transition-opacity"
            onClick={() => handleOAuthLogin('slack')}
            disabled={loadingProvider !== null || isSubmitting}
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M5.042 15.165a2.528 2.528 0 0 1-2.52 2.523A2.528 2.528 0 0 1 0 15.165a2.527 2.527 0 0 1 2.522-2.52h2.52v2.52zM6.313 15.165a2.527 2.527 0 0 1 2.521-2.52 2.527 2.527 0 0 1 2.521 2.52v6.313A2.528 2.528 0 0 1 8.834 24a2.528 2.528 0 0 1-2.521-2.522v-6.313zM8.834 5.042a2.528 2.528 0 0 1-2.521-2.52A2.528 2.528 0 0 1 8.834 0a2.528 2.528 0 0 1 2.521 2.522v2.52H8.834zM8.834 6.313a2.528 2.528 0 0 1 2.521 2.521 2.528 2.528 0 0 1-2.521 2.521H2.522A2.528 2.528 0 0 1 0 8.834a2.528 2.528 0 0 1 2.522-2.521h6.312zM18.956 8.834a2.528 2.528 0 0 1 2.522-2.521A2.528 2.528 0 0 1 24 8.834a2.528 2.528 0 0 1-2.522 2.521h-2.522V8.834zM17.688 8.834a2.528 2.528 0 0 1-2.523 2.521 2.527 2.527 0 0 1-2.52-2.521V2.522A2.527 2.527 0 0 1 15.165 0a2.528 2.528 0 0 1 2.523 2.522v6.312zM15.165 18.956a2.528 2.528 0 0 1 2.523 2.522A2.528 2.528 0 0 1 15.165 24a2.527 2.527 0 0 1-2.52-2.522v-2.522h2.52zM15.165 17.688a2.527 2.527 0 0 1-2.52-2.523 2.526 2.526 0 0 1 2.52-2.52h6.313A2.527 2.527 0 0 1 24 15.165a2.528 2.528 0 0 1-2.522 2.523h-6.313z"/>
            </svg>
            {loadingProvider === 'slack' ? '...' : 'Slack'}
          </button>
        </div>
      </div>
    </div>
  );
}
