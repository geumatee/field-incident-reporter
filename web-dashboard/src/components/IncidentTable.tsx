import {
  CATEGORY_LABELS,
  Incident,
  IncidentSeverity,
  IncidentStatus,
  SEVERITY_BADGE_CLASSES,
  SEVERITY_LABELS,
  STATUS_BADGE_CLASSES,
  STATUS_LABELS,
} from "@/types/incident";

interface IncidentTableProps {
  incidents: Incident[];
  isLoading: boolean;
  updatingId: string | null;
  onUpdateIncident: (
    id: string,
    patch: Partial<Pick<Incident, "status" | "severity">>
  ) => void;
  onDeleteIncident: (id: string, title: string) => void;
  onPreviewPhoto: (photo: { url: string; title: string }) => void;
}

function formatTimestamp(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso;
  return date.toLocaleString("en-US", {
    month: "short",
    day: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function IncidentTable({
  incidents,
  isLoading,
  updatingId,
  onUpdateIncident,
  onDeleteIncident,
  onPreviewPhoto,
}: IncidentTableProps) {
  if (isLoading && incidents.length === 0) {
    return (
      <section className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <div className="p-8 text-center text-sm text-slate-500">
          Loading incident reports...
        </div>
      </section>
    );
  }

  if (incidents.length === 0) {
    return (
      <section className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <div className="p-8 text-center text-sm text-slate-500">
          No incidents match the current filters.
        </div>
      </section>
    );
  }

  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
          <thead className="bg-slate-50 text-xs font-medium uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-4 py-3">Photo</th>
              <th className="px-4 py-3">Incident Details</th>
              <th className="px-4 py-3">Category &amp; Location</th>
              <th className="px-4 py-3">Severity</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {incidents.map((incident) => {
              const isRowUpdating = updatingId === incident.id;
              return (
                <tr
                  key={incident.id}
                  className="align-top hover:bg-slate-50/70"
                >
                  <td className="px-4 py-4">
                    {incident.imageUrl ? (
                      <button
                        type="button"
                        onClick={() =>
                          onPreviewPhoto({
                            url: incident.imageUrl as string,
                            title: incident.title,
                          })
                        }
                        className="block h-16 w-16 overflow-hidden rounded border border-slate-200 bg-slate-100"
                      >
                        <img
                          src={incident.imageUrl}
                          alt={incident.title}
                          className="h-full w-full object-cover"
                        />
                      </button>
                    ) : (
                      <div className="flex h-16 w-16 items-center justify-center rounded border border-slate-200 bg-slate-100 text-xs text-slate-400">
                        No Photo
                      </div>
                    )}
                  </td>

                  <td className="max-w-md px-4 py-4">
                    <div className="font-medium text-slate-900">
                      {incident.title}
                    </div>
                    <p className="mt-1 text-xs text-slate-600 line-clamp-3">
                      {incident.description || "No description provided."}
                    </p>
                    <p className="mt-1.5 text-xs text-slate-400">
                      Reported: {formatTimestamp(incident.createdAt)}
                    </p>
                  </td>

                  <td className="px-4 py-4">
                    <span className="inline-block rounded bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
                      {CATEGORY_LABELS[incident.category] ?? incident.category}
                    </span>
                    <div className="mt-1.5 text-xs text-slate-600">
                      {incident.location}
                    </div>
                  </td>

                  <td className="px-4 py-4">
                    <span
                      className={`inline-block rounded border px-2 py-0.5 text-xs font-medium ${
                        SEVERITY_BADGE_CLASSES[incident.severity] ??
                        SEVERITY_BADGE_CLASSES.MEDIUM
                      }`}
                    >
                      {SEVERITY_LABELS[incident.severity] ?? incident.severity}
                    </span>
                    <div className="mt-2">
                      <select
                        aria-label={`Severity for ${incident.title}`}
                        value={incident.severity}
                        disabled={isRowUpdating}
                        onChange={(e) =>
                          onUpdateIncident(incident.id, {
                            severity: e.target.value as IncidentSeverity,
                          })
                        }
                        className="rounded border border-slate-300 bg-white px-2 py-1 text-xs text-slate-700 disabled:opacity-50"
                      >
                        <option value="LOW">{SEVERITY_LABELS.LOW}</option>
                        <option value="MEDIUM">{SEVERITY_LABELS.MEDIUM}</option>
                        <option value="HIGH">{SEVERITY_LABELS.HIGH}</option>
                      </select>
                    </div>
                  </td>

                  <td className="px-4 py-4">
                    <span
                      className={`inline-block rounded border px-2 py-0.5 text-xs font-medium ${
                        STATUS_BADGE_CLASSES[incident.status] ??
                        STATUS_BADGE_CLASSES.OPEN
                      }`}
                    >
                      {STATUS_LABELS[incident.status] ?? incident.status}
                    </span>
                    <div className="mt-2">
                      <select
                        aria-label={`Status for ${incident.title}`}
                        value={incident.status}
                        disabled={isRowUpdating}
                        onChange={(e) =>
                          onUpdateIncident(incident.id, {
                            status: e.target.value as IncidentStatus,
                          })
                        }
                        className="rounded border border-slate-300 bg-white px-2 py-1 text-xs text-slate-700 disabled:opacity-50"
                      >
                        <option value="OPEN">{STATUS_LABELS.OPEN}</option>
                        <option value="IN_PROGRESS">{STATUS_LABELS.IN_PROGRESS}</option>
                        <option value="RESOLVED">{STATUS_LABELS.RESOLVED}</option>
                      </select>
                    </div>
                  </td>

                  <td className="px-4 py-4 text-right">
                    <div className="flex justify-end gap-2">
                      {incident.status !== "RESOLVED" && (
                        <button
                          type="button"
                          disabled={isRowUpdating}
                          onClick={() =>
                            onUpdateIncident(incident.id, {
                              status: "RESOLVED",
                            })
                          }
                          className="rounded border border-emerald-300 bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-800 hover:bg-emerald-100 disabled:opacity-50"
                        >
                          Resolve
                        </button>
                      )}
                      <button
                        type="button"
                        disabled={isRowUpdating}
                        onClick={() =>
                          onDeleteIncident(incident.id, incident.title)
                        }
                        className="rounded border border-slate-300 bg-white px-2.5 py-1 text-xs font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}
