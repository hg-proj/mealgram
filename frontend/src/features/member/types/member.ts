// 내 정보 응답 타입

export type Gender = 'MALE' | 'FEMALE';
export type ActivityLevel = 'LOW' | 'LIGHT' | 'MODERATE' | 'HIGH';

export type MemberProfile = {
  nickname: string;
  loginId: string;
  email: string;
  birthDate: string | null;
  gender: Gender | null;
  height: number | null;
  weight: number | null;
  activityLevel: ActivityLevel | null;
};
