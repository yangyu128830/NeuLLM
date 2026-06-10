<template>
  <div class="t-page t-tasks-page">
    <div class="t-workspace t-workspace--split t-tasks-workspace">
      <aside class="t-tasks-sidebar">
        <div class="t-tasks-sidebar__head">
          <div>
            <h2>我的任务</h2>
            <p>选择任务查看或编辑</p>
          </div>
          <span class="t-tasks-sidebar__count">{{ tasks.length }}</span>
        </div>

        <div v-if="tasks.length > 3" class="t-tasks-sidebar__search">
          <i class="fas fa-search"></i>
          <input v-model="taskSearchQuery" type="search" placeholder="搜索任务…" />
        </div>

        <TeacherSubjectFilter />

        <div class="t-tasks-sidebar__scroll">
          <button
            type="button"
            :class="['t-tasks-create', { 't-tasks-create--on': isNewTask && !isEditingTask }]"
            @click="startNewTask"
          >
            <span class="t-tasks-create__icon"><i class="fas fa-plus"></i></span>
            <span class="t-tasks-create__body">
              <strong>新建任务</strong>
              <span>手动创建课堂任务</span>
            </span>
          </button>

          <button
            v-for="t in paginatedSidebarTasks"
            :key="t.taskId"
            type="button"
            :class="['t-tasks-item', { 't-tasks-item--on': t.taskId === currentTaskId && !isNewTask }]"
            @click="selectTask(t)"
          >
            <span class="t-tasks-item__accent" aria-hidden="true"></span>
            <span class="t-tasks-item__body">
              <span class="t-tasks-item__title">{{ t.title }}</span>
              <span class="t-tasks-item__meta">
                <span class="t-tasks-item__meta-left">
                  <span class="t-tasks-item__id">{{ t.taskId }}</span>
                  <span v-if="t.subject" class="t-tasks-item__subject">{{ t.subject }}</span>
                </span>
                <span
                  class="t-tasks-item__status"
                  :class="t.published ? 't-tasks-item__status--live' : 't-tasks-item__status--draft'"
                >
                  <i
                    v-if="t.published && t.taskId === currentTaskId && !isNewTask"
                    class="fas fa-check"
                    aria-hidden="true"
                  ></i>
                  {{ t.published ? '已发布' : '草稿' }}
                </span>
              </span>
              <span v-if="formatTaskTarget(t) !== '未指定布置对象'" class="t-tasks-item__audience">
                {{ formatTaskTarget(t) }}
              </span>
            </span>
          </button>

          <div v-if="tasks.length && !filteredSidebarTasks.length" class="t-tasks-sidebar__empty">
            没有匹配的任务
          </div>
        </div>

        <nav
          v-if="sidebarTotalPages > 1"
          class="t-tasks-sidebar__pager"
          aria-label="任务列表分页"
        >
          <button
            type="button"
            class="t-tasks-sidebar__pager-btn"
            :disabled="taskPage <= 1"
            aria-label="上一页"
            @click="taskPage -= 1"
          >
            <i class="fas fa-chevron-left"></i>
          </button>
          <span class="t-tasks-sidebar__pager-info">
            {{ taskPage }} / {{ sidebarTotalPages }}
          </span>
          <button
            type="button"
            class="t-tasks-sidebar__pager-btn"
            :disabled="taskPage >= sidebarTotalPages"
            aria-label="下一页"
            @click="taskPage += 1"
          >
            <i class="fas fa-chevron-right"></i>
          </button>
        </nav>

        <div class="t-tasks-sidebar__foot">
          <router-link to="/teacher/chat" class="t-tasks-agent-link">
            <i class="fas fa-robot"></i>
            <span>Agent 设计子任务</span>
            <i class="fas fa-arrow-right"></i>
          </router-link>
        </div>
      </aside>

      <div class="t-tasks-panel">
        <!-- 新建入口：卡片选择 -->
        <template v-if="isCreateHub">
          <header class="t-tasks-panel__head">
            <div class="t-tasks-panel__head-main">
              <h1>发布任务</h1>
              <p>选择创建方式，开始布置课堂作业</p>
            </div>
          </header>

          <div class="t-tasks-hub">
            <button type="button" class="t-tasks-hub-card" @click="beginCreateTask">
              <span class="t-tasks-hub-card__icon"><i class="fas fa-edit"></i></span>
              <span class="t-tasks-hub-card__body">
                <strong>手动创建任务</strong>
                <span>填写标题、说明与子任务，保存草稿或直接发布</span>
              </span>
              <span class="t-tasks-hub-card__arrow"><i class="fas fa-arrow-right"></i></span>
            </button>

            <router-link to="/teacher/chat" class="t-tasks-hub-card t-tasks-hub-card--agent">
              <span class="t-tasks-hub-card__icon"><i class="fas fa-robot"></i></span>
              <span class="t-tasks-hub-card__body">
                <strong>Agent 辅助设计</strong>
                <span>对话生成子任务结构，再确认发布</span>
              </span>
              <span class="t-tasks-hub-card__arrow"><i class="fas fa-arrow-right"></i></span>
            </router-link>
          </div>
        </template>

        <!-- 查看模式 -->
        <template v-else-if="isViewMode">
          <header class="t-tasks-panel__head t-tasks-panel__head--split">
            <div class="t-tasks-panel__head-main">
              <span class="t-tasks-panel__eyebrow">任务详情</span>
              <h1>查看任务</h1>
              <p>点击「编辑」可修改任务内容；草稿可直接发布</p>
            </div>
            <div v-if="currentTask" class="t-tasks-panel__badges">
              <span
                class="t-tasks-badge"
                :class="currentTask.published ? 't-tasks-badge--live' : 't-tasks-badge--draft'"
              >
                {{ currentTask.published ? '已发布' : '草稿' }}
              </span>
              <span class="t-tasks-badge t-tasks-badge--id">{{ currentTask.taskId }}</span>
            </div>
          </header>

          <div class="t-tasks-detail">
            <section class="t-tasks-detail__hero">
              <div class="t-tasks-detail__hero-top">
                <h2>{{ currentTask?.title || '—' }}</h2>
                <span v-if="currentTask?.subject" class="t-tasks-detail__subject-badge">
                  {{ currentTask.subject }}
                </span>
              </div>

              <div v-if="currentTask?.description" class="t-tasks-detail__desc-wrap">
                <p
                  class="t-tasks-detail__desc"
                  :class="{ 't-tasks-detail__desc--clamp': !descExpanded }"
                >
                  {{ currentTask.description }}
                </p>
                <button
                  v-if="descNeedsToggle"
                  type="button"
                  class="t-tasks-detail__desc-toggle"
                  @click="descExpanded = !descExpanded"
                >
                  {{ descExpanded ? '收起' : '展开全部' }}
                  <i :class="descExpanded ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"></i>
                </button>
              </div>
              <p v-else class="t-tasks-detail__desc t-tasks-detail__desc--empty">暂无说明</p>

              <div v-if="currentTask?.startTime || currentTask?.endTime" class="t-tasks-detail__timeline">
                <div class="t-tasks-detail__timeline-node">
                  <span class="t-tasks-detail__timeline-dot t-tasks-detail__timeline-dot--start"></span>
                  <div class="t-tasks-detail__timeline-text">
                    <em>开始时间</em>
                    <strong>{{ currentTask?.startTime ? formatTaskDateTime(currentTask.startTime) : '—' }}</strong>
                  </div>
                </div>
                <div class="t-tasks-detail__timeline-track" aria-hidden="true">
                  <span class="t-tasks-detail__timeline-line"></span>
                </div>
                <div class="t-tasks-detail__timeline-node">
                  <span class="t-tasks-detail__timeline-dot t-tasks-detail__timeline-dot--end"></span>
                  <div class="t-tasks-detail__timeline-text">
                    <em>截至时间</em>
                    <strong>{{ currentTask?.endTime ? formatTaskDateTime(currentTask.endTime) : '—' }}</strong>
                  </div>
                </div>
              </div>

              <div class="t-tasks-detail__audience">
                <i class="fas fa-users" aria-hidden="true"></i>
                <span>布置对象</span>
                <strong>{{ formatTaskTarget(currentTask) }}</strong>
              </div>
            </section>

            <section class="t-tasks-detail__section">
              <div class="t-tasks-detail__section-head">
                <h3>子任务结构</h3>
                <span>{{ viewSubTasks.length }} 项</span>
              </div>

              <div v-if="viewSubTasks.length" class="t-tasks-sub-grid">
                <div
                  v-for="(st, i) in viewSubTasks"
                  :key="st.subTaskId || i"
                  class="t-tasks-sub-card"
                >
                  <span class="t-tasks-sub-card__num">{{ i + 1 }}</span>
                  <div class="t-tasks-sub-card__body">
                    <strong>{{ st.title }}</strong>
                    <span v-if="st.description">{{ st.description }}</span>
                  </div>
                </div>
              </div>
              <p v-else class="t-tasks-detail__empty">暂无子任务</p>
            </section>
          </div>

          <footer class="t-tasks-panel__actions">
            <button type="button" class="t-action-btn t-action-btn--primary" @click="startEditTask">
              <i class="fas fa-pen"></i> 编辑
            </button>
            <button
              v-if="currentTask && !currentTask.published"
              type="button"
              class="t-action-btn t-action-btn--publish"
              @click="publishCurrent"
            >
              <i class="fas fa-paper-plane"></i> 发布任务
            </button>
            <button type="button" class="t-action-btn t-action-btn--danger" @click="deleteTask">
              <i class="fas fa-trash-alt"></i> 删除任务
            </button>
          </footer>
        </template>

        <!-- 新建 / 编辑表单 -->
        <template v-else-if="isEditingTask">
          <header class="t-tasks-panel__head t-tasks-panel__head--split">
            <div class="t-tasks-panel__head-main">
              <span class="t-tasks-panel__eyebrow">{{ isNewTask ? '新建' : '编辑' }}</span>
              <h1>{{ formTitle }}</h1>
              <p>{{ formHint }}</p>
            </div>
            <div class="t-tasks-panel__badges">
              <span v-if="isNewTask" class="t-tasks-badge t-tasks-badge--new">新建中</span>
              <template v-else-if="currentTaskId">
                <span
                  class="t-tasks-badge"
                  :class="currentTask?.published ? 't-tasks-badge--live' : 't-tasks-badge--draft'"
                >
                  {{ currentTask?.published ? '已发布' : '草稿' }}
                </span>
                <span class="t-tasks-badge t-tasks-badge--id">{{ currentTaskId }}</span>
              </template>
            </div>
          </header>

          <div class="t-tasks-panel__body">
            <section class="t-tasks-ai">
              <div class="t-tasks-ai__head">
                <span class="t-tasks-ai__badge"><i class="fas fa-magic"></i> AI 辅助填写</span>
                <button
                  type="button"
                  class="t-tasks-ai__cta"
                  :disabled="taskDraftAiLoading || !taskDraftPrompt.trim()"
                  @click="onGenerateDraft"
                >
                  <i :class="taskDraftAiLoading ? 'fas fa-circle-notch fa-spin' : 'fas fa-magic'"></i>
                  {{ generateDraftLabel }}
                </button>
              </div>
              <p class="t-tasks-ai__hint">{{ aiPanelHint }}</p>
              <textarea
                v-model="taskDraftPrompt"
                class="t-tasks-ai__prompt"
                rows="2"
                :placeholder="aiPromptPlaceholder"
              />
              <div v-if="isNewTask" class="t-tasks-ai__chips">
                <button
                  v-for="chip in draftPromptChips"
                  :key="chip"
                  type="button"
                  class="t-tasks-ai__chip"
                  @click="setTaskDraftPrompt(chip)"
                >
                  {{ chip }}
                </button>
              </div>

              <div v-if="taskDraftAiLoading" class="t-tasks-ai__loading">
                <div class="t-tasks-ai__loading-bar"></div>
                <p>正在生成任务草稿…</p>
              </div>

              <template v-else-if="taskDraftAiResult">
                <div class="t-tasks-ai__preview">
                  <h4>{{ taskDraftAiResult.title }}</h4>
                  <p>{{ taskDraftAiResult.description }}</p>
                  <ol v-if="taskDraftAiResult.subTasks?.length">
                    <li v-for="(st, i) in taskDraftAiResult.subTasks" :key="i">
                      <strong>{{ st.title }}</strong>
                      <span v-if="st.description">{{ st.description }}</span>
                    </li>
                  </ol>
                </div>
                <p v-if="taskDraftAiResult.designNote" class="t-tasks-ai__tip">
                  {{ taskDraftAiResult.designNote }}
                </p>
                <div class="t-tasks-ai__actions">
                  <button type="button" class="t-action-btn t-action-btn--primary" @click="applyTaskDraftAssist">
                    <i class="fas fa-arrow-down"></i> 填入表单
                  </button>
                  <button
                    type="button"
                    class="t-action-btn t-action-btn--ghost"
                    :disabled="taskDraftAiLoading"
                    @click="onGenerateDraft"
                  >
                    <i class="fas fa-redo-alt"></i> 重新生成
                  </button>
                </div>
              </template>
            </section>

            <div class="t-tasks-form">
            <section class="t-tasks-form__section">
              <header class="t-tasks-form__section-head">
                <h3><i class="fas fa-info-circle"></i>基本信息</h3>
              </header>
              <div class="t-tasks-form__field">
                <label for="task-title">任务标题</label>
                <input id="task-title" v-model="taskForm.title" placeholder="例如：自主决策 Agent 设计" />
              </div>
              <div class="t-tasks-form__field">
                <label for="task-desc">任务说明</label>
                <textarea
                  id="task-desc"
                  v-model="taskForm.description"
                  rows="3"
                  placeholder="向学生说明任务目标与要求"
                />
              </div>
            </section>

            <section class="t-tasks-form__section t-tasks-form__section--schedule">
              <header class="t-tasks-form__section-head">
                <h3><i class="fas fa-calendar-alt"></i>课程与时间安排</h3>
                <span>便于按课程筛选与截止提醒</span>
              </header>
              <div class="t-tasks-form__field">
                <label for="task-subject">科目</label>
                <select id="task-subject" v-model="taskForm.subject">
                  <option value="">请选择科目</option>
                  <option v-for="opt in subjectOptions" :key="opt" :value="opt">{{ opt }}</option>
                </select>
              </div>
              <div class="t-tasks-form__row">
                <div class="t-tasks-form__field">
                  <label for="task-start">开始时间</label>
                  <input id="task-start" v-model="taskForm.startTime" type="datetime-local" />
                </div>
                <div class="t-tasks-form__field">
                  <label for="task-end">截至时间</label>
                  <input id="task-end" v-model="taskForm.endTime" type="datetime-local" />
                </div>
              </div>
            </section>

            <section class="t-tasks-form__section t-tasks-form__section--audience">
              <header class="t-tasks-form__section-head">
                <h3><i class="fas fa-users"></i>布置对象</h3>
                <span>指定接收任务的专业、年级与班级</span>
              </header>
              <div class="t-tasks-form__row t-tasks-form__row--triple">
                <div class="t-tasks-form__field">
                  <label for="task-target-major">专业</label>
                  <select id="task-target-major" v-model="taskForm.targetMajor">
                    <option value="">请选择专业</option>
                    <option v-for="opt in targetMajorOptions" :key="opt" :value="opt">{{ opt }}</option>
                  </select>
                </div>
                <div class="t-tasks-form__field">
                  <label for="task-target-grade">年级</label>
                  <select id="task-target-grade" v-model="taskForm.targetGrade">
                    <option value="">请选择年级</option>
                    <option v-for="opt in targetGradeOptions" :key="opt" :value="opt">{{ opt }}</option>
                  </select>
                </div>
                <div class="t-tasks-form__field">
                  <label for="task-target-class">班级</label>
                  <select id="task-target-class" v-model="taskForm.targetClassName">
                    <option value="">请选择班级</option>
                    <option v-for="opt in targetClassNameOptions" :key="opt" :value="opt">{{ opt }}</option>
                  </select>
                </div>
              </div>
            </section>

            <section class="t-tasks-form__section">
              <header class="t-tasks-form__section-head">
                <h3><i class="fas fa-layer-group"></i>子任务结构</h3>
              </header>
              <div class="t-tasks-form__field">
                <label for="task-sub">
                  子任务列表
                  <span class="t-tasks-form__hint">拖拽调整顺序 · {{ orderedSubTasks.length }} 项</span>
                </label>
              <textarea
                id="task-sub"
                v-model="taskForm.subTaskText"
                rows="4"
                placeholder="每行一个子任务，或在下方列表中拖拽排序"
                @input="syncFromText"
              />
              <ul v-if="orderedSubTasks.length" class="t-drag-list">
                <li
                  v-for="(line, i) in orderedSubTasks"
                  :key="`${i}-${line}`"
                  class="t-drag-item"
                  :class="{
                    't-drag-item--ghost': dragIndex === i,
                    't-drag-item--over': dropIndex === i && dragIndex !== null && dragIndex !== i,
                  }"
                  draggable="true"
                  @dragstart="onDragStart(i)"
                  @dragend="onDragEnd"
                  @dragover.prevent="onDragOver(i)"
                  @drop.prevent="onDrop(i)"
                >
                  <span class="t-drag-item__num">{{ i + 1 }}</span>
                  <span class="t-drag-handle"><i class="fas fa-grip-lines"></i></span>
                  <span>{{ line }}</span>
                </li>
              </ul>
            </div>
            </section>
            </div>
          </div>

          <footer class="t-tasks-panel__actions">
            <button type="button" class="t-action-btn t-action-btn--save" @click="saveTask(false)">
              <i class="fas fa-save"></i>
              {{ isNewTask || !currentTask?.published ? '保存草稿' : '保存修改' }}
            </button>
            <button type="button" class="t-action-btn t-action-btn--ghost" @click="onCancelEdit">
              <i class="fas fa-times"></i> 取消
            </button>
            <button
              v-if="currentTaskId && !isNewTask"
              type="button"
              class="t-action-btn t-action-btn--danger"
              @click="deleteTask"
            >
              <i class="fas fa-trash-alt"></i> 删除任务
            </button>
          </footer>
        </template>
      </div>
    </div>

    <button v-if="showFab" type="button" class="t-fab" @click="onPrimaryAction">
      <i :class="primaryActionIcon"></i> {{ primaryActionLabel }}
    </button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useTeacherClassroom } from '../../composables/useTeacherClassroom';
import TeacherSubjectFilter from '../../components/teacher/TeacherSubjectFilter.vue';
import { SUBJECT_OPTIONS } from '../../constants/subjects';

const {
  taskForm,
  targetMajorOptions,
  targetGradeOptions,
  targetClassNameOptions,
  formatTaskTarget,
  tasks,
  tasksBySubjectFilter,
  subjectFilter,
  currentTaskId,
  isNewTask,
  isEditingTask,
  currentTask,
  saveTask,
  deleteTask,
  selectTask,
  startEditTask,
  cancelEditTask,
  startNewTask,
  beginCreateTask,
  cancelCreateTask,
  publishCurrent,
  openTasksPage,
  taskDraftPrompt,
  taskDraftAiResult,
  taskDraftAiLoading,
  fetchTaskDraftAssist,
  applyTaskDraftAssist,
  setTaskDraftPrompt,
  formatTaskDateTime,
} = useTeacherClassroom();

const subjectOptions = SUBJECT_OPTIONS;

const draftPromptChips = [
  '自主决策 Agent 设计，3 个子任务，2 周完成',
  '周学习报告：本周总结 + 问题与计划',
  '实验报告：实验记录 + 分析与结论',
];

const TASKS_PAGE_SIZE = 5;
const taskSearchQuery = ref('');
const taskPage = ref(1);
const descExpanded = ref(false);

const descNeedsToggle = computed(() => {
  const text = currentTask.value?.description || '';
  return text.length > 120 || text.split('\n').length > 4;
});

watch(currentTaskId, () => {
  descExpanded.value = false;
});

function taskSortKey(taskId) {
  const match = String(taskId).match(/(\d+)/);
  return match ? Number(match[1]) : 0;
}

const sortedTasks = computed(() =>
  [...tasks.value].sort((a, b) => taskSortKey(b.taskId) - taskSortKey(a.taskId)),
);

const filteredSidebarTasks = computed(() => {
  const q = taskSearchQuery.value.trim().toLowerCase();
  let list = sortedTasks.value.filter((t) =>
    tasksBySubjectFilter.value.some((ft) => ft.taskId === t.taskId),
  );
  if (!q) return list;
  return list.filter(
    (t) =>
      String(t.taskId).toLowerCase().includes(q) ||
      String(t.title || '').toLowerCase().includes(q) ||
      String(t.subject || '').toLowerCase().includes(q),
  );
});

const sidebarTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredSidebarTasks.value.length / TASKS_PAGE_SIZE)),
);

const paginatedSidebarTasks = computed(() => {
  const start = (taskPage.value - 1) * TASKS_PAGE_SIZE;
  return filteredSidebarTasks.value.slice(start, start + TASKS_PAGE_SIZE);
});

function syncTaskPageToSelection() {
  const id = currentTaskId.value;
  if (!id || isNewTask.value) return;
  const idx = filteredSidebarTasks.value.findIndex((t) => t.taskId === id);
  if (idx >= 0) {
    taskPage.value = Math.floor(idx / TASKS_PAGE_SIZE) + 1;
  }
}

watch(taskSearchQuery, () => {
  taskPage.value = 1;
  syncTaskPageToSelection();
});

watch(subjectFilter, () => {
  taskPage.value = 1;
  syncTaskPageToSelection();
});

watch(filteredSidebarTasks, () => {
  if (taskPage.value > sidebarTotalPages.value) {
    taskPage.value = sidebarTotalPages.value;
  }
});

watch(currentTaskId, () => {
  syncTaskPageToSelection();
});

async function onGenerateDraft() {
  await fetchTaskDraftAssist();
}

onMounted(async () => {
  await openTasksPage();
  syncTaskPageToSelection();
});

const orderedSubTasks = ref([]);
const dragIndex = ref(null);
const dropIndex = ref(null);

const isCreateHub = computed(() => isNewTask.value && !isEditingTask.value);
const isViewMode = computed(() => !isNewTask.value && !isEditingTask.value && !!currentTask.value);

const viewSubTasks = computed(() => currentTask.value?.subTasks || []);

const formTitle = computed(() => {
  if (isNewTask.value) return '填写新任务';
  return currentTask.value?.published ? '编辑任务' : '编辑草稿';
});

const formHint = computed(() => {
  if (isNewTask.value) return '完成后可保存草稿，或直接发布给学生';
  return currentTask.value?.published
    ? '修改后保存将更新任务内容'
    : '编辑完成后可保存草稿或直接发布';
});

const aiPanelHint = computed(() =>
  isNewTask.value
    ? '用一句话说明课程、主题与子任务数量，AI 将生成标题、说明与子任务，填入后你可再修改。'
    : '已载入当前任务摘要，可补充优化要求后点击生成，AI 将在现有内容基础上改进。',
);

const aiPromptPlaceholder = computed(() =>
  isNewTask.value
    ? '例如：大二软件工程，3 周项目「自主决策 Agent」，子任务：需求分析、方案设计、成果交付'
    : '当前任务摘要已填入，可补充「增加一个答辩子任务」等优化要求',
);

const generateDraftLabel = computed(() => {
  if (taskDraftAiLoading.value) return '生成中…';
  return isNewTask.value ? '生成草稿' : 'AI 优化';
});

const showFab = computed(() => isEditingTask.value);

const primaryActionLabel = computed(() => {
  if (isNewTask.value) return '发布任务';
  return currentTask.value?.published ? '保存修改' : '发布任务';
});

const primaryActionIcon = computed(() => {
  if (isNewTask.value || !currentTask.value?.published) return 'fas fa-paper-plane';
  return 'fas fa-save';
});

function onPrimaryAction() {
  if (isNewTask.value || !currentTask.value?.published) {
    saveTask(true);
  } else {
    saveTask(false);
  }
}

function onCancelEdit() {
  if (isNewTask.value) {
    cancelCreateTask();
  } else {
    cancelEditTask();
  }
}

function parseLines() {
  return taskForm.subTaskText
    .split('\n')
    .map((l) => l.trim())
    .filter(Boolean);
}

function syncFromText() {
  orderedSubTasks.value = parseLines();
}

function syncToText() {
  taskForm.subTaskText = orderedSubTasks.value.join('\n');
}

watch(
  () => taskForm.subTaskText,
  () => {
    if (!isEditingTask.value) return;
    const parsed = parseLines();
    if (parsed.join('\n') !== orderedSubTasks.value.join('\n')) {
      orderedSubTasks.value = parsed;
    }
  },
  { immediate: true },
);

function onDragStart(i) {
  dragIndex.value = i;
}

function onDragEnd() {
  dragIndex.value = null;
  dropIndex.value = null;
}

function onDragOver(i) {
  dropIndex.value = i;
}

function onDrop(i) {
  const from = dragIndex.value;
  if (from === null || from === i) {
    onDragEnd();
    return;
  }
  const list = [...orderedSubTasks.value];
  const [item] = list.splice(from, 1);
  list.splice(i, 0, item);
  orderedSubTasks.value = list;
  syncToText();
  onDragEnd();
}
</script>
