import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Pages/Login';
import Register from './Pages/Register';
import ForgotPassword from './Pages/ForgotPassword';
import ResetPassword from './Pages/ResetPassword';
import VisitanteDashboard from './Pages/VisitanteDashboard';
import OperadorDashboard from './Pages/OperadorDashboard';
import AdminDashboard from './Pages/AdminDashboard';

import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/visitante" element={<VisitanteDashboard />} />
        <Route path="/operador" element={<OperadorDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        {/* Agregar más rutas aquí después */}
      </Routes>
    </Router>
  );
}

export default App;

