// 화면에 보이는 오류와 안내 문구 모음

export const messages = {
  network: '서버에 연결하지 못했습니다. 네트워크 상태를 확인해 주세요.',
  loginFailed: '로그인하지 못했습니다. 잠시 후 다시 시도해 주세요.',
  validation: {
    loginIdRequired: '아이디를 입력해 주세요.',
    passwordRequired: '비밀번호를 입력해 주세요.',
  },
} as const;
