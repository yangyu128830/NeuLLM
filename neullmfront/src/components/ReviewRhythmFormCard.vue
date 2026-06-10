<template>
  <div class="rr-form-root">
    <div class="rr-form-glow" aria-hidden="true" />
    <div class="rr-form-inner">
      <div class="rr-form-head">
        <div class="rr-form-icon">
          <i class="fas fa-calendar-check"></i>
        </div>
        <div>
          <h3 class="rr-form-title">{{ titleText }}</h3>
          <p class="rr-form-sub">{{ subText }}</p>
        </div>
      </div>

      <div class="rr-form-grid">
        <label class="rr-field">
          <span class="rr-label"><i class="fas fa-book"></i> 科目 / 主题</span>
          <input v-model.trim="subject" type="text" placeholder="例如：数据结构、操作系统、英语六级…" maxlength="120" />
        </label>

        <label class="rr-field">
          <span class="rr-label"><i class="fas fa-hourglass-half"></i> 备考天数</span>
          <input v-model.number="days" type="number" min="1" max="90" placeholder="7" />
        </label>

        <label class="rr-field">
          <span class="rr-label"><i class="fas fa-clock"></i> 每天可投入</span>
          <select v-model="dailyHours">
            <option value="1">约 1 小时</option>
            <option value="1.5">约 1.5 小时</option>
            <option value="2">约 2 小时</option>
            <option value="3">约 3 小时</option>
            <option value="4">4 小时及以上</option>
          </select>
        </label>

        <label class="rr-field">
          <span class="rr-label"><i class="fas fa-layer-group"></i> 当前基础</span>
          <select v-model="level">
            <option value="零基础 / 刚开始">零基础 / 刚开始</option>
            <option value="有基础，想巩固">有基础，想巩固</option>
            <option value="考前冲刺">考前冲刺</option>
          </select>
        </label>

        <label class="rr-field rr-field-full">
          <span class="rr-label"><i class="fas fa-bullseye"></i> 侧重点（可选）</span>
          <textarea
            v-model.trim="focusNote"
            rows="2"
            maxlength="300"
            placeholder="例如：优先图与树、大题为主；或希望早晚分段…"
          />
        </label>
      </div>

      <div class="rr-form-actions">
        <button type="button" class="rr-btn rr-btn-ghost" @click="$emit('cancel')">
          <i class="fas fa-times"></i> 先不用了
        </button>
        <button type="button" class="rr-btn rr-btn-primary" :disabled="!canSubmit" @click="submit">
          <i class="fas fa-magic"></i> {{ submitLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  /** rhythm：排复习节奏；plan：只说「制定复习计划」等模糊句时先填表再生成 */
  mode: {
    type: String,
    default: 'rhythm',
    validator: (v) => v === 'rhythm' || v === 'plan'
  }
});

const emit = defineEmits(['submit', 'cancel']);

const titleText = computed(() =>
  props.mode === 'plan' ? '先补齐复习计划的关键信息' : '定制你的复习节奏'
);

const subText = computed(() =>
  props.mode === 'plan'
    ? '科目、备考天数和每天能学多久先定下来，我再按表帮你生成一整份可执行的复习计划～'
    : '填几项关键信息，我会按你的节奏生成一份清晰可执行的周计划～'
);

const submitLabel = computed(() =>
  props.mode === 'plan' ? '生成复习计划' : '生成复习节奏'
);

const subject = ref('');
const days = ref(7);
const dailyHours = ref('2');
const level = ref('有基础，想巩固');
const focusNote = ref('');

const canSubmit = computed(() => subject.value.length > 0 && days.value >= 1 && days.value <= 90);

function submit() {
  if (!canSubmit.value) return;
  emit('submit', {
    subject: subject.value,
    days: Math.min(90, Math.max(1, Math.floor(Number(days.value)) || 7)),
    dailyHours: dailyHours.value,
    level: level.value,
    focusNote: focusNote.value || ''
  });
}
</script>

<style scoped>
.rr-form-root {
  position: relative;
  border-radius: 20px;
  padding: 2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #06b6d4);
  box-shadow:
    0 18px 50px rgba(99, 102, 241, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.12) inset;
  max-width: 560px;
}

.rr-form-glow {
  position: absolute;
  inset: -30%;
  background: radial-gradient(circle at 30% 20%, rgba(99, 102, 241, 0.25), transparent 45%),
    radial-gradient(circle at 80% 80%, rgba(6, 182, 212, 0.2), transparent 40%);
  pointer-events: none;
  z-index: 0;
}

.rr-form-inner {
  position: relative;
  z-index: 1;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  padding: 22px 22px 18px;
}

.rr-form-head {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.rr-form-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.35);
}

.rr-form-title {
  margin: 0 0 6px;
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.02em;
}

.rr-form-sub {
  margin: 0;
  font-size: 0.88rem;
  color: #64748b;
  line-height: 1.55;
}

.rr-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
}

@media (max-width: 520px) {
  .rr-form-grid {
    grid-template-columns: 1fr;
  }
}

.rr-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rr-field-full {
  grid-column: 1 / -1;
}

.rr-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rr-label i {
  color: #6366f1;
  font-size: 0.75rem;
}

.rr-field input,
.rr-field select,
.rr-field textarea {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 0.92rem;
  color: #1e293b;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
  width: 100%;
}

.rr-field input:focus,
.rr-field select:focus,
.rr-field textarea:focus {
  outline: none;
  border-color: #818cf8;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.25);
}

.rr-field textarea {
  resize: vertical;
  min-height: 64px;
  line-height: 1.5;
}

.rr-form-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.rr-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: transform 0.15s, box-shadow 0.2s, opacity 0.2s;
}

.rr-btn-primary {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  box-shadow: 0 6px 18px rgba(79, 70, 229, 0.4);
}

.rr-btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.45);
}

.rr-btn-primary:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.rr-btn-ghost {
  background: #f1f5f9;
  color: #64748b;
}

.rr-btn-ghost:hover {
  background: #e2e8f0;
  color: #334155;
}
</style>
