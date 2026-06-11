/** 教师端底部 Tab 导航（手机端） */
export const TEACHER_NAV_ITEMS = [
  { key: 'chat', to: '/teacher/chat', icon: 'fa-robot', label: 'Agent' },
  { key: 'progress', to: '/teacher/progress', icon: 'fa-chart-line', label: '看板' },
  { key: 'grading', to: '/teacher/grading', icon: 'fa-clipboard-check', label: '批改' },
  { key: 'tasks', to: '/teacher/tasks', icon: 'fa-plus-circle', label: '任务' },
  { key: 'profile', to: '/teacher/profile', icon: 'fa-user', label: '我的' },
];

export function teacherNavKeyFromRoute(routeName) {
  const map = {
    TeacherChat: 'chat',
    TeacherProgress: 'progress',
    TeacherGrading: 'grading',
    TeacherTasks: 'tasks',
    TeacherProfile: 'profile',
    TeacherStudents: 'profile',
  };
  return map[routeName] || '';
}
