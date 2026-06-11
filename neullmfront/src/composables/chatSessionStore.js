import { getUser } from '@/stores/auth';

const memory = new Map();
const KEY_PREFIX = 'neullm_chat_';
const MAX_MESSAGES = 120;

/** @param {boolean} teacherMode */
export function chatSessionKey(teacherMode) {
  const u = getUser();
  const uid = u?.id ?? u?.username ?? u?.userId ?? 'anon';
  return `${KEY_PREFIX}${uid}_${teacherMode ? 'teacher' : 'student'}`;
}

function isEphemeralMessage(m) {
  if (!m || typeof m !== 'object') return true;
  if (m.isStreaming) return true;
  if (m.isMessagesSummary && m.messagesSummaryLoading) return true;
  if (m.isAssignmentsSummary && m.assignmentsSummaryLoading) return true;
  if (m.isTeacherSubmissionsSummary && m.teacherSubmissionsSummaryLoading) return true;
  if (m.isTeacherClassInsightSummary && m.teacherClassInsightSummaryLoading) return true;
  return !!(
    m.isLearningReminderForm ||
    m.isEmailReminderForm ||
    m.isUserProfileForm ||
    m.isReviewRhythmForm ||
    m.isClassroomTaskConfirm ||
    m.isClassroomDocImport
  );
}

/** @param {unknown[]} list */
function sanitizeMessages(list) {
  if (!Array.isArray(list)) return [];
  return list
    .filter((m) => !isEphemeralMessage(m))
    .slice(-MAX_MESSAGES)
    .map((m) => {
      const copy = { ...m };
      copy.isStreaming = false;
      return copy;
    });
}

/**
 * @param {boolean} teacherMode
 * @returns {unknown[] | null}
 */
export function loadChatSession(teacherMode) {
  const key = chatSessionKey(teacherMode);
  if (memory.has(key)) {
    const cached = memory.get(key);
    return Array.isArray(cached) && cached.length ? cached : null;
  }
  try {
    const raw = sessionStorage.getItem(key);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed) || parsed.length === 0) return null;
    memory.set(key, parsed);
    return parsed;
  } catch {
    return null;
  }
}

/** @param {boolean} teacherMode @param {unknown[]} messages */
export function saveChatSession(teacherMode, messages) {
  const key = chatSessionKey(teacherMode);
  const sanitized = sanitizeMessages(messages);
  if (sanitized.length === 0) {
    memory.delete(key);
    try {
      sessionStorage.removeItem(key);
    } catch {
      /* ignore */
    }
    return;
  }
  memory.set(key, sanitized);
  try {
    sessionStorage.setItem(key, JSON.stringify(sanitized));
  } catch {
    /* quota exceeded — memory cache still helps within tab */
  }
}

export function clearChatSession(teacherMode) {
  const key = chatSessionKey(teacherMode);
  memory.delete(key);
  try {
    sessionStorage.removeItem(key);
  } catch {
    /* ignore */
  }
}

export function clearAllChatSessions() {
  memory.clear();
  try {
    for (let i = sessionStorage.length - 1; i >= 0; i -= 1) {
      const k = sessionStorage.key(i);
      if (k?.startsWith(KEY_PREFIX)) sessionStorage.removeItem(k);
    }
  } catch {
    /* ignore */
  }
}
