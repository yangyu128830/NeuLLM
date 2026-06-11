<template>
  <nav class="mtb mtb--indigo" aria-label="教师端主导航">
    <div class="mtb-inner">
      <router-link
        v-for="item in TEACHER_NAV_ITEMS"
        :key="item.key"
        :to="item.to"
        class="mtb-item"
        :class="{ 'mtb-item--active': active === item.key }"
      >
        <span class="mtb-icon">
          <i class="fas" :class="item.icon"></i>
          <em
            v-if="item.key === 'grading' && pendingCount > 0"
            class="mtb-badge"
          >{{ pendingCount > 99 ? '99+' : pendingCount }}</em>
        </span>
        <span class="mtb-label">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { TEACHER_NAV_ITEMS } from '@/constants/teacherNav';

defineProps({
  active: { type: String, default: '' },
  pendingCount: { type: Number, default: 0 },
});
</script>
