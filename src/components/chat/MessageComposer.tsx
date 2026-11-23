import React, { useState, useRef, useEffect } from 'react';
import { ArrowUp, SlidersHorizontal } from 'lucide-react';

interface MessageComposerProps {
  onSendMessage: (message: string) => void;
  onOpenClarification?: (message: string) => void;
  isLoading?: boolean;
  placeholder?: string;
}

export const MessageComposer: React.FC<MessageComposerProps> = ({
  onSendMessage,
  onOpenClarification,
  isLoading = false,
  placeholder = 'What course would you like to design today? (e.g. "I need to design a course plan for Computer Vision.")'
}) => {
  const [text, setText] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-resize textarea height up to 160px
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 160)}px`;
    }
  }, [text]);

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed || isLoading) return;

    onSendMessage(trimmed);
    setText('');
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div className="w-full max-w-4xl mx-auto px-3 sm:px-6 pb-3.5 pt-2">
      <form
        onSubmit={handleSubmit}
        className="relative bg-white rounded-2xl border border-slate-300 shadow-sm focus-within:border-brand-600 focus-within:ring-2 focus-within:ring-brand-500/20 transition-all"
      >
        <div className="p-3 sm:p-3.5">
          <textarea
            ref={textareaRef}
            rows={1}
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={isLoading}
            placeholder={placeholder}
            className="w-full resize-none border-0 p-0 text-sm sm:text-base text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-0 bg-transparent max-h-40 leading-relaxed"
          />
        </div>

        <div className="flex items-center justify-between px-3 pb-2.5 pt-1 text-xs text-slate-400">
          {/* Left: Quick Constraints helper button */}
          <div className="flex items-center gap-1.5">
            {onOpenClarification && (
              <button
                type="button"
                onClick={() => onOpenClarification(text.trim() || 'Computer Vision')}
                disabled={isLoading}
                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-medium text-slate-600 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 transition-colors"
                title="Clarify syllabus constraints (Lab 1 Planner Loop)"
              >
                <SlidersHorizontal className="w-3 h-3 text-slate-500" />
                <span className="hidden sm:inline">Clarify Constraints</span>
              </button>
            )}
            <span className="text-[11px] text-slate-400 hidden md:inline">
              <kbd className="font-mono text-[10px] bg-slate-100 px-1 py-0.5 rounded border border-slate-200">Enter</kbd> to send
            </span>
          </div>

          {/* Right: Send Button */}
          <button
            type="submit"
            disabled={!text.trim() || isLoading}
            className={`w-8 h-8 rounded-full flex items-center justify-center transition-all ${
              text.trim() && !isLoading
                ? 'bg-brand-700 text-white hover:bg-brand-800 shadow-xs active:scale-95'
                : 'bg-slate-100 text-slate-300 cursor-not-allowed'
            }`}
            aria-label="Send message"
          >
            {isLoading ? (
              <div className="w-4 h-4 border-2 border-slate-400 border-t-transparent rounded-full animate-spin" />
            ) : (
              <ArrowUp className="w-4 h-4" />
            )}
          </button>
        </div>
      </form>

      <p className="text-[11px] text-slate-400 text-center mt-2 font-normal">
        Curriculum AI verifies institutional syllabi and ACM/APNI benchmarks. Always review generated recommendations.
      </p>
    </div>
  );
};
