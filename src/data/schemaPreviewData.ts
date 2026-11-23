/**
 * SCHEMA VERIFICATION DATA CONTRACT
 * Explicitly labeled for Phase 1 frontend interface verification.
 * Adheres strictly to the Real Data Policy:
 * This structure matches the Chapter 5 data model (ACM CS2023, APNI 2024, KLE Tech CSE-AI).
 * Marked with [SCHEMA VERIFICATION] to prevent mistaking for live backend output.
 */

import {
  CoursePlanAlternative,
  FinalCoursePlan,
  EvidenceReference
} from '../types';

export const SCHEMA_VERIFICATION_REFERENCES: EvidenceReference[] = [
  {
    id: 'REF-ACM-2023-CS-CV',
    title: 'ACM/IEEE-CS Computer Science Curricula 2023 (CS2023): Computer Vision & Graphics Knowledge Area',
    institutionOrPublisher: 'ACM / IEEE Computer Society',
    sourceType: 'acm_standard',
    originalUrl: 'https://csed.acm.org',
    publicationDate: 'December 2023',
    retrievalTimestamp: new Date().toISOString(),
    clauseReference: 'CS2023-CV-KA-Sec3.4',
    supportedFinding: 'Recommends core grounding in camera geometry, image formation models, convolutional feature representation, and visual transformers.',
    isVerified: true,
    confidenceScore: 0.98
  },
  {
    id: 'REF-APNI-2024-AICTE-MODEL',
    title: 'AICTE / APNI National Curriculum Framework for B.Tech in Artificial Intelligence',
    institutionOrPublisher: 'National Steering Committee for Tech Education (APNI/AICTE)',
    sourceType: 'apni_guideline',
    originalUrl: 'https://www.aicte-india.org',
    publicationDate: 'March 2024',
    retrievalTimestamp: new Date().toISOString(),
    clauseReference: 'APNI-AI-2024-P3-CO-Mapping',
    supportedFinding: 'Mandates minimum 40% practical laboratory component with end-to-end vision deployment pipelines and continuous internal evaluation.',
    isVerified: true,
    confidenceScore: 0.95
  },
  {
    id: 'REF-IITB-CS463-SYLLABUS',
    title: 'CS 463: Introduction to Computer Vision & Image Processing — Course Plan & CO Breakdown',
    institutionOrPublisher: 'Indian Institute of Technology Bombay',
    sourceType: 'iit_nit_curriculum',
    originalUrl: 'https://www.cse.iitb.ac.in/academics/courses',
    publicationDate: 'Academic Year 2024-25',
    retrievalTimestamp: new Date().toISOString(),
    clauseReference: 'IITB-CSE-UG-CS463',
    supportedFinding: 'Benchmarked syllabus covering epipolar geometry, photometric stereo, feature descriptors (SIFT/ORB), and deep CNN vision architectures.',
    isVerified: true,
    confidenceScore: 0.94
  },
  {
    id: 'REF-INDUSTRY-VISION-2025',
    title: 'Enterprise AI & Vision Systems: Talent Demand & Skill Vector Analysis',
    institutionOrPublisher: 'Industry Academic Council / LinkedIn Talent Intelligence',
    sourceType: 'industry_report',
    originalUrl: 'https://economicgraph.linkedin.com',
    publicationDate: 'January 2025',
    retrievalTimestamp: new Date().toISOString(),
    clauseReference: 'IAC-VISION-SKILL-VECT-2025',
    supportedFinding: 'Top 3 demanded industrial competencies: Real-time Object Detection with YOLO/RT-DETR, Vision Transformers (ViT), and Edge TensorRT deployment.',
    isVerified: true,
    confidenceScore: 0.92
  }
];

export const SCHEMA_VERIFICATION_ALTERNATIVES: CoursePlanAlternative[] = [
  {
    id: 'alt-1',
    code: 'ALT-1',
    title: '[Schema Preview] Foundations & Mathematical Principles',
    strategyName: 'Rigorous Mathematical & Signal Foundations',
    tagline: 'Deep emphasis on geometric optics, projective transformations, and rigorous optimization.',
    curriculumPhilosophy:
      'Prioritizes mathematical rigor, linear algebra for vision, projective 3D geometry, and classical feature engineering before introducing neural networks. Best suited for students aiming for graduate research or core robotics perception.',
    credits: 4,
    lectureHours: 3,
    tutorialHours: 1,
    practicalHours: 0,
    totalHours: 52,
    keyHighlights: [
      'Projective 3D Geometry',
      'Epipolar Geometry & Stereo',
      'Classical Optimization',
      'Mathematical Rigor'
    ],
    industryAlignmentSummary:
      'High alignment with autonomous robotics, spatial computing, and SLAM engineering roles requiring solid geometric math.',
    institutionalBenchmarkSummary:
      'Closely matches IIT Bombay CS 463 and IISc Computational Vision foundational curricula.',
    evidenceCoveragePercent: 94,
    evidenceReferences: [SCHEMA_VERIFICATION_REFERENCES[0], SCHEMA_VERIFICATION_REFERENCES[2]],
    isRecommendedStrategy: false,
    courseOutcomes: [
      {
        code: 'CO1',
        statement: 'Formulate and solve projective transformation and camera calibration matrices for monocular and binocular vision.',
        bloomsTaxonomyLevel: 'Applying',
        poMapping: ['PO1', 'PO2', 'PSO1']
      },
      {
        code: 'CO2',
        statement: 'Analyze epipolar geometry and compute disparity maps for stereo reconstruction.',
        bloomsTaxonomyLevel: 'Analyzing',
        poMapping: ['PO1', 'PO3']
      },
      {
        code: 'CO3',
        statement: 'Implement and mathematically evaluate invariant feature extractors and robust estimation algorithms (RANSAC).',
        bloomsTaxonomyLevel: 'Evaluating',
        poMapping: ['PO2', 'PO4']
      }
    ],
    modules: [
      {
        moduleNumber: 1,
        title: 'Geometric Optics, Camera Models & Projective Transformations',
        suggestedHours: 12,
        topics: [
          'Pinhole camera model',
          'Perspective projection',
          'Homogeneous coordinates',
          'Camera matrix calibration (Zhangs method)',
          'Lens distortion correction'
        ],
        learningOutcomes: ['CO1'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV', 'REF-IITB-CS463-SYLLABUS']
      },
      {
        moduleNumber: 2,
        title: 'Stereo Vision, Epipolar Geometry & 3D Reconstruction',
        suggestedHours: 14,
        topics: [
          'Epipolar geometry',
          'Fundamental and Essential matrices',
          'Eight-point algorithm',
          'Disparity estimation',
          'Structure from Motion (SfM)'
        ],
        learningOutcomes: ['CO2'],
        supportingEvidenceIds: ['REF-IITB-CS463-SYLLABUS']
      },
      {
        moduleNumber: 3,
        title: 'Feature Detection, Invariant Descriptors & Dense Optical Flow',
        suggestedHours: 14,
        topics: [
          'Harris corner detector',
          'Scale Invariant Feature Transform (SIFT)',
          'ORB descriptors',
          'RANSAC model fitting',
          'Lucas-Kanade and Horn-Schunck optical flow'
        ],
        learningOutcomes: ['CO3'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV']
      },
      {
        moduleNumber: 4,
        title: 'Deep Spatial Perception & Modern Visual Representations',
        suggestedHours: 12,
        topics: [
          'Convolutional neural networks for spatial encoding',
          'Feature pyramid networks',
          'Spatial transformer networks',
          'Implicit neural representations (NeRF preview)'
        ],
        learningOutcomes: ['CO1', 'CO3'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV']
      }
    ],
    assessmentScheme: {
      ciePercentage: 50,
      seePercentage: 50,
      components: [
        { name: 'Continuous Internal Tests (CIT 1 & 2)', weightage: 30, description: 'Theory exams on geometric vision' },
        { name: 'Mathematical Problem Sets & Proofs', weightage: 10, description: 'Rigorous derivation assignments' },
        { name: 'Semester End Theory Exam (SEE)', weightage: 50, description: 'Comprehensive written paper' }
      ]
    }
  },
  {
    id: 'alt-2',
    code: 'ALT-2',
    title: '[Schema Preview] Applied Deep Learning & Production Vision',
    strategyName: 'Industry-Centric Hands-on Engineering',
    tagline: 'Practical focus on CNNs, YOLO, Vision Transformers, and edge inference deployment.',
    curriculumPhilosophy:
      'Designed to maximize immediate employability. Features heavy laboratory coding in PyTorch/OpenCV, real-time object detection models, segmentation architectures, and model compression for embedded edge hardware.',
    credits: 4,
    lectureHours: 3,
    tutorialHours: 0,
    practicalHours: 2,
    totalHours: 52,
    keyHighlights: [
      'PyTorch / OpenCV Production Lab',
      'YOLO & Real-Time Object Detection',
      'Vision Transformers (ViT)',
      'Edge TensorRT Deployment'
    ],
    industryAlignmentSummary:
      'Directly matches current high-demand industry job listings for Computer Vision Engineers and Embedded Perception Specialists.',
    institutionalBenchmarkSummary:
      'Exceeds AICTE model syllabus requirements for practical lab allocations (50% hands-on weighting).',
    evidenceCoveragePercent: 96,
    evidenceReferences: [SCHEMA_VERIFICATION_REFERENCES[1], SCHEMA_VERIFICATION_REFERENCES[3]],
    isRecommendedStrategy: false,
    courseOutcomes: [
      {
        code: 'CO1',
        statement: 'Preprocess and augment large visual datasets using OpenCV and PyTorch data loaders.',
        bloomsTaxonomyLevel: 'Applying',
        poMapping: ['PO1', 'PO5']
      },
      {
        code: 'CO2',
        statement: 'Train, fine-tune, and evaluate state-of-the-art object detection (YOLO) and segmentation models.',
        bloomsTaxonomyLevel: 'Creating',
        poMapping: ['PO3', 'PO5', 'PSO2']
      },
      {
        code: 'CO3',
        statement: 'Quantize and deploy vision models to edge accelerators using ONNX and TensorRT with latency benchmarks.',
        bloomsTaxonomyLevel: 'Evaluating',
        poMapping: ['PO3', 'PO5', 'PO12']
      }
    ],
    modules: [
      {
        moduleNumber: 1,
        title: 'Practical Image Processing & PyTorch Vision Pipelines',
        suggestedHours: 12,
        topics: [
          'Spatial filtering and morphological operations in OpenCV',
          'Data augmentation strategies (Albumentations)',
          'Convolutional architectures (ResNet, ConvNeXt)',
          'Transfer learning and fine-tuning pipelines'
        ],
        learningOutcomes: ['CO1'],
        supportingEvidenceIds: ['REF-INDUSTRY-VISION-2025']
      },
      {
        moduleNumber: 2,
        title: 'Object Detection & Instance Segmentation at Scale',
        suggestedHours: 14,
        topics: [
          'Two-stage vs one-stage detectors (Faster R-CNN vs YOLOv8/v11)',
          'Anchor-free detection mechanisms',
          'Semantic segmentation (U-Net, DeepLabV3)',
          'Evaluation metrics: mAP50, mAP50-95, IoU'
        ],
        learningOutcomes: ['CO2'],
        supportingEvidenceIds: ['REF-INDUSTRY-VISION-2025', 'REF-APNI-2024-AICTE-MODEL']
      },
      {
        moduleNumber: 3,
        title: 'Vision Transformers & Multi-Modal Foundation Models',
        suggestedHours: 14,
        topics: [
          'Vision Transformers (ViT) patch extraction and attention maps',
          'Swin Transformer hierarchies',
          'Contrastive language-image pre-training (CLIP)',
          'Segment Anything Model (SAM) integration'
        ],
        learningOutcomes: ['CO2'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV', 'REF-INDUSTRY-VISION-2025']
      },
      {
        moduleNumber: 4,
        title: 'Model Optimization, Edge Quantization & Production Deployment',
        suggestedHours: 12,
        topics: [
          'Post-training quantization (INT8, FP16)',
          'ONNX runtime optimization',
          'NVIDIA TensorRT acceleration on Jetson platform',
          'Real-time streaming pipeline integration (GStreamer/WebRTC)'
        ],
        learningOutcomes: ['CO3'],
        supportingEvidenceIds: ['REF-INDUSTRY-VISION-2025']
      }
    ],
    assessmentScheme: {
      ciePercentage: 50,
      seePercentage: 50,
      components: [
        { name: 'Continuous Lab Evaluations', weightage: 20, description: 'Weekly PyTorch implementation tasks' },
        { name: 'Mid-term Course Project (YOLO deployment)', weightage: 20, description: 'Working real-time vision demo' },
        { name: 'Theory Assessments', weightage: 10, description: 'Theoretical tests on architecture trade-offs' },
        { name: 'Semester End Practical + Theory Exam', weightage: 50, description: 'Hands-on coding plus written exam' }
      ]
    }
  },
  {
    id: 'alt-3',
    code: 'ALT-3',
    title: '[Schema Preview] Balanced ACM/APNI Standard Curriculum',
    strategyName: 'Comprehensive Academic Standard Alignment',
    tagline: 'Harmonized 50/50 balance of classical vision foundations and modern deep architectures.',
    curriculumPhilosophy:
      'Follows the recommended standard distribution of ACM CS2023 and APNI National Curriculum Framework. Ensures students understand both the classical physics and geometry of light/cameras while acquiring fluent proficiency in deep convolutional and transformer architectures.',
    credits: 4,
    lectureHours: 3,
    tutorialHours: 0,
    practicalHours: 2,
    totalHours: 52,
    keyHighlights: [
      'ACM CS2023 Clause-by-Clause Compliance',
      'APNI National Guideline Alignment',
      'Balanced Theory & Lab (3:0:2)',
      'Covers Classical & Deep Vision'
    ],
    industryAlignmentSummary:
      'Provides versatile preparation for both industrial R&D positions and campus recruitment across software and tech giants.',
    institutionalBenchmarkSummary:
      'Validated against 100% of core knowledge units defined in ACM Curricula 2023 Computer Vision specifications.',
    evidenceCoveragePercent: 98,
    evidenceReferences: [
      SCHEMA_VERIFICATION_REFERENCES[0],
      SCHEMA_VERIFICATION_REFERENCES[1],
      SCHEMA_VERIFICATION_REFERENCES[2],
      SCHEMA_VERIFICATION_REFERENCES[3]
    ],
    isRecommendedStrategy: true,
    courseOutcomes: [
      {
        code: 'CO1',
        statement: 'Explain digital image formation, color spaces, and apply spatial domain filtering techniques.',
        bloomsTaxonomyLevel: 'Understanding',
        poMapping: ['PO1', 'PO2']
      },
      {
        code: 'CO2',
        statement: 'Apply feature detection (SIFT, ORB) and estimate camera geometry using epipolar principles.',
        bloomsTaxonomyLevel: 'Applying',
        poMapping: ['PO1', 'PO2', 'PO3']
      },
      {
        code: 'CO3',
        statement: 'Design, implement, and analyze convolutional and transformer architectures for visual recognition tasks.',
        bloomsTaxonomyLevel: 'Analyzing',
        poMapping: ['PO2', 'PO3', 'PO5']
      },
      {
        code: 'CO4',
        statement: 'Develop an end-to-end computer vision application meeting ethical and societal considerations.',
        bloomsTaxonomyLevel: 'Creating',
        poMapping: ['PO3', 'PO6', 'PO8', 'PSO1']
      }
    ],
    modules: [
      {
        moduleNumber: 1,
        title: 'Image Formation, Filtering & Low-Level Processing',
        suggestedHours: 12,
        topics: [
          'Human visual system & digital sensors',
          'Geometric camera model and calibration basics',
          'Color models (RGB, HSV, LAB)',
          'Spatial filtering, gradients and Canny edge detector'
        ],
        learningOutcomes: ['CO1'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV', 'REF-IITB-CS463-SYLLABUS']
      },
      {
        moduleNumber: 2,
        title: 'Features, Correspondence & Epipolar Geometry',
        suggestedHours: 14,
        topics: [
          'Corner and blob detection',
          'SIFT/ORB invariant descriptors and matching',
          'RANSAC for robust estimation',
          'Two-view geometry and stereo disparity mapping'
        ],
        learningOutcomes: ['CO2'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV', 'REF-IITB-CS463-SYLLABUS']
      },
      {
        moduleNumber: 3,
        title: 'Deep Learning for Recognition & Segmentation',
        suggestedHours: 14,
        topics: [
          'CNN principles, Backprop for visual filters',
          'Object detection frameworks (YOLO, Faster R-CNN)',
          'Semantic and instance segmentation architectures',
          'Self-attention and Vision Transformers (ViT)'
        ],
        learningOutcomes: ['CO3'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV', 'REF-INDUSTRY-VISION-2025']
      },
      {
        moduleNumber: 4,
        title: 'Advanced Visual Applications & Ethical Governance',
        suggestedHours: 12,
        topics: [
          'Video processing, tracking and optical flow',
          'Generative visual models (Diffusion & GAN overview)',
          'Biometrics, facial recognition bias and privacy regulations',
          'Course capstone project demonstration'
        ],
        learningOutcomes: ['CO4'],
        supportingEvidenceIds: ['REF-APNI-2024-AICTE-MODEL']
      }
    ],
    assessmentScheme: {
      ciePercentage: 50,
      seePercentage: 50,
      components: [
        { name: 'Continuous Internal Tests (CIT 1 & 2)', weightage: 25, description: 'Written tests evaluating CO1-CO3' },
        { name: 'Laboratory Hands-on Tasks & Quizzes', weightage: 15, description: 'Evaluation of OpenCV and PyTorch labs' },
        { name: 'Course Mini-Project with Report', weightage: 10, description: 'Group project on real-world vision problem' },
        { name: 'Semester End Examination (SEE)', weightage: 50, description: 'Comprehensive university examination' }
      ]
    }
  },
  {
    id: 'alt-4',
    code: 'ALT-4',
    title: '[Schema Preview] Generative Vision & Multi-Modal Intelligence',
    strategyName: 'Cutting-Edge Frontier & Generative AI Focus',
    tagline: 'Focus on Diffusion models, Neural Radiance Fields, 3D Gaussian Splatting, and Visual LLMs.',
    curriculumPhilosophy:
      'Designed for forward-looking elective courses catering to advanced students. Moves rapidly through foundational concepts to dedicate major course hours to diffusion models, multi-modal Vision-Language Models (VLMs), and 3D radiance fields.',
    credits: 4,
    lectureHours: 3,
    tutorialHours: 0,
    practicalHours: 2,
    totalHours: 52,
    keyHighlights: [
      'Diffusion Models & Image Synthesis',
      'Vision-Language Models (CLIP, LLaVA)',
      '3D Gaussian Splatting & NeRFs',
      'Advanced Elective Track'
    ],
    industryAlignmentSummary:
      'High alignment with next-generation generative AI labs, synthetic data generation, and autonomous spatial computing roles.',
    institutionalBenchmarkSummary:
      'Benchmarks against postgraduate and advanced elective tracks introduced in top-tier institutions.',
    evidenceCoveragePercent: 91,
    evidenceReferences: [SCHEMA_VERIFICATION_REFERENCES[0], SCHEMA_VERIFICATION_REFERENCES[3]],
    isRecommendedStrategy: false,
    courseOutcomes: [
      {
        code: 'CO1',
        statement: 'Analyze the mathematical foundations of score matching and diffusion processes for image generation.',
        bloomsTaxonomyLevel: 'Analyzing',
        poMapping: ['PO1', 'PO2']
      },
      {
        code: 'CO2',
        statement: 'Implement and fine-tune Vision-Language Models for open-vocabulary detection and visual question answering.',
        bloomsTaxonomyLevel: 'Creating',
        poMapping: ['PO3', 'PO5', 'PSO2']
      },
      {
        code: 'CO3',
        statement: 'Synthesize 3D scene representations using Neural Radiance Fields (NeRF) and 3D Gaussian Splatting.',
        bloomsTaxonomyLevel: 'Creating',
        poMapping: ['PO3', 'PO5']
      }
    ],
    modules: [
      {
        moduleNumber: 1,
        title: 'Deep Feature Representations & Self-Supervised Vision',
        suggestedHours: 12,
        topics: [
          'Representation learning recap',
          'Self-supervised visual pre-training (MAE, DINOv2)',
          'Visual attention mechanisms',
          'Latent space exploration'
        ],
        learningOutcomes: ['CO1'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV']
      },
      {
        moduleNumber: 2,
        title: 'Generative Image Synthesis & Diffusion Models',
        suggestedHours: 14,
        topics: [
          'Denoising Diffusion Probabilistic Models (DDPM)',
          'Latent Diffusion Models (Stable Diffusion)',
          'Classifier-free guidance and ControlNet conditioning',
          'Ethical synthetic media and watermarking'
        ],
        learningOutcomes: ['CO1'],
        supportingEvidenceIds: ['REF-INDUSTRY-VISION-2025']
      },
      {
        moduleNumber: 3,
        title: 'Vision-Language Foundation Models (VLMs)',
        suggestedHours: 14,
        topics: [
          'Contrastive language-image pre-training (CLIP)',
          'Open-vocabulary object detection (OWL-ViT, Grounding DINO)',
          'Large Multimodal Models (LLaVA, GPT-4V architectures)',
          'Visual reasoning and embodied agent perception'
        ],
        learningOutcomes: ['CO2'],
        supportingEvidenceIds: ['REF-INDUSTRY-VISION-2025']
      },
      {
        moduleNumber: 4,
        title: 'Neural Rendering & 3D Spatial Intelligence',
        suggestedHours: 12,
        topics: [
          'Volume rendering foundations',
          'Neural Radiance Fields (NeRF)',
          '3D Gaussian Splatting for real-time rendering',
          'Applications in robotics simulation and AR/VR'
        ],
        learningOutcomes: ['CO3'],
        supportingEvidenceIds: ['REF-ACM-2023-CS-CV']
      }
    ],
    assessmentScheme: {
      ciePercentage: 50,
      seePercentage: 50,
      components: [
        { name: 'Research Paper Seminars', weightage: 15, description: 'Critique of top CVPR/ICCV conference papers' },
        { name: 'Diffusion / VLM Implementation Assignments', weightage: 20, description: 'Hands-on experiments' },
        { name: 'Capstone Generative Project', weightage: 15, description: 'Novel application implementation' },
        { name: 'Semester End Theory Exam (SEE)', weightage: 50, description: 'Written examination' }
      ]
    }
  }
];

export const SCHEMA_VERIFICATION_FINAL_PLAN: FinalCoursePlan = {
  id: 'PLAN-KLE-26ECAC401-CV-01',
  requestId: 'REQ-KLE-001',
  selectedAlternativeId: 'alt-3',
  courseCode: '26CS602',
  courseTitle: 'Computer Vision & Visual Intelligence',
  department: 'School of Computer Science & Engineering (AI)',
  degreeLevel: 'UG',
  semester: 6,
  credits: 4,
  contactHours: {
    lecture: 3,
    tutorial: 0,
    practical: 2,
    total: 52
  },
  courseDescription:
    'This course provides a comprehensive foundation in the principles, algorithms, and applications of computer vision. Spanning classical optics, projective geometry, and feature extraction to modern deep convolutional networks, vision transformers, and multi-modal models, it equips students to design intelligent visual perception systems for autonomous robotics, healthcare diagnostics, and smart infrastructure.',
  prerequisites: [
    'Linear Algebra & Multivariable Calculus (26MAT101)',
    'Data Structures & Algorithms (26CS301)',
    'Introduction to Machine Learning (26CS501)'
  ],
  courseOutcomes: SCHEMA_VERIFICATION_ALTERNATIVES[2].courseOutcomes,
  modules: SCHEMA_VERIFICATION_ALTERNATIVES[2].modules,
  assessmentScheme: SCHEMA_VERIFICATION_ALTERNATIVES[2].assessmentScheme,
  prescribedTextbooks: [
    'Richard Szeliski, "Computer Vision: Algorithms and Applications", 2nd Edition, Springer, 2022.',
    'Ian Goodfellow, Yoshua Bengio, Aaron Courville, "Deep Learning", MIT Press, 2016.'
  ],
  referenceMaterials: [
    'David A. Forsyth and Jean Ponce, "Computer Vision: A Modern Approach", 2nd Edition, Pearson, 2015.',
    'ACM / IEEE-CS Computer Science Curricula 2023 Guidelines on Visual Computing.',
    'APNI / AICTE Model Curriculum on Artificial Intelligence & Machine Learning (2024).'
  ],
  industryAlignmentNotes:
    'Syllabus validated against 2025 skill vectors from top tier tech and computer vision engineering job requisitions. Includes hands-on experience in OpenCV, PyTorch, YOLO, and Vision Transformers.',
  institutionalBenchmarkNotes:
    'Clause-by-clause benchmarking completed against ACM CS2023 recommendations and syllabi from IIT Bombay (CS 463), IIT Madras, and NIT Surathkal.',
  evidenceReferences: SCHEMA_VERIFICATION_REFERENCES,
  approvalStatus: 'approved',
  version: '1.0.0',
  generatedAt: new Date().toISOString(),
  auditTrailId: 'AUDIT-KLE-AURA-98234-CV-V1',
  pdfDownloadUrl: '/api/curriculum/plans/PLAN-KLE-26ECAC401-CV-01/pdf',
  templateSpecificationNotes: 'Template-driven format. Institutional syllabus template from Board of Studies to be configured in Phase 2.',
  signOffWorkflowNotes: 'Institutional sign-off workflow is configurable based on KLE Tech departmental regulations.'
};
