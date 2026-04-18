import request from './request'
import type { DashboardOverview, RecentActivity } from './types'

export const dashboardApi = {
// 获取概览数据
getOverview(): Promise<DashboardOverview> {
    return request.get('/dashboard/overview')
  },

  // 获取最近活动
  getRecentActivities(): Promise<RecentActivity> {
    return request.get('/dashboard/recent-activities')
  }
}
