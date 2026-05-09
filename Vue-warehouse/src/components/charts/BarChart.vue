<template>
  <div class="glass-card p-6 relative">
    <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10 rounded-xl">
      <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
      <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
      <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
    </div>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-4">
      <h3 class="text-lg font-medium text-foreground/80">产品库存数量统计</h3>
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex flex-wrap items-center gap-2">
          <el-select v-model="selectedWarehouse" placeholder="选择仓库" class="!w-32 flex-shrink-0 glass-select">
            <el-option
              v-for="wh in WAREHOUSES"
              :key="wh.key"
              :label="wh.name"
              :value="wh.key"
            />
          </el-select>
          <el-select v-model="selectedOperation" placeholder="选择运营" class="!w-32 flex-shrink-0 glass-select">
            <el-option
              v-for="op in OPERATIONS"
              :key="op.key"
              :label="op.name"
              :value="op.key"
            />
          </el-select>
          <el-select v-model="pageSize" placeholder="显示数量" class="!w-36 flex-shrink-0 glass-select">
            <el-option
              v-for="size in PAGE_SIZES"
              :key="size"
              :label="`显示${size}个产品`"
              :value="size"
            />
          </el-select>
        </div>
        <div class="flex items-center gap-2 border-l border-border pl-3">
          <div class="relative">
            <div class="group inline-flex">
              <AutoRefresh v-model="autoRefreshEnabled" />
              <div
                class="pointer-events-none absolute -top-9 left-1/2 -translate-x-1/2 opacity-0 transition-opacity group-hover:opacity-100"
              >
                <div
                  class="whitespace-nowrap rounded-md border border-border bg-card/95 px-2 py-1 text-xs text-foreground shadow-lg backdrop-blur"
                >
                  自动刷新
                </div>
              </div>
            </div>
          </div>
          <div class="relative">
            <div class="group inline-flex">
              <button
                type="button"
                class="rounded-lg border border-border p-2 text-sm transition-colors h-[34px] w-[34px] flex items-center justify-center"
                :class="
                  hideZeroStock
                    ? 'bg-primary text-primary-foreground hover:bg-primary/90'
                    : 'bg-muted/60 text-foreground hover:bg-muted'
                "
                @click="hideZeroStock = !hideZeroStock"
              >
                <component :is="hideZeroStock ? EyeOff : Eye" class="h-4 w-4" />
              </button>
              <div
                class="pointer-events-none absolute -top-9 left-1/2 -translate-x-1/2 opacity-0 transition-opacity group-hover:opacity-100"
              >
                <div
                  class="whitespace-nowrap rounded-md border border-border bg-card/95 px-2 py-1 text-xs text-foreground shadow-lg backdrop-blur"
                >
                  {{ hideZeroStock ? '显示0库存产品' : '隐藏0库存产品' }}
                </div>
              </div>
            </div>
          </div>
          <div class="relative">
            <div class="group inline-flex">
              <button
                class="rounded-lg border border-border bg-muted/60 p-2 text-sm text-foreground transition-colors hover:bg-muted h-[34px] w-[34px] flex items-center justify-center"
                @click="fetchBarData"
              >
                <RefreshCw class="h-4 w-4" />
              </button>
              <div
                class="pointer-events-none absolute -top-9 left-1/2 -translate-x-1/2 opacity-0 transition-opacity group-hover:opacity-100"
              >
                <div
                  class="whitespace-nowrap rounded-md border border-border bg-card/95 px-2 py-1 text-xs text-foreground shadow-lg backdrop-blur"
                >
                  刷新数据
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="h-80 relative">
      <div v-if="barLoading" class="flex items-center justify-center h-full">
        <div class="scale-90">
          <LoadStyleData />
        </div>
      </div>
      <v-chart v-else-if="barData.names.length > 0" class="h-full w-full" :option="chartOption" autoresize />
      <div v-else class="flex items-center justify-center h-full">
        <div class="text-muted-foreground text-center">
          <div>暂无数据</div>
          <div class="text-xs mt-1">请切换运营或仓库重试</div>
        </div>
      </div>
    </div>
    <div class="mt-4 flex items-center justify-center gap-2">
      <button
        class="rounded-md bg-muted/60 px-3 py-1 text-sm text-foreground transition hover:bg-muted"
        :disabled="currentPage === 1"
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="text-sm text-muted-foreground">
        第 {{ currentPage }} / {{ totalPages }} 页
      </span>
      <button
        class="rounded-md bg-muted/60 px-3 py-1 text-sm text-foreground transition hover:bg-muted"
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Eye, EyeOff, RefreshCw } from 'lucide-vue-next'
import api from '@/lib/api'
import { getApiPath } from '@/config/env'
import AutoRefresh from '@/components/AutoRefresh.vue'
import LoadStyleData from '@/components/LoadStyleData.vue'
import {
  WAREHOUSES,
  OPERATIONS,
  PAGE_SIZES,
  type Warehouse,
  type Operation
} from '@/config/chartData'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

type LcInventoryItem = {
  product_title: string
  sellable: string | number
}

type YcInventoryItem = {
  productName: string
  productStockDtl?: {
    availableAmount?: number
  }
}

const selectedWarehouse = ref<Warehouse>('YC')
const selectedOperation = ref<Operation>('ma')
const pageSize = ref(15)
const currentPage = ref(1)

const barLoading = ref(false)
const rawBarData = ref<{ names: string[]; values: number[] }>({ names: [], values: [] })
const autoRefreshEnabled = ref(false)
const hideZeroStock = ref(false)
let autoRefreshInterval: ReturnType<typeof setInterval> | undefined = undefined

const operationSuffixMap: Record<Operation, string> = {
  ma: '宇',
  yu: '瑜',
  shuai: '帅',
  shou: '收',
  xu: '序'
}

const filteredBarData = computed(() => {
  if (!hideZeroStock.value) return rawBarData.value

  const names: string[] = []
  const values: number[] = []

  rawBarData.value.values.forEach((value, index) => {
    const numericValue = Number(value) || 0
    if (numericValue !== 0) {
      names.push(rawBarData.value.names[index] || '未知产品')
      values.push(numericValue)
    }
  })

  return { names, values }
})

const totalPages = computed(() => {
  return Math.ceil(filteredBarData.value.names.length / pageSize.value) || 1
})

const barData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return {
    names: filteredBarData.value.names.slice(start, end),
    values: filteredBarData.value.values.slice(start, end)
  }
})

const fetchBarData = async () => {
  barLoading.value = true
  try {
    if (selectedWarehouse.value === 'LC') {
      const payload = {
        pageSize: null,
        page: 1,
        product_sku: '',
        product_sku_arr: [],
        warehouse_code: '',
        warehouse_code_arr: [],
        update_start_time: '',
      }
      const resp = await api.post(getApiPath('/api/getLCWarehouseDataAll'), payload)
      if (resp.data?.code === 200) {
        const items: LcInventoryItem[] = (resp.data?.data || [])
        
        const suffix = operationSuffixMap[selectedOperation.value]
        const filteredItems = items.filter(item => 
          item.product_title && item.product_title.endsWith(`-${suffix}`)
        )
        
        rawBarData.value = {
          names: filteredItems.map(item => item.product_title || '未知产品'),
          values: filteredItems.map(item => Number(item.sellable) || 0)
        }
      } else {
        rawBarData.value = { names: [], values: [] }
      }
    } else {
      const resp = await api.post(getApiPath('/api/getYCWarehouseData'), {
        skuList: '',
        whCodeList: '',
        page: 1,
        pageSize: null,
      })
      if (resp.data?.code === 200) {
        const items: YcInventoryItem[] = (resp.data?.data?.records || [])
        
        const suffix = operationSuffixMap[selectedOperation.value]
        const filteredItems = items.filter(item => 
          item.productName && item.productName.endsWith(`-${suffix}`)
        )
        
        rawBarData.value = {
          names: filteredItems.map(item => item.productName || '未知产品'),
          values: filteredItems.map(item => Number(item.productStockDtl?.availableAmount ?? 0))
        }
      } else {
        rawBarData.value = { names: [], values: [] }
      }
    }
  } catch (error) {
    console.error('获取柱状图数据失败:', error)
    rawBarData.value = { names: [], values: [] }
  } finally {
    barLoading.value = false
  }
}

const silentFetchBarData = async () => {
  try {
    if (selectedWarehouse.value === 'LC') {
      const payload = {
        pageSize: null,
        page: 1,
        product_sku: '',
        product_sku_arr: [],
        warehouse_code: '',
        warehouse_code_arr: [],
        update_start_time: '',
      }
      const resp = await api.post(getApiPath('/api/getLCWarehouseDataAll'), payload)
      if (resp.data?.code === 200) {
        const items: LcInventoryItem[] = (resp.data?.data || [])
        
        const suffix = operationSuffixMap[selectedOperation.value]
        const filteredItems = items.filter(item => 
          item.product_title && item.product_title.endsWith(`-${suffix}`)
        )
        
        rawBarData.value = {
          names: filteredItems.map(item => item.product_title || '未知产品'),
          values: filteredItems.map(item => Number(item.sellable) || 0)
        }
      } else {
        rawBarData.value = { names: [], values: [] }
      }
    } else {
      const resp = await api.post(getApiPath('/api/getYCWarehouseData'), {
        skuList: '',
        whCodeList: '',
        page: 1,
        pageSize: null,
      })
      if (resp.data?.code === 200) {
        const items: YcInventoryItem[] = (resp.data?.data?.records || [])
        
        const suffix = operationSuffixMap[selectedOperation.value]
        const filteredItems = items.filter(item => 
          item.productName && item.productName.endsWith(`-${suffix}`)
        )
        
        rawBarData.value = {
          names: filteredItems.map(item => item.productName || '未知产品'),
          values: filteredItems.map(item => Number(item.productStockDtl?.availableAmount ?? 0))
        }
      } else {
        rawBarData.value = { names: [], values: [] }
      }
    }
  } catch (error) {
    console.error('获取柱状图数据失败:', error)
  }
}

const startAutoRefresh = () => {
  if (autoRefreshInterval !== undefined) window.clearInterval(autoRefreshInterval)
  autoRefreshInterval = window.setInterval(() => {
    silentFetchBarData()
  }, 10 * 1000)
}

const stopAutoRefresh = () => {
  if (autoRefreshInterval !== undefined) {
    window.clearInterval(autoRefreshInterval)
    autoRefreshInterval = undefined
  }
}

watch([selectedWarehouse, selectedOperation, pageSize], () => {
  currentPage.value = 1
  fetchBarData()
})

watch(hideZeroStock, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages
})

watch(autoRefreshEnabled, (enabled) => {
  if (enabled) startAutoRefresh()
  else stopAutoRefresh()
})

const chartData = computed(() => barData.value)

const barColor = computed(() => {
  const color = OPERATIONS.find(op => op.key === selectedOperation.value)?.color || '#3b82f6'
  const lighterColor = OPERATIONS.find(op => op.key === selectedOperation.value)?.color || '#60a5fa'
  return { color, lighterColor }
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderColor: '#e5e7eb',
    borderWidth: 1,
    textStyle: { color: '#374151' },
    formatter: (params: { name: string; value: number }[] | undefined) => {
      if (!params || params.length === 0) return ''
      const param = params[0]!
      return `<div style="padding: 4px;">
        <div style="font-weight: 600; margin-bottom: 4px;">${param.name}</div>
        <div>库存数量: <span style="color: ${barColor.value.color}; font-weight: 600;">${param.value}</span></div>
      </div>`
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '8%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: chartData.value.names,
    axisLabel: {
      rotate: 30,
      interval: 0,
      color: '#9ca3af',
      fontSize: 11
    },
    axisLine: { lineStyle: { color: '#e5e7eb' } },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    name: '库存数量',
    nameTextStyle: { color: '#9ca3af', padding: [0, 0, 0, 20] },
    axisLabel: { color: '#9ca3af' },
    splitLine: { lineStyle: { color: '#f3f4f6' } }
  },
  series: [
    {
      name: '库存数量',
      type: 'bar',
      data: chartData.value.values,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: barColor.value.lighterColor },
            { offset: 1, color: barColor.value.color }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: barColor.value.color },
              { offset: 1, color: barColor.value.color }
            ]
          }
        }
      },
      animationDuration: 800,
      animationEasing: 'cubicOut'
    }
  ]
}))

onMounted(() => {
  fetchBarData()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>
