import type { TaskForDisplay } from './TaskList';

interface TaskItemProps {
  task: TaskForDisplay;
  onDelete?: (id: number) => void;
  onClick?: (id: number) => void;
  onToggle?: (id: number) => void;
}

export default function TaskItem({ task, onDelete, onClick, onToggle }: TaskItemProps) {
  const isCompleted = task.status === 'COMPLETED';

  return (
    <div className="flex items-center gap-1.5 py-1 px-1.5 rounded-md group hover:bg-bg transition-colors">
      <span
        className={`flex-1 text-[13px] text-text ${isCompleted ? 'line-through opacity-50' : ''} ${onClick ? 'cursor-pointer' : ''}`}
        onClick={() => onClick?.(task.id)}
      >
        {task.title}
      </span>
      <div className="flex items-center gap-1.5">
        <button
          className="bg-transparent border-none cursor-pointer p-0 flex items-center gap-1"
          onClick={() => onToggle?.(task.id)}
          title={isCompleted ? '미완료로 변경' : '완료로 변경'}
        >
          <span
            className={`w-7 h-3.5 rounded-full relative inline-block transition-colors ${
              isCompleted ? 'bg-primary' : 'bg-border'
            }`}
          >
            <span
              className="absolute top-0.5 left-0.5 w-2.5 h-2.5 bg-white rounded-full transition-transform shadow-sm"
              style={{ transform: isCompleted ? 'translateX(14px)' : 'translateX(0)' }}
            />
          </span>
          <span
            className={`text-[11px] font-medium ${
              isCompleted ? 'text-primary' : 'text-text-secondary'
            }`}
          >
            {isCompleted ? '완료' : '미완료'}
          </span>
        </button>
        {onDelete && (
          <button
            onClick={() => onDelete(task.id)}
            className="text-gray-300 hover:text-red-500 hover:bg-red-50 bg-transparent border-none rounded px-1 py-0.5 text-[11px] cursor-pointer transition-colors"
            title="삭제"
          >
            x
          </button>
        )}
      </div>
    </div>
  );
}
