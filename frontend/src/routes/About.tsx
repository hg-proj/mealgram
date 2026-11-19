import LegalPage from '@/features/legal/components/LegalPage';
import { about } from '@/features/legal/data/about';

// 서비스 소개 화면

export default function About() {
  return <LegalPage document={about} />;
}
