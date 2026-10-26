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