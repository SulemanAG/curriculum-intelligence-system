import React, { useState } from 'react';
import {
  Layers,
  ArrowRight,
  BookOpen
} from 'lucide-react';
import { CoursePlanAlternative } from '../types';
import { AlternativePlanCard } from './AlternativePlanCard';
import { CoursePlanDetails } from './CoursePlanDetails';

interface AlternativeComparisonProps {
  alternatives: CoursePlanAlternative[];
  selectedAlternativeId: string | null;
  onSelectAlternative: (id: string) => void;
  onProceedToFeedback: () => void;
  isLoading?: boolean;
}

export const AlternativeComparison: React.FC<AlternativeComparisonProps> = ({
  alternatives,
  selectedAlternativeId,
  onSelectAlternative,
  onProceedToFeedback,
  isLoading
}) => {
  const [inspectingPlan, setInspectingPlan] = useState<CoursePlanAlternative | null>(null);

  if (isLoading) {
    return (
      <div className="w-full py-8 text-center">
        <div className="bg-white rounded-xl border border-slate-200 p-8 max-w-lg mx-auto shadow-xs">
          <div className="w-10 h-10 border-2 border-brand-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <h3 className="text-sm font-semibold text-slate-800">
            Synthesizing 4 Course-Plan Alternatives...
          </h3>
          <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
            The Recommendation Agent is generating four evidence-grounded proposals with distinct pedagogical approaches.
          </p>
        </div>
      </div>
    );
  }

  if (!alternatives || alternatives.length === 0) {
    return (
      <div className="w-full py-8 text-center">
        <div className="bg-white rounded-xl border border-dashed border-slate-300 p-8 max-w-lg mx-auto">
          <Layers className="w-8 h-8 text-slate-400 mx-auto mb-3" />
          <h3 className="text-sm font-semibold text-slate-700">No Course Proposals Ready</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
            Course alternatives will appear here once the Coordinator completes the parallel research and validation phases.
          </p>
        </div>
      </div>
    );
  }

  const selectedAlternative = alternatives.find((alt) => alt.id === selectedAlternativeId);

  return (
    <div className="w-full py-2">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5">
        <div>
          <div className="flex items-center gap-2 mb-1.5 flex-wrap">
            <span className="text-xs font-semibold uppercase tracking-wider text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200/60">
              Recommendation Stage
            </span>
            <span className="text-xs text-slate-500 font-normal">
              Four Distinct Curriculum Approaches
            </span>
          </div>
          <h2 className="text-lg sm:text-xl font-bold text-slate-900 tracking-tight">
            Compare Course-Plan Proposals
          </h2>
          <p className="text-xs sm:text-sm text-slate-600 mt-0.5">
            Examine each evidence-backed alternative below, inspect full modules and source citations, then select your preferred version.
          </p>
        </div>

        {/* Selected Plan Action Floating Pill */}
        {selectedAlternative && (
          <div className="flex items-center gap-3 bg-white p-2 sm:p-2.5 rounded-lg border border-brand-200 shadow-xs flex-shrink-0">
            <div className="text-left">
              <span className="text-[10px] uppercase font-semibold text-brand-600 block">
                Current Choice
              </span>
              <span className="text-xs font-medium text-slate-900 truncate max-w-[200px] block" title={selectedAlternative.title}>
                {selectedAlternative.code}: {selectedAlternative.title.replace(/\[Schema Preview\]/gi, '').trim()}
              </span>
            </div>
            <button
              type="button"
              onClick={onProceedToFeedback}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-white bg-brand-800 hover:bg-brand-900 rounded-md shadow-xs transition-colors"
            >
              <span>Record Preference</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        )}
      </div>

      {/* Grid of Exactly Four Alternatives in one row on desktop */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 xl:gap-5 w-full items-stretch">
        {alternatives.map((alt) => (
          <AlternativePlanCard
            key={alt.id}
            alternative={alt}
            isSelected={alt.id === selectedAlternativeId}
            onSelect={onSelectAlternative}
            onViewDetails={(plan) => setInspectingPlan(plan)}
          />
        ))}
      </div>

      {/* Bottom Guidance Notice */}
      <div className="mt-5 p-3.5 rounded-lg bg-slate-100/70 border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-slate-600">
        <div className="flex items-center gap-2">
          <BookOpen className="w-4 h-4 text-slate-500 flex-shrink-0" />
          <span>
            Every proposal has been validated against Bloom&apos;s Taxonomy, CO-PO alignment rules, and verified academic references.
          </span>
        </div>
        {selectedAlternativeId && (
          <button
            type="button"
            onClick={onProceedToFeedback}
            className="inline-flex items-center gap-1 font-semibold text-brand-700 hover:text-brand-900 transition-colors flex-shrink-0"
          >
            <span>Proceed with selected proposal</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        )}
      </div>

      {/* Modal / Detailed View for Selected Plan */}
      {inspectingPlan && (
        <CoursePlanDetails
          alternative={inspectingPlan}
          isSelected={inspectingPlan.id === selectedAlternativeId}
          onSelect={(id) => {
            onSelectAlternative(id);
            setInspectingPlan(null);
          }}
          onClose={() => setInspectingPlan(null)}
        />
      )}
    </div>
  );
};

