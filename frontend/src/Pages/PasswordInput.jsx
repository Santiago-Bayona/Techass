import React, { useState } from 'react';

/**
 * Input de contraseña con botón 👁️ dentro del campo (a la derecha).
 * Props:
 * - value, onChange
 * - placeholder
 * - disabled
 * - required
 * - id, name
 * - className (opcional)
 */
export default function PasswordInput({
  value,
  onChange,
  placeholder = '••••••••',
  disabled = false,
  required = false,
  id,
  name,
  className = '',
}) {
  const [show, setShow] = useState(false);

  return (
    <div className={`password-input-group ${className}`}>
      <input
        type={show ? 'text' : 'password'}
        id={id}
        name={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        disabled={disabled}
        required={required}
        autoComplete="current-password"
      />

      <button
        type="button"
        className="toggle-password"
        onClick={() => setShow((s) => !s)}
        disabled={disabled}
        aria-label={show ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        title={show ? 'Ocultar' : 'Mostrar'}
      >
        {show ? '🙈' : '👁️'}
      </button>
    </div>
  );
}