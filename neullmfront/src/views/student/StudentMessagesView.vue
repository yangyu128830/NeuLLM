<!-- 学生消息中心页面（路由 /messages，对应 router/index.js 的 StudentMessages） -->
<template>
  <div class="student-page classroom-theme messages-page">
    <StudentPageHeader
      title="消息中心"
      icon="fa-bell"
      active="messages"
      :unread-count="unread"
      @logout="logout"
    />

    <main class="content page-inner">
      <div v-if="!loading && !loadError" class="msgs-sticky">
        <div class="msgs-bar">
          <div class="msgs-bar__status">
            <span v-if="unread > 0" class="unread-pill">
              {{ unread }} 未读
            </span>
            <span v-else class="toolbar-muted">全部已读</span>
          </div>
          <div class="msgs-bar__actions">
            <button
              type="button"
              class="msgs-icon-btn"
              :disabled="loading"
              title="刷新消息"
              aria-label="刷新消息"
              @click="load"
            >
              <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }" aria-hidden="true"></i>
            </button>
            <button
              v-if="unread > 0"
              type="button"
              class="msgs-icon-btn"
              :disabled="markingAll"
              title="全部标为已读"
              aria-label="全部标为已读"
              @click="markAllRead"
            >
              <i class="fas fa-check-double" aria-hidden="true"></i>
            </button>
          </div>
        </div>

        <div
          v-if="items.length"
          class="type-filter"
          role="tablist"
          aria-label="消息分类筛选"
        >
          <button
            v-for="opt in typeFilterOptions"
            :key="opt.value"
            type="button"
            role="tab"
            class="type-filter__btn"
            :class="{ 'type-filter__btn--active': typeFilter === opt.value }"
            :aria-selected="typeFilter === opt.value"
            @click="typeFilter = opt.value"
          >
            {{ opt.label }}
            <em v-if="opt.count > 0">{{ opt.count }}</em>
          </button>
        </div>
      </div>

      <div v-if="loading" class="state-box">
        <div class="state-icon-wrap">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
        <p>加载消息中…</p>
      </div>

      <div v-else-if="loadError" class="state-box state-box--error">
        <div class="state-icon-wrap state-icon-wrap--warn">
          <i class="fas fa-circle-exclamation"></i>
        </div>
        <p>加载失败</p>
        <span class="state-hint">{{ loadError }}</span>
        <button type="button" class="btn-retry" @click="load">重试</button>
      </div>

      <div v-else-if="!items.length" class="state-box state-box--empty">
        <div class="state-icon-wrap state-icon-wrap--empty">
          <i class="fas fa-inbox"></i>
        </div>
        <p>暂无消息</p>
        <span class="state-hint">教师催交、发布作业或批改完成后，通知会出现在这里</span>
        <div class="empty-tips">
          <div class="empty-tip">
            <i class="fas fa-bell"></i>
            <span>作业催交提醒</span>
          </div>
          <div class="empty-tip">
            <i class="fas fa-book-open"></i>
            <span>新作业发布</span>
          </div>
          <div class="empty-tip">
            <i class="fas fa-check-circle"></i>
            <span>批改与反馈</span>
          </div>
        </div>
      </div>

      <div v-else-if="!filteredItems.length" class="state-box state-box--empty">
        <div class="state-icon-wrap state-icon-wrap--empty">
          <i class="fas fa-filter"></i>
        </div>
        <p>该分类暂无消息</p>
        <span class="state-hint">切换其他分类或等待新通知</span>
        <button type="button" class="btn-retry btn-retry--ghost" @click="typeFilter = 'all'">
          查看全部消息
        </button>
      </div>

      <!-- 消息列表：item.read 为 false 时加 msg-card--unread（左侧绿边 + 浅绿背景） -->
      <ul v-else class="msg-list">
        <li
          v-for="item in filteredItems"
          :key="item.id"
          :class="['msg-card', { 'msg-card--unread': !item.read }]"
          @click="openItem(item)"
        >
          <!-- 按后端 type 切换图标/颜色：TASK_REMIND 催交、GRADE_RESULT 批改等 -->
          <div class="msg-card__icon" :class="typeTone(item.type)">
            <i :class="typeIcon(item.type)"></i>
          </div>
          <div class="msg-card__body">
            <div class="msg-card__head">
              <!-- typeLabel 由后端 NotificationService 转成中文，如「作业催交」 -->
              <span class="msg-type" :class="typeTone(item.type)">{{ item.typeLabel }}</span>
              <span class="msg-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <h3 class="msg-title">{{ item.title }}</h3>
            <p class="msg-body">{{ item.content }}</p>
          </div>
          <span v-if="!item.read" class="msg-dot" aria-label="未读"></span>
        </li>
      </ul>
    </main>
  </div>
</template>

<script setup>
/**
 * 学生消息中心 StudentMessagesView
 * 前后端配合：
 *   - 列表 GET /api/notifications          → items
 *   - 未读数 GET /api/notifications/unread-count → unread
 *   - 单条已读 POST /api/notifications/{id}/read
 *   - 全部已读 POST /api/notifications/read-all
 * 后端入口：NotificationController → NotificationService
 */
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import StudentPageHeader from '@/components/student/StudentPageHeader.vue';
import notificationsApi from '@/services/notificationsApi';
import authApi from '@/services/authApi';
import { clearAuth } from '@/stores/auth';
import { formatApiDateTime } from '@/utils/datetime';

const router = useRouter();
const loading = ref(true);
const markingAll = ref(false);
const loadError = ref('');
const items = ref([]); // 消息列表，每条含 id / type / typeLabel / read / title / content / linkPath
const unread = ref(0); // 未读总数，驱动顶部「N 条未读」和 msg-card--unread 以外的全局 UI
const typeFilter = ref('all');

const TYPE_FILTER_OPTIONS = [
  { value: 'all', label: '全部', types: null },
  { value: 'TASK_REMIND', label: '作业催交', types: ['TASK_REMIND'] },
  { value: 'TASK_PUBLISHED', label: '新作业', types: ['TASK_PUBLISHED'] },
  { value: 'GRADE_RESULT', label: '批改结果', types: ['GRADE_RESULT'] },
  { value: 'ACTIVITY', label: '活动', types: ['ACTIVITY'] },
];

const typeFilterOptions = computed(() =>
  TYPE_FILTER_OPTIONS.map((opt) => ({
    ...opt,
    count: opt.types
      ? items.value.filter((it) => opt.types.includes(it.type)).length
      : items.value.length,
  })),
);

const filteredItems = computed(() => {
  const opt = TYPE_FILTER_OPTIONS.find((o) => o.value === typeFilter.value);
  if (!opt?.types) return items.value;
  return items.value.filter((it) => opt.types.includes(it.type));
});

/** 消息类型 → Font Awesome 图标（与后端 NotificationType 常量对应） */
function typeIcon(type) {
  switch (type) {
    case 'TASK_REMIND': return 'fas fa-bell';
    case 'TASK_PUBLISHED': return 'fas fa-book-open';
    case 'GRADE_RESULT': return 'fas fa-check-circle';
    case 'ACTIVITY': return 'fas fa-calendar';
    default: return 'fas fa-info-circle';
  }
}

/** 消息类型 → CSS 色调类名（tone-remind / tone-grade 等，见下方 style） */
function typeTone(type) {
  switch (type) {
    case 'TASK_REMIND': return 'tone-remind';
    case 'TASK_PUBLISHED': return 'tone-task';
    case 'GRADE_RESULT': return 'tone-grade';
    case 'ACTIVITY': return 'tone-activity';
    default: return 'tone-system';
  }
}

/** 进入页面 / 点刷新：并行拉列表与未读数 */
async function load() {
  loading.value = true;
  loadError.value = '';
  try {
    const [list, countRes] = await Promise.all([
      notificationsApi.list(50),       // GET /api/notifications
      notificationsApi.unreadCount(),  // GET /api/notifications/unread-count → { count }
    ]);
    items.value = list || [];
    unread.value = countRes?.count ?? 0;
  } catch (e) {
    loadError.value = e?.message || '请确认已登录且后端已启动';
    items.value = [];
    unread.value = 0;
  } finally {
    loading.value = false;
  }
}

/** 点击卡片：未读则先 markRead，再跳转 linkPath（默认作业页） */
async function openItem(item) {
  if (!item.read) {
    try {
      await notificationsApi.markRead(item.id); // 后端更新 read_flag，前端同步去掉 msg-card--unread / msg-dot
      item.read = true;
      unread.value = Math.max(0, unread.value - 1);
    } catch {
      /* ignore */
    }
  }
  const path = item.linkPath || '/assignments';
  if (path.startsWith('/')) {
    router.push(path);
  }
}

/** 全部已读：调后端批量更新，本地 items 全部 read=true，unread 归零 */
async function markAllRead() {
  markingAll.value = true;
  try {
    await notificationsApi.markAllRead();
    for (const it of items.value) {
      it.read = true;
    }
    unread.value = 0;
  } finally {
    markingAll.value = false;
  }
}

function formatTime(t) {
  return formatApiDateTime(t, { style: 'short' });
}

async function logout() {
  try {
    await authApi.logout();
  } finally {
    clearAuth();
    router.push('/login');
  }
}

onMounted(load); // 路由进入 /messages 时自动加载
</script>

<style scoped>
@import '../../assets/classroom-theme.css';

.student-page {
  min-height: 100vh;
  max-width: 100%;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  background: var(--page-bg);
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
  border: none;
  font-family: inherit;
}

.btn-ghost {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.btn-ghost:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.28);
}

.btn-outline {
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.btn-outline:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.12);
}

.btn-ghost:disabled,
.btn-outline:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.content {
  flex: 1;
  padding-top: 12px;
  padding-bottom: 24px;
}

.msgs-sticky {
  position: sticky;
  top: 0;
  z-index: 20;
  margin-bottom: 10px;
  padding-bottom: 2px;
  background: var(--page-bg);
}

.msgs-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.msgs-bar__status {
  min-width: 0;
}

.msgs-bar__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.msgs-icon-btn {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--accent-dark);
  font-size: 0.82rem;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
  -webkit-tap-highlight-color: transparent;
}

.msgs-icon-btn i {
  font-size: 0.82rem;
  line-height: 1;
  pointer-events: none;
}

.msgs-icon-btn:hover:not(:disabled) {
  background: #f0fdfa;
  border-color: rgba(20, 184, 166, 0.28);
  color: var(--accent-dark);
}

.msgs-icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-muted {
  font-size: 0.82rem;
  color: var(--muted);
}

.unread-pill {
  display: inline-flex;
  align-items: center;
  font-size: 0.78rem;
  font-weight: 700;
  color: #b45309;
  background: #fffbeb;
  padding: 3px 9px;
  border-radius: 999px;
  border: 1px solid #fde68a;
}

.type-filter {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding: 2px 0 4px;
}

.type-filter::-webkit-scrollbar {
  display: none;
}

.type-filter__btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--muted);
  font-size: 0.76rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  white-space: nowrap;
}

.type-filter__btn:hover:not(.type-filter__btn--active) {
  background: #f1f5f9;
  border-color: rgba(15, 23, 42, 0.12);
  color: var(--text);
}

.type-filter__btn--active {
  background: var(--hint-bg);
  border-color: rgba(20, 184, 166, 0.35);
  color: var(--accent-dark);
}

.type-filter__btn em {
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.07);
  color: var(--muted);
  font-size: 0.62rem;
  font-weight: 700;
  font-style: normal;
  line-height: 16px;
  text-align: center;
}

.type-filter__btn--active em {
  background: rgba(20, 184, 166, 0.18);
  color: var(--accent-dark);
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 32px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--muted);
  text-align: center;
}

.state-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--hint-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.state-icon-wrap i {
  font-size: 1.75rem;
  color: var(--accent);
}

.state-icon-wrap--empty {
  background: #f1f5f9;
}

.state-icon-wrap--empty i {
  color: #94a3b8;
}

.state-icon-wrap--warn {
  background: #fef3c7;
}

.state-icon-wrap--warn i {
  color: #d97706;
}

.state-box p {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text);
}

.state-hint {
  margin-top: 8px;
  font-size: 0.88rem;
  line-height: 1.55;
  max-width: 360px;
}

.btn-retry {
  margin-top: 18px;
  padding: 9px 20px;
  border-radius: 8px;
  border: none;
  background: var(--gradient-submit);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.btn-retry--ghost {
  background: #fff;
  border: 1px solid var(--border);
  color: var(--text);
}

.btn-retry--ghost:hover {
  background: #f8fafc;
}

.empty-tips {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
  justify-content: center;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  min-width: 100px;
  background: #f8fafc;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--muted);
}

.empty-tip i {
  font-size: 1.1rem;
  color: var(--accent-dark);
}

.msg-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.msg-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 16px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.msg-card:active {
  background: #f8fafc;
}

.msg-card:hover {
  border-color: rgba(20, 184, 166, 0.35);
  box-shadow: var(--shadow-sm);
}

.msg-card--unread {
  border-left: 3px solid var(--accent-dark);
  background: linear-gradient(90deg, rgba(20, 184, 166, 0.04) 0%, #fff 80px);
}

.msg-card__icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.96rem;
}

.tone-remind {
  background: #fef3c7;
  color: #b45309;
}

.tone-task {
  background: #ecfdf5;
  color: #0f766e;
}

.tone-grade {
  background: #ede9fe;
  color: #6d28d9;
}

.tone-activity {
  background: #dbeafe;
  color: #1d4ed8;
}

.tone-system {
  background: #f1f5f9;
  color: #64748b;
}

.msg-card__body {
  flex: 1;
  min-width: 0;
}

.msg-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.msg-type {
  font-size: 0.72rem;
  font-weight: 700;
  padding: 2px 9px;
  border-radius: 999px;
}

.msg-type.tone-remind { background: #fef3c7; color: #b45309; }
.msg-type.tone-task { background: #ecfdf5; color: #0f766e; }
.msg-type.tone-grade { background: #ede9fe; color: #6d28d9; }
.msg-type.tone-activity { background: #dbeafe; color: #1d4ed8; }
.msg-type.tone-system { background: #f1f5f9; color: #64748b; }

.msg-time {
  font-size: 0.76rem;
  color: #94a3b8;
  flex-shrink: 0;
}

.msg-title {
  margin: 0 0 5px;
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--text);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-body {
  margin: 0;
  font-size: 0.86rem;
  color: var(--muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-dot {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 0 2px #fff;
}

@media (max-width: 767px) {
  .content {
    padding-top: 8px;
  }

  .msgs-sticky {
    margin-bottom: 8px;
  }

  .msgs-bar {
    margin-bottom: 6px;
  }

  .msgs-icon-btn {
    width: 32px;
    height: 32px;
    border-radius: 9px;
    font-size: 0.78rem;
  }

  .unread-pill {
    font-size: 0.72rem;
    padding: 2px 8px;
  }

  .type-filter__btn {
    padding: 4px 10px;
    font-size: 0.72rem;
  }

  .msg-list {
    gap: 8px;
  }

  .msg-card {
    padding: 13px 14px;
    gap: 12px;
    border-radius: 13px;
  }

  .msg-card__icon {
    width: 38px;
    height: 38px;
    font-size: 0.9rem;
    border-radius: 10px;
  }

  .msg-type {
    font-size: 0.7rem;
  }

  .msg-time {
    font-size: 0.72rem;
  }

  .msg-title {
    font-size: 0.92rem;
  }

  .msg-body {
    font-size: 0.82rem;
  }

  .msg-dot {
    top: 13px;
    right: 13px;
    width: 7px;
    height: 7px;
  }
}
</style>
