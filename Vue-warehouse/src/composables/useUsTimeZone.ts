import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { formatTime as formatTimeHelper, formatDate as formatDateHelper } from '@/utils/timeHelpers';

export function useUsTimeZone() {
  const selectedTimezone = ref('America/New_York');
  const now = ref(new Date());
  let timer: number | undefined;

  // Initialize from localStorage
  const storedTimezone = localStorage.getItem('us_timezone_preference');
  if (storedTimezone) {
    selectedTimezone.value = storedTimezone;
  }

  // Watch for changes and save to localStorage
  watch(selectedTimezone, (newVal) => {
    localStorage.setItem('us_timezone_preference', newVal);
  });

  const formattedTime = computed(() => formatTimeHelper(now.value, selectedTimezone.value));
  const formattedDate = computed(() => formatDateHelper(now.value, selectedTimezone.value));

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

  return {
    selectedTimezone,
    now,
    formattedTime,
    formattedDate
  };
}
