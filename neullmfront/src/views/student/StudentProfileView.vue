<template>
  <div class="student-page classroom-theme profile-page">
    <StudentPageHeader
      title="个人中心"
      subtitle="绑定学号、班级与联系方式，便于作业通知与邮件提醒"
      icon="fa-user-circle"
      active="profile"
      :unread-count="unreadCount"
      @logout="logout"
    />

    <main class="content page-inner">
      <div v-if="loading" class="state-box">
        <i class="fas fa-spinner fa-spin"></i>
        <p>加载中…</p>
      </div>

      <template v-else>
        <div v-if="!profile.profileComplete" class="profile-alert">
          <i class="fas fa-circle-info"></i>
          <div>
            <strong>请完善学籍信息</strong>
            <p>学号、专业、年级、班级等信息会用于作业匹配与教师查看，请如实填写。</p>
          </div>
        </div>

        <section class="profile-card">
          <header class="profile-card-head">
            <div class="profile-avatar">{{ profileInitial }}</div>
            <div>
              <h2>{{ profile.displayName || '未命名' }}</h2>
              <p class="profile-meta">
                <span v-if="profile.studentNo">学号 {{ profile.studentNo }}</span>
                <span v-if="profile.studentNo && profile.className"> · </span>
                <span v-if="profile.className">{{ profile.className }}</span>
              </p>
            </div>
            <div class="profile-head-actions">
              <button v-if="!isEditing" type="button" class="btn-primary" @click="startEdit">
                <i class="fas fa-pen"></i> 编辑资料
              </button>
              <template v-else>
                <button type="button" class="btn-ghost-card" :disabled="saving" @click="cancelEdit">取消</button>
                <button type="button" class="btn-primary" :disabled="saving" @click="save">
                  <i v-if="saving" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-check"></i>
                  {{ saving ? '保存中…' : '保存' }}
                </button>
              </template>
            </div>
          </header>

          <form class="profile-form" @submit.prevent="save">
            <div class="form-section">
              <h3><i class="fas fa-id-card"></i> 学籍信息</h3>
              <div class="form-grid">
                <label class="field">
                  <span>姓名 <em>*</em></span>
                  <input v-model="form.displayName" type="text" required maxlength="32" :readonly="!isEditing" />
                </label>
                <label class="field">
                  <span>学号 <em>*</em></span>
                  <input v-model="form.studentNo" type="text" required maxlength="32" :readonly="!isEditing" />
                </label>
                <label class="field">
                  <span>专业 <em>*</em></span>
                  <input
                    v-model="form.major"
                    type="text"
                    required
                    maxlength="64"
                    placeholder="如：软件工程"
                    :readonly="!isEditing"
                    list="major-suggestions"
                  />
                  <datalist id="major-suggestions">
                    <option v-for="m in majorSuggestions" :key="m" :value="m" />
                  </datalist>
                </label>
                <label class="field">
                  <span>年级 <em>*</em></span>
                  <input
                    v-if="isEditing"
                    v-model="form.grade"
                    type="text"
                    required
                    maxlength="16"
                    placeholder="如：2024级"
                    list="grade-suggestions"
                  />
                  <input v-else :value="form.grade" type="text" readonly />
                  <datalist id="grade-suggestions">
                    <option v-for="g in gradeSuggestions" :key="g" :value="g" />
                  </datalist>
                </label>
                <label class="field field-wide">
                  <span>班级 <em>*</em></span>
                  <input
                    v-model="form.className"
                    type="text"
                    required
                    maxlength="64"
                    placeholder="如：软工2401班"
                    :readonly="!isEditing"
                  />
                </label>
              </div>
            </div>

            <div class="form-section">
              <h3><i class="fas fa-address-book"></i> 联系方式</h3>
              <div class="form-grid">
                <label class="field">
                  <span>邮箱</span>
                  <input
                    v-model="form.email"
                    type="email"
                    maxlength="128"
                    placeholder="接收作业与提醒通知"
                    :readonly="!isEditing"
                  />
                </label>
                <label class="field">
                  <span>手机号</span>
                  <input
                    v-model="form.phone"
                    type="tel"
                    maxlength="32"
                    placeholder="11 位手机号"
                    :readonly="!isEditing"
                  />
                </label>
              </div>
              <p class="field-hint">邮箱将用于学习提醒、作业催交等通知；请填写常用且有效的地址。</p>
            </div>

            <div class="form-section">
              <h3><i class="fas fa-key"></i> 账号安全</h3>
              <div class="form-grid">
                <label class="field">
                  <span>登录用户名 <em>*</em></span>
                  <input v-model="form.username" type="text" required maxlength="32" :readonly="!isEditing" />
                </label>
                <label v-if="isEditing" class="field">
                  <span>新密码</span>
                  <input
                    v-model="form.password"
                    type="password"
                    minlength="6"
                    maxlength="64"
                    placeholder="留空表示不修改"
                    autocomplete="new-password"
                  />
                </label>
                <label v-else class="field">
                  <span>密码</span>
                  <input value="••••••••" type="text" readonly />
                </label>
              </div>
            </div>
          </form>
        </section>
      </template>
    </main>

    <Transition name="toast-fade">
      <p v-if="toast" :class="['toast', toastOk ? 'ok' : 'fail']">{{ toast }}</p>
    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import StudentPageHeader from '@/components/student/StudentPageHeader.vue';
import authApi from '@/services/authApi';
import notificationsApi from '@/services/notificationsApi';
import { clearAuth, getToken, getUser, setAuth } from '@/stores/auth';

const router = useRouter();
const loading = ref(true);
const saving = ref(false);
const isEditing = ref(false);
const unreadCount = ref(0);
const toast = ref('');
const toastOk = ref(true);
let editSnapshot = null;

const profile = ref({});
const form = reactive({
  displayName: '',
  username: '',
  studentNo: '',
  major: '',
  grade: '',
  className: '',
  email: '',
  phone: '',
  password: ''
});

const majorSuggestions = ['软件工程', '计算机科学与技术', '人工智能', '数据科学', '网络工程'];
const gradeSuggestions = ['2022级', '2023级', '2024级', '2025级', '2026级'];

const profileInitial = computed(() => {
  const n = (profile.value.displayName || profile.value.username || '?').trim();
  return n ? n.charAt(0) : '?';
});

function showToast(message, ok = true) {
  toast.value = message;
  toastOk.value = ok;
  if (message) {
    setTimeout(() => {
      toast.value = '';
    }, 3200);
  }
}

function applyProfile(data) {
  profile.value = data || {};
  form.displayName = data?.displayName || '';
  form.username = data?.username || '';
  form.studentNo = data?.studentNo || '';
  form.major = data?.major || '';
  form.grade = data?.grade || '';
  form.className = data?.className || '';
  form.email = data?.email || '';
  form.phone = data?.phone || '';
  form.password = '';
}

function startEdit() {
  editSnapshot = { ...form, password: '' };
  isEditing.value = true;
}

function cancelEdit() {
  if (editSnapshot) {
    Object.assign(form, editSnapshot);
  }
  form.password = '';
  isEditing.value = false;
  editSnapshot = null;
}

function validateForm() {
  const em = form.email.trim();
  if (em && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(em)) {
    showToast('邮箱格式不正确', false);
    return false;
  }
  const ph = form.phone.trim();
  if (ph && !/^1\d{10}$/.test(ph)) {
    showToast('手机号应为 11 位数字', false);
    return false;
  }
  if (form.password.trim() && form.password.trim().length < 6) {
    showToast('新密码至少 6 位', false);
    return false;
  }
  return true;
}

async function save() {
  if (!isEditing.value || saving.value) return;
  if (!validateForm()) return;

  saving.value = true;
  try {
    const payload = {
      displayName: form.displayName.trim(),
      username: form.username.trim(),
      studentNo: form.studentNo.trim(),
      major: form.major.trim(),
      grade: form.grade.trim(),
      className: form.className.trim(),
      email: form.email.trim() || null,
      phone: form.phone.trim() || null
    };
    if (form.password.trim()) {
      payload.password = form.password.trim();
    }
    const data = await authApi.updateStudentProfile(payload);
    applyProfile(data);
    isEditing.value = false;
    editSnapshot = null;

    const current = getUser();
    if (current) {
      setAuth(getToken(), {
        ...current,
        username: form.username.trim(),
        displayName: form.displayName.trim(),
        studentNo: form.studentNo.trim()
      });
    }
    showToast('个人信息已保存');
  } catch (e) {
    showToast(e?.message || '保存失败', false);
  } finally {
    saving.value = false;
  }
}

async function load() {
  loading.value = true;
  try {
    const [data, countRes] = await Promise.all([
      authApi.getStudentProfile(),
      notificationsApi.unreadCount().catch(() => ({ count: 0 }))
    ]);
    applyProfile(data);
    unreadCount.value = countRes?.count ?? 0;
    if (!data?.profileComplete) {
      isEditing.value = true;
    }
  } catch (e) {
    showToast(e?.message || '加载失败', false);
  } finally {
    loading.value = false;
  }
}

async function logout() {
  try {
    await authApi.logout();
  } finally {
    clearAuth();
    router.push('/login');
  }
}

onMounted(load);
</script>

<style scoped>
@import '../../assets/classroom-theme.css';

.student-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--page-bg);
  color: var(--text);
  font-family: var(--font);
}

.content {
  flex: 1;
  padding-top: 20px;
  padding-bottom: 24px;
}

.state-box {
  text-align: center;
  padding: 48px 16px;
  color: #64748b;
}

.state-box i {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: var(--brand, #0d9488);
}

.profile-alert {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 16px;
  margin-bottom: 20px;
  border-radius: 12px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #92400e;
}

.profile-alert i {
  margin-top: 2px;
}

.profile-alert p {
  margin: 4px 0 0;
  font-size: 0.88rem;
  opacity: 0.9;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.profile-card-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #f0fdfa 0%, #ecfeff 100%);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.profile-avatar {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: var(--gradient-brand, linear-gradient(135deg, #0f766e, #14b8a6));
  color: #fff;
  font-size: 1.4rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-card-head h2 {
  margin: 0;
  font-size: 1.25rem;
}

.profile-meta {
  margin: 4px 0 0;
  font-size: 0.88rem;
  color: #64748b;
}

.profile-head-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.btn-primary,
.btn-ghost-card {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: var(--brand, #0d9488);
  color: #fff;
}

.btn-primary:disabled {
  opacity: 0.65;
}

.btn-ghost-card {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

.profile-form {
  padding: 8px 24px 24px;
}

.form-section {
  padding: 20px 0;
  border-bottom: 1px solid #f1f5f9;
}

.form-section:last-child {
  border-bottom: none;
}

.form-section h3 {
  margin: 0 0 16px;
  font-size: 0.95rem;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-section h3 i {
  color: var(--brand, #0d9488);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-wide {
  grid-column: 1 / -1;
}

.field span {
  font-size: 0.82rem;
  font-weight: 600;
  color: #475569;
}

.field span em {
  color: #ef4444;
  font-style: normal;
}

.field input {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.92rem;
  background: #fff;
  color: #0f172a;
}

.field input:read-only {
  background: #f8fafc;
  color: #334155;
}

.field input:focus {
  outline: none;
  border-color: var(--brand, #0d9488);
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.12);
}

.field-hint {
  margin: 10px 0 0;
  font-size: 0.82rem;
  color: #64748b;
}

.toast {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  z-index: 100;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15);
}

.toast.ok {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.toast.fail {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

@media (max-width: 767px) {
  .profile-card-head {
    padding: 16px;
  }

  .profile-head-actions {
    width: 100%;
    margin-left: 0;
  }

  .profile-head-actions .btn-primary,
  .profile-head-actions .btn-ghost-card {
    flex: 1;
    justify-content: center;
    min-height: 44px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .profile-form {
    padding: 4px 16px 20px;
  }
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .profile-head-actions {
    width: 100%;
    margin-left: 0;
  }
}
</style>
