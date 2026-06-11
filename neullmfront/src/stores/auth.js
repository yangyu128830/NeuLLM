import { clearAllChatSessions } from '../composables/chatSessionStore';

const TOKEN_KEY = 'neullm_auth_token';
const USER_KEY = 'neullm_auth_user';
const REMEMBER_KEY = 'neullm_remember_login';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function getUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setAuth(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  clearAllChatSessions();
}

export function isLoggedIn() {
  return !!getToken();
}

export function isStudent() {
  return getUser()?.role === 'STUDENT';
}

export function isTeacher() {
  return getUser()?.role === 'TEACHER';
}

/** 按角色读取「记住账号密码」{ remember, username, password } */
export function getRememberedLogin(role) {
  try {
    const raw = localStorage.getItem(REMEMBER_KEY);
    if (!raw) return { remember: false, username: '', password: '' };
    const all = JSON.parse(raw);
    const row = all?.[role];
    if (!row?.remember) return { remember: false, username: '', password: '' };
    return {
      remember: true,
      username: row.username || '',
      password: row.password || '',
    };
  } catch {
    return { remember: false, username: '', password: '' };
  }
}

/** 保存或清除某角色的记住登录信息 */
export function saveRememberedLogin(role, remember, username, password) {
  let all = {};
  try {
    const raw = localStorage.getItem(REMEMBER_KEY);
    if (raw) all = JSON.parse(raw) || {};
  } catch {
    all = {};
  }
  if (remember && username) {
    all[role] = { remember: true, username, password: password || '' };
  } else {
    delete all[role];
  }
  if (Object.keys(all).length === 0) {
    localStorage.removeItem(REMEMBER_KEY);
  } else {
    localStorage.setItem(REMEMBER_KEY, JSON.stringify(all));
  }
}
