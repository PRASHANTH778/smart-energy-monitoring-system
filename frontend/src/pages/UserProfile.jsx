import React, { useState } from 'react';
import {
  User,
  Shield,
  Lock,
  Mail,
  CheckCircle2,
  AlertCircle,
  Save,
  KeyRound,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/authApi';
import Badge from '../components/common/Badge';

const UserProfile = () => {
  const { user, updateProfileState } = useAuth();

  const [profileForm, setProfileForm] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [profileFeedback, setProfileFeedback] = useState({ type: '', text: '' });
  const [passwordFeedback, setPasswordFeedback] = useState({ type: '', text: '' });

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      const updated = await authApi.updateProfile(profileForm);
      updateProfileState(updated);
      setProfileFeedback({ type: 'success', text: 'Profile updated successfully' });
      setTimeout(() => setProfileFeedback({ type: '', text: '' }), 3000);
    } catch (err) {
      setProfileFeedback({ type: 'error', text: err.response?.data?.message || err.message });
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordFeedback({ type: 'error', text: 'New passwords do not match' });
      return;
    }

    try {
      await authApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordFeedback({ type: 'success', text: 'Password changed successfully' });
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => setPasswordFeedback({ type: '', text: '' }), 3000);
    } catch (err) {
      setPasswordFeedback({ type: 'error', text: err.response?.data?.message || err.message });
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">User Account & Authorization</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage your credentials, role authorizations, and personal preferences.
          </p>
        </div>
      </div>

      {/* User Summary Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs flex items-center gap-4">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-400 text-white font-bold text-lg flex items-center justify-center shadow-md shadow-emerald-950/20">
          {user?.username ? user.username.substring(0, 2).toUpperCase() : 'US'}
        </div>
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h2 className="text-base font-bold text-slate-900">{user?.fullName || user?.username}</h2>
            <Badge variant="online">{user?.role?.replace('ROLE_', '')}</Badge>
          </div>
          <div className="text-xs text-slate-500 font-mono">Username: {user?.username}</div>
          <div className="text-xs text-slate-400 font-mono">Email: {user?.email}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Update Profile Card */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <User className="w-4 h-4 text-emerald-600" />
            <h3 className="font-bold text-slate-900 text-sm">Personal Information</h3>
          </div>

          {profileFeedback.text && (
            <div
              className={`mb-4 p-2.5 rounded-lg text-xs flex items-center gap-2 ${
                profileFeedback.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'
              }`}
            >
              {profileFeedback.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
              <span>{profileFeedback.text}</span>
            </div>
          )}

          <form onSubmit={handleUpdateProfile} className="space-y-3.5">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Full Name</label>
              <input
                type="text"
                required
                value={profileForm.fullName}
                onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Email Address</label>
              <input
                type="email"
                required
                value={profileForm.email}
                onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <button
              type="submit"
              className="mt-2 py-2 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <Save className="w-3.5 h-3.5" />
              <span>Update Profile</span>
            </button>
          </form>
        </div>

        {/* Change Password Card */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <Lock className="w-4 h-4 text-emerald-600" />
            <h3 className="font-bold text-slate-900 text-sm">Security & Password</h3>
          </div>

          {passwordFeedback.text && (
            <div
              className={`mb-4 p-2.5 rounded-lg text-xs flex items-center gap-2 ${
                passwordFeedback.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'
              }`}
            >
              {passwordFeedback.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
              <span>{passwordFeedback.text}</span>
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-3.5">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Current Password</label>
              <input
                type="password"
                required
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">New Password</label>
              <input
                type="password"
                required
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Confirm New Password</label>
              <input
                type="password"
                required
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <button
              type="submit"
              className="mt-2 py-2 px-4 rounded-xl text-xs font-semibold text-white bg-slate-900 hover:bg-slate-800 flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <KeyRound className="w-3.5 h-3.5" />
              <span>Change Password</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
