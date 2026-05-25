import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminSidebar from './components/AdminSidebar';
import MapaAdmin from './components/MapaAdmin';
import ClimaAdmin from './components/ClimaAdmin';
import ReportesAdmin from './components/ReportesAdmin';
import Gestion from './components/gestion/Gestion';
import '../Styles/AdminDashboard.css';

const API_URL = 'http://localhost:8080/api';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('mapa');
  const [adminData, setAdminData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    verificarRolYCargarDatos();
  }, []);

  const verificarRolYCargarDatos = async () => {
    try {
      const token = localStorage.getItem('token');
      const usuarioId = localStorage.getItem('usuarioId');
      const rol = localStorage.getItem('rol');
      const adminGuardado = localStorage.getItem('admin');

      if (!token) {
        navigate('/');
        return;
      }

      if (rol !== 'ADMINISTRADOR') {
        navigate('/');
        return;
      }

      if (adminGuardado) {
        setAdminData(JSON.parse(adminGuardado));
        setLoading(false);
        return;
      }

      const response = await fetch(`${API_URL}/usuarios/perfil/${usuarioId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setAdminData(data.data);
      } else {
        const adminGuardado = localStorage.getItem('admin');
        if (adminGuardado) {
          setAdminData(JSON.parse(adminGuardado));
        } else {
          setError('Error al cargar datos');
        }
      }
    } catch (error) {
      console.error('Error:', error);
      setError('Error al conectar');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    if (window.confirm('¿Cerrar sesión?')) {
      localStorage.clear();
      navigate('/');
    }
  };

  if (loading) return <div className="loading-container"><div className="spinner"></div></div>;
  if (error) return <div className="error-container"><p>❌ {error}</p></div>;
  if (!adminData) return <div className="error-container"><p>Sin datos</p></div>;

  return (
    <div className="admin-dashboard">
      <AdminSidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab}
        onLogout={handleLogout}
        adminName={adminData.nombre}
      />
      <div className="main-content">
        {activeTab === 'mapa' && <MapaAdmin adminData={adminData} />}
        {activeTab === 'clima' && <ClimaAdmin adminData={adminData} />}
        {activeTab === 'reportes' && <ReportesAdmin adminData={adminData} />}
        {activeTab === 'gestion' && <Gestion adminId={adminData.id} />}
      </div>
    </div>
  );
}