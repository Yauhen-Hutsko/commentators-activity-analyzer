import type { MonthlyCount } from '../api/analytics';
import { AFTER_PERIOD, BEFORE_PERIOD, averageBetween, percentChange } from '../lib/stats';

interface Props {
  series: MonthlyCount[];
  total: number;
}

const formatCount = (value: number | null) => (value == null ? 'n/a' : Math.round(value).toLocaleString());

export function MetricCards({ series, total }: Props) {
  const before = averageBetween(series, BEFORE_PERIOD.from, BEFORE_PERIOD.to);
  const after = averageBetween(series, AFTER_PERIOD.from, AFTER_PERIOD.to);
  const change = percentChange(before, after);

  return (
    <div className="metrics">
      <Metric label={`Avg / month, ${BEFORE_PERIOD.label}`} value={formatCount(before)} />
      <Metric label={`Avg / month, ${AFTER_PERIOD.label}`} value={formatCount(after)} />
      <Metric
        label="Change"
        value={change == null ? 'n/a' : `${change > 0 ? '+' : ''}${change.toFixed(1)}%`}
        tone={change != null && change < 0 ? 'down' : undefined}
      />
      <Metric label="Users with a registration date" value={total.toLocaleString()} />
    </div>
  );
}

function Metric({ label, value, tone }: { label: string; value: string; tone?: 'down' }) {
  return (
    <div className="metric">
      <span className="muted">{label}</span>
      <strong className={tone === 'down' ? 'delta-down' : undefined}>{value}</strong>
    </div>
  );
}
