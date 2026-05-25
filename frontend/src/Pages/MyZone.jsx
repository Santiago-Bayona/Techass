import React, { useState, useEffect } from 'react';
import '../Styles/MyZone.css';

const API_URL = 'http://localhost:8080/api';

export default function MyZone({ operadorData }) {
  const [atracciones, setAtracciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showColaModal, setShowColaModal] = useState(false);
  const [selectedAtraccionModal, setSelectedAtraccionModal] = useState(null);
  const [colaData, setColaData] = useState([]);
  const [loadingCola, setLoadingCola] = useState(false);

  useEffect(() => {
    if (operadorData?.id) {
      fetchAtracciones();
    } else {
      setLoading(false);
      setError('No hay datos del operador');
    }
  }, [operadorData]);

  const fetchAtracciones = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem('token');
      
      const zonaId = operadorData?.zonaAsignada?.id;
      
      console.log('🔍 Zona ID:', zonaId);
      
      let url = 'http://localhost:8080/api/atracciones';
      if (zonaId) {
        url = `http://localhost:8080/api/atracciones/zona/${zonaId}`;
      }
      
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Atracciones obtenidas:', data.data);
        setAtracciones(data.data || []);
      } else {
        console.log('⚠️ Error en respuesta:', response.status);
        setError(`Error: ${response.status}`);
      }
    } catch (error) {
      console.error('❌ Error al obtener atracciones:', error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  // ✅ NUEVA FUNCIÓN: Obtener cola de una atracción
  const fetchColaPorAtraccion = async (atraccionId) => {
    try {
      setLoadingCola(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${API_URL}/colas/debug/atraccion/${atraccionId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Cola obtenida:', data.data);
        setColaData(data.data || []);
      } else {
        console.error('⚠️ Error obteniendo cola:', response.status);
        setColaData([]);
      }
    } catch (error) {
      console.error('❌ Error al obtener cola:', error);
      setColaData([]);
    } finally {
      setLoadingCola(false);
    }
  };

  // ✅ NUEVA FUNCIÓN: Abrir modal con cola
  const handleVerCola = (atraccion) => {
    setSelectedAtraccionModal(atraccion);
    setShowColaModal(true);
    fetchColaPorAtraccion(atraccion.id);
  };

  // ✅ NUEVA FUNCIÓN: Cerrar modal
  const handleCloseModal = () => {
    setShowColaModal(false);
    setSelectedAtraccionModal(null);
    setColaData([]);
  };

  const getEstadoIcon = (estado) => {
    if (!estado) return '❓';
    switch(estado.toUpperCase()) {
      case 'ACTIVA':
        return '✅';
      case 'MANTENIMIENTO':
        return '⚠️';
      case 'BLOQUEADA':
        return '🚫';
      default:
        return '❓';
    }
  };

  const handleCopiar = () => {
    const texto = `Operador: ${operadorData?.nombre || 'N/A'}
Documento: ${operadorData?.documento || 'N/A'}`;
    
    navigator.clipboard.writeText(texto);
    alert('✅ Información copiada al portapapeles');
  };

  return (
    <div className="myzone-container">
      {/* Sección: Información Personal */}
      <section className="personal-info">
        <div className="info-card">
          <div style={{ position: 'relative' }}>
            <h3>Información Personal</h3>
            <div className="copy-btn" onClick={handleCopiar} title="Copiar información">
              📋
            </div>
          </div>
          
          <div className="info-row">
            <span className="label">Operador:</span>
            <span className="value">{operadorData?.nombre || 'N/A'}</span>
          </div>
          <div className="info-row">
            <span className="label">Email:</span>
            <span className="value">{operadorData?.email || 'N/A'}</span>
          </div>
          <div className="info-row">
            <span className="label">Empleado #:</span>
            <span className="value">{operadorData?.numeroEmpleado || 'N/A'}</span>
          </div>
        </div>
      </section>

      {/* Sección: Atracciones de Mi Zona */}
      <section className="attractions-section">
        <h3>ATRACCIONES DE MI ZONA</h3>
        
        {loading ? (
          <div className="loading">⏳ Cargando atracciones...</div>
        ) : error ? (
          <div className="no-data">❌ {error}</div>
        ) : atracciones.length === 0 ? (
          <div className="no-data">📭 No hay atracciones en tu zona</div>
        ) : (
          <div className="attractions-grid">
            {atracciones.map((atraccion) => (
              <div 
                key={atraccion.id} 
                className={`attraction-card estado-${(atraccion.estado || '').toLowerCase()}`}
              >
                <div className="attraction-header">
                  <h4>{atraccion.nombre}</h4>
                  <span className="estado-badge">
                    {getEstadoIcon(atraccion.estado)} {(atraccion.estado || 'DESCONOCIDO').toUpperCase()}
                  </span>
                </div>
                
                <div className="attraction-info">
                  <div className="info-item">
                    <span className="label">Tipo:</span>
                    <span className="value">{atraccion.tipo || 'N/A'}</span>
                  </div>
                  <div className="info-item">
                    <span className="label">Visitantes actuales:</span>
                    <span className="value">{atraccion.visitantesActuales || 0}</span>
                  </div>
                  <div className="info-item">
                    <span className="label">Capacidad máxima:</span>
                    <span className="value">{atraccion.capacidadMaxima || 0}</span>
                  </div>
                  <div className="info-item">
                    <span className="label">Tiempo espera est.:</span>
                    <span className="value">{atraccion.tiempoEsperaEstimado || 0} min</span>
                  </div>
                </div>

                {atraccion.estado?.toUpperCase() === 'MANTENIMIENTO' && (
                  <button className="action-btn registrar-revision">
                    [Registrar revisión]
                  </button>
                )}
                
                {atraccion.estado?.toUpperCase() === 'ACTIVA' && (
                  <button 
                    className="action-btn ver-cola"
                    onClick={() => handleVerCola(atraccion)}
                  >
                    [Ver cola]
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      {/* ✅ MODAL DE COLA */}
      {showColaModal && selectedAtraccionModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>📋 Cola - {selectedAtraccionModal.nombre}</h2>
              <button className="modal-close" onClick={handleCloseModal}>✕</button>
            </div>

            <div className="modal-body">
              {loadingCola ? (
                <div className="loading-modal">Cargando cola...</div>
              ) : colaData.length === 0 ? (
                <div className="empty-cola">
                  <p>✅ No hay visitantes en cola</p>
                </div>
              ) : (
                <div className="cola-list-modal">
                  <div className="cola-stats">
                    <span className="stat-item">
                      📊 Total en cola: <strong>{colaData.length}</strong> personas
                    </span>
                  </div>

                  <div className="cola-items">
                    {colaData.map((visitante, index) => (
                      <div key={visitante.id} className="cola-item-modal">
                        <div className="position-badge">#{index + 1}</div>
                        <div className="visitante-info">
                          <p className="visitante-nombre">
                            <strong>{visitante.visitanteNombre}</strong>
                          </p>
                          <p className="visitante-ticket">
                            {visitante.prioridad === 1 ? '⚡ Fast Pass' : '👥 General'}
                          </p>
                          <p className="visitante-hora">
                            🕐 Ingreso: {new Date(visitante.horaIngreso).toLocaleTimeString()}
                          </p>
                        </div>
                        {index === 0 && (
                          <div className="siguiente-badge">SIGUIENTE</div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn-close" onClick={handleCloseModal}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}