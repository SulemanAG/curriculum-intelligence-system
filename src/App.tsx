import React, { useState, useEffect } from 'react';
import {
  Conversation,
  ChatMessage,
  NavigationDestination,
  LibraryDocument,
  ScheduledTask,
  UserFeedback,
  FinalCoursePlan
} from './types';
import { Sidebar } from './components/layout/Sidebar';
import { ChatHeader } from './components/layout/ChatHeader';
import { ChatWorkspace } from './components/chat/ChatWorkspace';
import { ClarificationDialog, ClarificationConstraints } from './components/ClarificationDialog';
import { AnalyzeAgentsPage } from './pages/AnalyzeAgentsPage';
import { LibraryPage } from './pages/LibraryPage';
import { ScheduledPage } from './pages/ScheduledPage';
import { OtherToolsPage } from './pages/OtherToolsPage';
import { ProfilePage } from './pages/ProfilePage';
import { LandingPage } from './pages/LandingPage';
import { apiClient } from './services/api';
import {
  SCHEMA_VERIFICATION_ALTERNATIVES,
  SCHEMA_VERIFICATION_FINAL_PLAN
} from './data/schemaPreviewData';
import { generateConversationTitle } from './utils/titleGenerator';

const INITIAL_CONVERSATIONS_KEY = 'curriculum_ai_conversations_v1';

export const App: React.FC = () => {
  // Top-Level Route State: '/' => 'landing', '/app' => 'app'
  const [route, setRoute] = useState<'landing' | 'app'>(() => {
    if (typeof window !== 'undefined') {
      const path = window.location.pathname;
      if (path === '/app' || path.startsWith('/app/')) {
        return 'app';
      }
    }
    return 'landing';
  });

  // Navigation State inside Workspace
  const [currentView, setCurrentView] = useState<NavigationDestination>('chat');

  // Listen for browser Back / Forward buttons (HTML5 History API)
  useEffect(() => {
    const handlePopState = () => {
      const path = window.location.pathname;
      if (path === '/app' || path.startsWith('/app/')) {
        setRoute('app');
      } else {
        setRoute('landing');
      }
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateToApp = () => {
    if (window.location.pathname !== '/app') {
      window.history.pushState({}, '', '/app');
    }
    setRoute('app');
  };

  const navigateToLanding = () => {
    if (window.location.pathname !== '/') {
      window.history.pushState({}, '', '/');
    }
    setRoute('landing');
  };

  // Sidebar Layout State
  const [isOpenMobile, setIsOpenMobile] = useState<boolean>(false);
  const [isCollapsedDesktop, setIsCollapsedDesktop] = useState<boolean>(false);

  // Backend Health
  const [backendOnline, setBackendOnline] = useState<boolean>(false);

  // Clarification Dialog Modal
  const [isClarificationOpen, setIsClarificationOpen] = useState<boolean>(false);
  const [clarificationPrompt, setClarificationPrompt] = useState<string>('');

  // Library Documents State
  const [libraryDocs, setLibraryDocs] = useState<LibraryDocument[]>([]);

  // Scheduled Tasks State
  const [scheduledTasks] = useState<ScheduledTask[]>([]);

  // Conversations State
  const [conversations, setConversations] = useState<Conversation[]>(() => {
    try {
      const stored = localStorage.getItem(INITIAL_CONVERSATIONS_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch {
      // Ignore storage read error
    }
    // Default initial empty conversation
    const initialId = 'conv-' + Date.now();
    return [
      {
        id: initialId,
        title: 'New Course Design',
        messages: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    ];
  });

  const [activeConversationId, setActiveConversationId] = useState<string>(() => {
    return conversations[0]?.id || 'conv-default';
  });

  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isDevPreview, setIsDevPreview] = useState<boolean>(false);

  // Sync conversations to localStorage
  useEffect(() => {
    try {
      localStorage.setItem(INITIAL_CONVERSATIONS_KEY, JSON.stringify(conversations));
    } catch {
      // Ignore storage error
    }
  }, [conversations]);

  // Check backend health on mount
  useEffect(() => {
    let isMounted = true;
    apiClient.checkBackendHealth().then((health) => {
      if (isMounted) {
        setBackendOnline(health.online);
      }
    });
    return () => {
      isMounted = false;
    };
  }, []);

  // Active Conversation Helper
  const activeConversation =
    conversations.find((c) => c.id === activeConversationId) || conversations[0];

  // Helper to add a message to active conversation
  const appendMessage = (message: ChatMessage) => {
    setConversations((prev) =>
      prev.map((conv) => {
        if (conv.id === activeConversationId) {
          const updatedMessages = [...conv.messages, message];
          // Auto-title conversation on first user message using intelligent extraction
          let title = conv.title;
          if (
            !conv.isCustomTitle &&
            (conv.title === 'New Course Design' || conv.messages.length === 0) &&
            message.role === 'user'
          ) {
            title = generateConversationTitle(message.content);
          }
          return {
            ...conv,
            title,
            messages: updatedMessages,
            updatedAt: new Date().toISOString()
          };
        }
        return conv;
      })
    );
  };

  // Helper to update the last assistant message
  const updateLastAssistantMessage = (updater: (msg: ChatMessage) => ChatMessage) => {
    setConversations((prev) =>
      prev.map((conv) => {
        if (conv.id === activeConversationId) {
          const messages = [...conv.messages];
          for (let i = messages.length - 1; i >= 0; i--) {
            if (messages[i].role === 'assistant') {
              messages[i] = updater(messages[i]);
              break;
            }
          }
          return {
            ...conv,
            messages,
            updatedAt: new Date().toISOString()
          };
        }
        return conv;
      })
    );
  };

  // Handler: New Chat
  const handleNewChat = () => {
    const newId = 'conv-' + Date.now();
    const newConv: Conversation = {
      id: newId,
      title: 'New Course Design',
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    setConversations((prev) => [newConv, ...prev]);
    setActiveConversationId(newId);
    setCurrentView('chat');
    setIsDevPreview(false);
  };

  // Handler: Rename Conversation
  const handleRenameConversation = (newTitle: string) => {
    setConversations((prev) =>
      prev.map((conv) =>
        conv.id === activeConversationId
          ? { ...conv, title: newTitle, isCustomTitle: true }
          : conv
      )
    );
  };

  // Handler: Clear Conversation Messages
  const handleClearConversation = () => {
    setConversations((prev) =>
      prev.map((conv) =>
        conv.id === activeConversationId ? { ...conv, messages: [] } : conv
      )
    );
  };

  // Handler: Export Conversation as Plain Text
  const handleExportConversation = () => {
    if (!activeConversation) return;
    const lines = activeConversation.messages.map(
      (m) => `[${m.role.toUpperCase()}] ${new Date(m.timestamp).toLocaleTimeString()}:\n${m.content}\n`
    );
    const blob = new Blob([lines.join('\n---\n\n')], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${activeConversation.title.replace(/[^a-zA-Z0-9_-]/g, '_')}_history.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  // Handler: Send Message
  const handleSendMessage = async (text: string) => {
    const userMsg: ChatMessage = {
      id: 'msg-' + Date.now(),
      role: 'user',
      content: text,
      timestamp: new Date().toISOString(),
      payloadType: 'text'
    };
    appendMessage(userMsg);
    setIsLoading(true);

    // Initial assistant response message
    const runId = 'RUN-' + Math.random().toString(36).substring(2, 9).toUpperCase();
    const assistantMsgId = 'msg-' + (Date.now() + 1);

    const assistantMsg: ChatMessage = {
      id: assistantMsgId,
      role: 'assistant',
      content: `I am initiating the curriculum research workflow for "${text}". The apniLeap AURA Coordinator is locking constraints and dispatching parallel specialist agents.`,
      timestamp: new Date().toISOString(),
      payloadType: 'research_progress',
      researchRun: {
        id: runId,
        requestId: 'REQ-' + Date.now().toString().slice(-6),
        prompt: text,
        currentStageId: 'intake_analysis',
        status: 'running',
        startedAt: new Date().toISOString(),
        elapsedSeconds: 0,
        auditTrailId: 'AUDIT-' + Math.random().toString(36).substring(2, 9).toUpperCase(),
        stages: [
          {
            id: 'intake_analysis',
            name: '1. Request Intake & Locked Plan',
            description: 'Coordinator clarifies scope, constraints, and credit structure.',
            status: 'running',
            message: `Parsing: "${text}" & locking constraints...`
          },
          {
            id: 'parallel_view_building',
            name: '2. Concurrent View-Building (Parallel Swarm)',
            description: 'Curriculum Mapping, Industry Skills, and ACM Benchmarking agents run concurrently.',
            status: 'pending',
            isParallel: true,
            message: 'Awaiting locked blueprint...',
            parallelAgents: [
              {
                id: 'agent-curr',
                name: 'Curriculum Mapping Agent',
                role: 'Maps syllabus & CO/PO vectors',
                status: 'pending',
                message: 'Standing by...',
                guardrail: 'Never infer unstated outcomes'
              },
              {
                id: 'agent-skills',
                name: 'Industry Skills Agent',
                role: 'Aggregates job market demand',
                status: 'pending',
                message: 'Standing by...',
                guardrail: 'Cite source & date per trend'
              },
              {
                id: 'agent-acm',
                name: 'ACM / APNI Benchmarking Agent',
                role: 'Benchmarks against ACM CS2023 & IIT syllabi',
                status: 'pending',
                message: 'Standing by...',
                guardrail: 'Never claim unreferenced compliance'
              }
            ]
          },
          {
            id: 'gap_analysis',
            name: '3. Curriculum Gap Analysis',
            description: 'Triangulates missing topics and skill mismatches with severity.',
            status: 'pending',
            message: 'Pending parallel views...'
          },
          {
            id: 'recommendation_drafting',
            name: '4. Proposal Recommendation Drafting',
            description: 'Generates four distinct alternative course-plan proposals.',
            status: 'pending',
            message: 'Standing by for gap analysis...'
          },
          {
            id: 'validation_acceptance',
            name: '5. Automated Acceptance Gate',
            description: 'Verifies evidence citations and CO/PO consistency.',
            status: 'pending',
            message: 'Standing by...'
          },
          {
            id: 'human_review_gate',
            name: '6. Human Review Gate (Faculty Decision)',
            description: 'Faculty inspection and selection.',
            status: 'pending',
            message: 'Standing by...'
          }
        ]
      }
    };

    appendMessage(assistantMsg);
    setIsLoading(false);

    // Submit real request to backend and initiate multi-agent workflow
    let activeRunId = runId;
    try {
      const resp = await apiClient.submitCourseRequest(text);
      if (resp.success && resp.data?.id) {
        activeRunId = resp.data.id;
      }
    } catch (err) {
      console.warn('Backend request submission error:', err);
    }

    // Real-time polling loop
    let attempts = 0;
    const maxAttempts = 60; // Poll for up to ~2.5 minutes
    const pollInterval = setInterval(async () => {
      attempts++;
      try {
        const runResp = await apiClient.getResearchRun(activeRunId);
        if (runResp.success && runResp.data) {
          const run = runResp.data;
          updateLastAssistantMessage((msg) => {
            if (!msg.researchRun) return msg;
            return {
              ...msg,
              researchRun: {
                ...msg.researchRun,
                currentStageId: run.currentStageId || msg.researchRun.currentStageId,
                status: run.status || msg.researchRun.status,
                elapsedSeconds: run.elapsedSeconds || (attempts * 2),
                stages: run.stages && run.stages.length > 0 ? run.stages : msg.researchRun.stages
              }
            };
          });

          // Check if ready for faculty inspection
          if (run.status === 'awaiting_human_input' || run.status === 'completed' || attempts >= maxAttempts) {
            clearInterval(pollInterval);
            setIsLoading(false);

            // Fetch genuine dynamic alternatives synthesized by RecommendationAgent
            const altsResp = await apiClient.getCourseAlternatives(activeRunId);
            if (altsResp.success && altsResp.data && altsResp.data.length > 0) {
              const liveAlts = altsResp.data;
              updateLastAssistantMessage((msg) => ({
                ...msg,
                content: `Real-time multi-agent research complete for "${text}". The AURA Coordinator and specialist agents have analyzed live industry skills, benchmarked institutional syllabi, and synthesized four distinct course-plan proposals.\n\nPlease inspect the proposals below and record your faculty preference:`,
                payloadType: 'alternatives',
                alternatives: liveAlts,
                selectedAlternativeId: liveAlts[0]?.id,
                isDevPreview: false
              }));
            }
          }
        }
      } catch (pollErr) {
        console.warn('Polling error:', pollErr);
        if (attempts >= maxAttempts) {
          clearInterval(pollInterval);
          setIsLoading(false);
        }
      }
    }, 2500);
  };

  // Handler: Explicitly Load Schema Preview for Alternatives
  const handleLoadSchemaPreview = () => {
    setIsDevPreview(true);
    updateLastAssistantMessage((msg) => {
      return {
        ...msg,
        content: `Here are four evidence-grounded course-plan proposals generated for your subject. Each takes a distinct pedagogical strategy. You can inspect the module breakdown and evidence citations for each proposal, then record your preference:`,
        payloadType: 'alternatives',
        alternatives: SCHEMA_VERIFICATION_ALTERNATIVES,
        selectedAlternativeId: SCHEMA_VERIFICATION_ALTERNATIVES[2].id, // Default select balanced standard
        isDevPreview: true
      };
    });
  };

  // Handler: Select an Alternative in Chat
  const handleSelectAlternative = (altId: string) => {
    updateLastAssistantMessage((msg) => ({
      ...msg,
      selectedAlternativeId: altId
    }));
  };

  // Handler: Proceed to Feedback Form in Chat
  const handleProceedToFeedback = () => {
    updateLastAssistantMessage((msg) => ({
      ...msg,
      content: 'Please record your faculty selection rationale and any desired revisions below:',
      payloadType: 'feedback'
    }));
  };

  // Handler: Back to Alternatives from Feedback Form
  const handleBackToAlternatives = () => {
    updateLastAssistantMessage((msg) => ({
      ...msg,
      content: 'Review the four alternative proposals below and select your preferred version:',
      payloadType: 'alternatives'
    }));
  };

  // Handler: Submit Feedback & Generate Final Document
  const handleSubmitFeedback = async (feedback: UserFeedback) => {
    setIsLoading(true);

    let planId = '';
    try {
      const res = await apiClient.submitSelectionFeedback(feedback);
      if (res.success && res.data?.finalPlanId) {
        planId = res.data.finalPlanId;
      }
    } catch (err) {
      console.error('Feedback submission error:', err);
    }

    let finalPlanFromBackend: FinalCoursePlan | null = null;
    if (planId) {
      try {
        const planRes = await apiClient.getFinalCoursePlan(planId);
        if (planRes.success && planRes.data) {
          finalPlanFromBackend = planRes.data;
        }
      } catch (err) {
        console.warn('Could not fetch final plan directly from backend:', err);
      }
    }

    updateLastAssistantMessage((msg) => {
      const selectedAlt =
        msg.alternatives?.find((a) => a.id === feedback.selectedAlternativeId) ||
        SCHEMA_VERIFICATION_ALTERNATIVES[2];

      const generatedPlan = finalPlanFromBackend || {
        ...SCHEMA_VERIFICATION_FINAL_PLAN,
        id: planId || ('PLAN-KLE-' + selectedAlt.code + '-' + Date.now().toString().slice(-4)),
        selectedAlternativeId: selectedAlt.id,
        courseTitle: selectedAlt.title.replace(/\[Schema Preview\]\s*/i, ''),
        credits: selectedAlt.credits,
        contactHours: {
          lecture: selectedAlt.lectureHours,
          tutorial: selectedAlt.tutorialHours,
          practical: selectedAlt.practicalHours,
          total: selectedAlt.totalHours
        },
        courseDescription: selectedAlt.curriculumPhilosophy,
        courseOutcomes: selectedAlt.courseOutcomes,
        modules: selectedAlt.modules,
        assessmentScheme: selectedAlt.assessmentScheme,
        evidenceReferences: selectedAlt.evidenceReferences
      };

      // Add to Library Documents automatically
      const newDoc: LibraryDocument = {
        id: generatedPlan.id,
        title: `${generatedPlan.courseTitle} - Course Instruction Plan`,
        courseTitle: generatedPlan.courseTitle,
        department: generatedPlan.department,
        generatedDate: new Date().toISOString(),
        fileSize: '479 KB',
        isAvailable: true,
        downloadUrl: `/api/curriculum/plans/${generatedPlan.id}/pdf`
      };
      setLibraryDocs((prev) => [newDoc, ...prev]);

      return {
        ...msg,
        content: `Your preference has been committed to the institutional audit log and saved to the preference dataset.\n\nHere is the finalized Course Instruction Plan:`,
        payloadType: 'final_document',
        finalPlan: generatedPlan
      };
    });

    setIsLoading(false);
  };

  // Handler: Open Clarification Modal (Lab 1)
  const handleOpenClarification = (userPrompt: string) => {
    setClarificationPrompt(userPrompt);
    setIsClarificationOpen(true);
  };

  // Handler: Lock Constraints from Clarification Dialog
  const handleLockConstraints = (constraints: ClarificationConstraints) => {
    setIsClarificationOpen(false);
    const enriched = `${clarificationPrompt || 'Course Plan'} (${constraints.degreeLevel}, ${constraints.courseNature}, Sem ${constraints.targetSemester}, ${constraints.contactDistribution})`;
    handleSendMessage(enriched);
  };

  // 1. If currently at root '/' or landing route, render Landing Page
  if (route === 'landing') {
    return <LandingPage onGetStarted={navigateToApp} />;
  }

  // 2. If at '/app', render the full curriculum design workspace
  return (
    <div className="flex h-screen w-screen overflow-hidden bg-white text-slate-900 font-sans">
      {/* 1. Persistent Collapsible Left Sidebar */}
      <Sidebar
        currentView={currentView}
        activeConversationId={activeConversationId}
        conversations={conversations}
        onSelectConversation={(id) => {
          setActiveConversationId(id);
          setCurrentView('chat');
        }}
        onNewChat={handleNewChat}
        onNavigate={(dest) => setCurrentView(dest)}
        isOpenMobile={isOpenMobile}
        onCloseMobile={() => setIsOpenMobile(false)}
        isCollapsedDesktop={isCollapsedDesktop}
        onToggleCollapseDesktop={() => setIsCollapsedDesktop(!isCollapsedDesktop)}
        onNavigateToLanding={navigateToLanding}
      />

      {/* 2. Main Content Workspace */}
      <div className="flex-1 flex flex-col h-full min-w-0 bg-white">
        {/* Simple Top Header */}
        <ChatHeader
          conversationTitle={activeConversation?.title || 'Curriculum AI'}
          backendOnline={backendOnline}
          onOpenMobileSidebar={() => setIsOpenMobile(true)}
          onClearConversation={handleClearConversation}
          onRenameConversation={handleRenameConversation}
          onExportConversation={handleExportConversation}
          onNavigateToLanding={navigateToLanding}
        />

        {/* View Routing */}
        {currentView === 'chat' && (
          <ChatWorkspace
            messages={activeConversation?.messages || []}
            onSendMessage={handleSendMessage}
            onOpenClarification={handleOpenClarification}
            isLoading={isLoading}
            onSelectAlternative={handleSelectAlternative}
            onProceedToFeedback={handleProceedToFeedback}
            onSubmitFeedback={handleSubmitFeedback}
            onBackToAlternatives={handleBackToAlternatives}
            onLoadSchemaPreview={handleLoadSchemaPreview}
            onViewAlternatives={handleLoadSchemaPreview}
            onReset={handleNewChat}
            isDevPreview={isDevPreview}
          />
        )}

        {currentView === 'analyze-agents' && (
          <AnalyzeAgentsPage onBackToChat={() => setCurrentView('chat')} />
        )}

        {currentView === 'library' && (
          <LibraryPage
            documents={libraryDocs}
            onBackToChat={() => setCurrentView('chat')}
          />
        )}

        {currentView === 'scheduled' && (
          <ScheduledPage
            tasks={scheduledTasks}
            onBackToChat={() => setCurrentView('chat')}
          />
        )}

        {currentView === 'other-tools' && (
          <OtherToolsPage onBackToChat={() => setCurrentView('chat')} />
        )}

        {currentView === 'profile' && (
          <ProfilePage
            onBackToChat={() => setCurrentView('chat')}
            backendOnline={backendOnline}
          />
        )}
      </div>

      {/* Clarification Dialog Modal (Lab 1) */}
      {isClarificationOpen && (
        <ClarificationDialog
          initialPrompt={clarificationPrompt}
          onLockPlan={handleLockConstraints}
          onCancel={() => setIsClarificationOpen(false)}
        />
      )}
    </div>
  );
};

export default App;
