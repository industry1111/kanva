interface MonthSelectorProps {
  selectedMonth: string; // 'YYYY-MM' format
  onSelectMonth: (month: string) => void;
}

export default function MonthSelector({ selectedMonth, onSelectMonth }: MonthSelectorProps) {
  const [year, month] = selectedMonth.split('-').map(Number);

  const changeMonth = (delta: number) => {
    const date = new Date(year, month - 1 + delta, 1);
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    onSelectMonth(`${y}-${m}`);
  };

  const isCurrentMonth = (): boolean => {
    const today = new Date();
    return year === today.getFullYear() && month === today.getMonth() + 1;
  };

  const goToCurrentMonth = () => {
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, '0');
    onSelectMonth(`${y}-${m}`);
  };

  return (
    <div className="flex items-center gap-3 py-1">
      <button
        className="flex items-center justify-center w-7 h-7 border border-border rounded-md bg-white text-text text-sm cursor-pointer transition-colors hover:bg-bg"
        onClick={() => changeMonth(-1)}
        title="이전 달"
      >
        ←
      </button>
      <button
        className={`text-[13px] font-semibold bg-transparent border-none px-2 py-0.5 rounded-md ${
          isCurrentMonth()
            ? 'text-text'
            : 'text-primary cursor-pointer hover:bg-bg'
        }`}
        onClick={goToCurrentMonth}
        disabled={isCurrentMonth()}
        title={isCurrentMonth() ? '' : '이번 달로 이동'}
      >
        {year}년 {month}월
      </button>
      <button
        className="flex items-center justify-center w-7 h-7 border border-border rounded-md bg-white text-text text-sm cursor-pointer transition-colors hover:bg-bg"
        onClick={() => changeMonth(1)}
        title="다음 달"
      >
        →
      </button>
    </div>
  );
}
