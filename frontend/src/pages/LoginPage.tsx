import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

type LoadingProvider = 'github' | 'slack' | null;

export default function LoginPage() {
  const { loginWithOAuth } = useAuth();
  const [loadingProvider, setLoadingProvider] = useState<LoadingProvider>(null);

  const handleLogin = async (provider: 'github' | 'slack') => {
    setLoadingProvider(provider);
    await loginWithOAuth(provider);
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Kanva</h1>
        <p style={styles.subtitle}>Personal Productivity Management</p>

        <div style={styles.divider} />

        <div style={styles.buttonGroup}>
          <button
            style={styles.githubButton}
            onClick={() => handleLogin('github')}
            disabled={loadingProvider !== null}
          >
            <svg style={styles.icon} viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            {loadingProvider === 'github' ? 'Connecting...' : 'Continue with GitHub'}
          </button>

          <button
            style={styles.slackButton}
            onClick={() => handleLogin('slack')}
            disabled={loadingProvider !== null}
          >
            <svg style={styles.icon} viewBox="0 0 24 24" fill="currentColor">
              <path d="M5.042 15.165a2.528 2.528 0 0 1-2.52 2.523A2.528 2.528 0 0 1 0 15.165a2.527 2.527 0 0 1 2.522-2.52h2.52v2.52zM6.313 15.165a2.527 2.527 0 0 1 2.521-2.52 2.527 2.527 0 0 1 2.521 2.52v6.313A2.528 2.528 0 0 1 8.834 24a2.528 2.528 0 0 1-2.521-2.522v-6.313zM8.834 5.042a2.528 2.528 0 0 1-2.521-2.52A2.528 2.528 0 0 1 8.834 0a2.528 2.528 0 0 1 2.521 2.522v2.52H8.834zM8.834 6.313a2.528 2.528 0 0 1 2.521 2.521 2.528 2.528 0 0 1-2.521 2.521H2.522A2.528 2.528 0 0 1 0 8.834a2.528 2.528 0 0 1 2.522-2.521h6.312zM18.956 8.834a2.528 2.528 0 0 1 2.522-2.521A2.528 2.528 0 0 1 24 8.834a2.528 2.528 0 0 1-2.522 2.521h-2.522V8.834zM17.688 8.834a2.528 2.528 0 0 1-2.523 2.521 2.527 2.527 0 0 1-2.52-2.521V2.522A2.527 2.527 0 0 1 15.165 0a2.528 2.528 0 0 1 2.523 2.522v6.312zM15.165 18.956a2.528 2.528 0 0 1 2.523 2.522A2.528 2.528 0 0 1 15.165 24a2.527 2.527 0 0 1-2.52-2.522v-2.522h2.52zM15.165 17.688a2.527 2.527 0 0 1-2.52-2.523 2.526 2.526 0 0 1 2.52-2.52h6.313A2.527 2.527 0 0 1 24 15.165a2.528 2.528 0 0 1-2.522 2.523h-6.313z"/>
            </svg>
            {loadingProvider === 'slack' ? 'Connecting...' : 'Continue with Slack'}
          </button>
        </div>

        <p style={styles.footer}>
          By continuing, you agree to our Terms of Service and Privacy Policy.
        </p>
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
    backgroundColor: '#0f0f0f',
    padding: '1rem',
  },
  card: {
    backgroundColor: '#1a1a1a',
    borderRadius: '16px',
    padding: '2.5rem',
    width: '100%',
    maxWidth: '400px',
    border: '1px solid #2a2a2a',
  },
  title: {
    fontSize: '2.5rem',
    fontWeight: 700,
    textAlign: 'center',
    color: '#ffffff',
    margin: 0,
    letterSpacing: '-0.02em',
  },
  subtitle: {
    textAlign: 'center',
    color: '#888888',
    margin: '0.5rem 0 0 0',
    fontSize: '0.95rem',
  },
  divider: {
    height: '1px',
    backgroundColor: '#2a2a2a',
    margin: '2rem 0',
  },
  buttonGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.75rem',
  },
  githubButton: {
    width: '100%',
    padding: '0.875rem 1rem',
    backgroundColor: '#ffffff',
    color: '#000000',
    border: 'none',
    borderRadius: '10px',
    fontSize: '1rem',
    fontWeight: 600,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.75rem',
    transition: 'opacity 0.2s, transform 0.1s',
  },
  slackButton: {
    width: '100%',
    padding: '0.875rem 1rem',
    backgroundColor: '#4A154B',
    color: '#ffffff',
    border: 'none',
    borderRadius: '10px',
    fontSize: '1rem',
    fontWeight: 600,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.75rem',
    transition: 'opacity 0.2s, transform 0.1s',
  },
  icon: {
    width: '20px',
    height: '20px',
  },
  footer: {
    textAlign: 'center',
    color: '#666666',
    fontSize: '0.75rem',
    marginTop: '1.5rem',
    lineHeight: 1.5,
  },
};
