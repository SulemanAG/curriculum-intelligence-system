import React, { useState } from 'react';
import { Download, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import { apiClient } from '../services/api';

interface PdfDownloadActionProps {
  finalPlanId: string;
  courseTitle: string;
}

export const PdfDownloadAction: React.FC<PdfDownloadActionProps> = ({
  finalPlanId,
  courseTitle
}) => {
  const [downloading, setDownloading] = useState(false);
  const [downloadStatus, setDownloadStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleDownload = async () => {
    setDownloading(true);
    setDownloadStatus('idle');
    setErrorMessage(null);

    try {
      const result = await apiClient.downloadCoursePlanPdf(finalPlanId);

      if (result.error || !result.blob) {
        setDownloadStatus('error');
        setErrorMessage(
          `PDF export service offline at ${apiClient.getBaseUrl()}. Institutional PDF generation will be provided by the Phase 2 Spring Boot backend.`
        );
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
    } catch {
      setDownloadStatus('error');
      setErrorMessage(
        `PDF export service offline at ${apiClient.getBaseUrl()}. Institutional PDF generation will be provided by the Phase 2 Spring Boot backend.`
      );
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="flex flex-col items-start gap-2">
      <button
        type="button"
        onClick={handleDownload}
        disabled={downloading}
        className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-brand-800 hover:bg-brand-900 active:bg-brand-950 rounded-md shadow-xs transition-colors disabled:opacity-50"
      >
        {downloading ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin text-white" />
            <span>Generating Institutional PDF...</span>
          </>
        ) : (
          <>
            <Download className="w-4 h-4" />
            <span>Download Course Plan (PDF)</span>
          </>
        )}
      </button>

      {/* Honest Status Feedback */}
      {downloadStatus === 'error' && errorMessage && (
        <div className="flex items-start gap-1.5 p-2 rounded bg-rose-50 border border-rose-200 text-[11px] text-rose-800 max-w-md">
          <AlertCircle className="w-3.5 h-3.5 text-rose-600 flex-shrink-0 mt-0.5" />
          <span>{errorMessage}</span>
        </div>
      )}

      {downloadStatus === 'success' && (
        <div className="flex items-center gap-1.5 text-[11px] text-emerald-700 font-medium">
          <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
          <span>Institutional PDF exported successfully.</span>
        </div>
      )}
    </div>
  );
};

