<template>
  <div>
    <div class="space-y-6">
      <h1 class="text-3xl font-bold text-foreground">良仓管理</h1>
      <div class="glass-card p-6 min-h-[500px] relative overflow-hidden">
        <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
          <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
          <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
          <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
        </div>
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-medium text-foreground/80">库存列表</h2>
          <div class="flex flex-wrap items-center justify-end gap-4">
            <div class="relative group w-64 md:w-80">
              <Search
                class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground group-focus-within:text-blue-400 transition-colors"
              />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索 SKU / 产品名称"
                class="h-10 w-full rounded-full border border-border bg-muted/50 px-10 text-sm text-foreground placeholder-muted-foreground outline-none backdrop-blur-sm transition-all focus:border-blue-500/50 focus:bg-muted focus:shadow-[0_0_20px_rgba(59,130,246,0.15)]"
              />
            </div>

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

            <button
              class="rounded-lg bg-primary text-primary-foreground px-4 py-2 text-sm hover:bg-primary/90 transition-colors disabled:opacity-60 disabled:hover:bg-primary"
              :disabled="loading"
              @click="reload"
            >
              刷新数据
            </button>
          </div>
        </div>

        <div class="relative w-full">
          <div v-if="loading" class="flex min-h-[400px] items-center justify-center">
            <div class="scale-90">
              <LoadStyle />
            </div>
          </div>

          <div v-else>
            <Transition name="fade-slide" mode="out-in">
              <div v-if="tableData.length > 0" class="table-fade-in">
                <ElTable
                  :data="tableData"
                  border
                  style="width: 100%"
                  class="lc-ep-table rounded-lg overflow-hidden"
                  :row-key="rowKey"
                >
                  <ElTableColumn prop="product_sku" label="SKU" min-width="140" />
                  <ElTableColumn prop="product_title" label="产品名称" min-width="240" show-overflow-tooltip />
                  <ElTableColumn prop="warehouse_code" label="仓库代码" min-width="120" />
                  <ElTableColumn
                    prop="sellable"
                    label="可售"
                    min-width="90"
                    align="right"
                    sortable
                    :sort-method="sortBySellable"
                  />
                  <ElTableColumn
                    prop="reserved"
                    label="待出库"
                    min-width="90"
                    align="right"
                    sortable
                    :sort-method="sortByReserved"
                  />
                  <ElTableColumn
                    prop="onway"
                    label="在途"
                    min-width="90"
                    align="right"
                    sortable
                    :sort-method="sortByOnway"
                  />
                  <ElTableColumn
                    prop="pending"
                    label="待上架"
                    min-width="90"
                    align="right"
                    sortable
                    :sort-method="sortByPending"
                  />
                  <ElTableColumn
                    prop="unsellable"
                    label="不合格"
                    min-width="90"
                    align="right"
                    sortable
                    :sort-method="sortByUnsellable"
                  />
                  <ElTableColumn
                    prop="shipped"
                    label="历史出库"
                    min-width="100"
                    align="right"
                    sortable
                    :sort-method="sortByShipped"
                  />
                </ElTable>
              </div>

              <div v-else class="text-muted-foreground text-center py-20">
                暂无数据
              </div>
            </Transition>

            <Transition name="fade-slide" mode="out-in">
              <div v-if="total > 0" class="mt-4 flex justify-end">
                <ElPagination
                  class="lc-ep-pagination"
                  v-model:current-page="page"
                  v-model:page-size="pageSize"
                  :total="total"
                  :page-sizes="[15, 30, 50, 100, 300]"
                  layout="total, sizes, prev, pager, next, jumper"
                />
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>

    <Transition name="sku-empty-modal" appear>
      <div
        v-if="skuEmptyModalOpen"
        class="lc-modal-overlay fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
        @click.self="closeSkuEmptyModal"
      >
        <div class="lc-modal-panel w-full max-w-md rounded-xl border border-border bg-card text-card-foreground shadow-2xl">
          <div class="px-6 pt-5 pb-4">
            <div class="text-xl font-semibold">提示</div>
            <div class="mt-2 text-base text-muted-foreground">
              飞书表格中SKU为空，请添加SKU后重试
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4">
            <button
              class="rounded-lg bg-primary text-primary-foreground px-4 py-2 text-sm transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              @click="closeSkuEmptyModal"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import api from '@/lib/api'
import { getApiPath } from '@/config/env'
import { Search } from 'lucide-vue-next'
import { onMounted, onUnmounted, ref, watch } from 'vue'
import 'element-plus/es/components/loading/style/css'
import AutoRefresh from '@/components/AutoRefresh.vue'
import LoadStyle from '@/components/LoadStyle.vue'

type LcInventoryItem = {
  product_sku: string
  product_title: string
  warehouse_code: string
  sellable: string | number
  reserved: string | number
  onway: string | number
  pending: string | number
  unsellable: string | number
  shipped: string | number
}

type ApiResult<T> = {
  code: number
  message?: string
  data?: T
}

type LcSkuData = {
  product_sku_arr?: string[]
}

type LcInventoryData = {
  count?: string | number
  total?: string | number
  data?: LcInventoryItem[]
  list?: LcInventoryItem[]
}

const skuArr = ref<string[]>([])
const tableData = ref<LcInventoryItem[]>([])
const loading = ref(false)
const skuEmptyModalOpen = ref(false)
const searchKeyword = ref('')
const autoRefreshEnabled = ref(false)
const fetching = ref(false)

const page = ref(1)
const pageSize = ref(15)
const total = ref(0)

let cachedAllRecords: LcInventoryItem[] | undefined
let cachedAllRecordsKeyword = ''
let cachedAllRecordsSkuArr = ''

const rowKey = (row: LcInventoryItem) => `${row.product_sku}-${row.warehouse_code}`

const suppressAutoFetch = ref(false)
let autoFetchTimer: number | undefined
let searchDebounceTimer: number | undefined
let autoRefreshInterval: number | undefined

const toNumber = (val: unknown): number => {
  if (typeof val === 'number') return val
  const n = Number(val)
  return Number.isFinite(n) ? n : 0
}

const sortByNumberField = (field: keyof LcInventoryItem) => {
  return (a: LcInventoryItem, b: LcInventoryItem) => {
    return toNumber(a[field]) - toNumber(b[field])
  }
}

const sortBySellable = sortByNumberField('sellable')
const sortByReserved = sortByNumberField('reserved')
const sortByOnway = sortByNumberField('onway')
const sortByPending = sortByNumberField('pending')
const sortByUnsellable = sortByNumberField('unsellable')
const sortByShipped = sortByNumberField('shipped')

const closeSkuEmptyModal = () => {
  skuEmptyModalOpen.value = false
}

const normalizeArray = (val: unknown): string[] => {
  if (!Array.isArray(val)) return []
  return val
    .map((s) => (typeof s === 'string' ? s.trim() : String(s)))
    .filter((s) => s.length > 0)
}

const fetchSkuArr = async (): Promise<boolean> => {
  const resp = await api.get<ApiResult<LcSkuData>>(
    getApiPath('/api/warehouse-lc/records/searchSKU'),
  )
  const root = resp.data
  if (root?.code !== 200) {
    throw new Error(root?.message || 'searchSKU 请求失败')
  }
  const arr = normalizeArray(root?.data?.product_sku_arr)
  skuArr.value = arr

  if (arr.length === 0) {
    skuEmptyModalOpen.value = true
    return false
  }
  return true
}

const normalizeTotal = (val: unknown): number => {
  const n = typeof val === 'number' ? val : Number(val)
  return Number.isFinite(n) && n >= 0 ? n : 0
}

const resetTable = () => {
  tableData.value = []
  total.value = 0
  cachedAllRecords = undefined
  cachedAllRecordsKeyword = ''
  cachedAllRecordsSkuArr = ''
}

const normalizeSearchKeyword = (val: string) => {
  const trimmed = val.trim()
  if (!trimmed) return ''
  return trimmed.replace(/^[^0-9A-Za-z._\-\u4e00-\u9fa5]+|[^0-9A-Za-z._\-\u4e00-\u9fa5]+$/g, '')
}

const buildSearchParams = () => {
  const rawKeyword = searchKeyword.value.trim()
  if (!rawKeyword) return { mode: 'none' as const, skuKeyword: '', titleKeyword: '' }

  const skuLike = /^[0-9A-Za-z._-]+$/.test(rawKeyword)
  if (skuLike) return { mode: 'sku' as const, skuKeyword: rawKeyword, titleKeyword: '' }

  const titleKeyword = normalizeSearchKeyword(rawKeyword)
  if (!titleKeyword) return { mode: 'none' as const, skuKeyword: '', titleKeyword: '' }
  return { mode: 'title' as const, skuKeyword: '', titleKeyword }
}

const filterSkuArrByKeyword = (list: string[], keyword: string) => {
  const lower = keyword.trim().toLowerCase()
  if (!lower) return list
  return list.filter((sku) => sku.toLowerCase().includes(lower))
}

const normalizeInventoryItems = (dataObj: unknown) => {
  if (Array.isArray(dataObj)) return dataObj as LcInventoryItem[]
  const obj = (dataObj ?? {}) as Record<string, unknown>
  const listRaw = (obj.data ?? obj.list ?? []) as unknown
  return Array.isArray(listRaw) ? (listRaw as LcInventoryItem[]) : []
}

const fetchAllInventoryRecords = async () => {
  if (skuArr.value.length === 0) return [] as LcInventoryItem[]

  const payload = {
    pageSize: null,
    page: 1,
    product_sku: '',
    product_sku_arr: skuArr.value,
    warehouse_code: '',
    warehouse_code_arr: [],
    update_start_time: '',
  }

  const resp = await api.post<ApiResult<unknown>>(
    getApiPath('/api/getLCWarehouseDataAll'),
    payload,
  )
  const root = resp.data
  if (root?.code !== 200) {
    throw new Error(root?.message || 'getLCWarehouseDataAll 请求失败')
  }
  return normalizeInventoryItems(root?.data)
}

const fetchInventory = async () => {
  if (skuArr.value.length === 0) return

  const { mode, skuKeyword, titleKeyword } = buildSearchParams()
  const requestOnce = async (payload: Record<string, unknown>) => {
    const resp = await api.post<ApiResult<LcInventoryData>>(
      getApiPath('/api/getLCWarehouseData'),
      payload,
    )

    const root = resp.data
    if (root?.code !== 200) {
      throw new Error(root?.message || 'getLCWarehouseData 请求失败')
    }
    const dataObj = root?.data ?? {}
    const items = normalizeInventoryItems(dataObj)
    const total = normalizeTotal(dataObj.count ?? dataObj.total ?? items.length)
    return { items, total }
  }

  if (mode === 'title') {
    const cacheKey = titleKeyword.toLowerCase()
    const skuCacheKey = skuArr.value.join(',')
    if (
      !cachedAllRecords ||
      cachedAllRecordsKeyword !== cacheKey ||
      cachedAllRecordsSkuArr !== skuCacheKey
    ) {
      cachedAllRecords = await fetchAllInventoryRecords()
      cachedAllRecordsKeyword = cacheKey
      cachedAllRecordsSkuArr = skuCacheKey
    }

    const filtered = cachedAllRecords.filter((row) =>
      (row.product_title || '').toLowerCase().includes(cacheKey),
    )
    const start = (page.value - 1) * pageSize.value
    tableData.value = filtered.slice(start, start + pageSize.value)
    total.value = filtered.length
    return
  }

  const effectiveSkuArr = mode === 'sku' ? filterSkuArrByKeyword(skuArr.value, skuKeyword) : skuArr.value
  cachedAllRecords = undefined
  cachedAllRecordsKeyword = ''
  cachedAllRecordsSkuArr = ''

  if (effectiveSkuArr.length === 0) {
    tableData.value = []
    total.value = 0
    return
  }

  const basePayload = {
    pageSize: pageSize.value,
    page: page.value,
    product_sku: mode === 'sku' ? skuKeyword : '',
    product_sku_arr: effectiveSkuArr,
    warehouse_code: '',
    warehouse_code_arr: [],
    update_start_time: '',
  }

  const result = await requestOnce(basePayload)

  tableData.value = result.items
  total.value = result.total
}

const scheduleFetchInventory = () => {
  if (suppressAutoFetch.value) return
  if (skuArr.value.length === 0) return
  if (skuEmptyModalOpen.value) return

  if (autoFetchTimer != null) window.clearTimeout(autoFetchTimer)
  autoFetchTimer = window.setTimeout(async () => {
    if (fetching.value) return
    fetching.value = true
    loading.value = true
    try {
      await fetchInventory()
    } catch {
      resetTable()
    } finally {
      loading.value = false
      fetching.value = false
    }
  }, 150)
}

const reload = async () => {
  if (fetching.value) return
  fetching.value = true
  loading.value = true
  try {
    suppressAutoFetch.value = true
    page.value = 1
    const ok = await fetchSkuArr()
    if (!ok) {
      autoRefreshEnabled.value = false
      resetTable()
      return
    }
    await fetchInventory()
  } catch {
    resetTable()
  } finally {
    suppressAutoFetch.value = false
    loading.value = false
    fetching.value = false
  }
}

const silentReload = async () => {
  if (fetching.value) return
  if (skuEmptyModalOpen.value) return
  fetching.value = true
  try {
    suppressAutoFetch.value = true
    const ok = await fetchSkuArr()
    if (!ok) {
      autoRefreshEnabled.value = false
      resetTable()
      return
    }
    await fetchInventory()
  } catch {
    resetTable()
  } finally {
    suppressAutoFetch.value = false
    fetching.value = false
  }
}

const startAutoRefresh = () => {
  if (autoRefreshInterval != null) window.clearInterval(autoRefreshInterval)
  autoRefreshInterval = window.setInterval(() => {
    void silentReload()
  }, 5 * 60 * 1000)
  void silentReload()
}

const stopAutoRefresh = () => {
  if (autoRefreshInterval != null) window.clearInterval(autoRefreshInterval)
  autoRefreshInterval = undefined
}

watch(pageSize, () => {
  if (suppressAutoFetch.value) return
  if (page.value !== 1) page.value = 1
  scheduleFetchInventory()
})

watch(page, () => {
  scheduleFetchInventory()
})

watch(searchKeyword, () => {
  if (suppressAutoFetch.value) return
  if (searchDebounceTimer != null) window.clearTimeout(searchDebounceTimer)
  searchDebounceTimer = window.setTimeout(() => {
    if (skuArr.value.length === 0) return
    if (skuEmptyModalOpen.value) return
    if (page.value !== 1) {
      suppressAutoFetch.value = true
      page.value = 1
      suppressAutoFetch.value = false
    }
    scheduleFetchInventory()
  }, 300)
})

watch(autoRefreshEnabled, (enabled) => {
  if (enabled) startAutoRefresh()
  else stopAutoRefresh()
})

onMounted(async () => {
  await reload()
})

onUnmounted(() => {
  stopAutoRefresh()
  if (autoFetchTimer != null) window.clearTimeout(autoFetchTimer)
  if (searchDebounceTimer != null) window.clearTimeout(searchDebounceTimer)
})
</script>

<style scoped>
:deep(.lc-ep-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-text-color: hsl(var(--foreground));
  --el-table-header-text-color: hsl(var(--foreground));
  --el-table-border-color: hsl(var(--border));
  --el-table-row-hover-bg-color: hsl(var(--muted));
}

:deep(.lc-ep-table .el-table__header-wrapper th.el-table__cell) {
  background: hsl(var(--card) / 0.35);
}

:deep(.lc-ep-table .el-table__body-wrapper td.el-table__cell) {
  background: transparent;
}

:deep(.lc-ep-pagination) {
  --el-bg-color: transparent;
  --el-fill-color-blank: transparent;
  --el-text-color-primary: hsl(var(--foreground));
  --el-text-color-regular: hsl(var(--foreground));
  --el-border-color: hsl(var(--border));
}

.sku-empty-modal-enter-active,
.sku-empty-modal-leave-active {
  transition: opacity 180ms ease;
}

.sku-empty-modal-enter-from,
.sku-empty-modal-leave-to {
  opacity: 0;
}

.sku-empty-modal-enter-active .lc-modal-panel,
.sku-empty-modal-leave-active .lc-modal-panel {
  transition: transform 180ms ease, opacity 180ms ease;
}

.sku-empty-modal-enter-from .lc-modal-panel,
.sku-empty-modal-leave-to .lc-modal-panel {
  transform: translateY(8px) scale(0.98);
  opacity: 0;
}

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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.table-fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}
</style>
