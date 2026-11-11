import { RouterProvider } from 'react-router';
import Provider from '@/app/provider';
import { router } from '@/app/router';

// 앱 최상위 컴포넌트

export default function App() {
  return (
    <Provider>
      <RouterProvider router={router} />
    </Provider>
  );
}
