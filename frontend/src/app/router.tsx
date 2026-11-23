import { createBrowserRouter } from 'react-router';
import { paths } from '@/config/paths';
import Layout from '@/components/layout/Layout';
import RequireLogin from '@/features/auth/components/RequireLogin';
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
        path: paths.ingredient,
        lazy: async () => ({ Component: (await import('@/routes/Recommend')).default }),
      },
      {
        path: paths.mealList,
        lazy: async () => ({ Component: (await import('@/routes/MealList')).default }),
      },
      {
        path: paths.mealDetail,
        lazy: async () => ({ Component: (await import('@/routes/MealDetail')).default }),
      },
      {
        element: <RequireLogin />,
        children: [
          {
            path: paths.myMeals,
            lazy: async () => ({ Component: (await import('@/routes/MyMeals')).default }),
          },
          {
            path: paths.myMealDetail,
            lazy: async () => ({ Component: (await import('@/routes/MyMealDetail')).default }),
          },
          {
            path: paths.myPage,
            lazy: async () => ({ Component: (await import('@/routes/MyPage')).default }),
            children: [
              {
                index: true,
                lazy: async () => ({ Component: (await import('@/routes/MyProfile')).default }),
              },
              {
                path: 'ingredients',
                lazy: async () => ({
                  Component: (await import('@/routes/MyIngredients')).default,
                }),
              },
            ],
          },
        ],
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
