<template>
  <div class="cf-result classroom-confirm-card">
    <div class="cf-result-icon">
      <i class="fas fa-clipboard-check"></i>
    </div>
    <div class="cf-result-body">
      <div class="cf-result-title">{{ isPublish ? '确认发布作业' : '确认创建作业' }}</div>
      <p class="cf-result-hint">{{ summary || '请核对以下内容，确认后将写入班级任务库。' }}</p>

      <template v-if="isPublish">
        <div class="cf-result-row">
          <span class="rk">任务编号</span>
          <span class="rv">{{ preview.taskId }}</span>
        </div>
        <div class="cf-result-row">
          <span class="rk">标题</span>
          <span class="rv">{{ preview.title }}</span>
        </div>
      </template>

      <template v-else>
        <div class="cf-field">
          <label>标题</label>
          <input v-model="form.title" type="text" />
        </div>
        <div class="cf-field">
          <label>说明</label>
          <textarea v-model="form.description" rows="2" />
        </div>
        <label class="cf-check">
          <input v-model="form.publish" type="checkbox" />
          创建后立即发布（学生可见）
        </label>
        <div v-if="form.subTasks.length" class="cf-subtasks">
          <div class="cf-subtasks-head">子任务（{{ form.subTasks.length }}）</div>
          <div v-for="(st, i) in form.subTasks" :key="i" class="cf-subtask-row">
            <input v-model="st.title" type="text" placeholder="子任务标题" />
            <input v-model="st.description" type="text" placeholder="要求说明" />
          </div>
        </div>
      </template>

      <p v-if="error" class="cf-error">{{ error }}</p>
      <div class="cf-actions">
        <button type="button" class="cf-btn cf-btn--primary" :disabled="busy" @click="confirm">
          <i class="fas fa-check"></i> {{ isPublish ? '确认发布' : '确认创建' }}
        </button>
        <button type="button" class="cf-btn cf-btn--ghost" :disabled="busy" @click="$emit('cancel')">
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import classroomApi from '@/services/classroomApi';

const props = defineProps({
  kind: { type: String, default: 'classroom_task_preview' },
  summary: { type: String, default: '' },
  preview: { type: Object, default: () => ({}) },
});

const emit = defineEmits(['confirmed', 'cancel']);

const isPublish = props.kind === 'classroom_publish_preview';
const busy = ref(false);
const error = ref('');

const form = reactive({
  title: '',
  description: '',
  publish: true,
  subTasks: [],
});

watch(
  () => props.preview,
  (p) => {
    if (!p || isPublish) return;
    form.title = p.title || '';
    form.description = p.description || '';
    form.publish = p.publish !== false;
    const subs = Array.isArray(p.subTasks) ? p.subTasks : [];
    form.subTasks = subs.map((s) => ({
      title: s.title || '',
      description: s.description || s.requirements || '',
    }));
  },
  { immediate: true, deep: true },
);

async function confirm() {
  error.value = '';
  busy.value = true;
  try {
    if (isPublish) {
      const data = await classroomApi.publishTask(props.preview.taskId);
      emit('confirmed', { kind: 'classroom_task_published', data });
    } else {
      const data = await classroomApi.createTask({
        title: form.title,
        description: form.description,
        publish: form.publish,
        subTasks: form.subTasks,
      });
      emit('confirmed', { kind: 'classroom_task_created', data });
    }
  } catch (e) {
    error.value = e.message || '操作失败';
  } finally {
    busy.value = false;
  }
}
</script>

<style scoped>
.classroom-confirm-card {
  text-align: left;
}
.cf-field {
  margin-top: 10px;
}
.cf-field label {
  display: block;
  font-size: 0.78rem;
  opacity: 0.8;
  margin-bottom: 4px;
}
.cf-field input,
.cf-field textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid rgba(13, 148, 136, 0.25);
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.88rem;
}
.cf-check {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  font-size: 0.86rem;
}
.cf-subtasks {
  margin-top: 12px;
}
.cf-subtasks-head {
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 6px;
}
.cf-subtask-row {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 6px;
  margin-bottom: 6px;
}
.cf-subtask-row input {
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 0.84rem;
}
.cf-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}
.cf-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.86rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-family: inherit;
}
.cf-btn--primary {
  background: #0d9488;
  color: #fff;
}
.cf-btn--primary:disabled {
  opacity: 0.6;
}
.cf-btn--ghost {
  background: transparent;
  border: 1px solid rgba(0, 0, 0, 0.15);
  color: inherit;
}
.cf-error {
  color: #b91c1c;
  font-size: 0.84rem;
  margin-top: 8px;
}
</style>
