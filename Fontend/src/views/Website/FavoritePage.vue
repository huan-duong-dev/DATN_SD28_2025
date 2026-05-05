<template>
  <div class="favorite-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Favorite Content -->
    <main class="favorite-main">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">Sản phẩm yêu thích</h1>
          <p class="page-subtitle" v-if="favoriteProducts.length > 0">{{ favoriteProducts.length }} sản phẩm yêu thích</p>
        </div>
        <div v-if="favoriteProducts.length === 0" class="empty-favorite">
          <i class="bi bi-heart"></i>
          <h2>Chưa có sản phẩm yêu thích</h2>
          <p>Bạn chưa thêm sản phẩm nào vào danh sách yêu thích</p>
          <router-link to="/shop" class="btn-shop">
            <i class="bi bi-shop"></i>
            <p> Tiếp tục mua sắm</p>
          </router-link>
        </div>

        <div v-else class="favorite-layout">
          <!-- Favorite Items -->
          <div class="favorite-items">
            <div class="favorite-header">
              <h2 class="section-title">Sản phẩm yêu thích ({{ favoriteProducts.length }})</h2>
            </div>

            <div v-if="loading" class="loading-state">
              <div class="spinner"></div>
              <p>Đang tải sản phẩm...</p>
            </div>

            <div v-else class="products-grid">
              <div v-for="product in loadedProducts" :key="product.id" class="favorite-item">
                <div class="item-image">
                  <img :src="getProductImage(product.hinhAnh || product.anh)" :alt="product.tenSanPham || product.ten" @error="handleImageError">
                  <button @click="removeFavorite(product.id)" class="btn-remove-favorite" title="Bỏ yêu thích">
                    <i class="bi bi-heart-fill"></i>
                  </button>
                </div>

                <div class="item-info">
                  <h3 class="item-name">{{ product.tenSanPham || product.ten }}</h3>
                  <div class="item-specs" v-if="product.tenRam || product.tenRom || product.tenMauSac">
                    <span v-if="product.tenRam">{{ product.tenRam }}</span>
                    <span v-if="product.tenRom">{{ product.tenRom }}</span>
                    <span v-if="product.tenMauSac">{{ product.tenMauSac }}</span>
                  </div>
                  <p class="item-price">{{ formatPrice(product.gia || product.donGia || 0) }}</p>
                  <div class="item-actions">
                    <button @click="viewProduct(product.id)" class="btn-view-detail">
                      <i class="bi bi-eye"></i>
                      Xem chi tiết
                    </button>
                    <button @click="addToCart(product)" class="btn-add-cart" :disabled="!product.chiTietSanPhamId">
                      <i class="bi bi-cart-plus"></i>
                      Thêm vào giỏ
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="favorite-footer">
      <FooterLayout/>
    </footer>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useCartStore } from '@/stores/cartStore.js'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'

const router = useRouter()
const cartStore = useCartStore()

const API_BASE_URL = 'http://localhost:8080'

// State
const favoriteProducts = ref([]) // Array of product IDs
const loadedProducts = ref([]) // Array of full product objects
const loading = ref(false)

const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: 'bi-check-circle-fill'
})

// Load favorite products from localStorage
const loadFavoriteProducts = () => {
  try {
    const stored = localStorage.getItem('favoriteProducts')
    if (stored) {
      favoriteProducts.value = JSON.parse(stored)
    } else {
      favoriteProducts.value = []
    }
  } catch (error) {
    console.error('Error loading favorite products:', error)
    favoriteProducts.value = []
  }
}

// Save favorite products to localStorage
const saveFavoriteProducts = () => {
  try {
    localStorage.setItem('favoriteProducts', JSON.stringify(favoriteProducts.value))
  } catch (error) {
    console.error('Error saving favorite products:', error)
  }
}

// Load product details from API
const loadProductDetails = async () => {
  if (favoriteProducts.value.length === 0) {
    loadedProducts.value = []
    return
  }

  loading.value = true
  try {
    // Load all products from san-pham-pos API
    const { data } = await axios.get(`${API_BASE_URL}/api/san-pham-pos`)
    
    if (data && Array.isArray(data)) {
      // Filter products that are in favorite list
      loadedProducts.value = data.filter((product) => {
        const productId = product.id || product.sanPhamId || product.chiTietSanPhamId
        return favoriteProducts.value.includes(productId)
      }).map((product) => ({
        id: product.id || product.sanPhamId || product.chiTietSanPhamId,
        sanPhamId: product.sanPhamId || product.id,
        chiTietSanPhamId: product.chiTietSanPhamId || product.id,
        tenSanPham: product.tenSanPham || product.ten,
        ten: product.ten || product.tenSanPham,
        gia: product.gia || product.donGia || product.giaSauGiam || 0,
        donGia: product.donGia || product.gia || product.giaSauGiam || 0,
        hinhAnh: product.hinhAnh || product.anh || product.imageUrls?.[0],
        anh: product.anh || product.hinhAnh || product.imageUrls?.[0],
        tenRam: product.tenRam,
        tenRom: product.tenRom,
        tenMauSac: product.tenMauSac,
      }))
    } else {
      loadedProducts.value = []
    }
  } catch (error) {
    console.error('Error loading product details:', error)
    showToast('error', 'Không thể tải thông tin sản phẩm', 'bi-exclamation-circle-fill')
    loadedProducts.value = []
  } finally {
    loading.value = false
  }
}

// Remove from favorites
const removeFavorite = (productId) => {
  favoriteProducts.value = favoriteProducts.value.filter(id => id !== productId)
  saveFavoriteProducts()
  loadProductDetails() // Reload to update UI
  showToast('success', 'Đã xóa khỏi danh sách yêu thích', 'bi-check-circle-fill')
}

// View product detail
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// Add to cart
const addToCart = (product) => {
  if (!product.chiTietSanPhamId) {
    showToast('error', 'Sản phẩm này không thể thêm vào giỏ hàng', 'bi-exclamation-circle-fill')
    return
  }

  try {
    cartStore.addItem({
      chiTietSanPhamId: product.chiTietSanPhamId,
      tenSanPham: product.tenSanPham || product.ten,
      gia: product.gia || product.donGia || 0,
      hinhAnh: product.hinhAnh || product.anh,
      tenRam: product.tenRam,
      tenRom: product.tenRom,
      tenMauSac: product.tenMauSac,
      quantity: 1
    })
    showToast('success', 'Đã thêm vào giỏ hàng', 'bi-check-circle-fill')
  } catch (error) {
    showToast('error', error.message || 'Không thể thêm vào giỏ hàng', 'bi-exclamation-circle-fill')
  }
}

// Format price
const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Get product image
const getProductImage = (imagePath) => {
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

  if (!imagePath) return placeholderSVG
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const handleImageError = (event) => {
  event.target.src = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
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

// Watch for changes in localStorage (from other tabs/pages)
const handleStorageChange = (e) => {
  if (e.key === 'favoriteProducts') {
    loadFavoriteProducts()
    loadProductDetails()
  }
}

// Lifecycle
onMounted(() => {
  loadFavoriteProducts()
  loadProductDetails()
  window.addEventListener('storage', handleStorageChange)
})

// Watch favoriteProducts for changes
watch(favoriteProducts, () => {
  loadProductDetails()
}, { deep: true })
</script>

<style scoped>
.favorite-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.favorite-main {
  flex: 1;
  padding: 2rem 0 3rem 0;
  margin-top: 77px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.page-header {
  margin-bottom: 2.5rem;
  padding-top: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e9ecef;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-title::before {
  content: '❤️';
  font-size: 2rem;
  -webkit-text-fill-color: initial;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
  margin: 0;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-subtitle::before {
  content: '📦';
  font-size: 1.2rem;
}

.empty-favorite {
  text-align: center;
  padding: 5rem 2rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  max-width: 600px;
  margin: 0 auto;
}

.empty-favorite i {
  font-size: 6rem;
  color: #dee2e6;
  margin-bottom: 1.5rem;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.empty-favorite h2 {
  font-size: 2rem;
  color: #2c3e50;
  margin: 0 0 0.75rem 0;
  font-weight: 700;
}

.empty-favorite p {
  color: #6c757d;
  margin: 0 0 2.5rem 0;
  font-size: 1.1rem;
}

.btn-shop {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.125rem 2.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
  border: none;
  cursor: pointer;
}

.btn-shop:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 85, 0, 0.4);
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
}

.favorite-layout {
  display: flex;
  gap: 2rem;
}

.favorite-items {
  flex: 1;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  padding: 2.5rem;
}

.favorite-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e9ecef;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 2rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #6c757d;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e9ecef;
  border-top-color: #FF5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.favorite-item {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.favorite-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: #FF5500;
}

.item-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%);
}

.item-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 1.5rem;
  transition: transform 0.3s ease;
}

.favorite-item:hover .item-image img {
  transform: scale(1.08);
}

.btn-remove-favorite {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  color: #FF5500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
  backdrop-filter: blur(10px);
}

.btn-remove-favorite:hover {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  transform: scale(1.15) rotate(10deg);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.item-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.75rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 3rem;
}

.item-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.item-specs span {
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #495057;
  border: 1px solid #dee2e6;
}

.item-price {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 1.25rem 0;
}

.item-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: auto;
}

.btn-view-detail,
.btn-add-cart {
  flex: 1;
  padding: 0.875rem 1rem;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-view-detail {
  background: #f8f9fa;
  color: #2c3e50;
  border: 2px solid #e9ecef;
}

.btn-view-detail:hover {
  background: #e9ecef;
  border-color: #dee2e6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-add-cart {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-add-cart:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
}

.btn-add-cart:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.favorite-footer {
  margin-top: auto;
}

/* Toast Notification */
.toast-notification {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 10000;
  animation: slideInRight 0.3s ease;
}

.toast-notification.success {
  border-left: 4px solid #28a745;
}

.toast-notification.error {
  border-left: 4px solid #dc3545;
}

.toast-notification i {
  font-size: 1.25rem;
}

.toast-notification.success i {
  color: #28a745;
}

.toast-notification.error i {
  color: #dc3545;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .favorite-items {
    padding: 1.5rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1.25rem;
  }

  .item-actions {
    flex-direction: column;
  }

  .btn-view-detail,
  .btn-add-cart {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 1.75rem;
  }

  .empty-favorite {
    padding: 3rem 1.5rem;
  }

  .empty-favorite i {
    font-size: 4rem;
  }

  .empty-favorite h2 {
    font-size: 1.5rem;
  }
}
</style>

