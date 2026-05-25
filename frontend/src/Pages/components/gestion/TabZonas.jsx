import React, { useState, useEffect } from 'react';
import '../../../Styles/TabZonas.css';

const API_URL = 'http://localhost:8080/api';

export default function TabZonas() {
  const [zonas, setZonas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedZona, setSelectedZona] = useState(null);

  useEffect(() => {
    fetchZonas();
  }, []);

  const fetchZonas = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/zonas`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (response.ok) {
        const data = await response.json();
        setZonas(data.data || []);
      }
      setLoading(false);
    } catch (error) {
      console.error('❌ Error fetching zonas:', error);
      setLoading(false);
    }
  };

  if (loading) return <div className="tab-loading">⏳ Cargando zonas...</div>;

  return (
    <div className="tab-zonas">
      <h3>Lista de zonas</h3>

      <div className="zonas-grid">
        {zonas.map((zona) => (
          <div
            key={zona.id}
            className="zona-card"
            onClick={() => setSelectedZona(zona)}
            style={{ borderLeftColor: zona.color || '#667eea' }}
          >
            <div className="zona-name">{zona.nombre}</div>
            <div className="zona-info">
              <p>Capacidad: {zona.capacidad}</p>
              <p>Aforo actual: {zona.aforoActual || 0}</p>
              <p>Atracciones: {zona.atraccionesCount || 0}</p>
            </div>
            <button className="zona-btn">[Ver atracciones]</button>
          </div>
        ))}
      </div>

      {selectedZona && (
        <div className="zona-details">
          <h4>{selectedZona.nombre}</h4>
          <p><strong>Capacidad:</strong> {selectedZona.capacidad}</p>
          <p><strong>Aforo actual:</strong> {selectedZona.aforoActual || 0}</p>
          <p><strong>Color:</strong> {selectedZona.color}</p>
          <button className="close-btn" onClick={() => setSelectedZona(null)}>Cerrar</button>
        </div>
      )}
    </div>
  );
}