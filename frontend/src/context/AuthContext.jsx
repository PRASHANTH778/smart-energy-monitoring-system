import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('smart_energy_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const savedToken = localStorage.getItem('smart_energy_token');
      const savedUser = localStorage.getItem('smart_energy_user');

      if (savedToken && savedUser) {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const login = async (username, password) => {
    const data = await authApi.login({ username, password });
    localStorage.setItem('smart_energy_token', data.token);
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
    };
    localStorage.setItem('smart_energy_user', JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
    return userData;
  };

  const register = async (userData) => {
    return await authApi.register(userData);
  };

  const logout = () => {
    localStorage.removeItem('smart_energy_token');
    localStorage.removeItem('smart_energy_user');
    setToken(null);
    setUser(null);
  };

  const updateProfileState = (updated) => {
    const newUser = { ...user, ...updated };
    setUser(newUser);
    localStorage.setItem('smart_energy_user', JSON.stringify(newUser));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        loading,
        login,
        register,
        logout,
        updateProfileState,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
