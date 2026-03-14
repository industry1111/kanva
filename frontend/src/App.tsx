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
      <div className="flex flex-col items-center justify-center h-screen gap-4 text-text-secondary">
        <div
          className="w-10 h-10 border-border border-t-primary rounded-full"
          style={{ borderWidth: '3px', animation: 'spin 1s linear infinite' }}
        />
        <span>Loading...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <div className="flex h-screen overflow-hidden">
      <nav className="w-56 min-w-56 bg-white border-r border-border flex flex-col items-center py-4 gap-1">
        <div className="w-9 h-9 rounded-lg bg-primary text-white flex items-center justify-center text-sm font-bold mb-4">K</div>
        {(['workspace', 'dashboard', 'report'] as Page[]).map((page) => (
          <button
            key={page}
            onClick={() => setCurrentPage(page)}
            className={`w-[192px] px-3 py-2 border-none rounded-lg text-sm font-medium cursor-pointer transition-colors flex items-center gap-2.5 text-left ${
              currentPage === page
                ? 'bg-primary text-white'
                : 'bg-transparent text-text-secondary hover:bg-bg'
            }`}
            title={page.charAt(0).toUpperCase() + page.slice(1)}
          >
            {page === 'workspace' && '\u{1F4DD}'}
            {page === 'dashboard' && '\u{1F4CA}'}
            {page === 'report' && '\u{1F4CB}'}
            <span className="text-[13px]">
              {page === 'workspace' && '\uC624\uB298'}
              {page === 'dashboard' && '\uCE98\uB9B0\uB354'}
              {page === 'report' && '\uB9AC\uD3EC\uD2B8'}
            </span>
          </button>
        ))}
      </nav>
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="flex justify-between items-center px-6 bg-white border-b border-border h-12">
          <span className="text-base font-bold text-text">Kanva</span>
          <div className="flex items-center gap-2">
            {import.meta.env.DEV && <span className="text-[11px] text-text-secondary font-medium">DEV</span>}
            <button onClick={() => {
              localStorage.removeItem('kanva_access_token');
              localStorage.removeItem('kanva_refresh_token');
              window.location.reload();
            }} className="px-2.5 py-0.5 border border-border rounded-md bg-white text-text-secondary text-xs cursor-pointer hover:bg-bg transition-colors">로그아웃</button>
          </div>
        </header>
        <div className="flex-1 overflow-auto">
          {currentPage === 'workspace' && <DailyWorkspacePage />}
          {currentPage === 'dashboard' && <DashboardPage />}
          {currentPage === 'report' && <AIReportPage />}
        </div>
      </div>
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

export default App;
