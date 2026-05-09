import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export const useLayoutStore = defineStore('layout', () => {
  // Theme State
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  // Sidebar State
  const isSidebarCollapsed = ref(false)

  // Actions
  function toggleTheme() {
    isDark.value = !isDark.value
  }

  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  // Watchers to apply side effects
  watch(isDark, (val) => {
    if (val) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }, { immediate: true })

  return {
    isDark,
    isSidebarCollapsed,
    toggleTheme,
    toggleSidebar
  }
})
