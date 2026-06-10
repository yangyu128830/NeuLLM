<template>
  <div class="rrp-root">
    <div class="rrp-mesh" aria-hidden="true" />
    <div class="rrp-frame">
      <header class="rrp-head">
        <div class="rrp-icon-wrap">
          <i :class="variant === 'plan' ? 'fas fa-calendar-check' : 'fas fa-magic'"></i>
        </div>
        <div class="rrp-head-text">
          <h3 class="rrp-title">{{ headTitle }}</h3>
          <p class="rrp-sub">
            <template v-if="streaming">{{ streamingSub }}</template>
            <template v-else-if="isErrorMode">这次没生成成功，点一下再试就好</template>
            <template v-else>{{ idleSub }}</template>
          </p>
        </div>
        <div v-if="streaming" class="rrp-pulse" aria-hidden="true" />
        <div v-else-if="!isErrorMode" class="rrp-head-badge" aria-hidden="true">智学伴</div>
      </header>

      <div class="rrp-body">
        <!-- 加载中：后端为整篇生成（非流式 Kimi），应答前只有骨架；长文生成完会分块推送正文 -->
        <div v-if="streaming && !content" class="rrp-skeleton-wrap">
          <p class="rrp-wait-hint">
            模型正在整篇撰写中，通常需要几十秒到一两分钟；请稍候，生成完成后会立刻出现在下方～
          </p>
          <div class="rrp-skeleton">
            <div class="rrp-sk-line" />
            <div class="rrp-sk-line short" />
            <div class="rrp-sk-line" />
            <div class="rrp-sk-line med" />
          </div>
        </div>

        <!-- 失败：柔和提示区 + 重试 -->
        <div v-else-if="isErrorMode" class="rrp-fail">
          <div class="rrp-fail-icon">
            <i class="fas fa-exclamation-circle"></i>
          </div>
          <p class="rrp-fail-text">{{ content || '稍等一下再试，我会继续帮你排～' }}</p>
          <button type="button" class="rrp-retry" @click="$emit('retry')">
            <i class="fas fa-redo-alt"></i>
            再试一次生成
          </button>
        </div>

        <!-- 成功：分块正文 -->
        <template v-else>
          <div v-if="sections.length" class="rrp-sections">
            <article v-for="(sec, i) in sections" :key="i" class="rrp-sec">
              <div class="rrp-sec-accent" aria-hidden="true" />
              <div class="rrp-sec-num">{{ i + 1 }}</div>
              <div class="rrp-sec-main">
                <h4 class="rrp-sec-title">{{ sec.title }}</h4>
                <div class="rrp-sec-body rrp-rich" v-html="richBody(sec.body)" />
              </div>
            </article>
          </div>
          <div v-else class="rrp-prose rrp-rich" v-html="richBody(content)" />
        </template>
      </div>

      <footer v-if="!streaming && !isErrorMode && content" class="rrp-foot">
        <span class="rrp-foot-dot" />
        <span class="rrp-foot-text">计划可按实际情况微调，贵在坚持～</span>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  content: { type: String, default: '' },
  streaming: { type: Boolean, default: false },
  /** 生成失败且可携带同一提示词重试 */
  canRetry: { type: Boolean, default: false },
  /** rhythm：复习节奏表单后的生成；plan：日常「复习计划」类自然语言 */
  variant: {
    type: String,
    default: 'rhythm',
    validator: (v) => v === 'rhythm' || v === 'plan'
  }
});

defineEmits(['retry']);

const isErrorMode = computed(() => props.canRetry === true);

const headTitle = computed(() =>
  props.variant === 'plan' ? '你的复习计划' : '你的复习节奏'
);

const idleSub = computed(() =>
  props.variant === 'plan'
    ? '从目标到日程 · 照着推进 · 可随时调整'
    : '按天拆解 · 照表执行 · 随时可微调'
);

const streamingSub = computed(() =>
  props.variant === 'plan' ? '正在为你写复习计划…' : '正在为你排版…'
);

function escapeHtml(s) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/** 轻量渲染：换行 + **加粗**（模型正文已转义后安全 v-html） */
function richBody(s) {
  if (!s) return '';
  const esc = escapeHtml(s);
  return esc
    .replace(/\*\*(.+?)\*\*/g, '<strong class="rrp-strong">$1</strong>')
    .replace(/\n/g, '<br/>');
}

/** 将 ## / ### 标题拆成卡片区块；无标题时整块展示 */
const sections = computed(() => {
  const raw = props.content || '';
  const lines = raw.split('\n');
  const out = [];
  let cur = null;
  const preamble = [];
  let sawHeader = false;

  for (const line of lines) {
    const m = line.match(/^#{2,4}\s+(.+)$/);
    if (m) {
      sawHeader = true;
      if (!cur && preamble.length) {
        const pre = preamble.join('\n').trim();
        if (pre) out.push({ title: '概要', bodyLines: preamble.slice() });
        preamble.length = 0;
      } else if (cur) {
        out.push(cur);
      }
      cur = { title: m[1].trim(), bodyLines: [] };
    } else if (cur) {
      cur.bodyLines.push(line);
    } else {
      preamble.push(line);
    }
  }
  if (cur) out.push(cur);

  if (!sawHeader) return [];

  return out
    .map((s) => ({
      title: s.title,
      body: (Array.isArray(s.bodyLines) ? s.bodyLines.join('\n') : s.body || '').trim()
    }))
    .filter((s) => s.title || s.body);
});
</script>

<style scoped>
.rrp-root {
  position: relative;
  max-width: 640px;
  border-radius: 22px;
  padding: 1px;
  background: linear-gradient(
    135deg,
    #c4b5fd 0%,
    #a5b4fc 25%,
    #7dd3fc 55%,
    #99f6e4 100%
  );
  box-shadow:
    0 24px 56px rgba(99, 102, 241, 0.18),
    0 0 0 1px rgba(255, 255, 255, 0.45) inset;
}

.rrp-mesh {
  pointer-events: none;
  position: absolute;
  inset: 0;
  border-radius: 21px;
  opacity: 0.55;
  background:
    radial-gradient(ellipse 120% 80% at 10% -10%, rgba(255, 255, 255, 0.7), transparent 55%),
    radial-gradient(circle at 92% 8%, rgba(167, 243, 208, 0.35), transparent 42%),
    radial-gradient(circle at 50% 100%, rgba(196, 181, 253, 0.25), transparent 50%);
}

.rrp-frame {
  position: relative;
  border-radius: 21px;
  overflow: hidden;
  background: linear-gradient(180deg, #fffefb 0%, #f8fafc 48%, #f1f5f9 100%);
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    'PingFang SC',
    'Hiragino Sans GB',
    'Microsoft YaHei',
    sans-serif;
  font-feature-settings: 'kern' 1;
  -webkit-font-smoothing: antialiased;
}

.rrp-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.95);
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.92) 0%, rgba(241, 245, 249, 0.65) 100%);
}

.rrp-icon-wrap {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(145deg, #6366f1 0%, #4f46e5 45%, #2563eb 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
  box-shadow:
    0 8px 22px rgba(79, 70, 229, 0.35),
    0 1px 0 rgba(255, 255, 255, 0.25) inset;
}

.rrp-head-text {
  flex: 1;
  min-width: 0;
}

.rrp-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.04em;
  line-height: 1.25;
}

.rrp-sub {
  margin: 6px 0 0;
  font-size: 0.84rem;
  color: #64748b;
  line-height: 1.45;
}

.rrp-pulse {
  flex-shrink: 0;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  animation: rrp-pulse 1.15s ease-in-out infinite;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.2);
}

@keyframes rrp-pulse {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.rrp-head-badge {
  flex-shrink: 0;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #94a3b8;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.rrp-body {
  padding: 18px 20px 22px;
  max-height: min(60vh, 540px);
  overflow-y: auto;
}

.rrp-skeleton-wrap {
  padding: 4px 0 10px;
}

.rrp-wait-hint {
  margin: 0 0 16px;
  font-size: 0.88rem;
  line-height: 1.65;
  color: #64748b;
}

.rrp-skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.rrp-sk-line {
  height: 12px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e2e8f0 0%, #f1f5f9 40%, #e2e8f0 80%);
  background-size: 200% 100%;
  animation: rrp-shimmer 1.4s ease-in-out infinite;
}

.rrp-sk-line.short {
  width: 55%;
}
.rrp-sk-line.med {
  width: 78%;
}

@keyframes rrp-shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: -100% 0;
  }
}

.rrp-fail {
  text-align: center;
  padding: 28px 22px 26px;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff7ed 0%, #fffbeb 100%);
  border: 1px solid rgba(251, 191, 36, 0.45);
  box-shadow: 0 10px 28px rgba(245, 158, 11, 0.08);
}

.rrp-fail-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fed7aa, #fdba74);
  color: #9a3412;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  box-shadow: 0 6px 16px rgba(234, 88, 12, 0.15);
}

.rrp-fail-text {
  margin: 0 auto 20px;
  font-size: 0.95rem;
  line-height: 1.65;
  color: #78350f;
  max-width: 28rem;
}

.rrp-retry {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 26px;
  border: none;
  border-radius: 999px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  box-shadow: 0 10px 28px rgba(79, 70, 229, 0.38);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.rrp-retry:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(79, 70, 229, 0.45);
}

.rrp-retry:active {
  transform: translateY(0);
}

.rrp-sections {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rrp-sec {
  position: relative;
  display: flex;
  gap: 14px;
  padding: 18px 18px 18px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow:
    0 2px 8px rgba(15, 23, 42, 0.03),
    0 12px 28px rgba(99, 102, 241, 0.06);
}

.rrp-sec-accent {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 0 4px 4px 0;
  background: linear-gradient(180deg, #818cf8, #38bdf8, #34d399);
  opacity: 0.85;
}

.rrp-sec-num {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(145deg, #eef2ff, #e0f2fe);
  color: #3730a3;
  font-size: 0.8rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 3px;
  margin-left: 6px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 1;
}

.rrp-sec-main {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.rrp-sec-title {
  margin: 0 0 12px;
  font-size: 1.06rem;
  font-weight: 750;
  color: #0f172a;
  line-height: 1.4;
  letter-spacing: 0.02em;
}

.rrp-sec-body {
  font-size: 0.93rem;
  line-height: 1.82;
  color: #475569;
}

.rrp-prose {
  font-size: 0.93rem;
  line-height: 1.82;
  color: #334155;
  padding: 6px 4px 2px;
}

.rrp-rich :deep(.rrp-strong) {
  color: #1e293b;
  font-weight: 650;
}

.rrp-foot {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 20px 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.5), rgba(241, 245, 249, 0.85));
}

.rrp-foot-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7185, #f472b6);
  flex-shrink: 0;
}

.rrp-foot-text {
  font-size: 0.84rem;
  color: #64748b;
  font-weight: 500;
}
</style>
