import {
  Bar,
  BarChart,
  CartesianGrid,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { MonthlyCount } from '../api/analytics';
import type { ChartEvent } from '../lib/stats';
import { colors } from '../lib/theme';

interface Props {
  data: MonthlyCount[];
  events: ChartEvent[];
}

export function RegistrationsChart({ data, events }: Props) {
  const visibleMonths = new Set(data.map((d) => d.month));

  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 24, right: 12, bottom: 0, left: 0 }}>
          <CartesianGrid vertical={false} stroke={colors.grid} />
          <XAxis dataKey="month" tick={{ fill: colors.ink, fontSize: 12 }} minTickGap={24} tickLine={false} />
          <YAxis
            tick={{ fill: colors.ink, fontSize: 12 }}
            tickFormatter={(v: number) => v.toLocaleString()}
            label={{ value: 'Users', angle: -90, position: 'insideLeft', fill: colors.ink, fontSize: 12 }}
          />
          <Tooltip
            cursor={{ fill: 'rgba(31, 111, 122, 0.08)' }}
            formatter={(value: number) => [`${value.toLocaleString()} new users`, '']}
            separator=""
          />
          <Bar dataKey="count" fill={colors.bar} radius={[2, 2, 0, 0]} isAnimationActive={false} />
          {events
            .filter((e) => visibleMonths.has(e.month))
            .map((e) => (
              <ReferenceLine
                key={e.month}
                x={e.month}
                stroke={colors.event}
                strokeDasharray="5 4"
                strokeWidth={1.5}
                label={{ value: e.label, position: 'insideTopRight', fill: colors.event, fontSize: 12, fontWeight: 600 }}
              />
            ))}
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
