import React from 'react';
import { BookOpen, Sparkles, Compass, ShieldCheck } from 'lucide-react';

interface WelcomeScreenProps {
  onSelectPrompt: (promptText: string) => void;
}

export const WelcomeScreen: React.FC<WelcomeScreenProps> = ({ onSelectPrompt }) => {
  const suggestedPrompts = [
    {
      title: 'Computer Vision',
      prompt: 'I need to design a course plan for Computer Vision.',
      description: 'Benchmarks camera geometry, deep CNNs, and vision transformers with industry demand.'
    },
    {
      title: 'Natural Language Processing',
      prompt: 'Course plan for Natural Language Processing for 6th semester B.Tech.',
      description: 'Aligns LLMs, tokenization, and sequence models against ACM CS2023 standards.'
    },
    {
      title: 'Agentic AI Systems',
      prompt: 'Prepare syllabus for Agentic AI Systems aligned with ACM and APNI standards.',
      description: 'Covers Coordinator-plus-workers architecture, tool use, and safety governance.'
    },
    {
      title: 'Edge Computing & IoT',
      prompt: 'Design an undergraduate course on Edge Computing and IoT Networks.',
      description: 'Synthesizes embedded inference, low-power protocols, and real-time sensor pipelines.'
    }
  ];

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-6 max-w-2xl mx-auto text-center animate-in fade-in duration-200">
      {/* Icon & Title */}
      <div className="w-12 h-12 rounded-2xl bg-brand-800 text-white flex items-center justify-center mx-auto mb-4 shadow-sm">
        <BookOpen className="w-6 h-6 text-brand-200" />
      </div>

      <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-900 mb-2">
        What course would you like to design today?
      </h1>
      <p className="text-sm text-slate-500 max-w-md mx-auto mb-8 font-normal leading-relaxed">
        Curriculum AI automatically researches industry requirements, benchmarks IIT/NIT syllabi, and drafts evidence-grounded course plans.
      </p>

      {/* Suggested Prompts Cards (Claude / ChatGPT style) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full text-left">
        {suggestedPrompts.map((item, idx) => (
          <button
            key={idx}
            type="button"
            onClick={() => onSelectPrompt(item.prompt)}
            className="p-3.5 rounded-xl border border-slate-200 bg-white hover:border-brand-400 hover:bg-slate-50/70 transition-all text-xs flex flex-col justify-between group shadow-2xs"
          >
            <div>
              <div className="flex items-center gap-1.5 font-semibold text-slate-800 group-hover:text-brand-900 mb-1">
                <Sparkles className="w-3.5 h-3.5 text-brand-600 flex-shrink-0" />
                <span>{item.title}</span>
              </div>
              <p className="text-slate-500 line-clamp-2 leading-relaxed">
                {item.description}
              </p>
            </div>
            <span className="text-[11px] text-brand-700 font-medium mt-2 block opacity-0 group-hover:opacity-100 transition-opacity">
              &ldquo;{item.prompt}&rdquo; &rarr;
            </span>
          </button>
        ))}
      </div>

      {/* Trust & Evidence Footnote */}
      <div className="mt-8 flex items-center justify-center gap-4 text-[11px] text-slate-400">
        <span className="flex items-center gap-1">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
          <span>Real-world evidence citations</span>
        </span>
        <span>&bull;</span>
        <span className="flex items-center gap-1">
          <Compass className="w-3.5 h-3.5 text-brand-600" />
          <span>IIT/NIT &amp; ACM/APNI benchmarking</span>
        </span>
      </div>
    </div>
  );
};

