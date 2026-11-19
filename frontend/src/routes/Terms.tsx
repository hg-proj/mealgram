import LegalPage from '@/features/legal/components/LegalPage';
import { terms } from '@/features/legal/data/terms';

// 이용약관 화면

export default function Terms() {
  return <LegalPage document={terms} />;
}
