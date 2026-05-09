export function getNthWeekday(year: number, month: number, weekday: number, n: number) {
  const firstDay = new Date(year, month, 1);
  const firstWeekday = firstDay.getDay();
  const day = 1 + (weekday - firstWeekday + 7) % 7 + (n - 1) * 7;
  return new Date(year, month, day);
}

export type USHolidayType = 'gift' | 'flag' | 'love' | 'sale' | 'spooky'

export interface USHoliday {
  name: string
  date: Date
  icon: string
  type: USHolidayType
  tips: string
}

export function getLastWeekday(year: number, month: number, weekday: number) {
  const lastDay = new Date(year, month + 1, 0);
  const lastWeekday = lastDay.getDay();
  const diff = (lastWeekday - weekday + 7) % 7;
  return new Date(year, month, lastDay.getDate() - diff);
}

export function getEasterDate(year: number) {
  const a = year % 19;
  const b = Math.floor(year / 100);
  const c = year % 100;
  const d = Math.floor(b / 4);
  const e = b % 4;
  const f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3);
  const h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4);
  const k = c % 4;
  const l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const month = Math.floor((h + l - 7 * m + 114) / 31) - 1;
  const day = ((h + l - 7 * m + 114) % 31) + 1;
  return new Date(year, month, day);
}

export function addDays(date: Date, days: number) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

export function getUSHolidays(year: number): USHoliday[] {
  const holidays: USHoliday[] = [
    { name: '新年', date: new Date(year, 0, 1), icon: '🎉', type: 'gift', tips: '新年促销收尾，清理库存好时机' },
    { name: '马丁·路德·金纪念日', date: getNthWeekday(year, 0, 1, 3), icon: '✊', type: 'flag', tips: '联邦假日，物流可能延迟' },
    { name: '情人节', date: new Date(year, 1, 14), icon: '💕', type: 'love', tips: '礼品类目爆发期，提前2周备货' },
    { name: '总统日', date: getNthWeekday(year, 1, 1, 3), icon: '🇺🇸', type: 'flag', tips: '冬季大促常见节点' },
    { name: '复活节', date: getEasterDate(year), icon: '🐰', type: 'gift', tips: '春季重要节日，家居装饰类热销' },
    { name: '母亲节', date: getNthWeekday(year, 4, 0, 2), icon: '💐', type: 'love', tips: '女性用品、珠宝、花卉类目重点' },
    { name: '阵亡将士纪念日', date: getLastWeekday(year, 4, 1), icon: '🎖️', type: 'flag', tips: '夏季开始，户外用品需求上升' },
    { name: '父亲节', date: getNthWeekday(year, 5, 0, 3), icon: '👔', type: 'gift', tips: '男性用品、工具类目重点' },
    { name: '独立日', date: new Date(year, 6, 4), icon: '🎆', type: 'flag', tips: '户外派对、烧烤用品热销' },
    { name: '劳动节', date: getNthWeekday(year, 8, 1, 1), icon: '👷', type: 'flag', tips: '返校季结束，秋季促销开始' },
    { name: '哥伦布日', date: getNthWeekday(year, 9, 1, 2), icon: '🚢', type: 'flag', tips: '秋季促销节点' },
    { name: '万圣节', date: new Date(year, 9, 31), icon: '🎃', type: 'spooky', tips: '装饰、服装、糖果类目爆发' },
    { name: '退伍军人节', date: new Date(year, 10, 11), icon: '🎖️', type: 'flag', tips: '感恩促销预热期' },
    { name: '感恩节', date: getNthWeekday(year, 10, 4, 4), icon: '🦃', type: 'gift', tips: '年度最大促销季开始' },
    { name: '黑色星期五', date: addDays(getNthWeekday(year, 10, 4, 4), 1), icon: '🛍️', type: 'sale', tips: '全年最大购物日，务必提前备货' },
    { name: '网络星期一', date: addDays(getNthWeekday(year, 10, 4, 4), 4), icon: '💻', type: 'sale', tips: '线上购物高峰，确保库存充足' },
    { name: '圣诞节', date: new Date(year, 11, 25), icon: '🎄', type: 'gift', tips: '最后发货截止日前2周是关键' },
    { name: '节礼日', date: new Date(year, 11, 26), icon: '🎁', type: 'sale', tips: '圣诞后促销，清库存好时机' }
  ];
  return holidays.sort((a, b) => a.date.getTime() - b.date.getTime());
}

export function formatTime(date: Date, timezone: string) {
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      timeZone: timezone,
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(date);
  } catch {
    return '--:--:--';
  }
}

export function formatDate(date: Date, timezone: string) {
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      timeZone: timezone,
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    }).format(date);
  } catch {
    return '----年--月--日';
  }
}

export function getHour(date: Date, timezone: string) {
  try {
    return parseInt(new Intl.DateTimeFormat('en-US', {
      timeZone: timezone,
      hour: 'numeric',
      hour12: false
    }).format(date));
  } catch {
    return 0;
  }
}

export function getTimeDiff(now: Date, timezone: string) {
  try {
    const localTime = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Shanghai' }));
    const targetTime = new Date(now.toLocaleString('en-US', { timeZone: timezone }));
    const diff = (targetTime.getTime() - localTime.getTime()) / (1000 * 60 * 60);
    return Math.round(diff * 10) / 10;
  } catch {
    return 0;
  }
}
