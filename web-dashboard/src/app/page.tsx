"use client";

import { useEffect, useState } from "react";
import { Incident } from "@/types/incident";
import {
  getIncidents,
  updateIncident,
  deleteIncident,
} from "@/services/incidentApi";

export default function Home() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  async function loadIncidents() {
    try {
      const data = await getIncidents();
      console.log(data)
    } catch (err) {
        console.log(err)
    }
  }

  
  async function handleUpdateIncident(
    id: string,
    patch: Partial<Pick<Incident, "status" | "severity">>
  ) {
    try {
      const updated = await updateIncident(id, patch);
      console.log(updated)
    } catch (err) {
        console.log(err)
    }
  }

  async function handleDeleteIncident(id: string, title: string) {
    try {
      await deleteIncident(id);
    } catch (err) {
        console.log(err)
    }
  }

  useEffect(() => {
    loadIncidents();
  }, []);

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1 className="text-3xl font-bold tracking-tight">
        Field Incident Triage Console
      </h1>
    </main>
  );
}