import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

interface OAuthCallbackPageProps {
  provider: string;
}

export default function OAuthCallbackPage({ provider }: OAuthCallbackPageProps) {
  const { handleOAuthCallback } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const processCallback = async () => {
      const params = new URLSearchParams(window.location.search);
      const code = params.get('code');
      const state = params.get('state');
      const errorParam = params.get('error');

      if (errorParam) {
        setError('인증이 취소되었습니다.');
        return;
      }

      if (!code || !state) {
        setError('잘못된 인증 응답입니다.');
        return;
      }

      const result = await handleOAuthCallback(provider, code, state);
      if (result.success) {
        window.location.href = '/';
      } else {
        setError(result.message || '인증에 실패했습니다.');
      }
    };

    processCallback();
  }, [provider, handleOAuthCallback]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-bg">
        <div className="bg-white rounded-xl p-6 text-center shadow-sm">
          <div className="w-10 h-10 rounded-full bg-bg text-danger text-xl font-bold flex items-center justify-center mx-auto mb-3">!</div>
          <h2 className="text-base font-semibold text-text mb-1.5">인증 실패</h2>
          <p className="text-[13px] text-text-secondary mb-4">{error}</p>
          <button
            className="py-2 px-5 bg-primary text-white border-none rounded-lg text-[13px] font-medium cursor-pointer"
            onClick={() => (window.location.href = '/')}
          >
            로그인 페이지로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg">
      <div className="bg-white rounded-xl p-6 text-center shadow-sm">
        <div
          className="w-8 h-8 rounded-full mx-auto mb-3"
          style={{ border: '3px solid var(--color-border)', borderTopColor: 'var(--color-primary)', animation: 'spin 1s linear infinite' }}
        />
        <p className="text-[13px] text-text-secondary mb-0">로그인 처리 중...</p>
      </div>
    </div>
  );
}
