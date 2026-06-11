<template>
  <div class="public-layout">
    <div class="public-bg" aria-hidden="true">
      <span class="public-orb public-orb--1"></span>
      <span class="public-orb public-orb--2"></span>
    </div>
    <PublicNav />
    <div class="public-page-stage">
      <router-view v-slot="{ Component }">
        <transition name="public-page">
          <component :is="Component" class="public-page-view" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import PublicNav from '../components/PublicNav.vue';
import '../assets/public-shell.css';
import '../assets/landing-shell.css';
import '../assets/auth-shell.css';
</script>
