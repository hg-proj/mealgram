// 불러오지 못했을 때 화면, 안내 문구와 다시 불러오기 버튼

type Props = {
  message: string;
  onRetry?: () => void;
};

export default function LoadFailed({ message, onRetry }: Props) {
  return (
    <div
      role="alert"
      className="flex min-h-[260px] flex-col items-center justify-center px-4 py-8 text-center"
    >
      <p className="text-[15px] text-sub">{message}</p>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-6 rounded-[3px] bg-green px-[26px] py-[13px] text-sm font-semibold text-white"
        >
          다시 불러오기
        </button>
      )}
    </div>
  );
}
