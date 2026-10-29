import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Zap, Lock, User, Mail, AlertCircle, ArrowRight, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    role: 'ROLE_OPERATOR',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(formData);
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative overflow-hidden">
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md z-10 text-center">
        <div className="inline-flex p-3 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-400 text-white shadow-xl shadow-emerald-950 mb-4">
          <Zap className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Create Energy Management Account</h2>
        <p className="mt-1 text-xs text-slate-400">
          Smart Building IoT Automated Monitoring Platform
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md z-10 px-4">
        <div className="bg-slate-900/90 backdrop-blur-md py-8 px-6 shadow-2xl rounded-2xl border border-slate-800 sm:px-10">
          {error && (
            <div className="mb-5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-5 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>Account registered! Redirecting to login...</span>
            </div>
          )}

          <form className="space-y-3.5" onSubmit={handleSubmit}>
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Full Name</label>
              <input
                type="text"
                required
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                className="block w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-100 text-xs placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="Dr. Rajesh Sharma"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Username</label>
              <input
                type="text"
                required
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="block w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-100 text-xs placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="rajesh_sharma"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Email Address</label>
              <input
                type="email"
                required
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="block w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-100 text-xs placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="rajesh@institution.edu"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Password</label>
              <input
                type="password"
                required
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="block w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-100 text-xs placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="••••••••"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Assigned Role</label>
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="block w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-100 text-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              >
                <option value="ROLE_OPERATOR">Facility Operator</option>
                <option value="ROLE_MANAGER">Energy Manager</option>
                <option value="ROLE_VIEWER">Academic / Viva Reviewer</option>
                <option value="ROLE_ADMIN">System Administrator</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl text-xs font-semibold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 focus:outline-none shadow-lg shadow-emerald-950/60 transition-all cursor-pointer disabled:opacity-50"
            >
              {loading ? 'Creating Account...' : 'Complete Registration'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="mt-5 text-center text-xs text-slate-400">
            Already registered?{' '}
            <Link to="/login" className="text-emerald-400 hover:underline font-medium">
              Sign in here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
