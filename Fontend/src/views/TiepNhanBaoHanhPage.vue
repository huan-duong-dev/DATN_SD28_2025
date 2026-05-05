<template>
  <div class="form-page">
    <PosHeader />
    <Toast ref="toastRef" />

    <div class="form-container">
      <div class="form-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Tiếp nhận Bảo hành Mới</h1>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="bao-hanh-form">
        <!-- Thông tin khách hàng -->
        <div class="form-section">
          <h2>Thông tin Khách hàng</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'user']" />
                <span>Tên khách hàng *</span>
              </label>
              <input
                type="text"
                v-model="formData.tenKhachHang"
                class="form-input"
                placeholder="Nhập tên khách hàng"
                required
              />
              <small class="form-help">Tên khách hàng hoặc thông tin liên hệ</small>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'phone']" />
                <span>Số điện thoại *</span>
              </label>
              <input
                type="tel"
                v-model="formData.soDienThoai"
                class="form-input"
                placeholder="Nhập số điện thoại"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'envelope']" />
                <span>Email (nếu có)</span>
              </label>
              <input
                type="email"
                v-model="formData.emailKhachHang"
                class="form-input"
                placeholder="Nhập email khách hàng"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'id-card']" />
                <span>Mã khách hàng (nếu có)</span>
              </label>
              <input
                type="number"
                v-model.number="formData.khachHangId"
                class="form-input"
                placeholder="ID khách hàng trong hệ thống"
              />
              <button type="button" class="btn-search-customer" @click="searchCustomer">
                <FontAwesomeIcon :icon="['fas', 'search']" />
                Tìm khách hàng
              </button>
            </div>
          </div>
        </div>

        <!-- Thông tin sản phẩm -->
        <div class="form-section">
          <h2>Thông tin Sản phẩm</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'mobile-alt']" />
                <span>Tên sản phẩm/Model *</span>
              </label>
              <input
                type="text"
                v-model="formData.tenSanPham"
                class="form-input"
                placeholder="Nhập tên sản phẩm"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'palette']" />
                <span>Màu/SKU</span>
              </label>
              <input
                type="text"
                v-model="formData.mauSacSku"
                class="form-input"
                placeholder="Nhập màu sắc hoặc SKU"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'barcode']" />
                <span>Số IMEI/Serial *</span>
              </label>
              <input
                type="text"
                v-model="formData.imeiSerial"
                class="form-input"
                placeholder="Nhập IMEI hoặc Serial"
                required
              />
              <small class="form-help"
                >Nhập IMEI/Serial và nhấn nút "Tìm kiếm" ở dưới để tự động điền thông tin</small
              >
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'layer-group']" />
                <span>Tìm kiếm thông tin sản phẩm</span>
              </label>
              <div style="display: flex; gap: 8px">
                <input
                  type="text"
                  v-model="formData.imeiSerial"
                  class="form-input"
                  placeholder="Nhập IMEI/Serial để tìm kiếm"
                  style="flex: 1"
                />
                <button type="button" class="btn-search-invoice" @click="searchByImei">
                  <FontAwesomeIcon :icon="['fas', 'search']" />
                  Tìm kiếm
                </button>
              </div>
              <small class="form-help"
                >Tìm kiếm thông tin sản phẩm, hóa đơn và chi tiết sản phẩm bằng IMEI/Serial</small
              >
            </div>

            <div class="form-group" v-if="formData.hoaDonId">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'receipt']" />
                <span>Mã hóa đơn</span>
              </label>
              <input
                type="text"
                v-model="formData.maHoaDon"
                class="form-input"
                placeholder="Mã hóa đơn mua hàng"
                readonly
              />
            </div>

            <div class="form-group" v-if="formData.ngayMua">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'calendar']" />
                <span>Ngày mua</span>
              </label>
              <input
                type="text"
                v-model="formData.ngayMua"
                class="form-input"
                placeholder="Ngày mua hàng"
                readonly
              />
            </div>

            <!-- Thông tin bảo hành -->
            <div class="form-group full-width" v-if="formData.thongTinBaoHanh">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'shield-alt']" />
                <span>Thông tin Bảo hành</span>
              </label>
              <div
                class="bao-hanh-info"
                :class="{
                  'bao-hanh-con-han': !formData.thongTinBaoHanh.isHetHan,
                  'bao-hanh-het-han': formData.thongTinBaoHanh.isHetHan,
                  'bao-hanh-sap-het-han':
                    !formData.thongTinBaoHanh.isHetHan &&
                    formData.thongTinBaoHanh.soNgayConLai <= 30,
                }"
              >
                <div class="bao-hanh-status">
                  <strong>Trạng thái:</strong>
                  <span
                    class="status-badge"
                    :class="{
                      'badge-success':
                        !formData.thongTinBaoHanh.isHetHan &&
                        formData.thongTinBaoHanh.soNgayConLai > 30,
                      'badge-warning':
                        !formData.thongTinBaoHanh.isHetHan &&
                        formData.thongTinBaoHanh.soNgayConLai <= 30,
                      'badge-danger': formData.thongTinBaoHanh.isHetHan,
                    }"
                  >
                    {{ formData.thongTinBaoHanh.trangThaiText || 'Đang kiểm tra...' }}
                  </span>
                </div>
                <div class="bao-hanh-details" v-if="formData.thongTinBaoHanh.ngayBatDau">
                  <div>
                    <strong>Thời hạn:</strong> {{ formData.thongTinBaoHanh.thoiHanBaoHanh }} tháng
                  </div>
                  <div>
                    <strong>Ngày bắt đầu:</strong>
                    {{ formatDate(formData.thongTinBaoHanh.ngayBatDau) }}
                  </div>
                  <div>
                    <strong>Ngày kết thúc:</strong>
                    {{ formatDate(formData.thongTinBaoHanh.ngayKetThuc) }}
                  </div>
                  <div
                    v-if="
                      formData.thongTinBaoHanh.soNgayConLai !== null &&
                      formData.thongTinBaoHanh.soNgayConLai !== undefined
                    "
                  >
                    <strong>Số ngày còn lại:</strong>
                    <span
                      :class="{
                        'text-danger': formData.thongTinBaoHanh.isHetHan,
                        'text-warning':
                          !formData.thongTinBaoHanh.isHetHan &&
                          formData.thongTinBaoHanh.soNgayConLai <= 30,
                        'text-success':
                          !formData.thongTinBaoHanh.isHetHan &&
                          formData.thongTinBaoHanh.soNgayConLai > 30,
                      }"
                    >
                      {{ formData.thongTinBaoHanh.soNgayConLai }} ngày
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tình trạng tiếp nhận -->
        <div class="form-section">
          <h2>Tình trạng Tiếp nhận</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'comment-alt']" />
                <span>Mô tả lỗi (theo khách hàng) *</span>
              </label>
              <textarea
                v-model="formData.moTaLoiKhachHang"
                class="form-textarea"
                rows="4"
                placeholder="Nhập mô tả lỗi mà khách hàng trình bày"
                required
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                <span>Mô tả lỗi (nhân viên ghi nhận)</span>
              </label>
              <textarea
                v-model="formData.moTaLoiNhanVien"
                class="form-textarea"
                rows="4"
                placeholder="Nhập mô tả lỗi sau khi kiểm tra nhanh"
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'exclamation-triangle']" />
                <span>Tình trạng vật lý (mô tả chi tiết)</span>
              </label>
              <textarea
                v-model="formData.tinhTrangVatLy"
                class="form-textarea"
                rows="3"
                placeholder="Mô tả tình trạng vật lý của sản phẩm, nên chụp ảnh lại"
              ></textarea>
              <small class="form-help">Nên chụp ảnh lại các vết trầy xước, cấn móp nếu có</small>
            </div>

            <!-- Tình trạng máy chi tiết (checkbox) -->
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'check-square']" />
                <span>Tình trạng máy khi tiếp nhận</span>
              </label>
              <div class="checkbox-grid">
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.trayXuocNhe" />
                  <span>Trầy xước nhẹ</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.canMop" />
                  <span>Cấn móp</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.meVen" />
                  <span>Mẻ viền</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.manHinhSocDiemChet" />
                  <span>Màn hình sọc/điểm chết</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.vaoNuoc" />
                  <span>Vào nước</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.temBaoHanhRachMat" />
                  <span>Tem bảo hành rách/mất</span>
                </label>
              </div>
            </div>

            <!-- Phụ kiện đi kèm chi tiết -->
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'luggage-cart']" />
                <span>Phụ kiện đi kèm</span>
              </label>
              <div class="checkbox-grid">
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.phuKienSac" />
                  <span>Sạc</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.phuKienCap" />
                  <span>Cáp</span>
                </label>
                <label class="checkbox-item">
                  <input type="checkbox" v-model="formData.phuKienHop" />
                  <span>Hộp</span>
                </label>
              </div>
              <input
                type="text"
                v-model="formData.phuKienKhac"
                class="form-input"
                style="margin-top: 10px"
                placeholder="Phụ kiện khác (nếu có)"
              />
              <small class="form-help">Liệt kê các phụ kiện khách hàng gửi kèm</small>
            </div>

            <!-- Kiểm tra nhanh -->
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                <span>Kiểm tra nhanh (NV/KTV)</span>
              </label>
              <textarea
                v-model="formData.kiemTraNhanh"
                class="form-textarea"
                rows="3"
                placeholder="Ghi các chức năng cơ bản đã test và kết quả"
              ></textarea>
              <small class="form-help">Ví dụ: Màn hình hoạt động tốt, camera chụp được, loa phát được...</small>
            </div>
          </div>
        </div>

        <!-- Xác minh bảo hành -->
        <div class="form-section">
          <h2>Xác minh Bảo hành</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'shield-check']" />
                <span>Thời hạn bảo hành</span>
              </label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="formData.baoHanhConHan" :value="true" />
                  <span>Còn hạn</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="formData.baoHanhConHan" :value="false" />
                  <span>Hết hạn</span>
                </label>
              </div>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'check-circle']" />
                <span>IMEI/Serial trùng khớp</span>
              </label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="formData.imeiTrungKhop" :value="true" />
                  <span>Có</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="formData.imeiTrungKhop" :value="false" />
                  <span>Không</span>
                </label>
              </div>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'stamp']" />
                <span>Tem nguyên vẹn</span>
              </label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="formData.temNguyenVen" :value="true" />
                  <span>Có</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="formData.temNguyenVen" :value="false" />
                  <span>Không</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Kết luận điều kiện bảo hành -->
        <div class="form-section">
          <h2>Kết luận Điều kiện Bảo hành</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'gavel']" />
                <span>Đánh giá điều kiện bảo hành *</span>
              </label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="formData.duDieuKienBaoHanh" :value="true" />
                  <span>Đủ điều kiện</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="formData.duDieuKienBaoHanh" :value="false" />
                  <span>Không đủ điều kiện</span>
                </label>
              </div>
            </div>

            <div class="form-group full-width" v-if="formData.duDieuKienBaoHanh === false">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'exclamation-circle']" />
                <span>Lý do không đủ điều kiện *</span>
              </label>
              <textarea
                v-model="formData.lyDoKhongDuDieuKien"
                class="form-textarea"
                rows="3"
                placeholder="Nhập lý do không đủ điều kiện bảo hành"
                :required="formData.duDieuKienBaoHanh === false"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Hướng xử lý -->
        <div class="form-section">
          <h2>Hướng xử lý</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'tools']" />
                <span>Hướng xử lý *</span>
              </label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="formData.huongXuLy" value="SUA_TAI_CUA_HANG" />
                  <span>Sửa tại cửa hàng</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="formData.huongXuLy" value="GUI_TTBH_HANG" />
                  <span>Gửi TTBH của hãng</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'user-tie']" />
                <span>Người phụ trách</span>
              </label>
              <input
                type="text"
                v-model="formData.nguoiPhuTrach"
                class="form-input"
                placeholder="Tên KTV/NV phụ trách"
              />
            </div>

            <div class="form-group full-width" v-if="formData.huongXuLy === 'GUI_TTBH_HANG'">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'truck']" />
                <span>Biên bản bàn giao (nếu gửi đi)</span>
              </label>
              <div class="form-grid" style="margin-top: 10px">
                <div class="form-group">
                  <label class="form-label">
                    <span>Mã vận đơn</span>
                  </label>
                  <input
                    type="text"
                    v-model="formData.maVanDon"
                    class="form-input"
                    placeholder="Nhập mã vận đơn"
                  />
                </div>
                <div class="form-group">
                  <label class="form-label">
                    <span>Tình trạng niêm phong</span>
                  </label>
                  <input
                    type="text"
                    v-model="formData.tinhTrangNiemPhong"
                    class="form-input"
                    placeholder="Mô tả tình trạng niêm phong"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Lịch hẹn -->
        <div class="form-section">
          <h2>Lịch hẹn</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'calendar-alt']" />
                <span>Ngày nhận</span>
              </label>
              <input type="date" v-model="formData.ngayNhan" class="form-input" />
              <small class="form-help">Mặc định là ngày hôm nay</small>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'calendar-check']" />
                <span>Ngày hẹn trả (dự kiến) *</span>
              </label>
              <input type="date" v-model="formData.ngayHenTraDuKien" class="form-input" required />
              <small class="form-help">Ngày dự kiến trả máy cho khách hàng</small>
            </div>
          </div>
        </div>

        <!-- Ghi chú -->
        <div class="form-section">
          <h2>Ghi chú</h2>
          <div class="form-group full-width">
            <textarea
              v-model="formData.ghiChu"
              class="form-textarea"
              rows="3"
              placeholder="Ghi chú thêm (nếu có)"
            ></textarea>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="goBack">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
            <FontAwesomeIcon :icon="['fas', 'save']" v-if="!isSubmitting" />
            <span v-if="isSubmitting">Đang lưu...</span>
            <span v-else>Tiếp nhận Bảo hành</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import PosHeader from '@/components/PosHeader.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const isSubmitting = ref(false)

interface ThongTinBaoHanh {
  isHetHan?: boolean
  soNgayConLai?: number
  trangThaiText?: string
  thoiHanBaoHanh?: number
  ngayBatDau?: string
  ngayKetThuc?: string
}

interface FormData {
  khachHangId?: number
  tenKhachHang: string
  soDienThoai: string
  emailKhachHang?: string
  sanPhamId?: number
  chiTietSanPhamId?: number
  hoaDonId?: number
  maHoaDon?: string
  ngayMua?: string
  tenSanPham: string
  imeiSerial: string
  mauSacSku?: string
  moTaLoiKhachHang: string
  moTaLoiNhanVien: string
  kiemTraNhanh?: string
  tinhTrangVatLy: string
  phuKienDiKem?: string
  // Tình trạng máy chi tiết
  trayXuocNhe?: boolean
  canMop?: boolean
  meVen?: boolean
  manHinhSocDiemChet?: boolean
  vaoNuoc?: boolean
  temBaoHanhRachMat?: boolean
  // Phụ kiện đi kèm
  phuKienSac?: boolean
  phuKienCap?: boolean
  phuKienHop?: boolean
  phuKienKhac?: string
  // Xác minh bảo hành
  baoHanhConHan?: boolean
  imeiTrungKhop?: boolean
  temNguyenVen?: boolean
  // Đánh giá điều kiện
  duDieuKienBaoHanh?: boolean
  lyDoKhongDuDieuKien?: string
  // Hướng xử lý
  huongXuLy?: string
  nguoiPhuTrach?: string
  maVanDon?: string
  tinhTrangNiemPhong?: string
  // Thời gian
  ngayNhan: string
  ngayHenTraDuKien: string
  ghiChu?: string
  thongTinBaoHanh?: ThongTinBaoHanh
}

const formData = ref<FormData>({
  tenKhachHang: '',
  soDienThoai: '',
  tenSanPham: '',
  imeiSerial: '',
  moTaLoiKhachHang: '',
  moTaLoiNhanVien: '',
  tinhTrangVatLy: '',
  phuKienDiKem: '',
  ngayNhan: new Date().toISOString().split('T')[0],
  ngayHenTraDuKien: '',
  ghiChu: '',
})

function goBack() {
  router.push('/bao-hanh')
}

async function searchCustomer() {
  if (!formData.value.soDienThoai && !formData.value.khachHangId) {
    toastRef.value?.warning(
      'Cảnh báo',
      'Vui lòng nhập số điện thoại hoặc mã khách hàng để tìm kiếm',
    )
    return
  }

  try {
    const searchQuery = formData.value.soDienThoai || formData.value.khachHangId?.toString()
    const response = await api.get(`/api/khach-hang/search?query=${searchQuery}`)
    const customers = response.data || []

    if (customers.length > 0) {
      const customer = customers[0]
      formData.value.khachHangId = customer.id
      formData.value.tenKhachHang = customer.hoTen
      formData.value.soDienThoai = customer.soDienThoai
      if (customer.email) {
        formData.value.emailKhachHang = customer.email
      }
      toastRef.value?.success('Thành công', 'Đã tìm thấy khách hàng')
    } else {
      toastRef.value?.warning('Không tìm thấy', 'Không tìm thấy khách hàng trong hệ thống')
    }
  } catch (error: any) {
    toastRef.value?.error(
      'Lỗi',
      'Không thể tìm kiếm khách hàng: ' + (error.message || 'Unknown error'),
    )
  }
}

async function searchByImei() {
  if (!formData.value.imeiSerial?.trim()) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng nhập số IMEI/Serial để tìm kiếm')
    return
  }

  try {
    // Tìm hóa đơn theo IMEI
    const response = await api.get(
      `/api/hoa-don/search?query=${encodeURIComponent(formData.value.imeiSerial)}&page=0&size=10`,
    )
    const hoaDons = response.data?.content || response.data || []

    if (hoaDons.length > 0) {
      const hoaDon = hoaDons[0]

      // Fill thông tin hóa đơn
      formData.value.hoaDonId = hoaDon.id
      if (hoaDon.maHoaDon) {
        formData.value.maHoaDon = hoaDon.maHoaDon
      }

      // Fill thông tin ngày mua
      if (hoaDon.ngayTao) {
        const ngayTao = new Date(hoaDon.ngayTao)
        formData.value.ngayMua = ngayTao.toLocaleDateString('vi-VN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
        })
      }

      // Tìm thông tin IMEI đã bán để lấy chi tiết sản phẩm
      try {
        const imeiResponse = await api.get(
          `/api/imei-da-ban/imei/${encodeURIComponent(formData.value.imeiSerial)}`,
        )
        if (imeiResponse.data) {
          const imeiDaBan = imeiResponse.data
          if (imeiDaBan.hoaDonChiTiet) {
            formData.value.chiTietSanPhamId = imeiDaBan.hoaDonChiTiet.idCtsp
            if (imeiDaBan.hoaDonChiTiet.chiTietSanPham) {
              formData.value.sanPhamId =
                imeiDaBan.hoaDonChiTiet.chiTietSanPham.idSp ||
                imeiDaBan.hoaDonChiTiet.chiTietSanPham.sanPham?.id
              if (!formData.value.tenSanPham && imeiDaBan.hoaDonChiTiet.chiTietSanPham.sanPham) {
                formData.value.tenSanPham =
                  imeiDaBan.hoaDonChiTiet.chiTietSanPham.sanPham.tenSanPham || ''
              }
              // Lấy thông tin màu sắc nếu có
              if (imeiDaBan.hoaDonChiTiet.chiTietSanPham.mauSac) {
                const mauSac = imeiDaBan.hoaDonChiTiet.chiTietSanPham.mauSac
                const mauSacText = mauSac.tenMauSac || mauSac.ten || ''
                const skuText = imeiDaBan.hoaDonChiTiet.chiTietSanPham.maCtsp || ''
                formData.value.mauSacSku = mauSacText + (skuText ? ` / ${skuText}` : '')
              } else if (imeiDaBan.hoaDonChiTiet.chiTietSanPham.maCtsp) {
                formData.value.mauSacSku = imeiDaBan.hoaDonChiTiet.chiTietSanPham.maCtsp
              }
            }
          }
        }
      } catch (imeiError) {
        console.log('Could not fetch IMEI details:', imeiError)
      }

      // Cập nhật thông tin khách hàng từ hóa đơn
      if (hoaDon.khachHangId) {
        formData.value.khachHangId = hoaDon.khachHangId
      }
      if (hoaDon.tenKhachHang) {
        formData.value.tenKhachHang = hoaDon.tenKhachHang
      }
      if (hoaDon.soDienThoai) {
        formData.value.soDienThoai = hoaDon.soDienThoai
      }
      if (hoaDon.email) {
        formData.value.emailKhachHang = hoaDon.email
      }

      // Kiểm tra thông tin bảo hành
      try {
        const baoHanhResponse = await api.get(
          `/api/bao-hanh/kiem-tra-chi-tiet/${encodeURIComponent(formData.value.imeiSerial)}`,
        )
        if (baoHanhResponse.data) {
          const baoHanh = baoHanhResponse.data
          formData.value.thongTinBaoHanh = {
            isHetHan: baoHanh.isHetHan || false,
            soNgayConLai: baoHanh.soNgayConLai || 0,
            trangThaiText: baoHanh.trangThaiText || 'Không xác định',
            thoiHanBaoHanh: baoHanh.thoiHanBaoHanh,
            ngayBatDau: baoHanh.ngayBatDau,
            ngayKetThuc: baoHanh.ngayKetThuc,
          }

          // Tự động điền xác minh bảo hành
          formData.value.baoHanhConHan = !baoHanh.isHetHan
          formData.value.imeiTrungKhop = true // IMEI đã tìm thấy trong hệ thống

          // Hiển thị cảnh báo nếu hết hạn hoặc sắp hết hạn
          if (baoHanh.isHetHan) {
            toastRef.value?.warning(
              'Cảnh báo',
              `Bảo hành đã hết hạn từ ${Math.abs(baoHanh.soNgayConLai || 0)} ngày trước`,
            )
          } else if (baoHanh.soNgayConLai <= 30) {
            toastRef.value?.warning(
              'Lưu ý',
              `Bảo hành sắp hết hạn, còn ${baoHanh.soNgayConLai} ngày`,
            )
          }
        }
      } catch (baoHanhError: any) {
        console.log('Could not fetch warranty info:', baoHanhError)
        // Không có bảo hành hoặc lỗi, không hiển thị thông tin bảo hành
        formData.value.thongTinBaoHanh = undefined
        // Vẫn đánh dấu IMEI trùng khớp nếu tìm thấy hóa đơn
        if (hoaDons.length > 0) {
          formData.value.imeiTrungKhop = true
        }
      }

      toastRef.value?.success(
        'Thành công',
        `Đã tìm thấy thông tin: Hóa đơn ${hoaDon.maHoaDon || hoaDon.id}, Khách hàng: ${hoaDon.tenKhachHang || 'N/A'}`,
      )
    } else {
      toastRef.value?.warning(
        'Không tìm thấy',
        'Không tìm thấy hóa đơn với IMEI/Serial này trong hệ thống',
      )
    }
  } catch (error: any) {
    console.error('Error searching by IMEI:', error)
    toastRef.value?.error(
      'Lỗi',
      'Không thể tìm kiếm: ' + (error.response?.data?.message || error.message || 'Unknown error'),
    )
  }
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true
  try {
    const payload = {
      ...formData.value,
      ngayNhan: formData.value.ngayNhan || new Date().toISOString().split('T')[0],
    }

    const response = await api.post('/api/bao-hanh/tiep-nhan', payload)

    toastRef.value?.success('Thành công', 'Đã tiếp nhận yêu cầu bảo hành thành công')

    // Download PDF biên bản tiếp nhận
    try {
      const pdfResponse = await api.get(`/api/bao-hanh/${response.data.id}/bien-ban-pdf`, {
        responseType: 'blob',
      })

      // Create blob and download
      const blob = new Blob([pdfResponse.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `BienBanTiepNhan_${response.data.maPhieu || response.data.id}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (pdfError) {
      console.error('Error downloading PDF:', pdfError)
      toastRef.value?.warning(
        'Cảnh báo',
        'Không thể tải biên bản PDF, nhưng phiếu bảo hành đã được tạo thành công',
      )
    }

    // Redirect to detail page
    setTimeout(() => {
      router.push(`/bao-hanh/chi-tiet/${response.data.id}`)
    }, 2000)
  } catch (error: any) {
    console.error('Error submitting form:', error)
    toastRef.value?.error(
      'Lỗi',
      'Không thể tiếp nhận bảo hành: ' +
        (error.response?.data?.message || error.message || 'Unknown error'),
    )
  } finally {
    isSubmitting.value = false
  }
}

function formatDate(dateString: string | undefined): string {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
  } catch (error) {
    return dateString
  }
}

function validateForm(): boolean {
  if (!formData.value.tenKhachHang?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên khách hàng')
    return false
  }

  if (!formData.value.soDienThoai?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số điện thoại')
    return false
  }

  if (!formData.value.tenSanPham?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên sản phẩm')
    return false
  }

  if (!formData.value.imeiSerial?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số IMEI/Serial')
    return false
  }

  if (!formData.value.moTaLoiKhachHang?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập mô tả lỗi theo khách hàng')
    return false
  }

  if (formData.value.duDieuKienBaoHanh === undefined || formData.value.duDieuKienBaoHanh === null) {
    toastRef.value?.error('Lỗi', 'Vui lòng đánh giá điều kiện bảo hành')
    return false
  }

  if (formData.value.duDieuKienBaoHanh === false && !formData.value.lyDoKhongDuDieuKien?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập lý do không đủ điều kiện bảo hành')
    return false
  }

  if (!formData.value.huongXuLy) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn hướng xử lý')
    return false
  }

  if (!formData.value.ngayHenTraDuKien) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn ngày hẹn trả dự kiến')
    return false
  }

  return true
}

onMounted(() => {
  // Set default date for ngayNhan
  if (!formData.value.ngayNhan) {
    formData.value.ngayNhan = new Date().toISOString().split('T')[0]
  }
})
</script>

<style scoped>
.form-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.form-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #5a6268;
}

.form-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.form-section h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-input,
.form-textarea,
.form-select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #2196f3;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.form-help {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.btn-search-customer,
.btn-search-invoice {
  margin-top: 8px;
  padding: 8px 16px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.3s;
}

.btn-search-customer:hover,
.btn-search-invoice:hover {
  background-color: #1976d2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #e0e0e0;
}

.btn-cancel,
.btn-submit {
  padding: 12px 30px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-submit {
  background-color: #4caf50;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Thông tin bảo hành */
.bao-hanh-info {
  padding: 15px;
  border-radius: 6px;
  border: 2px solid #e0e0e0;
  background-color: #f9f9f9;
  margin-top: 10px;
}

.bao-hanh-con-han {
  border-color: #4caf50;
  background-color: #f1f8f4;
}

.bao-hanh-het-han {
  border-color: #f44336;
  background-color: #ffebee;
}

.bao-hanh-sap-het-han {
  border-color: #ff9800;
  background-color: #fff3e0;
}

.bao-hanh-status {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  font-size: 16px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 14px;
}

.badge-success {
  background-color: #4caf50;
  color: white;
}

.badge-warning {
  background-color: #ff9800;
  color: white;
}

.badge-danger {
  background-color: #f44336;
  color: white;
}

.bao-hanh-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  font-size: 14px;
}

.bao-hanh-details > div {
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.text-success {
  color: #4caf50;
  font-weight: 600;
}

.text-warning {
  color: #ff9800;
  font-weight: 600;
}

.text-danger {
  color: #f44336;
  font-weight: 600;
}

/* Checkbox grid */
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.checkbox-item:hover {
  background-color: #f0f0f0;
}

.checkbox-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #2196f3;
}

.checkbox-item span {
  font-size: 14px;
  color: #555;
  user-select: none;
}

/* Radio group */
.radio-group {
  display: flex;
  gap: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.radio-item:hover {
  background-color: #f0f0f0;
}

.radio-item input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #2196f3;
}

.radio-item span {
  font-size: 14px;
  color: #555;
  user-select: none;
}
</style>
