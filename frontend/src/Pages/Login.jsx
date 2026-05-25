import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../Styles/Login.css';
import PasswordInput from './PasswordInput';

const API_URL = 'http://localhost:8080/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Por favor completa todos los campos');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Email inválido');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password,
        }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        // Guardar todos los datos necesarios ✅
        const datosUsuario = data.data;

        localStorage.setItem('token', datosUsuario.token);
        localStorage.setItem('usuarioId', datosUsuario.id);
        localStorage.setItem('rol', datosUsuario.rol);

        // ← CAMBIO: Guardar como 'visitante' con TODOS los campos
        localStorage.setItem('visitante', JSON.stringify({
          id: datosUsuario.id,
          nombre: datosUsuario.nombre,
          email: datosUsuario.email,
          documento: datosUsuario.documento,
          edad: datosUsuario.edad,
          estatura: datosUsuario.estatura,
          rol: datosUsuario.rol,
          activo: datosUsuario.activo,
          saldoVirtual: datosUsuario.saldoVirtual,
          ticketActivo: datosUsuario.ticketActivo,  // ← ESTO
          ubicacionActual: datosUsuario.ubicacionActual,
        }));

        console.log('✅ Login exitoso:', datosUsuario);
        console.log('🎫 Ticket guardado:', datosUsuario.ticketActivo);

        // Redirigir según el rol
        if (datosUsuario.rol === 'VISITANTE') {
          navigate('/visitante');
        } else if (datosUsuario.rol === 'OPERADOR') {
          navigate('/operador');
        } else if (datosUsuario.rol === 'ADMINISTRADOR') {
          navigate('/admin');
        }
      } else {
        setError(data.message || 'Credenciales inválidas');
        console.error('❌ Error en login:', data);
      }
    } catch (err) {
      setError('Error al conectar con el servidor. Asegúrate que está corriendo en http://localhost:8080');
      console.error('❌ Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>TechPark</h1>
        <p className="subtitle">Estructura de Datos</p>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              placeholder="tu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Contraseña</label>
            <PasswordInput
              id="password"
              name="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>

        <div className="links-container">
          <a href="/register" className="link">
            ¿No tienes cuenta? Registrate
          </a>
          <a href="/forgot-password" className="link">
            ¿Olvidaste tu contraseña?
          </a>
        </div>
      </div>
    </div>
  );
}