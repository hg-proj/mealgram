import { site } from '@/config/site';
import type { LegalDocument } from '@/features/legal/types/legal';

// 개인정보처리방침 내용

export const privacy: LegalDocument = {
  title: '개인정보처리방침',
  dated: true,
  sections: [
    {
      title: '1. 수집하는 항목',
      blocks: [
        {
          type: 'table',
          head: ['구분', '항목'],
          rows: [
            ['필수', '닉네임, 아이디, 비밀번호, 이메일'],
            ['선택', '생년월일, 성별, 키, 몸무게, 활동량'],
          ],
        },
      ],
    },
    {
      title: '2. 이용 목적',
      blocks: [
        {
          type: 'list',
          items: [
            '회원 확인과 로그인, 비밀번호 찾기 인증번호 발송',
            '목표 열량 계산과 맞춤 식단 추천',
          ],
        },
      ],
    },
    {
      title: '3. 보유 기간',
      blocks: [
        {
          type: 'text',
          text: '회원 탈퇴 시 입력하신 개인정보를 지체 없이 파기하며, 탈퇴 후에는 따로 보관하지 않습니다.',
        },
      ],
    },
    {
      title: '4. 제3자 제공과 위탁',
      blocks: [
        {
          type: 'text',
          text: '서비스는 이용자의 개인정보를 제3자에게 제공하지 않습니다. 인증번호 메일 발송을 위해 메일 발송 업체에 이메일을 전달합니다.',
        },
      ],
    },
    {
      title: '5. 파기',
      blocks: [
        {
          type: 'text',
          text: '보유 기간이 끝나거나 처리 목적을 달성한 개인정보는 복구할 수 없는 방법으로 파기합니다.',
        },
      ],
    },
    {
      title: '6. 이용자의 권리',
      blocks: [
        {
          type: 'text',
          text: '이용자는 언제든지 마이페이지에서 정보를 조회하고 수정하거나 탈퇴할 수 있습니다.',
        },
      ],
    },
    {
      title: '7. 문의처',
      blocks: [{ type: 'text', text: `개인정보 보호 문의는 ${site.email}으로 접수해 주세요.` }],
    },
  ],
};
