import { computed, inject, onMounted, onUnmounted, provide, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import classroomApi from '../services/classroomApi';
import authApi from '../services/authApi';
import { clearAuth } from '../stores/auth';

export const TEACHER_CLASSROOM_KEY = Symbol('teacherClassroom');

export function createTeacherClassroom() {
  const router = useRouter();
  const students = ref([]);
  const tasks = ref([]);
  const currentTaskId = ref('');
  const subjectFilter = ref('all');
  const isNewTask = ref(false);
  const isEditingTask = ref(false);
  const dashboard = ref(null);
  const submissions = ref([]);
  const toast = ref('');
  const toastOk = ref(true);
  let toastTimer = null;

  function showToast(message, ok = true, duration = 3000) {
    if (toastTimer) {
      clearTimeout(toastTimer);
      toastTimer = null;
    }
    toast.value = message;
    toastOk.value = ok;
    if (message) {
      toastTimer = setTimeout(() => {
        toast.value = '';
        toastTimer = null;
      }, duration);
    }
  }

  onUnmounted(() => {
    if (toastTimer) clearTimeout(toastTimer);
  });
  const gradeForms = reactive({});
  const aiInsights = reactive({});
  const aiLoading = reactive({});
  const taskDraftPrompt = ref('');
  const taskDraftAiResult = ref(null);
  const taskDraftAiLoading = ref(false);

  const taskForm = reactive({
    title: '',
    description: '',
    subject: '',
    targetMajor: '',
    targetGrade: '',
    targetClassName: '',
    startTime: '',
    endTime: '',
    subTaskText: '',
  });

  function uniqueStudentField(field) {
    const set = new Set();
    for (const s of students.value) {
      const v = (s[field] || '').trim();
      if (v) set.add(v);
    }
    return [...set].sort((a, b) => a.localeCompare(b, 'zh-CN'));
  }

  const targetMajorOptions = computed(() => uniqueStudentField('major'));
  const targetGradeOptions = computed(() => uniqueStudentField('grade'));
  const targetClassNameOptions = computed(() => uniqueStudentField('className'));

  function formatTaskTarget(task) {
    if (!task) return '';
    const parts = [task.targetMajor, task.targetGrade, task.targetClassName].filter(Boolean);
    return parts.length ? parts.join(' · ') : '未指定布置对象';
  }

  const summaryLabels = {
    studentCount: '学生',
    subTaskCount: '子任务',
    submittedCount: '已提交',
    availableCount: '可提交',
    reminderCount: '需提醒',
  };

  const summaryIcons = {
    studentCount: 'fas fa-users',
    subTaskCount: 'fas fa-tasks',
    submittedCount: 'fas fa-file-upload',
    availableCount: 'fas fa-door-open',
    reminderCount: 'fas fa-bell',
  };

  const currentTask = computed(() => tasks.value.find((t) => t.taskId === currentTaskId.value));

  const availableSubjects = computed(() => {
    const counts = new Map();
    for (const t of tasks.value) {
      const key = (t.subject || '').trim() || '__unset__';
      counts.set(key, (counts.get(key) || 0) + 1);
    }
    return [...counts.entries()]
      .map(([value, count]) => ({
        value,
        label: value === '__unset__' ? '未设置科目' : value,
        count,
      }))
      .sort((a, b) => {
        if (a.value === '__unset__') return 1;
        if (b.value === '__unset__') return -1;
        return a.label.localeCompare(b.label, 'zh-CN');
      });
  });

  const tasksBySubjectFilter = computed(() => {
    if (subjectFilter.value === 'all') return tasks.value;
    if (subjectFilter.value === '__unset__') {
      return tasks.value.filter((t) => !(t.subject || '').trim());
    }
    return tasks.value.filter((t) => (t.subject || '').trim() === subjectFilter.value);
  });

  function syncTaskToSubjectFilter() {
    const list = tasksBySubjectFilter.value;
    if (!list.length) {
      currentTaskId.value = '';
      dashboard.value = null;
      submissions.value = [];
      return;
    }
    if (!list.some((t) => t.taskId === currentTaskId.value)) {
      currentTaskId.value = list[0].taskId;
      refreshDashboard();
    }
  }

  function setSubjectFilter(value) {
    if (subjectFilter.value === value) return;
    subjectFilter.value = value;
    syncTaskToSubjectFilter();
  }

  const gradingStats = computed(() => {
    const list = submissions.value;
    return {
      total: list.length,
      pending: list.filter((s) => s.status === 'SUBMITTED').length,
      graded: list.filter((s) => s.status === 'GRADED').length,
      rejected: list.filter((s) => s.status === 'REJECTED').length,
    };
  });

  const classCompletionPercent = computed(() => {
    const rows = dashboard.value?.rows;
    if (!rows?.length) return 0;
    let sum = 0;
    for (const row of rows) {
      sum += studentProgressPercent(row);
    }
    return Math.round(sum / rows.length);
  });

  function studentProgressPercent(row) {
    const cells = row?.cells || [];
    if (!cells.length) return 0;
    const done = cells.filter((c) => !['AVAILABLE', 'LOCKED'].includes(c.state)).length;
    return Math.round((done / cells.length) * 100);
  }

  function studentDashboardRow(studentId) {
    return dashboard.value?.rows?.find((r) => r.studentId === studentId) || null;
  }

  function studentInitial(name) {
    const s = (name || '?').trim();
    return s ? s.charAt(0) : '?';
  }

  function subTaskTitle(subTaskId) {
    const st = dashboard.value?.subTasks?.find((s) => s.subTaskId === subTaskId);
    return st?.title || '';
  }

  function formatSubmittedAt(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return String(iso);
    return d.toLocaleString('zh-CN');
  }

  const statusLabels = {
    SUBMITTED: '待批改',
    GRADED: '已批改',
    REJECTED: '已打回',
  };

  function submissionStatusLabel(status) {
    return statusLabels[status] || status;
  }

  async function loadStudents() {
    students.value = await classroomApi.listStudents();
  }

  function apiErrorMessage(e, fallback) {
    return e?.response?.data?.message || e?.message || fallback;
  }

  async function saveStudent(payload, studentUserId = null) {
    try {
      const row = studentUserId
        ? await classroomApi.updateStudent(studentUserId, payload)
        : await classroomApi.createStudent(payload);
      await loadStudents();
      if (currentTaskId.value) await refreshDashboard();
      showToast(studentUserId ? '学生信息已更新' : '学生已添加');
      return row;
    } catch (e) {
      showToast(apiErrorMessage(e, '保存失败'), false, 4000);
      return null;
    }
  }

  async function removeStudent(studentUserId) {
    try {
      await classroomApi.deleteStudent(studentUserId);
      await loadStudents();
      if (currentTaskId.value) await refreshDashboard();
      showToast('学生已删除');
      return true;
    } catch (e) {
      showToast(apiErrorMessage(e, '删除失败'), false, 4000);
      return false;
    }
  }

  function resetTaskForm() {
    taskForm.title = '';
    taskForm.description = '';
    taskForm.subject = '';
    taskForm.targetMajor = '';
    taskForm.targetGrade = '';
    taskForm.targetClassName = '';
    taskForm.startTime = '';
    taskForm.endTime = '';
    taskForm.subTaskText = '';
    clearTaskDraftAssist();
  }

  function toDatetimeLocal(value) {
    if (!value) return '';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return '';
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function fromDatetimeLocal(value) {
    const v = (value || '').trim();
    return v || null;
  }

  function formatTaskDateTime(value) {
    if (!value) return '—';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return String(value);
    return d.toLocaleString('zh-CN', { hour12: false });
  }

  function clearTaskDraftAssist() {
    taskDraftPrompt.value = '';
    taskDraftAiResult.value = null;
    taskDraftAiLoading.value = false;
  }

  function buildTaskDraftPrompt(task) {
    if (!task) return '';
    const title = (task.title || '').trim();
    const description = (task.description || '').trim();
    const subTitles = (task.subTasks || [])
      .map((st) => (st.title || '').trim())
      .filter(Boolean);
    const segments = [];
    if (title) segments.push(`任务「${title}」`);
    if (task.subject) segments.push(`科目：${task.subject}`);
    if (description) segments.push(description);
    if (task.startTime || task.endTime) {
      const window = [formatTaskDateTime(task.startTime), formatTaskDateTime(task.endTime)].join(' 至 ');
      segments.push(`时间：${window}`);
    }
    if (subTitles.length) segments.push(`子任务：${subTitles.join('、')}`);
    if (!segments.length) return '';
    segments.push('请在此基础上优化完善任务结构与表述');
    return segments.join('；');
  }

  function initTaskDraftAssistForEdit(task) {
    taskDraftAiResult.value = null;
    taskDraftAiLoading.value = false;
    taskDraftPrompt.value = buildTaskDraftPrompt(task);
  }

  function loadTaskIntoForm(task) {
    if (!task) return;
    taskForm.title = task.title || '';
    taskForm.description = task.description || '';
    taskForm.subject = task.subject || '';
    taskForm.targetMajor = task.targetMajor || '';
    taskForm.targetGrade = task.targetGrade || '';
    taskForm.targetClassName = task.targetClassName || '';
    taskForm.startTime = toDatetimeLocal(task.startTime);
    taskForm.endTime = toDatetimeLocal(task.endTime);
    taskForm.subTaskText = (task.subTasks || []).map((st) => st.title).join('\n');
    isNewTask.value = false;
  }

  function selectTask(task) {
    const switching = task.taskId !== currentTaskId.value;
    const keepEditing = isEditingTask.value && switching;
    currentTaskId.value = task.taskId;
    isNewTask.value = false;
    loadTaskIntoForm(task);
    isEditingTask.value = keepEditing;
    if (keepEditing) {
      initTaskDraftAssistForEdit(task);
    } else if (switching) {
      clearTaskDraftAssist();
    }
  }

  function startEditTask() {
    if (!currentTask.value) return;
    loadTaskIntoForm(currentTask.value);
    isEditingTask.value = true;
    initTaskDraftAssistForEdit(currentTask.value);
  }

  function cancelEditTask() {
    isEditingTask.value = false;
    clearTaskDraftAssist();
    if (currentTask.value) {
      loadTaskIntoForm(currentTask.value);
    }
  }

  function startNewTask() {
    currentTaskId.value = '';
    isNewTask.value = true;
    isEditingTask.value = false;
    resetTaskForm();
  }

  function beginCreateTask() {
    currentTaskId.value = '';
    isNewTask.value = true;
    isEditingTask.value = true;
    resetTaskForm();
  }

  function cancelCreateTask() {
    isEditingTask.value = false;
    resetTaskForm();
    if (tasks.value.length) {
      isNewTask.value = false;
      selectTask(tasks.value[tasks.value.length - 1]);
    }
  }

  function buildTaskPayload(publish) {
    const lines = taskForm.subTaskText.split('\n').map((l) => l.trim()).filter(Boolean);
    const subTasks = lines.map((title) => ({ title, description: `请提交「${title}」成果物。` }));
    const startTime = fromDatetimeLocal(taskForm.startTime);
    const endTime = fromDatetimeLocal(taskForm.endTime);
    if (startTime && endTime && new Date(endTime) < new Date(startTime)) {
      showToast('截至时间不能早于开始时间', false);
      return null;
    }
    return {
      title: taskForm.title.trim(),
      description: taskForm.description.trim(),
      subject: taskForm.subject.trim() || null,
      targetMajor: taskForm.targetMajor.trim() || null,
      targetGrade: taskForm.targetGrade.trim() || null,
      targetClassName: taskForm.targetClassName.trim() || null,
      startTime,
      endTime,
      subTasks,
      publish,
    };
  }

  async function loadTasks() {
    tasks.value = await classroomApi.listTeacherTasks();
    if (!tasks.value.length) {
      currentTaskId.value = '';
      isNewTask.value = true;
      isEditingTask.value = false;
      resetTaskForm();
      return;
    }

    const selected = tasks.value.find((t) => t.taskId === currentTaskId.value);
    if (selected && !isNewTask.value) {
      syncTaskToSubjectFilter();
      loadTaskIntoForm(tasks.value.find((t) => t.taskId === currentTaskId.value) || selected);
      return;
    }

    if (!isNewTask.value && !currentTaskId.value) {
      selectTask(tasksBySubjectFilter.value[0] || tasks.value[tasks.value.length - 1]);
      await refreshDashboard();
      return;
    }

    syncTaskToSubjectFilter();
  }

  async function openTasksPage() {
    if (!students.value.length) {
      await loadStudents();
    }
    if (!tasks.value.length) {
      await loadTasks();
    }
    if (!tasks.value.length || isNewTask.value || currentTaskId.value) return;
    selectTask(tasks.value[tasks.value.length - 1]);
    await refreshDashboard();
  }

  async function init() {
    await loadStudents();
    await loadTasks();
    if (tasks.value.length && isNewTask.value && !isEditingTask.value && !currentTaskId.value) {
      selectTask(tasks.value[tasks.value.length - 1]);
      await refreshDashboard();
    }
  }

  async function saveTask(publish) {
    if (!taskForm.title.trim()) {
      showToast('请填写任务标题', false);
      return;
    }
    if (!taskForm.targetMajor.trim() || !taskForm.targetGrade.trim() || !taskForm.targetClassName.trim()) {
      showToast('请选择布置对象（专业、年级、班级）', false);
      return;
    }
    const payload = buildTaskPayload(publish);
    if (!payload) return;
    try {
      let task;
      if (currentTaskId.value && !isNewTask.value) {
        task = await classroomApi.updateTask(currentTaskId.value, payload);
        if (publish && currentTask.value?.published) {
          showToast('任务已更新');
        } else if (publish) {
          showToast('任务已发布');
        } else {
          showToast('草稿已保存');
        }
      } else {
        task = await classroomApi.createTask(payload);
        showToast(publish ? '任务已发布' : '草稿已保存');
      }
      currentTaskId.value = task.taskId;
      isNewTask.value = false;
      isEditingTask.value = false;
      await loadTasks();
      await refreshDashboard();
    } catch (e) {
      showToast(e.message, false, 4000);
    }
  }

  async function deleteTask() {
    if (!currentTaskId.value || isNewTask.value) return;
    if (!window.confirm('确定删除该任务？相关提交记录将一并删除，且不可恢复。')) return;
    try {
      await classroomApi.deleteTask(currentTaskId.value);
      showToast('任务已删除');
      await loadTasks();
      if (tasks.value.length) {
        selectTask(tasks.value[tasks.value.length - 1]);
      } else {
        startNewTask();
      }
    } catch (e) {
      showToast(e.message, false, 4000);
    }
  }

  async function createTask(publish) {
    await saveTask(publish);
  }

  async function publishCurrent() {
    await classroomApi.publishTask(currentTaskId.value);
    showToast('已发布');
    isEditingTask.value = false;
    await loadTasks();
  }

  async function refreshDashboard() {
    if (!currentTaskId.value) return;
    dashboard.value = await classroomApi.dashboard(currentTaskId.value);
    submissions.value = await classroomApi.listSubmissions(currentTaskId.value);
    for (const s of submissions.value) {
      if (!gradeForms[s.submissionId]) {
        gradeForms[s.submissionId] = { score: s.score ?? '', comment: s.teacherComment ?? '' };
      }
    }
  }

  async function sendStudentReminder(studentUserId, subTaskId = null) {
    if (!currentTaskId.value) {
      showToast('请先选择任务', false);
      return null;
    }
    if (!studentUserId) {
      showToast('学生信息无效', false);
      return null;
    }
    try {
      const result = await classroomApi.sendStudentReminder(currentTaskId.value, {
        studentUserId,
        subTaskId: subTaskId || undefined,
      });
      if (result?.notRemindable) {
        showToast('该子任务当前无需催交', false);
        return result;
      }
      const name = result?.studentName || '学生';
      if (result?.emailSent) {
        showToast(`已向 ${name} 发送催交（站内 + 邮件）`);
      } else if (result?.inAppSent) {
        showToast(`已向 ${name} 发送站内催交${result?.reason ? '（未配置邮箱）' : ''}`);
      } else {
        showToast('催交发送失败', false);
      }
      return result;
    } catch (e) {
      showToast(e.message || '催交发送失败', false, 4000);
      return null;
    }
  }

  async function fetchTaskDraftAssist() {
    const prompt = taskDraftPrompt.value.trim();
    if (!prompt) {
      showToast('请先描述你想布置的任务', false);
      return null;
    }
    taskDraftAiLoading.value = true;
    try {
      const data = await classroomApi.taskDraftAssist({
        prompt,
        title: taskForm.title,
        description: taskForm.description,
        subTaskText: taskForm.subTaskText,
      });
      taskDraftAiResult.value = data;
      return data;
    } catch (e) {
      showToast(e.message || 'AI 生成失败', false, 4000);
      return null;
    } finally {
      taskDraftAiLoading.value = false;
    }
  }

  function applyTaskDraftAssist() {
    const draft = taskDraftAiResult.value;
    if (!draft) return;
    const hasContent =
      taskForm.title.trim() || taskForm.description.trim() || taskForm.subTaskText.trim();
    if (hasContent && !window.confirm('将用 AI 草稿覆盖当前表单内容，是否继续？')) {
      return;
    }
    taskForm.title = draft.title || '';
    taskForm.description = draft.description || '';
    const lines = (draft.subTasks || []).map((st) => st.title).filter(Boolean);
    taskForm.subTaskText = lines.join('\n');
    showToast('已填入 AI 草稿，请检查后保存或发布');
  }

  function setTaskDraftPrompt(text) {
    taskDraftPrompt.value = text;
  }

  async function fetchGradingAssist(submissionId, { force = false } = {}) {
    if (!submissionId) return null;
    if (!force && aiInsights[submissionId]) {
      return aiInsights[submissionId];
    }
    aiLoading[submissionId] = true;
    try {
      const data = await classroomApi.gradingAssist(submissionId);
      aiInsights[submissionId] = data;
      return data;
    } catch (e) {
      showToast(e.message || 'AI 审阅失败', false, 4000);
      return null;
    } finally {
      aiLoading[submissionId] = false;
    }
  }

  function applyAiSuggestion(submissionId) {
    const insight = aiInsights[submissionId];
    if (!insight || !gradeForms[submissionId]) return;
    if (insight.suggestedScore != null && insight.suggestedScore !== '') {
      gradeForms[submissionId].score = insight.suggestedScore;
    }
    if (insight.suggestedComment) {
      gradeForms[submissionId].comment = insight.suggestedComment;
    }
    showToast('已填入 AI 建议，请确认后保存');
  }

  async function batchGradingAssist(pendingList) {
    const list = pendingList || submissions.value.filter((s) => s.status === 'SUBMITTED');
    for (const sub of list) {
      await fetchGradingAssist(sub.submissionId, { force: true });
    }
    showToast(`已完成 ${list.length} 份 AI 预审`);
  }

  async function grade(sub) {
    const f = gradeForms[sub.submissionId];
    try {
      await classroomApi.grade({
        submissionId: sub.submissionId,
        score: Number(f.score),
        comment: f.comment,
      });
      showToast('批改成功');
      await refreshDashboard();
    } catch (e) {
      showToast(e.message, false, 4000);
    }
  }

  async function reject(sub) {
    const f = gradeForms[sub.submissionId];
    try {
      await classroomApi.reject({
        submissionId: sub.submissionId,
        comment: f.comment || '请修改后重新提交',
      });
      showToast('已打回，学生可重交');
      await refreshDashboard();
    } catch (e) {
      showToast(e.message, false, 4000);
    }
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      clearAuth();
      router.push('/login');
    }
  }

  return {
    students,
    tasks,
    subjectFilter,
    availableSubjects,
    tasksBySubjectFilter,
    setSubjectFilter,
    syncTaskToSubjectFilter,
    currentTaskId,
    isNewTask,
    isEditingTask,
    dashboard,
    submissions,
    toast,
    toastOk,
    gradeForms,
    aiInsights,
    aiLoading,
    taskDraftPrompt,
    taskDraftAiResult,
    taskDraftAiLoading,
    taskForm,
    targetMajorOptions,
    targetGradeOptions,
    targetClassNameOptions,
    formatTaskTarget,
    summaryLabels,
    summaryIcons,
    currentTask,
    gradingStats,
    classCompletionPercent,
    studentProgressPercent,
    studentDashboardRow,
    studentInitial,
    subTaskTitle,
    formatSubmittedAt,
    formatTaskDateTime,
    submissionStatusLabel,
    loadStudents,
    saveStudent,
    removeStudent,
    loadTasks,
    openTasksPage,
    init,
    saveTask,
    createTask,
    deleteTask,
    selectTask,
    startEditTask,
    cancelEditTask,
    beginCreateTask,
    cancelCreateTask,
    startNewTask,
    loadTaskIntoForm,
    resetTaskForm,
    publishCurrent,
    refreshDashboard,
    sendStudentReminder,
    grade,
    reject,
    fetchTaskDraftAssist,
    applyTaskDraftAssist,
    setTaskDraftPrompt,
    clearTaskDraftAssist,
    fetchGradingAssist,
    applyAiSuggestion,
    batchGradingAssist,
    logout,
    showToast,
  };
}

export function provideTeacherClassroom() {
  const classroom = createTeacherClassroom();
  provide(TEACHER_CLASSROOM_KEY, classroom);
  onMounted(() => {
    classroom.init();
  });
  return classroom;
}

export function useTeacherClassroom() {
  const ctx = inject(TEACHER_CLASSROOM_KEY);
  if (!ctx) {
    throw new Error('useTeacherClassroom must be used within TeacherLayout');
  }
  return ctx;
}
