import { useState } from 'react';
import '../../Styles/Sidebar.css';

export default function Sidebar({ activeTab, setActiveTab, onLogout, visitanteName }) {
  const [mobileOpen, setMobileOpen] = useState(false);

  const tabs = [
    { id: 'perfil', label: 'Perfil', icon: '👤' },
    { id: 'mapa', label: 'Mapa', icon: '🗺️' },
    { id: 'colas', label: 'Mis Colas', icon: '⏳' },
    { id: 'favoritos', label: 'Favoritos e Historial', icon: '⭐' },
  ];

  return (
    <>
      {/* Botón hamburguesa mobile */}
      <button
        className="hamburger-menu"
        onClick={() => setMobileOpen(!mobileOpen)}
      >
        ☰
      </button>

      {/* Overlay mobile */}
      {mobileOpen && (
        <div
          className="sidebar-overlay"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`sidebar ${mobileOpen ? 'open' : ''}`}>
        {/* Header */}
        <div className="sidebar-header">
          <h1>🎢 TechPark</h1>
          <p className="welcome-text">¡Bienvenido, {visitanteName}!</p>
        </div>

        {/* Navigation */}
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

        {/* Footer */}
        <div className="sidebar-footer">
          <button
            className="logout-button"
            onClick={() => {
              setMobileOpen(false);
              onLogout();
            }}
          >
            🚪 Cerrar Sesión
          </button>
        </div>
      </aside>
    </>
  );
}