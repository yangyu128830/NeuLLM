<template>
  <header class="topbar">
    <div class="topbar-inner page-inner">
      <div class="brand">
        <span class="brand-icon"><i class="fas" :class="icon"></i></span>
        <div>
          <p class="brand-tag">智学伴 · 学生端</p>
          <h1>{{ title }}</h1>
          <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
        </div>
      </div>
      <div class="header-actions">
        <slot name="actions" />
        <router-link to="/messages" class="btn-ghost nav-with-badge" :class="{ active: active === 'messages' }">
          <i class="fas fa-bell"></i> 消息
          <span v-if="unreadCount > 0" class="nav-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </router-link>
        <router-link to="/assignments" class="btn-ghost" :class="{ active: active === 'assignments' }">
          <i class="fas fa-book-open"></i> 作业
        </router-link>
        <router-link to="/profile" class="btn-ghost" :class="{ active: active === 'profile' }">
          <i class="fas fa-user-circle"></i> 个人中心
        </router-link>
        <router-link to="/chat" class="btn-ghost" :class="{ active: active === 'chat' }">
          <i class="fas fa-comments"></i> 学习助手
        </router-link>
        <button type="button" class="btn-outline" @click="$emit('logout')">退出</button>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  icon: { type: String, default: 'fa-graduation-cap' },
  active: { type: String, default: '' },
  unreadCount: { type: Number, default: 0 }
});

defineEmits(['logout']);
</script>

<style scoped>
@import '../../assets/classroom-theme.css';

.topbar {
  flex-shrink: 0;
  background: var(--gradient-brand);
  color: #fff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
}

.topbar-inner {
  padding-top: 16px;
  padding-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.brand-tag {
  margin: 0 0 2px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  opacity: 0.85;
}

h1 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: #fff;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 0.85rem;
  opacity: 0.92;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.btn-ghost,
.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.btn-ghost {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #fff;
}

.btn-ghost:hover,
.btn-ghost.active {
  background: rgba(255, 255, 255, 0.22);
}

.btn-outline {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-with-badge {
  position: relative;
}

.nav-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
}

:slotted(.slot-btn) {
  font-family: inherit;
}

:slotted(.btn-ghost),
:slotted(.btn-outline) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

:slotted(.btn-ghost) {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #fff;
}

:slotted(.btn-outline) {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
}

:slotted(.btn-ghost:hover),
:slotted(.btn-outline:hover) {
  background: rgba(255, 255, 255, 0.18);
}

:slotted(.btn-ghost:disabled),
:slotted(.btn-outline:disabled) {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
