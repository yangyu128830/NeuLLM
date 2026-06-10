<template>
  <div class="cf-result classroom-doc-card">
    <div class="cf-result-icon"><i class="fas fa-file-import"></i></div>
    <div class="cf-result-body">
      <div class="cf-result-title">从文档 / 粘贴建任务</div>
      <p class="cf-result-hint">支持课堂任务书格式（一、二、子任务：）或上传 txt/docx/pdf。</p>

      <div class="doc-tabs">
        <button type="button" :class="{ on: tab === 'paste' }" @click="tab = 'paste'">粘贴文本</button>
        <button type="button" :class="{ on: tab === 'file' }" @click="tab = 'file'">上传文件</button>
        <button type="button" :class="{ on: tab === 'template' }" @click="loadTemplates">内置模板</button>
      </div>

      <textarea
        v-if="tab === 'paste'"
        v-model="pasteText"
        class="doc-textarea"
        rows="8"
        placeholder="粘贴任务说明，例如含「一、需求分析」与「提交要求」的文档…"
      />
      <div v-else-if="tab === 'file'" class="doc-file">
        <input type="file" accept=".txt,.doc,.docx,.pdf" @change="onFile" />
      </div>
      <div v-else-if="tab === 'template'" class="doc-templates">
        <button
          v-for="t in templates"
          :key="t.id"
          type="button"
          class="tpl-chip"
          @click="applyTemplate(t)"
        >
          {{ t.title }}
        </button>
        <p v-if="!templates.length" class="cf-result-hint">加载模板中…</p>
      </div>

      <p v-if="error" class="cf-error">{{ error }}</p>
      <div class="cf-actions">
        <button type="button" class="cf-btn cf-btn--primary" :disabled="busy" @click="parseAndPreview">
          <i class="fas fa-magic"></i> 解析并预览
        </button>
        <button type="button" class="cf-btn cf-btn--ghost" @click="$emit('cancel')">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import classroomApi from '@/services/classroomApi';

const emit = defineEmits(['parsed', 'cancel']);

const tab = ref('paste');
const pasteText = ref('');
const fileRef = ref(null);
const templates = ref([]);
const busy = ref(false);
const error = ref('');

async function loadTemplates() {
  tab.value = 'template';
  try {
    templates.value = await classroomApi.listTaskTemplates();
  } catch (e) {
    error.value = e.message || '加载模板失败';
  }
}

function onFile(e) {
  fileRef.value = e.target.files?.[0] || null;
}

function applyTemplate(t) {
  emit('parsed', {
    title: t.title,
    description: t.description,
    publish: true,
    subTasks: (t.subTasks || []).map((s) => ({
      title: s.title,
      description: s.description,
    })),
  });
}

async function parseAndPreview() {
  error.value = '';
  busy.value = true;
  try {
    let parsed;
    if (tab.value === 'file' && fileRef.value) {
      parsed = await classroomApi.parseTaskDocument(fileRef.value);
    } else if (tab.value === 'paste' && pasteText.value.trim()) {
      parsed = await classroomApi.parseTaskText(pasteText.value.trim());
    } else {
      error.value = '请粘贴文本或选择文件';
      return;
    }
    const subTasks = (parsed.subTasks || []).map((s) => ({
      title: s.title,
      description: s.requirements || s.description || '',
    }));
    emit('parsed', {
      title: parsed.title,
      description: parsed.description || '',
      publish: true,
      subTasks,
    });
  } catch (e) {
    error.value = e.message || '解析失败';
  } finally {
    busy.value = false;
  }
}
</script>

<style scoped>
.classroom-doc-card {
  text-align: left;
}
.doc-tabs {
  display: flex;
  gap: 6px;
  margin: 10px 0;
  flex-wrap: wrap;
}
.doc-tabs button {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(13, 148, 136, 0.3);
  background: transparent;
  cursor: pointer;
  font-size: 0.82rem;
}
.doc-tabs button.on {
  background: rgba(13, 148, 136, 0.15);
  font-weight: 600;
}
.doc-textarea {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  font-family: inherit;
  font-size: 0.86rem;
  resize: vertical;
}
.doc-file input {
  margin-top: 8px;
}
.doc-templates {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tpl-chip {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid rgba(13, 148, 136, 0.35);
  background: rgba(13, 148, 136, 0.08);
  cursor: pointer;
  font-size: 0.84rem;
}
.cf-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
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
.cf-btn--ghost {
  background: transparent;
  border: 1px solid rgba(0, 0, 0, 0.15);
}
.cf-error {
  color: #b91c1c;
  font-size: 0.84rem;
  margin-top: 8px;
}
</style>
