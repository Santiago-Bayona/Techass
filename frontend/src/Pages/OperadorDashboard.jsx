// OperadorDashboard.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import MyZone from './MyZone';
import QueueManagement from './QueueManagement';
import ActiveAlerts from './ActiveAlerts';
import '../Styles/OperadorDashboard.css';

export default function OperadorDashboard() {
  const [operadorData, setOperadorData] = useState(null);
  const [activeTab, setActiveTab] = useState('myzone');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOperadorData();
  }, []);

  const fetchOperadorData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');
      
      console.log('🔍 Intentando obtener datos del operador...');
      console.log('📦 usuarioId:', usuarioId);
      console.log('🔑 Token existe:', !!token);
      
      if (!token || !usuarioId) {
        setError('No hay sesión activa. Por favor, inicia sesión.');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/usuarios/perfil/${usuarioId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const apiData = await response.json();
        const userData = apiData.data;
        
        console.log('✅ Datos del operador obtenidos:', userData);
        
        localStorage.setItem('operador', JSON.stringify(userData));
        setOperadorData(userData);
      } else if (response.status === 401) {
        setError('Sesión expirada. Por favor, inicia sesión nuevamente.');
        localStorage.clear();
      } else {
        setError('Error al obtener datos del operador');
      }
    } catch (error) {
      console.error('❌ Error:', error);
      setError('Error al cargar el dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    console.log('🚪 Cerrando sesión...');
    localStorage.clear();
    navigate('/');
  };

  if (loading) {
    return (
      <div className="operador-container">
        <div className="operador-loading">⏳ Cargando dashboard...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="operador-container">
        <div className="operador-loading" style={{color: '#f44336'}}>❌ {error}</div>
      </div>
    );
  }

  if (!operadorData) {
    return (
      <div className="operador-container">
        <div className="operador-loading">⚠️ No se encontraron datos del operador</div>
      </div>
    );
  }

  return (
    <div className="operador-container">
      <header className="operador-header">
        <div className="header-content">
          <div>
            <h1>🎢 Dashboard Operador</h1>
            <p>Bienvenido, <strong>{operadorData?.nombre || 'Operador'}</strong></p>
          </div>
          <button className="logout-btn" onClick={handleLogout}>
            🚪 Cerrar Sesión
          </button>
        </div>
      </header>

      <nav className="operador-tabs">
        <button 
          className={`tab-button ${activeTab === 'myzone' ? 'active' : ''}`}
          onClick={() => setActiveTab('myzone')}
        >
          📍 Mi Zona
        </button>
        <button 
          className={`tab-button ${activeTab === 'queues' ? 'active' : ''}`}
          onClick={() => setActiveTab('queues')}
        >
          📋 Gestión de Colas
        </button>
        <button 
          className={`tab-button ${activeTab === 'alerts' ? 'active' : ''}`}
          onClick={() => setActiveTab('alerts')}
        >
          ⚠️ Alertas Activas
        </button>
      </nav>

      <main className="operador-content">
        {activeTab === 'myzone' && <MyZone operadorData={operadorData} />}
        {activeTab === 'queues' && <QueueManagement operadorId={operadorData?.id} />}
        {activeTab === 'alerts' && <ActiveAlerts operadorId={operadorData?.id} />}
      </main>
    </div>
  );
}
