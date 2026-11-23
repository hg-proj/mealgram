import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router';
import closeIcon from '@/assets/close.svg';
import menuIcon from '@/assets/menu.svg';
import Logo from '@/components/ui/Logo';
import MaskIcon from '@/components/ui/MaskIcon';
import { paths } from '@/config/paths';
import { useLogout } from '@/features/auth/api/useLogout';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 사이트 헤더, 좁은 화면에서는 메뉴를 접어 햄버거 버튼으로 엶

const menus = [
  { label: '홈', to: paths.home, end: true, loginOnly: false },
  { label: '식단 추천', to: paths.ingredient, end: false, loginOnly: false },
  { label: '내 식단', to: paths.myMeals, end: false, loginOnly: true },
  { label: '마이페이지', to: paths.myPage, end: false, loginOnly: true },
];

export default function Header() {
  const { isLoggedIn } = useAuth();
  const { mutate: logout } = useLogout();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [openedAt, setOpenedAt] = useState<string | null>(null);
  const open = openedAt === pathname;
  const visibleMenus = menus.filter((menu) => !menu.loginOnly || isLoggedIn);

  useEffect(() => {
    if (!open) {
      return;
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setOpenedAt(null);
      }
    };

    window.addEventListener('keydown', onKey);

    return () => window.removeEventListener('keydown', onKey);
  }, [open]);

  const doLogout = () => logout(undefined, { onSettled: () => navigate(paths.home) });

  const authButton =
    'rounded-[3px] border border-rule px-[clamp(10px,1.5vw,22px)] py-2 text-[clamp(12px,0.9vw,12.5px)] whitespace-nowrap';
  const mobileRow = 'border-b border-line py-3.5 text-left text-[15px] last:border-b-0';

  return (
    <header className="relative border-b border-line px-4">
      <div className="mx-auto flex h-[clamp(64px,5.4vw,80px)] max-w-[1240px] items-center gap-[clamp(14px,3.2vw,48px)]">
        <Link to={paths.home} className="text-[clamp(22px,2.5vw,36px)]">
          <Logo />
        </Link>
        <nav
          aria-label="주 메뉴"
          className="flex gap-[clamp(12px,2.4vw,36px)] self-stretch text-[clamp(13px,1vw,14px)] max-[700px]:hidden"
        >
          {visibleMenus.map((menu) => (
            <NavLink
              key={menu.label}
              to={menu.to}
              end={menu.end}
              className={({ isActive }) =>
                `relative flex items-center ${isActive ? 'font-medium text-green after:absolute after:inset-x-[-4px] after:bottom-[14px] after:h-0.5 after:bg-green' : ''}`
              }
            >
              {menu.label}
            </NavLink>
          ))}
        </nav>
        <div className="ml-auto flex items-center gap-2">
          {isLoggedIn ? (
            <button type="button" onClick={doLogout} className={`${authButton} max-[420px]:hidden`}>
              로그아웃
            </button>
          ) : (
            <Link
              to={paths.login}
              state={{ from: pathname }}
              className={`${authButton} max-[420px]:hidden`}
            >
              로그인
            </Link>
          )}
          <button
            type="button"
            aria-label={open ? '메뉴 닫기' : '메뉴 열기'}
            aria-expanded={open}
            onClick={() => setOpenedAt(open ? null : pathname)}
            className="hidden size-9 items-center justify-center max-[700px]:flex"
          >
            <MaskIcon src={open ? closeIcon : menuIcon} className="size-[22px]" />
          </button>
        </div>
      </div>
      {open && (
        <nav
          aria-label="모바일 메뉴"
          className="absolute top-full right-0 left-0 z-40 hidden flex-col border-b border-line bg-white px-4 py-2 shadow-[0_8px_20px_rgb(29_40_35_/_0.08)] max-[700px]:flex"
        >
          {visibleMenus.map((menu) => (
            <NavLink
              key={menu.label}
              to={menu.to}
              end={menu.end}
              className={({ isActive }) =>
                `border-b border-line py-3.5 text-[15px] last:border-b-0 ${isActive ? 'font-semibold text-green' : ''}`
              }
            >
              {menu.label}
            </NavLink>
          ))}
          {isLoggedIn ? (
            <button type="button" onClick={doLogout} className={`${mobileRow} min-[421px]:hidden`}>
              로그아웃
            </button>
          ) : (
            <Link
              to={paths.login}
              state={{ from: pathname }}
              className={`${mobileRow} min-[421px]:hidden`}
            >
              로그인
            </Link>
          )}
        </nav>
      )}
    </header>
  );
}
