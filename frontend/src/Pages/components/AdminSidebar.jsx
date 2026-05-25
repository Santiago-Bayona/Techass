import { useState } from 'react';
import '../../Styles/AdminSidebar.css';

export default function AdminSidebar({ activeTab, setActiveTab, onLogout, adminName }) {
  const [mobileOpen, setMobileOpen] = useState(false);

  const tabs = [
    { id: 'mapa', label: 'Mapa del Parque', icon: '🗺️' },
    { id: 'clima', label: 'Alertas Climáticas', icon: '⛈️' },
    { id: 'reportes', label: 'Reportes', icon: '📊' },
    { id: 'gestion', label: 'Gestión', icon: '⚙️' },
  ];

  return (
    <>
      <button className="hamburger-menu" onClick={() => setMobileOpen(!mobileOpen)}>
        ☰
      </button>

      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />}

      <aside className={`admin-sidebar ${mobileOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <h1>🎢 TechPark</h1>
          <p className="admin-badge">ADMINISTRADOR</p>
          <p className="welcome-text">¡Bienvenido, {adminName}!</p>
        </div>

        <nav className="sidebar-nav">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={`nav-button ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => {
                setActiveTab(tab.id);
                setMobileOpen(false);
              }}
            >
              <span className="icon">{tab.icon}</span>
              <span className="label">{tab.label}</span>
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button className="logout-button" onClick={onLogout}>
            🚪 Cerrar Sesión
          </button>
        </div>
      </aside>
    </>
  );
}