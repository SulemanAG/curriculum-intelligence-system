import React from 'react';
import { BookOpen, Sparkles, RefreshCw, Layers } from 'lucide-react';

interface ApplicationHeaderProps {
  onReset?: () => void;
  isProcessing?: boolean;
  activeRunId?: string | null;
  backendOnline?: boolean;
}

export const ApplicationHeader: React.FC<ApplicationHeaderProps> = ({
  onReset,
  isProcessing,
  activeRunId,
  backendOnline
}) => {
  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 sm:h-20">
          {/* Logo & Identity */}
          <div className="flex items-center gap-4">
            <button
              onClick={onReset}
              className="flex items-center gap-3 text-left focus:outline-none focus:ring-2 focus:ring-brand-500 rounded-lg p-1 transition-colors"
              aria-label="Curriculum Intelligence Home"
            >
              <div className="w-10 h-10 rounded-lg bg-brand-900 flex items-center justify-center text-white shadow-sm ring-1 ring-brand-800/20">
                <BookOpen className="w-5 h-5 text-brand-300" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-lg tracking-tight text-slate-900">
                    Curriculum Intelligence
                  </span>
                  <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[11px] font-medium bg-slate-100 text-slate-700 border border-slate-200">
                    AURA Coordinator
                  </span>
                </div>
                <p className="text-xs text-slate-500 font-normal">
                  KLE Technological University &bull; School of CSE (AI)
                </p>
              </div>
            </button>
          </div>

          {/* Right Status & Actions */}
          <div className="flex items-center gap-3">
            {/* Backend connectivity indicator */}
            <div
              className={`hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${
                backendOnline
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-slate-100 text-slate-600 border-slate-200'
              }`}
              title={
                backendOnline
                  ? 'Backend Spring Boot Coordinator is connected'
                  : 'Backend Coordinator offline (Phase 1 Frontend Standalone Mode)'
              }
            >
              <span
                className={`w-2 h-2 rounded-full ${
                  backendOnline ? 'bg-emerald-500 animate-pulse' : 'bg-slate-400'
                }`}
              />
              <span>{backendOnline ? 'Backend Connected' : 'Standby Mode'}</span>
            </div>

            {/* Run ID indicator when active */}
            {activeRunId && (
              <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-mono bg-slate-50 text-slate-600 border border-slate-200">
                <Layers className="w-3.5 h-3.5 text-slate-400" />
                <span>Run: {activeRunId.slice(0, 10)}</span>
              </div>
            )}

            {/* Reset / New Course Button */}
            {onReset && (
              <button
                type="button"
                onClick={onReset}
                disabled={isProcessing}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-md shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                {isProcessing ? (
                  <RefreshCw className="w-3.5 h-3.5 animate-spin text-slate-500" />
                ) : (
                  <Sparkles className="w-3.5 h-3.5 text-brand-600" />
                )}
                <span>New Inquiry</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

