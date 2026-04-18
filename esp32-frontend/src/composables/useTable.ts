import { ref, reactive, computed } from 'vue'

interface TableOptions<T> {
fetchFn: (page: number, size: number) => Promise<{ content: T[], totalElements: number }>
  pageSize?: number
  immediate?: boolean
}

export function useTable<T>(options: TableOptions<T>) {
  const { fetchFn, pageSize = 20, immediate = true } = options

  const loading = ref(false)
  const data = ref<T[]>([])
  const pagination = reactive({
    page: 1,
    size: pageSize,
    total: 0
  })

  const totalPages = computed(() => Math.ceil(pagination.total / pagination.size))

  const loadData = async () => {
    loading.value = true
    try {
      const res = await fetchFn(pagination.page, pagination.size)
      data.value = res.content
      pagination.total = res.totalElements
    } catch (error) {
      console.error('Failed to load table data:', error)
    } finally {
      loading.value = false
    }
  }

  const refresh = () => {
    pagination.page = 1
    return loadData()
  }

  const handlePageChange = (page: number) => {
    pagination.page = page
    return loadData()
  }

  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.page = 1
    return loadData()
  }

  if (immediate) {
    loadData()
  }

  return {
    loading,
    data,
    pagination,
    totalPages,
    loadData,
    refresh,
    handlePageChange,
    handleSizeChange
  }
}
