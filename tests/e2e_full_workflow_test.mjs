// End-to-end test script verifying the full Curriculum Intelligence System pipeline
import fs from 'fs';
import path from 'path';

const BACKEND_BASE = 'http://localhost:8081/api';

async function runFullWorkflowTest() {
  console.log('====================================================');
  console.log('CURRICULUM INTELLIGENCE SYSTEM - FULL END-TO-END RUN');
  console.log('====================================================');

  // STEP 1: Submit genuine course design request
  console.log('\n[STEP 1] Submitting Course Design Request...');
  const requestPayload = {
    rawPrompt: 'Design an undergraduate course on Agentic AI covering multi-agent swarms, tool use, and safety governance.',
    department: 'School of Electronics & Communication Engineering',
    degreeLevel: 'UG'
  };

  const reqResp = await fetch(`${BACKEND_BASE}/curriculum/requests`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': 'usr-default-faculty'
    },
    body: JSON.stringify(requestPayload)
  });

  if (!reqResp.ok) {
    throw new Error(`Failed to submit request: HTTP ${reqResp.status} ${reqResp.statusText}`);
  }

  const reqData = await reqResp.json();
  console.log('  Response status:', reqData.success);
  console.log('  Run ID:', reqData.data?.id);
  const runId = reqData.data?.id;
  if (!runId) throw new Error('No run ID returned!');

  // STEP 2: Poll run until awaiting_human_input or completed
  console.log('\n[STEP 2] Polling AURA Multi-Agent Execution Progress...');
  let attempts = 0;
  let finalRun = null;
  while (attempts < 60) {
    attempts++;
    await new Promise(r => setTimeout(r, 2000));

    const pollResp = await fetch(`${BACKEND_BASE}/curriculum/runs/${encodeURIComponent(runId)}`, {
      headers: { 'X-User-Id': 'usr-default-faculty' }
    });
    if (!pollResp.ok) {
      console.warn(`  Poll attempt ${attempts} warning: HTTP ${pollResp.status}`);
      continue;
    }

    const pollData = await pollResp.json();
    const run = pollData.data;
    console.log(`  [Attempt ${attempts}] Stage: ${run.currentStageId} | Status: ${run.status} | Elapsed: ${run.elapsedSeconds}s`);

    if (run.status === 'awaiting_human_input' || run.status === 'completed') {
      finalRun = run;
      break;
    }
  }

  if (!finalRun) {
    throw new Error('AURA run timed out before reaching awaiting_human_input / completed state');
  }

  console.log('  AURA run completed with status:', finalRun.status);
  console.log('  Audit Trail ID:', finalRun.auditTrailId);

  // STEP 3: Retrieve 4 Course-Plan Alternatives
  console.log('\n[STEP 3] Retrieving 4 Course-Plan Alternatives...');
  const altsResp = await fetch(`${BACKEND_BASE}/curriculum/runs/${encodeURIComponent(runId)}/alternatives`, {
    headers: { 'X-User-Id': 'usr-default-faculty' }
  });
  if (!altsResp.ok) throw new Error(`Failed to fetch alternatives: HTTP ${altsResp.status}`);
  const altsData = await altsResp.json();
  const alternatives = altsData.data || [];
  console.log(`  Retrieved ${alternatives.length} alternatives:`);
  alternatives.forEach((alt, idx) => {
    console.log(`    ${idx + 1}. [${alt.id}] "${alt.title}" (${alt.curriculumPhilosophy.slice(0, 60)}...)`);
  });

  if (alternatives.length !== 4) {
    throw new Error(`Expected exactly 4 distinct alternatives, got ${alternatives.length}`);
  }

  // STEP 4: Submit Faculty Selection & Feedback
  console.log('\n[STEP 4] Submitting Faculty Selection & Feedback...');
  const selectedAlt = alternatives[2] || alternatives[0]; // Choose Balanced Standard
  console.log(`  Selected Proposal: "${selectedAlt.title}" (ID: ${selectedAlt.id})`);

  const feedbackPayload = {
    runId: runId,
    selectedAlternativeId: selectedAlt.id,
    facultyRole: 'Associate Professor',
    facultyRemarks: 'Approved for institutional deployment. Strongly aligns with autonomous multi-agent engineering requirements.',
    rating: 5
  };

  const fbResp = await fetch(`${BACKEND_BASE}/curriculum/feedback`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': 'usr-default-faculty'
    },
    body: JSON.stringify(feedbackPayload)
  });
  if (!fbResp.ok) throw new Error(`Feedback submission failed: HTTP ${fbResp.status}`);
  const fbData = await fbResp.json();
  console.log('  Feedback commit status:', fbData.success);
  console.log('  Final Plan ID:', fbData.data?.finalPlanId);
  const finalPlanId = fbData.data?.finalPlanId;
  if (!finalPlanId) throw new Error('No finalPlanId returned from feedback endpoint');

  // STEP 5: Retrieve Final Assembled Course Plan
  console.log('\n[STEP 5] Retrieving Final Assembled Course Plan...');
  const planResp = await fetch(`${BACKEND_BASE}/curriculum/plans/${encodeURIComponent(finalPlanId)}`, {
    headers: { 'X-User-Id': 'usr-default-faculty' }
  });
  if (!planResp.ok) throw new Error(`Failed to retrieve final plan: HTTP ${planResp.status}`);
  const planData = await planResp.json();
  const plan = planData.data;
  console.log('  Course Code:', plan.courseCode);
  console.log('  Course Title:', plan.courseTitle);
  console.log('  Department:', plan.department);
  console.log('  Credits:', plan.credits);
  console.log('  Modules count:', plan.modules?.length);
  console.log('  Course Outcomes count:', plan.courseOutcomes?.length);
  console.log('  Evidence References count:', plan.evidenceReferences?.length);

  // STEP 6: Download and Verify DOCX Document
  console.log('\n[STEP 6] Downloading and Verifying DOCX Document...');
  const docxResp = await fetch(`${BACKEND_BASE}/curriculum/plans/${encodeURIComponent(finalPlanId)}/docx`, {
    headers: { 'X-User-Id': 'usr-default-faculty' }
  });
  if (!docxResp.ok) throw new Error(`DOCX download failed: HTTP ${docxResp.status}`);
  const docxBuffer = Buffer.from(await docxResp.arrayBuffer());
  console.log('  DOCX Download Size:', docxBuffer.length, 'bytes');
  console.log('  DOCX Content-Type:', docxResp.headers.get('content-type'));
  if (docxBuffer.length < 50000) {
    throw new Error('DOCX file is smaller than expected master template size');
  }

  // STEP 7: Download and Verify PDF Document
  console.log('\n[STEP 7] Downloading and Verifying PDF Document...');
  const pdfResp = await fetch(`${BACKEND_BASE}/curriculum/plans/${encodeURIComponent(finalPlanId)}/pdf`, {
    headers: { 'X-User-Id': 'usr-default-faculty' }
  });
  if (!pdfResp.ok) throw new Error(`PDF download failed: HTTP ${pdfResp.status}`);
  const pdfBuffer = Buffer.from(await pdfResp.arrayBuffer());
  console.log('  PDF Download Size:', pdfBuffer.length, 'bytes');
  console.log('  PDF Content-Type:', pdfResp.headers.get('content-type'));
  const pdfHeader = pdfBuffer.slice(0, 5).toString('utf-8');
  console.log('  PDF Signature Header:', pdfHeader);
  if (!pdfHeader.startsWith('%PDF-')) {
    throw new Error('Downloaded file does not have valid %PDF- magic signature');
  }

  console.log('\n====================================================');
  console.log('ALL WORKFLOW & DOCUMENT GENERATION STAGES PASSED!');
  console.log('====================================================\n');
}

runFullWorkflowTest().catch(err => {
  console.error('\nWorkflow Test Failure:', err);
  process.exit(1);
});

