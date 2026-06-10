<template>
  <article
    class="t-grade-item"
    :class="gradeItemClass(sub.status)"
  >
    <header class="t-grade-item__head">
      <div class="t-grade-item__student">
        <span :class="['t-avatar', `t-avatar--${idx % 5}`]">
          {{ studentInitial(sub.studentName) }}
        </span>
        <div class="t-grade-item__identity">
          <span class="t-grade-item__name">{{ sub.studentName }}</span>
          <span class="t-grade-item__sub">
            {{ sub.studentNo }} · {{ subTaskTitle(sub.subTaskId) || sub.subTaskId }}
          </span>
        </div>
      </div>

      <div class="t-grade-item__badges">
        <span class="t-grade-status" :class="statusBadgeClass(sub.status)">
          {{ submissionStatusLabel(sub.status) }}
        </span>
        <span v-if="sub.score != null" class="t-grade-score-badge">{{ sub.score }} 分</span>
      </div>
    </header>

    <div class="t-grade-item__meta">
      <span class="t-grade-chip">
        <i class="fas fa-clock"></i>{{ formatSubmittedAt(sub.submittedAt) }}
      </span>
      <span v-if="sub.fileName" class="t-grade-chip t-grade-chip--file">
        <i class="fas fa-file-alt"></i>{{ sub.fileName }}
      </span>
    </div>

    <div class="t-grade-item__layout">
      <div class="t-grade-item__main">
        <div
          class="t-grade-ai"
          :class="{ 't-grade-ai--empty': !aiInsights[sub.submissionId] && !aiLoading[sub.submissionId] }"
        >
          <div class="t-grade-ai__head">
            <span class="t-grade-ai__badge">
              <i class="fas fa-magic"></i> AI 审阅助手
            </span>
            <button
              v-if="!aiInsights[sub.submissionId]"
              type="button"
              class="t-grade-ai__cta"
              :disabled="aiLoading[sub.submissionId]"
              @click="fetchGradingAssist(sub.submissionId)"
            >
              <i :class="aiLoading[sub.submissionId] ? 'fas fa-circle-notch fa-spin' : 'fas fa-wand-magic-sparkles'"></i>
              {{ aiLoading[sub.submissionId] ? '分析中…' : '生成建议' }}
            </button>
            <button
              v-else
              type="button"
              class="t-grade-ai__retry"
              :disabled="aiLoading[sub.submissionId]"
              @click="fetchGradingAssist(sub.submissionId, { force: true })"
            >
              <i class="fas fa-redo-alt"></i> 重新分析
            </button>
          </div>

          <div v-if="aiLoading[sub.submissionId]" class="t-grade-ai__loading">
            <div class="t-grade-ai__loading-bar"></div>
            <p>正在阅读提交并生成批改建议…</p>
          </div>

          <template v-else-if="aiInsights[sub.submissionId]">
            <p class="t-grade-ai__summary">{{ aiInsights[sub.submissionId].summary }}</p>
            <div class="t-grade-ai__meta">
              <span
                v-if="aiInsights[sub.submissionId].suggestedScore != null"
                class="t-grade-ai__score"
              >
                建议 {{ aiInsights[sub.submissionId].suggestedScore }} 分
              </span>
              <span class="t-pill" :class="recommendPillClass(aiInsights[sub.submissionId].recommendation)">
                {{ recommendLabel(aiInsights[sub.submissionId].recommendation) }}
              </span>
              <span class="t-pill t-pill--muted">
                {{ completionLabel(aiInsights[sub.submissionId].completionLevel) }}
              </span>
            </div>
            <p v-if="aiInsights[sub.submissionId].suggestedComment" class="t-grade-ai__comment">
              「{{ aiInsights[sub.submissionId].suggestedComment }}」
            </p>
            <ul v-if="aiInsights[sub.submissionId].strengths?.length" class="t-grade-ai__list t-grade-ai__list--ok">
              <li v-for="(s, i) in aiInsights[sub.submissionId].strengths" :key="'s' + i">{{ s }}</li>
            </ul>
            <ul v-if="aiInsights[sub.submissionId].issues?.length" class="t-grade-ai__list t-grade-ai__list--warn">
              <li v-for="(s, i) in aiInsights[sub.submissionId].issues" :key="'i' + i">{{ s }}</li>
            </ul>
            <p v-if="aiInsights[sub.submissionId].teacherTip" class="t-grade-ai__tip">
              {{ aiInsights[sub.submissionId].teacherTip }}
            </p>
            <button
              v-if="sub.status === 'SUBMITTED' || sub.status === 'GRADED'"
              type="button"
              class="t-grade-ai__apply"
              @click="applyAiSuggestion(sub.submissionId)"
            >
              <i class="fas fa-check"></i> 采纳建议到评分
            </button>
          </template>

          <div v-else class="t-grade-ai__empty">
            <i class="fas fa-robot"></i>
            <p>无需通读全文，AI 可快速提炼要点并给出分数与评语建议</p>
          </div>
        </div>

        <details v-if="sub.content" class="t-grade-preview">
          <summary>
            <i class="fas fa-align-left"></i>
            查看原文摘要
            <span>{{ (sub.content || '').length }} 字</span>
          </summary>
          <div class="t-grade-preview__body">
            {{ (sub.content || '').slice(0, 1200) }}{{ (sub.content || '').length > 1200 ? '…' : '' }}
          </div>
        </details>

        <p v-if="sub.teacherComment" class="t-grade-item__comment">
          <i class="fas fa-quote-left"></i>{{ sub.teacherComment }}
        </p>
      </div>

      <aside
        v-if="sub.status === 'SUBMITTED' || sub.status === 'GRADED'"
        class="t-grade-form"
      >
        <div class="t-grade-form__head">
          <i class="fas fa-pen-fancy"></i>
          <span>教师评分</span>
        </div>
        <label class="t-grade-form__field">
          <span>分数</span>
          <input
            v-model="gradeForms[sub.submissionId].score"
            type="number"
            min="0"
            max="100"
            placeholder="0 – 100"
          />
        </label>
        <label class="t-grade-form__field">
          <span>评语</span>
          <textarea
            v-model="gradeForms[sub.submissionId].comment"
            rows="3"
            placeholder="填写评语或修改建议…"
          ></textarea>
        </label>
        <div class="t-grade-form__actions">
          <button type="button" class="t-btn-solid" @click="handleGrade">
            <i class="fas fa-check"></i> 保存评分
          </button>
          <button type="button" class="t-btn-ghost t-btn-ghost--warn" @click="handleReject">
            <i class="fas fa-undo"></i> 打回重交
          </button>
        </div>
      </aside>
    </div>
  </article>
</template>

<script setup>
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const props = defineProps({
  sub: { type: Object, required: true },
  idx: { type: Number, default: 0 },
});

const emit = defineEmits(['graded', 'rejected']);

const {
  gradeForms,
  studentInitial,
  subTaskTitle,
  formatSubmittedAt,
  submissionStatusLabel,
  grade,
  reject,
  aiInsights,
  aiLoading,
  fetchGradingAssist,
  applyAiSuggestion,
} = useTeacherClassroom();

function gradeItemClass(status) {
  if (status === 'SUBMITTED') return 't-grade-item--pending';
  if (status === 'GRADED') return 't-grade-item--done';
  if (status === 'REJECTED') return 't-grade-item--rejected';
  return '';
}

function statusBadgeClass(status) {
  if (status === 'SUBMITTED') return 't-grade-status--pending';
  if (status === 'GRADED') return 't-grade-status--done';
  return 't-grade-status--muted';
}

function recommendLabel(rec) {
  const map = { approve: '建议通过', revise: '建议打回', needs_review: '需细读' };
  return map[rec] || '待确认';
}

function recommendPillClass(rec) {
  if (rec === 'approve') return 't-pill--success';
  if (rec === 'revise') return 't-pill--warn';
  return 't-pill--brand';
}

function completionLabel(level) {
  const map = { complete: '完成度良好', partial: '部分完成', insufficient: '完成不足' };
  return map[level] || '完成度待判';
}

async function handleGrade() {
  await grade(props.sub);
  emit('graded', props.sub);
}

async function handleReject() {
  await reject(props.sub);
  emit('rejected', props.sub);
}
</script>
