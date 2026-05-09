<script setup lang="ts">
import Sidebar from './Sidebar.vue';
import Header from './Header.vue';
import SleekLineCursor from '@/components/ui/sleek-line-cursor/SleekLineCursor.vue';
import { useLayoutStore } from '@/stores/layout';
import { useCursorStore } from '@/stores/cursor';

const layoutStore = useLayoutStore();
const cursorStore = useCursorStore();
</script>

<template>
  <div class="min-h-screen bg-background text-foreground selection:bg-blue-500/30 transition-colors duration-300">
    <SleekLineCursor
      v-if="cursorStore.settings.enabled"
      :friction="cursorStore.settings.friction"
      :trails="cursorStore.settings.trails"
      :size="cursorStore.settings.size"
      :dampening="cursorStore.settings.dampening"
      :tension="cursorStore.settings.tension"
      :opacity="cursorStore.settings.opacity"
      :line-width="cursorStore.settings.lineWidth"
      :color="cursorStore.settings.color"
      :disable-mobile="cursorStore.settings.disableMobile"
    />

    <Sidebar />
    
    <div 
      class="relative z-10 flex min-h-screen flex-col transition-all duration-300"
      :class="[layoutStore.isSidebarCollapsed ? 'pl-[64px]' : 'pl-[240px]']"
    >
      <Header />
      
      <main class="flex-1 p-8">
        <RouterView v-slot="{ Component }">
          <Transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
  filter: blur(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
  filter: blur(10px);
}
</style>

