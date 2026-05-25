import React, { useState, useEffect, useRef } from 'react';
import '../../Styles/MapaAdmin.css';

const API_URL = 'http://localhost:8080/api';

export default function MapaAdmin({ adminData }) {
  const canvasRef = useRef(null);
  const [mapa, setMapa] = useState(null);
  const [grafo, setGrafo] = useState(null);
  const [atracciones, setAtracciones] = useState([]);
  const [selectedAtraccion, setSelectedAtraccion] = useState(null);
  const [origen, setOrigen] = useState(null);
  const [destino, setDestino] = useState(null);
  const [ruta, setRuta] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarDatos();
  }, []);

  useEffect(() => {
    if (mapa && atracciones.length > 0) {
      dibujarMapa();
    }
  }, [mapa, atracciones, ruta, selectedAtraccion]);

  const cargarDatos = async () => {
    try {
      const token = localStorage.getItem('token');

      const [respMapa, respGrafo, respAtracciones] = await Promise.all([
        fetch(`${API_URL}/rutas/mapa-visual`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/rutas/grafo-completo`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/atracciones`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      const dataMapa = await respMapa.json();
      const dataGrafo = await respGrafo.json();
      const dataAtracciones = await respAtracciones.json();

      setMapa(dataMapa.data);
      setGrafo(dataGrafo.data);
      setAtracciones(dataAtracciones.data || []);
    } catch (error) {
      console.error('Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const dibujarMapa = () => {
    const canvas = canvasRef.current;
    if (!canvas || !mapa || !mapa.nodos) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);

    const padding = 40;
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

    const rangeX = maxX - minX || 1;
    const rangeY = maxY - minY || 1;
    const scaleX = drawWidth / rangeX;
    const scaleY = drawHeight / rangeY;

    const convertX = (x) => padding + (x - minX) * scaleX;
    const convertY = (y) => padding + (y - minY) * scaleY;

    // Aristas
    if (mapa.aristas && mapa.aristas.length > 0) {
      ctx.strokeStyle = '#b0b0b0';
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
          ctx.fillStyle = '#666666';
          ctx.font = 'bold 11px Arial';
          ctx.textAlign = 'center';
          ctx.fillText(`${Math.round(arista.distancia)}m`, midX, midY - 5);
        }
      });
    }

    // Ruta
    if (ruta && ruta.pasos && ruta.pasos.length > 1) {
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

    // Nodos
    mapa.nodos.forEach(nodo => {
      const x = convertX(nodo.x);
      const y = convertY(nodo.y);
      const atraccion = atracciones.find(a => a.id === nodo.id);

      let color = '#27ae60';
      if (atraccion?.estado === 'CERRADA' || atraccion?.estado === 'BLOQUEADA') color = '#e74c3c';
      if (atraccion?.estado === 'MANTENIMIENTO') color = '#f39c12';

      if (selectedAtraccion?.id === nodo.id) {
        ctx.fillStyle = '#ffffff';
        ctx.strokeStyle = color;
        ctx.lineWidth = 4;
      } else {
        ctx.fillStyle = color;
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 2;
      }

      ctx.beginPath();
      ctx.arc(x, y, 15, 0, 2 * Math.PI);
      ctx.fill();
      ctx.stroke();

      ctx.fillStyle = selectedAtraccion?.id === nodo.id ? color : '#ffffff';
      ctx.font = 'bold 12px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(nodo.id, x, y);
    });

    // Panel info en canvas si hay seleccionada
    if (selectedAtraccion) {
      const panelX = 10;
      const panelY = 10;
      const panelWidth = 280;
      const panelHeight = 170;

      ctx.fillStyle = 'rgba(0, 0, 0, 0.85)';
      ctx.fillRect(panelX, panelY, panelWidth, panelHeight);

      ctx.strokeStyle = '#27ae60';
      ctx.lineWidth = 2;
      ctx.strokeRect(panelX, panelY, panelWidth, panelHeight);

      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 13px Arial';
      ctx.textAlign = 'left';
      ctx.fillText(selectedAtraccion.nombre, panelX + 10, panelY + 25);

      ctx.font = '11px Arial';
      ctx.fillStyle = '#ecf0f1';
      let lineY = panelY + 45;
      const lineHeight = 20;

      ctx.fillText(`Tipo: ${selectedAtraccion.tipo}`, panelX + 10, lineY); lineY += lineHeight;
      ctx.fillText(`Estado: ${selectedAtraccion.estado}`, panelX + 10, lineY); lineY += lineHeight;
      ctx.fillText(`Visitantes: ${selectedAtraccion.visitantesActuales || 0}/${selectedAtraccion.capacidadMaxima}`, panelX + 10, lineY); lineY += lineHeight;
      ctx.fillText(`Cola: ${selectedAtraccion.tiempoEsperaEstimado || 0} min`, panelX + 10, lineY); lineY += lineHeight;
      ctx.fillText(`Zona: ${selectedAtraccion.zona?.nombre || 'N/A'}`, panelX + 10, lineY);
    }
  };

  const handleCanvasClick = (e) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    if (!mapa || !mapa.nodos) return;

    const padding = 40;
    const drawWidth = canvas.width - 2 * padding;
    const drawHeight = canvas.height - 2 * padding;

    let minX = Infinity, maxX = -Infinity;
    let minY = Infinity, maxY = -Infinity;

    mapa.nodos.forEach(nodo => {
      minX = Math.min(minX, nodo.x);
      maxX = Math.max(maxX, nodo.x);
      minY = Math.min(minY, nodo.y);
      maxY = Math.max(maxY, nodo.y);
    });

    const rangeX = maxX - minX || 1;
    const rangeY = maxY - minY || 1;
    const scaleX = drawWidth / rangeX;
    const scaleY = drawHeight / rangeY;

    const convertX = (x) => padding + (x - minX) * scaleX;
    const convertY = (y) => padding + (y - minY) * scaleY;

    for (let nodo of mapa.nodos) {
      const x = convertX(nodo.x);
      const y = convertY(nodo.y);
      const distance = Math.sqrt((clickX - x) ** 2 + (clickY - y) ** 2);

      if (distance < 20) {
        const atraccion = atracciones.find(a => a.id === nodo.id);
        if (atraccion) {
          setSelectedAtraccion(atraccion);
        }
        return;
      }
    }

    setSelectedAtraccion(null);
  };

  const calcularRuta = async () => {
    if (!origen || !destino) {
      alert('Selecciona origen y destino');
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
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error al calcular ruta');
    }
  };

  const refrescarMapa = async () => {
    try {
      const token = localStorage.getItem('token');
      await fetch(`${API_URL}/rutas/refrescar`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      cargarDatos();
    } catch (error) {
      console.error('Error:', error);
    }
  };

  if (loading) {
    return (
      <div className="mapa-admin-container">
        <div className="mapa-header">
          <h2>🗺️ Cargando mapa...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="mapa-admin-container">
      <div className="mapa-header">
        <h2>🗺️ Mapa del Parque - Vista Administrativa</h2>
        <p>Análisis de conectividad y flujo de visitantes</p>
      </div>

      <div className="mapa-content">
        <canvas
          ref={canvasRef}
          width={700}
          height={600}
          onClick={handleCanvasClick}
          className="mapa-canvas"
        />

        <div className="mapa-panel">
          <div className="panel-section">
            <h3>📍 Calcular Ruta</h3>
            <select value={origen || ''} onChange={(e) => setOrigen(Number(e.target.value) || null)}>
              <option value="">Selecciona origen</option>
              {atracciones.map(a => (
                <option key={a.id} value={a.id}>{a.nombre}</option>
              ))}
            </select>
            <select value={destino || ''} onChange={(e) => setDestino(Number(e.target.value) || null)}>
              <option value="">Selecciona destino</option>
              {atracciones.map(a => (
                <option key={a.id} value={a.id}>{a.nombre}</option>
              ))}
            </select>
            <button onClick={calcularRuta} className="btn-calcular">Calcular Ruta</button>
            <button onClick={refrescarMapa} className="btn-refrescar">🔄 Refrescar</button>
          </div>

          {ruta && (
            <div className="panel-section">
              <h3>✅ Ruta Calculada</h3>
              <p style={{ fontSize: '13px', marginBottom: '10px' }}>{ruta.mensaje}</p>
              <div className="ruta-stats">
                <div className="stat">
                  <span className="label">Distancia:</span>
                  <span className="value">{Math.round(ruta.distanciaTotal)}m</span>
                </div>
                <div className="stat">
                  <span className="label">Tiempo:</span>
                  <span className="value">{ruta.tiempoEstimadoTotal || 'N/A'} min</span>
                </div>
              </div>
            </div>
          )}

          {/* Análisis del Grafo - MEJORADO */}
          {mapa && (
            <div className="panel-section">
              <h3>📊 Análisis del Grafo</h3>
              <div className="grafo-stats">
                <div className="stat">
                  <span className="label">Nodos:</span>
                  <span className="value">{mapa.nodos.length}</span>
                </div>
                <div className="stat">
                  <span className="label">Aristas:</span>
                  <span className="value">{mapa.aristas.length}</span>
                </div>
              </div>
            </div>
          )}

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