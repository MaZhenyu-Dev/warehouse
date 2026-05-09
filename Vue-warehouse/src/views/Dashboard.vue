<template>
  <div class="space-y-6">
    <h1 class="text-3xl font-bold text-foreground">仪表盘</h1>
    <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
      <!-- 时钟组件占据2列 -->
      <ClockCard class="md:col-span-2" />
      
      <!-- 美国时区卡片 -->
      <UsTimeZoneCard />
      
      <!-- 美国节日卡片 -->
      <UsHolidayCard />
    </div>
    <div class="grid gap-6 lg:grid-cols-5">
      <BarChart class="lg:col-span-3" />
      <PieChart class="lg:col-span-2" />
    </div>
  </div>
</template>

<script setup lang="ts">
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import ClockCard from '@/components/ClockCard.vue'
import UsTimeZoneCard from '@/components/dashboard/UsTimeZoneCard.vue'
import UsHolidayCard from '@/components/dashboard/UsHolidayCard.vue'

defineOptions({ name: 'DashboardView' })
</script>
