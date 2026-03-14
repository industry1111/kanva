import { useState } from 'react';

interface DateBadgeProps {
  selectedDate: string;
  onSelectDate: (date: string) => void;
}

function getWeekDates(baseDate: string): string[] {
  const dates: string[] = [];
  const base = new Date(baseDate);
  const dayOfWeek = base.getDay(); // 0 = 일요일

  // 해당 주의 일요일 찾기
  const sunday = new Date(base);
  sunday.setDate(base.getDate() - dayOfWeek);

  // 일요일부터 토요일까지
  for (let i = 0; i < 7; i++) {
    const d = new Date(sunday);
    d.setDate(sunday.getDate() + i);
    dates.push(d.toISOString().split('T')[0]);
  }
  return dates;
}

function formatDay(dateStr: string): { day: number; weekday: string } {
  const date = new Date(dateStr);
  const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
  return {
    day: date.getDate(),
    weekday: weekdays[date.getDay()],
  };
}

function formatWeekLabel(dates: string[]): string {
  const start = new Date(dates[0]);
  const end = new Date(dates[6]);
  const startMonth = start.getMonth() + 1;
  const endMonth = end.getMonth() + 1;

  if (startMonth === endMonth) {
    return `${start.getFullYear()}년 ${startMonth}월`;
  }
  return `${startMonth}월 - ${endMonth}월`;
}

function isToday(dateStr: string): boolean {
  return dateStr === new Date().toISOString().split('T')[0];
}

export default function DateBadge({ selectedDate, onSelectDate }: DateBadgeProps) {
  const [weekBase, setWeekBase] = useState(new Date().toISOString().split('T')[0]);
  const dates = getWeekDates(weekBase);

  const goToPrevWeek = () => {
    const current = new Date(weekBase);
    current.setDate(current.getDate() - 7);
    setWeekBase(current.toISOString().split('T')[0]);
  };

  const goToNextWeek = () => {
    const current = new Date(weekBase);
    current.setDate(current.getDate() + 7);
    setWeekBase(current.toISOString().split('T')[0]);
  };

  const goToToday = () => {
    const today = new Date().toISOString().split('T')[0];
    setWeekBase(today);
    onSelectDate(today);
  };

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-1.5">
        <span className="text-[13px] font-semibold text-text">{formatWeekLabel(dates)}</span>
        <div className="flex gap-1.5">
          <button
            className="py-0.5 px-2.5 border border-border rounded-md bg-white text-text text-xs font-medium cursor-pointer transition-colors hover:bg-bg"
            onClick={goToPrevWeek}
          >
            ←
          </button>
          <button
            className="py-0.5 px-2.5 bg-primary border border-primary text-white rounded-md text-xs font-medium cursor-pointer transition-colors hover:bg-primary-hover"
            onClick={goToToday}
          >
            오늘
          </button>
          <button
            className="py-0.5 px-2.5 border border-border rounded-md bg-white text-text text-xs font-medium cursor-pointer transition-colors hover:bg-bg"
            onClick={goToNextWeek}
          >
            →
          </button>
        </div>
      </div>
      <div className="flex gap-1.5 justify-between">
        {dates.map((date) => {
          const { day, weekday } = formatDay(date);
          const isSelected = date === selectedDate;
          const isTodayDate = isToday(date);

          const baseClasses = 'flex-1 flex flex-col items-center justify-center h-[44px] border rounded-lg cursor-pointer transition-colors';
          const stateClasses = isSelected
            ? 'bg-primary border-primary text-white'
            : isTodayDate
              ? 'bg-white border-primary text-primary hover:bg-bg'
              : 'bg-white border-border text-text hover:bg-bg';

          return (
            <button
              key={date}
              onClick={() => onSelectDate(date)}
              className={`${baseClasses} ${stateClasses}`}
            >
              <span className="text-[10px] font-medium opacity-70">{weekday}</span>
              <span className="text-base font-semibold">{day}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
