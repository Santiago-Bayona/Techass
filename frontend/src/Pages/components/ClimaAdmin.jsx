import React, { useState, useEffect } from 'react';
import '../../Styles/ClimaAdmin.css';

const API_URL = 'http://localhost:8080/api';

export default function ClimaAdmin({ adminData }) {
  const [alertasActivas, setAlertasActivas] = useState([]);
  const [historialAlertas, setHistorialAlertas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showFormulario, setShowFormulario] = useState(false);
  const [tipoAlerta, setTipoAlerta] = useState('TORMENTA_ELECTRICA');
  const [severidad, setSeveridad] = useState('MEDIA');
  const [duracion, setDuracion] = useState('');
  const [comentario, setComentario] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const tiposAlerta = [
    'TORMENTA_ELECTRICA',
    'LLUVIA_FUERTE',
    'VIENTO_FUERTE',
    'CALOR_EXTREMO'
  ];

  const severidades = ['BAJA', 'MEDIA', 'ALTA'];

  useEffect(() => {
    cargarDatos();
    const interval = setInterval(cargarDatos, 10000);
    return () => clearInterval(interval);
  }, []);

  const cargarDatos = async () => {
    try {
      const token = localStorage.getItem('token');

      const [respActivas, respHistorial] = await Promise.all([
        fetch(`${API_URL}/clima/activas`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/clima/historial`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      const dataActivas = await respActivas.json();
      const dataHistorial = await respHistorial.json();

      setAlertasActivas(dataActivas.data || []);
      setHistorialAlertas(dataHistorial.data || []);
    } catch (error) {
      console.error('❌ Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const activarAlerta = async () => {
    if (!duracion || duracion <= 0) {
      alert('⚠️ Ingresa una duración válida (5-180 minutos)');
      return;
    }

    const duracionNum = parseInt(duracion);
    if (duracionNum < 5 || duracionNum > 180) {
      alert('⚠️ La duración debe estar entre 5 y 180 minutos');
      return;
    }

    try {
      setSubmitting(true);
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const datosAlerta = {
        tipoClima: tipoAlerta,
        severidad: severidad,
        duracionEstimadaMinutos: duracionNum,
        comentario: comentario || 'Alerta de clima activada'
      };

      const response = await fetch(`${API_URL}/clima/activar/${usuarioId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(datosAlerta),
      });

      const responseData = await response.json();

      if (response.ok) {
        alert('✅ Alerta climática activada. Las atracciones afectadas han sido cerradas automáticamente.');
        setShowFormulario(false);
        setDuracion('');
        setComentario('');
        setTipoAlerta('TORMENTA_ELECTRICA');
        setSeveridad('MEDIA');
        await cargarDatos();
      } else {
        alert(`❌ Error: ${responseData.message || 'No se pudo activar la alerta'}`);
      }
    } catch (error) {
      alert('❌ Error de conexión: ' + error.message);
    } finally {
      setSubmitting(false);
    }
  };

  const desactivarAlerta = async (alertaId) => {
    if (!window.confirm('¿Desactivar esta alerta? Las atracciones afectadas volverán a estar activas.')) return;

    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');

      const response = await fetch(`${API_URL}/clima/desactivar/${alertaId}/${usuarioId}`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      const responseData = await response.json();

      if (response.ok) {
        alert('✅ Alerta desactivada. Las atracciones han sido reactivadas.');
        await cargarDatos();
      } else {
        alert(`❌ Error: ${responseData.message || 'No se pudo desactivar la alerta'}`);
      }
    } catch (error) {
      alert('❌ Error: ' + error.message);
    }
  };

  const getDuracion = (alerta) => {
    if (alerta.fechaGeneracion && alerta.fechaFinEstimada) {
      const inicio = new Date(alerta.fechaGeneracion);
      const fin = new Date(alerta.fechaFinEstimada);
      const minutos = Math.round((fin - inicio) / 60000);
      return `${minutos} min`;
    }
    return 'N/A';
  };

  if (loading) {
    return (
      <div className="clima-container">
        <div className="clima-header">
          <h2>⛈️ Gestión de Alertas Climáticas</h2>
        </div>
        <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
          <p>⏳ Cargando alertas...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="clima-container">
      <div className="clima-header">
        <h2>⛈️ Gestión de Alertas Climáticas</h2>
      </div>

      <div className="estado-actual">
        <div>
          <h3>ESTADO ACTUAL</h3>
          <p>
            {alertasActivas.length === 0
              ? '✅ Sin alertas activas'
              : `⚠️ ${alertasActivas.length} Alertas Activas`}
          </p>
        </div>
        <button
          className="btn-nueva-alerta"
          onClick={() => setShowFormulario(!showFormulario)}
        >
          {showFormulario ? '✕ Cancelar' : '+ Activar nueva alerta'}
        </button>
      </div>

      {showFormulario && (
        <div className="formulario-alerta">
          <h4>🌩️ Crear Nueva Alerta Climática</h4>
          <p style={{ color: '#666', fontSize: '13px', marginBottom: '20px' }}>
            El sistema cerrará automáticamente las atracciones afectadas según el tipo de clima seleccionado.
          </p>

          <div className="form-group">
            <label>Tipo de clima:</label>
            <select value={tipoAlerta} onChange={(e) => setTipoAlerta(e.target.value)}>
              {tiposAlerta.map(tipo => (
                <option key={tipo} value={tipo}>{tipo.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Severidad:</label>
            <select value={severidad} onChange={(e) => setSeveridad(e.target.value)}>
              {severidades.map(sev => (
                <option key={sev} value={sev}>{sev}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Duración estimada (minutos) - 5 a 180:</label>
            <input
              type="number"
              value={duracion}
              onChange={(e) => setDuracion(e.target.value)}
              min="5"
              max="180"
              placeholder="Ej: 30"
            />
          </div>

          <div className="form-group">
            <label>Comentario:</label>
            <textarea
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
              placeholder="Detalles adicionales sobre la alerta..."
            />
          </div>

          <button
            className="btn-activar"
            onClick={activarAlerta}
            disabled={submitting}
          >
            {submitting ? '⏳ Activando...' : '✅ Activar Alerta'}
          </button>
        </div>
      )}

      <div className="alertas-activas">
        <h3>🚨 ALERTAS ACTIVAS</h3>
        {alertasActivas.length === 0 ? (
          <p className="empty">✅ No hay alertas activas en este momento</p>
        ) : (
          <div className="alertas-lista">
            {alertasActivas.map(alerta => (
              <div key={alerta.id} className={`alerta-item ${alerta.severidad?.toLowerCase()}`}>
                <div className="alerta-header">
                  <span className="tipo">
                    {alerta.tipoAlerta?.replace(/_/g, ' ') || 'SIN TIPO'}
                  </span>
                  <span className={`severidad ${alerta.severidad?.toLowerCase()}`}>
                    {alerta.severidad || 'SIN SEVERIDAD'}
                  </span>
                </div>
                <p><strong>📅 Fecha:</strong> {alerta.fechaGeneracion ? new Date(alerta.fechaGeneracion).toLocaleString() : 'Sin fecha'}</p>
                <p><strong>⏱️ Duración:</strong> {getDuracion(alerta)}</p>
                <p><strong>📝 Mensaje:</strong> {alerta.mensaje || alerta.comentario || 'Sin comentario'}</p>
                {alerta.atraccionesAfectadasNombres?.length > 0 && (
                  <p><strong>🎢 Atracciones cerradas:</strong> {alerta.atraccionesAfectadasNombres.join(', ')}</p>
                )}
                <button
                  className="btn-desactivar"
                  onClick={() => desactivarAlerta(alerta.id)}
                >
                  ❌ Desactivar
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="historial-alertas">
        <h3>📋 HISTORIAL DE ALERTAS</h3>
        {historialAlertas.length === 0 ? (
          <p className="empty">No hay historial de alertas</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Tipo</th>
                <th>Severidad</th>
                <th>Duración</th>
                <th>Atracciones cerradas</th>
              </tr>
            </thead>
            <tbody>
              {historialAlertas.map(alerta => (
                <tr key={alerta.id}>
                  <td>{alerta.fechaGeneracion ? new Date(alerta.fechaGeneracion).toLocaleString() : 'Sin fecha'}</td>
                  <td>{alerta.tipoAlerta?.replace(/_/g, ' ') || 'SIN TIPO'}</td>
                  <td>{alerta.severidad || 'SIN SEVERIDAD'}</td>
                  <td>{getDuracion(alerta)}</td>
                  <td>{alerta.atraccionesAfectadasNombres?.join(', ') || 'Ninguna'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}