<template>
  <div class="space-y-6">
    <h1 class="text-3xl font-bold text-foreground">系统设置</h1>

    <div class="glass-card p-6 relative overflow-hidden">
      <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
        <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
      </div>
      <div class="space-y-4 max-w-2xl">
        <div class="flex items-center justify-between py-4 border-b border-border">
          <div>
            <div class="font-medium text-foreground">通知提醒</div>
            <div class="text-sm text-muted-foreground">接收系统更新和警报</div>
          </div>
          <div class="h-6 w-11 rounded-full bg-blue-600 relative cursor-pointer">
            <div class="absolute right-1 top-1 h-4 w-4 rounded-full bg-white transition-transform"></div>
          </div>
        </div>
        <div class="flex items-center justify-between py-4 border-b border-border">
          <div>
            <div class="font-medium text-foreground">自动备份</div>
            <div class="text-sm text-muted-foreground">每日凌晨自动备份数据</div>
          </div>
          <div class="h-6 w-11 rounded-full bg-muted relative cursor-pointer">
             <div class="absolute left-1 top-1 h-4 w-4 rounded-full bg-muted-foreground/50 transition-transform"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="glass-card p-6 relative overflow-hidden">
      <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
        <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
      </div>
      <h2 class="text-xl font-semibold text-foreground mb-4">鼠标光标效果</h2>
      <div class="space-y-6 max-w-2xl">
        <div class="flex items-center justify-between py-4 border-b border-border">
          <div>
            <div class="font-medium text-foreground">启用光标效果</div>
            <div class="text-sm text-muted-foreground">显示鼠标拖尾动画效果</div>
          </div>
          <button
            @click="cursorStore.updateSetting('enabled', !cursorStore.settings.enabled)"
            class="h-6 w-11 rounded-full transition-colors relative"
            :class="cursorStore.settings.enabled ? 'bg-blue-600' : 'bg-muted'"
          >
            <div
              class="absolute top-1 h-4 w-4 rounded-full bg-white transition-transform"
              :class="cursorStore.settings.enabled ? 'right-1' : 'left-1'"
            ></div>
          </button>
        </div>

        <div v-if="cursorStore.settings.enabled" class="space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                摩擦系数: {{ cursorStore.settings.friction.toFixed(2) }}
              </label>
              <input
                type="range"
                min="0.1"
                max="1"
                step="0.05"
                :value="cursorStore.settings.friction"
                @input="cursorStore.updateSetting('friction', parseFloat(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                轨迹数量: {{ cursorStore.settings.trails }}
              </label>
              <input
                type="range"
                min="1"
                max="50"
                step="1"
                :value="cursorStore.settings.trails"
                @input="cursorStore.updateSetting('trails', parseInt(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                大小: {{ cursorStore.settings.size }}
              </label>
              <input
                type="range"
                min="10"
                max="200"
                step="5"
                :value="cursorStore.settings.size"
                @input="cursorStore.updateSetting('size', parseInt(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                阻尼系数: {{ cursorStore.settings.dampening.toFixed(2) }}
              </label>
              <input
                type="range"
                min="0.01"
                max="1"
                step="0.01"
                :value="cursorStore.settings.dampening"
                @input="cursorStore.updateSetting('dampening', parseFloat(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                张力系数: {{ cursorStore.settings.tension.toFixed(2) }}
              </label>
              <input
                type="range"
                min="0.9"
                max="0.999"
                step="0.001"
                :value="cursorStore.settings.tension"
                @input="cursorStore.updateSetting('tension', parseFloat(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                透明度: {{ (cursorStore.settings.opacity * 100).toFixed(0) }}%
              </label>
              <input
                type="range"
                min="0.05"
                max="1"
                step="0.05"
                :value="cursorStore.settings.opacity"
                @input="cursorStore.updateSetting('opacity', parseFloat(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">
                线条宽度: {{ cursorStore.settings.lineWidth }}px
              </label>
              <input
                type="range"
                min="0.5"
                max="5"
                step="0.5"
                :value="cursorStore.settings.lineWidth"
                @input="cursorStore.updateSetting('lineWidth', parseFloat(($event.target as HTMLInputElement).value))"
                class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div class="space-y-2">
              <label class="text-sm font-medium text-foreground">颜色</label>
              <div class="flex items-center gap-3">
                <input
                  type="color"
                  :value="cursorStore.settings.color"
                  @input="cursorStore.updateSetting('color', ($event.target as HTMLInputElement).value)"
                  class="h-10 w-16 rounded cursor-pointer border border-border"
                />
                <input
                  type="text"
                  :value="cursorStore.settings.color"
                  @input="cursorStore.updateSetting('color', ($event.target as HTMLInputElement).value)"
                  class="flex-1 h-10 px-3 rounded bg-muted border border-border text-foreground text-sm"
                />
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between py-4 border-t border-border">
            <div>
              <div class="font-medium text-foreground">禁用移动端效果</div>
              <div class="text-sm text-muted-foreground">在触摸屏设备上隐藏光标效果</div>
            </div>
            <button
              @click="cursorStore.updateSetting('disableMobile', !cursorStore.settings.disableMobile)"
              class="h-6 w-11 rounded-full transition-colors relative"
              :class="cursorStore.settings.disableMobile ? 'bg-blue-600' : 'bg-muted'"
            >
              <div
                class="absolute top-1 h-4 w-4 rounded-full bg-white transition-transform"
                :class="cursorStore.settings.disableMobile ? 'right-1' : 'left-1'"
              ></div>
            </button>
          </div>

          <div class="pt-4 border-t border-border">
            <button
              @click="cursorStore.resetSettings"
              class="px-4 py-2 bg-muted hover:bg-muted/80 text-foreground rounded-lg transition-colors text-sm"
            >
              恢复默认设置
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useCursorStore } from '@/stores/cursor'

defineOptions({ name: 'SettingsView' })

const cursorStore = useCursorStore()
</script>
