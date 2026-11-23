import { NavLink, Outlet } from 'react-router';
import { paths } from '@/config/paths';

const menus = [
  { label: '내 정보', to: paths.myPage, end: true },
  { label: '내 재료', to: paths.myIngredients, end: false },
];

export default function MyPage() {
  return (
    <section className="px-4 pt-9 pb-16">
      <div className="mx-auto grid max-w-[1140px] grid-cols-[200px_1fr] items-start gap-10 max-[760px]:grid-cols-1 max-[760px]:gap-6">
        <aside>
          <h1 className="border-b-2 border-green pb-3.5 max-[760px]:border-b-0 max-[760px]:pb-0 font-heading text-[clamp(21px,2.4vw,26px)] font-extrabold tracking-[-0.04em]">
            마이페이지
          </h1>
          <nav
            aria-label="마이페이지 메뉴"
            className="max-[760px]:mt-4 max-[760px]:flex max-[760px]:border-b max-[760px]:border-rule"
          >
            {menus.map((menu) => (
              <NavLink
                key={menu.label}
                to={menu.to}
                end={menu.end}
                className={({ isActive }) =>
                  `block border-b border-line px-1 py-[13px] text-[14.5px] max-[760px]:-mb-px max-[760px]:border-b-2 max-[760px]:px-5 max-[760px]:py-3 max-[760px]:text-[15px] max-[760px]:font-semibold ${isActive ? 'font-extrabold text-ink max-[760px]:border-green max-[760px]:text-green' : 'text-sub max-[760px]:border-transparent'}`
                }
              >
                {menu.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <div className="min-w-0">
          <Outlet />
        </div>
      </div>
    </section>
  );
}
