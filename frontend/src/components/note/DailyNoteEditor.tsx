import { useState, useRef, useImperativeHandle, forwardRef, useEffect } from 'react';
import Markdown from 'react-markdown';

interface DailyNoteEditorProps {
  content: string;
  onSave: (content: string) => void;
}

export interface DailyNoteEditorRef {
  saveIfDirty: () => boolean;
}

type Mode = 'edit' | 'preview';

const DailyNoteEditor = forwardRef<DailyNoteEditorRef, DailyNoteEditorProps>(
  ({ content, onSave }, ref) => {
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
      <div style={styles.container}>
        <div style={styles.header}>
          <h2 style={styles.title}>
            Daily Note
            {isDirty && <span style={styles.dirtyIndicator}>*</span>}
          </h2>
          <div style={styles.toggleGroup}>
            <button
              onClick={() => setMode('edit')}
              style={{
                ...styles.toggleBtn,
                ...(mode === 'edit' ? styles.toggleBtnActive : {}),
              }}
            >
              Edit
            </button>
            <button
              onClick={handlePreviewClick}
              style={{
                ...styles.toggleBtn,
                ...(mode === 'preview' ? styles.toggleBtnActive : {}),
              }}
            >
              Preview
            </button>
          </div>
        </div>

        {mode === 'edit' ? (
          <textarea
            value={localContent}
            onChange={(e) => setLocalContent(e.target.value)}
            onBlur={handleBlur}
            placeholder="오늘의 메모를 작성하세요... (Markdown 지원)"
            style={styles.textarea}
          />
        ) : (
          <div style={styles.preview}>
            {localContent ? (
              <Markdown>{localContent}</Markdown>
            ) : (
              <p style={styles.placeholder}>작성된 내용이 없습니다.</p>
            )}
          </div>
        )}
      </div>
    );
  }
);

DailyNoteEditor.displayName = 'DailyNoteEditor';

export default DailyNoteEditor;

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: '12px',
  },
  title: {
    margin: 0,
    fontSize: '18px',
    fontWeight: '600',
  },
  dirtyIndicator: {
    color: '#f59e0b',
    marginLeft: '4px',
  },
  toggleGroup: {
    display: 'flex',
    gap: '4px',
    backgroundColor: '#f3f4f6',
    padding: '4px',
    borderRadius: '8px',
  },
  toggleBtn: {
    padding: '6px 12px',
    border: 'none',
    borderRadius: '6px',
    backgroundColor: 'transparent',
    color: '#6b7280',
    cursor: 'pointer',
    fontSize: '13px',
    fontWeight: '500',
  },
  toggleBtnActive: {
    backgroundColor: '#ffffff',
    color: '#1f2937',
    boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
  },
  textarea: {
    flex: 1,
    padding: '12px',
    fontSize: '14px',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    resize: 'none',
    fontFamily: 'monospace',
    lineHeight: '1.6',
  },
  preview: {
    flex: 1,
    padding: '12px',
    fontSize: '14px',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    overflowY: 'auto',
    lineHeight: '1.6',
  },
  placeholder: {
    color: '#9ca3af',
    margin: 0,
  },
};
