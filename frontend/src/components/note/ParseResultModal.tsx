import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import type { ParsingResult } from '../../types/api';

interface ParseResultModalProps {
  isOpen: boolean;
  results: ParsingResult[];
  onClose: () => void;
  onSave: (selected: ParsingResult[]) => void;
  isSaving: boolean;
}

export default function ParseResultModal({
  isOpen,
  results,
  onClose,
  onSave,
  isSaving,
}: ParseResultModalProps) {
  const [checkedIndexes, setCheckedIndexes] = useState<Set<number>>(new Set());

  useEffect(() => {
    setCheckedIndexes(new Set(results.map((_, i) => i)));
  }, [results]);

  if (results.length === 0) return null;

  const handleToggle = (index: number) => {
    setCheckedIndexes((prev) => {
      const next = new Set(prev);
      if (next.has(index)) next.delete(index);
      else next.add(index);
      return next;
    });
  };

  const handleSave = () => {
    onSave(results.filter((_, i) => checkedIndexes.has(i)));
  };

  const workItems = results.map((r, i) => ({ result: r, index: i })).filter(({ result }) => result.type === 'WORK');
  const scheduleItems = results.map((r, i) => ({ result: r, index: i })).filter(({ result }) => result.type === 'SCHEDULE');

  const categoryLabel = (category: string) => {
    switch (category) {
      case 'EXERCISE': return '운동';
      case 'OTHER': return '기타';
      default: return '업무';
    }
  };

  const renderItem = (result: ParsingResult, index: number) => (
    <label key={index} className="flex items-start gap-3 py-2.5 px-2 border-b border-gray-100 cursor-pointer hover:bg-gray-50 rounded">
      <input
        type="checkbox"
        checked={checkedIndexes.has(index)}
        onChange={() => handleToggle(index)}
        className="w-4 h-4 mt-0.5 shrink-0 cursor-pointer accent-primary"
      />
      <div className="flex-1 min-w-0">
        <div className="text-[13px] font-medium text-text flex items-center gap-1.5">
          {result.title}
          {result.status === 'COMPLETED' && (
            <span className="px-1.5 py-0.5 bg-green-100 text-green-800 text-[10px] rounded">완료</span>
          )}
        </div>
        {result.description && (
          <div className="text-[11px] text-text-secondary mt-0.5 truncate">{result.description}</div>
        )}
        <div className="text-[11px] text-text-secondary mt-1 flex gap-2 items-center">
          <span className="px-1.5 py-0.5 bg-gray-100 rounded text-[10px]">{categoryLabel(result.category)}</span>
          {result.dueDate && <span>마감: {result.dueDate}</span>}
          {result.startDateTime && <span>시작: {result.startDateTime.replace('T', ' ')}</span>}
        </div>
      </div>
    </label>
  );

  const footer = (
    <div className="flex justify-between items-center">
      <span className="text-[12px] text-text-secondary">{checkedIndexes.size}개 선택됨</span>
      <div className="flex gap-2">
        <button
          onClick={onClose}
          className="px-3 py-1.5 bg-bg text-text border border-border rounded-md cursor-pointer text-[13px] font-medium hover:bg-border/30"
        >
          취소
        </button>
        <button
          onClick={handleSave}
          disabled={checkedIndexes.size === 0 || isSaving}
          className={`px-3 py-1.5 bg-primary text-white border-none rounded-md text-[13px] font-medium ${
            checkedIndexes.size === 0 || isSaving
              ? 'opacity-40 cursor-not-allowed'
              : 'cursor-pointer hover:bg-primary-hover'
          }`}
        >
          {isSaving ? '저장 중...' : '저장'}
        </button>
      </div>
    </div>
  );

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="AI 추출 결과" width="w-[460px]" footer={footer}>
      {workItems.length > 0 && (
        <div>
          <div className="text-[13px] font-semibold text-text mb-1.5 pb-1 border-b border-border">
            할 일 ({workItems.length})
          </div>
          {workItems.map(({ result, index }) => renderItem(result, index))}
        </div>
      )}

      {scheduleItems.length > 0 && (
        <div className={workItems.length > 0 ? 'mt-4' : ''}>
          <div className="text-[13px] font-semibold text-text mb-1.5 pb-1 border-b border-border">
            일정 ({scheduleItems.length})
          </div>
          {scheduleItems.map(({ result, index }) => renderItem(result, index))}
        </div>
      )}
    </Modal>
  );
}
