import { Outlet } from 'react-router';
import ErrorBoundary from '@/components/ErrorBoundary';
import Footer from '@/components/layout/Footer';
import Header from '@/components/layout/Header';

// 헤더, 본문, 푸터 레이아웃

export default function Layout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex flex-1 flex-col">
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </main>
      <Footer />
    </div>
  );
}
