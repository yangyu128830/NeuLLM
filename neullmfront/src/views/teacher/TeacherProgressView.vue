<template>
  <div class="t-page t-prog-page">
    <header class="t-ws-head t-prog-head">
      <div class="t-ws-head__main">
        <div class="t-prog-head__title-row">
          <h1>学情看板</h1>
          <span v-if="dashboard" class="t-prog-live-badge">
            <i class="fas fa-circle"></i>实时
          </span>
        </div>
        <p>以完成度泳道掌握全班进度，需跟进学生自动置顶</p>
      </div>
      <div class="t-ws-head__actions">
        <TeacherWorkspaceContext
          v-model="currentTaskId"
          :tasks="tasksBySubjectFilter"
          refreshable
          :refreshing="refreshing"
          @change="refreshDashboard"
          @refresh="handleRefresh"
        >
          <button
            v-if="currentTask && !currentTask.published"
            type="button"
            class="t-btn-text t-btn-text--primary"
            @click="publishCurrent"
          >
            发布任务
          </button>
          <router-link
            v-if="pendingCount"
            to="/teacher/grading"
            class="t-btn-solid t-prog-grade-cta"
          >
            <i class="fas fa-clipboard-check"></i>
            待批改 {{ pendingCount }}
          </router-link>
        </TeacherWorkspaceContext>
      </div>
    </header>

    <section v-if="dashboard" class="t-prog-overview">
      <div class="t-prog-overview__insight">
        <div class="t-prog-overview__mesh"></div>
        <div class="t-prog-overview__insight-inner">
          <div class="t-prog-overview__copy">
            <span class="t-prog-overview__eyebrow">
              <i class="fas fa-magic"></i>智能洞察
            </span>
            <h2>{{ insightHeadline }}</h2>
            <p>{{ insightDetail }}</p>
            <div v-if="currentTask" class="t-prog-overview__meta">
              <span>{{ currentTask.title }}</span>
              <span v-if="dashboard.subTasks?.length">· {{ dashboard.subTasks.length }} 个子任务</span>
              <span
                class="t-prog-overview__tag"
                :class="currentTask.published ? 't-prog-overview__tag--ok' : 't-prog-overview__tag--warn'"
              >
                {{ currentTask.published ? '已发布' : '未发布' }}
              </span>
            </div>
          </div>
          <div class="t-prog-overview__aside">
            <div class="t-prog-overview__ring">
              <svg viewBox="0 0 120 120" aria-hidden="true">
                <circle class="t-prog-overview__ring-track" cx="60" cy="60" r="52" />
                <circle
                  class="t-prog-overview__ring-fill"
                  cx="60"
                  cy="60"
                  r="52"
                  :style="{ '--ring-offset': heroRingOffset }"
                />
              </svg>
              <div class="t-prog-overview__ring-label">
                <strong>{{ classCompletionPercent }}</strong>
                <em>班级完成度</em>
              </div>
            </div>
            <div class="t-prog-overview__buckets">
              <span class="t-prog-overview__bucket t-prog-overview__bucket--done">
                已完成 <strong>{{ statusBuckets.done }}</strong>
              </span>
              <span class="t-prog-overview__bucket t-prog-overview__bucket--progress">
                进行中 <strong>{{ statusBuckets.progress }}</strong>
              </span>
              <span class="t-prog-overview__bucket t-prog-overview__bucket--idle">
                未开始 <strong>{{ statusBuckets.idle }}</strong>
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="t-prog-overview__metrics">
        <div
          v-for="(label, key) in summaryLabels"
          :key="key"
          class="t-prog-overview__metric"
          :data-metric="key"
        >
          <i :class="summaryIcons[key]"></i>
          <strong>{{ dashboard.summary?.[key] ?? 0 }}</strong>
          <span>{{ label }}</span>
        </div>
      </div>

      <div v-if="dashboard.rows?.length" class="t-prog-overview__toolbar">
        <div class="t-filter-tabs t-prog-view-tabs" role="tablist" aria-label="展示方式">
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
            :class="{ 't-filter-tab--on': viewMode === 'lane' }"
            @click="viewMode = 'lane'"
          >
            <i class="fas fa-stream"></i>泳道
          </button>
        </div>
        <div class="t-filter-tabs" role="tablist">
          <button
            type="button"
            class="t-filter-tab"
            :class="{ 't-filter-tab--on': rowFilter === 'all' }"
            @click="rowFilter = 'all'"
          >
            全部<em>{{ dashboard.rows.length }}</em>
          </button>
          <button
            type="button"
            class="t-filter-tab"
            :class="{ 't-filter-tab--on': rowFilter === 'follow' }"
            @click="rowFilter = 'follow'"
          >
            需跟进<em>{{ followUpCount }}</em>
          </button>
        </div>
        <div class="t-prog-search">
          <i class="fas fa-search"></i>
          <input
            v-model="searchQuery"
            type="search"
            placeholder="搜索学生姓名或学号…"
            aria-label="搜索学生"
          />
        </div>
      </div>
    </section>

    <div class="t-ws-body">
      <section v-if="viewMode === 'matrix' && sortedRows.length" class="t-prog-matrix">
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
                <th class="t-prog-matrix__col-pct">完成度</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in sortedRows"
                :key="row.studentId"
                class="t-prog-matrix__row"
                :class="{ 't-prog-matrix__row--follow': isFollowUp(row) }"
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
                    <span v-if="isFollowUp(row)" class="t-pill t-pill--warn t-prog-matrix__pill">
                      <i class="fas fa-bolt"></i>
                    </span>
                  </div>
                </td>
                <td
                  v-for="st in subTasks"
                  :key="st.subTaskId"
                  class="t-prog-matrix__col-task"
                >
                  <div
                    v-if="getCell(row, st.subTaskId)"
                    class="t-prog-matrix__chip"
                    :class="[
                      matrixTagClass(getCell(row, st.subTaskId).state),
                      {
                        't-prog-matrix__chip--remindable': canRemindCell(getCell(row, st.subTaskId).state),
                        't-prog-matrix__chip--sending': isReminding(row, st.subTaskId),
                      },
                    ]"
                    :title="matrixCellTitle(getCell(row, st.subTaskId))"
                  >
                    <span class="t-prog-matrix__chip-label">
                      <i :class="cellIcon(getCell(row, st.subTaskId).state)"></i>
                      {{ getCell(row, st.subTaskId).label }}
                    </span>
                    <button
                      v-if="canRemindCell(getCell(row, st.subTaskId).state)"
                      type="button"
                      class="t-prog-matrix__chip-action"
                      :disabled="isReminding(row, st.subTaskId)"
                      :aria-label="isReminding(row, st.subTaskId) ? '发送中' : '发送催交提醒'"
                      @click="handleRemind(row, st.subTaskId)"
                    >
                      <i :class="isReminding(row, st.subTaskId) ? 'fas fa-spinner fa-spin' : 'fas fa-bell'"></i>
                    </button>
                  </div>
                  <span v-else class="t-prog-matrix__chip t-prog-matrix__chip--locked">
                    <span class="t-prog-matrix__chip-label">
                      <i class="fas fa-minus"></i>—
                    </span>
                  </span>
                </td>
                <td class="t-prog-matrix__col-pct">
                  <span
                    class="t-prog-matrix__pct"
                    :class="{
                      't-prog-matrix__pct--done': studentProgressPercent(row) >= 100,
                      't-prog-matrix__pct--follow': isFollowUp(row),
                    }"
                  >
                    {{ studentProgressPercent(row) }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="t-prog-matrix__legend">
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--graded">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-check"></i>已批改</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--submitted">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-paper-plane"></i>已提交</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--available">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-circle"></i>可提交</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--warn">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-exclamation"></i>需重交</span>
          </span>
          <span class="t-prog-matrix__legend-item t-prog-matrix__chip t-prog-matrix__chip--locked">
            <span class="t-prog-matrix__chip-label"><i class="fas fa-lock"></i>未解锁</span>
          </span>
          <span class="t-prog-matrix__legend-hint">
            <i class="fas fa-bell"></i>可催交单元格右侧铃铛可一键提醒
          </span>
        </footer>
      </section>

      <div v-else-if="viewMode === 'lane' && sortedRows.length" class="t-prog-board">
        <!-- 需跟进：全宽重点区 -->
        <section v-if="priorityRows.length" class="t-prog-zone t-prog-zone--priority">
          <header class="t-prog-zone__head">
            <div>
              <h3><i class="fas fa-bolt"></i> 需跟进</h3>
              <p>进行中但未完成 · 当前卡点一目了然</p>
            </div>
            <span class="t-prog-zone__count">{{ priorityRows.length }} 人</span>
          </header>
          <article
            v-for="(row, rowIdx) in priorityRows"
            :id="`student-${row.studentId}`"
            :key="row.studentId"
            class="t-prog-focus-card"
            :style="{ animationDelay: `${rowIdx * 50}ms` }"
          >
            <div class="t-prog-focus-card__main">
              <span :class="['t-avatar t-avatar--lg', `t-avatar--${studentColorIdx(row)}`]">
                {{ studentInitial(row.studentName) }}
              </span>
              <div class="t-prog-focus-card__info">
                <div class="t-prog-focus-card__top">
                  <span class="t-prog-focus-card__name">{{ row.studentName }}</span>
                  <span class="t-pill t-pill--warn"><i class="fas fa-bolt"></i>需跟进</span>
                </div>
                <span class="t-prog-focus-card__meta">{{ row.studentId }} · {{ progressLabel(row) }}</span>
                <p v-if="nextAction(row)" class="t-prog-focus-card__action">
                  <i :class="nextActionIcon(row)"></i>
                  {{ nextAction(row) }}
                </p>
              </div>
              <div class="t-prog-focus-card__stat">
                <div
                  class="t-ring t-prog-ring t-prog-ring--lg"
                  :class="ringClass(row)"
                  :style="{ '--ring-p': studentProgressPercent(row) }"
                >
                  <span class="t-ring__inner">{{ studentProgressPercent(row) }}%</span>
                </div>
                <button
                  v-if="canRemindStudent(row)"
                  type="button"
                  class="t-prog-remind-link"
                  :disabled="isReminding(row)"
                  @click="handleRemind(row)"
                >
                  <i :class="isReminding(row) ? 'fas fa-spinner fa-spin' : 'fas fa-bell'"></i>
                  {{ isReminding(row) ? '发送中…' : '催交提醒' }}
                </button>
              </div>
            </div>
            <div class="t-prog-pipeline" role="list" aria-label="子任务进度">
              <div
                v-for="(cell, ci) in row.cells"
                :key="cell.subTaskId"
                class="t-prog-pipe"
                :class="[
                  pipeClass(cell.state),
                  { 't-prog-pipe--current': isCurrentStep(row, cell) },
                ]"
                role="listitem"
              >
                <span class="t-prog-pipe__node">
                  <i :class="cellIcon(cell.state)"></i>
                </span>
                <div class="t-prog-pipe__text">
                  <span class="t-prog-pipe__title">{{ subTaskTitle(cell.subTaskId) || `子任务 ${ci + 1}` }}</span>
                  <span class="t-prog-pipe__label">{{ cell.label }}</span>
                </div>
                <span v-if="ci < row.cells.length - 1" class="t-prog-pipe__arrow" aria-hidden="true">
                  <i class="fas fa-chevron-right"></i>
                </span>
              </div>
            </div>
          </article>
        </section>

        <!-- 已完成：紧凑条 -->
        <section v-if="doneRows.length && rowFilter === 'all'" class="t-prog-zone t-prog-zone--done">
          <header class="t-prog-zone__head t-prog-zone__head--compact">
            <h3><i class="fas fa-crown"></i> 已完成</h3>
            <span class="t-prog-zone__count t-prog-zone__count--success">{{ doneRows.length }} 人</span>
          </header>
          <div class="t-prog-done-row">
            <div
              v-for="row in doneRows"
              :key="row.studentId"
              class="t-prog-done-chip"
            >
              <span :class="['t-avatar t-avatar--sm', `t-avatar--${studentColorIdx(row)}`]">
                {{ studentInitial(row.studentName) }}
              </span>
              <span>{{ row.studentName }}</span>
              <i class="fas fa-check-circle"></i>
            </div>
          </div>
        </section>

        <!-- 尚未开始：弱化列表 -->
        <section v-if="idleRows.length && rowFilter === 'all'" class="t-prog-zone t-prog-zone--idle">
          <header class="t-prog-zone__head t-prog-zone__head--compact">
            <div>
              <h3><i class="fas fa-hourglass-half"></i> 尚未开始</h3>
              <p>建议发送提醒，督促开启任务</p>
            </div>
            <span class="t-prog-zone__count">{{ idleRows.length }} 人</span>
          </header>
          <ul class="t-prog-idle-list">
            <li v-for="row in idleRows" :key="row.studentId" class="t-prog-idle-item">
              <span :class="['t-avatar t-avatar--sm', `t-avatar--${studentColorIdx(row)}`]">
                {{ studentInitial(row.studentName) }}
              </span>
              <span class="t-prog-idle-item__name">{{ row.studentName }}</span>
              <span class="t-prog-idle-item__id">{{ row.studentId }}</span>
              <span class="t-pill t-pill--muted">0/{{ row.cells?.length || 0 }} 子任务</span>
              <button
                v-if="canRemindStudent(row)"
                type="button"
                class="t-prog-remind-icon"
                :disabled="isReminding(row)"
                :aria-label="isReminding(row) ? '发送中' : '发送催交提醒'"
                :title="isReminding(row) ? '发送中…' : '发送催交提醒'"
                @click="handleRemind(row)"
              >
                <i :class="isReminding(row) ? 'fas fa-spinner fa-spin' : 'fas fa-bell'"></i>
              </button>
            </li>
          </ul>
        </section>
      </div>

      <div v-else-if="currentTaskId && dashboard?.rows?.length" class="t-empty-state">
        <div class="t-empty-state__art"><i class="fas fa-filter"></i></div>
        <h2>当前筛选下暂无学生</h2>
        <button type="button" class="t-btn-ghost" @click="clearFilters">显示全部</button>
      </div>

      <div v-else-if="currentTaskId" class="t-empty-state">
        <div class="t-empty-state__art"><i class="fas fa-chart-bar"></i></div>
        <h2>暂无进度数据</h2>
        <p>确认任务已发布，并提醒学生在「我的作业」提交</p>
        <div style="display: flex; gap: 10px; justify-content: center; margin-top: 16px">
          <button
            v-if="currentTask && !currentTask.published"
            type="button"
            class="t-btn-solid"
            @click="publishCurrent"
          >
            立即发布
          </button>
          <router-link to="/teacher/tasks" class="t-btn-ghost">编辑任务</router-link>
        </div>
      </div>

      <div v-else class="t-empty-state">
        <div class="t-empty-state__art"><i class="fas fa-tasks"></i></div>
        <h2>还没有课堂任务</h2>
        <p>创建并发布后，学情将自动汇总到此页</p>
        <router-link to="/teacher/tasks" class="t-btn-solid">去发布任务</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import TeacherWorkspaceContext from '../../components/teacher/TeacherWorkspaceContext.vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const route = useRoute();

const {
  tasks,
  tasksBySubjectFilter,
  currentTaskId,
  dashboard,
  submissions,
  summaryLabels,
  summaryIcons,
  currentTask,
  classCompletionPercent,
  studentProgressPercent,
  studentInitial,
  refreshDashboard,
  publishCurrent,
  sendStudentReminder,
} = useTeacherClassroom();

const rowFilter = ref('all');
const viewMode = ref('matrix');
const searchQuery = ref('');
const refreshing = ref(false);
const remindingKey = ref('');

const subTasks = computed(() => dashboard.value?.subTasks || []);

const pendingCount = computed(
  () => submissions.value.filter((s) => s.status === 'SUBMITTED').length,
);

function isFollowUp(row) {
  const p = studentProgressPercent(row);
  return p > 0 && p < 100;
}

const followUpCount = computed(() => {
  const rows = dashboard.value?.rows || [];
  return rows.filter(isFollowUp).length;
});

const statusBuckets = computed(() => {
  const rows = dashboard.value?.rows || [];
  let done = 0;
  let progress = 0;
  let idle = 0;
  for (const row of rows) {
    const p = studentProgressPercent(row);
    if (p >= 100) done += 1;
    else if (p > 0) progress += 1;
    else idle += 1;
  }
  return { done, progress, idle, total: rows.length };
});

const heroRingOffset = computed(() => {
  const pct = classCompletionPercent.value;
  const circumference = 2 * Math.PI * 52;
  return `${circumference * (1 - pct / 100)}`;
});

const insightHeadline = computed(() => {
  const { done, progress, idle, total } = statusBuckets.value;
  if (!total) return '等待学生开始学习';
  if (done === total) return '全班已完成，表现优秀';
  if (idle === total) return '尚未有学生开始，记得提醒';
  if (progress > 0 && followUpCount.value) return `${followUpCount.value} 名学生需精准跟进`;
  if (classCompletionPercent.value >= 70) return '整体进度良好，继续保持';
  return '班级进度偏慢，建议主动提醒';
});

const insightDetail = computed(() => {
  const parts = [];
  if (followUpCount.value) parts.push(`${followUpCount.value} 人处于进行中`);
  if (pendingCount.value) parts.push(`${pendingCount.value} 份作业待批改`);
  const reminder = dashboard.value?.summary?.reminderCount;
  if (reminder) parts.push(`${reminder} 人建议发送提醒`);
  return parts.length ? parts.join(' · ') : '随时掌握每位学生的子任务进度';
});

const filteredRows = computed(() => {
  let rows = dashboard.value?.rows || [];
  if (rowFilter.value === 'follow') {
    rows = rows.filter(isFollowUp);
  }
  const q = searchQuery.value.trim().toLowerCase();
  if (q) {
    rows = rows.filter(
      (r) =>
        (r.studentName || '').toLowerCase().includes(q) ||
        (r.studentId || '').toLowerCase().includes(q),
    );
  }
  return rows;
});

const sortedRows = computed(() => {
  const rows = [...filteredRows.value];
  return rows.sort((a, b) => {
    const af = isFollowUp(a) ? 0 : 1;
    const bf = isFollowUp(b) ? 0 : 1;
    if (af !== bf) return af - bf;
    return studentProgressPercent(a) - studentProgressPercent(b);
  });
});

const priorityRows = computed(() => sortedRows.value.filter(isFollowUp));

const doneRows = computed(() =>
  sortedRows.value.filter((r) => studentProgressPercent(r) >= 100),
);

const idleRows = computed(() =>
  sortedRows.value.filter((r) => studentProgressPercent(r) === 0),
);

function studentColorIdx(row) {
  const id = row.studentId || row.studentName || '';
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) hash = (hash + id.charCodeAt(i)) % 5;
  return hash;
}

function nextAction(row) {
  const cells = row.cells || [];
  const submitted = cells.find((c) => c.state === 'SUBMITTED');
  if (submitted) {
    const title = subTaskTitle(submitted.subTaskId);
    return `待批改：${title || '子任务'}`;
  }
  const revision = cells.find((c) => ['REJECTED', 'NEEDS_REVISION'].includes(c.state));
  if (revision) {
    const title = subTaskTitle(revision.subTaskId);
    return `需修改：${title || '子任务'}`;
  }
  const available = cells.find((c) => c.state === 'AVAILABLE');
  if (available) {
    const title = subTaskTitle(available.subTaskId);
    return `当前卡点：${title || '子任务'} · 可提交`;
  }
  return '';
}

function nextActionIcon(row) {
  const cells = row.cells || [];
  if (cells.some((c) => c.state === 'SUBMITTED')) return 'fas fa-clipboard-check';
  if (cells.some((c) => ['REJECTED', 'NEEDS_REVISION'].includes(c.state))) return 'fas fa-exclamation-circle';
  return 'fas fa-arrow-right';
}

function isCurrentStep(row, cell) {
  const cells = row.cells || [];
  const submitted = cells.find((c) => c.state === 'SUBMITTED');
  if (submitted) return cell.subTaskId === submitted.subTaskId;
  const revision = cells.find((c) => ['REJECTED', 'NEEDS_REVISION'].includes(c.state));
  if (revision) return cell.subTaskId === revision.subTaskId;
  const available = cells.find((c) => c.state === 'AVAILABLE');
  if (available) return cell.subTaskId === available.subTaskId;
  return false;
}

function pipeClass(state) {
  if (['GRADED', 'SUBMITTED'].includes(state)) return 't-prog-pipe--done';
  if (['REJECTED', 'NEEDS_REVISION'].includes(state)) return 't-prog-pipe--warn';
  if (state === 'AVAILABLE') return 't-prog-pipe--active';
  return 't-prog-pipe--idle';
}

function clearFilters() {
  rowFilter.value = 'all';
  searchQuery.value = '';
}

async function handleRefresh() {
  refreshing.value = true;
  try {
    await refreshDashboard();
  } finally {
    setTimeout(() => {
      refreshing.value = false;
    }, 600);
  }
}

function subTaskTitle(subTaskId) {
  const st = dashboard.value?.subTasks?.find((s) => s.subTaskId === subTaskId);
  return st?.title || '';
}

function progressLabel(row) {
  const cells = row.cells || [];
  const done = cells.filter((c) => !['AVAILABLE', 'LOCKED'].includes(c.state)).length;
  return `${done}/${cells.length} 子任务`;
}

function ringClass(row) {
  const p = studentProgressPercent(row);
  if (p >= 100) return 't-ring--success';
  if (isFollowUp(row)) return 't-ring--warn';
  return 't-ring--idle';
}

function cellIcon(state) {
  if (state === 'GRADED') return 'fas fa-check';
  if (state === 'SUBMITTED') return 'fas fa-paper-plane';
  if (state === 'AVAILABLE') return 'fas fa-circle';
  if (state === 'LOCKED') return 'fas fa-lock';
  if (['REJECTED', 'NEEDS_REVISION'].includes(state)) return 'fas fa-exclamation';
  return 'fas fa-minus';
}

function getCell(row, subTaskId) {
  return (row.cells || []).find((c) => c.subTaskId === subTaskId) || null;
}

function matrixTagClass(state) {
  if (state === 'GRADED') return 't-prog-matrix__chip--graded';
  if (state === 'SUBMITTED') return 't-prog-matrix__chip--submitted';
  if (state === 'AVAILABLE') return 't-prog-matrix__chip--available';
  if (['REJECTED', 'NEEDS_REVISION'].includes(state)) return 't-prog-matrix__chip--warn';
  return 't-prog-matrix__chip--locked';
}

function matrixCellTitle(cell) {
  const parts = [cell.label];
  if (cell.score != null) parts.push(`${cell.score} 分`);
  if (cell.fileName) parts.push(cell.fileName);
  return parts.join(' · ');
}

function canRemindCell(state) {
  return state === 'AVAILABLE' || state === 'NEEDS_REVISION';
}

function canRemindStudent(row) {
  return (row.cells || []).some((c) => canRemindCell(c.state));
}

function remindKey(row, subTaskId = null) {
  return `${row.studentUserId}:${subTaskId || 'all'}`;
}

function isReminding(row, subTaskId = null) {
  return remindingKey.value === remindKey(row, subTaskId);
}

async function handleRemind(row, subTaskId = null) {
  const key = remindKey(row, subTaskId);
  if (remindingKey.value === key) return;
  remindingKey.value = key;
  try {
    await sendStudentReminder(row.studentUserId, subTaskId);
  } finally {
    if (remindingKey.value === key) {
      remindingKey.value = '';
    }
  }
}

function applyStudentFocus() {
  const sid = route.query.student;
  if (!sid) return;
  searchQuery.value = String(sid);
  rowFilter.value = 'all';
  if (route.query.view === 'matrix') {
    viewMode.value = 'matrix';
  } else {
    viewMode.value = 'lane';
  }
  nextTick(() => {
    const el = document.getElementById(`student-${sid}`);
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
}

watch(
  () => [route.query.student, dashboard.value?.rows?.length],
  () => {
    if (route.query.student && dashboard.value?.rows?.length) {
      applyStudentFocus();
    }
  },
);

onMounted(async () => {
  if (currentTaskId.value) await refreshDashboard();
  applyStudentFocus();
});
</script>
