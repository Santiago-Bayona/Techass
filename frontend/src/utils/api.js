const API_URL = 'http://localhost:8080/api';

export const getToken = () => localStorage.getItem('token');

export const apiCall = async (endpoint, options = {}) => {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Agregar token si existe
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Error en la solicitud');
  }

  return data;
};

// Funciones específicas para tu API
export const login = (email, password) =>
  apiCall('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });

export const obtenerAtracciones = () =>
  apiCall('/atracciones');

export const obtenerZonas = () =>
  apiCall('/zonas');

export const unirseACola = (visitanteId, atraccionId, prioridad = 'NORMAL') =>
  apiCall('/colas/unirse', {
    method: 'POST',
    body: JSON.stringify({ visitanteId, atraccionId, prioridad }),
  });