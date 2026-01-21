interface CalendarModalProps {
  isOpen: boolean;
  currentDate: string;
  onClose: () => void;
  onSelectDate: (date: string) => void;
}

export default function CalendarModal({
  isOpen,
  currentDate,
  onClose,
  onSelectDate,
}: CalendarModalProps) {
  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onSelectDate(e.target.value);
    onClose();
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div style={styles.backdrop} onClick={handleBackdropClick}>
      <div style={styles.modal}>
        <h3 style={styles.title}>날짜 선택</h3>
        <input
          type="date"
          value={currentDate}
          onChange={handleChange}
          style={styles.input}
        />
        <button onClick={onClose} style={styles.closeButton}>
          닫기
        </button>
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
    padding: '24px',
    borderRadius: '12px',
    minWidth: '280px',
    textAlign: 'center',
  },
  title: {
    margin: '0 0 16px 0',
    fontSize: '18px',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #d1d5db',
    borderRadius: '8px',
    marginBottom: '16px',
  },
  closeButton: {
    padding: '8px 24px',
    backgroundColor: '#6b7280',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
  },
};
