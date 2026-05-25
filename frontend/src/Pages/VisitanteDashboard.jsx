import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Perfil from './components/Perfil';
import Mapa from './components/Mapa';
import MisColas from './components/MisColas';
import FavoritosHistorial from './components/FavoritosHistorial';
import '../Styles/VisitanteDashboard.css';

const API_URL = 'http://localhost:8080/api';

export default function VisitanteDashboard() {
  const [activeTab, setActiveTab] = useState('perfil');
  const [visitante, setVisitante] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    verificarRolYCargarDatos();
  }, []);

  const verificarRolYCargarDatos = async () => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId'); // ✅ CONSISTENTE
      const rol = localStorage.getItem('rol'); // ✅ YA VIENE DEL LOGIN

      console.log('🔍 Debug:', { token, usuarioId, rol });

      // Verificar que tenga token
      if (!token) {
        console.warn('⚠️ No hay token, redirigiendo a login');
        navigate('/');
        return;
      }

      // Verificar que sea visitante
      if (rol !== 'VISITANTE') {
        console.warn('⚠️ Rol no es VISITANTE:', rol);
        navigate('/');
        return;
      }

      // Obtener datos del visitante
      const response = await fetch(`${API_URL}/usuarios/perfil/${usuarioId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Datos cargados:', data.data);
        setVisitante(data.data);
      } else if (response.status === 401) {
        console.warn('⚠️ Token expirado');
        localStorage.clear();
        navigate('/');
      } else {
        console.error('❌ Error:', response.status);
        setError('Error al cargar datos del visitante');
      }
    } catch (error) {
      console.error('❌ Error:', error);
      setError('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    if (window.confirm('¿Estás seguro de que deseas cerrar sesión?')) {
      localStorage.clear();
      navigate('/');
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p>❌ {error}</p>
        <button onClick={() => {
          localStorage.clear();
          navigate('/');
        }}>
          Volver al login
        </button>
      </div>
    );
  }

  if (!visitante) {
    return (
      <div className="error-container">
        <p>No se pudo cargar la información del visitante</p>
        <button onClick={() => {
          localStorage.clear();
          navigate('/');
        }}>
          Volver al login
        </button>
      </div>
    );
  }

  return (
    <div className="visitante-dashboard">
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab}
        onLogout={handleLogout}
        visitanteName={visitante.nombre}
      />

      <div className="main-content">
        {activeTab === 'perfil' && <Perfil visitante={visitante} onUpdate={verificarRolYCargarDatos} />}
        {activeTab === 'mapa' && <Mapa visitante={visitante} />}
        {activeTab === 'colas' && <MisColas visitante={visitante} />}
        {activeTab === 'favoritos' && <FavoritosHistorial visitante={visitante} />}
      </div>
    </div>
  );
}