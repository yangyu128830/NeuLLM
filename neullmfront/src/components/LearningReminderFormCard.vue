<template>
  <div class="cf-root">
    <div class="cf-inner">
      <div class="cf-head">
        <div class="cf-icon">
          <i class="fas fa-bell"></i>
        </div>
        <div>
          <h3 class="cf-title">设置学习提醒</h3>
          <p class="cf-sub">
            <template v-if="hadPrefill">已按你的话填好了，核对后保存即可。</template>
            <template v-else>填写时间与事项，到点会邮件通知你。</template>
          </p>
        </div>
      </div>

      <div class="cf-grid">
        <label class="cf-field cf-field-full">
          <span class="cf-label">提醒标题</span>
          <input v-model.trim="title" type="text" placeholder="例如：背英语单词" maxlength="80" />
        </label>

        <label class="cf-field cf-field-full">
          <span class="cf-label">日期与时间</span>
          <input v-model="datetime" type="datetime-local" />
        </label>

        <label class="cf-field">
          <span class="cf-label">提前通知</span>
          <select v-model="advanceNotice">
            <option value="0">准时</option>
            <option value="5">提前 5 分钟</option>
            <option value="15">提前 15 分钟</option>
            <option value="30">提前 30 分钟</option>
            <option value="60">提前 1 小时</option>
            <option value="1440">提前 1 天</option>
          </select>
        </label>

        <label class="cf-field">
          <span class="cf-label">通知邮箱</span>
          <input
            v-model.trim="email"
            type="email"
            required
            autocomplete="email"
            placeholder="接收提醒的邮箱"
            maxlength="120"
          />
        </label>

        <label class="cf-field cf-field-full cf-check-row">
          <input v-model="repeatDaily" type="checkbox" />
          <span>每天同一时刻重复提醒</span>
        </label>
      </div>

      <button type="button" class="cf-more-toggle" @click="showMore = !showMore">
        <i class="fas" :class="showMore ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
        {{ showMore ? '收起更多选项' : '更多选项（可选）' }}
      </button>

      <div v-show="showMore" class="cf-grid cf-grid-more">
        <label class="cf-field">
          <span class="cf-label">地点</span>
          <input v-model.trim="location" type="text" placeholder="图书馆 / 寝室…" maxlength="60" />
        </label>
        <label class="cf-field cf-field-full">
          <span class="cf-label">备注</span>
          <textarea v-model.trim="notes" rows="2" maxlength="240" placeholder="要带的书、链接…" />
        </label>
      </div>

      <div class="cf-actions">
        <button type="button" class="cf-btn cf-btn-ghost" @click="$emit('cancel')">先不用</button>
        <button type="button" class="cf-btn cf-btn-primary" :disabled="!canSubmit" @click="submit">
          保存提醒
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import '../assets/chat-form-card.css';

const props = defineProps({
  prefill: { type: Object, default: null }
});

const emit = defineEmits(['submit', 'cancel']);

const title = ref('');
const datetime = ref('');
const location = ref('');
const notes = ref('');
const email = ref('');
const advanceNotice = ref('30');
const repeatDaily = ref(false);
const showMore = ref(false);

const emailLooksValid = computed(() => {
  const e = email.value.trim();
  if (!e) return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e);
});

const canSubmit = computed(
  () => title.value.length > 0 && datetime.value.length > 0 && emailLooksValid.value
);

const hadPrefill = computed(() => {
  const p = props.prefill;
  if (!p || typeof p !== 'object') return false;
  return !!(
    p.title ||
    p.datetimeLocal ||
    (p.advanceNotice != null && p.advanceNotice !== '') ||
    p.location ||
    p.notes ||
    p.email ||
    p.repeatDaily
  );
});

function defaultDatetimeLocal() {
  const d = new Date();
  d.setMinutes(0, 0, 0);
  d.setHours(d.getHours() + 1);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function applyPrefill(p) {
  if (!p || typeof p !== 'object') return;
  if (p.title) title.value = String(p.title).trim();
  if (p.datetimeLocal) datetime.value = String(p.datetimeLocal).trim();
  if (p.advanceNotice != null && p.advanceNotice !== '')
    advanceNotice.value = String(p.advanceNotice);
  if (p.location) location.value = String(p.location).trim();
  if (p.notes) notes.value = String(p.notes).trim();
  if (p.email) email.value = String(p.email).trim();
  if (p.repeatDaily) repeatDaily.value = true;
  if (p.location || p.notes) showMore.value = true;
}

onMounted(() => {
  applyPrefill(props.prefill);
  if (!datetime.value) datetime.value = defaultDatetimeLocal();
});

function submit() {
  if (!canSubmit.value) return;
  emit('submit', {
    title: title.value,
    datetime: datetime.value,
    location: location.value,
    notes: notes.value,
    email: email.value.trim(),
    advanceNotice: advanceNotice.value,
    repeatDaily: repeatDaily.value
  });
}
</script>
