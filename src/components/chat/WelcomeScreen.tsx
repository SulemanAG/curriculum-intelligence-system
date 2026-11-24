import React from 'react';
import { BookOpen, Sparkles, Compass, ShieldCheck } from 'lucide-react';

interface WelcomeScreenProps {
  onSelectPrompt: (promptText: string) => void;
}

export const WelcomeScreen: React.FC<WelcomeScreenProps> = ({ onSelectPrompt }) => {
  const suggestedPrompts = [
    {
      title: 'Design Course Plan',
      prompt: 'Design a course plan for Computer Vision.',
      description: 'Coordinates academic mapping, industry demand, and outcome taxonomy for Computer Vision.'
    },
    {
      title: 'Analyse Curriculum',
      prompt: 'Analyse the existing Data Structures curriculum.',
      description: 'Reviews prerequisites, algorithmic rigor, and alignment with modern software engineering.'
    },
    {
      title: 'Benchmark Course',
      prompt: 'Benchmark an Agentic AI course against leading universities.',
      description: 'Evaluates multi-agent architectures and autonomous decision systems against ACM CS2023.'
    },
    {
      title: 'Identify Skill Gaps',
      prompt: 'Identify industry skill gaps in a Machine Learning course.',
      description: 'Triangulates institutional course coverage against live employer skill requirements.'
    }
  ];

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-6 max-w-2xl mx-auto text-center animate-in fade-in duration-200">
      {/* Icon & Title */}
      <div className="w-12 h-12 rounded-2xl bg-brand-800 text-white flex items-center justify-center mx-auto mb-4 shadow-sm">
        <BookOpen className="w-6 h-6 text-brand-200" />
      </div>

      <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900 mb-3">
        Curriculum Intelligence System
      </h1>
      <p className="text-sm text-slate-600 max-w-lg mx-auto mb-8 font-normal leading-relaxed">
        Describe the course you want to design, analyse or improve. AURA will coordinate academic mapping, industry research, benchmarking, gap analysis and course-plan recommendations.
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

