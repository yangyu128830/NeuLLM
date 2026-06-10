<template>
  <div class="auth-page" :class="form.role === 'TEACHER' ? 'mode-teacher' : 'mode-student'">
    <div class="auth-shell">
      <aside class="auth-brand">
        <span class="auth-role-pill">
          {{ form.role === 'TEACHER' ? '教师工作台' : '学生学习空间' }}
        </span>
        <div class="auth-brand-badge">
          <i :class="form.role === 'TEACHER' ? 'fas fa-chalkboard-teacher' : 'fas fa-user-graduate'"></i>
        </div>
        <h1 class="auth-brand-title">智学伴</h1>
        <p class="auth-brand-tagline">
          {{ form.role === 'TEACHER' ? '注册后即可管理班级与教学事务' : '注册后即可使用 AI 学习助手' }}
        </p>
        <ul v-if="form.role === 'STUDENT'" class="auth-brand-list">
          <li><i class="fas fa-comments"></i> 智能答疑与学习陪伴</li>
          <li><i class="fas fa-book-open"></i> 作业提交与复习提醒</li>
          <li><i class="fas fa-id-card"></i> 学号绑定与班级归属</li>
        </ul>
        <ul v-else class="auth-brand-list">
          <li><i class="fas fa-users"></i> 创建与管理班级</li>
          <li><i class="fas fa-tasks"></i> 发布作业与查看提交</li>
          <li><i class="fas fa-robot"></i> 教学智能体辅助备课</li>
        </ul>
      </aside>

      <main class="auth-panel">
        <div class="role-tabs" role="tablist" aria-label="选择注册身份">
          <button
            type="button"
            role="tab"
            :aria-selected="form.role === 'STUDENT'"
            class="role-tab"
            :class="{ active: form.role === 'STUDENT' }"
            @click="setRole('STUDENT')"
          >
            <i class="fas fa-user-graduate"></i>
            <span>学生注册</span>
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="form.role === 'TEACHER'"
            class="role-tab"
            :class="{ active: form.role === 'TEACHER' }"
            @click="setRole('TEACHER')"
          >
            <i class="fas fa-chalkboard"></i>
            <span>教师注册</span>
          </button>
          <span class="role-tab-indicator" :class="form.role === 'TEACHER' ? 'to-teacher' : 'to-student'"></span>
        </div>

        <h2>注册{{ form.role === 'TEACHER' ? '教师' : '学生' }}账号</h2>

        <form @submit.prevent="onSubmit">
          <div class="auth-field">
            <label>用户名</label>
            <input v-model="form.username" :placeholder="form.role === 'TEACHER' ? 'teacher_new' : 'student_new'" required />
          </div>
          <div class="auth-field">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="至少 6 位" required />
          </div>
          <div class="auth-field">
            <label>显示名</label>
            <input v-model="form.displayName" :placeholder="form.role === 'TEACHER' ? '张老师' : '小明'" required />
          </div>
          <div v-if="form.role === 'STUDENT'" class="auth-field">
            <label>学号</label>
            <input v-model="form.studentNo" placeholder="如 2024001" required />
          </div>
          <p v-if="error" class="auth-error">{{ error }}</p>
          <button type="submit" class="auth-submit" :disabled="loading">
            {{ loading ? '注册中…' : (form.role === 'TEACHER' ? '注册并进入教师后台' : '注册并进入学习空间') }}
          </button>
        </form>
        <p class="auth-footer">
          <router-link :to="{ path: '/login', query: { role: form.role } }">返回登录</router-link>
        </p>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import authApi from '../services/authApi';
import { setAuth } from '../stores/auth';
import '../assets/auth-shell.css';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const initialRole = String(route.query.role || '').toUpperCase() === 'TEACHER' ? 'TEACHER' : 'STUDENT';
const form = reactive({
  username: '',
  password: '',
  role: initialRole,
  displayName: '',
  studentNo: '',
  classId: 'CLASS-01',
});

function setRole(role) {
  form.role = role;
  error.value = '';
  if (role === 'TEACHER') {
    form.studentNo = '';
  }
}

async function onSubmit() {
  loading.value = true;
  error.value = '';
  try {
    const res = await authApi.register(form);
    const user = res.data.data;
    setAuth(user.token, user);
    await router.replace(user.role === 'TEACHER' ? '/teacher/chat' : '/chat');
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '注册失败';
  } finally {
    loading.value = false;
  }
}
</script>
