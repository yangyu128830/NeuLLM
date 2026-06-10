<template>
  <div class="student-page classroom-theme">
    <StudentPageHeader
      title="我的作业"
      subtitle="查看任务说明 · 上传成果物（docx/pdf/txt/md，单文件 ≤20MB）· 等待教师批改"
      icon="fa-book-open"
      active="assignments"
      :unread-count="unreadCount"
      @logout="logout"
    />

    <main class="content page-inner">
      <div v-if="loading" class="state-box">
        <i class="fas fa-spinner fa-spin"></i>
        <p>加载中…</p>
      </div>

      <div v-else-if="!tasks.length" class="state-box">
        <i class="fas fa-inbox"></i>
        <p>暂无已发布作业</p>
        <span class="state-hint">请等待教师在课堂中发布任务</span>
      </div>

      <section v-else class="task-list">
        <article v-for="task in tasks" :key="task.taskId" class="task-card">
          <div class="task-head">
            <span class="task-id">{{ task.taskId }}</span>
            <h2>{{ task.title }}</h2>
          </div>
          <p v-if="task.description" class="task-desc">{{ task.description }}</p>

          <ul class="sub-list">
            <li v-for="sub in task.subTasks" :key="sub.subTaskId" class="sub-item">
              <div class="sub-info">
                <span class="sub-badge">{{ sub.subTaskId }}</span>
                <div>
                  <strong class="sub-title">{{ sub.title }}</strong>
                  <p v-if="sub.description" class="sub-hint">{{ sub.description }}</p>
                </div>
              </div>
              <div v-if="showSubmitted(sub)" class="status-done">
                <span class="status-badge" :class="statusClass(sub)">
                  <i class="fas fa-check-circle"></i>
                  {{ sub.statusLabel || '已提交' }}
                </span>
                <span v-if="sub.fileName" class="sub-file-name" :title="sub.fileName">{{ sub.fileName }}</span>
                <span v-if="sub.submittedAt" class="sub-time">{{ formatTime(sub.submittedAt) }}</span>
                <p v-if="sub.teacherComment && sub.status === 'GRADED'" class="sub-comment">
                  教师评语：{{ sub.teacherComment }}
                </p>
              </div>
              <span v-else-if="sub.status === 'LOCKED'" class="status-locked">
                <i class="fas fa-lock"></i> 未解锁
              </span>
              <label v-else-if="sub.canSubmit" class="upload-btn" :class="{ warn: sub.status === 'REJECTED' }">
                <i class="fas fa-cloud-upload-alt"></i>
                {{ sub.status === 'REJECTED' ? '重新提交' : '选择文件' }}
                <input
                  type="file"
                  class="upload-input"
                  accept=".docx,.pdf,.txt,.md"
                  @change="(e) => onFile(task.taskId, sub.subTaskId, e)"
                />
              </label>
            </li>
          </ul>
        </article>
      </section>
    </main>

    <Transition name="toast-fade">
      <p v-if="message" :class="['toast', msgOk ? 'ok' : 'fail']">{{ message }}</p>
    </Transition>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
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

/** 与后端 spring.servlet.multipart.max-file-size 一致 */
const MAX_FILE_BYTES = 20 * 1024 * 1024;

onMounted(load);

function showSubmitted(sub) {
  return sub.submissionId && sub.status !== 'REJECTED';
}

function statusClass(sub) {
  if (sub.status === 'GRADED') return 'graded';
  return 'submitted';
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
  background: #f1f5f9;
  color: var(--text);
  font-family: var(--font);
}

.topbar {
  flex-shrink: 0;
  background: var(--gradient-brand);
  color: #fff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
}

.topbar-inner {
  padding-top: 16px;
  padding-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.brand-tag {
  margin: 0 0 2px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  opacity: 0.85;
}

h1 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: #fff;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 0.85rem;
  opacity: 0.92;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-ghost,
.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-ghost {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.28);
}

.btn-outline {
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}

.nav-with-badge {
  position: relative;
}

.nav-badge {
  margin-left: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 18px;
  display: inline-block;
  text-align: center;
}

.content {
  flex: 1;
  padding-top: 16px;
  padding-bottom: 20px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}

.task-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  background: #fff;
}

.task-id {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
  background: var(--hint-bg);
  color: var(--accent-dark);
  flex-shrink: 0;
}

.task-head h2 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--accent-dark);
  word-break: break-word;
  line-height: 1.4;
}

.task-desc {
  margin: 0;
  padding: 12px 16px;
  font-size: 0.9rem;
  color: var(--muted);
  border-bottom: 1px solid var(--border);
  line-height: 1.6;
}

.sub-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.sub-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}

.sub-item:last-child {
  border-bottom: none;
}

.sub-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.sub-badge {
  flex-shrink: 0;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
  background: var(--accent-soft);
  color: var(--accent-dark);
}

.sub-title {
  display: block;
  font-size: 0.95rem;
  color: var(--text);
  margin-bottom: 4px;
  word-break: break-word;
  line-height: 1.45;
}

.sub-hint {
  margin: 0;
  font-size: 0.82rem;
  color: var(--muted);
  line-height: 1.5;
  word-break: break-word;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding: 9px 16px;
  border-radius: 8px;
  background: var(--gradient-submit);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.upload-btn:hover {
  opacity: 0.92;
}

.upload-btn.warn {
  background: linear-gradient(135deg, #b45309, #f59e0b);
}

.status-done {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  max-width: 42%;
  text-align: right;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-badge.submitted {
  background: #dcfce7;
  color: #166534;
}

.status-badge.graded {
  background: #ede9fe;
  color: #6d28d9;
}

.sub-file-name {
  font-size: 0.78rem;
  color: var(--muted);
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-time {
  font-size: 0.72rem;
  color: #94a3b8;
}

.sub-comment {
  margin: 4px 0 0;
  font-size: 0.78rem;
  color: var(--accent-dark);
  text-align: right;
  line-height: 1.4;
}

.status-locked {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #64748b;
  background: #f1f5f9;
}

.upload-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--muted);
}

.state-box i {
  font-size: 2rem;
  color: var(--accent);
  margin-bottom: 12px;
}

.state-box p {
  margin: 0;
  font-weight: 600;
  color: var(--text);
}

.state-hint {
  margin-top: 6px;
  font-size: 0.85rem;
}

.toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
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
  transform: translateY(8px);
}

@media (max-width: 640px) {
  .upload-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
