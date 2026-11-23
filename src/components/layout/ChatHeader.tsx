import React, { useState } from 'react';
import { Menu, MoreVertical, Trash2, Edit2, Download, Check } from 'lucide-react';

interface ChatHeaderProps {
  conversationTitle: string;
  backendOnline: boolean;
  onOpenMobileSidebar: () => void;
  onClearConversation?: () => void;
  onRenameConversation?: (newTitle: string) => void;
  onExportConversation?: () => void;
}

export const ChatHeader: React.FC<ChatHeaderProps> = ({
  conversationTitle,
  backendOnline,
  onOpenMobileSidebar,
  onClearConversation,
  onRenameConversation,
  onExportConversation
}) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(conversationTitle);

  const handleSaveTitle = (e: React.FormEvent) => {
    e.preventDefault();
    if (editTitle.trim() && onRenameConversation) {
      onRenameConversation(editTitle.trim());
    }
    setIsEditing(false);
  };

  return (
    <header className="h-14 border-b border-slate-200 bg-white/95 backdrop-blur-xs px-4 flex items-center justify-between z-20 flex-shrink-0">
      {/* Left: Mobile Sidebar Toggle & Title / Context */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileSidebar}
          className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 lg:hidden"
          aria-label="Open menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-slate-800 tracking-tight">
            Curriculum AI
          </span>
        </div>
      </div>

      {/* Right: Current Conversation Title & Action Dropdown */}
      <div className="flex items-center gap-3">
        {/* Subtle Backend Status */}
        <div
          className={`hidden md:flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium border ${
            backendOnline
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
              : 'bg-slate-50 text-slate-500 border-slate-200'
          }`}
          title={backendOnline ? 'Spring Boot Coordinator Connected' : 'Phase 1 Frontend Mode (Coordinator Offline)'}
        >
          <span
            className={`w-1.5 h-1.5 rounded-full ${
              backendOnline ? 'bg-emerald-500' : 'bg-slate-400'
            }`}
          />
          <span>{backendOnline ? 'Connected' : 'Standalone'}</span>
        </div>

        {/* Current Conversation Title & Menu */}
        <div className="relative flex items-center gap-1">
          {isEditing ? (
            <form onSubmit={handleSaveTitle} className="flex items-center gap-1">
              <input
                type="text"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                autoFocus
                className="text-xs px-2 py-1 border border-brand-500 rounded bg-white text-slate-900 focus:outline-none"
              />
              <button
                type="submit"
                className="p-1 text-emerald-600 hover:text-emerald-700"
                title="Save title"
              >
                <Check className="w-3.5 h-3.5" />
              </button>
            </form>
          ) : (
            <span
              className="text-xs font-medium text-slate-700 max-w-[150px] sm:max-w-[240px] truncate block text-right"
              title={conversationTitle}
            >
              {conversationTitle}
            </span>
          )}

          {/* Action Menu Toggle */}
          <div className="relative">
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="p-1 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
              aria-label="Conversation actions"
            >
              <MoreVertical className="w-4 h-4" />
            </button>

            {menuOpen && (
              <>
                <div
                  className="fixed inset-0 z-30"
                  onClick={() => setMenuOpen(false)}
                />
                <div className="absolute right-0 mt-1 w-44 rounded-lg bg-white border border-slate-200 shadow-lg py-1 z-40 text-xs text-slate-700 animate-in fade-in zoom-in-95 duration-100">
                  {onRenameConversation && (
                    <button
                      onClick={() => {
                        setEditTitle(conversationTitle);
                        setIsEditing(true);
                        setMenuOpen(false);
                      }}
                      className="w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-slate-50 text-slate-700"
                    >
                      <Edit2 className="w-3.5 h-3.5 text-slate-400" />
                      <span>Rename Chat</span>
                    </button>
                  )}

                  {onExportConversation && (
                    <button
                      onClick={() => {
                        onExportConversation();
                        setMenuOpen(false);
                      }}
                      className="w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-slate-50 text-slate-700"
                    >
                      <Download className="w-3.5 h-3.5 text-slate-400" />
                      <span>Export Text</span>
                    </button>
                  )}

                  {onClearConversation && (
                    <button
                      onClick={() => {
                        onClearConversation();
                        setMenuOpen(false);
                      }}
                      className="w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-rose-50 text-rose-600"
                    >
                      <Trash2 className="w-3.5 h-3.5 text-rose-500" />
                      <span>Clear Messages</span>
                    </button>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

