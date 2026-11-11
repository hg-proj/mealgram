import { isAxiosError } from 'axios';
import { messages } from '@/config/messages';
import type { ApiErrorBody } from '@/types/api';

// 서버 오류 응답 해석

export function getApiError(error: unknown): ApiErrorBody['error'] | null {
  return isAxiosError<ApiErrorBody>(error) ? (error.response?.data?.error ?? null) : null;
}

export function isNetworkError(error: unknown): boolean {
  return isAxiosError(error) && !error.response;
}

export function errorMessage(error: unknown, fallback: string): string {
  if (isNetworkError(error)) {
    return messages.network;
  }

  return getApiError(error)?.message ?? fallback;
}
