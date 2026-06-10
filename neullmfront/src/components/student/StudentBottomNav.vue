<template>
  <nav class="sbn" aria-label="学生端主导航">
    <router-link
      v-for="item in STUDENT_NAV_ITEMS"
      :key="item.key"
      :to="item.to"
      class="sbn-item"
      :class="{ 'sbn-item--active': active === item.key }"
    >
      <span class="sbn-icon-wrap">
        <i class="fas" :class="item.icon"></i>
        <em
          v-if="item.key === 'messages' && unreadCount > 0"
          class="sbn-badge"
        >{{ unreadCount > 99 ? '99+' : unreadCount }}</em>
      </span>
      <span class="sbn-label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { STUDENT_NAV_ITEMS } from '@/constants/studentNav';

defineProps({
  active: { type: String, default: '' },
  unreadCount: { type: Number, default: 0 }
});
</script>

<style scoped>
.sbn {
  display: none;
}

@media (max-width: 767px) {
  .sbn {
    display: flex;
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 300;
    align-items: stretch;
    justify-content: space-around;
    gap: 2px;
    padding: 6px 8px calc(6px + env(safe-area-inset-bottom, 0px));
    background: rgba(255, 255, 255, 0.94);
    border-top: 1px solid rgba(15, 23, 42, 0.08);
    box-shadow: 0 -4px 24px rgba(15, 23, 42, 0.08);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }

  .sbn-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 3px;
    min-height: 48px;
    padding: 4px 2px;
    border-radius: 12px;
    text-decoration: none;
    color: #64748b;
    -webkit-tap-highlight-color: transparent;
    transition: color 0.15s, background 0.15s;
  }

  .sbn-item--active {
    color: #0f766e;
    background: rgba(20, 184, 166, 0.1);
  }

  .sbn-icon-wrap {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    font-size: 1.05rem;
  }

  .sbn-label {
    font-size: 0.68rem;
    font-weight: 600;
    line-height: 1.1;
  }

  .sbn-badge {
    position: absolute;
    top: -4px;
    right: -10px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 999px;
    background: #ef4444;
    color: #fff;
    font-size: 0.58rem;
    font-weight: 800;
    font-style: normal;
    line-height: 16px;
    text-align: center;
    box-shadow: 0 0 0 2px #fff;
  }
}
</style>
