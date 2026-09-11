export function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function formatRelativeTime(value: string): string {
  const date = new Date(value);
  const now = Date.now();
  const diffSeconds = Math.round((date.getTime() - now) / 1000);
  const absSeconds = Math.abs(diffSeconds);

  const ranges: { limit: number; divisor: number; unit: Intl.RelativeTimeFormatUnit }[] = [
    { limit: 60, divisor: 1, unit: 'second' },
    { limit: 3600, divisor: 60, unit: 'minute' },
    { limit: 86400, divisor: 3600, unit: 'hour' },
    { limit: 604800, divisor: 86400, unit: 'day' },
    { limit: 2629800, divisor: 604800, unit: 'week' },
    { limit: 31557600, divisor: 2629800, unit: 'month' },
    { limit: Infinity, divisor: 31557600, unit: 'year' },
  ];

  const formatter = new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' });

  for (const range of ranges) {
    if (absSeconds < range.limit) {
      const value = Math.round(diffSeconds / range.divisor);
      return formatter.format(value, range.unit);
    }
  }

  return formatDateTime(value);
}

export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
    return text;
  }
  return `${text.slice(0, maxLength - 1)}…`;
}
