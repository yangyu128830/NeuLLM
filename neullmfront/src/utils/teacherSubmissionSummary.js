/** 教师端：查询学生最近提交 / 待批改（非单纯打开批改台） */
export function wantsTeacherSubmissionsSummary(text) {
  const raw = (text || '').trim();
  if (!raw || raw.length > 120) return false;
  const t = raw.replace(/\s/g, '');

  if (/(?:打开|去|进入|跳转).{0,8}(?:批改台|批改页)/.test(t)) return false;

  const needles = [
    /最近.{0,12}(学生|有人|谁).{0,8}(新)?提交/,
    /(新|最新).{0,8}提交/,
    /学生.{0,8}(新)?提交/,
    /有没有.{0,10}提交/,
    /待批改.{0,8}提交/,
    /提交.{0,8}(情况|列表|咋样|怎样|汇总)/,
    /谁.{0,6}交了作业/,
    /帮我.{0,6}查.{0,10}提交/,
    /^提交汇总$/,
  ];
  return needles.some((re) => re.test(t) || re.test(raw));
}

/** 教师批改台深度链接：选中任务并打开指定提交 */
export function buildTeacherGradingLink(item) {
  const params = new URLSearchParams();
  if (item?.taskId) params.set('taskId', item.taskId);
  if (item?.submissionId) params.set('submissionId', item.submissionId);
  params.set('status', 'pending');
  return `/teacher/grading?${params.toString()}`;
}
