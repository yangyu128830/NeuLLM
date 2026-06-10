<template>
  <div class="t-page t-students-page">
    <header class="t-ws-head t-students-head">
      <div class="t-ws-head__main">
        <div class="t-students-head__title-row">
          <h1>学生列表</h1>
          <span v-if="students.length" class="t-students-live-badge">
            <i class="fas fa-circle"></i>{{ filteredStudents.length }} / {{ students.length }} 名
          </span>
        </div>
        <p>管理本班学生档案，支持按专业、年级、班级筛选</p>
      </div>
      <div class="t-ws-head__actions t-students-head__actions">
        <button type="button" class="t-btn-solid" @click="openCreate">
          <i class="fas fa-user-plus"></i>
          添加学生
        </button>
      </div>
    </header>

    <section v-if="students.length" class="t-students-command t-students-command--roster">
      <div class="t-students-filters">
        <label class="t-students-filter">
          <span>专业</span>
          <select v-model="majorFilter">
            <option value="all">全部专业</option>
            <option v-for="item in majorOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label class="t-students-filter">
          <span>年级</span>
          <select v-model="gradeFilter">
            <option value="all">全部年级</option>
            <option v-for="item in gradeOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label class="t-students-filter">
          <span>班级</span>
          <select v-model="classFilter">
            <option value="all">全部班级</option>
            <option v-for="item in classOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
      </div>
      <div class="t-students-search">
        <i class="fas fa-search"></i>
        <input v-model="searchQuery" type="search" placeholder="搜索姓名、学号、用户名…" />
      </div>
      <button
        v-if="hasActiveFilters"
        type="button"
        class="t-btn-ghost t-students-clear"
        @click="clearFilters"
      >
        清除筛选
      </button>
    </section>

    <div class="t-ws-body t-students-body">
      <div v-if="!students.length" class="t-empty-state t-students-empty">
        <div class="t-empty-state__art"><i class="fas fa-user-graduate"></i></div>
        <h2>暂无学生</h2>
        <p>添加学生后，将出现在本班名册</p>
        <button type="button" class="t-btn-solid" @click="openCreate">
          <i class="fas fa-user-plus"></i>
          添加第一名学生
        </button>
      </div>

      <div v-else-if="!filteredStudents.length" class="t-empty-state t-students-empty">
        <div class="t-empty-state__art"><i class="fas fa-filter"></i></div>
        <h2>没有匹配的学生</h2>
        <p>尝试更换筛选条件或搜索关键词</p>
        <button type="button" class="t-btn-ghost" @click="clearFilters">显示全部</button>
      </div>

      <div v-else class="t-students-grid">
        <article
          v-for="(s, idx) in filteredStudents"
          :key="s.studentUserId"
          class="t-students-card"
          :class="`t-students-card--tone-${idx % 4}`"
        >
          <div class="t-students-card__banner" aria-hidden="true"></div>

          <div class="t-students-card__top">
            <div class="t-students-card__profile">
              <span :class="['t-students-card__avatar', `t-students-card__avatar--${idx % 4}`]">
                {{ studentInitial(s.name) }}
              </span>
              <div class="t-students-card__info">
                <div class="t-students-card__title-row">
                  <h3 class="t-students-card__name">{{ s.name }}</h3>
                  <div class="t-students-card__manage">
                    <button type="button" class="t-students-card__icon-btn" title="编辑" @click.stop="openEdit(s)">
                      <i class="fas fa-pen"></i>
                    </button>
                    <button
                      type="button"
                      class="t-students-card__icon-btn t-students-card__icon-btn--danger"
                      title="删除"
                      :disabled="deletingId === s.studentUserId"
                      @click.stop="confirmDelete(s)"
                    >
                      <i :class="deletingId === s.studentUserId ? 'fas fa-spinner fa-spin' : 'fas fa-trash-alt'"></i>
                    </button>
                  </div>
                </div>
                <p class="t-students-card__line t-students-card__line--muted">
                  学号 {{ s.studentId }}<template v-if="s.username"> · {{ s.username }}</template>
                </p>
              </div>
            </div>
          </div>

          <dl class="t-students-card__spec">
            <div class="t-students-card__spec-item">
              <dt>专业</dt>
              <dd>{{ s.major || '—' }}</dd>
            </div>
            <div class="t-students-card__spec-item">
              <dt>年级</dt>
              <dd>{{ s.grade || '—' }}</dd>
            </div>
            <div class="t-students-card__spec-item t-students-card__spec-item--wide">
              <dt>班级</dt>
              <dd>{{ s.className || '—' }}</dd>
            </div>
          </dl>

          <footer class="t-students-card__foot t-students-card__foot--roster">
            <button type="button" class="t-students-card__action t-students-card__action--detail" @click="openEdit(s)">
              <i class="fas fa-pen"></i>
              编辑档案
            </button>
          </footer>
        </article>
      </div>
    </div>

    <TeacherStudentFormModal
      :open="formOpen"
      :student="editingStudent"
      :saving="formSaving"
      @close="closeForm"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import TeacherStudentFormModal from '../../components/teacher/TeacherStudentFormModal.vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const {
  students,
  studentInitial,
  saveStudent,
  removeStudent,
} = useTeacherClassroom();

const searchQuery = ref('');
const majorFilter = ref('all');
const gradeFilter = ref('all');
const classFilter = ref('all');
const formOpen = ref(false);
const formSaving = ref(false);
const editingStudent = ref(null);
const deletingId = ref(null);

function uniqueField(field) {
  const set = new Set();
  for (const s of students.value) {
    const v = String(s[field] || '').trim();
    if (v) set.add(v);
  }
  return [...set].sort((a, b) => a.localeCompare(b, 'zh-CN'));
}

const majorOptions = computed(() => uniqueField('major'));
const gradeOptions = computed(() => uniqueField('grade'));
const classOptions = computed(() => uniqueField('className'));

const hasActiveFilters = computed(
  () =>
    majorFilter.value !== 'all'
    || gradeFilter.value !== 'all'
    || classFilter.value !== 'all'
    || searchQuery.value.trim(),
);

const sortedStudents = computed(() =>
  [...students.value].sort((a, b) =>
    String(a.studentId || '').localeCompare(String(b.studentId || ''), 'zh-CN'),
  ),
);

const filteredStudents = computed(() => {
  let list = sortedStudents.value;
  if (majorFilter.value !== 'all') {
    list = list.filter((s) => s.major === majorFilter.value);
  }
  if (gradeFilter.value !== 'all') {
    list = list.filter((s) => s.grade === gradeFilter.value);
  }
  if (classFilter.value !== 'all') {
    list = list.filter((s) => s.className === classFilter.value);
  }
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return list;
  return list.filter(
    (s) =>
      String(s.name || '').toLowerCase().includes(q) ||
      String(s.studentId || '').toLowerCase().includes(q) ||
      String(s.username || '').toLowerCase().includes(q) ||
      String(s.major || '').toLowerCase().includes(q) ||
      String(s.grade || '').toLowerCase().includes(q) ||
      String(s.className || '').toLowerCase().includes(q),
  );
});

function openCreate() {
  editingStudent.value = null;
  formOpen.value = true;
}

function openEdit(student) {
  editingStudent.value = student;
  formOpen.value = true;
}

function closeForm() {
  if (formSaving.value) return;
  formOpen.value = false;
  editingStudent.value = null;
}

async function handleSave({ payload, studentUserId }) {
  formSaving.value = true;
  try {
    const row = await saveStudent(payload, studentUserId);
    if (row) closeForm();
  } finally {
    formSaving.value = false;
  }
}

async function confirmDelete(student) {
  const name = student.name || student.studentId;
  if (!window.confirm(`确定删除学生「${name}」？\n其登录账号与相关数据将一并删除，且不可恢复。`)) {
    return;
  }
  deletingId.value = student.studentUserId;
  try {
    await removeStudent(student.studentUserId);
  } finally {
    deletingId.value = null;
  }
}

function clearFilters() {
  majorFilter.value = 'all';
  gradeFilter.value = 'all';
  classFilter.value = 'all';
  searchQuery.value = '';
}
</script>
