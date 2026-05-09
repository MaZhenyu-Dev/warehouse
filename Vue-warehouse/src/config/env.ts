export function getApiBaseUrl(): string {
  const envUrl = import.meta.env.VITE_API_BASE_URL
  if (envUrl) {
    return envUrl
  }
  if (import.meta.env.PROD) {
    return '/api'
  }
  return 'http://localhost:8080'
}

export function getApiPath(path: string): string {
  const baseUrl = getApiBaseUrl()
  if (baseUrl === '/api') {
    return path.startsWith('/') ? path : `/${path}`
  }
  return `${baseUrl}${path.startsWith('/') ? path : `/${path}`}`
}
