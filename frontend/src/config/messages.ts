// 화면에 보이는 오류와 안내 문구 모음

export const messages = {
  network: '서버에 연결하지 못했습니다. 네트워크 상태를 확인해 주세요.',
  loginFailed: '로그인하지 못했습니다. 잠시 후 다시 시도해 주세요.',
  signupFailed: '가입하지 못했습니다. 잠시 후 다시 시도해 주세요.',
  validation: {
    loginIdRequired: '아이디를 입력해 주세요.',
    passwordRequired: '비밀번호를 입력해 주세요.',
    nicknameRequired: '닉네임을 입력해 주세요.',
    nickname: '닉네임은 2자 이상 10자 이하로 입력해 주세요.',
    loginId: '아이디는 영문 소문자와 숫자 6자 이상 20자 이하로 입력해 주세요.',
    password:
      '비밀번호는 영문, 숫자, 특수문자 3가지 조합은 8자 이상, 2가지 조합은 10자 이상으로 입력해 주세요.',
    passwordSpace: '비밀번호에는 공백을 사용할 수 없습니다.',
    passwordMax: '비밀번호는 20자 이하로 입력해 주세요.',
    passwordConfirmRequired: '비밀번호를 한 번 더 입력해 주세요.',
    passwordMismatch: '비밀번호가 일치하지 않습니다.',
    emailRequired: '이메일을 입력해 주세요.',
    email: '이메일 형식이 올바르지 않습니다.',
    agreeTerms: '이용약관에 동의해 주세요.',
    agreePrivacy: '개인정보 수집 및 이용에 동의해 주세요.',
    birthIncomplete: '생년월일을 모두 선택해 주세요.',
    birthInvalid: '올바른 생년월일을 선택해 주세요.',
    birthRange: '만 3세 이상 120세 이하만 선택할 수 있습니다.',
    height: '키는 50cm 이상 250cm 이하로 입력해 주세요.',
    weight: '몸무게는 10kg 이상 300kg 이하로 입력해 주세요.',
  },
} as const;
