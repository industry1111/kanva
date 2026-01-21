import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

type AuthMode = 'login' | 'signup';

export default function LoginPage() {
  const { login, signUp } = useAuth();
  const [mode, setMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      if (mode === 'login') {
        const result = await login({ email, password });
        if (!result.success) {
          setError(result.message || '로그인에 실패했습니다.');
        }
      } else {
        const result = await signUp({ email, password, name });
        if (result.success) {
          setMode('login');
          setError('');
          alert('회원가입이 완료되었습니다. 로그인해주세요.');
        } else {
          setError(result.message || '회원가입에 실패했습니다.');
        }
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Kanva</h1>
        <p style={styles.subtitle}>개인 생산성 관리</p>

        <div style={styles.tabs}>
          <button
            style={{
              ...styles.tab,
              ...(mode === 'login' ? styles.activeTab : {}),
            }}
            onClick={() => setMode('login')}
          >
            로그인
          </button>
          <button
            style={{
              ...styles.tab,
              ...(mode === 'signup' ? styles.activeTab : {}),
            }}
            onClick={() => setMode('signup')}
          >
            회원가입
          </button>
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          {mode === 'signup' && (
            <div style={styles.inputGroup}>
              <label style={styles.label}>이름</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                style={styles.input}
                placeholder="이름을 입력하세요"
                required
              />
            </div>
          )}

          <div style={styles.inputGroup}>
            <label style={styles.label}>이메일</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              style={styles.input}
              placeholder="이메일을 입력하세요"
              required
            />
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.label}>비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={styles.input}
              placeholder="비밀번호를 입력하세요"
              minLength={8}
              required
            />
            {mode === 'signup' && (
              <span style={styles.hint}>8자 이상 입력하세요</span>
            )}
          </div>

          {error && <p style={styles.error}>{error}</p>}

          <button
            type="submit"
            style={styles.submitButton}
            disabled={isLoading}
          >
            {isLoading
              ? '처리 중...'
              : mode === 'login'
              ? '로그인'
              : '회원가입'}
          </button>
        </form>
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
    padding: '1rem',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '2rem',
    width: '100%',
    maxWidth: '400px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
  },
  title: {
    fontSize: '2rem',
    fontWeight: 700,
    textAlign: 'center',
    color: '#6366f1',
    margin: 0,
  },
  subtitle: {
    textAlign: 'center',
    color: '#6b7280',
    marginBottom: '1.5rem',
  },
  tabs: {
    display: 'flex',
    gap: '0.5rem',
    marginBottom: '1.5rem',
  },
  tab: {
    flex: 1,
    padding: '0.75rem',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    fontSize: '0.9rem',
    fontWeight: 500,
    backgroundColor: '#f3f4f6',
    color: '#6b7280',
    transition: 'all 0.2s',
  },
  activeTab: {
    backgroundColor: '#6366f1',
    color: 'white',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.5rem',
  },
  label: {
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#374151',
  },
  input: {
    padding: '0.75rem 1rem',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '1rem',
    outline: 'none',
    transition: 'border-color 0.2s',
  },
  hint: {
    fontSize: '0.75rem',
    color: '#9ca3af',
  },
  error: {
    color: '#ef4444',
    fontSize: '0.875rem',
    margin: 0,
    padding: '0.5rem',
    backgroundColor: '#fef2f2',
    borderRadius: '6px',
  },
  submitButton: {
    padding: '0.875rem',
    backgroundColor: '#6366f1',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    fontSize: '1rem',
    fontWeight: 600,
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    marginTop: '0.5rem',
  },
};
