import React, { useState } from 'react';
import {
  CheckCircle,
  ShieldAlert
} from 'lucide-react';
import { CoursePlanAlternative, UserFeedback } from '../types';

interface FeedbackFormProps {
  selectedAlternative: CoursePlanAlternative;
  allAlternatives: CoursePlanAlternative[];
  onSubmitFeedback: (feedback: UserFeedback) => void;
  onBackToComparison: () => void;
  isSubmitting?: boolean;
}

export const FeedbackForm: React.FC<FeedbackFormProps> = ({
  selectedAlternative,
  allAlternatives,
  onSubmitFeedback,
  onBackToComparison,
  isSubmitting = false
}) => {
  const [rationale, setRationale] = useState('');
  const [modifications, setModifications] = useState('');
  const [facultyName, setFacultyName] = useState('');
  const [facultyDept, setFacultyDept] = useState('Computer Science & Engineering');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const rejectedIds = allAlternatives
      .filter((alt) => alt.id !== selectedAlternative.id)
      .map((alt) => alt.id);

    const feedbackPayload: UserFeedback = {
      requestId: 'req-' + Date.now(),
      selectedAlternativeId: selectedAlternative.id,
      rejectedAlternativeIds: rejectedIds,
      facultyName: facultyName.trim() || undefined,
      facultyId: facultyDept,
      rationale: rationale.trim() || 'Selected based on institutional fit and evidence coverage.',
      suggestedModifications: modifications.trim() || undefined,
      submittedAt: new Date().toISOString()
    };

    onSubmitFeedback(feedbackPayload);
  };

  return (
    <div className="w-full max-w-4xl py-2 px-0">
      {/* Top Banner */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm mb-6">
        <div className="flex items-center gap-2 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200/60">
            Faculty Gate &amp; Preference Logging
          </span>
          <span className="text-xs text-slate-500 font-mono">Lab 12 / Human Review Gate</span>
        </div>
        <h2 className="text-xl font-semibold text-slate-900">
          Confirm Course Plan Preference &amp; Feedback
        </h2>
        <p className="text-xs sm:text-sm text-slate-600 mt-1">
          As the faculty authority, your selection certifies the curriculum approach for adoption.
          Your feedback is preserved in the audit log and contributes to the preference alignment dataset.
        </p>

        {/* Selected Plan Summary Card */}
        <div className="mt-4 p-4 rounded-lg bg-brand-50/60 border border-brand-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-brand-200 text-brand-900">
                {selectedAlternative.code}
              </span>
              <span className="text-sm font-bold text-slate-900">
                {selectedAlternative.title}
              </span>
            </div>
            <p className="text-xs text-slate-600 mt-1">
              Strategy: <strong>{selectedAlternative.strategyName}</strong> &bull; {selectedAlternative.credits} Credits &bull; {selectedAlternative.evidenceCoveragePercent}% Grounded
            </p>
          </div>

          <button
            type="button"
            onClick={onBackToComparison}
            disabled={isSubmitting}
            className="text-xs font-medium text-brand-700 hover:text-brand-900 hover:underline flex-shrink-0"
          >
            Change Selection
          </button>
        </div>
      </div>

      {/* Feedback Form */}
      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label htmlFor="faculty-name" className="block text-xs font-medium text-slate-700 mb-1">
              Faculty / Reviewer Name (Optional)
            </label>
            <input
              id="faculty-name"
              type="text"
              value={facultyName}
              onChange={(e) => setFacultyName(e.target.value)}
              placeholder="e.g., Prof. Faculty Member"
              disabled={isSubmitting}
              className="w-full text-xs rounded-md border border-slate-300 px-3 py-2 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div>
            <label htmlFor="faculty-dept" className="block text-xs font-medium text-slate-700 mb-1">
              Academic Department
            </label>
            <input
              id="faculty-dept"
              type="text"
              value={facultyDept}
              onChange={(e) => setFacultyDept(e.target.value)}
              disabled={isSubmitting}
              className="w-full text-xs rounded-md border border-slate-300 px-3 py-2 text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
        </div>

        {/* Selection Rationale */}
        <div>
          <label htmlFor="rationale-input" className="block text-xs font-medium text-slate-700 mb-1">
            Rationale for Selection <span className="text-slate-400 font-normal">(Why does this plan best satisfy requirements?)</span>
          </label>
          <textarea
            id="rationale-input"
            rows={3}
            value={rationale}
            onChange={(e) => setRationale(e.target.value)}
            disabled={isSubmitting}
            placeholder="e.g., This alternative offers superior alignment with our department's 6th semester lab infrastructure and balances ACM foundational units with modern industry frameworks."
            className="w-full text-xs rounded-md border border-slate-300 p-3 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        {/* Desired Modifications */}
        <div>
          <label htmlFor="modifications-input" className="block text-xs font-medium text-slate-700 mb-1">
            Suggested Revisions or Adjustments <span className="text-slate-400 font-normal">(Optional)</span>
          </label>
          <textarea
            id="modifications-input"
            rows={2}
            value={modifications}
            onChange={(e) => setModifications(e.target.value)}
            disabled={isSubmitting}
            placeholder="e.g., Would like 4 additional lab hours allocated to Unit 3 practical hands-on exercises."
            className="w-full text-xs rounded-md border border-slate-300 p-3 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        {/* Transparent Machine Learning Notice */}
        <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg flex items-start gap-2.5 text-xs text-slate-600">
          <ShieldAlert className="w-4 h-4 text-slate-500 flex-shrink-0 mt-0.5" />
          <div className="leading-relaxed">
            <span className="font-semibold text-slate-700 block">Audit &amp; Model Alignment Protocol:</span>
            Your decision is committed to the immutable audit log and stored in the institutional preference dataset.
            Recording this preference does <em>not</em> immediately retrain backend models; training and preference tuning are governed by separate offline evaluation pipelines.
          </div>
        </div>

        {/* Form Actions */}
        <div className="pt-3 border-t border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-3">
          <button
            type="button"
            onClick={onBackToComparison}
            disabled={isSubmitting}
            className="w-full sm:w-auto px-4 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-100 border border-slate-300 rounded-md transition-colors"
          >
            Back to Alternatives
          </button>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-2.5 text-xs font-semibold text-white bg-brand-800 hover:bg-brand-900 rounded-md shadow-xs transition-colors disabled:opacity-50"
          >
            {isSubmitting ? (
              <>
                <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                <span>Publishing Decision...</span>
              </>
            ) : (
              <>
                <CheckCircle className="w-4 h-4" />
                <span>Confirm Selection &amp; Generate Final Document</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
