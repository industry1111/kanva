import { useState, useEffect } from 'react';
import type { Task, TaskRequest, TaskStatus } from '../../types/api';

interface TaskDetailModalProps {
  isOpen: boolean;
  task: Task | null;
  onClose: () => void;
  onSave: (taskId: number, request: TaskRequest) => void;
}

export default function TaskDetailModal({
  isOpen,
  task,
  onClose,
  onSave,
}: TaskDetailModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('PENDING');
  const [repeatDaily, setRepeatDaily] = useState(false);
  const [stopOnComplete, setStopOnComplete] = useState(false);
  const [endDate, setEndDate] = useState('');

  useEffect(() => {
    if (task) {
      setTitle(task.title);
      setDescription(task.description || '');
      setStatus(task.status);
      setRepeatDaily(task.repeatDaily || false);
      setStopOnComplete(task.stopOnComplete || false);
      setEndDate(task.endDate || '');
    }
  }, [task]);

  if (!isOpen || !task) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handleSave = () => {
    if (!title.trim()) {
      alert('제목을 입력해주세요.');
      return;
    }

    if (repeatDaily && !endDate) {
      alert('반복 종료일을 입력해주세요.');
      return;
    }

    const request: TaskRequest = {
      title: title.trim(),
      description: description.trim() || undefined,
      status,
      repeatDaily,
      stopOnComplete: repeatDaily ? stopOnComplete : undefined,
      endDate: repeatDaily ? endDate : undefined,
    };

    onSave(task.id, request);
    onClose();
  };

  // 이미 시리즈에 속한 Task는 반복 설정 변경 불가
  const isSeriesTask = !!task.seriesId;

  return (
    <div style={styles.backdrop} onClick={handleBackdropClick}>
      <div style={styles.modal}>
        <div style={styles.header}>
          <h2 style={styles.headerTitle}>Task Details</h2>
          <button
            onClick={onClose}
            style={styles.closeButton}
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <div style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Task Title *</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              style={styles.input}
              placeholder="할 일을 입력하세요"
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={styles.textarea}
              placeholder="상세 설명 (선택)"
              rows={3}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Status</label>
            <div style={styles.radioGroup}>
              <label style={styles.radioLabel}>
                <input
                  type="radio"
                  name="status"
                  value="PENDING"
                  checked={status === 'PENDING'}
                  onChange={() => setStatus('PENDING')}
                  style={styles.radio}
                />
                대기
              </label>
              <label style={styles.radioLabel}>
                <input
                  type="radio"
                  name="status"
                  value="IN_PROGRESS"
                  checked={status === 'IN_PROGRESS'}
                  onChange={() => setStatus('IN_PROGRESS')}
                  style={styles.radio}
                />
                진행 중
              </label>
              <label style={styles.radioLabel}>
                <input
                  type="radio"
                  name="status"
                  value="COMPLETED"
                  checked={status === 'COMPLETED'}
                  onChange={() => setStatus('COMPLETED')}
                  style={styles.radio}
                />
                완료
              </label>
            </div>
          </div>

          {/* 반복 설정 섹션 */}
          <div style={styles.repeatSection}>
            <div style={styles.formGroup}>
              <label style={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  checked={repeatDaily}
                  onChange={(e) => setRepeatDaily(e.target.checked)}
                  style={styles.checkbox}
                  disabled={isSeriesTask}
                />
                매일 반복
                {isSeriesTask && (
                  <span style={styles.seriesBadge}>시리즈</span>
                )}
              </label>
            </div>

            {repeatDaily && (
              <>
                <div style={styles.formGroup}>
                  <label style={styles.label}>반복 종료일 *</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    style={styles.input}
                    disabled={isSeriesTask}
                  />
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.checkboxLabel}>
                    <input
                      type="checkbox"
                      checked={stopOnComplete}
                      onChange={(e) => setStopOnComplete(e.target.checked)}
                      style={styles.checkbox}
                      disabled={isSeriesTask}
                    />
                    완료 시 반복 중단
                  </label>
                  <p style={styles.helpText}>
                    {stopOnComplete
                      ? '완료하면 이후 날짜의 Task가 더 이상 생성되지 않습니다.'
                      : '완료해도 매일 새로운 Task가 계속 생성됩니다.'}
                  </p>
                </div>
              </>
            )}
          </div>
        </div>

        <div style={styles.actions}>
          <button onClick={onClose} style={styles.cancelButton}>
            Cancel
          </button>
          <button onClick={handleSave} style={styles.saveButton}>
            Save
          </button>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  backdrop: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '24px',
    width: '400px',
    maxWidth: '90vw',
    maxHeight: '90vh',
    overflow: 'auto',
    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  headerTitle: {
    margin: 0,
    fontSize: '18px',
    fontWeight: 600,
    color: '#111827',
  },
  closeButton: {
    width: '32px',
    height: '32px',
    border: 'none',
    borderRadius: '6px',
    backgroundColor: 'transparent',
    cursor: 'pointer',
    fontSize: '24px',
    color: '#6b7280',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'background-color 0.2s',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '14px',
    fontWeight: 500,
    color: '#374151',
  },
  input: {
    width: '100%',
    padding: '10px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '14px',
    outline: 'none',
    boxSizing: 'border-box',
  },
  textarea: {
    width: '100%',
    padding: '10px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    fontSize: '14px',
    outline: 'none',
    resize: 'vertical',
    fontFamily: 'inherit',
    boxSizing: 'border-box',
  },
  radioGroup: {
    display: 'flex',
    gap: '16px',
  },
  radioLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '14px',
    color: '#374151',
    cursor: 'pointer',
  },
  radio: {
    width: '16px',
    height: '16px',
    cursor: 'pointer',
  },
  repeatSection: {
    padding: '16px',
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  checkboxLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '14px',
    fontWeight: 500,
    color: '#374151',
    cursor: 'pointer',
  },
  checkbox: {
    width: '18px',
    height: '18px',
    cursor: 'pointer',
  },
  seriesBadge: {
    marginLeft: '8px',
    padding: '2px 8px',
    backgroundColor: '#6366f1',
    color: 'white',
    fontSize: '12px',
    borderRadius: '4px',
  },
  helpText: {
    margin: 0,
    fontSize: '12px',
    color: '#6b7280',
    lineHeight: 1.4,
  },
  actions: {
    display: 'flex',
    gap: '12px',
    marginTop: '24px',
  },
  cancelButton: {
    flex: 1,
    padding: '10px',
    backgroundColor: '#6b7280',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 500,
    transition: 'background-color 0.2s',
  },
  saveButton: {
    flex: 1,
    padding: '10px',
    backgroundColor: '#6366f1',
    color: 'white',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 500,
    transition: 'background-color 0.2s',
  },
};
