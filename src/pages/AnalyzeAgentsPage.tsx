import React from 'react';
import { Cpu, ArrowLeft, Clock } from 'lucide-react';

interface AnalyzeAgentsPageProps {
  onBackToChat: () => void;
}

export const AnalyzeAgentsPage: React.FC<AnalyzeAgentsPageProps> = ({ onBackToChat }) => {
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
            <h1 className="text-lg font-semibold text-slate-900">Analyze Agents</h1>
            <p className="text-xs text-slate-500">
              Multi-Agent Orchestration &amp; Performance Inspection
            </p>
          </div>
        </div>
        <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
          Phase 2 Capability
        </span>
      </div>

      {/* Honest Empty / Development Placeholder */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="max-w-md text-center">
          <div className="w-14 h-14 rounded-2xl bg-blue-50 border border-blue-200 flex items-center justify-center mx-auto mb-4 text-brand-700 shadow-xs">
            <Cpu className="w-7 h-7" />
          </div>
          <h2 className="text-base font-semibold text-slate-900 mb-2">
            Agent Analytics in Development
          </h2>
          <p className="text-xs text-slate-600 leading-relaxed mb-5">
            Real-time telemetry for the Coordinator and parallel workers (Curriculum Mapping, Industry Skills, and ACM/APNI Benchmarking) will be activated in Phase 2 alongside the Spring Boot agent runtime.
          </p>

          <div className="p-3.5 rounded-lg bg-slate-50 border border-slate-200 text-left text-xs text-slate-600 space-y-1.5 mb-6">
            <div className="flex items-center gap-2 font-medium text-slate-700">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              <span>Planned Agent Telemetry:</span>
            </div>
            <ul className="list-disc list-inside space-y-0.5 text-slate-500 pl-1">
              <li>Token consumption per specialist worker</li>
              <li>Validation gate retry rate and self-healing diagnostics</li>
              <li>Parallel execution latency and merge decisions</li>
              <li>Audit trail and citation verification logs</li>
            </ul>
          </div>

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

