<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="text-3xl font-bold text-foreground">数据统计</h1>
        <p class="mt-1 text-1xl text-muted-foreground">数据可能有误，请将标题务必以-x结尾，否则识别不到👿</p>
      </div>
      <button
        class="inline-flex items-center gap-2 rounded-full border border-border bg-muted/60 px-4 py-2 text-sm text-foreground transition hover:bg-muted"
        :disabled="loading"
        @click="fetchCounts"
      >
        <span
          class="h-2 w-2 rounded-full"
          :class="loading ? 'bg-blue-400 animate-pulse' : 'bg-emerald-400'"
        ></span>
        刷新数据
      </button>
    </div>

    <div v-if="error" class="glass-card p-6 relative overflow-hidden">
      <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
        <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
        <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
      </div>
      <div class="text-base font-medium text-foreground">数据加载失败</div>
      <div class="mt-2 text-sm text-muted-foreground">{{ error }}</div>
    </div>

    <div v-else-if="loading" class="flex min-h-[400px] items-center justify-center">
      <div class="scale-100 opacity-90 text-foreground">
        <LoadStyleData />
      </div>
    </div>

    <Transition name="fade-slide" mode="out-in">
      <div v-if="!loading && !error" class="grid gap-6 xl:grid-cols-2">
        <div class="glass-card p-6 relative overflow-hidden">
          <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
            <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
            <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
            <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
          </div>
          <div class="flex items-start justify-between">
            <div>
              <div class="text-sm text-muted-foreground">良仓</div>
              <div class="mt-1 text-lg font-medium text-foreground">运营人员产品量</div>
            </div>
            <div class="rounded-full bg-blue-500/15 px-3 py-1 text-xs font-semibold text-blue-400">
              LC
            </div>
          </div>
          <div class="mt-5 text-4xl font-semibold text-foreground">{{ lcTotal }}</div>
          <div class="mt-1 text-sm text-muted-foreground">产品总数</div>
          <div class="mt-6">
            <VueDraggable
              v-model="lcList"
              :animation="150"
              ghostClass="opacity-50"
              class="space-y-4"
              :delay="300"
              :delayOnTouchOnly="false"
            >
              <div
                v-for="item in lcList"
                :key="item.key"
                class="space-y-2 cursor-grab active:cursor-grabbing select-none"
                title="长按可拖拽排序"
              >
                <div class="flex items-center justify-between text-sm">
                  <div class="flex items-center gap-2">
                    <span class="h-2.5 w-2.5 rounded-full" :class="item.dotClass"></span>
                    <span class="text-foreground">{{ item.name }}</span>
                  </div>
                  <span class="text-muted-foreground">{{ item.value }}</span>
                </div>
                <div class="h-2 rounded-full bg-muted/60">
                  <div
                    class="h-2 rounded-full transition-all"
                    :class="item.barClass"
                    :style="{ width: `${(item.value / lcMax) * 100}%` }"
                  ></div>
                </div>
              </div>
            </VueDraggable>
          </div>
        </div>

        <div class="glass-card p-6 relative overflow-hidden">
          <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
            <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
            <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
            <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
          </div>
          <div class="flex items-start justify-between">
            <div>
              <div class="text-sm text-muted-foreground">翼仓</div>
              <div class="mt-1 text-lg font-medium text-foreground">运营人员产品量</div>
            </div>
            <div class="rounded-full bg-purple-500/15 px-3 py-1 text-xs font-semibold text-purple-300">
              YC
            </div>
          </div>
          <div class="mt-5 text-4xl font-semibold text-foreground">{{ ycTotal }}</div>
          <div class="mt-1 text-sm text-muted-foreground">产品总数</div>
          <div class="mt-6">
            <VueDraggable
              v-model="ycList"
              :animation="150"
              ghostClass="opacity-50"
              class="space-y-4"
              :delay="300"
              :delayOnTouchOnly="false"
            >
              <div
                v-for="item in ycList"
                :key="item.key"
                class="space-y-2 cursor-grab active:cursor-grabbing select-none"
                title="长按可拖拽排序"
              >
                <div class="flex items-center justify-between text-sm">
                  <div class="flex items-center gap-2">
                    <span class="h-2.5 w-2.5 rounded-full" :class="item.dotClass"></span>
                    <span class="text-foreground">{{ item.name }}</span>
                  </div>
                  <span class="text-muted-foreground">{{ item.value }}</span>
                </div>
                <div class="h-2 rounded-full bg-muted/60">
                  <div
                    class="h-2 rounded-full transition-all"
                    :class="item.barClass"
                    :style="{ width: `${(item.value / ycMax) * 100}%` }"
                  ></div>
                </div>
              </div>
            </VueDraggable>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import api from '@/lib/api'
import { getApiPath } from '@/config/env'
import { computed, onMounted, ref } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import LoadStyleData from '@/components/LoadStyleData.vue'

defineOptions({ name: 'AnalyticsView' })

type ApiResult<T> = {
  code: number
  message?: string
  data?: T
}

type CountData = {
  ma: number
  yu: number
  shuai: number
  shou: number
  xu: number
}

type StaffMeta = {
  key: keyof CountData
  name: string
  dotClass: string
  barClass: string
}

type StaffItem = StaffMeta & {
  value: number
}

const initialStaffOrder: StaffMeta[] = [
  {
    key: 'ma',
    name: '马振宇',
    dotClass: 'bg-blue-500',
    barClass: 'bg-gradient-to-r from-blue-500 to-cyan-400',
  },
  {
    key: 'yu',
    name: '刘瑜琦',
    dotClass: 'bg-indigo-500',
    barClass: 'bg-gradient-to-r from-indigo-500 to-violet-400',
  },
  {
    key: 'shuai',
    name: '薄铭帅',
    dotClass: 'bg-amber-500',
    barClass: 'bg-gradient-to-r from-amber-500 to-orange-400',
  },
  {
    key: 'shou',
    name: '王丰收',
    dotClass: 'bg-rose-500',
    barClass: 'bg-gradient-to-r from-rose-500 to-pink-400',
  },
  {
    key: 'xu',
    name: '尹文序',
    dotClass: 'bg-emerald-500',
    barClass: 'bg-gradient-to-r from-emerald-500 to-lime-400',
  },
]

const emptyCounts: CountData = { ma: 0, yu: 0, shuai: 0, shou: 0, xu: 0 }

const lcData = ref<CountData>({ ...emptyCounts })
const ycData = ref<CountData>({ ...emptyCounts })
const loading = ref(false)
const error = ref('')

const lcList = ref<StaffItem[]>([])
const ycList = ref<StaffItem[]>([])

const toNumber = (val: unknown) => {
  const num = Number(val ?? 0)
  return Number.isFinite(num) ? num : 0
}

const buildList = (data: CountData): StaffItem[] =>
  initialStaffOrder.map((meta) => ({
    ...meta,
    value: toNumber(data[meta.key]),
  }))

const sumCounts = (data: CountData) =>
  toNumber(data.ma) + toNumber(data.yu) + toNumber(data.shuai) + toNumber(data.shou) + toNumber(data.xu)

const lcTotal = computed(() => sumCounts(lcData.value))
const ycTotal = computed(() => sumCounts(ycData.value))

const getMax = (list: { value: number }[]) =>
  list.reduce((acc, item) => Math.max(acc, item.value), 1)

const lcMax = computed(() => getMax(lcList.value))
const ycMax = computed(() => getMax(ycList.value))

const normalizeCounts = (data: CountData | undefined) => ({
  ma: toNumber(data?.ma),
  yu: toNumber(data?.yu),
  shuai: toNumber(data?.shuai),
  shou: toNumber(data?.shou),
  xu: toNumber(data?.xu),
})

const fetchCounts = async () => {
  loading.value = true
  error.value = ''
  try {
    const [lcResp, ycResp] = await Promise.all([
      api.get<ApiResult<CountData>>(getApiPath('/api/getLCWarehouseDataCount')),
      api.get<ApiResult<CountData>>(getApiPath('/api/getYCWarehouseDataCount')),
    ])

    const lcRoot = lcResp.data
    const ycRoot = ycResp.data

    if (lcRoot?.code !== 200) {
      throw new Error(lcRoot?.message || '获取良仓统计失败')
    }
    if (ycRoot?.code !== 200) {
      throw new Error(ycRoot?.message || '获取翼仓统计失败')
    }

    lcData.value = normalizeCounts(lcRoot?.data)
    ycData.value = normalizeCounts(ycRoot?.data)

    lcList.value = buildList(lcData.value).sort((a, b) => b.value - a.value)
    ycList.value = buildList(ycData.value).sort((a, b) => b.value - a.value)

  } catch (err) {
    error.value = err instanceof Error ? err.message : '数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(fetchCounts)
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 300ms ease, transform 300ms ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
