import React from 'react';
import {
  CheckCircle2,
  Clock,
  AlertTriangle,
  PlayCircle,
  GitBranch,
  ShieldCheck,
  UserCheck,
  Check
} from 'lucide-react';
import { ResearchStage as ResearchStageType, ExecutionStatus } from '../types';

interface ResearchStageProps {
  stage: ResearchStageType;
  isCurrent?: boolean;
}

export const ResearchStage: React.FC<ResearchStageProps> = ({ stage, isCurrent }) => {
  const getStatusBadge = (status: ExecutionStatus) => {
    switch (status) {
      case 'completed':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
            <Check className="w-3 h-3" />
            <span>Completed</span>
          </span>
        );
      case 'running':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-brand-50 text-brand-700 border border-brand-200 animate-pulse">
            <PlayCircle className="w-3 h-3" />
            <span>Active</span>
          </span>
        );
      case 'awaiting_human_input':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
            <UserCheck className="w-3 h-3" />
            <span>Awaiting Review</span>
          </span>
        );
      case 'failed':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-rose-50 text-rose-700 border border-rose-200">
            <AlertTriangle className="w-3 h-3" />
            <span>Failed</span>
          </span>
        );
      case 'pending':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-500 border border-slate-200">
            <Clock className="w-3 h-3" />
            <span>Pending</span>
          </span>
        );
    }
  };

  const getStageIcon = () => {
    if (stage.status === 'completed') {
      return <CheckCircle2 className="w-5 h-5 text-emerald-600" />;
    }
    if (stage.status === 'running') {
      return <div className="w-5 h-5 rounded-full border-2 border-brand-600 border-t-transparent animate-spin" />;
    }
    if (stage.status === 'failed') {
      return <AlertTriangle className="w-5 h-5 text-rose-600" />;
    }
    if (stage.status === 'awaiting_human_input') {
      return <UserCheck className="w-5 h-5 text-amber-600" />;
    }
    return <div className="w-4 h-4 rounded-full border-2 border-slate-300" />;
  };

  return (
    <div
      className={`rounded-lg border p-4 transition-all ${
        isCurrent
          ? 'bg-white border-brand-300 ring-1 ring-brand-200 shadow-xs'
          : stage.status === 'completed'
          ? 'bg-slate-50/70 border-slate-200'
          : 'bg-white border-slate-200 opacity-90'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="mt-0.5 flex-shrink-0">{getStageIcon()}</div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold text-slate-900">{stage.name}</h3>
              {stage.isParallel && (
                <span className="inline-flex items-center gap-1 px-1.5 py-0.2 rounded text-[10px] font-medium bg-blue-50 text-blue-700 border border-blue-200">
                  <GitBranch className="w-3 h-3" />
                  <span>Parallel Workers</span>
                </span>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-0.5">{stage.description}</p>
          </div>
        </div>
        <div className="flex-shrink-0">{getStatusBadge(stage.status)}</div>
      </div>

      {/* Progress Message */}
      {stage.message && (
        <div className="mt-2.5 text-xs text-slate-600 bg-slate-100/70 px-3 py-1.5 rounded border border-slate-200/60 font-mono">
          {stage.message}
        </div>
      )}

      {/* Parallel Workers Container (If this stage is Parallel View-Building) */}
      {stage.isParallel && stage.parallelAgents && stage.parallelAgents.length > 0 && (
        <div className="mt-4 pt-3 border-t border-slate-100">
          <p className="text-[11px] font-semibold tracking-wider uppercase text-slate-400 mb-2.5">
            Concurrent View-Building Agents (Lab 7/8 Orchestration)
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-2.5">
            {stage.parallelAgents.map((agent) => (
              <div
                key={agent.id}
                className="p-2.5 rounded-md border border-slate-200 bg-white shadow-2xs flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-medium text-slate-900">{agent.name}</span>
                    <span
                      className={`w-2 h-2 rounded-full ${
                        agent.status === 'completed'
                          ? 'bg-emerald-500'
                          : agent.status === 'running'
                          ? 'bg-brand-500 animate-ping'
                          : 'bg-slate-300'
                      }`}
                    />
                  </div>
                  <p className="text-[11px] text-slate-500 line-clamp-2">{agent.role}</p>
                </div>
                <div className="mt-2 pt-2 border-t border-slate-100 flex items-center justify-between text-[10px] text-slate-400">
                  <span className="truncate max-w-[150px]" title={agent.guardrail}>
                    {agent.guardrail}
                  </span>
                  <span className="font-medium text-slate-600 uppercase">{agent.status}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Acceptance validation badge if stage is validation */}
      {stage.id === 'validation_acceptance' && stage.status === 'completed' && (
        <div className="mt-3 flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50/70 px-2.5 py-1.5 rounded border border-emerald-200">
          <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span>Automated Acceptance Gate passed: 100% of recommendations cite verified evidence.</span>
        </div>
      )}
    </div>
  );
};

