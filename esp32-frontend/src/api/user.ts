import request from './request'
import type {
User, UserDetail, LoginRequest, LoginResponse,
PasswordChangeRequest, UserStatistics, PageResponse
} from './type'

export const userApi = {
// 登录
login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/users/login', data)
  },

  // 登出
  logout(): Promise<void> {
    return request.post('/users/logout')
  },

  // 获取当前用户信息
  getProfile(): Promise<UserDetail> {
    return request.get('/users/profile')
  },

  // 获取用户详情
  getById(id: number): Promise<UserDetail> {
    return request.get(`/users/${id}`)
  },

  // 修改密码
  changePassword(data: PasswordChangeRequest): Promise<void> {
    return request.patch('/users/password', data)
  },

  // 获取用户统计
  getStatistics(): Promise<UserStatistics> {
    return request.get('/users/statistics')
  },

  // 分页查询用户
  query(params: any): Promise<PageResponse<User>> {
    return request.post('/users/query', params)
  },

  // 获取在线设备
  getOnlineDevices(): Promise<Device[]> {
    return request.get('/devices/online')
  }
}
