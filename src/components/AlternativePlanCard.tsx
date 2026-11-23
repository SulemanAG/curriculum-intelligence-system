import React from 'react';
import {
  CheckCircle,
  ExternalLink,
  BookOpen,
  Award,
  ShieldCheck,
  TrendingUp,
  Clock
} from 'lucide-react';
import { CoursePlanAlternative } from '../types';

interface AlternativePlanCardProps {
  alternative: CoursePlanAlternative;
  isSelected: boolean;
  onSelect: (altId: string) => void;
  onViewDetails: (alternative: CoursePlanAlternative) => void;
}

export const AlternativePlanCard: React.FC<AlternativePlanCardProps> = ({
  alternative,
  isSelected,
  onSelect,
  onViewDetails
}) => {
  // Clean display title without redundant dev tags
  const cleanTitle = alternative.title.replace(/\[Schema Preview\]/gi, '').trim();
  const isPreview = alternative.title.includes('[Schema Preview]') || alternative.id.includes('preview');

  return (
    <div
      className={`relative flex flex-col justify-between rounded-xl border-2 p-4 sm:p-4.5 transition-colors duration-150 min-w-0 w-full h-full ${
        isSelected
          ? 'border-brand-600 bg-blue-50/30 shadow-sm'
          : 'border-slate-200 hover:border-slate-300 bg-white shadow-2xs hover:shadow-xs'
      }`}
    >
      {/* Upper Content Section */}
      <div className="flex flex-col min-w-0">
        {/* Top Identification & Selection Row */}
        <div className="flex items-center justify-between gap-1.5 min-w-0">
          <div className="flex items-center gap-1.5 flex-wrap min-w-0">
            <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200 flex-shrink-0">
              {alternative.code}
            </span>
            {isPreview && (
              <span className="text-[10px] bg-amber-50 text-amber-700 border border-amber-200 px-1.5 py-0.5 rounded font-mono font-medium flex-shrink-0">
                DEV PREVIEW
              </span>
            )}
          </div>

          {isSelected ? (
            <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-brand-700 bg-brand-50 px-2 py-0.5 rounded-full border border-brand-200 flex-shrink-0">
              <CheckCircle className="w-3 h-3 text-brand-600 flex-shrink-0" />
              <span>Selected</span>
            </span>
          ) : (
            <span className="text-[11px] text-slate-400 font-normal flex-shrink-0">
              Option {alternative.code.replace('ALT-', '')}
            </span>
          )}
        </div>

        {/* Strategy Category Row (Uniform height baseline) */}
        <div className="mt-2 min-h-[22px] flex items-center">
          {alternative.isRecommendedStrategy ? (
            <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded bg-brand-50 text-brand-700 border border-brand-200">
              <Award className="w-3 h-3 text-brand-600 flex-shrink-0" />
              <span>Balanced Standard</span>
            </span>
          ) : (
            <span className="text-[11px] font-medium text-slate-500 uppercase tracking-wider">
              Alternative Pathway
            </span>
          )}
        </div>

        {/* Card Title (Natural word wrapping with line-clamp-2 and uniform min-height) */}
        <h3
          className="text-sm font-semibold text-slate-900 leading-snug mt-2 break-words min-h-[2.5rem] line-clamp-2"
          title={cleanTitle}
        >
          {cleanTitle}
        </h3>

        {/* Strategy Subtitle */}
        <p className="text-xs font-medium text-brand-700 mt-1 truncate" title={alternative.strategyName}>
          {alternative.strategyName}
        </p>

        {/* Philosophy / Description (Consistent 3-line height across all cards) */}
        <p className="text-xs text-slate-600 mt-2 leading-relaxed line-clamp-3 min-h-[3.6rem]">
          {alternative.curriculumPhilosophy}
        </p>

        {/* Quick Structure Metric */}
        <div className="mt-3.5 pt-3 border-t border-slate-100 grid grid-cols-2 gap-2 text-xs">
          <div className="flex items-center gap-1.5 text-slate-600 min-w-0">
            <BookOpen className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            <span className="truncate">Credits: <strong className="text-slate-900">{alternative.credits}</strong></span>
          </div>
          <div className="flex items-center gap-1.5 text-slate-600 min-w-0">
            <Clock className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            <span className="truncate">Hours: <strong className="text-slate-900">{alternative.totalHours} hrs</strong></span>
          </div>
        </div>

        {/* Key Highlights (Fixed height for vertical alignment) */}
        <div className="mt-3 flex flex-wrap gap-1 min-h-[44px] content-start">
          {alternative.keyHighlights?.slice(0, 2).map((highlight, idx) => (
            <span
              key={idx}
              className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-50 text-slate-600 border border-slate-200/80 truncate max-w-full"
              title={highlight}
            >
              {highlight}
            </span>
          ))}
        </div>

        {/* Evidence & Grounding Indicator */}
        <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-xs">
          <div className="flex items-center gap-1 text-emerald-700 font-medium" title="Evidence grounding score">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 flex-shrink-0" />
            <span>{alternative.evidenceCoveragePercent}% Grounded</span>
          </div>
          <div className="flex items-center gap-1 text-slate-500" title="Source citations">
            <TrendingUp className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            <span>{alternative.evidenceReferences?.length || 0} Sources</span>
          </div>
        </div>
      </div>

      {/* Action Footer (Evenly sized buttons anchored at card bottom) */}
      <div className="mt-4 pt-3 border-t border-slate-100 grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={() => onViewDetails(alternative)}
          className="inline-flex items-center justify-center gap-1 px-2 py-1.5 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-lg transition-colors focus:outline-none focus:ring-1 focus:ring-brand-500 truncate"
          title="Inspect syllabus modules and evidence citations"
        >
          <span>Inspect</span>
          <ExternalLink className="w-3 h-3 text-slate-400 flex-shrink-0" />
        </button>

        <button
          type="button"
          onClick={() => onSelect(alternative.id)}
          className={`inline-flex items-center justify-center gap-1 px-2 py-1.5 text-xs font-medium rounded-lg transition-colors focus:outline-none focus:ring-1 focus:ring-brand-500 truncate ${
            isSelected
              ? 'bg-brand-900 text-white hover:bg-brand-950 font-semibold shadow-xs'
              : 'bg-brand-700 text-white hover:bg-brand-800'
          }`}
        >
          {isSelected ? (
            <>
              <CheckCircle className="w-3 h-3 text-brand-300 flex-shrink-0" />
              <span>Preferred</span>
            </>
          ) : (
            <span>Select Plan</span>
          )}
        </button>
      </div>
    </div>
  );
};

