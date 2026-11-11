import { createBrowserRouter } from 'react-router';
import { paths } from '@/config/paths';
import Layout from '@/components/layout/Layout';
import ErrorPage from '@/routes/ErrorPage';
import Main from '@/routes/Main';

// 라우트 정의, 로그인 화면은 들어갈 때 불러옴

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { index: true, element: <Main /> },
      {
        path: paths.login,
        lazy: async () => ({ Component: (await import('@/routes/Login')).default }),
      },
      { path: '*', element: <ErrorPage status={404} /> },
    ],
  },
]);
