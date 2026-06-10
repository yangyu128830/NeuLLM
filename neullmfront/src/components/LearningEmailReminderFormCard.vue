<template>
  <div class="cf-root">
    <div class="cf-inner">
      <div class="cf-head">
        <div class="cf-icon em">
          <i class="fas fa-envelope"></i>
        </div>
        <div>
          <h3 class="cf-title">发送提醒邮件</h3>
          <p class="cf-sub">
            <template v-if="hadPrefill">已按你的话填好了，改完就能发送。</template>
            <template v-else>填写收件人、主题和正文即可发送。</template>
          </p>
        </div>
      </div>

      <div class="cf-grid-stack">
        <label class="cf-field">
          <span class="cf-label">收件人邮箱</span>
          <input
            v-model.trim="recipient"
            type="email"
            required
            autocomplete="email"
            placeholder="例如：zhangsan@qq.com"
            maxlength="120"
          />
        </label>

        <label class="cf-field">
          <span class="cf-label">主题</span>
          <input v-model.trim="subject" type="text" placeholder="例如：今晚复习提醒" maxlength="120" />
        </label>

        <label class="cf-field">
          <span class="cf-label">正文</span>
          <textarea v-model.trim="body" rows="3" maxlength="4000" placeholder="想传达的内容…" />
        </label>
      </div>

      <div v-if="localError" class="cf-local-err">{{ localError }}</div>

      <div class="cf-actions">
        <button type="button" class="cf-btn cf-btn-ghost" :disabled="submitting" @click="$emit('cancel')">
          先不用
        </button>
        <button type="button" class="cf-btn cf-btn-primary" :disabled="!canSubmit || submitting" @click="doSubmit">
          <i v-if="submitting" class="fas fa-spinner fa-spin"></i>
          {{ submitting ? '发送中…' : '发送邮件' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import '../assets/chat-form-card.css';

const props = defineProps({
  prefill: { type: Object, default: null }
});

const emit = defineEmits(['submit', 'cancel']);

const recipient = ref('');
const subject = ref('');
const body = ref('');
const submitting = ref(false);
const localError = ref('');

const canSubmit = computed(
  () => recipient.value.length > 0 && subject.value.length > 0 && body.value.length > 0
);

const hadPrefill = computed(() => {
  const p = props.prefill;
  if (!p || typeof p !== 'object') return false;
  return !!(p.recipient || p.subject || p.content);
});

function applyPrefill(p) {
  if (!p || typeof p !== 'object') return;
  if (p.recipient) recipient.value = String(p.recipient).trim();
  if (p.subject) subject.value = String(p.subject).trim();
  if (p.content) body.value = String(p.content).trim();
}

onMounted(() => {
  applyPrefill(props.prefill);
});

watch(
  () => props.prefill,
  (p) => applyPrefill(p),
  { deep: true }
);

function doSubmit() {
  if (!canSubmit.value || submitting.value) return;
  localError.value = '';
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!re.test(recipient.value)) {
    localError.value = '收件人邮箱格式好像不太对，再检查一下？';
    return;
  }
  submitting.value = true;
  emit('submit', {
    recipient: recipient.value,
    subject: subject.value,
    content: body.value
  });
}
</script>
