<template>
  <div
    ref="sidebarCourseRef"
    v-if="showFilter"
    class="t-sidebar-course"
  >
    <button
      type="button"
      class="t-sidebar-course__trigger"
      :class="{ 't-sidebar-course__trigger--open': open }"
      @click.stop="open = !open"
    >
      <i class="fas fa-filter"></i>
      <span class="t-sidebar-course__label">{{ currentLabel }}</span>
      <i class="fas fa-chevron-down t-sidebar-course__chev" :class="{ 't-sidebar-course__chev--open': open }"></i>
    </button>
    <Transition name="t-ws-context-drop">
      <div v-if="open" class="t-sidebar-course__menu" @click.stop>
        <button
          type="button"
          class="t-sidebar-course__item"
          :class="{ 't-sidebar-course__item--on': subjectFilter === 'all' }"
          @click="pick('all')"
        >
          全部课程 <em>{{ tasks.length }}</em>
        </button>
        <button
          v-for="item in availableSubjects"
          :key="item.value"
          type="button"
          class="t-sidebar-course__item"
          :class="{ 't-sidebar-course__item--on': subjectFilter === item.value }"
          @click="pick(item.value)"
        >
          {{ item.label }} <em>{{ item.count }}</em>
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';

const {
  tasks,
  subjectFilter,
  availableSubjects,
  setSubjectFilter,
} = useTeacherClassroom();

const sidebarCourseRef = ref(null);
const open = ref(false);

const showFilter = computed(
  () => tasks.value.length > 0 && availableSubjects.value.length > 0,
);

const currentLabel = computed(() => {
  if (subjectFilter.value === 'all') return '全部课程';
  return availableSubjects.value.find((s) => s.value === subjectFilter.value)?.label || '全部课程';
});

function pick(value) {
  setSubjectFilter(value);
  open.value = false;
}

function onDocumentClick(e) {
  if (!sidebarCourseRef.value?.contains(e.target)) open.value = false;
}

onMounted(() => document.addEventListener('click', onDocumentClick));
onUnmounted(() => document.removeEventListener('click', onDocumentClick));
</script>
