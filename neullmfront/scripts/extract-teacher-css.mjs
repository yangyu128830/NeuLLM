import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const vuePath = path.join(__dirname, '../src/views/teacher/TeacherDashboardView.vue')
const outPath = path.join(__dirname, '../src/assets/teacher-shell.css')

const t = fs.readFileSync(vuePath, 'utf8')
const m = t.match(/<style scoped>([\s\S]*)<\/style>/)
if (!m) throw new Error('no style block')
let css = m[1].replace(/@import\s+['"]\.\.\/\.\.\/assets\/classroom-theme\.css['"];\s*/g, '')

css += `
.teacher-subnav {
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}
.teacher-subnav-inner {
  display: flex;
  gap: 6px;
  padding: 10px 0;
  overflow-x: auto;
}
.teacher-subnav-link {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--muted);
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s, color 0.2s;
}
.teacher-subnav-link:hover {
  background: var(--hint-bg);
  color: var(--accent-dark);
}
.teacher-subnav-link.router-link-active {
  background: var(--accent-soft);
  color: var(--accent-dark);
}
.teacher-page .content {
  min-height: 0;
}
.teacher-page .student-list-full {
  max-height: none;
}
`

fs.writeFileSync(outPath, `@import './classroom-theme.css';\n${css}`)
console.log('wrote', outPath)
