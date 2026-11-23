import React from 'react';
import { Wrench, ArrowLeft } from 'lucide-react';

interface OtherToolsPageProps {
  onBackToChat: () => void;
}

export const OtherToolsPage: React.FC<OtherToolsPageProps> = ({ onBackToChat }) => {
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
            <h1 className="text-lg font-semibold text-slate-900">Academic &amp; Research Tools</h1>
            <p className="text-xs text-slate-500">
              Department Utility Integrations
            </p>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="max-w-md text-center">
          <div className="w-14 h-14 rounded-2xl bg-blue-50 border border-blue-200 flex items-center justify-center mx-auto mb-4 text-brand-700 shadow-xs">
            <Wrench className="w-7 h-7" />
          </div>
          <h2 className="text-base font-semibold text-slate-900 mb-2">
            Tool Directory Standing By
          </h2>
          <p className="text-xs text-slate-500 leading-relaxed mb-6">
            This module provides a modular mount point for future departmental utilities, such as AICTE compliance checkers, bloom taxonomy analyzers, and syllabus comparison diff engines. Specific tools will be introduced in subsequent project milestones.
          </p>
          <button
            onClick={onBackToChat}
            className="px-4 py-2 text-xs font-medium rounded-lg text-white bg-brand-700 hover:bg-brand-800 transition-colors shadow-xs"
          >
            Return to Conversation
          </button>
        </div>
      </div>
    </div>
  );
};

