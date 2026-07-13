// Tracks the products a shopper has viewed, newest first, in localStorage.

const KEY = "luxshop_recent";
const MAX = 8;

export function recordView(id: string) {
  if (typeof window === "undefined") return;
  try {
    const next = [id, ...getRecentIds().filter((x) => x !== id)].slice(0, MAX);
    localStorage.setItem(KEY, JSON.stringify(next));
  } catch {
    /* ignore quota / corrupt storage */
  }
}

export function getRecentIds(): string[] {
  if (typeof window === "undefined") return [];
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as string[]) : [];
  } catch {
    return [];
  }
}
