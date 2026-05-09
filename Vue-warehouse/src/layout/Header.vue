<script setup lang="ts">
import { Search, Bell, Moon, Sun } from 'lucide-vue-next';
import { useRoute } from 'vue-router';
import { useLayoutStore } from '@/stores/layout';

defineOptions({ name: 'AppHeader' });

const route = useRoute();
const layoutStore = useLayoutStore();
</script>

<template>
  <header class="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-card/80 px-6 backdrop-blur-md transition-all">
    <!-- Left: Breadcrumb / Title -->
    <div class="flex items-center gap-2">
      <h2 class="text-lg font-medium text-foreground">
        {{ route.meta.title || 'Dashboard' }}
      </h2>
    </div>

    <!-- Center: Search -->
    <div class="absolute left-1/2 top-1/2 hidden w-full max-w-md -translate-x-1/2 -translate-y-1/2 md:block">
      <div class="relative group">
        <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground group-focus-within:text-blue-400 transition-colors" />
        <input 
          type="text" 
          placeholder="Search..." 
          class="h-10 w-full rounded-full border border-border bg-muted/50 px-10 text-sm text-foreground placeholder-muted-foreground outline-none backdrop-blur-sm transition-all focus:w-[105%] focus:-translate-x-[2.5%] focus:border-blue-500/50 focus:bg-muted focus:shadow-[0_0_20px_rgba(59,130,246,0.1)]"
        />
      </div>
    </div>

    <!-- Right: Actions -->
    <div class="flex items-center gap-4">
      <button 
        @click="layoutStore.toggleTheme"
        class="flex h-9 w-9 items-center justify-center rounded-full text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
      >
        <Moon v-if="layoutStore.isDark" class="h-5 w-5" />
        <Sun v-else class="h-5 w-5" />
      </button>

      <button class="relative flex h-9 w-9 items-center justify-center rounded-full text-muted-foreground hover:bg-muted hover:text-foreground transition-colors">
        <Bell class="h-5 w-5" />
        <span class="absolute right-2 top-2 h-2 w-2 rounded-full bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.6)]"></span>
      </button>

      <button class="h-9 w-9 overflow-hidden rounded-full shadow-lg ring-2 ring-white/10 hover:ring-white/20 transition-all">
        <img 
          src="https://patchwiki.biligame.com/images/lysk/4/47/m08jf8cnjikohmjuiqvfmcv174ct98x.png" 
          alt="用户头像"
          class="h-full w-full object-cover"
        />
      </button>
    </div>
  </header>
</template>
