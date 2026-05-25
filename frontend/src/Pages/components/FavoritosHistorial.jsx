import { useEffect, useState } from 'react';
import '../../Styles/FavoritoHistorial.css';

const API_URL = 'http://localhost:8080/api';

export default function FavoritosHistorial({ visitante }) {
  const [activeTab, setActiveTab] = useState('favoritos');
  const [favoritos, setFavoritos] = useState([]);
  const [historial, setHistorial] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState('');

  const PAGE_SIZE = 5;

  useEffect(() => {
    if (activeTab === 'favoritos') {
      cargarFavoritos();
    } else {
      cargarHistorial(0);
    }
  }, [activeTab]);

  const cargarFavoritos = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(`${API_URL}/atracciones/favoritos/${usuarioId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setFavoritos(data.data || []);
      }
    } catch (error) {
      console.error('Error cargando favoritos:', error);
      setMensaje('Error al cargar favoritos');
    } finally {
      setLoading(false);
    }
  };

  const cargarHistorial = async (pageNum) => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(
        `${API_URL}/atracciones/historial/${usuarioId}?page=${pageNum}&size=${PAGE_SIZE}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setHistorial(data.data?.content || []);
        setTotalPages(data.data?.totalPages || 0);
        setPage(pageNum);
      }
    } catch (error) {
      console.error('Error cargando historial:', error);
      setMensaje('Error al cargar historial');
    } finally {
      setLoading(false);
    }
  };

  const removerFavorito = async (atraccionId) => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(
        `${API_URL}/atracciones/favorito/${usuarioId}/${atraccionId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        setMensaje('❌ Removido de favoritos');
        cargarFavoritos();
        setTimeout(() => setMensaje(''), 2000);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const unirseCola = async (atraccionId) => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(`${API_URL}/colas/unirse`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          visitanteId: parseInt(usuarioId),
          atraccionId: atraccionId,
        }),
      });

      if (response.ok) {
        setMensaje('✅ Unido a la cola');
      } else {
        setMensaje('❌ Error al unirse a la cola');
      }
      setTimeout(() => setMensaje(''), 2000);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  if (loading) {
    return <div className="fav-hist-container"><p>Cargando...</p></div>;
  }

  return (
    <div className="fav-hist-container">
      <div className="fav-hist-header">
        <h2>⭐ Favoritos e Historial</h2>
      </div>

      {mensaje && (
        <div className={`mensaje ${mensaje.includes('✅') ? 'success' : 'error'}`}>
          {mensaje}
        </div>
      )}

      {/* Tabs */}
      <div className="tabs">
        <button
          className={`tab-button ${activeTab === 'favoritos' ? 'active' : ''}`}
          onClick={() => setActiveTab('favoritos')}
        >
          ⭐ Mis Favoritos
        </button>
        <button
          className={`tab-button ${activeTab === 'historial' ? 'active' : ''}`}
          onClick={() => setActiveTab('historial')}
        >
          📜 Mi Historial
        </button>
      </div>

      {/* FAVORITOS TAB */}
      {activeTab === 'favoritos' && (
        <div className="tab-content">
          {favoritos.length === 0 ? (
            <div className="empty-state">
              <p>📭 No tienes atracciones favoritas</p>
              <p className="subtext">¡Ve al mapa y haz clic en el corazón para agregar!</p>
            </div>
          ) : (
            <div className="favoritos-list">
              {favoritos.map((atraccion) => (
                <div key={atraccion.id} className="favorito-item">
                  <div className="item-header">
                    <h3>{atraccion.nombre}</h3>
                    <button
                      onClick={() => removerFavorito(atraccion.id)}
                      className="btn-remove-fav"
                      title="Remover de favoritos"
                    >
                      ❌
                    </button>
                  </div>

                  <div className="item-details">
                    <div className="detail">
                      <span className="label">🎢 Tipo:</span>
                      <span className="value">{atraccion.tipo}</span>
                    </div>
                    <div className="detail">
                      <span className="label">📊 Estado:</span>
                      <span className={`estado ${atraccion.estado}`}>
                        {atraccion.estado}
                      </span>
                    </div>
                    <div className="detail">
                      <span className="label">⏱️ Espera:</span>
                      <span className="value">{atraccion.tiempoEsperaEstimado} min</span>
                    </div>
                  </div>

                  <div className="item-restrictions">
                    {atraccion.edadMinima && (
                      <span>👶 Edad mín: {atraccion.edadMinima} años</span>
                    )}
                    {atraccion.alturaMinima && (
                      <span>📏 Altura mín: {atraccion.alturaMinima}m</span>
                    )}
                  </div>

                  <button
                    onClick={() => unirseCola(atraccion.id)}
                    className="btn-cola"
                    disabled={atraccion.estado !== 'ACTIVA'}
                  >
                    {atraccion.estado === 'ACTIVA' ? '➕ Unirse a Cola' : '❌ No disponible'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* HISTORIAL TAB */}
      {activeTab === 'historial' && (
        <div className="tab-content">
          {historial.length === 0 ? (
            <div className="empty-state">
              <p>📭 No tienes historial de visitas</p>
            </div>
          ) : (
            <>
              <div className="historial-list">
                {historial.map((visita, index) => (
                  <div key={index} className="historial-item">
                    <div className="item-main">
                      <div className="item-atraccion">
                        <h4>{visita.atraccionNombre}</h4>
                        <p className="tipo">{visita.atraccionTipo}</p>
                      </div>
                      <div className="item-fecha">
                        <span className="fecha">
                          📅 {new Date(visita.fechaVisita).toLocaleDateString()}
                        </span>
                        <span className="hora">
                          🕐 {new Date(visita.fechaVisita).toLocaleTimeString()}
                        </span>
                      </div>
                    </div>

                    <div className="item-details">
                      <div className="detail">
                        <span className="label">Tiempo espera real:</span>
                        <span className="value">{visita.tiempoEsperaReal} min</span>
                      </div>
                      <div className="detail">
                        <span className="label">Fast-Pass:</span>
                        <span className="value">
                          {visita.usoFastPass ? '⚡ Sí' : '❌ No'}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Paginación */}
              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    onClick={() => cargarHistorial(page - 1)}
                    disabled={page === 0}
                    className="btn-pag"
                  >
                    ← Anterior
                  </button>

                  <div className="page-info">
                    Página {page + 1} de {totalPages}
                  </div>

                  <button
                    onClick={() => cargarHistorial(page + 1)}
                    disabled={page >= totalPages - 1}
                    className="btn-pag"
                  >
                    Siguiente →
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}