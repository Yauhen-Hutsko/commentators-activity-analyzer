import { describe, expect, it } from 'vitest';
import { averageBetween, filterRange, percentChange } from './stats';

const series = [
  { month: '2020-06', count: 600 },
  { month: '2020-07', count: 400 },
  { month: '2020-08', count: 300 },
  { month: '2020-09', count: 100 },
];

describe('stats', () => {
  it('filters by inclusive month range, open bounds allowed', () => {
    expect(filterRange(series, '2020-07', '2020-08').map((r) => r.month)).toEqual(['2020-07', '2020-08']);
    expect(filterRange(series, undefined, '2020-06')).toHaveLength(1);
    expect(filterRange(series)).toHaveLength(4);
  });

  it('averages counts in range and returns null for empty range', () => {
    expect(averageBetween(series, '2020-06', '2020-07')).toBe(500);
    expect(averageBetween(series, '2021-01', '2021-12')).toBeNull();
  });

  it('computes percent change, null when undefined', () => {
    expect(percentChange(500, 200)).toBe(-60);
    expect(percentChange(0, 10)).toBeNull();
    expect(percentChange(null, 10)).toBeNull();
  });
});
