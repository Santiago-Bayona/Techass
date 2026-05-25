import React, { useState, useEffect } from 'react';
import '../../../Styles/TabUsuarios.css';

const API_URL = 'http://localhost:8080/api';

export default function TabUsuarios({ adminId }) {
  const [usuarios, setUsuarios] = useState([]);
  const [filtro, setFiltro] = useState('TODOS');
  const [loading, setLoading] = useState(true);
  const [showPromoForm, setShowPromoForm] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [promoData, setPromoData] = useState({
    titulo: '',
    descripcion: '',
    codigoDescuento: ''
  });

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const fetchUsuarios = async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('🔍 Fetching usuarios desde:', `${API_URL}/usuarios`);

      const response = await fetch(`${API_URL}/usuarios`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      console.log('📊 Response status:', response.status);

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Usuarios obtenidos:', data);
        setUsuarios(data.data || []);
      } else {
        console.error('❌ Error response:', response.status);
      }
      setLoading(false);
    } catch (error) {
      console.error('❌ Error:', error);
      setLoading(false);
    }
  };

  const handleDesactivar = async (usuarioId) => {
    if (window.confirm('¿Desactivar este usuario?')) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(
          `${API_URL}/admin/usuario/desactivar/${usuarioId}/${adminId}`,
          {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
          }
        );

        if (response.ok) {
          alert('✅ Usuario desactivado');
          fetchUsuarios();
        }
      } catch (error) {
        console.error('❌ Error:', error);
        alert('Error al desactivar usuario');
      }
    }
  };

  const handleReactivar = async (usuarioId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(
        `${API_URL}/admin/usuario/reactivar/${usuarioId}`,
        {
          method: 'PUT',
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );

      if (response.ok) {
        alert('✅ Usuario reactivado');
        fetchUsuarios();
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al reactivar usuario');
    }
  };

  /**
 * Eliminar usuario permanentemente
 */
const handleEliminar = async (usuarioId) => {
  if (window.confirm('¿Eliminar permanentemente este usuario?')) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(
        `${API_URL}/usuarios/${usuarioId}`,  // ✅ CORRECTO
        {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );

      if (response.ok) {
        alert('✅ Usuario eliminado');
        fetchUsuarios();
      } else {
        alert('Error al eliminar usuario');
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al eliminar usuario');
    }
  }
};

  const handleEnviarPromo = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/admin/promocion/enviar`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          emailDestino: selectedUser.email,
          titulo: promoData.titulo,
          descripcion: promoData.descripcion,
          codigoDescuento: promoData.codigoDescuento
        })
      });

      if (response.ok) {
        alert('✅ Promoción enviada');
        setShowPromoForm(false);
        setSelectedUser(null);
        setPromoData({ titulo: '', descripcion: '', codigoDescuento: '' });
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al enviar promoción');
    }
  };

  const filteredUsuarios = usuarios.filter((usuario) => {
    if (filtro === 'TODOS') return true;
    return usuario.rol === filtro;
  });

  if (loading) return <div className="tab-loading">⏳ Cargando usuarios...</div>;

  return (
    <div className="tab-usuarios">
      <div className="filtros-usuarios">
        <button
          className={`filtro-btn ${filtro === 'TODOS' ? 'active' : ''}`}
          onClick={() => setFiltro('TODOS')}
        >
          TODOS
        </button>
        <button
          className={`filtro-btn ${filtro === 'VISITANTE' ? 'active' : ''}`}
          onClick={() => setFiltro('VISITANTE')}
        >
          VISITANTES
        </button>
        <button
          className={`filtro-btn ${filtro === 'OPERADOR' ? 'active' : ''}`}
          onClick={() => setFiltro('OPERADOR')}
        >
          OPERADORES
        </button>
      </div>

      <table className="usuarios-table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Email</th>
            <th>Rol</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {filteredUsuarios.map((usuario) => (
            <tr key={usuario.id}>
              <td>{usuario.nombre}</td>
              <td>{usuario.email}</td>
              <td>{usuario.rol}</td>
              <td className={usuario.activo ? 'activo' : 'inactivo'}>
                {usuario.activo ? 'Activo' : 'Inactivo'}
              </td>
              <td className="acciones">
                {usuario.activo ? (
                  <button
                    className="action-btn deactivate"
                    onClick={() => handleDesactivar(usuario.id)}
                    title="Desactivar"
                  >
                    [O]
                  </button>
                ) : (
                  <button
                    className="action-btn reactivate"
                    onClick={() => handleReactivar(usuario.id)}
                    title="Reactivar"
                  >
                    [V]
                  </button>
                )}
                <button
                  className="action-btn delete"
                  onClick={() => handleEliminar(usuario.id)}
                  title="Eliminar"
                >
                  [!]
                </button>
                <button
                  className="action-btn promo"
                  onClick={() => {
                    setSelectedUser(usuario);
                    setShowPromoForm(true);
                  }}
                  title="Enviar promoción"
                >
                  [E]
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {showPromoForm && selectedUser && (
        <form className="promo-form" onSubmit={handleEnviarPromo}>
          <h4>Enviar promoción a {selectedUser.nombre}</h4>
          <p className="form-label">Email destino: {selectedUser.email}</p>

          <input
            type="text"
            placeholder="Título de la promoción"
            value={promoData.titulo}
            onChange={(e) => setPromoData({ ...promoData, titulo: e.target.value })}
            required
          />

          <textarea
            placeholder="Descripción"
            value={promoData.descripcion}
            onChange={(e) => setPromoData({ ...promoData, descripcion: e.target.value })}
            required
            rows="4"
          />

          <input
            type="text"
            placeholder="Código de descuento"
            value={promoData.codigoDescuento}
            onChange={(e) => setPromoData({ ...promoData, codigoDescuento: e.target.value })}
            required
          />

          <div className="form-buttons">
            <button type="submit" className="submit-btn">Enviar</button>
            <button
              type="button"
              className="cancel-btn"
              onClick={() => {
                setShowPromoForm(false);
                setSelectedUser(null);
                setPromoData({ titulo: '', descripcion: '', codigoDescuento: '' });
              }}
            >
              Cancelar
            </button>
          </div>
        </form>
      )}
    </div>
  );
}