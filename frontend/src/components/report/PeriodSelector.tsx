interface PeriodSelectorProps {
  startDate: string;
  endDate: string;
  onChangeStart: (date: string) => void;
  onChangeEnd: (date: string) => void;
  disabled?: boolean;
}

export default function PeriodSelector({
  startDate,
  endDate,
  onChangeStart,
  onChangeEnd,
  disabled = false,
}: PeriodSelectorProps) {
  return (
    <div className="period-selector">
      <h3 className="period-selector-title">기간 선택</h3>
      <div className="period-date-range">
        <input
          type="date"
          className="period-date-input"
          value={startDate}
          onChange={(e) => onChangeStart(e.target.value)}
          disabled={disabled}
        />
        <span className="period-date-separator">~</span>
        <input
          type="date"
          className="period-date-input"
          value={endDate}
          onChange={(e) => onChangeEnd(e.target.value)}
          disabled={disabled}
        />
      </div>
    </div>
  );
}
