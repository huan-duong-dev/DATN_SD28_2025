<template>
  <div class="compare-page">
    <!-- Header -->
    <HeaderLayout />

    <!-- Main Content -->
    <main class="compare-main">
      <div class="container">
        <!-- Page Title -->
        <div class="page-header">
          <h1 class="page-title" v-if="compareProducts.length > 0">
            So Sánh {{ compareProducts.map(p => p.tenSanPham).join(' và ') }}
          </h1>
          <h1 class="page-title" v-else>So sánh sản phẩm</h1>
        </div>

        <!-- Compare Products Grid -->
        <div class="compare-products-section">
          <div class="compare-grid">
            <!-- Product Columns -->
            <div
              v-for="(product, index) in compareProducts"
              :key="product.id || index"
              class="compare-product-column"
            >
              <div class="product-image-wrapper">
                <button
                  v-if="compareProducts.length > 1"
                  @click="removeProduct(index)"
                  class="btn-remove-compare"
                  title="Xóa sản phẩm"
                >
                  <i class="bi bi-x"></i>
                </button>
                <img
                  :src="getProductImage(product.hinhAnh)"
                  :alt="product.tenSanPham"
                  class="product-image"
                />
              </div>
              <h3 class="product-name">{{ product.tenSanPham }}</h3>
              
              <!-- Price Section -->
              <div class="price-section">
                <div class="price-item">
                  <span class="price-label">Giá:</span>
                  <span class="price-value">{{ formatPrice(product.gia || product.donGia || 0) }}</span>
                </div>
                <div v-if="product.giaGoc && product.giaGoc > (product.gia || product.donGia)" class="price-item">
                  <span class="price-label">Giá lên đời:</span>
                  <span class="price-value">{{ formatPrice(product.giaGoc) }}</span>
                </div>
              </div>

              <!-- Promotion Section -->
              <div v-if="product.giamPhanTram && product.giamPhanTram > 0" class="promotion-box">
                <p>Giảm thêm {{ product.giamPhanTram }}% cho các sản phẩm khác khi mua sản phẩm này</p>
              </div>

              <!-- Buy Button -->
              <button @click="buyNow(product)" class="btn-buy-now">
                Mua ngay
              </button>
            </div>

            <!-- Add Product Column -->
            <div
              v-if="compareProducts.length < 3"
              class="compare-product-column add-product-column"
              @click="addMoreProducts"
            >
              <div class="add-product-icon">
                <i class="bi bi-plus-circle"></i>
              </div>
              <p class="add-product-text">Thêm sản phẩm để so sánh</p>
            </div>
          </div>
        </div>

        <!-- Specifications Comparison Table -->
        <div v-if="compareProducts.length > 0" class="specs-comparison-section">
          <h2 class="specs-title">Thông tin cơ bản</h2>
          <div class="specs-table-wrapper">
            <table class="specs-table">
              <thead>
                <tr>
                  <th class="spec-label-col">Thông tin cơ bản</th>
                  <th
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value-col"
                  >
                    {{ product.tenSanPham }}
                  </th>
                  <th v-if="compareProducts.length < 3" class="spec-value-col empty-col"></th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="spec-label">Kích thước màn hình</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenManHinh || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Công nghệ màn hình</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenManHinh || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Camera sau</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenCameraSau || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Camera trước</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenCameraTruoc || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">CPU</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenCpu || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">RAM</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenRam || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Bộ nhớ trong</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenRom || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Pin</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenPin || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">Hệ điều hành</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenHeDieuHanh || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
                <tr>
                  <td class="spec-label">SIM</td>
                  <td
                    v-for="(product, index) in compareProducts"
                    :key="product.id || index"
                    class="spec-value"
                  >
                    {{ product.tenSim || 'Đang cập nhật' }}
                  </td>
                  <td v-if="compareProducts.length < 3" class="spec-value empty-cell">-</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="compareProducts.length === 0" class="empty-compare">
          <i class="bi bi-arrow-left-right"></i>
          <h2>Chưa có sản phẩm để so sánh</h2>
          <p>Hãy chọn ít nhất 2 sản phẩm để so sánh</p>
          <button @click="openAddProductModal" class="btn-shop">
            <i class="bi bi-plus-circle"></i>
            <span>Thêm sản phẩm để so sánh</span>
          </button>
        </div>
      </div>
    </main>

    <!-- Add Product Modal -->
    <div v-if="showAddProductModal" class="add-product-modal" @click.self="closeAddProductModal">
      <div class="add-product-modal-content" @click.stop>
        <div class="add-product-modal-header">
          <h2>Chọn sản phẩm để so sánh</h2>
          <button @click="closeAddProductModal" class="add-product-modal-close">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="add-product-modal-body">
          <div class="product-search-wrapper-modal">
            <div class="search-input-wrapper-modal">
              <i class="bi bi-search search-icon-input-modal"></i>
              <input
                v-model="productSearchQuery"
                @input="searchProducts"
                @focus="showProductDropdown = true"
                @click="showProductDropdown = true"
                type="text"
                placeholder="Tìm sản phẩm muốn so sánh"
                class="product-search-input-modal"
              />
              <button 
                @click="openFavoriteModal" 
                class="favorite-filter-btn-modal"
              >
                <i class="bi bi-heart"></i>
                <span>Yêu thích</span>
              </button>
            </div>
            <div v-if="showProductDropdown" class="product-dropdown-modal">
              <div v-if="isLoadingProducts" class="loading-products-modal">
                Đang tải sản phẩm...
              </div>
              <div v-else-if="filteredProducts.length === 0" class="no-products-modal">
                Không tìm thấy sản phẩm
              </div>
              <div v-else class="product-list-modal">
                <div
                  v-for="product in filteredProducts"
                  :key="product.id || product.chiTietSanPhamId"
                  class="product-item-modal"
                  @click="selectProductForCompare(product)"
                >
                  <img :src="getProductImage(product.hinhAnh)" :alt="product.tenSanPham" />
                  <div class="product-item-info-modal">
                    <h4>{{ product.tenSanPham }}</h4>
                    <div class="product-item-price-modal">
                      <span class="current-price-modal">{{ formatPrice(product.gia || product.donGia || 0) }}</span>
                    </div>
                  </div>
                  <button class="select-product-btn-modal">Chọn</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Favorite Products Modal -->
    <div v-if="showFavoriteModal" class="favorite-modal-compare" @click.self="closeFavoriteModal">
      <div class="favorite-modal-content-compare" @click.stop>
        <div class="favorite-modal-header-compare">
          <h2>Sản phẩm yêu thích</h2>
          <button @click="closeFavoriteModal" class="favorite-modal-close-compare">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="favorite-modal-body-compare">
          <div v-if="favoriteProductsList.length === 0" class="empty-favorites-modal-compare">
            <i class="bi bi-heart"></i>
            <p>Chưa có sản phẩm yêu thích</p>
          </div>
          <div v-else class="favorite-products-grid-compare">
            <div
              v-for="product in favoriteProductsList"
              :key="product.id || product.chiTietSanPhamId"
              class="favorite-product-card-compare"
            >
              <div class="favorite-product-image-compare">
                <img :src="getProductImage(product.hinhAnh)" :alt="product.tenSanPham" />
              </div>
              <div class="favorite-product-info-compare">
                <h4>{{ product.tenSanPham }}</h4>
                <div class="favorite-product-specs-compare" v-if="product.tenRam || product.tenRom || product.tenMauSac">
                  <span v-if="product.tenRam">{{ product.tenRam }}</span>
                  <span v-if="product.tenRom">{{ product.tenRom }}</span>
                  <span v-if="product.tenMauSac">{{ product.tenMauSac }}</span>
                </div>
                <div class="favorite-product-price-compare">{{ formatPrice(product.gia || product.donGia || 0) }}</div>
                <button @click="selectProductForCompare(product)" class="btn-select-favorite-compare">
                  Chọn
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <FooterLayout />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'

const route = useRoute()
const router = useRouter()

const compareProducts = ref([])
const loading = ref(false)

// Add product modal state
const showAddProductModal = ref(false)
const showProductDropdown = ref(false)
const productSearchQuery = ref('')
const allProducts = ref([])
const filteredProducts = ref([])
const isLoadingProducts = ref(false)

// Favorite modal state
const showFavoriteModal = ref(false)
const favoriteProducts = ref([]) // Array of product IDs
const favoriteProductsList = ref([]) // Array of full product objects

// Get product IDs from query params
const productIds = computed(() => {
  const ids = route.query.ids
  if (typeof ids === 'string') {
    return ids.split(',').map(id => parseInt(id)).filter(id => !isNaN(id))
  }
  return []
})

// Load product details
async function loadProducts() {
  if (productIds.value.length === 0) {
    compareProducts.value = []
    return
  }

  loading.value = true
  try {
    // Load POS products for pricing info
    let posProducts = []
    try {
      const posResponse = await api.get('/api/san-pham-pos')
      if (posResponse.data && Array.isArray(posResponse.data)) {
        posProducts = posResponse.data
      }
    } catch (error) {
      console.warn('Could not load POS products:', error)
    }

    const products = []
    for (const id of productIds.value) {
      try {
        const { data } = await api.get(`/api/san-pham/${id}/view`)
        if (data) {
          // Get variant info if available
          const variant = data.variants && data.variants.length > 0 ? data.variants[0] : null
          
          // Find matching POS product for pricing
          const posProduct = posProducts.find((p) => 
            (p.sanPhamId === data.id || p.id === data.id) &&
            (variant ? (p.chiTietSanPhamId === variant.id || 
                       (p.idRam === variant.idRam && p.idRom === variant.idRom && p.idMauSac === variant.idMauSac)) : true)
          ) || posProducts.find((p) => p.sanPhamId === data.id || p.id === data.id)

          products.push({
            id: data.id,
            tenSanPham: data.tenSanPham,
            hinhAnh: variant?.imageUrls?.[0] || posProduct?.hinhAnh || '',
            gia: posProduct?.giaSauGiam || variant?.giaSauGiam || variant?.donGia || 0,
            donGia: variant?.donGia || posProduct?.gia || 0,
            giaGoc: posProduct?.giaGoc || variant?.giaGoc || null,
            giaSauGiam: posProduct?.giaSauGiam || variant?.giaSauGiam || null,
            giamPhanTram: posProduct?.giamPhanTram || variant?.giamPhanTram || 0,
            tenManHinh: data.tenManHinh,
            tenCameraSau: data.tenCameraSau,
            tenCameraTruoc: data.tenCameraTruoc,
            tenCpu: data.tenCpu,
            tenRam: variant?.tenRam || posProduct?.tenRam || '',
            tenRom: variant?.tenRom || posProduct?.tenRom || '',
            tenPin: data.tenPin,
            tenHeDieuHanh: data.tenHeDieuHanh,
            tenSim: data.tenSim,
            chiTietSanPhamId: variant?.id || posProduct?.chiTietSanPhamId || null
          })
        }
      } catch (error) {
        console.error(`Error loading product ${id}:`, error)
      }
    }
    compareProducts.value = products
  } catch (error) {
    console.error('Error loading products:', error)
  } finally {
    loading.value = false
  }
}

function removeProduct(index) {
  compareProducts.value.splice(index, 1)
  updateUrl()
}

function addMoreProducts() {
  openAddProductModal()
}

function openAddProductModal() {
  showAddProductModal.value = true
  showProductDropdown.value = true
  loadAllProducts()
}

function closeAddProductModal() {
  showAddProductModal.value = false
  showProductDropdown.value = false
  productSearchQuery.value = ''
}

async function loadAllProducts() {
  isLoadingProducts.value = true
  try {
    const { data } = await api.get('/api/san-pham-pos')
    allProducts.value = (data || []).map((p) => ({
      ...p,
      chiTietSanPhamId: p.chiTietSanPhamId || p.id,
      tenSanPham: p.tenSanPham || p.ten || '',
      gia: p.gia || p.giaBan || p.donGia || 0,
      hinhAnh: p.hinhAnh || p.hinhAnhUrl || '',
      tenRam: p.tenRam || '',
      tenRom: p.tenRom || '',
      tenMauSac: p.tenMauSac || '',
      sanPhamId: p.sanPhamId || p.id
    }))
    
    // Filter out already selected products
    const selectedIds = compareProducts.value.map(p => p.id)
    filteredProducts.value = allProducts.value.filter(p => 
      !selectedIds.includes(p.sanPhamId || p.id)
    )
  } catch (error) {
    console.error('Error loading products:', error)
    allProducts.value = []
    filteredProducts.value = []
  } finally {
    isLoadingProducts.value = false
  }
}

function searchProducts() {
  const query = productSearchQuery.value.toLowerCase().trim()
  
  const selectedIds = compareProducts.value.map(p => p.id)
  
  if (query) {
    filteredProducts.value = allProducts.value.filter(p => {
      const name = (p.tenSanPham || '').toLowerCase()
      return name.includes(query) && !selectedIds.includes(p.sanPhamId || p.id)
    })
  } else {
    filteredProducts.value = allProducts.value.filter(p => 
      !selectedIds.includes(p.sanPhamId || p.id)
    )
  }
}

function selectProductForCompare(product) {
  // Load full product details
  loadProductDetailForCompare(product.sanPhamId || product.id, product)
}

async function loadProductDetailForCompare(productId, posProduct) {
  try {
    const { data } = await api.get(`/api/san-pham/${productId}/view`)
    if (data) {
      const variant = data.variants && data.variants.length > 0 ? data.variants[0] : null
      
      const productData = {
        id: data.id,
        tenSanPham: data.tenSanPham,
        hinhAnh: variant?.imageUrls?.[0] || posProduct?.hinhAnh || '',
        gia: posProduct?.giaSauGiam || variant?.giaSauGiam || variant?.donGia || 0,
        donGia: variant?.donGia || posProduct?.gia || 0,
        giaGoc: posProduct?.giaGoc || variant?.giaGoc || null,
        giaSauGiam: posProduct?.giaSauGiam || variant?.giaSauGiam || null,
        giamPhanTram: posProduct?.giamPhanTram || variant?.giamPhanTram || 0,
        tenManHinh: data.tenManHinh,
        tenCameraSau: data.tenCameraSau,
        tenCameraTruoc: data.tenCameraTruoc,
        tenCpu: data.tenCpu,
        tenRam: variant?.tenRam || posProduct?.tenRam || '',
        tenRom: variant?.tenRom || posProduct?.tenRom || '',
        tenPin: data.tenPin,
        tenHeDieuHanh: data.tenHeDieuHanh,
        tenSim: data.tenSim,
        chiTietSanPhamId: variant?.id || posProduct?.chiTietSanPhamId || null
      }
      
      compareProducts.value.push(productData)
      updateUrl()
      closeAddProductModal()
      closeFavoriteModal()
    }
  } catch (error) {
    console.error('Error loading product detail:', error)
  }
}

function openFavoriteModal() {
  showFavoriteModal.value = true
  loadFavoriteProductsList()
}

function closeFavoriteModal() {
  showFavoriteModal.value = false
}

function loadFavoriteProducts() {
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

async function loadFavoriteProductsList() {
  const favoriteIds = favoriteProducts.value
  if (favoriteIds.length === 0) {
    favoriteProductsList.value = []
    return
  }
  
  try {
    const { data } = await api.get('/api/san-pham-pos')
    const selectedIds = compareProducts.value.map(p => p.id)
    favoriteProductsList.value = (data || [])
      .filter((p) => 
        favoriteIds.includes(p.sanPhamId || p.id) &&
        !selectedIds.includes(p.sanPhamId || p.id)
      )
      .map((p) => ({
        ...p,
        chiTietSanPhamId: p.chiTietSanPhamId || p.id,
        tenSanPham: p.tenSanPham || p.ten || '',
        gia: p.gia || p.giaBan || p.donGia || 0,
        hinhAnh: p.hinhAnh || p.hinhAnhUrl || '',
        tenRam: p.tenRam || '',
        tenRom: p.tenRom || '',
        tenMauSac: p.tenMauSac || '',
        sanPhamId: p.sanPhamId || p.id
      }))
  } catch (error) {
    console.error('Error loading favorite products:', error)
    favoriteProductsList.value = []
  }
}

function updateUrl() {
  const ids = compareProducts.value.map(p => p.id).join(',')
  router.replace({ query: { ids } })
}

function getProductImage(imagePath) {
  if (!imagePath) return '/placeholder.png'
  if (imagePath.startsWith('http')) return imagePath
  return `http://localhost:8080${imagePath}`
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function buyNow(product) {
  if (product.chiTietSanPhamId) {
    router.push({
      path: '/dat-hang',
      query: {
        productId: product.id,
        variantId: product.chiTietSanPhamId,
        quantity: 1,
        buyNow: 'true'
      }
    })
  } else {
    router.push(`/product/${product.id}`)
  }
}

onMounted(() => {
  loadProducts()
  loadFavoriteProducts()
})

// Watch for route changes
watch(() => route.query.ids, () => {
  loadProducts()
})
</script>

<style scoped>
.compare-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-top: 100px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.compare-products-section {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.compare-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
}

.compare-product-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.compare-product-column:hover {
  border-color: #FF5500;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.2);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  background: white;
  border-radius: 12px;
  padding: 1rem;
}

.btn-remove-compare {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s ease;
  z-index: 10;
}

.btn-remove-compare:hover {
  background: #dc3545;
  transform: scale(1.1);
}

.product-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 1rem 0;
  text-align: center;
  line-height: 1.4;
  min-height: 3rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-section {
  width: 100%;
  margin-bottom: 1rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.price-label {
  font-size: 0.9rem;
  color: #6c757d;
}

.price-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #dc3545;
}

.promotion-box {
  width: 100%;
  background: #e7f3ff;
  border: 1px solid #b3d9ff;
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 1rem;
  font-size: 0.85rem;
  color: #0066cc;
  line-height: 1.5;
}

.btn-buy-now {
  width: 100%;
  padding: 12px 24px;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-buy-now:hover {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.add-product-column {
  cursor: pointer;
  border-style: dashed;
  justify-content: center;
  min-height: 400px;
}

.add-product-column:hover {
  border-color: #FF5500;
  background: #fff5f5;
}

.add-product-icon {
  font-size: 4rem;
  color: #6c757d;
  margin-bottom: 1rem;
}

.add-product-text {
  font-size: 1rem;
  color: #6c757d;
  text-align: center;
  margin: 0;
}

.specs-comparison-section {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.specs-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 1.5rem 0;
}

.specs-table-wrapper {
  overflow-x: auto;
}

.specs-table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table thead {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.specs-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #dee2e6;
}

.spec-label-col {
  width: 200px;
  min-width: 200px;
}

.spec-value-col {
  min-width: 250px;
}

.empty-col {
  background: #f8f9fa;
}

.specs-table tbody tr {
  border-bottom: 1px solid #e9ecef;
  transition: background 0.2s ease;
}

.specs-table tbody tr:hover {
  background: #f8f9fa;
}

.specs-table td {
  padding: 1rem;
  vertical-align: top;
}

.spec-label {
  font-weight: 600;
  color: #2c3e50;
  background: #f8f9fa;
}

.spec-value {
  color: #495057;
}

.empty-cell {
  color: #adb5bd;
  font-style: italic;
}

.empty-compare {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.empty-compare i {
  font-size: 5rem;
  color: #dee2e6;
  margin-bottom: 1.5rem;
  display: block;
}

.empty-compare h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.empty-compare p {
  color: #6c757d;
  margin: 0 0 2rem 0;
}

.btn-shop {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-shop:hover {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

@media (max-width: 1024px) {
  .compare-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .compare-grid {
    grid-template-columns: 1fr;
  }
  
  .specs-table-wrapper {
    overflow-x: scroll;
  }
  
  .spec-label-col {
    width: 150px;
    min-width: 150px;
  }
  
  .spec-value-col {
    min-width: 200px;
  }
}

/* Add Product Modal */
.add-product-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.3s;
}

.add-product-modal-content {
  background: white;
  border-radius: 20px;
  max-width: 900px;
  width: 95%;
  max-height: 90vh;
  min-height: 700px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.add-product-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #e9ecef;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

.add-product-modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.add-product-modal-close {
  background: #f8f9fa;
  border: none;
  color: #6c757d;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s ease;
}

.add-product-modal-close:hover {
  background: #e9ecef;
  color: #2c3e50;
}

.add-product-modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
  position: relative;
  min-height: 550px;
}

.product-search-wrapper-modal {
  margin-bottom: 1rem;
  position: relative;
}

.search-input-wrapper-modal {
  display: flex;
  gap: 8px;
  align-items: center;
  position: relative;
}

.search-icon-input-modal {
  position: absolute;
  left: 12px;
  color: #6c757d;
  font-size: 1rem;
  pointer-events: none;
  z-index: 1;
}

.product-search-input-modal {
  flex: 1;
  padding: 10px 12px 10px 36px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.product-search-input-modal:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.favorite-filter-btn-modal {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 12px;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  color: #6c757d;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.favorite-filter-btn-modal:hover {
  border-color: #dc3545;
  color: #dc3545;
}

.favorite-filter-btn-modal i {
  font-size: 1rem;
}

.product-dropdown-modal {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  max-height: 600px;
  min-height: 450px;
  overflow-y: auto;
  z-index: 10001;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.loading-products-modal,
.no-products-modal {
  padding: 2rem;
  text-align: center;
  color: #6c757d;
  font-size: 0.9rem;
}

.product-list-modal {
  display: flex;
  flex-direction: column;
}

.product-item-modal {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 90px;
}

.product-item-modal:hover {
  background: #f8f9fa;
}

.product-item-modal:last-child {
  border-bottom: none;
}

.product-item-modal img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border-radius: 6px;
  background: #f8f9fa;
  flex-shrink: 0;
}

.product-item-info-modal {
  flex: 1;
  min-width: 0;
}

.product-item-info-modal h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-item-price-modal {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price-modal {
  font-size: 0.95rem;
  font-weight: 700;
  color: #dc3545;
}

.select-product-btn-modal {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.select-product-btn-modal:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

/* Favorite Modal for Compare Page */
.favorite-modal-compare {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10001;
  animation: fadeIn 0.3s;
}

.favorite-modal-content-compare {
  background: white;
  border-radius: 20px;
  max-width: 900px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.favorite-modal-header-compare {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #e9ecef;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

.favorite-modal-header-compare h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.favorite-modal-close-compare {
  background: #f8f9fa;
  border: none;
  color: #6c757d;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s ease;
}

.favorite-modal-close-compare:hover {
  background: #e9ecef;
  color: #2c3e50;
}

.favorite-modal-body-compare {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.empty-favorites-modal-compare {
  text-align: center;
  padding: 3rem 2rem;
  color: #6c757d;
}

.empty-favorites-modal-compare i {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1rem;
  display: block;
}

.empty-favorites-modal-compare p {
  font-size: 1.1rem;
  margin: 0;
}

.favorite-products-grid-compare {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.favorite-product-card-compare {
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  cursor: pointer;
}

.favorite-product-card-compare:hover {
  border-color: #FF5500;
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.favorite-product-image-compare {
  width: 100%;
  height: 180px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.favorite-product-image-compare img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.favorite-product-info-compare {
  padding: 1rem;
  background: white;
}

.favorite-product-info-compare h4 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8rem;
}

.favorite-product-specs-compare {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 0.5rem;
}

.favorite-product-specs-compare span {
  font-size: 0.75rem;
  padding: 2px 6px;
  background: #e9ecef;
  border-radius: 4px;
  color: #6c757d;
}

.favorite-product-price-compare {
  font-size: 1rem;
  font-weight: 700;
  color: #dc3545;
  margin-bottom: 0.75rem;
}

.btn-select-favorite-compare {
  width: 100%;
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-select-favorite-compare:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>

