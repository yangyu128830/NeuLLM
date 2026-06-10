import http from './http';

function unwrap(res) {
  const body = res.data;
  if (body && body.ok === false) {
    throw new Error(body.message || '请求失败');
  }
  return body?.data ?? body;
}

export default {
  list(limit = 50) {
    return http.get('/api/notifications', { params: { limit } }).then(unwrap);
  },
  unreadCount() {
    return http.get('/api/notifications/unread-count').then(unwrap);
  },
  markRead(id) {
    return http.post(`/api/notifications/${id}/read`).then(unwrap);
  },
  markAllRead() {
    return http.post('/api/notifications/read-all').then(unwrap);
  },
};
