/** 学生端：对话内一键跳转目标 */
export const STUDENT_APP_NAV_TARGETS = [
  {
    key: 'profile',
    to: '/profile',
    label: '个人中心',
    icon: 'fa-user',
    hint: '查看与编辑姓名、邮箱、学号等资料',
    patterns: [
      /个人中心/,
      /我的资料/,
      /我的信息/,
      /账号设置/,
      /个人设置/,
      /(?:打开|去|进入|看看|查看|跳转).{0,6}个人/,
      /^我的$/,
    ],
  },
  {
    key: 'messages',
    to: '/messages',
    label: '消息中心',
    icon: 'fa-bell',
    hint: '查看作业通知、系统提醒与未读消息',
    patterns: [
      /消息中心/,
      /站内信/,
      /(?:打开|去|进入|跳转).{0,8}消息(?:中心|页面)?/,
      /(?:打开|去|进入|跳转).{0,6}通知(?:中心|页面)?/,
    ],
  },
  {
    key: 'assignments',
    to: '/assignments',
    label: '我的作业',
    icon: 'fa-book-open',
    hint: '查看待交、已交与需重交的作业',
    patterns: [
      /我的作业/,
      /作业列表/,
      /作业中心/,
      /(?:打开|去|进入|跳转).{0,8}作业(?:中心|页面)?/,
    ],
  },
  {
    key: 'chat',
    to: '/chat',
    label: 'AI 助手',
    icon: 'fa-comments',
    hint: '回到智学伴对话页',
    patterns: [
      /(?:回到|打开|去).{0,6}(助手|对话|聊天)/,
      /^助手$/,
    ],
  },
];

/** 教师端：对话内一键跳转目标 */
export const TEACHER_APP_NAV_TARGETS = [
  {
    key: 'progress',
    to: '/teacher/progress',
    label: '学情看板',
    icon: 'fa-chart-line',
    hint: '查看作业提交进度与班级学情',
    patterns: [
      /学情看板/,
      /提交进度/,
      /进度看板/,
      /(?:打开|去|进入|看看|查看|跳转).{0,8}(看板|学情|进度)/,
      /^看板$/,
    ],
  },
  {
    key: 'grading',
    to: '/teacher/grading',
    label: '批改台',
    icon: 'fa-clipboard-check',
    hint: '批改学生提交、给出分数与评语',
    patterns: [
      /批改台/,
      /(?:打开|去|进入|看看|查看|跳转).{0,6}批改/,
      /^批改$/,
    ],
  },
  {
    key: 'tasks',
    to: '/teacher/tasks',
    label: '任务管理',
    icon: 'fa-plus-circle',
    hint: '创建、编辑与发布课堂作业',
    patterns: [
      /任务管理/,
      /作业管理/,
      /(?:打开|去|进入|看看|查看|跳转).{0,8}(任务|发布作业)/,
      /^任务$/,
    ],
  },
  {
    key: 'students',
    to: '/teacher/students',
    label: '学生列表',
    icon: 'fa-users',
    hint: '查看本班学生与联系方式',
    patterns: [
      /学生列表/,
      /本班学生/,
      /(?:打开|去|进入|看看|查看|跳转).{0,6}学生/,
    ],
  },
  {
    key: 'profile',
    to: '/teacher/profile',
    label: '个人中心',
    icon: 'fa-user',
    hint: '查看与编辑教师资料',
    patterns: [
      /个人中心/,
      /我的资料/,
      /(?:打开|去|进入|看看|查看|跳转).{0,6}个人/,
      /^我的$/,
    ],
  },
  {
    key: 'chat',
    to: '/teacher/chat',
    label: '教学 Agent',
    icon: 'fa-robot',
    hint: '回到教学 Agent 对话页',
    patterns: [
      /(?:回到|打开|去).{0,8}(agent|助手|对话)/i,
    ],
  },
];

/** 明显是在问内容而非跳转时，不走快捷导航 */
const CONTENT_QUERY_HINT =
  /怎么|如何|为什么|多少|介绍|说明|解释|整理|复习|计划|提醒|发邮件|@/;

/**
 * @param {string} text
 * @param {{ teacherMode?: boolean }} [opts]
 * @returns {(typeof STUDENT_APP_NAV_TARGETS)[number] | null}
 */
export function detectAppNavIntent(text, opts = {}) {
  const raw = (text || '').trim();
  if (!raw || raw.length > 80) return null;
  if (CONTENT_QUERY_HINT.test(raw)) return null;

  const normalized = raw.replace(/\s/g, '');
  const targets = opts.teacherMode ? TEACHER_APP_NAV_TARGETS : STUDENT_APP_NAV_TARGETS;

  for (const target of targets) {
    if (target.patterns.some((re) => re.test(normalized) || re.test(raw))) {
      return target;
    }
  }
  return null;
}
