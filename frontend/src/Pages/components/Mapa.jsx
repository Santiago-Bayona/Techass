import { useEffect, useRef, useState } from 'react';
import '../../Styles/Mapa.css';

const API_URL = 'http://localhost:8080/api';

export default function Mapa({ visitante }) {
  const canvasRef = useRef(null);
  const [atracciones, setAtracciones] = useState([]);
  const [mapa, setMapa] = useState(null);
  const [selectedAtraccion, setSelectedAtraccion] = useState(null);
  const [origen, setOrigen] = useState(null);
  const [destino, setDestino] = useState(null);
  const [ruta, setRuta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [atraccionesInfo, setAtraccionesInfo] = useState({});
  const [favoritos, setFavoritos] = useState([]);
  const [mensaje, setMensaje] = useState('');

  useEffect(() => {
    cargarMapa();
    cargarFavoritos();
  }, []);

  useEffect(() => {
    if (mapa && atracciones.length > 0) {
      dibujarMapa();
    }
  }, [mapa, atracciones, ruta, selectedAtraccion]);

  // Redibujar cuando cambie el tamaño del contenedor
  useEffect(() => {
    const handleResize = () => {
      if (mapa && atracciones.length > 0) {
        dibujarMapa();
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [mapa, atracciones, ruta, selectedAtraccion]);

  const cargarMapa = async () => {
    try {
      const token = localStorage.getItem('token');

      const response1 = await fetch(`${API_URL}/atracciones`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      const dataAtracciones = await response1.json();
      setAtracciones(dataAtracciones.data || []);

      const info = {};
      (dataAtracciones.data || []).forEach(a => {
        info[a.id] = a;
      });
      setAtraccionesInfo(info);

      const response2 = await fetch(`${API_URL}/rutas/mapa-visual`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      const dataMapa = await response2.json();
      setMapa(dataMapa.data);
    } catch (error) {
      console.error('Error cargando mapa:', error);
    } finally {
      setLoading(false);
    }
  };

  const cargarFavoritos = async () => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(`${API_URL}/atracciones/favoritos/${usuarioId}`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });

      if (response.ok) {
        const data = await response.json();
        setFavoritos(data.data || []);
      }
    } catch (error) {
      console.error('Error cargando favoritos:', error);
    }
  };

  const esFavorito = (atraccionId) => {
    return favoritos.some(fav => fav.id === atraccionId);
  };

  const toggleFavorito = async (atraccionId) => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');
      const esActualmenteFavorito = esFavorito(atraccionId);

      const method = esActualmenteFavorito ? 'DELETE' : 'POST';
      const response = await fetch(
        `${API_URL}/atracciones/favorito/${usuarioId}/${atraccionId}`,
        {
          method: method,
          headers: { 'Authorization': `Bearer ${token}` },
        }
      );

      if (response.ok) {
        setMensaje(esActualmenteFavorito ? '❌ Removido de favoritos' : '⭐ Agregado a favoritos');
        cargarFavoritos();
        setTimeout(() => setMensaje(''), 2000);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const getCanvasCoords = () => {
    const canvas = canvasRef.current;
    if (!canvas || !mapa) return null;

    const width = canvas.width;
    const height = canvas.height;
    const padding = 50;
    const drawWidth = width - 2 * padding;
    const drawHeight = height - 2 * padding;

    let minX = Infinity, maxX = -Infinity;
    let minY = Infinity, maxY = -Infinity;

    mapa.nodos.forEach(nodo => {
      minX = Math.min(minX, nodo.x);
      maxX = Math.max(maxX, nodo.x);
      minY = Math.min(minY, nodo.y);
      maxY = Math.max(maxY, nodo.y);
    });

    const scaleX = (maxX - minX) > 0 ? drawWidth / (maxX - minX) : 1;
    const scaleY = (maxY - minY) > 0 ? drawHeight / (maxY - minY) : 1;

    const convertX = (x) => padding + (x - minX) * scaleX;
    const convertY = (y) => padding + (y - minY) * scaleY;

    return { convertX, convertY, minX, minY, scaleX, scaleY };
  };

  const dibujarMapa = () => {
    const canvas = canvasRef.current;
    if (!canvas || !mapa) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.fillStyle = '#f8f9fa';
    ctx.fillRect(0, 0, width, height);

    const coords = getCanvasCoords();
    if (!coords) return;
    const { convertX, convertY } = coords;

    // Dibujar aristas
    ctx.strokeStyle = '#bdc3c7';
    ctx.lineWidth = 2;

    mapa.aristas.forEach(arista => {
      const nodoOrigen = mapa.nodos.find(n => n.id === arista.origen);
      const nodoDestino = mapa.nodos.find(n => n.id === arista.destino);

      if (nodoOrigen && nodoDestino) {
        const x1 = convertX(nodoOrigen.x);
        const y1 = convertY(nodoOrigen.y);
        const x2 = convertX(nodoDestino.x);
        const y2 = convertY(nodoDestino.y);

        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.stroke();

        const midX = (x1 + x2) / 2;
        const midY = (y1 + y2) / 2;
        ctx.fillStyle = '#7f8c8d';
        ctx.font = '12px Arial';
        ctx.fillText(`${arista.distancia.toFixed(0)}m`, midX, midY - 5);
      }
    });

    // Dibujar ruta si existe
    if (ruta && ruta.pasos) {
      ctx.strokeStyle = '#27ae60';
      ctx.lineWidth = 4;
      ctx.setLineDash([10, 5]);

      for (let i = 0; i < ruta.pasos.length - 1; i++) {
        const paso1 = ruta.pasos[i];
        const paso2 = ruta.pasos[i + 1];

        const nodo1 = mapa.nodos.find(n => n.id === paso1.atraccionId);
        const nodo2 = mapa.nodos.find(n => n.id === paso2.atraccionId);

        if (nodo1 && nodo2) {
          ctx.beginPath();
          ctx.moveTo(convertX(nodo1.x), convertY(nodo1.y));
          ctx.lineTo(convertX(nodo2.x), convertY(nodo2.y));
          ctx.stroke();
        }
      }
      ctx.setLineDash([]);
    }

    // Dibujar nodos
    mapa.nodos.forEach(nodo => {
      const x = convertX(nodo.x);
      const y = convertY(nodo.y);
      const atraccion = atraccionesInfo[nodo.id];

      let color = '#27ae60';
      if (atraccion?.estado === 'CERRADA') color = '#e74c3c';
      if (atraccion?.estado === 'MANTENIMIENTO') color = '#f39c12';

      if (selectedAtraccion?.id === nodo.id) {
        ctx.fillStyle = '#fff';
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
      } else {
        ctx.fillStyle = color;
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 2;
      }

      ctx.beginPath();
      ctx.arc(x, y, 15, 0, 2 * Math.PI);
      ctx.fill();
      ctx.stroke();

      ctx.fillStyle = selectedAtraccion?.id === nodo.id ? color : '#fff';
      ctx.font = 'bold 12px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(nodo.id, x, y);
    });

    if (selectedAtraccion) {
      dibujarPanelInfo(ctx, selectedAtraccion);
    }
  };

  const dibujarPanelInfo = (ctx, atraccion) => {
    const x = 20;
    const y = 20;
    const width = 300;
    const height = 200;

    ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
    ctx.fillRect(x, y, width, height);

    ctx.strokeStyle = '#27ae60';
    ctx.lineWidth = 2;
    ctx.strokeRect(x, y, width, height);

    ctx.fillStyle = '#fff';
    ctx.font = 'bold 14px Arial';
    ctx.textAlign = 'left';

    ctx.fillText(atraccion.nombre, x + 10, y + 25);

    ctx.font = '12px Arial';
    ctx.fillStyle = '#ecf0f1';
    ctx.fillText(`Tipo: ${atraccion.tipo}`, x + 10, y + 45);
    ctx.fillText(`Estado: ${atraccion.estado}`, x + 10, y + 65);
    ctx.fillText(`Tiempo espera: ${atraccion.tiempoEsperaEstimado || 'N/A'} min`, x + 10, y + 85);
    ctx.fillText(`Capacidad: ${atraccion.capacidadMaxima} personas`, x + 10, y + 105);
    ctx.fillText(`Edad mínima: ${atraccion.edadMinima || 'Sin límite'} años`, x + 10, y + 125);
    ctx.fillText(`Altura mínima: ${atraccion.alturaMinima || 'Sin límite'} m`, x + 10, y + 145);
  };

  const handleCanvasClick = (e) => {
    const canvas = canvasRef.current;
    if (!canvas || !mapa) return;

    const rect = canvas.getBoundingClientRect();

    // FIX: escalar coordenadas del click al espacio interno del canvas
    const scaleFactorX = canvas.width / rect.width;
    const scaleFactorY = canvas.height / rect.height;

    const clickX = (e.clientX - rect.left) * scaleFactorX;
    const clickY = (e.clientY - rect.top) * scaleFactorY;

    const coords = getCanvasCoords();
    if (!coords) return;
    const { convertX, convertY } = coords;

    for (let nodo of mapa.nodos) {
      const x = convertX(nodo.x);
      const y = convertY(nodo.y);
      const distance = Math.sqrt((clickX - x) ** 2 + (clickY - y) ** 2);

      if (distance < 20) {
        const atraccion = atraccionesInfo[nodo.id];
        setSelectedAtraccion(atraccion);
        return;
      }
    }

    setSelectedAtraccion(null);
  };

  const calcularRuta = async () => {
    if (!origen || !destino) {
      alert('Por favor selecciona origen y destino');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/rutas/mas-corta`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          origenId: origen,
          destinoId: destino,
        }),
      });

      const data = await response.json();
      if (data.success) {
        setRuta(data.data);
      } else {
        alert('No se pudo calcular la ruta');
      }
    } catch (error) {
      console.error('Error calculando ruta:', error);
      alert('Error al calcular ruta');
    }
  };

  const unirseCola = async () => {
    if (!selectedAtraccion) return;

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
          atraccionId: selectedAtraccion.id,
        }),
      });

      const data = await response.json();
      if (data.success) {
        alert('✅ Te has unido a la cola de ' + selectedAtraccion.nombre);
      } else {
        alert('❌ ' + (data.message || 'Error al unirse a la cola'));
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error al unirse a la cola');
    }
  };

  if (loading) {
    return <div className="mapa-container"><p>Cargando mapa...</p></div>;
  }

  return (
    <div className="mapa-container">
      <div className="mapa-header">
        <h2>🗺️ Mapa del Parque</h2>
        <p>Selecciona una atracción para ver detalles o calcular rutas</p>
      </div>

      {mensaje && (
        <div className={`mensaje ${mensaje.includes('❌') ? 'error' : 'success'}`}>
          {mensaje}
        </div>
      )}

      <div className="mapa-content">
        <div className="canvas-wrapper">
          <canvas
            ref={canvasRef}
            width={800}
            height={550}
            onClick={handleCanvasClick}
            className="mapa-canvas"
          />
        </div>

        <div className="mapa-panel">
          {/* Selector de Ruta */}
          <div className="panel-section">
            <h3>📍 Calcular Ruta</h3>
            <div className="selectors">
              <select
                value={origen || ''}
                onChange={(e) => setOrigen(Number(e.target.value) || null)}
              >
                <option value="">Origen</option>
                {atracciones.map(a => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>

              <select
                value={destino || ''}
                onChange={(e) => setDestino(Number(e.target.value) || null)}
              >
                <option value="">Destino</option>
                {atracciones.map(a => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>

              <button onClick={calcularRuta} className="btn-calcular">
                Calcular Ruta
              </button>
            </div>
          </div>

          {/* Info Ruta */}
          {ruta && (
            <div className="panel-section ruta-info">
              <h3>✅ Ruta Calculada</h3>
              <p className="ruta-mensaje">{ruta.mensaje}</p>
              <div className="ruta-stats">
                <div className="stat">
                  <span className="label">Distancia:</span>
                  <span className="value">{ruta.distanciaTotal.toFixed(0)}m</span>
                </div>
                <div className="stat">
                  <span className="label">Tiempo:</span>
                  <span className="value">{ruta.tiempoEstimadoTotal} min</span>
                </div>
              </div>
              <div className="ruta-pasos">
                {ruta.pasos.map((paso, i) => (
                  <div key={i} className="paso">
                    <span className="numero">{paso.orden}</span>
                    <span className="nombre">{paso.atraccionNombre}</span>
                    {i < ruta.pasos.length - 1 && (
                      <span className="distancia">{paso.distanciaDesdeAnterior.toFixed(0)}m</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Info Atracción Seleccionada */}
          {selectedAtraccion && (
            <div className="panel-section">
              <div className="atraccion-header">
                <h3>{selectedAtraccion.nombre}</h3>
                <button
                  className={`heart-btn ${esFavorito(selectedAtraccion.id) ? 'favorito' : ''}`}
                  onClick={() => toggleFavorito(selectedAtraccion.id)}
                  title="Agregar a favoritos"
                >
                  {esFavorito(selectedAtraccion.id) ? '❤️' : '🤍'}
                </button>
              </div>

              <div className="atraccion-detail">
                <p><strong>Tipo:</strong> {selectedAtraccion.tipo}</p>
                <p><strong>Estado:</strong>
                  <span className={`estado ${selectedAtraccion.estado}`}>
                    {selectedAtraccion.estado}
                  </span>
                </p>
                <p><strong>Tiempo espera:</strong> {selectedAtraccion.tiempoEsperaEstimado} min</p>
                <p><strong>Capacidad:</strong> {selectedAtraccion.capacidadMaxima} personas</p>
                {selectedAtraccion.edadMinima && (
                  <p><strong>Edad mínima:</strong> {selectedAtraccion.edadMinima} años</p>
                )}
                {selectedAtraccion.alturaMinima && (
                  <p><strong>Altura mínima:</strong> {selectedAtraccion.alturaMinima}m</p>
                )}

                <div className="atraccion-buttons">
                  <button
                    onClick={unirseCola}
                    className="btn-cola"
                    disabled={selectedAtraccion.estado !== 'ACTIVA'}
                  >
                    {selectedAtraccion.estado === 'ACTIVA' ? '➕ Unirse a Cola' : '❌ No disponible'}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Leyenda */}
          <div className="panel-section legend">
            <h3>Leyenda</h3>
            <div className="legend-item">
              <span className="legend-color" style={{ backgroundColor: '#27ae60' }}></span>
              <span>Activa</span>
            </div>
            <div className="legend-item">
              <span className="legend-color" style={{ backgroundColor: '#f39c12' }}></span>
              <span>Mantenimiento</span>
            </div>
            <div className="legend-item">
              <span className="legend-color" style={{ backgroundColor: '#e74c3c' }}></span>
              <span>Cerrada</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}