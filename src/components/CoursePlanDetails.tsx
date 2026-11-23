import React from 'react';
import {
  X,
  CheckCircle,
  BookOpen,
  Award,
  Layers,
  GraduationCap,
  Clock,
  Briefcase
} from 'lucide-react';
import { CoursePlanAlternative } from '../types';
import { EvidenceReferenceList } from './EvidenceReferenceList';

interface CoursePlanDetailsProps {
  alternative: CoursePlanAlternative;
  isSelected: boolean;
  onSelect: (id: string) => void;
  onClose: () => void;
}

export const CoursePlanDetails: React.FC<CoursePlanDetailsProps> = ({
  alternative,
  isSelected,
  onSelect,
  onClose
}) => {
  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-3 sm:p-6"
      role="dialog"
      aria-modal="true"
      aria-labelledby="course-details-title"
    >
      <div className="bg-white rounded-xl shadow-2xl border border-slate-200 w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Modal Top Bar */}
        <div className="px-6 py-4 bg-white border-b border-slate-200 flex items-start justify-between gap-4 sticky top-0 z-10">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
                {alternative.code}
              </span>
              <span className="text-xs font-medium text-brand-800">
                {alternative.strategyName}
              </span>
              {isSelected && (
                <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                  <CheckCircle className="w-3 h-3 text-emerald-600" />
                  <span>Currently Selected</span>
                </span>
              )}
            </div>
            <h2 id="course-details-title" className="text-xl font-bold text-slate-900">
              {alternative.title}
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">{alternative.tagline}</p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors focus:outline-none focus:ring-2 focus:ring-brand-500"
            aria-label="Close details"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Scrollable Body */}
        <div className="px-6 py-5 overflow-y-auto space-y-6 text-slate-800 text-sm">
          {/* Credit & Hours Summary Strip */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-3.5 bg-slate-50 rounded-lg border border-slate-200 text-xs">
            <div>
              <span className="text-slate-500 block">Total Credits</span>
              <span className="text-base font-bold text-slate-900">{alternative.credits}</span>
            </div>
            <div>
              <span className="text-slate-500 block">Total Contact Hours</span>
              <span className="text-base font-bold text-slate-900">{alternative.totalHours} hrs</span>
            </div>
            <div>
              <span className="text-slate-500 block">L-T-P Distribution</span>
              <span className="text-base font-bold text-slate-900">
                {alternative.lectureHours}-{alternative.tutorialHours}-{alternative.practicalHours}
              </span>
            </div>
            <div>
              <span className="text-slate-500 block">Evidence Grounding</span>
              <span className="text-base font-bold text-emerald-700">
                {alternative.evidenceCoveragePercent}%
              </span>
            </div>
          </div>

          {/* Curriculum Philosophy */}
          <div>
            <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
              Curriculum Philosophy & Pedagogical Strategy
            </h3>
            <p className="text-xs leading-relaxed text-slate-700 bg-white p-3 rounded border border-slate-200">
              {alternative.curriculumPhilosophy}
            </p>
          </div>

          {/* Course Outcomes (COs) */}
          <div>
            <div className="flex items-center gap-2 mb-2">
              <GraduationCap className="w-4 h-4 text-brand-600" />
              <h3 className="text-sm font-semibold text-slate-900">
                Course Outcomes (COs) & Bloom&apos;s Taxonomy
              </h3>
            </div>
            {alternative.courseOutcomes && alternative.courseOutcomes.length > 0 ? (
              <div className="border border-slate-200 rounded-lg overflow-hidden">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold">
                    <tr>
                      <th className="py-2 px-3 w-16">CO</th>
                      <th className="py-2 px-3">Outcome Statement</th>
                      <th className="py-2 px-3 w-28">Bloom&apos;s Level</th>
                      <th className="py-2 px-3 w-24">PO Mapping</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {alternative.courseOutcomes.map((co) => (
                      <tr key={co.code}>
                        <td className="py-2 px-3 font-mono font-semibold text-brand-800">{co.code}</td>
                        <td className="py-2 px-3 text-slate-700 leading-relaxed">{co.statement}</td>
                        <td className="py-2 px-3">
                          <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 text-[10px] font-medium border border-slate-200">
                            {co.bloomsTaxonomyLevel}
                          </span>
                        </td>
                        <td className="py-2 px-3 font-mono text-[11px] text-slate-600">
                          {co.poMapping.join(', ')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">No course outcomes mapped in this draft.</p>
            )}
          </div>

          {/* Unit / Module Breakdown */}
          <div>
            <div className="flex items-center gap-2 mb-2.5">
              <Layers className="w-4 h-4 text-brand-600" />
              <h3 className="text-sm font-semibold text-slate-900">Unit / Module Breakdown</h3>
            </div>
            {alternative.modules && alternative.modules.length > 0 ? (
              <div className="space-y-3">
                {alternative.modules.map((module) => (
                  <div
                    key={module.moduleNumber}
                    className="p-3.5 rounded-lg border border-slate-200 bg-slate-50/40 text-xs"
                  >
                    <div className="flex items-center justify-between gap-2 mb-1.5">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-900">
                          Unit {module.moduleNumber}: {module.title}
                        </span>
                      </div>
                      <span className="flex items-center gap-1 text-[11px] font-mono text-slate-500 bg-white px-2 py-0.5 rounded border border-slate-200">
                        <Clock className="w-3 h-3 text-slate-400" />
                        <span>{module.suggestedHours} hours</span>
                      </span>
                    </div>

                    {/* Topics */}
                    <div className="mt-2">
                      <span className="text-slate-500 font-medium block mb-1">Topics Covered:</span>
                      <div className="flex flex-wrap gap-1.5">
                        {module.topics.map((t, idx) => (
                          <span
                            key={idx}
                            className="bg-white px-2 py-0.5 rounded border border-slate-200 text-slate-700 text-[11px]"
                          >
                            {t}
                          </span>
                        ))}
                      </div>
                    </div>

                    {/* Learning Outcomes */}
                    {module.learningOutcomes && module.learningOutcomes.length > 0 && (
                      <div className="mt-2 pt-2 border-t border-slate-200/60 text-[11px] text-slate-600">
                        <span className="font-medium text-slate-700">Target Outcomes: </span>
                        <span>{module.learningOutcomes.join('; ')}</span>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">No modules defined in this alternative.</p>
            )}
          </div>

          {/* Assessment Scheme */}
          {alternative.assessmentScheme && (
            <div>
              <div className="flex items-center gap-2 mb-2">
                <Award className="w-4 h-4 text-brand-600" />
                <h3 className="text-sm font-semibold text-slate-900">
                  {alternative.assessmentScheme.ciePercentage !== undefined && alternative.assessmentScheme.seePercentage !== undefined
                    ? `Assessment Structure (CIE: ${alternative.assessmentScheme.ciePercentage}%, SEE: ${alternative.assessmentScheme.seePercentage}%)`
                    : (alternative.assessmentScheme.evaluationFrameworkName || 'Evaluation & Assessment Structure')}
                </h3>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
                {alternative.assessmentScheme.components.map((comp, idx) => (
                  <div
                    key={idx}
                    className="p-2.5 rounded bg-white border border-slate-200 text-xs flex flex-col justify-between"
                  >
                    <div>
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-semibold text-slate-900">{comp.name}</span>
                        <span className="font-mono font-medium text-brand-700">{comp.weightage}%</span>
                      </div>
                      <p className="text-[11px] text-slate-500">{comp.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Industry Alignment & Institutional Benchmarking Summaries */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="p-3.5 rounded-lg border border-slate-200 bg-white text-xs">
              <div className="flex items-center gap-1.5 mb-1.5 font-semibold text-slate-900">
                <Briefcase className="w-3.5 h-3.5 text-brand-600" />
                <span>Industry Alignment</span>
              </div>
              <p className="text-slate-600 leading-relaxed">
                {alternative.industryAlignmentSummary}
              </p>
            </div>

            <div className="p-3.5 rounded-lg border border-slate-200 bg-white text-xs">
              <div className="flex items-center gap-1.5 mb-1.5 font-semibold text-slate-900">
                <BookOpen className="w-3.5 h-3.5 text-brand-600" />
                <span>Institutional Benchmarking</span>
              </div>
              <p className="text-slate-600 leading-relaxed">
                {alternative.institutionalBenchmarkSummary}
              </p>
            </div>
          </div>

          {/* Source Transparency & Citations */}
          <div className="pt-2 border-t border-slate-200">
            <EvidenceReferenceList
              references={alternative.evidenceReferences || []}
              title={`Supporting Evidence (${alternative.evidenceReferences?.length || 0} citations)`}
            />
          </div>
        </div>

        {/* Modal Bottom Sticky Action Bar */}
        <div className="px-6 py-3.5 bg-slate-50 border-t border-slate-200 flex items-center justify-between gap-3 sticky bottom-0 z-10">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-100 border border-slate-300 rounded-md transition-colors"
          >
            Close
          </button>

          <button
            type="button"
            onClick={() => onSelect(alternative.id)}
            className={`inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold rounded-md shadow-sm transition-colors ${
              isSelected
                ? 'bg-emerald-700 text-white hover:bg-emerald-800'
                : 'bg-brand-800 text-white hover:bg-brand-900'
            }`}
          >
            <CheckCircle className="w-4 h-4" />
            <span>{isSelected ? 'Confirmed as Preferred Plan' : 'Select This Course Plan'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};

