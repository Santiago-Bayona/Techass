import React, { useState, useEffect } from 'react';
import '../../../Styles/gestion/TabZonas.css';  // ✅ RUTA CORREGIDA

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
      console.log('🔍 Fetching zonas desde:', `${API_URL}/zonas`);

      const response = await fetch(`${API_URL}/zonas/con-atracciones`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      console.log('📊 Response status:', response.status);

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Zonas obtenidas:', data);  // ✅ LOG
        setZonas(data.data || []);
      } else {
        console.error('❌ Error response:', response.status);  // ✅ LOG
      }
      setLoading(false);
    } catch (error) {
      console.error('❌ Error fetching zonas:', error);  // ✅ LOG
      setLoading(false);
    }
  };

  if (loading) return <div className="tab-loading">⏳ Cargando zonas...</div>;

  return (
    <div className="tab-zonas">
      <h3>Lista de zonas</h3>

      {/* ✅ VALIDACION DE DATOS VACIOS */}
      {zonas.length === 0 ? (
        <div className="tab-loading">📭 No hay zonas disponibles</div>
      ) : (
        <div className="zonas-grid">
          {zonas.map((zona) => (
            <div
              key={zona.id}
              className="zona-card"
              onClick={() => setSelectedZona(zona)}
              style={{ borderLeftColor: zona.colorRepresentativo || '#667eea' }}  
            >
              <div className="zona-name">{zona.nombre}</div>
              <div className="zona-info">
                <p>Capacidad: {zona.capacidadMaxima}</p>       
                <p>Aforo actual: {zona.aforoActual || 0}</p>
                <p>Atracciones: {zona.atracciones?.length || 0}</p>  
              </div>
              <button className="zona-btn">[Ver atracciones]</button>
            </div>
          ))}
        </div>
      )}

      {selectedZona && (
        <div className="zona-details">
          <h4>{selectedZona.nombre}</h4>
          <p><strong>Capacidad:</strong> {selectedZona.capacidadMaxima}</p>
          <p><strong>Aforo actual:</strong> {selectedZona.aforoActual || 0}</p>
          <p><strong>Color:</strong> {selectedZona.colorRepresentativo}</p>
          <button className="close-btn" onClick={() => setSelectedZona(null)}>Cerrar</button>
        </div>
      )}
    </div>
  );
}