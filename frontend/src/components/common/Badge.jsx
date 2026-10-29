import React from 'react';

const Badge = ({ variant = 'default', children, className = '' }) => {
  const styles = {
    default: 'bg-slate-100 text-slate-700 border border-slate-200',
    simulation: 'bg-amber-100 text-amber-900 border border-amber-300 font-semibold tracking-wide animate-pulse',
    online: 'bg-emerald-100 text-emerald-800 border border-emerald-300',
    offline: 'bg-rose-100 text-rose-800 border border-rose-300',
    low: 'bg-blue-100 text-blue-800 border border-blue-200',
    medium: 'bg-amber-100 text-amber-800 border border-amber-200',
    high: 'bg-orange-100 text-orange-800 border border-orange-200',
    critical: 'bg-rose-100 text-rose-800 border border-rose-200 font-bold',
    success: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    info: 'bg-sky-100 text-sky-800 border border-sky-200',
  };

  const selected = styles[variant] || styles.default;

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${selected} ${className}`}>
      {children}
    </span>
  );
};

export default Badge;
