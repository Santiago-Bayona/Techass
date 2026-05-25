import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../Styles/ForgotPassword.css';

const API_URL = 'http://localhost:8080/api';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validaciones
    if (!email) {
      setError('Por favor ingresa tu email');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Email inválido');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/auth/forgot-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const data = await response.json();

      if (response.ok) {
        setSuccess('✅ Si el email existe en nuestro sistema, recibirás un enlace para recuperar tu contraseña.');
        console.log('✅ Solicitud de recuperación enviada para:', email);
        
        // Limpiar el formulario después de 2 segundos
        setTimeout(() => {
          setEmail('');
        }, 2000);
      } else {
        setError(data.message || 'Error al procesar la solicitud');
        console.error('❌ Error:', data);
      }
    } catch (err) {
      setError('Error al conectar con el servidor');
      console.error('❌ Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="forgot-password-container">
      <div className="forgot-password-box">
        <h1>TechPark</h1>
        <p className="subtitle">Recuperar Contraseña</p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">
              <span className="icon">📧</span> Correo Electrónico
            </label>
            <input
              type="email"
              id="email"
              placeholder="tu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
          </div>

          <button type="submit" className="send-btn" disabled={loading}>
            {loading ? 'Enviando...' : 'Enviar Enlace de Recuperación'}
          </button>
        </form>

        <div className="links-container">
          <p>
            ¿Recordaste tu contraseña? <a href="/">Inicia sesión</a>
          </p>
          <p>
            ¿No tienes cuenta? <a href="/register">Regístrate</a>
          </p>
        </div>

        <div className="info-box">
          <p>
            📬 Te enviaremos un enlace a tu email para restablecer tu contraseña. 
            El enlace será válido por 1 hora.
          </p>
        </div>
      </div>
    </div>
  );
}