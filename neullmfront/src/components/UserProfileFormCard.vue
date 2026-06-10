<template>
  <div class="cf-root">
    <div class="cf-inner">
      <div class="cf-head">
        <div class="cf-icon">
          <i class="fas fa-user-circle"></i>
        </div>
        <div>
          <h3 class="cf-title">保存我的资料</h3>
          <p class="cf-sub">
            <template v-if="hadPrefill">已按你刚才说的填好了，核对一下就能保存。</template>
            <template v-else>填写常用联系方式，方便后续提醒与个性化服务。</template>
          </p>
        </div>
      </div>

      <div class="cf-grid">
        <label class="cf-field">
          <span class="cf-label">显示名</span>
          <input v-model.trim="displayName" type="text" placeholder="昵称" maxlength="100" />
        </label>
        <label class="cf-field">
          <span class="cf-label">真实姓名</span>
          <input v-model.trim="realName" type="text" placeholder="姓名" maxlength="100" />
        </label>
        <label class="cf-field">
          <span class="cf-label">邮箱</span>
          <input v-model.trim="email" type="email" placeholder="常用邮箱" maxlength="128" />
        </label>
        <label class="cf-field">
          <span class="cf-label">手机</span>
          <input v-model.trim="phone" type="tel" placeholder="手机号" maxlength="32" />
        </label>
      </div>

      <button type="button" class="cf-more-toggle" @click="showMore = !showMore">
        <i class="fas" :class="showMore ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
        {{ showMore ? '收起更多选项' : '更多选项（可选）' }}
      </button>

      <div v-show="showMore" class="cf-grid cf-grid-more">
        <label class="cf-field cf-field-full">
          <span class="cf-label">外部 ID</span>
          <input v-model.trim="externalId" type="text" placeholder="登录账号等唯一标识" maxlength="64" />
        </label>
        <label class="cf-field cf-field-full">
          <span class="cf-label">地址</span>
          <input v-model.trim="address" type="text" placeholder="联系地址" maxlength="255" />
        </label>
        <label class="cf-field">
          <span class="cf-label">性别</span>
          <select v-model="gender">
            <option value="">不填</option>
            <option value="0">未知</option>
            <option value="1">男</option>
            <option value="2">女</option>
          </select>
        </label>
        <label class="cf-field">
          <span class="cf-label">头像 URL</span>
          <input v-model.trim="avatarUrl" type="url" placeholder="https://…" maxlength="512" />
        </label>
        <label class="cf-field cf-field-full">
          <span class="cf-label">备注</span>
          <textarea v-model.trim="remark" rows="2" maxlength="500" placeholder="其它说明" />
        </label>
      </div>

      <div v-if="localError" class="cf-local-err">{{ localError }}</div>

      <div class="cf-actions">
        <button type="button" class="cf-btn cf-btn-ghost" :disabled="submitting" @click="$emit('cancel')">
          先不用
        </button>
        <button type="button" class="cf-btn cf-btn-primary" :disabled="!canSubmit || submitting" @click="doSubmit">
          <i v-if="submitting" class="fas fa-spinner fa-spin"></i>
          {{ submitting ? '保存中…' : '保存资料' }}
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

const externalId = ref('');
const displayName = ref('');
const realName = ref('');
const email = ref('');
const phone = ref('');
const address = ref('');
const gender = ref('');
const avatarUrl = ref('');
const remark = ref('');
const submitting = ref(false);
const localError = ref('');
const showMore = ref(false);

const canSubmit = computed(() => {
  const bits = [
    externalId.value,
    displayName.value,
    realName.value,
    email.value,
    phone.value
  ].map((s) => (s || '').trim());
  return bits.some((s) => s.length > 0);
});

const hadPrefill = computed(() => {
  const p = props.prefill;
  if (!p || typeof p !== 'object') return false;
  return !!(p.externalId || p.displayName || p.realName || p.email || p.phone || p.address);
});

function applyPrefill(p) {
  if (!p || typeof p !== 'object') return;
  if (p.externalId) externalId.value = String(p.externalId).trim();
  if (p.displayName) displayName.value = String(p.displayName).trim();
  if (p.realName) realName.value = String(p.realName).trim();
  if (p.email) email.value = String(p.email).trim();
  if (p.phone) phone.value = String(p.phone).trim();
  if (p.address) address.value = String(p.address).trim();
  if (p.gender !== undefined && p.gender !== null && String(p.gender).trim() !== '') {
    const g = String(p.gender).trim();
    if (g === '0' || g === '1' || g === '2' || g === '男' || g === '女' || g === '未知') {
      if (g === '男') gender.value = '1';
      else if (g === '女') gender.value = '2';
      else if (g === '未知') gender.value = '0';
      else gender.value = g;
    }
  }
  if (p.avatarUrl) avatarUrl.value = String(p.avatarUrl).trim();
  if (p.remark) remark.value = String(p.remark).trim();
  if (p.externalId || p.address || p.gender || p.avatarUrl || p.remark) {
    showMore.value = true;
  }
}

onMounted(() => {
  applyPrefill(props.prefill);
});

function doSubmit() {
  if (!canSubmit.value || submitting.value) return;
  localError.value = '';
  const em = email.value.trim();
  if (em && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(em)) {
    localError.value = '邮箱格式好像不太对，再检查一下？';
    return;
  }
  submitting.value = true;
  const payload = {
    externalId: externalId.value.trim() || null,
    displayName: displayName.value.trim() || null,
    realName: realName.value.trim() || null,
    email: email.value.trim() || null,
    phone: phone.value.trim() || null,
    address: address.value.trim() || null,
    avatarUrl: avatarUrl.value.trim() || null,
    remark: remark.value.trim() || null
  };
  if (gender.value !== '') {
    payload.gender = parseInt(gender.value, 10);
  }
  emit('submit', payload);
}
</script>
