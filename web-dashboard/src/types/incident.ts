export type IncidentCategory = "EQUIPMENT" | "HAZARD" | "ELECTRICAL" | "OTHER";
export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH";
export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";

export interface Incident {
  id: string;
  title: string;
  description: string;
  category: IncidentCategory;
  severity: IncidentSeverity;
  status: IncidentStatus;
  location: string;
  imageUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export const CATEGORY_LABELS: Record<IncidentCategory, string> = {
  EQUIPMENT: "Equipment",
  HAZARD: "Road / Facility Hazard",
  ELECTRICAL: "Electrical",
  OTHER: "Other",
};

export const SEVERITY_BADGE_CLASSES: Record<IncidentSeverity, string> = {
  HIGH: "bg-red-100 text-red-800 border-red-200",
  MEDIUM: "bg-amber-100 text-amber-800 border-amber-200",
  LOW: "bg-slate-100 text-slate-700 border-slate-200",
};

export const STATUS_BADGE_CLASSES: Record<IncidentStatus, string> = {
  OPEN: "bg-red-50 text-red-700 border-red-200",
  IN_PROGRESS: "bg-blue-50 text-blue-700 border-blue-200",
  RESOLVED: "bg-emerald-50 text-emerald-700 border-emerald-200",
};
