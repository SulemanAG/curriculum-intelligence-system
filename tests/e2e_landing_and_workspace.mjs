import puppeteer from 'puppeteer-core';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function runE2ETests() {
  console.log('====================================================');
  console.log('CURRICULUM INTELLIGENCE SYSTEM - END-TO-END UI TESTS');
  console.log('====================================================');

  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  try {
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });

    // ----------------------------------------------------
    // TEST 1: Visiting root URL '/' displays Landing Page
    // ----------------------------------------------------
    console.log('\n[TEST 1] Visiting http://localhost:5173/ ...');
    await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
    await new Promise(r => setTimeout(r, 800));

    const headline = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  Hero headline:', headline);
    if (!headline || !headline.includes('Design Better Courses')) {
      throw new Error(`Expected headline to contain 'Design Better Courses', got: ${headline}`);
    }

    const hasLandingSections = await page.evaluate(() => {
      const arch = document.getElementById('architecture');
      const workflow = document.getElementById('workflow');
      const features = document.getElementById('features');
      const trust = document.getElementById('trust');
      return !!(arch && workflow && features && trust);
    });
    console.log('  Landing page sections present (arch, workflow, features, trust):', hasLandingSections);
    if (!hasLandingSections) {
      throw new Error('Required landing page sections not found!');
    }

    // Verify chat workspace does NOT appear on '/'
    const hasChatArea = await page.$('input[placeholder*="Ask"], textarea, [placeholder*="course"]').catch(() => null);
    console.log('  Chat input on root page (should be null):', hasChatArea === null);
    if (hasChatArea !== null) {
      throw new Error('Chat workspace appeared on root URL when landing page was expected!');
    }

    // ----------------------------------------------------
    // TEST 2: Clicking "Get Started" navigates to '/app'
    // ----------------------------------------------------
    console.log('\n[TEST 2] Clicking "Get Started" button...');
    const getStartedButtons = await page.$$('button');
    let clicked = false;
    for (const btn of getStartedButtons) {
      const text = await page.evaluate(el => el.innerText, btn);
      if (text.includes('Get Started')) {
        await btn.click();
        clicked = true;
        break;
      }
    }
    if (!clicked) throw new Error('Could not find Get Started button');

    await new Promise(r => setTimeout(r, 600));

    const currentUrl = page.url();
    console.log('  Current URL after click:', currentUrl);
    if (!currentUrl.includes('/app')) {
      throw new Error(`Expected URL to include '/app', got: ${currentUrl}`);
    }

    // Verify no login or registration screen
    const loginForm = await page.$('form[action*="login"], input[type="password"]');
    console.log('  Login/password form present (should be null):', loginForm === null);
    if (loginForm !== null) {
      throw new Error('Login screen appeared! Skip-login requirement violated.');
    }

    // Verify Chat Workspace is rendered
    const workspaceHeading = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  Workspace heading:', workspaceHeading);
    if (!workspaceHeading || !workspaceHeading.includes('Curriculum Intelligence System')) {
      throw new Error(`Expected workspace heading to be 'Curriculum Intelligence System', got: ${workspaceHeading}`);
    }

    // Verify suggested example prompts exist
    const promptButtons = await page.$$('button span');
    let foundDesign = false, foundBenchmark = false;
    for (const span of promptButtons) {
      const t = await page.evaluate(el => el.innerText, span);
      if (t.includes('Design Course Plan')) foundDesign = true;
      if (t.includes('Benchmark Course')) foundBenchmark = true;
    }
    console.log('  Example prompts present (Design Course Plan, Benchmark Course):', foundDesign && foundBenchmark);

    // ----------------------------------------------------
    // TEST 3: Return to Landing Page from Workspace
    // ----------------------------------------------------
    console.log('\n[TEST 3] Clicking "Landing Page" / "Curriculum AI" in header to return home...');
    const homeBtn = await page.$('button[title="Return to Landing Page"]');
    if (!homeBtn) throw new Error('Return to Landing Page button not found in ChatHeader');
    await homeBtn.click();
    await new Promise(r => setTimeout(r, 600));

    console.log('  Current URL after returning home:', page.url());
    const returnedHeadline = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  Returned to landing headline:', returnedHeadline);
    if (!returnedHeadline || !returnedHeadline.includes('Design Better Courses')) {
      throw new Error('Failed to return to Landing Page!');
    }

    // ----------------------------------------------------
    // TEST 4: Direct navigation to '/app'
    // ----------------------------------------------------
    console.log('\n[TEST 4] Direct navigation to http://localhost:5173/app ...');
    await page.goto('http://localhost:5173/app', { waitUntil: 'domcontentloaded' });
    await new Promise(r => setTimeout(r, 800));
    console.log('  Current URL:', page.url());
    const directHeading = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  Direct workspace heading:', directHeading);
    if (!directHeading || !directHeading.includes('Curriculum Intelligence System')) {
      throw new Error('Direct navigation to /app failed to open workspace!');
    }

    // ----------------------------------------------------
    // TEST 5: Browser Back / Forward History Navigation
    // ----------------------------------------------------
    console.log('\n[TEST 5] Testing browser Back and Forward navigation...');
    await page.goBack();
    await new Promise(r => setTimeout(r, 400));
    console.log('  After goBack(), URL is:', page.url());
    const backHeadline = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  After goBack(), page is:', backHeadline?.slice(0, 30));

    await page.goForward();
    await new Promise(r => setTimeout(r, 400));
    console.log('  After goForward(), URL is:', page.url());
    const forwardHeadline = await page.$eval('h1', el => el.innerText).catch(() => null);
    console.log('  After goForward(), page is:', forwardHeadline);

    console.log('\n====================================================');
    console.log('ALL FRONTEND ROUTING & UX TESTS PASSED SUCCESSFULLY!');
    console.log('====================================================\n');
  } finally {
    await browser.close();
  }
}

runE2ETests().catch(err => {
  console.error('\nE2E Test Failure:', err);
  process.exit(1);
});
