<template>
  <div class="tbc-root" :class="scene.bgClass">
    <div class="tbc-deco" aria-hidden="true">
      <span class="deco-float" v-for="(e, i) in scene.floatEmojis" :key="i" :style="{ '--d': i }">{{ e }}</span>
    </div>

    <div v-if="title" class="tbc-head">
      <div class="tbc-icon-wrap">
        <i class="fas fa-suitcase-rolling"></i>
      </div>
      <div class="tbc-head-text">
        <div class="tbc-title">{{ title }}</div>
        <div class="tbc-hint">{{ hint }}</div>
      </div>
    </div>
    <div v-else class="tbc-head tbc-head-compact">
      <div class="tbc-icon-wrap">
        <i class="fas fa-cloud-sun"></i>
      </div>
      <div class="tbc-head-text">
        <div class="tbc-title">出行天气</div>
        <div class="tbc-hint">{{ hint }}</div>
      </div>
    </div>

    <!-- 天气主图 + 辅助图标条 -->
    <div class="tbc-visual">
      <div class="tbc-hero" :class="scene.heroTone">
        <div class="tbc-hero-icon">
          <i :class="scene.mainIcon"></i>
        </div>
        <div class="tbc-hero-meta">
          <span v-if="scene.label" class="tbc-weather-label">{{ scene.label }}</span>
          <span class="tbc-hero-sub">实况参考 · 精简暖心</span>
        </div>
      </div>
      <div class="tbc-thumb-row">
        <div v-for="(t, idx) in scene.thumbs" :key="idx" class="tbc-thumb" :title="t.title">
          <i :class="t.icon"></i>
          <span>{{ t.text }}</span>
        </div>
      </div>
    </div>

    <div class="tbc-body">
      <div class="tbc-content">{{ content }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  title: { type: String, default: '' },
  hint: { type: String, default: '天气 · 穿衣 · 小贴士' },
  /** 高德等接口原文，用于在流式正文未出全时匹配天气图标 */
  rawWeather: { type: String, default: '' },
  content: { type: String, default: '' }
});

/** 根据高德/模型常见中文描述推断展示用图标（无网络请求） */
function inferScene(text) {
  const t = text || '';

  let mainIcon = 'fas fa-cloud-sun';
  let label = '';
  let bgClass = 'scene-default';
  let heroTone = 'tone-soft';
  let floatEmojis = ['☁️', '✨'];

  if (/雷|阵雨|雷阵雨|暴雨|大雨|中雨|小雨|雨雪|降雨|下雨/.test(t)) {
    mainIcon = 'fas fa-cloud-showers-heavy';
    label = /雷/.test(t) ? '雷雨' : '有雨';
    bgClass = 'scene-rain';
    heroTone = 'tone-cool';
    floatEmojis = ['🌧️', '💧', '☂️'];
  } else if (/雪|降雪/.test(t)) {
    mainIcon = 'fas fa-snowflake';
    label = '雪';
    bgClass = 'scene-snow';
    heroTone = 'tone-frost';
    floatEmojis = ['❄️', '⛄', '✨'];
  } else if (/雾|霾|沙尘/.test(t)) {
    mainIcon = 'fas fa-smog';
    label = /霾/.test(t) ? '霾' : '雾';
    bgClass = 'scene-haze';
    heroTone = 'tone-muted';
    floatEmojis = ['🌫️', '😷'];
  } else if (/晴|少云/.test(t) && !/多云|阴/.test(t)) {
    mainIcon = 'fas fa-sun';
    label = '晴好';
    bgClass = 'scene-clear';
    heroTone = 'tone-warm';
    floatEmojis = ['☀️', '🌤️', '✨'];
  } else if (/多云|阴/.test(t)) {
    mainIcon = 'fas fa-cloud';
    label = /阴/.test(t) ? '阴天' : '多云';
    bgClass = 'scene-cloud';
    heroTone = 'tone-soft';
    floatEmojis = ['☁️', '🌥️'];
  } else if (/风|级/.test(t)) {
    mainIcon = 'fas fa-wind';
    label = '有风';
    floatEmojis = ['🍃', '☁️'];
  }

  const thumbs = [
    { icon: 'fas fa-cloud-sun', text: '天气', title: '实况与体感' },
    { icon: 'fas fa-tshirt', text: '穿衣', title: '穿搭建议' },
    { icon: 'fas fa-lightbulb', text: '注意', title: '出行小贴士' }
  ];

  return { mainIcon, label, bgClass, heroTone, floatEmojis, thumbs };
}

const scene = computed(() =>
  inferScene(`${props.rawWeather} ${props.content} ${props.title}`)
);
</script>

<style scoped>
.tbc-root {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(102, 126, 234, 0.22);
  box-shadow: 0 10px 32px rgba(31, 38, 135, 0.14);
  transition: background 0.35s ease;
}

.scene-default {
  background: linear-gradient(165deg, #ffffff 0%, #eef2ff 55%, #f8f9ff 100%);
}
.scene-clear {
  background: linear-gradient(165deg, #fffdf5 0%, #fff8e7 40%, #f0f7ff 100%);
}
.scene-cloud {
  background: linear-gradient(165deg, #f7f9fc 0%, #e8edf5 50%, #f4f6ff 100%);
}
.scene-rain {
  background: linear-gradient(165deg, #f0f6ff 0%, #e3ecfa 45%, #dde8f7 100%);
}
.scene-snow {
  background: linear-gradient(165deg, #fbfdff 0%, #eef6ff 50%, #e8f4fc 100%);
}
.scene-haze {
  background: linear-gradient(165deg, #f5f5f4 0%, #ebeae8 50%, #eef0f3 100%);
}

.tbc-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.deco-float {
  position: absolute;
  font-size: 1.6rem;
  opacity: 0.14;
  animation: floaty 8s ease-in-out infinite;
  animation-delay: calc(var(--d, 0) * 1.4s);
}

.deco-float:nth-child(1) {
  right: 8%;
  top: 18%;
}
.deco-float:nth-child(2) {
  right: 22%;
  top: 42%;
  font-size: 1.1rem;
}
.deco-float:nth-child(3) {
  right: 14%;
  top: 62%;
  font-size: 1.25rem;
}

@keyframes floaty {
  0%,
  100% {
    transform: translateY(0) rotate(-4deg);
  }
  50% {
    transform: translateY(-10px) rotate(4deg);
  }
}

.tbc-head,
.tbc-visual,
.tbc-body {
  position: relative;
  z-index: 1;
}

.tbc-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: linear-gradient(120deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.tbc-head-compact {
  padding: 14px 18px;
}

.tbc-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  flex-shrink: 0;
}

.tbc-head-text {
  min-width: 0;
}

.tbc-title {
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.tbc-hint {
  margin-top: 4px;
  font-size: 0.82rem;
  opacity: 0.92;
}

/* 天气主图区 */
.tbc-visual {
  padding: 14px 16px 10px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.tbc-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 14px;
}

.tone-warm {
  background: linear-gradient(100deg, rgba(255, 193, 7, 0.2), rgba(255, 255, 255, 0.65));
}
.tone-soft {
  background: linear-gradient(100deg, rgba(102, 126, 234, 0.12), rgba(255, 255, 255, 0.7));
}
.tone-cool {
  background: linear-gradient(100deg, rgba(52, 152, 219, 0.18), rgba(255, 255, 255, 0.72));
}
.tone-frost {
  background: linear-gradient(100deg, rgba(174, 214, 241, 0.35), rgba(255, 255, 255, 0.75));
}
.tone-muted {
  background: linear-gradient(100deg, rgba(149, 165, 166, 0.2), rgba(255, 255, 255, 0.7));
}

.tbc-hero-icon {
  width: 76px;
  height: 76px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.35rem;
  flex-shrink: 0;
  background: linear-gradient(145deg, #fff 0%, rgba(255, 255, 255, 0.85) 100%);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.18);
  color: #5a67d8;
}

.scene-clear .tbc-hero-icon {
  color: #f39c12;
}
.scene-rain .tbc-hero-icon {
  color: #3498db;
}
.scene-snow .tbc-hero-icon {
  color: #74b9ff;
}
.scene-haze .tbc-hero-icon {
  color: #95a5a6;
}
.scene-cloud .tbc-hero-icon {
  color: #7f8c8d;
}

.tbc-hero-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tbc-weather-label {
  font-size: 1.05rem;
  font-weight: 700;
  color: #2c3e50;
}

.tbc-hero-sub {
  font-size: 0.78rem;
  color: #7f8c8d;
}

.tbc-thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tbc-thumb {
  flex: 1;
  min-width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 6px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(102, 126, 234, 0.12);
  font-size: 0.72rem;
  color: #5d6d7e;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tbc-thumb i {
  font-size: 1.1rem;
  color: #667eea;
}

.tbc-body {
  padding: 14px 18px 18px;
}

.tbc-content {
  font-size: 0.95rem;
  line-height: 1.65;
  color: #2c3e50;
  white-space: pre-wrap;
}
</style>
