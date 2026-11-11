import { site } from '@/config/site';

// 사이트 푸터

export default function Footer() {
  return (
    <footer className="bg-green-d px-4 text-white">
      <div className="mx-auto max-w-[1140px] pt-9 pb-11">
        <nav
          aria-label="푸터 링크"
          className="flex flex-wrap gap-x-[22px] gap-y-2 pb-7 text-[13px]"
        >
          <span>서비스 소개</span>
          <span>이용약관</span>
          <span className="font-bold">개인정보처리방침</span>
        </nav>
        <div className="grid grid-cols-[1fr_380px] items-start gap-y-6 max-[820px]:grid-cols-1">
          <div>
            <div className="mb-3.5 font-logo text-4xl leading-none font-semibold">{site.name}</div>
            <p className="text-xs leading-[1.9] opacity-85">
              서비스명 : {site.serviceName}
              <br />
              서비스 : {site.service}
              <br />
              이메일 : {site.email}
            </p>
            <p className="mt-3 text-xs leading-[1.9] opacity-85">{site.copyright}</p>
          </div>
          <div className="flex items-center gap-3.5">
            <svg viewBox="0 0 24 24" aria-hidden="true" className="size-[34px] flex-none">
              <path
                d="M6 3.5h3l1.5 4-2 1.5a11 11 0 006 6l1.5-2 4 1.5v3a2 2 0 01-2 2A15 15 0 014 5.5a2 2 0 012-2z"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.6"
                strokeLinejoin="round"
              />
            </svg>
            <div>
              <b className="block text-xl font-bold tracking-[-0.03em]">
                고객센터 <em className="ml-1 text-lg not-italic">{site.phone}</em>
              </b>
              <small className="mt-1.5 block text-xs leading-[1.6] opacity-85">
                {site.hours[0]}
                <br />
                {site.hours[1]}
              </small>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
