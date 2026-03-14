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
    <div className="bg-white rounded-xl p-4 shadow-sm">
      <h3 className="text-[13px] font-semibold text-text mb-1.5">기간 선택</h3>
      <div className="flex flex-col items-center gap-1.5">
        <input
          type="date"
          className="w-full min-w-0 py-1 px-2.5 border border-border rounded-lg bg-white text-text text-[13px] font-[inherit] cursor-pointer transition-colors box-border outline-none focus:border-primary disabled:opacity-50 disabled:cursor-not-allowed"
          value={startDate}
          onChange={(e) => onChangeStart(e.target.value)}
          disabled={disabled}
        />
        <span className="text-text-secondary text-xs font-medium shrink-0">~</span>
        <input
          type="date"
          className="w-full min-w-0 py-1 px-2.5 border border-border rounded-lg bg-white text-text text-[13px] font-[inherit] cursor-pointer transition-colors box-border outline-none focus:border-primary disabled:opacity-50 disabled:cursor-not-allowed"
          value={endDate}
          onChange={(e) => onChangeEnd(e.target.value)}
          disabled={disabled}
        />
      </div>
    </div>
  );
}
