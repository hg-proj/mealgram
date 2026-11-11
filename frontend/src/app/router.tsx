import { createBrowserRouter } from 'react-router';
import Layout from '@/components/layout/Layout';
import ErrorPage from '@/routes/ErrorPage';
import Main from '@/routes/Main';

// 라우트 정의

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { index: true, element: <Main /> },
      { path: '*', element: <ErrorPage status={404} /> },
    ],
  },
]);
