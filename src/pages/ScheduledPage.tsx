import React from 'react';
import { Calendar, ArrowLeft, Plus } from 'lucide-react';
import { ScheduledTask } from '../types';

interface ScheduledPageProps {
  tasks?: ScheduledTask[];
  onBackToChat: () => void;
}

export const ScheduledPage: React.FC<ScheduledPageProps> = ({
  tasks = [],
  onBackToChat
}) => {
  return (
    <div className="flex-1 flex flex-col bg-white overflow-y-auto">
      {/* Header */}
      <div className="border-b border-slate-200 px-6 py-4 flex items-center justify-between bg-white">
        <div className="flex items-center gap-3">
          <button
            onClick={onBackToChat}
            className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
            title="Return to Chat"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-lg font-semibold text-slate-900">Scheduled Curriculum Tasks</h1>
            <p className="text-xs text-slate-500">
              Recurring Industry Scans &amp; Automated Syllabus Revisions
            </p>
          </div>
        </div>

        <button
          type="button"
          disabled
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-slate-100 text-slate-400 cursor-not-allowed border border-slate-200"
          title="Scheduled background automation will be activated with Spring Boot cron runners in Phase 2"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Schedule Task</span>
        </button>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-6 max-w-5xl w-full mx-auto">
        {tasks.length === 0 ? (
          <div className="py-20 text-center">
            <div className="w-14 h-14 rounded-2xl bg-blue-50 border border-blue-200 flex items-center justify-center mx-auto mb-4 text-brand-700 shadow-xs">
              <Calendar className="w-7 h-7" />
            </div>
            <h2 className="text-base font-semibold text-slate-900 mb-1">
              No Scheduled Tasks Configured
            </h2>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mb-6 leading-relaxed">
              Recurring automated scans (such as periodic ACM curricula updates and industry job trend tracking) will run here when configured via the backend scheduler.
            </p>
            <button
              onClick={onBackToChat}
              className="px-4 py-2 text-xs font-medium rounded-lg text-white bg-brand-700 hover:bg-brand-800 transition-colors shadow-xs"
            >
              Return to Conversation
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            {tasks.map((task) => (
              <div
                key={task.id}
                className="p-4 rounded-xl border border-slate-200 bg-white flex items-center justify-between"
              >
                <div>
                  <h3 className="text-sm font-semibold text-slate-900">{task.title}</h3>
                  <p className="text-xs text-slate-500">
                    Frequency: {task.frequency} &bull; Department: {task.targetDepartment}
                  </p>
                </div>
                <span className="text-xs font-mono text-slate-600 bg-slate-50 px-2.5 py-1 rounded border border-slate-200">
                  Next: {task.nextRunDate}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

