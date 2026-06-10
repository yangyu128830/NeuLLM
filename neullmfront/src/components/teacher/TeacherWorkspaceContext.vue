<template>
  <div class="t-ws-context t-ws-context--head" aria-label="工作台上下文">
    <div class="t-ws-context__panel">
      <!-- 课程 -->
      <div
        v-if="showCourseFilter"
        ref="courseRef"
        class="t-ws-context__cell t-ws-context__cell--course"
      >
        <button
          type="button"
          class="t-ws-context__trigger"
          :class="{ 't-ws-context__trigger--open': courseOpen }"
          aria-haspopup="listbox"
          :aria-expanded="courseOpen"
          @click.stop="toggleCourse"
        >
          <span class="t-ws-context__icon t-ws-context__icon--course">
            <i class="fas fa-book-open"></i>
          </span>
          <span class="t-ws-context__copy">
            <em class="t-ws-context__eyebrow">课程筛选</em>
            <strong class="t-ws-context__value">{{ currentSubjectLabel }}</strong>
          </span>
          <i class="fas fa-chevron-down t-ws-context__chev" :class="{ 't-ws-context__chev--open': courseOpen }"></i>
        </button>

        <Transition name="t-ws-context-drop">
          <div v-if="courseOpen" class="t-ws-context__menu" role="listbox" @click.stop>
            <button
              type="button"
              role="option"
              class="t-ws-context__option"
              :class="{ 't-ws-context__option--on': subjectFilter === 'all' }"
              @click="pickSubject('all')"
            >
              <span class="t-ws-context__option-main">
                <span class="t-ws-context__option-title">全部课程</span>
                <span class="t-ws-context__option-sub">显示所有任务</span>
              </span>
              <span class="t-ws-context__option-count">{{ tasks.length }}</span>
              <i v-if="subjectFilter === 'all'" class="fas fa-check t-ws-context__option-check"></i>
            </button>
            <button
              v-for="item in availableSubjects"
              :key="item.value"
              type="button"
              role="option"
              class="t-ws-context__option"
              :class="{ 't-ws-context__option--on': subjectFilter === item.value }"
              @click="pickSubject(item.value)"
            >
              <span class="t-ws-context__option-main">
                <span class="t-ws-context__option-title">{{ item.label }}</span>
                <span class="t-ws-context__option-sub">{{ item.count }} 个任务</span>
              </span>
              <span class="t-ws-context__option-count">{{ item.count }}</span>
              <i v-if="subjectFilter === item.value" class="fas fa-check t-ws-context__option-check"></i>
            </button>
          </div>
        </Transition>
      </div>

      <div v-if="showCourseFilter" class="t-ws-context__divider" aria-hidden="true"></div>

      <!-- 任务 -->
      <div class="t-ws-context__cell t-ws-context__cell--task">
        <TeacherTaskPicker
          v-model="taskId"
          embedded
          :tasks="tasks"
          :refreshable="refreshable"
          :refreshing="refreshing"
          @change="emit('change')"
          @refresh="emit('refresh')"
        />
      </div>

      <div v-if="$slots.default" class="t-ws-context__divider" aria-hidden="true"></div>

      <!-- 扩展操作 -->
      <div v-if="$slots.default" class="t-ws-context__cell t-ws-context__cell--actions">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import TeacherTaskPicker from './TeacherTaskPicker.vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const props = defineProps({
  modelValue: { type: String, default: '' },
  tasks: { type: Array, default: () => [] },
  refreshable: { type: Boolean, default: false },
  refreshing: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue', 'change', 'refresh']);

const {
  tasks: allTasks,
  subjectFilter,
  availableSubjects,
  setSubjectFilter,
} = useTeacherClassroom();

const courseRef = ref(null);
const courseOpen = ref(false);

const taskId = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const showCourseFilter = computed(
  () => allTasks.value.length > 0 && availableSubjects.value.length > 0,
);

const currentSubjectLabel = computed(() => {
  if (subjectFilter.value === 'all') return '全部课程';
  const item = availableSubjects.value.find((s) => s.value === subjectFilter.value);
  return item?.label || '全部课程';
});

function toggleCourse() {
  courseOpen.value = !courseOpen.value;
}

function pickSubject(value) {
  setSubjectFilter(value);
  courseOpen.value = false;
}

function onDocumentClick(e) {
  if (!courseRef.value?.contains(e.target)) {
    courseOpen.value = false;
  }
}

onMounted(() => document.addEventListener('click', onDocumentClick));
onUnmounted(() => document.removeEventListener('click', onDocumentClick));
</script>
