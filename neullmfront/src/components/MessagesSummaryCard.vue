<template>
  <div class="msg-sum">
    <header class="msg-sum__head">
      <span class="msg-sum__icon" aria-hidden="true"><i class="fas fa-bell"></i></span>
      <div>
        <h4 class="msg-sum__title">消息概况</h4>
        <p v-if="!loading && !error" class="msg-sum__sub">
          {{ empty ? '暂无通知' : unread > 0 ? `${unread} 条待查看` : '暂无新消息' }}
        </p>
      </div>
    </header>

    <div v-if="loading" class="msg-sum__state">
      <i class="fas fa-spinner fa-spin"></i>
      <span>正在读取并帮你总结…</span>
    </div>

    <div v-else-if="error" class="msg-sum__state msg-sum__state--error">
      <i class="fas fa-circle-exclamation"></i>
      <p>{{ error }}</p>
      <button type="button" class="msg-sum__retry" @click="$emit('retry')">重试</button>
    </div>

    <template v-else-if="empty">
      <div class="msg-sum__empty">
        <div class="msg-sum__empty-icon"><i class="fas fa-inbox"></i></div>
        <p class="msg-sum__empty-title">还没有收到消息</p>
        <p class="msg-sum__empty-hint">
          老师发布作业、催交或批改完成后，通知会出现在这里。有动静了我再帮你汇总～
        </p>
      </div>
      <footer class="msg-sum__foot">
        <router-link to="/messages" class="msg-sum__link msg-sum__link--ghost">
          去消息中心看看
        </router-link>
      </footer>
    </template>

    <template v-else>
      <section class="msg-sum__ai">
        <p class="msg-sum__ai-text">{{ summary }}</p>
      </section>

      <section v-if="unreadItems.length" class="msg-sum__section">
        <h5 class="msg-sum__section-title">
          <span class="msg-sum__dot" aria-hidden="true"></span>
          未读消息
        </h5>
        <ul class="msg-sum__list">
          <li v-for="item in visibleItems" :key="item.id" class="msg-sum__card">
            <div class="msg-sum__card-icon" :class="typeTone(item.type)">
              <i :class="typeIcon(item.type)"></i>
            </div>
            <div class="msg-sum__card-body">
              <span class="msg-sum__tag" :class="typeTone(item.type)">{{ item.typeLabel }}</span>
              <strong class="msg-sum__card-title">{{ item.title }}</strong>
              <p class="msg-sum__card-snippet">{{ snippet(item.content) }}</p>
              <time>{{ formatTime(item.createdAt) }}</time>
            </div>
          </li>
        </ul>
        <button
          v-if="hasMore"
          type="button"
          class="msg-sum__fold"
          @click="expanded = !expanded"
        >
          <i class="fas" :class="expanded ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
          {{
            expanded
              ? '收起'
              : `还有 ${unreadItems.length - previewLimit} 条，展开查看`
          }}
        </button>
      </section>

      <section v-else class="msg-sum__caught-up">
        <i class="fas fa-check-circle"></i>
        <div>
          <strong>暂时没有新的未读消息</strong>
          <p>之前的通知你都看过了，有作业相关更新时我会再提醒你。</p>
        </div>
      </section>

      <footer class="msg-sum__foot">
        <router-link to="/messages" class="msg-sum__link">
          <i class="fas fa-arrow-right"></i>
          查看全部消息
        </router-link>
      </footer>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { formatApiDateTime } from '@/utils/datetime';

const previewLimit = 3;

const props = defineProps({
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  summary: { type: String, default: '' },
  unread: { type: Number, default: 0 },
  unreadItems: { type: Array, default: () => [] },
  empty: { type: Boolean, default: false },
});

defineEmits(['retry']);

const expanded = ref(false);

const hasMore = computed(() => props.unreadItems.length > previewLimit);

const visibleItems = computed(() =>
  expanded.value ? props.unreadItems : props.unreadItems.slice(0, previewLimit),
);

function formatTime(value) {
  return formatApiDateTime(value, { style: 'short' });
}

function snippet(text) {
  const s = (text || '').trim();
  if (!s) return '—';
  return s.length > 72 ? `${s.slice(0, 72)}…` : s;
}

function typeIcon(type) {
  switch (type) {
    case 'TASK_REMIND':
      return 'fas fa-bell';
    case 'TASK_PUBLISHED':
      return 'fas fa-book-open';
    case 'GRADE_RESULT':
      return 'fas fa-check-circle';
    case 'ACTIVITY':
      return 'fas fa-star';
    default:
      return 'fas fa-info-circle';
  }
}

function typeTone(type) {
  switch (type) {
    case 'TASK_REMIND':
      return 'tone-remind';
    case 'TASK_PUBLISHED':
      return 'tone-task';
    case 'GRADE_RESULT':
      return 'tone-grade';
    case 'ACTIVITY':
      return 'tone-activity';
    default:
      return 'tone-system';
  }
}
</script>

<style scoped>
.msg-sum {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(165deg, #f0fdfa 0%, #fff 42%);
  border: 1px solid rgba(13, 148, 136, 0.16);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.07);
}

.msg-sum__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 12px;
}

.msg-sum__icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #14b8a6, #0d9488);
  color: #fff;
  font-size: 17px;
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.28);
}

.msg-sum__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.msg-sum__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.msg-sum__state {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px 20px;
  font-size: 14px;
  color: #64748b;
}

.msg-sum__state--error {
  flex-direction: column;
  align-items: flex-start;
}

.msg-sum__state--error p {
  margin: 0;
  color: #b45309;
}

.msg-sum__retry {
  margin-top: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.msg-sum__empty {
  padding: 8px 18px 4px;
  text-align: center;
}

.msg-sum__empty-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(148, 163, 184, 0.12);
  color: #94a3b8;
  font-size: 22px;
}

.msg-sum__empty-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.msg-sum__empty-hint {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #64748b;
}

.msg-sum__ai {
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(13, 148, 136, 0.1);
}

.msg-sum__ai-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
}

.msg-sum__section {
  padding: 0 14px 12px;
}

.msg-sum__section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 4px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.msg-sum__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.18);
}

.msg-sum__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-sum__fold {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  margin-top: 8px;
  padding: 8px 12px;
  border: none;
  border-radius: 10px;
  background: rgba(13, 148, 136, 0.08);
  color: #0d9488;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.msg-sum__fold:hover {
  background: rgba(13, 148, 136, 0.14);
}

.msg-sum__card {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(13, 148, 136, 0.14);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

.msg-sum__card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.msg-sum__card-body {
  min-width: 0;
  flex: 1;
}

.msg-sum__tag {
  display: inline-block;
  margin-bottom: 4px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.msg-sum__card-title {
  display: block;
  font-size: 14px;
  line-height: 1.4;
  color: #0f172a;
  margin-bottom: 4px;
}

.msg-sum__card-snippet {
  margin: 0 0 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #64748b;
}

.msg-sum__card-body time {
  font-size: 11px;
  color: #94a3b8;
}

.tone-remind {
  background: rgba(245, 158, 11, 0.14);
  color: #b45309;
}
.tone-task {
  background: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
}
.tone-grade {
  background: rgba(16, 185, 129, 0.12);
  color: #047857;
}
.tone-activity {
  background: rgba(168, 85, 247, 0.12);
  color: #7e22ce;
}
.tone-system {
  background: rgba(100, 116, 139, 0.12);
  color: #475569;
}

.msg-sum__caught-up {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(16, 185, 129, 0.08);
  border: 1px solid rgba(16, 185, 129, 0.18);
}

.msg-sum__caught-up > i {
  color: #059669;
  font-size: 20px;
  margin-top: 2px;
}

.msg-sum__caught-up strong {
  display: block;
  font-size: 14px;
  color: #065f46;
  margin-bottom: 4px;
}

.msg-sum__caught-up p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #047857;
}

.msg-sum__foot {
  padding: 4px 14px 16px;
}

.msg-sum__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  background: #0d9488;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.15s ease;
}

.msg-sum__link:hover {
  background: #0f766e;
}

.msg-sum__link--ghost {
  background: #fff;
  color: #0d9488;
  border: 1px solid rgba(13, 148, 136, 0.35);
}

.msg-sum__link--ghost:hover {
  background: #f0fdfa;
}
</style>
