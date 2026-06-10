<template>
  <div ref="rootRef" class="t-task-picker" :class="{ 't-task-picker--embedded': embedded }">
    <div v-if="!embedded" class="t-task-picker__head">
      <span class="t-task-picker__label">当前任务</span>
      <span v-if="tasks.length" class="t-task-picker__count">{{ tasks.length }}</span>
    </div>

    <div class="t-task-picker__bar">
      <button
        type="button"
        class="t-task-picker__trigger"
        :class="{
          't-task-picker__trigger--open': open,
          't-task-picker__trigger--empty': !currentTask,
          't-task-picker__trigger--embedded': embedded,
        }"
        :aria-expanded="open"
        aria-haspopup="listbox"
        @click.stop="toggleOpen"
        @keydown="onTriggerKeydown"
      >
        <span class="t-task-picker__trigger-icon" aria-hidden="true">
          <i class="fas fa-tasks"></i>
        </span>
        <span class="t-task-picker__trigger-body">
          <template v-if="currentTask">
            <span v-if="embedded" class="t-task-picker__trigger-eyebrow">当前任务</span>
            <span class="t-task-picker__trigger-title">{{ currentTask.title }}</span>
            <span class="t-task-picker__trigger-meta">
              <span class="t-task-picker__code">{{ currentTask.taskId }}</span>
              <span
                class="t-task-picker__status"
                :class="currentTask.published ? 't-task-picker__status--live' : 't-task-picker__status--draft'"
              >
                {{ currentTask.published ? '已发布' : '草稿' }}
              </span>
              <span v-if="subTaskCount(currentTask)" class="t-task-picker__subcount">
                {{ subTaskCount(currentTask) }} 个子任务
              </span>
            </span>
          </template>
          <span v-else class="t-task-picker__placeholder">请选择任务</span>
        </span>
        <i
          class="fas fa-chevron-down t-task-picker__chev"
          :class="{ 't-task-picker__chev--open': open }"
          aria-hidden="true"
        ></i>
      </button>

      <button
        v-if="refreshable"
        type="button"
        class="t-task-picker__refresh"
        aria-label="刷新任务数据"
        :class="{ 't-task-picker__refresh--spin': refreshing }"
        @click="emitRefresh"
      >
        <i class="fas fa-sync-alt"></i>
      </button>

      <Transition name="t-task-picker-drop">
        <div v-if="open" class="t-task-picker__menu" role="listbox" @click.stop>
          <div v-if="sortedTasks.length > 4" class="t-task-picker__search">
            <i class="fas fa-search" aria-hidden="true"></i>
            <input
              ref="searchRef"
              v-model="query"
              type="search"
              placeholder="搜索编号或标题…"
              autocomplete="off"
              @keydown="onSearchKeydown"
            />
            <button
              v-if="query"
              type="button"
              class="t-task-picker__search-clear"
              aria-label="清除搜索"
              @click="clearSearch"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div class="t-task-picker__list">
            <button
              v-for="(task, index) in filteredTasks"
              :key="task.taskId"
              type="button"
              role="option"
              class="t-task-picker__option"
              :class="{
                't-task-picker__option--on': task.taskId === modelValue,
                't-task-picker__option--focus': index === focusIndex,
              }"
              :aria-selected="task.taskId === modelValue"
              @click="selectTask(task)"
              @mouseenter="focusIndex = index"
            >
              <span class="t-task-picker__option-main">
                <span class="t-task-picker__option-title">{{ task.title }}</span>
                <span class="t-task-picker__option-meta">
                  <span v-if="task.subject" class="t-task-picker__subject">{{ task.subject }}</span>
                  <span class="t-task-picker__code">{{ task.taskId }}</span>
                  <span
                    class="t-task-picker__status"
                    :class="task.published ? 't-task-picker__status--live' : 't-task-picker__status--draft'"
                  >
                    {{ task.published ? '已发布' : '草稿' }}
                  </span>
                  <span v-if="subTaskCount(task)" class="t-task-picker__subcount">
                    {{ subTaskCount(task) }} 项
                  </span>
                </span>
              </span>
              <i
                v-if="task.taskId === modelValue"
                class="fas fa-check t-task-picker__check"
                aria-hidden="true"
              ></i>
            </button>

            <div v-if="!filteredTasks.length" class="t-task-picker__empty">
              <i class="fas fa-inbox" aria-hidden="true"></i>
              <span>{{ query ? '没有匹配的任务' : '暂无任务' }}</span>
            </div>
          </div>

          <router-link to="/teacher/tasks" class="t-task-picker__foot" @click="closeMenu">
            <i class="fas fa-sliders-h" aria-hidden="true"></i>
            管理全部任务
          </router-link>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';

const props = defineProps({
  modelValue: { type: String, default: '' },
  tasks: { type: Array, default: () => [] },
  refreshable: { type: Boolean, default: false },
  refreshing: { type: Boolean, default: false },
  embedded: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue', 'change', 'refresh']);

const rootRef = ref(null);
const searchRef = ref(null);
const open = ref(false);
const query = ref('');
const focusIndex = ref(-1);

const currentTask = computed(() => props.tasks.find((t) => t.taskId === props.modelValue) || null);

const sortedTasks = computed(() =>
  [...props.tasks].sort((a, b) => taskSortKey(b.taskId) - taskSortKey(a.taskId)),
);

const filteredTasks = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return sortedTasks.value;
  return sortedTasks.value.filter(
    (t) =>
      String(t.taskId).toLowerCase().includes(q) ||
      String(t.title || '').toLowerCase().includes(q) ||
      String(t.subject || '').toLowerCase().includes(q),
  );
});

watch(open, (isOpen) => {
  if (!isOpen) {
    query.value = '';
    focusIndex.value = -1;
  }
});

watch(filteredTasks, (list) => {
  if (!open.value) return;
  const selectedIdx = list.findIndex((t) => t.taskId === props.modelValue);
  focusIndex.value = selectedIdx >= 0 ? selectedIdx : list.length ? 0 : -1;
});

function taskSortKey(taskId) {
  const match = String(taskId).match(/(\d+)/);
  return match ? Number(match[1]) : 0;
}

function subTaskCount(task) {
  return task?.subTasks?.length || 0;
}

function closeMenu() {
  open.value = false;
}

function toggleOpen() {
  open.value = !open.value;
  if (open.value) {
    nextTick(() => {
      const selectedIdx = filteredTasks.value.findIndex((t) => t.taskId === props.modelValue);
      focusIndex.value = selectedIdx >= 0 ? selectedIdx : filteredTasks.value.length ? 0 : -1;
      searchRef.value?.focus();
    });
  }
}

function selectTask(task) {
  if (task.taskId === props.modelValue) {
    closeMenu();
    return;
  }
  emit('update:modelValue', task.taskId);
  emit('change');
  closeMenu();
}

function clearSearch() {
  query.value = '';
  searchRef.value?.focus();
}

function emitRefresh() {
  emit('refresh');
}

function onTriggerKeydown(e) {
  if (e.key === 'ArrowDown' || e.key === 'Enter' || e.key === ' ') {
    e.preventDefault();
    if (!open.value) toggleOpen();
  }
  if (e.key === 'Escape') closeMenu();
}

function onSearchKeydown(e) {
  const list = filteredTasks.value;
  if (e.key === 'ArrowDown') {
    e.preventDefault();
    if (!list.length) return;
    focusIndex.value = focusIndex.value < list.length - 1 ? focusIndex.value + 1 : 0;
  } else if (e.key === 'ArrowUp') {
    e.preventDefault();
    if (!list.length) return;
    focusIndex.value = focusIndex.value > 0 ? focusIndex.value - 1 : list.length - 1;
  } else if (e.key === 'Enter') {
    e.preventDefault();
    if (focusIndex.value >= 0 && list[focusIndex.value]) {
      selectTask(list[focusIndex.value]);
    }
  } else if (e.key === 'Escape') {
    e.preventDefault();
    closeMenu();
  }
}

function onDocumentClick(e) {
  if (!rootRef.value?.contains(e.target)) closeMenu();
}

function onDocumentKeydown(e) {
  if (e.key === 'Escape' && open.value) closeMenu();
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick);
  document.addEventListener('keydown', onDocumentKeydown);
});

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick);
  document.removeEventListener('keydown', onDocumentKeydown);
});
</script>
