<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import * as XLSX from 'xlsx'

const router = useRouter()
const route = useRoute()

interface ChiTietThanhToan {
  id?: number
  soTien?: number
  maGiaoDich?: string
  ngayThanhToan?: string
  trangThai?: number
  loaiThanhToan?: string
  phuongThucThanhToan?: {
    id?: number
    tenPhuongThuc?: string
  }
}

interface HoaDon {
  id: number
  maHoaDon: string
  tenKhachHang?: string
  soDienThoai?: string
  email?: string
  loaiHoaDon: string
  tongTien: number
  tongTienSauGiam?: number
  trangThai: number
  ngayTao: string
  phieuGiamGiaId?: number
  ghiChu?: string
  phuongThucThanhToan?: string
  phuongThucNhanHang?: string
  phiVanChuyen?: number
  nhanVienId?: number
  tenNhanVien?: string
  maNhanVien?: string
  lichSuThanhToan?: ChiTietThanhToan[]
}


// Reactive data
const activeTab = ref('list')
const hoaDons = ref<HoaDon[]>([])
const loading = ref(false)
const toastRef = ref(null)
const paymentHistoryCache = ref<Map<number, ChiTietThanhToan[]>>(new Map())
const cacheTrigger = ref(0) // Trigger để force update computed property

const showDetailsModal = ref(false)
const selectedHoaDon = ref<HoaDon | null>(null)
const showTransferRefundModal = ref(false)
const selectedRefundInfo = ref<{ hoaDon: HoaDon | null, refundId: number | null, soTien: number | null, maGiaoDich: string | null }>({
  hoaDon: null,
  refundId: null,
  soTien: null,
  maGiaoDich: null
})
const currentPage = ref(1)
const pageSize = ref(50)
const totalPages = ref(0)
const totalElements = ref(0)
const updatingStatus = ref<number | null>(null)
const isUpdatingStatus = ref(false)
const isTransferringRefund = ref(false)
let autoRefreshTimer: any = null
const AUTO_REFRESH_MS = 5000

// Selection state
const selectedHoaDons = ref<Set<number>>(new Set())
const selectAll = ref(false)


// Search and filter
const searchKeyword = ref('')
const showFilters = ref(false)
const filterTrangThai = ref('')
const filterLoaiHoaDon = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const sortBy = ref('ngayTao')
const sortDirection = ref('asc')
const quickSort = ref('ngayTao_asc')

// Status options - Đồng bộ với backend OrderStatusUtil
const statusOptions = [
  { value: 0, label: 'Chờ xác nhận', color: '#ffc107' },
  { value: 1, label: 'Chờ giao hàng', color: '#17a2b8' },
  { value: 2, label: 'Đang giao', color: '#ff6b35' },
  { value: 3, label: 'Hoàn thành', color: '#28a745' },
  { value: 4, label: 'Đã hủy', color: '#dc3545' }
]

// Sort options
const sortOptions = [
  { value: 'ngayTao', label: 'Ngày tạo' },
  { value: 'tongTien', label: 'Tổng tiền' }
]

const sortDirectionOptions = [
  { value: 'desc', label: 'Giảm dần' },
  { value: 'asc', label: 'Tăng dần' }
]

// Quick sort options for common use cases
const quickSortOptions = [
  { value: 'ngayTao_desc', label: 'Mới nhất', sortBy: 'ngayTao', direction: 'desc' },
  { value: 'ngayTao_asc', label: 'Cũ nhất', sortBy: 'ngayTao', direction: 'asc' },
  { value: 'tongTien_desc', label: 'Tổng tiền cao nhất', sortBy: 'tongTien', direction: 'desc' },
  { value: 'tongTien_asc', label: 'Tổng tiền thấp nhất', sortBy: 'tongTien', direction: 'asc' }
]

// Computed - Filter hóa đơn theo tab và các bộ lọc
const filteredHoaDons = computed(() => {
  let filtered = hoaDons.value

  // Filter theo tab (loaiThanhToan) - sử dụng cacheTrigger để force re-compute
  const _trigger = cacheTrigger.value // Force dependency tracking
  
  if (activeTab.value === 'refund') {
    // Lọc hóa đơn có ít nhất 1 chi tiết thanh toán với loaiThanhToan = REFUND
    filtered = filtered.filter(hd => {
      const paymentHistory = paymentHistoryCache.value.get(hd.id) || hd.lichSuThanhToan || []
      const hasRefund = paymentHistory.some(payment => payment.loaiThanhToan === 'REFUND')
      if (hasRefund) {
        console.log(`✅ Hoa don ${hd.id} (${hd.maHoaDon}) has REFUND payment`)
      }
      return hasRefund
    })
    console.log(`🔍 Filtered by REFUND: ${filtered.length} hoa dons out of ${hoaDons.value.length}, cache size: ${paymentHistoryCache.value.size}`)
  } else if (activeTab.value === 'additional-fee') {
    // Lọc hóa đơn có ít nhất 1 chi tiết thanh toán với loaiThanhToan = ADDITIONAL_FEE
    filtered = filtered.filter(hd => {
      const paymentHistory = paymentHistoryCache.value.get(hd.id) || hd.lichSuThanhToan || []
      const hasAdditionalFee = paymentHistory.some(payment => payment.loaiThanhToan === 'ADDITIONAL_FEE')
      if (hasAdditionalFee) {
        console.log(`✅ Hoa don ${hd.id} (${hd.maHoaDon}) has ADDITIONAL_FEE payment`)
      }
      return hasAdditionalFee
    })
    console.log(`🔍 Filtered by ADDITIONAL_FEE: ${filtered.length} hoa dons out of ${hoaDons.value.length}, cache size: ${paymentHistoryCache.value.size}`)
  }
  
  // Unused but forces tracking
  void _trigger
  // activeTab.value === 'list' -> hiển thị tất cả

  // Apply các filter khác
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(hd =>
      hd.maHoaDon.toLowerCase().includes(keyword) ||
      (hd.tenKhachHang && hd.tenKhachHang.toLowerCase().includes(keyword)) ||
      (hd.soDienThoai && hd.soDienThoai.includes(keyword))
    )
  }

  if (filterTrangThai.value !== '') {
    filtered = filtered.filter(hd => hd.trangThai === parseInt(filterTrangThai.value))
  }

  if (filterLoaiHoaDon.value !== '') {
    filtered = filtered.filter(hd => hd.loaiHoaDon === filterLoaiHoaDon.value)
  }

  // Bỏ filter date ở frontend vì backend đã filter rồi (tuNgay và denNgay)
  // Nếu filter lại ở frontend có thể gây lỗi do timezone hoặc format ngày
  // if (dateFrom.value) {
  //   filtered = filtered.filter(hd => new Date(hd.ngayTao) >= new Date(dateFrom.value))
  // }

  // if (dateTo.value) {
  //   filtered = filtered.filter(hd => new Date(hd.ngayTao) <= new Date(dateTo.value))
  // }

  return filtered
})

// Methods
function handleStatusChange() {
  console.log('🔄 Status changed to:', filterTrangThai.value)
  console.log('🔄 Status type:', typeof filterTrangThai.value)
  console.log('🔄 Status parsed:', parseInt(filterTrangThai.value))
  currentPage.value = 1 // Reset to first page

  // Thông báo toast cho lọc trạng thái
  if (filterTrangThai.value !== '') {
    const statusName = statusOptions.find(s => s.value === parseInt(filterTrangThai.value))?.label || 'Trạng thái'
    toastRef.value?.info('Lọc', `Đã lọc theo trạng thái: ${statusName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả trạng thái')
  }

  loadHoaDons()
}

function handleLoaiHoaDonChange() {
  console.log('🔄 Loai hoa don changed to:', filterLoaiHoaDon.value)
  currentPage.value = 1 // Reset to first page

  // Thông báo toast cho lọc loại đơn hàng
  if (filterLoaiHoaDon.value !== '') {
    let loaiName = ''
    switch(filterLoaiHoaDon.value) {
      case 'BAN_THUONG':
        loaiName = 'Bán tại quầy'
        break
      case 'BAN_ONLINE':
      case 'ONLINE':
        loaiName = 'Đơn online'
        break
      default:
        loaiName = filterLoaiHoaDon.value
    }
    toastRef.value?.info('Lọc', `Đã lọc theo loại: ${loaiName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả loại đơn hàng')
  }

  loadHoaDons()
}


// Load lịch sử thanh toán cho một hóa đơn
async function loadPaymentHistory(hoaDonId: number) {
  try {
    // Kiểm tra cache trước
    if (paymentHistoryCache.value.has(hoaDonId)) {
      const cached = paymentHistoryCache.value.get(hoaDonId) || []
      console.log(`✅ Using cached payment history for hoa don ${hoaDonId}: ${cached.length} records`)
      return cached
    }

    console.log(`🔍 Loading payment history for hoa don ${hoaDonId}...`)
    // Load từ API
    const { data } = await api.get(`/api/hoa-don/${hoaDonId}`)
    const paymentHistory = data.lichSuThanhToan || []
    
    console.log(`📊 Loaded payment history for hoa don ${hoaDonId}:`, paymentHistory.length, 'records')
    paymentHistory.forEach((payment: ChiTietThanhToan) => {
      console.log(`  - ID: ${payment.id}, Loai: ${payment.loaiThanhToan}, SoTien: ${payment.soTien}`)
    })
    
    // Lưu vào cache và trigger update
    paymentHistoryCache.value.set(hoaDonId, paymentHistory)
    cacheTrigger.value++ // Force reactive update
    
    return paymentHistory
  } catch (error) {
    console.error(`❌ Error loading payment history for hoa don ${hoaDonId}:`, error)
    return []
  }
}

// Load lịch sử thanh toán cho nhiều hóa đơn (batch)
async function loadPaymentHistoryBatch(hoaDonIds: number[]) {
  const promises = hoaDonIds.map(id => loadPaymentHistory(id))
  await Promise.all(promises)
}

async function loadHoaDons() {
  loading.value = true
  try {
    const searchRequest = {
      keyword: searchKeyword.value,
      trangThai: filterTrangThai.value !== '' ? parseInt(filterTrangThai.value) : null,
      loaiHoaDon: filterLoaiHoaDon.value || null,
      tuNgay: dateFrom.value ? (dateFrom.value + 'T00:00:00') : null,
      denNgay: dateTo.value ? (dateTo.value + 'T23:59:59') : null,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    }

    console.log('🔍 Search Request:', searchRequest)
    console.log('🔍 Date From:', dateFrom.value, '-> tuNgay:', searchRequest.tuNgay)
    console.log('🔍 Date To:', dateTo.value, '-> denNgay:', searchRequest.denNgay)
    console.log('🔍 Filter Loai Hoa Don:', filterLoaiHoaDon.value)
    console.log('🔍 Filter Trang Thai:', filterTrangThai.value)
    console.log('🔍 Sort By:', sortBy.value, 'Sort Direction:', sortDirection.value)

    // Nếu đang ở tab refund hoặc additional-fee, load nhiều hóa đơn hơn để có đủ data để filter
    const originalPageSize = pageSize.value
    if ((activeTab.value === 'refund' || activeTab.value === 'additional-fee') && pageSize.value < 200) {
      searchRequest.size = 200 // Load nhiều hơn để có đủ data
      console.log(`🔍 Loading more hoa dons for ${activeTab.value} tab (size: ${searchRequest.size})`)
    }

    const { data } = await api.post('/api/hoa-don/search-advanced', searchRequest)
    console.log('📊 Response Data:', data)
    console.log('📊 Response Content:', data.content)
    console.log('📊 Response Total Elements:', data.totalElements)
    console.log('📊 Pagination Info:', {
      content: data.content?.length || 0,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
      currentPage: currentPage.value,
      pageSize: searchRequest.size
    })

    hoaDons.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0

    console.log(`📋 Loaded ${hoaDons.value.length} hoa dons`)
    console.log(`🔍 Active tab: ${activeTab.value}`)

    // Load lịch sử thanh toán nếu đang ở tab refund hoặc additional-fee
    if (activeTab.value === 'refund' || activeTab.value === 'additional-fee') {
      const hoaDonIds = hoaDons.value.map(hd => hd.id)
      console.log(`🔍 Loading payment history for ${hoaDonIds.length} hoa dons...`)
      if (hoaDonIds.length > 0) {
        await loadPaymentHistoryBatch(hoaDonIds)
        console.log(`✅ Payment history cache size: ${paymentHistoryCache.value.size}`)
        
        // Force re-evaluate computed property
        cacheTrigger.value++
      } else {
        console.log('⚠️ No hoa dons to load payment history for')
      }
    }

    console.log('📊 Updated pagination state:', {
      totalPages: totalPages.value,
      totalElements: totalElements.value,
      currentPage: currentPage.value
    })

    console.log('📋 Filtered Results:', hoaDons.value.length, 'items')
    console.log('📋 Sample Data:', hoaDons.value.slice(0, 3).map(hd => ({
      maHoaDon: hd.maHoaDon,
      trangThai: hd.trangThai,
      loaiHoaDon: hd.loaiHoaDon
    })))
  } catch (error) {
    console.error('Lỗi khi tải danh sách hóa đơn:', error)
    console.error('Error details:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: error.config
    })
    hoaDons.value = []
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách hóa đơn')
  } finally {
    loading.value = false
  }
}

// Hàm chuyển tab
async function switchTab(tab: string) {
  activeTab.value = tab
  currentPage.value = 1
  
  // Tự động lọc theo ngày hôm nay khi chuyển sang tab danh sách hóa đơn
  if (tab === 'list') {
    const todayStr = getTodayDateString()
    dateFrom.value = todayStr
    dateTo.value = todayStr
  }
  // Tự động chuyển bộ lọc ngày sang 7 ngày gần nhất khi chuyển sang tab hoàn phí hoặc phụ phí
  else if (tab === 'refund' || tab === 'additional-fee') {
    const today = new Date()
    const sevenDaysAgo = new Date(today)
    sevenDaysAgo.setDate(today.getDate() - 7)
    
    // Format date as YYYY-MM-DD
    dateTo.value = today.toISOString().split('T')[0]
    dateFrom.value = sevenDaysAgo.toISOString().split('T')[0]
  }
  
  // Reload hóa đơn để có đầy đủ data
  await loadHoaDons()
}

function getTabLabel(tab: string): string {
  switch(tab) {
    case 'list':
      return 'Danh sách hóa đơn'
    case 'refund':
      return 'Hoàn phí'
    case 'additional-fee':
      return 'Phụ phí'
    default:
      return 'Danh sách hóa đơn'
  }
}


// Auto refresh silently to detect new orders without disturbing UI
async function autoRefreshHoaDons() {
  try {
    if (document.visibilityState !== 'visible') return

    const searchRequest = {
      keyword: searchKeyword.value,
      trangThai: filterTrangThai.value !== '' ? parseInt(filterTrangThai.value) : null,
      loaiHoaDon: filterLoaiHoaDon.value || null,
      tuNgay: dateFrom.value ? (dateFrom.value + 'T00:00:00') : null,
      denNgay: dateTo.value ? (dateTo.value + 'T23:59:59') : null,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    }

    const previousIds = new Set(hoaDons.value.map(h => h.id))
    const { data } = await api.post('/api/hoa-don/search-advanced', searchRequest)
    const newContent = data.content || []
    const newIds = new Set(newContent.map((h: any) => h.id))

    let newCount = 0
    newIds.forEach((id: number) => {
      if (!previousIds.has(id)) newCount++
    })

    hoaDons.value = newContent
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0

    if (newCount > 0) {
      toastRef.value?.success('Đơn hàng mới', `Có ${newCount} đơn hàng mới vừa được tạo`)
    }
  } catch (e) {
    // silent on auto refresh
  }
}

async function deleteHoaDon(id: number) {
  // Sử dụng toast để xác nhận thay vì confirm
  toastRef.value?.warning('Xác nhận', 'Bạn có chắc muốn xóa hóa đơn này?')

  // Tạm thời tự động xóa sau 2 giây (có thể cải tiến thành modal xác nhận riêng)
  setTimeout(async () => {
    try {
      await api.delete(`/api/hoa-don/${id}`)
      await loadHoaDons()
      toastRef.value?.success('Thành công', 'Đã xóa hóa đơn thành công')
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
      toastRef.value?.error('Lỗi', 'Không thể xóa hóa đơn')
    }
  }, 2000)
}

function viewDetails(hoaDon: HoaDon) {
  selectedHoaDon.value = hoaDon
  showDetailsModal.value = true
  toastRef.value?.info('Hiển thị', `Đang xem chi tiết hóa đơn: ${hoaDon.maHoaDon}`)
}

async function viewDetailsAndTrack(hoaDon: HoaDon) {
  // Điều hướng đến trang chi tiết đơn hàng (Theo dõi đơn hàng)
  router.push({
    path: '/don-hang',
    query: { code: hoaDon.maHoaDon }
  })
}

// Helper function để lấy refund ID đang chờ chuyển tiền (PENDING)
function getPendingRefundId(hoaDon: HoaDon): number | null {
  const paymentHistory = paymentHistoryCache.value.get(hoaDon.id) || hoaDon.lichSuThanhToan || []
  const pendingRefund = paymentHistory.find(
    (payment: ChiTietThanhToan) => 
      payment.loaiThanhToan === 'REFUND' && 
      payment.trangThai === 0 // REFUND_PENDING
  )
  return pendingRefund?.id || null
}

// Helper function để lấy thông tin refund đang chờ chuyển tiền
function getPendingRefundInfo(hoaDon: HoaDon): { refundId: number | null, soTien: number | null, maGiaoDich: string | null } {
  const paymentHistory = paymentHistoryCache.value.get(hoaDon.id) || hoaDon.lichSuThanhToan || []
  const pendingRefund = paymentHistory.find(
    (payment: ChiTietThanhToan) => 
      payment.loaiThanhToan === 'REFUND' && 
      payment.trangThai === 0 // REFUND_PENDING
  )
  
  if (!pendingRefund) {
    return { refundId: null, soTien: null, maGiaoDich: null }
  }
  
  return {
    refundId: pendingRefund.id || null,
    soTien: pendingRefund.soTien || null,
    maGiaoDich: pendingRefund.maGiaoDich || null
  }
}

// Helper function để lấy trạng thái hoàn phí
function getRefundStatus(hoaDon: HoaDon): { status: number | null, label: string } {
  const paymentHistory = paymentHistoryCache.value.get(hoaDon.id) || hoaDon.lichSuThanhToan || []
  const refund = paymentHistory.find(
    (payment: ChiTietThanhToan) => payment.loaiThanhToan === 'REFUND'
  )
  
  if (!refund) {
    return { status: null, label: '-' }
  }
  
  const status = refund.trangThai
  let label = ''
  
  switch (status) {
    case 0: // REFUND_PENDING
      label = 'Chờ chuyển tiền'
      break
    case 1: // REFUND_TRANSFERRED
      label = 'Đã chuyển tiền'
      break
    case 2: // REFUND_COMPLETED
      label = 'Hoàn thành'
      break
    default:
      label = 'Không xác định'
  }
  
  return { status, label }
}

// Helper function để lấy class cho badge trạng thái hoàn phí
function getRefundStatusBadgeClass(status: number | null): string {
  if (status === null) return ''
  
  switch (status) {
    case 0: // REFUND_PENDING
      return 'badge-refund-pending'
    case 1: // REFUND_TRANSFERRED
      return 'badge-refund-transferred'
    case 2: // REFUND_COMPLETED
      return 'badge-refund-completed'
    default:
      return 'badge-refund-unknown'
  }
}

// Helper function để lấy số tiền phụ phí (tổng các ADDITIONAL_FEE payments)
function getAdditionalFeeAmount(hoaDon: HoaDon): number {
  const paymentHistory = paymentHistoryCache.value.get(hoaDon.id) || hoaDon.lichSuThanhToan || []
  const additionalFeePayments = paymentHistory.filter(
    (payment: ChiTietThanhToan) => payment.loaiThanhToan === 'ADDITIONAL_FEE'
  )
  
  const totalAmount = additionalFeePayments.reduce((sum: number, payment: ChiTietThanhToan) => {
    return sum + (payment.soTien || 0)
  }, 0)
  
  return totalAmount
}

// Helper function để lấy trạng thái thanh toán của phụ phí
function getAdditionalFeeStatus(hoaDon: HoaDon): { status: number | null, label: string } {
  const paymentHistory = paymentHistoryCache.value.get(hoaDon.id) || hoaDon.lichSuThanhToan || []
  const additionalFeePayments = paymentHistory.filter(
    (payment: ChiTietThanhToan) => payment.loaiThanhToan === 'ADDITIONAL_FEE'
  )
  
  if (additionalFeePayments.length === 0) {
    return { status: null, label: '-' }
  }
  
  // Kiểm tra xem có phụ phí nào chưa thanh toán không (trangThai = 0)
  const hasUnpaidFee = additionalFeePayments.some(
    (payment: ChiTietThanhToan) => payment.trangThai === 0
  )
  
  if (hasUnpaidFee) {
    return { status: 0, label: 'Chưa thanh toán' }
  } else {
    return { status: 1, label: 'Đã thanh toán' }
  }
}

// Helper function để lấy class cho badge trạng thái phụ phí
function getAdditionalFeeStatusBadgeClass(status: number | null): string {
  if (status === null) return ''
  
  switch (status) {
    case 0: // Chưa thanh toán
      return 'badge-additional-fee-unpaid'
    case 1: // Đã thanh toán
      return 'badge-additional-fee-paid'
    default:
      return 'badge-additional-fee-unknown'
  }
}

// Handle transfer refund (Admin) - Mở modal xác nhận
function handleTransferRefund(hoaDon: HoaDon) {
  const refundInfo = getPendingRefundInfo(hoaDon)
  if (!refundInfo.refundId || !hoaDon.maHoaDon) {
    toastRef.value?.error('Lỗi', 'Không tìm thấy hoàn phí cần chuyển tiền')
    return
  }

  // Lưu thông tin refund để hiển thị trong modal
  selectedRefundInfo.value = {
    hoaDon: hoaDon,
    refundId: refundInfo.refundId,
    soTien: refundInfo.soTien,
    maGiaoDich: refundInfo.maGiaoDich
  }
  
  // Mở modal xác nhận
  showTransferRefundModal.value = true
}

// Xác nhận chuyển tiền hoàn phí
async function confirmTransferRefund() {
  if (!selectedRefundInfo.value.hoaDon || !selectedRefundInfo.value.refundId) {
    toastRef.value?.error('Lỗi', 'Không tìm thấy thông tin hoàn phí')
    return
  }

  const hoaDon = selectedRefundInfo.value.hoaDon
  const refundId = selectedRefundInfo.value.refundId

  // Đóng modal
  showTransferRefundModal.value = false

  isTransferringRefund.value = true
  try {
    const { data } = await api.put(`/api/hoa-don/${hoaDon.maHoaDon}/refund/${refundId}/transfer`)
    
    if (data && data.success) {
      toastRef.value?.success('Thành công', 'Đã chuyển tiền hoàn phí thành công')
      // Refresh payment history cache
      await loadPaymentHistory(hoaDon.id)
      // Force re-evaluate computed property
      cacheTrigger.value++
    } else {
      throw new Error(data?.message || 'Chuyển tiền thất bại')
    }
  } catch (error: any) {
    console.error('Error transferring refund:', error)
    toastRef.value?.error('Lỗi', error.response?.data?.message || 'Có lỗi xảy ra khi chuyển tiền hoàn phí')
  } finally {
    isTransferringRefund.value = false
    // Reset selected refund info
    selectedRefundInfo.value = {
      hoaDon: null,
      refundId: null,
      soTien: null,
      maGiaoDich: null
    }
  }
}

// Hủy chuyển tiền
function cancelTransferRefund() {
  showTransferRefundModal.value = false
  selectedRefundInfo.value = {
    hoaDon: null,
    refundId: null,
    soTien: null,
    maGiaoDich: null
  }
}

function updateOrder(hoaDon: HoaDon) {
  // Chuyển đến trang cập nhật đơn hàng với mã đơn hàng
  router.push({
    name: 'update-order',
    query: { code: hoaDon.maHoaDon }
  })
}

async function updateOrderStatus(hoaDonId: number, newStatus: string) {
  updatingStatus.value = hoaDonId

  try {
    const { data } = await api.put(`/api/hoa-don/${hoaDonId}/status`, {
      trangThai: parseInt(newStatus)
    })

    // Cập nhật trạng thái trong danh sách hiện tại
    const index = hoaDons.value.findIndex(hd => hd.id === hoaDonId)
    if (index !== -1) {
      hoaDons.value[index].trangThai = parseInt(newStatus)
    }

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', 'Cập nhật trạng thái thành công!')

  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    updatingStatus.value = null
  }
}

function getStatusName(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? status.label : 'Không xác định'
}

function getStatusClass(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? `status-${trangThai}` : 'status-unknown'
}

function getStatusColor(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? status.color : '#6c757d'
}

function getStatusIcon(trangThai: number): string {
  const iconMap: Record<number, string> = {
    0: 'clock',           // Chờ xác nhận
    1: 'shipping-fast',   // Chờ giao hàng
    2: 'truck',           // Đang giao
    3: 'check-circle',    // Hoàn thành
    4: 'times-circle'      // Đã hủy
  }
  return iconMap[trangThai] || 'question-circle'
}

function getPaymentMethodName(method: string): string {
  if (!method) return 'Chưa chọn'
  
  const paymentMethods: { [key: string]: string } = {
    'cod': 'Thanh toán khi nhận hàng (COD)',
    'momo': 'Ví MoMo',
    'bank': 'Chuyển khoản ngân hàng',
    'prepaid': 'Thanh toán trước',
    'cash': 'Tiền mặt',
    'card': 'Thẻ tín dụng/ghi nợ'
  }
  
  return paymentMethods[method] || method
}

function getShippingMethodName(method: string): string {
  if (!method) return 'Chưa chọn'
  
  const shippingMethods: { [key: string]: string } = {
    'standard': 'Tiêu chuẩn',
    'express': 'Giao nhanh', 
    'ghn': 'Hỏa tốc',
    'fast': 'Giao nhanh',
    'urgent': 'Hỏa tốc'
  }
  
  return shippingMethods[method] || method
}

function getStatusBadgeClass(trangThai: number): string {
  const badgeMap: Record<number, string> = {
    0: 'badge-pending',     // Chờ xác nhận
    1: 'badge-shipping',     // Chờ giao hàng
    2: 'badge-delivering',   // Đang giao
    3: 'badge-completed',    // Hoàn thành
    4: 'badge-cancelled'     // Đã hủy
  }
  return badgeMap[trangThai] || 'badge-unknown'
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatDateTime(dateString: string): string {
  return new Date(dateString).toLocaleString('vi-VN')
}

function toggleFilters() {
  showFilters.value = !showFilters.value
}

// Debounce search function
let searchTimeout: NodeJS.Timeout | null = null

function handleSearch() {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  searchTimeout = setTimeout(() => {
    currentPage.value = 1
    loadHoaDons()

    // Thông báo toast cho tìm kiếm
    if (searchKeyword.value.trim()) {
      toastRef.value?.info('Tìm kiếm', `Đang tìm kiếm: "${searchKeyword.value}"`)
    } else {
      toastRef.value?.info('Tìm kiếm', 'Đã xóa bộ lọc tìm kiếm')
    }
  }, 500) // 500ms delay
}

// Helper function để lấy ngày hôm nay
function getTodayDateString(): string {
  const today = new Date()
  const yyyy = today.getFullYear()
  const mm = String(today.getMonth() + 1).padStart(2, '0')
  const dd = String(today.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

// Hàm xử lý khi thay đổi ngày lọc
function handleDateChange() {
  currentPage.value = 1
  
  // Hiển thị toast khi lọc từ ngày đến ngày
  if (dateFrom.value && dateTo.value) {
    const fromDate = new Date(dateFrom.value).toLocaleDateString('vi-VN')
    const toDate = new Date(dateTo.value).toLocaleDateString('vi-VN')
    toastRef.value?.info('Lọc', `Đã lọc từ ngày ${fromDate} đến ngày ${toDate}`)
  } else if (dateFrom.value) {
    const fromDate = new Date(dateFrom.value).toLocaleDateString('vi-VN')
    toastRef.value?.info('Lọc', `Đã lọc từ ngày ${fromDate}`)
  } else if (dateTo.value) {
    const toDate = new Date(dateTo.value).toLocaleDateString('vi-VN')
    toastRef.value?.info('Lọc', `Đã lọc đến ngày ${toDate}`)
  }
  
  loadHoaDons()
}

function resetFilters() {
  searchKeyword.value = ''
  filterTrangThai.value = ''
  filterLoaiHoaDon.value = ''
  // Reset về ngày hôm nay thay vì rỗng
  const todayStr = getTodayDateString()
  dateFrom.value = todayStr
  dateTo.value = todayStr
  sortBy.value = 'ngayTao'
  sortDirection.value = 'asc'
  quickSort.value = 'ngayTao_asc'
  currentPage.value = 1
  loadHoaDons()
  toastRef.value?.info('Thông báo', 'Đã reset bộ lọc về ngày hôm nay')
}

// Auto-filter today will be applied onMounted

function exportToExcel() {
  try {
    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()

    // Lấy dữ liệu để xuất (chỉ những hóa đơn được chọn hoặc tất cả nếu không có gì được chọn)
    const dataToExport = selectedHoaDons.value.size > 0 
      ? hoaDons.value.filter(hd => selectedHoaDons.value.has(hd.id))
      : hoaDons.value

    if (dataToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu để xuất Excel')
      return
    }

    // Chuẩn bị dữ liệu cho Excel
    const excelData = []

    // Thêm header theo thứ tự mới
    const headers = [
      'STT',
      'Mã Nhân Viên',
      'Tên khách hàng',
      'SĐT khách hàng',
      'Tổng tiền',
      'Hình thức hóa đơn',
      'Trạng thái',
      'Ngày tạo'
    ]
    excelData.push(headers)

    // Thêm dữ liệu từ bảng
    dataToExport.forEach((hd, index) => {
      const row = [
        index + 1,
        hd.maNhanVien || 'N/A',
        hd.tenKhachHang || 'Không có',
        hd.soDienThoai || 'Không có',
        formatCurrency(hd.tongTienSauGiam || hd.tongTien),
        (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy',
        getStatusName(hd.trangThai),
        formatDate(hd.ngayTao)
      ]
      excelData.push(row)
    })

    // Tạo worksheet từ dữ liệu
    const ws = XLSX.utils.aoa_to_sheet(excelData)

    // Điều chỉnh độ rộng cột
    ws['!cols'] = [
      { width: 5 },   // STT
      { width: 15 },  // Mã Nhân Viên
      { width: 20 },  // Tên khách hàng
      { width: 15 },  // SĐT khách hàng
      { width: 15 },  // Tổng tiền
      { width: 15 },  // Hình thức hóa đơn
      { width: 15 },  // Trạng thái
      { width: 12 }   // Ngày tạo
    ]

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách hóa đơn')

    // Tạo tên file với timestamp
    const now = new Date()
    const timestamp = now.toISOString().slice(0, 19).replace(/:/g, '-')
    const fileName = `Danh_sach_hoa_don_${timestamp}.xlsx`

    // Xuất file
    XLSX.writeFile(wb, fileName)

    const exportCount = dataToExport.length
    const message = selectedHoaDons.value.size > 0 
      ? `Đã xuất ${exportCount} hóa đơn được chọn thành công!`
      : `Đã xuất ${exportCount} hóa đơn thành công!`
    
    toastRef.value?.success('Xuất Excel', message)

  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

function handleSortChange() {
  currentPage.value = 1 // Reset to first page when changing sort
  loadHoaDons()

  const sortLabel = sortOptions.find(opt => opt.value === sortBy.value)?.label || 'Ngày tạo'
  const directionLabel = sortDirection.value === 'desc' ? 'Giảm dần' : 'Tăng dần'

  toastRef.value?.info('Sắp xếp', `Đã sắp xếp theo ${sortLabel} - ${directionLabel}`)
}

function handleQuickSortChange() {
  const selectedOption = quickSortOptions.find(opt => opt.value === quickSort.value)
  console.log('🔄 Quick Sort Change:', {
    quickSortValue: quickSort.value,
    selectedOption: selectedOption,
    sortBy: selectedOption?.sortBy,
    sortDirection: selectedOption?.direction
  })

  if (selectedOption) {
    sortBy.value = selectedOption.sortBy
    sortDirection.value = selectedOption.direction
    currentPage.value = 1

    console.log('🔄 Updated sort values:', {
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    })

    loadHoaDons()

    toastRef.value?.info('Sắp xếp', `Đã sắp xếp: ${selectedOption.label}`)
  }
}

function goToPage(page: number) {
  console.log('🔍 goToPage called:', {
    requestedPage: page,
    currentPage: currentPage.value,
    totalPages: totalPages.value,
    canNavigate: page >= 1 && page <= totalPages.value
  })

  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    console.log('✅ Navigating to page:', page)
    loadHoaDons()
  } else {
    console.log('❌ Cannot navigate to page:', page, 'Total pages:', totalPages.value)
    toastRef.value?.error('Lỗi', `Không thể chuyển đến trang ${page}`)
  }
}

function handlePageSizeChange() {
  currentPage.value = 1 // Reset to first page when changing page size
  loadHoaDons()
}

// Selection functions
function toggleSelectAll() {
  if (selectAll.value) {
    // Chọn tất cả hóa đơn trên trang hiện tại
    selectedHoaDons.value.clear()
    hoaDons.value.forEach(hd => selectedHoaDons.value.add(hd.id))
  } else {
    // Bỏ chọn tất cả
    selectedHoaDons.value.clear()
  }
}

function toggleSelectHoaDon(hoaDonId: number) {
  if (selectedHoaDons.value.has(hoaDonId)) {
    selectedHoaDons.value.delete(hoaDonId)
  } else {
    selectedHoaDons.value.add(hoaDonId)
  }
  
  // Cập nhật trạng thái selectAll
  selectAll.value = selectedHoaDons.value.size === hoaDons.value.length && hoaDons.value.length > 0
}

function clearSelection() {
  selectedHoaDons.value.clear()
  selectAll.value = false
}

// Computed để hiển thị số lượng đã chọn
const selectedCount = computed(() => selectedHoaDons.value.size)


// Function to get visible page numbers for pagination
function getVisiblePages() {
  const pages = []
  const current = currentPage.value
  const total = totalPages.value

  console.log('🔍 getVisiblePages called:', {
    current,
    total,
    currentPage: currentPage.value,
    totalPages: totalPages.value
  })

  if (total === 0) {
    console.log('❌ No pages available (total = 0)')
    return []
  }

  if (total <= 7) {
    // Show all pages if total is 7 or less
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // Show pages around current page
    const start = Math.max(1, current - 2)
    const end = Math.min(total, current + 2)

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }

  console.log('✅ getVisiblePages returning:', pages, 'Length:', pages.length)
  return pages
}

onMounted(() => {
  // Tự động lọc danh sách theo ngày hôm nay khi vào trang
  const todayStr = getTodayDateString()
  dateFrom.value = todayStr
  dateTo.value = todayStr
  sortBy.value = 'ngayTao'
  sortDirection.value = 'asc'
  quickSort.value = 'ngayTao_asc'
  currentPage.value = 1
  loadHoaDons()

  // Start auto refresh polling
  if (!autoRefreshTimer) {
    autoRefreshTimer = setInterval(() => {
      autoRefreshHoaDons()
    }, AUTO_REFRESH_MS)
  }
})

// Cleanup khi component bị unmount
onBeforeUnmount(() => {
  // Cleanup nếu cần
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
})
</script>

<template>
  <div class="hoa-don-page">
    <PosHeader />


    <main class="main-content">
      <!-- Tab Content -->
      <div class="tab-content">
        <!-- Danh sách hóa đơn -->
        <div class="tab-panel">
          <!-- Search and Filter Section -->
          <section class="search-filter-section">
              <!-- Search Bar -->
              <div class="search-bar">
                <div class="input-group">
                  <font-awesome-icon icon="search" class="input-icon" />
                  <input
                    v-model="searchKeyword"
                    type="text"
                    placeholder="Tìm kiếm theo mã hóa đơn, tên khách hàng, SĐT"
                    class="search-input"
                    @input="handleSearch"
                  />
                </div>
                <button @click="toggleFilters" class="btn-filter">
                  <font-awesome-icon icon="filter" />
                  Bộ lọc
                </button>
                <button @click="exportToExcel" class="btn-excel">
                  <font-awesome-icon icon="file-excel" />
                  Xuất Excel
                </button>
                <button v-if="selectedCount > 0" @click="clearSelection" class="btn-clear">
                  <font-awesome-icon icon="times" />
                  Xóa chọn ({{ selectedCount }})
                </button>
              </div>

              <!-- Expandable Filter Options -->
              <div v-if="showFilters" class="filter-options">
                <div class="filter-grid">
                  <!-- Row 1: 3 filters -->
                  <div class="filter-group">
                    <label>Trạng thái:</label>
                    <select v-model="filterTrangThai" class="filter-select" @change="handleStatusChange">
                      <option value="">Tất cả trạng thái</option>
                      <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                        {{ status.label }}
                      </option>
                    </select>
                  </div>
                  <div class="filter-group">
                    <label>Loại đơn:</label>
                    <select v-model="filterLoaiHoaDon" class="filter-select" @change="handleLoaiHoaDonChange">
                      <option value="">Tất cả loại</option>
                      <option value="BAN_THUONG">Bán tại quầy</option>
                      <option value="BAN_ONLINE">Đơn online</option>
                      <option value="ONLINE">Đơn online</option>
                    </select>
                  </div>
                  <div class="filter-group">
                    <label>Sắp xếp:</label>
                    <select v-model="quickSort" class="filter-select" @change="handleQuickSortChange">
                      <option v-for="option in quickSortOptions" :key="option.value" :value="option.value">
                        {{ option.label }}
                      </option>
                    </select>
                  </div>

                  <!-- Row 2: 2 date inputs + 2 buttons -->
                  <div class="filter-group">
                    <label>Từ ngày:</label>
                    <input v-model="dateFrom" type="date" class="filter-input" @change="handleDateChange" />
                  </div>
                  <div class="filter-group">
                    <label>Đến ngày:</label>
                    <input v-model="dateTo" type="date" class="filter-input" @change="handleDateChange" />
                  </div>
                  <div class="filter-group button-group">
                  <button @click="resetFilters" class="btn-reset">
                      <font-awesome-icon icon="times" />
                      Xóa bộ lọc
                    </button>
                  </div>
                </div>
            </div>
          </section>

          <!-- Table Section -->
          <section class="table-section">
            <div class="table-container">
              <div class="table-header">
                <div class="table-header-content">
                  <div class="tab-navigation-inline">
                    <button 
                      @click="switchTab('list')" 
                      :class="['tab-button-inline', { 'active': activeTab === 'list' }]"
                    >
                      <font-awesome-icon icon="file-invoice" />
                      Danh sách hóa đơn
                    </button>
                    <button 
                      @click="switchTab('refund')" 
                      :class="['tab-button-inline', { 'active': activeTab === 'refund' }]"
                    >
                      <font-awesome-icon icon="undo" />
                      Hoàn phí
                    </button>
                    <button 
                      @click="switchTab('additional-fee')" 
                      :class="['tab-button-inline', { 'active': activeTab === 'additional-fee' }]"
                    >
                      <font-awesome-icon icon="plus-circle" />
                      Phụ phí
                    </button>
                  </div>
                </div>
              </div>
              <div class="table-wrapper">
                <table>
                  <thead>
                  <tr>
                    <th>
                      <input 
                        type="checkbox" 
                        v-model="selectAll" 
                        @change="toggleSelectAll"
                        class="checkbox-select-all"
                      />
                    </th>
                    <th>STT</th>
                    <th>Mã Nhân Viên</th>
                    <th>Tên khách hàng</th>
                    <th>SĐT khách hàng</th>
                    <th>Tổng tiền</th>
                    <th>Hình thức hóa đơn</th>
                    <th>PTNH</th>
                    <th v-if="activeTab !== 'additional-fee'">Trạng thái</th>
                    <th v-if="activeTab === 'additional-fee'">Phụ phí (Số tiền chênh lệch)</th>
                    <th v-if="activeTab === 'refund' || activeTab === 'additional-fee'">Tình trạng</th>
                    <th>Ngày tạo</th>
                    <th>Hành động</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-if="loading">
                    <td :colspan="activeTab === 'refund' || activeTab === 'additional-fee' ? 12 : 11" class="text-center">
                      <div class="loading">
                        <font-awesome-icon icon="spinner" class="fa-spin" />
                        Đang tải...
                      </div>
                    </td>
                  </tr>
                  <tr v-else-if="filteredHoaDons.length === 0">
                    <td :colspan="activeTab === 'refund' || activeTab === 'additional-fee' ? 12 : 11" class="text-center">
                      <div class="empty-state">
                        <font-awesome-icon icon="file-invoice" class="empty-icon" />
                        <p>Không có dữ liệu</p>
                        <p v-if="activeTab === 'refund'" class="text-sm text-gray-500 mt-2">
                          Không có hoàn phí trong 7 ngày gần nhất
                        </p>
                        <p v-else-if="activeTab === 'additional-fee'" class="text-sm text-gray-500 mt-2">
                          Không có phụ phí trong 7 ngày gần nhất
                        </p>
                      </div>
                    </td>
                  </tr>
                  <tr v-else v-for="(hd, index) in filteredHoaDons" :key="hd.id">
                    <td>
                      <input 
                        type="checkbox" 
                        :checked="selectedHoaDons.has(hd.id)"
                        @change="toggleSelectHoaDon(hd.id)"
                        class="checkbox-row"
                      />
                    </td>
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>
                      <div class="staff-id">
                        {{ hd.maNhanVien || (hd.nhanVienId ? `NV${hd.nhanVienId}` : 'N/A') }}
                      </div>
                    </td>
                    <td>
                      <div class="customer-info">
                        <div class="customer-name">{{ hd.tenKhachHang || 'Khách lẻ' }}</div>
                      </div>
                    </td>
                    <td>
                      <div class="phone-info" v-if="hd.soDienThoai">
                        {{ hd.soDienThoai }}
                      </div>
                      <div class="no-phone" v-else>
                        <span class="text-muted">Không có</span>
                      </div>
                    </td>
                    <td>
                      <div class="amount">
                        {{ formatCurrency(hd.tongTienSauGiam || hd.tongTien) }}
                      </div>
                    </td>
                    <td>
                        <span :class="['order-type', (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'online' : 'normal']">
                          {{ (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy' }}
              </span>
                    </td>
                    <td>
                      <div class="delivery-method" v-if="hd.phuongThucNhanHang">
                        {{ hd.phuongThucNhanHang }}
                      </div>
                      <div class="text-muted" v-else>
                        -
                      </div>
                    </td>
                    <!-- Trạng thái (ẩn ở tab phụ phí) -->
                    <td v-if="activeTab !== 'additional-fee'">
                        <span :class="['status-badge', getStatusBadgeClass(hd.trangThai)]">
                          {{ getStatusName(hd.trangThai) }}
                        </span>
                    </td>
                    <!-- Phụ phí (Số tiền chênh lệch) - chỉ hiển thị ở tab phụ phí -->
                    <td v-if="activeTab === 'additional-fee'">
                      <div class="amount">
                        {{ formatCurrency(getAdditionalFeeAmount(hd)) }}
                      </div>
                    </td>
                    <!-- Tình trạng (cho hoàn phí và phụ phí) -->
                    <td v-if="activeTab === 'refund' || activeTab === 'additional-fee'">
                      <!-- Hiển thị trạng thái hoàn phí nếu ở tab refund -->
                      <div v-if="activeTab === 'refund'">
                        <div v-if="getRefundStatus(hd).status !== null">
                          <span :class="['refund-status-badge', getRefundStatusBadgeClass(getRefundStatus(hd).status)]">
                            {{ getRefundStatus(hd).label }}
                          </span>
                        </div>
                        <div v-else class="text-muted">
                          -
                        </div>
                      </div>
                      <!-- Hiển thị trạng thái phụ phí nếu ở tab additional-fee -->
                      <div v-if="activeTab === 'additional-fee'">
                        <div v-if="getAdditionalFeeStatus(hd).status !== null">
                          <span :class="['additional-fee-status-badge', getAdditionalFeeStatusBadgeClass(getAdditionalFeeStatus(hd).status)]">
                            {{ getAdditionalFeeStatus(hd).label }}
                          </span>
                        </div>
                        <div v-else class="text-muted">
                          -
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="date-info">
                        {{ formatDate(hd.ngayTao) }}
                      </div>
                    </td>
                    <td>
                      <div class="action-buttons">
                        <button @click="viewDetailsAndTrack(hd)" class="btn-view" title="Xem chi tiết và theo dõi">
                          <font-awesome-icon icon="eye" />
                        </button>
                        <!-- Nút Chuyển tiền cho hoàn phí (chỉ hiển thị ở tab refund và khi có REFUND với trạng thái PENDING) -->
                        <button 
                          v-if="activeTab === 'refund' && getPendingRefundId(hd) !== null"
                          @click="handleTransferRefund(hd)" 
                          class="btn-transfer-refund"
                          :disabled="isTransferringRefund"
                          title="Chuyển tiền hoàn phí"
                        >
                          <font-awesome-icon icon="exchange-alt" />
                        </button>
                        <!-- Ở tab phụ phí, chỉ hiển thị icon mắt -->
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>

          <!-- Pagination -->
          <section class="pagination-section">
            <div class="pagination-controls">
              <!-- Page Size Selector -->
              <div class="page-size-selector">
                <label for="pageSize">Hiển thị:</label>
                <select v-model="pageSize" @change="handlePageSizeChange" id="pageSize" class="page-size-select">
                  <option value="5">5</option>
                  <option value="10">10</option>
                  <option value="20">20</option>
                  <option value="50">50</option>
                  <option value="100">100</option>
                </select>
                <span>hóa đơn/trang</span>
              </div>

              <!-- Pagination Info -->
              <div class="pagination-info">
                <span v-if="activeTab === 'list'">
                  Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, totalElements) }} - {{ Math.min(currentPage * pageSize, totalElements) }} trong tổng số {{ totalElements}} hóa đơn
                </span>
                <span v-else>
                  Hiển thị {{ filteredHoaDons.length }} hóa đơn {{ activeTab === 'refund' ? 'có hoàn phí' : 'có phụ phí' }}
                </span>
              </div>
            </div>

            <!-- Page Navigation -->
            <div class="pagination" v-if="totalPages > 0">

              <button @click="() => { console.log('Previous clicked'); goToPage(currentPage - 1); }" :disabled="currentPage <= 1" class="page-btn">
                <font-awesome-icon icon="chevron-left" />
                Trước
              </button>

              <!-- Page Numbers -->
              <div class="page-numbers">
                <!-- First page -->
                <button
                  v-if="totalPages > 1 && currentPage > 3"
                  @click="() => { console.log('First page clicked'); goToPage(1); }"
                  class="page-number"
                >
                  1
                </button>
                <span v-if="totalPages > 1 && currentPage > 4" class="page-ellipsis">...</span>

                <!-- Pages around current page -->
                <template v-for="page in getVisiblePages()" :key="page">
                  <button
                    @click="() => { console.log('Page number clicked:', page); goToPage(page); }"
                    :class="['page-number', { 'active': page === currentPage }]"
                  >
                    {{ page }}
                  </button>
                </template>

                <!-- Last page -->
                <span v-if="totalPages > 1 && currentPage < totalPages - 3" class="page-ellipsis">...</span>
                <button
                  v-if="totalPages > 1 && currentPage < totalPages - 2"
                  @click="() => { console.log('Last page clicked:', totalPages); goToPage(totalPages); }"
                  class="page-number"
                >
                  {{ totalPages }}
                </button>
              </div>

              <button @click="() => { console.log('Next clicked'); goToPage(currentPage + 1); }" :disabled="currentPage >= totalPages" class="page-btn">
                Sau
                <font-awesome-icon icon="chevron-right" />
              </button>
            </div>
          </section>
        </div>
      </div>
    </main>


    <!-- Transfer Refund Confirmation Modal -->
    <div v-if="showTransferRefundModal" class="modal" @click.self="cancelTransferRefund">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Xác nhận chuyển tiền hoàn phí</h2>
          <button @click="cancelTransferRefund" class="btn-close">×</button>
        </div>
        <div class="modal-body-transfer">
          <div class="transfer-confirm-content">
            <div class="transfer-icon">
              <font-awesome-icon icon="exchange-alt" />
            </div>
            <p class="transfer-question">Bạn có chắc chắn muốn chuyển tiền hoàn phí này?</p>
            
            <div class="transfer-info-box">
              <div class="transfer-info-item">
                <label>Mã hóa đơn:</label>
                <span class="transfer-value">{{ selectedRefundInfo.hoaDon?.maHoaDon || 'N/A' }}</span>
              </div>
              <div class="transfer-info-item">
                <label>Số tiền hoàn phí:</label>
                <span class="transfer-value transfer-amount">{{ formatCurrency(selectedRefundInfo.soTien || 0) }}</span>
              </div>
              <div class="transfer-info-item">
                <label>Mã giao dịch:</label>
                <span class="transfer-value transfer-code">{{ selectedRefundInfo.maGiaoDich || 'Chưa có' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer-transfer">
          <button @click="cancelTransferRefund" class="btn-cancel-transfer">
            Hủy
          </button>
          <button @click="confirmTransferRefund" class="btn-confirm-transfer" :disabled="isTransferringRefund">
            <font-awesome-icon v-if="isTransferringRefund" icon="spinner" spin />
            {{ isTransferringRefund ? 'Đang xử lý...' : 'Xác nhận chuyển tiền' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Details Modal -->
    <div v-if="showDetailsModal" class="modal">
      <div class="modal-content large">
        <div class="modal-header">
          <h2>Chi tiết hóa đơn</h2>
          <button @click="showDetailsModal = false" class="btn-close">×</button>
        </div>
        <div v-if="selectedHoaDon" class="details-content">
          <div class="details-grid">
            <div class="detail-section">
              <h3>Thông tin hóa đơn</h3>
              <div class="detail-item">
                <label>Mã hóa đơn:</label>
                <span>{{ selectedHoaDon.maHoaDon }}</span>
              </div>
              <div class="detail-item">
                <label>Loại đơn:</label>
                <span :class="['order-type', (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY' || selectedHoaDon.loaiHoaDon === 'ONLINE') ? 'online' : 'normal']">
                  {{ (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY' || selectedHoaDon.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy' }}
                </span>
              </div>
              <div class="detail-item">
                <label>Phương thức thanh toán:</label>
                <span class="payment-method">
                  {{ getPaymentMethodName(selectedHoaDon.phuongThucThanhToan) }}
                </span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.phuongThucNhanHang">
                <label>Phương thức nhận hàng:</label>
                <span class="delivery-method">
                  {{ selectedHoaDon.phuongThucNhanHang }}
                </span>
              </div>
              <div class="detail-item">
                <label>Trạng thái:</label>
                <span :class="getStatusClass(selectedHoaDon.trangThai)" :style="{ color: getStatusColor(selectedHoaDon.trangThai) }">
                  {{ getStatusName(selectedHoaDon.trangThai) }}
                </span>
              </div>
              <div class="detail-item">
                <label>Ngày tạo:</label>
                <span>{{ formatDateTime(selectedHoaDon.ngayTao) }}</span>
              </div>
            </div>
            <div class="detail-section">
              <h3>Thông tin khách hàng</h3>
              <div class="detail-item">
                <label>Tên khách hàng:</label>
                <span>{{ selectedHoaDon.tenKhachHang || 'Khách lẻ' }}</span>
              </div>
              <div class="detail-item">
                <label>Số điện thoại:</label>
                <span>{{ selectedHoaDon.soDienThoai || 'Không có' }}</span>
              </div>
              <div class="detail-item">
                <label>Email:</label>
                <span>{{ selectedHoaDon.email || 'Không có' }}</span>
              </div>
            </div>
            <div class="detail-section">
              <h3>Thông tin thanh toán</h3>
              <div class="detail-item">
                <label>Tổng tiền:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.tongTien) }}</span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.tongTienSauGiam">
                <label>Tiền sau giảm:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.tongTienSauGiam) }}</span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.phiVanChuyen && selectedHoaDon.phiVanChuyen > 0">
                <label>Phí vận chuyển:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.phiVanChuyen) }}</span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.ghiChu">
                <label>Ghi chú:</label>
                <span>{{ selectedHoaDon.ghiChu }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<style scoped>
.hoa-don-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.main-content {
  padding: 20px;
  padding-top: 100px;
  width: 100%;
  margin: 0;
}


/* Tab Navigation */
.tab-section {
  margin-top: 20px;
  margin-bottom: 24px;
}

.tab-navigation {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  gap: 4px;
}

.tab-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: transparent;
  color: #718096;
  cursor: pointer;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-button:hover {
  background: #f7fafc;
  color: #4a5568;
}

.tab-button.active {
  background: #ff6b35; /* Orange */
  color: white;
  border-bottom: 3px solid #ff6b35;
}

.tab-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tab-panel {
  padding: 24px;
}

/* Search and Filter Section */
.search-filter-section {
  background: white;
  padding: 28px;
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.search-filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
  border-radius: 16px 16px 0 0;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.input-group {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #718096;
  z-index: 10;
  pointer-events: none;
  font-size: 16px;
  width: 16px;
  height: 16px;
}

.search-input {
  width: 100%;
  height: 48px;
  padding: 14px 18px 14px 45px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.btn-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 16px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-filter:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.btn-excel {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 16px;
  background: #22c55e;
  color: white;
  border: 1px solid #22c55e;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-excel:hover {
  background: #16a34a;
  color: white;
  border-color: #16a34a;
  transform: translateY(-1px);
}

.btn-clear {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 16px;
  background: #ef4444;
  color: white;
  border: 1px solid #ef4444;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-clear:hover {
  background: #dc2626;
  color: white;
  border-color: #dc2626;
  transform: translateY(-1px);
}

.filter-options {
  border-top: 1px solid #f1f5f9;
  padding: 20px 0 0 0;
  background: transparent;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  position: relative;
  margin-bottom: 20px;
}

/* First row: 3 columns with equal width */
.filter-grid .filter-group:nth-child(-n+3) {
  grid-column: auto;
}

/* Second row: 2 date inputs on left */
.filter-grid .filter-group:nth-child(4) {
  grid-column: 1;
}

.filter-grid .filter-group:nth-child(5) {
  grid-column: 2;
}

/* Second row: button group on same row as date inputs */
.filter-grid .filter-group:nth-child(6) {
  grid-column: 3;
  grid-row: 2;
}

/* Button group styling */
.button-group {
  display: flex !important;
  flex-direction: row !important;
  gap: 10px;
  align-items: end;
  justify-content: center;
  width: 100%;
  margin-top: 20px;
}

.button-group button {
  width: auto !important;
  padding: 8px 16px;
  white-space: nowrap;
  flex-shrink: 0;
  display: inline-flex !important;
  float: none !important;
  margin-bottom: 5px !important;
}

/* Button styling within filter-group */
.filter-group button {
  margin-top: 0;
  height: 40px;
  align-self: end;
}

/* Specific styling for buttons in column 3 - restore original size */
.filter-grid .filter-group:nth-child(6) button,
.filter-grid .filter-group:nth-child(7) button {
  width: auto;
  padding: 8px 16px;
}

/* Make buttons inline in the same row */
.filter-grid .filter-group:nth-child(6),
.filter-grid .filter-group:nth-child(7) {
  display: inline-block;
  width: auto;
}

.filter-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

/* Hide label for button groups */
.filter-group:nth-child(6) label,
.filter-group:nth-child(7) label {
  display: none;
}

.filter-grid::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #f97316, #ea580c);
  border-radius: 2px;
  opacity: 0.6;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.filter-group:hover {
  transform: translateY(-2px);
}

.filter-group:hover label {
  color: #f97316;
}

.filter-group label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-group label::before {
  content: '';
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 2px;
}

.filter-select,
.filter-input {
  width: 100%;
  height: 48px;
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
  vertical-align: middle;
}

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.filter-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}

/* Đặc biệt cho input type="date" */
input[type="date"].filter-input {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  width: 100%;
  height: 48px;
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
  vertical-align: middle;
}

input[type="date"].filter-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

input[type="date"].filter-input::-webkit-calendar-picker-indicator {
  background: transparent;
  bottom: 0;
  color: transparent;
  cursor: pointer;
  height: auto;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
  width: auto;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 2a1 1 0 0 0-1 1v1H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1V3a1 1 0 1 0-2 0v1H7V3a1 1 0 0 0-1-1zM4 7h12v9a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7z'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  opacity: 1;
  padding-right: 40px;
}

.filter-select,
.filter-input,
input[type="date"].filter-input {
  text-align: left;
}

.filter-select::placeholder,
.filter-input::placeholder,
input[type="date"].filter-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
  opacity: 1;
}

.btn-search,
.btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-search {
  background: #f97316;
  color: white;
}

.btn-search:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-reset {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-reset:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

/* Table */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  background: white;
}

.table-header {
  padding: 16px 20px 12px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.table-header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* Inline Tab Navigation */
.tab-navigation-inline {
  display: flex;
  gap: 0;
  background: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
  border: none;
  border-bottom: 1px solid #e5e7eb;
  position: relative;
}

.tab-button-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 0;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
  overflow: visible;
  min-width: fit-content;
  flex: 1;
}

.tab-button-inline::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: transparent;
  transition: background 0.2s ease;
}

.tab-button-inline:hover:not(.active) {
  background: transparent;
  color: #4b5563;
}

.tab-button-inline.active {
  background: transparent;
  color: #dc2626;
  font-weight: 600;
  box-shadow: none;
  transform: none;
  border: none;
}

.tab-button-inline.active::after {
  background: #dc2626;
  height: 2px;
}

.tab-button-inline.active:hover {
  background: transparent;
  color: #dc2626;
  box-shadow: none;
  transform: none;
}

.tab-button-inline svg,
.tab-button-inline .fa-icon {
  font-size: 14px;
  transition: all 0.2s ease;
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: inherit;
}

.tab-button-inline.active svg,
.tab-button-inline.active .fa-icon {
  transform: none;
  filter: none;
  color: #dc2626;
}

.tab-button-inline:active {
  transform: none;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1540px;
}

/* Điều chỉnh độ rộng cột */
table th:nth-child(1),
table td:nth-child(1) {
  width: 50px;
  min-width: 50px;
  max-width: 50px;
  text-align: center;
}

table th:nth-child(2),
table td:nth-child(2) {
  width: 60px;
  min-width: 60px;
  max-width: 60px;
  text-align: center;
}

table th:nth-child(3),
table td:nth-child(3) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  text-align: center;
}

table th:nth-child(4),
table td:nth-child(4) {
  width: 180px;
  min-width: 180px;
}

table th:nth-child(5),
table td:nth-child(5) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
}

table th:nth-child(6),
table td:nth-child(6) {
  width: 130px;
  min-width: 130px;
  text-align: right;
}

table th:nth-child(7),
table td:nth-child(7) {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

table th:nth-child(8),
table td:nth-child(8) {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

table th:nth-child(9),
table td:nth-child(9) {
  width: 140px;
  min-width: 140px;
  text-align: center;
}

table th:nth-child(10),
table td:nth-child(10) {
  width: 110px;
  min-width: 110px;
  text-align: center;
}

table th:nth-child(11),
table td:nth-child(11) {
  width: 80px;
  min-width: 80px;
  text-align: center;
}

th, td {
  border: 1px solid #e0e0e0;
  padding: 12px;
  text-align: left;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.text-center {
  text-align: center;
  color: #6c757d;
  font-style: italic;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #007bff;
}

/* Customer Info */
.customer-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  background: none !important;
  border: none !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.customer-name {
  font-weight: 500;
  color: #333;
  background: none !important;
  border: none !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.phone-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
  font-weight: 500;
}

.phone-info svg {
  color: #6b7280;
  font-size: 14px;
}

.no-phone {
  color: #9ca3af;
  font-style: italic;
}

.text-muted {
  color: #9ca3af;
}

/* Order Type */
.order-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.order-type.normal {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #f59e0b;
}

.order-type.online {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.payment-method {
  font-weight: 500;
  color: #374151;
  padding: 4px 8px;
  background: #f3f4f6;
  border-radius: 6px;
  display: inline-block;
}

/* Status Badge */
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}


.badge-shipping {
  background: #cce5ff;
  color: #004085;
  border: 1px solid #99d3ff;
}

.badge-delivering {
  background: #ffe6cc;
  color: #cc6600;
  border: 1px solid #ffcc99;
}

.badge-completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.badge-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.badge-unknown {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

/* Status Classes */
.status-0 { color: #ffc107; font-weight: 600; }  /* Chờ xác nhận */
.status-1 { color: #17a2b8; font-weight: 600; }  /* Chờ giao hàng */
.status-2 { color: #ff6b35; font-weight: 600; }   /* Đang giao */
.status-3 { color: #28a745; font-weight: 600; }   /* Hoàn thành */
.status-4 { color: #dc3545; font-weight: 600; }   /* Đã hủy */
.status-unknown { color: #6c757d; font-weight: 600; }

/* Checkbox Styles */
.checkbox-select-all,
.checkbox-row {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #ff6b35;
}

.checkbox-select-all:checked,
.checkbox-row:checked {
  background-color: #ff6b35;
  border-color: #ff6b35;
}

/* Staff ID */
.staff-id {
  font-family: monospace;
  font-weight: 600;
  color: #374151;
  text-align: center;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 4px;
}

.btn-view {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 14px;
  background: #e0f2fe;
  color: #0277bd;
}

.btn-view:hover {
  background: #b3e5fc;
  transform: translateY(-1px);
}

.btn-transfer-refund {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 14px;
  background: #d1fae5;
  color: #059669;
}

.btn-transfer-refund:hover:not(:disabled) {
  background: #a7f3d0;
  color: #047857;
  transform: translateY(-1px);
}

.btn-transfer-refund:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Refund Status Badge */
.refund-status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-refund-pending {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fbbf24;
}

.badge-refund-transferred {
  background: #dbeafe;
  color: #2563eb;
  border: 1px solid #60a5fa;
}

.badge-refund-completed {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #10b981;
}

.badge-refund-unknown {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #9ca3af;
}

/* Additional Fee Status Badge */
.additional-fee-status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-additional-fee-unpaid {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.badge-additional-fee-paid {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #10b981;
}

.badge-additional-fee-unknown {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #9ca3af;
}

/* Transfer Refund Modal */
.modal-body-transfer {
  padding: 24px;
}

.transfer-confirm-content {
  text-align: center;
}

.transfer-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.transfer-question {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.transfer-info-box {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  border: 1px solid #e5e7eb;
}

.transfer-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.transfer-info-item:last-child {
  border-bottom: none;
}

.transfer-info-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 14px;
}

.transfer-value {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.transfer-amount {
  color: #059669;
  font-size: 16px;
}

.transfer-code {
  font-family: monospace;
  color: #2563eb;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 6px;
}

.modal-footer-transfer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  background: #f8f9fa;
}

.btn-cancel-transfer {
  padding: 10px 20px;
  border: 1px solid #d1d5db;
  background: white;
  color: #374151;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-cancel-transfer:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.btn-confirm-transfer {
  padding: 10px 24px;
  border: none;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-confirm-transfer:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-confirm-transfer:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Pagination */
.pagination-section {
  margin-top: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector label {
  font-weight: 500;
  color: #333;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  font-size: 14px;
  cursor: pointer;
}

.page-size-select:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.1);
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-number {
  min-width: 40px;
  height: 40px;
  padding: 0 8px;
  border: 1px solid #e0e0e0;
  background: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-number:hover {
  background: #f5f5f5;
  border-color: #ff6b35;
  color: #ff6b35;
}

.page-number.active {
  background: #ff6b35;
  border-color: #ff6b35;
  color: white;
}

.page-number.active:hover {
  background: #e55a2b;
  border-color: #e55a2b;
}

.page-ellipsis {
  padding: 0 8px;
  color: #999;
  font-weight: 500;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #007bff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.modal-content.large {
  width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: #f0f0f0;
  color: #333;
}

/* Details Modal */
.details-content {
  padding: 20px;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.detail-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  min-width: 120px;
}

.detail-item span {
  color: #333;
  font-weight: 500;
}

.amount {
  color: #28a745;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .filter-grid .filter-group:nth-child(4),
  .filter-grid .filter-group:nth-child(5),
  .filter-grid .filter-group:nth-child(6),
  .filter-grid .filter-group:nth-child(7) {
    grid-column: 1;
    grid-row: auto;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .modal-content.large {
    width: 95%;
  }
}
</style>
