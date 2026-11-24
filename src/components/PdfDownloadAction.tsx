import React, { useState } from 'react';
import { Download, CheckCircle, AlertCircle, Loader2, FileText } from 'lucide-react';
import { apiClient } from '../services/api';

interface PdfDownloadActionProps {
  finalPlanId: string;
  courseTitle: string;
}

export const PdfDownloadAction: React.FC<PdfDownloadActionProps> = ({
  finalPlanId,
  courseTitle
}) => {
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingDocx, setDownloadingDocx] = useState(false);
  const [downloadStatus, setDownloadStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const handleDownloadPdf = async () => {
    setDownloadingPdf(true);
    setDownloadStatus('idle');
    setStatusMessage(null);

    try {
      const result = await apiClient.downloadCoursePlanPdf(finalPlanId);

      if (result.error || !result.blob) {
        setDownloadStatus('error');
        setStatusMessage(result.error || 'Failed to download official PDF course plan.');
        return;
      }

      // Create download link for blob
      const url = window.URL.createObjectURL(result.blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${courseTitle.replace(/[^a-zA-Z0-9_-]/g, '_')}_Curriculum_Plan.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      setDownloadStatus('success');
      setStatusMessage('Institutional PDF downloaded successfully.');
    } catch (err: unknown) {
      setDownloadStatus('error');
      setStatusMessage(err instanceof Error ? err.message : 'PDF export encountered a connection error.');
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleDownloadDocx = async () => {
    setDownloadingDocx(true);
    setDownloadStatus('idle');
    setStatusMessage(null);

    try {
      const result = await apiClient.downloadCoursePlanDocx(finalPlanId);

      if (result.error || !result.blob) {
        setDownloadStatus('error');
        setStatusMessage(result.error || 'Failed to download official Word (.docx) course plan.');
        return;
      }

      const url = window.URL.createObjectURL(result.blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${courseTitle.replace(/[^a-zA-Z0-9_-]/g, '_')}_Curriculum_Plan.docx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      setDownloadStatus('success');
      setStatusMessage('Editable Word (.docx) document downloaded successfully.');
    } catch (err: unknown) {
      setDownloadStatus('error');
      setStatusMessage(err instanceof Error ? err.message : 'DOCX export encountered a connection error.');
    } finally {
      setDownloadingDocx(false);
    }
  };

  return (
    <div className="flex flex-col items-start gap-2">
      <div className="flex flex-wrap items-center gap-2">
        {/* PDF Download Button */}
        <button
          type="button"
          onClick={handleDownloadPdf}
          disabled={downloadingPdf || downloadingDocx}
          className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-white bg-brand-800 hover:bg-brand-900 active:bg-brand-950 rounded-lg shadow-xs transition-colors disabled:opacity-50 cursor-pointer"
        >
          {downloadingPdf ? (
            <>
              <Loader2 className="w-3.5 h-3.5 animate-spin text-white" />
              <span>Generating PDF...</span>
            </>
          ) : (
            <>
              <Download className="w-3.5 h-3.5" />
              <span>Download PDF</span>
            </>
          )}
        </button>

        {/* DOCX Download Button */}
        <button
          type="button"
          onClick={handleDownloadDocx}
          disabled={downloadingPdf || downloadingDocx}
          className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-lg shadow-2xs transition-colors disabled:opacity-50 cursor-pointer"
        >
          {downloadingDocx ? (
            <>
              <Loader2 className="w-3.5 h-3.5 animate-spin text-slate-700" />
              <span>Generating DOCX...</span>
            </>
          ) : (
            <>
              <FileText className="w-3.5 h-3.5 text-slate-600" />
              <span>Download DOCX</span>
            </>
          )}
        </button>
      </div>

      {/* Honest Status Feedback */}
      {downloadStatus === 'error' && statusMessage && (
        <div className="flex items-start gap-1.5 p-2 rounded bg-rose-50 border border-rose-200 text-[11px] text-rose-800 max-w-md">
          <AlertCircle className="w-3.5 h-3.5 text-rose-600 flex-shrink-0 mt-0.5" />
          <span>{statusMessage}</span>
        </div>
      )}

      {downloadStatus === 'success' && statusMessage && (
        <div className="flex items-center gap-1.5 text-[11px] text-emerald-700 font-medium">
          <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
          <span>{statusMessage}</span>
        </div>
      )}
    </div>
  );
};

