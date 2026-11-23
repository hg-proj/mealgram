import { useRouteError } from 'react-router';
import ErrorPage from '@/routes/ErrorPage';

// 화면을 불러오다 난 오류 화면

const CHUNK_ERROR = /dynamically imported module|Importing a module script failed|Failed to fetch/i;

export default function RouteError() {
  const error = useRouteError();
  const isChunkError = error instanceof Error && CHUNK_ERROR.test(error.message);

  if (!isChunkError) {
    return <ErrorPage status={500} />;
  }

  return (
    <section className="mx-auto flex min-h-[520px] w-full max-w-[1140px] flex-1 flex-col items-center justify-center px-4 py-10 text-center">
      <span className="mb-2.5 block text-[10.5px] tracking-[0.1em] text-green">NEW VERSION</span>
      <h1 className="font-heading text-[clamp(24px,2.6vw,34px)] leading-[1.1] font-extrabold tracking-[-0.04em]">
        새 버전이 배포되었습니다
      </h1>
      <p className="mt-3 text-[15px] text-sub">새로고침하면 최신 화면으로 볼 수 있습니다.</p>
      <button
        type="button"
        onClick={() => window.location.reload()}
        className="mt-7 rounded-[3px] bg-green px-[26px] py-[13px] text-sm font-semibold text-white"
      >
        새로고침
      </button>
    </section>
  );
}
