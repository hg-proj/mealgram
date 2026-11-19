import { createBrowserRouter } from 'react-router';
import { paths } from '@/config/paths';
import Layout from '@/components/layout/Layout';
import ErrorPage from '@/routes/ErrorPage';
import Main from '@/routes/Main';

// 라우트 정의, 화면은 들어갈 때 불러옴

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { index: true, element: <Main /> },
      {
        path: paths.login,
        lazy: async () => ({ Component: (await import('@/routes/Login')).default }),
      },
      {
        path: paths.signup,
        lazy: async () => ({ Component: (await import('@/routes/Signup')).default }),
      },
      {
        path: paths.forgotPassword,
        lazy: async () => ({ Component: (await import('@/routes/ForgotPassword')).default }),
      },
      {
        path: paths.about,
        lazy: async () => ({ Component: (await import('@/routes/About')).default }),
      },
      {
        path: paths.terms,
        lazy: async () => ({ Component: (await import('@/routes/Terms')).default }),
      },
      {
        path: paths.privacy,
        lazy: async () => ({ Component: (await import('@/routes/Privacy')).default }),
      },
      { path: '*', element: <ErrorPage status={404} /> },
    ],
  },
]);
