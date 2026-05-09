<script setup lang="ts">
import { 
  LayoutDashboard, 
  Package, 
  Warehouse, 
  BarChart3, 
  Settings, 
  Box,
  ChevronLeft,
  ChevronRight,
  Clock
} from 'lucide-vue-next';
import { useRoute } from 'vue-router';
import { useLayoutStore } from '@/stores/layout';

defineOptions({ name: 'AppSidebar' });

const route = useRoute();
const layoutStore = useLayoutStore();

const menuItems = [
  { name: '仪表盘', path: '/dashboard', icon: LayoutDashboard },
  { name: '良仓管理', path: '/lc-depot', icon: Package },
  { name: '翼仓管理', path: '/yc-depot', icon: Warehouse },
  { name: '数据统计', path: '/analytics', icon: BarChart3 },
  { name: '美国时区', path: '/us-time', icon: Clock },
  { name: '系统设置', path: '/settings', icon: Settings },
];
</script>

<template>
  <aside 
    class="fixed left-0 top-0 z-40 h-screen border-r border-border bg-card/80 backdrop-blur-xl transition-all duration-300 flex flex-col"
    :class="[layoutStore.isSidebarCollapsed ? 'w-[64px]' : 'w-[240px]']"
  >
    <!-- Logo Area -->
    <div class="flex h-16 items-center border-b border-border px-4 transition-all duration-300"
         :class="[layoutStore.isSidebarCollapsed ? 'justify-center' : 'gap-3']">
      <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-blue-500/20 text-blue-400 shadow-[0_0_15px_rgba(59,130,246,0.5)]">
        <Box class="h-5 w-5" />
      </div>
      <span 
        class="text-lg font-bold tracking-wide text-foreground whitespace-nowrap overflow-hidden transition-all duration-300"
        :class="[layoutStore.isSidebarCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100']"
      >
        Ma OS
      </span>
    </div>

    <!-- Navigation -->
    <nav class="mt-6 flex-1 px-2 space-y-1 overflow-y-auto overflow-x-hidden">
      <RouterLink
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="group relative flex items-center rounded-xl py-3 font-medium transition-all duration-200 text-sm"
        :class="[
          layoutStore.isSidebarCollapsed ? 'justify-center px-0' : 'gap-3 px-4',
          route.path === item.path 
            ? 'bg-blue-500/10 text-blue-500 dark:bg-blue-500/20 dark:text-white' 
            : 'text-muted-foreground hover:bg-muted hover:text-foreground'
        ]"
        :title="layoutStore.isSidebarCollapsed ? item.name : ''"
      >
        <!-- Active Indicator Strip -->
        <div 
          v-if="route.path === item.path"
          class="absolute left-0 h-6 w-1 rounded-r-full bg-blue-400 shadow-[0_0_10px_rgba(96,165,250,0.8)]"
        ></div>

        <component :is="item.icon" class="h-5 w-5 shrink-0 transition-transform group-hover:scale-110" />
        <span 
          class="whitespace-nowrap overflow-hidden transition-all duration-300"
          :class="[layoutStore.isSidebarCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100']"
        >
          {{ item.name }}
        </span>
      </RouterLink>
    </nav>

    <!-- Collapse Button -->
    <div class="p-4 border-t border-border flex" :class="[layoutStore.isSidebarCollapsed ? 'justify-center' : 'justify-end']">
      <button 
        @click="layoutStore.toggleSidebar"
        class="flex h-8 w-8 items-center justify-center rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
      >
        <ChevronRight v-if="layoutStore.isSidebarCollapsed" class="h-5 w-5" />
        <ChevronLeft v-else class="h-5 w-5" />
      </button>
    </div>
  </aside>
</template>
