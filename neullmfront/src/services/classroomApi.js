import http from './http';

function unwrap(res) {
  const body = res.data;
  if (body && body.ok === false) {
    throw new Error(body.message || '请求失败');
  }
  return body?.data ?? body;
}

export default {
  listStudents() {
    return http.get('/api/classroom/students').then(unwrap);
  },
  listStudentScopeOptions() {
    return http.get('/api/classroom/students/scope-options').then(unwrap);
  },
  getStudent(studentUserId) {
    return http.get(`/api/classroom/students/${studentUserId}`).then(unwrap);
  },
  createStudent(data) {
    return http.post('/api/classroom/students', data).then(unwrap);
  },
  updateStudent(studentUserId, data) {
    return http.put(`/api/classroom/students/${studentUserId}`, data).then(unwrap);
  },
  deleteStudent(studentUserId) {
    return http.delete(`/api/classroom/students/${studentUserId}`).then(unwrap);
  },
  listTeacherTasks() {
    return http.get('/api/classroom/tasks').then(unwrap);
  },
  createTask(data) {
    return http.post('/api/classroom/tasks', data).then(unwrap);
  },
  getTask(taskId) {
    return http.get(`/api/classroom/tasks/${taskId}`).then(unwrap);
  },
  updateTask(taskId, data) {
    return http.put(`/api/classroom/tasks/${taskId}`, data).then(unwrap);
  },
  deleteTask(taskId) {
    return http.delete(`/api/classroom/tasks/${taskId}`).then(unwrap);
  },
  publishTask(taskId) {
    return http.post(`/api/classroom/tasks/${taskId}/publish`).then(unwrap);
  },
  dashboard(taskId) {
    return http.get(`/api/classroom/tasks/${taskId}/dashboard`).then(unwrap);
  },
  listSubmissions(taskId) {
    return http.get(`/api/classroom/tasks/${taskId}/submissions`).then(unwrap);
  },
  myAssignments() {
    return http.get('/api/classroom/my-assignments').then(unwrap);
  },
  submitFile(taskId, subTaskId, file) {
    const form = new FormData();
    form.append('taskId', taskId);
    form.append('subTaskId', subTaskId);
    form.append('file', file);
    return http.post('/api/classroom/submit-file', form).then(unwrap);
  },
  grade(data) {
    return http.post('/api/classroom/grade', data).then(unwrap);
  },
  reject(data) {
    return http.post('/api/classroom/reject', data).then(unwrap);
  },
  gradingAssist(submissionId) {
    return http.post(`/api/classroom/submissions/${submissionId}/grading-assist`).then(unwrap);
  },
  taskDraftAssist(data) {
    return http.post('/api/classroom/task-draft-assist', data).then(unwrap);
  },
  listTaskTemplates() {
    return http.get('/api/classroom/task-templates').then(unwrap);
  },
  parseTaskText(text) {
    return http.post('/api/classroom/parse-task-text', { text, fileName: 'paste.txt' }).then(unwrap);
  },
  parseTaskDocument(file) {
    const form = new FormData();
    form.append('file', file);
    return http.post('/api/classroom/parse-task-document', form).then(unwrap);
  },
  sendReminders(taskId) {
    return http.post(`/api/classroom/tasks/${taskId}/send-reminders`).then(unwrap);
  },
  sendStudentReminder(taskId, data) {
    return http.post(`/api/classroom/tasks/${taskId}/send-reminder`, data).then(unwrap);
  },
};
