<template>
  <div class="hotel-booking-container">
    <div class="booking-header">
      <div class="hotel-info">
        <i class="fas fa-hotel"></i>
        <div class="hotel-details">
          <h3>预订酒店</h3>
          <p>{{ hotel.hotelName || hotel.name }}</p>
        </div>
      </div>
      <button class="close-btn" @click="$emit('cancel')">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <div class="booking-body">
      <!-- 酒店信息卡片 -->
      <div class="selected-hotel" v-if="hotel.address">
        <div class="hotel-name-row">
          <i class="fas fa-building"></i>
          <span>{{ hotel.hotelName || hotel.name }}</span>
        </div>
        <div class="hotel-address-row">
          <i class="fas fa-map-marker-alt"></i>
          <span>{{ hotel.address }}</span>
        </div>
      </div>

      <!-- 预订表单 -->
      <div class="booking-form">
        <h4><i class="fas fa-calendar-alt"></i> 入住信息</h4>

        <div class="form-row">
          <div class="form-group">
            <label>入住日期</label>
            <input type="date" v-model="form.checkInDate" />
          </div>
          <div class="form-group">
            <label>退房日期</label>
            <input type="date" v-model="form.checkOutDate" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>入住人数</label>
            <select v-model="form.guests">
              <option value="1">1人</option>
              <option value="2">2人</option>
              <option value="3">3人</option>
              <option value="4">4人</option>
              <option value="5">5人及以上</option>
            </select>
          </div>
        </div>

        <h4><i class="fas fa-user"></i> 联系人信息</h4>

        <div class="form-row">
          <div class="form-group">
            <label>姓名 <span class="required">*</span></label>
            <input type="text" v-model="form.contactName" placeholder="请输入联系人姓名" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>手机号 <span class="required">*</span></label>
            <input type="tel" v-model="form.contactPhone" placeholder="请输入手机号码" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>邮箱（选填）</label>
            <input type="email" v-model="form.contactEmail" placeholder="请输入邮箱地址" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>特殊需求（选填）</label>
            <textarea v-model="form.specialRequests" placeholder="如：大床房、禁烟房、靠近电梯等"></textarea>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="booking-actions">
        <button class="cancel-btn" @click="$emit('cancel')">
          <i class="fas fa-times"></i> 取消
        </button>
        <button class="confirm-btn" @click="confirmBooking" :disabled="!isFormValid">
          <i class="fas fa-check"></i> 确认预订
        </button>
      </div>

      <!-- 状态提示 -->
      <div v-if="bookingStatus" :class="['booking-status', bookingStatus]">
        <i v-if="bookingStatus === 'success'" class="fas fa-check-circle"></i>
        <i v-else-if="bookingStatus === 'error'" class="fas fa-exclamation-circle"></i>
        <i v-else class="fas fa-spinner fa-spin"></i>
        <span>{{ statusMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  hotel: {
    type: Object,
    required: true
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['cancel', 'book']);

const form = ref({
  checkInDate: props.initialData.checkInDate || '',
  checkOutDate: props.initialData.checkOutDate || '',
  guests: props.initialData.guests || '2',
  contactName: props.initialData.contactName || '',
  contactPhone: props.initialData.contactPhone || '',
  contactEmail: props.initialData.contactEmail || '',
  specialRequests: props.initialData.specialRequests || ''
});

const bookingStatus = ref(null);
const statusMessage = ref('');

const isFormValid = computed(() => {
  return form.value.contactName.trim() !== '' &&
         form.value.contactPhone.trim() !== '' &&
         form.value.checkInDate !== '' &&
         form.value.checkOutDate !== '';
});

const confirmBooking = () => {
  if (!isFormValid.value) return;

  bookingStatus.value = 'loading';
  statusMessage.value = '正在提交预订...';

  emit('book', {
    hotelName: props.hotel.hotelName || props.hotel.name,
    checkInDate: form.value.checkInDate,
    checkOutDate: form.value.checkOutDate,
    guests: parseInt(form.value.guests),
    contactName: form.value.contactName.trim(),
    contactPhone: form.value.contactPhone.trim(),
    contactEmail: form.value.contactEmail.trim(),
    specialRequests: form.value.specialRequests.trim()
  });
};
</script>

<style scoped>
.hotel-booking-container {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  margin-top: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.booking-header {
  background: linear-gradient(135deg, #27ae60, #2ecc71);
  color: white;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotel-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hotel-info i {
  font-size: 1.8rem;
  opacity: 0.9;
}

.hotel-details h3 {
  margin: 0 0 4px 0;
  font-size: 1.1rem;
}

.hotel-details p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.9;
}

.close-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255,255,255,0.3);
}

.booking-body {
  padding: 20px;
}

.selected-hotel {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.hotel-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.hotel-name-row i {
  color: #3498db;
}

.hotel-address-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #888;
}

.hotel-address-row i {
  color: #e74c3c;
}

.booking-form h4 {
  color: #2c3e50;
  font-size: 1rem;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e8e8e8;
}

.booking-form h4 i {
  color: #3498db;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  color: #555;
  font-weight: 500;
}

.required {
  color: #e74c3c;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.booking-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f5f6f7;
  border: 1px solid #ddd;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
  color: #333;
}

.confirm-btn {
  background: linear-gradient(135deg, #27ae60, #2ecc71);
  border: none;
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
}

.confirm-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.booking-status {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
}

.booking-status.loading {
  background: #fffbeb;
  color: #d97706;
}

.booking-status.success {
  background: #ecfdf5;
  color: #059669;
}

.booking-status.error {
  background: #fef2f2;
  color: #dc2626;
}
</style>
