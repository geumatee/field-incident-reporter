interface PhotoPreviewModalProps {
  photo: { url: string; title: string } | null;
  onClose: () => void;
}

export default function PhotoPreviewModal({
  photo,
  onClose,
}: PhotoPreviewModalProps) {
  if (!photo) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4"
      onClick={onClose}
    >
      <div
        className="max-w-lg rounded-lg bg-white p-4 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-900">{photo.title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded border border-slate-300 px-2 py-0.5 text-xs text-slate-600 hover:bg-slate-100"
          >
            Close
          </button>
        </div>
        <img
          src={photo.url}
          alt={photo.title}
          className="max-h-96 w-full rounded border border-slate-200 object-contain"
        />
      </div>
    </div>
  );
}
