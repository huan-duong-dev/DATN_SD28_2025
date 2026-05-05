<template>
  <div class="voucher-page">
    <!-- Header Layout -->
    <HeaderLayout />

    <!-- Voucher Section -->
    <section class="voucher-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Danh Sách Voucher</h2>
          <p class="section-description">
            <span v-if="!isCustomerLoggedIn">Đăng nhập để xem voucher cá nhân của bạn</span>
            <span v-else>Voucher công khai và voucher cá nhân của bạn</span>
          </p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Đang tải voucher...</p>
        </div>

        <!-- Voucher Grid -->
        <div v-else class="voucher-grid">
          <!-- Public Vouchers -->
          <div v-if="publicVouchers.length > 0" class="voucher-category">
            <h3 class="category-title">
              <i class="bi bi-gift"></i>
              Voucher Công Khai
            </h3>
            <div class="voucher-cards">
              <div 
                v-for="voucher in publicVouchers" 
                :key="voucher.id" 
                class="voucher-card"
                :class="{ 'expired': isExpired(voucher), 'used': isUsed(voucher) }"
              >
                <div class="voucher-header">
                  <div class="voucher-discount">
                    <span class="discount-value">{{ formatDiscount(voucher) }}</span>
                    <span class="discount-type">{{ voucher.loaiPhieuGiamGia === 'PHAN_TRAM' ? '%' : 'đ' }}</span>
                  </div>
                  <div class="voucher-badge" v-if="isExpired(voucher)">Hết hạn</div>
                  <div class="voucher-badge used" v-else-if="isUsed(voucher)">Đã dùng</div>
                </div>
                <div class="voucher-body">
                  <h4 class="voucher-name">{{ voucher.tenPhieuGiamGia }}</h4>
                  <p class="voucher-code">Mã: <strong>{{ voucher.maPhieuGiamGia }}</strong></p>
                  <div class="voucher-details">
                    <p class="voucher-condition" v-if="voucher.dieuKienApDung">
                      <i class="bi bi-info-circle"></i>
                      {{ voucher.dieuKienApDung }}
                    </p>
                    <p class="voucher-date">
                      <i class="bi bi-calendar"></i>
                      {{ formatDate(voucher.ngayBatDau) }} - {{ formatDate(voucher.ngayKetThuc) }}
                    </p>
                  </div>
                </div>
                <div class="voucher-footer">
                  <button 
                    class="btn-copy-code" 
                    @click="copyCode(voucher.maPhieuGiamGia)"
                    :disabled="isExpired(voucher) || isUsed(voucher)"
                  >
                    <i class="bi bi-clipboard"></i>
                    Sao chép mã
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Personal Vouchers (only if logged in) -->
          <div v-if="isCustomerLoggedIn && personalVouchers.length > 0" class="voucher-category">
            <h3 class="category-title">
              <i class="bi bi-person-badge"></i>
              Voucher Cá Nhân Của Tôi
            </h3>
            <div class="voucher-cards">
              <div 
                v-for="voucher in personalVouchers" 
                :key="voucher.id" 
                class="voucher-card personal"
                :class="{ 'expired': isExpired(voucher), 'used': isUsed(voucher) }"
              >
                <div class="voucher-header">
                  <div class="voucher-discount">
                    <span class="discount-value">{{ formatDiscount(voucher) }}</span>
                    <span class="discount-type">{{ voucher.loaiPhieuGiamGia === 'PHAN_TRAM' ? '%' : 'đ' }}</span>
                  </div>
                  <div class="voucher-badge personal-badge">Cá nhân</div>
                  <div class="voucher-badge expired" v-if="isExpired(voucher)">Hết hạn</div>
                  <div class="voucher-badge used" v-else-if="isUsed(voucher)">Đã dùng</div>
                </div>
                <div class="voucher-body">
                  <h4 class="voucher-name">{{ voucher.tenPhieuGiamGia }}</h4>
                  <p class="voucher-code">Mã: <strong>{{ voucher.maPhieuGiamGia }}</strong></p>
                  <div class="voucher-details">
                    <p class="voucher-condition" v-if="voucher.dieuKienApDung">
                      <i class="bi bi-info-circle"></i>
                      {{ voucher.dieuKienApDung }}
                    </p>
                    <p class="voucher-date">
                      <i class="bi bi-calendar"></i>
                      {{ formatDate(voucher.ngayBatDau) }} - {{ formatDate(voucher.ngayKetThuc) }}
                    </p>
                  </div>
                </div>
                <div class="voucher-footer">
                  <button 
                    class="btn-copy-code" 
                    @click="copyCode(voucher.maPhieuGiamGia)"
                    :disabled="isExpired(voucher) || isUsed(voucher)"
                  >
                    <i class="bi bi-clipboard"></i>
                    Sao chép mã
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="!loading && publicVouchers.length === 0 && personalVouchers.length === 0" class="empty-state">
            <i class="bi bi-inbox"></i>
            <h3>Chưa có voucher nào</h3>
            <p>Hiện tại chưa có voucher khả dụng. Vui lòng quay lại sau!</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer Layout -->
    <FooterLayout />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

// State
const loading = ref(false)
const publicVouchers = ref([])
const personalVouchers = ref([])
const customerUser = ref(null)
const customerToken = ref(null)

// Computed
const isCustomerLoggedIn = computed(() => {
  return !!customerToken.value && !!customerUser.value
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
    }
  }
}

const loadVouchers = async () => {
  loading.value = true
  try {
    // Load public vouchers (always)
    const publicResponse = await axios.get(`${API_BASE_URL}/api/phieu-giam-gia`)
    const allVouchers = Array.isArray(publicResponse.data) ? publicResponse.data : []
    
    // Filter public vouchers (not private)
    publicVouchers.value = allVouchers.filter(v => 
      v.trangThai === 1 && 
      (!v.riengTu || v.riengTu === false) &&
      !isExpired(v)
    )

    // Load personal vouchers if logged in
    if (isCustomerLoggedIn.value && customerUser.value?.id) {
      try {
        const personalResponse = await axios.get(
          `${API_BASE_URL}/api/phieu-giam-gia/customer/${customerUser.value.id}`,
          {
            headers: {
              Authorization: `Bearer ${customerToken.value}`
            }
          }
        )
        const personalData = Array.isArray(personalResponse.data) ? personalResponse.data : []
        
        // Normalize data (handle nested structure)
        personalVouchers.value = personalData.map(v => {
          // If voucher is nested in relation object
          if (v.phieuGiamGia) {
            return {
              ...v.phieuGiamGia,
              daSuDung: v.daSuDung,
              ngaySuDung: v.ngaySuDung
            }
          }
          return v
        }).filter(v => v.trangThai === 1 && !isExpired(v))
      } catch (error) {
        console.error('Error loading personal vouchers:', error)
        personalVouchers.value = []
      }
    } else {
      personalVouchers.value = []
    }
  } catch (error) {
    console.error('Error loading vouchers:', error)
    publicVouchers.value = []
    personalVouchers.value = []
  } finally {
    loading.value = false
  }
}

const isExpired = (voucher) => {
  if (!voucher.ngayKetThuc) return false
  const endDate = new Date(voucher.ngayKetThuc)
  return endDate < new Date()
}

const isUsed = (voucher) => {
  return voucher.daSuDung === true || voucher.daSuDung === 1
}

const formatDiscount = (voucher) => {
  if (voucher.loaiPhieuGiamGia === 'PHAN_TRAM') {
    return voucher.giaTriGiamGia || 0
  } else {
    return (voucher.giaTriGiamGia || 0).toLocaleString('vi-VN')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    alert(`Đã sao chép mã: ${code}`)
  } catch (error) {
    // Fallback for older browsers
    const textArea = document.createElement('textarea')
    textArea.value = code
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    alert(`Đã sao chép mã: ${code}`)
  }
}

// Lifecycle
onMounted(() => {
  loadCustomerFromStorage()
  loadVouchers()
})
</script>

<style scoped>
.voucher-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Section Header */
.section-header {
  text-align: center;
  margin-bottom: 3rem;
  padding-top: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  border-radius: 2px;
}

.section-description {
  font-size: 1.1rem;
  color: #666;
  margin-top: 1.5rem;
}

/* Voucher Section */
.voucher-section {
  background: #f8f9fa;
  padding: 4rem 0;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 0;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ee4d2d;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Voucher Category */
.voucher-category {
  margin-bottom: 3rem;
}

.category-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.category-title i {
  color: #ee4d2d;
  font-size: 1.75rem;
}

/* Voucher Grid */
.voucher-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

/* Voucher Card */
.voucher-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
}

.voucher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(238, 77, 45, 0.15);
  border-color: #ee4d2d;
}

.voucher-card.personal {
  border-color: #ff6b35;
  background: linear-gradient(135deg, #fff 0%, #fff5f0 100%);
}

.voucher-card.expired {
  opacity: 0.6;
  filter: grayscale(0.5);
}

.voucher-card.used {
  opacity: 0.7;
}

.voucher-header {
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  padding: 1.5rem;
  color: white;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.voucher-card.personal .voucher-header {
  background: linear-gradient(135deg, #ff6b35 0%, #ee4d2d 100%);
}

.voucher-card.expired .voucher-header {
  background: linear-gradient(135deg, #999 0%, #777 100%);
}

.voucher-discount {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
}

.discount-value {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1;
}

.discount-type {
  font-size: 1.5rem;
  font-weight: 600;
  opacity: 0.9;
}

.voucher-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.voucher-badge.personal-badge {
  background: rgba(255, 255, 255, 0.3);
}

.voucher-badge.expired,
.voucher-badge.used {
  background: rgba(0, 0, 0, 0.3);
}

.voucher-body {
  padding: 1.5rem;
}

.voucher-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
}

.voucher-code {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1rem;
}

.voucher-code strong {
  color: #ee4d2d;
  font-size: 1.1rem;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.voucher-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.voucher-condition,
.voucher-date {
  font-size: 0.875rem;
  color: #666;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.voucher-condition i,
.voucher-date i {
  color: #ee4d2d;
}

.voucher-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn-copy-code {
  width: 100%;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-copy-code:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.3);
}

.btn-copy-code:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #999;
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #666;
}

/* Responsive Design */
@media (max-width: 768px) {
  .section-title {
    font-size: 2rem;
  }

  .voucher-cards {
    grid-template-columns: 1fr;
  }

  .discount-value {
    font-size: 2.5rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .section-header {
    padding-top: 2rem;
  }
}
</style>


