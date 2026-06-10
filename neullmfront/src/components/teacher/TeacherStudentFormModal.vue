<template>
  <Teleport to="body">
    <div v-if="open" class="teacher-app classroom-theme t-student-modal-portal">
      <div
        class="t-student-modal"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="titleId"
        @click.self="close"
      >
        <div class="t-student-modal__panel" @click.stop>
          <header class="t-student-modal__head">
            <div>
              <p class="t-student-modal__eyebrow">学生档案</p>
              <h2 :id="titleId">{{ editing ? '编辑学生' : '添加学生' }}</h2>
            </div>
            <button type="button" class="t-student-modal__close" aria-label="关闭" @click="close">
              <i class="fas fa-times"></i>
            </button>
          </header>

          <form class="t-student-modal__body" @submit.prevent="submit">
            <section class="t-student-form-section">
              <h3><i class="fas fa-id-card"></i>基本信息</h3>
              <div class="t-student-form-grid">
                <label class="t-student-field">
                  <span>姓名 <em>*</em></span>
                  <input v-model="form.displayName" type="text" required maxlength="32" placeholder="如：张三" />
                </label>
                <label class="t-student-field">
                  <span>学号 <em>*</em></span>
                  <input v-model="form.studentNo" type="text" required maxlength="32" placeholder="如：20240001" />
                </label>
                <label class="t-student-field">
                  <span>专业 <em>*</em></span>
                  <input v-model="form.major" type="text" required maxlength="64" placeholder="如：软件工程" />
                </label>
                <label class="t-student-field">
                  <span>年级 <em>*</em></span>
                  <input v-model="form.grade" type="text" required maxlength="16" placeholder="如：2024级" />
                </label>
                <label class="t-student-field t-student-field--wide">
                  <span>班级 <em>*</em></span>
                  <input v-model="form.className" type="text" required maxlength="64" placeholder="如：软工2401班" />
                </label>
              </div>
            </section>

            <section class="t-student-form-section">
              <h3><i class="fas fa-key"></i>账号信息</h3>
              <div class="t-student-form-grid">
                <label class="t-student-field">
                  <span>登录用户名</span>
                  <input v-model="form.username" type="text" maxlength="32" :placeholder="usernamePlaceholder" />
                  <small>留空则自动生成（如 s + 学号）</small>
                </label>
                <label class="t-student-field">
                  <span>{{ editing ? '新密码' : '初始密码' }} <em v-if="!editing">*</em></span>
                  <input
                    v-model="form.password"
                    type="password"
                    :required="!editing"
                    minlength="6"
                    maxlength="64"
                    :placeholder="editing ? '留空表示不修改' : '至少 6 位'"
                  />
                </label>
              </div>
            </section>

            <footer class="t-student-modal__foot">
              <button type="button" class="t-btn-ghost" :disabled="saving" @click="close">取消</button>
              <button type="submit" class="t-btn-solid" :disabled="saving">
                <i v-if="saving" class="fas fa-spinner fa-spin"></i>
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </footer>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onUnmounted, reactive, watch } from 'vue';

const props = defineProps({
  open: { type: Boolean, default: false },
  student: { type: Object, default: null },
  saving: { type: Boolean, default: false },
});

const emit = defineEmits(['close', 'save']);

const titleId = 't-student-modal-title';
const editing = computed(() => !!props.student?.studentUserId);

const form = reactive({
  displayName: '',
  studentNo: '',
  major: '',
  grade: '',
  className: '',
  username: '',
  password: '',
});

const usernamePlaceholder = computed(() =>
  form.studentNo ? `默认 s${form.studentNo}` : '默认 s + 学号',
);

watch(
  () => [props.open, props.student],
  () => {
    if (!props.open) return;
    form.displayName = props.student?.name || '';
    form.studentNo = props.student?.studentId || '';
    form.major = props.student?.major || '';
    form.grade = props.student?.grade || '';
    form.className = props.student?.className || '';
    form.username = props.student?.username || '';
    form.password = '';
  },
  { immediate: true },
);

watch(
  () => props.open,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : '';
  },
);

onUnmounted(() => {
  document.body.style.overflow = '';
});

function close() {
  if (props.saving) return;
  emit('close');
}

function submit() {
  const payload = {
    displayName: form.displayName.trim(),
    studentNo: form.studentNo.trim(),
    major: form.major.trim(),
    grade: form.grade.trim(),
    className: form.className.trim(),
    username: form.username.trim() || undefined,
    password: form.password.trim() || undefined,
  };
  if (!payload.displayName || !payload.studentNo || !payload.major || !payload.grade || !payload.className) {
    return;
  }
  if (!editing.value && !payload.password) return;
  emit('save', { payload, studentUserId: props.student?.studentUserId || null });
}
</script>
