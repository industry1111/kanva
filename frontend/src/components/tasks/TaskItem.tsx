import type { TaskForDisplay } from './TaskList';
import type { TaskStatus } from '../../types/api';

interface TaskItemProps {
  task: TaskForDisplay;
  onDelete?: (id: number) => void;
  onClick?: (id: number) => void;
}

const statusConfig: Record<TaskStatus, { label: string; color: string; bgColor: string }> = {
  PENDING: { label: 'Pending', color: '#6b7280', bgColor: '#f3f4f6' },
  IN_PROGRESS: { label: 'In Progress', color: '#2563eb', bgColor: '#dbeafe' },
  COMPLETED: { label: 'Completed', color: '#059669', bgColor: '#d1fae5' },
};

export default function TaskItem({ task, onDelete, onClick }: TaskItemProps) {
  const statusStyle = statusConfig[task.status];

  return (
    <div style={styles.container}>
      <span
        style={{
          ...styles.title,
          textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none',
          opacity: task.status === 'COMPLETED' ? 0.5 : 1,
          cursor: onClick ? 'pointer' : 'default',
        }}
        onClick={() => onClick?.(task.id)}
      >
        {task.title}
      </span>
      <div style={styles.actions}>
        <span
          style={{
            ...styles.statusBadge,
            color: statusStyle.color,
            backgroundColor: statusStyle.bgColor,
          }}
        >
          {statusStyle.label}
        </span>
        {onDelete && (
          <button
            onClick={() => onDelete(task.id)}
            style={styles.deleteButton}
            title="삭제"
          >
            ×
          </button>
        )}
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '12px',
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    marginBottom: '8px',
  },
  title: {
    fontSize: '14px',
    flex: 1,
  },
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  statusBadge: {
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 500,
  },
  deleteButton: {
    width: '28px',
    height: '28px',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '16px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#fee2e2',
    color: '#ef4444',
    transition: 'all 0.2s',
  },
};
