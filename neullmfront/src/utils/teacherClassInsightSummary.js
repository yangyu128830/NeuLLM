/** 教师端：学情洞察类提问（待交 / 表现好 / 班级概况等） */
export function detectTeacherClassInsight(text) {
  const raw = (text || '').trim();
  if (!raw || raw.length > 120) return null;
  const t = raw.replace(/\s/g, '');

  if (/(?:打开|去|进入|跳转).{0,8}(?:看板|学情|进度|批改|任务)/.test(t)) return null;
  if (/(?:发送|一键|直接|马上).{0,6}催交/.test(t)) return null;
  if (/(?:创建|发布|布置).{0,6}作业/.test(t)) return null;

  const unsubmitted = [
    /一直.{0,8}不?交/,
    /不?交作业/,
    /没交.{0,8}作业/,
    /未交/,
    /谁.{0,10}没交/,
    /有没有.{0,12}(学生|谁).{0,10}不?交/,
    /待提交/,
    /催交.{0,8}(名单|情况|谁)/,
    /谁.{0,8}还没交/,
    /谁.{0,8}不交/,
  ];
  if (unsubmitted.some((re) => re.test(t) || re.test(raw))) {
    return { focus: 'unsubmitted', label: '待交作业' };
  }

  const underperformers = [
    /表现.{0,8}(差|最差|不好|不佳|落后|低)/,
    /谁.{0,8}(最差|最差|落后|差|垫底)/,
    /成绩.{0,8}(差|最低|不好)/,
    /完成度.{0,8}(低|最差|差)/,
    /谁.{0,8}拖后腿/,
    /垫底/,
    /谁.{0,8}最不行/,
  ];
  if (underperformers.some((re) => re.test(t) || re.test(raw))) {
    return { focus: 'underperformers', label: '待提升' };
  }

  const performers = [
    /表现.{0,8}(好|优秀|棒|不错|突出)/,
    /谁.{0,8}(最好|最棒|优秀|厉害|突出)/,
    /完成度.{0,8}(高|好)/,
    /成绩.{0,8}(好|优秀|高)/,
    /学情.{0,8}(好|优秀)/,
    /表扬.{0,6}谁/,
  ];
  if (performers.some((re) => re.test(t) || re.test(raw))) {
    return { focus: 'performers', label: '表现突出' };
  }

  const followUp = [
    /需跟进/,
    /要跟进/,
    /谁.{0,8}落后/,
    /进度.{0,8}(慢|差|落后)/,
    /谁.{0,12}需要.{0,6}关注/,
    /进行中的学生/,
  ];
  if (followUp.some((re) => re.test(t) || re.test(raw))) {
    return { focus: 'follow_up', label: '需跟进' };
  }

  const general = [
    /学情.{0,10}(怎么样|咋样|如何|概况|汇总)/,
    /班级.{0,10}(情况|进度|怎么样|咋样)/,
    /进度.{0,10}(怎么样|咋样|如何|概况)/,
    /整体.{0,8}情况/,
    /学生.{0,8}情况/,
    /帮我.{0,8}(看看|查|汇总|了解).{0,8}学情/,
    /班里.{0,8}怎么样/,
  ];
  if (general.some((re) => re.test(t) || re.test(raw))) {
    return { focus: 'general', label: '班级学情' };
  }

  return null;
}
