<template>
  <div :class="['cf-result', 'classroom-result', { 'classroom-result--task': isTaskKind }]">
    <template v-if="isTaskKind">
      <div class="task-success">
        <div class="task-success__hero">
          <div class="task-success__icon" :class="taskData.published ? 'is-live' : 'is-draft'">
            <i :class="taskData.published ? 'fas fa-paper-plane' : 'fas fa-check'"></i>
          </div>
          <div class="task-success__head">
            <div class="task-success__headline">{{ title }}</div>
            <p class="task-success__subtitle">{{ taskHint }}</p>
          </div>
          <span :class="['task-success__badge', taskData.published ? 'is-live' : 'is-draft']">
            {{ taskData.published ? '已发布' : '草稿' }}
          </span>
        </div>

        <div class="task-success__panel">
          <div class="task-success__title-row">
            <h3 class="task-success__title">{{ taskData.title || '—' }}</h3>
            <span class="task-success__id">{{ taskData.taskId || '—' }}</span>
          </div>

          <div v-if="subTasks.length" class="task-success__subs">
            <div class="task-success__subs-label">子任务 · {{ subTasks.length }} 项</div>
            <ol class="task-success__sub-list">
              <li v-for="(st, i) in subTasks" :key="st.subTaskId || i">
                <span class="task-success__sub-num">{{ i + 1 }}</span>
                <span class="task-success__sub-text">{{ st.title }}</span>
              </li>
            </ol>
          </div>
        </div>

        <div class="task-success__actions">
          <router-link v-if="taskData.taskId" to="/teacher/tasks" class="task-success__btn task-success__btn--primary">
            <i class="fas fa-tasks"></i>
            前往任务管理
          </router-link>
          <span v-if="!taskData.published" class="task-success__tip">可在任务管理中继续编辑后发布</span>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="cf-result-icon">
        <i :class="iconClass"></i>
      </div>
      <div class="cf-result-body">
        <div class="cf-result-title">{{ title }}</div>
        <p v-if="summary" class="cf-result-hint">{{ summary }}</p>

      <!-- 学情看板 -->
      <template v-if="kind === 'classroom_dashboard'">
        <div class="cf-result-row">
          <span class="rk">任务</span>
          <span class="rv">{{ dashboard.taskId }} · {{ dashboard.title }}</span>
        </div>
        <div v-if="dashSummary" class="classroom-dash-grid">
          <div v-for="(val, key) in dashSummary" :key="key" class="classroom-dash-cell">
            <span class="rk">{{ formatKey(key) }}</span>
            <span class="rv">{{ val }}</span>
          </div>
        </div>
        <router-link :to="dashboardProgressLink" class="classroom-action-link">
          打开进度看板 <i class="fas fa-chart-line"></i>
        </router-link>
      </template>

      <!-- AI 批改建议 -->
      <template v-else-if="kind === 'classroom_grading_assist'">
        <div class="cf-result-row">
          <span class="rk">提交</span>
          <span class="rv">{{ grading.submissionId || '—' }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">建议分数</span>
          <span class="rv">{{ grading.suggestedScore ?? '—' }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">摘要</span>
          <span class="rv">{{ grading.summary || '—' }}</span>
        </div>
        <p v-if="grading.comment" class="classroom-comment">{{ grading.comment }}</p>
        <router-link to="/teacher/grading" class="classroom-action-link">
          前往批改台 <i class="fas fa-pen"></i>
        </router-link>
      </template>

      <!-- 提交列表 -->
      <template v-else-if="kind === 'classroom_submissions'">
        <p class="cf-result-hint">共 {{ submissions.length }} 条提交</p>
        <ul class="classroom-sub-list">
          <li v-for="s in submissions.slice(0, 6)" :key="s.submissionId">
            {{ s.submissionId }} · {{ s.studentName || s.studentNo }} · {{ s.status }}
          </li>
        </ul>
      </template>

      <!-- 催交预览 -->
      <template v-else-if="kind === 'classroom_remind'">
        <div class="cf-result-row">
          <span class="rk">任务</span>
          <router-link
            v-if="remindData.taskId"
            :to="remindTaskLink"
            class="rv classroom-remind-task-link"
          >
            {{ remindData.title || remindData.taskId }}
            <i class="fas fa-external-link-alt" aria-hidden="true"></i>
          </router-link>
          <span v-else class="rv">{{ remindData.title || '—' }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">待跟进</span>
          <span class="rv">{{ remindData.pendingCount ?? 0 }} 人</span>
        </div>
        <p v-if="remindData.shortMessage" class="classroom-remind-summary">{{ remindData.shortMessage }}</p>
        <ul v-if="pendingStudents.length" class="classroom-pending-list">
          <li v-for="(s, i) in pendingStudents" :key="i">
            <router-link
              v-if="remindData.taskId && s.studentId"
              :to="remindStudentLink(s)"
              class="classroom-pending-link"
            >
              <span class="pending-name">{{ s.studentName }}</span>
              <span class="pending-meta">{{ s.studentId }} · {{ s.status }}</span>
              <i class="fas fa-chevron-right classroom-pending-arrow" aria-hidden="true"></i>
            </router-link>
            <template v-else>
              <span class="pending-name">{{ s.studentName }}</span>
              <span class="pending-meta">{{ s.studentId }} · {{ s.status }}</span>
            </template>
          </li>
        </ul>
        <p v-else-if="!remindData.pendingCount" class="classroom-remind-summary">全班已提交，无需催交。</p>
        <div class="classroom-remind-actions">
          <button
            type="button"
            class="classroom-send-btn"
            :disabled="sending || !remindData.taskId || !(remindData.pendingCount > 0)"
            @click="sendRemindersNow"
          >
            <i class="fas fa-paper-plane"></i>
            {{ sending ? '发送中…' : '一键发送催交邮件' }}
          </button>
          <button type="button" class="classroom-copy-btn" @click="copyRemind">复制话术</button>
        </div>
        <router-link
          v-if="remindData.taskId && remindData.pendingCount > 0"
          :to="remindTaskLink"
          class="classroom-action-link"
        >
          在学情看板查看待交学生 <i class="fas fa-chart-line"></i>
        </router-link>
        <p v-if="sendError" class="classroom-send-err">{{ sendError }}</p>
        <p v-if="sendHint" class="classroom-send-hint">{{ sendHint }}</p>
      </template>

      <!-- 催交已发送 -->
      <template v-else-if="kind === 'classroom_remind_sent'">
        <div class="cf-result-row">
          <span class="rk">任务</span>
          <span class="rv">{{ remindData.title || remindData.taskId }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">站内消息</span>
          <span class="rv">{{ remindData.inAppCount ?? 0 }} 人已收到（学生端「消息中心」）</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">邮件</span>
          <span class="rv">成功 {{ remindData.sentCount ?? 0 }} 封</span>
        </div>
        <div v-if="remindData.skippedCount" class="cf-result-row">
          <span class="rk">邮件跳过</span>
          <span class="rv">{{ remindData.skippedCount }} 人（未配置邮箱，站内已通知）</span>
        </div>
        <div v-if="remindData.failedCount" class="cf-result-row">
          <span class="rk">邮件失败</span>
          <span class="rv">{{ remindData.failedCount }} 封</span>
        </div>
        <p v-if="remindData.allSubmitted" class="classroom-remind-summary">当前无需催交。</p>
      </template>

      <!-- 批量批改 -->
      <template v-else-if="kind === 'classroom_batch_grade'">
        <div class="cf-result-row">
          <span class="rk">任务</span>
          <span class="rv">{{ batchData.taskId }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">进度</span>
          <span class="rv"
            >已生成 {{ batchData.processed }} / 待批改 {{ batchData.totalPending }}</span
          >
        </div>
        <p v-if="batchData.hint" class="cf-result-hint">{{ batchData.hint }}</p>
        <ul class="classroom-sub-list batch-grade-list">
          <li v-for="(r, i) in batchResults" :key="i">
            <strong>{{ r.studentName || r.submissionId }}</strong>
            <span v-if="r.suggestedScore != null"> · 建议 {{ r.suggestedScore }} 分</span>
            <span v-if="r.error" class="batch-err"> · {{ r.error }}</span>
            <span v-else-if="r.summary"> — {{ short(r.summary) }}</span>
          </li>
        </ul>
        <router-link to="/teacher/grading" class="classroom-action-link">
          前往批改台采纳分数 <i class="fas fa-pen"></i>
        </router-link>
      </template>

      <!-- 学生名单 -->
      <template v-else-if="kind === 'classroom_students'">
        <p class="cf-result-hint">本班 {{ students.length }} 人</p>
        <ul class="classroom-sub-list">
          <li v-for="s in students.slice(0, 8)" :key="s.studentId">
            {{ s.studentId }} {{ s.name }}
          </li>
        </ul>
      </template>

      <template v-else>
        <pre class="classroom-raw">{{ rawPreview }}</pre>
      </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import classroomApi from '@/services/classroomApi';
import { buildTeacherProgressLink } from '@/utils/teacherClassroomNav';

const props = defineProps({
  kind: { type: String, default: '' },
  summary: { type: String, default: '' },
  data: { type: Object, default: null },
});

const emit = defineEmits(['remind-sent']);

const sending = ref(false);
const sendError = ref('');
const sendHint = ref('');

const title = computed(() => {
  const map = {
    classroom_task_created: '作业已创建',
    classroom_task_published: '作业已发布',
    classroom_dashboard: '学情进度看板',
    classroom_grading_assist: 'AI 批改建议',
    classroom_submissions: '提交列表',
    classroom_students: '本班学生',
    classroom_graded: '批改已完成',
    classroom_rejected: '已打回提交',
    classroom_remind: '催交提醒',
    classroom_remind_sent: '催交已发送',
    classroom_batch_grade: '批量批改建议',
    classroom_result: '课堂任务结果',
  };
  return map[props.kind] || '任务已完成';
});

const iconClass = computed(() => {
  if (props.kind === 'classroom_error') return 'fas fa-exclamation-circle';
  if (props.kind === 'classroom_grading_assist') return 'fas fa-pen-fancy';
  if (props.kind === 'classroom_dashboard') return 'fas fa-chart-pie';
  return 'fas fa-check';
});

const isTaskKind = computed(() =>
  props.kind === 'classroom_task_created' || props.kind === 'classroom_task_published',
);

const taskData = computed(() => props.data || {});
const subTasks = computed(() => (Array.isArray(taskData.value.subTasks) ? taskData.value.subTasks : []));

const taskHint = computed(() => {
  if (props.kind === 'classroom_task_published') {
    return '学生可在「我的作业」中查看并提交';
  }
  if (props.summary && props.summary !== title.value) {
    return props.summary;
  }
  return '已写入任务库，可在任务管理中编辑或发布';
});

const dashboard = computed(() => props.data || {});
const dashSummary = computed(() => dashboard.value.summary || null);

const dashboardProgressLink = computed(() =>
  buildTeacherProgressLink({
    taskId: dashboard.value.taskId,
    view: 'matrix',
  }),
);

const grading = computed(() => props.data || {});
const remindData = computed(() => props.data || {});
const pendingStudents = computed(() =>
  Array.isArray(remindData.value.pendingStudents) ? remindData.value.pendingStudents : [],
);

const remindTaskLink = computed(() =>
  buildTeacherProgressLink({
    taskId: remindData.value.taskId,
    filter: 'remind',
    view: 'matrix',
  }),
);

function remindStudentLink(student) {
  return buildTeacherProgressLink({
    taskId: remindData.value.taskId,
    studentId: student.studentId,
    view: 'lane',
  });
}
const sentList = computed(() =>
  Array.isArray(remindData.value.sent) ? remindData.value.sent : [],
);
const batchData = computed(() => props.data || {});
const batchResults = computed(() =>
  Array.isArray(batchData.value.results) ? batchData.value.results : [],
);

function short(s) {
  const t = String(s || '');
  return t.length > 60 ? t.slice(0, 60) + '…' : t;
}

async function copyRemind() {
  const text = remindData.value.reminderMessage || remindData.value.shortMessage || '';
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    sendHint.value = '话术已复制到剪贴板';
  } catch {
    sendHint.value = '复制失败，请手动选择文本';
  }
}

async function sendRemindersNow() {
  const taskId = remindData.value.taskId;
  if (!taskId) return;
  sending.value = true;
  sendError.value = '';
  sendHint.value = '';
  try {
    const result = await classroomApi.sendReminders(taskId);
    emit('remind-sent', result);
  } catch (e) {
    sendError.value = e.message || '发送失败';
  } finally {
    sending.value = false;
  }
}
const submissions = computed(() => (Array.isArray(props.data) ? props.data : []));
const students = computed(() => (Array.isArray(props.data) ? props.data : []));

const rawPreview = computed(() => {
  try {
    const s = JSON.stringify(props.data, null, 2);
    return s.length > 1200 ? s.slice(0, 1200) + '…' : s;
  } catch {
    return String(props.data ?? '');
  }
});

function formatKey(key) {
  const labels = {
    totalStudents: '班级人数',
    submittedCount: '已提交',
    pendingCount: '待提交',
    completionRate: '完成率',
  };
  return labels[key] || key;
}
</script>

<style scoped>
.classroom-result {
  margin-top: 4px;
  width: min(540px, 100%);
}

.classroom-result--task {
  display: block;
  padding: 0;
  overflow: hidden;
}

.classroom-result--task::before {
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #0d9488, #059669);
}

.task-success {
  display: flex;
  flex-direction: column;
}

.task-success__hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: start;
  padding: 20px 22px 16px;
}

.task-success__icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.task-success__icon.is-draft {
  background: linear-gradient(145deg, #ecfdf5, #d1fae5);
  border: 1px solid rgba(16, 185, 129, 0.25);
  color: #059669;
}

.task-success__icon.is-live {
  background: linear-gradient(145deg, #eef2ff, #e0e7ff);
  border: 1px solid rgba(99, 102, 241, 0.25);
  color: #4f46e5;
}

.task-success__headline {
  font-size: 1.05rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.task-success__subtitle {
  margin: 6px 0 0;
  font-size: 0.84rem;
  color: #64748b;
  line-height: 1.55;
}

.task-success__badge {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.task-success__badge.is-draft {
  background: #fef3c7;
  color: #b45309;
  border: 1px solid rgba(245, 158, 11, 0.25);
}

.task-success__badge.is-live {
  background: #dcfce7;
  color: #15803d;
  border: 1px solid rgba(34, 197, 94, 0.25);
}

.task-success__panel {
  margin: 0 14px 14px;
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.task-success__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.task-success__title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
  letter-spacing: -0.03em;
}

.task-success__id {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 8px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  font-size: 0.72rem;
  font-weight: 700;
  color: #64748b;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.task-success__subs {
  margin-top: 16px;
}

.task-success__subs-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 10px;
}

.task-success__sub-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-success__sub-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 10px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
}

.task-success__sub-num {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: rgba(99, 102, 241, 0.12);
  color: #4f46e5;
  font-size: 0.78rem;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.task-success__sub-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
}

.task-success__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 0 22px 20px;
}

.task-success__btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 700;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.task-success__btn--primary {
  background: linear-gradient(135deg, #0d9488 0%, #059669 100%);
  color: #fff;
  box-shadow: 0 8px 20px rgba(13, 148, 136, 0.28);
}

.task-success__btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(13, 148, 136, 0.34);
}

.task-success__tip {
  font-size: 0.8rem;
  color: #94a3b8;
}

.classroom-sub-list {
  margin: 10px 0 0;
  padding-left: 1.1rem;
  font-size: 0.88rem;
  color: var(--zx-text, #1a3a35);
  line-height: 1.5;
}
.classroom-action-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 0.86rem;
  font-weight: 600;
  color: var(--zx-primary, #0d9488);
  text-decoration: none;
}
.classroom-action-link:hover {
  text-decoration: underline;
}
.classroom-dash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  margin-top: 10px;
}
.classroom-dash-cell {
  background: rgba(13, 148, 136, 0.08);
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 0.84rem;
}
.classroom-dash-cell .rk {
  display: block;
  font-size: 0.72rem;
  opacity: 0.75;
}
.classroom-comment {
  margin: 8px 0 0;
  font-size: 0.88rem;
  line-height: 1.55;
  white-space: pre-wrap;
}
.classroom-remind-summary {
  margin: 8px 0 0;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  font-size: 0.86rem;
  color: #334155;
  line-height: 1.55;
}
.classroom-pending-list {
  list-style: none;
  margin: 10px 0 0;
  padding: 0;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}
.classroom-pending-list li {
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.86rem;
}
.classroom-pending-list li:last-child {
  border-bottom: none;
}
.classroom-pending-link {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 10px 12px;
  text-decoration: none;
  color: inherit;
  transition: background 0.15s ease;
}
.classroom-pending-link:hover {
  background: rgba(13, 148, 136, 0.06);
}
.classroom-pending-arrow {
  flex-shrink: 0;
  font-size: 0.72rem;
  color: #94a3b8;
  margin-left: auto;
}
.classroom-pending-link:hover .classroom-pending-arrow {
  color: #0d9488;
}
.pending-name {
  font-weight: 600;
  color: #0f172a;
}
.pending-meta {
  color: #64748b;
}
.classroom-remind-task-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0d9488;
  font-weight: 600;
  text-decoration: none;
}
.classroom-remind-task-link:hover {
  text-decoration: underline;
}
.classroom-remind-task-link i {
  font-size: 0.72rem;
  opacity: 0.85;
}
.classroom-remind-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
.classroom-send-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: #0d9488;
  color: #fff;
  font-size: 0.86rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}
.classroom-send-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.classroom-copy-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  cursor: pointer;
  font-size: 0.84rem;
  font-family: inherit;
}
.classroom-send-err {
  color: #b91c1c;
  font-size: 0.84rem;
  margin-top: 8px;
}
.classroom-send-hint {
  color: #64748b;
  font-size: 0.82rem;
  margin-top: 6px;
}
.batch-err {
  color: #b91c1c;
}
.classroom-raw {
  font-size: 0.75rem;
  max-height: 200px;
  overflow: auto;
  margin: 8px 0 0;
  padding: 8px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 6px;
}
</style>
