<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUsTimeZone } from '@/composables/useUsTimeZone';
import { US_TIMEZONES, getAllTimezones } from '@/utils/timeConstants';
import { getHour } from '@/utils/timeHelpers';
import { Clock, ArrowRight, Globe, MapPin } from 'lucide-vue-next';

const router = useRouter();
const { selectedTimezone, formattedTime, formattedDate, now } = useUsTimeZone();

const timezones = getAllTimezones();

const currentTzInfo = computed(() => {
  return timezones.find(tz => tz.id === selectedTimezone.value);
});

const isWorking = computed(() => {
  const hour = getHour(now.value, selectedTimezone.value);
  return hour >= 9 && hour < 18;
});

const typeLabel = computed(() => {
  if (!currentTzInfo.value) return '';
  switch (currentTzInfo.value.type) {
    case 'mainland': return '本土';
    case 'state': return '州';
    case 'territory': return '领地';
    default: return '';
  }
});

const typeColor = computed(() => {
  if (!currentTzInfo.value) return 'bg-gray-100 text-gray-600';
  switch (currentTzInfo.value.type) {
    case 'mainland': return 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400';
    case 'state': return 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400';
    case 'territory': return 'bg-purple-100 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400';
    default: return 'bg-gray-100 text-gray-600';
  }
});

function handleNavigate() {
  router.push({ 
    name: 'us-time', 
    query: { timezone: selectedTimezone.value } 
  });
}
</script>

<template>
  <div class="glass-card p-6 relative overflow-hidden flex flex-col justify-between h-full group/card hover:shadow-md transition-all duration-300">
    <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
      <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
    </div>
    
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-medium text-muted-foreground flex items-center gap-2">
        <Clock class="w-4 h-4" />
        美国时区
      </h3>
      <div v-if="currentTzInfo" class="text-[10px] px-2 py-0.5 rounded-full font-medium" :class="typeColor">
        {{ typeLabel }}
      </div>
    </div>

    <div class="space-y-4 mb-2">
      <div class="relative group">
        <div class="absolute inset-y-0 left-0 z-10 flex items-center pl-3 pointer-events-none">
          <Globe class="w-4 h-4 text-muted-foreground/70 group-hover:text-primary transition-colors" />
        </div>
        <el-select
          v-model="selectedTimezone"
          class="glass-select tz-glass-select w-full"
          popper-class="glass-select-popper"
          :teleported="true"
          aria-label="选择时区"
        >
          <el-option-group label="🇺🇸 本土四大时区">
            <el-option
              v-for="tz in US_TIMEZONES.mainland"
              :key="tz.id"
              :label="`${tz.name} (${tz.abbr})`"
              :value="tz.id"
            />
          </el-option-group>
          <el-option-group label="🏝️ 非本土州">
            <el-option
              v-for="tz in US_TIMEZONES.states"
              :key="tz.id"
              :label="`${tz.name} (${tz.abbr})`"
              :value="tz.id"
            />
          </el-option-group>
          <el-option-group label="🌏 海外领地">
            <el-option
              v-for="tz in US_TIMEZONES.territories"
              :key="tz.id"
              :label="`${tz.name} (${tz.abbr})`"
              :value="tz.id"
            />
          </el-option-group>
        </el-select>
        <div class="absolute inset-y-0 right-0 z-10 flex items-center pr-3 pointer-events-none">
          <div class="border-l border-border h-4 mx-2"></div>
          <MapPin class="w-3.5 h-3.5 text-muted-foreground/50" />
        </div>
      </div>

      <div class="text-center py-2">
        <div class="text-4xl font-bold text-foreground tracking-tight tabular-nums bg-gradient-to-br from-foreground to-foreground/70 bg-clip-text">
          {{ formattedTime }}
        </div>
        <div class="text-base text-muted-foreground/80 font-medium mt-1">
          {{ formattedDate }}
        </div>
        <div class="mt-2">
          <span 
            class="px-2 py-0.5 rounded-full text-xs font-medium" 
            :class="isWorking ? 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400' : 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400'" 
          > 
            ● {{ isWorking ? '工作时间' : '非工作时间' }} 
          </span>
        </div>
      </div>
    </div>

    <button 
      @click="handleNavigate"
      class="mt-auto w-full flex items-center justify-center gap-2 text-[13px] font-medium py-2.5 rounded-xl bg-primary/5 hover:bg-primary/10 text-primary transition-all group active:scale-[0.98]"
    >
      前往时区详情
      <ArrowRight class="w-3.5 h-3.5 transition-transform group-hover:translate-x-1" />
    </button>
  </div>
</template>

<style scoped>
:deep(.tz-glass-select .el-select__wrapper) {
  padding-left: 2.5rem;
  padding-right: 3.25rem;
}

:deep(.tz-glass-select .el-select__caret) {
  display: none;
}
</style>
