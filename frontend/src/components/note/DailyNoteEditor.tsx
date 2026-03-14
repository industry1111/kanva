import { useState, useRef, useImperativeHandle, forwardRef, useEffect } from 'react';
import Markdown from 'react-markdown';

interface DailyNoteEditorProps {
  content: string;
  onSave: (content: string) => void;
  onExtract?: (content: string) => void;
}

export interface DailyNoteEditorRef {
  saveIfDirty: () => boolean;
}

type Mode = 'edit' | 'preview';

const DailyNoteEditor = forwardRef<DailyNoteEditorRef, DailyNoteEditorProps>(
  ({ content, onSave, onExtract }, ref) => {
    const [mode, setMode] = useState<Mode>('preview');
    const [localContent, setLocalContent] = useState(content);
    const originalContentRef = useRef(content);

    // content prop이 변경되면 (날짜 변경 등) 로컬 상태 동기화
    useEffect(() => {
      setLocalContent(content);
      originalContentRef.current = content;
    }, [content]);

    const getIsDirty = () => localContent !== originalContentRef.current;

    const saveIfDirty = (): boolean => {
      if (localContent !== originalContentRef.current) {
        onSave(localContent);
        originalContentRef.current = localContent;
        return true;
      }
      return false;
    };

    const isDirty = getIsDirty();

    // 부모에서 호출 가능하도록 노출
    useImperativeHandle(ref, () => ({
      saveIfDirty,
    }));

    const handleBlur = () => {
      saveIfDirty();
    };

    const handlePreviewClick = () => {
      saveIfDirty();
      setMode('preview');
    };

    return (
      <div className="flex flex-col h-full">
        <div className="flex justify-between items-center mb-2">
          <h2 className="m-0 text-[13px] font-semibold text-text">
            오늘의 메모
            {isDirty && <span className="text-primary ml-1">*</span>}
          </h2>
          <div className="flex items-center gap-1.5">
            <div className="flex gap-0.5 bg-bg p-0.5 rounded-md">
              <button
                onClick={() => setMode('edit')}
                className={`px-2 py-0.5 border-none rounded text-[11px] font-medium cursor-pointer transition-colors ${
                  mode === 'edit'
                    ? 'bg-white text-text shadow-sm'
                    : 'bg-transparent text-text-secondary'
                }`}
              >
                편집
              </button>
              <button
                onClick={handlePreviewClick}
                className={`px-2 py-0.5 border-none rounded text-[11px] font-medium cursor-pointer transition-colors ${
                  mode === 'preview'
                    ? 'bg-white text-text shadow-sm'
                    : 'bg-transparent text-text-secondary'
                }`}
              >
                미리보기
              </button>
            </div>
            <button
              className={`px-2.5 py-1 border-none rounded-md bg-primary text-white text-[11px] font-semibold cursor-pointer whitespace-nowrap transition-opacity ${
                !localContent.trim() ? 'opacity-40 cursor-not-allowed' : ''
              }`}
              disabled={!localContent.trim()}
              onClick={() => {
                saveIfDirty();
                onExtract?.(localContent);
              }}
            >
              AI 추출
            </button>
          </div>
        </div>

        {mode === 'edit' ? (
          <textarea
            value={localContent}
            onChange={(e) => setLocalContent(e.target.value)}
            onBlur={handleBlur}
            placeholder="오늘의 메모를 작성하세요... (Markdown 지원)"
            className="flex-1 min-h-[260px] p-2.5 text-[13px] border border-border rounded-lg resize-none font-mono leading-relaxed text-text outline-none"
          />
        ) : (
          <div className="flex-1 min-h-[260px] p-2.5 text-[13px] border border-border rounded-lg overflow-y-auto leading-relaxed">
            {localContent ? (
              <Markdown>{localContent}</Markdown>
            ) : (
              <p className="text-text-secondary italic m-0">작성된 내용이 없습니다.</p>
            )}
          </div>
        )}
      </div>
    );
  }
);

DailyNoteEditor.displayName = 'DailyNoteEditor';

export default DailyNoteEditor;
