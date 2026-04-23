const API_BASE = window.location.hostname === 'localhost' ? 'http://localhost:8080' : '';

async function apiRequest(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
  const payload = await res.json();
  if (!res.ok || payload.success === false) {
    throw new Error(payload?.error?.message || 'Request failed');
  }
  return payload.data;
}

const Api = {
  listFunds: (page = 0, size = 20) => apiRequest(`/api/funds?page=${page}&size=${size}`),
  getFund: (id) => apiRequest(`/api/funds/${id}`),
  getSignal: (id) => apiRequest(`/api/funds/${id}/latest-signal`),
  getNav: (id) => apiRequest(`/api/funds/${id}/nav-history`),
  importMock: () => apiRequest('/api/import/mock', { method: 'POST' })
};
