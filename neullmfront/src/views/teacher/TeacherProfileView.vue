<template>
  <div class="t-page t-profile-page" :class="{ 't-profile-page--editing': isEditing }">
    <div v-if="loading" class="t-profile-loading">
      <i class="fas fa-spinner fa-spin"></i>
      <span>加载个人信息…</span>
    </div>

    <div v-else class="t-profile-shell">
      <header class="t-profile-banner">
        <div class="t-profile-banner__left">
          <div class="t-profile-banner__avatar" aria-hidden="true">
            <span>{{ profileInitial }}</span>
            <em v-if="displayTitle">{{ displayTitle.charAt(0) }}</em>
          </div>
          <div class="t-profile-banner__intro">
            <p class="t-profile-banner__eyebrow">教师工作台 · 个人档案</p>
            <h1>{{ displayName || '未命名教师' }}</h1>
            <p class="t-profile-banner__meta">
              <span v-if="displayDepartment">{{ displayDepartment }}</span>
              <span v-if="displayDepartment && displayTitle"> · </span>
              <span v-if="displayTitle">{{ displayTitle }}</span>
            </p>
            <div class="t-profile-banner__tags">
              <span v-if="displayEmployeeNo" class="t-profile-pill">
                <i class="fas fa-id-badge"></i>{{ displayEmployeeNo }}
              </span>
              <span v-if="profile.classId" class="t-profile-pill">
                <i class="fas fa-school"></i>{{ profile.classId }}
              </span>
              <span v-if="isEditing" class="t-profile-pill t-profile-pill--edit">
                <i class="fas fa-pen"></i>编辑中
              </span>
            </div>
          </div>
        </div>

        <div class="t-profile-banner__metrics">
          <div class="t-profile-metric">
            <strong>{{ displaySubjects.length }}</strong>
            <span>授课课程</span>
          </div>
          <div class="t-profile-metric">
            <strong>{{ displayScopes.length }}</strong>
            <span>教学板块</span>
          </div>
          <div class="t-profile-metric t-profile-metric--accent">
            <strong>{{ matchedStudentCount }}</strong>
            <span>可见学生</span>
          </div>
        </div>

        <div class="t-profile-banner__actions">
          <template v-if="isEditing">
            <button type="button" class="t-btn-ghost" :disabled="saving" @click="cancelEdit">
              取消
            </button>
            <button type="button" class="t-btn-solid" :disabled="saving" @click="save">
              <i v-if="saving" class="fas fa-spinner fa-spin"></i>
              <i v-else class="fas fa-check"></i>
              {{ saving ? '保存中…' : '保存修改' }}
            </button>
          </template>
          <button v-else type="button" class="t-btn-solid" @click="startEdit">
            <i class="fas fa-pen"></i>
            编辑档案
          </button>
        </div>
      </header>

      <div class="t-profile-grid">
        <section class="t-profile-card t-profile-card--teaching">
          <header class="t-profile-card__head">
            <div class="t-profile-card__icon t-profile-card__icon--violet">
              <i class="fas fa-book-open"></i>
            </div>
            <div>
              <h2>授课设置</h2>
              <p>所教课程与教学板块将决定您可查看的学生范围</p>
            </div>
          </header>

          <div class="t-profile-card__body">
            <div class="t-profile-segment">
              <div class="t-profile-segment__title">
                <span>所教课程</span>
                <em v-if="isEditing">至少 1 门</em>
              </div>
              <div v-if="isEditing" class="t-profile-subjects">
                <button
                  v-for="subject in subjectOptions"
                  :key="subject"
                  type="button"
                  class="t-profile-subject"
                  :class="{ 't-profile-subject--on': form.taughtSubjects.includes(subject) }"
                  @click="toggleSubject(subject)"
                >
                  <span class="t-profile-subject__dot"></span>
                  <span class="t-profile-subject__text">{{ subject }}</span>
                  <i class="fas fa-check t-profile-subject__check"></i>
                </button>
              </div>
              <div v-else class="t-profile-readonly-chips">
                <span
                  v-for="subject in displaySubjects"
                  :key="subject"
                  class="t-profile-readonly-chip"
                >{{ subject }}</span>
                <span v-if="!displaySubjects.length" class="t-profile-readonly-empty">未设置</span>
              </div>
            </div>

            <div class="t-profile-segment t-profile-segment--grow">
              <div class="t-profile-segment__title">
                <span>教学板块</span>
                <em>专业 · 年级 · 班级</em>
              </div>

              <div class="t-profile-segment__content">
                <div v-if="!displayScopes.length && !isEditing" class="t-profile-scope-empty">
                  <div class="t-profile-scope-empty__icon"><i class="fas fa-layer-group"></i></div>
                  <p>尚未配置教学板块</p>
                </div>

                <div v-else-if="!isEditing" class="t-profile-scopes t-profile-scopes--readonly">
                  <div
                    v-for="(scope, index) in displayScopes"
                    :key="index"
                    class="t-profile-scope t-profile-scope--ready t-profile-scope--readonly"
                  >
                    <div class="t-profile-scope__head">
                      <span class="t-profile-scope__badge">{{ index + 1 }}</span>
                      <span class="t-profile-scope__preview">{{ scopePreview(scope) }}</span>
                    </div>
                  </div>
                </div>

                <template v-else>
                  <div v-if="!form.teachingScopes.length" class="t-profile-scope-empty">
                    <div class="t-profile-scope-empty__icon"><i class="fas fa-layer-group"></i></div>
                    <p>添加板块后，仅对应学生出现在学生列表、学情看板与批改作业</p>
                    <button type="button" class="t-profile-scope-empty__btn" @click="addScope">
                      <i class="fas fa-plus"></i> 添加第一个板块
                    </button>
                  </div>

                  <ul v-else class="t-profile-scopes">
                    <li
                      v-for="(scope, index) in form.teachingScopes"
                      :key="index"
                      class="t-profile-scope"
                      :class="{ 't-profile-scope--ready': isScopeReady(scope) }"
                    >
                      <div class="t-profile-scope__head">
                        <span class="t-profile-scope__badge">{{ index + 1 }}</span>
                        <span class="t-profile-scope__preview">{{ scopePreview(scope) }}</span>
                        <button
                          type="button"
                          class="t-profile-scope__remove"
                          aria-label="删除板块"
                          @click="removeScope(index)"
                        >
                          <i class="fas fa-times"></i>
                        </button>
                      </div>
                      <div class="t-profile-scope__fields">
                        <label class="t-profile-select">
                          <span>专业</span>
                          <div class="t-profile-select__wrap">
                            <i class="fas fa-graduation-cap"></i>
                            <select v-model="scope.major">
                              <option value="">请选择专业</option>
                              <option v-for="opt in scopeOptions.majors" :key="opt" :value="opt">{{ opt }}</option>
                            </select>
                          </div>
                        </label>
                        <label class="t-profile-select">
                          <span>年级</span>
                          <div class="t-profile-select__wrap">
                            <i class="fas fa-calendar-alt"></i>
                            <select v-model="scope.grade">
                              <option value="">请选择年级</option>
                              <option v-for="opt in scopeOptions.grades" :key="opt" :value="opt">{{ opt }}</option>
                            </select>
                          </div>
                        </label>
                        <label class="t-profile-select">
                          <span>班级</span>
                          <div class="t-profile-select__wrap">
                            <i class="fas fa-users"></i>
                            <select v-model="scope.className">
                              <option value="">请选择班级</option>
                              <option v-for="opt in scopeOptions.classNames" :key="opt" :value="opt">{{ opt }}</option>
                            </select>
                          </div>
                        </label>
                      </div>
                    </li>
                  </ul>

                  <button v-if="form.teachingScopes.length" type="button" class="t-profile-add-scope" @click="addScope">
                    <i class="fas fa-plus"></i>
                    继续添加板块
                  </button>
                </template>
              </div>
            </div>
          </div>
        </section>

        <section class="t-profile-card t-profile-card--identity">
          <header class="t-profile-card__head">
            <div class="t-profile-card__icon t-profile-card__icon--cyan">
              <i class="fas fa-id-card"></i>
            </div>
            <div>
              <h2>档案与账号</h2>
              <p>基本信息、联系方式与登录凭据</p>
            </div>
          </header>

          <div class="t-profile-card__body">
            <div class="t-profile-segment">
              <div class="t-profile-segment__title">
                <span>基本信息</span>
                <em>对外展示</em>
              </div>
              <div v-if="isEditing" class="t-profile-fields t-profile-fields--grid">
                <label class="t-profile-input">
                  <span>姓名 <em>*</em></span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-user"></i>
                    <input v-model="form.displayName" type="text" maxlength="32" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>工号</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-hashtag"></i>
                    <input v-model="form.employeeNo" type="text" maxlength="32" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>院系</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-building"></i>
                    <input v-model="form.department" type="text" maxlength="64" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>职称</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-award"></i>
                    <input v-model="form.title" type="text" maxlength="32" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>邮箱</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-envelope"></i>
                    <input v-model="form.email" type="email" maxlength="128" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>手机号</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-phone"></i>
                    <input v-model="form.phone" type="tel" maxlength="32" />
                  </div>
                </label>
              </div>
              <dl v-else class="t-profile-readonly-grid">
                <div class="t-profile-readonly-item">
                  <dt>姓名</dt>
                  <dd>{{ displayName || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>工号</dt>
                  <dd>{{ displayEmployeeNo || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>院系</dt>
                  <dd>{{ displayDepartment || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>职称</dt>
                  <dd>{{ displayTitle || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>邮箱</dt>
                  <dd>{{ displayEmail || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>手机号</dt>
                  <dd>{{ displayPhone || '—' }}</dd>
                </div>
              </dl>
            </div>

            <div class="t-profile-segment t-profile-segment--grow">
              <div class="t-profile-segment__title">
                <span>账号安全</span>
                <em>登录凭据</em>
              </div>
              <div v-if="isEditing" class="t-profile-fields t-profile-fields--grid">
                <label class="t-profile-input">
                  <span>登录用户名 <em>*</em></span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-at"></i>
                    <input v-model="form.username" type="text" maxlength="32" />
                  </div>
                </label>
                <label class="t-profile-input">
                  <span>新密码</span>
                  <div class="t-profile-input__wrap">
                    <i class="fas fa-lock"></i>
                    <input
                      v-model="form.password"
                      type="password"
                      maxlength="64"
                      placeholder="留空表示不修改"
                      autocomplete="new-password"
                    />
                  </div>
                </label>
              </div>
              <dl v-else class="t-profile-readonly-grid t-profile-readonly-grid--narrow">
                <div class="t-profile-readonly-item">
                  <dt>登录用户名</dt>
                  <dd>{{ displayUsername || '—' }}</dd>
                </div>
                <div class="t-profile-readonly-item">
                  <dt>密码</dt>
                  <dd>••••••••</dd>
                </div>
              </dl>
              <div class="t-profile-tip">
                <i class="fas fa-lightbulb"></i>
                <p>
                  {{ isEditing
                    ? '保存后，学生列表与学情数据将按左侧「教学板块」自动过滤。'
                    : '点击右上角「编辑档案」后可修改个人信息与授课范围。' }}
                </p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import authApi from '../../services/authApi';
import classroomApi from '../../services/classroomApi';
import { SUBJECT_OPTIONS } from '../../constants/subjects';
import { getToken, getUser, setAuth } from '../../stores/auth';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const { showToast, loadStudents } = useTeacherClassroom();

const subjectOptions = SUBJECT_OPTIONS;
const loading = ref(true);
const saving = ref(false);
const isEditing = ref(false);
const profile = reactive({});
let editSnapshot = null;

const scopeOptions = reactive({
  majors: [],
  grades: [],
  classNames: [],
  roster: [],
});

const form = reactive({
  displayName: '',
  username: '',
  employeeNo: '',
  department: '',
  title: '',
  email: '',
  phone: '',
  password: '',
  taughtSubjects: [],
  teachingScopes: [],
});

const displayName = computed(() => (isEditing.value ? form.displayName : profile.displayName));
const displayUsername = computed(() => (isEditing.value ? form.username : profile.username));
const displayEmployeeNo = computed(() => (isEditing.value ? form.employeeNo : profile.employeeNo));
const displayDepartment = computed(() => (isEditing.value ? form.department : profile.department));
const displayTitle = computed(() => (isEditing.value ? form.title : profile.title));
const displayEmail = computed(() => (isEditing.value ? form.email : profile.email));
const displayPhone = computed(() => (isEditing.value ? form.phone : profile.phone));
const displaySubjects = computed(() =>
  isEditing.value ? form.taughtSubjects : (profile.taughtSubjects || []),
);
const displayScopes = computed(() =>
  isEditing.value ? form.teachingScopes : (profile.teachingScopes || []),
);

const profileInitial = computed(() => {
  const name = displayName.value || '师';
  return name.trim().charAt(0);
});

const matchedStudentCount = computed(() => {
  const scopes = displayScopes.value;
  if (!scopes.length) return 0;
  return scopeOptions.roster.filter((s) =>
    scopes.some(
      (scope) =>
        scope.major &&
        scope.grade &&
        scope.className &&
        s.major === scope.major &&
        s.grade === scope.grade &&
        s.className === scope.className,
    ),
  ).length;
});

function isScopeReady(scope) {
  return !!(scope.major && scope.grade && scope.className);
}

function scopePreview(scope) {
  if (!isScopeReady(scope)) return '待完善板块信息';
  return `${scope.major} · ${scope.grade} · ${scope.className}`;
}

function cloneFormState() {
  return {
    displayName: form.displayName,
    username: form.username,
    employeeNo: form.employeeNo,
    department: form.department,
    title: form.title,
    email: form.email,
    phone: form.phone,
    password: '',
    taughtSubjects: [...form.taughtSubjects],
    teachingScopes: form.teachingScopes.map((s) => ({
      major: s.major,
      grade: s.grade,
      className: s.className,
    })),
  };
}

function applyProfile(data) {
  Object.assign(profile, data);
  form.displayName = data.displayName || '';
  form.username = data.username || '';
  form.employeeNo = data.employeeNo || '';
  form.department = data.department || '';
  form.title = data.title || '';
  form.email = data.email || '';
  form.phone = data.phone || '';
  form.password = '';
  form.taughtSubjects = [...(data.taughtSubjects || [])];
  form.teachingScopes = (data.teachingScopes || []).map((s) => ({
    major: s.major || '',
    grade: s.grade || '',
    className: s.className || '',
  }));
}

function restoreFormState(snap) {
  if (!snap) return;
  form.displayName = snap.displayName;
  form.username = snap.username;
  form.employeeNo = snap.employeeNo;
  form.department = snap.department;
  form.title = snap.title;
  form.email = snap.email;
  form.phone = snap.phone;
  form.password = '';
  form.taughtSubjects = [...snap.taughtSubjects];
  form.teachingScopes = snap.teachingScopes.map((s) => ({ ...s }));
}

function startEdit() {
  editSnapshot = cloneFormState();
  isEditing.value = true;
}

function cancelEdit() {
  restoreFormState(editSnapshot);
  editSnapshot = null;
  isEditing.value = false;
}

function toggleSubject(subject) {
  if (!isEditing.value) return;
  const idx = form.taughtSubjects.indexOf(subject);
  if (idx >= 0) {
    form.taughtSubjects.splice(idx, 1);
  } else {
    form.taughtSubjects.push(subject);
  }
}

function addScope() {
  form.teachingScopes.push({ major: '', grade: '', className: '' });
}

function removeScope(index) {
  form.teachingScopes.splice(index, 1);
}

function apiErrorMessage(e, fallback) {
  return e?.response?.data?.message || e?.message || fallback;
}

async function loadScopeOptions() {
  try {
    const data = await classroomApi.listStudentScopeOptions();
    scopeOptions.majors = data.majors || [];
    scopeOptions.grades = data.grades || [];
    scopeOptions.classNames = data.classNames || [];
    scopeOptions.roster = data.roster || [];
  } catch {
    scopeOptions.majors = [];
    scopeOptions.grades = [];
    scopeOptions.classNames = [];
    scopeOptions.roster = [];
  }
}

async function loadProfile() {
  loading.value = true;
  try {
    const data = await authApi.getTeacherProfile();
    applyProfile(data || {});
    isEditing.value = false;
    editSnapshot = null;
  } catch (e) {
    showToast(apiErrorMessage(e, '加载个人信息失败'), false, 4000);
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!isEditing.value) return;

  if (!form.displayName.trim()) {
    showToast('请填写姓名', false);
    return;
  }
  if (!form.username.trim()) {
    showToast('请填写登录用户名', false);
    return;
  }
  if (!form.taughtSubjects.length) {
    showToast('请至少选择一门所教课程', false);
    return;
  }
  if (!form.teachingScopes.length) {
    showToast('请至少添加一个教学板块', false);
    return;
  }
  if (form.teachingScopes.some((s) => !s.major || !s.grade || !s.className)) {
    showToast('请完整填写每个教学板块的专业、年级与班级', false);
    return;
  }
  if (form.password.trim() && form.password.trim().length < 6) {
    showToast('新密码至少 6 位', false);
    return;
  }

  saving.value = true;
  try {
    const payload = {
      displayName: form.displayName.trim(),
      username: form.username.trim(),
      employeeNo: form.employeeNo.trim() || null,
      department: form.department.trim() || null,
      title: form.title.trim() || null,
      email: form.email.trim() || null,
      phone: form.phone.trim() || null,
      taughtSubjects: [...form.taughtSubjects],
      teachingScopes: form.teachingScopes.map((s) => ({
        major: s.major.trim(),
        grade: s.grade.trim(),
        className: s.className.trim(),
      })),
    };
    if (form.password.trim()) {
      payload.password = form.password.trim();
    }
    const data = await authApi.updateTeacherProfile(payload);
    applyProfile(data || {});
    isEditing.value = false;
    editSnapshot = null;
    const current = getUser();
    if (current) {
      setAuth(getToken(), {
        ...current,
        username: form.username.trim(),
        displayName: form.displayName.trim(),
        studentNo: form.employeeNo.trim() || null,
      });
    }
    await loadStudents();
    showToast('个人信息已保存');
  } catch (e) {
    showToast(apiErrorMessage(e, '保存失败'), false, 4000);
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadScopeOptions(), loadStudents()]);
});
</script>
