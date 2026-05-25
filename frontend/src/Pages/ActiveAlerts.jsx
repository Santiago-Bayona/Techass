import React, { useState, useEffect } from 'react';
import '../Styles/ActiveAlerts.css';

export default function ActiveAlerts({ operadorId }) {
  const [alertas, setAlertas] = useState([]);
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [showTechnicalForm, setShowTechnicalForm] = useState(false);
  const [formData, setFormData] = useState({
    comentario: '',
    mantenimientoExitoso: null
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchAlertas();
    
    // Auto-refresh cada 5 segundos
    const interval = setInterval(() => {
      fetchAlertas();
    }, 5000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchAlertas = async () => {
    try {
      const token = localStorage.getItem('token');
      
      console.log('🔍 Obteniendo alertas pendientes...');
      
      const response = await fetch('http://localhost:8080/api/mantenimiento/alertas-pendientes', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Alertas obtenidas:', data.data);
        setAlertas(data.data || []);
      } else {
        console.log('⚠️ Error:', response.status);
        setAlertas([]);
      }
    } catch (error) {
      console.error('❌ Error al obtener alertas:', error);
      setAlertas([]);
    } finally {
      setLoading(false);
    }
  };

  const getAlertIcon = (prioridad) => {
    if (!prioridad) return '⚪';
    switch(prioridad.toUpperCase()) {
      case 'ALTA':
        return '🔴';
      case 'MEDIA':
        return '🟡';
      case 'BAJA':
        return '🟠';
      default:
        return '⚪';
    }
  };

  const handleRegistrarRevision = (alerta) => {
    console.log('📋 Abriendo formulario para alerta:', alerta.atraccionNombre);
    setSelectedAlert(alerta);
    setShowTechnicalForm(true);
    setFormData({ comentario: '', mantenimientoExitoso: null });
  };

  const handleSubmitRevision = async () => {
    if (formData.mantenimientoExitoso === null) {
      alert('⚠️ Debes seleccionar si el mantenimiento fue exitoso');
      return;
    }

    try {
      setSubmitting(true);
      const token = localStorage.getItem('token');
      
      const revisionData = {
        atraccionId: selectedAlert.atraccionId,
        operadorId: operadorId,
        comentario: formData.comentario || 'Revisión completada',
        mantenimientoExitoso: formData.mantenimientoExitoso,
        reactivarAtraccion: formData.mantenimientoExitoso
      };

      console.log('📤 Enviando revisión:', revisionData);

      const response = await fetch('http://localhost:8080/api/mantenimiento/revision', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(revisionData)
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Revisión registrada:', data);
        
        alert('✅ Revisión técnica registrada exitosamente');
        setShowTechnicalForm(false);
        setSelectedAlert(null);
        setFormData({ comentario: '', mantenimientoExitoso: null });
        
        // Actualizar lista de alertas
        fetchAlertas();
      } else {
        const errorData = await response.json();
        console.log('❌ Error:', errorData);
        alert(`❌ Error: ${errorData.message || 'No se pudo registrar la revisión'}`);
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('❌ Error al registrar la revisión: ' + error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="active-alerts-container">
      <section className="alerts-section">
        <h3>ALERTAS ACTIVAS</h3>
        <div className="alerts-divider"></div>

        {loading ? (
          <div className="loading">⏳ Cargando alertas...</div>
        ) : alertas.length === 0 ? (
          <div className="no-alerts">✅ No hay alertas activas en este momento</div>
        ) : (
          <div className="alerts-list">
            {alertas.map((alerta) => (
              <div 
                key={alerta.id} 
                className={`alert-item alerta-${(alerta.prioridad || 'baja').toLowerCase()}`}
              >
                <div className="alert-icon">
                  {getAlertIcon(alerta.prioridad)}
                </div>
                <div className="alert-content">
                  <h4>{alerta.atraccionNombre || 'Atracción desconocida'}</h4>
                  <p>{alerta.visitantesActuales || 0} visitantes acumulados</p>
                  <p>🚫 Bloqueada automáticamente</p>
                  {alerta.prioridad && (
                    <p><strong>Prioridad: {alerta.prioridad.toUpperCase()}</strong></p>
                  )}
                </div>
                <button 
                  className="action-link"
                  onClick={() => handleRegistrarRevision(alerta)}
                >
                  [Registrar revisión técnica]
                </button>
              </div>
            ))}
          </div>
        )}

        <div className="alerts-divider"></div>
      </section>

      {/* Formulario de Revisión Técnica */}
      {showTechnicalForm && selectedAlert && (
        <section className="technical-form-section">
          <h3>FORMULARIO DE REVISIÓN TÉCNICA</h3>
          <p className="form-subtitle">(Completa la revisión para reactivar la atracción)</p>

          <div className="form-container">
            <div className="form-info">
              <div className="info-row">
                <span className="label">Atracción:</span>
                <span className="value">{selectedAlert.atraccionNombre}</span>
              </div>
              <div className="info-row">
                <span className="label">Estado actual:</span>
                <span className="value">⚠️ EN MANTENIMIENTO</span>
              </div>
              <div className="info-row">
                <span className="label">Visitantes acumulados:</span>
                <span className="value">{selectedAlert.visitantesActuales || 0}</span>
              </div>
              {selectedAlert.prioridad && (
                <div className="info-row">
                  <span className="label">Prioridad:</span>
                  <span className="value">{selectedAlert.prioridad.toUpperCase()}</span>
                </div>
              )}
            </div>

            <div className="form-group">
              <label>Comentario de la revisión:</label>
              <textarea
                value={formData.comentario}
                onChange={(e) => setFormData({ ...formData, comentario: e.target.value })}
                placeholder="Describe los detalles de la revisión técnica realizada..."
                className="form-textarea"
              />
            </div>

            <div className="form-group">
              <label>¿Mantenimiento exitoso?</label>
              <div className="checkbox-group">
                <label className="checkbox-label">
                  <input
                    type="radio"
                    name="mantenimiento"
                    value="yes"
                    checked={formData.mantenimientoExitoso === true}
                    onChange={() => setFormData({ ...formData, mantenimientoExitoso: true })}
                    disabled={submitting}
                  />
                  <span className="checkmark">✅</span>
                  <span>Sí - Reactivar atracción</span>
                </label>
                <label className="checkbox-label">
                  <input
                    type="radio"
                    name="mantenimiento"
                    value="no"
                    checked={formData.mantenimientoExitoso === false}
                    onChange={() => setFormData({ ...formData, mantenimientoExitoso: false })}
                    disabled={submitting}
                  />
                  <span className="checkmark">❌</span>
                  <span>No - Mantener bloqueada</span>
                </label>
              </div>
            </div>

            <button 
              className="submit-button"
              onClick={handleSubmitRevision}
              disabled={submitting || formData.mantenimientoExitoso === null}
            >
              {submitting ? '⏳ Registrando...' : '[Registrar y reactivar atracción]'}
            </button>

            <div className="form-note">
              ℹ️ Al confirmar "Sí":
              <ul>
                <li>→ Contador de visitantes se resetea a 0</li>
                <li>→ Atracción pasa a estado ACTIVA</li>
                <li>→ Se reabren las colas</li>
              </ul>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
