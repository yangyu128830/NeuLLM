/**
 * 后端 LocalDateTime JSON 无时区后缀，语义为北京时间「墙钟时间」。
 * 直接 new Date(iso) 在部分环境下会按 UTC 解析，导致显示偏差 8 小时。
 */

const API_DT = /^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2}))?/;

export function parseApiDateTimeParts(value) {
  if (value == null || value === '') return null;
  const m = String(value).trim().match(API_DT);
  if (!m) return null;
  return {
    year: Number(m[1]),
    month: Number(m[2]),
    day: Number(m[3]),
    hour: Number(m[4]),
    minute: Number(m[5]),
    second: m[6] ? Number(m[6]) : 0
  };
}

/**
 * @param {string} value API 时间字符串
 * @param {{ style?: 'default'|'short'|'dateTimeLocal', hour12?: boolean }} [options]
 */
export function formatApiDateTime(value, options = {}) {
  const p = parseApiDateTimeParts(value);
  if (!p) {
    if (!value) return '—';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return String(value);
    return d.toLocaleString('zh-CN', { hour12: options.hour12 ?? false });
  }
  const pad = (n) => String(n).padStart(2, '0');
  if (options.style === 'short') {
    return `${p.month}月${p.day}日 ${pad(p.hour)}:${pad(p.minute)}`;
  }
  if (options.style === 'dateTimeLocal') {
    return `${p.year}-${pad(p.month)}-${pad(p.day)}T${pad(p.hour)}:${pad(p.minute)}`;
  }
  return `${p.year}/${pad(p.month)}/${pad(p.day)} ${pad(p.hour)}:${pad(p.minute)}`;
}

export function toDatetimeLocalFromApi(value) {
  return formatApiDateTime(value, { style: 'dateTimeLocal' }) || '';
}

export function compareApiDateTime(a, b) {
  const pa = parseApiDateTimeParts(a);
  const pb = parseApiDateTimeParts(b);
  if (!pa && !pb) return 0;
  if (!pa) return -1;
  if (!pb) return 1;
  const key = (p) =>
    p.year * 1e10 + p.month * 1e8 + p.day * 1e6 + p.hour * 1e4 + p.minute * 100 + p.second;
  return key(pa) - key(pb);
}
