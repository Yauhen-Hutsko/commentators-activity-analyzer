<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Registration Analytics</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css"/>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.js"></script>
    <style>
        .analytics { margin-top: 22px; }
        .chart-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; flex-wrap: wrap; }
        .range-toggle { display: flex; gap: 8px; }
        .range-toggle .btn[aria-pressed="true"] { border-color: var(--accent-2); background: var(--accent-2); color: #fff; }
        .chart-box { position: relative; height: 380px; margin-top: 12px; }
        .metric .delta-down { color: var(--accent); }
        .status { padding: 40px 0; text-align: center; }
    </style>
</head>
<body class="app">
<header class="app-header">
    <div>
        <div class="brand">Registration Analytics</div>
        <div class="meta">New onliner.by accounts per month, around the 2020 Belarus presidential election</div>
    </div>
    <div class="nav-actions">
        <a class="btn btn-light" href="${pageContext.request.contextPath}/jsp/welcome.jsp">Back to dashboard</a>
    </div>
</header>

<main class="analytics stack">
    <section class="panel">
        <div class="metrics" id="metrics">
            <div class="metric"><span class="muted">Avg / month, Aug 2019 – Jul 2020</span><strong id="m-before">–</strong></div>
            <div class="metric"><span class="muted">Avg / month, Aug 2020 – Jul 2021</span><strong id="m-after">–</strong></div>
            <div class="metric"><span class="muted">Change</span><strong id="m-change">–</strong></div>
            <div class="metric"><span class="muted">Users with a registration date</span><strong id="m-total">–</strong></div>
        </div>
    </section>

    <section class="panel">
        <div class="chart-head">
            <div>
                <h2>Monthly new registrations</h2>
                <p class="muted">Each user is counted once, by account creation date. Dashed line marks the election on 9 Aug 2020.</p>
            </div>
            <div class="range-toggle" role="group" aria-label="Date range">
                <button class="btn btn-light" type="button" data-from="2018-01" data-to="2023-12" aria-pressed="true">2018 – 2023</button>
                <button class="btn btn-light" type="button" data-from="" data-to="" aria-pressed="false">All time</button>
            </div>
        </div>
        <div class="chart-box">
            <div class="status muted" id="status">Loading…</div>
            <canvas id="registrations" aria-label="Monthly new registrations bar chart" role="img" hidden></canvas>
        </div>
    </section>
</main>

<script>
    (function () {
        var API_URL = '${pageContext.request.contextPath}/api/analytics/registrations';
        var EVENTS = [
            {month: '2020-08', label: 'Election · 9 Aug 2020'}
        ];
        var css = getComputedStyle(document.documentElement);
        var COLOR_BAR = css.getPropertyValue('--accent-2').trim() || '#1f6f7a';
        var COLOR_EVENT = css.getPropertyValue('--accent').trim() || '#b4572e';
        var COLOR_INK = css.getPropertyValue('--ink-2').trim() || '#4a5a64';

        var fullSeries = [];
        var chart = null;

        // Draws a dashed vertical line with a label for each event month visible on the chart.
        var eventMarkers = {
            id: 'eventMarkers',
            afterDatasetsDraw: function (c) {
                var labels = c.data.labels;
                var area = c.chartArea;
                var ctx = c.ctx;
                EVENTS.forEach(function (event) {
                    var index = labels.indexOf(event.month);
                    if (index < 0) return;
                    var x = c.scales.x.getPixelForValue(index);
                    ctx.save();
                    ctx.strokeStyle = COLOR_EVENT;
                    ctx.lineWidth = 1.5;
                    ctx.setLineDash([5, 4]);
                    ctx.beginPath();
                    ctx.moveTo(x, area.top);
                    ctx.lineTo(x, area.bottom);
                    ctx.stroke();
                    ctx.setLineDash([]);
                    ctx.fillStyle = COLOR_EVENT;
                    ctx.font = '600 12px Georgia, serif';
                    var textWidth = ctx.measureText(event.label).width;
                    var textX = x + 6 + textWidth > area.right ? x - 6 - textWidth : x + 6;
                    ctx.fillText(event.label, textX, area.top + 14);
                    ctx.restore();
                });
            }
        };

        function averageBetween(from, to) {
            var rows = fullSeries.filter(function (r) { return r.month >= from && r.month <= to; });
            if (!rows.length) return null;
            return rows.reduce(function (sum, r) { return sum + r.count; }, 0) / rows.length;
        }

        function renderMetrics(total) {
            var before = averageBetween('2019-08', '2020-07');
            var after = averageBetween('2020-08', '2021-07');
            document.getElementById('m-before').textContent = before == null ? 'n/a' : Math.round(before).toLocaleString();
            document.getElementById('m-after').textContent = after == null ? 'n/a' : Math.round(after).toLocaleString();
            var change = document.getElementById('m-change');
            if (before && after != null) {
                var pct = (after - before) / before * 100;
                change.textContent = (pct > 0 ? '+' : '') + pct.toFixed(1) + '%';
                change.className = pct < 0 ? 'delta-down' : '';
            } else {
                change.textContent = 'n/a';
            }
            document.getElementById('m-total').textContent = total.toLocaleString();
        }

        function renderChart(from, to) {
            var rows = fullSeries.filter(function (r) {
                return (!from || r.month >= from) && (!to || r.month <= to);
            });
            var labels = rows.map(function (r) { return r.month; });
            var data = rows.map(function (r) { return r.count; });
            if (chart) {
                chart.data.labels = labels;
                chart.data.datasets[0].data = data;
                chart.update();
                return;
            }
            chart = new Chart(document.getElementById('registrations'), {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{label: 'New registrations', data: data, backgroundColor: COLOR_BAR, borderRadius: 2}]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    plugins: {
                        legend: {display: false},
                        tooltip: {
                            callbacks: {
                                label: function (item) { return item.parsed.y.toLocaleString() + ' new users'; }
                            }
                        }
                    },
                    scales: {
                        x: {grid: {display: false}, ticks: {color: COLOR_INK, maxRotation: 0, autoSkipPadding: 12}},
                        y: {beginAtZero: true, ticks: {color: COLOR_INK}, title: {display: true, text: 'Users', color: COLOR_INK}}
                    }
                },
                plugins: [eventMarkers]
            });
        }

        function showStatus(text) {
            var status = document.getElementById('status');
            status.textContent = text;
            status.hidden = false;
            document.getElementById('registrations').hidden = true;
        }

        document.querySelectorAll('.range-toggle .btn').forEach(function (button) {
            button.addEventListener('click', function () {
                document.querySelectorAll('.range-toggle .btn').forEach(function (b) {
                    b.setAttribute('aria-pressed', b === button ? 'true' : 'false');
                });
                if (fullSeries.length) renderChart(button.dataset.from, button.dataset.to);
            });
        });

        fetch(API_URL)
            .then(function (resp) {
                return resp.json().then(function (body) {
                    if (!resp.ok) throw new Error(body.error || ('HTTP ' + resp.status));
                    return body;
                });
            })
            .then(function (body) {
                fullSeries = body.series || [];
                if (!fullSeries.length) {
                    showStatus('No registration data found.');
                    return;
                }
                document.getElementById('status').hidden = true;
                document.getElementById('registrations').hidden = false;
                renderMetrics(body.total);
                var active = document.querySelector('.range-toggle .btn[aria-pressed="true"]');
                renderChart(active.dataset.from, active.dataset.to);
            })
            .catch(function (err) {
                showStatus('Could not load data: ' + err.message);
            });
    })();
</script>
</body>
</html>
