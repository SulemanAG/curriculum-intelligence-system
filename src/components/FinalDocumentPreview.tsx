import React, { useState } from 'react';
import {
  ShieldCheck,
  CheckCircle2,
  Printer,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import { FinalCoursePlan } from '../types';
import { PdfDownloadAction } from './PdfDownloadAction';
import { EvidenceReferenceList } from './EvidenceReferenceList';

interface FinalDocumentPreviewProps {
  plan: FinalCoursePlan;
  onReset?: () => void;
}

export const FinalDocumentPreview: React.FC<FinalDocumentPreviewProps> = ({ plan, onReset }) => {
  const [showEvidence, setShowEvidence] = useState(false);

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="w-full max-w-6xl py-2 px-0">
      {/* Top Action & Governance Banner */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-flex items-center gap-1 text-xs font-semibold uppercase tracking-wider text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
              <span>Governed Publication (Version {plan.version})</span>
            </span>
            <span className="text-xs text-slate-500 font-mono">
              Audit ID: {plan.auditTrailId.slice(0, 12)}
            </span>
          </div>
          <h1 className="text-xl font-bold text-slate-900">
            Institutional Course Plan: {plan.courseTitle}
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Prepared for KLE Technological University &bull; {plan.department} &bull; Semester {plan.semester}
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={handlePrint}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-md transition-colors"
          >
            <Printer className="w-3.5 h-3.5 text-slate-500" />
            <span>Print View</span>
          </button>

          <PdfDownloadAction
            finalPlanId={plan.id}
            courseTitle={plan.courseTitle}
          />
        </div>
      </div>

      {/* Institutional Document Sheet */}
      <div className="bg-white rounded-xl border border-slate-300 shadow-sm p-6 sm:p-10 text-slate-900 space-y-8 print:shadow-none print:border-none print:p-0">
        {/* Document Header Template */}
        <div className="border-b-2 border-slate-900 pb-5 text-center">
          <p className="text-xs font-bold uppercase tracking-widest text-slate-600">
            KLE Technological University, Belagavi
          </p>
          <p className="text-xs font-medium text-slate-500 mt-0.5">
            School of Computer Science &amp; Engineering (AI)
          </p>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 mt-3">
            COURSE INSTRUCTION PLAN
          </h2>
          <div className="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-2 text-left text-xs bg-slate-50 p-3 rounded-lg border border-slate-200 font-medium">
            <div>
              <span className="text-slate-400 block font-normal text-[10px]">COURSE CODE</span>
              <span className="font-mono text-slate-900">{plan.courseCode}</span>
            </div>
            <div>
              <span className="text-slate-400 block font-normal text-[10px]">CREDITS</span>
              <span className="text-slate-900">{plan.credits} Credits</span>
            </div>
            <div>
              <span className="text-slate-400 block font-normal text-[10px]">CONTACT HOURS</span>
              <span className="text-slate-900">
                {plan.contactHours.total} hrs (L:{plan.contactHours.lecture} T:{plan.contactHours.tutorial} P:{plan.contactHours.practical})
              </span>
            </div>
            <div>
              <span className="text-slate-400 block font-normal text-[10px]">GOVERNANCE STATUS</span>
              <span className="text-emerald-700 font-semibold capitalize">{plan.approvalStatus}</span>
            </div>
          </div>
        </div>

        {/* 1. Course Description & Prerequisites */}
        <div>
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-900 mb-2 border-b border-slate-200 pb-1">
            1. Course Description &amp; Scope
          </h3>
          <p className="text-xs leading-relaxed text-slate-700">
            {plan.courseDescription}
          </p>
          {plan.prerequisites && plan.prerequisites.length > 0 && (
            <div className="mt-2 text-xs text-slate-600">
              <strong className="text-slate-800">Prerequisites: </strong>
              <span>{plan.prerequisites.join(', ')}</span>
            </div>
          )}
        </div>

        {/* 2. Course Outcomes (COs) */}
        <div>
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-900 mb-2 border-b border-slate-200 pb-1 flex items-center justify-between">
            <span>2. Course Outcomes (COs)</span>
            <span className="text-[11px] font-normal lowercase text-slate-500">
              Bloom&apos;s Taxonomy &amp; PO Mapping
            </span>
          </h3>
          <div className="border border-slate-200 rounded-md overflow-hidden">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold">
                <tr>
                  <th className="py-2 px-3 w-16">CO</th>
                  <th className="py-2 px-3">Outcome Statement</th>
                  <th className="py-2 px-3 w-28">Bloom&apos;s Level</th>
                  <th className="py-2 px-3 w-24">PO Mapping</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {plan.courseOutcomes.map((co) => (
                  <tr key={co.code}>
                    <td className="py-2 px-3 font-mono font-semibold text-brand-900">{co.code}</td>
                    <td className="py-2 px-3 text-slate-700">{co.statement}</td>
                    <td className="py-2 px-3 font-medium text-slate-600">{co.bloomsTaxonomyLevel}</td>
                    <td className="py-2 px-3 font-mono text-[11px] text-slate-500">{co.poMapping.join(', ')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* 3. Detailed Modular Syllabus */}
        <div>
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-900 mb-2 border-b border-slate-200 pb-1">
            3. Modular Syllabus Structure
          </h3>
          <div className="space-y-4">
            {plan.modules.map((module) => (
              <div key={module.moduleNumber} className="border border-slate-200 rounded-md p-3.5 text-xs">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-bold text-slate-900">
                    Unit {module.moduleNumber}: {module.title}
                  </span>
                  <span className="font-mono text-slate-500 text-[11px]">
                    {module.suggestedHours} Contact Hours
                  </span>
                </div>
                <p className="text-slate-700 mb-2 leading-relaxed">
                  <strong className="text-slate-800">Topics: </strong>
                  {module.topics.join(' &bull; ')}
                </p>
                {module.learningOutcomes && module.learningOutcomes.length > 0 && (
                  <p className="text-[11px] text-slate-500">
                    <strong>Learning Outcomes: </strong>
                    {module.learningOutcomes.join('; ')}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* 4. Assessment Structure */}
        <div>
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-900 mb-2 border-b border-slate-200 pb-1">
            4. Evaluation &amp; Assessment Scheme
          </h3>
          <p className="text-xs text-slate-600 mb-3">
            {plan.assessmentScheme?.ciePercentage !== undefined && plan.assessmentScheme?.seePercentage !== undefined ? (
              <>Continuous Internal Evaluation (CIE): <strong>{plan.assessmentScheme.ciePercentage}%</strong> &bull; Semester End Examination (SEE): <strong>{plan.assessmentScheme.seePercentage}%</strong></>
            ) : (
              <span>Institutional Evaluation Framework &bull; Component weightages defined below (Subject to Board of Studies guidelines)</span>
            )}
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 text-xs">
            {plan.assessmentScheme.components.map((comp, idx) => (
              <div key={idx} className="p-2.5 rounded bg-slate-50 border border-slate-200">
                <div className="flex items-center justify-between font-semibold text-slate-900">
                  <span>{comp.name}</span>
                  <span className="font-mono text-brand-700">{comp.weightage}%</span>
                </div>
                <p className="text-[11px] text-slate-500 mt-1">{comp.description}</p>
              </div>
            ))}
          </div>
        </div>

        {/* 5. Textbooks & Reference Materials */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 mb-2">
              Prescribed Textbooks
            </h4>
            <ul className="list-disc list-inside text-xs text-slate-700 space-y-1">
              {plan.prescribedTextbooks.map((book, idx) => (
                <li key={idx} className="leading-relaxed">{book}</li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-800 mb-2">
              Reference Materials &amp; Standards
            </h4>
            <ul className="list-disc list-inside text-xs text-slate-700 space-y-1">
              {plan.referenceMaterials.map((ref, idx) => (
                <li key={idx} className="leading-relaxed">{ref}</li>
              ))}
            </ul>
          </div>
        </div>

        {/* 6. Evidence Grounding & Verification Toggle */}
        <div className="pt-4 border-t border-slate-200">
          <button
            type="button"
            onClick={() => setShowEvidence(!showEvidence)}
            className="w-full flex items-center justify-between p-3 rounded-lg bg-slate-50 hover:bg-slate-100 border border-slate-200 text-xs font-semibold text-slate-800 transition-colors"
          >
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Evidence Grounding &amp; Institutional Audit Trail ({plan.evidenceReferences?.length || 0} Citations)</span>
            </div>
            {showEvidence ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>

          {showEvidence && (
            <div className="mt-4 pl-1">
              <EvidenceReferenceList
                references={plan.evidenceReferences || []}
                title="Grounding Citations for Final Approved Course Plan"
              />
            </div>
          )}
        </div>

        {/* Institutional Review & Sign-off Block (Configurable; pending official institutional template) */}
        <div className="pt-8 border-t border-slate-300">
          <div className="text-[11px] text-slate-400 italic mb-4 text-center">
            Institutional Approval Workflow (Configurable &mdash; to be finalized upon delivery of KLE Tech BoS syllabus specification)
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center text-xs text-slate-500">
            <div className="border-t border-slate-300 pt-2">
              <span className="font-medium text-slate-700 block">Course In-charge / Drafter</span>
              <span className="text-[10px] text-slate-400">Curriculum Drafting</span>
            </div>
            <div className="border-t border-slate-300 pt-2">
              <span className="font-medium text-slate-700 block">Department DUGC / Committee</span>
              <span className="text-[10px] text-slate-400">Academic Review</span>
            </div>
            <div className="border-t border-slate-300 pt-2">
              <span className="font-medium text-slate-700 block">Head of Department (HoD)</span>
              <span className="text-[10px] text-slate-400">School of CSE (AI)</span>
            </div>
            <div className="border-t border-slate-300 pt-2">
              <span className="font-medium text-slate-700 block">Dean (Academics) / BoS</span>
              <span className="text-[10px] text-slate-400">Board of Studies Sign-off</span>
            </div>
          </div>
        </div>
      </div>

      {/* New Course Plan Restart */}
      {onReset && (
        <div className="mt-6 text-center">
          <button
            type="button"
            onClick={onReset}
            className="text-xs font-medium text-slate-500 hover:text-slate-800 hover:underline"
          >
            &larr; Design another course curriculum
          </button>
        </div>
      )}
    </div>
  );
};
