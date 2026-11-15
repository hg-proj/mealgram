import { useCallback, useEffect, useState } from 'react';

export function useCountdown() {
  const [endsAt, setEndsAt] = useState<number | null>(null);
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    if (endsAt === null) {
      return;
    }

    const timer = setInterval(() => {
      const current = Date.now();
      setNow(current);

      if (current >= endsAt) {
        setEndsAt(null);
      }
    }, 250);

    return () => clearInterval(timer);
  }, [endsAt]);

  const start = useCallback((seconds: number) => {
    const current = Date.now();
    setNow(current);
    setEndsAt(current + seconds * 1000);
  }, []);

  const stop = useCallback(() => setEndsAt(null), []);

  const left = endsAt === null ? 0 : Math.max(0, Math.ceil((endsAt - now) / 1000));

  return { left, start, stop };
}

export function formatClock(seconds: number): string {
  const minutes = Math.floor(seconds / 60);

  return `${String(minutes).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`;
}
