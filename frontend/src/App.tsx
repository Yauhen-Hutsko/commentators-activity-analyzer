import { useEffect, useMemo, useState } from 'react';
import { fetchRegistrations, type RegistrationsResponse } from './api/analytics';
import { MetricCards } from './components/MetricCards';
import { RangeToggle } from './components/RangeToggle';
import { RegistrationsChart } from './components/RegistrationsChart';
import { EVENTS, RANGES, filterRange } from './lib/stats';

type LoadState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: RegistrationsResponse };

export function App() {
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [range, setRange] = useState(RANGES[0]);

  useEffect(() => {
    const controller = new AbortController();
    fetchRegistrations(controller.signal)
      .then((data) => setState({ status: 'ready', data }))
      .catch((err: Error) => {
        if (err.name !== 'AbortError') setState({ status: 'error', message: err.message });
      });
    return () => controller.abort();
  }, []);

  const visible = useMemo(
    () => (state.status === 'ready' ? filterRange(state.data.series, range.from, range.to) : []),
    [state, range],
  );

  return (
    <div className="app">
      <header className="app-header">
        <div>
          <div className="brand">Registration Analytics</div>
          <div className="meta">New onliner.by accounts per month, around the 2020 Belarus presidential election</div>
        </div>
        <nav className="nav-actions">
          <a className="btn btn-light" href={`${import.meta.env.BASE_URL}../analytics.html`}>
            HTML version
          </a>
          <a className="btn btn-light" href={`${import.meta.env.BASE_URL}../jsp/welcome.jsp`}>
            Back to dashboard
          </a>
        </nav>
      </header>

      <main className="analytics stack">
        {state.status === 'ready' && (
          <section className="panel">
            <MetricCards series={state.data.series} total={state.data.total} />
          </section>
        )}

        <section className="panel">
          <div className="chart-head">
            <div>
              <h2>Monthly new registrations</h2>
              <p className="muted">
                Each user is counted once, by account creation date. Dashed line marks the election on 9 Aug 2020.
              </p>
            </div>
            <RangeToggle ranges={RANGES} selected={range} onSelect={setRange} />
          </div>

          {state.status === 'loading' && <div className="status muted">Loading…</div>}
          {state.status === 'error' && <div className="status muted">Could not load data: {state.message}</div>}
          {state.status === 'ready' &&
            (state.data.series.length === 0 ? (
              <div className="status muted">No registration data found.</div>
            ) : (
              <RegistrationsChart data={visible} events={EVENTS} />
            ))}
        </section>
      </main>
    </div>
  );
}
