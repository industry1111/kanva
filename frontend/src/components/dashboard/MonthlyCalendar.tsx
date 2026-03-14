import { useState } from 'react';

type CalendarItemType = 'series' | 'single' | 'schedule';
type FilterTab = 'all' | 'task' | 'schedule';

interface CalendarTask {
  title: string;
  type: CalendarItemType;
  status: 'completed' | 'pending';
  time?: string; // 일정용 시간 (e.g. "14:00")
}

interface CellData {
  day: number;
  currentMonth: boolean;
  dateStr: string;
}

interface SeriesBar {
  title: string;
  startCol: number;
  span: number;
  lane: number;
  segments: { status: string }[];
}

interface MonthlyCalendarProps {
  selectedMonth: string;
  onSelectDate?: (date: string) => void;
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

const FILTER_TABS: { key: FilterTab; label: string }[] = [
  { key: 'all', label: '전체' },
  { key: 'task', label: '할 일' },
  { key: 'schedule', label: '일정' },
];

const MOCK_TASKS: Record<string, CalendarTask[]> = {
  '2026-03-01': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
  ],
  '2026-03-02': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: 'Kanva 프론트 작업', type: 'single', status: 'completed' },
  ],
  '2026-03-03': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: '인터페이스 구현 회의', type: 'schedule', status: 'completed', time: '14:00' },
  ],
  '2026-03-04': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: 'CI/CD 구축', type: 'single', status: 'completed' },
  ],
  '2026-03-05': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: '블로그 글 작성', type: 'single', status: 'completed' },
    { title: 'BOM 관리 미팅', type: 'schedule', status: 'completed', time: '10:00' },
  ],
  '2026-03-06': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
  ],
  '2026-03-07': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
  ],
  '2026-03-08': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
  ],
  '2026-03-09': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: 'DB 스키마 설계', type: 'single', status: 'completed' },
    { title: '주간회의', type: 'schedule', status: 'completed', time: '15:00' },
  ],
  '2026-03-10': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: 'LLM API 연동', type: 'single', status: 'completed' },
  ],
  '2026-03-11': [
    { title: '알고리즘 1문제', type: 'series', status: 'completed' },
    { title: '캘린더 UI 구현', type: 'single', status: 'completed' },
  ],
  '2026-03-12': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
    { title: '대시보드 리팩토링', type: 'single', status: 'pending' },
    { title: '포트폴리오 정리', type: 'single', status: 'pending' },
    { title: 'LIMS 인터페이스 회의', type: 'schedule', status: 'pending', time: '11:00' },
  ],
  '2026-03-13': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
    { title: 'Slack 알림 테스트', type: 'single', status: 'pending' },
  ],
  '2026-03-14': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
  ],
  '2026-03-15': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
  ],
  '2026-03-16': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
    { title: '이력서 업데이트', type: 'single', status: 'pending' },
    { title: '코드리뷰', type: 'schedule', status: 'pending', time: '14:00' },
  ],
  '2026-03-17': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
  ],
  '2026-03-18': [
    { title: '알고리즘 1문제', type: 'series', status: 'pending' },
    { title: 'Redis 캐싱 적용', type: 'single', status: 'pending' },
  ],
  '2026-03-20': [
    { title: '스프린트 회고', type: 'schedule', status: 'pending', time: '16:00' },
  ],
};

const SEGMENT_COLORS: Record<string, string> = {
  completed: '#0F9D9A',
  pending: '#E2E8F0',
};

function filterTasks(tasks: CalendarTask[], filter: FilterTab): CalendarTask[] {
  if (filter === 'all') return tasks;
  if (filter === 'task') return tasks.filter((t) => t.type === 'series' || t.type === 'single');
  return tasks.filter((t) => t.type === 'schedule');
}

function getSeriesBars(weekCells: CellData[], filter: FilterTab): { bars: SeriesBar[]; laneCount: number } {
  if (filter === 'schedule') return { bars: [], laneCount: 0 };

  const seriesTitles: string[] = [];
  const seen = new Set<string>();

  weekCells.forEach((cell) => {
    if (!cell.currentMonth) return;
    (MOCK_TASKS[cell.dateStr] || [])
      .filter((t) => t.type === 'series')
      .forEach((t) => {
        if (!seen.has(t.title)) {
          seen.add(t.title);
          seriesTitles.push(t.title);
        }
      });
  });

  const bars: SeriesBar[] = [];

  seriesTitles.forEach((title, lane) => {
    let startIdx = -1;
    let segments: { status: string }[] = [];

    const flush = () => {
      if (startIdx !== -1 && segments.length > 0) {
        bars.push({ title, startCol: startIdx + 1, span: segments.length, lane, segments });
      }
      startIdx = -1;
      segments = [];
    };

    weekCells.forEach((cell, idx) => {
      if (!cell.currentMonth) {
        flush();
        return;
      }

      const seriesTask = (MOCK_TASKS[cell.dateStr] || []).find(
        (t) => t.type === 'series' && t.title === title,
      );

      if (seriesTask) {
        if (startIdx === -1) startIdx = idx;
        segments.push({ status: seriesTask.status });
      } else {
        flush();
      }
    });

    flush();
  });

  return { bars, laneCount: seriesTitles.length };
}

export default function MonthlyCalendar({ selectedMonth, onSelectDate }: MonthlyCalendarProps) {
  const [activeFilter, setActiveFilter] = useState<FilterTab>('all');
  const [year, month] = selectedMonth.split('-').map(Number);

  const today = new Date();
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

  const firstDay = new Date(year, month - 1, 1);
  const lastDay = new Date(year, month, 0);
  const startDayOfWeek = firstDay.getDay();
  const totalDays = lastDay.getDate();
  const prevMonthLastDay = new Date(year, month - 1, 0).getDate();

  const cells: CellData[] = [];

  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    const d = prevMonthLastDay - i;
    const prevM = month - 1 <= 0 ? 12 : month - 1;
    const prevY = month - 1 <= 0 ? year - 1 : year;
    cells.push({ day: d, currentMonth: false, dateStr: `${prevY}-${String(prevM).padStart(2, '0')}-${String(d).padStart(2, '0')}` });
  }

  for (let d = 1; d <= totalDays; d++) {
    cells.push({ day: d, currentMonth: true, dateStr: `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}` });
  }

  const remaining = 42 - cells.length;
  for (let d = 1; d <= remaining; d++) {
    const nextM = month + 1 > 12 ? 1 : month + 1;
    const nextY = month + 1 > 12 ? year + 1 : year;
    cells.push({ day: d, currentMonth: false, dateStr: `${nextY}-${String(nextM).padStart(2, '0')}-${String(d).padStart(2, '0')}` });
  }

  const allWeeks: CellData[][] = [];
  for (let i = 0; i < cells.length; i += 7) {
    allWeeks.push(cells.slice(i, i + 7));
  }

  // 마지막 주가 전부 다음달 날짜면 제거
  const weeks = allWeeks.filter(
    (week) => week.some((cell) => cell.currentMonth),
  );

  return (
    <div className="bg-white rounded-xl shadow-sm border border-border overflow-hidden">
      {/* 필터 탭 */}
      <div className="flex gap-0.5 py-1 px-2 border-b border-border bg-bg">
        {FILTER_TABS.map((tab) => (
          <button
            key={tab.key}
            className={`py-0.5 px-2.5 border-none rounded text-xs font-medium cursor-pointer transition-colors ${
              activeFilter === tab.key
                ? 'bg-primary text-white'
                : 'bg-transparent text-text-secondary hover:bg-bg hover:text-text'
            }`}
            onClick={() => setActiveFilter(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-7 bg-bg border-b border-border">
        {WEEKDAYS.map((day, i) => (
          <div
            key={day}
            className={`text-center text-[10px] font-semibold py-1 tracking-wider uppercase ${
              i === 0 ? 'text-danger' : i === 6 ? 'text-primary' : 'text-text-secondary'
            }`}
          >
            {day}
          </div>
        ))}
      </div>

      <div>
        {weeks.map((week, weekIdx) => {
          const { bars, laneCount } = getSeriesBars(week, activeFilter);

          return (
            <div
              key={weekIdx}
              className={`flex flex-col border-b border-border min-h-[96px] ${
                weekIdx === weeks.length - 1 ? 'border-b-0' : ''
              }`}
            >
              <div className="grid grid-cols-7">
                {week.map((cell, dayIdx) => {
                  const isToday = cell.dateStr === todayStr;
                  return (
                    <div
                      key={cell.dateStr}
                      className={`py-0.5 px-1.5 cursor-pointer transition-colors hover:bg-bg ${
                        !cell.currentMonth ? 'opacity-35' : ''
                      }`}
                      onClick={() => cell.currentMonth && onSelectDate?.(cell.dateStr)}
                    >
                      <span
                        className={`text-xs font-bold w-[22px] h-[22px] inline-flex items-center justify-center rounded-full transition-all ${
                          isToday
                            ? 'bg-primary text-white font-bold shadow-sm'
                            : cell.currentMonth && dayIdx === 0
                              ? 'text-danger'
                              : cell.currentMonth && dayIdx === 6
                                ? 'text-primary'
                                : 'text-text'
                        }`}
                      >
                        {cell.day}
                      </span>
                    </div>
                  );
                })}
              </div>

              {laneCount > 0 && (
                <div
                  className="grid grid-cols-7 py-0.5"
                  style={{ gridTemplateRows: `repeat(${laneCount}, 20px)` }}
                >
                  {bars.map((bar, i) => (
                    <div
                      key={i}
                      className="relative overflow-hidden cursor-pointer transition-transform hover:-translate-y-px hover:shadow-md"
                      style={{
                        gridColumn: `${bar.startCol} / span ${bar.span}`,
                        gridRow: bar.lane + 1,
                      }}
                    >
                      <div className="flex h-full">
                        {bar.segments.map((seg, j) => (
                          <div
                            key={j}
                            className="flex-1 min-w-0"
                            style={{ backgroundColor: SEGMENT_COLORS[seg.status] || SEGMENT_COLORS.pending }}
                          />
                        ))}
                      </div>
                      <span
                        className="absolute top-0 left-0 right-0 px-1.5 text-[10px] font-semibold text-white leading-[20px] whitespace-nowrap overflow-hidden text-ellipsis pointer-events-none"
                        style={{ textShadow: '0 1px 2px rgba(0,0,0,0.3)' }}
                      >
                        {bar.title}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              <div className="grid grid-cols-7 flex-1">
                {week.map((cell) => {
                  const allTasks = cell.currentMonth
                    ? filterTasks(MOCK_TASKS[cell.dateStr] || [], activeFilter)
                    : [];
                  const singles = allTasks.filter((t) => t.type === 'single');
                  const schedules = allTasks.filter((t) => t.type === 'schedule');

                  return (
                    <div key={cell.dateStr} className="flex flex-col gap-px py-px px-0.5">
                      {schedules.map((task, i) => (
                        <div
                          key={`s-${i}`}
                          className={`py-1 px-2 rounded-md text-xs whitespace-nowrap overflow-hidden text-ellipsis leading-snug cursor-pointer transition-all border-l-2 ${
                            task.status === 'completed'
                              ? 'bg-bg text-text border-l-success line-through decoration-border'
                              : 'bg-bg text-text-secondary border-l-border'
                          }`}
                        >
                          <span className="font-semibold mr-0.5 text-[10px]">{task.time}</span>
                          {task.title}
                        </div>
                      ))}
                      {singles.map((task, i) => (
                        <div
                          key={`t-${i}`}
                          className={`py-1 px-2 rounded-md text-xs whitespace-nowrap overflow-hidden text-ellipsis leading-snug cursor-pointer transition-all border-l-2 ${
                            task.status === 'completed'
                              ? 'bg-bg text-text border-l-primary line-through decoration-border'
                              : 'bg-bg text-text-secondary border-l-border'
                          }`}
                        >
                          {task.title}
                        </div>
                      ))}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
