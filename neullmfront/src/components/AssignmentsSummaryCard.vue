<template>
  <div class="asg-sum">
    <header class="asg-sum__head">
      <span class="asg-sum__icon" aria-hidden="true"><i class="fas fa-book-open"></i></span>
      <div>
        <h4 class="asg-sum__title">作业待办</h4>
        <p v-if="!loading && !error" class="asg-sum__sub">
          {{ empty ? '暂无作业' : pendingCount > 0 ? `${pendingCount} 项待处理` : '全部交齐' }}
        </p>
      </div>
    </header>

    <div v-if="loading" class="asg-sum__state">
      <i class="fas fa-spinner fa-spin"></i>
      <span>正在查看你的作业进度…</span>
    </div>

    <div v-else-if="error" class="asg-sum__state asg-sum__state--error">
      <i class="fas fa-circle-exclamation"></i>
      <p>{{ error }}</p>
      <button type="button" class="asg-sum__retry" @click="$emit('retry')">重试</button>
    </div>

    <template v-else-if="empty">
      <div class="asg-sum__empty">
        <div class="asg-sum__empty-icon"><i class="fas fa-inbox"></i></div>
        <p class="asg-sum__empty-title">还没有已发布的作业</p>
        <p class="asg-sum__empty-hint">
          老师发布任务后会出现在「我的作业」里。有布置了我再帮你汇总哪些步骤要交～
        </p>
      </div>
      <footer class="asg-sum__foot">
        <router-link to="/assignments" class="asg-sum__link asg-sum__link--ghost">
          去我的作业看看
        </router-link>
      </footer>
    </template>

    <template v-else>
      <section class="asg-sum__ai">
        <p class="asg-sum__ai-text">{{ summary }}</p>
      </section>

      <section v-if="pendingItems.length" class="asg-sum__section">
        <h5 class="asg-sum__section-title">
          <span class="asg-sum__dot" aria-hidden="true"></span>
          待提交 / 需重交
        </h5>
        <ul class="asg-sum__list">
          <li
            v-for="(item, idx) in visibleItems"
            :key="`${item.taskId}-${item.subTaskId}-${idx}`"
            class="asg-sum__card"
          >
            <div class="asg-sum__card-icon" :class="tone(item.status)">
              <i :class="icon(item.status)"></i>
            </div>
            <div class="asg-sum__card-body">
              <span class="asg-sum__tag" :class="tone(item.status)">{{ item.statusLabel }}</span>
              <strong class="asg-sum__task">{{ item.taskTitle }}</strong>
              <p class="asg-sum__step">{{ item.subTaskTitle }}</p>
              <time v-if="item.endTime">截止 {{ formatTime(item.endTime) }}</time>
            </div>
          </li>
        </ul>
        <button
          v-if="hasMore"
          type="button"
          class="asg-sum__fold"
          @click="expanded = !expanded"
        >
          <i class="fas" :class="expanded ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
          {{
            expanded
              ? '收起'
              : `还有 ${pendingItems.length - previewLimit} 项，展开查看`
          }}
        </button>
      </section>

      <section v-else class="asg-sum__done">
        <i class="fas fa-check-circle"></i>
        <div>
          <strong>所有步骤都已提交</strong>
          <p>共 {{ totalTasks }} 个作业都交齐啦，等老师批改后会通知你。</p>
        </div>
      </section>

      <footer class="asg-sum__foot">
        <router-link to="/assignments" class="asg-sum__link">
          <i class="fas fa-arrow-right"></i>
          去提交作业
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
  pendingCount: { type: Number, default: 0 },
  totalTasks: { type: Number, default: 0 },
  pendingItems: { type: Array, default: () => [] },
  empty: { type: Boolean, default: false },
});

defineEmits(['retry']);

const expanded = ref(false);

const hasMore = computed(() => props.pendingItems.length > previewLimit);

const visibleItems = computed(() =>
  expanded.value ? props.pendingItems : props.pendingItems.slice(0, previewLimit),
);

function formatTime(value) {
  return formatApiDateTime(value, { style: 'short' });
}

function tone(status) {
  return status === 'REJECTED' ? 'tone-reject' : 'tone-pending';
}

function icon(status) {
  return status === 'REJECTED' ? 'fas fa-redo' : 'fas fa-upload';
}
</script>

<style scoped>
.asg-sum {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(165deg, #eff6ff 0%, #fff 42%);
  border: 1px solid rgba(59, 130, 246, 0.16);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.07);
}

.asg-sum__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 12px;
}

.asg-sum__icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 17px;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.28);
}

.asg-sum__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.asg-sum__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.asg-sum__state {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px 20px;
  font-size: 14px;
  color: #64748b;
}

.asg-sum__state--error {
  flex-direction: column;
  align-items: flex-start;
}

.asg-sum__state--error p {
  margin: 0;
  color: #b45309;
}

.asg-sum__retry {
  margin-top: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.asg-sum__empty {
  padding: 8px 18px 4px;
  text-align: center;
}

.asg-sum__empty-icon {
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

.asg-sum__empty-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.asg-sum__empty-hint {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #64748b;
}

.asg-sum__ai {
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(59, 130, 246, 0.12);
}

.asg-sum__ai-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
}

.asg-sum__section {
  padding: 0 14px 12px;
}

.asg-sum__section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 4px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.asg-sum__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.2);
}

.asg-sum__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.asg-sum__fold {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  margin-top: 8px;
  padding: 8px 12px;
  border: none;
  border-radius: 10px;
  background: rgba(59, 130, 246, 0.08);
  color: #2563eb;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.asg-sum__fold:hover {
  background: rgba(59, 130, 246, 0.14);
}

.asg-sum__card {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(59, 130, 246, 0.14);
}

.asg-sum__card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.tone-pending {
  background: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
}

.tone-reject {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.asg-sum__card-body {
  min-width: 0;
  flex: 1;
}

.asg-sum__tag {
  display: inline-block;
  margin-bottom: 4px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.asg-sum__task {
  display: block;
  font-size: 14px;
  color: #0f172a;
  margin-bottom: 2px;
}

.asg-sum__step {
  margin: 0 0 4px;
  font-size: 13px;
  color: #64748b;
}

.asg-sum__card-body time {
  font-size: 11px;
  color: #94a3b8;
}

.asg-sum__done {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(16, 185, 129, 0.08);
  border: 1px solid rgba(16, 185, 129, 0.18);
}

.asg-sum__done > i {
  color: #059669;
  font-size: 20px;
  margin-top: 2px;
}

.asg-sum__done strong {
  display: block;
  font-size: 14px;
  color: #065f46;
  margin-bottom: 4px;
}

.asg-sum__done p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #047857;
}

.asg-sum__foot {
  padding: 4px 14px 16px;
}

.asg-sum__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}

.asg-sum__link:hover {
  background: #1d4ed8;
}

.asg-sum__link--ghost {
  background: #fff;
  color: #2563eb;
  border: 1px solid rgba(37, 99, 235, 0.35);
}

.asg-sum__link--ghost:hover {
  background: #eff6ff;
}
</style>
