interface IncidentFilterBarProps {
  searchQuery: string;
  statusFilter: string;
  severityFilter: string;
  categoryFilter: string;
  onSearchQueryChange: (value: string) => void;
  onStatusFilterChange: (value: string) => void;
  onSeverityFilterChange: (value: string) => void;
  onCategoryFilterChange: (value: string) => void;
}

export default function IncidentFilterBar({
  searchQuery,
  statusFilter,
  severityFilter,
  categoryFilter,
  onSearchQueryChange,
  onStatusFilterChange,
  onSeverityFilterChange,
  onCategoryFilterChange,
}: IncidentFilterBarProps) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="grid grid-cols-1 gap-3 md:grid-cols-5">
        <div className="md:col-span-2">
          <label className="block text-xs font-medium text-slate-600">
            Search Title or Location
          </label>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchQueryChange(e.target.value)}
            placeholder="Filter by keyword or building..."
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-600">
            Status
          </label>
          <select
            value={statusFilter}
            onChange={(e) => onStatusFilterChange(e.target.value)}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-600">
            Severity
          </label>
          <select
            value={severityFilter}
            onChange={(e) => onSeverityFilterChange(e.target.value)}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none"
          >
            <option value="ALL">All Severities</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-600">
            Category
          </label>
          <select
            value={categoryFilter}
            onChange={(e) => onCategoryFilterChange(e.target.value)}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none"
          >
            <option value="ALL">All Categories</option>
            <option value="EQUIPMENT">Equipment</option>
            <option value="HAZARD">Road / Facility Hazard</option>
            <option value="ELECTRICAL">Electrical</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
      </div>
    </section>
  );
}
