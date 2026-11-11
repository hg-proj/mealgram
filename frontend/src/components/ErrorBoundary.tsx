import { Component, type ErrorInfo, type ReactNode } from 'react';
import ErrorPage from '@/routes/ErrorPage';

// 화면 오류 경계

type Props = { children: ReactNode };
type State = { failed: boolean };

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { failed: false };

  static getDerivedStateFromError(): State {
    return { failed: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(error, info.componentStack);
  }

  render() {
    return this.state.failed ? <ErrorPage status={500} /> : this.props.children;
  }
}
