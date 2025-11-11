'use client';
// src/lib/session.ts

export function getOrCreateSessionId(usePerTab = false): string {
  // Si no estamos en el navegador, devolver un ID temporal
  if (typeof window === 'undefined') {
    // Evita usar localStorage/sessionStorage en SSR
    return `server-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  }

  const storage = usePerTab ? sessionStorage : localStorage;
  const KEY = 'chat.sessionId';

  let id = storage.getItem(KEY);
  if (!id) {
    id = (typeof crypto !== 'undefined' && 'randomUUID' in crypto)
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    storage.setItem(KEY, id);
  }

  return id;
}