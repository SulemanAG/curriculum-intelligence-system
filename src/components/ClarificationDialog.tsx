import React, { useState } from 'react';
import { HelpCircle, Lock, ArrowRight, X } from 'lucide-react';
import { DegreeLevel } from '../types';

export interface ClarificationConstraints {
  degreeLevel: DegreeLevel;
  courseNature: 'Program Core' | 'Professional Elective' | 'Open Elective';
  credits: number;
  contactDistribution: '3:0:2 (Theory + Practical Lab)' | '3:1:0 (Theory + Tutorial)' | '3:0:0 (Theory Only)';
  targetSemester: number;
}

interface ClarificationDialogProps {
  initialPrompt: string;
  onLockPlan: (constraints: ClarificationConstraints) => void;
  onCancel: () => void;
}

export const ClarificationDialog: React.FC<ClarificationDialogProps> = ({
  initialPrompt,
  onLockPlan,
  onCancel
}) => {
  const [degreeLevel, setDegreeLevel] = useState<DegreeLevel>('UG');
  const [courseNature, setCourseNature] = useState<'Program Core' | 'Professional Elective' | 'Open Elective'>('Program Core');
  const [contactDistribution, setContactDistribution] = useState<'3:0:2 (Theory + Practical Lab)' | '3:1:0 (Theory + Tutorial)' | '3:0:0 (Theory Only)'>('3:0:2 (Theory + Practical Lab)');
  const [targetSemester, setTargetSemester] = useState<number>(6);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onLockPlan({
      degreeLevel,
      courseNature,
      credits: contactDistribution.startsWith('3:0:0') ? 3 : 4,
      contactDistribution,
      targetSemester
    });
  };

  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="clarification-title"
    >
      <div className="bg-white rounded-xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-md bg-brand-100 text-brand-800">
              <HelpCircle className="w-4 h-4" />
            </div>
            <div>
              <h2 id="clarification-title" className="text-sm font-semibold text-slate-900">
                Coordinator Clarification &amp; Blueprint
              </h2>
              <span className="text-[11px] text-slate-500 font-mono">
                Lab 1: Clarify &rarr; Plan &rarr; Lock Constraints
              </span>
            </div>
          </div>
          <button
            type="button"
            onClick={onCancel}
            className="text-slate-400 hover:text-slate-600 p-1 rounded-md"
            aria-label="Cancel"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4 text-xs text-slate-800">
          <div className="p-3 rounded bg-brand-50/70 border border-brand-200">
            <span className="text-slate-500 block mb-0.5">Course Request:</span>
            <p className="font-medium text-brand-900 text-xs italic">&ldquo;{initialPrompt}&rdquo;</p>
          </div>

          <p className="text-slate-600 leading-relaxed">
            The AURA Coordinator locks these curriculum constraints before parallel specialist agents execute.
          </p>

          {/* 1. Academic Level */}
          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              1. Degree Level &amp; Cohort
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setDegreeLevel('UG')}
                className={`py-2 px-3 rounded-md border text-center font-medium transition-colors ${
                  degreeLevel === 'UG'
                    ? 'border-brand-600 bg-brand-50 text-brand-900'
                    : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                }`}
              >
                Undergraduate (B.Tech)
              </button>
              <button
                type="button"
                onClick={() => setDegreeLevel('PG')}
                className={`py-2 px-3 rounded-md border text-center font-medium transition-colors ${
                  degreeLevel === 'PG'
                    ? 'border-brand-600 bg-brand-50 text-brand-900'
                    : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                }`}
              >
                Postgraduate (M.Tech)
              </button>
            </div>
          </div>

          {/* 2. Course Category */}
          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              2. Program Curriculum Role
            </label>
            <div className="grid grid-cols-3 gap-2">
              {(['Program Core', 'Professional Elective', 'Open Elective'] as const).map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCourseNature(cat)}
                  className={`py-1.5 px-2 rounded-md border text-center text-[11px] font-medium transition-colors ${
                    courseNature === cat
                      ? 'border-brand-600 bg-brand-50 text-brand-900'
                      : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {/* 3. Credit & Contact Hours Structure */}
          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              3. Credit &amp; Contact Structure (L:T:P)
            </label>
            <select
              value={contactDistribution}
              onChange={(e) => setContactDistribution(e.target.value as any)}
              className="w-full rounded-md border border-slate-300 p-2 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="3:0:2 (Theory + Practical Lab)">
                4 Credits &mdash; 3:0:2 (3 hrs Lecture + 2 hrs Lab) [Recommended for AI/Vision]
              </option>
              <option value="3:1:0 (Theory + Tutorial)">
                4 Credits &mdash; 3:1:0 (3 hrs Lecture + 1 hr Tutorial)
              </option>
              <option value="3:0:0 (Theory Only)">
                3 Credits &mdash; 3:0:0 (3 hrs Lecture Only)
              </option>
            </select>
          </div>

          {/* 4. Target Semester */}
          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              4. Target Academic Semester
            </label>
            <div className="grid grid-cols-4 gap-2">
              {[5, 6, 7, 8].map((sem) => (
                <button
                  key={sem}
                  type="button"
                  onClick={() => setTargetSemester(sem)}
                  className={`py-1.5 px-2 rounded-md border text-center font-medium transition-colors ${
                    targetSemester === sem
                      ? 'border-brand-600 bg-brand-50 text-brand-900'
                      : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  Semester {sem}
                </button>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div className="pt-3 border-t border-slate-200 flex items-center justify-between gap-2">
            <button
              type="button"
              onClick={onCancel}
              className="px-3 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-md"
            >
              Back
            </button>
            <button
              type="submit"
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-brand-800 hover:bg-brand-900 rounded-md shadow-xs transition-colors"
            >
              <Lock className="w-3.5 h-3.5" />
              <span>Lock Blueprint &amp; Run Swarm</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

