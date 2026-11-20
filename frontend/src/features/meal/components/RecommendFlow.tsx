import { useQueryClient } from '@tanstack/react-query';
import { Fragment, useState } from 'react';
import { useNavigate } from 'react-router';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useIngredientCatalog } from '@/features/meal/api/useIngredientCatalog';
import { useRecommendMeals } from '@/features/meal/api/useRecommendMeals';
import BodyInfoStep from '@/features/meal/components/BodyInfoStep';
import GoalStep from '@/features/meal/components/GoalStep';
import IngredientStep from '@/features/meal/components/IngredientStep';
import { DEFAULT_STYLE, RANDOM_GENRE } from '@/features/meal/data/options';
import { resultStore } from '@/features/meal/lib/resultStore';
import {
  bodyInfoDefaults,
  hasBodyInfo,
  toBodyInfoInput,
  toBodyInfoRequest,
  type BodyInfoInput,
} from '@/features/meal/schemas/bodyInfoSchema';
import type { PickedIngredients } from '@/features/meal/types/ingredient';
import type { Goal } from '@/features/meal/types/meal';
import { updateMyProfile } from '@/features/member/api/updateMyProfile';
import { useMyProfile } from '@/features/member/api/useMyProfile';
import { errorMessage } from '@/lib/apiError';

// 식단 추천 흐름, 신체 정보와 목표와 재료 순서

const steps = ['신체 정보 (선택)', '목표', '재료'];

export default function RecommendFlow() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { isLoggedIn } = useAuth();
  const profile = useMyProfile(isLoggedIn);
  const recommend = useRecommendMeals();
  const catalog = useIngredientCatalog();
  const [step, setStep] = useState(0);
  const [body, setBody] = useState<BodyInfoInput>(bodyInfoDefaults);
  const [saveBody, setSaveBody] = useState(false);
  const [goal, setGoal] = useState<Goal | ''>('');
  const [genres, setGenres] = useState<string[]>([]);
  const [style, setStyle] = useState<string>(DEFAULT_STYLE);
  const [picked, setPicked] = useState<PickedIngredients>({ requiredId: null, subIds: [] });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    if (submitting) {
      return;
    }
    setSubmitting(true);
    setError('');

    try {
      if (isLoggedIn && saveBody && hasBodyInfo(body)) {
        await updateMyProfile(toBodyInfoRequest(body));
        await queryClient.invalidateQueries({ queryKey: ['members', 'me'] });
      }

      const chosen = genres.filter((genre) => genre !== RANDOM_GENRE);
      const result = await recommend.mutateAsync({
        ...toBodyInfoRequest(body),
        requiredId: picked.requiredId ?? undefined,
        subIds: picked.subIds,
        goal,
        style,
        genre: chosen.length > 0 ? chosen.join(', ') : undefined,
      });

      const nameOf = new Map(
        (catalog.data ?? []).flatMap((c) => c.ingredients.map((i) => [i.id, i.name] as const)),
      );

      resultStore.save(result);
      resultStore.saveContext({
        goal,
        style,
        genre: chosen.length > 0 ? chosen.join(', ') : null,
        requiredName: picked.requiredId === null ? null : (nameOf.get(picked.requiredId) ?? null),
        ownedNames: picked.subIds.flatMap((id) => nameOf.get(id) ?? []),
      });
      navigate(paths.mealList);
    } catch (e) {
      setError(errorMessage(e, messages.recommendFailed));
    } finally {
      setSubmitting(false);
    }
  };

  if (isLoggedIn && profile.isPending) {
    return <div className="mx-auto h-[420px] max-w-[420px] animate-pulse bg-band" />;
  }

  return (
    <div>
      <div className="mb-[18px] flex justify-center gap-2 text-[12.5px] text-sub">
        {steps.map((label, index) => (
          <Fragment key={label}>
            {index > 0 && <span aria-hidden="true">›</span>}
            <span className={index === step ? 'font-semibold text-green' : ''}>
              {index + 1} {label}
            </span>
          </Fragment>
        ))}
      </div>
      {step === 0 && (
        <BodyInfoStep
          defaultValues={profile.data ? toBodyInfoInput(profile.data) : body}
          showSave={isLoggedIn}
          save={saveBody}
          onSave={setSaveBody}
          onNext={(values) => {
            setBody(values);
            setStep(1);
          }}
        />
      )}
      {step === 1 && (
        <GoalStep
          goal={goal}
          style={style}
          onStyle={setStyle}
          selectedGenres={genres}
          onGoal={setGoal}
          onGenres={setGenres}
          onPrev={() => setStep(0)}
          onNext={() => setStep(2)}
        />
      )}
      {step === 2 && (
        <IngredientStep
          picked={picked}
          onPick={setPicked}
          onPrev={() => setStep(1)}
          onSubmit={submit}
          pending={recommend.isPending || submitting}
          error={error}
        />
      )}
    </div>
  );
}
