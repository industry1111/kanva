import { useState, useEffect } from 'react';

interface DailyProductivity {
  date: string;
  completed: number;
  total: number;
}

interface ProductivityChartProps {
  data: DailyProductivity[];
  selectedMonth: string;
}

export default function ProductivityChart({ data, selectedMonth }: ProductivityChartProps) {
  const [startIndex, setStartIndex] = useState(0);
  const VISIBLE_DAYS = 7;

  // Reset to current date position when month changes
  useEffect(() => {
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    // Find today's index in data, or last day if not found
    let todayIndex = data.findIndex((d) => d.date === todayStr);

    if (todayIndex === -1) {
      // If today is not in the data (different month), show last 7 days
      todayIndex = data.length - 1;
    }

    // Center the current date (show 3 days before and 3 days after)
    const newStartIndex = Math.max(0, Math.min(todayIndex - 3, data.length - VISIBLE_DAYS));
    setStartIndex(newStartIndex);
  }, [data, selectedMonth]);

  const visibleData = data.slice(startIndex, startIndex + VISIBLE_DAYS);
  const maxTotal = Math.max(...data.map((d) => d.total), 1);

  const getBarHeight = (value: number) => {
    return (value / maxTotal) * 100;
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.getDate()}`;
  };

  const formatWeekday = (dateStr: string) => {
    const date = new Date(dateStr);
    const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
    return weekdays[date.getDay()];
  };

  const isToday = (dateStr: string) => {
    const today = new Date().toISOString().split('T')[0];
    return dateStr === today;
  };

  const canGoPrev = startIndex > 0;
  const canGoNext = startIndex + VISIBLE_DAYS < data.length;

  const handlePrev = () => {
    setStartIndex(Math.max(0, startIndex - VISIBLE_DAYS));
  };

  const handleNext = () => {
    setStartIndex(Math.min(data.length - VISIBLE_DAYS, startIndex + VISIBLE_DAYS));
  };

  return (
    <div className="bg-white rounded-xl p-4 shadow-sm border border-border">
      <div className="flex items-center justify-between mb-3 pb-2 border-b border-border">
        <h3 className="text-sm font-semibold text-text">Productivity Chart</h3>
        <div className="flex items-center gap-1">
          <button
            className="flex items-center justify-center w-6 h-6 border border-border rounded bg-white text-text-secondary text-xs cursor-pointer transition-colors hover:bg-bg disabled:opacity-40 disabled:cursor-not-allowed"
            onClick={handlePrev}
            disabled={!canGoPrev}
          >
            ◀
          </button>
          <button
            className="flex items-center justify-center w-6 h-6 border border-border rounded bg-white text-text-secondary text-xs cursor-pointer transition-colors hover:bg-bg disabled:opacity-40 disabled:cursor-not-allowed"
            onClick={handleNext}
            disabled={!canGoNext}
          >
            ▶
          </button>
        </div>
      </div>

      <div>
        <div className="flex items-end justify-around gap-2 h-36">
          {visibleData.map((d) => (
            <div
              key={d.date}
              className={`flex flex-col items-center flex-1 gap-1 ${
                isToday(d.date) ? 'relative' : ''
              }`}
            >
              <div className="w-full flex justify-center items-end gap-0.5 h-24">
                <div
                  className="w-3.5 rounded-t-sm bg-border transition-all"
                  style={{ height: `${getBarHeight(d.total)}%` }}
                  title={`전체: ${d.total}`}
                >
                  {d.total > 0 && (
                    <span className="block text-center text-[9px] font-bold text-text-secondary -mt-3.5">
                      {d.total}
                    </span>
                  )}
                </div>
                <div
                  className="w-3.5 rounded-t-sm bg-primary transition-all"
                  style={{ height: `${getBarHeight(d.completed)}%` }}
                  title={`완료: ${d.completed}`}
                >
                  {d.completed > 0 && (
                    <span className="block text-center text-[9px] font-bold text-white -mt-3.5">
                      {d.completed}
                    </span>
                  )}
                </div>
              </div>
              <div className="flex flex-col items-center">
                <span
                  className={`text-xs font-bold ${
                    isToday(d.date) ? 'text-primary' : 'text-text'
                  }`}
                >
                  {formatDate(d.date)}
                </span>
                <span className="text-[10px] text-text-secondary">
                  {formatWeekday(d.date)}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div className="flex items-center justify-center gap-4 mt-3 pt-2 border-t border-border">
          <div className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-sm bg-border" />
            <span className="text-[11px] text-text-secondary">전체</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-sm bg-primary" />
            <span className="text-[11px] text-text-secondary">완료</span>
          </div>
        </div>
      </div>
    </div>
  );
}
