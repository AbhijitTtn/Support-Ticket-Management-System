import { Link, Outlet } from 'react-router-dom';

export function Layout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="header-inner">
          <Link to="/" className="brand">Support Tickets</Link>
          <nav className="header-nav">
            <Link to="/" className="nav-link">Dashboard</Link>
            <Link to="/tickets/new" className="button button-primary">+ New Ticket</Link>
          </nav>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
