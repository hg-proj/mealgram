import { messages } from '@/config/messages';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 목록과 상세 화면 안내 문구

export default function DetailNotice() {
  const { isLoggedIn } = useAuth();

  return (
    <p>
      {isLoggedIn ? messages.notice.nutritionEstimate : messages.notice.nutritionEstimateAndLogin}
    </p>
  );
}
