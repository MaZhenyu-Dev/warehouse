<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { US_TIMEZONES, getAllTimezones } from '@/utils/timeConstants';
import { 
  getUSHolidays, 
  formatTime, 
  formatDate, 
  getHour,
  getTimeDiff,
  type USHoliday
} from '@/utils/timeHelpers';

const route = useRoute();

// ==================== 状态变量 ====================
const selectedTimezone = ref('America/New_York');
const now = ref(new Date());
let timer: number | undefined;

// ==================== Computed Properties ====================
const beijingTime = computed(() => formatTime(now.value, 'Asia/Shanghai'));

const mainTime = computed(() => formatTime(now.value, selectedTimezone.value));
const mainDate = computed(() => formatDate(now.value, selectedTimezone.value));

const tzInfo = computed(() => {
  return getAllTimezones().find(t => t.id === selectedTimezone.value);
});

const dotColor = computed(() => {
  if (!tzInfo.value) return 'bg-gray-400';
  switch (tzInfo.value.type) {
    case 'mainland': return 'bg-blue-500';
    case 'state': return 'bg-green-500';
    case 'territory': return 'bg-purple-500';
    default: return 'bg-gray-400';
  }
});

const isWorking = computed(() => {
  const hour = getHour(now.value, selectedTimezone.value);
  return hour >= 9 && hour < 18;
});

const timeDiff = computed(() => {
  const diff = getTimeDiff(now.value, selectedTimezone.value);
  const absDiff = Math.abs(diff);
  return {
    diff,
    absDiff,
    text: `与北京时间相差 ${absDiff} 小时（美国${diff < 0 ? '晚' : '早'}）`
  };
});


const upcomingHolidays = computed(() => {
  const year = now.value.getFullYear();
  const holidays = [...getUSHolidays(year), ...getUSHolidays(year + 1)];
  return holidays.filter(h => h.date >= now.value).slice(0, 6);
});

function getCountdown(holiday: USHoliday) {
  const daysUntil = Math.ceil((holiday.date.getTime() - now.value.getTime()) / (1000 * 60 * 60 * 24));
  if (daysUntil === 0) return '今天！';
  if (daysUntil === 1) return '明天';
  return `还有 ${daysUntil} 天`;
}

function isUpcoming(holiday: USHoliday) {
  const daysUntil = Math.ceil((holiday.date.getTime() - now.value.getTime()) / (1000 * 60 * 60 * 24));
  return daysUntil <= 30;
}

function formatHolidayDate(date: Date) {
  return date.toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  });
}

// ==================== Lifecycle ====================
onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date();
  }, 1000);

  const queryTz = route.query.timezone as string;
  if (queryTz) {
    selectedTimezone.value = queryTz;
    nextTick(() => {
      const el = document.getElementById('tz-' + queryTz);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el.classList.add('ring-2', 'ring-primary');
        setTimeout(() => el.classList.remove('ring-2', 'ring-primary'), 2000);
      }
    });
  }
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <div class="container mx-auto p-4 max-w-10xl">
    <!-- 头部 -->
    <header class="text-center py-8">
      <div class="logo inline-flex items-center gap-3 mb-2">
        <div class="logo-icon w-12 h-12 bg-gradient-to-br from-[#ff6b35] to-[#ff8c42] rounded-xl flex items-center justify-center text-2xl">🛒</div>
        <h1 class="text-3xl font-bold tracking-tight">Temu 美国站时间助手</h1>
      </div>
      <p class="text-gray-500 mt-2 text-lg">实时掌握美国时间，把握最佳运营时机</p>
    </header>

    <!-- 北京时间 -->
    <div class="beijing-bar bg-card glass-card rounded-2xl p-4 md:p-6 flex flex-col md:flex-row justify-between items-center shadow-sm mb-6 border border-border">
      <div>
        <span class="text-muted-foreground text-sm md:text-base">🇨🇳 北京时间</span>
      </div>
      <div class="text-3xl font-bold tracking-tight mt-2 md:mt-0">{{ beijingTime }}</div>
    </div>

    <!-- 主时钟 -->
    <div class="main-clock-card bg-card glass-card rounded-3xl p-6 md:p-10 shadow-sm text-center mb-6 border border-border">
      <!-- 本土时区 -->
      <div class="timezone-category mb-4">
        <div class="text-xs text-muted-foreground uppercase tracking-widest mb-2">🇺🇸 美国本土时区</div>
        <div class="flex justify-center gap-2 flex-wrap">
          <button
            v-for="tz in US_TIMEZONES.mainland"
            :key="tz.id"
            @click="selectedTimezone = tz.id"
            class="tab-btn px-4 py-2 rounded-full text-sm font-medium transition-all duration-200"
            :class="[
              selectedTimezone === tz.id 
                ? 'bg-foreground text-background' 
                : 'bg-muted text-muted-foreground hover:bg-muted/80',
              'border border-border'
            ]"
          >
            {{ tz.name }}
          </button>
        </div>
      </div>

      <!-- 其他州和领地 -->
      <div class="timezone-category mb-4">
        <div class="text-xs text-muted-foreground uppercase tracking-widest mb-2">🏝️ 其他州 & 海外领地</div>
        <div class="flex justify-center gap-2 flex-wrap">
          <button
            v-for="tz in [...US_TIMEZONES.states, ...US_TIMEZONES.territories]"
            :key="tz.id"
            @click="selectedTimezone = tz.id"
            class="tab-btn px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 border border-dashed border-border"
            :class="[
              selectedTimezone === tz.id 
                ? 'bg-foreground text-background' 
                : 'bg-muted text-muted-foreground hover:bg-muted/80'
            ]"
          >
            {{ tz.name }}
          </button>
        </div>
      </div>
      
      <div class="text-xl text-muted-foreground font-medium mb-2 mt-6">{{ mainDate }}</div>
      <div class="text-6xl md:text-8xl font-bold tracking-tighter leading-none my-5 tabular-nums">{{ mainTime }}</div>
      
      <div class="inline-flex items-center gap-2 px-4 py-2 bg-muted rounded-full text-sm text-muted-foreground">
        <span class="w-2 h-2 rounded-full shrink-0" :class="dotColor"></span>
        <span v-if="tzInfo">{{ tzInfo.name }} {{ tzInfo.abbr }}</span>
        <span 
          class="ml-1 px-2 py-0.5 rounded-full text-xs font-medium"
          :class="isWorking ? 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400' : 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400'"
        >
          ● {{ isWorking ? '工作时间' : '非工作时间' }}
        </span>
      </div>
      
      <div class="mt-4 text-sm text-muted-foreground">
        与北京时间相差 <strong class="text-[#ff6b35]">{{ timeDiff.absDiff }}</strong> 小时（美国{{ timeDiff.diff < 0 ? '晚' : '早' }}）
      </div>

      <!-- 时区类型说明 -->
      <div class="flex justify-center gap-6 mt-8 pt-6 border-t border-border flex-wrap">
        <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
          <span class="w-2 h-2 rounded-full bg-[#007aff]"></span>
          <span>本土时区</span>
        </div>
        <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
          <span class="w-2 h-2 rounded-full bg-[#34c759]"></span>
          <span>非本土州</span>
        </div>
        <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
          <span class="w-2 h-2 rounded-full bg-[#af52de]"></span>
          <span>海外领地</span>
        </div>
      </div>
    </div>

    <!-- 时区卡片 -->
    <section class="mb-8">
      <h2 class="text-2xl font-bold mb-5 flex items-center gap-2">🕐 所有美国时区</h2>
      
      <div class="text-sm font-semibold text-muted-foreground mb-3 pl-1">📍 本土四大时区（主要消费市场）</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-6">
        <div 
          v-for="tz in US_TIMEZONES.mainland" 
          :key="tz.id"
          :id="'tz-' + tz.id"
          @click="selectedTimezone = tz.id"
          class="tz-card bg-card glass-card rounded-2xl p-6 shadow-sm cursor-pointer transition-all duration-200 border-2 relative hover:-translate-y-0.5 hover:shadow-md"
          :class="selectedTimezone === tz.id ? 'border-[#ff6b35]' : 'border-transparent'"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-lg">{{ tz.name }}</span>
              <span class="text-[10px] px-2 py-0.5 rounded-full font-medium bg-blue-500/10 text-blue-500">本土</span>
            </div>
            <span class="px-2.5 py-1 bg-muted rounded-lg text-xs font-semibold text-muted-foreground">{{ tz.abbr }}</span>
          </div>
          <div class="text-3xl font-bold tracking-tight mb-1 tabular-nums">{{ formatTime(now, tz.id) }}</div>
          <div class="text-sm text-muted-foreground">{{ formatDate(now, tz.id).replace(/\d{4}年/, '') }}</div>
          <div class="text-xs text-muted-foreground mt-2 opacity-70">{{ tz.utc }}</div>
          <div class="mt-3 pt-3 border-t border-border text-[15px] text-muted-foreground">📍 {{ tz.cities }}</div>
          <div v-if="tz.note" class="text-sm text-[#ff6b35] mt-1.5">{{ tz.note }}</div>
        </div>
      </div>
      
      <div class="text-sm font-semibold text-muted-foreground mb-3 pl-1">🏝️ 非本土州</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
        <div 
          v-for="tz in US_TIMEZONES.states" 
          :key="tz.id"
          :id="'tz-' + tz.id"
          @click="selectedTimezone = tz.id"
          class="tz-card bg-card glass-card rounded-2xl p-6 shadow-sm cursor-pointer transition-all duration-200 border-2 relative hover:-translate-y-0.5 hover:shadow-md"
          :class="selectedTimezone === tz.id ? 'border-[#ff6b35]' : 'border-transparent'"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-lg">{{ tz.name }}</span>
              <span class="text-[10px] px-2 py-0.5 rounded-full font-medium bg-green-500/10 text-green-500">州</span>
            </div>
            <span class="px-2.5 py-1 bg-muted rounded-lg text-xs font-semibold text-muted-foreground">{{ tz.abbr }}</span>
          </div>
          <div class="text-3xl font-bold tracking-tight mb-1 tabular-nums">{{ formatTime(now, tz.id) }}</div>
          <div class="text-sm text-muted-foreground">{{ formatDate(now, tz.id).replace(/\d{4}年/, '') }}</div>
          <div class="text-xs text-muted-foreground mt-2 opacity-70">{{ tz.utc }}</div>
          <div class="mt-3 pt-3 border-t border-border text-[15px] text-muted-foreground">📍 {{ tz.cities }}</div>
          <div v-if="tz.note" class="text-sm text-[#ff6b35] mt-1.5">{{ tz.note }}</div>
        </div>
      </div>
      
      <div class="text-sm font-semibold text-muted-foreground mb-3 pl-1">🌏 海外领地</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div 
          v-for="tz in US_TIMEZONES.territories" 
          :key="tz.id"
          :id="'tz-' + tz.id"
          @click="selectedTimezone = tz.id"
          class="tz-card bg-card glass-card rounded-2xl p-6 shadow-sm cursor-pointer transition-all duration-200 border-2 relative hover:-translate-y-0.5 hover:shadow-md"
          :class="selectedTimezone === tz.id ? 'border-[#ff6b35]' : 'border-transparent'"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-lg">{{ tz.name }}</span>
              <span class="text-[10px] px-2 py-0.5 rounded-full font-medium bg-purple-500/10 text-purple-500">领地</span>
            </div>
            <span class="px-2.5 py-1 bg-muted rounded-lg text-xs font-semibold text-muted-foreground">{{ tz.abbr }}</span>
          </div>
          <div class="text-3xl font-bold tracking-tight mb-1 tabular-nums">{{ formatTime(now, tz.id) }}</div>
          <div class="text-sm text-muted-foreground">{{ formatDate(now, tz.id).replace(/\d{4}年/, '') }}</div>
          <div class="text-xs text-muted-foreground mt-2 opacity-70">{{ tz.utc }}</div>
          <div class="mt-3 pt-3 border-t border-border text-[15px] text-muted-foreground">📍 {{ tz.cities }}</div>
          <div v-if="tz.note" class="text-sm text-[#ff6b35] mt-1.5">{{ tz.note }}</div>
        </div>
      </div>
    </section>

    <!-- 节日提醒 -->
    <section class="mb-8">
      <h2 class="text-2xl font-bold mb-5 flex items-center gap-2">📅 重要节日提醒</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="holiday in upcomingHolidays" 
          :key="holiday.name"
          class="holiday-card bg-card glass-card rounded-2xl p-6 shadow-sm flex gap-4 items-start border transition-all duration-300"
          :class="isUpcoming(holiday) 
            ? 'border-orange-300 dark:border-orange-500/50 shadow-[0_0_15px_-3px_rgba(255,107,53,0.15)] dark:shadow-[0_0_20px_-5px_rgba(255,107,53,0.3)]' 
            : 'border-transparent hover:bg-muted/50'"
        >
          <div 
            class="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl shrink-0"
            :class="{
              'bg-red-100 dark:bg-red-900/30': holiday.type === 'sale',
              'bg-green-100 dark:bg-green-900/30': holiday.type === 'gift',
              'bg-pink-100 dark:bg-pink-900/30': holiday.type === 'love',
              'bg-blue-100 dark:bg-blue-900/30': holiday.type === 'flag',
              'bg-orange-100 dark:bg-orange-900/30': holiday.type === 'spooky'
            }"
          >
            {{ holiday.icon }}
          </div>
          <div class="flex-1">
            <div class="text-lg font-semibold mb-1">{{ holiday.name }}</div>
            <div class="text-sm text-muted-foreground mb-2">{{ formatHolidayDate(holiday.date) }}</div>
            <span class="inline-block px-3 py-1 bg-[#ff6b35] text-white rounded-full text-xs font-semibold">{{ getCountdown(holiday) }}</span>
            <div class="mt-2 text-sm text-muted-foreground leading-relaxed">💡 {{ holiday.tips }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Temu运营提示 -->
    <section class="bg-card glass-card rounded-3xl p-8 shadow-sm mb-8 border border-border">
      <h2 class="text-2xl font-bold mb-5 flex items-center gap-2">💡 Temu运营时间攻略</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">🌅 上新时间</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">建议在美东时间早8点前完成上新，即北京时间晚8-9点操作，确保美国用户早间能看到新品。</p>
        </div>
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">📊 数据查看</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">Temu后台数据通常在太平洋时间凌晨更新，建议北京时间下午3-4点后查看前日完整数据。</p>
        </div>
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">💬 客服响应</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">美国用户活跃时段为当地9:00-23:00，对应北京时间21:00-次日11:00，建议此时段优先处理。</p>
        </div>
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">🎯 促销活动</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">大促活动通常按美东时间开始，记得提前12小时准备，避免因时差错过重要节点。</p>
        </div>
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">📦 物流时效</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">关注美国当地节假日，USPS等快递公司节假日不派送，提前告知买家预期送达时间。</p>
        </div>
        <div class="p-5 bg-muted/50 rounded-2xl">
          <h4 class="text-base font-semibold mb-2 flex items-center gap-2">⚡ 黄金时段</h4>
          <p class="text-[15px] text-muted-foreground leading-relaxed">美东时间19:00-23:00是流量高峰，对应北京时间次日7:00-11:00，可重点关注订单。</p>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="text-center py-8 text-muted-foreground text-sm">
      <p>时间基于自己的设备自动计算 · 自动识别夏令时 · 覆盖全部11个美国时区</p>
      <p class="mt-2 opacity-70">© 2026 Temu时间助手 - 小马助力各位运营</p>
    </footer>
  </div>
</template>

<style scoped>
/* 
  Some styles are already converted to Tailwind CSS classes in the template.
  Keeping custom styles if needed for complex animations or specific behaviors.
*/
</style>
