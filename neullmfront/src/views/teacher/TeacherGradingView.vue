<template>
  <div class="t-page t-grade-page">
    <header class="t-ws-head t-grade-head">
      <div class="t-ws-head__main">
        <div class="t-grade-head__title-row">
          <h1>批改作业</h1>
          <span v-if="gradingStats.pending" class="t-grade-live-badge">
            <i class="fas fa-circle"></i>{{ gradingStats.pending }} 待处理
          </span>
        </div>
        <p>AI 先总结提交内容与学情，再给分数与评语建议；你确认后再保存</p>
      </div>
      <div class="t-ws-head__actions">
        <TeacherWorkspaceContext
          v-model="currentTaskId"
          :tasks="tasksBySubjectFilter"
          refreshable
          @change="refreshDashboard"
          @refresh="refreshDashboard"
        >
          <button
            v-if="gradingStats.pending"
            type="button"
            class="t-btn-solid t-btn-solid--ai t-grade-batch-btn"
            :disabled="batchAiRunning"
            @click="runBatchAi"
          >
            <i class="fas fa-wand-magic-sparkles"></i>
            {{ batchAiRunning ? 'AI 预审中…' : `AI 预审 (${gradingStats.pending})` }}
          </button>
        </TeacherWorkspaceContext>
      </div>
    </header>

    <section v-if="currentTaskId && submissions.length" class="t-grade-command">
      <div class="t-grade-command__stats">
        <div class="t-grade-stat" data-tone="pending">
          <span class="t-grade-stat__val">{{ gradingStats.pending }}</span>
          <span class="t-grade-stat__lbl">待批改</span>
        </div>
        <div class="t-grade-stat" data-tone="graded">
          <span class="t-grade-stat__val">{{ gradingStats.graded }}</span>
          <span class="t-grade-stat__lbl">已批改</span>
        </div>
        <div class="t-grade-stat" data-tone="rejected">
          <span class="t-grade-stat__val">{{ gradingStats.rejected }}</span>
          <span class="t-grade-stat__lbl">已打回</span>
        </div>
        <div class="t-grade-stat" data-tone="total">
          <span class="t-grade-stat__val">{{ gradingStats.total }}</span>
          <span class="t-grade-stat__lbl">总提交</span>
        </div>
      </div>

      <div class="t-grade-command__toolbar">
        <div class="t-filter-tabs t-grade-view-tabs" role="tablist" aria-label="展示方式">
          <button
            type="button"
            class="t-filter-tab"
            :class="{ 't-filter-tab--on': viewMode === 'matrix' }"
            @click="viewMode = 'matrix'"
          >
            <i class="fas fa-th"></i>矩阵
          </button>
          <button
            type="button"
            class="t-filter-tab"
            :class="{ 't-filter-tab--on': viewMode === 'list' }"
            @click="viewMode = 'list'"
          >
            <i class="fas fa-list"></i>列表
          </button>
        </div>

        <div class="t-grade-command__filters" role="tablist">
          <button
            v-for="tab in gradeTabs"
            :key="tab.key"
            type="button"
            class="t-grade-filter"
            :class="{ 't-grade-filter--on': statusFilter === tab.key }"
            @click="statusFilter = tab.key"
          >
            {{ tab.label }}
            <em>{{ tab.count }}</em>
          </button>
        </div>
      </div>
    </section>

    <div class="t-ws-body t-ws-body--scroll t-grade-body">
      <section
        v-if="viewMode === 'matrix' && matrixRows.length && subTasks.length"
        class="t-prog-matrix t-grade-matrix"
      >
        <div class="t-prog-matrix__scroll">
          <table class="t-prog-matrix__table">
            <thead>
              <tr>
                <th class="t-prog-matrix__col-student">学生</th>
                <th
                  v-for="(st, idx) in subTasks"
                  :key="st.subTaskId"
                  class="t-prog-matrix__col-task"
                >
                  <span class="t-prog-matrix__th-index">子任务 {{ idx + 1 }}</span>
                  <span class="t-prog-matrix__th-title">{{ st.title }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in matrixRows"
                :key="row.studentId"
                class="t-prog-matrix__row"
              >
                <td class="t-prog-matrix__col-student">
                  <div class="t-prog-matrix__student">
                    <span :class="['t-avatar t-avatar--sm', `t-avatar--${studentColorIdx(row)}`]">
                      {{ studentInitial(row.studentName) }}
                    </span>
                    <div class="t-prog-matrix__student-text">
                      <span class="t-prog-matrix__name">{{ row.studentName }}</span>
                      <span class="t-prog-matrix__sid">{{ row.studentId }}</span>
                    </div>
                  </div>
                </td>
                <td
                  v-for="st in subTasks"
                  :key="st.subTaskId"
                  class="t-prog-matrix__col-task t-grade-matrix__cell"
                  :class="{
                    't-grade-matrix__cell--interactive': !!resolveSubmission(row, st.subTaskId),
                    't-grade-matrix__cell--dim': !matchesFilter(resolveSubmission(row, st.subTaskId)),
                  }"
                  :title="matrixCellTitle(resolveSubmission(row, st.subTaskId))"
                  @click="onMatrixCellClick(row, st.subTaskId)"
                >
                  <span
                    v-if="resolveSubmission(row, st.subTaskId)"
                    class="t-prog-matrix__chip t-grade-matrix__chip"
                    :class="matrixChipClass(resolveSubmission(row, st.subTaskId))"
                  >
                    <span class="t-prog-matrix__chip-label">
                      <i :class="submissionIcon(resolveSubmission(row, st.subTaskId).status)"></i>
                      {{ matrixCellLabel(row, st.subTaskId, resolveSubmission(row, st.subTaskId)) }}
                    </span>
                  </span>
                  <span
                    v-else
                    class="t-prog-matrix__chip t-prog-matrix__chip--locked t-grade-matrix__chip--empty"
                  >
                    <span class="t-prog-matrix__chip-label">
                      <i class="fas fa-minus"></i>未提交
                    </span>
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="t-prog-matrix__legend">
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--submitted">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-paper-plane"></i>待批改</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--graded">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-check"></i>已批改</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--warn">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-undo"></i>已打回</span>
          </span>
          <span class="t-prog-matrix__legend-hint">
            <i class="fas fa-hand-pointer"></i>点击单元格打开批改卡片
          </span>
        </footer>
      </section>

      <div v-else-if="viewMode === 'list' && filteredSubmissions.length" class="t-grade-feed">
        <TeacherGradingCard
          v-for="(sub, idx) in filteredSubmissions"
          :key="sub.submissionId"
          :sub="sub"
          :idx="idx"
        />
      </div>

      <div v-else-if="submissions.length" class="t-empty-state t-grade-empty">
        <div class="t-empty-state__art"><i class="fas fa-filter"></i></div>
        <h2>该状态下暂无提交</h2>
        <p>切换筛选标签查看其他状态的作业</p>
        <button type="button" class="t-btn-ghost" @click="statusFilter = 'all'">查看全部</button>
      </div>

      <div v-else class="t-empty-state t-grade-empty">
        <div class="t-empty-state__art"><i class="fas fa-inbox"></i></div>
        <h2>暂无学生提交</h2>
        <p v-if="currentTask">
          任务「{{ currentTask.title }}」已就绪，学生提交后将出现在此处。
        </p>
        <p v-else>请先选择或创建课堂任务。</p>
        <div v-if="dashboard?.summary" class="t-summary-strip">
          <span class="t-summary-chip t-summary-chip--brand">{{ dashboard.summary.studentCount }} 名学生</span>
          <span class="t-summary-chip">{{ dashboard.summary.subTaskCount }} 个子任务</span>
          <span class="t-summary-chip t-summary-chip--success">班级完成 {{ classCompletionPercent }}%</span>
        </div>
        <router-link to="/teacher/tasks" class="t-btn-ghost">去发布任务</router-link>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="modalOpen && activeSubmission"
        class="teacher-app classroom-theme t-grade-modal-portal"
      >
        <div
          class="t-grade-modal"
          role="dialog"
          aria-modal="true"
          :aria-label="`${activeSubmission.studentName} 的作业批改`"
          @click.self="closeModal"
        >
          <div class="t-grade-modal__panel" @click.stop>
            <header class="t-grade-modal__head">
              <span class="t-grade-modal__title">
                批改详情 · {{ activeSubmission.studentName }}
              </span>
              <button type="button" class="t-grade-modal__close" aria-label="关闭" @click="closeModal">
                <i class="fas fa-times"></i>
              </button>
            </header>
            <div class="t-grade-modal__body">
              <TeacherGradingCard
                :sub="activeSubmission"
                :idx="activeSubmissionIdx"
                @graded="onModalAction"
                @rejected="onModalAction"
              />
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import TeacherWorkspaceContext from '../../components/teacher/TeacherWorkspaceContext.vue';
import TeacherGradingCard from '../../components/teacher/TeacherGradingCard.vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const {
  tasks,
  tasksBySubjectFilter,
  currentTaskId,
  currentTask,
  dashboard,
  submissions,
  gradingStats,
  classCompletionPercent,
  studentInitial,
  submissionStatusLabel,
  gradeForms,
  refreshDashboard,
  batchGradingAssist,
} = useTeacherClassroom();

const route = useRoute();
const statusFilter = ref('all');
const viewMode = ref('matrix');
const batchAiRunning = ref(false);
const modalOpen = ref(false);
const activeSubmission = ref(null);
const activeSubmissionIdx = ref(0);

const subTasks = computed(() => dashboard.value?.subTasks || currentTask.value?.subTasks || []);

const submissionById = computed(() => {
  const map = new Map();
  for (const s of submissions.value) {
    map.set(s.submissionId, s);
  }
  return map;
});

const submissionMap = computed(() => {
  const map = new Map();
  for (const s of submissions.value) {
    if (s.studentNo) map.set(`${s.studentNo}:${s.subTaskId}`, s);
    if (s.studentName) map.set(`${s.studentName}:${s.subTaskId}`, s);
    if (s.studentUserId) map.set(`${s.studentUserId}:${s.subTaskId}`, s);
  }
  return map;
});

const gradeTabs = computed(() => [
  { key: 'pending', label: '待批改', count: gradingStats.value.pending },
  { key: 'graded', label: '已批改', count: gradingStats.value.graded },
  { key: 'rejected', label: '已打回', count: gradingStats.value.rejected },
  { key: 'all', label: '全部', count: gradingStats.value.total },
]);

const filteredSubmissions = computed(() => {
  let list = submissions.value;
  if (statusFilter.value === 'pending') list = list.filter((s) => s.status === 'SUBMITTED');
  else if (statusFilter.value === 'graded') list = list.filter((s) => s.status === 'GRADED');
  else if (statusFilter.value === 'rejected') list = list.filter((s) => s.status === 'REJECTED');
  const sid = route.query.student;
  if (sid) {
    const q = String(sid).toLowerCase();
    list = list.filter(
      (s) =>
        String(s.studentNo || '').toLowerCase() === q
        || String(s.studentId || '').toLowerCase() === q,
    );
  }
  return list;
});

function applyGradingQuery() {
  const status = route.query.status;
  if (status === 'pending' || status === 'graded' || status === 'rejected' || status === 'all') {
    statusFilter.value = status;
  }
}

const matrixRows = computed(() => {
  const rows = dashboard.value?.rows || [];
  if (rows.length) {
    return [...rows].sort((a, b) => (a.studentName || '').localeCompare(b.studentName || '', 'zh-CN'));
  }
  const seen = new Map();
  for (const s of submissions.value) {
    const key = s.studentNo || s.studentName;
    if (!seen.has(key)) {
      seen.set(key, {
        studentId: s.studentNo || key,
        studentName: s.studentName || key,
      });
    }
  }
  return [...seen.values()].sort((a, b) => (a.studentName || '').localeCompare(b.studentName || '', 'zh-CN'));
});

function getDashboardCell(row, subTaskId) {
  return (row.cells || []).find((c) => c.subTaskId === subTaskId) || null;
}

function findSubmission(row, subTaskId) {
  const sid = row.studentId || row.studentNo || '';
  return (
    submissionMap.value.get(`${sid}:${subTaskId}`)
    || submissionMap.value.get(`${row.studentName}:${subTaskId}`)
    || (row.studentUserId
      ? submissionMap.value.get(`${row.studentUserId}:${subTaskId}`)
      : null)
    || submissions.value.find(
      (s) =>
        s.subTaskId === subTaskId
        && (s.studentNo === sid || s.studentNo === row.studentId || s.studentName === row.studentName),
    )
    || null
  );
}

function resolveSubmission(row, subTaskId) {
  const direct = findSubmission(row, subTaskId);
  if (direct) return direct;
  const cell = getDashboardCell(row, subTaskId);
  if (cell?.submissionId) {
    return submissionById.value.get(cell.submissionId) || null;
  }
  return null;
}

function matrixCellLabel(row, subTaskId, sub) {
  const cell = getDashboardCell(row, subTaskId);
  if (cell?.label) return cell.label;
  if (!sub) return '未提交';
  const scoreText = sub.score != null ? ` ${sub.score}分` : '';
  return `${submissionStatusLabel(sub.status)}${scoreText}`;
}

function ensureGradeForm(sub) {
  if (!gradeForms[sub.submissionId]) {
    gradeForms[sub.submissionId] = {
      score: sub.score ?? '',
      comment: sub.teacherComment ?? '',
    };
  }
}

function matchesFilter(sub) {
  if (!sub) return false;
  if (statusFilter.value === 'all') return true;
  if (statusFilter.value === 'pending') return sub.status === 'SUBMITTED';
  if (statusFilter.value === 'graded') return sub.status === 'GRADED';
  if (statusFilter.value === 'rejected') return sub.status === 'REJECTED';
  return true;
}

function studentColorIdx(row) {
  const id = row.studentId || row.studentName || '';
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) hash = (hash + id.charCodeAt(i)) % 5;
  return hash;
}

function matrixChipClass(sub) {
  if (sub.status === 'GRADED') return 't-prog-matrix__chip--graded';
  if (sub.status === 'SUBMITTED') return 't-prog-matrix__chip--submitted';
  if (sub.status === 'REJECTED') return 't-prog-matrix__chip--warn';
  return 't-prog-matrix__chip--locked';
}

function submissionIcon(status) {
  if (status === 'GRADED') return 'fas fa-check';
  if (status === 'SUBMITTED') return 'fas fa-paper-plane';
  if (status === 'REJECTED') return 'fas fa-undo';
  return 'fas fa-minus';
}

function matrixCellTitle(sub) {
  if (!sub) return '';
  const parts = [submissionStatusLabel(sub.status)];
  if (sub.score != null) parts.push(`${sub.score} 分`);
  if (sub.fileName) parts.push(sub.fileName);
  if (sub.submittedAt) parts.push(sub.submittedAt);
  return parts.join(' · ');
}

function onMatrixCellClick(row, subTaskId) {
  const sub = resolveSubmission(row, subTaskId);
  if (!sub) return;
  ensureGradeForm(sub);
  activeSubmission.value = sub;
  activeSubmissionIdx.value = studentColorIdx(row);
  modalOpen.value = true;
}

function closeModal() {
  modalOpen.value = false;
  activeSubmission.value = null;
}

function onModalAction() {
  if (!activeSubmission.value) return;
  const id = activeSubmission.value.submissionId;
  const updated = submissions.value.find((s) => s.submissionId === id);
  if (updated) {
    activeSubmission.value = { ...updated };
  } else {
    closeModal();
  }
}

async function runBatchAi() {
  const pending = submissions.value.filter((s) => s.status === 'SUBMITTED');
  if (!pending.length) return;
  batchAiRunning.value = true;
  try {
    await batchGradingAssist(pending);
  } finally {
    batchAiRunning.value = false;
  }
}

watch(modalOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : '';
  if (open) {
    window.addEventListener('keydown', onEscapeKey);
  } else {
    window.removeEventListener('keydown', onEscapeKey);
  }
});

function onEscapeKey(e) {
  if (e.key === 'Escape') closeModal();
}

onUnmounted(() => {
  document.body.style.overflow = '';
  window.removeEventListener('keydown', onEscapeKey);
});

watch(() => route.query.student, applyGradingQuery);

onMounted(async () => {
  applyGradingQuery();
  if (currentTaskId.value) await refreshDashboard();
});
</script>
