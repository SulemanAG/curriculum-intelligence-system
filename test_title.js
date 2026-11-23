import { generateConversationTitle } from './src/utils/titleGenerator.ts';

const testCases = [
  {
    input: "I need to design the course plan for Computer Vision",
    expected: "Computer Vision"
  },
  {
    input: "Create a curriculum for Machine Learning",
    expected: "Machine Learning"
  },
  {
    input: "Help me prepare a course plan for Deep Learning",
    expected: "Deep Learning"
  },
  {
    input: "I want to analyse industry requirements for Generative AI",
    expected: "Generative AI"
  },
  {
    input: "Compare the syllabus of Data Structures across IITs",
    expected: "Data Structures"
  },
  {
    input: "Design a course plan for Natural Language Processing",
    expected: "Natural Language Processing"
  },
  {
    input: "Course plan for Natural Language Processing for 6th semester B.Tech.",
    expected: "Natural Language Processing"
  },
  {
    input: "Prepare syllabus for Agentic AI Systems aligned with ACM and APNI standards.",
    expected: "Agentic AI Systems"
  },
  {
    input: "Design an undergraduate course on Edge Computing and IoT Networks.",
    expected: "Edge Computing and IoT Networks"
  },
  {
    input: "Computer Vision (UG, Core, Sem 5, 3:0:2)",
    expected: "Computer Vision"
  },
  {
    input: "Hello there",
    expected: "Hello There"
  }
];

let failed = 0;
console.log('--- TESTING TITLE EXTRACTION CASES ---');
for (const tc of testCases) {
  const actual = generateConversationTitle(tc.input);
  const pass = actual === tc.expected;
  console.log(`[${pass ? 'PASS' : 'FAIL'}] Input: "${tc.input}"`);
  console.log(`       Output:   "${actual}"`);
  console.log(`       Expected: "${tc.expected}"`);
  if (!pass) failed++;
}

if (failed === 0) {
  console.log('\nAll test cases passed successfully!');
} else {
  console.error(`\n${failed} test case(s) failed.`);
  process.exit(1);
}

