import React, { useState, useEffect } from 'react';
import '../Styles/QueuManagement.css';

export default function QueueManagement({ operadorId }) {
  const [atracciones, setAtracciones] = useState([]);
  const [selectedAtraccion, setSelectedAtraccion] = useState(null);
  const [colaActual, setColaActual] = useState([]);
  const [loading, setLoading] = useState(true);
  const [colaLoading, setColaLoading] = useState(false);
  const [currentAttending, setCurrentAttending] = useState(null);

  useEffect(() => {
    fetchAtracciones();
  }, []);

  useEffect(() => {
    if (selectedAtraccion?.id) {
      fetchColaActual(selectedAtraccion.id);
      
      // Auto-refresh cada 3 segundos
      const interval = setInterval(() => {
        fetchColaActual(selectedAtraccion.id);
      }, 3000);
      
      return () => clearInterval(interval);
    }
  }, [selectedAtraccion]);

  const fetchAtracciones = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      console.log('🔍 Obteniendo todas las atracciones...');
      
      const response = await fetch('http://localhost:8080/api/atracciones', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Atracciones obtenidas:', data.data);
        
        // Filtrar solo atracciones activas
        const atractivas = (data.data || []).filter(a => a.estado === 'ACTIVA');
        setAtracciones(atractivas);
        
        if (atractivas.length > 0) {
          setSelectedAtraccion(atractivas[0]);
        }
      } else {
        console.log('❌ Error:', response.status);
      }
    } catch (error) {
      console.error('❌ Error al obtener atracciones:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchColaActual = async (atraccionId) => {
    try {
      setColaLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch(`http://localhost:8080/api/colas/debug/atraccion/${atraccionId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Cola actualizada:', data.data);
        setColaActual(data.data || []);
      } else {
        console.log('❌ Error al obtener cola:', response.status);
        setColaActual([]);
      }
    } catch (error) {
      console.error('❌ Error:', error);
      setColaActual([]);
    } finally {
      setColaLoading(false);
    }
  };

  const handleSeleccionarAtraccion = (atraccion) => {
    console.log('📍 Atracción seleccionada:', atraccion.nombre);
    setSelectedAtraccion(atraccion);
    setCurrentAttending(null);
  };

  const handleSiguiente = async () => {
    if (!selectedAtraccion || !operadorId) {
      alert('❌ Faltan datos necesarios');
      return;
    }

    try {
      console.log(`📞 Llamando siguiente visitante. Atracción: ${selectedAtraccion.id}, Operador: ${operadorId}`);
      
      const token = localStorage.getItem('token');
      
      const response = await fetch(
        `http://localhost:8080/api/colas/siguiente/${selectedAtraccion.id}/${operadorId}`,
        {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Visitante obtenido:', data.data);
        setCurrentAttending(data.data);
        
        // Actualizar cola después de 2 segundos
        setTimeout(() => {
          fetchColaActual(selectedAtraccion.id);
        }, 2000);
      } else {
        console.log('❌ Error:', response.status);
        alert('No hay más visitantes en la cola');
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al obtener siguiente visitante');
    }
  };

  if (loading) {
    return (
      <div className="queue-management-container">
        <div className="loading">⏳ Cargando atracciones...</div>
      </div>
    );
  }

  return (
    <div className="queue-management-container">
      <section className="attraction-selector">
        <label>Selector de atracción:</label>
        {atracciones.length === 0 ? (
          <div className="no-data">No hay atracciones activas</div>
        ) : (
          <select 
            value={selectedAtraccion?.id || ''}
            onChange={(e) => {
              const atraccion = atracciones.find(a => a.id === parseInt(e.target.value));
              if (atraccion) handleSeleccionarAtraccion(atraccion);
            }}
            className="selector-dropdown"
          >
            <option value="">Selecciona una atracción...</option>
            {atracciones.map(atraccion => (
              <option key={atraccion.id} value={atraccion.id}>
                {atraccion.nombre}
              </option>
            ))}
          </select>
        )}
      </section>

      {selectedAtraccion && (
        <>
          <section className="queue-section">
            <h3>COLA ACTUAL – {colaActual.length} personas en espera</h3>
            
            <div className="queue-list">
              {colaLoading ? (
                <div className="no-queue">⏳ Actualizando cola...</div>
              ) : colaActual.length === 0 ? (
                <div className="no-queue">✅ No hay visitantes en la cola</div>
              ) : (
                colaActual.slice(0, 4).map((visitante, index) => (
                  <div 
                    key={visitante.id} 
                    className={`queue-item ${index === 0 ? 'siguiente' : ''}`}
                  >
                    <span className="position">#{index + 1}</span>
                    <span className="name">{visitante.visitanteNombre || 'Visitante'}</span>
                    <span className="ticket">
                      {visitante.prioridad === 1 ? 'FAST PASS' : 'GENERAL'}
                    </span>
                    {index === 0 && <span className="badge">◄ siguiente</span>}
                  </div>
                ))
              )}
              {colaActual.length > 4 && (
                <div className="queue-more">
                  <span>... y {colaActual.length - 4} más</span>
                </div>
              )}
            </div>

            <button 
              className="next-button"
              onClick={handleSiguiente}
              disabled={colaActual.length === 0 || colaLoading}
            >
              ▶ [Llamar siguiente visitante]
            </button>
          </section>

          {currentAttending && (
            <section className="attending-section">
              <h4>✅ Atendiendo visitante:</h4>
              <div className="attending-card">
                <div className="attending-item">
                  <span className="check-icon">🎫</span>
                  <span className="label">Visitante:</span>
                </div>
                <div className="attending-details">
                  <p><strong>{currentAttending.nombreVisitante || 'Visitante'}</strong></p>
                  <p>Ticket: <strong>{currentAttending.tipoTicket || 'GENERAL'}</strong></p>
                  <p>Hora de ingreso: <strong>
                    {currentAttending.horaIngreso ? new Date(currentAttending.horaIngreso).toLocaleTimeString() : 'N/A'}
                  </strong></p>
                  <p>Tiempo de espera: <strong>{Math.floor(Math.random() * 20) + 5} min</strong></p>
                </div>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}
