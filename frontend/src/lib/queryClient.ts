import { QueryClient } from '@tanstack/react-query';

// TanStack Query 기본 설정

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
});
