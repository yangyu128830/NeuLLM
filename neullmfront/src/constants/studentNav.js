/** 学生端顶栏胶囊导航（不含助手） */
export const STUDENT_MAIN_NAV_ITEMS = [
  { key: 'assignments', to: '/assignments', icon: 'fa-book-open', label: '作业' },
  { key: 'messages', to: '/messages', icon: 'fa-bell', label: '消息' },
  { key: 'profile', to: '/profile', icon: 'fa-user', label: '我的' }
];

/** 助手入口（顶栏单独展示） */
export const STUDENT_CHAT_NAV = {
  key: 'chat',
  to: '/chat',
  icon: 'fa-comments',
  label: '助手'
};

/** 学生端底部 Tab 导航（含助手） */
export const STUDENT_NAV_ITEMS = [
  STUDENT_MAIN_NAV_ITEMS[0],
  STUDENT_MAIN_NAV_ITEMS[1],
  STUDENT_CHAT_NAV,
  STUDENT_MAIN_NAV_ITEMS[2]
];
