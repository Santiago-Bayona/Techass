import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../Styles/Register.css';
import PasswordInput from './PasswordInput';

const API_URL = 'http://localhost:8080/api';

export default function Register() {
  const [formData, setFormData] = useState({
    nombre: '',
    documento: '',
    email: '',
    password: '',
    edad: '',
    estatura: '',
    tipoTicket: 'GENERAL',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validaciones
    if (!formData.nombre || !formData.documento || !formData.email || !formData.password || !formData.edad || !formData.estatura) {
      setError('Por favor completa todos los campos');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setError('Email inválido');
      return;
    }

    if (formData.password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    if (isNaN(formData.edad) || formData.edad < 1 || formData.edad > 150) {
      setError('La edad debe ser un número válido entre 1 y 150');
      return;
    }

    // ⭐ VALIDAR ESTATURA EN METROS (entre 0.5 y 2.5)
    if (isNaN(formData.estatura) || formData.estatura < 0.5 || formData.estatura > 2.5) {
      setError('La estatura debe estar entre 0.5m y 2.5m');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/auth/registro`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nombre: formData.nombre,
          documento: formData.documento,
          email: formData.email,
          password: formData.password,
          edad: parseInt(formData.edad),
          estatura: parseFloat(formData.estatura),
          tipoTicket: formData.tipoTicket,
        }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setSuccess('✅ ¡Registro exitoso! Redirigiendo al login...');
        console.log('✅ Visitante registrado:', data.data);

        // Redirigir al login después de 2 segundos
        setTimeout(() => {
          navigate('/');
        }, 2000);
      } else {
        setError(data.message || 'Error al registrarse');
        console.error('❌ Error en registro:', data);
      }
    } catch (err) {
      setError('Error al conectar con el servidor');
      console.error('❌ Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <h1>TechPark</h1>
        <p className="subtitle">Crea tu cuenta</p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit}>
          {/* Nombre */}
          <div className="form-group">
            <label htmlFor="nombre">Nombre Completo</label>
            <input
              type="text"
              id="nombre"
              name="nombre"
              placeholder="Juan Pérez"
              value={formData.nombre}
              onChange={handleChange}
              disabled={loading}
            />
          </div>

          {/* Documento */}
          <div className="form-group">
            <label htmlFor="documento">Documento de Identidad</label>
            <input
              type="text"
              id="documento"
              name="documento"
              placeholder="12345678"
              value={formData.documento}
              onChange={handleChange}
              disabled={loading}
            />
          </div>

          {/* Email */}
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              placeholder="tu@email.com"
              value={formData.email}
              onChange={handleChange}
              disabled={loading}
            />
          </div>

          {/* Contraseña */}
          <div className="form-group">
            <label htmlFor="password">Contraseña</label>

            <PasswordInput
              id="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
              required
            />
          </div>

          {/* Edad */}
          <div className="form-group">
            <label htmlFor="edad">Edad</label>
            <input
              type="number"
              id="edad"
              name="edad"
              placeholder="25"
              value={formData.edad}
              onChange={handleChange}
              disabled={loading}
              min="1"
              max="150"
            />
          </div>

          {/* Estatura */}
          <div className="form-group">
            <label htmlFor="estatura">Estatura (metros)</label>
            <input
              type="number"
              id="estatura"
              name="estatura"
              placeholder="1.75"
              value={formData.estatura}
              onChange={handleChange}
              disabled={loading}
              step="0.01"
              min="0.5"
              max="2.5"
            />
          </div>

          {/* Tipo de Ticket */}
          <div className="form-group">
            <label htmlFor="tipoTicket">Tipo de Ticket</label>
            <select
              id="tipoTicket"
              name="tipoTicket"
              value={formData.tipoTicket}
              onChange={handleChange}
              disabled={loading}
              className="ticket-select"
            >
              <option value="GENERAL">General</option>
              <option value="FAMILIAR">Familiar</option>
              <option value="FAST_PASS">FastPass</option>
            </select>
          </div>

          {/* Botón Registrar */}
          <button type="submit" className="register-btn" disabled={loading}>
            {loading ? 'Registrando...' : 'Registrarse'}
          </button>
        </form>

        {/* Link al login */}
        <div className="links-container">
          <p>¿Ya tienes cuenta? <a href="/">Inicia sesión</a></p>
        </div>
      </div>
    </div>
  );
}