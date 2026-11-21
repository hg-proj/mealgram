import { Navigate, Outlet, useLocation } from 'react-router';
import { paths } from '@/config/paths';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 로그인한 회원만 들어가는 화면 묶음

export default function RequireLogin() {
  const { isLoggedIn } = useAuth();
  const { pathname } = useLocation();

  return isLoggedIn ? <Outlet /> : <Navigate to={paths.login} state={{ from: pathname }} replace />;
}
