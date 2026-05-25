import React, { useState, useEffect } from 'react';
import '../../../Styles/gestion/TabAtracciones.css';  // ✅ RUTA CORREGIDA

const API_URL = 'http://localhost:8080/api';

export default function TabAtracciones() {
  const [atracciones, setAtracciones] = useState([]);
  const [zonas, setZonas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filtroZona, setFiltroZona] = useState('');
  const [filtroTipo, setFiltroTipo] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingAtraccion, setEditingAtraccion] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    tipo: 'MECANICA',
    zonaId: '',
    capacidadMaxima: '',
    alturaMinima: '',
    edadMinima: '',
    costoAdicional: '',
    posicionX: '',
    posicionY: ''
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('🔍 Fetching atracciones y zonas...');  // ✅ LOG

      const [atraccionesRes, zonasRes] = await Promise.all([
        fetch(`${API_URL}/atracciones`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${API_URL}/zonas`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      console.log('📊 Atracciones Response status:', atraccionesRes.status);  // ✅ LOG
      console.log('📊 Zonas Response status:', zonasRes.status);  // ✅ LOG

      if (atraccionesRes.ok) {
        const data = await atraccionesRes.json();
        console.log('✅ Atracciones obtenidas:', data);  // ✅ LOG
        setAtracciones(data.data || []);
      } else {
        console.error('❌ Error al obtener atracciones:', atraccionesRes.status);  // ✅ LOG
      }

      if (zonasRes.ok) {
        const data = await zonasRes.json();
        console.log('✅ Zonas obtenidas:', data);  // ✅ LOG
        setZonas(data.data || []);
      } else {
        console.error('❌ Error al obtener zonas:', zonasRes.status);  // ✅ LOG
      }

      setLoading(false);
    } catch (error) {
      console.error('❌ Error:', error);  // ✅ LOG
      setLoading(false);
    }
  };

  const handleAddAtraccion = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const method = editingAtraccion ? 'PUT' : 'POST';
      const url = editingAtraccion
        ? `${API_URL}/atracciones/${editingAtraccion.id}`
        : `${API_URL}/atracciones`;

      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...formData,
          zonaId: parseInt(formData.zonaId),
          capacidadMaxima: parseInt(formData.capacidadMaxima),
          alturaMinima: parseFloat(formData.alturaMinima),
          edadMinima: parseInt(formData.edadMinima),
          costoAdicional: parseFloat(formData.costoAdicional)
        })
      });

      if (response.ok) {
        alert('✅ Atracción ' + (editingAtraccion ? 'actualizada' : 'creada') + ' exitosamente');
        setShowForm(false);
        setEditingAtraccion(null);
        setFormData({
          nombre: '',
          tipo: 'MECANICA',
          zonaId: '',
          capacidadMaxima: '',
          alturaMinima: '',
          edadMinima: '',
          costoAdicional: '',
          posicionX: '',
          posicionY: ''
        });
        fetchData();
      }
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Error al guardar atracción');
    }
  };

  const handleDeleteAtraccion = async (id) => {
    if (window.confirm('¿Eliminar esta atracción?')) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_URL}/atracciones/${id}`, {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
          alert('✅ Atracción eliminada');
          fetchData();
        }
      } catch (error) {
        console.error('❌ Error:', error);
        alert('Error al eliminar');
      }
    }
  };

  const handleEdit = (atraccion) => {
    setEditingAtraccion(atraccion);
    setFormData({
      nombre: atraccion.nombre,
      tipo: atraccion.tipo,
      zonaId: atraccion.zonaId,
      capacidadMaxima: atraccion.capacidadMaxima,
      alturaMinima: atraccion.alturaMinima,
      edadMinima: atraccion.edadMinima,
      costoAdicional: atraccion.costoAdicional,
      posicionX: atraccion.posicionX,
      posicionY: atraccion.posicionY
    });
    setShowForm(true);
  };

  const filteredAtracciones = atracciones.filter((atraccion) => {
    return (
      (filtroZona === '' || atraccion.zonaId === parseInt(filtroZona)) &&
      (filtroTipo === '' || atraccion.tipo === filtroTipo) &&
      (filtroEstado === '' || atraccion.estado === filtroEstado)
    );
  });

  if (loading) return <div className="tab-loading">⏳ Cargando atracciones...</div>;

  return (
    <div className="tab-atracciones">
      <div className="filtros">
        <select value={filtroZona} onChange={(e) => setFiltroZona(e.target.value)}>
          <option value="">Por zona</option>
          {zonas.map((zona) => (
            <option key={zona.id} value={zona.id}>
              {zona.nombre}
            </option>
          ))}
        </select>

        <select value={filtroTipo} onChange={(e) => setFiltroTipo(e.target.value)}>
          <option value="">Por tipo</option>
          <option value="MECANICA">MECANICA</option>
          <option value="ACUATICA">ACUATICA</option>
          <option value="INFANTIL">INFANTIL</option>
          <option value="SHOW">SHOW</option>
        </select>

        <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
          <option value="">Por estado</option>
          <option value="ACTIVA">ACTIVA</option>
          <option value="MANTENIMIENTO">MANTENIMIENTO</option>
          <option value="CERRADA">CERRADA</option>
        </select>

        <button className="new-btn" onClick={() => {
          setEditingAtraccion(null);
          setShowForm(!showForm);
        }}>
          [+ Nueva atracción]
        </button>
      </div>

      {/* ✅ VALIDACION DE DATOS VACIOS */}
      {filteredAtracciones.length === 0 ? (
        <div className="tab-loading">📭 No hay atracciones disponibles</div>
      ) : (
        <table className="atracciones-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Zona</th>
              <th>Tipo</th>
              <th>Estado</th>
              <th>Visitantes</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredAtracciones.map((atraccion) => (
              <tr key={atraccion.id}>
                <td>{atraccion.nombre}</td>
                <td>{zonas.find((z) => z.id === atraccion.zonaId)?.nombre}</td>
                <td>{atraccion.tipo}</td>
                <td>{atraccion.estado}</td>
                <td>{atraccion.visitantesActuales || 0}/{atraccion.capacidadMaxima}</td>
                <td className="actions">
                  <button className="edit-btn" onClick={() => handleEdit(atraccion)}>
                    [─]
                  </button>
                  <button
                    className="delete-btn"
                    onClick={() => handleDeleteAtraccion(atraccion.id)}
                  >
                    [!]
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showForm && (
        <form className="atraccion-form" onSubmit={handleAddAtraccion}>
          <h4>{editingAtraccion ? 'Editar atracción' : 'Nueva atracción'}</h4>

          <input
            type="text"
            placeholder="Nombre"
            value={formData.nombre}
            onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
            required
          />

          <select
            value={formData.tipo}
            onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
          >
            <option value="MECANICA">MECANICA</option>
            <option value="ACUATICA">ACUATICA</option>
            <option value="INFANTIL">INFANTIL</option>
            <option value="SHOW">SHOW</option>
          </select>

          <select
            value={formData.zonaId}
            onChange={(e) => setFormData({ ...formData, zonaId: e.target.value })}
            required
          >
            <option value="">Seleccionar zona</option>
            {zonas.map((zona) => (
              <option key={zona.id} value={zona.id}>
                {zona.nombre}
              </option>
            ))}
          </select>

          <input
            type="number"
            placeholder="Capacidad máxima"
            value={formData.capacidadMaxima}
            onChange={(e) => setFormData({ ...formData, capacidadMaxima: e.target.value })}
            required
          />

          <input
            type="number"
            step="0.01"
            placeholder="Altura mínima"
            value={formData.alturaMinima}
            onChange={(e) => setFormData({ ...formData, alturaMinima: e.target.value })}
          />

          <input
            type="number"
            placeholder="Edad mínima"
            value={formData.edadMinima}
            onChange={(e) => setFormData({ ...formData, edadMinima: e.target.value })}
          />

          <input
            type="number"
            step="0.01"
            placeholder="Costo adicional"
            value={formData.costoAdicional}
            onChange={(e) => setFormData({ ...formData, costoAdicional: e.target.value })}
          />

          <input
            type="number"
            placeholder="Posición X"
            value={formData.posicionX}
            onChange={(e) => setFormData({ ...formData, posicionX: e.target.value })}
          />

          <input
            type="number"
            placeholder="Posición Y"
            value={formData.posicionY}
            onChange={(e) => setFormData({ ...formData, posicionY: e.target.value })}
          />

          <div className="form-buttons">
            <button type="submit" className="submit-btn">
              {editingAtraccion ? 'Actualizar' : 'Crear'}
            </button>
            <button
              type="button"
              className="cancel-btn"
              onClick={() => {
                setShowForm(false);
                setEditingAtraccion(null);
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