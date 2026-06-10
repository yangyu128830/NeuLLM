<template>
  <header class="public-nav" :class="{ scrolled: isScrolled }">
    <router-link to="/" class="public-nav-brand">
      <span class="public-nav-icon">
        <i class="fas fa-graduation-cap"></i>
      </span>
      <span class="public-nav-name">智学伴</span>
    </router-link>

    <nav class="public-nav-links" aria-label="主导航">
      <router-link to="/" class="public-nav-link" :class="{ active: route.name === 'Landing' }">主页</router-link>
      <a
        v-if="route.name === 'Landing'"
        href="#features"
        class="public-nav-link"
        @click.prevent="scrollToFeatures"
      >功能特色</a>
      <router-link to="/login" class="public-nav-link" :class="{ active: route.name === 'Login' }">登录</router-link>
      <router-link to="/register" class="public-nav-link" :class="{ active: route.name === 'Register' }">注册</router-link>
    </nav>

    <div class="public-nav-actions">
      <button
        type="button"
        class="public-nav-toggle"
        :aria-expanded="menuOpen"
        aria-label="打开菜单"
        @click="menuOpen = !menuOpen"
      >
        <i class="fas fa-bars"></i>
      </button>
      <router-link
        v-if="route.name !== 'Login'"
        to="/login"
        class="public-nav-cta"
        :class="{ 'public-nav-cta--outline': route.name === 'Landing' }"
      >
        <i class="fas fa-sign-in-alt"></i>
        <span>立即登录</span>
      </router-link>
      <router-link
        v-else
        to="/register"
        class="public-nav-cta"
      >
        <i class="fas fa-user-plus"></i>
        <span>免费注册</span>
      </router-link>
    </div>

    <div class="public-nav-mobile" :class="{ open: menuOpen }">
      <router-link to="/" @click="menuOpen = false">主页</router-link>
      <a
        v-if="route.name === 'Landing'"
        href="#features"
        @click.prevent="scrollToFeatures(); menuOpen = false"
      >功能特色</a>
      <router-link to="/login" @click="menuOpen = false">登录</router-link>
      <router-link to="/register" @click="menuOpen = false">注册</router-link>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const menuOpen = ref(false);
const isScrolled = ref(false);

function scrollToFeatures() {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });
}

function onScroll() {
  isScrolled.value = window.scrollY > 12;
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }));
onUnmounted(() => window.removeEventListener('scroll', onScroll));
</script>
