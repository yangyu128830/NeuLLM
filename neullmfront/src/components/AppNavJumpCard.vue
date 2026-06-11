<template>
  <div class="app-nav-jump">
    <div class="app-nav-jump__icon" aria-hidden="true">
      <i class="fas" :class="target.icon"></i>
    </div>
    <div class="app-nav-jump__body">
      <div class="app-nav-jump__title">一键前往{{ target.label }}</div>
      <p class="app-nav-jump__hint">{{ target.hint }}</p>
      <router-link :to="target.to" class="app-nav-jump__btn">
        <i class="fas fa-arrow-right"></i>
        打开{{ target.label }}
      </router-link>
    </div>
  </div>
</template>

<script setup>
defineProps({
  target: {
    type: Object,
    required: true,
  },
});
</script>

<style scoped>
.app-nav-jump {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(14, 165, 233, 0.06));
  border: 1px solid rgba(99, 102, 241, 0.18);
}

.app-nav-jump__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(99, 102, 241, 0.12);
  color: #4f46e5;
  flex-shrink: 0;
}

.app-nav-jump__title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.app-nav-jump__hint {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.5;
  color: #64748b;
}

.app-nav-jump__btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border-radius: 999px;
  background: #4f46e5;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.15s ease, transform 0.15s ease;
}

.app-nav-jump__btn:hover {
  background: #4338ca;
  transform: translateY(-1px);
}
</style>
