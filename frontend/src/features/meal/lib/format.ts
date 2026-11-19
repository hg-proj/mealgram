// 영양 비율 표시 문구

export function percent(value: number | null | undefined): string {
  return value == null ? '-' : `${Math.round(value)}%`;
}
