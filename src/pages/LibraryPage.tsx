import React, { useState } from 'react';
import { FileText, ArrowLeft, Download, ExternalLink, Search } from 'lucide-react';
import { LibraryDocument } from '../types';

interface LibraryPageProps {
  documents?: LibraryDocument[];
  onBackToChat: () => void;
}

export const LibraryPage: React.FC<LibraryPageProps> = ({
  documents = [],
  onBackToChat
}) => {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredDocs = documents.filter((doc) =>
    doc.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    doc.courseTitle.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 flex flex-col bg-white overflow-y-auto">
      {/* Header */}
      <div className="border-b border-slate-200 px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white">
        <div className="flex items-center gap-3">
          <button
            onClick={onBackToChat}
            className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
            title="Return to Chat"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-lg font-semibold text-slate-900">Document Library</h1>
            <p className="text-xs text-slate-500">
              Approved Course Plans &amp; Institutional Curriculum PDFs
            </p>
          </div>
        </div>

        {/* Search */}
        {documents.length > 0 && (
          <div className="relative w-full sm:w-64">
            <Search className="w-4 h-4 text-slate-400 absolute left-2.5 top-2.5" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Filter course plans..."
              className="w-full text-xs pl-8 pr-3 py-1.5 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 bg-slate-50 focus:bg-white"
            />
          </div>
        )}
      </div>

      {/* Main Content */}
      <div className="flex-1 p-6 max-w-5xl w-full mx-auto">
        {documents.length === 0 ? (
          <div className="py-20 text-center">
            <div className="w-14 h-14 rounded-2xl bg-blue-50 border border-blue-200 flex items-center justify-center mx-auto mb-4 text-brand-700 shadow-xs">
              <FileText className="w-7 h-7" />
            </div>
            <h2 className="text-base font-semibold text-slate-900 mb-1">
              No Generated Documents in Library
            </h2>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mb-6">
              When you finalize course plans in your conversations and generate institutional documents, they will be archived here for download and review.
            </p>
            <button
              onClick={onBackToChat}
              className="px-4 py-2 text-xs font-medium rounded-lg text-white bg-brand-700 hover:bg-brand-800 transition-colors shadow-xs"
            >
              Start New Curriculum Inquiry
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
              {filteredDocs.map((doc) => (
                <div
                  key={doc.id}
                  className="p-4 rounded-xl border border-slate-200 bg-white hover:border-slate-300 transition-all flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between text-[11px] text-slate-400 mb-1.5">
                      <span className="font-mono">{doc.department}</span>
                      <span>{new Date(doc.generatedDate).toLocaleDateString()}</span>
                    </div>
                    <h3 className="text-sm font-semibold text-slate-900 leading-snug">
                      {doc.title}
                    </h3>
                    <p className="text-xs text-slate-500 mt-0.5">{doc.courseTitle}</p>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-[11px] text-slate-400">
                      {doc.fileSize || 'PDF Document'}
                    </span>
                    <div className="flex items-center gap-2">
                      {doc.downloadUrl && (
                        <a
                          href={doc.downloadUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded border border-slate-300 text-slate-700 hover:bg-slate-50"
                        >
                          <ExternalLink className="w-3 h-3" />
                          <span>View</span>
                        </a>
                      )}
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 px-3 py-1 text-xs font-medium rounded bg-brand-700 text-white hover:bg-brand-800"
                      >
                        <Download className="w-3 h-3" />
                        <span>Download</span>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

