const el = {
  fundTableBody: document.querySelector('#fundTable tbody'),
  fundTable: document.getElementById('fundTable'),
  fundLoading: document.getElementById('fundLoading'),
  fundEmpty: document.getElementById('fundEmpty'),
  fundCount: document.getElementById('fundCount'),
  globalError: document.getElementById('globalError'),
  detailSection: document.getElementById('fundDetailSection'),
  fundDetail: document.getElementById('fundDetail'),
  latestSignal: document.getElementById('latestSignal'),
  signalLoading: document.getElementById('signalLoading')
};

async function fetchFunds() {
  setFundsState({ loading: true, empty: false, error: '' });
  try {
    const page = await Api.listFunds(0, 50);
    const funds = page.items;
    el.fundCount.textContent = `${page.totalElements} funds`;
    el.fundTableBody.innerHTML = '';

    if (!funds.length) {
      setFundsState({ loading: false, empty: true });
      return;
    }

    funds.forEach(f => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${f.code}</td><td>${f.name}</td><td>${formatNum(f.latestNav)}</td><td>${formatNum(f.latestScore)}</td><td><span class="badge ${f.latestLabel || ''}">${f.latestLabel || '-'}</span></td>`;
      tr.onclick = () => selectFund(f.id);
      el.fundTableBody.appendChild(tr);
    });
    setFundsState({ loading: false, empty: false });
  } catch (e) {
    setFundsState({ loading: false, empty: false, error: e.message });
  }
}

function setFundsState({ loading = false, empty = false, error = '' }) {
  el.fundLoading.classList.toggle('hidden', !loading);
  el.fundEmpty.classList.toggle('hidden', !empty);
  el.fundTable.classList.toggle('hidden', loading || empty);
  el.globalError.classList.toggle('hidden', !error);
  el.globalError.textContent = error || '';
}

async function selectFund(id) {
  el.detailSection.classList.remove('hidden');
  el.signalLoading.classList.remove('hidden');
  try {
    const [fund, signal, nav] = await Promise.all([Api.getFund(id), Api.getSignal(id), Api.getNav(id)]);
    el.fundDetail.innerHTML = `<p><b>${fund.code}</b> - ${fund.name}</p><p>Benchmark: ${fund.benchmarkCode || 'N/A'}</p>`;
    el.latestSignal.innerHTML = `<p>Score: <b>${signal.score}</b> | Label: <span class="badge ${signal.label}">${signal.label}</span></p><p>${signal.reasons}</p>`;
    drawChart(nav.map(n => Number(n.nav)));
  } catch (e) {
    el.latestSignal.innerHTML = `<div class='error'>${e.message}</div>`;
  } finally {
    el.signalLoading.classList.add('hidden');
  }
}

function drawChart(data) {
  const canvas = document.getElementById('navChart');
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  if (!data.length) return;
  const min = Math.min(...data), max = Math.max(...data);
  ctx.beginPath();
  data.forEach((v, i) => {
    const x = (i / (data.length - 1 || 1)) * (canvas.width - 40) + 20;
    const y = canvas.height - ((v - min) / (max - min || 1)) * (canvas.height - 40) - 20;
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
  });
  ctx.strokeStyle = '#2563eb';
  ctx.lineWidth = 2;
  ctx.stroke();
}

function formatNum(v) { return v == null ? '-' : Number(v).toFixed(2); }

document.getElementById('importMockBtn').onclick = async () => {
  try {
    await Api.importMock();
    await fetchFunds();
    alert('Mock data imported successfully.');
  } catch (e) {
    alert(`Import failed: ${e.message}`);
  }
};

document.getElementById('resetDemoBtn').onclick = async () => {
  try {
    await Api.importMock();
    await fetchFunds();
    alert('Demo data refreshed.');
  } catch (e) {
    alert(`Reset failed: ${e.message}`);
  }
};

fetchFunds();
