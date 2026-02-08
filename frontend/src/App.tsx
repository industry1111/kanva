import { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import DailyWorkspacePage from './pages/DailyWorkspacePage';
import DashboardPage from './pages/DashboardPage';
import AIReportPage from './pages/AIReportPage';
import LoginPage from './pages/LoginPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';

type Page = 'workspace' | 'dashboard' | 'report';

function getOAuthProvider(): string | null {
  const path = window.location.pathname;
  const match = path.match(/^\/login\/oauth2\/code\/(\w+)$/);
  return match ? match[1] : null;
}

function AppContent() {
  const { isAuthenticated, isLoading } = useAuth();
  const [currentPage, setCurrentPage] = useState<Page>('workspace');
  const [oauthProvider, setOAuthProvider] = useState<string | null>(null);

  useEffect(() => {
    setOAuthProvider(getOAuthProvider());
  }, []);

  if (oauthProvider) {
    return <OAuthCallbackPage provider={oauthProvider} />;
  }

  if (isLoading) {
    return (
      <div style={styles.loading}>
        <div style={styles.spinner} />
        <span>Loading...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <div>
      <nav style={styles.nav}>
        <button
          onClick={() => setCurrentPage('workspace')}
          style={{
            ...styles.navButton,
            ...(currentPage === 'workspace' ? styles.navButtonActive : {}),
          }}
        >
          Workspace
        </button>
        <button
          onClick={() => setCurrentPage('dashboard')}
          style={{
            ...styles.navButton,
            ...(currentPage === 'dashboard' ? styles.navButtonActive : {}),
          }}
        >
          Dashboard
        </button>
        <button
          onClick={() => setCurrentPage('report')}
          style={{
            ...styles.navButton,
            ...(currentPage === 'report' ? styles.navButtonActive : {}),
          }}
        >
          Report
        </button>
      </nav>
      {currentPage === 'workspace' && <DailyWorkspacePage />}
      {currentPage === 'dashboard' && <DashboardPage />}
      {currentPage === 'report' && <AIReportPage />}
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

const styles: Record<string, React.CSSProperties> = {
  loading: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    gap: '1rem',
    color: '#6b7280',
  },
  spinner: {
    width: '40px',
    height: '40px',
    border: '3px solid #e5e7eb',
    borderTopColor: '#6366f1',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
  },
  nav: {
    display: 'flex',
    justifyContent: 'center',
    gap: '8px',
    padding: '12px 24px',
    backgroundColor: '#ffffff',
    borderBottom: '1px solid #e5e7eb',
  },
  navButton: {
    padding: '8px 20px',
    border: '1px solid #e5e7eb',
    borderRadius: '6px',
    backgroundColor: '#ffffff',
    color: '#6b7280',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
    transition: 'all 0.15s ease',
  },
  navButtonActive: {
    backgroundColor: '#6366f1',
    borderColor: '#6366f1',
    color: '#ffffff',
  },
};

export default App;
