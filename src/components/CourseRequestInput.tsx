import React, { useState } from 'react';
import { ArrowRight, Sparkles, AlertCircle, SlidersHorizontal } from 'lucide-react';

interface CourseRequestInputProps {
  onSubmit: (prompt: string) => void;
  onOpenClarification?: (prompt: string) => void;
  isSubmitting?: boolean;
}

export const CourseRequestInput: React.FC<CourseRequestInputProps> = ({
  onSubmit,
  onOpenClarification,
  isSubmitting = false
}) => {
  const [prompt, setPrompt] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = prompt.trim();

    if (!trimmed) {
      setValidationError('Please enter a course or subject to begin curriculum research.');
      return;
    }

    if (trimmed.length < 5) {
      setValidationError('Please provide a slightly more descriptive course request (e.g., "I need to design the course plan for Computer Vision").');
      return;
    }

    setValidationError(null);
    onSubmit(trimmed);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const handleExampleClick = (exampleText: string) => {
    setPrompt(exampleText);
    setValidationError(null);
  };

  const handleClarifyClick = () => {
    const trimmed = prompt.trim() || 'Computer Vision';
    if (onOpenClarification) {
      onOpenClarification(trimmed);
    }
  };

  return (
    <div className="w-full max-w-3xl mx-auto py-10 px-4 sm:px-6">
      {/* Hero Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 border border-brand-200/60 mb-4">
          <Sparkles className="w-3.5 h-3.5 text-brand-500" />
          <span>Automated Academic Intelligence</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-semibold tracking-tight text-slate-900 mb-3">
          Design better curricula, backed by real-world intelligence.
        </h1>
        <p className="text-sm sm:text-base text-slate-600 max-w-xl mx-auto font-normal leading-relaxed">
          The system researches industry requirements, benchmarks institutional curricula (IITs, NITs, ACM/APNI),
          and generates evidence-backed course plans through coordinated AI agents.
        </p>
      </div>

      {/* Main Natural Language Request Form */}
      <form onSubmit={handleSubmit} className="relative">
        <div className="bg-white rounded-xl border border-slate-300 shadow-sm transition-all focus-within:border-brand-500 focus-within:ring-2 focus-within:ring-brand-500/20">
          <div className="p-4 sm:p-5">
            <label htmlFor="course-request-input" className="sr-only">
              Course Design Request
            </label>
            <textarea
              id="course-request-input"
              rows={3}
              value={prompt}
              onChange={(e) => {
                setPrompt(e.target.value);
                if (validationError) setValidationError(null);
              }}
              onKeyDown={handleKeyDown}
              disabled={isSubmitting}
              placeholder="What course would you like to design today? (e.g., I need to design the course plan for Computer Vision.)"
              className="w-full resize-none border-0 p-0 text-base sm:text-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-0 disabled:opacity-60 bg-transparent"
            />
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 sm:px-5 py-3 bg-slate-50/80 border-t border-slate-100 rounded-b-xl">
            <div className="text-xs text-slate-500 font-normal">
              Press <kbd className="px-1.5 py-0.5 text-[10px] font-semibold text-slate-600 bg-white border border-slate-200 rounded shadow-xs">Enter</kbd> to submit, <kbd className="px-1.5 py-0.5 text-[10px] font-semibold text-slate-600 bg-white border border-slate-200 rounded shadow-xs">Shift + Enter</kbd> for new line
            </div>

            <div className="flex items-center gap-2 w-full sm:w-auto">
              {onOpenClarification && (
                <button
                  type="button"
                  onClick={handleClarifyClick}
                  disabled={isSubmitting}
                  className="inline-flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-lg shadow-2xs transition-colors"
                  title="Clarify and lock constraints (Lab 1 Planner Loop)"
                >
                  <SlidersHorizontal className="w-3.5 h-3.5 text-slate-500" />
                  <span className="hidden sm:inline">Clarify Constraints</span>
                </button>
              )}

              <button
                type="submit"
                disabled={isSubmitting || !prompt.trim()}
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 text-sm font-medium text-white bg-brand-700 hover:bg-brand-800 active:bg-brand-900 rounded-lg shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2"
              >
                <span>{isSubmitting ? 'Initializing Coordinator...' : 'Generate Course Plans'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Validation Error Banner */}
        {validationError && (
          <div className="mt-3 flex items-center gap-2 text-xs text-rose-700 bg-rose-50 border border-rose-200 px-3.5 py-2 rounded-lg">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{validationError}</span>
          </div>
        )}
      </form>

      {/* Suggested Natural Language Prompts */}
      <div className="mt-6">
        <p className="text-xs font-medium text-slate-400 mb-2">
          Or try a natural language prompt:
        </p>
        <div className="flex flex-wrap gap-2">
          {[
            'I need to design the course plan for Computer Vision.',
            'Course plan for Natural Language Processing for 6th semester B.Tech.',
            'Prepare syllabus for Agentic AI Systems aligned with ACM 2023 standards.',
            'Design an undergraduate course on Edge Computing and IoT Networks.'
          ].map((example, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => handleExampleClick(example)}
              disabled={isSubmitting}
              className="text-xs text-left px-3 py-1.5 rounded-md bg-white hover:bg-slate-100 border border-slate-200 text-slate-700 transition-colors disabled:opacity-50"
            >
              &ldquo;{example}&rdquo;
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
