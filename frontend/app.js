const API = window.location.hostname === 'localhost' ? 'http://localhost:8080' : '';
const fundTableBody = document.querySelector('#fundTable tbody');
const detailSection = document.getElementById('fundDetailSection');

async function fetchFunds() {
  const res = await fetch(`${API}/api/funds`);
  const funds = await res.json();
  fundTableBody.innerHTML = '';
  funds.forEach(f => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${f.code}</td><td>${f.name}</td><td>${f.latestNav ?? '-'}</td><td>${f.latestScore ?? '-'}</td><td><span class="badge ${f.latestLabel || ''}">${f.latestLabel || '-'}</span></td>`;
    tr.onclick = () => selectFund(f.id);
    fundTableBody.appendChild(tr);
  });
}

async function selectFund(id) {
  detailSection.classList.remove('hidden');
  const [fundRes, signalRes, navRes] = await Promise.all([
    fetch(`${API}/api/funds/${id}`),
    fetch(`${API}/api/funds/${id}/latest-signal`),
    fetch(`${API}/api/funds/${id}/nav-history`)
  ]);
  const fund = await fundRes.json();
  const signal = await signalRes.json();
  const nav = await navRes.json();

  document.getElementById('fundDetail').innerHTML = `<p><b>${fund.code}</b> - ${fund.name}</p><p>Benchmark: ${fund.benchmarkCode || 'N/A'}</p>`;
  document.getElementById('latestSignal').innerHTML = `<p>Score: <b>${signal.score}</b> | Label: <span class="badge ${signal.label}">${signal.label}</span></p><p>${signal.reasons}</p>`;

  drawChart(nav.map(n => Number(n.nav)));
}

function drawChart(data) {
  const canvas = document.getElementById('navChart');
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0,0,canvas.width,canvas.height);
  if (!data.length) return;
  const min = Math.min(...data);
  const max = Math.max(...data);
  ctx.beginPath();
  data.forEach((v, i) => {
    const x = (i / (data.length - 1)) * (canvas.width - 40) + 20;
    const y = canvas.height - ((v - min) / (max - min || 1)) * (canvas.height - 40) - 20;
    if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
  });
  ctx.strokeStyle = '#2563eb';
  ctx.lineWidth = 2;
  ctx.stroke();
}

document.getElementById('importMockBtn').onclick = async () => {
  await fetch(`${API}/api/import/mock`, { method: 'POST' });
  await fetchFunds();
  alert('Mock data imported and signals generated');
};

fetchFunds();
