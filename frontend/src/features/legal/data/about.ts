import type { LegalDocument } from '@/features/legal/types/legal';

// 서비스 소개 내용

export const about: LegalDocument = {
  title: '서비스 소개',
  sections: [
    {
      title: '1. 밀그램이란',
      blocks: [
        {
          type: 'text',
          text: '밀그램은 가지고 있는 재료와 신체 정보를 바탕으로 하루 식단을 추천하는 서비스입니다. 재료를 고르고 목표를 선택하면 식단 3가지를 추천합니다.',
        },
      ],
    },
    {
      title: '2. 밀그램의 강점',
      blocks: [
        {
          type: 'list',
          items: [
            '한국인 영양소 섭취기준: 보건복지부 2025 기준으로 하루 필요 열량을 계산합니다.',
            '보유 재료 우선: 선택한 재료를 먼저 쓰고, 부족한 재료는 장보기 목록에 담습니다.',
            '영양 검증: 추천한 식단의 열량과 영양 성분을 목표와 비교해 확인합니다.',
            '비회원 이용: 가입하지 않아도 식단 추천을 받을 수 있습니다.',
          ],
        },
      ],
    },
  ],
};
