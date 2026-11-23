import React from 'react';
import { ExternalLink, CheckCircle, AlertCircle, FileText } from 'lucide-react';
import { EvidenceReference } from '../types';

interface EvidenceReferenceCardProps {
  reference: EvidenceReference;
}

export const EvidenceReferenceCard: React.FC<EvidenceReferenceCardProps> = ({ reference }) => {
  const getSourceTypeLabel = (type: string) => {
    switch (type) {
      case 'acm_standard':
        return 'ACM Curricula Standard';
      case 'apni_guideline':
        return 'APNI National Guideline';
      case 'iit_nit_curriculum':
        return 'Institutional Syllabus';
      case 'industry_report':
        return 'Industry Skill Report';
      case 'job_posting':
        return 'Industry Role Demand';
      case 'alumni_feedback':
        return 'Alumni / Partner Feedback';
      default:
        return 'Verified Academic Source';
    }
  };

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-3.5 hover:border-slate-300 transition-all text-xs">
      <div className="flex items-start justify-between gap-2">
        <div>
          <div className="flex items-center gap-1.5 flex-wrap mb-1">
            <span className="font-semibold text-slate-800">
              {reference.institutionOrPublisher}
            </span>
            <span className="text-[10px] px-1.5 py-0.2 rounded bg-slate-100 text-slate-600 border border-slate-200">
              {getSourceTypeLabel(reference.sourceType)}
            </span>
            {reference.isVerified ? (
              <span className="inline-flex items-center gap-0.5 text-[10px] text-emerald-700 font-medium">
                <CheckCircle className="w-3 h-3 text-emerald-600" />
                <span>Verified Source</span>
              </span>
            ) : (
              <span className="inline-flex items-center gap-0.5 text-[10px] text-amber-700 font-medium">
                <AlertCircle className="w-3 h-3 text-amber-600" />
                <span>Pending Verification</span>
              </span>
            )}
          </div>
          <h4 className="font-medium text-slate-900 text-xs leading-snug">
            {reference.title}
          </h4>
        </div>

        {reference.originalUrl && (
          <a
            href={reference.originalUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="p-1 rounded text-brand-700 hover:text-brand-900 hover:bg-brand-50 transition-colors flex-shrink-0"
            title="Open original source URL"
            aria-label={`Open source: ${reference.title}`}
          >
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        )}
      </div>

      {/* Clause or Specific Target Reference */}
      {reference.clauseReference && (
        <div className="mt-1.5 font-mono text-[11px] text-slate-500 bg-slate-50 px-2 py-0.5 rounded border border-slate-200/60 inline-block">
          Clause: {reference.clauseReference}
        </div>
      )}

      {/* Grounded Finding */}
      <div className="mt-2 text-slate-600 bg-slate-50/70 p-2 rounded border border-slate-200/50">
        <span className="font-semibold text-slate-700 block mb-0.5">Supported Finding:</span>
        <p className="leading-relaxed">{reference.supportedFinding}</p>
      </div>

      {/* Metadata Footnote */}
      <div className="mt-2 pt-2 border-t border-slate-100 flex items-center justify-between text-[10px] text-slate-400">
        <div className="flex items-center gap-1">
          <FileText className="w-3 h-3 text-slate-300" />
          <span>Retrieved: {new Date(reference.retrievalTimestamp).toLocaleDateString()}</span>
        </div>
        {reference.publicationDate && (
          <span>Pub: {reference.publicationDate}</span>
        )}
      </div>
    </div>
  );
};

