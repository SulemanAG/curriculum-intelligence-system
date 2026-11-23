import React, { useState } from 'react';
import {
  CheckCircle2,
  Clock,
  ChevronDown,
  ChevronUp,
  Cpu,
  Eye
} from 'lucide-react';
import { ResearchRun } from '../../types';

interface ConversationalResearchStageProps {
  run: ResearchRun;
  onLoadSchemaPreview?: () => void;
  onViewAlternatives?: () => void;
  hasAlternatives?: boolean;
}

export const ConversationalResearchStage: React.FC<ConversationalResearchStageProps> = ({
  run,
  onLoadSchemaPreview,
  onViewAlternatives,
  hasAlternatives = false
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <div className="w-full rounded-xl border border-slate-200 bg-white shadow-2xs overflow-hidden text-xs my-2">
      {/* Summary Header */}
      <div className="p-3.5 bg-slate-50/70 border-b border-slate-200 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-blue-50 border border-blue-200 flex items-center justify-center text-brand-700 flex-shrink-0">
            {run.status === 'running' ? (
              <Cpu className="w-4 h-4 animate-spin text-brand-600" />
            ) : run.status === 'completed' || run.status === 'awaiting_human_input' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            ) : (
              <Clock className="w-4 h-4 text-slate-500" />
            )}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-slate-900">
                {run.status === 'running'
                  ? 'Coordinator Orchestrating Specialist Agents...'
                  : 'Multi-Agent Research Complete'}
              </span>
              <span className="font-mono text-[10px] text-slate-400">
                {run.id}
              </span>
            </div>
            <p className="text-[11px] text-slate-500 mt-0.5">
              3 parallel view-builders &bull; Gap analysis &bull; Validation gate
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {run.elapsedSeconds !== undefined && (
            <span className="text-[11px] text-slate-500 font-mono bg-white px-2 py-0.5 rounded border border-slate-200">
              {run.elapsedSeconds}s
            </span>
          )}
          <button
            type="button"
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-1 rounded text-slate-400 hover:text-slate-700 hover:bg-white transition-colors"
            aria-label={isExpanded ? 'Collapse research stages' : 'Expand research stages'}
          >
            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Expandable Stages View */}
      {isExpanded && (
        <div className="p-3.5 bg-white space-y-2.5 border-b border-slate-100">
          {run.stages.map((stage) => (
            <div
              key={stage.id}
              className={`p-2.5 rounded-lg border text-xs transition-colors ${
                stage.status === 'completed'
                  ? 'bg-slate-50/50 border-slate-200'
                  : stage.status === 'running'
                  ? 'bg-blue-50/40 border-blue-200'
                  : 'bg-white border-slate-200 opacity-70'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-semibold text-slate-800">{stage.name}</span>
                <span
                  className={`text-[10px] font-medium uppercase px-1.5 py-0.2 rounded border ${
                    stage.status === 'completed'
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      : stage.status === 'running'
                      ? 'bg-blue-50 text-brand-700 border-blue-200 animate-pulse'
                      : 'bg-slate-100 text-slate-500 border-slate-200'
                  }`}
                >
                  {stage.status}
                </span>
              </div>
              {stage.message && (
                <p className="text-[11px] text-slate-500 mt-1 font-mono">{stage.message}</p>
              )}

              {/* Parallel Workers Badge if Parallel Swarm */}
              {stage.isParallel && stage.parallelAgents && (
                <div className="mt-2 pt-2 border-t border-slate-100 grid grid-cols-1 sm:grid-cols-3 gap-1.5">
                  {stage.parallelAgents.map((ag) => (
                    <div
                      key={ag.id}
                      className="p-1.5 rounded bg-white border border-slate-200 text-[10px]"
                    >
                      <div className="flex items-center justify-between font-medium text-slate-800">
                        <span className="truncate">{ag.name}</span>
                        <span
                          className={`w-1.5 h-1.5 rounded-full ${
                            ag.status === 'completed'
                              ? 'bg-emerald-500'
                              : ag.status === 'running'
                              ? 'bg-blue-500 animate-ping'
                              : 'bg-slate-300'
                          }`}
                        />
                      </div>
                      <p className="text-slate-400 truncate mt-0.5">{ag.guardrail}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Action Footer */}
      <div className="p-3 bg-slate-50 flex items-center justify-between gap-2">
        <div className="text-[11px] text-slate-500">
          {hasAlternatives
            ? '4 distinct proposals ready for comparison'
            : 'Coordinator standing by'}
        </div>

        <div className="flex items-center gap-2">
          {onLoadSchemaPreview && (
            <button
              type="button"
              onClick={onLoadSchemaPreview}
              className="inline-flex items-center gap-1 px-3 py-1 text-xs font-medium rounded-md bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 shadow-2xs transition-colors"
            >
              <Eye className="w-3.5 h-3.5 text-slate-500" />
              <span>Inspect Schema Preview</span>
            </button>
          )}

          {hasAlternatives && onViewAlternatives && (
            <button
              type="button"
              onClick={onViewAlternatives}
              className="inline-flex items-center gap-1 px-3.5 py-1 text-xs font-semibold rounded-md bg-brand-700 hover:bg-brand-800 text-white shadow-2xs transition-colors"
            >
              <span>Compare 4 Proposals &rarr;</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
