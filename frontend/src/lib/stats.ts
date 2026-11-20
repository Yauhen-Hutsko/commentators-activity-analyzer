import type { MonthlyCount } from '../api/analytics';

export interface ChartEvent {
  month: string;
  label: string;
}

export interface DateRange {
  label: string;
  from?: string;
  to?: string;
}

export const EVENTS: ChartEvent[] = [{ month: '2020-08', label: 'Election · 9 Aug 2020' }];

export const RANGES: DateRange[] = [
  { label: '2018 – 2023', from: '2018-01', to: '2023-12' },
  { label: 'All time' },
];

export const BEFORE_PERIOD = { from: '2019-08', to: '2020-07', label: 'Aug 2019 – Jul 2020' };
export const AFTER_PERIOD = { from: '2020-08', to: '2021-07', label: 'Aug 2020 – Jul 2021' };

/** Months are `YYYY-MM`, so string comparison equals chronological comparison. */
export function filterRange(series: MonthlyCount[], from?: string, to?: string): MonthlyCount[] {
  return series.filter((r) => (!from || r.month >= from) && (!to || r.month <= to));
}

export function averageBetween(series: MonthlyCount[], from: string, to: string): number | null {
  const rows = filterRange(series, from, to);
  if (rows.length === 0) return null;
  return rows.reduce((sum, r) => sum + r.count, 0) / rows.length;
}

/** Relative change in percent, or null when it cannot be computed. */
export function percentChange(before: number | null, after: number | null): number | null {
  if (before == null || after == null || before === 0) return null;
  return ((after - before) / before) * 100;
}
