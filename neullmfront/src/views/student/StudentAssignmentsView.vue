<template>
  <div class="student-page classroom-theme assignments-page">
    <StudentPageHeader
      title="我的作业"
      icon="fa-book-open"
      active="assignments"
      :unread-count="unreadCount"
      @logout="logout"
    />

    <main class="content page-inner">
      <div v-if="loading" class="state-box">
        <div class="state-box__icon"><i class="fas fa-spinner fa-spin"></i></div>
        <p>加载中…</p>
      </div>

      <div v-else-if="!tasks.length" class="state-box state-box--empty">
        <div class="state-box__icon"><i class="fas fa-inbox"></i></div>
        <p>暂无已发布作业</p>
        <span class="state-hint">教师发布任务后，会出现在这里</span>
      </div>

      <template v-else>
        <div class="filter-bar" role="tablist" aria-label="作业分类">
          <button
            v-for="opt in filterOptions"
            :key="opt.value"
            type="button"
            role="tab"
            class="filter-bar__btn"
            :class="{ 'filter-bar__btn--active': taskFilter === opt.value }"
            :aria-selected="taskFilter === opt.value"
            @click="setTaskFilter(opt.value)"
          >
            {{ opt.label }}
            <em>{{ opt.count }}</em>
          </button>
        </div>

        <div v-if="!filteredTasks.length" class="state-box state-box--empty state-box--compact">
          <div class="state-box__icon"><i class="fas fa-filter"></i></div>
          <p>{{ filterEmptyTitle }}</p>
          <span class="state-hint">{{ filterEmptyHint }}</span>
          <button
            v-if="taskFilter !== 'all'"
            type="button"
            class="filter-reset"
            @click="setTaskFilter('all')"
          >
            查看全部作业
          </button>
        </div>

        <section v-else class="task-list">
          <article
            v-for="task in filteredTasks"
            :key="task.taskId"
            class="task-card"
            :class="{
              'task-card--open': expandedTaskId === task.taskId,
              'task-card--done': isTaskComplete(task),
              [`task-card--${taskStatusClass(task).replace('task-status--', '')}`]: true,
            }"
          >
            <button
              type="button"
              class="task-head"
              :aria-expanded="expandedTaskId === task.taskId"
              @click="toggleTask(task.taskId)"
            >
              <div class="task-head__accent" :class="taskStatusClass(task)" aria-hidden="true"></div>

              <div class="task-head__content">
                <div class="task-head__title-row">
                  <h2>{{ task.title }}</h2>
                  <span class="task-status" :class="taskStatusClass(task)">
                    {{ taskStatusLabel(task) }}
                  </span>
                </div>

                <div class="task-head__progress">
                  <div class="task-bar" aria-hidden="true">
                    <span
                      class="task-bar__fill"
                      :class="{ 'task-bar__fill--done': isTaskComplete(task) }"
                      :style="{ width: `${taskProgressPct(task)}%` }"
                    ></span>
                  </div>
                  <div class="task-head__meta">
                    <span class="task-head__fraction">
                      {{ taskDoneCount(task) }}/{{ task.subTasks?.length || 0 }}
                    </span>
                    <span class="task-head__hint">{{ taskCollapsedHint(task) }}</span>
                    <span class="task-head__chev-wrap" aria-hidden="true">
                      <i
                        class="fas fa-chevron-down task-head__chev"
                        :class="{ 'task-head__chev--open': expandedTaskId === task.taskId }"
                      ></i>
                    </span>
                  </div>
                </div>
              </div>
            </button>

            <Transition name="task-expand">
              <div v-show="expandedTaskId === task.taskId" class="task-body">
                <div v-if="task.description" class="task-desc-wrap">
                  <p
                    class="task-desc"
                    :class="{ 'task-desc--clamped': !isDescExpanded(task.taskId) }"
                  >
                    {{ task.description }}
                  </p>
                  <button
                    v-if="needsDescToggle(task.description)"
                    type="button"
                    class="task-desc-toggle"
                    @click.stop="toggleDesc(task.taskId)"
                  >
                    {{ isDescExpanded(task.taskId) ? '收起说明' : '展开说明' }}
                    <i
                      class="fas fa-chevron-down"
                      :class="{ 'task-desc-toggle__icon--open': isDescExpanded(task.taskId) }"
                    ></i>
                  </button>
                </div>

                <ol class="step-list">
                  <li
                    v-for="(sub, si) in task.subTasks"
                    :key="sub.subTaskId"
                    class="step-card"
                    :class="stepCardClass(sub)"
                  >
                    <div class="step-card__top">
                      <span class="step-badge" :class="stepBadgeClass(sub)">
                        <i v-if="showSubmitted(sub)" class="fas fa-check"></i>
                        <i v-else-if="sub.status === 'LOCKED'" class="fas fa-lock"></i>
                        <template v-else>{{ si + 1 }}</template>
                      </span>
                      <div class="step-card__title-wrap">
                        <strong class="step-card__title">{{ sub.title }}</strong>
                        <span v-if="showSubmitted(sub)" class="step-tag" :class="statusClass(sub)">
                          {{ sub.statusLabel || '已提交' }}
                        </span>
                        <span v-else-if="sub.status === 'LOCKED'" class="step-tag step-tag--locked">
                          待解锁
                        </span>
                        <span v-else-if="sub.status === 'REJECTED'" class="step-tag step-tag--rejected">
                          需重传
                        </span>
                        <span v-else class="step-tag step-tag--active">进行中</span>
                      </div>
                    </div>

                    <p
                      v-if="sub.description && (sub.canSubmit || sub.status === 'REJECTED')"
                      class="step-card__desc"
                    >
                      {{ sub.description }}
                    </p>

                    <div v-if="showSubmitted(sub)" class="step-card__result">
                      <div v-if="sub.fileName" class="step-file">
                        <i class="fas fa-file-alt"></i>
                        <span>{{ sub.fileName }}</span>
                      </div>
                      <span v-if="sub.submittedAt" class="step-time">{{ formatTime(sub.submittedAt) }}</span>
                      <p v-if="sub.teacherComment && sub.status === 'GRADED'" class="step-comment">
                        <i class="fas fa-comment-dots"></i>
                        {{ sub.teacherComment }}
                      </p>
                    </div>

                    <label
                      v-else-if="sub.canSubmit"
                      class="step-upload"
                      :class="{ 'step-upload--warn': sub.status === 'REJECTED' }"
                      @click.stop
                    >
                      <i class="fas fa-cloud-upload-alt"></i>
                      <span>{{ sub.status === 'REJECTED' ? '重新上传作业' : '上传作业文件' }}</span>
                      <small>支持 docx、pdf、txt、md，最大 20MB</small>
                      <input
                        type="file"
                        class="upload-input"
                        accept=".docx,.pdf,.txt,.md"
                        @change="(e) => onFile(task.taskId, sub.subTaskId, e)"
                      />
                    </label>
                  </li>
                </ol>
              </div>
            </Transition>
          </article>
        </section>
      </template>
    </main>

    <Transition name="toast-fade">
      <p v-if="message" :class="['toast', msgOk ? 'ok' : 'fail']">{{ message }}</p>
    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import StudentPageHeader from '@/components/student/StudentPageHeader.vue';
import classroomApi from '../../services/classroomApi';
import notificationsApi from '../../services/notificationsApi';
import authApi from '../../services/authApi';
import { clearAuth } from '../../stores/auth';
import { formatApiDateTime } from '../../utils/datetime';

const router = useRouter();
const tasks = ref([]);
const unreadCount = ref(0);
const loading = ref(true);
const message = ref('');
const msgOk = ref(true);
const expandedTaskId = ref(null);
const expandedDescIds = ref(new Set());
const taskFilter = ref('doing');

const MAX_FILE_BYTES = 20 * 1024 * 1024;

const notStartedTaskCount = computed(() => tasks.value.filter((t) => isTaskNotStarted(t)).length);
const inProgressTaskCount = computed(() => tasks.value.filter((t) => isTaskInProgress(t)).length);
const doneTaskCount = computed(() => tasks.value.filter((t) => isTaskComplete(t)).length);

const filterOptions = computed(() => [
  { value: 'all', label: '全部', count: tasks.value.length },
  { value: 'todo', label: '未开始', count: notStartedTaskCount.value },
  { value: 'doing', label: '进行中', count: inProgressTaskCount.value },
  { value: 'done', label: '已完成', count: doneTaskCount.value },
]);

const filteredTasks = computed(() => {
  switch (taskFilter.value) {
    case 'todo':
      return tasks.value.filter((t) => isTaskNotStarted(t));
    case 'doing':
      return tasks.value.filter((t) => isTaskInProgress(t));
    case 'done':
      return tasks.value.filter((t) => isTaskComplete(t));
    default:
      return tasks.value;
  }
});

const filterEmptyTitle = computed(() => {
  switch (taskFilter.value) {
    case 'todo':
      return '暂无未开始作业';
    case 'doing':
      return '暂无进行中作业';
    case 'done':
      return '暂无已完成作业';
    default:
      return '暂无作业';
  }
});

const filterEmptyHint = computed(() => {
  switch (taskFilter.value) {
    case 'todo':
      return '所有作业都已开始或完成';
    case 'doing':
      return '开始提交步骤后，会出现在这里';
    case 'done':
      return '完成并提交全部步骤后，会出现在这里';
    default:
      return '教师发布任务后，会出现在这里';
  }
});

onMounted(load);

watch(taskFilter, () => {
  if (!filteredTasks.value.some((t) => t.taskId === expandedTaskId.value)) {
    expandedTaskId.value = pickDefaultExpandedTask(filteredTasks.value);
  }
});

function showSubmitted(sub) {
  return sub.submissionId && sub.status !== 'REJECTED';
}

function statusClass(sub) {
  if (sub.status === 'GRADED') return 'graded';
  return 'submitted';
}

function taskDoneCount(task) {
  return (task.subTasks || []).filter((s) => showSubmitted(s)).length;
}

function isTaskComplete(task) {
  const total = task.subTasks?.length || 0;
  return total > 0 && taskDoneCount(task) === total;
}

function isTaskNotStarted(task) {
  return !isTaskComplete(task) && taskDoneCount(task) === 0;
}

function isTaskInProgress(task) {
  return !isTaskComplete(task) && taskDoneCount(task) > 0;
}

function taskProgressPct(task) {
  const total = task.subTasks?.length || 0;
  if (!total) return 0;
  return Math.round((taskDoneCount(task) / total) * 100);
}

function taskCollapsedHint(task) {
  if (isTaskComplete(task)) {
    const total = task.subTasks?.length || 0;
    return total ? `共 ${total} 个步骤已全部完成` : '已完成';
  }
  const active = (task.subTasks || []).find((s) => s.canSubmit || s.status === 'REJECTED');
  if (active) return `当前步骤 · ${active.title}`;
  const done = taskDoneCount(task);
  const total = task.subTasks?.length || 0;
  return total ? `已完成 ${done}/${total} 步` : '';
}

function taskStatusLabel(task) {
  if (isTaskComplete(task)) return '已完成';
  const done = taskDoneCount(task);
  if (done === 0) return '未开始';
  return '进行中';
}

function taskStatusClass(task) {
  if (isTaskComplete(task)) return 'task-status--done';
  if (taskDoneCount(task) === 0) return 'task-status--todo';
  return 'task-status--doing';
}

function stepCardClass(sub) {
  if (showSubmitted(sub)) return 'step-card--done';
  if (sub.status === 'LOCKED') return 'step-card--locked';
  if (sub.status === 'REJECTED') return 'step-card--rejected';
  return 'step-card--active';
}

function stepBadgeClass(sub) {
  if (showSubmitted(sub)) return 'step-badge--done';
  if (sub.status === 'LOCKED') return 'step-badge--locked';
  if (sub.status === 'REJECTED') return 'step-badge--rejected';
  return 'step-badge--active';
}

function needsDescToggle(text) {
  return text && text.length > 72;
}

function isDescExpanded(taskId) {
  return expandedDescIds.value.has(taskId);
}

function toggleDesc(taskId) {
  const next = new Set(expandedDescIds.value);
  if (next.has(taskId)) next.delete(taskId);
  else next.add(taskId);
  expandedDescIds.value = next;
}

function pickDefaultExpandedTask(list) {
  if (!list.length) return null;
  const incomplete = list.find((t) => !isTaskComplete(t));
  return incomplete?.taskId ?? list[0]?.taskId ?? null;
}

function setTaskFilter(value) {
  taskFilter.value = value;
}

function toggleTask(taskId) {
  expandedTaskId.value = expandedTaskId.value === taskId ? null : taskId;
}

function formatTime(iso) {
  if (!iso) return '';
  return formatApiDateTime(iso, { style: 'short' });
}

function formatSize(bytes) {
  if (bytes < 1024 * 1024) return `${Math.ceil(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

async function load() {
  loading.value = true;
  try {
    const [list, countRes] = await Promise.all([
      classroomApi.myAssignments(),
      notificationsApi.unreadCount().catch(() => ({ count: 0 })),
    ]);
    tasks.value = list;
    unreadCount.value = countRes?.count ?? 0;
    if (!expandedTaskId.value || !filteredTasks.value.some((t) => t.taskId === expandedTaskId.value)) {
      expandedTaskId.value = pickDefaultExpandedTask(filteredTasks.value);
    }
  } catch (e) {
    message.value = e.message;
    msgOk.value = false;
  } finally {
    loading.value = false;
  }
}

async function onFile(taskId, subTaskId, event) {
  const file = event.target.files?.[0];
  if (!file) return;
  if (file.size > MAX_FILE_BYTES) {
    message.value = `文件过大（${formatSize(file.size)}），单文件不超过 20MB`;
    msgOk.value = false;
    event.target.value = '';
    return;
  }
  message.value = '正在上传…';
  try {
    await classroomApi.submitFile(taskId, subTaskId, file);
    message.value = `「${file.name}」提交成功`;
    msgOk.value = true;
    event.target.value = '';
    await load();
  } catch (e) {
    const status = e.response?.status;
    const serverMsg = e.response?.data?.message;
    if (status === 413 || (serverMsg && serverMsg.includes('20MB'))) {
      message.value = serverMsg || '上传文件不能超过 20MB';
    } else {
      message.value = serverMsg || e.message || '上传失败';
    }
    msgOk.value = false;
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
</script>

<style scoped>
@import '../../assets/classroom-theme.css';

.student-page {
  min-height: 100vh;
  max-width: 100%;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  background: #eef2f6;
  color: var(--text);
  font-family: var(--font);
}

.content {
  flex: 1;
  padding-top: 12px;
  padding-bottom: 20px;
}

/* ── 分类筛选 ── */
.filter-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 14px;
  padding: 4px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.filter-bar::-webkit-scrollbar {
  display: none;
}

.filter-bar__btn {
  flex: 1 0 auto;
  min-width: calc(25% - 4px);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 40px;
  padding: 8px 4px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  font-family: inherit;
  font-size: 0.74rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  -webkit-tap-highlight-color: transparent;
  white-space: nowrap;
}

.filter-bar__btn em {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #64748b;
  font-size: 0.65rem;
  font-weight: 800;
  font-style: normal;
  line-height: 18px;
  text-align: center;
}

.filter-bar__btn--active {
  color: #0f766e;
  background: #fff;
  box-shadow: 0 2px 8px rgba(13, 148, 136, 0.12);
}

.filter-bar__btn--active em {
  background: rgba(20, 184, 166, 0.16);
  color: #0d9488;
}

.filter-reset {
  margin-top: 12px;
  padding: 8px 16px;
  border: 1px solid rgba(13, 148, 136, 0.25);
  border-radius: 999px;
  background: #fff;
  color: #0d9488;
  font-family: inherit;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
}

.state-box--compact {
  padding: 40px 24px;
  margin-bottom: 8px;
}

/* ── 任务列表 ── */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  transition: box-shadow 0.22s ease, border-color 0.22s ease;
}

.task-card--todo:not(.task-card--open) {
  background: linear-gradient(135deg, #fff 0%, #f8fafc 100%);
}

.task-card--doing:not(.task-card--open) {
  background: linear-gradient(135deg, #fff 0%, rgba(240, 253, 250, 0.65) 100%);
  border-color: rgba(13, 148, 136, 0.1);
}

.task-card--done:not(.task-card--open) {
  background: linear-gradient(135deg, #fff 0%, rgba(240, 253, 244, 0.7) 100%);
  border-color: rgba(16, 185, 129, 0.12);
}

.task-card--open {
  border-color: rgba(13, 148, 136, 0.2);
  box-shadow: 0 8px 28px rgba(13, 148, 136, 0.1);
}

.task-card--done {
  opacity: 0.98;
}

.task-head {
  display: flex;
  align-items: stretch;
  gap: 0;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  -webkit-tap-highlight-color: transparent;
}

.task-head:active .task-head__content {
  background: rgba(15, 23, 42, 0.02);
}

.task-head__accent {
  flex-shrink: 0;
  width: 4px;
  background: #cbd5e1;
}

.task-head__accent.task-status--doing {
  background: linear-gradient(180deg, #0f766e, #14b8a6);
}

.task-head__accent.task-status--done {
  background: linear-gradient(180deg, #059669, #34d399);
}

.task-head__accent.task-status--todo {
  background: #cbd5e1;
}

.task-head__content {
  flex: 1;
  min-width: 0;
  padding: 14px 14px 13px 12px;
  transition: background 0.15s;
}

.task-head__title-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
}

.task-head h2 {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 0.92rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.45;
  letter-spacing: -0.01em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-status {
  flex-shrink: 0;
  margin-top: 1px;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 0.6rem;
  font-weight: 700;
  line-height: 1.2;
}

.task-status--todo {
  color: #64748b;
  background: rgba(241, 245, 249, 0.95);
}

.task-status--doing {
  color: #0f766e;
  background: rgba(20, 184, 166, 0.14);
}

.task-status--done {
  color: #047857;
  background: rgba(220, 252, 231, 0.95);
}

.task-head__progress {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.task-bar {
  height: 6px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.9);
  overflow: hidden;
}

.task-bar__fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #0f766e, #14b8a6);
  transition: width 0.35s ease;
}

.task-bar__fill--done {
  background: linear-gradient(90deg, #059669, #34d399);
}

.task-head__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.task-head__fraction {
  flex-shrink: 0;
  font-size: 0.72rem;
  font-weight: 800;
  color: #0d9488;
  letter-spacing: -0.02em;
}

.task-card--done .task-head__fraction {
  color: #059669;
}

.task-head__hint {
  flex: 1;
  min-width: 0;
  font-size: 0.72rem;
  color: #64748b;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-head__chev-wrap {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.04);
}

.task-card--open .task-head__chev-wrap {
  background: rgba(20, 184, 166, 0.12);
}

.task-head__chev {
  font-size: 0.62rem;
  color: #94a3b8;
  transition: transform 0.25s ease, color 0.25s ease;
}

.task-head__chev--open {
  transform: rotate(180deg);
  color: #0d9488;
}

/* ── 展开内容 ── */
.task-body {
  padding: 0 16px 16px;
  border-top: 1px solid rgba(15, 23, 42, 0.05);
}

.task-desc-wrap {
  padding-top: 14px;
  margin-bottom: 14px;
}

.task-desc {
  margin: 0;
  font-size: 0.82rem;
  color: #475569;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.task-desc--clamped {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-desc-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 0.75rem;
  font-weight: 700;
  color: #0d9488;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.task-desc-toggle i {
  font-size: 0.6rem;
  transition: transform 0.2s ease;
}

.task-desc-toggle__icon--open {
  transform: rotate(180deg);
}

/* ── 步骤卡片 ── */
.step-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-card {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: #f8fafc;
}

.step-card--active {
  background: linear-gradient(135deg, rgba(20, 184, 166, 0.06) 0%, rgba(255, 255, 255, 0.9) 100%);
  border-color: rgba(13, 148, 136, 0.2);
  box-shadow: 0 2px 12px rgba(13, 148, 136, 0.08);
}

.step-card--done {
  background: #f0fdf4;
  border-color: rgba(16, 185, 129, 0.15);
}

.step-card--locked {
  background: #f8fafc;
  border-color: rgba(15, 23, 42, 0.04);
  opacity: 0.72;
}

.step-card--rejected {
  background: #fffbeb;
  border-color: rgba(245, 158, 11, 0.25);
}

.step-card__top {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.step-badge {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  font-size: 0.72rem;
  font-weight: 800;
}

.step-badge--active {
  color: #fff;
  background: linear-gradient(145deg, #0f766e, #14b8a6);
  box-shadow: 0 2px 8px rgba(13, 148, 136, 0.3);
}

.step-badge--done {
  color: #fff;
  background: linear-gradient(145deg, #059669, #34d399);
  font-size: 0.62rem;
}

.step-badge--locked {
  color: #94a3b8;
  background: #e2e8f0;
  font-size: 0.58rem;
}

.step-badge--rejected {
  color: #fff;
  background: linear-gradient(145deg, #d97706, #f59e0b);
  font-size: 0.62rem;
}

.step-card__title-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 8px;
}

.step-card__title {
  font-size: 0.88rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
}

.step-card--locked .step-card__title {
  color: #94a3b8;
}

.step-tag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.62rem;
  font-weight: 700;
}

.step-tag.submitted {
  background: #dcfce7;
  color: #166534;
}

.step-tag.graded {
  background: #ede9fe;
  color: #6d28d9;
}

.step-tag--locked {
  background: #e2e8f0;
  color: #64748b;
}

.step-tag--rejected {
  background: #fef3c7;
  color: #b45309;
}

.step-tag--active {
  background: rgba(20, 184, 166, 0.15);
  color: #0f766e;
}

.step-card__desc {
  margin: 10px 0 0;
  padding-left: 38px;
  font-size: 0.8rem;
  color: #64748b;
  line-height: 1.55;
}

.step-card__result {
  margin-top: 10px;
  padding-left: 38px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.step-file {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  color: #475569;
}

.step-file i {
  color: #0d9488;
  flex-shrink: 0;
}

.step-file span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.step-time {
  font-size: 0.72rem;
  color: #94a3b8;
}

.step-comment {
  margin: 4px 0 0;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 0.78rem;
  color: #0f766e;
  background: rgba(20, 184, 166, 0.08);
  line-height: 1.5;
}

.step-comment i {
  margin-right: 4px;
  opacity: 0.75;
}

.step-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1.5px dashed rgba(13, 148, 136, 0.35);
  background: rgba(255, 255, 255, 0.85);
  color: #0f766e;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  -webkit-tap-highlight-color: transparent;
}

.step-upload i {
  font-size: 1.1rem;
  margin-bottom: 2px;
}

.step-upload small {
  font-size: 0.68rem;
  font-weight: 500;
  color: #64748b;
}

.step-upload:active {
  background: rgba(20, 184, 166, 0.08);
  border-color: rgba(13, 148, 136, 0.5);
}

.step-upload--warn {
  color: #b45309;
  border-color: rgba(245, 158, 11, 0.4);
  background: rgba(255, 251, 235, 0.9);
}

.upload-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
}

/* ── 展开动画 ── */
.task-expand-enter-active,
.task-expand-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
  overflow: hidden;
}

.task-expand-enter-from,
.task-expand-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ── 空状态 / Toast ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 24px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 18px;
  color: var(--muted);
  text-align: center;
}

.state-box__icon {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: var(--hint-bg);
  margin-bottom: 14px;
}

.state-box__icon i {
  font-size: 1.35rem;
  color: var(--accent);
}

.state-box p {
  margin: 0;
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text);
}

.state-hint {
  margin-top: 8px;
  font-size: 0.8rem;
  line-height: 1.55;
  max-width: 260px;
}

.toast {
  position: fixed;
  bottom: calc(var(--mobile-nav-height, 72px) + 12px + env(safe-area-inset-bottom, 0px));
  left: 50%;
  transform: translateX(-50%);
  max-width: calc(100vw - 32px);
  padding: 12px 18px;
  border-radius: 14px;
  font-size: 0.85rem;
  font-weight: 600;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.14);
  z-index: 100;
}

.toast.ok {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #86efac;
}

.toast.fail {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fca5a5;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

/* ── 桌面端微调 ── */
@media (min-width: 768px) {
  .task-head h2 {
    font-size: 1.02rem;
  }

  .step-upload {
    flex-direction: row;
    justify-content: center;
    gap: 8px;
  }

  .step-upload i {
    margin-bottom: 0;
  }

  .step-upload small {
    margin-left: 4px;
  }
}
</style>
