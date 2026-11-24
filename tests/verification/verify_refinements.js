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

async function runRefinementVerification() {
  console.log('--- STARTING PHASE 1.3 LAYOUT REFINEMENT VERIFICATION ---');

  // 1. Start Vite preview / dev server
  console.log('Starting Vite server on port 5173...');
  const viteProcess = spawn('npx.cmd', ['vite', '--port', '5173', '--host'], {
    stdio: 'pipe',
    shell: true
  });

  await new Promise((r) => setTimeout(r, 3500));

  const USER_DATA_DIR = path.resolve('./.chrome-profile-refine');
  if (fs.existsSync(USER_DATA_DIR)) {
    try { fs.rmSync(USER_DATA_DIR, { recursive: true, force: true }); } catch {}
  }
  fs.mkdirSync(USER_DATA_DIR, { recursive: true });

  console.log('Launching headless Chrome...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    userDataDir: USER_DATA_DIR,
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-gpu',
      '--disable-dev-shm-usage'
    ]
  });

  const page = await browser.newPage();
  const logs = [];
  const errors = [];

  page.on('console', (msg) => logs.push(`[${msg.type()}] ${msg.text()}`));
  page.on('pageerror', (err) => {
    errors.push(err.toString());
    console.error('Page error:', err.message);
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
    // 1. Inspect Landing Page at 1920x1080
    console.log('Checking Landing Page at 1920x1080...');
    await page.setViewport({ width: 1920, height: 1080 });
    await page.goto('http://localhost:5173', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await new Promise((r) => setTimeout(r, 1200));
    await saveScreenshot('refine_01_landing_1920.png');

    // 2. Submit a request to start conversation
    console.log('Submitting Course Request...');
    const textarea = await page.$('textarea');
    if (textarea) {
      await textarea.type('I need to design a course plan for Computer Vision.');
      await page.keyboard.press('Enter');
    }
    await new Promise((r) => setTimeout(r, 1000));

    // 3. Load Schema Preview to display all 4 proposals
    console.log('Loading Schema Preview proposals...');
    await clickByText('Inspect Development Schema Preview');
    await new Promise((r) => setTimeout(r, 1000));

    // Check at 1920px: 4 columns in one row
    console.log('Capturing 1920x1080 alternatives in one row...');
    await saveScreenshot('refine_02_alternatives_1920_row.png');

    // Scroll conversation container to bottom and verify composer does not overlap
    console.log('Checking scroll container and composer clearance at bottom...');
    await page.evaluate(() => {
      const scrollArea = document.querySelector('main');
      if (scrollArea) scrollArea.scrollTop = scrollArea.scrollHeight;
    });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_03_alternatives_scrolled_to_bottom.png');

    // 4. Test Viewport 1440x900
    console.log('Checking 1440x900 desktop viewport...');
    await page.setViewport({ width: 1440, height: 900 });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_04_alternatives_1440.png');

    // 5. Test Viewport 1280x850
    console.log('Checking 1280x850 desktop viewport...');
    await page.setViewport({ width: 1280, height: 850 });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_05_alternatives_1280.png');

    // 6. Test Card Selection (Verify no jump or shift)
    console.log('Testing card selection (ALT-2)...');
    await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      const selectBtn = buttons.find((b) => b.textContent && b.textContent.includes('Select Plan'));
      if (selectBtn) selectBtn.click();
    });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_06_card_selected_no_shift.png');

    // 7. Test Tablet Viewport (768x1024) - should be 2 columns
    console.log('Checking 768x1024 tablet viewport (2 columns)...');
    await page.setViewport({ width: 768, height: 1024 });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_07_tablet_2_columns.png');

    // 8. Test Mobile Viewport (375x812) - should be 1 column
    console.log('Checking 375x812 mobile viewport (1 column)...');
    await page.setViewport({ width: 375, height: 812 });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_08_mobile_1_column.png');

    // 9. Check horizontal overflow on mobile
    const hasHorizontalOverflow = await page.evaluate(() => {
      return document.documentElement.scrollWidth > window.innerWidth;
    });
    console.log('Mobile horizontal overflow detected:', hasHorizontalOverflow);

    // 10. Inspect Details Modal
    console.log('Testing Inspect Details Modal on desktop...');
    await page.setViewport({ width: 1440, height: 900 });
    await clickByText('Inspect');
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_09_inspect_details_modal.png');

    // Close details
    const closeBtn = await page.$('button[aria-label="Close details"]');
    if (closeBtn) await closeBtn.click();
    await new Promise((r) => setTimeout(r, 400));

    // 11. Proceed to Feedback & Final Document Preview
    console.log('Testing Feedback Form & Final Document Preview...');
    await clickByText('Record Preference');
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('refine_10_feedback_form_aligned.png');

    await clickByText('Confirm Selection');
    await new Promise((r) => setTimeout(r, 800));
    await saveScreenshot('refine_11_final_document_preview.png');

    console.log('--- ALL VISUAL VERIFICATIONS COMPLETED SUCCESSFULLY ---');
  } catch (err) {
    console.error('Verification error:', err);
  } finally {
    await browser.close();
    try {
      viteProcess.kill();
    } catch {}
  }
}

runRefinementVerification();

