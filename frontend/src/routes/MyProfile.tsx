import { useState } from 'react';
import { useNavigate } from 'react-router';
import LoadFailed from '@/components/ui/LoadFailed';
import Skeleton from '@/components/ui/Skeleton';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import { useDeleteMyAccount } from '@/features/member/api/useDeleteMyAccount';
import { useMyProfile } from '@/features/member/api/useMyProfile';
import ProfileEditForm from '@/features/member/components/ProfileEditForm';
import ProfileView from '@/features/member/components/ProfileView';
import WithdrawDialog from '@/features/member/components/WithdrawDialog';
import { errorMessage } from '@/lib/apiError';

export default function MyProfile() {
  const navigate = useNavigate();
  const profile = useMyProfile(true);
  const withdraw = useDeleteMyAccount();
  const [editing, setEditing] = useState(false);
  const [asking, setAsking] = useState(false);
  const [failed, setFailed] = useState('');

  const closeDialog = () => {
    setAsking(false);
    setFailed('');
  };

  const confirmWithdraw = async () => {
    setFailed('');

    try {
      await withdraw.mutateAsync();
      navigate(paths.home);
    } catch (e) {
      setFailed(errorMessage(e, messages.withdrawFailed));
    }
  };

  return (
    <div className="max-w-[760px]">
      <h2 className="border-b-2 border-green pb-3 font-heading text-xl font-extrabold tracking-[-0.03em]">
        내 정보
      </h2>
      {profile.isPending ? (
        <Skeleton className="mt-4 h-[420px]" />
      ) : profile.isError ? (
        <LoadFailed message={messages.profileFailed} onRetry={() => void profile.refetch()} />
      ) : editing ? (
        <ProfileEditForm profile={profile.data} onClose={() => setEditing(false)} />
      ) : (
        <>
          <ProfileView profile={profile.data} onEdit={() => setEditing(true)} />
          <div className="mt-[22px] text-right">
            <button
              type="button"
              onClick={() => setAsking(true)}
              className="text-[13px] text-sub underline"
            >
              회원탈퇴
            </button>
          </div>
        </>
      )}
      {asking && (
        <WithdrawDialog
          pending={withdraw.isPending}
          error={failed}
          onConfirm={() => void confirmWithdraw()}
          onClose={closeDialog}
        />
      )}
    </div>
  );
}
