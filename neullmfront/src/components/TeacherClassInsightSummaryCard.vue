<template>
  <div class="tch-insight">
    <header class="tch-insight__head">
      <span class="tch-insight__icon" aria-hidden="true"><i class="fas fa-chart-line"></i></span>
      <div>
        <h4 class="tch-insight__title">{{ focusLabel || '班级学情' }}</h4>
        <p v-if="!loading && !error" class="tch-insight__sub">
          {{
            empty
              ? '暂无已发布作业'
              : classCompletionPercent != null
                ? `班级完成度约 ${classCompletionPercent}%`
                : '已汇总真实学情数据'
          }}
        </p>
      </div>
    </header>

    <div v-if="loading" class="tch-insight__state">
      <i class="fas fa-spinner fa-spin"></i>
      <span>正在查询学情并由 AI 总结…</span>
    </div>

    <div v-else-if="error" class="tch-insight__state tch-insight__state--error">
      <i class="fas fa-circle-exclamation"></i>
      <p>{{ error }}</p>
      <button type="button" class="tch-insight__retry" @click="$emit('retry')">重试</button>
    </div>

    <template v-else-if="empty">
      <div class="tch-insight__empty">
        <div class="tch-insight__empty-icon"><i class="fas fa-chart-line"></i></div>
        <p class="tch-insight__empty-title">还没有可分析的学情</p>
        <p class="tch-insight__empty-hint">发布作业后，我就能帮你看看谁交了、谁还没交、谁表现好～</p>
      </div>
      <footer class="tch-insight__foot">
        <router-link to="/teacher/tasks" class="tch-insight__link tch-insight__link--ghost">
          去发布任务
        </router-link>
      </footer>
    </template>

    <template v-else>
      <section class="tch-insight__ai">
        <p class="tch-insight__ai-text">{{ summary }}</p>
      </section>

      <section v-if="highlightStudents.length" class="tch-insight__section">
        <h5 class="tch-insight__section-title">
          <span class="tch-insight__dot" aria-hidden="true"></span>
          {{ sectionTitle }}
        </h5>
        <ul class="tch-insight__list">
          <li
            v-for="(item, idx) in visibleItems"
            :key="`${item.studentId}-${item.taskId}-${idx}`"
          >
            <router-link
              v-if="item.taskId && item.studentId"
              :to="studentLink(item)"
              class="tch-insight__card tch-insight__card--link"
            >
              <div class="tch-insight__card-icon">
                <i class="fas fa-user-graduate"></i>
              </div>
              <div class="tch-insight__card-body">
                <strong class="tch-insight__student">{{ item.studentName || item.studentId }}</strong>
                <p class="tch-insight__task">{{ item.taskTitle || '课堂作业' }}</p>
                <p class="tch-insight__meta">
                  {{ item.studentId }}
                  · {{ item.statusLabel || '—' }}
                  <template v-if="item.completionPercent != null"> · 完成 {{ item.completionPercent }}%</template>
                  <template v-if="item.avgScore != null"> · 均分 {{ item.avgScore }}</template>
                </p>
              </div>
              <i class="fas fa-chevron-right tch-insight__card-arrow" aria-hidden="true"></i>
            </router-link>
          </li>
        </ul>
        <button
          v-if="hasMore"
          type="button"
          class="tch-insight__fold"
          @click="expanded = !expanded"
        >
          <i class="fas" :class="expanded ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
          {{
            expanded
              ? '收起'
              : `还有 ${highlightStudents.length - previewLimit} 人，展开查看`
          }}
        </button>
      </section>

      <footer class="tch-insight__foot">
        <router-link :to="progressLink" class="tch-insight__link">
          <i class="fas fa-arrow-right"></i>
          前往学情看板
        </router-link>
      </footer>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { buildTeacherProgressLink } from '@/utils/teacherClassroomNav';

const previewLimit = 3;

const props = defineProps({
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  summary: { type: String, default: '' },
  focus: { type: String, default: 'general' },
  focusLabel: { type: String, default: '班级学情' },
  classCompletionPercent: { type: Number, default: null },
  highlightStudents: { type: Array, default: () => [] },
  latestTaskId: { type: String, default: '' },
  empty: { type: Boolean, default: false },
});

defineEmits(['retry']);

const expanded = ref(false);

const hasMore = computed(() => props.highlightStudents.length > previewLimit);

const visibleItems = computed(() =>
  expanded.value ? props.highlightStudents : props.highlightStudents.slice(0, previewLimit),
);

const sectionTitle = computed(() => {
  if (props.focus === 'unsubmitted') return '待交学生';
  if (props.focus === 'performers') return '表现突出';
  if (props.focus === 'underperformers') return '待提升学生';
  if (props.focus === 'follow_up') return '需跟进学生';
  return '重点关注';
});

const progressLink = computed(() =>
  buildTeacherProgressLink({
    taskId: props.latestTaskId,
    filter: props.focus === 'unsubmitted' ? 'remind' : undefined,
    view: props.focus === 'unsubmitted' ? 'matrix' : 'lane',
  }),
);

function studentLink(item) {
  return buildTeacherProgressLink({
    taskId: item.taskId,
    studentId: item.studentId,
    filter: props.focus === 'unsubmitted' ? 'remind' : undefined,
    view: 'lane',
  });
}
</script>

<style scoped>
.tch-insight {
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(165deg, #ecfdf5 0%, #fff 42%);
  border: 1px solid rgba(13, 148, 136, 0.18);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.07);
}

.tch-insight__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 12px;
}

.tch-insight__icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0d9488, #059669);
  color: #fff;
  font-size: 17px;
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.28);
}

.tch-insight__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.tch-insight__sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.tch-insight__state {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px 20px;
  font-size: 14px;
  color: #64748b;
}

.tch-insight__state--error {
  color: #b91c1c;
  flex-wrap: wrap;
}

.tch-insight__retry {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #fecaca;
  background: #fff;
  color: #b91c1c;
  cursor: pointer;
  font-size: 13px;
}

.tch-insight__empty {
  padding: 8px 18px 4px;
  text-align: center;
}

.tch-insight__empty-icon {
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

.tch-insight__empty-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.tch-insight__empty-hint {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #64748b;
}

.tch-insight__ai {
  margin: 0 14px 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(13, 148, 136, 0.12);
}

.tch-insight__ai-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #334155;
}

.tch-insight__section {
  padding: 0 14px 12px;
}

.tch-insight__section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 4px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.tch-insight__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #0d9488;
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.2);
}

.tch-insight__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tch-insight__fold {
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
  color: #0f766e;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.tch-insight__fold:hover {
  background: rgba(13, 148, 136, 0.14);
}

.tch-insight__card {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(13, 148, 136, 0.14);
}

.tch-insight__card--link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.15s ease;
}

.tch-insight__card--link:hover {
  border-color: rgba(13, 148, 136, 0.35);
  box-shadow: 0 6px 18px rgba(13, 148, 136, 0.12);
  transform: translateY(-1px);
}

.tch-insight__card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: rgba(13, 148, 136, 0.12);
  color: #0d9488;
}

.tch-insight__card-body {
  flex: 1;
  min-width: 0;
}

.tch-insight__student {
  display: block;
  font-size: 14px;
  color: #0f172a;
}

.tch-insight__task {
  margin: 4px 0 0;
  font-size: 13px;
  color: #475569;
}

.tch-insight__meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.tch-insight__card-arrow {
  flex-shrink: 0;
  align-self: center;
  font-size: 12px;
  color: #94a3b8;
  margin-left: auto;
}

.tch-insight__card--link:hover .tch-insight__card-arrow {
  color: #0d9488;
}

.tch-insight__foot {
  display: flex;
  gap: 10px;
  padding: 4px 14px 16px;
}

.tch-insight__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0d9488, #059669);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.22);
}

.tch-insight__link--ghost {
  background: #fff;
  color: #0f766e;
  border: 1px solid rgba(13, 148, 136, 0.25);
  box-shadow: none;
}
</style>
