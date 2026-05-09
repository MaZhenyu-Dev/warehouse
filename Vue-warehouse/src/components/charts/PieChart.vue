<template>
  <div class="glass-card p-6 h-full relative overflow-hidden">
    <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
      <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
      <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
      <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
    </div>
    <div class="mb-4 flex items-center justify-between gap-4">
      <h3 class="text-lg font-medium text-foreground/80 shrink-0">运营产品分布</h3>
      <el-select v-model="selectedWarehouse" placeholder="选择仓库" class="!w-32 glass-select">
        <el-option
          v-for="wh in WAREHOUSES"
          :key="wh.key"
          :label="wh.name"
          :value="wh.key"
        />
      </el-select>
    </div>
    <div class="h-[calc(100%-80px)] relative">
      <div v-if="loading" class="flex items-center justify-center h-full">
        <div class="text-muted-foreground">加载中...</div>
      </div>
      <v-chart v-else class="h-full w-full" :option="chartOption" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import api from '@/lib/api'
import { getApiPath } from '@/config/env'
import {
  WAREHOUSES,
  OPERATIONS,
  type Warehouse
} from '@/config/chartData'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

const selectedWarehouse = ref<Warehouse>('YC')
const pieData = ref<{ name: string; value: number; color: string }[]>([])
const loading = ref(false)

const fetchPieData = async () => {
  loading.value = true
  try {
    const apiPath = selectedWarehouse.value === 'LC'
      ? '/api/getLCWarehouseDataCount'
      : '/api/getYCWarehouseDataCount'
    const resp = await api.get(getApiPath(apiPath))
    if (resp.data?.code === 200) {
      const data = resp.data?.data || { ma: 0, yu: 0, shuai: 0, shou: 0, xu: 0 }
      pieData.value = OPERATIONS.map(op => ({
        name: op.name,
        value: data[op.key] || 0,
        color: op.color
      }))
    }
  } catch (error) {
    console.error('获取扇形图数据失败:', error)
    pieData.value = []
  } finally {
    loading.value = false
  }
}

watch(selectedWarehouse, fetchPieData)

onMounted(fetchPieData)

const legendSelected = computed(() => {
  const selected: Record<string, boolean> = {}
  pieData.value.forEach(item => {
    selected[item.name] = item.value > 0
  })
  return selected
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e5e7eb',
    borderWidth: 1,
    textStyle: { color: '#374151' },
    formatter: (params: { name: string; value: number; percent: number; color?: string }) => {
      const item = pieData.value.find(d => d.name === params.name)
      return `<div style="padding: 8px;">
        <div style="font-weight: 600; margin-bottom: 4px;">${params.name}</div>
        <div style="color: #6b7280;">产品数量: <span style="color: ${item?.color || '#3b82f6'}; font-weight: 600;">${params.value}</span></div>
        <div style="color: #6b7280;">占比: <span style="font-weight: 600;">${params.percent}%</span></div>
      </div>`
    }
  },
  legend: {
    orient: 'horizontal',
    bottom: 8,
    left: 'center',
    textStyle: { color: '#6b7280', fontSize: 11 },
    itemWidth: 10,
    itemHeight: 10,
    itemGap: 8,
    selected: legendSelected.value
  },
  series: [
    {
      name: '产品分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%',
        color: '#6b7280',
        fontSize: 12
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 20,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      labelLine: {
        show: true,
        smooth: true,
        lineStyle: { color: '#9ca3af' }
      },
      data: pieData.value.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: item.color }
      })),
      animationType: 'scale',
      animationDuration: 800,
      animationEasing: 'cubicOut'
    }
  ]
}))
</script>
