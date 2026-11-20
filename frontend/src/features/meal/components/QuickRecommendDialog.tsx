import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import Modal from '@/components/ui/Modal';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useRecommendMeals } from '@/features/meal/api/useRecommendMeals';
import { DEFAULT_STYLE, RANDOM_GOAL } from '@/features/meal/data/options';
import { resultStore } from '@/features/meal/lib/resultStore';
import {
  hasBodyInfo,
  toBodyInfoInput,
  toBodyInfoRequest,
} from '@/features/meal/schemas/bodyInfoSchema';
import { useMyProfile } from '@/features/member/api/useMyProfile';
import { errorMessage } from '@/lib/apiError';

// 빠르게 추천받기 선택 창

type Props = { onClose: () => void };

const buttonBase = 'h-14 rounded-[3px] px-2 text-[14.5px] font-bold disabled:cursor-not-allowed';

export default function QuickRecommendDialog({ onClose }: Props) {
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const profile = useMyProfile(isLoggedIn);
  const recommend = useRecommendMeals();
  const firstButton = useRef<HTMLButtonElement>(null);
  const [error, setError] = useState('');

  const body = profile.data ? toBodyInfoInput(profile.data) : null;
  const hasSavedBody = body !== null && hasBodyInfo(body);
  const tip = !isLoggedIn
    ? messages.notice.quickLoginForBody
    : profile.isSuccess && !hasSavedBody
      ? messages.notice.quickNoBody
      : '';

  useEffect(() => {
    firstButton.current?.focus();
  }, []);

  const submit = async (useBody: boolean) => {
    setError('');

    try {
      const result = await recommend.mutateAsync({
        ...(useBody && body !== null ? toBodyInfoRequest(body) : {}),
        subIds: [],
        goal: RANDOM_GOAL,
        style: DEFAULT_STYLE,
      });

      resultStore.save(result);
      resultStore.saveContext({
        goal: RANDOM_GOAL,
        style: DEFAULT_STYLE,
        genre: null,
        requiredName: null,
        ownedNames: [],
      });
      navigate(paths.mealList);
    } catch (e) {
      setError(errorMessage(e, messages.recommendFailed));
    }
  };

  return (
    <Modal titleId="quick-recommend-title" onClose={onClose}>
      <h2
        id="quick-recommend-title"
        className="mb-[18px] font-heading text-[19px] font-extrabold tracking-[-0.03em]"
      >
        빠르게 추천받기
      </h2>
      <div className="grid grid-cols-2 gap-2 max-[420px]:grid-cols-1">
        <button
          ref={firstButton}
          type="button"
          onClick={() => submit(false)}
          disabled={recommend.isPending}
          className={`${buttonBase} bg-green text-white disabled:bg-band disabled:text-disabled`}
        >
          완전 랜덤으로 추천받기
        </button>
        <button
          type="button"
          onClick={() => submit(true)}
          disabled={!hasSavedBody || recommend.isPending}
          className={`${buttonBase} bg-mist text-ink disabled:bg-band disabled:text-disabled`}
        >
          내 신체정보로 추천받기
        </button>
      </div>
      {recommend.isPending && (
        <p className="mt-3.5 text-[13px] text-sub">{messages.notice.quickMaking}</p>
      )}
      {!recommend.isPending && tip !== '' && <p className="mt-3.5 text-[13px] text-sub">{tip}</p>}
      {error !== '' && (
        <p role="alert" className="mt-3.5 text-[13px] text-danger">
          {error}
        </p>
      )}
    </Modal>
  );
}
