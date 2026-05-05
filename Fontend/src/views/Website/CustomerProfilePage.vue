<template>
  <div class="customer-profile-page">
    <!-- Header -->
    <HeaderLayout />

    <!-- Main Content -->
    <main class="profile-main">
      <div class="container">
        <div class="profile-layout">
          <!-- Sidebar -->
          <aside class="profile-sidebar">
            <div class="sidebar-content">
              <!-- Customer Name -->
              <div class="customer-name-section">
                <h2>{{ customerName }}</h2>
              </div>

              <!-- Navigation Menu -->
              <nav class="profile-nav">
                <router-link
                  to="/tai-khoan"
                  class="nav-item"
                  :class="{ active: currentTab === 'orders' }"
                  @click="currentTab = 'orders'"
                >
                  <i class="bi bi-receipt-cutoff"></i>
                  <span>Đơn hàng đã mua</span>
                </router-link>
                <router-link
                  to="/tai-khoan/thong-tin"
                  class="nav-item"
                  :class="{ active: currentTab === 'info' }"
                  @click="currentTab = 'info'"
                >
                  <i class="bi bi-person-lines-fill"></i>
                  <span>Thông tin và số địa chỉ</span>
                </router-link>
                <router-link
                  to="/tai-khoan/yeu-thich"
                  class="nav-item"
                  :class="{ active: currentTab === 'favorite' }"
                  @click="currentTab = 'favorite'"
                >
                  <i class="bi bi-heart"></i>
                  <span>Sản phẩm yêu thích</span>
                </router-link>
              </nav>

              <!-- Logout Button -->
              <button class="btn-logout" @click="handleLogout">
                <i class="bi bi-box-arrow-right"></i>
                Đăng Xuất
              </button>
            </div>
          </aside>

          <!-- Main Content Area -->
          <div class="profile-content">
            <!-- Orders Tab -->
            <div v-if="currentTab === 'orders'" class="orders-tab">
              <div class="tab-header">
                <h1>Đơn hàng đã mua</h1>
              </div>

              <!-- Status Tabs -->
              <div class="status-tabs">
                <button
                  v-for="(status, index) in statusTabs"
                  :key="index"
                  :class="['status-tab', { active: selectedStatus === status.value }]"
                  @click="selectedStatus = status.value"
                >
                  {{ status.label }}
                </button>
              </div>

              <!-- Orders List -->
              <div v-if="loadingOrders" class="loading-state">
                <div class="spinner"></div>
                <p>Đang tải đơn hàng...</p>
              </div>

              <div v-else-if="filteredOrders.length === 0" class="empty-state">
                <i class="bi bi-inbox"></i>
                <h3>Chưa có đơn hàng nào</h3>
                <p>Bạn chưa có đơn hàng nào trong khoảng thời gian này</p>
              </div>

              <div v-else class="orders-list">
                <div
                  v-for="order in paginatedOrders"
                  :key="order.id"
                  class="order-card"
                >
                  <div class="order-content">
                    <div class="order-header-row">
                      <div class="order-id">#{{ order.maHoaDon || order.id }}</div>
                      <div class="order-date">{{ formatOrderDate(order) }}</div>
                    </div>
                    <div class="order-body">
                      <div class="order-product-section">
                        <div class="product-image-wrapper">
                          <img
                            :src="getFirstProductImage(order)"
                            :alt="getFirstProductName(order)"
                            class="product-image"
                            @error="($event) => { $event.target.src = getPlaceholderImage() }"
                          />
                        </div>
                        <div class="product-info">
                          <h4 class="product-name">{{ getFirstProductName(order) }}</h4>
                          <div v-if="getFirstProductVariants(order).length > 0" class="product-variants">
                            <span
                              v-for="(variant, idx) in getFirstProductVariants(order)"
                              :key="idx"
                              class="variant-tag"
                            >
                              {{ variant }}
                            </span>
                          </div>
                          <p v-if="getOtherProductsCount(order) > 0" class="other-products">
                            và {{ getOtherProductsCount(order) }} sản phẩm khác
                          </p>
                        </div>
                      </div>
                      <div class="order-details">
                        <div class="order-status-badge" :class="getStatusClass(order.trangThai)">
                          {{ getStatusText(order.trangThai) }}
                        </div>
                        <div class="order-price">{{ formatPrice(order.thanhTien || order.tongTienSauGiam || order.tongTien || 0) }}</div>
                        <router-link
                          :to="`/theo-doi-don-hang?orderId=${order.maHoaDon || order.id}`"
                          class="btn-view-detail"
                        >
                          Xem chi tiết
                        </router-link>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Pagination -->
              <div v-if="filteredOrders.length > pageSize" class="pagination-container">
                <div class="pagination-info">
                  Hiển thị {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredOrders.length) }} trong tổng số {{ filteredOrders.length }} đơn hàng
                </div>
                <div class="pagination-controls">
                  <button 
                    class="pagination-btn"
                    @click="currentPage = Math.max(1, currentPage - 1)"
                    :disabled="currentPage === 1"
                  >
                    <i class="bi bi-chevron-left"></i>
                    Trước
                  </button>
                  <div class="pagination-pages">
                    <button
                      v-for="page in totalPages"
                      :key="page"
                      class="pagination-page-btn"
                      :class="{ active: currentPage === page }"
                      @click="currentPage = page"
                    >
                      {{ page }}
                    </button>
                  </div>
                  <button 
                    class="pagination-btn"
                    @click="currentPage = Math.min(totalPages, currentPage + 1)"
                    :disabled="currentPage === totalPages"
                  >
                    Sau
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- Info Tab -->
            <div v-else-if="currentTab === 'info'" class="info-tab">
              <h1>Thông tin tài khoản</h1>

              <!-- Personal Information Section -->
              <div class="info-section">
                <h2 class="section-title">THÔNG TIN CÁ NHÂN</h2>
                <div class="info-item-card">
                  <div class="info-item-content">
                    <span class="info-text">{{ customerName }} - {{ customerPhone || 'Chưa có số điện thoại' }}</span>
                    <button class="btn-edit" @click="openEditModal">
                      <i class="bi bi-pencil"></i>
                      <span>Sửa</span>
                    </button>
                  </div>
                </div>
              </div>

              <!-- Delivery Address Section -->
              <div class="info-section">
                <h2 class="section-title">ĐỊA CHỈ NHẬN HÀNG</h2>

                <div v-if="loadingAddresses" class="loading-state">
                  <div class="spinner"></div>
                  <p>Đang tải địa chỉ...</p>
                </div>

                <div v-else-if="addresses.length === 0" class="no-address">
                  <p>Chưa có địa chỉ nào</p>
                </div>

                <div v-else class="addresses-list">
                  <div
                    v-for="address in sortedAddresses"
                    :key="address.id"
                    class="address-item-card"
                  >
                    <div class="address-content">
                      <div class="address-text">
                        <span v-if="address.macDinh" class="default-badge">Mặc định</span>
                        {{ formatAddress(address) }}
                      </div>
                      <div class="address-actions">
                        <button
                          class="btn-edit"
                          @click="openEditAddressModal(address)"
                        >
                          <i class="bi bi-pencil"></i>
                          <span>Chỉnh sửa</span>
                        </button>
                        <button
                          v-if="!address.macDinh"
                          class="btn-delete"
                          @click="confirmDeleteAddress(address)"
                        >
                          <i class="bi bi-trash"></i>
                          <span>Xóa</span>
                        </button>
                        <button
                          v-if="!address.macDinh"
                          class="btn-set-default"
                          @click="setDefaultAddress(address)"
                        >
                          <i class="bi bi-star"></i>
                          <span>Đặt mặc định</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Add New Address Button -->
                <button
                  class="btn-add-address"
                  @click="openAddAddressModal"
                  :disabled="addresses.length >= 5"
                >
                  <i class="bi bi-plus-circle"></i>
                  <span>Thêm thông tin địa chỉ giao hàng mới</span>
                </button>
                <div v-if="addresses.length >= 5" class="max-address-warning">
                  <i class="bi bi-info-circle"></i>
                  <span>Bạn đã đạt tối đa 5 địa chỉ giao hàng</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Edit Personal Info Modal -->
    <div v-if="showEditPersonalInfo" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content edit-modal">
        <div class="modal-header">
          <h3>Chỉnh sửa thông tin cá nhân</h3>
          <button class="modal-close" @click="closeEditModal">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleUpdatePersonalInfo" class="edit-form">
            <div class="form-group">
              <label for="hoTen">Họ và tên <span class="required">*</span></label>
              <input
                id="hoTen"
                type="text"
                v-model="editForm.hoTen"
                required
                placeholder="Nhập họ và tên"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="soDienThoai">Số điện thoại</label>
              <input
                id="soDienThoai"
                type="tel"
                v-model="editForm.soDienThoai"
                placeholder="Nhập số điện thoại"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="email">Email</label>
              <input
                id="email"
                type="email"
                v-model="editForm.email"
                placeholder="Nhập email"
                class="form-input"
                :disabled="true"
              />
              <small class="form-hint">Email không thể thay đổi</small>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeEditModal">
                Hủy
              </button>
              <button type="submit" class="btn-save" :disabled="updatingPersonalInfo">
                <span v-if="updatingPersonalInfo">Đang lưu...</span>
                <span v-else>Lưu thay đổi</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Add/Edit Address Modal -->
    <div v-if="showAddAddressModal || showEditAddressModal" class="modal-overlay" @click.self="closeAddressModal">
      <div class="modal-content address-modal">
        <div class="modal-header">
          <h3>{{ showEditAddressModal ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
          <button class="modal-close" @click="closeAddressModal">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSaveAddress" class="address-form">
            <div class="form-group">
              <label for="diaChiChiTiet">Địa chỉ chi tiết <span class="required">*</span></label>
              <input
                id="diaChiChiTiet"
                type="text"
                v-model="addressForm.diaChiChiTiet"
                required
                placeholder="Ví dụ: Số nhà, tên đường"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="tinhThanhPho">Tỉnh/Thành phố <span class="required">*</span></label>
              <input
                id="tinhThanhPho"
                type="text"
                v-model="addressForm.tinhThanhPho"
                required
                placeholder="Nhập tỉnh/thành phố"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="phuongXa">Phường/Xã <span class="required">*</span></label>
              <input
                id="phuongXa"
                type="text"
                v-model="addressForm.phuongXa"
                required
                placeholder="Nhập phường/xã"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="maBuuDien">Mã bưu điện</label>
              <input
                id="maBuuDien"
                type="text"
                v-model="addressForm.maBuuDien"
                placeholder="Nhập mã bưu điện (tùy chọn)"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label for="loaiDiaChi">Loại địa chỉ</label>
              <select id="loaiDiaChi" v-model="addressForm.loaiDiaChi" class="form-input">
                <option value="Nhà riêng">Nhà riêng</option>
                <option value="Công ty">Công ty</option>
                <option value="Khác">Khác</option>
              </select>
            </div>

            <div class="form-group">
              <label>
                <input
                  type="checkbox"
                  v-model="addressForm.macDinh"
                  class="checkbox-input"
                />
                <span>Đặt làm địa chỉ mặc định</span>
              </label>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeAddressModal">
                Hủy
              </button>
              <button type="submit" class="btn-save" :disabled="savingAddress">
                <span v-if="savingAddress">Đang lưu...</span>
                <span v-else>Lưu địa chỉ</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="modal-content delete-modal">
        <div class="modal-header">
          <h3>Xác nhận xóa</h3>
          <button class="modal-close" @click="showDeleteConfirm = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa địa chỉ này không?</p>
          <div class="delete-actions">
            <button type="button" class="btn-cancel" @click="showDeleteConfirm = false">
              Hủy
            </button>
            <button type="button" class="btn-delete-confirm" @click="handleDeleteAddress" :disabled="deletingAddress">
              <span v-if="deletingAddress">Đang xóa...</span>
              <span v-else>Xóa</span>
            </button>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

// State
const customerUser = ref(null)
const orders = ref([])
const loadingOrders = ref(false)
const addresses = ref([])
const loadingAddresses = ref(false)
const currentTab = ref('orders')
const selectedStatus = ref('all')
const startDate = ref(new Date(Date.now() - 365 * 24 * 60 * 60 * 1000)) // 1 year ago
const endDate = ref(new Date())
const currentPage = ref(1)
const pageSize = ref(5)
const totalPoints = ref(0)
const showEditPersonalInfo = ref(false)
const showAddAddressModal = ref(false)
const showEditAddressModal = ref(false)
const showDeleteConfirm = ref(false)
const updatingPersonalInfo = ref(false)
const savingAddress = ref(false)
const deletingAddress = ref(false)
const addressToDelete = ref(null)
const editingAddress = ref(null)
const editForm = ref({
  hoTen: '',
  soDienThoai: '',
  email: ''
})

const addressForm = ref({
  diaChiChiTiet: '',
  tinhThanhPho: '',
  phuongXa: '',
  quanHuyen: '',
  maBuuDien: '',
  loaiDiaChi: 'Nhà riêng',
  macDinh: false
})

// Toast
const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: ''
})

// Status tabs
const statusTabs = ref([
  { label: 'Tất cả', value: 'all' },
  { label: 'Chờ xử lý', value: 'pending' },
  { label: 'Đã xác nhận', value: 'confirmed' },
  { label: 'Đang chuyển hàng', value: 'shipping' },
  { label: 'Đang giao hàng', value: 'delivering' },
  { label: 'Đã hủy', value: 'cancelled' },
  { label: 'Thành công', value: 'success' }
])

// Computed
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

const filteredOrders = computed(() => {
  let filtered = []
  if (selectedStatus.value === 'all') {
    filtered = orders.value
  } else {
    const statusMap = {
      'pending': [0],
      'confirmed': [1],
      'shipping': [2],
      'delivering': [2],
      'cancelled': [4],
      'success': [3]
    }
    const statusCodes = statusMap[selectedStatus.value] || []
    filtered = orders.value.filter(order => statusCodes.includes(order.trangThai))
  }
  return filtered
})

// Paginated orders - chỉ hiển thị 5 đơn hàng mỗi trang
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// Total pages
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize.value)
})

// Reset to page 1 when status changes
watch(selectedStatus, () => {
  currentPage.value = 1
})

const customerPhone = computed(() => {
  return customerUser.value?.soDienThoai || null
})

const sortedAddresses = computed(() => {
  if (!addresses.value || addresses.value.length === 0) return []

  // Sort addresses: default first, then by creation date
  return [...addresses.value].sort((a, b) => {
    // Default addresses first
    if (a.macDinh && !b.macDinh) return -1
    if (!a.macDinh && b.macDinh) return 1

    // Then sort by creation date (newest first)
    const dateA = new Date(a.ngayTao || 0).getTime()
    const dateB = new Date(b.ngayTao || 0).getTime()
    return dateB - dateA
  })
})

// Methods
const loadCustomerFromStorage = () => {
  const token = localStorage.getItem('customerToken')
  const userStr = localStorage.getItem('customerUser')

  if (token && userStr) {
    try {
      customerUser.value = JSON.parse(userStr)
    } catch (e) {
      console.error('Error parsing customer user:', e)
      localStorage.removeItem('customerToken')
      localStorage.removeItem('customerUser')
      router.push('/')
    }
  } else {
    router.push('/')
  }
}

const loadOrders = async () => {
  if (!customerUser.value?.id) {
    orders.value = []
    return
  }

  loadingOrders.value = true

  try {
    const response = await api.get(`/api/hoa-don/khach-hang/${customerUser.value.id}`)
    const data = response.data || response

    if (data && Array.isArray(data)) {
      // Debug: Log first order to see structure (only in development)
      if (data.length > 0 && import.meta.env.DEV) {
        const items = data[0].chiTietHoaDonList || data[0].chiTietHoaDon || data[0].chiTietDonHang || []
        if (items.length > 0) {
          console.log('📦 First item hinhAnh:', items[0].hinhAnh, '→ Full URL:', getImageUrl(items[0].hinhAnh))
          console.log('📦 First item tenSanPham:', items[0].tenSanPham)
        }
      }
      
      // Sort by date descending (newest first)
      orders.value = data.sort((a, b) => {
        const dateA = new Date(a.ngayTao || a.ngayDat || 0)
        const dateB = new Date(b.ngayTao || b.ngayDat || 0)
        return dateB - dateA
      })
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    orders.value = []
  } finally {
    loadingOrders.value = false
  }
}

const loadAddresses = async () => {
  if (!customerUser.value?.id) {
    addresses.value = []
    return
  }

  loadingAddresses.value = true

  try {
    const response = await api.get(`/api/user-dia-chi/khach-hang/${customerUser.value.id}`)
    const addressesData = response.data || []

    if (addressesData && Array.isArray(addressesData)) {
      // Process UserDiaChi data structure
      addresses.value = addressesData.map((userDiaChi) => {
        const diaChi = userDiaChi.diaChi || {}
        return {
          id: userDiaChi.id,
          diaChiChiTiet: diaChi.diaChiChiTiet || '',
          phuongXa: diaChi.phuongXa || '',
          quanHuyen: diaChi.quanHuyen || '',
          tinhThanhPho: diaChi.tinhThanhPho || '',
          maBuuDien: diaChi.maBuuDien || '',
          ghiChu: diaChi.ghiChu || '',
          loaiDiaChi: userDiaChi.loaiDiaChi || '',
          macDinh: userDiaChi.macDinh || false,
          ngayTao: userDiaChi.ngayTao || '',
          ngayCapNhat: userDiaChi.ngayCapNhat || '',
          trangThai: userDiaChi.trangThai || 1
        }
      })
    } else {
      addresses.value = []
    }
  } catch (error) {
    console.error('Error loading addresses:', error)
    addresses.value = []
  } finally {
    loadingAddresses.value = false
  }
}

const formatAddress = (address) => {
  const parts = []
  // Handle both direct fields and nested diaChi object
  const diaChi = address.diaChi || {}
  const diaChiChiTiet = address.diaChiChiTiet || diaChi.diaChiChiTiet || ''
  const phuongXa = address.phuongXa || diaChi.phuongXa || ''
  const quanHuyen = address.quanHuyen || diaChi.quanHuyen || ''
  const tinhThanhPho = address.tinhThanhPho || diaChi.tinhThanhPho || ''

  if (diaChiChiTiet) parts.push(diaChiChiTiet)
  if (phuongXa) parts.push(phuongXa)
  if (quanHuyen) parts.push(quanHuyen)
  if (tinhThanhPho) parts.push(tinhThanhPho)

  return parts.join(', ') || 'Chưa có địa chỉ'
}

const openAddAddressModal = () => {
  if (addresses.value.length >= 5) {
    showToast('error', 'Bạn đã đạt tối đa 5 địa chỉ giao hàng', 'bi-exclamation-circle-fill')
    return
  }

  addressForm.value = {
    diaChiChiTiet: '',
    tinhThanhPho: '',
    phuongXa: '',
    quanHuyen: '',
    maBuuDien: '',
    loaiDiaChi: 'Nhà riêng',
    macDinh: addresses.value.length === 0 // Set as default if no addresses
  }
  editingAddress.value = null
  showAddAddressModal.value = true
}

const openEditAddressModal = (address) => {
  editingAddress.value = address
  const diaChi = address.diaChi || {}
  addressForm.value = {
    diaChiChiTiet: diaChi.diaChiChiTiet || '',
    tinhThanhPho: diaChi.tinhThanhPho || '',
    phuongXa: diaChi.phuongXa || '',
    quanHuyen: diaChi.quanHuyen || '',
    maBuuDien: diaChi.maBuuDien || '',
    loaiDiaChi: address.loaiDiaChi || 'Nhà riêng',
    macDinh: address.macDinh || false
  }
  showEditAddressModal.value = true
}

const closeAddressModal = () => {
  showAddAddressModal.value = false
  showEditAddressModal.value = false
  editingAddress.value = null
  addressForm.value = {
    diaChiChiTiet: '',
    tinhThanhPho: '',
    phuongXa: '',
    quanHuyen: '',
    maBuuDien: '',
    loaiDiaChi: 'Nhà riêng',
    macDinh: false
  }
}

const handleSaveAddress = async () => {
  if (!customerUser.value?.id) {
    showToast('error', 'Không tìm thấy thông tin khách hàng', 'bi-exclamation-circle-fill')
    return
  }

  savingAddress.value = true

  try {
    // Prepare DiaChi data
    const diaChiData = {
      diaChiChiTiet: addressForm.value.diaChiChiTiet.trim(),
      tinhThanhPho: addressForm.value.tinhThanhPho.trim(),
      phuongXa: addressForm.value.phuongXa.trim(),
      quanHuyen: addressForm.value.quanHuyen.trim() || '',
      maBuuDien: addressForm.value.maBuuDien.trim() || '',
      ghiChu: '',
      trangThai: 1
    }

    let diaChiId

    if (showEditAddressModal.value && editingAddress.value) {
      // Get diaChi ID from editingAddress (could be in diaChi.id or idDiaChi)
      const existingDiaChiId = editingAddress.value.diaChi?.id || editingAddress.value.idDiaChi

      if (existingDiaChiId) {
        // Update existing DiaChi
        const updateResponse = await api.put(`/api/dia-chi/${existingDiaChiId}`, diaChiData)
        diaChiId = updateResponse.data.id
      } else {
        // If no DiaChi ID, create new one
        const createResponse = await api.post('/api/dia-chi', diaChiData)
        diaChiId = createResponse.data.id
      }

      // Update UserDiaChi
      const userDiaChiData = {
        idDiaChi: diaChiId,
        idUser: customerUser.value.id,
        loaiDiaChi: addressForm.value.loaiDiaChi,
        macDinh: addressForm.value.macDinh,
        trangThai: 1
      }

      await api.put(`/api/user-dia-chi/${editingAddress.value.id}`, userDiaChiData)

      // If setting as default, update other addresses
      if (addressForm.value.macDinh) {
        await api.put(`/api/user-dia-chi/${editingAddress.value.id}/mac-dinh`)
      }

      showToast('success', 'Cập nhật địa chỉ thành công!', 'bi-check-circle-fill')
    } else {
      // Check address limit
      if (addresses.value.length >= 5) {
        showToast('error', 'Bạn đã đạt tối đa 5 địa chỉ giao hàng', 'bi-exclamation-circle-fill')
        savingAddress.value = false
        return
      }

      // Create new DiaChi
      const createResponse = await api.post('/api/dia-chi', diaChiData)
      diaChiId = createResponse.data.id

      // Create UserDiaChi
      const userDiaChiData = {
        idDiaChi: diaChiId,
        idUser: customerUser.value.id,
        loaiDiaChi: addressForm.value.loaiDiaChi,
        macDinh: addressForm.value.macDinh,
        trangThai: 1
      }

      const userDiaChiResponse = await api.post('/api/user-dia-chi', userDiaChiData)

      // If setting as default, update API
      if (addressForm.value.macDinh) {
        await api.put(`/api/user-dia-chi/${userDiaChiResponse.data.id}/mac-dinh`)
      }

      showToast('success', 'Thêm địa chỉ thành công!', 'bi-check-circle-fill')
    }

    closeAddressModal()

    // Reload addresses
    await loadAddresses()

    // Reload page after 1 second to reflect changes
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error) {
    console.error('Error saving address:', error)
    let errorMessage = 'Có lỗi xảy ra khi lưu địa chỉ'

    if (error.response?.status === 400 && error.response?.data) {
      const errors = error.response.data
      if (typeof errors === 'object' && errors.error) {
        errorMessage = errors.error
      } else if (typeof errors === 'string') {
        errorMessage = errors
      }
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    }

    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    savingAddress.value = false
  }
}

const confirmDeleteAddress = (address) => {
  if (address.macDinh) {
    showToast('error', 'Không thể xóa địa chỉ mặc định', 'bi-exclamation-circle-fill')
    return
  }
  addressToDelete.value = address
  showDeleteConfirm.value = true
}

const handleDeleteAddress = async () => {
  if (!addressToDelete.value?.id) {
    showToast('error', 'Không tìm thấy địa chỉ cần xóa', 'bi-exclamation-circle-fill')
    return
  }

  deletingAddress.value = true

  try {
    await api.delete(`/api/user-dia-chi/${addressToDelete.value.id}`)

    // If the deleted address has a DiaChi, we might want to delete it too
    // But for now, we'll just delete UserDiaChi to avoid breaking other references

    showToast('success', 'Xóa địa chỉ thành công!', 'bi-check-circle-fill')
    showDeleteConfirm.value = false
    addressToDelete.value = null

    // Reload addresses
    await loadAddresses()

    // Reload page after 1 second to reflect changes
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error) {
    console.error('Error deleting address:', error)
    showToast('error', 'Có lỗi xảy ra khi xóa địa chỉ', 'bi-exclamation-circle-fill')
  } finally {
    deletingAddress.value = false
  }
}

const setDefaultAddress = async (address) => {
  if (!address.id) {
    showToast('error', 'Địa chỉ chưa được lưu', 'bi-exclamation-circle-fill')
    return
  }

  try {
    await api.put(`/api/user-dia-chi/${address.id}/mac-dinh`)
    showToast('success', 'Đã đặt địa chỉ làm mặc định!', 'bi-check-circle-fill')

    // Reload addresses
    await loadAddresses()

    // Reload page after 1 second to reflect changes
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error) {
    console.error('Error setting default address:', error)
    showToast('error', 'Có lỗi xảy ra khi đặt địa chỉ mặc định', 'bi-exclamation-circle-fill')
  }
}

const openEditModal = () => {
  if (customerUser.value) {
    editForm.value = {
      hoTen: customerUser.value.hoTen || '',
      soDienThoai: customerUser.value.soDienThoai || '',
      email: customerUser.value.email || ''
    }
    showEditPersonalInfo.value = true
  }
}

const closeEditModal = () => {
  showEditPersonalInfo.value = false
  editForm.value = {
    hoTen: '',
    soDienThoai: '',
    email: ''
  }
}

const handleUpdatePersonalInfo = async () => {
  if (!customerUser.value?.id) {
    showToast('error', 'Không tìm thấy thông tin khách hàng', 'bi-exclamation-circle-fill')
    return
  }

  updatingPersonalInfo.value = true

  try {
    const updateData = {
      hoTen: editForm.value.hoTen.trim(),
      soDienThoai: editForm.value.soDienThoai.trim() || null,
      email: editForm.value.email.trim(),
      trangThai: customerUser.value.trangThai !== undefined ? customerUser.value.trangThai : 1
    }

    const response = await api.put(`/api/khach-hang/${customerUser.value.id}`, updateData)

    // Update local storage
    const updatedUser = {
      ...customerUser.value,
      hoTen: updateData.hoTen,
      soDienThoai: updateData.soDienThoai,
      email: updateData.email
    }
    customerUser.value = updatedUser
    localStorage.setItem('customerUser', JSON.stringify(updatedUser))

    showToast('success', 'Cập nhật thông tin thành công!', 'bi-check-circle-fill')
    closeEditModal()

    // Reload page after 1 second to reflect changes
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error) {
    console.error('Error updating personal info:', error)
    let errorMessage = 'Có lỗi xảy ra khi cập nhật thông tin'

    if (error.response?.status === 400 && error.response?.data) {
      const errors = error.response.data
      if (typeof errors === 'object') {
        if (errors.email) {
          errorMessage = errors.email
        } else if (errors.soDienThoai) {
          errorMessage = errors.soDienThoai
        } else if (errors.error) {
          errorMessage = errors.error
        } else {
          errorMessage = Object.values(errors)[0] || errorMessage
        }
      }
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    }

    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  } finally {
    updatingPersonalInfo.value = false
  }
}

const showToast = (type, message, icon) => {
  toast.value = {
    show: true,
    type,
    message,
    icon
  }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

const getStatusText = (trangThai) => {
  const statusMap = {
    0: 'Chờ xác nhận',
    1: 'Chờ giao hàng',
    2: 'Đang giao hàng',
    3: 'Hoàn thành',
    4: 'Đã hủy'
  }
  return statusMap[trangThai] || 'Không xác định'
}

const getStatusClass = (trangThai) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-shipping',
    3: 'status-success',
    4: 'status-cancelled'
  }
  return classMap[trangThai] || 'status-pending'
}

const getPaymentStatus = (trangThaiThanhToan) => {
  if (trangThaiThanhToan === true || trangThaiThanhToan === 1) {
    return 'Đã thanh toán'
  }
  return 'Thanh toán online thất bại'
}

const getPaymentClass = (trangThaiThanhToan) => {
  if (trangThaiThanhToan === true || trangThaiThanhToan === 1) {
    return 'payment-success'
  }
  return 'payment-failed'
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDateRange = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
}

const formatDeliveryTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hours = date.getHours()
  const minutes = date.getMinutes()
  const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy']
  const dayName = days[date.getDay()]

  return `${hours}h${minutes.toString().padStart(2, '0')} - ${hours + 2}h${minutes.toString().padStart(2, '0')} ${dayName} (${formatDateRange(date)})`
}

// Helper function to get full image URL
// Helper function to get placeholder image
const getPlaceholderImage = () => {
  return 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
}

const getImageUrl = (imagePath) => {
  if (!imagePath) return getPlaceholderImage()
  
  // If it's already a full URL, return as is
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // Get base URL from environment or use default
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  
  // If path already starts with /uploads/, use it directly with base URL
  if (imagePath.startsWith('/uploads/')) {
    return `${baseUrl}${imagePath}`
  }
  
  // If path doesn't start with /uploads/, add it
  return `${baseUrl}/uploads/${imagePath}`
}

// Helper functions for order display
const getOrderItems = (order) => {
  // HoaDonDTO có chiTietHoaDonList, nhưng có thể có các tên khác
  const items = order.chiTietHoaDonList || order.chiTietHoaDon || order.chiTietDonHang || []
  
  // Debug: Log để kiểm tra
  if (items.length === 0 && (order.chiTietHoaDonList || order.chiTietHoaDon || order.chiTietDonHang)) {
    console.warn('⚠️ Order has items but array is empty:', order.id, {
      chiTietHoaDonList: order.chiTietHoaDonList,
      chiTietHoaDon: order.chiTietHoaDon,
      chiTietDonHang: order.chiTietDonHang
    })
  }
  
  return items
}

const getFirstProductImage = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) {
    console.warn('⚠️ [getFirstProductImage] No items in order:', order.id, order.maHoaDon)
    return null
  }
  
  const firstItem = items[0]
  
  // Thử nhiều cách để lấy ảnh (camelCase và snake_case)
  const imagePath = firstItem.hinhAnh || 
                    firstItem.hinh_anh || 
                    firstItem.image || 
                    firstItem.urlAnh ||
                    firstItem.url_anh ||
                    firstItem.chiTietSanPham?.hinhAnh ||
                    firstItem.chiTietSanPham?.hinh_anh ||
                    firstItem.chiTietSanPham?.sanPham?.hinhAnh ||
                    null
  
  if (!imagePath) {
    console.warn('⚠️ [getFirstProductImage] No image found. Order ID:', order.id, 'Item keys:', Object.keys(firstItem))
    return getPlaceholderImage()
  }
  
  // Convert relative path to full URL
  return getImageUrl(imagePath)
}

const getFirstProductName = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) {
    console.warn('⚠️ [getFirstProductName] No items in order:', order.id, order.maHoaDon)
    return 'Không có sản phẩm'
  }
  
  const firstItem = items[0]
  
  // Thử nhiều cách để lấy tên sản phẩm (camelCase và snake_case)
  const name = firstItem.tenSanPham || 
               firstItem.ten_san_pham || 
               firstItem.productName ||
               firstItem.tenSanPham ||
               firstItem.chiTietSanPham?.tenSanPham ||
               firstItem.chiTietSanPham?.sanPham?.tenSanPham ||
               'Không có sản phẩm'
  
  if (name === 'Không có sản phẩm') {
    console.warn('⚠️ [getFirstProductName] No product name found. Order ID:', order.id, 'Item keys:', Object.keys(firstItem))
  }
  
  return name
}

const getOtherProductsCount = (order) => {
  const items = getOrderItems(order)
  return Math.max(0, items.length - 1)
}

const getFirstProductVariants = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) return []
  
  const firstItem = items[0]
  const variants = []
  
  // Lấy RAM (camelCase và snake_case)
  if (firstItem.tenRam) {
    variants.push(firstItem.tenRam)
  } else if (firstItem.ten_ram) {
    variants.push(firstItem.ten_ram)
  }
  
  // Lấy ROM (camelCase và snake_case)
  if (firstItem.tenRom) {
    variants.push(firstItem.tenRom)
  } else if (firstItem.ten_rom) {
    variants.push(firstItem.ten_rom)
  } else if (firstItem.dungLuong) {
    variants.push(firstItem.dungLuong)
  }
  
  // Lấy màu sắc (camelCase và snake_case)
  if (firstItem.tenMauSac) {
    variants.push(firstItem.tenMauSac)
  } else if (firstItem.ten_mau_sac) {
    variants.push(firstItem.ten_mau_sac)
  } else if (firstItem.tenMau) {
    variants.push(firstItem.tenMau)
  }
  
  return variants
}

const formatOrderDate = (order) => {
  const date = order.ngayTao || order.ngayDat || order.ngayThanhToan
  if (!date) return ''
  return formatDateRange(date)
}

const handleLogout = () => {
  localStorage.removeItem('customerToken')
  localStorage.removeItem('customerUser')
  router.push('/')
  window.location.reload()
}

// Watch for tab changes
watch(currentTab, (newTab) => {
  if (newTab === 'info' && customerUser.value?.id) {
    loadAddresses()
  }
})

// Lifecycle
onMounted(() => {
  loadCustomerFromStorage()
  if (customerUser.value?.id) {
    loadOrders()
    // Load addresses if on info tab
    if (route.path.includes('/thong-tin')) {
      currentTab.value = 'info'
      loadAddresses()
  } else if (route.path.includes('/yeu-thich')) {
    currentTab.value = 'favorite'
    }
  }

  // Set current tab based on route
  if (route.path.includes('/thong-tin')) {
    currentTab.value = 'info'
  } else if (route.path.includes('/yeu-thich')) {
    currentTab.value = 'favorite'
  } else {
    currentTab.value = 'orders'
  }
})
</script>

<style scoped>
.customer-profile-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 77px;
}

.profile-main {
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

.profile-layout {
  margin-top: 25px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
}

/* Sidebar */
.profile-sidebar {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  height: fit-content;
  position: sticky;
  top: 100px;
}

.customer-name-section {
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 1.5rem;
}

.customer-name-section h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}

.profile-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  color: #495057;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.nav-item:hover {
  background: #f8f9fa;
  color: #ff5500;
}

.nav-item.active {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
}

.nav-item i {
  font-size: 1.1rem;
}

.btn-logout {
  width: 100%;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.btn-logout:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

/* Content Area */
.profile-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.tab-header h1 {
  margin: 0;
  font-size: 1.75rem;
  color: #333;
}

.status-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.status-tab {
  padding: 0.625rem 1.25rem;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  color: #495057;
}

.status-tab:hover {
  border-color: #ff5500;
  color: #ff5500;
}

.status-tab.active {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border-color: transparent;
}

/* Orders List */
.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-state .spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state i {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin: 0 0 0.5rem;
  color: #495057;
}

.empty-state p {
  color: #6c757d;
  margin: 0;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  color: #666;
  font-size: 0.9rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  background: white;
  color: #333;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pagination-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #FF5500;
  color: #FF5500;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-pages {
  display: flex;
  gap: 0.25rem;
}

.pagination-page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 0.5rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  background: white;
  color: #333;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-page-btn:hover {
  background: #f8f9fa;
  border-color: #FF5500;
  color: #FF5500;
}

.pagination-page-btn.active {
  background: #FF5500;
  border-color: #FF5500;
  color: white;
  font-weight: 600;
}

.order-card {
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
  background: white;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 0.5rem;
}

.order-product-section {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex: 1;
}

.product-image-wrapper {
  position: relative;
  flex-shrink: 0;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  background: #f8f9fa;
  display: block;
}

.order-body {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-name {
  margin: 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
  line-height: 1.4;
}

.product-variants {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.5rem;
}

.variant-tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #495057;
  font-weight: 500;
}

.other-products {
  margin: 0;
  font-size: 0.875rem;
  color: #666;
}

.order-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
  min-width: 200px;
}

.order-id {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
  flex-shrink: 0;
}

.order-date {
  font-size: 0.9rem;
  color: #666;
  white-space: nowrap;
}

.order-status-badge {
  display: inline-block;
  padding: 0.375rem 0.875rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}

.order-status-badge.status-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffc107;
}

.order-status-badge.status-confirmed {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #17a2b8;
}

.order-status-badge.status-shipping {
  background: #cce5ff;
  color: #004085;
  border: 1px solid #0066cc;
}

.order-status-badge.status-success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #28a745;
}

.order-status-badge.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #dc3545;
}

.order-price {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
}

.btn-view-detail {
  padding: 0.625rem 1.5rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-block;
  font-size: 0.9rem;
  white-space: nowrap;
}

.btn-view-detail:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

/* Info Tab Styles */
.info-tab {
  padding: 0;
}

.info-tab h1 {
  margin: 0 0 2rem;
  font-size: 1.75rem;
  color: #333;
  font-weight: 600;
}

.info-section {
  margin-bottom: 2.5rem;
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 1rem;
}

.info-item-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.25rem;
  margin-bottom: 1rem;
}

.info-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-text {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #495057;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-edit:hover {
  background: #f8f9fa;
  border-color: #ff5500;
  color: #ff5500;
}

.btn-edit i {
  font-size: 1rem;
}

.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.address-item-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.25rem;
}

.address-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.address-text {
  flex: 1;
  font-size: 1rem;
  color: #333;
  line-height: 1.5;
}

.default-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-right: 0.75rem;
}

.old-address-label {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f1f3f5;
  font-size: 0.9rem;
  color: #6c757d;
}

.no-address {
  padding: 2rem;
  text-align: center;
  color: #6c757d;
}

.btn-add-address {
  width: 100%;
  padding: 0.875rem 1.5rem;
  background: transparent;
  border: 2px dashed #4285f4;
  border-radius: 8px;
  color: #4285f4;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.btn-add-address:hover:not(:disabled) {
  background: #f0f7ff;
  border-color: #1a73e8;
  color: #1a73e8;
}

.btn-add-address:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-add-address i {
  font-size: 1.25rem;
}

.max-address-warning {
  margin-top: 0.75rem;
  padding: 0.75rem 1rem;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  color: #856404;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.max-address-warning i {
  font-size: 1.1rem;
}

.address-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.btn-delete {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #dc3545;
  border-radius: 6px;
  color: #dc3545;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-delete:hover {
  background: #dc3545;
  color: white;
}

.btn-delete i {
  font-size: 1rem;
}

.btn-set-default {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #ff5500;
  border-radius: 6px;
  color: #ff5500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-set-default:hover {
  background: #ff5500;
  color: white;
}

.btn-set-default i {
  font-size: 1rem;
}

.checkbox-input {
  margin-right: 0.5rem;
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.address-modal {
  max-width: 600px;
}

.delete-modal {
  max-width: 400px;
}

.delete-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-delete-confirm {
  padding: 0.75rem 1.5rem;
  background: #dc3545;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-delete-confirm:hover:not(:disabled) {
  background: #c82333;
  transform: translateY(-1px);
}

.btn-delete-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 3rem 2rem;
}

.loading-state .spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

/* Modal Styles */
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

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}

.modal-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: #f8f9fa;
  color: #333;
}

.modal-body {
  padding: 1.5rem;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
}

.required {
  color: #dc3545;
}

.form-input {
  padding: 0.75rem 1rem;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #ff5500;
  box-shadow: 0 0 0 3px rgba(255, 85, 0, 0.1);
}

.form-input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
}

.form-hint {
  font-size: 0.85rem;
  color: #6c757d;
  margin-top: 0.25rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.btn-cancel {
  padding: 0.75rem 1.5rem;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #495057;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #e9ecef;
}

.btn-save {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Toast Notification */
.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 9999;
  max-width: 400px;
  animation: slideIn 0.3s ease;
}

.toast-notification.success {
  background: #28a745;
  color: white;
}

.toast-notification.error {
  background: #dc3545;
  color: white;
}

.toast-notification i {
  font-size: 1.25rem;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

/* Responsive */
@media (max-width: 992px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .tab-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .status-tabs {
    overflow-x: auto;
  }

  .order-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .order-product-section {
    width: 100%;
  }

  .order-details {
    width: 100%;
    align-items: flex-start;
  }

  .btn-view-detail {
    width: 100%;
    text-align: center;
  }
}
</style>

