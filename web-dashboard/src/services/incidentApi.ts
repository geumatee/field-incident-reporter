import { Incident } from "@/types/incident";

export const API_URL = "http://localhost:3001/incidents";

export async function getIncidents(): Promise<Incident[]> {
  const response = await fetch(`${API_URL}?_sort=createdAt&_order=desc`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`Server responded with HTTP ${response.status}`);
  }

  return response.json();
}

export async function updateIncident(
  id: string,
  patch: Partial<Pick<Incident, "status" | "severity">>
): Promise<Incident> {
  const response = await fetch(`${API_URL}/${encodeURIComponent(id)}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...patch,
      updatedAt: new Date().toISOString(),
    }),
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

export async function deleteIncident(id: string): Promise<void> {
  const response = await fetch(`${API_URL}/${encodeURIComponent(id)}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
}
