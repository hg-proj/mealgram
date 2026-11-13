import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter, Route, Routes } from 'react-router';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import SignupForm from '@/features/auth/components/SignupForm';

let signupBody: unknown = null;

const server = setupServer(
  http.post('*/api/auth/signup', async ({ request }) => {
    signupBody = await request.json();

    return HttpResponse.json({ id: 1 }, { status: 201 });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
beforeEach(() => {
  signupBody = null;
});

function renderForm() {
  const client = new QueryClient({ defaultOptions: { mutations: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/signup']}>
        <Routes>
          <Route path="/signup" element={<SignupForm />} />
          <Route path="/login" element={<p>로그인 화면</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

async function fillAccount(passwordConfirm = 'password1!') {
  await userEvent.type(screen.getByLabelText('닉네임'), '테스터');
  await userEvent.type(screen.getByLabelText('아이디'), 'tester01');
  await userEvent.type(screen.getByLabelText('비밀번호'), 'password1!');
  await userEvent.type(screen.getByLabelText('비밀번호 확인'), passwordConfirm);
  await userEvent.type(screen.getByLabelText('이메일'), 'tester@example.com');
}

const agreeAll = () => userEvent.click(screen.getByRole('checkbox', { name: '전체 약관동의' }));
const press = (name: string) => userEvent.click(screen.getByRole('button', { name }));

const birthYear = new Date().getFullYear() - 30;

describe('SignupForm', () => {
  it('빈 값으로 다음을 누르면 요청을 보내지 않고 처음 문제 하나만 보여준다.', async () => {
    renderForm();

    await press('다음');

    expect(await screen.findByText('닉네임을 입력해 주세요.')).toBeInTheDocument();
    expect(screen.queryByText('아이디를 입력해 주세요.')).not.toBeInTheDocument();
    expect(screen.queryByText('이용약관에 동의해 주세요.')).not.toBeInTheDocument();
    expect(signupBody).toBeNull();
  });

  it('처음 문제를 고쳐도 건드리지 않은 다음 칸의 오류는 바로 보이지 않는다.', async () => {
    renderForm();

    await press('다음');
    await userEvent.type(screen.getByLabelText('닉네임'), '테스터');

    expect(screen.queryByText('닉네임을 입력해 주세요.')).not.toBeInTheDocument();
    expect(screen.queryByText('아이디를 입력해 주세요.')).not.toBeInTheDocument();

    await press('다음');

    expect(await screen.findByText('아이디를 입력해 주세요.')).toBeInTheDocument();
    expect(screen.queryByText('비밀번호를 입력해 주세요.')).not.toBeInTheDocument();
  });

  it('닉네임을 한 글자만 입력하고 칸을 벗어나면 바로 안내하고, 고치면 사라진다.', async () => {
    renderForm();

    await userEvent.type(screen.getByLabelText('닉네임'), '가');
    await userEvent.tab();

    expect(
      await screen.findByText('닉네임은 2자 이상 10자 이하로 입력해 주세요.'),
    ).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText('닉네임'), '나');

    expect(
      screen.queryByText('닉네임은 2자 이상 10자 이하로 입력해 주세요.'),
    ).not.toBeInTheDocument();
  });

  it('아이디 규칙에 맞지 않으면 칸을 벗어날 때 안내한다.', async () => {
    renderForm();

    await userEvent.type(screen.getByLabelText('아이디'), 'ABC');
    await userEvent.tab();

    expect(
      await screen.findByText('아이디는 영문 소문자와 숫자 6자 이상 20자 이하로 입력해 주세요.'),
    ).toBeInTheDocument();
  });

  it('입력칸에서 Enter를 누르면 제출하지 않고 다음 칸으로 이동한다.', async () => {
    renderForm();

    await userEvent.type(screen.getByLabelText('비밀번호'), 'password1!{Enter}');

    expect(screen.getByLabelText('비밀번호 확인')).toHaveFocus();
    expect(screen.queryByText('비밀번호를 한 번 더 입력해 주세요.')).not.toBeInTheDocument();
    expect(signupBody).toBeNull();
  });

  it('비밀번호 확인이 다르면 다음으로 넘어가지 않는다.', async () => {
    renderForm();

    await fillAccount('password2!');
    await agreeAll();
    await press('다음');

    expect(await screen.findByText('비밀번호가 일치하지 않습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '이전' })).not.toBeInTheDocument();
  });

  it('1단계에서 가입하면 필수 항목만 보내고 로그인 화면으로 이동한다.', async () => {
    renderForm();

    await fillAccount();
    await agreeAll();
    await press('가입');

    expect(await screen.findByText('로그인 화면')).toBeInTheDocument();
    expect(signupBody).toEqual({
      nickname: '테스터',
      loginId: 'tester01',
      password: 'password1!',
      email: 'tester@example.com',
    });
  });

  it('2단계에서 입력한 신체 정보를 숫자와 코드 값으로 보낸다.', async () => {
    renderForm();

    await fillAccount();
    await agreeAll();
    await press('다음');
    await userEvent.click(screen.getByRole('button', { name: '활동량' }));
    await userEvent.click(screen.getByRole('option', { name: /^활동적/ }));
    await press('남자');
    await userEvent.click(screen.getByRole('button', { name: '출생 연도' }));
    await userEvent.click(screen.getByRole('option', { name: `${birthYear}년` }));
    await userEvent.click(screen.getByRole('button', { name: '출생 월' }));
    await userEvent.click(screen.getByRole('option', { name: '3월' }));
    await userEvent.click(screen.getByRole('button', { name: '출생 일' }));
    await userEvent.click(screen.getByRole('option', { name: '5일' }));
    await userEvent.type(screen.getByLabelText('키(cm)'), '175');
    await userEvent.type(screen.getByLabelText('몸무게(kg)'), '70.5');
    await press('가입');

    expect(await screen.findByText('로그인 화면')).toBeInTheDocument();
    expect(signupBody).toEqual({
      nickname: '테스터',
      loginId: 'tester01',
      password: 'password1!',
      email: 'tester@example.com',
      birthDate: `${birthYear}-03-05`,
      gender: 'MALE',
      height: 175,
      weight: 70.5,
      activityLevel: 'MODERATE',
    });
  });

  it('아이디가 중복이면 1단계로 돌아가 아이디 칸에 서버 문구를 보여준다.', async () => {
    server.use(
      http.post('*/api/auth/signup', () =>
        HttpResponse.json(
          {
            success: false,
            error: { code: 'DUPLICATE_LOGIN_ID', message: '이미 사용 중인 아이디입니다.' },
          },
          { status: 409 },
        ),
      ),
    );
    renderForm();

    await fillAccount();
    await agreeAll();
    await press('다음');
    await press('가입');

    expect(await screen.findByText('이미 사용 중인 아이디입니다.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '다음' })).toBeInTheDocument();
  });
});
