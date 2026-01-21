import { AuthProvider, useAuth } from './contexts/AuthContext';
import DailyWorkspacePage from './pages/DailyWorkspacePage';
import LoginPage from './pages/LoginPage';

function AppContent() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={styles.loading}>
        <div style={styles.spinner} />
        <span>로딩 중...</span>
      </div>
    );
  }

  return isAuthenticated ? <DailyWorkspacePage /> : <LoginPage />;
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
};

// Add keyframes for spinner animation in index.css

export default App;
