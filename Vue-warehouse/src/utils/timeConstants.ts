export const US_TIMEZONES = {
  // 本土四大时区
  mainland: [
    {
      id: 'America/New_York',
      name: '东部时间',
      abbr: 'ET',
      cities: '纽约、华盛顿、迈阿密、亚特兰大、波士顿',
      utc: 'UTC-5 / UTC-4',
      note: '美国金融中心，人口最多',
      type: 'mainland'
    },
    {
      id: 'America/Chicago',
      name: '中部时间',
      abbr: 'CT',
      cities: '芝加哥、休斯顿、达拉斯、新奥尔良',
      utc: 'UTC-6 / UTC-5',
      note: '美国中部工业区',
      type: 'mainland'
    },
    {
      id: 'America/Denver',
      name: '山地时间',
      abbr: 'MT',
      cities: '丹佛、盐湖城、阿尔伯克基',
      utc: 'UTC-7 / UTC-6',
      note: '落基山脉地区',
      type: 'mainland'
    },
    {
      id: 'America/Los_Angeles',
      name: '太平洋时间',
      abbr: 'PT',
      cities: '洛杉矶、旧金山、西雅图、拉斯维加斯',
      utc: 'UTC-8 / UTC-7',
      note: '硅谷、好莱坞所在地',
      type: 'mainland'
    },
    {
      id: 'America/Phoenix',
      name: '亚利桑那时间',
      abbr: 'MST',
      cities: '凤凰城、图森',
      utc: 'UTC-7（全年）',
      note: '⚠️ 不使用夏令时',
      type: 'mainland'
    }
  ],
  // 非本土州
  states: [
    {
      id: 'America/Anchorage',
      name: '阿拉斯加时间',
      abbr: 'AKT',
      cities: '安克雷奇、费尔班克斯、朱诺',
      utc: 'UTC-9 / UTC-8',
      note: '美国最大的州',
      type: 'state'
    },
    {
      id: 'Pacific/Honolulu',
      name: '夏威夷时间',
      abbr: 'HST',
      cities: '檀香山、希洛',
      utc: 'UTC-10（全年）',
      note: '⚠️ 不使用夏令时',
      type: 'state'
    },
    {
      id: 'America/Adak',
      name: '阿留申时间',
      abbr: 'HST',
      cities: '阿达克岛',
      utc: 'UTC-10 / UTC-9',
      note: '阿拉斯加州最西端',
      type: 'state'
    }
  ],
  // 海外领地
  territories: [
    {
      id: 'America/Puerto_Rico',
      name: '大西洋时间',
      abbr: 'AST',
      cities: '圣胡安（波多黎各）、夏洛特阿马利亚',
      utc: 'UTC-4（全年）',
      note: '波多黎各自治邦、美属维尔京群岛',
      type: 'territory'
    },
    {
      id: 'Pacific/Guam',
      name: '查莫罗时间',
      abbr: 'ChST',
      cities: '阿加尼亚（关岛）、塞班岛',
      utc: 'UTC+10（全年）',
      note: '关岛、北马里亚纳群岛',
      type: 'territory'
    },
    {
      id: 'Pacific/Pago_Pago',
      name: '萨摩亚时间',
      abbr: 'SST',
      cities: '帕果帕果',
      utc: 'UTC-11（全年）',
      note: '美属萨摩亚',
      type: 'territory'
    },
    {
      id: 'Pacific/Wake',
      name: '威克岛时间',
      abbr: 'WAKT',
      cities: '威克岛',
      utc: 'UTC+12（全年）',
      note: '威克岛（无常住人口）',
      type: 'territory'
    }
  ]
};

export function getAllTimezones() {
  return [
    ...US_TIMEZONES.mainland,
    ...US_TIMEZONES.states,
    ...US_TIMEZONES.territories
  ];
}
