<template>
  <div class="public-layout">
    <div class="public-bg" aria-hidden="true">
      <span class="public-orb public-orb--1"></span>
      <span class="public-orb public-orb--2"></span>
    </div>
    <PublicNav />
    <router-view v-slot="{ Component }">
      <transition name="public-page" mode="out-in">
        <component :is="Component" class="public-page-view" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import PublicNav from '../components/PublicNav.vue';
import '../assets/public-shell.css';
</script>
