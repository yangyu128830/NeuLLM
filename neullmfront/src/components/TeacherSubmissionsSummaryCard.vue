<template>
  <div class="tch-sub">
    <header class="tch-sub__head">
      <span class="tch-sub__icon" aria-hidden="true"><i class="fas fa-inbox"></i></span>
      <div>
        <h4 class="tch-sub__title">学生提交概况</h4>
        <p v-if="!loading && !error" class="tch-sub__sub">
          {{ empty ? '暂无提交' : pendingCount > 0 ? `${pendingCount} 条待批改` : '暂无待批改' }}
        </p>
      </div>
    </header>

    <div v-if="loading" class="tch-sub__state">
      <i class="fas fa-spinner fa-spin"></i>
      <span>正在汇总学生提交…</span>
    </div>

    <div v-else-if="error" class="tch-sub__state tch-sub__state--error">
      <i class="fas fa-circle-exclamation"></i>
      <p>{{ error }}</p>
      <button type="button" class="tch-sub__retry" @click="$emit('retry')">重试</button>
    </div>

    <template v-else-if="empty">
      <div class="tch-sub__empty">
        <div class="tch-sub__empty-icon"><i class="fas fa-inbox"></i></div>
        <p class="tch-sub__empty-title">还没有学生提交</p>
        <p class="tch-sub__empty-hint">
          学生交作业后，待批改的会出现在这里。你也可以先去任务管理发布作业～
        </p>
      </div>
      <footer class="tch-sub__foot">
        <router-link to="/teacher/tasks" class="tch-sub__link tch-sub__link--ghost">
          去任务管理
        </router-link>
      </footer>
    </template>

    <template v-else>
      <section class="tch-sub__ai">
        <p class="tch-sub__ai-text">{{ summary }}</p>
      </section>

      <section v-if="pendingItems.length" class="tch-sub__section">
        <h5 class="tch-sub__section-title">
          <span class="tch-sub__dot" aria-hidden="true"></span>
          待批改提交
        </h5>
        <ul class="tch-sub__list">
          <li
            v-for="(item, idx) in visibleItems"
            :key="`${item.submissionId}-${idx}`"
          >
            <router-link
              :to="gradingLink(item)"
              class="tch-sub__card tch-sub__card--link"
            >
              <div class="tch-sub__card-icon">
                <i class="fas fa-file-alt"></i>
              </div>
              <div class="tch-sub__card-body">
                <span class="tch-sub__tag">{{ item.statusLabel || '待批改' }}</span>
                <strong class="tch-sub__student">{{ item.studentName || item.studentNo || '学生' }}</strong>
                <p class="tch-sub__task">{{ item.taskTitle }}</p>
                <p class="tch-sub__meta">{{ item.submissionId }}<template v-if="item.fileName"> · {{ item.fileName }}</template></p>
                <time v-if="item.submittedAt">提交于 {{ formatTime(item.submittedAt) }}</time>
              </div>
              <i class="fas fa-chevron-right tch-sub__card-arrow" aria-hidden="true"></i>
            </router-link>
          </li>
        </ul>
        <button
          v-if="hasMore"
          type="button"
          class="tch-sub__fold"
          @click="expanded = !expanded"
        >
          <i class="fas" :class="expanded ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
          {{
            expanded
              ? '收起'
              : `还有 ${pendingItems.length - previewLimit} 条，展开查看`
          }}
        </button>
      </section>

      <section v-else class="tch-sub__caught-up">
        <i class="fas fa-check-circle"></i>
        <div>
          <strong>暂无新的待批改提交</strong>
          <p>共 {{ totalSubmissions }} 条历史提交，最近的都已批改完成。</p>
        </div>
      </section>

      <footer class="tch-sub__foot">
        <router-link :to="gradingFootLink" class="tch-sub__link">
          <i class="fas fa-arrow-right"></i>
          {{ pendingItems.length ? '前往批改这条' : '前往批改台' }}
        </router-link>
      </footer>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { formatApiDateTime } from '@/utils/datetime';
import { buildTeacherGradingLink } from '@/utils/teacherSubmissionSummary';

const previewLimit = 3;

const props = defineProps({
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  summary: { type: String, default: '' },
  pendingCount: { type: Number, default: 0 },
  totalSubmissions: { type: Number, default: 0 },
  pendingItems: { type: Array, default: () => [] },
  empty: { type: Boolean, default: false },
});

defineEmits(['retry']);

const expanded = ref(false);

const hasMore = computed(() => props.pendingItems.length > previewLimit);

const visibleItems = computed(() =>
  expanded.value ? props.pendingItems : props.pendingItems.slice(0, previewLimit),
);

const gradingFootLink = computed(() => {
  if (props.pendingItems.length) return buildTeacherGradingLink(props.pendingItems[0]);
  return '/teacher/grading?status=pending';
});

function gradingLink(item) {
  return buildTeacherGradingLink(item);
}

function formatTime(value) {
  return formatApiDateTime(value, { style: 'short' });
}
</script>

<style scoped>
.tch-sub {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(165deg, #f5f3ff 0%, #fff 42%);
  border: 1px solid rgba(99, 102, 241, 0.18);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.07);
}

.tch-sub__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 12px;
}

.tch-sub__icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  font-size: 17px;
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.28);
}

.tch-sub__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.tch-sub__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.tch-sub__state {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px 20px;
  font-size: 14px;
  color: #64748b;
}

.tch-sub__state--error {
  flex-direction: column;
  align-items: flex-start;
}

.tch-sub__state--error p {
  margin: 0;
  color: #b45309;
}

.tch-sub__retry {
  margin-top: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.tch-sub__empty {
  padding: 8px 18px 4px;
  text-align: center;
}

.tch-sub__empty-icon {
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

.tch-sub__empty-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.tch-sub__empty-hint {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #64748b;
}

.tch-sub__ai {
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.tch-sub__ai-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
}

.tch-sub__section {
  padding: 0 14px 12px;
}

.tch-sub__section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 4px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.tch-sub__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.tch-sub__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tch-sub__fold {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  margin-top: 8px;
  padding: 8px 12px;
  border: none;
  border-radius: 10px;
  background: rgba(99, 102, 241, 0.08);
  color: #4f46e5;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.tch-sub__fold:hover {
  background: rgba(99, 102, 241, 0.14);
}

.tch-sub__card {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(99, 102, 241, 0.14);
}

.tch-sub__card--link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}

.tch-sub__card--link:hover {
  border-color: rgba(99, 102, 241, 0.35);
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.12);
  transform: translateY(-1px);
}

.tch-sub__card-arrow {
  flex-shrink: 0;
  align-self: center;
  font-size: 12px;
  color: #94a3b8;
  margin-left: auto;
}

.tch-sub__card--link:hover .tch-sub__card-arrow {
  color: #6366f1;
}

.tch-sub__card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: rgba(99, 102, 241, 0.12);
  color: #4f46e5;
}

.tch-sub__card-body {
  min-width: 0;
  flex: 1;
}

.tch-sub__tag {
  display: inline-block;
  margin-bottom: 4px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(245, 158, 11, 0.14);
  color: #b45309;
}

.tch-sub__student {
  display: block;
  font-size: 14px;
  color: #0f172a;
  margin-bottom: 2px;
}

.tch-sub__task {
  margin: 0 0 4px;
  font-size: 13px;
  color: #475569;
}

.tch-sub__meta {
  margin: 0 0 4px;
  font-size: 12px;
  color: #94a3b8;
}

.tch-sub__card-body time {
  font-size: 11px;
  color: #94a3b8;
}

.tch-sub__caught-up {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(16, 185, 129, 0.08);
  border: 1px solid rgba(16, 185, 129, 0.18);
}

.tch-sub__caught-up > i {
  color: #059669;
  font-size: 20px;
  margin-top: 2px;
}

.tch-sub__caught-up strong {
  display: block;
  font-size: 14px;
  color: #065f46;
  margin-bottom: 4px;
}

.tch-sub__caught-up p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #047857;
}

.tch-sub__foot {
  padding: 4px 14px 16px;
}

.tch-sub__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  background: #4f46e5;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}

.tch-sub__link:hover {
  background: #4338ca;
}

.tch-sub__link--ghost {
  background: #fff;
  color: #4f46e5;
  border: 1px solid rgba(79, 70, 229, 0.35);
}

.tch-sub__link--ghost:hover {
  background: #f5f3ff;
}
</style>
