import LegalPage from '@/features/legal/components/LegalPage';
import { privacy } from '@/features/legal/data/privacy';

// 개인정보처리방침 화면

export default function Privacy() {
  return <LegalPage document={privacy} />;
}
