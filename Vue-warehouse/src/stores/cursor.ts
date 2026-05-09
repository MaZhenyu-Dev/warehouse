import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export interface CursorSettings {
  friction: number
  trails: number
  size: number
  dampening: number
  tension: number
  opacity: number
  lineWidth: number
  color: string
  disableMobile: boolean
  enabled: boolean
}

const DEFAULT_SETTINGS: CursorSettings = {
  friction: 0.5,
  trails: 15,
  size: 50,
  dampening: 0.25,
  tension: 0.98,
  opacity: 0.2,
  lineWidth: 1,
  color: '#8b5cf6',
  disableMobile: true,
  enabled: true,
}

function loadSettings(): CursorSettings {
  try {
    const saved = localStorage.getItem('cursorSettings')
    if (saved) {
      return { ...DEFAULT_SETTINGS, ...JSON.parse(saved) }
    }
  } catch (e) {
    console.warn('Failed to load cursor settings:', e)
  }
  return DEFAULT_SETTINGS
}

export const useCursorStore = defineStore('cursor', () => {
  const settings = ref<CursorSettings>(loadSettings())

  function updateSetting<K extends keyof CursorSettings>(key: K, value: CursorSettings[K]) {
    settings.value[key] = value
  }

  function resetSettings() {
    settings.value = { ...DEFAULT_SETTINGS }
  }

  watch(settings, (val) => {
    localStorage.setItem('cursorSettings', JSON.stringify(val))
  }, { deep: true })

  return {
    settings,
    updateSetting,
    resetSettings
  }
})
