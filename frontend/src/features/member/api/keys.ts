// 회원 정보 조회 키

export const memberKeys = {
  me: ['members', 'me'] as const,
  ingredients: ['members', 'me', 'ingredients'] as const,
};
