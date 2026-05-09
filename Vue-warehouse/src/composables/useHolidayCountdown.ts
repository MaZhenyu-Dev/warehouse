import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getUSHolidays, type USHoliday } from '@/utils/timeHelpers';

export function useHolidayCountdown() {
  const now = ref(new Date());
  let timer: number | undefined;

  function updateNow() {
    now.value = new Date();
  }

  onMounted(() => {
    updateNow();
    timer = window.setInterval(() => {
      requestAnimationFrame(updateNow);
    }, 1000);
  });

  onUnmounted(() => {
    if (timer) clearInterval(timer);
  });

  const upcomingHolidays = computed(() => {
    const year = now.value.getFullYear();
    // Get holidays for current and next year to ensure we always find upcoming ones
    const holidays = [...getUSHolidays(year), ...getUSHolidays(year + 1)];
    return holidays.filter(h => h.date >= now.value).sort((a, b) => a.date.getTime() - b.date.getTime());
  });

  const nearestHoliday = computed(() => {
    return upcomingHolidays.value[0] || null;
  });

  function getCountdown(holiday: USHoliday | null) {
    if (!holiday) return '';
    const diff = holiday.date.getTime() - now.value.getTime();
    if (diff <= 0) return '今天！';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);

    return `${days}天 ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }

  function getHolidayDateDisplay(holiday: USHoliday | null) {
    if (!holiday) return '';
    return new Intl.DateTimeFormat('zh-CN', {
      month: 'long',
      day: 'numeric',
      weekday: 'long',
      timeZone: 'America/New_York' // Using a US timezone for reference, or just local date
    }).format(holiday.date);
  }

  return {
    nearestHoliday,
    getCountdown,
    getHolidayDateDisplay
  };
}
