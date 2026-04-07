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
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [editedResults, setEditedResults] = useState<ParsingResult[]>([]);

  useEffect(() => {
    setCheckedIndexes(new Set(results.map((_, i) => i)));
    setEditedResults([...results]);
    setEditingIndex(null);
  }, [results]);

  if (editedResults.length === 0) return null;

  const handleToggle = (index: number) => {
    setCheckedIndexes((prev) => {
      const next = new Set(prev);
      if (next.has(index)) next.delete(index);
      else next.add(index);
      return next;
    });
  };

  const handleSave = () => {
    onSave(editedResults.filter((_, i) => checkedIndexes.has(i)));
  };

  const handleEdit = (index: number, field: keyof ParsingResult, value: string | null) => {
    setEditedResults((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value } as ParsingResult;
      return next;
    });
  };

  const workItems = editedResults.map((r, i) => ({ result: r, index: i })).filter(({ result }) => result.type === 'WORK');
  const scheduleItems = editedResults.map((r, i) => ({ result: r, index: i })).filter(({ result }) => result.type === 'SCHEDULE');

  const categoryLabel = (category: string) => {
    switch (category) {
      case 'EXERCISE': return '운동';
      case 'OTHER': return '기타';
      default: return '업무';
    }
  };

  const renderItem = (result: ParsingResult, index: number) => {
    const isEditing = editingIndex === index;

    if (isEditing) {
      return (
        <div key={index} className="py-2.5 px-2 border-b border-gray-100 bg-gray-50 rounded">
          <div className="flex items-start gap-3">
            <input
              type="checkbox"
              checked={checkedIndexes.has(index)}
              onChange={() => handleToggle(index)}
              className="w-4 h-4 mt-1 shrink-0 cursor-pointer accent-primary"
            />
            <div className="flex-1 flex flex-col gap-2">
              <input
                type="text"
                value={result.title}
                onChange={(e) => handleEdit(index, 'title', e.target.value)}
                className="w-full px-2 py-1 text-[13px] border border-border rounded bg-white text-text"
                placeholder="제목"
              />
              <input
                type="text"
                value={result.description || ''}
                onChange={(e) => handleEdit(index, 'description', e.target.value || null)}
                className="w-full px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                placeholder="설명 (선택)"
              />
              <div className="flex gap-2 items-center flex-wrap">
                <select
                  value={result.type}
                  onChange={(e) => handleEdit(index, 'type', e.target.value)}
                  className="px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                >
                  <option value="WORK">할 일</option>
                  <option value="SCHEDULE">일정</option>
                </select>
                <select
                  value={result.category}
                  onChange={(e) => handleEdit(index, 'category', e.target.value)}
                  className="px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                >
                  <option value="WORK">업무</option>
                  <option value="EXERCISE">운동</option>
                  <option value="OTHER">기타</option>
                </select>
                <select
                  value={result.status}
                  onChange={(e) => handleEdit(index, 'status', e.target.value)}
                  className="px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                >
                  <option value="PENDING">미완료</option>
                  <option value="COMPLETED">완료</option>
                </select>
              </div>
              <div className="flex gap-2 items-center">
                <label className="text-[11px] text-text-secondary">마감일</label>
                <input
                  type="date"
                  value={result.dueDate || ''}
                  onChange={(e) => handleEdit(index, 'dueDate', e.target.value || null)}
                  className="px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                />
                <label className="text-[11px] text-text-secondary ml-2">시작일시</label>
                <input
                  type="datetime-local"
                  value={result.startDateTime || ''}
                  onChange={(e) => handleEdit(index, 'startDateTime', e.target.value || null)}
                  className="px-2 py-1 text-[11px] border border-border rounded bg-white text-text"
                />
              </div>
              <div className="flex justify-end">
                <button
                  onClick={() => setEditingIndex(null)}
                  className="px-2.5 py-1 text-[11px] bg-primary text-white border-none rounded cursor-pointer hover:bg-primary-hover"
                >
                  확인
                </button>
              </div>
            </div>
          </div>
        </div>
      );
    }

    return (
      <div
        key={index}
        className="flex items-start gap-3 py-2.5 px-2 border-b border-gray-100 hover:bg-gray-50 rounded cursor-pointer"
      >
        <input
          type="checkbox"
          checked={checkedIndexes.has(index)}
          onChange={() => handleToggle(index)}
          className="w-4 h-4 mt-0.5 shrink-0 cursor-pointer accent-primary"
        />
        <div
          className="flex-1 min-w-0"
          onClick={() => setEditingIndex(index)}
        >
          <div className="text-[13px] font-medium text-text flex items-center gap-1.5">
            {result.title}
            {result.status === 'COMPLETED' && (
              <span className="px-1.5 py-0.5 bg-green-100 text-green-800 text-[10px] rounded">완료</span>
            )}
            <span className="ml-auto text-[10px] text-text-secondary hover:text-primary">수정</span>
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
      </div>
    );
  };

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
    <Modal isOpen={isOpen} onClose={onClose} title="AI 추출 결과" width="w-[520px]" footer={footer}>
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
