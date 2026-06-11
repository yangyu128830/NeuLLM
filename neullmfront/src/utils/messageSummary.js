/** 用户想查消息内容/汇总（非单纯打开消息页） */
export function wantsMessagesSummary(text) {
  const raw = (text || '').trim();
  if (!raw || raw.length > 120) return false;
  const t = raw.replace(/\s/g, '');

  if (/(?:打开|去|进入|跳转).{0,8}(?:消息中心|消息页)/.test(t)) return false;
  if (/消息中心/.test(t) && !/(什么|哪些|有没有|收到|未读|总结|汇总)/.test(t)) return false;

  const needles = [
    /收到.{0,16}(什么|哪些|啥).{0,8}(消息|通知|信息)/,
    /(有什么|有哪些|有没有|还有没有).{0,12}(消息|通知|信息)/,
    /(消息|通知).{0,8}(总结|汇总|概况|情况)/,
    /未读.{0,8}(消息|通知)/,
    /最近.{0,12}(消息|通知|信息)/,
    /帮我.{0,6}查.{0,10}(消息|通知)/,
    /我.{0,4}消息.{0,6}(多不多|咋样|怎样)/,
    /^消息汇总$/,
    /^总结消息$/,
  ];
  return needles.some((re) => re.test(t) || re.test(raw));
}
