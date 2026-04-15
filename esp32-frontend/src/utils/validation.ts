/**
* 验证邮箱格式
*/
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证手机号格式
 */
export function isValidPhone(phone: string): boolean {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 验证设备ID格式
 */
export function isValidDeviceId(deviceId: string): boolean {
  const deviceIdRegex = /^[a-zA-Z0-9_-]{1,50}$/
  return deviceIdRegex.test(deviceId)
}

/**
 * 验证紧急事件级别
 */
export function isValidEmergencyLevel(level: string): boolean {
  const validLevels = ['low', 'medium', 'high', 'critical']
  return validLevels.includes(level)
}

/**
 * 验证提醒重复模式
 */
export function isValidRepeatMode(repeat: string): boolean {
  const validModes = ['once', 'daily', 'weekly', 'monthly']
  return validModes.includes(repeat)
}
