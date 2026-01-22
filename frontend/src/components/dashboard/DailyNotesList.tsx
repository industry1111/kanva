interface DailyNoteItem {
  date: string;
  hasContent: boolean;
}

interface DailyNotesListProps {
  notes: DailyNoteItem[];
  onNoteClick?: (date: string) => void;
}

export default function DailyNotesList({ notes, onNoteClick }: DailyNotesListProps) {
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}/${day}`;
  };

  return (
    <div className="dashboard-card">
      <h3 className="dashboard-card-title">Daily Notes</h3>
      <ul className="notes-list">
        {notes.length === 0 ? (
          <li className="notes-list-empty">작성된 노트가 없습니다</li>
        ) : (
          notes.map((note) => (
            <li
              key={note.date}
              className="notes-list-item"
              onClick={() => onNoteClick?.(note.date)}
            >
              <span className="notes-indicator" />
              <span className="notes-date">Note {formatDate(note.date)}</span>
            </li>
          ))
        )}
      </ul>
    </div>
  );
}
