export type Warehouse = 'LC' | 'YC'

export type Operation = 'ma' | 'yu' | 'shuai' | 'shou' | 'xu'

export interface OperationInfo {
  key: Operation
  name: string
  color: string
}

export interface ProductData {
  name: string
  outboundCount: number
}

export const OPERATIONS: OperationInfo[] = [
  { key: 'ma', name: '马振宇', color: '#3b82f6' },
  { key: 'yu', name: '刘瑜琦', color: '#8b5cf6' },
  { key: 'shuai', name: '薄铭帅', color: '#f59e0b' },
  { key: 'shou', name: '王丰收', color: '#f43f5e' },
  { key: 'xu', name: '尹文序', color: '#10b981' },
]

export const WAREHOUSES: { key: Warehouse; name: string; color: string }[] = [
  { key: 'LC', name: '良仓', color: '#3b82f6' },
  { key: 'YC', name: '翼仓', color: '#a855f7' },
]

const generateProducts = (operation: Operation, warehouse: Warehouse, startIndex: number, count: number): ProductData[] => {
  const products: ProductData[] = []
  const baseMultiplier = operation === 'ma' ? 100 : operation === 'yu' ? 90 : operation === 'shuai' ? 80 : operation === 'shou' ? 70 : 60
  const warehouseMultiplier = warehouse === 'LC' ? 1.2 : 1.0

  for (let i = 0; i < count; i++) {
    const index = startIndex + i
    products.push({
      name: `${operation}-product-${index + 1}`,
      outboundCount: Math.floor((baseMultiplier + Math.random() * 50) * warehouseMultiplier * (index + 1) / 10)
    })
  }
  return products
}

export const getBarChartData = (
  warehouse: Warehouse,
  operation: Operation,
  page: number,
  pageSize: number
): { names: string[]; values: number[] } => {
  const startIndex = (page - 1) * pageSize
  const products = generateProducts(operation, warehouse, startIndex, pageSize)
  return {
    names: products.map(p => p.name),
    values: products.map(p => p.outboundCount)
  }
}

export const getPieChartData = (warehouse: Warehouse): { name: string; value: number; color: string }[] => {
  return OPERATIONS.map(op => ({
    name: op.name,
    value: Math.floor((op.key === 'ma' ? 500 : op.key === 'yu' ? 450 : op.key === 'shuai' ? 400 : op.key === 'shou' ? 350 : 300) * (warehouse === 'LC' ? 1.2 : 1.0) + Math.random() * 50),
    color: op.color
  }))
}

export const TOTAL_PRODUCTS = 30

export const PAGE_SIZES = [10, 15, 20, 25]
