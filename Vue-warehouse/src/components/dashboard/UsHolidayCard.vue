<script setup lang="ts">
import { useHolidayCountdown } from '@/composables/useHolidayCountdown';

const { nearestHoliday, getCountdown, getHolidayDateDisplay } = useHolidayCountdown();
</script>

<template>
  <div class="h-full">
    <div 
      v-if="nearestHoliday"
      class="h-full holiday-card bg-card glass-card rounded-2xl p-6 shadow-sm flex flex-col relative overflow-hidden transition-all duration-300 group hover:shadow-md"
      :class="[
        nearestHoliday.type === 'sale' ? 'border-red-200 dark:border-red-900/30' :
        nearestHoliday.type === 'gift' ? 'border-green-200 dark:border-green-900/30' :
        nearestHoliday.type === 'love' ? 'border-pink-200 dark:border-pink-900/30' :
        nearestHoliday.type === 'flag' ? 'border-blue-200 dark:border-blue-900/30' :
        'border-orange-200 dark:border-orange-900/30'
      ]"
    >
      <!-- Background decoration -->
      <div class="absolute -right-6 -top-6 w-24 h-24 rounded-full opacity-20 blur-2xl"
        :class="{
          'bg-red-500': nearestHoliday.type === 'sale',
          'bg-green-500': nearestHoliday.type === 'gift',
          'bg-pink-500': nearestHoliday.type === 'love',
          'bg-blue-500': nearestHoliday.type === 'flag',
          'bg-orange-500': nearestHoliday.type === 'spooky'
        }"
      ></div>

      <!-- Header Section -->
      <div class="flex items-start gap-4 mb-4 relative z-10">
        <div 
          class="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl shrink-0 shadow-sm transition-transform duration-300 group-hover:scale-105"
          :class="{
            'bg-red-100 dark:bg-red-900/30': nearestHoliday.type === 'sale',
            'bg-green-100 dark:bg-green-900/30': nearestHoliday.type === 'gift',
            'bg-pink-100 dark:bg-pink-900/30': nearestHoliday.type === 'love',
            'bg-blue-100 dark:bg-blue-900/30': nearestHoliday.type === 'flag',
            'bg-orange-100 dark:bg-orange-900/30': nearestHoliday.type === 'spooky'
          }"
        >
          {{ nearestHoliday.icon }}
        </div>
        <div class="flex-1 min-w-0 pt-1">
          <h3 class="text-lg font-bold text-foreground truncate leading-tight mb-1">{{ nearestHoliday.name }}</h3>
          <div class="text-sm text-muted-foreground">{{ getHolidayDateDisplay(nearestHoliday) }}</div>
        </div>
      </div>

      <!-- Countdown Section -->
      <div class="mb-4 relative z-10">
        <div class="text-[15px] uppercase tracking-wider text-muted-foreground/80 font-bold mb-1 pl-1">距离节日还有</div>
        <div 
          class="text-3xl font-bold text-[#ff6b35] tabular-nums tracking-tight"
          aria-live="polite"
        >
          {{ getCountdown(nearestHoliday) }}
        </div>
      </div>

      <!-- Tips Section (Original Content) -->
      <div class="mt-auto pt-4 border-t border-black/5 dark:border-white/5 relative z-10">
        <div class="text-[15px] text-muted-foreground leading-relaxed line-clamp-3">
          <span class="font-semibold text-foreground/80 mr-1">💡 运营思路:</span>
          {{ nearestHoliday.tips }}
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="h-full glass-card flex flex-col items-center justify-center text-muted-foreground p-6">
      <p>暂无近期节日</p>
    </div>
  </div>
</template>

<style scoped>
/* Scoped styles if needed */
</style>
