import React, { useState, useEffect } from 'react';
import '../../Styles/ReportesAdmin.css';

const API_URL = 'http://localhost:8080/api';

export default function ReportesAdmin({ adminData }) {
  const [activeTab, setActiveTab] = useState('mantenimiento');
  const [reportes, setReportes] = useState({});
  const [loading, setLoading] = useState(true);
  const hoy = new Date();
  hoy.setMinutes(hoy.getMinutes() - hoy.getTimezoneOffset());
  const [fechaSeleccionada, setFechaSeleccionada] = useState(hoy.toISOString().split('T')[0]);
  const [tipoReporte, setTipoReporte] = useState('DIARIO');

  useEffect(() => {
    cargarReportes();
  }, [fechaSeleccionada, tipoReporte]);

  const cargarReportes = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');

      const [respMant, respAfl, respIng, respAtr, respTiempos] = await Promise.all([
        fetch(`${API_URL}/reportes/mantenimiento`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/reportes/afluencia/${fechaSeleccionada}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/reportes/ingresos/diario/${fechaSeleccionada}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/reportes/atracciones`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/reportes/tiempos/${fechaSeleccionada}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      const dataMant = await respMant.json();
      const dataAfl = await respAfl.json();
      const dataIng = await respIng.json();
      const dataAtr = await respAtr.json();
      const dataTiempos = await respTiempos.json();

      setReportes({
        mantenimiento: dataMant.data,
        afluencia: dataAfl.data,
        ingresos: dataIng.data,
        atracciones: dataAtr.data,
        tiempos: dataTiempos.data
      });
    } catch (error) {
      console.error('Error cargando reportes:', error);
    } finally {
      setLoading(false);
    }
  };

  const downloadReporte = async (tipo) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(
        `${API_URL}/reportes/${tipo}/descargar?fecha=${fechaSeleccionada}&tipo=${tipoReporte}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reporte_${tipo}_${fechaSeleccionada}.pdf`;
      a.click();
    } catch (error) {
      console.error('Error descargando reporte:', error);
    }
  };

  const renderEmpty = (msg = 'No hay datos disponibles para este período') => (
    <p style={{ color: '#999', textAlign: 'center', padding: '30px', fontStyle: 'italic' }}>{msg}</p>
  );

  if (loading) {
    return <div className="reportes-container"><p style={{padding:'40px',textAlign:'center'}}>⏳ Cargando reportes...</p></div>;
  }

  return (
    <div className="reportes-container">
      <div className="reportes-header">
        <h2>📊 Reportes del Parque</h2>
      </div>

      <div className="filtros">
        <div className="filtro-group">
          <label>Fecha:</label>
          <input
            type="date"
            value={fechaSeleccionada}
            onChange={(e) => setFechaSeleccionada(e.target.value)}
          />
        </div>
        <div className="filtro-group">
          <label>Tipo:</label>
          <select value={tipoReporte} onChange={(e) => setTipoReporte(e.target.value)}>
            <option value="DIARIO">Diario</option>
            <option value="SEMANAL">Semanal</option>
            <option value="MENSUAL">Mensual</option>
          </select>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${activeTab === 'mantenimiento' ? 'active' : ''}`} onClick={() => setActiveTab('mantenimiento')}>🔧 Mantenimiento</button>
        <button className={`tab ${activeTab === 'afluencia' ? 'active' : ''}`} onClick={() => setActiveTab('afluencia')}>👥 Afluencia</button>
        <button className={`tab ${activeTab === 'ingresos' ? 'active' : ''}`} onClick={() => setActiveTab('ingresos')}>💰 Ingresos</button>
        <button className={`tab ${activeTab === 'atracciones' ? 'active' : ''}`} onClick={() => setActiveTab('atracciones')}>🎢 Atracciones</button>
        <button className={`tab ${activeTab === 'tiempos' ? 'active' : ''}`} onClick={() => setActiveTab('tiempos')}>⏱️ Tiempos</button>
      </div>

      <div className="reporte-content">

        {/* MANTENIMIENTO */}
        {activeTab === 'mantenimiento' && (
          <div className="reporte-section">
            <h3>🔧 Reporte de Mantenimiento</h3>
            {!reportes.mantenimiento ? renderEmpty() : (
              <>
                <div className="stats-grid">
                  <div className="stat-card">
                    <p className="stat-label">TOTAL MANTENIMIENTOS:</p>
                    <p className="stat-value">{reportes.mantenimiento.totalMantenimientos ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">PENDIENTES:</p>
                    <p className="stat-value">{reportes.mantenimiento.mantenimientosPendientes ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">COMPLETADOS:</p>
                    <p className="stat-value">{reportes.mantenimiento.mantenimientosCompletados ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">TIEMPO PROMEDIO:</p>
                    <p className="stat-value">
                      {reportes.mantenimiento.tiempoPromedioResolucionMinutos
                        ? `${reportes.mantenimiento.tiempoPromedioResolucionMinutos.toFixed(0)} min`
                        : 'N/A'}
                    </p>
                  </div>
                </div>
                {reportes.mantenimiento.atraccionesMasIncidentes?.length > 0 && (
                  <>
                    <h4>Atracciones con más incidentes:</h4>
                    <ul>
                      {reportes.mantenimiento.atraccionesMasIncidentes.map((a, i) => (
                        <li key={i}>{a.nombre} — {a.incidentes} incidentes ({a.estadoActual})</li>
                      ))}
                    </ul>
                  </>
                )}
                <button className="btn-descargar" onClick={() => downloadReporte('mantenimiento')}>⬇️ Descargar</button>
              </>
            )}
          </div>
        )}

        {/* AFLUENCIA */}
        {activeTab === 'afluencia' && (
          <div className="reporte-section">
            <h3>👥 Reporte de Afluencia</h3>
            {!reportes.afluencia ? renderEmpty() : (
              <>
                <div className="stats-grid">
                  <div className="stat-card">
                    <p className="stat-label">VISITANTES HOY:</p>
                    <p className="stat-value">{reportes.afluencia.totalVisitantesDia ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">NIVEL DE AFLUENCIA:</p>
                    <p className="stat-value" style={{
                      color: reportes.afluencia.nivelAfluencia === 'ALTA' || reportes.afluencia.nivelAfluencia === 'LLENO'
                        ? '#e74c3c' : '#27ae60'
                    }}>
                      {reportes.afluencia.nivelAfluencia ?? 'N/A'}
                    </p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">OCUPACIÓN:</p>
                    <p className="stat-value">
                      {reportes.afluencia.porcentajeOcupacion != null
                        ? `${reportes.afluencia.porcentajeOcupacion.toFixed(1)}%`
                        : 'N/A'}
                    </p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">CAPACIDAD MÁXIMA:</p>
                    <p className="stat-value">{reportes.afluencia.capacidadMaximaParque ?? 'N/A'}</p>
                  </div>
                </div>
                {reportes.afluencia.visitantesPorZona && (
                  <>
                    <h4>Visitantes por zona:</h4>
                    <div className="zona-list">
                      {Object.entries(reportes.afluencia.visitantesPorZona).map(([zona, visitantes]) => (
                        <div key={zona} className="zona-item">
                          <span>{zona}: {visitantes} visitantes</span>
                          {reportes.afluencia.ocupacionPorZona?.[zona] != null && (
                            <div className="progress-bar">
                              <div className="progress-fill" style={{ width: `${Math.min(reportes.afluencia.ocupacionPorZona[zona], 100)}%` }}></div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </>
                )}
                {reportes.afluencia.recomendaciones?.length > 0 && (
                  <>
                    <h4>Recomendaciones:</h4>
                    <ul>{reportes.afluencia.recomendaciones.map((r, i) => <li key={i}>{r}</li>)}</ul>
                  </>
                )}
                <button className="btn-descargar" onClick={() => downloadReporte('afluencia')}>⬇️ Descargar</button>
              </>
            )}
          </div>
        )}

        {/* INGRESOS */}
        {activeTab === 'ingresos' && (
          <div className="reporte-section">
            <h3>💰 Reporte de Ingresos</h3>
            {!reportes.ingresos ? renderEmpty() : (
              <>
                <div className="stats-grid">
                  <div className="stat-card">
                    <p className="stat-label">INGRESOS TOTALES:</p>
                    <p className="stat-value">${reportes.ingresos.ingresosTotales?.toFixed(2) ?? '0.00'}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">VISITANTES:</p>
                    <p className="stat-value">{reportes.ingresos.totalVisitantes ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">PROMEDIO POR VISITANTE:</p>
                    <p className="stat-value">${reportes.ingresos.promedioPorVisitante?.toFixed(2) ?? '0.00'}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">TENDENCIA:</p>
                    <p className="stat-value">{reportes.ingresos.tendencia ?? 'N/A'}</p>
                  </div>
                </div>
                {reportes.ingresos.ingresosPorTipoTicket && (
                  <>
                    <h4>Ingresos por tipo de ticket:</h4>
                    <div className="ticket-list">
                      {Object.entries(reportes.ingresos.ingresosPorTipoTicket).map(([tipo, monto]) => (
                        <div key={tipo} className="ticket-item">
                          <span>{tipo}: ${monto?.toFixed(2) ?? '0.00'}
                            {reportes.ingresos.ticketsVendidosPorTipo?.[tipo] != null &&
                              ` (${reportes.ingresos.ticketsVendidosPorTipo[tipo]} tickets)`}
                          </span>
                        </div>
                      ))}
                    </div>
                  </>
                )}
                <button className="btn-descargar" onClick={() => downloadReporte('ingresos')}>⬇️ Descargar</button>
              </>
            )}
          </div>
        )}

        {/* ATRACCIONES */}
        {activeTab === 'atracciones' && (
          <div className="reporte-section">
            <h3>🎢 Reporte de Atracciones</h3>
            {!reportes.atracciones ? renderEmpty() : (
              <>
                <div className="stats-grid">
                  <div className="stat-card">
                    <p className="stat-label">TOTAL VISITAS:</p>
                    <p className="stat-value">{reportes.atracciones.totalVisitasParque ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">ATRACCIÓN ESTRELLA:</p>
                    <p className="stat-value">{reportes.atracciones.atraccionEstrella ?? 'N/A'}</p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">PROMEDIO POR ATRACCIÓN:</p>
                    <p className="stat-value">
                      {reportes.atracciones.promedioVisitantesPorAtraccion?.toFixed(1) ?? 'N/A'}
                    </p>
                  </div>
                </div>
                {reportes.atracciones.atraccionesMasVisitadas?.length > 0 && (
                  <>
                    <h4>Top más visitadas:</h4>
                    <ol>
                      {reportes.atracciones.atraccionesMasVisitadas.map((a, i) => (
                        <li key={i}>{a.nombre} — {a.totalVisitantes} visitantes ({a.zona})</li>
                      ))}
                    </ol>
                  </>
                )}
                {reportes.atracciones.atraccionesMenosVisitadas?.length > 0 && (
                  <>
                    <h4>Top menos visitadas:</h4>
                    <ol>
                      {reportes.atracciones.atraccionesMenosVisitadas.map((a, i) => (
                        <li key={i}>{a.nombre} — {a.totalVisitantes} visitantes ({a.zona})</li>
                      ))}
                    </ol>
                  </>
                )}
                <button className="btn-descargar" onClick={() => downloadReporte('atracciones')}>⬇️ Descargar</button>
              </>
            )}
          </div>
        )}

        {/* TIEMPOS */}
        {activeTab === 'tiempos' && (
          <div className="reporte-section">
            <h3>⏱️ Reporte de Tiempos</h3>
            {!reportes.tiempos ? renderEmpty() : (
              <>
                <div className="stats-grid">
                  <div className="stat-card">
                    <p className="stat-label">TIEMPO PROMEDIO:</p>
                    <p className="stat-value">
                      {reportes.tiempos.tiempoPromedioGeneral != null
                        ? `${reportes.tiempos.tiempoPromedioGeneral.toFixed(1)} min`
                        : 'N/A'}
                    </p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">TIEMPO MÁXIMO:</p>
                    <p className="stat-value">
                      {reportes.tiempos.tiempoMaximoRegistrado != null
                        ? `${reportes.tiempos.tiempoMaximoRegistrado} min`
                        : 'N/A'}
                    </p>
                  </div>
                  <div className="stat-card">
                    <p className="stat-label">TIEMPO MÍNIMO:</p>
                    <p className="stat-value">
                      {reportes.tiempos.tiempoMinimoRegistrado != null
                        ? `${reportes.tiempos.tiempoMinimoRegistrado} min`
                        : 'N/A'}
                    </p>
                  </div>
                </div>
                {reportes.tiempos.cuellosDeBotella?.length > 0 && (
                  <>
                    <h4>Cuellos de botella:</h4>
                    <ul>
                      {reportes.tiempos.cuellosDeBotella.map((c, i) => (
                        <li key={i}>{c.nombre} — {c.tiempoPromedio} min promedio. {c.recomendacion}</li>
                      ))}
                    </ul>
                  </>
                )}
                {reportes.tiempos.tiemposPromedioPorAtraccion && (
                  <>
                    <h4>Tiempos por atracción:</h4>
                    <div className="ticket-list">
                      {Object.entries(reportes.tiempos.tiemposPromedioPorAtraccion).map(([nombre, tiempo]) => (
                        <div key={nombre} className="ticket-item">
                          <span>{nombre}: {tiempo?.toFixed(1)} min</span>
                        </div>
                      ))}
                    </div>
                  </>
                )}
                <button className="btn-descargar" onClick={() => downloadReporte('tiempos')}>⬇️ Descargar</button>
              </>
            )}
          </div>
        )}

      </div>
    </div>
  );
}