import type { DateRange } from '../lib/stats';

interface Props {
  ranges: DateRange[];
  selected: DateRange;
  onSelect: (range: DateRange) => void;
}

export function RangeToggle({ ranges, selected, onSelect }: Props) {
  return (
    <div className="range-toggle" role="group" aria-label="Date range">
      {ranges.map((range) => (
        <button
          key={range.label}
          type="button"
          className="btn btn-light"
          aria-pressed={range === selected}
          onClick={() => onSelect(range)}
        >
          {range.label}
        </button>
      ))}
    </div>
  );
}
