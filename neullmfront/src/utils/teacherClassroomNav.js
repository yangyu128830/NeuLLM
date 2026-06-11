/** 教师学情看板深度链接 */
export function buildTeacherProgressLink({ taskId, studentId, filter, view } = {}) {
  const params = new URLSearchParams();
  if (taskId) params.set('taskId', taskId);
  if (studentId) params.set('student', studentId);
  if (filter) params.set('filter', filter);
  if (view) params.set('view', view);
  const qs = params.toString();
  return qs ? `/teacher/progress?${qs}` : '/teacher/progress';
}
