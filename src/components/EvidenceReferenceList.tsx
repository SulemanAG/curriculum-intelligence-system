import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { EvidenceReference } from '../types';
import { EvidenceReferenceCard } from './EvidenceReferenceCard';

interface EvidenceReferenceListProps {
  references: EvidenceReference[];
  title?: string;
  emptyMessage?: string;
}

export const EvidenceReferenceList: React.FC<EvidenceReferenceListProps> = ({
  references,
  title = 'Verified Research Evidence & Source Transparency',
  emptyMessage = 'No source references attached to this section.'
}) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
        </div>
        <span className="text-xs text-slate-500 font-mono">
          {references.length} {references.length === 1 ? 'citation' : 'citations'}
        </span>
      </div>

      {references.length === 0 ? (
        <p className="text-xs text-slate-400 italic p-3 bg-slate-50 rounded border border-slate-200">
          {emptyMessage}
        </p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {references.map((ref) => (
            <EvidenceReferenceCard key={ref.id} reference={ref} />
          ))}
        </div>
      )}
    </div>
  );
};

