import React from 'react';
import { BookOpen } from 'lucide-react';
import { ChatMessage, UserFeedback } from '../../types';
import { ConversationalResearchStage } from './ConversationalResearchStage';
import { AlternativeComparison } from '../AlternativeComparison';
import { FeedbackForm } from '../FeedbackForm';
import { FinalDocumentPreview } from '../FinalDocumentPreview';

interface ChatMessageItemProps {
  message: ChatMessage;
  onSelectAlternative?: (altId: string) => void;
  onProceedToFeedback?: () => void;
  onSubmitFeedback?: (feedback: UserFeedback) => void;
  onBackToAlternatives?: () => void;
  onLoadSchemaPreview?: () => void;
  onViewAlternatives?: () => void;
  onReset?: () => void;
}

export const ChatMessageItem: React.FC<ChatMessageItemProps> = ({
  message,
  onSelectAlternative,
  onProceedToFeedback,
  onSubmitFeedback,
  onBackToAlternatives,
  onLoadSchemaPreview,
  onViewAlternatives,
  onReset
}) => {
  const isUser = message.role === 'user';

  if (isUser) {
    return (
      <div className="flex justify-end my-3 px-1 sm:px-2">
        <div className="max-w-[85%] sm:max-w-[75%] md:max-w-2xl rounded-2xl bg-blue-50 border border-blue-200/80 px-4 py-3 text-sm text-slate-900 shadow-2xs leading-relaxed">
          <p className="whitespace-pre-wrap">{message.content}</p>
          <span className="text-[10px] text-slate-400 block text-right mt-1.5 font-mono">
            {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </span>
        </div>
      </div>
    );
  }

  // Assistant Message
  return (
    <div className="flex justify-start my-4 px-1 sm:px-2 w-full">
      <div className="flex items-start gap-3 w-full min-w-0">
        {/* Assistant Avatar */}
        <div className="w-8 h-8 rounded-xl bg-brand-800 text-white flex items-center justify-center flex-shrink-0 shadow-2xs mt-0.5">
          <BookOpen className="w-4 h-4 text-brand-200" />
        </div>

        {/* Message Content Container */}
        <div className="flex-1 min-w-0 space-y-3">
          {/* Header identifier */}
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-slate-900">Curriculum AI</span>
            <span className="text-[10px] text-slate-400 font-mono">
              {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          </div>

          {/* Text Content (Readable line-length constraint) */}
          {message.content && (
            <div className="text-sm text-slate-800 leading-relaxed font-normal whitespace-pre-wrap max-w-3xl">
              {message.content}
            </div>
          )}

          {/* Interactive Payload: Research Progress */}
          {message.payloadType === 'research_progress' && message.researchRun && (
            <div className="w-full min-w-0">
              <ConversationalResearchStage
                run={message.researchRun}
                onLoadSchemaPreview={onLoadSchemaPreview}
                onViewAlternatives={onViewAlternatives}
                hasAlternatives={Boolean(message.alternatives && message.alternatives.length > 0)}
              />
            </div>
          )}

          {/* Interactive Payload: 4 Alternatives Comparison */}
          {message.payloadType === 'alternatives' && message.alternatives && (
            <div className="mt-2 w-full min-w-0">
              <AlternativeComparison
                alternatives={message.alternatives}
                selectedAlternativeId={message.selectedAlternativeId || null}
                onSelectAlternative={onSelectAlternative || (() => {})}
                onProceedToFeedback={onProceedToFeedback || (() => {})}
              />
            </div>
          )}

          {/* Interactive Payload: Feedback Form */}
          {message.payloadType === 'feedback' && message.alternatives && (
            <div className="mt-2 w-full">
              <FeedbackForm
                selectedAlternative={
                  message.alternatives.find((a) => a.id === message.selectedAlternativeId) ||
                  message.alternatives[0]
                }
                allAlternatives={message.alternatives}
                onSubmitFeedback={onSubmitFeedback || (() => {})}
                onBackToComparison={onBackToAlternatives || (() => {})}
              />
            </div>
          )}

          {/* Interactive Payload: Final Document Preview */}
          {message.payloadType === 'final_document' && message.finalPlan && (
            <div className="mt-2 w-full">
              <FinalDocumentPreview
                plan={message.finalPlan}
                onReset={onReset}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
