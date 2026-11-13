import { describe, expect, it } from 'vitest';
import {
  birthDateIssue,
  isValidPassword,
  signupDefaults,
  signupSchema,
} from '@/features/auth/schemas/signupSchema';

const valid = {
  ...signupDefaults,
  nickname: '테스터',
  loginId: 'tester01',
  password: 'password1!',
  passwordConfirm: 'password1!',
  email: 'tester@example.com',
  agreeTerms: true,
  agreePrivacy: true,
};

function messages(override: Partial<typeof valid>) {
  const result = signupSchema.safeParse({ ...valid, ...override });

  return result.success ? [] : result.error.issues.map((issue) => issue.message);
}

describe('isValidPassword', () => {
  it('세 가지 종류는 8자 이상, 두 가지 종류는 10자 이상이어야 한다.', () => {
    expect(isValidPassword('abc123!@')).toBe(true);
    expect(isValidPassword('abc123!')).toBe(false);
    expect(isValidPassword('abcdefg123')).toBe(true);
    expect(isValidPassword('abcdefg12')).toBe(false);
  });

  it('한 가지 종류와 공백과 20자 초과는 허용하지 않는다.', () => {
    expect(isValidPassword('abcdefghijklmn')).toBe(false);
    expect(isValidPassword('abc 123!@#')).toBe(false);
    expect(isValidPassword('abc123!@#abc123!@#abc')).toBe(false);
  });
});

describe('signupSchema', () => {
  it('필수 항목만 있어도 통과한다.', () => {
    expect(messages({})).toEqual([]);
  });

  it('신체 정보는 범위를 벗어나면 안내 문구를 돌려준다.', () => {
    expect(messages({ height: '49' })).toEqual(['키는 50cm 이상 250cm 이하로 입력해 주세요.']);
    expect(messages({ weight: '301' })).toEqual(['몸무게는 10kg 이상 300kg 이하로 입력해 주세요.']);
    expect(messages({ height: '175.5', weight: '70.5' })).toEqual([]);
  });

  it('생년월일은 모두 비우거나 모두 골라야 하고 존재하는 날짜여야 한다.', () => {
    const year = String(new Date().getFullYear() - 30);

    expect(messages({})).toEqual([]);
    expect(messages({ birthYear: year, birthMonth: '3', birthDay: '5' })).toEqual([]);
    expect(messages({ birthYear: year })).toEqual(['생년월일을 모두 선택해 주세요.']);
    expect(messages({ birthYear: '2001', birthMonth: '2', birthDay: '30' })).toEqual([
      '올바른 생년월일을 선택해 주세요.',
    ]);
  });

  it('생년월일로 계산한 만 나이가 3세 미만이거나 120세 초과이면 통과하지 못한다.', () => {
    const range = '만 3세 이상 120세 이하만 선택할 수 있습니다.';
    const thisYear = new Date().getFullYear();

    expect(messages({ birthYear: String(thisYear), birthMonth: '1', birthDay: '1' })).toEqual([
      range,
    ]);
    expect(messages({ birthYear: String(thisYear - 121), birthMonth: '1', birthDay: '1' })).toEqual(
      [range],
    );
  });

  it('만 나이는 생일이 지났는지에 따라 계산한다.', () => {
    const today = new Date(2026, 5, 10);

    expect(birthDateIssue('2023', '6', '10', today)).toBeNull();
    expect(birthDateIssue('2023', '6', '11', today)).toBe('birthRange');
  });

  it('아이디와 닉네임 규칙을 확인한다.', () => {
    expect(messages({ loginId: 'Tester01' })).toEqual([
      '아이디는 영문 소문자와 숫자 6자 이상 20자 이하로 입력해 주세요.',
    ]);
    expect(messages({ nickname: '가' })).toEqual(['닉네임은 2자 이상 10자 이하로 입력해 주세요.']);
  });

  it('비밀번호 문제에 따라 알맞은 안내 문구를 돌려준다.', () => {
    const first = (password: string) => messages({ password, passwordConfirm: password })[0];

    expect(first('abc123')).toBe(
      '비밀번호는 영문, 숫자, 특수문자 3가지 조합은 8자 이상, 2가지 조합은 10자 이상으로 입력해 주세요.',
    );
    expect(first('abc123!@#abc123!@#abc')).toBe('비밀번호는 20자 이하로 입력해 주세요.');
    expect(first('abc 123!@#')).toBe('비밀번호에는 공백을 사용할 수 없습니다.');
  });

  it('비밀번호 확인이 다르면 안내 문구를 돌려준다.', () => {
    expect(messages({ passwordConfirm: 'password2!' })).toEqual(['비밀번호가 일치하지 않습니다.']);
  });

  it('약관에 동의하지 않으면 통과하지 못한다.', () => {
    expect(messages({ agreeTerms: false })).toEqual(['이용약관에 동의해 주세요.']);
    expect(messages({ agreePrivacy: false })).toEqual(['개인정보 수집 및 이용에 동의해 주세요.']);
  });
});
