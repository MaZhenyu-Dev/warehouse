<template>
  <div>
    <div class="space-y-6">
      <h1 class="text-3xl font-bold text-foreground">翼仓管理</h1>
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
                  class="yc-ep-table rounded-lg overflow-hidden"
                  :row-key="rowKey"
                >
                  <ElTableColumn prop="sku" label="SKU" min-width="140" />
                  <ElTableColumn prop="productName" label="产品名称" min-width="240" show-overflow-tooltip />
                  <ElTableColumn prop="whCode" label="仓库代码" min-width="120" />
                  <ElTableColumn
                    prop="totalAmount"
                    label="综合库存总库存"
                    min-width="140"
                    align="right"
                    sortable
                    :sort-method="sortByTotalAmount"
                  />
                  <ElTableColumn
                    prop="productTotalAmount"
                    label="产品总库存数量"
                    min-width="140"
                    align="right"
                    sortable
                    :sort-method="sortByProductTotalAmount"
                  />
                  <ElTableColumn label="可用库存" min-width="120" align="right" sortable :sort-method="sortByAvailableAmount">
                    <template #default="{ row }">
                      {{ row.productStockDtl?.availableAmount ?? 0 }}
                    </template>
                  </ElTableColumn>
                  <ElTableColumn label="锁定库存" min-width="120" align="right" sortable :sort-method="sortByLockAmount">
                    <template #default="{ row }">
                      {{ row.productStockDtl?.lockAmount ?? 0 }}
                    </template>
                  </ElTableColumn>
                  <ElTableColumn
                    label="在途库存"
                    min-width="120"
                    align="right"
                    sortable
                    :sort-method="sortByTransportAmount"
                  >
                    <template #default="{ row }">
                      {{ row.productStockDtl?.transportAmount ?? 0 }}
                    </template>
                  </ElTableColumn>
                </ElTable>
              </div>

              <div v-else class="text-muted-foreground text-center py-20">
                暂无数据
              </div>
            </Transition>

            <Transition name="fade-slide" mode="out-in">
              <div v-if="total > 0" class="mt-4 flex justify-end">
                <ElPagination
                  class="yc-ep-pagination"
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
        class="yc-modal-overlay fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
        @click.self="closeSkuEmptyModal"
      >
        <div class="yc-modal-panel w-full max-w-md rounded-xl border border-border bg-card text-card-foreground shadow-2xl">
          <div class="px-6 pt-5 pb-4">
            <div class="text-xl font-semibold">提示</div>
            <div class="mt-2 text-base text-muted-foreground">
              飞书表格中SKU为空，请添加SKU后重试😒
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

defineOptions({ name: 'YCDepotView' })

type ApiResult<T> = {
  code: number
  message?: string
  msg?: string
  data?: T
}

type YcSkuData = {
  skuList?: string
}

type YcStockDtl = {
  availableAmount?: number
  lockAmount?: number
  transportAmount?: number
}

type YcInventoryItem = {
  whCode: string
  sku: string
  skuId?: string
  productName: string
  totalAmount: number
  productTotalAmount: number
  productStockDtl?: YcStockDtl
}

type YcInventoryData = {
  records?: YcInventoryItem[]
  total?: number
  page?: number
  pageSize?: number
  pages?: number
}

const skuList = ref('')
const tableData = ref<YcInventoryItem[]>([])
const loading = ref(false)
const skuEmptyModalOpen = ref(false)
const searchKeyword = ref('')
const autoRefreshEnabled = ref(false)
const fetching = ref(false)

const page = ref(1)
const pageSize = ref(15)
const total = ref(0)

const suppressAutoFetch = ref(false)
let autoFetchTimer: number | undefined
let searchDebounceTimer: number | undefined
let autoRefreshInterval: number | undefined

let cachedAllRecords: YcInventoryItem[] | undefined
let cachedAllRecordsKeyword = ''
let cachedAllRecordsSkuList = ''

const rowKey = (row: YcInventoryItem) => row.skuId || `${row.sku}-${row.whCode}`

const toNumber = (val: unknown): number => {
  if (typeof val === 'number') return val
  const n = Number(val)
  return Number.isFinite(n) ? n : 0
}

const sortByYcField = (getter: (row: YcInventoryItem) => number) => {
  return (a: YcInventoryItem, b: YcInventoryItem) => {
    return getter(a) - getter(b)
  }
}

const sortByTotalAmount = sortByYcField((row) => toNumber(row.totalAmount))
const sortByProductTotalAmount = sortByYcField((row) => toNumber(row.productTotalAmount))
const sortByAvailableAmount = sortByYcField((row) => toNumber(row.productStockDtl?.availableAmount ?? 0))
const sortByLockAmount = sortByYcField((row) => toNumber(row.productStockDtl?.lockAmount ?? 0))
const sortByTransportAmount = sortByYcField((row) => toNumber(row.productStockDtl?.transportAmount ?? 0))

const closeSkuEmptyModal = () => {
  skuEmptyModalOpen.value = false
}

const normalizeSkuList = (val: unknown): string => {
  if (typeof val !== 'string') return ''
  const trimmed = val.trim()
  if (!trimmed) return ''
  return trimmed
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0)
    .join(',')
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
  cachedAllRecordsSkuList = ''
}

const fetchSkuList = async (): Promise<boolean> => {
  const resp = await api.get<ApiResult<YcSkuData>>(
    getApiPath('/api/warehouse-yc/records/searchSKU'),
  )
  const root = resp.data
  if (root?.code !== 200) {
    throw new Error(root?.message || root?.msg || 'searchSKU 请求失败')
  }
  const list = normalizeSkuList(root?.data?.skuList)
  skuList.value = list

  if (!list) {
    skuEmptyModalOpen.value = true
    return false
  }
  return true
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

const filterSkuListByKeyword = (list: string, keyword: string) => {
  const lower = keyword.trim().toLowerCase()
  if (!lower) return list
  return list
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0)
    .filter((s) => s.toLowerCase().includes(lower))
    .join(',')
}

const fetchAllInventoryRecords = async () => {
  if (!skuList.value) return [] as YcInventoryItem[]

  const payload: Record<string, unknown> = {
    skuList: skuList.value,
    whCodeList: '',
    page: 1,
    pageSize: null,
  }

  const resp = await api.post<ApiResult<YcInventoryData>>(
    getApiPath('/api/getYCWarehouseData'),
    payload,
  )
  const root = resp.data
  if (root?.code !== 200) {
    throw new Error(root?.message || root?.msg || 'getYCWarehouseData 请求失败')
  }

  const dataObj = root?.data ?? {}
  const itemsRaw = (dataObj.records ?? []) as unknown
  const items = Array.isArray(itemsRaw) ? (itemsRaw as YcInventoryItem[]) : []
  return items
}

const fetchInventory = async () => {
  if (!skuList.value) return

  const { mode, skuKeyword, titleKeyword } = buildSearchParams()

  if (mode === 'title') {
    const cacheKey = titleKeyword.toLowerCase()
    if (!cachedAllRecords || cachedAllRecordsKeyword !== cacheKey || cachedAllRecordsSkuList !== skuList.value) {
      cachedAllRecords = await fetchAllInventoryRecords()
      cachedAllRecordsKeyword = cacheKey
      cachedAllRecordsSkuList = skuList.value
    }

    const filtered = cachedAllRecords.filter((row) =>
      (row.productName || '').toLowerCase().includes(cacheKey),
    )

    const start = (page.value - 1) * pageSize.value
    tableData.value = filtered.slice(start, start + pageSize.value)
    total.value = filtered.length
    return
  }

  const effectiveSkuList = mode === 'sku' ? filterSkuListByKeyword(skuList.value, skuKeyword) : skuList.value
  cachedAllRecords = undefined
  cachedAllRecordsKeyword = ''
  cachedAllRecordsSkuList = ''

  if (!effectiveSkuList) {
    tableData.value = []
    total.value = 0
    return
  }

  const payload = {
    skuList: effectiveSkuList,
    whCodeList: '',
    page: page.value,
    pageSize: pageSize.value,
  }

  const resp = await api.post<ApiResult<YcInventoryData>>(
    getApiPath('/api/getYCWarehouseData'),
    payload,
  )
  const root = resp.data
  if (root?.code !== 200) {
    throw new Error(root?.message || root?.msg || 'getYCWarehouseData 请求失败')
  }

  const dataObj = root?.data ?? {}
  const itemsRaw = (dataObj.records ?? []) as unknown
  const items = Array.isArray(itemsRaw) ? (itemsRaw as YcInventoryItem[]) : []
  tableData.value = items
  total.value = normalizeTotal(dataObj.total ?? items.length)
}

const scheduleFetchInventory = () => {
  if (suppressAutoFetch.value) return
  if (!skuList.value) return
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
    const ok = await fetchSkuList()
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
    const ok = await fetchSkuList()
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
    if (!skuList.value) return
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
:deep(.yc-ep-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-text-color: hsl(var(--foreground));
  --el-table-header-text-color: hsl(var(--foreground));
  --el-table-border-color: hsl(var(--border));
  --el-table-row-hover-bg-color: hsl(var(--muted));
}

:deep(.yc-ep-table .el-table__header-wrapper th.el-table__cell) {
  background: hsl(var(--card) / 0.35);
}

:deep(.yc-ep-table .el-table__body-wrapper td.el-table__cell) {
  background: transparent;
}

:deep(.yc-ep-pagination) {
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

.sku-empty-modal-enter-active .yc-modal-panel,
.sku-empty-modal-leave-active .yc-modal-panel {
  transition: transform 180ms ease, opacity 180ms ease;
}

.sku-empty-modal-enter-from .yc-modal-panel,
.sku-empty-modal-leave-to .yc-modal-panel {
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
