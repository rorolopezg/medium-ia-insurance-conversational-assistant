export function getOrCreateSessionId(): string {
  if (typeof window === 'undefined') return 'server-session';
  const KEY = 'superchat-session-id';
  let id = localStorage.getItem(KEY);
  if (!id) {
    id = (window.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`);
    localStorage.setItem(KEY, id);
  }
  return id;
}
