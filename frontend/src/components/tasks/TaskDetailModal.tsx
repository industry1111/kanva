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
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      onClick={handleBackdropClick}
    >
      <div className="bg-white rounded-xl p-4 w-[380px] max-w-[90vw] max-h-[90vh] overflow-auto shadow-xl mx-4">
        <div className="flex justify-between items-center mb-3">
          <h2 className="m-0 text-base font-semibold text-text">할 일 상세</h2>
          <button
            onClick={onClose}
            className="w-7 h-7 border-none rounded-md bg-transparent cursor-pointer text-xl text-text-secondary flex items-center justify-center transition-colors hover:bg-bg"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <label className="text-[13px] font-medium text-text">제목 *</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full py-1.5 px-2.5 border border-border rounded-lg text-[13px] outline-none box-border"
              placeholder="할 일을 입력하세요"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[13px] font-medium text-text">설명</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full py-1.5 px-2.5 border border-border rounded-lg text-[13px] outline-none resize-y font-[inherit] box-border"
              placeholder="상세 설명 (선택)"
              rows={2}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[13px] font-medium text-text">상태</label>
            <div className="flex gap-3">
              <label className="flex items-center gap-1.5 text-[13px] text-text cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="PENDING"
                  checked={status === 'PENDING' || status === 'IN_PROGRESS'}
                  onChange={() => setStatus('PENDING')}
                  className="w-3.5 h-3.5 cursor-pointer"
                />
                미완료
              </label>
              <label className="flex items-center gap-1.5 text-[13px] text-text cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="COMPLETED"
                  checked={status === 'COMPLETED'}
                  onChange={() => setStatus('COMPLETED')}
                  className="w-3.5 h-3.5 cursor-pointer"
                />
                완료
              </label>
            </div>
          </div>

          {/* 반복 설정 섹션 */}
          <div className="p-3 bg-bg rounded-lg flex flex-col gap-2.5">
            <div className="flex flex-col gap-1">
              <label className="flex items-center gap-2 text-[13px] font-medium text-text cursor-pointer">
                <input
                  type="checkbox"
                  checked={repeatDaily}
                  onChange={(e) => setRepeatDaily(e.target.checked)}
                  className="w-4 h-4 cursor-pointer"
                  disabled={isSeriesTask}
                />
                매일 반복
                {isSeriesTask && (
                  <span className="ml-1.5 px-1.5 py-0.5 bg-primary/10 text-primary rounded text-[11px] font-medium">시리즈</span>
                )}
              </label>
            </div>

            {repeatDaily && (
              <>
                <div className="flex flex-col gap-1">
                  <label className="text-[13px] font-medium text-text">반복 종료일 *</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full py-1.5 px-2.5 border border-border rounded-lg text-[13px] outline-none box-border"
                    disabled={isSeriesTask}
                  />
                </div>

                <div className="flex flex-col gap-1">
                  <label className="flex items-center gap-2 text-[13px] font-medium text-text cursor-pointer">
                    <input
                      type="checkbox"
                      checked={stopOnComplete}
                      onChange={(e) => setStopOnComplete(e.target.checked)}
                      className="w-4 h-4 cursor-pointer"
                      disabled={isSeriesTask}
                    />
                    완료 시 반복 중단
                  </label>
                  <p className="m-0 text-[11px] text-text-secondary leading-snug">
                    {stopOnComplete
                      ? '완료하면 이후 날짜의 Task가 더 이상 생성되지 않습니다.'
                      : '완료해도 매일 새로운 Task가 계속 생성됩니다.'}
                  </p>
                </div>
              </>
            )}
          </div>
        </div>

        <div className="flex gap-2.5 mt-4">
          <button
            onClick={onClose}
            className="flex-1 py-1.5 bg-bg text-text border border-border rounded-lg cursor-pointer text-[13px] font-medium transition-colors hover:bg-border/30"
          >
            취소
          </button>
          <button
            onClick={handleSave}
            className="flex-1 py-1.5 bg-primary text-white border-none rounded-lg cursor-pointer text-[13px] font-medium transition-colors hover:bg-primary-hover"
          >
            저장
          </button>
        </div>
      </div>
    </div>
  );
}
