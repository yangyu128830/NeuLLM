<template>
  <div class="teacher-app classroom-theme">
    <aside class="teacher-sidebar">
      <div class="sidebar-brand">
        <span class="sidebar-logo"><i class="fas fa-graduation-cap"></i></span>
        <div class="sidebar-brand__text">
          <strong>智学伴</strong>
          <span>教师工作台</span>
        </div>
        <span class="sidebar-brand__chip">AI 赋能</span>
      </div>

      <nav class="sidebar-nav" aria-label="教师后台导航">
        <router-link to="/teacher/chat" class="sidebar-link">
          <i class="fas fa-robot"></i>
          <span>Agent 对话</span>
        </router-link>

        <div class="sidebar-nav__divider" aria-hidden="true"></div>

        <p class="sidebar-nav__label">课堂管理</p>

        <router-link to="/teacher/progress" class="sidebar-link">
          <i class="fas fa-chart-line"></i>
          <span>学情看板</span>
        </router-link>
        <router-link to="/teacher/grading" class="sidebar-link">
          <i class="fas fa-clipboard-check"></i>
          <span>批改作业</span>
          <em v-if="pendingCount" class="sidebar-badge">{{ pendingCount }}</em>
        </router-link>
        <router-link to="/teacher/tasks" class="sidebar-link">
          <i class="fas fa-plus-circle"></i>
          <span>发布任务</span>
        </router-link>
        <router-link to="/teacher/students" class="sidebar-link">
          <i class="fas fa-users"></i>
          <span>学生列表</span>
        </router-link>

        <div class="sidebar-nav__divider" aria-hidden="true"></div>

        <p class="sidebar-nav__label">账户</p>

        <router-link to="/teacher/profile" class="sidebar-link">
          <i class="fas fa-user-circle"></i>
          <span>个人信息</span>
        </router-link>
      </nav>

      <div class="sidebar-foot">
        <button type="button" class="sidebar-link sidebar-link--soft sidebar-btn" @click="logout">
          <i class="fas fa-sign-out-alt"></i>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <div class="teacher-body">
      <main class="teacher-main">
        <router-view v-slot="{ Component }">
          <Transition name="t-page-fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </main>
    </div>

    <Transition name="toast-fade">
      <p v-if="toast" :class="['toast', toastOk ? 'ok' : 'fail']">{{ toast }}</p>
    </Transition>

    <TeacherBottomNav :active="teacherNavActive" :pending-count="pendingCount" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { provideTeacherClassroom } from '../composables/useTeacherClassroom';
import TeacherBottomNav from '../components/teacher/TeacherBottomNav.vue';
import { teacherNavKeyFromRoute } from '../constants/teacherNav';
import '../assets/teacher-shell.css';
import '../assets/teacher-premium.css';
import '../assets/teacher-mobile.css';

const route = useRoute();
const classroom = provideTeacherClassroom();
const { toast, toastOk, logout, submissions } = classroom;

const pendingCount = computed(
  () => submissions.value.filter((s) => s.status === 'SUBMITTED').length,
);

const teacherNavActive = computed(() => teacherNavKeyFromRoute(route.name));
</script>
