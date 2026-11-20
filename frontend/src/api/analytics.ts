export interface MonthlyCount {
  /** Calendar month, `YYYY-MM`. */
  month: string;
  count: number;
}

export interface RegistrationsResponse {
  series: MonthlyCount[];
  total: number;
}

// BASE_URL is "/greatanalyzer/react/", so the API lives one level up at "/greatanalyzer/api/...".
const API_ROOT = `${import.meta.env.BASE_URL}../api`;

export async function fetchRegistrations(signal?: AbortSignal): Promise<RegistrationsResponse> {
  const resp = await fetch(`${API_ROOT}/analytics/registrations`, { signal });
  const body = await resp.json().catch(() => ({}));
  if (!resp.ok) {
    throw new Error(body.error ?? `HTTP ${resp.status}`);
  }
  return body as RegistrationsResponse;
}
