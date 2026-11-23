import React from 'react';
import { Layers, Clock, AlertCircle, Cpu, Eye, ArrowRight } from 'lucide-react';
import { ResearchRun } from '../types';
import { ResearchStage } from './ResearchStage';

interface ResearchProgressProps {
  run: ResearchRun | null;
  isLoading?: boolean;
  backendOnline?: boolean;
  onLoadSchemaPreview?: () => void;
  onViewAlternatives?: () => void;
  hasAlternatives?: boolean;
}

export const ResearchProgress: React.FC<ResearchProgressProps> = ({
  run,
  isLoading,
  backendOnline = false,
  onLoadSchemaPreview,
  onViewAlternatives,
  hasAlternatives = false
}) => {
  if (isLoading && !run) {
    return (
      <div className="w-full max-w-4xl mx-auto py-12 px-4 sm:px-6">
        <div className="bg-white rounded-xl border border-slate-200 p-8 text-center shadow-xs">
          <div className="w-12 h-12 rounded-full bg-brand-50 border border-brand-200 flex items-center justify-center mx-auto mb-4">
            <Cpu className="w-6 h-6 text-brand-600 animate-spin" />
          </div>
          <h2 className="text-base font-semibold text-slate-900 mb-1">
            Contacting apniLeap AURA Coordinator...
          </h2>
          <p className="text-xs text-slate-500 max-w-md mx-auto">
            Initializing research run context, locking curriculum constraints, and establishing agent channels.
          </p>
        </div>
      </div>
    );
  }

  if (!run) {
    return (
      <div className="w-full max-w-4xl mx-auto py-12 px-4 sm:px-6">
        <div className="bg-white rounded-xl border border-dashed border-slate-300 p-8 text-center">
          <Layers className="w-8 h-8 text-slate-400 mx-auto mb-3" />
          <h3 className="text-sm font-semibold text-slate-700 mb-1">No Active Research Run</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Enter a course topic or syllabus requirement above to initiate Coordinator orchestration.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-4xl mx-auto py-6 px-4 sm:px-6">
      {/* Run Header Bar */}
      <div className="bg-white rounded-t-xl border border-slate-200 border-b-0 p-5 shadow-2xs">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-semibold uppercase tracking-wider text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200/60">
                Orchestration Run
              </span>
              <span className="text-xs font-mono text-slate-500">ID: {run.id}</span>
            </div>
            <h2 className="text-lg font-semibold text-slate-900">
              Researching Course: &ldquo;{run.prompt}&rdquo;
            </h2>
          </div>

          <div className="flex items-center gap-3 text-xs text-slate-500">
            {run.elapsedSeconds !== undefined && (
              <div className="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded border border-slate-200">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                <span>Elapsed: {run.elapsedSeconds}s</span>
              </div>
            )}
            <div className="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded border border-slate-200">
              <span
                className={`w-2 h-2 rounded-full ${
                  run.status === 'completed'
                    ? 'bg-emerald-500'
                    : run.status === 'running'
                    ? 'bg-brand-500 animate-pulse'
                    : run.status === 'awaiting_human_input'
                    ? 'bg-amber-500'
                    : 'bg-slate-400'
                }`}
              />
              <span className="capitalize font-medium text-slate-700">
                {run.status.replace(/_/g, ' ')}
              </span>
            </div>
          </div>
        </div>

        {/* Real Data Policy Notification Banner */}
        {!backendOnline && (
          <div className="mt-4 p-3.5 rounded-lg bg-amber-50 border border-amber-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-start gap-2 text-xs text-amber-900">
              <AlertCircle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <div>
                <strong className="block">Real Data Policy Notice:</strong>
                Live multi-agent research against real job postings and institutional syllabi requires the Phase 2 Java/Spring Boot backend service. To strictly maintain data integrity, no fake agent research results are fabricated.
              </div>
            </div>

            {onLoadSchemaPreview && (
              <button
                type="button"
                onClick={onLoadSchemaPreview}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-amber-800 text-white hover:bg-amber-900 flex-shrink-0 shadow-xs transition-colors"
              >
                <Eye className="w-3.5 h-3.5" />
                <span>Load Schema Preview</span>
              </button>
            )}
          </div>
        )}

        {/* Action button if alternatives are ready */}
        {hasAlternatives && onViewAlternatives && (
          <div className="mt-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-900">
              Four alternative course-plan proposals are ready for review.
            </span>
            <button
              type="button"
              onClick={onViewAlternatives}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold text-white bg-emerald-700 hover:bg-emerald-800 rounded-md transition-colors"
            >
              <span>Compare Proposals</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        )}

        {/* Global Error Banner if failed */}
        {run.error && (
          <div className="mt-4 flex items-center gap-2 p-3 text-xs text-rose-800 bg-rose-50 border border-rose-200 rounded-lg">
            <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
            <span>{run.error}</span>
          </div>
        )}
      </div>

      {/* Sequential Stages & Parallel Execution View */}
      <div className="bg-slate-50/50 rounded-b-xl border border-slate-200 p-5 space-y-3">
        {run.stages.map((stage) => (
          <ResearchStage
            key={stage.id}
            stage={stage}
            isCurrent={stage.id === run.currentStageId}
          />
        ))}
      </div>
    </div>
  );
};
