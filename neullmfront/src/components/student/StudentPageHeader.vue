<template>
  <header class="sph">
    <div class="sph-inner page-inner">
      <div class="sph-brand">
        <span class="sph-icon" aria-hidden="true"><i class="fas" :class="icon"></i></span>
        <div class="sph-copy">
          <p class="sph-tag">智学伴 · 学生端</p>
          <h1>{{ title }}</h1>
          <p v-if="subtitle" class="sph-subtitle">{{ subtitle }}</p>
        </div>
      </div>

      <div class="sph-actions">
        <slot name="actions" />

        <nav class="sph-nav sph-nav--desktop" aria-label="学生端导航">
          <router-link
            v-for="item in mainNavItems"
            :key="item.key"
            :to="item.to"
            class="sph-nav-item"
            :class="{ 'sph-nav-item--active': active === item.key }"
          >
            <i class="fas" :class="item.icon"></i>
            <span>{{ item.label }}</span>
            <em
              v-if="item.key === 'messages' && unreadCount > 0"
              class="sph-nav-badge"
            >{{ unreadCount > 99 ? '99+' : unreadCount }}</em>
          </router-link>
        </nav>

        <router-link
          :to="chatNav.to"
          class="sph-chat sph-nav--desktop"
          :class="{ 'sph-chat--active': active === chatNav.key }"
        >
          <i class="fas" :class="chatNav.icon"></i>
          <span>{{ chatNav.label }}</span>
        </router-link>

        <button type="button" class="sph-logout" title="退出登录" @click="$emit('logout')">
          <i class="fas fa-sign-out-alt" aria-hidden="true"></i>
          <span class="sph-logout-text">退出</span>
        </button>
      </div>
    </div>

    <StudentBottomNav :active="active" :unread-count="unreadCount" />
  </header>
</template>

<script setup>
import StudentBottomNav from '@/components/student/StudentBottomNav.vue';
import { STUDENT_MAIN_NAV_ITEMS, STUDENT_CHAT_NAV } from '@/constants/studentNav';

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  icon: { type: String, default: 'fa-graduation-cap' },
  active: { type: String, default: '' },
  unreadCount: { type: Number, default: 0 }
});

defineEmits(['logout']);

const mainNavItems = STUDENT_MAIN_NAV_ITEMS;
const chatNav = STUDENT_CHAT_NAV;
</script>

<style scoped>
@import '../../assets/classroom-theme.css';

/* 左侧品牌区：图二亮色顶栏 */
.sph {
  flex-shrink: 0;
  background: var(--gradient-brand);
  color: #fff;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);
}

.sph-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 16px;
  padding-bottom: 16px;
}

.sph-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1 1 220px;
}

.sph-icon {
  flex-shrink: 0;
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

.sph-tag {
  margin: 0 0 2px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  opacity: 0.85;
}

.sph-copy h1 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: #fff;
  line-height: 1.25;
}

.sph-subtitle {
  margin: 6px 0 0;
  font-size: 0.85rem;
  opacity: 0.92;
  line-height: 1.45;
  max-width: 520px;
}

.sph-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

/* 右侧导航：图三胶囊按钮 */
.sph-nav {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
}

.sph-nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 0.84rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
  text-decoration: none;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  white-space: nowrap;
}

.sph-nav-item i {
  font-size: 0.82rem;
}

.sph-nav-item:hover:not(.sph-nav-item--active) {
  background: rgba(255, 255, 255, 0.1);
}

.sph-nav-item--active {
  color: #0f766e;
  background: #fff;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.14);
}

.sph-nav-item--active i {
  color: #0d9488;
}

.sph-nav-badge {
  min-width: 17px;
  height: 17px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 0.62rem;
  font-weight: 800;
  font-style: normal;
  line-height: 17px;
  text-align: center;
}

.sph-nav-item--active .sph-nav-badge {
  box-shadow: 0 0 0 2px #fff;
}

/* 助手入口：独立于胶囊导航 */
.sph-chat {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 0.84rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
  text-decoration: none;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.28);
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  white-space: nowrap;
}

.sph-chat i {
  font-size: 0.82rem;
}

.sph-chat:hover:not(.sph-chat--active) {
  background: rgba(255, 255, 255, 0.22);
}

.sph-chat--active {
  color: #0f766e;
  background: #fff;
  border-color: #fff;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.14);
}

.sph-chat--active i {
  color: #0d9488;
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
  font-size: 0.84rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  border: none;
  text-decoration: none;
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

.sph-logout {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  padding: 9px 16px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: transparent;
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
  -webkit-tap-highlight-color: transparent;
}

.sph-logout:hover {
  background: rgba(255, 255, 255, 0.12);
}

@media (max-width: 900px) {
  .sph-subtitle {
    max-width: none;
  }
}

@media (max-width: 767px) {
  .sph {
    background: linear-gradient(135deg, #047857 0%, #0d9488 42%, #14b8a6 78%, #2dd4bf 100%);
    box-shadow: 0 4px 24px rgba(13, 148, 136, 0.18);
  }

  .sph-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: calc(10px + env(safe-area-inset-top, 0px));
    padding-bottom: 10px;
    gap: 10px;
  }

  .sph-brand {
    flex: 1;
    min-width: 0;
  }

  .sph-actions {
    flex-shrink: 0;
    width: auto;
    overflow: visible;
    flex-wrap: nowrap;
    gap: 0;
  }

  .sph-logout {
    width: 36px;
    height: 36px;
    min-height: 36px;
    padding: 0;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.12);
    border-color: rgba(255, 255, 255, 0.22);
  }

  .sph-logout i {
    font-size: 0.95rem;
    color: #fff;
  }

  .sph-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    font-size: 0.95rem;
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .sph-tag {
    display: none;
  }

  .sph-copy h1 {
    font-size: 1rem;
    font-weight: 800;
    letter-spacing: -0.02em;
  }

  .sph-subtitle {
    display: none;
  }

  .sph-nav--desktop {
    display: none;
  }

  .sph-chat.sph-nav--desktop {
    display: none;
  }

  .sph-logout-text {
    display: none;
  }

  :slotted(.btn-ghost),
  :slotted(.btn-outline) {
    display: none;
  }
}
</style>
