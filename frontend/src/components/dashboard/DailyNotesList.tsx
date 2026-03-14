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
    <div className="bg-white rounded-xl p-4 shadow-sm border border-border">
      <h3 className="text-sm font-semibold text-text mb-3 pb-2 border-b border-border">Daily Notes</h3>
      <ul className="list-none p-0 m-0 flex flex-col gap-1">
        {notes.length === 0 ? (
          <li className="text-sm text-text-secondary text-center py-4">작성된 노트가 없습니다</li>
        ) : (
          notes.map((note) => (
            <li
              key={note.date}
              className="flex items-center gap-2 py-1.5 px-2 rounded-md cursor-pointer transition-colors hover:bg-bg"
              onClick={() => onNoteClick?.(note.date)}
            >
              <span className="w-1.5 h-1.5 rounded-full bg-primary flex-shrink-0" />
              <span className="text-sm text-text">Note {formatDate(note.date)}</span>
            </li>
          ))
        )}
      </ul>
    </div>
  );
}
