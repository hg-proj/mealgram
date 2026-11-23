import ProfileTable from '@/features/member/components/ProfileTable';
import {
  activityText,
  birthText,
  genderText,
  measureText,
} from '@/features/member/lib/profileText';
import type { MemberProfile } from '@/features/member/types/member';

type Props = {
  profile: MemberProfile;
  onEdit: () => void;
};

export default function ProfileView({ profile, onEdit }: Props) {
  return (
    <>
      <ProfileTable
        rows={[
          { label: '닉네임', content: profile.nickname },
          { label: '아이디', content: profile.loginId },
          { label: '이메일', content: profile.email },
          { label: '생년월일', content: birthText(profile.birthDate) },
          { label: '성별', content: genderText(profile.gender) },
          { label: '키', content: measureText(profile.height, 'cm') },
          { label: '몸무게', content: measureText(profile.weight, 'kg') },
          { label: '활동량', content: activityText(profile.activityLevel) },
        ]}
      />
      <div className="mt-[26px] flex justify-center">
        <button
          type="button"
          onClick={onEdit}
          className="h-12 min-w-[150px] rounded-[3px] bg-green px-6 text-[15px] font-semibold text-white"
        >
          수정하기
        </button>
      </div>
    </>
  );
}
