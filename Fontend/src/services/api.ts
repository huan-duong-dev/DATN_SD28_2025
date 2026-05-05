import axios from 'axios'

const baseURL = 'http://localhost:8080'

const api = axios.create({
  baseURL,
  timeout: 10000, // 10 seconds timeout
  headers: {
    'Content-Type': 'application/json',
  },
})

// Helper function to validate JWT token format
const isValidJwtToken = (token: string): boolean => {
  if (!token || typeof token !== 'string') return false
  const parts = token.split('.')
  return parts.length === 3 && parts.every(part => part.length > 0)
}

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    
    if (token && isValidJwtToken(token)) {
      config.headers.Authorization = `Bearer ${token}`
    } else if (token && !isValidJwtToken(token)) {
      // Remove invalid token from localStorage
      console.warn('Invalid JWT token format detected, removing from localStorage')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle token refresh and errors
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const currentToken = localStorage.getItem('token')
      
      // Only try to refresh if we have a valid token
      if (currentToken && isValidJwtToken(currentToken)) {
        try {
          // Try to refresh token
          const refreshResponse = await api.post('/api/auth/refresh', {}, {
            headers: { Authorization: `Bearer ${currentToken}` }
          })

          const { token: newToken, user: userData } = refreshResponse.data
          
          // Validate new token before storing
          if (newToken && isValidJwtToken(newToken)) {
            // Update stored data
            localStorage.setItem('token', newToken)
            localStorage.setItem('user', JSON.stringify(userData))
            
            // Update authorization header
            api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`
            
            // Retry original request
            return api(originalRequest)
          } else {
            throw new Error('Invalid token received from refresh endpoint')
          }
        } catch (refreshError) {
          const currentPath = window.location.pathname
          const isWebsiteRoute = currentPath === '/' ||
                                 currentPath.startsWith('/shop') ||
                                 currentPath.startsWith('/product/') ||
                                 currentPath.startsWith('/cart') ||
                                 currentPath.startsWith('/dat-hang') ||
                                 currentPath.startsWith('/checkout') ||
                                 currentPath.startsWith('/payment/') ||
                                 currentPath.startsWith('/don-hang/') ||
                                 currentPath.startsWith('/theo-doi-don-hang') ||
                                 currentPath.startsWith('/account') ||
                                 currentPath.startsWith('/faq') ||
                                 currentPath.startsWith('/customer-chat')
          
          // Chỉ log error và clear token nếu KHÔNG phải website route
          // Trên website routes, 401 là bình thường khi không đăng nhập
          if (!isWebsiteRoute) {
            console.error('Token refresh failed:', refreshError)
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            delete api.defaults.headers.common['Authorization']
            
            // Chỉ redirect nếu không phải login page
            if (currentPath !== '/login' && currentPath !== '/admin/login') {
              // Redirect về admin login nếu đang ở admin route
              if (currentPath.startsWith('/admin')) {
                window.location.href = '/admin/login'
              } else {
                window.location.href = '/login'
              }
            }
          }
          // Trên website routes, chỉ reject error mà không làm gì cả
          
          return Promise.reject(refreshError)
        }
      } else {
        // No valid token
        const currentPath = window.location.pathname
        const isWebsiteRoute = currentPath === '/' ||
                               currentPath.startsWith('/shop') ||
                               currentPath.startsWith('/product/') ||
                               currentPath.startsWith('/cart') ||
                               currentPath.startsWith('/dat-hang') ||
                               currentPath.startsWith('/checkout') ||
                               currentPath.startsWith('/payment/') ||
                               currentPath.startsWith('/don-hang/') ||
                               currentPath.startsWith('/theo-doi-don-hang') ||
                               currentPath.startsWith('/account') ||
                               currentPath.startsWith('/faq') ||
                               currentPath.startsWith('/customer-chat')
        
        // Chỉ log warning và clear token nếu KHÔNG phải website route
        // Trên website routes, 401 là bình thường khi không đăng nhập
        if (!isWebsiteRoute) {
          console.warn('No valid token available for refresh')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          delete api.defaults.headers.common['Authorization']
          
          // Chỉ redirect nếu không phải login page
          if (currentPath !== '/login' && currentPath !== '/admin/login') {
            // Redirect về admin login nếu đang ở admin route
            if (currentPath.startsWith('/admin')) {
              window.location.href = '/admin/login'
            } else {
              window.location.href = '/login'
            }
          }
        }
        // Trên website routes, chỉ reject error mà không làm gì cả
        // (401 là bình thường, component sẽ xử lý)
        
        return Promise.reject(error)
      }
    }

    return Promise.reject(error)
  }
)

export default api

export const paymentApi = {
  getBanks() {
    return api.get('/api/payments/banks')
  },
  createVnPayPayment(data: { amount: number; orderInfo: string; bankCode?: string }) {
    return api.post('/api/payments/vnpay/create', data)
  },
  createVnPayPaymentWithOrder(data: { amount: number; orderInfo: string; bankCode?: string; order: any }) {
    return api.post('/api/payments/vnpay/create-with-order', data)
  },
  createZaloPayPaymentWithOrder(data: { amount: number; orderInfo: string; order: any }) {
    return api.post('/api/payments/zalopay/create-with-order', data)
  },
  handleZaloPayReturn(params: any) {
    return api.get('/api/payments/zalopay/return', { params })
  }
}
