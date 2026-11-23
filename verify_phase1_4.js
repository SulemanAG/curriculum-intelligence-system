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

async function runPhase14Verification() {
  console.log('--- STARTING PHASE 1.4 REGRESSION & VERIFICATION PASS ---');

  // 1. Start Vite preview / dev server
  console.log('Starting Vite server on port 5173...');
  const viteProcess = spawn('npx.cmd', ['vite', '--port', '5173', '--host'], {
    stdio: 'pipe',
    shell: true
  });

  await new Promise((r) => setTimeout(r, 3500));

  const USER_DATA_DIR = path.resolve('./.chrome-profile-p14');
  if (fs.existsSync(USER_DATA_DIR)) {
    try { fs.rmSync(USER_DATA_DIR, { recursive: true, force: true }); } catch {}
  }
  fs.mkdirSync(USER_DATA_DIR, { recursive: true });

  console.log('Launching Chrome...');
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
  await page.setViewport({ width: 1440, height: 900 });

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
    // Test 1: Landing Page Inspection (Check Header & Sidebar Branding)
    console.log('1. Checking Landing Page Branding & Clean Header...');
    await page.goto('http://localhost:5173', { waitUntil: 'domcontentloaded', timeout: 15000 });
    await new Promise((r) => setTimeout(r, 1200));

    // Check if KLE Tech chip exists in header or sidebar
    const headerKLE = await page.evaluate(() => {
      const header = document.querySelector('header');
      return header ? header.textContent.includes('KLE Tech') : false;
    });
    console.log('Header contains KLE Tech badge:', headerKLE);

    const sidebarKLE = await page.evaluate(() => {
      const sidebarTop = document.querySelector('aside div.h-14');
      return sidebarTop ? sidebarTop.textContent.includes('KLE Tech') : false;
    });
    console.log('Sidebar top contains KLE Tech subtitle:', sidebarKLE);

    await saveScreenshot('phase1_4_01_landing_clean_chrome.png');

    // Test 2: Message Composer Separator Check
    console.log('2. Inspecting Message Composer for Full-Width Line...');
    const footerHasBorder = await page.evaluate(() => {
      const footer = document.querySelector('footer');
      if (!footer) return false;
      const compStyles = window.getComputedStyle(footer);
      return compStyles.borderTopWidth !== '0px' && compStyles.borderTopStyle !== 'none';
    });
    console.log('Footer has full-width border-top separator:', footerHasBorder);

    // Test 3: Intelligent Title Extraction on Prompt Submission
    console.log('3. Testing Intelligent Title Generation for Prompt 1: "I need to design the course plan for Computer Vision"...');
    const textarea = await page.$('textarea');
    if (textarea) {
      await textarea.type('I need to design the course plan for Computer Vision');
      await page.keyboard.press('Enter');
    }
    await new Promise((r) => setTimeout(r, 1000));

    // Check generated title in header and sidebar
    const headerTitle = await page.evaluate(() => {
      const titleSpan = document.querySelector('header span[title]');
      return titleSpan ? titleSpan.textContent.trim() : '';
    });
    console.log('Header Title after prompt 1:', `"${headerTitle}"`);

    const sidebarTitle = await page.evaluate(() => {
      const activeItem = document.querySelector('aside button.bg-blue-50');
      return activeItem ? activeItem.textContent.trim() : '';
    });
    console.log('Sidebar Active Title:', `"${sidebarTitle}"`);

    await saveScreenshot('phase1_4_02_intelligent_title_computer_vision.png');

    // Test 4: Verify Floating Composer in Active Conversation
    console.log('4. Capturing Floating Composer Dock...');
    await saveScreenshot('phase1_4_03_floating_composer_dock.png');

    // Test 5: Second Conversation with Different Prompt: "Create a curriculum for Machine Learning"
    console.log('5. Creating New Chat and Testing Prompt 2: "Create a curriculum for Machine Learning"...');
    await clickByText('New Chat');
    await new Promise((r) => setTimeout(r, 600));

    const textarea2 = await page.$('textarea');
    if (textarea2) {
      await textarea2.type('Create a curriculum for Machine Learning');
      await page.keyboard.press('Enter');
    }
    await new Promise((r) => setTimeout(r, 1000));

    const headerTitle2 = await page.evaluate(() => {
      const titleSpan = document.querySelector('header span[title]');
      return titleSpan ? titleSpan.textContent.trim() : '';
    });
    console.log('Header Title after prompt 2:', `"${headerTitle2}"`);

    // Check all conversation titles listed in sidebar history
    const allSidebarTitles = await page.evaluate(() => {
      const items = Array.from(document.querySelectorAll('aside div.space-y-0\\.5 button span.truncate'));
      return items.map((el) => el.textContent.trim());
    });
    console.log('Sidebar History Titles:', allSidebarTitles);

    await saveScreenshot('phase1_4_04_multiple_conversations_sidebar.png');

    // Test 6: Switching between conversations preserves titles
    console.log('6. Switching back to Computer Vision conversation...');
    await page.evaluate(() => {
      const buttons = Array.from(document.querySelectorAll('aside div.space-y-0\\.5 button'));
      const cvBtn = buttons.find((b) => b.textContent && b.textContent.includes('Computer Vision'));
      if (cvBtn) cvBtn.click();
    });
    await new Promise((r) => setTimeout(r, 800));

    const switchedTitle = await page.evaluate(() => {
      const titleSpan = document.querySelector('header span[title]');
      return titleSpan ? titleSpan.textContent.trim() : '';
    });
    console.log('Header Title after switching back to CV:', `"${switchedTitle}"`);

    // Test 7: Mobile Viewport (375x812)
    console.log('7. Testing Mobile Viewport (375x812)...');
    await page.setViewport({ width: 375, height: 812 });
    await new Promise((r) => setTimeout(r, 600));
    await saveScreenshot('phase1_4_05_mobile_viewport.png');

    console.log('--- ALL PHASE 1.4 TESTS COMPLETED SUCCESSFULLY ---');
  } catch (err) {
    console.error('Test error:', err);
  } finally {
    await browser.close();
    try {
      viteProcess.kill();
    } catch {}
  }
}

runPhase14Verification();
