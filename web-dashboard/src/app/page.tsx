"use client";

import { useEffect } from "react";
import {
  getIncidents,
} from "@/services/incidentApi";

export default function Home() {
    async function loadIncidents() {
    try {
      const data = await getIncidents();
      console.log(data)
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