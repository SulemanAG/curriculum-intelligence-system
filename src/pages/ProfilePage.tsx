import React, { useState } from 'react';
import { ArrowLeft, Server, Building2, Check } from 'lucide-react';
import { apiClient } from '../services/api';

interface ProfilePageProps {
  onBackToChat: () => void;
  backendOnline: boolean;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({ onBackToChat, backendOnline }) => {
  const [facultyName, setFacultyName] = useState('Faculty Member');
  const [department, setDepartment] = useState('School of Computer Science & Engineering (AI)');
  const [institution] = useState('KLE Technological University, Belagavi');
  const [apiEndpoint] = useState(apiClient.getBaseUrl());
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  return (
    <div className="flex-1 flex flex-col bg-white overflow-y-auto">
      {/* Header */}
      <div className="border-b border-slate-200 px-6 py-4 flex items-center justify-between bg-white">
        <div className="flex items-center gap-3">
          <button
            onClick={onBackToChat}
            className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
            title="Return to Chat"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-lg font-semibold text-slate-900">User Profile &amp; Settings</h1>
            <p className="text-xs text-slate-500">
              Department Preferences &amp; Integration Configuration
            </p>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-6 max-w-2xl w-full mx-auto space-y-6">
        {/* Profile Card */}
        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-2xs">
          <div className="flex items-center gap-4 mb-6">
            <div className="w-14 h-14 rounded-full bg-brand-900 text-white flex items-center justify-center text-lg font-bold shadow-xs">
              {facultyName.split(' ').map((n) => n[0]).join('').slice(0, 2) || 'FM'}
            </div>
            <div>
              <h2 className="text-base font-semibold text-slate-900">{facultyName}</h2>
              <p className="text-xs text-slate-500">{department}</p>
              <div className="flex items-center gap-1.5 text-[11px] text-slate-400 mt-1">
                <Building2 className="w-3.5 h-3.5 text-slate-400" />
                <span>{institution}</span>
              </div>
            </div>
          </div>

          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label htmlFor="pref-name" className="block text-xs font-medium text-slate-700 mb-1">
                Faculty / Reviewer Display Name
              </label>
              <input
                id="pref-name"
                type="text"
                value={facultyName}
                onChange={(e) => setFacultyName(e.target.value)}
                className="w-full text-xs rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <div>
              <label htmlFor="pref-dept" className="block text-xs font-medium text-slate-700 mb-1">
                Academic Department
              </label>
              <input
                id="pref-dept"
                type="text"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="w-full text-xs rounded-lg border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>

            <div className="pt-2 flex items-center justify-between">
              <span className="text-[11px] text-slate-400">
                Local frontend preference settings
              </span>
              <button
                type="submit"
                className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-medium rounded-lg text-white bg-brand-700 hover:bg-brand-800 transition-colors shadow-xs"
              >
                {saved ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-300" />
                    <span>Saved Preferences</span>
                  </>
                ) : (
                  <span>Save Changes</span>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Backend Connectivity Card */}
        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-2xs">
          <div className="flex items-center gap-2 mb-3">
            <Server className="w-4 h-4 text-brand-700" />
            <h3 className="text-sm font-semibold text-slate-900">
              Backend Service Environment
            </h3>
          </div>
          <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 text-xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-slate-500">Coordinator API URL:</span>
              <code className="font-mono text-slate-800 bg-white px-2 py-0.5 rounded border border-slate-200 text-[11px]">
                {apiEndpoint}
              </code>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-500">Service Status:</span>
              <span
                className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                  backendOnline
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                    : 'bg-slate-100 text-slate-600 border-slate-200'
                }`}
              >
                <span
                  className={`w-1.5 h-1.5 rounded-full ${
                    backendOnline ? 'bg-emerald-500' : 'bg-slate-400'
                  }`}
                />
                <span>{backendOnline ? 'Connected (Live Mode)' : 'Offline (Phase 1 Standalone)'}</span>
              </span>
            </div>
          </div>
          <p className="text-[11px] text-slate-400 mt-2">
            Configurable via <code className="font-mono text-[10px]">VITE_API_BASE_URL</code> in environment configuration.
          </p>
        </div>
      </div>
    </div>
  );
};
