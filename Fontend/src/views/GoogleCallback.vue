<template>
  <div class="google-callback-page">
    <div class="callback-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner-large"></div>
        <h2>Đang xử lý đăng nhập...</h2>
        <p>Vui lòng đợi trong giây lát</p>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <h2>Đăng nhập thất bại</h2>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="goToHome">Về trang chủ</button>
      </div>

      <div v-else-if="success" class="success-state">
        <div class="success-icon">✅</div>
        <h2>Đăng nhập thành công!</h2>
        <p>Đang chuyển hướng...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const error = ref<string | null>(null)
const success = ref(false)

onMounted(async () => {
  try {
    // Lấy authorization code từ URL query params
    const code = route.query.code as string
    const errorParam = route.query.error as string

    if (errorParam) {
      error.value = 'Đăng nhập bị hủy hoặc có lỗi xảy ra'
      loading.value = false
      return
    }

    if (!code) {
      error.value = 'Không tìm thấy authorization code'
      loading.value = false
      return
    }

    // Lấy port từ sessionStorage (đã lưu khi bắt đầu Google login) - QUAN TRỌNG!
    // Phải ưu tiên port đã lưu, không dùng port hiện tại vì Google có thể redirect về port khác
    const savedPort = sessionStorage.getItem('google_login_port')
    const currentPort = window.location.port || (window.location.protocol === 'https:' ? '443' : '80')
    
    // Port để sử dụng: ƯU TIÊN port đã lưu, nếu không có mới dùng port hiện tại
    const portToUse = savedPort || currentPort
    
    // Xác định xem đang ở Admin (port 5173) hay Website (port 5174)
    // QUAN TRỌNG: Chỉ xác định dựa trên port đã lưu hoặc port hiện tại, KHÔNG dựa vào path
    const isAdmin = portToUse === '5173'
    
    console.log('GoogleCallback - Saved port:', savedPort, 'Current port:', currentPort, 'Port to use:', portToUse, 'isAdmin:', isAdmin)
    
    // Lấy redirect URI từ sessionStorage hoặc xác định từ port đã lưu
    let redirectUri = sessionStorage.getItem('google_redirect_uri')
    if (!redirectUri) {
      redirectUri = `http://localhost:${portToUse}/google-callback`
    }

    // Xác định endpoint và redirect path dựa trên port
    let apiEndpoint = '/api/customer/auth/google-login'
    let redirectPath = '/'
    let tokenKey = 'customerToken'
    let userKey = 'customerUser'
    
    if (isAdmin) {
      // Admin (port 5173)
      redirectPath = '/admin/dashboard'
      tokenKey = 'token'
      userKey = 'user'
    } else {
      // Website (port 5174) - luôn redirect về home page
      redirectPath = '/'
      tokenKey = 'customerToken'
      userKey = 'customerUser'
    }

    // Gửi code và redirect URI đến backend để xử lý
    const response = await api.post(apiEndpoint, { 
      code,
      redirectUri 
    })
    
    // Xóa redirect URI và port khỏi sessionStorage sau khi sử dụng (sẽ xóa lại sau khi redirect)

    if (response.data.success && response.data.token) {
      // Lưu token vào localStorage với key phù hợp
      localStorage.setItem(tokenKey, response.data.token)
      localStorage.setItem(userKey, JSON.stringify(response.data.user))
      
      // Nếu là admin, cần set Authorization header
      if (isAdmin) {
        api.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`
      }

      success.value = true

      // Redirect về trang phù hợp sau 1 giây
      // QUAN TRỌNG: Dùng port đã lưu để redirect, không dùng port hiện tại
      setTimeout(() => {
        // Xóa port khỏi sessionStorage sau khi đã xác định redirect
        sessionStorage.removeItem('google_login_port')
        sessionStorage.removeItem('google_redirect_uri')
        
        // Đảm bảo redirect đúng port - luôn dùng port đã lưu (portToUse)
        if (isAdmin) {
          // Admin: redirect về port 5173
          window.location.href = `http://localhost:5173${redirectPath}`
        } else {
          // Website: redirect về port 5174 với home page
          window.location.href = `http://localhost:5174${redirectPath}`
        }
      }, 1000)
    } else {
      error.value = response.data.error || 'Đăng nhập thất bại'
      loading.value = false
    }
  } catch (err: any) {
    console.error('Error processing Google callback:', err)
    error.value = err.response?.data?.error || 'Lỗi khi xử lý đăng nhập Google'
    loading.value = false
  }
})

function goToHome() {
  // Lấy port từ sessionStorage - ƯU TIÊN port đã lưu
  const savedPort = sessionStorage.getItem('google_login_port')
  const currentPort = window.location.port || (window.location.protocol === 'https:' ? '443' : '80')
  const portToUse = savedPort || currentPort
  
  // Xác định dựa trên port: 5173 = Admin, 5174 = Website
  const isAdmin = portToUse === '5173'
  
  // Xóa sessionStorage
  sessionStorage.removeItem('google_login_port')
  sessionStorage.removeItem('google_redirect_uri')
  
  if (isAdmin) {
    window.location.href = 'http://localhost:5173/admin/dashboard'
  } else {
    // Website: luôn redirect về port 5174 với home page
    window.location.href = 'http://localhost:5174/'
  }
}
</script>

<style scoped>
.google-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.callback-container {
  background: white;
  border-radius: 12px;
  padding: 40px;
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.loading-state,
.error-state,
.success-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.spinner-large {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4285f4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-icon,
.success-icon {
  font-size: 64px;
}

h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.btn-retry {
  margin-top: 10px;
  padding: 12px 24px;
  background-color: #4285f4;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-retry:hover {
  background-color: #357ae8;
}
</style>

