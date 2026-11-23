import React, { useRef, useEffect } from 'react';
import { ChatMessage, UserFeedback } from '../../types';
import { ChatMessageItem } from './ChatMessageItem';
import { WelcomeScreen } from './WelcomeScreen';
import { MessageComposer } from './MessageComposer';
import { AlertTriangle } from 'lucide-react';

interface ChatWorkspaceProps {
  messages: ChatMessage[];
  onSendMessage: (message: string) => void;
  onOpenClarification?: (message: string) => void;
  isLoading?: boolean;
  onSelectAlternative?: (altId: string) => void;
  onProceedToFeedback?: () => void;
  onSubmitFeedback?: (feedback: UserFeedback) => void;
  onBackToAlternatives?: () => void;
  onLoadSchemaPreview?: () => void;
  onViewAlternatives?: () => void;
  onReset?: () => void;
  isDevPreview?: boolean;
}

export const ChatWorkspace: React.FC<ChatWorkspaceProps> = ({
  messages,
  onSendMessage,
  onOpenClarification,
  isLoading = false,
  onSelectAlternative,
  onProceedToFeedback,
  onSubmitFeedback,
  onBackToAlternatives,
  onLoadSchemaPreview,
  onViewAlternatives,
  onReset,
  isDevPreview = false
}) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  return (
    <div className="flex-1 flex flex-col h-full bg-white relative overflow-hidden">
      {/* Dev Preview Watermark Banner */}
      {isDevPreview && (
        <div className="bg-amber-100/90 border-b border-amber-300 px-4 py-1.5 text-xs text-amber-900 text-center font-medium flex items-center justify-center gap-2 z-10 flex-shrink-0">
          <AlertTriangle className="w-3.5 h-3.5 text-amber-700 flex-shrink-0" />
          <span>
            <strong>DEVELOPMENT PREVIEW MODE:</strong> Displaying Chapter 5 preview schema records for UI evaluation. This is non-production preview data, NOT real research results.
          </span>
        </div>
      )}

      {/* 2. Scrollable Conversation Content */}
      <main className="flex-1 overflow-y-auto min-h-0 px-3 sm:px-6 lg:px-8 py-4">
        {messages.length === 0 ? (
          <WelcomeScreen onSelectPrompt={onSendMessage} />
        ) : (
          <div className="w-full max-w-[1536px] mx-auto space-y-4 pb-16">
            {messages.map((msg) => (
              <ChatMessageItem
                key={msg.id}
                message={msg}
                onSelectAlternative={onSelectAlternative}
                onProceedToFeedback={onProceedToFeedback}
                onSubmitFeedback={onSubmitFeedback}
                onBackToAlternatives={onBackToAlternatives}
                onLoadSchemaPreview={onLoadSchemaPreview}
                onViewAlternatives={onViewAlternatives}
                onReset={onReset}
              />
            ))}

            {/* Loading / Generating Indicator */}
            {isLoading && (
              <div className="flex items-center gap-3 px-4 py-3 text-xs text-slate-500 animate-in fade-in duration-150">
                <div className="w-8 h-8 rounded-xl bg-brand-800 text-white flex items-center justify-center flex-shrink-0">
                  <div className="w-4 h-4 border-2 border-brand-200 border-t-transparent rounded-full animate-spin" />
                </div>
                <div className="flex items-center gap-1.5 bg-slate-50 px-3 py-2 rounded-xl border border-slate-200">
                  <span className="font-medium text-slate-700">Curriculum AI is analyzing</span>
                  <span className="flex gap-0.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce" />
                    <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce [animation-delay:0.2s]" />
                    <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce [animation-delay:0.4s]" />
                  </span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} className="h-4" />
          </div>
        )}
      </main>

      {/* 3. Anchored Message Composer (Clean floating dock without horizontal separator line) */}
      <footer className="bg-white/95 backdrop-blur-md flex-shrink-0 z-10">
        <MessageComposer
          onSendMessage={onSendMessage}
          onOpenClarification={onOpenClarification}
          isLoading={isLoading}
        />
      </footer>
    </div>
  );
};
