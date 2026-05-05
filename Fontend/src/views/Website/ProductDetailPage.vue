<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { useCartStore } from '@/stores/cartStore'
import Toast from '@/components/Toast.vue'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'

interface ProductDetail {
  id: number
  maSanPham: string
  tenSanPham: string
  moTa?: string
  thietKe?: string
  kichThuoc?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  tenDanhMuc?: string
  tenHang?: string
  tenManHinh?: string
  tenCameraTruoc?: string
  tenCameraSau?: string
  tenChip?: string
  tenGpu?: string
  tenSim?: string
  tenHeDieuHanh?: string
  tenCpu?: string
  tenPin?: string
  tongImei?: number
  variants?: VariantDetail[]
  averageRating?: number
  reviewCount?: number
}

interface VariantDetail {
  id: number
  idRam: number
  idRom: number
  idMauSac: number
  soLuong: number
  donGia: number
  giaNhap?: number
  ghiChu?: string
  tenRam?: string
  tenRom?: string
  tenMauSac?: string
  imeis?: string[]
  imageUrls?: string[]
  giaGoc?: number
  giaSauGiam?: number
  giamPhanTram?: number
}

interface Review {
  reviewId: number
  idSanPham: number
  idChiTietSanPham?: number
  idNguoiDung: number
  tenNguoiDung: string
  rating: number
  comment: string
  ngayTao: string
  trangThai: number
}

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref<ProductDetail | null>(null)
const loading = ref(true)
const reviews = ref<Review[]>([])
const relatedProducts = ref<ProductDetail[]>([])

const selectedImageIndex = ref(0)
const showImageModal = ref(false)
const showSpecsModal = ref(false)
const showCompareModal = ref(false)
const isFavorite = ref(false)
const favoriteProducts = ref<number[]>([]) // Danh sách ID sản phẩm yêu thích

// Compare products state
const compareProducts = ref<Array<{ id: number | null; data: any | null }>>([
  { id: null, data: null }, // Product 1 (current product)
  { id: null, data: null }, // Product 2
  { id: null, data: null }, // Product 3
])
const showProductDropdown = ref<number | null>(null) // Index of dropdown to show
const productSearchQuery = ref<{ [key: number]: string }>({ 0: '', 1: '', 2: '' })
const allProducts = ref<any[]>([])
const filteredProducts = ref<{ [key: number]: any[] }>({ 0: [], 1: [], 2: [] })
const isLoadingProducts = ref(false)
const showFavoriteProducts = ref<{ [key: number]: boolean }>({ 0: false, 1: false, 2: false })
const showFavoriteModal = ref(false)
const favoriteModalIndex = ref<number | null>(null) // Track which slot is opening favorite modal
const favoriteProductsList = ref<any[]>([]) // List of favorite products for modal

const selectedVariant = ref<VariantDetail | null>(null)
const selectedColor = ref<number | null>(null)
const selectedStorage = ref<number | null>(null)
const quantity = ref(1)

const relatedProductIndex = ref(0)
const windowWidth = ref(window.innerWidth)

// State quản lý scroll cho related products
const scrollPosition = ref(0)
const isScrollAtEnd = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)

const reviewForm = ref({
  tenNguoiDung: '',
  rating: null, // Rating sẽ được thực hiện ở TheoDoiDonHangPage
  comment: '',
})
const isSubmittingReview = ref(false)
const isLoggedIn = ref(false)
const userInfo = ref(null)

// Check user login status
const checkUserLogin = () => {
  const customerToken = localStorage.getItem('customerToken')
  const customerUser = localStorage.getItem('customerUser')

  if (customerToken && customerUser) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(customerUser)
      // Tự động điền tên nếu đã login
      if (userInfo.value) {
        reviewForm.value.tenNguoiDung =
          userInfo.value.hoTen || userInfo.value.tenKhachHang || userInfo.value.taiKhoan || ''
      }
    } catch (error) {
      console.error('Error parsing customer user data:', error)
      isLoggedIn.value = false
      userInfo.value = null
    }
  } else {
    isLoggedIn.value = false
    userInfo.value = null
  }
}

const productId = computed(() => {
  const id = route.params.id
  return id ? parseInt(id as string) : null
})

const ratingStats = ref({
  averageRating: 0,
  reviewCount: 0,
})

// Computed: Lấy reviews có rating (không null)
const reviewsWithRating = computed(() => {
  return reviews.value.filter((r) => r.rating != null && r.rating > 0)
})

// Computed: Tính điểm trung bình từ reviews thực tế
const computedAverageRating = computed(() => {
  if (reviewsWithRating.value.length === 0) return 0
  const sum = reviewsWithRating.value.reduce((acc, r) => acc + (r.rating || 0), 0)
  return sum / reviewsWithRating.value.length
})

// Function: Đếm số lượng reviews theo mức rating
function getRatingCount(star: number): number {
  return reviewsWithRating.value.filter((r) => r.rating === star).length
}

// Function: Tính phần trăm reviews theo mức rating
function getRatingPercentage(star: number): number {
  if (reviewsWithRating.value.length === 0) return 0
  const count = getRatingCount(star)
  return (count / reviewsWithRating.value.length) * 100
}

const availableColors = computed(() => {
  if (!product.value?.variants) return []
  const colors = new Map()
  product.value.variants.forEach((variant) => {
    if (variant.idMauSac && variant.tenMauSac) {
      colors.set(variant.idMauSac, {
        id: variant.idMauSac,
        name: variant.tenMauSac,
        code: getColorCode(variant.idMauSac),
      })
    }
  })
  return Array.from(colors.values())
})

const availableStorage = computed(() => {
  if (!product.value?.variants) return []
  const storage = new Map()
  product.value.variants.forEach((variant) => {
    if (variant.idRom && variant.tenRom) {
      storage.set(variant.idRom, {
        id: variant.idRom,
        name: variant.tenRom,
      })
    }
  })
  return Array.from(storage.values())
})

const currentVariant = computed(() => {
  if (!product.value?.variants) return null

  return (
    product.value.variants.find(
      (variant) =>
        variant.idMauSac === selectedColor.value && variant.idRom === selectedStorage.value,
    ) || product.value.variants[0]
  )
})

const productImages = computed(() => {
  if (!product.value?.variants) return []

  const allImages: string[] = []
  product.value.variants.forEach((variant) => {
    if (variant.imageUrls && variant.imageUrls.length > 0) {
      allImages.push(...variant.imageUrls)
    }
  })

  if (allImages.length === 0) {
    return [createPlaceholderImage(product.value.tenSanPham)]
  }

  return allImages
})

const isInStock = computed(() => {
  return currentVariant.value ? currentVariant.value.soLuong > 0 : false
})

const stockQuantity = computed(() => {
  return currentVariant.value?.soLuong || 0
})

// Computed properties for discount pricing
const currentPrice = computed(() => {
  if (!currentVariant.value) return 0
  // Ưu tiên giá sau giảm, nếu không có thì dùng giá gốc
  return currentVariant.value.giaSauGiam || currentVariant.value.donGia || 0
})

const originalPrice = computed(() => {
  if (!currentVariant.value) return null
  // Nếu có giá gốc và giá sau giảm khác nhau, hiển thị giá gốc
  if (currentVariant.value.giaGoc && currentVariant.value.giaSauGiam) {
    if (currentVariant.value.giaGoc > currentVariant.value.giaSauGiam) {
      return currentVariant.value.giaGoc
    }
  }
  // Nếu có giảm phần trăm và giá gốc, tính giá gốc từ giá sau giảm
  if (currentVariant.value.giamPhanTram && currentVariant.value.giamPhanTram > 0) {
    const discountedPrice = currentVariant.value.giaSauGiam || currentVariant.value.donGia
    const discountPercent = currentVariant.value.giamPhanTram / 100
    const original = discountedPrice / (1 - discountPercent)
    return Math.round(original)
  }
  return null
})

const discountPercent = computed(() => {
  if (!currentVariant.value) return 0
  return currentVariant.value.giamPhanTram || 0
})

const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal for actions (add to cart / buy now)
const confirmModal = ref<{ show: boolean; loading: boolean; action: 'add' | 'buy' | null }>({
  show: false,
  loading: false,
  action: null,
})

function openConfirm(action: 'add' | 'buy') {
  if (!currentVariant.value || !isInStock.value) {
    toastRef.value?.showToast('error', 'Lỗi', 'Sản phẩm hiện không có sẵn')
    return
  }
  confirmModal.value = { show: true, loading: false, action }
}

function closeConfirm() {
  if (confirmModal.value.loading) return
  confirmModal.value.show = false
}

async function confirmProceed() {
  if (!confirmModal.value.action) return
  confirmModal.value.loading = true
  try {
    if (confirmModal.value.action === 'add') {
      await addToCart()
    } else {
      await buyNow()
    }
    confirmModal.value.show = false
  } finally {
    confirmModal.value.loading = false
  }
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

function getColorCode(idMauSac: number | null) {
  if (!idMauSac) return '#ccc'

  const colorMap: { [key: number]: string } = {
    1: '#FF6B35',
    2: '#1E3A8A',
    3: '#C0C0C0',
    4: '#000000',
    5: '#FFFFFF',
    6: '#FF0000',
    7: '#00FF00',
    8: '#FFFF00',
    9: '#800080',
    10: '#FFC0CB',
  }

  return colorMap[idMauSac] || '#ccc'
}

async function loadProductDetail() {
  if (!productId.value) return

  loading.value = true
  // Check login status
  checkUserLogin()

  try {
    // Load chi tiết sản phẩm từ API view
    const { data } = await api.get<ProductDetail>(`/api/san-pham/${productId.value}/view`)

    // Load từ API san-pham-pos để có thông tin khuyến mãi (giống ShopPage.vue)
    try {
      const posResponse = await api.get(`/api/san-pham-pos`)

      if (posResponse.data && Array.isArray(posResponse.data)) {
        // Tìm tất cả variants của sản phẩm này trong danh sách từ san-pham-pos
        // API san-pham-pos trả về các chi tiết sản phẩm (variants) với id sản phẩm hoặc chiTietSanPhamId
        const posVariants = posResponse.data.filter((p: any) => {
          // Kiểm tra nhiều cách để match sản phẩm
          return (
            p.id === productId.value ||
            (p.sanPhamId && p.sanPhamId === productId.value) ||
            (data.id && p.id === data.id) ||
            (p.tenSanPham && data.tenSanPham && p.tenSanPham === data.tenSanPham)
          )
        })

        console.log('🔍 Product ID:', productId.value, 'Found POS variants:', posVariants.length)

        // Merge thông tin khuyến mãi từ posVariants vào variants của product
        if (data.variants && data.variants.length > 0 && posVariants.length > 0) {
          data.variants.forEach((variant: any) => {
            // Tìm variant tương ứng trong posVariants theo chiTietSanPhamId hoặc các thuộc tính khác
            const matchingPosVariant = posVariants.find((pv: any) => {
              // Match theo chiTietSanPhamId (variant ID)
              if (pv.chiTietSanPhamId === variant.id) return true
              // Match theo các thuộc tính: RAM, ROM, Màu sắc
              if (
                pv.idRam === variant.idRam &&
                pv.idRom === variant.idRom &&
                pv.idMauSac === variant.idMauSac
              )
                return true
              return false
            })

            if (matchingPosVariant) {
              console.log(
                '✅ Found matching POS variant for variant ID:',
                variant.id,
                matchingPosVariant,
              )
              // Áp dụng thông tin khuyến mãi từ posVariant
              if (matchingPosVariant.giamPhanTram && matchingPosVariant.giamPhanTram > 0) {
                variant.giamPhanTram = matchingPosVariant.giamPhanTram
              }
              if (matchingPosVariant.giaGoc) {
                variant.giaGoc = matchingPosVariant.giaGoc
              }
              if (matchingPosVariant.giaSauGiam) {
                variant.giaSauGiam = matchingPosVariant.giaSauGiam
              }
              console.log('💰 Applied discount to variant:', {
                id: variant.id,
                giamPhanTram: variant.giamPhanTram,
                giaGoc: variant.giaGoc,
                giaSauGiam: variant.giaSauGiam,
                donGia: variant.donGia,
              })
            } else if (posVariants.length > 0) {
              // Nếu không tìm thấy variant cụ thể, thử dùng thông tin từ variant đầu tiên (nếu có)
              const firstPosVariant = posVariants[0]
              if (
                firstPosVariant &&
                firstPosVariant.giamPhanTram &&
                firstPosVariant.giamPhanTram > 0
              ) {
                console.log('⚠️ Using first POS variant discount for variant:', variant.id)
                variant.giamPhanTram = firstPosVariant.giamPhanTram
                if (firstPosVariant.giaGoc) variant.giaGoc = firstPosVariant.giaGoc
                if (firstPosVariant.giaSauGiam) variant.giaSauGiam = firstPosVariant.giaSauGiam
              }
            }
          })
        }
      }
    } catch (posError) {
      console.warn('Không thể load thông tin khuyến mãi từ san-pham-pos:', posError)
      // Tiếp tục với dữ liệu từ API view
    }

    product.value = data

    if (data.variants && data.variants.length > 0) {
      const firstVariant = data.variants[0]
      selectedColor.value = firstVariant.idMauSac
      selectedStorage.value = firstVariant.idRom
      selectedVariant.value = firstVariant
    }

    await loadRelatedProducts()
    await loadReviews()
    await loadRatingStats()

    // Check favorite status after loading product
    checkFavoriteStatus()
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin sản phẩm')
  } finally {
    loading.value = false
  }
}

async function loadRelatedProducts() {
  try {
    // Load từ /api/san-pham-pos giống CartPage để có đầy đủ thông tin (giá, hình ảnh, etc.)
    const { data } = await api.get(`/api/san-pham-pos`)

    if (data && Array.isArray(data)) {
      // Lọc bỏ sản phẩm hiện tại và lấy tối đa 8 sản phẩm
      const currentProductId = product.value?.id
      relatedProducts.value = data
        .filter((p: any) => {
          // Lọc theo id sản phẩm hoặc chiTietSanPhamId
          if (!currentProductId) return true

          // So sánh với nhiều trường có thể có
          const pId = p.id || p.sanPhamId || p.chiTietSanPhamId
          if (pId && currentProductId) {
            return pId !== currentProductId
          }

          // Nếu không có ID để so sánh, giữ lại sản phẩm
          return true
        })
        .slice(0, 8)
        .map((p: any) => ({
          id: p.id || p.sanPhamId || p.chiTietSanPhamId,
          sanPhamId: p.sanPhamId || p.id,
          chiTietSanPhamId: p.chiTietSanPhamId || p.id,
          tenSanPham: p.tenSanPham || p.ten,
          ten: p.ten || p.tenSanPham,
          gia: p.gia || p.donGia || 0,
          hinhAnh: p.hinhAnh || p.anh || p.imageUrls?.[0],
          anh: p.anh || p.hinhAnh || p.imageUrls?.[0],
          variants: p.variants || [
            {
              id: p.chiTietSanPhamId || p.id,
              donGia: p.gia || p.donGia || 0,
            },
          ],
        }))
    } else {
      // Fallback: load sản phẩm cùng danh mục
      if (product.value?.danhMucId) {
        const { data: categoryData } = await api.get<ProductDetail[]>(
          `/api/san-pham/danh-muc/${product.value.danhMucId}`,
        )
        relatedProducts.value = categoryData.filter((p) => p.id !== product.value?.id).slice(0, 8)
      } else {
        // Fallback cuối: load sản phẩm ngẫu nhiên
        const { data: allData } = await api.get<ProductDetail[]>('/api/san-pham')
        relatedProducts.value = allData.filter((p) => p.id !== product.value?.id).slice(0, 8)
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm liên quan:', error)
    relatedProducts.value = []
  }
}

async function loadReviews() {
  try {
    // Load reviews theo sản phẩm chung
    const { data } = await api.get<Review[]>(`/api/reviews/san-pham/${productId.value}`)
    console.log('Loaded reviews:', data)
    reviews.value = data || []
    console.log('Reviews count:', reviews.value.length)
  } catch (error: any) {
    // 401 là bình thường khi người dùng không đăng nhập (website public)
    // Chỉ log lỗi nếu không phải 401
    if (error?.response?.status !== 401) {
      console.error('Lỗi khi tải đánh giá:', error)
    }
    reviews.value = []
  }
}

async function loadRatingStats() {
  try {
    // Load rating stats theo sản phẩm chung
    const { data } = await api.get(`/api/reviews/san-pham/${productId.value}/rating`)
    ratingStats.value = {
      averageRating: data?.averageRating || 0,
      reviewCount: data?.reviewCount || 0,
    }

    if (product.value) {
      product.value.averageRating = data?.averageRating || 0
      product.value.reviewCount = data?.reviewCount || 0
    }
  } catch (error: any) {
    // 401 là bình thường khi người dùng không đăng nhập (website public)
    // Chỉ log lỗi nếu không phải 401
    if (error?.response?.status !== 401) {
      console.error('Lỗi khi tải thống kê đánh giá:', error)
    }
    ratingStats.value = {
      averageRating: 0,
      reviewCount: 0,
    }
  }
}

function selectImage(index: number) {
  selectedImageIndex.value = index
}

function openImageModal() {
  showImageModal.value = true
}

function closeImageModal() {
  showImageModal.value = false
}

function nextImage() {
  if (productImages.value.length > 0) {
    selectedImageIndex.value = (selectedImageIndex.value + 1) % productImages.value.length
  }
}

function prevImage() {
  if (productImages.value.length > 0) {
    selectedImageIndex.value =
      selectedImageIndex.value === 0 ? productImages.value.length - 1 : selectedImageIndex.value - 1
  }
}

function updateCurrentVariant() {
  // Cập nhật selectedVariant để đồng bộ với currentVariant computed
  // currentVariant tự động cập nhật dựa trên selectedColor và selectedStorage
  if (currentVariant.value) {
    selectedVariant.value = currentVariant.value
  }
}

function selectColor(colorId: number) {
  selectedColor.value = colorId
  quantity.value = 1
  updateCurrentVariant()

  // Cập nhật ảnh chính khi chọn màu
  updateMainImageForColor(colorId)
}

function selectStorage(storageId: number) {
  selectedStorage.value = storageId
  quantity.value = 1
  updateCurrentVariant()

  // Cập nhật ảnh chính khi chọn storage (nếu có ảnh riêng cho storage)
  updateMainImageForStorage(storageId)
}

function increaseQuantity() {
  if (currentVariant.value && quantity.value < currentVariant.value.soLuong) {
    quantity.value++
  }
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

function addToCart() {
  if (!currentVariant.value || !isInStock.value) {
    toastRef.value?.showToast('error', 'Lỗi', 'Sản phẩm hiện không có sẵn')
    return
  }

  if (quantity.value < 1) {
    toastRef.value?.showToast('error', 'Lỗi', 'Số lượng phải lớn hơn 0')
    return
  }

  if (quantity.value > currentVariant.value.soLuong) {
    toastRef.value?.showToast('error', 'Lỗi', `Chỉ còn ${currentVariant.value.soLuong} sản phẩm`)
    return
  }

  try {
    // Tạo cart item theo format của cartStore
    // Sử dụng giá sau giảm (currentPrice) thay vì giá gốc (donGia)
    const cartItem = {
      chiTietSanPhamId: currentVariant.value.id,
      tenSanPham: product.value?.tenSanPham || '',
      tenRam: getStorageName(currentVariant.value.idRom),
      tenRom: getStorageName(currentVariant.value.idRom),
      tenMauSac: getColorName(currentVariant.value.idMauSac),
      gia: currentPrice.value, // Sử dụng giá sau giảm
      quantity: quantity.value,
      hinhAnh: getMainImageUrl(),
      soLuongTon: currentVariant.value.soLuong,
    }

    // Thêm vào cartStore
    cartStore.addItem(cartItem)

    // Hiển thị thông báo thành công
    console.log('Thêm vào giỏ hàng thành công')
    toastRef.value?.showToast('success', 'Thành công', 'Thêm vào giỏ hàng thành công')

    // Fallback notification nếu Toast không hoạt động
    if (!toastRef.value) {
      alert('Thêm vào giỏ hàng thành công!')
    }

    // Reset quantity về 1
    quantity.value = 1
  } catch (error) {
    console.error('Lỗi khi thêm vào giỏ hàng:', error)
    toastRef.value?.showToast('error', 'Lỗi', 'Có lỗi xảy ra khi thêm vào giỏ hàng')
  }
}

async function buyNow() {
  if (!currentVariant.value || !isInStock.value) {
    toastRef.value?.showToast('error', 'Lỗi', 'Sản phẩm hiện không có sẵn')
    return
  }

  // Thêm sản phẩm vào giỏ hàng trước khi chuyển đến trang đặt hàng
  await addToCart()

  // Chờ một chút để đảm bảo cart store được cập nhật
  await new Promise((resolve) => setTimeout(resolve, 100))

  // Chuyển đến trang đặt hàng với thông tin sản phẩm
  router.push({
    path: '/dat-hang',
    query: {
      productId: product.value?.id,
      variantId: currentVariant.value?.id,
      quantity: quantity.value,
      buyNow: 'true', // Đánh dấu đây là mua ngay
    },
  })
}

// Helper functions for reviews display
function getInitials(name: string): string {
  if (!name) return '?'
  const words = name.trim().split(' ')
  if (words.length >= 2) {
    return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
  }
  return name.charAt(0).toUpperCase()
}

function getAvatarColor(name: string): string {
  if (!name) return '#FF5500'

  // Tạo màu dựa trên hash của tên để mỗi người có màu nhất quán
  const colors = [
    'linear-gradient(135deg, #28a745 0%, #20c997 100%)', // Xanh lá
    'linear-gradient(135deg, #6f42c1 0%, #9c27b0 100%)', // Tím
    'linear-gradient(135deg, #FF5500 0%, #DC143C 100%)', // Cam đỏ
    'linear-gradient(135deg, #007bff 0%, #0056b3 100%)', // Xanh dương
    'linear-gradient(135deg, #17a2b8 0%, #138496 100%)', // Xanh ngọc
    'linear-gradient(135deg, #ffc107 0%, #ff9800 100%)', // Vàng cam
    'linear-gradient(135deg, #e83e8c 0%, #c2185b 100%)', // Hồng
    'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)', // Xanh lá ngọc
  ]

  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

function getRatingText(rating: number | null): string {
  if (!rating) return ''
  const ratingMap: { [key: number]: string } = {
    5: 'Tuyệt vời',
    4: 'Rất tốt',
    3: 'Tốt',
    2: 'Tạm được',
    1: 'Không hài lòng',
  }
  return ratingMap[rating] || ''
}

function formatReviewDate(dateString: string): string {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const diffWeeks = Math.floor(diffDays / 7)
  const diffMonths = Math.floor(diffDays / 30)
  const diffYears = Math.floor(diffDays / 365)

  if (diffYears > 0) {
    return `Đánh giá đã đăng vào ${diffYears} ${diffYears === 1 ? 'năm' : 'năm'} trước`
  } else if (diffMonths > 0) {
    return `Đánh giá đã đăng vào ${diffMonths} ${diffMonths === 1 ? 'tháng' : 'tháng'} trước`
  } else if (diffWeeks > 0) {
    return `Đánh giá đã đăng vào ${diffWeeks} ${diffWeeks === 1 ? 'tuần' : 'tuần'} trước`
  } else if (diffDays > 0) {
    return `Đánh giá đã đăng vào ${diffDays} ${diffDays === 1 ? 'ngày' : 'ngày'} trước`
  } else {
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    if (diffHours > 0) {
      return `Đánh giá đã đăng vào ${diffHours} ${diffHours === 1 ? 'giờ' : 'giờ'} trước`
    } else {
      const diffMinutes = Math.floor(diffMs / (1000 * 60))
      if (diffMinutes > 0) {
        return `Đánh giá đã đăng vào ${diffMinutes} ${diffMinutes === 1 ? 'phút' : 'phút'} trước`
      } else {
        return 'Đánh giá đã đăng vừa xong'
      }
    }
  }
}

function submitReview() {
  // Nếu chưa login, yêu cầu nhập tên
  if (!isLoggedIn.value && !reviewForm.value.tenNguoiDung.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên của bạn')
    return
  }

  if (!reviewForm.value.comment.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập bình luận')
    return
  }

  isSubmittingReview.value = true

  const reviewData = {
    idSanPham: productId.value,
    idNguoiDung: isLoggedIn.value ? userInfo.value?.id || userInfo.value?.khachHangId : null,
    tenNguoiDung: isLoggedIn.value
      ? userInfo.value?.hoTen ||
        userInfo.value?.tenKhachHang ||
        userInfo.value?.taiKhoan ||
        'Người dùng'
      : reviewForm.value.tenNguoiDung,
    rating: null, // Rating sẽ được thực hiện ở TheoDoiDonHangPage
    comment: reviewForm.value.comment,
  }

  api
    .post('/api/reviews', reviewData)
    .then(() => {
      toastRef.value?.success('Thành công', 'Bình luận đã được gửi, chờ duyệt')
      reviewForm.value.comment = ''
      if (!isLoggedIn.value) {
        reviewForm.value.tenNguoiDung = ''
      }
      loadReviews()
      loadRatingStats()
    })
    .catch((error) => {
      console.error('Lỗi khi gửi bình luận:', error)
      const errorMessage = error?.response?.data?.error || 'Có lỗi xảy ra khi gửi bình luận'
      toastRef.value?.error('Lỗi', errorMessage)
    })
    .finally(() => {
      isSubmittingReview.value = false
    })
}

function goBack() {
  router.go(-1)
}

function viewProduct(productId: number | string | undefined) {
  if (!productId) {
    console.warn('viewProduct: No product ID provided')
    return
  }
  // Đảm bảo ID là số hoặc string hợp lệ
  const id = typeof productId === 'number' ? productId : parseInt(String(productId), 10)
  if (isNaN(id)) {
    console.warn('viewProduct: Invalid product ID:', productId)
    return
  }
  router.push({ name: 'product-detail', params: { id: String(id) } })
}

// Logic xử lý scroll cho related products
const scrollLeft = () => {
  if (scrollContainer.value) {
    const cardWidth = 300 // 280px + 20px gap
    const newPosition = Math.max(0, scrollPosition.value - cardWidth)
    scrollContainer.value.scrollTo({
      left: newPosition,
      behavior: 'smooth',
    })
    scrollPosition.value = newPosition
    updateScrollState()
  }
}

const scrollRight = () => {
  if (scrollContainer.value) {
    const cardWidth = 300 // 280px + 20px gap
    const maxScroll = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
    const newPosition = Math.min(maxScroll, scrollPosition.value + cardWidth)
    scrollContainer.value.scrollTo({
      left: newPosition,
      behavior: 'smooth',
    })
    scrollPosition.value = newPosition
    updateScrollState()
  }
}

const updateScrollState = () => {
  if (scrollContainer.value) {
    const maxScroll = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
    isScrollAtEnd.value = scrollPosition.value >= maxScroll
  }
}

const handleScroll = () => {
  if (scrollContainer.value) {
    scrollPosition.value = scrollContainer.value.scrollLeft
    updateScrollState()
  }
}

function createPlaceholderImage(text: string): string {
  const canvas = document.createElement('canvas')
  canvas.width = 400
  canvas.height = 400
  const ctx = canvas.getContext('2d')!

  ctx.fillStyle = '#f3f4f6'
  ctx.fillRect(0, 0, 400, 400)

  ctx.fillStyle = '#6b7280'
  ctx.font = '16px Arial'
  ctx.textAlign = 'center'
  ctx.fillText(text, 200, 200)

  return canvas.toDataURL()
}

function getRelatedProductImage(product: ProductDetail | any): string {
  // Nếu product có hinhAnh trực tiếp (từ san-pham-pos)
  if (product.hinhAnh || product.anh) {
    return createFullImageUrl(product.hinhAnh || product.anh)
  }

  // Nếu có variants
  if (product.variants && product.variants.length > 0) {
    const firstVariant = product.variants[0]
    if (firstVariant.imageUrls && firstVariant.imageUrls.length > 0) {
      return createFullImageUrl(firstVariant.imageUrls[0])
    }
  }

  return createPlaceholderImage(product.tenSanPham || product.ten || 'No Image')
}

function createFullImageUrl(imagePath: string): string {
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  // Simple fallback to localhost
  return `http://localhost:8080${imagePath}`
}

function getColorImage(colorId: number): string {
  if (!product.value?.variants) return createPlaceholderImage('No Image')

  // Tìm variant có màu này
  const colorVariant = product.value.variants.find((variant) => variant.idMauSac === colorId)

  if (colorVariant?.imageUrls && colorVariant.imageUrls.length > 0) {
    return createFullImageUrl(colorVariant.imageUrls[0])
  }

  // Fallback: tìm ảnh có chứa tên màu
  const colorName = availableColors.value.find((c) => c.id === colorId)?.name.toLowerCase() || ''
  const matchingImage = productImages.value.find(
    (img) =>
      img.toLowerCase().includes(colorName) ||
      img.toLowerCase().includes('color') ||
      img.toLowerCase().includes('mau'),
  )

  if (matchingImage) {
    return createFullImageUrl(matchingImage)
  }

  // Fallback cuối cùng: ảnh đầu tiên
  return productImages.value.length > 0
    ? createFullImageUrl(productImages.value[0])
    : createPlaceholderImage('No Image')
}

function getStoragePrice(storageId: number): number {
  if (!product.value?.variants) return 0

  // Tìm variant có storage này (với màu hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idRom === storageId && v.idMauSac === selectedColor.value,
  )

  if (variant) {
    return variant.donGia || 0
  }

  // Fallback: tìm variant có storage này (bất kỳ màu nào)
  const fallbackVariant = product.value.variants.find((v) => v.idRom === storageId)
  return fallbackVariant?.donGia || 0
}

function getStoragePriceAfterDiscount(storageId: number): number {
  if (!product.value?.variants) return 0

  // Tìm variant có storage này (với màu hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idRom === storageId && v.idMauSac === selectedColor.value,
  )

  if (variant) {
    return variant.giaSauGiam || variant.donGia || 0
  }

  // Fallback: tìm variant có storage này (bất kỳ màu nào)
  const fallbackVariant = product.value.variants.find((v) => v.idRom === storageId)
  return fallbackVariant?.giaSauGiam || fallbackVariant?.donGia || 0
}

function getStorageOriginalPrice(storageId: number): number | null {
  if (!product.value?.variants) return null

  // Tìm variant có storage này (với màu hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idRom === storageId && v.idMauSac === selectedColor.value,
  )

  if (variant) {
    if (variant.giaGoc && variant.giaSauGiam && variant.giaGoc > variant.giaSauGiam) {
      return variant.giaGoc
    }
    if (variant.giamPhanTram && variant.giamPhanTram > 0) {
      const discountedPrice = variant.giaSauGiam || variant.donGia
      const discountPercent = variant.giamPhanTram / 100
      return Math.round(discountedPrice / (1 - discountPercent))
    }
  }

  // Fallback: tìm variant có storage này (bất kỳ màu nào)
  const fallbackVariant = product.value.variants.find((v) => v.idRom === storageId)
  if (fallbackVariant) {
    if (
      fallbackVariant.giaGoc &&
      fallbackVariant.giaSauGiam &&
      fallbackVariant.giaGoc > fallbackVariant.giaSauGiam
    ) {
      return fallbackVariant.giaGoc
    }
    if (fallbackVariant.giamPhanTram && fallbackVariant.giamPhanTram > 0) {
      const discountedPrice = fallbackVariant.giaSauGiam || fallbackVariant.donGia
      const discountPercent = fallbackVariant.giamPhanTram / 100
      return Math.round(discountedPrice / (1 - discountPercent))
    }
  }

  return null
}

function getColorPrice(colorId: number): number {
  if (!product.value?.variants) return 0

  // Tìm variant có màu này (với storage hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idMauSac === colorId && v.idRom === selectedStorage.value,
  )

  if (variant) {
    return variant.donGia || 0
  }

  // Fallback: tìm variant có màu này (bất kỳ storage nào)
  const fallbackVariant = product.value.variants.find((v) => v.idMauSac === colorId)
  return fallbackVariant?.donGia || 0
}

function getColorPriceAfterDiscount(colorId: number): number {
  if (!product.value?.variants) return 0

  // Tìm variant có màu này (với storage hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idMauSac === colorId && v.idRom === selectedStorage.value,
  )

  if (variant) {
    return variant.giaSauGiam || variant.donGia || 0
  }

  // Fallback: tìm variant có màu này (bất kỳ storage nào)
  const fallbackVariant = product.value.variants.find((v) => v.idMauSac === colorId)
  return fallbackVariant?.giaSauGiam || fallbackVariant?.donGia || 0
}

function getColorOriginalPrice(colorId: number): number | null {
  if (!product.value?.variants) return null

  // Tìm variant có màu này (với storage hiện tại)
  const variant = product.value.variants.find(
    (v) => v.idMauSac === colorId && v.idRom === selectedStorage.value,
  )

  if (variant) {
    if (variant.giaGoc && variant.giaSauGiam && variant.giaGoc > variant.giaSauGiam) {
      return variant.giaGoc
    }
    if (variant.giamPhanTram && variant.giamPhanTram > 0) {
      const discountedPrice = variant.giaSauGiam || variant.donGia
      const discountPercent = variant.giamPhanTram / 100
      return Math.round(discountedPrice / (1 - discountPercent))
    }
  }

  // Fallback: tìm variant có màu này (bất kỳ storage nào)
  const fallbackVariant = product.value.variants.find((v) => v.idMauSac === colorId)
  if (fallbackVariant) {
    if (
      fallbackVariant.giaGoc &&
      fallbackVariant.giaSauGiam &&
      fallbackVariant.giaGoc > fallbackVariant.giaSauGiam
    ) {
      return fallbackVariant.giaGoc
    }
    if (fallbackVariant.giamPhanTram && fallbackVariant.giamPhanTram > 0) {
      const discountedPrice = fallbackVariant.giaSauGiam || fallbackVariant.donGia
      const discountPercent = fallbackVariant.giamPhanTram / 100
      return Math.round(discountedPrice / (1 - discountPercent))
    }
  }

  return null
}

function updateMainImageForColor(colorId: number) {
  if (!product.value?.variants) return

  // Tìm variant có màu này
  const colorVariant = product.value.variants.find((variant) => variant.idMauSac === colorId)

  if (colorVariant?.imageUrls && colorVariant.imageUrls.length > 0) {
    // Cập nhật ảnh chính với ảnh của màu được chọn
    const colorImageUrl = createFullImageUrl(colorVariant.imageUrls[0])

    // Tìm index của ảnh này trong productImages hoặc thêm mới
    const existingIndex = productImages.value.findIndex(
      (img) => createFullImageUrl(img) === colorImageUrl,
    )

    if (existingIndex !== -1) {
      selectedImageIndex.value = existingIndex
    } else {
      // Thêm ảnh mới vào đầu danh sách và chọn nó
      productImages.value.unshift(colorVariant.imageUrls[0])
      selectedImageIndex.value = 0
    }
  } else {
    // Fallback: tìm ảnh có chứa tên màu
    const colorName = availableColors.value.find((c) => c.id === colorId)?.name.toLowerCase() || ''
    const matchingImage = productImages.value.find(
      (img) =>
        img.toLowerCase().includes(colorName) ||
        img.toLowerCase().includes('color') ||
        img.toLowerCase().includes('mau'),
    )

    if (matchingImage) {
      const matchingIndex = productImages.value.findIndex((img) => img === matchingImage)
      if (matchingIndex !== -1) {
        selectedImageIndex.value = matchingIndex
      }
    }
  }
}

function updateMainImageForStorage(storageId: number) {
  if (!product.value?.variants) return

  // Tìm variant có storage này (với màu hiện tại)
  const storageVariant = product.value.variants.find(
    (variant) => variant.idRom === storageId && variant.idMauSac === selectedColor.value,
  )

  if (storageVariant?.imageUrls && storageVariant.imageUrls.length > 0) {
    // Cập nhật ảnh chính với ảnh của storage được chọn
    const storageImageUrl = createFullImageUrl(storageVariant.imageUrls[0])

    // Tìm index của ảnh này trong productImages hoặc thêm mới
    const existingIndex = productImages.value.findIndex(
      (img) => createFullImageUrl(img) === storageImageUrl,
    )

    if (existingIndex !== -1) {
      selectedImageIndex.value = existingIndex
    } else {
      // Thêm ảnh mới vào đầu danh sách và chọn nó
      productImages.value.unshift(storageVariant.imageUrls[0])
      selectedImageIndex.value = 0
    }
  }
}

function getMainImageUrl(): string {
  if (!product.value?.variants) {
    return productImages.value.length > 0
      ? createFullImageUrl(productImages.value[selectedImageIndex.value])
      : createPlaceholderImage('No Image')
  }

  // Tìm variant có màu và storage hiện tại
  const currentVariant = product.value.variants.find(
    (variant) =>
      variant.idMauSac === selectedColor.value && variant.idRom === selectedStorage.value,
  )

  if (currentVariant?.imageUrls && currentVariant.imageUrls.length > 0) {
    return createFullImageUrl(currentVariant.imageUrls[0])
  }

  // Fallback: tìm variant có màu hiện tại (bất kỳ storage nào)
  const colorVariant = product.value.variants.find(
    (variant) => variant.idMauSac === selectedColor.value,
  )
  if (colorVariant?.imageUrls && colorVariant.imageUrls.length > 0) {
    return createFullImageUrl(colorVariant.imageUrls[0])
  }

  // Fallback cuối cùng: ảnh từ selectedImageIndex
  return productImages.value.length > 0
    ? createFullImageUrl(productImages.value[selectedImageIndex.value])
    : createPlaceholderImage('No Image')
}

function getStorageName(storageId: number): string {
  const storage = availableStorage.value.find((s) => s.id === storageId)
  return storage?.name || 'Unknown Storage'
}

function getColorName(colorId: number): string {
  const color = availableColors.value.find((c) => c.id === colorId)
  return color?.name || 'Unknown Color'
}

watch(
  () => route.params.id,
  () => {
    if (route.params.id) {
      loadProductDetail()
    }
  },
)

function handleResize() {
  windowWidth.value = window.innerWidth
}

// Load favorite products from localStorage
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

// Save favorite products to localStorage
function saveFavoriteProducts() {
  try {
    localStorage.setItem('favoriteProducts', JSON.stringify(favoriteProducts.value))
  } catch (error) {
    console.error('Error saving favorite products:', error)
  }
}

// Check if current product is favorite
function checkFavoriteStatus() {
  if (productId.value) {
    isFavorite.value = favoriteProducts.value.includes(productId.value)
  }
}

// Toggle favorite status
function toggleFavorite() {
  if (!productId.value) return

  if (isFavorite.value) {
    // Remove from favorites
    favoriteProducts.value = favoriteProducts.value.filter((id) => id !== productId.value)
    isFavorite.value = false
    if (toastRef.value) {
      toastRef.value.info('Đã bỏ yêu thích', 'Sản phẩm đã được xóa khỏi danh sách yêu thích')
    }
  } else {
    // Add to favorites
    if (!favoriteProducts.value.includes(productId.value)) {
      favoriteProducts.value.push(productId.value)
    }
    isFavorite.value = true
    if (toastRef.value) {
      toastRef.value.success('Đã thêm yêu thích', 'Sản phẩm đã được thêm vào danh sách yêu thích')
    }
  }
  saveFavoriteProducts()
}

// Compare products functions
async function loadAllProducts() {
  isLoadingProducts.value = true
  try {
    const { data } = await api.get('/api/san-pham-pos')
    allProducts.value = (data || []).map((p: any) => ({
      ...p,
      chiTietSanPhamId: p.chiTietSanPhamId || p.id,
      tenSanPham: p.tenSanPham || p.ten || '',
      gia: p.gia || p.giaBan || p.donGia || 0,
      hinhAnh: p.hinhAnh || p.hinhAnhUrl || '',
      tenRam: p.tenRam || '',
      tenRom: p.tenRom || '',
      tenMauSac: p.tenMauSac || '',
      sanPhamId: p.sanPhamId || p.id,
    }))

    // Initialize filtered products for all dropdowns
    filteredProducts.value = {
      0: allProducts.value.filter((p) => (p.sanPhamId || p.id) !== productId.value),
      1: allProducts.value,
      2: allProducts.value,
    }
  } catch (error) {
    console.error('Error loading products:', error)
    allProducts.value = []
  } finally {
    isLoadingProducts.value = false
  }
}

function openCompareModal() {
  // Set current product as first compare product
  if (product.value) {
    compareProducts.value[0] = {
      id: productId.value,
      data: {
        id: productId.value,
        tenSanPham: product.value.tenSanPham,
        gia: currentPrice.value,
        hinhAnh: getMainImageUrl(),
        tenRam: currentVariant.value?.tenRam || '',
        tenRom: currentVariant.value?.tenRom || '',
        tenMauSac: currentVariant.value?.tenMauSac || '',
      },
    }
  }
  showCompareModal.value = true
  loadAllProducts()
}

function closeCompareModal() {
  showCompareModal.value = false
  showProductDropdown.value = null
  // Reset compare products except first one
  compareProducts.value[1] = { id: null, data: null }
  compareProducts.value[2] = { id: null, data: null }
  productSearchQuery.value = { 0: '', 1: '', 2: '' }
  showFavoriteProducts.value = { 0: false, 1: false, 2: false }
}

function toggleProductDropdown(index: number) {
  if (showProductDropdown.value === index) {
    showProductDropdown.value = null
  } else {
    showProductDropdown.value = index
    searchProducts(index)
  }
}

function searchProducts(index: number) {
  const query = (productSearchQuery.value[index] || '').toLowerCase().trim()

  if (query) {
    filteredProducts.value[index] = allProducts.value.filter((p) => {
      const name = (p.tenSanPham || '').toLowerCase()
      return name.includes(query)
    })
  } else {
    // Show all products except already selected ones
    const selectedIds = compareProducts.value.map((cp) => cp.id).filter((id) => id !== null)
    filteredProducts.value[index] = allProducts.value.filter(
      (p) => !selectedIds.includes(p.sanPhamId || p.id),
    )
  }
}

function selectProduct(index: number, product: any) {
  compareProducts.value[index] = {
    id: product.sanPhamId || product.id,
    data: product,
  }
  showProductDropdown.value = null
  productSearchQuery.value[index] = ''
  showFavoriteProducts.value[index] = false
}

function removeProduct(index: number) {
  if (index === 0) {
    // Cannot remove first product (current product)
    return
  }
  compareProducts.value[index] = { id: null, data: null }
}

function openFavoriteModal(index: number) {
  favoriteModalIndex.value = index
  loadFavoriteProductsList()
  showFavoriteModal.value = true
}

function closeFavoriteModal() {
  showFavoriteModal.value = false
  favoriteModalIndex.value = null
}

async function loadFavoriteProductsList() {
  const favoriteIds = favoriteProducts.value
  if (favoriteIds.length === 0) {
    favoriteProductsList.value = []
    return
  }

  try {
    const { data } = await api.get('/api/san-pham-pos')
    favoriteProductsList.value = (data || [])
      .filter((p: any) => favoriteIds.includes(p.sanPhamId || p.id))
      .map((p: any) => ({
        ...p,
        chiTietSanPhamId: p.chiTietSanPhamId || p.id,
        tenSanPham: p.tenSanPham || p.ten || '',
        gia: p.gia || p.giaBan || p.donGia || 0,
        hinhAnh: p.hinhAnh || p.hinhAnhUrl || '',
        tenRam: p.tenRam || '',
        tenRom: p.tenRom || '',
        tenMauSac: p.tenMauSac || '',
        sanPhamId: p.sanPhamId || p.id,
      }))
  } catch (error) {
    console.error('Error loading favorite products:', error)
    favoriteProductsList.value = []
  }
}

function selectFavoriteProduct(product: any) {
  if (favoriteModalIndex.value !== null) {
    selectProduct(favoriteModalIndex.value, product)
    closeFavoriteModal()
  }
}

function performCompare() {
  const selectedProducts = compareProducts.value.filter((cp) => cp.id !== null)
  if (selectedProducts.length < 2) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn ít nhất 2 sản phẩm để so sánh')
    return
  }

  // Navigate to compare page with product IDs
  const productIds = selectedProducts.map((p) => p.id).join(',')
  router.push({
    path: '/so-sanh',
    query: { ids: productIds },
  })
  closeCompareModal()
}

onMounted(() => {
  checkUserLogin() // Check login status on mount
  loadFavoriteProducts() // Load favorite products
  loadProductDetail()
  window.addEventListener('resize', handleResize)

  // Thêm event listener cho scroll container sau khi component mount
  setTimeout(() => {
    if (scrollContainer.value) {
      scrollContainer.value.addEventListener('scroll', handleScroll)
      updateScrollState()
    }
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)

  // Remove scroll event listener
  if (scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<template>
  <div class="clickbuy-product-page">
    <!-- Header Layout -->
    <HeaderLayout />

    <br />
    <br />
    <!-- Loading State -->
    <div v-if="loading" class="loading-wrapper">
      <div class="loading-spinner"></div>
      <p>Đang tải thông tin sản phẩm...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="!product" class="error-wrapper">
      <div class="error-icon">⚠️</div>
      <h2>Không tìm thấy sản phẩm</h2>
      <p>Sản phẩm bạn tìm kiếm không tồn tại hoặc đã bị xóa.</p>
      <button @click="goBack" class="btn-back">Quay lại</button>
    </div>

    <!-- Product Detail Content -->
    <div v-else class="product-content">
      <!-- Breadcrumb -->
      <div class="breadcrumb-section">
        <div class="container">
          <nav class="breadcrumb">
            <a href="/">Trang chủ</a>
            <span class="separator">›</span>
            <a href="/shop">Điện thoại</a>
            <span class="separator">›</span>
            <span class="current">{{ product.tenSanPham }}</span>
          </nav>
        </div>
      </div>

      <!-- Main Product Info -->
      <div class="product-main">
        <div class="container">
          <div class="product-grid">
            <!-- Left: Image Gallery -->
            <div class="product-gallery">
              <!-- Warranty Badge -->

              <!-- Main Image -->
              <div class="main-image-wrapper">
                <img
                  :src="getMainImageUrl()"
                  :alt="product.tenSanPham"
                  class="main-image"
                  @click="openImageModal"
                />
              </div>

              <!-- Thumbnails with Library Button -->
              <div v-if="productImages.length > 1" class="thumbnails-section">
                <button class="library-btn">
                  <span class="library-icon">📷</span>
                  <span class="library-text">Thư viện</span>
                </button>
                <div class="thumbnails">
                  <button
                    v-for="(image, index) in productImages"
                    :key="index"
                    @click="selectImage(index)"
                    :class="['thumbnail-btn', { active: index === selectedImageIndex }]"
                  >
                    <img :src="createFullImageUrl(image)" :alt="`Ảnh ${index + 1}`" />
                  </button>
                </div>
              </div>

              <!-- Promotional Banner -->
            </div>

            <!-- Right: Product Info -->
            <div class="product-info">
              <!-- Product Title and Favorite Button -->
              <div class="product-title-row">
                <h1 class="product-title">{{ product.tenSanPham }}</h1>
                <button
                  @click="toggleFavorite"
                  class="btn-favorite"
                  :class="{ active: isFavorite }"
                  :title="isFavorite ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'"
                >
                  <i :class="isFavorite ? 'bi bi-heart-fill' : 'bi bi-heart'"></i>
                </button>
              </div>

              <!-- Rating and Specs Link -->
              <div class="rating-specs">
                <div class="rating-wrapper">
                  <div class="stars">
                    <span
                      v-for="i in 5"
                      :key="i"
                      class="star"
                      :class="{ filled: i <= Math.round(computedAverageRating || 0) }"
                      >★</span
                    >
                  </div>
                  <span class="rating-count">({{ reviewsWithRating.length }} đánh giá)</span>
                </div>
                <div class="specs-compare-buttons">
                  <a href="#" @click.prevent="showSpecsModal = true" class="specs-link">
                    <i class="bi bi-cpu"></i>
                    <span>Thông số</span>
                  </a>
                  <div class="button-divider"></div>
                  <a href="#" @click.prevent="openCompareModal" class="compare-link">
                    <i class="bi bi-plus-circle"></i>
                    <span>So sánh</span>
                  </a>
                </div>
              </div>

              <!-- Price Section -->
              <div class="price-section">
                <div class="price-wrapper">
                  <div class="current-price">
                    {{ currentVariant ? formatPrice(currentPrice) : 'Liên hệ' }}
                  </div>
                  <span v-if="originalPrice" class="old-price">
                    {{ formatPrice(originalPrice) }}
                  </span>
                </div>
                <div class="installment-info">
                  <button class="installment-btn">Trả góp 0%</button>
                  <span class="installment-text">Trả góp chỉ từ 0₫</span>
                </div>
              </div>

              <!-- Storage Options -->
              <div v-if="availableStorage.length > 0" class="version-section">
                <div class="section-title">Phiên bản khác</div>
                <div class="version-buttons">
                  <button
                    v-for="storage in availableStorage"
                    :key="storage.id"
                    @click="selectStorage(storage.id)"
                    :class="['version-btn', { active: selectedStorage === storage.id }]"
                  >
                    <div class="version-name">{{ storage.name }}</div>
                    <div class="version-price-wrapper">
                      <div class="version-price">
                        {{ formatPrice(getStoragePriceAfterDiscount(storage.id)) }}
                      </div>
                      <div
                        v-if="getStorageOriginalPrice(storage.id) !== null"
                        class="version-old-price"
                      >
                        {{ formatPrice(getStorageOriginalPrice(storage.id)!) }}
                      </div>
                    </div>
                    <div v-if="selectedStorage === storage.id" class="checkmark">✓</div>
                  </button>
                </div>
              </div>

              <!-- Color Options -->
              <div v-if="availableColors.length > 0" class="color-section">
                <div class="section-title">Màu sắc:</div>
                <div class="color-buttons">
                  <button
                    v-for="color in availableColors"
                    :key="color.id"
                    @click="selectColor(color.id)"
                    :class="['color-btn', { active: selectedColor === color.id }]"
                  >
                    <div class="color-preview">
                      <img :src="getColorImage(color.id)" :alt="color.name" />
                    </div>
                    <div class="color-info">
                      <div class="color-name">{{ color.name }}</div>
                      <div class="color-price-wrapper">
                        <div class="color-price">
                          {{ formatPrice(getColorPriceAfterDiscount(color.id)) }}
                        </div>
                        <div
                          v-if="getColorOriginalPrice(color.id) !== null"
                          class="color-old-price"
                        >
                          {{ formatPrice(getColorOriginalPrice(color.id)!) }}
                        </div>
                      </div>
                    </div>
                    <div v-if="selectedColor === color.id" class="checkmark">✓</div>
                  </button>
                </div>
              </div>

              <!-- Warranty Options -->
              <div class="warranty-section">
                <div class="section-title">Bảo Hành</div>
                <div class="warranty-buttons">
                  <button class="warranty-btn active">
                    <div class="warranty-info">
                      <div class="warranty-name">1 đổi 1 12 tháng</div>
                      <div class="warranty-price">Miễn phí</div>
                    </div>
                    <div class="checkmark">✓</div>
                  </button>
                  <button class="warranty-btn">
                    <div class="warranty-info">
                      <div class="warranty-name">1 đổi 1 24 tháng</div>
                      <div class="warranty-price">+ 1.200.000 ₫</div>
                    </div>
                  </button>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                <button @click="openConfirm('buy')" :disabled="!isInStock" class="btn-buy-now">
                  {{ isInStock ? 'MUA NGAY' : 'HẾT HÀNG' }}
                </button>
                <button @click="openConfirm('add')" :disabled="!isInStock" class="btn-add-cart">
                  Thêm vào giỏ hàng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Details Tabs -->
      <div class="product-details-section">
        <div class="container">
          <div class="details-layout">
            <!-- Main Content -->
            <div class="details-main">
              <!-- Description -->
              <div class="detail-block">
                <h2 class="block-title">Mô tả sản phẩm</h2>
                <div class="block-content">
                  <p v-if="product.moTa">{{ product.moTa }}</p>
                  <p v-else>
                    {{ product.tenSanPham }} là sản phẩm chính hãng, mới 100%, nguyên seal với đầy
                    đủ phụ kiện từ nhà sản xuất.
                  </p>
                </div>
              </div>

              <!-- Specifications -->
              <div class="detail-block">
                <h2 class="block-title">Thông số kỹ thuật</h2>
                <div class="specs-table">
                  <div class="spec-row">
                    <span class="spec-label">Kích thước màn hình</span>
                    <span class="spec-value">{{ product.tenManHinh || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Công nghệ màn hình</span>
                    <span class="spec-value">{{ product.tenManHinh || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Camera sau</span>
                    <span class="spec-value">{{ product.tenCameraSau || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Camera trước</span>
                    <span class="spec-value">{{ product.tenCameraTruoc || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Chipset</span>
                    <span class="spec-value">{{ product.tenChip || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">CPU</span>
                    <span class="spec-value">{{ product.tenCpu || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">GPU</span>
                    <span class="spec-value">{{ product.tenGpu || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">RAM</span>
                    <span class="spec-value">{{ currentVariant?.tenRam || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Bộ nhớ trong</span>
                    <span class="spec-value">{{ currentVariant?.tenRom || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Pin</span>
                    <span class="spec-value">{{ product.tenPin || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">Hệ điều hành</span>
                    <span class="spec-value">{{ product.tenHeDieuHanh || 'Đang cập nhật' }}</span>
                  </div>
                  <div class="spec-row">
                    <span class="spec-label">SIM</span>
                    <span class="spec-value">{{ product.tenSim || 'Đang cập nhật' }}</span>
                  </div>
                </div>
              </div>

              <!-- Reviews -->
              <div class="detail-block">
                <h2 class="block-title">Đánh giá sản phẩm</h2>

                <!-- Rating Summary -->
                <div class="rating-summary">
                  <div class="rating-score">
                    <div
                      class="score-number"
                      :class="{
                        'no-rating': !computedAverageRating || computedAverageRating === 0,
                      }"
                    >
                      {{ computedAverageRating?.toFixed(1) || '0.0' }}
                    </div>
                    <div class="score-stars">
                      <span
                        v-for="i in 5"
                        :key="i"
                        class="star"
                        :class="{ filled: i <= Math.round(computedAverageRating || 0) }"
                        >★</span
                      >
                    </div>
                    <div class="score-count">{{ reviewsWithRating.length }} đánh giá</div>
                  </div>
                  <div class="rating-bars">
                    <div class="rating-bar-item">
                      <span>5 Sao</span>
                      <div class="bar">
                        <div
                          class="bar-fill"
                          :style="{ width: getRatingPercentage(5) + '%' }"
                        ></div>
                      </div>
                      <span class="bar-count">{{ getRatingCount(5) }}</span>
                    </div>
                    <div class="rating-bar-item">
                      <span>4 Sao</span>
                      <div class="bar">
                        <div
                          class="bar-fill"
                          :style="{ width: getRatingPercentage(4) + '%' }"
                        ></div>
                      </div>
                      <span class="bar-count">{{ getRatingCount(4) }}</span>
                    </div>
                    <div class="rating-bar-item">
                      <span>3 Sao</span>
                      <div class="bar">
                        <div
                          class="bar-fill"
                          :style="{ width: getRatingPercentage(3) + '%' }"
                        ></div>
                      </div>
                      <span class="bar-count">{{ getRatingCount(3) }}</span>
                    </div>
                    <div class="rating-bar-item">
                      <span>2 Sao</span>
                      <div class="bar">
                        <div
                          class="bar-fill"
                          :style="{ width: getRatingPercentage(2) + '%' }"
                        ></div>
                      </div>
                      <span class="bar-count">{{ getRatingCount(2) }}</span>
                    </div>
                    <div class="rating-bar-item">
                      <span>1 Sao</span>
                      <div class="bar">
                        <div
                          class="bar-fill"
                          :style="{ width: getRatingPercentage(1) + '%' }"
                        ></div>
                      </div>
                      <span class="bar-count">{{ getRatingCount(1) }}</span>
                    </div>
                  </div>
                </div>

                <!-- Review Form - Chỉ bình luận, không có rating -->
                <div class="review-form">
                  <h3 class="form-title">Bình luận</h3>
                  <form @submit.prevent="submitReview">
                    <div class="form-row" v-if="!isLoggedIn">
                      <label>Tên của bạn:</label>
                      <input
                        v-model="reviewForm.tenNguoiDung"
                        type="text"
                        required
                        placeholder="Nhập tên của bạn"
                      />
                    </div>
                    <div class="form-row">
                      <textarea
                        v-model="reviewForm.comment"
                        required
                        placeholder="Nhập đánh giá ở đây"
                        rows="4"
                      ></textarea>
                    </div>
                    <button type="submit" :disabled="isSubmittingReview" class="btn-submit-review">
                      {{ isSubmittingReview ? 'Đang gửi...' : 'Gửi bình luận' }}
                    </button>
                  </form>
                </div>

                <!-- Reviews List -->
                <div class="reviews-list">
                  <div v-if="reviews.length === 0" class="no-reviews">
                    <p>Chưa có bình luận nào. Hãy là người đầu tiên bình luận về sản phẩm này!</p>
                  </div>
                  <div v-for="review in reviews" :key="review.reviewId" class="review-item">
                    <div class="review-header">
                      <div
                        class="reviewer-avatar"
                        :style="{ background: getAvatarColor(review.tenNguoiDung) }"
                      >
                        {{ getInitials(review.tenNguoiDung) }}
                      </div>
                      <div class="reviewer-info">
                        <h4>{{ review.tenNguoiDung }}</h4>
                        <div v-if="review.rating" class="review-rating-section">
                          <div class="review-stars">
                            <span
                              v-for="i in 5"
                              :key="i"
                              class="star"
                              :class="{ filled: i <= review.rating }"
                              >★</span
                            >
                          </div>
                          <span class="rating-text">{{ getRatingText(review.rating) }}</span>
                        </div>
                        <!-- Tags mô tả - hiển thị nếu có rating -->
                        <div v-if="review.rating && review.rating >= 4" class="review-tags">
                          <span class="review-tag">Hiệu năng Siêu mạnh mẽ</span>
                          <span class="review-tag">Thời lượng pin Cực khủng</span>
                          <span class="review-tag">Chất lượng camera Chụp đẹp, chuyên nghiệp</span>
                        </div>
                      </div>
                      <span class="review-date">{{ formatReviewDate(review.ngayTao) }}</span>
                    </div>
                    <p class="review-text">{{ review.comment }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Sidebar -->
            <div class="details-sidebar">
              <!-- Policy Box -->
              <div class="policy-box">
                <h3 class="policy-title">Chính sách bán hàng</h3>
                <ul class="policy-list">
                  <li>
                    <span class="policy-icon">✓</span>
                    <span>Bảo hành chính hãng 12 tháng</span>
                  </li>
                  <li>
                    <span class="policy-icon">✓</span>
                    <span>1 đổi 1 trong 30 ngày nếu có lỗi từ NSX</span>
                  </li>
                  <li>
                    <span class="policy-icon">✓</span>
                    <span>Giao hàng toàn quốc</span>
                  </li>
                  <li>
                    <span class="policy-icon">✓</span>
                    <span>Thu cũ đổi mới - Giá cao nhất</span>
                  </li>
                </ul>
              </div>

              <!-- Hotline Box -->
              <div class="hotline-box">
                <h3 class="hotline-title">Tổng đài hỗ trợ</h3>
                <div class="hotline-item">
                  <div class="hotline-label">Hotline bán hàng:</div>
                  <div class="hotline-number">1900.633.471</div>
                </div>
                <div class="hotline-item">
                  <div class="hotline-label">Hotline bảo hành:</div>
                  <div class="hotline-number">024.6683.9292</div>
                </div>
                <div class="hotline-item">
                  <div class="hotline-label">Thời gian:</div>
                  <div class="hotline-number">8:00 - 21:00</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <section v-if="relatedProducts.length > 0" class="related-products">
        <div class="related-container">
          <h3 class="related-title">Có thể bạn quan tâm</h3>
          <div class="related-wrapper">
            <button
              class="scroll-btn scroll-left"
              @click="scrollLeft"
              :disabled="scrollPosition === 0"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            <div class="related-scroll-container" ref="scrollContainer">
              <div class="related-grid">
                <div
                  v-for="(relatedProduct, index) in relatedProducts"
                  :key="relatedProduct.id || index"
                  class="related-card"
                >
                  <img
                    :src="getRelatedProductImage(relatedProduct)"
                    :alt="relatedProduct.tenSanPham || relatedProduct.ten"
                    class="related-image"
                    @error="
                      (e) => {
                        ;(e.target as HTMLImageElement).src = createPlaceholderImage('No Image')
                      }
                    "
                  />
                  <h4 class="related-name">
                    {{ relatedProduct.tenSanPham || relatedProduct.ten }}
                  </h4>
                  <p class="related-price">
                    {{
                      relatedProduct.gia
                        ? formatPrice(relatedProduct.gia)
                        : relatedProduct.variants?.[0]
                          ? formatPrice(relatedProduct.variants[0].donGia)
                          : 'Liên hệ'
                    }}
                  </p>
                  <button
                    class="related-btn"
                    @click="
                      viewProduct(
                        relatedProduct.id ||
                          relatedProduct.sanPhamId ||
                          relatedProduct.chiTietSanPhamId,
                      )
                    "
                  >
                    Xem chi tiết
                  </button>
                </div>
              </div>
            </div>
            <button class="scroll-btn scroll-right" @click="scrollRight" :disabled="isScrollAtEnd">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- Image Modal -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <div class="modal-wrapper" @click.stop>
        <button @click="closeImageModal" class="modal-close">&times;</button>
        <img
          :src="createFullImageUrl(productImages[selectedImageIndex])"
          :alt="product?.tenSanPham"
        />
        <div v-if="productImages.length > 1" class="modal-nav">
          <button @click="prevImage" class="nav-btn prev">‹</button>
          <button @click="nextImage" class="nav-btn next">›</button>
        </div>
      </div>
    </div>

    <!-- Compare Modal -->
    <div v-if="showCompareModal" class="compare-modal" @click.self="closeCompareModal">
      <div class="compare-modal-content" @click.stop>
        <div class="compare-modal-header">
          <h2>So sánh sản phẩm</h2>
          <button @click="closeCompareModal" class="compare-modal-close">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="compare-modal-body">
          <div class="compare-products-grid">
            <div
              v-for="(compareProduct, index) in compareProducts"
              :key="index"
              class="compare-product-slot"
            >
              <div class="compare-slot-header">
                <h3>Sản phẩm {{ index + 1 }}</h3>
                <button
                  v-if="index > 0 && compareProduct.id"
                  @click="removeProduct(index)"
                  class="remove-product-btn"
                >
                  <i class="bi bi-x"></i>
                </button>
              </div>
              <div v-if="compareProduct.data" class="selected-product-card">
                <img
                  :src="createFullImageUrl(compareProduct.data.hinhAnh)"
                  :alt="compareProduct.data.tenSanPham"
                />
                <h4>{{ compareProduct.data.tenSanPham }}</h4>
                <div class="product-specs-mini">
                  <span v-if="compareProduct.data.tenRam">{{ compareProduct.data.tenRam }}</span>
                  <span v-if="compareProduct.data.tenRom">{{ compareProduct.data.tenRom }}</span>
                  <span v-if="compareProduct.data.tenMauSac">{{
                    compareProduct.data.tenMauSac
                  }}</span>
                </div>
                <div class="product-price-mini">{{ formatPrice(compareProduct.data.gia) }}</div>
              </div>
              <div v-else class="product-selector">
                <div class="product-search-wrapper">
                  <div class="search-input-wrapper">
                    <i class="bi bi-search search-icon-input"></i>
                    <input
                      v-model="productSearchQuery[index]"
                      @input="searchProducts(index)"
                      @focus="toggleProductDropdown(index)"
                      type="text"
                      placeholder="Tìm sản phẩm muốn so sánh"
                      class="product-search-input"
                    />
                    <button @click="openFavoriteModal(index)" class="favorite-filter-btn">
                      <i class="bi bi-heart"></i>
                      <span>Yêu thích</span>
                    </button>
                  </div>
                  <div v-if="showProductDropdown === index" class="product-dropdown">
                    <div v-if="isLoadingProducts" class="loading-products">
                      Đang tải sản phẩm...
                    </div>
                    <div v-else-if="filteredProducts[index].length === 0" class="no-products">
                      Không tìm thấy sản phẩm
                    </div>
                    <div v-else class="product-list">
                      <div
                        v-for="product in filteredProducts[index]"
                        :key="product.id || product.chiTietSanPhamId"
                        class="product-item"
                        @click="selectProduct(index, product)"
                      >
                        <img :src="createFullImageUrl(product.hinhAnh)" :alt="product.tenSanPham" />
                        <div class="product-item-info">
                          <h4>{{ product.tenSanPham }}</h4>
                          <div class="product-item-price">
                            <span class="current-price">{{ formatPrice(product.gia) }}</span>
                          </div>
                        </div>
                        <button class="select-product-btn">Chọn</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="compare-modal-footer">
          <button @click="closeCompareModal" class="btn-cancel-compare">Hủy</button>
          <button
            @click="performCompare"
            class="btn-compare"
            :disabled="compareProducts.filter((cp) => cp.id).length < 2"
          >
            So sánh
          </button>
        </div>
      </div>
    </div>

    <!-- Favorite Products Modal -->
    <div v-if="showFavoriteModal" class="favorite-modal" @click.self="closeFavoriteModal">
      <div class="favorite-modal-content" @click.stop>
        <div class="favorite-modal-header">
          <h2>Sản phẩm yêu thích</h2>
          <button @click="closeFavoriteModal" class="favorite-modal-close">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="favorite-modal-body">
          <div v-if="favoriteProductsList.length === 0" class="empty-favorites-modal">
            <i class="bi bi-heart"></i>
            <p>Chưa có sản phẩm yêu thích</p>
          </div>
          <div v-else class="favorite-products-grid">
            <div
              v-for="product in favoriteProductsList"
              :key="product.id || product.chiTietSanPhamId"
              class="favorite-product-card"
            >
              <div class="favorite-product-image">
                <img :src="createFullImageUrl(product.hinhAnh)" :alt="product.tenSanPham" />
              </div>
              <div class="favorite-product-info">
                <h4>{{ product.tenSanPham }}</h4>
                <div
                  class="favorite-product-specs"
                  v-if="product.tenRam || product.tenRom || product.tenMauSac"
                >
                  <span v-if="product.tenRam">{{ product.tenRam }}</span>
                  <span v-if="product.tenRom">{{ product.tenRom }}</span>
                  <span v-if="product.tenMauSac">{{ product.tenMauSac }}</span>
                </div>
                <div class="favorite-product-price">{{ formatPrice(product.gia) }}</div>
                <button @click="selectFavoriteProduct(product)" class="btn-select-favorite">
                  Chọn
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Specs Modal -->
    <div v-if="showSpecsModal" class="specs-modal" @click="showSpecsModal = false">
      <div class="specs-modal-content" @click.stop>
        <div class="specs-modal-header">
          <div class="specs-header-left">
            <div class="specs-header-icon">
              <i class="bi bi-cpu"></i>
            </div>
            <div>
              <h2 class="specs-modal-title">Thông số kỹ thuật</h2>
              <p class="specs-modal-subtitle">{{ product?.tenSanPham }}</p>
            </div>
          </div>
          <button @click="showSpecsModal = false" class="specs-modal-close">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="specs-modal-body">
          <div class="specs-table-modal">
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-display"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Kích thước màn hình</span>
                <span class="spec-value-modal">{{ product?.tenManHinh || 'Đang cập nhật' }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-cpu-fill"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">CPU</span>
                <span class="spec-value-modal">{{ product?.tenCpu || 'Đang cập nhật' }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-phone"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Hệ điều hành</span>
                <span class="spec-value-modal">{{
                  product?.tenHeDieuHanh || 'Đang cập nhật'
                }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-hdd"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Bộ nhớ trong</span>
                <span class="spec-value-modal">{{
                  currentVariant?.tenRom ||
                  availableStorage.map((s) => s.name).join(', ') ||
                  'Đang cập nhật'
                }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-camera"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Camera chính</span>
                <span class="spec-value-modal">{{ product?.tenCameraSau || 'Đang cập nhật' }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-camera-video"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Camera trước</span>
                <span class="spec-value-modal">{{
                  product?.tenCameraTruoc || 'Đang cập nhật'
                }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-battery-full"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Dung lượng pin</span>
                <span class="spec-value-modal">{{ product?.tenPin || 'Đang cập nhật' }}</span>
              </div>
            </div>
            <div class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-palette"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">Màu sắc</span>
                <span class="spec-value-modal">{{
                  availableColors.map((c) => c.name).join(', ') || 'Đang cập nhật'
                }}</span>
              </div>
            </div>
            <div v-if="product?.tenRam" class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-memory"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">RAM</span>
                <span class="spec-value-modal">{{ product.tenRam }}</span>
              </div>
            </div>
            <div v-if="product?.tenSim" class="spec-row-modal">
              <div class="spec-icon-wrapper">
                <i class="bi bi-sim"></i>
              </div>
              <div class="spec-content-wrapper">
                <span class="spec-label-modal">SIM</span>
                <span class="spec-value-modal">{{ product.tenSim }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
    <!-- Confirm Modal -->
    <div v-if="confirmModal.show" class="confirm-overlay" @click="closeConfirm">
      <div class="confirm-card" @click.stop>
        <div class="confirm-header">
          <h3>Xác nhận {{ confirmModal.action === 'buy' ? 'mua ngay' : 'thêm vào giỏ' }}</h3>
          <button class="confirm-close" type="button" @click="closeConfirm">×</button>
        </div>
        <div class="confirm-content">
          <div class="confirm-row">
            <span class="label">Sản phẩm</span><span class="value">{{ product?.tenSanPham }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Phiên bản</span
            ><span class="value"
              >{{ currentVariant?.tenRom }} / {{ currentVariant?.tenMauSac }}</span
            >
          </div>
          <div class="confirm-row">
            <span class="label">Số lượng</span><span class="value">{{ quantity }}</span>
          </div>
          <div class="confirm-divider"></div>
          <div class="confirm-row total">
            <span class="label">Giá</span
            ><span class="value">{{
              currentVariant ? formatPrice(currentPrice * quantity) : '—'
            }}</span>
          </div>
        </div>
        <div class="confirm-actions">
          <button
            type="button"
            class="btn-cancel"
            @click="closeConfirm"
            :disabled="confirmModal.loading"
          >
            Hủy
          </button>
          <button
            type="button"
            class="btn-confirm"
            @click="confirmProceed"
            :disabled="confirmModal.loading"
          >
            <i v-if="confirmModal.loading" class="bi bi-hourglass-split"></i>
            {{ confirmModal.action === 'buy' ? 'Xác nhận mua ngay' : 'Xác nhận thêm giỏ' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Variables - Clickbuy Colors */
:root {
  --primary-color: #27ae60;
  --primary-hover: #229954;
  --secondary-color: #e74c3c;
  --text-dark: #2c3e50;
  --text-gray: #7f8c8d;
  --border-color: #ecf0f1;
  --bg-light: #f8f9fa;
}

/* Base Styles */
.clickbuy-product-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #2c3e50;
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
  min-height: 100vh;
  padding-top: 0;
}

/* Add top margin to account for fixed header */
.clickbuy-product-page {
  margin-top: 100px; /* Đảm bảo không bị chồm vào header */
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Loading & Error States */
.loading-wrapper,
.error-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #27ae60;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.btn-back {
  padding: 0.875rem 2rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-back:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

/* Breadcrumb */
.breadcrumb-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 1rem 0;
  border-bottom: 2px solid #e9ecef;
  margin-top: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb a {
  color: #7f8c8d;
  text-decoration: none;
  transition: color 0.3s;
}

.breadcrumb a:hover {
  color: #27ae60;
}

.breadcrumb .separator {
  color: #7f8c8d;
}

.breadcrumb .current {
  color: #2c3e50;
  font-weight: 500;
}

/* Main Product Section */
.product-main {
  padding: 2.5rem 0;
  background: transparent;
}

.product-grid {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 32px;
}

/* Product Gallery */
.product-gallery {
  position: relative;
}

.warranty-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #e74c3c;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
}

.badge-text {
  font-size: 12px;
  font-weight: 700;
  line-height: 1.1;
}

.badge-duration {
  font-size: 12px;
  font-weight: 700;
  line-height: 1.1;
}

.main-image-wrapper {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 1rem;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main-image {
  width: 100%;
  height: 300px;
  object-fit: contain;
  cursor: pointer;
  transition: transform 0.3s;
}

.main-image:hover {
  transform: scale(1.02);
}

.thumbnails-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.library-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
  color: #6c757d;
}

.library-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.library-icon {
  font-size: 16px;
}

.library-text {
  font-weight: 500;
}

.thumbnails {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  flex: 1;
}

.thumbnail-btn {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border: 2px solid #dee2e6;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  padding: 0;
}

.thumbnail-btn:hover {
  border-color: #ff5500;
  transform: scale(1.05);
}

.thumbnail-btn.active {
  border-color: #ff5500;
  box-shadow: 0 0 0 2px rgba(255, 85, 0, 0.3);
}

.thumbnail-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Promotional Banner */
.promo-banner {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.promo-content {
  flex: 1;
}

.promo-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.promo-dates {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.promo-offers {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.offer-item {
  font-size: 14px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
}

.promo-warranty {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
}

.promo-tagline {
  font-size: 14px;
  font-weight: 700;
}

.promo-image {
  margin-left: 16px;
}

.student-avatar {
  font-size: 48px;
}

/* Product Info */
.product-info {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.product-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.product-title {
  font-size: 2.25rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  line-height: 1.3;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  flex: 1;
}

.btn-favorite {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 2px solid #dee2e6;
  background: white;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 24px;
  flex-shrink: 0;
}

.btn-favorite:hover {
  border-color: #ff5500;
  color: #ff5500;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.2);
}

.btn-favorite.active {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  border-color: #ff5500;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.4);
}

.btn-favorite.active:hover {
  transform: scale(1.15);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.5);
}

.rating-specs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.specs-compare-buttons {
  display: flex;
  align-items: center;
  gap: 0;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 4px;
  border: 1px solid #e9ecef;
}

.specs-link,
.compare-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: #007bff;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border-radius: 6px;
  white-space: nowrap;
}

.specs-link:hover,
.compare-link:hover {
  background: #e7f3ff;
  color: #0056b3;
}

.specs-link i,
.compare-link i {
  font-size: 1.1rem;
}

.button-divider {
  width: 1px;
  height: 24px;
  background: #dee2e6;
  margin: 0 4px;
}

.rating-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 16px;
  color: #ddd;
  transition: color 0.3s;
}

.star.filled {
  color: #f39c12;
}

.rating-count {
  color: #7f8c8d;
  font-size: 14px;
}

.specs-link {
  color: #ff5500;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.specs-link:hover {
  color: #dc143c;
  text-decoration: underline;
}

.price-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.current-price {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.old-price {
  font-size: 20px;
  color: #999;
  text-decoration: line-through;
  font-weight: 500;
}

.installment-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.installment-btn {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.3);
}

.installment-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.4);
}

.installment-text {
  font-size: 12px;
  color: #7f8c8d;
}

/* Version Section */
.version-section,
.color-section,
.warranty-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #2c3e50 !important;
  margin-bottom: 12px !important;
  text-align: left !important;
}

.version-buttons {
  display: flex !important;
  flex-wrap: wrap !important;
  gap: 8px !important;
}

.color-buttons {
  display: flex !important;
  flex-direction: row !important;
  gap: 8px !important;
  flex-wrap: wrap !important;
}

.warranty-buttons {
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
}

.version-btn {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 1.25rem 1rem !important;
  border: 2px solid #e9ecef !important;
  border-radius: 12px !important;
  background: white !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  min-width: 90px !important;
  flex: 1 !important;
  max-width: 130px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.color-btn {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 1rem !important;
  border: 2px solid #e9ecef !important;
  border-radius: 12px !important;
  background: white !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  min-width: 100px !important;
  max-width: 120px !important;
  flex: 1 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.warranty-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.version-btn:hover,
.color-btn:hover,
.warranty-btn:hover {
  border-color: #ff5500;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.2);
}

.version-btn.active,
.color-btn.active,
.warranty-btn.active {
  border-color: #ff5500 !important;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%) !important;
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.2) !important;
  transform: translateY(-2px);
}

.version-name {
  font-size: 16px !important;
  font-weight: 700 !important;
  color: #2c3e50 !important;
  margin-bottom: 4px !important;
  text-align: center !important;
}

.version-price-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.version-price {
  font-size: 0.95rem !important;
  font-weight: 700 !important;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center !important;
}

.version-old-price {
  font-size: 11px !important;
  color: #999 !important;
  text-decoration: line-through !important;
  text-align: center !important;
}

.warranty-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.warranty-price {
  font-size: 0.95rem;
  font-weight: 700;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.color-preview {
  width: 60px !important;
  height: 60px !important;
  border-radius: 6px !important;
  overflow: hidden !important;
  margin-bottom: 8px !important;
}

.color-preview img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

.color-info {
  text-align: center !important;
}

.color-name {
  font-size: 12px !important;
  font-weight: 600 !important;
  color: #2c3e50 !important;
  margin-bottom: 4px !important;
  line-height: 1.2 !important;
}

.color-price-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.color-price {
  font-size: 0.85rem !important;
  font-weight: 700 !important;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.color-old-price {
  font-size: 9px !important;
  color: #999 !important;
  text-decoration: line-through !important;
}

.warranty-info {
  flex: 1;
}

.checkmark {
  position: absolute !important;
  top: 6px !important;
  right: 6px !important;
  width: 20px !important;
  height: 20px !important;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%) !important;
  color: white !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.4);
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-buy-now {
  flex: 2;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%) !important;
  color: white;
  border: none;
  padding: 1.25rem 2rem;
  border-radius: 15px;
  font-size: 1.15rem;
  font-weight: 700;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
  letter-spacing: 0.5px;
}

.btn-buy-now:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%) !important;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 85, 0, 0.4);
}

.btn-buy-now:disabled {
  background: #95a5a6;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-add-cart {
  flex: 1;
  background: white;
  color: #ff5500;
  border: 2px solid #ff5500 !important;
  padding: 1.25rem 2rem;
  border-radius: 15px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.2);
}

.btn-add-cart:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%) !important;
  color: white;
  border-color: #ff5500 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-add-cart:disabled {
  border-color: #95a5a6;
  color: #95a5a6;
  cursor: not-allowed;
}

/* Promotion Box */
.promotion-box {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.promo-title {
  font-size: 16px;
  font-weight: 700;
  color: #856404;
  margin-bottom: 12px;
}

.promo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.promo-list li {
  padding: 6px 0;
  color: #856404;
  font-size: 14px;
  line-height: 1.5;
}

/* Option Groups */
.option-group {
  margin-bottom: 20px;
}

.option-label {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.storage-buttons,
.color-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.storage-btn {
  padding: 12px 20px;
  border: 2px solid #ecf0f1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
  min-width: 100px;
  text-align: center;
}

.storage-btn:hover {
  border-color: #27ae60;
}

.storage-btn.active {
  border-color: #27ae60;
  background: #27ae60;
  color: white;
}

.color-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 2px solid #ecf0f1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.color-btn:hover {
  border-color: #27ae60;
}

.color-btn.active {
  border-color: #27ae60;
  background: #e8f5e9;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #ddd;
}

.color-name {
  font-weight: 500;
  font-size: 14px;
}

/* Quantity */
.quantity-group {
  margin-bottom: 20px;
}

.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #ecf0f1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 20px;
  font-weight: 600;
  transition: all 0.3s;
}

.qty-btn:hover {
  border-color: #27ae60;
  color: #27ae60;
}

.qty-input {
  width: 80px;
  height: 40px;
  border: 2px solid #ecf0f1;
  border-radius: 6px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.qty-input:focus {
  outline: none;
  border-color: #27ae60;
}

.stock-info {
  color: #7f8c8d;
  font-size: 14px;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.btn-buy-now {
  flex: 2;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  text-transform: uppercase;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.btn-buy-now:hover:not(:disabled) {
  background: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
}

.btn-buy-now:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.btn-subtitle {
  font-size: 11px;
  font-weight: 400;
  opacity: 0.9;
  text-transform: none;
}

.btn-add-cart {
  flex: 1;
  background: white;
  color: #27ae60;
  border: 2px solid #27ae60;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-add-cart:hover:not(:disabled) {
  background: #27ae60;
  color: white;
}

.btn-add-cart:disabled {
  border-color: #95a5a6;
  color: #95a5a6;
  cursor: not-allowed;
}

/* Support Box */
.support-box {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
}

.support-item {
  flex: 1;
  display: flex;
  gap: 12px;
  align-items: center;
}

.support-icon {
  font-size: 32px;
}

.support-label {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.support-value {
  font-size: 16px;
  font-weight: 700;
  color: #e74c3c;
}

/* Product Details Section */
.product-details-section {
  background: white;
  padding: 32px 0;
}

.details-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.details-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-block {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.block-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 1.5rem 0;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.block-title::before {
  content: '';
  width: 4px;
  height: 2rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  border-radius: 2px;
}

.block-content {
  line-height: 1.8;
  color: #7f8c8d;
}

/* Specifications Table */
.specs-table {
  border: 1px solid #e9ecef;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.spec-row {
  display: flex;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.spec-row:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  transform: translateX(4px);
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  flex: 1;
  font-weight: 600;
  color: #2c3e50;
}

.spec-value {
  flex: 2;
  color: #7f8c8d;
}

/* Rating Summary */
.rating-summary {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 2rem;
  padding: 2rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.rating-score {
  text-align: center;
}

.score-number {
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
}

.score-number.no-rating {
  color: #dc3545;
  -webkit-text-fill-color: #dc3545;
  background: none;
}

.score-stars {
  margin-bottom: 8px;
}

.score-count {
  color: #7f8c8d;
  font-size: 14px;
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.rating-bar-item > span:first-child {
  width: 50px;
  color: #7f8c8d;
}

.bar {
  flex: 1;
  height: 8px;
  background: #ecf0f1;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #f39c12;
  transition: width 0.5s;
}

.bar-count {
  width: 40px;
  text-align: right;
  color: #7f8c8d;
}

/* Review Form */
.review-form {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 2rem;
  border-radius: 16px;
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.form-row {
  margin-bottom: 16px;
}

.form-row label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

.form-row input,
.form-row textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #ecf0f1;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: #27ae60;
}

.rating-stars {
  display: flex;
  gap: 4px;
}

.rating-stars .star {
  font-size: 28px;
  cursor: pointer;
  transition: all 0.3s;
}

.rating-stars .star:hover {
  transform: scale(1.2);
}

.btn-submit-review {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  padding: 1rem 2.5rem;
  border-radius: 12px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-submit-review:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-submit-review:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

/* Reviews List */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 1.5rem;
  border-radius: 16px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.review-item:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  min-width: 0;
}

.reviewer-info h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #333;
}

.review-rating-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.review-stars {
  display: flex;
  gap: 2px;
}

.review-stars .star {
  font-size: 16px;
  color: #ddd;
}

.review-stars .star.filled {
  color: #ffd700;
}

.rating-text {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.review-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.review-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.review-date {
  margin-left: auto;
  color: #7f8c8d;
  font-size: 13px;
  white-space: nowrap;
  flex-shrink: 0;
}

.user-info-display {
  padding: 0.5rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.user-info-display strong {
  color: #333;
  font-size: 0.95rem;
}

.form-note {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #e7f3ff;
  border-radius: 8px;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #0066cc;
}

.form-note i {
  font-size: 1rem;
  margin-top: 2px;
}

.no-reviews {
  text-align: center;
  padding: 2rem;
  color: #7f8c8d;
  font-size: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px dashed #dee2e6;
}

.review-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

/* Details Sidebar */
.details-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.policy-box,
.hotline-box {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.policy-title,
.hotline-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.policy-title::before,
.hotline-title::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  border-radius: 2px;
}

.policy-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.policy-list li {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #ecf0f1;
  font-size: 14px;
  line-height: 1.5;
}

.policy-list li:last-child {
  border-bottom: none;
}

.policy-icon {
  color: #ff5500;
  font-weight: 700;
  font-size: 1.1rem;
}

.hotline-item {
  padding: 12px 0;
  border-bottom: 1px solid #ecf0f1;
}

.hotline-item:last-child {
  border-bottom: none;
}

.hotline-label {
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 4px;
}

.hotline-number {
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Related Products Section - Giống CartPage */
.related-products {
  background: #f8f9fa !important;
  padding: 3rem 0 !important;
  margin-top: 3rem !important;
}

.related-container {
  max-width: 1200px !important;
  margin: 0 auto !important;
  padding: 0 2rem !important;
}

.related-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 2rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.related-wrapper {
  width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.scroll-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  border: 2px solid #ff5500;
  color: #ff5500;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.scroll-btn:hover:not(:disabled) {
  background: #ff5500;
  color: white;
  transform: translateY(-50%) scale(1.1);
}

.scroll-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.scroll-left {
  left: -25px;
}

.scroll-right {
  right: -25px;
}

.related-scroll-container {
  overflow-x: auto;
  overflow-y: hidden;
  padding: 1rem 0;
  scroll-behavior: smooth;
  flex: 1;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.related-scroll-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.related-grid {
  display: flex;
  gap: 1.5rem;
  min-width: max-content;
}

.related-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
  text-align: center;
  flex: 0 0 280px;
  width: 280px;
  min-width: 280px;
  max-width: 280px;
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: #ff5500;
}

.related-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 10px;
  background: #f9f9f9;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.related-card:hover .related-image {
  transform: scale(1.05);
}

.related-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0.5rem 0;
  flex-shrink: 0;
}

.related-price {
  font-size: 1.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0.5rem 0;
  flex-shrink: 0;
}

.related-btn {
  width: 100%;
  padding: 0.875rem 1.25rem;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: auto;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.related-btn:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

/* Image Modal */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-wrapper {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.modal-wrapper img {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
}

.modal-close {
  position: absolute;
  top: -50px;
  right: 0;
  background: transparent;
  border: none;
  color: white;
  font-size: 48px;
  cursor: pointer;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.modal-close:hover {
  transform: scale(1.2);
}

.modal-nav {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  pointer-events: none;
}

.nav-btn {
  pointer-events: all;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;
  font-size: 48px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.3s;
}

.nav-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.nav-btn.prev {
  margin-left: -80px;
}

.nav-btn.next {
  margin-right: -80px;
}

/* Confirm Modal */
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 16px;
}
.confirm-card {
  width: 100%;
  max-width: 520px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  border: 1px solid #e9ecef;
}
.confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.confirm-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #333;
}
.confirm-close {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #666;
}
.confirm-content {
  padding: 16px 20px;
}
.confirm-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  color: #555;
}
.confirm-row .label {
  font-weight: 600;
}
.confirm-row.total {
  font-size: 1.1rem;
  color: #333;
}
.confirm-divider {
  height: 1px;
  background: #eee;
  margin: 8px 0 4px;
}
.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px 20px;
}
.btn-cancel {
  background: #6c757d;
  color: #fff;
  border: none;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
}
.btn-confirm {
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: #fff;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}
.btn-confirm:hover {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

/* Specs Modal */
.specs-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s;
}

.specs-modal-content {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid #e9ecef;
}

.specs-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
}

.specs-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.specs-header-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.specs-modal-title {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.specs-modal-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0.25rem 0 0 0;
}

.specs-modal-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
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

.specs-modal-close:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.specs-modal-body {
  padding: 1.5rem 2rem;
  max-height: calc(80vh - 120px);
  overflow-y: auto;
}

.specs-table-modal {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.spec-row-modal {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #ff5500;
  transition: all 0.3s ease;
}

.spec-row-modal:hover {
  background: #e9ecef;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.spec-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.spec-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.spec-label-modal {
  font-size: 13px;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.spec-value-modal {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  font-weight: 500;
}

.spec-value-modal {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 700;
  text-align: right;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .clickbuy-product-page {
    margin-top: 77px; /* Keep header margin on tablet */
  }

  .product-grid {
    grid-template-columns: 1fr;
  }

  .details-layout {
    grid-template-columns: 1fr;
  }

  .details-sidebar {
    order: 1;
  }

  .details-main {
    order: 2;
  }

  .related-card {
    flex: 0 0 250px;
    width: 250px;
    min-width: 250px;
    max-width: 250px;
    height: 380px;
    padding: 1rem;
  }

  .related-title {
    font-size: 1.5rem;
  }

  .related-image {
    height: 160px;
  }

  .scroll-btn {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .scroll-left {
    left: -20px;
  }

  .scroll-right {
    right: -20px;
  }

  .related-grid {
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .clickbuy-product-page {
    margin-top: 77px; /* Keep header margin on mobile */
  }

  .product-title {
    font-size: 20px;
  }

  .current-price {
    font-size: 24px;
  }

  .version-buttons {
    flex-wrap: wrap !important;
  }

  .version-btn {
    min-width: 70px !important;
    max-width: 100px !important;
  }

  .color-buttons {
    flex-direction: column !important;
  }

  .color-btn {
    min-width: 100% !important;
    max-width: 100% !important;
    flex-direction: row !important;
    justify-content: flex-start !important;
  }

  .color-preview {
    width: 40px !important;
    height: 40px !important;
    margin-right: 12px !important;
    margin-bottom: 0 !important;
  }

  .color-info {
    text-align: left !important;
  }

  .action-buttons {
    flex-direction: column !important;
  }

  .btn-buy-now,
  .btn-add-cart {
    flex: 1 !important;
    width: 100% !important;
  }

  .rating-summary {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .related-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .support-box {
    flex-direction: column;
  }

  .nav-btn.prev {
    margin-left: -40px;
  }

  .nav-btn.next {
    margin-right: -40px;
  }
}

@media (max-width: 480px) {
  .clickbuy-product-page {
    margin-top: 77px; /* Keep header margin on small mobile */
  }

  .main-image {
    height: 300px;
  }

  .product-title {
    font-size: 18px;
  }

  .current-price {
    font-size: 20px;
  }

  .version-buttons {
    flex-direction: column !important;
  }

  .version-btn {
    min-width: 100% !important;
    max-width: 100% !important;
  }

  .related-card {
    flex: 0 0 220px;
    width: 220px;
    min-width: 220px;
    max-width: 220px;
    height: 360px;
    padding: 0.75rem;
  }

  .related-image {
    height: 120px;
  }

  .related-title {
    font-size: 1.2rem;
  }

  .related-name {
    font-size: 1rem;
  }

  .related-price {
    font-size: 1.1rem;
  }

  .scroll-btn {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }

  .scroll-left {
    left: -15px;
  }

  .scroll-right {
    right: -15px;
  }

  .nav-btn {
    display: none;
  }
}

/* Compare Modal Styles */
.compare-modal {
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

.compare-modal-content {
  background: white;
  border-radius: 20px;
  max-width: 1200px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.compare-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #e9ecef;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

.compare-modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.compare-modal-close {
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

.compare-modal-close:hover {
  background: #e9ecef;
  color: #2c3e50;
}

.compare-modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.compare-products-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.compare-product-slot {
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 1rem;
  background: #f8f9fa;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.compare-slot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.compare-slot-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.remove-product-btn {
  background: #dc3545;
  border: none;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.3s ease;
}

.remove-product-btn:hover {
  background: #c82333;
  transform: scale(1.1);
}

.selected-product-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
}

.selected-product-card img {
  width: 100%;
  height: 150px;
  object-fit: contain;
  margin-bottom: 0.75rem;
  border-radius: 8px;
  background: #f8f9fa;
}

.selected-product-card h4 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-specs-mini {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.product-specs-mini span {
  font-size: 0.75rem;
  padding: 2px 6px;
  background: #e9ecef;
  border-radius: 4px;
  color: #6c757d;
}

.product-price-mini {
  font-size: 1rem;
  font-weight: 700;
  color: #dc3545;
  margin-top: auto;
}

.product-selector {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
}

.product-search-wrapper {
  position: relative;
  z-index: 1;
}

.search-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
  position: relative;
}

.search-icon-input {
  position: absolute;
  left: 12px;
  color: #6c757d;
  font-size: 1rem;
  pointer-events: none;
  z-index: 1;
}

.product-search-input {
  flex: 1;
  padding: 10px 12px 10px 36px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.product-search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.favorite-filter-btn {
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

.favorite-filter-btn:hover {
  border-color: #dc3545;
  color: #dc3545;
}

.favorite-filter-btn.active {
  background: #dc3545;
  border-color: #dc3545;
  color: white;
}

.favorite-filter-btn i {
  font-size: 1rem;
}

.product-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
  z-index: 10001;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.loading-products,
.no-products {
  padding: 2rem;
  text-align: center;
  color: #6c757d;
  font-size: 0.9rem;
}

.product-list {
  display: flex;
  flex-direction: column;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:hover {
  background: #f8f9fa;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border-radius: 6px;
  background: #f8f9fa;
  flex-shrink: 0;
}

.product-item-info {
  flex: 1;
  min-width: 0;
}

.product-item-info h4 {
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

.product-item-price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-item-price .current-price {
  font-size: 0.95rem;
  font-weight: 700;
  color: #dc3545;
}

.select-product-btn {
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

.select-product-btn:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.compare-modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 1.5rem 2rem;
  border-top: 2px solid #e9ecef;
  background: #f8f9fa;
}

.btn-cancel-compare {
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel-compare:hover {
  background: #5a6268;
}

.btn-compare {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff5500 0%, #dc143c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-compare:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc143c 0%, #ff5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-compare:disabled {
  background: #95a5a6;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 1024px) {
  .compare-products-grid {
    grid-template-columns: 1fr;
  }

  .compare-product-slot {
    min-height: 300px;
  }
}

/* Favorite Products Modal */
.favorite-modal {
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

.favorite-modal-content {
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

.favorite-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #e9ecef;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

.favorite-modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.favorite-modal-close {
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

.favorite-modal-close:hover {
  background: #e9ecef;
  color: #2c3e50;
}

.favorite-modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.empty-favorites-modal {
  text-align: center;
  padding: 3rem 2rem;
  color: #6c757d;
}

.empty-favorites-modal i {
  font-size: 4rem;
  color: #dee2e6;
  margin-bottom: 1rem;
  display: block;
}

.empty-favorites-modal p {
  font-size: 1.1rem;
  margin: 0;
}

.favorite-products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.favorite-product-card {
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  cursor: pointer;
}

.favorite-product-card:hover {
  border-color: #ff5500;
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.favorite-product-image {
  width: 100%;
  height: 180px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.favorite-product-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.favorite-product-info {
  padding: 1rem;
  background: white;
}

.favorite-product-info h4 {
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

.favorite-product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 0.5rem;
}

.favorite-product-specs span {
  font-size: 0.75rem;
  padding: 2px 6px;
  background: #e9ecef;
  border-radius: 4px;
  color: #6c757d;
}

.favorite-product-price {
  font-size: 1rem;
  font-weight: 700;
  color: #dc3545;
  margin-bottom: 0.75rem;
}

.btn-select-favorite {
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

.btn-select-favorite:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

@media (max-width: 768px) {
  .favorite-products-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
  }

  .favorite-product-image {
    height: 150px;
  }
}
</style>
