import dayjs from 'dayjs'

export const formatDate = (date: string | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
return dayjs(date).format(format)
}

export const formatRelativeTime = (date: string | Date): string => {
  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'second')

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`
  return formatDate(date)
}

export const deviceStatusMap: Record<string, { text: string; type: string }> = {
  ONLINE: { text: '在线', type: 'success' },
  OFFLINE: { text: '离线', type: 'info' },
  MAINTENANCE: { text: '维护中', type: 'warning' },
  ERROR: { text: '错误', type: 'danger' }
}

export const emergencyLevelMap: Record<string, { text: string; type: string }> = {
  LOW: { text: '低', type: 'info' },
  MEDIUM: { text: '中', type: 'warning' },
  HIGH: { text: '高', type: 'danger' },
  CRITICAL: { text: '紧急', type: 'danger' }
}

export const emergencyStatusMap: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待处理', type: 'warning' },
  HANDLING: { text: '处理中', type: 'primary' },
  RESOLVED: { text: '已解决', type: 'success' },
  CLOSED: { text: '已关闭', type: 'info' },
  FALSE_ALARM: { text: '误报', type: 'info' }
}

export const userRoleMap: Record<string, string> = {
  ELDERLY: '老年人',
  COMMUNITY: '社区工作人员',
  STAFF: '管理员',
  CHILD: '子女',
  HOSPITAL: '医院联系人'
}
