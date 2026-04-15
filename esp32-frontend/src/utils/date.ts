/**
* 格式化日期时间
*/
export function formatDateTime(date: Date | string, format: string = 'yyyy-MM-dd HH:mm:ss'): string {
  const d = typeof date === 'string' ? new Date(date) : date

  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const seconds = d.getSeconds().toString().padStart(2, '0')

  return format
    .replace('yyyy', year.toString())
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化相对时间（如：3分钟前）
 */
export function formatRelativeTime(date: Date | string): string {
  const now = new Date()
  const d = typeof date === 'string' ? new Date(date) : date
  const diffInSeconds = Math.floor((now.getTime() - d.getTime()) / 1000)

  if (diffInSeconds < 60) {
    return '刚刚'
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60)
    return `${minutes}分钟前`
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600)
    return `${hours}小时前`
  } else if (diffInSeconds < 2592000) {
    const days = Math.floor(diffInSeconds / 86400)
    return `${days}天前`
  } else if (diffInSeconds < 31536000) {
    const months = Math.floor(diffInSeconds / 2592000)
    return `${months}个月前`
  } else {
    const years = Math.floor(diffInSeconds / 31536000)
    return `${years}年前`
  }
}

/**
 * 解析时间字符串为可读格式
 */
export function parseTimeString(time: string): string {
  if (time.includes('T')) {
    // ISO 格式
    return formatDateTime(time, 'HH:mm')
  } else if (time.includes(':')) {
    // HH:mm 格式
    return time.substring(0, 5)
  } else {
    return time
  }
}

/**
 * 获取今天日期
 */
export function getToday(): string {
  return new Date().toISOString().split('T')[0]
}

/**
 * 获取明天日期
 */
export function getTomorrow(): string {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  return tomorrow.toISOString().split('T')[0]
}

/**
 * 计算两个日期之间的天数差
 */
export function daysBetween(date1: Date, date2: Date): number {
  const timeDiff = Math.abs(date2.getTime() - date1.getTime())
  return Math.ceil(timeDiff / (1000 * 3600 * 24))
}
