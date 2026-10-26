"use client";

import { useEffect, useState } from "react";
import { Incident } from "@/types/incident";
import {
  API_URL,
  deleteIncident,
  getIncidents,
  updateIncident,
} from "@/services/incidentApi";
import KpiSummaryCards from "@/components/KpiSummaryCards";
import IncidentFilterBar from "@/components/IncidentFilterBar";
import IncidentTable from "@/components/IncidentTable";
import PhotoPreviewModal from "@/components/PhotoPreviewModal";

export default function OperatorDashboardPage() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [previewImage, setPreviewImage] = useState<{
    url: string;
    title: string;
  } | null>(null);

  const [searchQuery, setSearchQuery] = useState<string>("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [severityFilter, setSeverityFilter] = useState<string>("ALL");
  const [categoryFilter, setCategoryFilter] = useState<string>("ALL");

  async function loadIncidents() {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getIncidents();
      setIncidents(data);
    } catch (err) {
      setError(
        err instanceof Error
          ? `Unable to reach backend at ${API_URL} (${err.message})`
          : "Unable to load incidents"
      );
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadIncidents();
  }, []);

  async function handleUpdateIncident(
    id: string,
    patch: Partial<Pick<Incident, "status" | "severity">>
  ) {
    setUpdatingId(id);
    try {
      const updated = await updateIncident(id, patch);
      setIncidents((prev) =>
        prev.map((item) => (item.id === id ? updated : item))
      );
    } catch (err) {
      setError(
        err instanceof Error
          ? `Failed to update incident ${id}: ${err.message}`
          : "Failed to update incident"
      );
    } finally {
      setUpdatingId(null);
    }
  }

  async function handleDeleteIncident(id: string, title: string) {
    if (
      !window.confirm(
        `Delete incident report "${title}"? This action cannot be undone.`
      )
    ) {
      return;
    }
    setUpdatingId(id);
    try {
      await deleteIncident(id);
      setIncidents((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(
        err instanceof Error
          ? `Failed to delete incident ${id}: ${err.message}`
          : "Failed to delete incident"
      );
    } finally {
      setUpdatingId(null);
    }
  }

  const query = searchQuery.trim().toLowerCase();
  const filteredIncidents = incidents.filter((incident) => {
    if (statusFilter !== "ALL" && incident.status !== statusFilter) return false;
    if (severityFilter !== "ALL" && incident.severity !== severityFilter)
      return false;
    if (categoryFilter !== "ALL" && incident.category !== categoryFilter)
      return false;
    if (query.length > 0) {
      const matchesTitle = incident.title.toLowerCase().includes(query);
      const matchesLocation = incident.location.toLowerCase().includes(query);
      if (!matchesTitle && !matchesLocation) return false;
    }
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-xl font-semibold text-slate-900">
              Field Incident Triage Console
            </h1>
            <p className="text-sm text-slate-500">
              Review, prioritize, and resolve maintenance reports submitted from
              field devices
            </p>
          </div>
          <button
            type="button"
            onClick={loadIncidents}
            disabled={isLoading}
            className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {isLoading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-7xl space-y-6 px-6 py-6">
        <KpiSummaryCards incidents={incidents} />

        <IncidentFilterBar
          searchQuery={searchQuery}
          statusFilter={statusFilter}
          severityFilter={severityFilter}
          categoryFilter={categoryFilter}
          onSearchQueryChange={setSearchQuery}
          onStatusFilterChange={setStatusFilter}
          onSeverityFilterChange={setSeverityFilter}
          onCategoryFilterChange={setCategoryFilter}
        />

        {error && (
          <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
            <span>{error}</span>
            <button
              type="button"
              onClick={loadIncidents}
              className="rounded border border-red-300 bg-white px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
            >
              Retry
            </button>
          </div>
        )}

        <IncidentTable
          incidents={filteredIncidents}
          isLoading={isLoading}
          updatingId={updatingId}
          onUpdateIncident={handleUpdateIncident}
          onDeleteIncident={handleDeleteIncident}
          onPreviewPhoto={setPreviewImage}
        />
      </main>

      <PhotoPreviewModal
        photo={previewImage}
        onClose={() => setPreviewImage(null)}
      />
    </div>
  );
}
