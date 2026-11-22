import { activityOptions } from '@/features/meal/data/options';
import type { MemberProfile } from '@/features/member/types/member';

// 내 정보 화면에 보이는 글자 변환

const EMPTY = '-';

export function birthText(birthDate: string | null): string {
  if (birthDate === null) {
    return EMPTY;
  }
  const [year, month, day] = birthDate.split('-').map(Number);

  return `${year}년 ${month}월 ${day}일`;
}

export function genderText(gender: MemberProfile['gender']): string {
  return gender === 'MALE' ? '남자' : gender === 'FEMALE' ? '여자' : EMPTY;
}

export function activityText(level: MemberProfile['activityLevel']): string {
  return activityOptions.find((option) => option.value === level)?.label ?? EMPTY;
}

export function measureText(value: number | null, unit: string): string {
  return value === null ? EMPTY : `${value}${unit}`;
}
