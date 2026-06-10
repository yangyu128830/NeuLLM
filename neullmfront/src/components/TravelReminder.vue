<template>
  <div class="reminder-editor">
    <div class="reminder-header">
      <div class="reminder-title">
        <i class="fas fa-bell"></i>
        <span>学习日程提醒</span>
      </div>
      <div class="reminder-actions">
        <button type="button" class="reminder-button cancel-reminder-button" @click="cancelReminder">
          <i class="fas fa-times"></i> 取消
        </button>
        <button type="button" class="reminder-button save-reminder-button" @click="saveReminder">
          <i class="fas fa-save"></i> 保存提醒
        </button>
      </div>
    </div>

    <div class="reminder-content">
      <div class="reminder-field">
        <label><i class="fas fa-tag"></i> 提醒标题</label>
        <input type="text" v-model="reminderData.title" placeholder="例如：英语单词背诵" />
      </div>
      <div class="reminder-field">
        <label><i class="fas fa-map-marker-alt"></i> 地点</label>
        <input type="text" v-model="reminderData.location" placeholder="例如：图书馆 / 寝室（可选）" />
      </div>
      <div class="reminder-field">
        <label><i class="fas fa-calendar"></i> 日期和时间</label>
        <input type="datetime-local" v-model="reminderData.datetime" />
      </div>
      <div class="reminder-field">
        <label><i class="fas fa-info-circle"></i> 备注</label>
        <textarea v-model="reminderData.notes" placeholder="添加备注信息..."></textarea>
      </div>
      <div class="reminder-field">
        <label><i class="fas fa-bell"></i> 提前提醒时间</label>
        <select v-model="reminderData.advanceNotice">
          <option value="0">准时</option>
          <option value="5">提前5分钟</option>
          <option value="15">提前15分钟</option>
          <option value="30">提前30分钟</option>
          <option value="60">提前1小时</option>
          <option value="1440">提前1天</option>
        </select>
      </div>
    </div>

    <div v-if="reminderStatus" :class="['reminder-status', reminderStatus]">
      <span v-if="reminderStatus === 'saving'"><i class="fas fa-spinner fa-spin"></i> 保存中...</span>
      <span v-else-if="reminderStatus === 'error'"><i class="fas fa-exclamation-circle"></i> 请填写标题和时间</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  initialData: { type: Object, default: () => ({}) }
});

const emit = defineEmits(['cancel', 'save']);

const reminderData = ref({
  title: '',
  location: '',
  datetime: '',
  notes: '',
  advanceNotice: '30',
  ...props.initialData
});

const reminderStatus = ref(null);

const saveReminder = () => {
  if (!reminderData.value.title || !reminderData.value.datetime) {
    reminderStatus.value = 'error';
    setTimeout(() => { reminderStatus.value = null; }, 3000);
    return;
  }
  reminderStatus.value = 'saving';
  emit('save', { ...reminderData.value });
};

const cancelReminder = () => emit('cancel');
</script>

<style scoped>
.reminder-editor {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-top: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e7ff;
}
.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.reminder-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}
.reminder-actions { display: flex; gap: 12px; }
.reminder-button {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
}
.save-reminder-button { background: #27ae60; color: white; }
.cancel-reminder-button { background: #f5f7fa; color: #7f8c8d; }
.reminder-content { display: flex; flex-direction: column; gap: 15px; }
.reminder-field { display: flex; flex-direction: column; gap: 5px; }
.reminder-field label { font-weight: 500; color: #7f8c8d; font-size: 0.9rem; }
.reminder-field input,
.reminder-field textarea,
.reminder-field select {
  padding: 10px;
  border: 1px solid #e0e7ff;
  border-radius: 8px;
}
.reminder-field textarea { min-height: 80px; resize: vertical; }
.reminder-status {
  margin-top: 15px;
  padding: 10px;
  border-radius: 8px;
  text-align: center;
  font-size: 0.9rem;
}
.reminder-status.saving { background: #fffbeb; color: #d97706; }
.reminder-status.error { background: #fef2f2; color: #b91c1c; }
</style>
