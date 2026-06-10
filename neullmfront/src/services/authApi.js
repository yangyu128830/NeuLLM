import http from './http';

function unwrap(res) {
  const body = res.data;
  if (body && body.ok === false) {
    throw new Error(body.message || '请求失败');
  }
  return body?.data ?? body;
}

export default {
  login(data) {
    return http.post('/api/auth/login', data);
  },
  register(data) {
    return http.post('/api/auth/register', data);
  },
  logout() {
    return http.post('/api/auth/logout');
  },
  me() {
    return http.get('/api/auth/me');
  },
  getTeacherProfile() {
    return http.get('/api/auth/teacher/profile').then(unwrap);
  },
  updateTeacherProfile(data) {
    return http.put('/api/auth/teacher/profile', data).then(unwrap);
  },
  getStudentProfile() {
    return http.get('/api/auth/student/profile').then(unwrap);
  },
  updateStudentProfile(data) {
    return http.put('/api/auth/student/profile', data).then(unwrap);
  },
};
