import React from 'react';
import {
  BookOpen,
  Plus,
  Cpu,
  FileText,
  Calendar,
  Wrench,
  MessageSquare,
  Settings,
  PanelLeftClose,
  PanelLeft,
  X
} from 'lucide-react';
import { Conversation, NavigationDestination } from '../../types';

interface SidebarProps {
  currentView: NavigationDestination;
  activeConversationId: string | null;
  conversations: Conversation[];
  onSelectConversation: (id: string) => void;
  onNewChat: () => void;
  onNavigate: (destination: NavigationDestination) => void;
  isOpenMobile: boolean;
  onCloseMobile: () => void;
  isCollapsedDesktop: boolean;
  onToggleCollapseDesktop: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentView,
  activeConversationId,
  conversations,
  onSelectConversation,
  onNewChat,
  onNavigate,
  isOpenMobile,
  onCloseMobile,
  isCollapsedDesktop,
  onToggleCollapseDesktop
}) => {
  return (
    <>
      {/* Mobile Backdrop */}
      {isOpenMobile && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-2xs lg:hidden"
          onClick={onCloseMobile}
          aria-hidden="true"
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex flex-col bg-[#f8fafc] border-r border-slate-200 transition-all duration-300 lg:static ${
          isOpenMobile ? 'translate-x-0 w-72' : '-translate-x-full lg:translate-x-0'
        } ${isCollapsedDesktop ? 'lg:w-16' : 'lg:w-64'}`}
      >
        {/* Top Header: Logo & Branding */}
        <div className="h-14 px-4 flex items-center justify-between border-b border-slate-200/80 bg-[#f8fafc]">
          <div
            className="flex items-center gap-2.5 cursor-pointer overflow-hidden"
            onClick={() => onNavigate('chat')}
          >
            <div className="w-8 h-8 rounded-lg bg-brand-800 text-white flex items-center justify-center flex-shrink-0 shadow-2xs">
              <BookOpen className="w-4 h-4 text-brand-200" />
            </div>
            {!isCollapsedDesktop && (
              <div className="leading-tight truncate">
                <span className="font-semibold text-sm tracking-tight text-slate-900 block truncate">
                  Curriculum AI
                </span>
              </div>
            )}
          </div>

          {/* Desktop Collapse Toggle / Mobile Close */}
          <div className="flex items-center">
            <button
              onClick={onCloseMobile}
              className="p-1 rounded-md text-slate-400 hover:text-slate-600 lg:hidden"
              aria-label="Close sidebar"
            >
              <X className="w-5 h-5" />
            </button>
            <button
              onClick={onToggleCollapseDesktop}
              className="p-1 rounded-md text-slate-400 hover:text-slate-600 hidden lg:block"
              title={isCollapsedDesktop ? 'Expand sidebar' : 'Collapse sidebar'}
              aria-label={isCollapsedDesktop ? 'Expand sidebar' : 'Collapse sidebar'}
            >
              {isCollapsedDesktop ? (
                <PanelLeft className="w-4 h-4" />
              ) : (
                <PanelLeftClose className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>

        {/* New Chat Button */}
        <div className="p-3">
          <button
            onClick={() => {
              onNewChat();
              onCloseMobile();
            }}
            className={`w-full flex items-center gap-2 px-3 py-2 text-xs font-semibold rounded-lg bg-brand-700 hover:bg-brand-800 text-white shadow-xs transition-colors ${
              isCollapsedDesktop ? 'justify-center px-0' : 'justify-start'
            }`}
            title="New Conversation"
          >
            <Plus className="w-4 h-4 flex-shrink-0" />
            {!isCollapsedDesktop && <span>New Chat</span>}
          </button>
        </div>

        {/* Navigation Options in Exact Specified Order */}
        <div className="px-2 py-1 space-y-0.5 border-b border-slate-200/80">
          {[
            { id: 'analyze-agents', label: 'Analyze Agents', icon: Cpu },
            { id: 'library', label: 'Library', icon: FileText },
            { id: 'scheduled', label: 'Scheduled', icon: Calendar },
            { id: 'other-tools', label: 'Other Tools', icon: Wrench }
          ].map((item) => {
            const Icon = item.icon;
            const isActive = currentView === item.id;
            return (
              <button
                key={item.id}
                onClick={() => {
                  onNavigate(item.id as NavigationDestination);
                  onCloseMobile();
                }}
                className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-brand-900 font-semibold'
                    : 'text-slate-600 hover:bg-slate-200/50 hover:text-slate-900'
                } ${isCollapsedDesktop ? 'justify-center px-0' : 'justify-start'}`}
                title={item.label}
              >
                <Icon
                  className={`w-4 h-4 flex-shrink-0 ${
                    isActive ? 'text-brand-700' : 'text-slate-500'
                  }`}
                />
                {!isCollapsedDesktop && <span className="truncate">{item.label}</span>}
              </button>
            );
          })}
        </div>

        {/* Chat History Section */}
        <div className="flex-1 overflow-y-auto px-2 py-3">
          {!isCollapsedDesktop && (
            <div className="px-2 pb-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              Chat History
            </div>
          )}

          {conversations.length === 0 ? (
            !isCollapsedDesktop && (
              <p className="px-2 py-3 text-[11px] text-slate-400 italic">
                No recent conversations.
              </p>
            )
          ) : (
            <div className="space-y-0.5">
              {conversations.map((conv) => {
                const isSelected = currentView === 'chat' && activeConversationId === conv.id;
                return (
                  <button
                    key={conv.id}
                    onClick={() => {
                      onSelectConversation(conv.id);
                      onNavigate('chat');
                      onCloseMobile();
                    }}
                    className={`w-full flex items-center gap-2 px-2.5 py-2 rounded-lg text-xs text-left transition-colors truncate ${
                      isSelected
                        ? 'bg-blue-50 text-brand-900 font-medium'
                        : 'text-slate-700 hover:bg-slate-200/50'
                    } ${isCollapsedDesktop ? 'justify-center px-0' : 'justify-start'}`}
                    title={conv.title}
                  >
                    <MessageSquare
                      className={`w-3.5 h-3.5 flex-shrink-0 ${
                        isSelected ? 'text-brand-700' : 'text-slate-400'
                      }`}
                    />
                    {!isCollapsedDesktop && (
                      <span className="truncate">{conv.title}</span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Fixed Profile Section at Very Bottom */}
        <div className="mt-auto p-2 border-t border-slate-200/80 bg-[#f8fafc]">
          <button
            onClick={() => {
              onNavigate('profile');
              onCloseMobile();
            }}
            className={`w-full flex items-center gap-2.5 p-2 rounded-lg text-left transition-colors hover:bg-slate-200/50 ${
              currentView === 'profile' ? 'bg-blue-50 text-brand-900' : 'text-slate-700'
            } ${isCollapsedDesktop ? 'justify-center p-1.5' : ''}`}
            title="Faculty Profile & Settings"
          >
            <div className="w-7 h-7 rounded-full bg-brand-900 text-white flex items-center justify-center text-xs font-semibold flex-shrink-0 shadow-2xs">
              FM
            </div>
            {!isCollapsedDesktop && (
              <div className="leading-tight flex-1 min-w-0">
                <span className="text-xs font-semibold text-slate-900 block truncate">
                  Faculty Member
                </span>
                <span className="text-[10px] text-slate-400 block truncate">
                  School of CSE (AI)
                </span>
              </div>
            )}
            {!isCollapsedDesktop && (
              <Settings className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            )}
          </button>
        </div>
      </aside>
    </>
  );
};

