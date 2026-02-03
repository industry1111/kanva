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
      <div style={styles.container}>
        <div style={styles.card}>
          <div style={styles.errorIcon}>!</div>
          <h2 style={styles.title}>인증 실패</h2>
          <p style={styles.message}>{error}</p>
          <button
            style={styles.button}
            onClick={() => (window.location.href = '/')}
          >
            로그인 페이지로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.spinner} />
        <p style={styles.message}>로그인 처리 중...</p>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f3f4f6',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '2rem',
    textAlign: 'center',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
  },
  spinner: {
    width: '40px',
    height: '40px',
    border: '3px solid #e5e7eb',
    borderTopColor: '#6366f1',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    margin: '0 auto 1rem',
  },
  errorIcon: {
    width: '48px',
    height: '48px',
    borderRadius: '50%',
    backgroundColor: '#fef2f2',
    color: '#ef4444',
    fontSize: '24px',
    fontWeight: 'bold',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    margin: '0 auto 1rem',
  },
  title: {
    fontSize: '1.25rem',
    fontWeight: 600,
    color: '#1f2937',
    margin: '0 0 0.5rem 0',
  },
  message: {
    color: '#6b7280',
    margin: '0 0 1.5rem 0',
  },
  button: {
    padding: '0.75rem 1.5rem',
    backgroundColor: '#6366f1',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '0.9rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
};
