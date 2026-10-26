import React from "react";
import { Incident } from "@/types/incident";

interface KpiSummaryCardsProps {
  incidents: Incident[];
}

export default function KpiSummaryCards({ incidents }: KpiSummaryCardsProps) {
  const total = incidents.length;
  const open = incidents.filter((i) => i.status === "OPEN").length;
  const inProgress = incidents.filter((i) => i.status === "IN_PROGRESS").length;
  const highSeverityOpen = incidents.filter(
    (i) => i.severity === "HIGH" && i.status !== "RESOLVED"
  ).length;

  return (
    <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          Total Incidents
        </p>
        <p className="mt-2 text-2xl font-semibold text-slate-900">{total}</p>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          Open (Needs Triage)
        </p>
        <p className="mt-2 text-2xl font-semibold text-red-600">{open}</p>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          In Progress
        </p>
        <p className="mt-2 text-2xl font-semibold text-blue-600">{inProgress}</p>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          High Severity Open
        </p>
        <p className="mt-2 text-2xl font-semibold text-amber-600">
          {highSeverityOpen}
        </p>
      </div>
    </section>
  );
}
