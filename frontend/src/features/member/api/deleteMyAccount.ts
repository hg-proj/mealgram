import { api } from '@/lib/axios';

// 회원탈퇴 요청

export async function deleteMyAccount(): Promise<void> {
  await api.delete('/members/me');
}
