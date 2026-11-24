import { spawn } from 'child_process';
import puppeteer from 'puppeteer-core';
import fs from 'fs';
import path from 'path';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const ARTIFACT_DIR = 'C:\\Users\\Suleman Agasimani\\.gemini\\antigravity\\brain\\b0b21682-ebc1-4597-89f6-73da05bd86fb';
const LOCAL_SCREENSHOT_DIR = path.resolve('./screenshots');

if (!fs.existsSync(LOCAL_SCREENSHOT_DIR)) {
  fs.mkdirSync(LOCAL_SCREENSHOT_DIR, { recursive: true });
}

async function runQA() {
  console.log('--- STARTING FRONTEND QA & BROWSER VERIFICATION ---');
  
  // 1. Start Vite development server
  console.log('Starting Vite development server...');
  const viteProcess = spawn('npx.cmd', ['vite', '--port', '5173', '--host'], {
    stdio: 'pipe',
    shell: true
  });

  // Wait 4 seconds for Vite server to become ready
  await new Promise((r) => setTimeout(r, 4000));

  const USER_DATA_DIR = path.resolve('./.chrome-profile');
  if (fs.existsSync(USER_DATA_DIR)) {
    try { fs.rmSync(USER_DATA_DIR, { recursive: true, force: true }); } catch {}
  }
  fs.mkdirSync(USER_DATA_DIR, { recursive: true });

  console.log('Connecting headless Chrome via puppeteer-core...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    userDataDir: USER_DATA_DIR,
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-gpu',
      '--disable-dev-shm-usage',
      '--remote-debugging-port=9222'
    ]
  });

  const consoleLogs = [];
  const errors = [];

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 850 });

  page.on('console', (msg) => {
    consoleLogs.push(`[${msg.type()}] ${msg.text()}`);
  });
  page.on('pageerror', (err) => {
    errors.push(err.toString());
    console.error('Browser Page Error:', err.message);
  });

  const saveScreenshot = async (name) => {
    const localPath = path.join(LOCAL_SCREENSHOT_DIR, name);
    const artifactPath = path.join(ARTIFACT_DIR, name);
    await page.screenshot({ path: localPath, fullPage: false });
    fs.copyFileSync(localPath, artifactPath);
    console.log(`Saved screenshot: ${name}`);
  };

  const clickByText = async (text, tag = 'button') => {
    return page.evaluate(({ text, tag }) => {
      const elements = Array.from(document.querySelectorAll(tag));
      const el = elements.find((e) => e.textContent && e.textContent.includes(text));
      if (el) {
        el.click();
        return true;
      }
      return false;
    }, { text, tag });
  };

  try {
    // Test 1: Landing Page
    console.log('Testing 1: Landing Page at http://localhost:5173 ...');
    await page.goto('http://localhost:5173', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await new Promise((r) => setTimeout(r, 1500));
    await saveScreenshot('01_landing_page.png');

    // Test 2: Clarification Dialog (Lab 1)
    console.log('Testing 2: Clarification Dialog (Lab 1)...');
    const clarified = await clickByText('Clarify Constraints');
    if (clarified) {
      await new Promise((r) => setTimeout(r, 600));
      await saveScreenshot('02_clarification_dialog.png');
      // Cancel dialog to return
      await clickByText('Back');
      await new Promise((r) => setTimeout(r, 400));
    }

    // Test 3: Natural Language Request Submission & Research Progress
    console.log('Testing 3: Submitting Course Request...');
    const textarea = await page.$('textarea#course-request-input');
    if (textarea) {
      await textarea.type('I need to design the course plan for Computer Vision.');
      await page.keyboard.press('Enter');
    }

    // Capture Research Progress (Parallel Swarm Stage)
    await new Promise((r) => setTimeout(r, 1800));
    await saveScreenshot('03_research_progress_parallel_swarm.png');

    // Wait for the research progress to advance to human review gate
    await new Promise((r) => setTimeout(r, 4000));
    await saveScreenshot('04_research_progress_completed.png');

    // Test 4: Four Alternatives Comparison Grid
    console.log('Testing 4: Inspecting Development Schema Proposals...');
    const loadedSchema = await clickByText('Load Schema Preview');
    if (!loadedSchema) {
      await clickByText('Inspect Development Schema Preview');
    }
    await new Promise((r) => setTimeout(r, 800));
    await saveScreenshot('05_four_alternatives_grid.png');

    // Test 5: Side-by-Side Comparison Matrix
    console.log('Testing 5: Toggling Matrix View...');
    const matrixBtn = await page.$('button[title="Side-by-Side Matrix View"]');
    if (matrixBtn) {
      await matrixBtn.click();
      await new Promise((r) => setTimeout(r, 600));
      await saveScreenshot('06_alternatives_comparison_matrix.png');
      // Toggle back to cards
      const cardsBtn = await page.$('button[title="Card Grid View"]');
      if (cardsBtn) await cardsBtn.click();
      await new Promise((r) => setTimeout(r, 400));
    }

    // Test 6: Course Plan Details Modal & Evidence References
    console.log('Testing 6: Inspecting Course Plan Details Modal...');
    const clickedInspect = await clickByText('Inspect Details');
    if (clickedInspect) {
      await new Promise((r) => setTimeout(r, 800));
      await saveScreenshot('07_course_plan_details_modal.png');

      // Scroll modal to show Course Outcomes & Modules & Evidence
      await page.evaluate(() => {
        const dialogBody = document.querySelector('div[role="dialog"] div.overflow-y-auto');
        if (dialogBody) dialogBody.scrollTop = 500;
      });
      await new Promise((r) => setTimeout(r, 600));
      await saveScreenshot('08_course_plan_modules_and_evidence.png');

      // Close modal
      const closeBtn = await page.$('button[aria-label="Close details"]');
      if (closeBtn) await closeBtn.click();
      await new Promise((r) => setTimeout(r, 400));
    }

    // Test 7: Feedback Form (Human Gate - Lab 12)
    console.log('Testing 7: Recording Preference & Feedback...');
    const recordPref = await clickByText('Record Preference');
    if (recordPref) {
      await new Promise((r) => setTimeout(r, 600));
      await saveScreenshot('09_feedback_form.png');

      // Fill in feedback form and submit
      const nameInput = await page.$('#faculty-name');
      if (nameInput) await nameInput.type('Dr. Faculty Member');
      const rationaleInput = await page.$('#rationale-input');
      if (rationaleInput) await rationaleInput.type('Selected for ideal balance of theory and hands-on laboratory.');

      await clickByText('Confirm Selection');
      await new Promise((r) => setTimeout(r, 1200));
    }

    // Test 8: Final Document Preview & PDF Download
    console.log('Testing 8: Final Document Preview...');
    await saveScreenshot('10_final_document_preview.png');

    // Test 9: PDF Download action honest error reporting
    console.log('Testing 9: PDF Download button interaction...');
    const clickedPdf = await clickByText('Download Course Plan (PDF)');
    if (clickedPdf) {
      await new Promise((r) => setTimeout(r, 1000));
      await saveScreenshot('11_pdf_download_honest_status.png');
    }

    // Test 10: Mobile Responsive View
    console.log('Testing 10: Mobile Responsive View (375x812)...');
    await page.setViewport({ width: 375, height: 812, isMobile: true });
    // Go to landing page
    const resetBtn = await page.$('button[aria-label="Curriculum Intelligence Home"]');
    if (resetBtn) await resetBtn.click();
    await new Promise((r) => setTimeout(r, 800));
    await saveScreenshot('12_mobile_responsive_view.png');

  } catch (err) {
    console.error('QA Test execution failed:', err);
  } finally {
    try { await browser.close(); } catch {}
    try { viteProcess.kill(); } catch {}
    console.log('Browser and Vite server cleanly shut down.');
  }

  console.log('\n--- QA CONSOLE AUDIT ---');
  console.log(`Page Errors: ${errors.length}`);
  errors.forEach((e) => console.log('ERROR:', e));
  console.log(`Console Logs Captured: ${consoleLogs.length}`);
  consoleLogs.slice(0, 15).forEach((l) => console.log(l));

  fs.writeFileSync(
    './qa_audit_log.json',
    JSON.stringify({ errors, consoleLogs, timestamp: new Date().toISOString() }, null, 2)
  );
  console.log('QA audit log written to qa_audit_log.json');
}

runQA();
