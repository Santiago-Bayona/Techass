import { useEffect, useState } from 'react';
import '../../Styles/MisColas.css';

const API_URL = 'http://localhost:8080/api';

export default function MisColas({ visitante }) {
  const [colas, setColas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarColas();
  }, []);

  const cargarColas = async () => {
    try {
      setLoading(true);
      setError(null);

      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      console.log('📋 Cargando colas para usuario:', usuarioId);

      const response = await fetch(`${API_URL}/colas/mis-colas/${usuarioId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Colas obtenidas:', data.data);

      // Debug: mostrar la estructura de datos
      if (data.data && data.data.length > 0) {
        console.log('📊 Estructura de primera cola:', data.data[0]);
      }

      setColas(data.data || []);
    } catch (error) {
      console.error('❌ Error cargando colas:', error);
      setError('Error al cargar colas: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const cancelarCola = async (cola) => {
    if (!window.confirm('¿Estás seguro de que quieres cancelar esta cola?')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      // Obtener atraccionId - intenta de varias formas
      const atraccionId = cola.atraccion?.id || cola.atraccionId;

      if (!atraccionId) {
        console.error('❌ No se pudo obtener atraccionId:', cola);
        setMensaje('❌ Error: No se puede cancelar esta cola');
        return;
      }

      console.log(`Cancelando cola - Usuario: ${usuarioId}, Atracción: ${atraccionId}`);

      const response = await fetch(
        `${API_URL}/colas/cancelar/${usuarioId}/${atraccionId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        setMensaje('✅ Cola cancelada');
        setTimeout(() => setMensaje(''), 3000);
        cargarColas();
      } else {
        setMensaje('❌ Error al cancelar cola');
      }
    } catch (error) {
      console.error('Error:', error);
      setMensaje('❌ Error al cancelar cola');
    }
  };

  if (loading) {
    return (
      <div className="colas-container">
        <p>Cargando colas...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="colas-container">
        <div style={{ color: 'red' }}>
          <p>{error}</p>
          <button onClick={cargarColas}>Reintentar</button>
        </div>
      </div>
    );
  }

  return (
    <div className="colas-container">
      <div className="colas-header">
        <h2>⏳ Mis Colas Activas</h2>
      </div>

      {mensaje && (
        <div className={`mensaje ${mensaje.includes('✅') ? 'success' : 'error'}`}>
          {mensaje}
        </div>
      )}

      {colas.length === 0 ? (
        <div className="empty-state">
          <p>📭 No tienes colas activas</p>
          <p className="subtext">¡Dirígete al mapa y únete a tus atracciones favoritas!</p>
        </div>
      ) : (
        <div className="colas-list">
          {colas.map((cola) => {
            // Obtener el nombre de la atracción - intenta varias rutas
            const nombreAtraccion = cola.atraccion?.nombre || cola.atraccionNombre || 'Atracción desconocida';
            const tiempoEstimado = cola.atraccion?.tiempoEsperaEstimado || cola.tiempoEstimado || 'N/A';
            const posicion = cola.posicion || 'N/A';

            return (
              <div key={cola.id} className="cola-card">
                <div className="cola-header">
                  <h3>{nombreAtraccion}</h3>
                  <span className={`badge ${cola.prioridad === 1 ? 'fast-pass' : 'general'}`}>
                    {cola.prioridad === 1 ? '⚡ Fast-Pass' : '👥 General'}
                  </span>
                </div>

                <div className="cola-info">
                  <div className="info-item">
                    <span className="label">📍 Posición en cola:</span>
                    <span className="value">#{posicion}</span>
                  </div>
                  <div className="info-item">
                    <span className="label">⏱️ Tiempo estimado:</span>
                    <span className="value">{tiempoEstimado} minutos</span>
                  </div>
                  <div className="info-item">
                    <span className="label">📅 Hora ingreso:</span>
                    <span className="value">
                      {new Date(cola.horaIngresoCola || Date.now()).toLocaleTimeString()}
                    </span>
                  </div>
                </div>

                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{ width: `${Math.min((posicion / 50) * 100, 100)}%` }}
                  ></div>
                </div>

                <div className="cola-buttons">
                  <button
                    onClick={() => cancelarCola(cola)}
                    className="btn-cancelar"
                  >
                    ❌ Cancelar Cola
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}