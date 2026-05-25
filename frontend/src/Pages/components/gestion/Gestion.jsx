import React, { useState } from 'react';
import TabZonas from './TabZonas';
import TabAtracciones from './TabAtracciones';
import TabUsuarios from './TabUsuarios';
import '../../../Styles/Gestion.css';

export default function Gestion({ adminId }) {
  const [activeTab, setActiveTab] = useState('zonas');

  return (
    <div className="gestion-container">
      <div className="gestion-tabs">
        <button
          className={`tab-btn ${activeTab === 'zonas' ? 'active' : ''}`}
          onClick={() => setActiveTab('zonas')}
        >
          [Zonas]
        </button>
        <button
          className={`tab-btn ${activeTab === 'atracciones' ? 'active' : ''}`}
          onClick={() => setActiveTab('atracciones')}
        >
          [Atracciones]
        </button>
        <button
          className={`tab-btn ${activeTab === 'usuarios' ? 'active' : ''}`}
          onClick={() => setActiveTab('usuarios')}
        >
          [Usuarios]
        </button>
      </div>

      <div className="tab-content">
        {activeTab === 'zonas' && <TabZonas />}
        {activeTab === 'atracciones' && <TabAtracciones />}
        {activeTab === 'usuarios' && <TabUsuarios adminId={adminId} />}
      </div>
    </div>
  );
}