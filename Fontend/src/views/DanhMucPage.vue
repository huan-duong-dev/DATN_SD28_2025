<template>
  <div class="danh-muc-page">
    <!-- POS Header -->
    <PosHeader title="Quản Lý Danh Mục" />

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-row">
        <!-- Search Input -->
        <div class="filter-item search-item">
          <label class="filter-label">
            <font-awesome-icon icon="search" class="label-icon" />
            Tìm kiếm
          </label>
          <input
            v-model="searchText"
            type="text"
            placeholder="Nhập mã hoặc tên danh mục..."
            class="filter-input"
            @input="applyFilters"
          />
        </div>

        <!-- Status Filter -->
        <div class="filter-item">
          <label class="filter-label">
            <font-awesome-icon icon="filter" class="label-icon" />
            Trạng thái
          </label>
          <select v-model="statusFilter" class="filter-select" @change="applyFilters">
            <option value="all">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>

        <!-- Clear Filter Button -->
        <button
          v-if="searchText || statusFilter !== 'all'"
          class="clear-filter-btn"
          @click="clearAllFilters"
        >
          <font-awesome-icon icon="times" />
          Xóa bộ lọc
        </button>
      </div>
    </div>

    <!-- Add Danh Muc Button -->
    <div class="add-customer-section">
      <div class="add-buttons">
        <button class="btn-export-excel" @click="exportExcel">
          <font-awesome-icon icon="file-excel" />
          Xuất Excel
        </button>
        <button class="btn-add-customer" @click="addDanhMuc">
          <font-awesome-icon icon="plus-circle" />
          Thêm Danh Mục
        </button>
      </div>
    </div>

    <!-- Table Section -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="checkbox-column">
              <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
            </th>
            <th class="stt-column">STT</th>
            <th>Mã Danh Mục</th>
            <th>Tên Danh Mục</th>
            <th>Hình Ảnh</th>
            <th>Ngày Tạo</th>
            <th>Trạng Thái</th>
            <th class="action-column">Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(item, index) in paginatedDanhMucs"
            :key="item.id"
            :class="{ 'selected-row': selectedDanhMucIds.includes(item.id) }"
          >
            <td class="checkbox-column">
              <input
                type="checkbox"
                :checked="selectedDanhMucIds.includes(item.id)"
                @change="toggleSelectDanhMuc(item.id)"
              />
            </td>
            <td class="stt-column">{{ startItem + index }}</td>
            <td>
              <span class="ma-danh-muc">{{
                item.maDanhMuc || `DM${String(item.id).padStart(5, '0')}`
              }}</span>
            </td>
            <td>
              <span class="font-medium">{{ item.tenDanhMuc }}</span>
            </td>
            <td>
              <div v-if="item.hinhAnhs && item.hinhAnhs.length > 0" class="danh-muc-images">
                <img
                  v-for="(hinh, imgIndex) in item.hinhAnhs.slice(0, 3)"
                  :key="imgIndex"
                  :src="getImageUrl(hinh.urlAnh)"
                  :alt="item.tenDanhMuc"
                  class="danh-muc-thumbnail"
                  @error="handleImageError"
                  @click="openImageModal(item.hinhAnhs, imgIndex)"
                />
                <span
                  v-if="item.hinhAnhs.length > 3"
                  class="more-images"
                  @click="openImageModal(item.hinhAnhs, 0)"
                >
                  +{{ item.hinhAnhs.length - 3 }}
                </span>
              </div>
              <div v-else class="no-image">
                <span class="no-image-text">Chưa có hình ảnh</span>
              </div>
            </td>
            <td>
              <span class="ngay-tao">{{ formatDate(item.ngayTao || '') }}</span>
            </td>
            <td>
              <span
                class="status-badge"
                :class="item.trangThai ? 'status-active' : 'status-inactive'"
              >
                {{ item.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
              </span>
            </td>
            <td class="action-column">
              <div class="action-buttons">
                <button class="btn-view" @click="viewDanhMuc(item)" title="Xem chi tiết">
                  <font-awesome-icon icon="eye" />
                </button>
                <button class="btn-edit" @click="editDanhMuc(item)" title="Chỉnh sửa">
                  <font-awesome-icon icon="edit" />
                </button>
                <label class="toggle-switch" title="Đổi trạng thái">
                  <input
                    type="checkbox"
                    :checked="item.trangThai === 1"
                    @change="toggleDanhMucStatus(item)"
                  />
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </td>
          </tr>
          <tr v-if="paginatedDanhMucs.length === 0">
            <td colspan="8" class="no-data">
              <font-awesome-icon icon="inbox" class="no-data-icon" />
              <p>Không có dữ liệu</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination-container">
      <div class="pagination-info">
        Hiển thị {{ startItem }} - {{ endItem }} / {{ filteredDanhMucs.length }} danh mục
      </div>
      <div class="pagination-controls">
        <button class="pagination-btn" :disabled="currentPage === 1" @click="previousPage">
          <font-awesome-icon icon="chevron-left" />
        </button>

        <button
          v-for="page in getVisiblePages()"
          :key="page"
          class="pagination-btn"
          :class="{
            active: page === currentPage,
            ellipsis: page === '...',
          }"
          :disabled="page === '...'"
          @click="typeof page === 'number' && goToPage(page)"
        >
          {{ page }}
        </button>

        <button class="pagination-btn" :disabled="currentPage === totalPages" @click="nextPage">
          <font-awesome-icon icon="chevron-right" />
        </button>
      </div>
      <div class="items-per-page">
        <label>Hiển thị:</label>
        <select v-model.number="itemsPerPage" @change="currentPage = 1">
          <option :value="10">10</option>
          <option :value="25">25</option>
          <option :value="50">50</option>
          <option :value="100">100</option>
        </select>
      </div>
    </div>

    <!-- View Modal -->
    <ViewModal
      v-model="showViewModal"
      title="Chi tiết Danh Mục"
      :data="viewingDanhMuc || {}"
      :fields="viewFields"
    />

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <!-- Enhanced Danh Mục Form Modal -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="enhanced-modal">
        <div class="modal-header">
          <div class="modal-title">
            <h3>{{ editingDanhMuc ? 'Sửa Danh Mục' : 'Thêm Danh Mục Mới' }}</h3>
          </div>
          <button @click="showForm = false" class="close-btn">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-fields">
            <div class="field-group">
              <label class="field-label">Mã Danh Mục</label>
              <input
                v-model="formData.maDanhMuc"
                type="text"
                placeholder="VD: DM001 (để trống để tự động)"
                class="form-input"
                @blur="formData.maDanhMuc = normalizeCode(formData.maDanhMuc)"
              />
              <div class="field-hint">⚠️ Mã chỉ chứa chữ viết hoa, số, gạch ngang và gạch dưới</div>
            </div>

            <div class="field-group">
              <label class="field-label">Tên Danh Mục *</label>
              <input
                v-model="formData.tenDanhMuc"
                type="text"
                placeholder="VD: Điện Thoại"
                class="form-input"
                @blur="formData.tenDanhMuc = normalizeName(formData.tenDanhMuc)"
                required
              />
              <div class="field-hint">⚠️ Tên không được có dấu cách liên tiếp</div>
            </div>

            <div class="field-group">
              <label class="field-label">Trạng Thái</label>
              <div class="toggle-field">
                <label class="toggle-switch-large">
                  <input
                    type="checkbox"
                    :checked="formData.trangThai === 1"
                    @change="formData.trangThai = $event.target.checked ? 1 : 0"
                  />
                  <span class="toggle-slider-large"></span>
                </label>
                <span class="toggle-label">{{
                  formData.trangThai ? 'Hoạt động' : 'Ngừng hoạt động'
                }}</span>
              </div>
            </div>

            <!-- Image Upload Section -->
            <div class="image-upload-section">
              <label class="form-label">Hình ảnh danh mục</label>

              <div class="image-upload-area">
                <!-- Hiển thị ảnh hiện tại trong vùng upload -->
                <div
                  v-if="editingDanhMuc && editingDanhMuc.hinhAnhs && editingDanhMuc.hinhAnhs.length > 0"
                  class="existing-images-in-upload"
                >
                  <h4 class="existing-images-title">Ảnh hiện tại:</h4>
                  <div class="existing-images-grid">
                    <div
                      v-for="(hinh, index) in editingDanhMuc.hinhAnhs"
                      :key="hinh.id"
                      class="existing-image-item"
                    >
                      <img
                        :src="getImageUrl(hinh.urlAnh)"
                        :alt="`Ảnh ${index + 1}`"
                        class="existing-image"
                        @error="handleImageError"
                        @click="openImageModal(editingDanhMuc.hinhAnhs, index)"
                      />
                      <button
                        @click.stop="removeExistingImage(hinh.id, index)"
                        class="remove-existing-image"
                        title="Xóa ảnh này"
                      >
                        ×
                      </button>
                    </div>
                  </div>
                </div>

                <div
                  class="upload-zone"
                  @click="triggerFileInput"
                  @dragover.prevent
                  @drop.prevent="handleDrop"
                >
                  <div class="upload-placeholder">
                    <div class="upload-icon">📁</div>
                    <p class="upload-hint">Kéo thả hình ảnh vào đây hoặc click để chọn</p>
                    <p class="upload-hint">Hỗ trợ: JPG, PNG, GIF (tối đa 5MB)</p>
                  </div>
                </div>
                <input
                  type="file"
                  ref="fileInput"
                  multiple
                  accept="image/*"
                  @change="handleFileSelect"
                  style="display: none"
                />
                <div v-if="selectedFiles.length > 0" class="selected-files">
                  <h4 class="new-images-title">Ảnh mới sẽ thêm:</h4>
                  <div v-for="(file, index) in selectedFiles" :key="index" class="file-preview">
                    <img
                      v-if="file.preview"
                      :src="file.preview as string"
                      :alt="file.name"
                      class="preview-image"
                    />
                    <div class="file-info">
                      <div class="file-name">{{ file.name }}</div>
                      <div class="file-size">{{ formatFileSize(file.size) }}</div>
                    </div>
                    <button @click="removeFile(index)" class="remove-file">×</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="showForm = false" class="btn-cancel">Hủy</button>
          <button @click="submitForm" class="btn-submit">
            {{ editingDanhMuc ? 'Cập Nhật' : 'Thêm Mới' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Image View Modal -->
    <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
      <div class="image-modal" @click.stop>
        <button class="close-btn" @click="closeImageModal">&times;</button>
        <div class="main-image-container">
          <img
            v-if="currentImages && currentImages.length > 0"
            :src="currentImageUrl"
            :alt="`Hình ${currentImageIndex + 1}`"
            class="main-image"
            @error="handleImageError"
          />
          <div v-else class="no-image-placeholder">
            <span>Không có hình ảnh</span>
          </div>
        </div>
        <div class="image-gallery">
          <img
            v-for="(hinh, index) in currentImages"
            :key="index"
            :src="getImageUrl(hinh.urlAnh)"
            :alt="`Hình ${index + 1}`"
            class="gallery-image"
            :class="{ active: currentImageIndex === index }"
            @click="setCurrentImage(index)"
            @error="handleImageError"
          />
        </div>
        <div class="image-navigation" v-if="currentImages && currentImages.length > 1">
          <button
            class="nav-btn prev-btn"
            @click="previousImage"
          >
            ‹
          </button>
          <span class="image-counter">
            {{ currentImageIndex + 1 }} / {{ currentImages.length }}
          </span>
          <button
            class="nav-btn next-btn"
            @click="nextImage"
          >
            ›
          </button>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Teleport to="body">
      <Toast ref="toastRef" />
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import ViewModal from '@/components/ViewModal.vue'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import { useAttributeValidation } from '@/composables/useAttributeValidation'
import * as XLSX from 'xlsx'

const {
  validateCode,
  validateName,
  checkDuplicateCode,
  checkDuplicateName,
  normalizeCode,
  normalizeName,
} = useAttributeValidation()

interface HinhAnh {
  id: number
  urlAnh: string
  ngayTao: string
  ngaySua: string
  trangThai: number
}

interface DanhMuc {
  id: number
  maDanhMuc: string
  tenDanhMuc: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  hinhAnhs?: HinhAnh[]
}

const danhMucs = ref<DanhMuc[]>([])
const showForm = ref(false)
const editingDanhMuc = ref<DanhMuc | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// Filter states
const searchText = ref('')
const statusFilter = ref('all')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(10)

// Selection states
const selectedDanhMucIds = ref<number[]>([])

// View modal state
const showViewModal = ref(false)
const viewingDanhMuc = ref<DanhMuc | null>(null)

// Image modal state
const showImageModal = ref(false)
const currentImages = ref<HinhAnh[]>([])
const currentImageIndex = ref(0)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

// Form data
const formData = ref({
  maDanhMuc: '',
  tenDanhMuc: '',
  trangThai: 1,
})

// Computed properties
const filteredDanhMucs = computed(() => {
  return danhMucs.value.filter((item: DanhMuc) => {
    const matchesSearch =
      searchText.value.trim() === '' ||
      item.tenDanhMuc.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (item.maDanhMuc && item.maDanhMuc.toLowerCase().includes(searchText.value.toLowerCase()))

    const matchesStatus =
      statusFilter.value === 'all' ||
      (statusFilter.value === 'active' && item.trangThai) ||
      (statusFilter.value === 'inactive' && !item.trangThai)

    return matchesSearch && matchesStatus
  })
})

const paginatedDanhMucs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredDanhMucs.value.slice(start, end)
})

const totalPages = computed(() => Math.ceil(filteredDanhMucs.value.length / itemsPerPage.value))

const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() =>
  Math.min(currentPage.value * itemsPerPage.value, filteredDanhMucs.value.length),
)

const isAllSelected = computed({
  get: () =>
    paginatedDanhMucs.value.length > 0 &&
    paginatedDanhMucs.value.every((dm: DanhMuc) => selectedDanhMucIds.value.includes(dm.id)),
  set: (value: boolean) => {
    if (value) {
      selectedDanhMucIds.value = [
        ...new Set([
          ...selectedDanhMucIds.value,
          ...paginatedDanhMucs.value.map((dm: DanhMuc) => dm.id),
        ]),
      ]
    } else {
      const currentPageIds = paginatedDanhMucs.value.map((dm: DanhMuc) => dm.id)
      selectedDanhMucIds.value = selectedDanhMucIds.value.filter(
        (id: number) => !currentPageIds.includes(id),
      )
    }
  },
})

// Image upload state
const selectedFiles = ref<FileWithPreview[]>([])
const isUploading = ref(false)

// Images to be deleted (temporary removal)
const imagesToDelete = ref<number[]>([])

// Methods
const loadDanhMucs = async () => {
  try {
    const { data } = await api.get('/api/danh-muc')
    // Sắp xếp theo ngày tạo giảm dần (mới nhất lên đầu)
    danhMucs.value = data.sort((a: DanhMuc, b: DanhMuc) => {
      // Nếu có ngayTao, so sánh theo ngày tạo
      if (a.ngayTao && b.ngayTao) {
        return new Date(b.ngayTao).getTime() - new Date(a.ngayTao).getTime()
      }
      // Nếu không có ngayTao, so sánh theo ID (ID lớn hơn = mới hơn)
      return b.id - a.id
    })
  } catch (error) {
    console.error('Lỗi khi tải danh sách danh mục:', error)
    showToast('Lỗi khi tải danh sách danh mục', 'error')
  }
}

const openForm = (danhMuc: DanhMuc | null = null) => {
  editingDanhMuc.value = danhMuc
  selectedFiles.value = []
  imagesToDelete.value = []

  if (danhMuc) {
    formData.value = {
      maDanhMuc: danhMuc.maDanhMuc || '',
      tenDanhMuc: danhMuc.tenDanhMuc,
      trangThai: danhMuc.trangThai === 1 ? 1 : 0,
    }
  } else {
    formData.value = {
      maDanhMuc: '',
      tenDanhMuc: '',
      trangThai: 1,
    }
  }

  showForm.value = true
}

const submitForm = async () => {
  await handleFormSubmit(formData.value)
}

const handleFormSubmit = async (submitData: Record<string, unknown>) => {
  try {
    // Normalize data before validation
    if (submitData.maDanhMuc) {
      submitData.maDanhMuc = normalizeCode(submitData.maDanhMuc as string)
    }
    if (submitData.tenDanhMuc) {
      submitData.tenDanhMuc = normalizeName(submitData.tenDanhMuc as string)
    }

    // Validate mã danh mục if provided
    if (submitData.maDanhMuc) {
      const codeValidation = validateCode(submitData.maDanhMuc as string, 'Mã danh mục')
      if (!codeValidation.valid) {
        showToast(codeValidation.errors[0] || 'Mã danh mục không hợp lệ', 'error')
        return
      }

      // Check duplicate code (only if not editing or code changed)
      if (!editingDanhMuc.value || editingDanhMuc.value.maDanhMuc !== submitData.maDanhMuc) {
        const duplicateCodeResult = await checkDuplicateCode(
          '/api/danh-muc',
          'maDanhMuc',
          submitData.maDanhMuc as string,
          editingDanhMuc.value?.id,
        )
        if (!duplicateCodeResult.valid) {
          showToast(duplicateCodeResult.errors[0] || 'Mã danh mục đã tồn tại', 'error')
          return
        }
      }
    }

    // Validate tên danh mục
    if (!submitData.tenDanhMuc || (submitData.tenDanhMuc as string).trim() === '') {
      showToast('Tên danh mục không được để trống', 'error')
      return
    }

    const nameValidation = validateName(submitData.tenDanhMuc as string, 'Tên danh mục')
    if (!nameValidation.valid) {
      showToast(nameValidation.errors[0] || 'Tên danh mục không hợp lệ', 'error')
      return
    }

    // Check duplicate name (only if not editing or name changed)
    if (!editingDanhMuc.value || editingDanhMuc.value.tenDanhMuc !== submitData.tenDanhMuc) {
      const duplicateNameResult = await checkDuplicateName(
        '/api/danh-muc',
        'tenDanhMuc',
        submitData.tenDanhMuc as string,
        editingDanhMuc.value?.id,
      )
      if (!duplicateNameResult.valid) {
        showToast(duplicateNameResult.errors[0] || 'Tên danh mục đã tồn tại', 'error')
        return
      }
    }

    let danhMucId: number

    if (editingDanhMuc.value) {
      // Cập nhật: chỉ gửi các field cần thiết
      const updatePayload = {
        maDanhMuc: submitData.maDanhMuc,
        tenDanhMuc: submitData.tenDanhMuc,
        trangThai: submitData.trangThai,
      }
      await api.put(`/api/danh-muc/${editingDanhMuc.value.id}`, updatePayload)
      danhMucId = editingDanhMuc.value.id
      showToast('Cập nhật danh mục thành công!', 'success')
    } else {
      // Thêm mới: chỉ gửi các field cần thiết, backend tự set ngayTao
      const createPayload = {
        maDanhMuc: submitData.maDanhMuc || null,
        tenDanhMuc: submitData.tenDanhMuc,
        trangThai: 1,
      }
      const response = await api.post('/api/danh-muc', createPayload)
      danhMucId = response.data.id
      showToast('Thêm danh mục thành công!', 'success')
    }

    // Xóa ảnh đã đánh dấu xóa
    if (imagesToDelete.value.length > 0) {
      await deleteMarkedImages()
      imagesToDelete.value = []
    }

    // Upload hình ảnh nếu có
    if (selectedFiles.value.length > 0) {
      await uploadImagesForDanhMuc(danhMucId)
    }

    await loadDanhMucs()
    showForm.value = false
    editingDanhMuc.value = null
    selectedFiles.value = []
    imagesToDelete.value = []
  } catch (error) {
    const err = error as { response?: { data?: { message?: string } }; message?: string }
    console.error('Lỗi khi lưu danh mục:', error)
    showToast('Lỗi khi lưu danh mục: ' + (err.response?.data?.message || err.message), 'error')
  }
}

const toggleDanhMucStatus = async (danhMuc: DanhMuc) => {
  try {
    const newStatus = danhMuc.trangThai === 1 ? 0 : 1
    await api.put(`/api/danh-muc/${danhMuc.id}`, { trangThai: newStatus })
    await loadDanhMucs()
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    showToast(`Đã chuyển danh mục "${danhMuc.tenDanhMuc}" sang trạng thái ${statusText}`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    showToast(
      'Lỗi khi cập nhật trạng thái: ' + (error.response?.data?.message || error.message),
      'error',
    )
  }
}

const exportExcel = () => {
  try {
    // Lấy danh mục đã chọn hoặc tất cả
    const danhMucsToExport =
      selectedDanhMucIds.value.length > 0
        ? danhMucs.value.filter((dm) => selectedDanhMucIds.value.includes(dm.id))
        : danhMucs.value

    if (danhMucsToExport.length === 0) {
      showToast('Không có dữ liệu danh mục để xuất Excel', 'warning')
      return
    }

    // Tạo dữ liệu cho Excel
    const excelData = danhMucsToExport.map((dm, index) => ({
      STT: index + 1,
      'Mã Danh Mục': dm.maDanhMuc || `DM${String(dm.id).padStart(5, '0')}`,
      'Tên Danh Mục': dm.tenDanhMuc,
      'Số Hình Ảnh': dm.hinhAnhs ? dm.hinhAnhs.length : 0,
      'Trạng Thái': dm.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động',
      'Ngày Tạo': dm.ngayTao ? new Date(dm.ngayTao).toLocaleDateString('vi-VN') : '',
      'Ngày Cập Nhật': dm.ngayCapNhat ? new Date(dm.ngayCapNhat).toLocaleDateString('vi-VN') : '',
    }))

    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)

    // Điều chỉnh độ rộng cột tự động
    const colWidths: { wch: number }[] = []
    const headers = Object.keys(excelData[0])

    // Tính toán độ rộng cho mỗi cột
    headers.forEach((header) => {
      let maxLength = header.length

      // Kiểm tra độ dài của dữ liệu trong cột
      excelData.forEach((row) => {
        const cellValue = String(row[header as keyof typeof row] || '')
        if (cellValue.length > maxLength) {
          maxLength = cellValue.length
        }
      })

      // Đặt độ rộng tối thiểu và tối đa
      const width = Math.min(Math.max(maxLength + 2, 10), 50)
      colWidths.push({ wch: width })
    })

    // Áp dụng độ rộng cột
    ws['!cols'] = colWidths

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách Danh Mục')

    // Xuất file Excel
    const fileName = `danh_sach_danh_muc_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    showToast('Đã export danh sách danh mục thành công!', 'success')
  } catch (error) {
    console.error('Lỗi khi export Excel:', error)
    showToast('Có lỗi xảy ra khi export Excel', 'error')
  }
}

// Pagination functions
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const getVisiblePages = () => {
  const pages = []
  const maxVisible = 5

  if (totalPages.value <= maxVisible) {
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    if (currentPage.value <= 3) {
      for (let i = 1; i <= 4; i++) pages.push(i)
      pages.push('...')
      pages.push(totalPages.value)
    } else if (currentPage.value >= totalPages.value - 2) {
      pages.push(1)
      pages.push('...')
      for (let i = totalPages.value - 3; i <= totalPages.value; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      pages.push(currentPage.value - 1)
      pages.push(currentPage.value)
      pages.push(currentPage.value + 1)
      pages.push('...')
      pages.push(totalPages.value)
    }
  }

  return pages
}

// Filter functions
const applyFilters = () => {
  currentPage.value = 1
}

const clearAllFilters = () => {
  searchText.value = ''
  statusFilter.value = 'all'
  currentPage.value = 1
}

// Selection functions
const toggleSelectAll = () => {
  isAllSelected.value = !isAllSelected.value
}

const toggleSelectDanhMuc = (id: number) => {
  const index = selectedDanhMucIds.value.indexOf(id)
  if (index > -1) {
    selectedDanhMucIds.value.splice(index, 1)
  } else {
    selectedDanhMucIds.value.push(id)
  }
}

// View fields configuration
const viewFields = [
  { key: 'maDanhMuc', label: 'Mã Danh Mục', type: 'text' },
  { key: 'tenDanhMuc', label: 'Tên Danh Mục', type: 'text' },
  { key: 'trangThai', label: 'Trạng thái', type: 'text' },
  { key: 'ngayTao', label: 'Ngày tạo', type: 'date' },
  { key: 'ngayCapNhat', label: 'Ngày cập nhật', type: 'date' },
]

// View modal function
const viewDanhMuc = (danhMuc: DanhMuc) => {
  viewingDanhMuc.value = danhMuc
  showViewModal.value = true
}

// Add/Edit functions
const addDanhMuc = () => {
  openForm(null)
}

const editDanhMuc = (danhMuc: DanhMuc) => {
  openForm(danhMuc)
}

// Format date helper
const formatDate = (date: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleConfirm = () => {
  if (pendingAction.value) {
    pendingAction.value()
  }
  showConfirmModal.value = false
  pendingAction.value = null
}

const handleCancel = () => {
  showConfirmModal.value = false
  pendingAction.value = null
}

// Image upload methods

const triggerFileInput = () => {
  const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
  fileInput?.click()
}

interface FileWithPreview {
  file: File
  name: string
  size: number
  preview: string | ArrayBuffer | null
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files) return
  const files = Array.from(target.files)
  processFiles(files)
}

const handleDrop = (event: DragEvent) => {
  if (!event.dataTransfer) return
  const files = Array.from(event.dataTransfer.files)
  processFiles(files)
}

const processFiles = (files: File[]) => {
  const imageFiles = files.filter((file) => file.type.startsWith('image/'))

  // Kiểm tra số lượng ảnh hiện tại
  const currentImageCount =
    (editingDanhMuc.value?.hinhAnhs?.length || 0) + selectedFiles.value.length

  // Chỉ cho phép 1 ảnh
  if (currentImageCount >= 1) {
    showToast('Chỉ được phép tải lên 1 ảnh!', 'warning')
    return
  }

  // Chỉ lấy ảnh đầu tiên nếu có nhiều ảnh được chọn
  const fileToProcess = imageFiles[0]
  if (!fileToProcess) return

  if (imageFiles.length > 1) {
    showToast('Chỉ được phép tải lên 1 ảnh! Đã chọn ảnh đầu tiên.', 'warning')
  }

  if (fileToProcess.size > 5 * 1024 * 1024) {
    showToast(`File ${fileToProcess.name} quá lớn (tối đa 5MB)`, 'warning')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    if (e.target) {
      ;(selectedFiles.value as FileWithPreview[]).push({
        file: fileToProcess,
        name: fileToProcess.name,
        size: fileToProcess.size,
        preview: e.target.result,
      })
    }
  }
  reader.readAsDataURL(fileToProcess)
}

const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

const removeExistingImage = (imageId: number, index: number) => {
  if (!imageId) {
    showToast('ID ảnh không hợp lệ', 'error')
    return
  }

  console.log('Đánh dấu xóa ảnh ID:', imageId)
  ;(imagesToDelete.value as number[]).push(imageId)

  if (editingDanhMuc.value && editingDanhMuc.value.hinhAnhs) {
    editingDanhMuc.value.hinhAnhs.splice(index, 1)
  }

  showToast('Ảnh đã được đánh dấu xóa. Bấm Cập nhật để hoàn tất!', 'info')
}

const deleteMarkedImages = async () => {
  if (imagesToDelete.value.length === 0) return

  console.log('Bắt đầu xóa ảnh, IDs:', imagesToDelete.value)
  let successCount = 0
  let errorCount = 0

  // Xóa từng ảnh và đếm số lượng thành công/thất bại
  for (const imageId of imagesToDelete.value) {
    try {
      await api.delete(`/api/upload/image/delete/${imageId}`)
      console.log(`Đã xóa ảnh ID: ${imageId}`)
      successCount++
    } catch (error: any) {
      errorCount++
      // Bỏ qua lỗi 404 (ảnh đã bị xóa)
      if (error.response?.status !== 404) {
        console.error(`Lỗi khi xóa ảnh ID ${imageId}:`, error)
      } else {
        console.warn(`Ảnh ID ${imageId} không tồn tại`)
        successCount++ // Tính là thành công vì mục đích là xóa
      }
    }
  }

  imagesToDelete.value = []

  if (errorCount > 0 && successCount === 0) {
    console.error('Không thể xóa bất kỳ ảnh nào')
    showToast('Không thể xóa ảnh', 'error')
    // Không throw error để không làm dừng quá trình cập nhật
  } else if (successCount > 0) {
    console.log(`Đã xóa thành công ${successCount} ảnh`)
    if (errorCount > 0) {
      showToast(`Đã xóa ${successCount} ảnh, ${errorCount} ảnh thất bại`, 'warning')
    }
  }
}

const uploadImagesForDanhMuc = async (danhMucId: number) => {
  if (selectedFiles.value.length === 0) return

  isUploading.value = true

  try {
    for (const fileData of selectedFiles.value as FileWithPreview[]) {
      const formData = new FormData()
      formData.append('file', fileData.file)
      formData.append('idDanhMuc', danhMucId.toString())

      await api.post('/api/upload/image/danh-muc', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
    }

    showToast(`Upload thành công ${selectedFiles.value.length} hình ảnh!`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi upload hình ảnh:', error)
    showToast(
      'Lỗi khi upload hình ảnh: ' + (error.response?.data?.message || error.message),
      'error',
    )
  } finally {
    isUploading.value = false
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// Helper function to get full image URL
const getImageUrl = (urlAnh: string) => {
  if (!urlAnh) return ''

  // If already a full URL (Cloudinary or https), return as is
  if (urlAnh.startsWith('http://') || urlAnh.startsWith('https://')) {
    return urlAnh
  }

  // If relative path, prepend base URL
  return `http://localhost:8080${urlAnh.startsWith('/') ? '' : '/'}${urlAnh}`
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.style.display = 'none'
  console.error('Image load error:', target.src)
}

// Image modal methods
const currentImageUrl = computed(() => {
  if (!currentImages.value || currentImages.value.length === 0) {
    console.warn('No modal images available')
    return ''
  }

  if (currentImageIndex.value < 0 || currentImageIndex.value >= currentImages.value.length) {
    console.warn('Invalid modal index:', currentImageIndex.value)
    return ''
  }

  const currentImage = currentImages.value[currentImageIndex.value]
  if (!currentImage) {
    console.warn('Current modal image is null/undefined')
    return ''
  }

  if (!currentImage.urlAnh) {
    console.warn('Current modal image missing urlAnh:', currentImage)
    return ''
  }

  const fullUrl = getImageUrl(currentImage.urlAnh)
  console.log('Computed modal URL:', {
    originalUrl: currentImage.urlAnh,
    fullUrl: fullUrl,
    index: currentImageIndex.value
  })

  return fullUrl
})

const openImageModal = (images: HinhAnh[], startIndex = 0) => {
  if (!images || images.length === 0) {
    console.warn('No images to preview')
    return
  }

  console.log('Opening image modal:', {
    images,
    startIndex,
    imagesCount: images.length,
    firstImage: images[0]
  })

  // Filter out images without urlAnh
  const validImages = images.filter(img => img && img.urlAnh)

  if (validImages.length === 0) {
    console.error('No valid images found (all images missing urlAnh)')
    toastRef.value?.error('Lỗi', 'Không có ảnh hợp lệ để hiển thị')
    return
  }

  currentImages.value = validImages
  currentImageIndex.value = Math.max(0, Math.min(startIndex, validImages.length - 1))
  showImageModal.value = true

  console.log('Modal state:', {
    currentImagesCount: currentImages.value.length,
    currentIndex: currentImageIndex.value,
    currentImageUrl: currentImageUrl.value
  })
}

const closeImageModal = () => {
  showImageModal.value = false
  currentImages.value = []
  currentImageIndex.value = 0
}

const setCurrentImage = (index: number) => {
  currentImageIndex.value = index
}

const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else if (currentImages.value.length > 1) {
    // Loop to last image
    currentImageIndex.value = currentImages.value.length - 1
  }
}

const nextImage = () => {
  if (currentImageIndex.value < currentImages.value.length - 1) {
    currentImageIndex.value++
  } else if (currentImages.value.length > 1) {
    // Loop to first image
    currentImageIndex.value = 0
  }
}

// Keyboard navigation for image modal
const handleKeydown = (event: KeyboardEvent) => {
  if (!showImageModal.value) return

  switch (event.key) {
    case 'ArrowLeft':
      event.preventDefault()
      previousImage()
      break
    case 'ArrowRight':
      event.preventDefault()
      nextImage()
      break
    case 'Escape':
      event.preventDefault()
      closeImageModal()
      break
  }
}

const showToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'success') => {
  if (toastRef.value) {
    const title =
      type === 'success'
        ? 'Thành công'
        : type === 'error'
          ? 'Lỗi'
          : type === 'warning'
            ? 'Cảnh báo'
            : 'Thông báo'
    toastRef.value.showToast(type, title, message, 3000)
  }
}

// Status toggle methods
const isUpdatingStatus = ref(false)

onMounted(() => {
  loadDanhMucs()
  // Add keyboard navigation for image modal
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // Remove keyboard navigation
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* ===== Page Container ===== */
.danh-muc-page {
  padding: 20px;
  background: #f1f5f9;
  min-height: 100vh;
}

/* ===== Filter Section ===== */
.filter-section {
  background: white;
  padding: 24px;
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
  flex: 1;
}

.filter-item.search-item {
  flex: 2;
  min-width: 300px;
}

.filter-label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.label-icon {
  color: #f97316;
  font-size: 14px;
}

.filter-input,
.filter-select {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #1f2937;
  font-weight: 500;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
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

.clear-filter-btn {
  padding: 10px 16px;
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
  align-self: flex-end;
}

.clear-filter-btn:hover {
  background: #e2e8f0;
  color: #374151;
  transform: translateY(-1px);
}

/* ===== Add Danh Muc Section ===== */
.add-customer-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: 0;
}

.add-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-export-excel {
  background: #28a745;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.3);
  margin-right: 12px;
}

.btn-export-excel:hover {
  background: #218838;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.4);
}

.btn-add-customer {
  background: #f97316;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-add-customer:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

/* ===== Table Container ===== */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: #f8fafc;
}

.data-table th {
  padding: 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
  color: #1f2937;
}

.data-table tbody tr {
  transition: background 0.15s ease;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.data-table tbody tr.selected-row {
  background: #fef3e6;
}

.checkbox-column {
  width: 50px;
  text-align: center;
}

.stt-column {
  width: 60px;
  text-align: center;
  font-weight: 600;
  color: #64748b;
}

.action-column {
  width: 180px;
  text-align: center;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.no-data p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

/* ===== Status Badge ===== */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #d1fae5;
  color: #065f46;
}

.status-inactive {
  background: #fee2e2;
  color: #991b1b;
}

/* ===== Action Buttons in Table ===== */
.action-column .action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.btn-view,
.btn-edit {
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
}

.btn-icon {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.btn-view {
  background: #dbeafe;
  color: #1e40af;
}

.btn-view:hover {
  background: #bfdbfe;
  transform: translateY(-1px);
}

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-edit:hover {
  background: #fde68a;
  transform: translateY(-1px);
}

.btn-delete {
  background: #fee2e2;
  color: #991b1b;
}

.btn-delete:hover {
  background: #fecaca;
  transform: translateY(-1px);
}

/* ===== Toggle Switch ===== */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  cursor: pointer;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  border-radius: 24px;
  transition: 0.3s;
}

.toggle-slider:before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  border-radius: 50%;
  transition: 0.3s;
}

.toggle-switch input:checked + .toggle-slider {
  background-color: #10b981;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

/* ===== Pagination ===== */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
  gap: 16px;
}

.pagination-info {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-btn {
  padding: 8px 12px;
  min-width: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  transition: all 0.2s ease;
}

.pagination-btn:hover:not(:disabled):not(.ellipsis) {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.pagination-btn.active {
  background: #f97316;
  color: white;
  border-color: #f97316;
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-btn.ellipsis {
  cursor: default;
  border: none;
  background: transparent;
}

.items-per-page {
  display: flex;
  align-items: center;
  gap: 8px;
}

.items-per-page label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.items-per-page select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}

/* ===== Field Hints ===== */
.field-hint {
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
  margin-top: 4px;
}

/* ===== Existing DanhMuc-specific styles ===== */
.danh-muc-name {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.danh-muc-images {
  display: flex;
  gap: 4px;
  align-items: center;
  margin-top: 4px;
}

.danh-muc-thumbnail {
  width: 32px;
  height: 32px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.more-images {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 10px;
}

.ma-danh-muc {
  font-family: 'Courier New', monospace;
  background: #f8f9fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.ngay-tao {
  font-size: 12px;
  color: #666;
}

/* Image column styles */
.danh-muc-images {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.danh-muc-thumbnail {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.danh-muc-thumbnail:hover {
  transform: scale(1.1);
  border-color: #007bff;
}

.more-images {
  font-size: 11px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  background: #f8f9fa;
  border: 1px dashed #ddd;
  border-radius: 4px;
}

.no-image-text {
  font-size: 11px;
  color: #999;
  font-style: italic;
}

/* Image Upload Modal Styles */
.image-upload-modal {
  width: 600px;
  max-width: 90vw;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #007bff;
  background: #f0f8ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-icon {
  font-size: 48px;
  opacity: 0.5;
}

.upload-hint {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.selected-files {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
}

.preview-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 500;
  font-size: 14px;
}

.file-size {
  font-size: 12px;
  color: #666;
}

.remove-file {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.remove-file:hover {
  background: #c82333;
}

.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-height: 90vh;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
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
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

/* Action buttons */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.edit-btn,
.upload-btn {
  background: none;
  border: none;
  padding: 8px;
  margin: 0 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  background-color: #e3f2fd;
  transform: translateY(-1px);
}

.upload-btn:hover {
  background-color: #f0f9ff;
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* Status styles */
.status-active {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-inactive {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Toggle Switch Styles */
.status-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
  cursor: pointer;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

.toggle-switch input:checked + .toggle-slider {
  background-color: #28a745;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.toggle-switch input:disabled + .toggle-slider {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Form Image Upload Styles */
.image-upload-section {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

/* Existing Images Styles */
.existing-images {
  margin-bottom: 20px;
  padding: 15px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.existing-images-in-upload {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.existing-images-in-upload .existing-images-grid {
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
}

.existing-images-in-upload .existing-image-item {
  position: relative;
  aspect-ratio: 1;
}

.existing-images-in-upload .existing-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.existing-images-in-upload .existing-image:hover {
  transform: scale(1.05);
}

.existing-images-in-upload .remove-existing-image {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
}

.existing-images-in-upload .remove-existing-image:hover {
  background: #dc3545;
  transform: scale(1.1);
}

.existing-images-title {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.existing-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.existing-image-item {
  position: relative;
  display: inline-block;
}

.existing-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #ddd;
  cursor: pointer;
  transition: all 0.2s ease;
}

.existing-image:hover {
  border-color: #007bff;
  transform: scale(1.05);
}

.remove-existing-image {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.remove-existing-image:hover {
  background: #c82333;
  transform: scale(1.1);
}

.new-images-title {
  margin: 15px 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #007bff;
}

.form-label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #333;
}

.image-upload-area {
  width: 100%;
}

.upload-zone {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-zone:hover {
  border-color: #007bff;
  background: #f0f8ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-icon {
  font-size: 48px;
  opacity: 0.5;
}

.upload-hint {
  font-size: 12px;
  color: #666;
  margin: 0;
}

/* Image Modal Styles */
.image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.image-modal {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.image-gallery {
  display: flex;
  gap: 8px;
  margin-top: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.gallery-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  opacity: 0.7;
}

.gallery-image:hover {
  opacity: 1;
  transform: scale(1.05);
}

.gallery-image.active {
  opacity: 1;
  border-color: white;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3);
}

.main-image-container {
  position: relative;
  width: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.main-image {
  max-width: 100%;
  max-height: 80vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
  display: block;
  background: transparent;
}

.no-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.image-navigation {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  display: flex;
  justify-content: space-between;
  pointer-events: none;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
  pointer-events: auto;
  margin: 0 20px;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.nav-btn.prev {
  left: 0;
}

.nav-btn.next {
  right: 0;
}

.image-counter {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 16px;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

/* Field hint styles - consistent with other attribute pages */
.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
}

/* Action buttons */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-btn {
  padding: 6px 10px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.action-icon {
  width: 16px;
  height: 16px;
}

.status-toggle {
  display: flex;
  align-items: center;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  cursor: pointer;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  border-radius: 24px;
  transition: 0.3s;
}

.toggle-slider:before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  border-radius: 50%;
  transition: 0.3s;
}

.toggle-switch input:checked + .toggle-slider {
  background-color: #10b981;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

.toggle-switch input:disabled + .toggle-slider {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== Enhanced Modal Styles ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
  backdrop-filter: blur(4px);
}

.enhanced-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.modal-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-title h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.modal-body {
  padding: 32px;
  flex: 1;
  overflow-y: auto;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-input,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.toggle-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-switch-large {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 32px;
}

.toggle-switch-large input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider-large {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  border-radius: 32px;
  transition: 0.3s;
}

.toggle-slider-large:before {
  content: '';
  position: absolute;
  height: 24px;
  width: 24px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  border-radius: 50%;
  transition: 0.3s;
}

.toggle-switch-large input:checked + .toggle-slider-large {
  background-color: #10b981;
}

.toggle-switch-large input:checked + .toggle-slider-large:before {
  transform: translateX(28px);
}

.toggle-label {
  font-weight: 500;
  color: #374151;
}

.modal-footer {
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.btn-cancel,
.btn-submit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
  border: 2px solid #e5e7eb;
}

.btn-cancel:hover {
  background: #e5e7eb;
  color: #374151;
  transform: translateY(-1px);
}

.btn-submit {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.3);
  min-width: 120px;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.4);
}

/* Image Upload Section Styles */
.image-upload-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px dashed #e5e7eb;
}

.form-label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  margin-bottom: 12px;
  display: block;
}

.existing-images {
  margin-bottom: 20px;
}

.existing-images-in-upload {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.existing-images-in-upload .existing-images-grid {
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
}

.existing-images-in-upload .existing-image-item {
  aspect-ratio: 1;
  border-radius: 8px;
}

.existing-images-in-upload .existing-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.existing-images-in-upload .existing-image:hover {
  transform: scale(1.05);
}

.existing-images-in-upload .remove-existing-image {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(220, 38, 38, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
}

.existing-images-in-upload .remove-existing-image:hover {
  background: #dc2626;
  transform: scale(1.1);
}

.existing-images-title {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 12px;
}

.existing-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.existing-image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
}

.existing-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.existing-image:hover {
  transform: scale(1.05);
}

.remove-existing-image {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(220, 38, 38, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s ease;
}

.remove-existing-image:hover {
  background: #dc2626;
  transform: scale(1.1);
}

.image-upload-area {
  margin-top: 16px;
}

.upload-zone {
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8fafc;
}

.upload-zone:hover {
  border-color: #f97316;
  background: #fef3e6;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 48px;
  opacity: 0.5;
}

.upload-hint {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.selected-files {
  margin-top: 16px;
}

.new-images-title {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 12px;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  margin-bottom: 8px;
}

.preview-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.remove-file {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.remove-file:hover {
  background: #fecaca;
  transform: scale(1.1);
}

/* Responsive Modal */
@media (max-width: 768px) {
  .enhanced-modal {
    width: 95%;
    margin: 20px;
  }

  .modal-header {
    padding: 20px 24px;
  }

  .modal-body {
    padding: 24px;
  }

  .modal-footer {
    padding: 20px 24px;
  }
}
</style>
