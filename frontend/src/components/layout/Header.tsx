import { Link, NavLink, useNavigate } from 'react-router';
import Logo from '@/components/ui/Logo';
import { paths } from '@/config/paths';
import { useLogout } from '@/features/auth/api/useLogout';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 사이트 헤더

const menus = [
  { label: '홈', to: paths.home, end: true, loginOnly: false },
  { label: '식단 추천', to: paths.ingredient, end: false, loginOnly: false },
  { label: '마이페이지', to: paths.myPage, end: false, loginOnly: true },
];

export default function Header() {
  const { isLoggedIn } = useAuth();
  const { mutate: logout } = useLogout();
  const navigate = useNavigate();
  const visibleMenus = menus.filter((menu) => !menu.loginOnly || isLoggedIn);

  return (
    <header className="border-b border-line px-4">
      <div className="mx-auto flex h-[clamp(64px,5.4vw,80px)] max-w-[1240px] items-center gap-[clamp(14px,3.2vw,48px)]">
        <Link to={paths.home} className="text-[clamp(26px,2.5vw,36px)]">
          <Logo />
        </Link>
        <nav
          aria-label="주 메뉴"
          className="flex gap-[clamp(12px,2.4vw,36px)] self-stretch text-[clamp(13px,1vw,14px)]"
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
        {isLoggedIn ? (
          <button
            type="button"
            onClick={() => logout(undefined, { onSettled: () => navigate(paths.home) })}
            className="ml-auto rounded-[3px] border border-rule px-[clamp(10px,1.5vw,22px)] py-2 text-[clamp(12px,0.9vw,12.5px)] whitespace-nowrap"
          >
            로그아웃
          </button>
        ) : (
          <Link
            to={paths.login}
            className="ml-auto rounded-[3px] border border-rule px-[clamp(10px,1.5vw,22px)] py-2 text-[clamp(12px,0.9vw,12.5px)] whitespace-nowrap"
          >
            로그인
          </Link>
        )}
      </div>
    </header>
  );
}
