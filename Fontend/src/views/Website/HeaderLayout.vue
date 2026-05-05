<template>
  <header class="main-header">
    <div class="header-container">
      <!-- Logo -->
      <div class="logo-section">
        <router-link to="/">
          <img src="/Logo4.png" alt="PhoniX" class="logo-img" />
        </router-link>
      </div>

      <!-- Search Bar -->
      <div class="search-section" @click.stop>
        <input
          v-model="searchQuery"
          @input="handleSearch"
          @keyup.enter="performSearch"
          @focus="showSearchSuggestions = true"
          @blur="handleSearchBlur"
          type="text"
          placeholder="Tìm kiếm sản phẩm, thương hiệu, và tên shop"
          class="search-input"
        />
        <i class="bi bi-search search-icon" @click="performSearch"></i>
        
        <!-- Search Suggestions Dropdown -->
        <div v-if="showSearchSuggestions && searchSuggestions.length > 0" class="search-suggestions">
          <div
            v-for="product in searchSuggestions"
            :key="product.id"
            class="suggestion-item"
            @mousedown="selectSuggestion(product)"
          >
            <div class="suggestion-image">
              <img
                :src="getProductImage(product.hinhAnh)"
                :alt="product.tenSanPham"
                @error="handleSuggestionImageError"
              />
            </div>
            <div class="suggestion-info">
              <h4 class="suggestion-name">{{ product.tenSanPham }}</h4>
              <div class="suggestion-details">
                <span v-if="product.tenHang" class="suggestion-brand">{{ product.tenHang }}</span>
                <span v-if="product.tenRam || product.tenRom" class="suggestion-specs">
                  {{ [product.tenRam, product.tenRom].filter(Boolean).join(' ') }}
                </span>
              </div>
              <div class="suggestion-price">{{ formatPrice(product.giaBan || product.gia) }}</div>
            </div>
          </div>
          <div v-if="searchSuggestions.length >= 5" class="suggestion-footer">
            <button @click="performSearch" class="view-all-results-btn">
              Xem tất cả kết quả cho "{{ searchQuery }}"
            </button>
          </div>
        </div>
      </div>

      <!-- Header Actions -->
      <div class="header-actions">
        <!-- Notifications -->
        <div class="action-item notifications" @click="toggleNotifications">
          <div class="notification-icon-wrapper">
            <i class="bi bi-bell"></i>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
          <span class="action-text">Thông báo</span>
          
          <!-- Notifications Dropdown -->
          <div v-if="showNotificationsDropdown" class="notifications-dropdown" @click.stop>
            <div class="notifications-header">
              <h3>Thông báo</h3>
              <button v-if="unreadCount > 0" @click.stop="markAllAsRead" class="mark-all-read-btn">
                Đánh dấu tất cả đã đọc
              </button>
            </div>
            <div class="notifications-list">
              <div v-if="notifications.length === 0" class="no-notifications">
                <i class="bi bi-bell-slash"></i>
                <p>Không có thông báo nào</p>
              </div>
              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-item"
                :class="{ unread: !notification.daDoc }"
                @click="!isCompletedOrder(notification) && handleNotificationClick(notification)"
              >
                <div class="notification-icon">
                  <i :class="getNotificationIcon(notification.loai)"></i>
                </div>
                <div class="notification-content">
                  <h4>{{ getNotificationContent(notification).tieuDe }}</h4>
                  <p>{{ getNotificationContent(notification).noiDung }}</p>
                  <span class="notification-time">{{ formatNotificationTime(notification.thoiGian) }}</span>
                  <button
                    v-if="isCompletedOrder(notification)"
                    @click.stop="handleViewOrderDetail(notification)"
                    class="btn-view-order"
                  >
                    Xem chi tiết đơn hàng
                  </button>
                </div>
                <div v-if="!notification.daDoc" class="unread-indicator"></div>
              </div>
            </div>
            <!-- Removed "Xem tất cả" link as there's no dedicated notifications page -->
          </div>
        </div>

        <!-- Cart -->
        <router-link to="/cart" class="action-item cart">
          <div class="cart-icon-wrapper">
            <i class="bi bi-cart3"></i>
            <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
          </div>
          <span class="action-text">Giỏ hàng</span>
        </router-link>

        <!-- User -->
        <div v-if="isCustomerLoggedIn" class="user-dropdown-wrapper" @mouseenter="handleDropdownEnter" @mouseleave="handleDropdownLeave">
          <router-link to="/tai-khoan" class="action-item user" @click.stop>
          <i class="bi bi-person-circle"></i>
          <span class="action-text">{{ customerName }}</span>
            <i class="bi bi-chevron-down dropdown-arrow" :class="{ rotated: showUserDropdown }"></i>
        </router-link>
          <!-- User Dropdown Menu -->
          <div v-if="showUserDropdown" class="user-dropdown-menu" @mouseenter="handleDropdownEnter" @mouseleave="handleDropdownLeave">
            <router-link to="/tai-khoan/thong-tin" class="dropdown-item" @click="showUserDropdown = false">
              <i class="bi bi-person"></i>
              <span>Tài khoản</span>
            </router-link>
            <router-link to="/tai-khoan" class="dropdown-item" @click="showUserDropdown = false">
              <i class="bi bi-receipt-cutoff"></i>
              <span>Đơn hàng</span>
            </router-link>
            <router-link to="/tai-khoan/yeu-thich" class="dropdown-item" @click="showUserDropdown = false">
              <i class="bi bi-heart"></i>
              <span>Yêu thích</span>
            </router-link>
            <div class="dropdown-divider"></div>
            <button class="dropdown-item logout-item" @click="handleLogout">
              <i class="bi bi-box-arrow-right"></i>
              <span>Đăng xuất</span>
            </button>
          </div>
        </div>

        <div v-else class="action-item user" @click="showLogin = true">
          <i class="bi bi-person-circle"></i>
          <span class="action-text">Đăng nhập</span>
        </div>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="nav-menu">
      <div class="nav-menu-container">
        <ul class="menu-list">
          <li class="categories-item">
            <button class="categories-btn" @click="toggleCategoriesDropdown">
              <i class="bi bi-list"></i>
              <span>Danh mục</span>
              <i class="bi bi-chevron-down" :class="{ rotated: showCategoriesDropdown }"></i>
            </button>
            <!-- Categories Dropdown -->
            <div v-if="showCategoriesDropdown" class="categories-dropdown" @click.stop>
              <div v-if="brandsLoading" class="categories-loading">
                <i class="bi bi-arrow-repeat"></i>
                <span>Đang tải...</span>
              </div>
              <div v-else-if="brands.length === 0" class="categories-empty">
                <i class="bi bi-inbox"></i>
                <span>Không có hãng nào</span>
              </div>
              <div v-else class="categories-list">
                <div
                  v-for="brand in brands"
                  :key="brand.id"
                  class="category-item"
                  @click="handleBrandClick(brand)"
                >
                  <span>{{ brand.ten || brand.tenHang }}</span>
                  <i class="bi bi-chevron-right"></i>
                </div>
              </div>
            </div>
          </li>
          <li>
            <router-link to="/" class="menu-link" :class="{ active: $route.path === '/' }"
              >Trang chủ</router-link
            >
          </li>
          <li>
            <router-link to="/shop" class="menu-link" :class="{ active: $route.path === '/shop' }"
              >Sản phẩm</router-link
            >
          </li>
          <li>
            <router-link to="/voucher" class="menu-link" :class="{ active: $route.path === '/voucher' }"
              >Voucher</router-link
            >
          </li>
          <li>
            <router-link to="/tin-tuc" class="menu-link" :class="{ active: $route.path === '/tin-tuc' }"
              >Tin tức</router-link
            >
          </li>
          <li>
            <router-link to="/lien-he" class="menu-link" :class="{ active: $route.path === '/lien-he' }"
              >Liên hệ</router-link
            >
          </li>
        </ul>
      </div>
    </nav>
  </header>

  <!-- Login Modal -->
  <div v-if="showLogin" class="modal-overlay" @click="showLogin = false">
    <div class="modal-box login-modal" @click.stop>
      <div class="modal-header">
        <h3>Đăng nhập</h3>
        <button class="close-modal" @click="showLogin = false">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div class="modal-body">
        <div class="login-content">
          <p class="login-description">Đăng nhập để trải nghiệm tốt hơn</p>

          <!-- Login Form (JWT) -->
          <form @submit.prevent="handleCustomerLogin" class="customer-login-form">
            <div class="form-group">
              <label for="customer-email">Email:</label>
              <input
                id="customer-email"
                type="email"
                v-model="customerLoginForm.email"
                required
                placeholder="Nhập địa chỉ email"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="customer-password">Mật khẩu:</label>
              <input
                id="customer-password"
                type="password"
                v-model="customerLoginForm.password"
                required
                placeholder="Nhập mật khẩu"
                class="form-input"
              />
            </div>

            <button type="submit" class="btn-login-jwt" :disabled="customerLoginLoading">
              <span v-if="customerLoginLoading">Đang đăng nhập...</span>
              <span v-else>Đăng nhập</span>
            </button>

            <div class="forgot-password-link">
              <a href="#" @click.prevent="openForgotPasswordModal">Quên mật khẩu?</a>
            </div>
          </form>

          <div class="login-divider">
            <span>hoặc</span>
          </div>

          <!-- Google Login Button -->
          <GoogleLoginButton @success="handleLoginSuccess" @error="handleLoginError" />

          <div class="register-link-section">
            <p class="register-text">
              Chưa có tài khoản?
              <a href="#" @click.prevent="openRegisterModal" class="register-link">Đăng ký ngay</a>
            </p>
          </div>

          <p class="login-note">
            Bằng cách đăng nhập, bạn đồng ý với
            <a href="#" @click.prevent>Điều khoản sử dụng</a>
            và
            <a href="#" @click.prevent>Chính sách bảo mật</a>
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- Forgot Password Modal -->
  <div v-if="showForgotPassword" class="modal-overlay" @click.self="closeForgotPasswordModal">
    <div class="modal-box forgot-password-modal" @click.stop>
      <div class="modal-header">
        <h3>Quên mật khẩu</h3>
        <button class="close-modal" @click="closeForgotPasswordModal">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div class="modal-body">
        <div class="forgot-password-content">
          <!-- Step 1: Enter Email -->
          <div v-if="forgotPasswordStep === 1" class="forgot-step">
            <p class="step-description">Chúng tôi sẽ gửi OTP 6 số đến email của bạn</p>
            <form @submit.prevent="handleSendOTP" class="forgot-form">
              <div class="form-group">
                <label for="forgot-email">Email: </label>
                <input
                  id="forgot-email"
                  type="email"
                  v-model="forgotPasswordForm.email"
                  required
                  placeholder="Nhập địa chỉ email"
                  class="form-input"
                />
              </div>
              <button type="submit" class="btn-send-otp" :disabled="sendingOTP">
                <span v-if="sendingOTP">Đang gửi...</span>
                <span v-else>Gửi mã OTP</span>
              </button>
            </form>
          </div>

          <!-- Step 2: Enter OTP -->
          <div v-else-if="forgotPasswordStep === 2" class="forgot-step">
            <p class="step-description">Nhập mã OTP 6 số đã được gửi đến email của bạn</p>
            <form @submit.prevent="handleVerifyOTP" class="forgot-form">
              <div class="form-group">
                <label for="forgot-otp">Mã OTP:</label>
                <input
                  id="forgot-otp"
                  type="text"
                  v-model="forgotPasswordForm.otp"
                  required
                  placeholder="Nhập mã OTP 6 số"
                  class="form-input otp-input"
                  maxlength="6"
                  pattern="[0-9]{6}"
                  @input="forgotPasswordForm.otp = forgotPasswordForm.otp.replace(/[^0-9]/g, '')"
                />
                <small class="form-hint"
                  >Mã OTP đã được gửi đến {{ forgotPasswordForm.email }}</small
                >
              </div>
              <div class="resend-otp-section">
                <button
                  type="button"
                  class="btn-resend-otp"
                  @click="handleResendOTP"
                  :disabled="resendCooldown > 0 || resendingOTP"
                >
                  <span v-if="resendingOTP">Đang gửi...</span>
                  <span v-else-if="resendCooldown > 0">Gửi lại sau {{ resendCooldown }}s</span>
                  <span v-else>Gửi lại mã OTP</span>
                </button>
              </div>
              <div class="form-actions">
                <button type="button" class="btn-back" @click="forgotPasswordStep = 1">
                  Quay lại
                </button>
                <button
                  type="submit"
                  class="btn-verify-otp"
                  :disabled="verifyingOTP || forgotPasswordForm.otp.length !== 6"
                >
                  <span v-if="verifyingOTP">Đang xác thực...</span>
                  <span v-else>Xác thực OTP</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Step 3: Reset Password -->
          <div v-else-if="forgotPasswordStep === 3" class="forgot-step">
            <p class="step-description">Nhập mật khẩu mới của bạn</p>
            <form @submit.prevent="handleResetPassword" class="forgot-form">
              <div class="form-group">
                <label for="new-password">Mật khẩu mới:</label>
                <input
                  id="new-password"
                  type="password"
                  v-model="forgotPasswordForm.newPassword"
                  required
                  placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
                  class="form-input"
                  minlength="6"
                />
              </div>
              <div class="form-group">
                <label for="confirm-password">Xác nhận mật khẩu:</label>
                <input
                  id="confirm-password"
                  type="password"
                  v-model="forgotPasswordForm.confirmPassword"
                  required
                  placeholder="Nhập lại mật khẩu mới"
                  class="form-input"
                  minlength="6"
                />
                <small
                  v-if="
                    forgotPasswordForm.newPassword &&
                    forgotPasswordForm.confirmPassword &&
                    forgotPasswordForm.newPassword !== forgotPasswordForm.confirmPassword
                  "
                  class="form-error"
                >
                  Mật khẩu xác nhận không khớp
                </small>
              </div>
              <div class="form-actions">
                <button type="button" class="btn-back" @click="forgotPasswordStep = 2">
                  Quay lại
                </button>
                <button
                  type="submit"
                  class="btn-reset-password"
                  :disabled="
                    resettingPassword ||
                    forgotPasswordForm.newPassword !== forgotPasswordForm.confirmPassword ||
                    forgotPasswordForm.newPassword.length < 6
                  "
                >
                  <span v-if="resettingPassword">Đang đặt lại...</span>
                  <span v-else>Đặt lại mật khẩu</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Register Modal -->
  <div v-if="showRegister" class="modal-overlay" @click="showRegister = false">
    <div class="modal-box register-modal" @click.stop>
      <div class="modal-header">
        <h3>Đăng ký tài khoản</h3>
        <button class="close-modal" @click="showRegister = false">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div class="modal-body">
        <div class="register-content">
          <p class="register-description">Tạo tài khoản để trải nghiệm tốt hơn</p>

          <!-- Register Form -->
          <form @submit.prevent="handleCustomerRegister" class="customer-register-form">
            <div class="form-group">
              <label for="register-hoTen">Họ tên:</label>
              <input
                id="register-hoTen"
                type="text"
                v-model="customerRegisterForm.hoTen"
                required
                placeholder="Nhập họ tên"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="register-email">Email:</label>
              <input
                id="register-email"
                type="email"
                v-model="customerRegisterForm.email"
                required
                placeholder="Nhập địa chỉ email"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="register-phone">Số điện thoại:</label>
              <input
                id="register-phone"
                type="tel"
                v-model="customerRegisterForm.soDienThoai"
                required
                placeholder="Nhập số điện thoại (10 chữ số)"
                class="form-input"
                maxlength="10"
                pattern="[0-9]{10}"
                @input="customerRegisterForm.soDienThoai = customerRegisterForm.soDienThoai.replace(/[^0-9]/g, '')"
              />
            </div>

            <div class="form-group">
              <label for="register-password">Mật khẩu:</label>
              <input
                id="register-password"
                type="password"
                v-model="customerRegisterForm.password"
                required
                placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)"
                class="form-input"
                minlength="6"
              />
            </div>

            <div class="form-group">
              <label for="register-confirmPassword">Xác nhận mật khẩu:</label>
              <input
                id="register-confirmPassword"
                type="password"
                v-model="customerRegisterForm.confirmPassword"
                required
                placeholder="Nhập lại mật khẩu"
                class="form-input"
                minlength="6"
              />
              <small v-if="customerRegisterForm.password && customerRegisterForm.confirmPassword && customerRegisterForm.password !== customerRegisterForm.confirmPassword" class="form-error">
                Mật khẩu xác nhận không khớp
              </small>
            </div>

            <div class="form-group">
              <label for="register-gioiTinh">Giới tính:</label>
              <select
                id="register-gioiTinh"
                v-model="customerRegisterForm.gioiTinh"
                class="form-input"
              >
                <option value="">Chọn giới tính</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
              </select>
            </div>

            <div class="form-group">
              <label for="register-ngaySinh">Ngày sinh: <span class="required">*</span></label>
              <input
                id="register-ngaySinh"
                type="date"
                v-model="customerRegisterForm.ngaySinh"
                class="form-input"
                required
                :max="maxDate"
              />
              <small class="form-hint">Bạn phải đủ 18 tuổi mới được đăng ký tài khoản</small>
            </div>

            <button
              type="submit"
              class="btn-register"
              :disabled="customerRegisterLoading || (customerRegisterForm.password !== customerRegisterForm.confirmPassword)"
            >
              <span v-if="customerRegisterLoading">Đang đăng ký...</span>
              <span v-else>Đăng ký</span>
            </button>
          </form>

          <div class="login-link-section">
            <p class="login-text">
              Đã có tài khoản?
              <a href="#" @click.prevent="openLoginModal" class="login-link">Đăng nhập ngay</a>
            </p>
          </div>

          <div class="register-divider">
            <span>hoặc</span>
          </div>

          <!-- Google Login Button -->
          <GoogleLoginButton @success="handleLoginSuccess" @error="handleLoginError" />

          <p class="register-note">
            Bằng cách đăng ký, bạn đồng ý với
            <a href="#" @click.prevent>Điều khoản sử dụng</a>
            và
            <a href="#" @click.prevent>Chính sách bảo mật</a>
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- Toast Notification -->
  <transition name="toast">
    <div v-if="toast.show" class="toast-notification" :class="toast.type">
      <i class="bi" :class="toast.icon"></i>
      <span>{{ toast.message }}</span>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import GoogleLoginButton from '@/components/GoogleLoginButton.vue'
import axios from 'axios'

const router = useRouter()
const cartStore = useCartStore()

// API Base URL
const API_BASE_URL = 'http://localhost:8080'

// State
const searchQuery = ref('')
const showLogin = ref(false)
const showRegister = ref(false)
const showForgotPassword = ref(false)
const customerLoginLoading = ref(false)
const customerRegisterLoading = ref(false)
const customerLoginForm = ref({
  email: '',
  password: '',
})
const customerRegisterForm = ref({
  hoTen: '',
  email: '',
  soDienThoai: '',
  password: '',
  confirmPassword: '',
  gioiTinh: '',
  ngaySinh: '',
})

// Forgot Password State
const forgotPasswordStep = ref(1) // 1: email, 2: OTP, 3: new password
const sendingOTP = ref(false)
const verifyingOTP = ref(false)
const resettingPassword = ref(false)
const resendingOTP = ref(false)
const resendCooldown = ref(0)
let resendTimer = null
const forgotPasswordForm = ref({
  email: '',
  otp: '',
  newPassword: '',
  confirmPassword: '',
  resetToken: '',
})

// Customer state
const customerUser = ref(null)
const customerToken = ref(null)

// Notifications state
const showNotificationsDropdown = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let notificationsPollingInterval = null
const notificationsApiAvailable = ref(true)
let consecutiveApiErrors = 0

// Categories/Brands state
const showCategoriesDropdown = ref(false)
const brands = ref([])
const brandsLoading = ref(false)

// User dropdown state
const showUserDropdown = ref(false)

// Search suggestions state
const searchSuggestions = ref([])
const showSearchSuggestions = ref(false)
let searchDebounceTimer = null

// Computed
const isCustomerLoggedIn = computed(() => {
  return !!customerToken.value && !!customerUser.value
})

const customerName = computed(() => {
  if (customerUser.value?.hoTen) {
    return customerUser.value.hoTen
  }
  if (customerUser.value?.taiKhoan) {
    return customerUser.value.taiKhoan
  }
  if (customerUser.value?.email) {
    return customerUser.value.email.split('@')[0]
  }
  return 'Khách hàng'
})

const customerEmail = computed(() => {
  return customerUser.value?.email || 'Chưa có email'
})

const customerPhone = computed(() => {
  return customerUser.value?.soDienThoai || null
})

// Computed: Max date for date input (18 years ago from today)
const maxDate = computed(() => {
  const today = new Date()
  const maxDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate())
  return maxDate.toISOString().split('T')[0]
})

// Toast
const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: '',
})

// Methods
const loadCustomerFromStorage = () => {
  const token = localStorage.getItem('customerToken')
  const userStr = localStorage.getItem('customerUser')

  if (token && userStr) {
    try {
      customerToken.value = token
      customerUser.value = JSON.parse(userStr)
    } catch (e) {
      console.error('Error parsing customer user:', e)
      localStorage.removeItem('customerToken')
      localStorage.removeItem('customerUser')
    }
  }
}

// Categories/Brands Methods
const toggleCategoriesDropdown = (event) => {
  event.stopPropagation()
  showCategoriesDropdown.value = !showCategoriesDropdown.value
  if (showCategoriesDropdown.value && brands.value.length === 0) {
    loadBrands()
  }
}

const loadBrands = async () => {
  brandsLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/api/hang/active`)
    if (response.data && Array.isArray(response.data)) {
      brands.value = response.data.filter(b => b.trangThai === 1)
    } else {
      brands.value = []
    }
  } catch (error) {
    console.error('Error loading brands:', error)
    brands.value = []
  } finally {
    brandsLoading.value = false
  }
}

const handleBrandClick = (brand) => {
  // Gửi brandId thay vì brandName vì ShopPage.vue xử lý brandId
  const brandId = brand.id
  if (brandId) {
    router.push({ path: '/shop', query: { brand: brandId } })
    showCategoriesDropdown.value = false
  }
}

// Notifications Methods
const toggleNotifications = (event) => {
  if (!isCustomerLoggedIn.value) {
    showLogin.value = true
    return
  }
  event.stopPropagation()
  showNotificationsDropdown.value = !showNotificationsDropdown.value
  if (showNotificationsDropdown.value) {
    // Reset error tracking khi mở dropdown để thử lại API
    consecutiveApiErrors = 0
    notificationsApiAvailable.value = true
    loadNotifications()
  }
}

const closeNotifications = () => {
  showNotificationsDropdown.value = false
}

const loadNotifications = async () => {
  if (!isCustomerLoggedIn.value || !customerToken.value) {
    notifications.value = []
    unreadCount.value = 0
    return
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/api/customer/notifications`, {
      headers: {
        Authorization: `Bearer ${customerToken.value}`
      }
    })
    
    if (response.data && Array.isArray(response.data)) {
      // Lọc chỉ các thông báo phù hợp cho khách hàng
      const filteredNotifications = response.data.filter(notification => {
        const loai = notification.loai
        
        // Đặt đơn thành công
        if (loai === 'DON_HANG_TAO_MOI' || loai === 'DON_HANG') {
          return true
        }
        
        // Đơn cập nhật (các trạng thái cụ thể)
        if (loai === 'DON_HANG_CAP_NHAT') {
          const trangThai = notification.trangThai || notification.duLieu?.trangThai
          // Chỉ lấy các trạng thái: 1 (Chờ xác nhận active), 2 (Chờ giao hàng), 3 (Đang giao hàng), 4 (Hoàn thành)
          return trangThai === 1 || trangThai === 2 || trangThai === 3 || trangThai === 4
        }
        
        // Voucher cá nhân mới
        if (loai === 'VOUCHER') {
          return true
        }
        
        return false
      })
      
      notifications.value = filteredNotifications
      unreadCount.value = filteredNotifications.filter(n => !n.daDoc).length
      
      // Reset error counter khi API hoạt động
      consecutiveApiErrors = 0
      notificationsApiAvailable.value = true
    } else {
      notifications.value = []
      unreadCount.value = 0
    }
  } catch (error) {
    // Xử lý lỗi im lặng - API có thể chưa sẵn sàng
    const status = error.response?.status
    
    // Xử lý các loại lỗi khác nhau
    if (status === 401 || status === 403) {
      // Lỗi authentication - token có thể không hợp lệ hoặc backend đang dùng sai auth service
      // Xử lý im lặng, không load notifications
      notifications.value = []
      unreadCount.value = 0
      // Không tăng error counter vì đây là lỗi authentication, không phải lỗi API
      return
    }
    
    // Nếu là lỗi 404 hoặc 500, tăng số lỗi liên tiếp
    if (status === 404 || status === 500) {
      consecutiveApiErrors++
      // Sau 3 lần lỗi liên tiếp, đánh dấu API không sẵn sàng
      if (consecutiveApiErrors >= 3) {
        notificationsApiAvailable.value = false
      }
    } else if (status && status !== 404 && status !== 500 && status !== 401 && status !== 403) {
      // Chỉ log các lỗi khác không phải 404/500/401/403
      console.warn('Error loading notifications:', status)
    }
    
    // Đảm bảo UI vẫn hoạt động - set empty array
    notifications.value = []
    unreadCount.value = 0
  }
}

const markAsRead = async (notificationId) => {
  if (!isCustomerLoggedIn.value || !customerToken.value) return

  // Lưu trạng thái cũ để revert nếu API fail
  const notification = notifications.value.find(n => n.id === notificationId)
  const wasUnread = notification && !notification.daDoc
  const previousUnreadCount = unreadCount.value

  // Cập nhật UI ngay lập tức (optimistic update)
  if (notification && !notification.daDoc) {
    notification.daDoc = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  try {
    await axios.put(`${API_BASE_URL}/api/customer/notifications/${notificationId}/read`, {}, {
      headers: {
        Authorization: `Bearer ${customerToken.value}`
      }
    })
    
    // Reload notifications từ backend để đảm bảo đồng bộ
    await loadNotifications()
  } catch (error) {
    // Revert optimistic update nếu API fail
    if (notification && wasUnread) {
      notification.daDoc = false
      unreadCount.value = previousUnreadCount
    }
    
    // Chỉ log nếu không phải lỗi 404 hoặc 500
    if (error.response?.status !== 404 && error.response?.status !== 500) {
      console.warn('Error marking notification as read:', error.response?.status || error.message)
    }
  }
}

const markAllAsRead = async () => {
  if (!isCustomerLoggedIn.value || !customerToken.value) return

  // Lưu trạng thái cũ để revert nếu API fail
  const previousNotifications = notifications.value.map(n => ({ ...n }))
  const previousUnreadCount = unreadCount.value

  // Cập nhật UI ngay lập tức (optimistic update)
  notifications.value.forEach(n => {
    n.daDoc = true
  })
  unreadCount.value = 0

  try {
    await axios.put(`${API_BASE_URL}/api/customer/notifications/read-all`, {}, {
      headers: {
        Authorization: `Bearer ${customerToken.value}`
      }
    })
    
    // Reload notifications từ backend để đảm bảo đồng bộ
    await loadNotifications()
  } catch (error) {
    // Revert optimistic update nếu API fail
    notifications.value = previousNotifications
    unreadCount.value = previousUnreadCount
    
    // Chỉ log nếu không phải lỗi 404 hoặc 500
    if (error.response?.status !== 404 && error.response?.status !== 500) {
      console.warn('Error marking all notifications as read:', error.response?.status || error.message)
    }
  }
}

const handleNotificationClick = (notification) => {
  if (!notification.daDoc) {
    markAsRead(notification.id)
  }
  
  // Navigate based on notification type
  const loai = notification.loai
  const orderId = notification.duLieuId || notification.duLieu?.id || notification.hoaDonId
  
  if (loai === 'DON_HANG' || loai === 'DON_HANG_TAO_MOI' || loai === 'DON_HANG_CAP_NHAT') {
    if (orderId) {
      router.push(`/theo-doi-don-hang?orderId=${orderId}`)
    } else {
      router.push('/theo-doi-don-hang')
    }
  } else if (loai === 'VOUCHER') {
    router.push('/voucher')
  }
  
  closeNotifications()
}

const handleViewOrderDetail = (notification) => {
  if (!notification.daDoc) {
    markAsRead(notification.id)
  }
  
  const orderId = notification.duLieuId || notification.duLieu?.id || notification.hoaDonId
  if (orderId) {
    router.push(`/theo-doi-don-hang?orderId=${orderId}`)
  } else {
    router.push('/theo-doi-don-hang')
  }
  
  closeNotifications()
}

const formatNotificationTime = (timeString) => {
  if (!timeString) return ''
  
  const now = new Date()
  const time = new Date(timeString)
  const diffMs = now - time
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Vừa xong'
  if (diffMins < 60) return `${diffMins} phút trước`
  if (diffHours < 24) return `${diffHours} giờ trước`
  if (diffDays < 7) return `${diffDays} ngày trước`
  
  return time.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const getNotificationIcon = (loai) => {
  switch (loai) {
    case 'DON_HANG':
    case 'DON_HANG_TAO_MOI':
    case 'DON_HANG_CAP_NHAT':
      return 'bi-receipt'
    case 'VOUCHER':
      return 'bi-ticket-perforated'
    default:
      return 'bi-bell'
  }
}

const getNotificationContent = (notification) => {
  const loai = notification.loai
  
  // Đặt đơn thành công (Trạng thái chờ xác nhận - 0)
  if (loai === 'DON_HANG_TAO_MOI' || (loai === 'DON_HANG' && !notification.trangThai)) {
    return {
      tieuDe: notification.tieuDe || 'Đặt hàng thành công',
      noiDung: notification.noiDung || 'Đặt hàng thành công chờ xác nhận'
    }
  }
  
  // Đơn cập nhật
  if (loai === 'DON_HANG_CAP_NHAT' || loai === 'DON_HANG') {
    const trangThai = notification.trangThai || notification.duLieu?.trangThai
    
    switch (trangThai) {
      case 1: // CHO_GIAO_HANG - Chờ xác nhận active (đã xác nhận phía admin)
        return {
          tieuDe: notification.tieuDe || 'Cửa hàng đang chuẩn bị sản phẩm',
          noiDung: notification.noiDung || 'Cửa hàng đang chuẩn bị sản phẩm'
        }
      case 2: // DANG_GIAO - Chờ giao hàng active
        return {
          tieuDe: notification.tieuDe || 'Đơn hàng đang được giao đến bạn',
          noiDung: notification.noiDung || 'Đơn hàng đang được giao đến bạn'
        }
      case 3: // HOAN_THANH - Đang giao hàng active
        return {
          tieuDe: notification.tieuDe || 'Đơn hàng đã giao đến bạn',
          noiDung: notification.noiDung || 'Đơn hàng đã giao đến bạn vui lòng xác nhận hoàn thành đơn hàng',
          showButton: true
        }
      case 4: // DA_HUY - Không hiển thị thông báo hủy
        return {
          tieuDe: notification.tieuDe || 'Đơn hàng đã bị hủy',
          noiDung: notification.noiDung || 'Đơn hàng của bạn đã bị hủy'
        }
      default:
        return {
          tieuDe: notification.tieuDe || 'Cập nhật đơn hàng',
          noiDung: notification.noiDung || 'Đơn hàng của bạn đã được cập nhật'
        }
    }
  }
  
  // Voucher cá nhân mới
  if (loai === 'VOUCHER') {
    return {
      tieuDe: notification.tieuDe || 'Voucher mới',
      noiDung: notification.noiDung || 'Bạn có voucher cá nhân mới'
    }
  }
  
  // Mặc định
  return {
    tieuDe: notification.tieuDe || 'Thông báo',
    noiDung: notification.noiDung || ''
  }
}

const isCompletedOrder = (notification) => {
  const loai = notification.loai
  const trangThai = notification.trangThai || notification.duLieu?.trangThai
  return (loai === 'DON_HANG_CAP_NHAT' || loai === 'DON_HANG') && trangThai === 4
}

const startNotificationsPolling = () => {
  if (!isCustomerLoggedIn.value) return
  
  // Reset error tracking
  consecutiveApiErrors = 0
  notificationsApiAvailable.value = true
  
  // Load notifications immediately
  loadNotifications()
  
  // Poll every 30 seconds (chỉ khi API sẵn sàng)
  notificationsPollingInterval = setInterval(() => {
    if (!isCustomerLoggedIn.value) {
      stopNotificationsPolling()
      return
    }
    
    // Chỉ poll nếu API sẵn sàng
    if (notificationsApiAvailable.value) {
      loadNotifications()
    }
  }, 30000)
}

const stopNotificationsPolling = () => {
  if (notificationsPollingInterval) {
    clearInterval(notificationsPollingInterval)
    notificationsPollingInterval = null
  }
}

// Click outside handler for notifications, categories, and search suggestions
const handleClickOutside = (event) => {
  // Handle notifications dropdown
  const notificationsElement = document.querySelector('.notifications')
  const notificationsDropdownElement = document.querySelector('.notifications-dropdown')
  
  if (showNotificationsDropdown.value && 
      notificationsElement && 
      notificationsDropdownElement &&
      !notificationsElement.contains(event.target) &&
      !notificationsDropdownElement.contains(event.target)) {
    closeNotifications()
  }
  
  // Handle categories dropdown
  const categoriesElement = document.querySelector('.categories-item')
  const categoriesDropdownElement = document.querySelector('.categories-dropdown')
  
  if (showCategoriesDropdown.value && 
      categoriesElement && 
      categoriesDropdownElement &&
      !categoriesElement.contains(event.target) &&
      !categoriesDropdownElement.contains(event.target)) {
    showCategoriesDropdown.value = false
  }
  
  // Handle search suggestions
  const searchSectionElement = document.querySelector('.search-section')
  const searchSuggestionsElement = document.querySelector('.search-suggestions')
  
  if (showSearchSuggestions.value && 
      searchSectionElement && 
      searchSuggestionsElement &&
      !searchSectionElement.contains(event.target) &&
      !searchSuggestionsElement.contains(event.target)) {
    showSearchSuggestions.value = false
  }
}

// Methods
const handleSearch = () => {
  // Clear previous timer
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }

  // Nếu search query rỗng, ẩn suggestions
  if (!searchQuery.value.trim()) {
    searchSuggestions.value = []
    showSearchSuggestions.value = false
    return
  }

  // Debounce search - chờ 300ms sau khi người dùng ngừng gõ
  searchDebounceTimer = setTimeout(async () => {
    await fetchSearchSuggestions()
  }, 300)
}

const fetchSearchSuggestions = async () => {
  if (!searchQuery.value.trim()) {
    searchSuggestions.value = []
    return
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/api/san-pham`, {
      params: {
        search: searchQuery.value.trim(),
      },
    })

    if (response.data && Array.isArray(response.data)) {
      // Giới hạn 5 suggestions
      searchSuggestions.value = response.data.slice(0, 5)
      showSearchSuggestions.value = true
    } else {
      searchSuggestions.value = []
    }
  } catch (error) {
    console.error('Error fetching search suggestions:', error)
    searchSuggestions.value = []
  }
}

const handleSearchBlur = () => {
  // Delay để cho phép click vào suggestion
  setTimeout(() => {
    showSearchSuggestions.value = false
  }, 200)
}

const selectSuggestion = (product) => {
  // Điều hướng đến trang chi tiết sản phẩm
  router.push({ name: 'product-detail', params: { id: product.id } })
  searchQuery.value = ''
  searchSuggestions.value = []
  showSearchSuggestions.value = false
}

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/shop', query: { q: searchQuery.value } })
    searchQuery.value = ''
    searchSuggestions.value = []
    showSearchSuggestions.value = false
  }
}

const getProductImage = (imagePath) => {
  if (!imagePath) {
    return 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
  }
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const handleSuggestionImageError = (event) => {
  event.target.src =
    'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Customer Login Handler (JWT)
async function handleCustomerLogin() {
  if (!customerLoginForm.value.email || !customerLoginForm.value.password) {
    showToast('error', 'Vui lòng nhập đầy đủ thông tin', 'bi-exclamation-circle-fill')
    return
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(customerLoginForm.value.email)) {
    showToast('error', 'Email không hợp lệ', 'bi-exclamation-circle-fill')
    return
  }

  customerLoginLoading.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/login`, {
      email: customerLoginForm.value.email.trim().toLowerCase(),
      password: customerLoginForm.value.password,
    })

    if (response.data && response.data.success) {
      // Lưu token và user info
      localStorage.setItem('customerToken', response.data.token)
      localStorage.setItem('customerUser', JSON.stringify(response.data.user))

      // Update local state
      loadCustomerFromStorage()

      showLogin.value = false
      showToast('success', 'Đăng nhập thành công!', 'bi-check-circle-fill')

      // Reload page để cập nhật trạng thái đăng nhập
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      showToast('error', response.data?.error || 'Đăng nhập thất bại', 'bi-exclamation-circle-fill')
    }
  } catch (error) {
    console.error('Customer login error:', error)
    const errorMessage =
      error.response?.data?.error || error.response?.data?.message || 'Có lỗi xảy ra khi đăng nhập'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    customerLoginLoading.value = false
  }
}

// Google Login handlers
const handleLoginSuccess = () => {
  // Update local state
  loadCustomerFromStorage()

  showLogin.value = false
  showToast('success', 'Đăng nhập thành công!', 'bi-check-circle-fill')
  // Reload page để cập nhật trạng thái đăng nhập
  setTimeout(() => {
    window.location.reload()
  }, 1000)
}

const handleLoginError = (errorMessage) => {
  showToast('error', errorMessage || 'Đăng nhập thất bại', 'bi-exclamation-circle-fill')
}

// Logout handler
const handleLogout = () => {
  localStorage.removeItem('customerToken')
  localStorage.removeItem('customerUser')
  showUserDropdown.value = false
  router.push('/')
  window.location.reload()
}

// Handle dropdown leave with delay
let dropdownTimeout = null
const handleDropdownLeave = () => {
  // Delay để người dùng có thể di chuột từ button sang dropdown menu
  dropdownTimeout = setTimeout(() => {
    showUserDropdown.value = false
  }, 200) // 200ms delay
}

// Clear timeout khi hover lại
const handleDropdownEnter = () => {
  if (dropdownTimeout) {
    clearTimeout(dropdownTimeout)
    dropdownTimeout = null
  }
  showUserDropdown.value = true
}

// Register Handlers
const openRegisterModal = () => {
  showLogin.value = false
  showRegister.value = true
}

const openLoginModal = () => {
  showRegister.value = false
  showLogin.value = true
}

// Customer Register Handler
async function handleCustomerRegister() {
  // Validation
  if (!customerRegisterForm.value.hoTen || !customerRegisterForm.value.email ||
      !customerRegisterForm.value.soDienThoai || !customerRegisterForm.value.password ||
      !customerRegisterForm.value.confirmPassword || !customerRegisterForm.value.ngaySinh) {
    showToast('error', 'Vui lòng nhập đầy đủ thông tin', 'bi-exclamation-circle-fill')
    return
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(customerRegisterForm.value.email)) {
    showToast('error', 'Email không hợp lệ', 'bi-exclamation-circle-fill')
    return
  }

  // Validate phone number
  if (!customerRegisterForm.value.soDienThoai.match(/^(0[3|5|7|8|9])+([0-9]{8})$/)) {
    showToast('error', 'Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số', 'bi-exclamation-circle-fill')
    return
  }

  // Validate password
  if (customerRegisterForm.value.password.length < 6) {
    showToast('error', 'Mật khẩu phải có ít nhất 6 ký tự', 'bi-exclamation-circle-fill')
    return
  }

  // Validate password match
  if (customerRegisterForm.value.password !== customerRegisterForm.value.confirmPassword) {
    showToast('error', 'Mật khẩu xác nhận không khớp', 'bi-exclamation-circle-fill')
    return
  }

  // Validate age (must be at least 18 years old)
  if (customerRegisterForm.value.ngaySinh) {
    const birthDate = new Date(customerRegisterForm.value.ngaySinh)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()

    // Adjust age if birthday hasn't occurred this year
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }

    if (age < 18) {
      showToast('error', 'Bạn phải đủ 18 tuổi mới được đăng ký tài khoản', 'bi-exclamation-circle-fill')
      return
    }
  } else {
    // Nếu không nhập ngày sinh, yêu cầu nhập để kiểm tra tuổi
    showToast('error', 'Vui lòng nhập ngày sinh để xác minh độ tuổi', 'bi-exclamation-circle-fill')
    return
  }

  customerRegisterLoading.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/register`, {
      hoTen: customerRegisterForm.value.hoTen.trim(),
      email: customerRegisterForm.value.email.trim().toLowerCase(),
      soDienThoai: customerRegisterForm.value.soDienThoai.trim(),
      password: customerRegisterForm.value.password,
      confirmPassword: customerRegisterForm.value.confirmPassword,
      gioiTinh: customerRegisterForm.value.gioiTinh || null,
      ngaySinh: customerRegisterForm.value.ngaySinh || null,
    })

    if (response.data && response.data.success) {
      // Lưu token và user info
      localStorage.setItem('customerToken', response.data.token)
      localStorage.setItem('customerUser', JSON.stringify(response.data.user))

      // Update local state
      loadCustomerFromStorage()

      showRegister.value = false
      showToast('success', response.data.message || 'Đăng ký thành công!', 'bi-check-circle-fill')

      // Reset form
      customerRegisterForm.value = {
        hoTen: '',
        email: '',
        soDienThoai: '',
        password: '',
        confirmPassword: '',
        gioiTinh: '',
        ngaySinh: '',
      }

      // Reload page để cập nhật trạng thái đăng nhập
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      showToast('error', response.data?.error || 'Đăng ký thất bại', 'bi-exclamation-circle-fill')
    }
  } catch (error) {
    console.error('Customer register error:', error)
    const errorMessage =
      error.response?.data?.error || error.response?.data?.message || 'Có lỗi xảy ra khi đăng ký'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    customerRegisterLoading.value = false
  }
}

// Forgot Password Handlers
const openForgotPasswordModal = () => {
  showLogin.value = false
  showForgotPassword.value = true
}

const closeForgotPasswordModal = () => {
  showForgotPassword.value = false
  forgotPasswordStep.value = 1
  forgotPasswordForm.value = {
    email: '',
    otp: '',
    newPassword: '',
    confirmPassword: '',
    resetToken: '',
  }
  resendCooldown.value = 0
  if (resendTimer) {
    clearInterval(resendTimer)
    resendTimer = null
  }
}

const handleSendOTP = async () => {
  if (!forgotPasswordForm.value.email || !forgotPasswordForm.value.email.includes('@')) {
    showToast('error', 'Vui lòng nhập email hợp lệ', 'bi-exclamation-circle-fill')
    return
  }

  sendingOTP.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/forgot-password`, {
      email: forgotPasswordForm.value.email.trim().toLowerCase(),
    })

    if (response.data && response.data.success) {
      showToast(
        'success',
        response.data.message || 'Mã OTP đã được gửi đến email của bạn!',
        'bi-check-circle-fill',
      )
      forgotPasswordStep.value = 2
      startResendCooldown()
    } else {
      showToast(
        'error',
        response.data?.message || 'Có lỗi xảy ra khi gửi mã OTP',
        'bi-exclamation-circle-fill',
      )
    }
  } catch (error) {
    console.error('Send OTP error:', error)
    let errorMessage = 'Có lỗi xảy ra khi gửi mã OTP'

    if (error.response?.data) {
      if (error.response.data.message) {
        errorMessage = error.response.data.message
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error
      } else if (typeof error.response.data === 'string') {
        errorMessage = error.response.data
      }
    } else if (error.message) {
      errorMessage = error.message
    }

    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    sendingOTP.value = false
  }
}

const handleVerifyOTP = async () => {
  if (!forgotPasswordForm.value.otp || forgotPasswordForm.value.otp.length !== 6) {
    showToast('error', 'Vui lòng nhập mã OTP 6 số', 'bi-exclamation-circle-fill')
    return
  }

  verifyingOTP.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/verify-otp`, {
      email: forgotPasswordForm.value.email.trim().toLowerCase(),
      otp: forgotPasswordForm.value.otp.trim(),
    })

    if (response.data && response.data.success) {
      forgotPasswordForm.value.resetToken = response.data.resetToken
      showToast(
        'success',
        response.data.message || 'Xác thực OTP thành công!',
        'bi-check-circle-fill',
      )
      setTimeout(() => {
        forgotPasswordStep.value = 3
      }, 1000)
    } else {
      showToast(
        'error',
        response.data?.message || 'Mã OTP không đúng hoặc đã hết hạn',
        'bi-exclamation-circle-fill',
      )
    }
  } catch (error) {
    console.error('Verify OTP error:', error)
    const errorMessage = error.response?.data?.message || 'Mã OTP không đúng hoặc đã hết hạn'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    verifyingOTP.value = false
  }
}

const handleResendOTP = async () => {
  if (resendCooldown.value > 0) return

  resendingOTP.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/forgot-password`, {
      email: forgotPasswordForm.value.email.trim().toLowerCase(),
    })

    if (response.data && response.data.success) {
      showToast('success', 'Mã OTP mới đã được gửi!', 'bi-check-circle-fill')
      forgotPasswordForm.value.otp = ''
      startResendCooldown()
    } else {
      showToast(
        'error',
        response.data?.message || 'Có lỗi xảy ra khi gửi lại mã OTP',
        'bi-exclamation-circle-fill',
      )
    }
  } catch (error) {
    console.error('Resend OTP error:', error)
    const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi gửi lại mã OTP'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    resendingOTP.value = false
  }
}

const handleResetPassword = async () => {
  if (!forgotPasswordForm.value.newPassword || forgotPasswordForm.value.newPassword.length < 6) {
    showToast('error', 'Mật khẩu phải có ít nhất 6 ký tự', 'bi-exclamation-circle-fill')
    return
  }

  if (forgotPasswordForm.value.newPassword !== forgotPasswordForm.value.confirmPassword) {
    showToast('error', 'Mật khẩu xác nhận không khớp', 'bi-exclamation-circle-fill')
    return
  }

  if (!forgotPasswordForm.value.resetToken) {
    showToast(
      'error',
      'Token không hợp lệ. Vui lòng thực hiện lại từ đầu',
      'bi-exclamation-circle-fill',
    )
    return
  }

  resettingPassword.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/api/customer/auth/reset-password`, {
      resetToken: forgotPasswordForm.value.resetToken,
      newPassword: forgotPasswordForm.value.newPassword,
    })

    if (response.data && response.data.success) {
      showToast(
        'success',
        response.data.message || 'Đặt lại mật khẩu thành công!',
        'bi-check-circle-fill',
      )
      setTimeout(() => {
        closeForgotPasswordModal()
        showLogin.value = true
      }, 2000)
    } else {
      showToast(
        'error',
        response.data?.message || 'Có lỗi xảy ra khi đặt lại mật khẩu',
        'bi-exclamation-circle-fill',
      )
    }
  } catch (error) {
    console.error('Reset password error:', error)
    const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi đặt lại mật khẩu'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    resettingPassword.value = false
  }
}

const startResendCooldown = () => {
  resendCooldown.value = 60
  if (resendTimer) {
    clearInterval(resendTimer)
  }
  resendTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) {
      clearInterval(resendTimer)
      resendTimer = null
    }
  }, 1000)
}

const showToast = (type, message, icon) => {
  toast.value = {
    show: true,
    type,
    message,
    icon,
  }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// Lifecycle
let loginCheckInterval = null

onMounted(() => {
  // Load customer info from localStorage
  loadCustomerFromStorage()
  
  // Start notifications polling if logged in
  if (isCustomerLoggedIn.value) {
    startNotificationsPolling()
  }
  
  // Watch for login changes
  const checkLogin = () => {
    if (isCustomerLoggedIn.value && !notificationsPollingInterval) {
      startNotificationsPolling()
    } else if (!isCustomerLoggedIn.value) {
      stopNotificationsPolling()
      notifications.value = []
      unreadCount.value = 0
    }
  }
  
  // Check login status every second
  loginCheckInterval = setInterval(checkLogin, 1000)
  
  // Add click outside listener for notifications
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (resendTimer) {
    clearInterval(resendTimer)
    resendTimer = null
  }
  if (loginCheckInterval) {
    clearInterval(loginCheckInterval)
    loginCheckInterval = null
  }
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
  stopNotificationsPolling()
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* Phoenix Theme Colors */
:root {
  --phoenix-primary: #ff6b35;
  --phoenix-secondary: #f7931e;
  --phoenix-accent: #dc143c;
  --phoenix-gold: #ffd700;
  --phoenix-dark: #2c1810;
  --phoenix-light: #fff5e1;
}

/* Header */
.main-header {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  height: 77px;
  position: relative;
}

.header-container::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 100%
  );
}

.logo-section .logo-img {
  height: 60px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.3));
  transition: all 0.3s ease;
}

.logo-section:hover .logo-img {
  transform: scale(1.05);
  filter: drop-shadow(2px 2px 6px rgba(0, 0, 0, 0.4));
}

.search-section {
  flex: 1;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.875rem 3rem 0.875rem 1.25rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50px;
  font-size: 0.95rem;
  outline: none;
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-input:focus {
  background: white;
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.search-icon {
  position: absolute;
  right: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  color: #ff5500;
  font-size: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 1;
}

.search-icon:hover {
  color: #dc143c;
  transform: translateY(-50%) scale(1.1);
}

/* Search Suggestions Dropdown */
.search-suggestions {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  max-height: 500px;
  overflow-y: auto;
  z-index: 1100;
  border: 1px solid #e9ecef;
}

.suggestion-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: #f8f9fa;
}

.suggestion-image {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.suggestion-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.suggestion-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.suggestion-name {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.suggestion-details {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.suggestion-brand {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.suggestion-specs {
  font-size: 0.8rem;
  color: #999;
}

.suggestion-price {
  font-size: 1rem;
  font-weight: 700;
  color: #ff5500;
  margin-top: 0.25rem;
}

.suggestion-footer {
  padding: 0.75rem 1rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
  text-align: center;
}

.view-all-results-btn {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-results-btn:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

/* Scrollbar for suggestions */
.search-suggestions::-webkit-scrollbar {
  width: 6px;
}

.search-suggestions::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.search-suggestions::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.search-suggestions::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.header-actions {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: white;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  position: relative;
}

.action-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.action-item i {
  font-size: 1.5rem;
  transition: all 0.3s ease;
}

.action-item:hover i {
  transform: scale(1.1);
}

.action-text {
  font-size: 0.9rem;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-text .label {
  display: block;
  font-size: 0.75rem;
  opacity: 0.9;
}

/* User Dropdown */
.user-dropdown-wrapper {
  position: relative;
}

.dropdown-arrow {
  font-size: 0.75rem;
  transition: transform 0.3s ease;
  margin-left: 0.25rem;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.user-dropdown-menu {
  position: absolute;
  top: calc(100% + 2px);
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  min-width: 220px;
  z-index: 1000;
  overflow: hidden;
  animation: slideDown 0.3s ease;
  /* Tạo vùng đệm vô hình để dễ di chuột */
  padding-top: 4px;
  margin-top: -4px;
}

.user-dropdown-menu::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 0;
  right: 0;
  height: 4px;
  background: transparent;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  color: #333;
  text-decoration: none;
  transition: all 0.2s ease;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-size: 0.95rem;
}

.dropdown-item:hover {
  background: #f8f9fa;
  color: #FF5500;
}

.dropdown-item i {
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
}

.dropdown-item.logout-item {
  color: #dc3545;
}

.dropdown-item.logout-item:hover {
  background: #fff5f5;
  color: #dc3545;
}

.dropdown-divider {
  height: 1px;
  background: #e9ecef;
  margin: 0.5rem 0;
}

.cart-icon-wrapper {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  color: white;
  font-size: 0.75rem;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(220, 20, 60, 0.4);
  border: 2px solid white;
  padding: 0 4px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* Notifications */
.notification-icon-wrapper {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(220, 20, 60, 0.4);
  border: 2px solid white;
  padding: 0 4px;
  animation: pulse 2s infinite;
}

.notifications-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 380px;
  max-height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1100;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.action-item.notifications {
  position: relative;
  z-index: 1101;
}

.notifications-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.notifications-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
}

.mark-all-read-btn {
  background: transparent;
  border: none;
  color: #ff5500;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.mark-all-read-btn:hover {
  background: rgba(255, 85, 0, 0.1);
}

.notifications-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.no-notifications {
  padding: 3rem 2rem;
  text-align: center;
  color: #999;
}

.no-notifications i {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-notifications p {
  margin: 0;
  font-size: 0.95rem;
}

.notification-item {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: #fff9e6;
}

.notification-item.unread:hover {
  background: #fff5d6;
}

.notification-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.notification-content p {
  margin: 0 0 0.5rem 0;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 0.75rem;
  color: #999;
  display: block;
  margin-top: 0.5rem;
}

.btn-view-order {
  margin-top: 0.75rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.3);
}

.btn-view-order:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.4);
}

.btn-view-order:active {
  transform: translateY(0);
}

.unread-indicator {
  position: absolute;
  top: 1rem;
  right: 1.25rem;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff5500;
}

.notifications-footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid #e9ecef;
  text-align: center;
  background: #f8f9fa;
}

.notifications-footer a {
  color: #ff5500;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: color 0.2s ease;
}

.notifications-footer a:hover {
  color: #dc143c;
  text-decoration: underline;
}

/* Navigation Menu */
.nav-menu {
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-menu-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0.25rem 2rem;
  display: flex;
  align-items: center;
}

.categories-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  position: relative;
}

.categories-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.categories-btn::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) scaleX(1);
  width: 80%;
  height: 2px;
  background: white;
  border-radius: 2px;
  transition: transform 0.3s ease;
}

.categories-btn i:first-child {
  font-size: 1rem;
}

.categories-btn i:last-child {
  font-size: 0.65rem;
  margin-left: 0.25rem;
  transition: transform 0.3s ease;
}

.categories-btn i.rotated {
  transform: rotate(180deg);
}

.categories-item {
  position: relative;
}

.categories-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 250px;
  max-width: 350px;
  max-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1100;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.categories-loading,
.categories-empty {
  padding: 2rem 1.5rem;
  text-align: center;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.categories-loading i {
  font-size: 1.5rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.categories-empty i {
  font-size: 2rem;
  opacity: 0.5;
}

.categories-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.category-item {
  padding: 0.875rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

.category-item:hover {
  background: #f8f9fa;
  color: #ff5500;
}

.category-item span {
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  transition: color 0.2s ease;
}

.category-item:hover span {
  color: #ff5500;
}

.category-item i {
  font-size: 0.75rem;
  color: #999;
  transition: all 0.2s ease;
}

.category-item:hover i {
  color: #ff5500;
  transform: translateX(4px);
}

/* Scrollbar styling for categories dropdown */
.categories-list::-webkit-scrollbar {
  width: 6px;
}

.categories-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.categories-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.categories-list::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.menu-list {
  width: 100%;
  display: flex;
  gap: 0;
  list-style: none;
  justify-content: space-between;
  margin: 0;
  padding: 0;
  align-items: center;
}

.menu-list li {
  display: flex;
  align-items: center;
}

.menu-list li:first-child .categories-btn,
.menu-list li:first-child .menu-link {
  margin-left: 0;
}

.menu-list li:last-child .menu-link {
  margin-right: 0;
}

.menu-link {
  color: white;
  text-decoration: none;
  font-weight: 600;
  padding: 0.25rem 0.75rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  font-size: 0.9rem;
}

.menu-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 80%;
  height: 2px;
  background: white;
  border-radius: 2px;
  transition: transform 0.3s ease;
}

.menu-link:hover,
.menu-link.active {
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.menu-link:hover::after,
.menu-link.active::after {
  transform: translateX(-50%) scaleX(1);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-box {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 2.5rem;
  border-radius: 20px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid #e9ecef;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-weight: 700;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.modal-header h3::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  border-radius: 2px;
}

.close-modal {
  background: rgba(0, 0, 0, 0.05);
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-modal:hover {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  transform: rotate(90deg) scale(1.1);
}

.modal-body {
  text-align: center;
}

.modal-body .btn {
  margin-top: 1rem;
  padding: 0.875rem 2rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.modal-body .btn:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

/* Login Modal Styles */
.login-modal {
  max-width: 400px;
}

.login-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.login-description {
  text-align: center;
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 10px 0;
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e0e0e0;
}

.login-divider span {
  padding: 0 15px;
  color: #999;
  font-size: 12px;
}

.login-note {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin: 0;
}

.login-note a {
  color: #4285f4;
  text-decoration: none;
}

.login-note a:hover {
  text-decoration: underline;
}

/* Customer Login Form Styles */
.customer-login-form {
  margin-bottom: 20px;
}

.customer-login-form .form-group {
  margin-bottom: 16px;
}

.customer-login-form label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
  text-align: left;
}

.customer-login-form .form-group {
  text-align: left;
}

.customer-login-form .form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
  text-align: left;
}

.customer-login-form .form-input::placeholder {
  text-align: left;
}

.customer-login-form .form-input:focus {
  outline: none;
  border-color: #4285f4;
}

.btn-login-jwt {
  width: 100%;
  padding: 12px 20px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-login-jwt:hover:not(:disabled) {
  background: linear-gradient(135deg, #e55a2b, #e8841a);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.btn-login-jwt:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Forgot Password Link */
.forgot-password-link {
  margin-top: 0.75rem;
  text-align: right;
}

.forgot-password-link a {
  color: #d32f2f;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.forgot-password-link a:hover {
  color: #b71c1c;
  text-decoration: underline;
}

/* Register Link Section */
.register-link-section {
  margin-top: 1rem;
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
}

.register-text {
  color: #666;
  font-size: 0.875rem;
  margin: 0;
}

.register-link {
  color: #ff6b35;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #e55a2b;
  text-decoration: underline;
}

/* Register Modal */
.register-modal {
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-content {
  padding: 1rem 0;
}

.register-description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.customer-register-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.customer-register-form .form-group {
  text-align: left;
  margin-bottom: 0;
}

.customer-register-form .form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  text-align: left;
}

.customer-register-form .form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  text-align: left;
  box-sizing: border-box;
}

.customer-register-form .form-input:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.customer-register-form select.form-input {
  cursor: pointer;
}

.btn-register {
  width: 100%;
  padding: 0.875rem 1.5rem;
  background: linear-gradient(135deg, #ff6b35 0%, #e55a2b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
  margin-top: 0.5rem;
}

.btn-register:hover:not(:disabled) {
  background: linear-gradient(135deg, #e55a2b, #e8841a);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.login-link-section {
  margin-top: 1rem;
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
}

.login-text {
  color: #666;
  font-size: 0.875rem;
  margin: 0;
}

.login-link {
  color: #ff6b35;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #e55a2b;
  text-decoration: underline;
}

.register-divider {
  margin: 1.5rem 0;
  text-align: center;
  position: relative;
}

.register-divider::before,
.register-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 45%;
  height: 1px;
  background: #e0e0e0;
}

.register-divider::before {
  left: 0;
}

.register-divider::after {
  right: 0;
}

.register-divider span {
  background: white;
  padding: 0 1rem;
  color: #999;
  font-size: 0.875rem;
  position: relative;
  z-index: 1;
}

.register-note {
  margin-top: 1rem;
  font-size: 0.75rem;
  color: #999;
  text-align: center;
  line-height: 1.5;
}

.register-note a {
  color: #ff6b35;
  text-decoration: none;
}

.register-note a:hover {
  text-decoration: underline;
}

/* Forgot Password Modal */
.forgot-password-modal {
  max-width: 450px;
}

.forgot-password-content {
  padding: 1rem 0;
}

.forgot-step {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.forgot-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.forgot-form .form-group {
  text-align: left;
  margin-bottom: 0;
}

.forgot-form .form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  text-align: left;
}

.forgot-form .form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  text-align: left;
}

.forgot-form .form-input:focus {
  outline: none;
  border-color: #d32f2f;
  box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.1);
}

.btn-send-otp {
  width: 100%;
  padding: 0.875rem 1.5rem;
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.btn-send-otp:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 152, 0, 0.4);
}

.btn-verify-otp,
.btn-reset-password {
  width: 100%;
  padding: 0.875rem 1.5rem;
  background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.3);
}

.btn-verify-otp:hover:not(:disabled),
.btn-reset-password:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(211, 47, 47, 0.4);
}

.btn-send-otp:disabled,
.btn-verify-otp:disabled,
.btn-reset-password:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.otp-input {
  text-align: center;
  font-size: 1.5rem;
  letter-spacing: 0.5rem;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.form-hint {
  display: block;
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.875rem;
}

.required {
  color: #d32f2f;
  font-weight: bold;
}

.form-error {
  display: block;
  margin-top: 0.5rem;
  color: #d32f2f;
  font-size: 0.875rem;
}

.resend-otp-section {
  text-align: center;
  margin-top: 0.5rem;
}

.btn-resend-otp {
  background: transparent;
  border: 1px solid #d32f2f;
  color: #d32f2f;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-resend-otp:hover:not(:disabled) {
  background: #d32f2f;
  color: white;
}

.btn-resend-otp:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.btn-back {
  flex: 1;
  padding: 0.875rem 1.5rem;
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #e0e0e0;
  border-color: #bbb;
}

.btn-verify-otp,
.btn-reset-password {
  flex: 2;
}

/* Toast Notification */
.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 9999;
  max-width: 400px;
}

.toast-notification.success {
  background: #28a745;
  color: white;
}

.toast-notification.error {
  background: #dc3545;
  color: white;
}

.toast-notification.info {
  background: #17a2b8;
  color: white;
}

.toast-notification i {
  font-size: 1.5rem;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

/* Responsive */
@media (max-width: 992px) {
  .header-container {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .search-section {
    order: 3;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .logo-section .logo-img {
    height: 50px;
  }

  .header-container {
    height: 67px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-around;
  }

  .action-text .label {
    display: none;
  }

  .menu-list {
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
  }
}

@media (max-width: 480px) {
  .logo-section .logo-img {
    height: 45px;
  }

  .header-container {
    height: 62px;
  }
}
</style>
