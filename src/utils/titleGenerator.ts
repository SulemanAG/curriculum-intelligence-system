export function generateConversationTitle(prompt: string): string {
  if (!prompt || typeof prompt !== 'string') return 'New Course Design';

  let cleaned = prompt.trim().replace(/^["'`]+|["'`]+$/g, '');

  // If prompt has metadata in parens like 'Computer Vision (UG, Core, Sem 5, 3:0:2)'
  const parenMatch = cleaned.match(/^([^(]+)\s*\([^)]+\)$/);
  if (parenMatch && parenMatch[1].trim().length > 2) {
    cleaned = parenMatch[1].trim();
  }

  // 1. Remove conversational prefixes and tasks
  // Matches:
  // "I need to design the course plan for Computer Vision"
  // "Create a curriculum for Machine Learning"
  // "Help me prepare a course plan for Deep Learning"
  // "I want to analyse industry requirements for Generative AI"
  // "Compare the syllabus of Data Structures across IITs"
  // "Design a course plan for Natural Language Processing"
  const prefixRegex = /^(?:(?:please|can you|could you|help me|i need to|i want to|i would like to|we need to|kindly)\s+)?(?:(?:create|design|prepare|build|generate|develop|compare|analyse|analyze|review|draft|synthesize)\s+)?(?:(?:a|an|the)\s+)?(?:(?:undergraduate|postgraduate|ug|pg)\s+)?(?:(?:course\s+plan|curriculum|syllabus|course\s+structure|instruction\s+plan|course\s+outline|course)\s+(?:for|of|on|in|across)\s+|(?:syllabus|curriculum)\s+of\s+|(?:industry\s+requirements|industry\s+demand)\s+for\s+|(?:course)\s+(?:on|for|in)\s+)?/i;

  cleaned = cleaned.replace(prefixRegex, '').trim();

  // 2. Remove trailing context phrases like 'across IITs', 'for 6th semester B.Tech', 'for my university', 'aligned with...'
  const trailingRegex = /\s+(?:across\s+(?:iits?|nits?|universities|different\s+institutions|colleges|institutions?)|for\s+(?:\d+(?:st|nd|rd|th)\s+semester|b\.?tech|m\.?tech|my\s+university|our\s+department|university|students?|engineering).*|aligned\s+with\s+.*|based\s+on\s+.*|according\s+to\s+.*|with\s+(?:hands-on|industry|lab).*)$/i;

  cleaned = cleaned.replace(trailingRegex, '').trim();

  // 3. Strip trailing punctuation
  cleaned = cleaned.replace(/[.?!,:;]+$/, '').trim();

  // If cleaning stripped too much or result is too short, return fallback
  if (!cleaned || cleaned.length < 2) {
    return 'New Course Design';
  }

  // 4. Normalize casing: Preserve well-known acronyms and title-case normal words
  const acronyms = new Set([
    'AI', 'ML', 'NLP', 'CV', 'IoT', 'LLM', 'CNN', 'RNN', 'VLSI', 'OS',
    'DBMS', 'API', 'AWS', 'GCP', 'IIT', 'NIT', 'ACM', 'IEEE', 'GPU', 'CPU'
  ]);

  const minorWords = new Set(['and', 'or', 'of', 'in', 'on', 'for', 'with', 'to', 'at', 'by']);

  const words = cleaned.split(/\s+/).map((word, index) => {
    const upper = word.toUpperCase();
    if (acronyms.has(upper)) return upper;
    // Lowercase minor connecting words unless it's the leading word
    if (index > 0 && minorWords.has(word.toLowerCase())) {
      return word.toLowerCase();
    }
    // If word is already mixed case/capitalized (e.g., 'DataStructures', 'TensorFlow'), retain
    if (word[0] === word[0].toUpperCase() && word.slice(1) !== word.slice(1).toUpperCase()) {
      return word;
    }
    // Standard capitalization
    return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
  });

  // Limit to 5 words max
  const title = words.slice(0, 5).join(' ');

  // Avoid generic titles
  const genericTitles = new Set(['Course Plan', 'Curriculum', 'Syllabus', 'New Course', 'Course', 'Plan']);
  if (genericTitles.has(title)) {
    return 'New Course Design';
  }

  return title;
}
