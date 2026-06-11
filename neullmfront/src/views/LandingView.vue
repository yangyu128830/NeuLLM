<template>

  <div class="landing-page">

    <main id="home" class="landing-hero">

      <div class="landing-hero-copy">

        <span class="landing-badge">

          <span class="landing-badge-dot" aria-hidden="true"></span>

          多智能体 · 个性化学习

        </span>



        <h1 class="landing-title">

          课堂作业到个性复习

          <span class="landing-title-accent">一站式提升学习成效</span>

        </h1>



        <p class="landing-subtitle">

          智能答疑、作业辅导与学情分析协同运作，<br class="landing-br" />

          让每一次学习都有清晰路径与即时反馈

        </p>



        <div class="landing-actions">

          <router-link to="/register" class="landing-cta-primary">

            立即免费体验

            <i class="fas fa-arrow-right"></i>

          </router-link>

          <a href="#features" class="landing-cta-secondary" @click.prevent="scrollToFeatures">

            <span class="landing-play-icon"><i class="fas fa-play"></i></span>

            了解功能特色

          </a>

        </div>

        <p class="landing-highlights">
          课堂作业、智能答疑与学情分析协同，学习路径清晰、反馈即时
        </p>

        <div class="landing-stats-row">
          <div class="landing-stats" aria-label="产品亮点">
            <div v-for="stat in stats" :key="stat.label" class="landing-stat">
              <strong>{{ stat.value }}</strong>
              <span>{{ stat.label }}</span>
            </div>
          </div>

          <p class="landing-footer-link">
            还没准备好？
            <router-link to="/register">免费注册学生账号</router-link>
          </p>
        </div>

      </div>



      <aside class="landing-hero-visual" aria-hidden="true">

        <div class="landing-preview-glow"></div>

        <div class="landing-preview">

          <div class="landing-preview-header">

            <span class="landing-preview-dot"></span>

            <span class="landing-preview-dot"></span>

            <span class="landing-preview-dot"></span>

            <span class="landing-preview-title">智学伴 · 学习助手</span>

          </div>

          <div class="landing-preview-msg landing-preview-msg--user">

            <span class="landing-preview-avatar landing-preview-avatar--user"><i class="fas fa-user"></i></span>

            <div class="landing-preview-bubble landing-preview-bubble--user">帮我制定本周数学复习计划</div>

          </div>

          <div class="landing-preview-msg landing-preview-msg--ai">

            <span class="landing-preview-avatar landing-preview-avatar--ai"><i class="fas fa-robot"></i></span>

            <div class="landing-preview-bubble landing-preview-bubble--ai">

              已根据你的作业完成情况生成 3 天复习节奏，并设置课前提醒。

            </div>

          </div>

          <div class="landing-preview-chips">

            <span class="landing-preview-chip">智能答疑</span>

            <span class="landing-preview-chip">作业批改</span>

            <span class="landing-preview-chip">复习提醒</span>

          </div>

        </div>

        <div class="landing-float-card landing-float-card--1">

          <i class="fas fa-check-circle"></i>

          <span>作业已提交 · 待批改</span>

        </div>

        <div class="landing-float-card landing-float-card--2">

          <i class="fas fa-chart-line"></i>

          <span>学情周报已更新</span>

        </div>

      </aside>

    </main>



    <section id="features" class="landing-features" aria-label="功能特色">

      <div class="landing-features-head">

        <span class="landing-section-label">核心能力</span>

        <h2 class="landing-section-title">全方位覆盖学习全流程</h2>

        <p class="landing-section-desc">智能答疑 · 作业管理 · 学情分析，一站式学习陪伴</p>

      </div>

      <div class="landing-features-grid">

        <article v-for="item in features" :key="item.title" class="landing-feature-card">

          <div class="landing-feature-icon">

            <i :class="item.icon"></i>

          </div>

          <h3>{{ item.title }}</h3>

          <p>{{ item.desc }}</p>

        </article>

      </div>

    </section>



    <section class="landing-cta-band" aria-label="开始使用">

      <h2>现在开始，让 AI 成为你的学习搭档</h2>

      <p>注册即可体验智能答疑、作业管理与教师端学情看板</p>

      <router-link to="/register" class="landing-cta-primary">

        免费注册

        <i class="fas fa-arrow-right"></i>

      </router-link>

    </section>



    <footer class="landing-footer-bar">

      © 2026 智学伴 · 个性化 AI 学习助手

    </footer>

  </div>

</template>



<script setup>




const stats = [

  { value: '24/7', label: '智能答疑在线' },

  { value: '多 Agent', label: '协作式辅导' },

  { value: '一站式', label: '作业到学情' },

];



const features = [

  {

    icon: 'fas fa-comments',

    title: '智能答疑',

    desc: '多智能体协作，随时解答学习疑问，提供个性化辅导与复习建议。',

  },

  {

    icon: 'fas fa-book-open',

    title: '作业管理',

    desc: '作业提交、批改反馈与复习提醒，学习进度清晰可追溯。',

  },

  {

    icon: 'fas fa-chart-line',

    title: '学情分析',

    desc: '教师端学情看板与班级管理，助力精准教学与因材施教。',

  },

];



function scrollToFeatures() {

  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });

}

</script>

