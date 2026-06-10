<template>
  <div class="auth-page" :class="loginRole === 'TEACHER' ? 'mode-teacher' : 'mode-student'">
    <div class="auth-shell">
      <aside class="auth-brand">
        <span class="auth-role-pill">
          {{ loginRole === 'TEACHER' ? '教师工作台' : '学生学习空间' }}
        </span>
        <div class="auth-brand-badge">
          <i :class="loginRole === 'TEACHER' ? 'fas fa-chalkboard-teacher' : 'fas fa-user-graduate'"></i>
        </div>
        <h1 class="auth-brand-title">智学伴</h1>
        <p class="auth-brand-tagline">
          {{ loginRole === 'TEACHER' ? '班级教学与学情管理一站到位' : 'AI 驱动的个性化学习伙伴' }}
        </p>
        <ul v-if="loginRole === 'STUDENT'" class="auth-brand-list">
          <li><i class="fas fa-comments"></i> 智能答疑与学习陪伴</li>
          <li><i class="fas fa-book-open"></i> 作业提交与复习提醒</li>
          <li><i class="fas fa-map-marked-alt"></i> 出行规划与学习节奏</li>
        </ul>
        <ul v-else class="auth-brand-list">
          <li><i class="fas fa-users"></i> 班级与学生档案管理</li>
          <li><i class="fas fa-tasks"></i> 作业发布与批改跟踪</li>
          <li><i class="fas fa-chart-line"></i> 学情分析与教学助手</li>
        </ul>
      </aside>

      <main class="auth-panel">
        <div class="role-tabs" role="tablist" aria-label="选择登录端">
          <button
            type="button"
            role="tab"
            :aria-selected="loginRole === 'STUDENT'"
            class="role-tab"
            :class="{ active: loginRole === 'STUDENT' }"
            @click="setLoginRole('STUDENT')"
          >
            <i class="fas fa-user-graduate"></i>
            <span>学生端</span>
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="loginRole === 'TEACHER'"
            class="role-tab"
            :class="{ active: loginRole === 'TEACHER' }"
            @click="setLoginRole('TEACHER')"
          >
            <i class="fas fa-chalkboard"></i>
            <span>教师端</span>
          </button>
          <span class="role-tab-indicator" :class="loginRole === 'TEACHER' ? 'to-teacher' : 'to-student'"></span>
        </div>

        <p class="role-hint">
          <template v-if="loginRole === 'STUDENT'">
            <i class="fas fa-star"></i>
            登录后进入学习助手、作业与出行规划
          </template>
          <template v-else>
            <i class="fas fa-briefcase"></i>
            登录后进入班级管理与教学后台
          </template>
        </p>

        <form @submit.prevent="onSubmit">
          <div class="auth-field">
            <label for="username">用户名</label>
            <div class="auth-input-wrap">
              <i class="fas fa-user auth-input-icon"></i>
              <input
                id="username"
                v-model="form.username"
                autocomplete="username"
                :placeholder="loginRole === 'TEACHER' ? 'teacher' : 'student1'"
                required
              />
            </div>
          </div>

          <div class="auth-field">
            <label for="password">密码</label>
            <div class="auth-input-wrap">
              <i class="fas fa-lock auth-input-icon"></i>
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="请输入密码"
                required
              />
              <button
                type="button"
                class="auth-pwd-toggle"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                @click="showPassword = !showPassword"
              >
                <i class="fas" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"></i>
              </button>
            </div>
          </div>

          <label class="auth-remember">
            <input v-model="rememberMe" type="checkbox" />
            <span>记住账号和密码</span>
          </label>

          <p v-if="error" class="auth-error" role="alert">
            <i class="fas fa-exclamation-circle"></i>
            {{ error }}
          </p>

          <button type="submit" class="auth-submit" :disabled="loading">
            <span v-if="loading"><i class="fas fa-spinner fa-spin"></i> 登录中…</span>
            <span v-else>
              <i :class="loginRole === 'TEACHER' ? 'fas fa-door-open' : 'fas fa-sign-in-alt'"></i>
              {{ loginRole === 'TEACHER' ? '进入教师后台' : '进入学习空间' }}
            </span>
          </button>
        </form>

        <p class="auth-footer">
          还没有账号？
          <router-link :to="{ path: '/register', query: { role: loginRole } }">
            注册{{ loginRole === 'TEACHER' ? '教师' : '学生' }}账号
          </router-link>
        </p>

        <div class="demo-box">
          <p class="demo-title"><i class="fas fa-flask"></i> 演示账号</p>
          <div class="demo-grid">
            <div class="demo-item" :class="{ muted: loginRole !== 'TEACHER' }">
              <span class="demo-label">教师</span>
              <code>teacher</code>
            </div>
            <div class="demo-item" :class="{ muted: loginRole !== 'STUDENT' }">
              <span class="demo-label">学生</span>
              <code>student1 ~ student4</code>
            </div>
          </div>
          <p class="demo-pwd">密码均为 <code>123456</code></p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import authApi from '../services/authApi';
import { setAuth, getRememberedLogin, saveRememberedLogin } from '../stores/auth';
import '../assets/auth-shell.css';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');

function initialRole() {
  const q = String(route.query.role || '').toUpperCase();
  return q === 'TEACHER' ? 'TEACHER' : 'STUDENT';
}

const loginRole = ref(initialRole());
const form = reactive({ username: '', password: '' });
const rememberMe = ref(false);
const showPassword = ref(false);

function applyRemembered(role) {
  const saved = getRememberedLogin(role);
  rememberMe.value = saved.remember;
  form.username = saved.username;
  form.password = saved.password;
}

function setLoginRole(role) {
  loginRole.value = role;
  error.value = '';
  applyRemembered(role);
}

onMounted(() => applyRemembered(loginRole.value));

async function onSubmit() {
  loading.value = true;
  error.value = '';
  try {
    const res = await authApi.login({
      username: form.username,
      password: form.password,
      role: loginRole.value,
    });
    const user = res.data.data;
    if (user.role !== loginRole.value) {
      error.value =
        loginRole.value === 'TEACHER'
          ? '该账号不是教师账号，请切换到学生端登录'
          : '该账号不是学生账号，请切换到教师端登录';
      return;
    }
    setAuth(user.token, user);
    saveRememberedLogin(
      loginRole.value,
      rememberMe.value,
      form.username.trim(),
      form.password
    );
    await router.replace(user.role === 'TEACHER' ? '/teacher/chat' : '/chat');
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '登录失败';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-submit i {
  margin-right: 6px;
}

.auth-error i {
  margin-top: 2px;
  flex-shrink: 0;
}

.auth-remember {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 2px 0 18px;
  font-size: 0.88rem;
  color: rgba(15, 23, 42, 0.72);
  cursor: pointer;
  user-select: none;
}

.auth-remember input[type='checkbox'] {
  width: 16px;
  height: 16px;
  accent-color: var(--auth-accent, #0d9488);
  cursor: pointer;
}

.auth-pwd-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: rgba(15, 23, 42, 0.45);
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.auth-pwd-toggle:hover {
  color: rgba(15, 23, 42, 0.75);
}

.auth-input-wrap:has(.auth-pwd-toggle) input {
  padding-right: 42px;
}
</style>
