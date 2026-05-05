<template>
  <div class="tracking-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="tracking-main">
      <div class="container">
        <!-- Page Header -->

        <div class="page-header" style="margin-top: 20px">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <h1 style="margin: 0;">{{ isLoggedIn ? 'Đơn hàng đã mua' : 'Chi tiết đơn hàng' }}</h1>
            <div v-if="isLoggedIn && !orderDetails" class="date-range-filter">
              <span>Từ {{ formatDateRange(startDate) }} - {{ formatDateRange(endDate) }}</span>
              <button class="btn-change-date" @click="showDatePicker = true">Thay đổi</button>
            </div>
          </div>
        </div>


        <!-- No Order Message - Show when no orderId in query and not logged in -->
        <div v-if="!isLoggedIn && !route.query.orderId && !orderDetails" class="no-order">
          <div class="no-order-card">
            <i class="bi bi-info-circle-fill"></i>
            <h3>Không tìm thấy đơn hàng</h3>
            <p>Vui lòng truy cập đơn hàng thông qua link trong email hoặc từ trang "Đơn hàng đã mua" nếu bạn đã đăng nhập.</p>
          </div>
        </div>

        <!-- Orders List - Show when logged in and no orderDetails -->
        <div v-if="isLoggedIn && !orderDetails && !route.query.orderId" class="orders-section">
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
          <div v-if="isLoadingOrders" class="loading-state">
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
              v-for="order in filteredOrders"
              :key="order.id"
              class="order-card"
            >
              <div class="order-content">
                <div class="order-header-row">
                  <div class="order-id">#{{ order.id }}</div>
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
                  <div class="order-actions-section">
                    <div class="order-status-badge" :class="getStatusClassForOrder(order.status)">
                      {{ getStatusTextForOrder(order.status) }}
                    </div>
                    <div class="order-price">{{ formatPrice(order.total || 0) }}</div>
                    <button
                      @click="viewOrderDetail(order.originalOrder)"
                      class="btn-view-detail"
                    >
                      Xem chi tiết
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Details - Show for both logged in and non-logged in users -->
        <div v-if="orderDetails" class="order-details">
          <div class="order-card">
            <div class="order-header">
              <div class="order-header-left">
                <button 
                  v-if="isLoggedIn" 
                  @click="goBackToOrders" 
                  class="btn-back-inline"
                >
                  <i class="bi bi-arrow-left"></i>
                </button>
                <h2>Chi tiết đơn hàng</h2>
              </div>
              <div class="order-id">#{{ orderDetails.id }}</div>
            </div>

            <!-- Order Status -->
            <div class="status-section">
              <div class="status-badge" :class="getStatusClass(orderDetails.trangThai)">
                <i :class="getStatusIcon(orderDetails.trangThai)"></i>
                {{ getStatusText(orderDetails.trangThai) }}
              </div>
              <!-- Action Buttons - Only show for pending/confirmed orders and not cancelled -->
              <div v-if="orderDetails && orderDetails.trangThai !== 4" class="order-actions-inline">
                <button 
                  v-if="orderDetails.trangThai === 0" 
                  class="btn-cancel-order" 
                  @click="showCancelConfirmModal = true"
                  :disabled="isProcessing"
                >
                  <i class="bi bi-x-circle"></i>
                  Hủy đơn hàng
                </button>
                <button 
                  v-if="orderDetails.trangThai === 0 && !hasUpdatedOnce" 
                  class="btn-update-order" 
                  @click="showUpdateModal = true"
                  :disabled="isProcessing"
                >
                  <i class="bi bi-pencil-square"></i>
                  Cập nhật đơn hàng
                </button>
                <!-- Nút Đã nhận hàng - chỉ hiển thị khi admin hoàn thành đơn hàng (trangThai === 3) và chưa xác nhận -->
                <button 
                  v-if="orderDetails.trangThai === 3 && !hasConfirmedReceived"
                  class="btn-confirm-received" 
                  @click="showConfirmReceivedModal = true"
                  :disabled="isProcessing"
                >
                  <i class="bi bi-check-circle"></i>
                  Đã nhận hàng
                </button>
                <!-- Nút Đánh giá sản phẩm - hiển thị sau khi đã xác nhận nhận hàng và chưa gửi review -->
                <button
                  v-if="orderDetails.trangThai === 3 && hasConfirmedReceived && !hasSubmittedReview"
                  class="btn-review-order"
                  @click="openReviewModal"
                  :disabled="isProcessing"
                >
                  <i class="bi bi-star"></i>
                  Đánh giá sản phẩm
                </button>
                <!-- Nút Đã hoàn thành - hiển thị sau khi đã gửi review -->
                <button
                  v-if="orderDetails.trangThai === 3 && hasConfirmedReceived && hasSubmittedReview"
                  class="btn-review-completed"
                  disabled
                >
                  <i class="bi bi-check-circle-fill"></i>
                  Đã hoàn thành
                </button>
              </div>
            </div>

            <!-- Order Status Timeline -->
            <div class="order-status-section">
              <div class="status-header">
                <i class="bi bi-clock-history"></i>
                <span>Trạng Thái Hóa Đơn - {{ orderDetails.maHoaDon }}</span>
              </div>
              <div class="status-timeline">
                <div class="status-steps">
                  <div
                    v-for="(step, index) in getTrackingSteps"
                    :key="index"
                    :class="['status-step', {
                      active: !(index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex)),
                      completed: index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex),
                      isCancelled: step.isCancelled
                    }]"
                  >
                    <div class="step-icon">
                      <i
                        :class="getStepIcon(step, index)"
                        v-if="index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex) || step.isCancelled"
                      ></i>
                      <i :class="getStepIconOriginal(step.icon)" v-else></i>
                    </div>
                    <div class="step-content">
                      <div class="step-title">{{ step.title }}</div>
                      <div class="step-time">{{ step.time || 'Không có thời gian' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Info -->
            <div class="order-info">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">Khách hàng:</span>
                  <span class="info-value">{{ orderDetails.tenKhachHang || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Số điện thoại:</span>
                  <span class="info-value">{{ orderDetails.soDienThoai || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Email:</span>
                  <span class="info-value">{{ orderDetails.email ? orderDetails.email : 'Không có' }}</span>
                </div>

                <div class="info-item">
                  <span class="info-label">Ngày đặt:</span>
                  <span class="info-value">{{ formatDate(orderDetails.ngayDat || orderDetails.ngayTao) }}</span>
                </div>
                <div class="info-item" v-if="orderDetails.maVoucher || orderDetails.phieuGiamGiaId">
                  <span class="info-label">Phiếu giảm giá:&nbsp;</span>
                  <span class="info-value">{{ orderDetails.maVoucher || `VC_${orderDetails.phieuGiamGiaId}` }}</span>
                </div>
                <div class="info-item" v-if="orderDetails.ghiChu">
                  <span class="info-label">Ghi chú:</span>
                  <span class="info-value">{{ orderDetails.ghiChu }}</span>
                </div>
              </div>
              <div class="info-item">
                <span class="info-label">Địa chỉ:</span>
                <span class="info-value">{{ formatAddress(orderDetails) || 'Không có' }}</span>
              </div>
            </div>

            <!-- Order Items -->
            <div class="order-items">
              <h3>Sản phẩm đã đặt</h3>
              <div class="items-list">
                <div v-for="(item, index) in orderDetails.chiTietDonHang" :key="item.id || item.chiTietHoaDonId || index" class="item-card">
                  <div class="item-image">
                    <img
                      :src="item.hinhAnh || getPlaceholderImage()"
                      :alt="item.tenSanPham"
                      @error="($event) => $event.target.src = getPlaceholderImage()"
                    />
                  </div>
                  <div class="item-info">
                    <h4>{{ item.tenSanPham }}</h4>
                    <div class="item-specs">
                      <span v-if="item.tenRam" class="spec-badge">{{ item.tenRam }}</span>
                      <span v-if="item.tenRom" class="spec-badge">{{ item.tenRom }}</span>
                      <span v-if="item.tenMauSac" class="spec-badge">{{ item.tenMauSac }}</span>
                    </div>
                    <!-- Hiển thị IMEI nếu có - giống admin -->
                    <div v-if="getAllImeis(item).length > 0" class="item-imei">
                      <div v-for="(imei, imeiIndex) in getAllImeis(item)" :key="imeiIndex" class="imei-item">
                        <span class="imei-label">IMEI:</span>
                        <span class="imei-value">{{ typeof imei === 'object' ? (imei.imei || imei.maImei) : imei }}</span>
                      </div>
                    </div>
                    <!-- Rating section - chỉ hiển thị khi đơn hàng đã hoàn thành (trangThai === 3) -->
                    <div v-if="orderDetails.trangThai === 3 && isLoggedIn" class="item-rating-section">
                      <div v-if="!item.hasRated" class="rating-form-inline">
                        <label>Đánh giá:</label>
                        <div class="rating-stars-inline">
                          <span 
                            v-for="i in 5" 
                            :key="i" 
                            @click="submitRating(item, i)"
                            :class="['star', { filled: i <= (item.tempRating || 0) }]"
                            @mouseenter="item.tempRating = i"
                            @mouseleave="item.tempRating = null"
                          >★</span>
                        </div>
                      </div>
                      <div v-else class="rating-display">
                        <span class="rating-label">Đánh giá của bạn:</span>
                        <div class="rating-stars-display">
                          <span 
                            v-for="i in 5" 
                            :key="i" 
                            :class="['star', { filled: i <= item.userRating }]"
                          >★</span>
                        </div>
                        <button @click="item.hasRated = false" class="btn-change-rating">Thay đổi</button>
                      </div>
                    </div>
                  </div>
                  <div class="item-price">{{ formatPrice(item.thanhTien || (item.donGia * item.soLuong) || item.gia || 0) }}</div>
                </div>
                <div v-if="!orderDetails.chiTietDonHang || orderDetails.chiTietDonHang.length === 0" class="no-products">
                  Không có sản phẩm
                </div>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="order-summary">
              <div class="summary-row">
                <span>Tạm tính:&nbsp;</span>
                <span>{{ formatPrice(orderDetails.tongTienHang || orderDetails.tongTien || 0) }}</span>
              </div>
              <div class="summary-row" v-if="orderDetails.giamGia && orderDetails.giamGia > 0">
                <span>Giảm giá:&nbsp;</span>
                <span class="discount-text">-{{ formatPrice(orderDetails.giamGia) }}</span>
              </div>
              <div class="summary-row" v-if="orderDetails.phiVanChuyen && orderDetails.phiVanChuyen > 0">
                <span>Phí vận chuyển:&nbsp;</span>
                <span>{{ formatPrice(orderDetails.phiVanChuyen) }}</span>
              </div>
              <div class="summary-row total">
                <span>Tổng cộng:&nbsp;</span>
                <span>{{ formatPrice(orderDetails.thanhTien || orderDetails.tongTienSauGiam || orderDetails.tongTien || 0) }}</span>
              </div>
            </div>

            <!-- Payment History Section -->
            <div v-if="orderDetails && orderDetails.lichSuThanhToan && Array.isArray(orderDetails.lichSuThanhToan) && orderDetails.lichSuThanhToan.length > 0" class="payment-history-section">
              <h3>Lịch Sử Thanh Toán</h3>
              <div class="payment-history-list">
                <div v-for="(payment, index) in orderDetails.lichSuThanhToan" :key="`payment-${index}-${payment.ngayThanhToan || payment.id || index}`" class="payment-history-item">
                  <div class="payment-history-content">
                    <div class="payment-amount" :class="getPaymentAmountClass(payment.loaiThanhToan)">
                      {{ payment.loaiThanhToan === 'REFUND' ? '-' : '' }}{{ formatPrice(payment.soTien) }}
                    </div>
                    <div class="payment-details">
                      <span v-if="payment.phuongThucThanhToan" class="payment-method">
                        {{ payment.phuongThucThanhToan.tenPhuongThuc }}
                      </span>
                      <span :class="getPaymentTypeBadgeClass(payment.loaiThanhToan)" class="payment-type-badge">
                        {{ getPaymentTypeLabel(payment.loaiThanhToan) }}
                      </span>
                      <span v-if="payment.loaiThanhToan === 'REFUND'" :class="getRefundStatusBadgeClass(payment.trangThai)" class="refund-status-badge">
                        {{ getRefundStatusLabel(payment.trangThai) }}
                      </span>
                    </div>
                    <div class="payment-time">
                      <span v-if="payment.trangThai === 0 || !payment.ngayThanhToan">Thanh toán khi nhận hàng</span>
                      <span v-else>{{ formatDateTime(payment.ngayThanhToan) }}</span>
                    </div>
                  </div>
                  <!-- Customer: Đã nhận hoàn phí button (only show when refund status is TRANSFERRED) -->
                  <div v-if="payment.loaiThanhToan === 'REFUND' && payment.trangThai === 1" class="payment-actions">
                    <button 
                      @click="handleConfirmRefundReceived(payment.id)" 
                      class="btn-confirm-refund"
                      :disabled="isConfirmingRefund"
                      title="Xác nhận đã nhận hoàn phí"
                    >
                      <i class="bi bi-check-circle"></i>
                      Đã nhận hoàn phí
                    </button>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- Confirm Received Modal -->
        <div v-if="showConfirmReceivedModal" class="modal-overlay" @click.self="showConfirmReceivedModal = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>Xác nhận đã nhận hàng</h3>
              <button class="modal-close" @click="showConfirmReceivedModal = false">&times;</button>
            </div>
            <div class="modal-body">
              <p>Bạn có chắc chắn đã nhận được hàng từ đơn hàng <strong>{{ orderDetails?.maHoaDon }}</strong>?</p>
              <p class="info-text">Sau khi xác nhận, bạn sẽ có thể đánh giá và bình luận về sản phẩm.</p>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showConfirmReceivedModal = false">Hủy</button>
              <button class="btn-confirm-update" @click="handleConfirmReceived" :disabled="isProcessing">
                {{ isProcessing ? 'Đang xử lý...' : 'Xác nhận đã nhận hàng' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Review and Rating Modal -->
        <div v-if="showReviewModal" class="modal-overlay" @click.self="closeReviewModal">
          <div class="modal-content review-modal">
            <div class="modal-header">
              <h3>Đánh giá và bình luận đơn hàng</h3>
              <button class="modal-close" @click="closeReviewModal">&times;</button>
            </div>
            <div class="modal-body">
              <div v-if="reviewError" class="error-message-box">
                {{ reviewError }}
              </div>

              <div class="review-products-list">
                <div 
                  v-for="(item, index) in orderDetails.chiTietDonHang" 
                  :key="item.id || index" 
                  class="review-product-item"
                >
                  <div class="review-product-header">
                    <img 
                      :src="item.hinhAnh || getPlaceholderImage()" 
                      :alt="item.tenSanPham" 
                      class="review-product-image"
                      @error="($event) => $event.target.src = getPlaceholderImage()"
                    />
                    <div class="review-product-info">
                      <h4>{{ item.tenSanPham }}</h4>
                      <div class="review-product-specs">
                        <span v-if="item.tenRam" class="spec-badge">{{ item.tenRam }}</span>
                        <span v-if="item.tenRom" class="spec-badge">{{ item.tenRom }}</span>
                        <span v-if="item.tenMauSac" class="spec-badge">{{ item.tenMauSac }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="review-form-section">
                    <div class="form-group">
                      <label>Đánh giá (sao):</label>
                      <div class="rating-stars-input">
                        <span
                          v-for="i in 5"
                          :key="i"
                          @click="reviewForms[index].rating = i"
                          :class="['star', { filled: i <= (reviewForms[index].rating || 0) }]"
                          @mouseenter="reviewForms[index].tempRating = i"
                          @mouseleave="reviewForms[index].tempRating = null"
                        >★</span>
                      </div>
                      <p v-if="!reviewForms[index].rating" class="rating-help-text">Vui lòng chọn số sao đánh giá</p>
                    </div>

                    <div class="form-group">
                      <label>Bình luận:</label>
                      <textarea
                        v-model="reviewForms[index].comment"
                        placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
                        rows="4"
                        class="form-input form-textarea"
                        maxlength="500"
                      ></textarea>
                      <div class="char-count">{{ (reviewForms[index].comment || '').length }}/500</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="closeReviewModal">Hủy</button>
              <button 
                class="btn-confirm-update" 
                @click="submitReviews" 
                :disabled="isSubmittingReview || !canSubmitReviews"
              >
                {{ isSubmittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Cancel Order Confirm Modal -->
        <div v-if="showCancelConfirmModal" class="modal-overlay" @click.self="showCancelConfirmModal = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>Xác nhận hủy đơn hàng</h3>
              <button class="modal-close" @click="showCancelConfirmModal = false">&times;</button>
            </div>
            <div class="modal-body">
              <p>Bạn có chắc chắn muốn hủy đơn hàng <strong>{{ orderDetails?.maHoaDon }}</strong>?</p>
              <p class="warning-text">Hành động này không thể hoàn tác. Đơn hàng sẽ được hủy và hoàn tiền (nếu đã thanh toán).</p>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showCancelConfirmModal = false">Không</button>
              <button class="btn-confirm-cancel" @click="handleCancelOrder" :disabled="isProcessing">
                {{ isProcessing ? 'Đang xử lý...' : 'Xác nhận hủy' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Update Order Modal -->
        <div v-if="showUpdateModal" class="modal-overlay" @click.self="closeUpdateModal">
          <div class="modal-content update-modal">
            <div class="modal-header">
              <h3>Cập nhật đơn hàng</h3>
              <button class="modal-close" @click="closeUpdateModal">&times;</button>
            </div>
            <div class="modal-body">
              <div v-if="updateError" class="error-message-box">
                {{ updateError }}
              </div>

              <!-- Customer Info Update -->
              <div class="update-section">
                <h4>Thông tin khách hàng</h4>
                <div class="form-group">
                  <label>Tên khách hàng:</label>
                  <input 
                    type="text" 
                    v-model="updateForm.tenKhachHang" 
                    placeholder="Nhập tên khách hàng"
                    class="form-input"
                  />
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>Số điện thoại:</label>
                    <input 
                      type="text" 
                      v-model="updateForm.soDienThoai" 
                      placeholder="Nhập số điện thoại"
                      class="form-input"
                    />
                  </div>
                  <div class="form-group">
                    <label>Email:</label>
                    <input 
                      type="email" 
                      v-model="updateForm.email" 
                      placeholder="Nhập email"
                      class="form-input"
                    />
                  </div>
                </div>
                <div class="form-group">
                  <label>Ghi chú:</label>
                  <textarea 
                    v-model="updateForm.ghiChu" 
                    placeholder="Nhập ghi chú"
                    class="form-input form-textarea"
                    rows="3"
                  ></textarea>
                </div>
              </div>

              <!-- Address Update -->
              <div class="update-section">
                <h4>Địa chỉ giao hàng</h4>
                
                <!-- Radio buttons for delivery type -->
                <div class="delivery-type-radio-group">
                  <label class="radio-option">
                    <input 
                      type="radio" 
                      name="deliveryType" 
                      value="address"
                      v-model="deliveryAddressType"
                      @change="onDeliveryTypeChange"
                    />
                    <span>Chọn địa chỉ</span>
                  </label>
                  <label class="radio-option">
                    <input 
                      type="radio" 
                      name="deliveryType" 
                      value="pickup"
                      v-model="deliveryAddressType"
                      @change="onDeliveryTypeChange"
                    />
                    <span>Lấy tại cửa hàng</span>
                  </label>
                </div>

                <!-- Show address fields when "Chọn địa chỉ" is selected -->
                <div v-if="deliveryAddressType === 'address'">
                  <!-- Address selection for logged in users -->
                  <div v-if="isLoggedIn && userAddresses.length > 0" class="form-group">
                    <label>Chọn địa chỉ đã lưu:</label>
                    <select 
                      v-model="selectedAddressId" 
                      @change="onAddressSelected"
                      class="form-input"
                    >
                      <option :value="null">-- Chọn địa chỉ --</option>
                      <option 
                        v-for="addr in userAddresses" 
                        :key="addr.id" 
                        :value="addr.id"
                      >
                        {{ addr.loaiDiaChi || 'Địa chỉ' }} - {{ getAddressDisplayText(addr) }}
                      </option>
                    </select>
                  </div>
                  <div v-if="!selectedAddressId" class="form-group">
                    <label>Địa chỉ chi tiết:</label>
                    <FreeMapPicker
                      v-model="updateForm.diaChi"
                      placeholder="Số nhà, tên đường, phường/xã hoặc chọn trên bản đồ"
                      @address-selected="onAddressSelectedFromMap"
                      @location-updated="onLocationUpdatedFromMap"
                    />
                  </div>
                  <!-- Show province/district when not using saved address -->
                  <div v-if="!selectedAddressId" class="form-row">
                    <div class="form-group">
                      <label>Tỉnh/Thành phố:</label>
                      <select 
                        v-model="selectedProvinceId" 
                        @change="onProvinceChange"
                        :disabled="isLoadingProvinces"
                        class="form-input"
                      >
                        <option :value="null">-- Chọn tỉnh/thành phố --</option>
                        <option 
                          v-for="province in provinces" 
                          :key="province.ProvinceID" 
                          :value="province.ProvinceID"
                        >
                          {{ province.ProvinceName }}
                        </option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label>Quận/Huyện:</label>
                      <select 
                        v-model="selectedDistrictId" 
                        @change="onDistrictChange"
                        :disabled="isLoadingDistricts || !selectedProvinceId"
                        class="form-input"
                      >
                        <option :value="null">-- Chọn quận/huyện --</option>
                        <option 
                          v-for="district in districts" 
                          :key="district.DistrictID" 
                          :value="district.DistrictID"
                        >
                          {{ district.DistrictName }}
                        </option>
                      </select>
                    </div>
                  </div>
                </div>

                <!-- Show "Lấy tại cửa hàng" when pickup is selected -->
                <div v-if="deliveryAddressType === 'pickup'" class="form-group">
                  <label>Địa chỉ chi tiết:</label>
                  <input 
                    type="text" 
                    :value="'Lấy tại cửa hàng'"
                    class="form-input"
                    readonly
                  />
                </div>
                <div v-if="isCalculatingShipping" class="shipping-fee-preview">
                  <span>Đang tính phí vận chuyển...</span>
                </div>
                <div v-else-if="newShippingFee !== null" class="shipping-fee-preview">
                  <span>Phí vận chuyển mới: <strong>{{ formatPrice(newShippingFee) }}</strong></span>
                </div>
                <div v-if="shippingError" class="error-message">
                  {{ shippingError }}
                </div>
              </div>

              <!-- Products Update -->
              <div class="update-section">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                  <h4 style="margin: 0;">Sản phẩm</h4>
                  <button 
                    class="btn-add-product" 
                    @click="openProductModal"
                    type="button"
                  >
                    <i class="bi bi-plus-circle"></i>
                    Mua thêm sản phẩm khác
                  </button>
                </div>
                <div class="product-update-list">
                  <div v-for="(item, index) in updateForm.products" :key="item.id || index" class="product-update-item">
                    <div class="product-image-container">
                      <img :src="getProductImageUrl(item.hinhAnh) || '/placeholder-product.jpg'" :alt="item.tenSanPham" class="product-main-image" />
                    </div>
                    <div class="product-details-container">
                      <div class="product-name-large">{{ item.tenSanPham }}</div>
                      <div class="product-specs-large">
                        <button v-if="item.tenRam" class="spec-button">{{ item.tenRam }}</button>
                        <button v-if="item.tenRom" class="spec-button">{{ item.tenRom }}</button>
                        <button v-if="item.tenMauSac" class="spec-button">{{ item.tenMauSac }}</button>
                      </div>
                      <div class="product-price-large">Giá: {{ formatPrice(item.donGia || item.gia || 0) }}</div>
                      <div class="product-actions-large">
                        <button 
                          class="btn-quantity" 
                          @click="decreaseQuantity(index)"
                          :disabled="item.soLuong <= 1"
                        >-</button>
                        <span class="quantity-display">{{ item.soLuong }}</span>
                        <button 
                          class="btn-quantity" 
                          @click="increaseQuantity(index)"
                        >+</button>
                        <button 
                          class="btn-remove-product" 
                          @click="removeProduct(index)"
                          :disabled="updateForm.products.length <= 1"
                        >
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                      <div class="product-total-large">Tổng: {{ formatPrice((item.donGia || item.gia || 0) * item.soLuong) }}</div>
                    </div>
                  </div>
                </div>

                <!-- Price Summary -->
                <div class="price-summary">
                  <div class="price-row">
                    <span>Tổng tiền hàng cũ:</span>
                    <span>{{ formatPrice(orderDetails?.tongTienHang || orderDetails?.tongTien || 0) }}</span>
                  </div>
                  <div class="price-row">
                    <span>Tổng tiền hàng mới:</span>
                    <span class="new-price">{{ formatPrice(calculateNewTotal()) }}</span>
                  </div>
                  <div v-if="calculatePriceDifference() !== 0" class="price-row difference">
                    <span>Chênh lệch:</span>
                    <span :class="calculatePriceDifference() > 0 ? 'positive' : 'negative'">
                      {{ calculatePriceDifference() > 0 ? '+' : '' }}{{ formatPrice(calculatePriceDifference()) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="closeUpdateModal">Hủy</button>
              <button class="btn-confirm-update" @click="showPriceConfirmModal = true" :disabled="isProcessing || !hasChanges()">
                {{ isProcessing ? 'Đang xử lý...' : 'Xác nhận cập nhật' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Product Selection Modal -->
        <div v-if="showProductModal" class="modal-overlay" @click.self="showProductModal = false">
          <div class="modal-content product-modal">
            <div class="modal-header">
              <h3>Chọn sản phẩm</h3>
              <button class="modal-close" @click="showProductModal = false">&times;</button>
            </div>
            <div class="modal-body">
              <div class="search-section">
                <input 
                  type="text" 
                  v-model="productSearchText" 
                  placeholder="Tìm kiếm sản phẩm..."
                  class="search-input"
                  @input="searchProducts"
                />
              </div>
              
              <div v-if="isLoadingProducts" class="loading-products">
                <div class="spinner"></div>
                <p>Đang tải sản phẩm...</p>
              </div>
              
              <div v-else class="products-list">
                <div v-for="product in filteredProductList" :key="product.chiTietSanPhamId" class="product-item">
                  <label class="product-checkbox-label">
                    <input 
                      type="checkbox" 
                      :value="product.chiTietSanPhamId"
                      :checked="isProductSelected(product.chiTietSanPhamId)"
                      @change="toggleProduct(product)"
                    />
                    <div class="product-item-info">
                      <img v-if="product.hinhAnh" :src="getProductImageUrl(product.hinhAnh)" :alt="product.tenSanPham" class="product-thumb" />
                      <div v-else class="product-thumb-placeholder">
                        <i class="bi bi-image"></i>
                      </div>
                      <div class="product-item-details">
                        <h4>{{ product.tenSanPham }}</h4>
                        <p class="product-meta">Mã: {{ product.maCtsp }} | {{ formatPrice(product.gia) }}</p>
                        <div class="product-specs">
                          <span v-if="product.tenRam" class="spec-badge">{{ product.tenRam }}</span>
                          <span v-if="product.tenRom" class="spec-badge">{{ product.tenRom }}</span>
                          <span v-if="product.tenMauSac" class="spec-badge">{{ product.tenMauSac }}</span>
                        </div>
                      </div>
                    </div>
                  </label>
                </div>
                
                <div v-if="filteredProductList.length === 0" class="no-products">
                  <i class="bi bi-box"></i>
                  <p>Không tìm thấy sản phẩm</p>
                </div>
              </div>
            </div>
            
            <div class="modal-footer">
              <button class="btn-cancel" @click="showProductModal = false">Đóng</button>
              <button class="btn-submit" @click="confirmProductSelection">
                Xác nhận ({{ selectedProducts.length }})
              </button>
            </div>
          </div>
        </div>

        <!-- Price Confirm Modal - Show when price changes -->
        <div v-if="showPriceConfirmModal" class="modal-overlay" @click.self="showPriceConfirmModal = false">
          <div class="modal-content price-confirm-modal">
            <div class="modal-header">
              <h3>Xác nhận cập nhật đơn hàng</h3>
              <button class="modal-close" @click="showPriceConfirmModal = false">&times;</button>
            </div>
            <div class="modal-body">
              <div class="price-confirm-content">
                <h4>Thay đổi về giá tiền</h4>
                
                <div class="price-comparison">
                  <div class="price-row">
                    <span>Tổng tiền hàng cũ:</span>
                    <span>{{ formatPrice(orderDetails?.tongTienHang || orderDetails?.tongTien || 0) }}</span>
                  </div>
                  <div class="price-row">
                    <span>Phí vận chuyển cũ:</span>
                    <span>{{ formatPrice(orderDetails?.phiVanChuyen || 0) }}</span>
                  </div>
                  <div class="price-row">
                    <span>Tổng cộng cũ:&nbsp;</span>
                    <span>{{ formatPrice((orderDetails?.tongTienHang || orderDetails?.tongTien || 0) + (orderDetails?.phiVanChuyen || 0)) }}</span>
                  </div>
                  
                  <hr class="price-divider" />
                  
                  <div class="price-row">
                    <span>Tổng tiền hàng mới:&nbsp;</span>
                    <span class="new-price">{{ formatPrice(calculateNewTotal()) }}</span>
                  </div>
                  <div class="price-row">
                    <span>Phí vận chuyển mới:&nbsp;</span>
                    <span class="new-price">{{ formatPrice(newShippingFee !== null ? newShippingFee : (orderDetails?.phiVanChuyen || 0)) }}</span>
                  </div>
                  <div class="price-row">
                    <span>Tổng cộng mới:&nbsp;</span>
                    <span class="new-price">{{ formatPrice(calculateNewTotalWithShipping()) }}</span>
                  </div>
                  
                  <hr class="price-divider" />
                  
                  <div class="price-row difference" v-if="calculatePriceDifference() !== 0">
                    <span>Chênh lệch:</span>
                    <span :class="calculatePriceDifference() > 0 ? 'positive' : 'negative'">
                      {{ calculatePriceDifference() > 0 ? '+' : '' }}{{ formatPrice(calculatePriceDifference()) }}
                    </span>
                  </div>
                  <div v-else class="price-row difference">
                    <span>Chênh lệch:</span>
                    <span class="no-change">Không thay đổi</span>
                  </div>
                </div>

                <div v-if="calculatePriceDifference() > 0" class="payment-notice">
                  <p><strong>Lưu ý:</strong> Tổng tiền đơn hàng tăng thêm <strong>{{ formatPrice(calculatePriceDifference()) }}</strong>. Bạn cần thanh toán số tiền này để hoàn tất cập nhật đơn hàng.</p>
                </div>
                <div v-else-if="calculatePriceDifference() < 0" class="refund-notice">
                  <p><strong>Lưu ý:</strong> Tổng tiền đơn hàng giảm <strong>{{ formatPrice(Math.abs(calculatePriceDifference())) }}</strong>. Số tiền chênh lệch sẽ được hoàn lại sau khi cập nhật.</p>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showPriceConfirmModal = false">Hủy</button>
              <div v-if="calculatePriceDifference() > 0" class="payment-buttons-group">
                <button class="btn-pay-on-delivery" @click="handleUpdateOrderWithCOD" :disabled="isProcessing">
                  {{ isProcessing ? 'Đang xử lý...' : 'Thanh toán khi nhận hàng' }}
                </button>
                <button class="btn-confirm-payment" @click="handleUpdateWithPayment" :disabled="isProcessing">
                  {{ isProcessing ? 'Đang xử lý...' : 'Thanh toán' }}
                </button>
              </div>
              <button v-else class="btn-confirm-update" @click="handleUpdateOrder" :disabled="isProcessing">
                {{ isProcessing ? 'Đang xử lý...' : 'Xác nhận cập nhật' }}
              </button>
            </div>
          </div>
        </div>

      </div>
    </main>

    <!-- Date Picker Modal -->
    <div v-if="showDatePicker" class="modal-overlay" @click.self="showDatePicker = false">
      <div class="modal-content date-picker-modal">
        <div class="modal-header">
          <h3>Chọn khoảng thời gian</h3>
          <button class="modal-close" @click="showDatePicker = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="date-picker-form">
            <div class="form-group">
              <label>Từ ngày:</label>
              <input
                type="date"
                v-model="tempStartDate"
                class="form-input"
                :max="tempEndDate || undefined"
              />
            </div>
            <div class="form-group">
              <label>Đến ngày:</label>
              <input
                type="date"
                v-model="tempEndDate"
                class="form-input"
                :min="tempStartDate || undefined"
                :max="new Date().toISOString().split('T')[0]"
              />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showDatePicker = false">Hủy</button>
          <button class="btn-confirm" @click="applyDateFilter">Áp dụng</button>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="tracking-footer">
      <FooterLayout/>
    </footer>

    <!-- Toast Notification -->
    <Toast ref="toastRef" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'
import FreeMapPicker from '@/components/FreeMapPicker.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'
import shippingService from '@/services/shippingService'

const router = useRouter()
const route = useRoute()

const API_BASE_URL = 'http://localhost:8080'

// Authentication state
const isLoggedIn = ref(false)
const userInfo = ref(null)

// Form data for search by order code (for non-logged in users)
const searchForm = ref({
  orderId: ''
})

const errors = ref({})
const isSearching = ref(false)
const isLoadingOrders = ref(false)
const orderDetails = ref(null)
const showNoOrder = ref(false)
const refreshInterval = ref(null)

// Order update state
const showCancelConfirmModal = ref(false)
const showUpdateModal = ref(false)
const showPriceConfirmModal = ref(false)
const showConfirmReceivedModal = ref(false)
const showReviewModal = ref(false)
const hasConfirmedReceived = ref(false)
const hasSubmittedReview = ref(false) // Track đã gửi review chưa
const isProcessing = ref(false)
const updateError = ref('')
const hasUpdatedOnce = ref(false)
const newShippingFee = ref(null)
const pendingUpdateRequest = ref(null) // Lưu request update để dùng sau khi thanh toán
const isConfirmingRefund = ref(false)

// Review form state
const reviewForms = ref([])
const isSubmittingReview = ref(false)
const reviewError = ref('')
const toastRef = ref(null) // Toast notification ref

// Address selection state
const userAddresses = ref([])
const selectedAddressId = ref(null)
const provinces = ref([])
const districts = ref([])
const isLoadingProvinces = ref(false)
const isLoadingDistricts = ref(false)
const isCalculatingShipping = ref(false)
const shippingError = ref('')
const selectedProvinceId = ref(null)
const selectedDistrictId = ref(null)
const deliveryAddressType = ref('address') // 'address' or 'pickup'

// Product modal state
const showProductModal = ref(false)
const productList = ref([])
const filteredProductList = ref([])
const selectedProducts = ref([])
const productSearchText = ref('')
const isLoadingProducts = ref(false)

// Update form
const updateForm = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  tinhThanh: '',
  quanHuyen: '',
  ghiChu: '',
  products: []
})

// Check if order has been updated once (check for ADDITIONAL_FEE in payment history)
const checkHasUpdatedOnce = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) {
    hasUpdatedOnce.value = false
    return
  }

  try {
    // Load payment history from tracking API
    const { data: trackingData } = await api.get(`/api/hoa-don/tracking/${orderDetails.value.maHoaDon}`)
    const paymentHistory = trackingData.lichSuThanhToan || []

    // Check if there's any ADDITIONAL_FEE payment
    hasUpdatedOnce.value = paymentHistory.some(payment => payment.loaiThanhToan === 'ADDITIONAL_FEE')
  } catch (error) {
    console.error('Error checking update status:', error)
    hasUpdatedOnce.value = false
  }
}

// New variables for the redesigned interface
const selectedStatus = ref('all')
const searchQuery = ref('')

const statusTabs = ref([
  { label: 'Tất cả', value: 'all' },
  { label: 'Hoàn thành', value: 'completed' },
  { label: 'Đã hủy', value: 'cancelled' },
  { label: 'Trả hàng/Hoàn tiền', value: 'return' }
])

// Orders data - loaded from API
const orders = ref([])

// Check user login status
const checkUserLogin = () => {
  // Website uses customerToken and customerUser, not user_token and user_data
  const customerToken = localStorage.getItem('customerToken')
  const customerUser = localStorage.getItem('customerUser')
  
  // Also check for admin token/user_token for backward compatibility
  const userToken = localStorage.getItem('user_token')
  const userData = localStorage.getItem('user_data')

  if (customerToken && customerUser) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(customerUser)
      console.log('✅ Customer logged in:', userInfo.value)
    } catch (error) {
      console.error('Error parsing customer user data:', error)
      isLoggedIn.value = false
      userInfo.value = null
    }
  } else if (userToken && userData) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(userData)
      console.log('✅ User logged in (admin):', userInfo.value)
    } catch (error) {
      console.error('Error parsing user data:', error)
      isLoggedIn.value = false
      userInfo.value = null
    }
  } else {
    isLoggedIn.value = false
    userInfo.value = null
    console.log('❌ No user found in localStorage')
  }
}

// Load orders for logged in user
const loadUserOrders = async () => {
  if (!isLoggedIn.value || !userInfo.value) {
    return
  }

  // Get khachHangId from userInfo
  const khachHangId = userInfo.value.id || userInfo.value.khachHangId || userInfo.value.userId

  if (!khachHangId) {
    console.error('Không tìm thấy ID khách hàng', userInfo.value)
    orders.value = []
    return
  }

  isLoadingOrders.value = true

  try {
    const response = await api.get(`/api/hoa-don/khach-hang/${khachHangId}`)
    const data = response.data || response

    if (data && Array.isArray(data)) {
      // Transform API response to match UI format
      orders.value = data.map(order => {
        // Get chiTietHoaDon or chiTietDonHang from order
        const chiTiet = order.chiTietHoaDon || order.chiTietDonHang || []

        return {
          id: order.maHoaDon || order.id,
          date: order.ngayTao,
          status: mapTrangThaiToStatus(order.trangThai),
          total: order.tongTien || 0,
          items: chiTiet,
          originalOrder: order
        }
      })
      
      // If orderId is in query params, open that order's detail
      if (route.query.orderId) {
        const orderId = route.query.orderId
        const foundOrder = orders.value.find(o => 
          (o.id || '').toString() === orderId.toString()
        )
        if (foundOrder && foundOrder.originalOrder) {
          // Use nextTick to ensure DOM is ready
          await nextTick()
          viewOrderDetail(foundOrder.originalOrder)
        } else {
          // If order not found in list, try to load it directly using tracking API
          searchForm.value.orderId = orderId
          searchOrder()
        }
      }
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('Error loading user orders:', error)
    orders.value = []
  } finally {
    isLoadingOrders.value = false
  }
}

// Map backend trangThai to frontend status
const mapTrangThaiToStatus = (trangThai) => {
  // Backend status: 0=Chờ xác nhận, 1=Chờ giao hàng, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy
  const statusMap = {
    0: 'pending',
    1: 'pending',
    2: 'shipping',
    3: 'completed',
    4: 'cancelled'
  }
  return statusMap[trangThai] || 'pending'
}

// Load order detail directly from orderId (for direct links from email or order list)
const loadOrderDetail = async (orderId) => {
  if (!orderId || !orderId.trim()) {
    console.warn('No orderId provided to loadOrderDetail')
    return
  }

  isSearching.value = true
  showNoOrder.value = false
  orderDetails.value = null

  try {
    // Check if orderId is a number (ID) or string (maHoaDon)
    const orderIdStr = orderId.trim()
    const isNumeric = /^\d+$/.test(orderIdStr)
    
    let result
    if (isNumeric) {
      // If orderId is numeric, try tracking-by-id endpoint first
      try {
        const { data } = await api.get(`/api/hoa-don/tracking-by-id/${orderIdStr}`)
        result = data
      } catch (error) {
        // If tracking-by-id fails, try regular tracking endpoint (in case it's actually a maHoaDon that looks like a number)
        console.warn(`⚠️ tracking-by-id failed for ${orderIdStr}, trying tracking endpoint...`)
        const { data } = await api.get(`/api/hoa-don/tracking/${orderIdStr}`)
        result = data
      }
    } else {
      // If orderId is not numeric, use tracking endpoint (maHoaDon)
      const { data } = await api.get(`/api/hoa-don/tracking/${orderIdStr}`)
      result = data
    }
    
    // Copy the rest of the logic from searchOrder() - same processing

    console.log('🔍 API Response:', result)
    console.log('🔍 Full lichSuTrangThai from API:', result.lichSuTrangThai)
    console.log('🔍 lichSuTrangThai length:', result.lichSuTrangThai?.length)
    if (result.lichSuTrangThai && result.lichSuTrangThai.length > 0) {
      result.lichSuTrangThai.forEach((item, idx) => {
        console.log(`🔍 API History ${idx}:`, JSON.stringify(item, null, 2))
      })
    }

    if (result && result.maHoaDon) {
      // Get products from danhSachSanPham (tracking endpoint structure)
      const products = result.danhSachSanPham || []

      // Get product image URL helper
      const getProductImage = (imagePath) => {
        if (!imagePath) return '/placeholder-product.jpg'
        if (imagePath.startsWith('http')) return imagePath
        return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
      }

      // Ensure trangThai is a number
      const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

      console.log('📊 Status from API:', result.trangThai, 'Type:', typeof result.trangThai, 'Parsed:', trangThai)

      orderDetails.value = {
        id: result.maHoaDon,
        maHoaDon: result.maHoaDon,
        tenKhachHang: result.tenKhachHang,
        soDienThoai: result.soDienThoai,
        email: result.email || '',
        diaChi: result.diaChi || '',
        tinhThanh: result.tinhThanh || '',
        quanHuyen: result.quanHuyen || '',
        ngayTao: result.ngayTao,
        ngayDat: result.ngayDat || result.ngayTao,
        ngayCapNhat: result.ngayCapNhat,
        loaiHoaDon: result.loaiHoaDon || 'BAN_ONLINE', // Important for timeline
        // Use correct price fields from tracking API
        tongTienHang: result.tongTienHang || 0,
        tongTien: result.tongTien || result.tongTienHang || 0,
        tongTienSauGiam: result.tongTienSauGiam || 0,
        thanhTien: result.thanhTien || result.tongTienSauGiam || 0,
        giamGia: result.giamGia || 0,
        phiVanChuyen: result.phiVanChuyen || 0,
        trangThai: trangThai, // Ensure it's a number
        maVoucher: result.maVoucher,
        phieuGiamGia: result.phieuGiamGia,
        phieuGiamGiaId: result.phieuGiamGiaId,
        ghiChu: result.ghiChu,
        lichSuTrangThai: result.lichSuTrangThai || [],
        lichSuThanhToan: result.lichSuThanhToan || [],
        chiTietDonHang: products.map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: getProductImage(item.hinhAnh),
          // Lưu IMEI từ API response
          imeis: item.imeis || (item.imei ? [item.imei] : []),
          imei: item.imei || null,
          // Rating state
          hasRated: false,
          userRating: null,
          tempRating: null,
          sanPhamId: item.sanPhamId || item.idSanPham,
          chiTietSanPhamId: item.chiTietSanPhamId || item.id
        }))
      }

      console.log('✅ Order details set:', orderDetails.value)
      console.log('📊 Final status in orderDetails:', orderDetails.value.trangThai, 'Type:', typeof orderDetails.value.trangThai)
      console.log('📅 ngayTao:', orderDetails.value.ngayTao)
      console.log('📅 ngayCapNhat:', orderDetails.value.ngayCapNhat)
      console.log('📅 lichSuTrangThai:', orderDetails.value.lichSuTrangThai)
      if (orderDetails.value.lichSuTrangThai && orderDetails.value.lichSuTrangThai.length > 0) {
        orderDetails.value.lichSuTrangThai.forEach((item, idx) => {
          console.log(`📅 History ${idx}:`, {
            trangThai: item.trangThai,
            thoiGian: item.thoiGian,
            tenTrangThai: item.tenTrangThai,
            moTa: item.moTa
          })
        })
      }
      showNoOrder.value = false

      // Check if order has been updated once
      await checkHasUpdatedOnce()
      
      // Check if customer has confirmed received (for showing review button)
      // Assume confirmed if trangThai === 3 and user is logged in
      if (orderDetails.value.trangThai === 3 && isLoggedIn.value) {
        hasConfirmedReceived.value = true
      }

      // Start auto-refresh to update status when admin changes it
      startAutoRefresh()
    } else {
      showNoOrder.value = true
      orderDetails.value = null
    }
  } catch (error) {
    console.error('Error loading order detail:', error)
    if (error.response) {
      // API trả về lỗi
      if (error.response.status === 404) {
        console.warn(`⚠️ Order not found: ${orderId}`)
      } else {
        console.error(`❌ API Error (${error.response.status}):`, error.response.data)
      }
    } else if (error.request) {
      // Request được gửi nhưng không có response
      console.error('❌ No response from server:', error.request)
    } else {
      // Lỗi khi setup request
      console.error('❌ Error setting up request:', error.message)
    }
    showNoOrder.value = true
    orderDetails.value = null
  } finally {
    isSearching.value = false
  }
}

// Methods for non-logged in users: search order by code (kept for backward compatibility, but not used)
const searchOrder = async () => {
  if (!searchForm.value.orderId.trim()) {
    errors.value.orderId = 'Vui lòng nhập mã đơn hàng'
    return
  }
  
  // Use loadOrderDetail instead
  await loadOrderDetail(searchForm.value.orderId)
}

const resetSearch = () => {
  searchForm.value = { orderId: '' }
  errors.value = {}
  orderDetails.value = null
  showNoOrder.value = false
  stopAutoRefresh() // Stop auto-refresh when clearing search
}

const contactSupport = () => {
  // You can implement actual contact support functionality here
  if (toastRef.value) {
    toastRef.value.showToast('info', 'Liên hệ hỗ trợ', '📞 Hotline: 1900 1234\n📧 Email: support@phonix.com\n⏰ Giờ làm việc: 8:00 - 22:00 (Hàng ngày)')
  }
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  if (!dateString) return 'Không có'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Không có'
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
      date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
  } catch (e) {
    return 'Không có'
  }
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${hours}:${minutes} ${day}/${month}/${year}`
}

const formatAddress = (order) => {
  const parts = []
  // Try multiple possible address fields
  const diaChi = order.diaChi || order.diaChiGiaoHang || ''
  const quanHuyen = order.quanHuyen || order.quanHuyenGiaoHang || ''
  const tinhThanh = order.tinhThanh || order.tinhThanhGiaoHang || ''

  // Nếu là "Lấy tại cửa hàng" hoặc "Tại cửa hàng", chỉ hiển thị "Lấy tại cửa hàng" thôi
  if (diaChi === 'Lấy tại cửa hàng' || diaChi === 'Tại cửa hàng') {
    return diaChi
  } else {
    // Địa chỉ bình thường
    if (diaChi) parts.push(diaChi)
    if (quanHuyen) parts.push(quanHuyen)
    if (tinhThanh) parts.push(tinhThanh)
  }

  return parts.length > 0 ? parts.join(', ') : 'Không có'
}

const getLoaiHoaDonText = (loaiHoaDon) => {
  const typeMap = {
    'BAN_ONLINE': 'Đơn online',
    'ONLINE': 'Đơn online',
    'BAN_THUONG': 'Đơn tại quầy',
    'NORMAL': 'Đơn tại quầy'
  }
  return typeMap[loaiHoaDon] || loaiHoaDon || 'Không xác định'
}

const getStatusBadgeClass = (status) => {
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }
  const classMap = {
    'pending': 'badge-warning',
    'shipping': 'badge-info',
    'completed': 'badge-success',
    'cancelled': 'badge-danger',
    'return': 'badge-secondary'
  }
  return classMap[status] || 'badge-warning'
}

// Get order status steps for progress bar
const getOrderStatusSteps = (order) => {
  if (!order) return []

  const isCancelled = order.trangThai === 4
  const isBanThuong = order.loaiHoaDon === 'BAN_THUONG' || order.loaiHoaDon === 'NORMAL'

  if (isCancelled) {
    const cancelTime = order.ngayCapNhat || order.ngayTao
    return [{
      title: 'Đã hủy',
      icon: 'bi-x-circle-fill',
      time: cancelTime ? formatDateTime(cancelTime) : '',
      isActive: true,
      isCompleted: false,
      status: 4
    }]
  }

  if (isBanThuong) {
    const completionTime = order.ngayTao ? formatDateTime(order.ngayTao) : ''
    return [{
      title: 'Hoàn thành',
      icon: 'bi-check-circle-fill',
      time: completionTime,
      isActive: true,
      isCompleted: true,
      status: 3
    }]
  }

  // BAN_ONLINE: 4 steps
  const steps = [
    { title: 'Chờ xác nhận', icon: 'bi-check-circle-fill', status: 0 },
    { title: 'Chờ giao hàng', icon: 'bi-check-circle-fill', status: 1 },
    { title: 'Đang giao hàng', icon: 'bi-truck', status: 2 },
    { title: 'Hoàn thành', icon: 'bi-check-circle-fill', status: 3 }
  ]

  const currentStatus = order.trangThai || 0

  // Get history from lichSuTrangThai if available
  const history = order.lichSuTrangThai || []

  return steps.map((step, index) => {
    let time = ''
    const historyItem = history.find(h => h.trangThai === step.status)
    if (historyItem && historyItem.thoiGian) {
      time = formatDateTime(historyItem.thoiGian)
    } else if (step.status === 0 && order.ngayTao && currentStatus > 0) {
      // Fallback: use ngayTao for step 0 if order has progressed
      time = formatDateTime(order.ngayTao)
    }

    const isCompleted = step.status < currentStatus
    const isActive = step.status === currentStatus

    return {
      ...step,
      time,
      isActive,
      isCompleted
    }
  })
}

// Get order history
const getOrderHistory = (order) => {
  if (!order) return []

  const history = []

  // Add history from lichSuTrangThai if available
  if (order.lichSuTrangThai && Array.isArray(order.lichSuTrangThai)) {
    order.lichSuTrangThai.forEach(item => {
      const statusText = getStatusText(item.trangThai)
      history.push({
        action: `Chuyển đơn sang trạng thái: ${statusText}`,
        time: item.thoiGian ? formatDateTime(item.thoiGian) : '',
        statusText: item.trangThai === order.trangThai ? 'Đã xử lý' : 'Chưa xử lý',
        statusClass: item.trangThai === order.trangThai ? 'status-processed' : 'status-pending',
        nguoiThucHien: item.nguoiThucHien || order.maNhanVien || order.nguoiTao || 'online_customer'
      })
    })
  } else {
    // Fallback: create history from order data
    if (order.ngayTao) {
      const statusText = getStatusText(order.trangThai)
      history.push({
        action: `Chuyển đơn sang trạng thái: ${statusText}`,
        time: formatDateTime(order.ngayTao),
        statusText: 'Đã xử lý',
        statusClass: 'status-processed',
        nguoiThucHien: order.maNhanVien || order.nguoiTao || 'online_customer'
      })
    }
  }

  return history.sort((a, b) => {
    // Sort by time descending (newest first)
    if (!a.time || !b.time) return 0
    return new Date(b.time) - new Date(a.time)
  })
}



const getDeliveryMethodText = (method) => {
  if (!method) return 'Chưa chọn'

  const methodMap = {
    'standard': 'Tiêu chuẩn',
    'express': 'Nhanh',
    'ghn': 'Hỏa tốc',
    'fast': 'Nhanh',
    'urgent': 'Hỏa tốc',
    'pickup': 'Lấy tại cửa hàng',
    'delivery': 'Giao hàng tận nơi'
  }
  return methodMap[method.toLowerCase()] || method
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'cod': 'Thanh toán khi nhận hàng',
    'bank-transfer': 'Chuyển khoản ngân hàng',
    'momo': 'Ví MoMo',
    'vnpay': 'VNPay'
  }
  return methodMap[method] || method
}

const getStatusClass = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }

  const statusMap = {
    'pending': 'status-pending',
    'shipping': 'status-shipping',
    'completed': 'status-completed',
    'cancelled': 'status-cancelled',
    'return': 'status-return'
  }
  return statusMap[status] || 'status-pending'
}

const getStatusText = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    const statusTextMap = {
      0: 'Chờ xác nhận',
      1: 'Chờ giao hàng',
      2: 'Đang giao hàng',
      3: 'Đã nhận hàng',
      4: 'Đã hủy'
    }
    return statusTextMap[status] || 'Chờ xác nhận'
  }

  const textMap = {
    'pending': 'Chờ xác nhận',
    'shipping': 'Đang giao hàng',
    'completed': 'Đã nhận hàng',
    'cancelled': 'Đã hủy',
    'return': 'Trả hàng/Hoàn tiền'
  }
  return textMap[status] || 'Chờ xác nhận'
}

const getStatusIcon = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }

  const iconMap = {
    'pending': 'bi-clock-history',
    'shipping': 'bi-truck',
    'completed': 'bi-check-circle-fill',
    'cancelled': 'bi-x-circle-fill',
    'return': 'bi-arrow-return-left'
  }
  return iconMap[status] || 'bi-clock-history'
}

const getTotalAmountClass = (status) => {
  return status === 'cancelled' ? 'total-cancelled' : 'total-normal'
}

const getPaymentStatusClass = (paymentStatus) => {
  return paymentStatus.includes('thất bại') ? 'payment-failed' : 'payment-success'
}

// Timeline steps - giống HoaDonDetailPage.vue
const trackingSteps = ref([
  { title: 'Chờ xác nhận', icon: 'clock', time: '', status: 0 },
  { title: 'Chờ giao hàng', icon: 'box', time: '', status: 1 },
  { title: 'Đang giao hàng', icon: 'truck', time: '', status: 2 },
  { title: 'Hoàn thành', icon: 'check-circle', time: '', status: 3 },
  { title: 'Đã hủy', icon: 'times-circle', time: '', status: 4 }
])

// Computed property để lấy steps phù hợp với loại đơn hàng và cập nhật thời gian từ lichSuTrangThai
const getTrackingSteps = computed(() => {
  if (!orderDetails.value) {
    return trackingSteps.value.filter(step => step.status !== 4)
  }

  const isBanThuong = orderDetails.value.loaiHoaDon === 'BAN_THUONG' || orderDetails.value.loaiHoaDon === 'NORMAL'
  const isCancelled = orderDetails.value.trangThai === 4

  // Nếu đơn hàng đã hủy - chỉ hiển thị step "Đã hủy"
  if (isCancelled) {
    let cancelTime = ''
    const timeSource = orderDetails.value.ngayCapNhat || orderDetails.value.ngayTao
    if (timeSource) {
      const date = new Date(timeSource)
      cancelTime = date.toLocaleTimeString('vi-VN') + ' ' + date.toLocaleDateString('vi-VN')
    }
    return [{ title: 'Đã hủy', icon: 'times-circle', time: cancelTime, status: 4, isCancelled: true }]
  }

  // Nếu không phải đã hủy, hiển thị timeline bình thường
  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước "Hoàn thành"
    let completionTime = ''
    if (orderDetails.value.ngayTao) {
      const ngayTao = new Date(orderDetails.value.ngayTao)
      completionTime = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
    }
    return [{ title: 'Hoàn thành', icon: 'check-circle', time: completionTime, status: 3, isCompleted: true }]
  } else {
    // Bán online: 4 bước (không bao gồm "Đã hủy" khi chưa hủy)
    // Tạo bản sao của steps để không mutate trackingSteps.value
    const steps = trackingSteps.value.filter(step => step.status !== 4).map(step => ({ ...step }))

    // Cập nhật thời gian từ lichSuTrangThai - luôn cập nhật tất cả
    console.log('🔍 getTrackingSteps - lichSuTrangThai:', orderDetails.value.lichSuTrangThai)

    if (orderDetails.value.lichSuTrangThai && orderDetails.value.lichSuTrangThai.length > 0) {
      // Sắp xếp theo thời gian để đảm bảo thứ tự đúng
      const sortedHistory = [...orderDetails.value.lichSuTrangThai].sort((a, b) => {
        const timeA = new Date(a.thoiGian || a.thoiGianCapNhat || 0).getTime()
        const timeB = new Date(b.thoiGian || b.thoiGianCapNhat || 0).getTime()
        return timeA - timeB
      })

      console.log('🔍 Sorted history:', sortedHistory)

      // Group by status và lấy entry mới nhất cho mỗi status
      const statusMap = new Map()

      sortedHistory.forEach((trangThaiItem) => {
        const status = trangThaiItem.trangThai
        // Kiểm tra nhiều field có thể có thời gian
        const thoiGian = trangThaiItem.thoiGian || trangThaiItem.thoiGianCapNhat || trangThaiItem.ngayTao || trangThaiItem.time

        if (thoiGian) {
          try {
            const thoiGianDate = new Date(thoiGian)
            if (!isNaN(thoiGianDate.getTime())) {
              // Lưu entry mới nhất cho mỗi status
              if (!statusMap.has(status) || new Date(thoiGian) > new Date(statusMap.get(status).thoiGian)) {
                statusMap.set(status, { ...trangThaiItem, thoiGian })
              }
            }
          } catch (e) {
            console.error(`❌ Error parsing date for status ${status}:`, e, thoiGian)
          }
        }
      })

      console.log('🔍 Status map:', Array.from(statusMap.entries()))

      // Cập nhật thời gian cho các step
      statusMap.forEach((trangThaiItem, status) => {
        const thoiGian = trangThaiItem.thoiGian || trangThaiItem.thoiGianCapNhat || trangThaiItem.ngayTao || trangThaiItem.time

        if (thoiGian) {
          try {
            const thoiGianDate = new Date(thoiGian)
            if (!isNaN(thoiGianDate.getTime())) {
              const timeString = thoiGianDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                thoiGianDate.toLocaleDateString('vi-VN')

              const step = steps.find(s => s.status === status)

              if (step) {
                // Step 0 (Chờ xác nhận): chỉ set khi tất cả sản phẩm đã có IMEI (giống admin)
                if (status === 0) {
                  if (checkAllProductsHaveImei()) {
                    step.time = timeString
                    console.log(`✅ Set time for step "Chờ xác nhận" từ DB (IMEI đầy đủ):`, timeString)
                  } else {
                    console.log('⚠️ Skipping auto-set time for step "Chờ xác nhận" - chưa có đủ IMEI')
                  }
                } else {
                  // Steps khác: luôn set theo DB
                  step.time = timeString
                  console.log(`✅ Set time for step ${status} (${step.title}):`, timeString)
                }
              } else {
                console.warn(`⚠️ No step found for status ${status}`)
              }
            }
          } catch (e) {
            console.error(`❌ Error formatting date for status ${status}:`, e)
          }
        }
      })

      console.log('🔍 Final steps with times:', steps.map(s => ({ title: s.title, status: s.status, time: s.time })))
    } else {
      console.warn('⚠️ No lichSuTrangThai found')
    }

    // Fallback: Nếu thiếu thời gian cho các step, suy ra từ status hiện tại
    // Nếu status > 0, các step trước đó đã hoàn thành (có thể chưa có trong lichSuTrangThai)
    const currentStatus = orderDetails.value.trangThai
    const currentIndex = getCurrentStepIndex.value

    // Nếu có ngayCapNhat, dùng nó cho step hiện tại
    if (orderDetails.value.ngayCapNhat && currentIndex >= 0 && currentIndex < steps.length) {
      const currentStep = steps[currentIndex]
      if (!currentStep.time) {
        // Step 0 (Chờ xác nhận): chỉ set time khi đã có đủ IMEI
        if (currentIndex === 0) {
          if (checkAllProductsHaveImei()) {
            try {
              const ngayCapNhat = new Date(orderDetails.value.ngayCapNhat)
              if (!isNaN(ngayCapNhat.getTime())) {
                currentStep.time = ngayCapNhat.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                  ngayCapNhat.toLocaleDateString('vi-VN')
                console.log(`⏰ Fallback: Set time for step "Chờ xác nhận" from ngayCapNhat (IMEI đầy đủ):`, currentStep.time)
              }
            } catch (e) {
              console.error('❌ Error formatting ngayCapNhat:', e)
            }
          } else {
            console.log('⚠️ Skipping fallback time for step "Chờ xác nhận" from ngayCapNhat - chưa có đủ IMEI')
          }
        } else {
          // Steps khác: luôn set time
          try {
            const ngayCapNhat = new Date(orderDetails.value.ngayCapNhat)
            if (!isNaN(ngayCapNhat.getTime())) {
              currentStep.time = ngayCapNhat.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                ngayCapNhat.toLocaleDateString('vi-VN')
              console.log(`⏰ Fallback: Set time for current step ${currentStep.title} from ngayCapNhat:`, currentStep.time)
            }
          } catch (e) {
            console.error('❌ Error formatting ngayCapNhat:', e)
          }
        }
      }
    }

    // Nếu có ngayTao, dùng nó cho step đầu tiên (nếu chưa có time) - CHỈ khi đã có đủ IMEI
    if (orderDetails.value.ngayTao && steps[0] && !steps[0].time) {
      // Step 0 (Chờ xác nhận): chỉ set time khi tất cả sản phẩm đã có IMEI
      if (checkAllProductsHaveImei()) {
        try {
          const ngayTao = new Date(orderDetails.value.ngayTao)
          if (!isNaN(ngayTao.getTime())) {
            steps[0].time = ngayTao.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
              ngayTao.toLocaleDateString('vi-VN')
            console.log(`⏰ Fallback: Set time for step 0 (${steps[0].title}) from ngayTao (IMEI đầy đủ):`, steps[0].time)
          }
        } catch (e) {
          console.error('❌ Error formatting ngayTao:', e)
        }
      } else {
        console.log('⚠️ Skipping fallback time for step "Chờ xác nhận" - chưa có đủ IMEI')
      }
    }

    // Nếu status > 0 và các step trước đó chưa có time, dùng ngayTao làm thời gian ước tính
    // (vì các step đã completed nhưng có thể chưa có entry trong lichSuTrangThai)
    if (currentStatus > 0 && orderDetails.value.ngayTao) {
      try {
        const ngayTao = new Date(orderDetails.value.ngayTao)
        if (!isNaN(ngayTao.getTime())) {
          for (let i = 0; i < currentIndex && i < steps.length; i++) {
            if (!steps[i].time) {
              // Step 0 (Chờ xác nhận): chỉ set time khi đã có đủ IMEI
              if (i === 0) {
                if (checkAllProductsHaveImei()) {
                  steps[i].time = ngayTao.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                    ngayTao.toLocaleDateString('vi-VN')
                  console.log(`⏰ Fallback: Set estimated time for step "Chờ xác nhận" from ngayTao (IMEI đầy đủ):`, steps[i].time)
                } else {
                  console.log('⚠️ Skipping fallback time for step "Chờ xác nhận" - chưa có đủ IMEI')
                }
              } else {
                // Steps khác: luôn set time
                steps[i].time = ngayTao.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                  ngayTao.toLocaleDateString('vi-VN')
                console.log(`⏰ Fallback: Set estimated time for completed step ${steps[i].title} from ngayTao:`, steps[i].time)
              }
            }
          }
        }
      } catch (e) {
        console.error('❌ Error in fallback time estimation:', e)
      }
    }

    console.log('🔍 Final steps after fallback:', steps.map(s => ({ title: s.title, status: s.status, time: s.time })))

    return steps
  }
})

// Get current step index based on status
const getCurrentStepIndex = computed(() => {
  if (!orderDetails.value) return 0

  const status = orderDetails.value.trangThai
  const loaiHoaDon = orderDetails.value.loaiHoaDon
  const isBanThuong = loaiHoaDon === 'BAN_THUONG' || loaiHoaDon === 'NORMAL'
  const isCancelled = status === 4

  if (isCancelled) return 0
  if (isBanThuong) return 0

  switch (status) {
    case 0: return 0  // Chờ xác nhận
    case 1: return 1  // Chờ giao hàng
    case 2: return 2  // Đang giao hàng
    case 3: return 3  // Hoàn thành
    default: return 0
  }
})

// Get step icon - hiển thị check nếu completed hoặc active với time
const getStepIcon = (step, index) => {
  if (!orderDetails.value) return 'bi-check-circle-fill'

  const currentIndex = getCurrentStepIndex.value
  const isCompleted = index < currentIndex || (step.time && index === currentIndex)
  const isCancelled = step.isCancelled

  if (isCancelled) {
    return 'bi-x-circle-fill'
  }

  if (isCompleted) {
    return 'bi-check-circle-fill'
  }

  return 'bi-check-circle-fill' // Fallback
}

// Get original icon for active step without time
const getStepIconOriginal = (iconName) => {
  const iconMap = {
    'clock': 'bi-clock-history',
    'box': 'bi-box',
    'truck': 'bi-truck',
    'check-circle': 'bi-check-circle-fill',
    'times-circle': 'bi-x-circle-fill'
  }
  return iconMap[iconName] || 'bi-clock-history'
}

// Auto-refresh order status
const startAutoRefresh = () => {
  // Clear existing interval
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }

  // Only refresh if we have an order displayed
  if (!orderDetails.value || !orderDetails.value.maHoaDon) {
    return
  }

  // Refresh every 5 seconds to get latest status
  refreshInterval.value = setInterval(async () => {
    // Check if orderDetails still exists and has maHoaDon
    if (!orderDetails.value || !orderDetails.value.maHoaDon) {
      stopAutoRefresh()
      return
    }

    try {
      const { data: result } = await api.get(`/api/hoa-don/tracking/${orderDetails.value.maHoaDon}`)

      if (result && result.maHoaDon && orderDetails.value) {
        const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

        // Only update if status changed and orderDetails still exists
        if (orderDetails.value && orderDetails.value.trangThai !== trangThai) {
          console.log('🔄 Status changed from', orderDetails.value.trangThai, 'to', trangThai)
          orderDetails.value.trangThai = trangThai
          // Also update other fields that might have changed
          orderDetails.value.ngayCapNhat = result.ngayCapNhat
          orderDetails.value.lichSuTrangThai = result.lichSuTrangThai || []
          orderDetails.value.lichSuThanhToan = result.lichSuThanhToan || []
          orderDetails.value.loaiHoaDon = result.loaiHoaDon || orderDetails.value.loaiHoaDon || 'BAN_ONLINE'
        } else if (orderDetails.value && result.lichSuTrangThai) {
          // Update lichSuTrangThai even if status hasn't changed (in case new log entry was added)
          const oldHistoryLength = orderDetails.value.lichSuTrangThai?.length || 0
          const newHistoryLength = result.lichSuTrangThai?.length || 0
          if (newHistoryLength > oldHistoryLength) {
            console.log('🔄 New status log entry added')
            orderDetails.value.lichSuTrangThai = result.lichSuTrangThai
            orderDetails.value.lichSuThanhToan = result.lichSuThanhToan || orderDetails.value.lichSuThanhToan || []
            orderDetails.value.loaiHoaDon = result.loaiHoaDon || orderDetails.value.loaiHoaDon || 'BAN_ONLINE'
          }
        }
      }
    } catch (error) {
      console.error('Error refreshing order status:', error)
      // Don't stop on error, just log it
    }
  }, 5000) // Refresh every 5 seconds
}

const stopAutoRefresh = () => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }
}

const viewOrderDetail = async (order) => {
  // Use tracking API to get full order details
  const orderId = order.maHoaDon || order.id
  if (!orderId) return

  try {
    const { data: result } = await api.get(`/api/hoa-don/tracking/${orderId}`)

    if (result && result.maHoaDon) {
      const products = result.danhSachSanPham || []

      // Get product image URL helper
      const getProductImage = (imagePath) => {
        if (!imagePath) {
          // Tạo placeholder SVG local
          return 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
        }
        if (imagePath.startsWith('http')) return imagePath
        return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
      }

      // Ensure trangThai is a number
      const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

      orderDetails.value = {
        id: result.maHoaDon,
        maHoaDon: result.maHoaDon,
        tenKhachHang: result.tenKhachHang,
        soDienThoai: result.soDienThoai,
        email: result.email || '',
        diaChi: result.diaChi || '',
        tinhThanh: result.tinhThanh || '',
        quanHuyen: result.quanHuyen || '',
        ngayTao: result.ngayTao,
        ngayDat: result.ngayDat || result.ngayTao,
        ngayCapNhat: result.ngayCapNhat,
        loaiHoaDon: result.loaiHoaDon || 'BAN_ONLINE', // Important for timeline
        tongTienHang: result.tongTienHang || 0,
        tongTien: result.tongTien || result.tongTienHang || 0,
        tongTienSauGiam: result.tongTienSauGiam || 0,
        thanhTien: result.thanhTien || result.tongTienSauGiam || 0,
        giamGia: result.giamGia || 0,
        phiVanChuyen: result.phiVanChuyen || 0,
        trangThai: trangThai, // Ensure it's a number
        maVoucher: result.maVoucher,
        phieuGiamGia: result.phieuGiamGia,
        phieuGiamGiaId: result.phieuGiamGiaId,
        ghiChu: result.ghiChu,
        lichSuTrangThai: result.lichSuTrangThai || [],
        lichSuThanhToan: result.lichSuThanhToan || [],
        chiTietDonHang: products.map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: getProductImage(item.hinhAnh),
          // Lưu IMEI từ API response
          imeis: item.imeis || (item.imei ? [item.imei] : []),
          imei: item.imei || null,
          // Rating state
          hasRated: false,
          userRating: null,
          tempRating: null,
          sanPhamId: item.sanPhamId || item.idSanPham,
          chiTietSanPhamId: item.chiTietSanPhamId || item.id
        }))
      }

      // Check if order has been updated once
      await checkHasUpdatedOnce()
      
      // Check if customer has confirmed received (for showing review button)
      // Assume confirmed if trangThai === 3 and user is logged in
      if (orderDetails.value.trangThai === 3 && isLoggedIn.value) {
        hasConfirmedReceived.value = true
      }

      // Start auto-refresh
      startAutoRefresh()
    }
  } catch (error) {
    console.error('Error loading order details:', error)
    // Fallback: use order data if available
    if (order.originalOrder) {
      const orig = order.originalOrder
      orderDetails.value = {
        id: orig.maHoaDon || order.id,
        maHoaDon: orig.maHoaDon || order.id,
        tenKhachHang: orig.tenKhachHang,
        soDienThoai: orig.soDienThoai,
        email: orig.email || '',
        diaChi: orig.diaChi || '',
        tinhThanh: orig.tinhThanh || '',
        quanHuyen: orig.quanHuyen || '',
        ngayTao: orig.ngayTao,
        ngayDat: orig.ngayDat || orig.ngayTao,
        ngayCapNhat: orig.ngayCapNhat,
        tongTienHang: orig.tongTienHang || 0,
        tongTien: orig.tongTien || 0,
        tongTienSauGiam: orig.tongTienSauGiam || 0,
        thanhTien: orig.thanhTien || orig.tongTienSauGiam || 0,
        giamGia: orig.giamGia || 0,
        phiVanChuyen: orig.phiVanChuyen || 0,
        trangThai: orig.trangThai,
        maVoucher: orig.maVoucher,
        phieuGiamGia: orig.phieuGiamGia,
        phieuGiamGiaId: orig.phieuGiamGiaId,
        ghiChu: orig.ghiChu,
        lichSuTrangThai: orig.lichSuTrangThai || [],
        lichSuThanhToan: orig.lichSuThanhToan || [],
        chiTietDonHang: (orig.danhSachSanPham || orig.chiTietHoaDon || orig.chiTietDonHang || []).map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: item.hinhAnh || '/placeholder-product.jpg',
          // Lưu IMEI từ API response
          imeis: item.imeis || (item.imei ? [item.imei] : []),
          imei: item.imei || null,
          // Rating state
          hasRated: false,
          userRating: null,
          tempRating: null,
          sanPhamId: item.sanPhamId || item.idSanPham,
          chiTietSanPhamId: item.chiTietSanPhamId || item.id
        }))
      }
    }
  }
}

// Check if viewing order detail
const isViewingOrderDetail = computed(() => {
  // Show back button if logged in and either:
  // 1. Has orderDetails (order detail is loaded)
  // 2. Has orderId in query (navigated from order list or email)
  return isLoggedIn.value && !!(orderDetails.value || route.query.orderId)
})

// Date filter state
const showDatePicker = ref(false)
const startDate = ref(new Date(Date.now() - 365 * 24 * 60 * 60 * 1000)) // 1 year ago
const endDate = ref(new Date())
const tempStartDate = ref('')
const tempEndDate = ref('')

// Format date for display
const formatDateRange = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
}

// Apply date filter
const applyDateFilter = () => {
  if (tempStartDate.value && tempEndDate.value) {
    startDate.value = new Date(tempStartDate.value)
    endDate.value = new Date(tempEndDate.value)
    // Set time to end of day for endDate
    endDate.value.setHours(23, 59, 59, 999)
  }
  showDatePicker.value = false
}

// Watch for date picker open to initialize temp dates
watch(showDatePicker, (show) => {
  if (show) {
    // Format dates to YYYY-MM-DD for input type="date"
    const formatDateForInput = (date) => {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    tempStartDate.value = formatDateForInput(startDate.value)
    tempEndDate.value = formatDateForInput(endDate.value)
  }
})

// Load orders for logged in users (alias for loadUserOrders)
const loadOrders = async () => {
  await loadUserOrders()
}

// Filter orders by selected status and date range
const filteredOrders = computed(() => {
  let filtered = orders.value

  // Filter by date range
  if (startDate.value && endDate.value) {
    filtered = filtered.filter(order => {
      const orderDate = new Date(order.date || order.ngayTao || order.originalOrder?.ngayTao)
      if (isNaN(orderDate.getTime())) return false

      // Set time to start of day for startDate
      const start = new Date(startDate.value)
      start.setHours(0, 0, 0, 0)

      // Set time to end of day for endDate
      const end = new Date(endDate.value)
      end.setHours(23, 59, 59, 999)

      return orderDate >= start && orderDate <= end
    })
  }

  // Filter by status
  if (selectedStatus.value === 'all') {
    return filtered
  }

  // Map status tab values to actual status values
  const statusMapping = {
    'completed': ['completed'],
    'cancelled': ['cancelled'],
    'return': ['return']
  }

  const targetStatuses = statusMapping[selectedStatus.value] || []
  return filtered.filter(order => targetStatuses.includes(order.status))
})

// Handle cancel order
const handleCancelOrder = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  // Chỉ cho phép hủy đơn hàng khi ở trạng thái chờ xác nhận (trangThai === 0)
  if (orderDetails.value.trangThai !== 0) {
    if (toastRef.value) {
      toastRef.value.showToast('warning', 'Cảnh báo', 'Chỉ có thể hủy đơn hàng khi đơn hàng đang ở trạng thái chờ xác nhận.')
    }
    showCancelConfirmModal.value = false
    return
  }

  isProcessing.value = true
  updateError.value = ''

  try {
    // Call API to cancel order (update status to 4 - DA_HUY)
    const { data } = await api.put(`/api/hoa-don/update-status/${orderDetails.value.maHoaDon}`, {
      trangThai: 4, // DA_HUY
      nguoiThucHien: userInfo.value?.tenKhachHang || 'Khách hàng'
    })

    if (data) {
      // Update order details
      orderDetails.value.trangThai = 4
      orderDetails.value.lichSuTrangThai = data.lichSuTrangThai || orderDetails.value.lichSuTrangThai
      orderDetails.value.lichSuThanhToan = data.lichSuThanhToan || orderDetails.value.lichSuThanhToan || []

      showCancelConfirmModal.value = false
      if (toastRef.value) {
        toastRef.value.showToast('success', 'Thành công', 'Đơn hàng đã được hủy thành công!')
      }
      
      // Reload order details to get updated payment history (refund)
      await searchOrder()
    }
  } catch (error) {
    console.error('Error canceling order:', error)
    updateError.value = error.response?.data?.message || 'Lỗi khi hủy đơn hàng. Vui lòng thử lại.'
  } finally {
    isProcessing.value = false
  }
}

// Initialize update form
const initUpdateForm = async () => {
  if (!orderDetails.value) return

  // Load products để lấy thông tin soLuongTon
  if (productList.value.length === 0) {
    await loadProducts()
  }

  updateForm.value = {
    tenKhachHang: orderDetails.value.tenKhachHang || '',
    soDienThoai: orderDetails.value.soDienThoai || '',
    email: orderDetails.value.email || '',
    diaChi: orderDetails.value.diaChi || '',
    tinhThanh: orderDetails.value.tinhThanh || '',
    quanHuyen: orderDetails.value.quanHuyen || '',
    ghiChu: orderDetails.value.ghiChu || '',
    products: (orderDetails.value.chiTietDonHang || []).map(item => {
      // Tìm sản phẩm trong productList để lấy soLuongTon
      const productFromList = productList.value.find(p =>
        p.chiTietSanPhamId === item.id ||
        p.id === item.id ||
        p.chiTietSanPhamId === item.chiTietSanPhamId
      )

      return {
      ...item,
      chiTietSanPhamId: item.id, // id trong SanPhamTrackingDTO là chiTietSanPhamId
      chiTietHoaDonId: item.chiTietHoaDonId, // ID của chi tiết hóa đơn (nếu có)
      soLuong: item.soLuong || 1,
        donGia: item.donGia || item.gia || 0,
        soLuongTon: productFromList?.soLuongTon || item.soLuongTon || 0 // Lấy soLuongTon từ productList hoặc item
      }
    })
  }

  newShippingFee.value = null
  updateError.value = ''
  
  // Set delivery address type based on existing order
  if (updateForm.value.diaChi === 'Tại cửa hàng' || updateForm.value.diaChi === 'Lấy tại cửa hàng') {
    deliveryAddressType.value = 'pickup'
    updateForm.value.diaChi = 'Lấy tại cửa hàng'
  } else {
    deliveryAddressType.value = 'address'
  }
  
  // Load provinces and districts if address is not "Lấy tại cửa hàng"
  if (updateForm.value.diaChi !== 'Lấy tại cửa hàng' && updateForm.value.diaChi !== 'Tại cửa hàng') {
    await loadProvinces()
    // Try to find and set province/district from existing address
    if (updateForm.value.tinhThanh && provinces.value.length > 0) {
      const province = provinces.value.find(p => 
        p.ProvinceName.toLowerCase().includes(updateForm.value.tinhThanh.toLowerCase()) ||
        updateForm.value.tinhThanh.toLowerCase().includes(p.ProvinceName.toLowerCase())
      )
      if (province) {
        selectedProvinceId.value = province.ProvinceID
        await onProvinceChange()
        if (updateForm.value.quanHuyen && districts.value.length > 0) {
          const district = districts.value.find(d =>
            d.DistrictName.toLowerCase().includes(updateForm.value.quanHuyen.toLowerCase()) ||
            updateForm.value.quanHuyen.toLowerCase().includes(d.DistrictName.toLowerCase())
          )
          if (district) {
            selectedDistrictId.value = district.DistrictID
            await onDistrictChange()
          }
        }
      }
    }
  }
  
  // Load user addresses if logged in
  if (isLoggedIn.value && userInfo.value) {
    await loadUserAddresses()
  }
}

// Load user addresses
const loadUserAddresses = async () => {
  if (!isLoggedIn.value || !userInfo.value) return
  
  const khachHangId = userInfo.value.id || userInfo.value.khachHangId || userInfo.value.userId
  if (!khachHangId) return

  try {
    const response = await api.get(`/api/user-dia-chi/khach-hang/${khachHangId}`)
    const addresses = response.data || []
    
    userAddresses.value = addresses.map(addr => {
      const diaChi = addr.diaChi || {}
      return {
        id: addr.id,
        loaiDiaChi: addr.loaiDiaChi || 'Địa chỉ',
        diaChiChiTiet: diaChi.diaChiChiTiet || '',
        phuongXa: diaChi.phuongXa || '',
        quanHuyen: diaChi.quanHuyen || '',
        tinhThanhPho: diaChi.tinhThanhPho || '',
        macDinh: addr.macDinh || false,
        diaChi: addr.diaChi
      }
    })
  } catch (error) {
    console.error('Error loading user addresses:', error)
    userAddresses.value = []
  }
}

// Get address display text
const getAddressDisplayText = (addr) => {
  const parts = []
  if (addr.diaChiChiTiet) parts.push(addr.diaChiChiTiet)
  if (addr.phuongXa) parts.push(addr.phuongXa)
  if (addr.quanHuyen) parts.push(addr.quanHuyen)
  if (addr.tinhThanhPho) parts.push(addr.tinhThanhPho)
  return parts.join(', ') || 'Địa chỉ'
}

// Handle address selected from combobox
const onAddressSelected = async () => {
  if (!selectedAddressId.value) {
    // Nếu bỏ chọn địa chỉ, hiển thị lại phần địa chỉ chi tiết
    return
  }
  
  const addr = userAddresses.value.find(a => a.id === selectedAddressId.value)
  if (!addr) return

  const diaChi = addr.diaChi || {}
  updateForm.value.diaChi = diaChi.diaChiChiTiet || ''
  updateForm.value.tinhThanh = diaChi.tinhThanhPho || ''
  updateForm.value.quanHuyen = diaChi.quanHuyen || ''
  
  // Load provinces and set selected province/district
  await loadProvinces()
  if (updateForm.value.tinhThanh && provinces.value.length > 0) {
    const province = provinces.value.find(p => 
      p.ProvinceName.toLowerCase().includes(updateForm.value.tinhThanh.toLowerCase()) ||
      updateForm.value.tinhThanh.toLowerCase().includes(p.ProvinceName.toLowerCase())
    )
    if (province) {
      selectedProvinceId.value = province.ProvinceID
      await onProvinceChange()
      if (updateForm.value.quanHuyen && districts.value.length > 0) {
        const district = districts.value.find(d =>
          d.DistrictName.toLowerCase().includes(updateForm.value.quanHuyen.toLowerCase()) ||
          updateForm.value.quanHuyen.toLowerCase().includes(d.DistrictName.toLowerCase())
        )
        if (district) {
          selectedDistrictId.value = district.DistrictID
          await onDistrictChange()
        }
      }
    }
  }
}

// Load provinces
const loadProvinces = async () => {
  if (provinces.value.length > 0) return

  isLoadingProvinces.value = true
  try {
    const data = await shippingService.loadProvinces()
    provinces.value = data
  } catch (error) {
    console.error('Error loading provinces:', error)
  } finally {
    isLoadingProvinces.value = false
  }
}

// Handle province change
const onProvinceChange = async () => {
  selectedDistrictId.value = null
  districts.value = []

  if (selectedProvinceId.value) {
    isLoadingDistricts.value = true
    try {
      const data = await shippingService.loadDistricts(selectedProvinceId.value)
      districts.value = data
      
      // Update updateForm.tinhThanh with province name
      const province = provinces.value.find(p => p.ProvinceID === selectedProvinceId.value)
      if (province) {
        updateForm.value.tinhThanh = province.ProvinceName
      }
      
      // Recalculate shipping
      await calculateShippingRates()
    } catch (error) {
      console.error('Error loading districts:', error)
    } finally {
      isLoadingDistricts.value = false
    }
  }
}

// Handle district change
const onDistrictChange = async () => {
  if (selectedDistrictId.value) {
    // Update updateForm.quanHuyen with district name
    const district = districts.value.find(d => d.DistrictID === selectedDistrictId.value)
    if (district) {
      updateForm.value.quanHuyen = district.DistrictName
    }
    
    // Recalculate shipping
    await calculateShippingRates()
  }
}

// Handle address selected from map
const onAddressSelectedFromMap = (addressData) => {
  updateForm.value.diaChi = addressData.formatted_address || updateForm.value.diaChi
  // Recalculate shipping if province/district are set
  if (selectedProvinceId.value && selectedDistrictId.value) {
    calculateShippingRates()
  }
}

// Handle location updated from map (with province/district info)
const onLocationUpdatedFromMap = async (locationData) => {
  updateForm.value.diaChi = locationData.address || updateForm.value.diaChi

  let updatedFields = []

  // Load provinces if not loaded
  if (provinces.value.length === 0) {
    await loadProvinces()
  }

  // Try to find and set province
  if (locationData.province && provinces.value.length > 0) {
    const province = provinces.value.find(p =>
      p.ProvinceName.toLowerCase().includes(locationData.province.toLowerCase()) ||
      locationData.province.toLowerCase().includes(p.ProvinceName.toLowerCase())
    )

    if (province) {
      selectedProvinceId.value = province.ProvinceID
      updateForm.value.tinhThanh = province.ProvinceName
      updatedFields.push(`Tỉnh/Thành phố: ${province.ProvinceName}`)

      // Load districts for the selected province
      await onProvinceChange()

      // Try to find and set district with multiple matching strategies
      if (locationData.district && districts.value.length > 0) {
        console.log('Looking for district:', locationData.district) // Debug log
        console.log('Available districts:', districts.value.map(d => d.DistrictName)) // Debug log

        // Try multiple matching strategies
        let district = null

        // Strategy 1: Exact match
        district = districts.value.find(d =>
          d.DistrictName.toLowerCase() === locationData.district.toLowerCase()
        )

        // Strategy 2: Contains match
        if (!district) {
          district = districts.value.find(d =>
            d.DistrictName.toLowerCase().includes(locationData.district.toLowerCase()) ||
            locationData.district.toLowerCase().includes(d.DistrictName.toLowerCase())
          )
        }

        // Strategy 3: Remove common prefixes and try again
        if (!district) {
          const cleanDistrict = locationData.district
            .replace(/^quận\s*/i, '')
            .replace(/^huyện\s*/i, '')
            .replace(/^thành phố\s*/i, '')
            .replace(/^thị xã\s*/i, '')
            .replace(/^phường\s*/i, '')
            .replace(/^xã\s*/i, '')
            .trim()

          district = districts.value.find(d =>
            d.DistrictName.toLowerCase().includes(cleanDistrict.toLowerCase()) ||
            cleanDistrict.toLowerCase().includes(d.DistrictName.toLowerCase())
          )
        }

        // Strategy 4: Partial matching for complex names
        if (!district) {
          const searchTerm = locationData.district.toLowerCase()
          district = districts.value.find(d => {
            const districtName = d.DistrictName.toLowerCase()

            // Check if any significant part of the search term matches
            const searchWords = searchTerm.split(/\s+/).filter(word => word.length > 2)
            const districtWords = districtName.split(/\s+/).filter(word => word.length > 2)

            // Check if any search word is contained in district name
            return searchWords.some(searchWord =>
              districtWords.some(districtWord =>
                districtWord.includes(searchWord) || searchWord.includes(districtWord)
              )
            )
          })
        }

        // Strategy 5: Special cases for common mismatches
        if (!district) {
          const searchTerm = locationData.district.toLowerCase()

          // Special case: "Từ Liêm" should match "Nam Từ Liêm" or "Bắc Từ Liêm"
          if (searchTerm.includes('từ liêm')) {
            // Prefer "Nam Từ Liêm" over "Bắc Từ Liêm" for "Phường Từ Liêm"
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('nam từ liêm')
            ) || districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('từ liêm')
            )
          }

          // Special case: "Cầu Giấy" variations
          if (searchTerm.includes('cầu giấy') || searchTerm.includes('cau giay')) {
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('cầu giấy')
            )
          }

          // Special case: "Hoàn Kiếm" variations
          if (searchTerm.includes('hoàn kiếm') || searchTerm.includes('hoan kiem')) {
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('hoàn kiếm')
            )
          }
        }

        if (district) {
          selectedDistrictId.value = district.DistrictID
          updateForm.value.quanHuyen = district.DistrictName
          updatedFields.push(`Quận/Huyện: ${district.DistrictName}`)
          console.log('Found district:', district.DistrictName) // Debug log
          await onDistrictChange()
        } else {
          console.log('No district found for:', locationData.district) // Debug log
        }
      }
    }
  }

  // Show success message if fields were updated
  if (updatedFields.length > 0) {
    console.log('✅ Auto-updated fields:', updatedFields.join(', '))
  } else if (locationData.province || locationData.district) {
    // Show debug info if parsing failed
    const debugInfo = []
    if (locationData.province) debugInfo.push(`Tỉnh: ${locationData.province}`)
    if (locationData.district) debugInfo.push(`Quận: ${locationData.district}`)
    console.warn('⚠️ Could not auto-update:', debugInfo.join(', '))
  }
}

// Calculate shipping rates based on address
const calculateShippingRates = async () => {
  if (updateForm.value.diaChi === 'Tại cửa hàng' || updateForm.value.diaChi === 'Lấy tại cửa hàng') {
    newShippingFee.value = 0
    return
  }

  // Kiểm tra nếu tổng tiền hàng trên 30 triệu thì miễn phí ship
  const newTotal = calculateNewTotal()
  if (newTotal >= 30000000) {
    newShippingFee.value = 0
    return
  }

  if (!updateForm.value.diaChi || !selectedProvinceId.value || !selectedDistrictId.value) {
    return
  }

  isCalculatingShipping.value = true
  shippingError.value = ''

  try {
    // Calculate weight from products
    const weight = updateForm.value.products.reduce((total, item) => {
      return total + (item.soLuong || 1) * 500 // Assume 500g per item
    }, 0) || 1000

    const rates = await shippingService.calculateShippingFee(
      selectedProvinceId.value,
      selectedDistrictId.value,
      weight
    )

    // Use express fee as default
    newShippingFee.value = rates.express?.price || rates.ghn?.price || rates.standard?.price || orderDetails.value?.phiVanChuyen || 0

  } catch (error) {
    console.error('Error calculating shipping rates:', error)
    shippingError.value = 'Không thể tính phí vận chuyển. Vui lòng thử lại.'
    newShippingFee.value = orderDetails.value?.phiVanChuyen || 0
  } finally {
    isCalculatingShipping.value = false
  }
}

// Handle delivery type change
const onDeliveryTypeChange = () => {
  if (deliveryAddressType.value === 'pickup') {
    // Khi chọn "Lấy tại cửa hàng", chỉ set diaChi, xóa thông tin quận/huyện/thành phố
    updateForm.value.diaChi = 'Lấy tại cửa hàng'
    updateForm.value.tinhThanh = ''
    updateForm.value.quanHuyen = ''
    selectedAddressId.value = null
    selectedProvinceId.value = null
    selectedDistrictId.value = null
    newShippingFee.value = 0
  } else {
    // Reset to empty if switching to address
    if (updateForm.value.diaChi === 'Lấy tại cửa hàng' || updateForm.value.diaChi === 'Tại cửa hàng') {
      updateForm.value.diaChi = ''
    }
  }
}

// Watch for address changes to recalculate shipping
watch(
  () => [updateForm.value.diaChi, selectedProvinceId.value, selectedDistrictId.value, updateForm.value.products],
  () => {
    if (updateForm.value.diaChi === 'Tại cửa hàng' || updateForm.value.diaChi === 'Lấy tại cửa hàng') {
      newShippingFee.value = 0
      return
    }
    // Kiểm tra nếu tổng tiền hàng trên 30 triệu thì miễn phí ship
    const newTotal = calculateNewTotal()
    if (newTotal >= 30000000) {
      newShippingFee.value = 0
      return
    }
    if (updateForm.value.diaChi && selectedProvinceId.value && selectedDistrictId.value) {
      calculateShippingRates()
    }
  },
  { deep: true }
)

// Open update modal
watch(showUpdateModal, (show) => {
  if (show) {
    initUpdateForm()
  }
})

// Product modal functions
const openProductModal = async () => {
  showProductModal.value = true
  selectedProducts.value = []
  productSearchText.value = ''
  
  if (productList.value.length === 0) {
    await loadProducts()
  } else {
    filteredProductList.value = productList.value
  }
}

const loadProducts = async () => {
  isLoadingProducts.value = true
  try {
    const { data } = await api.get('/api/san-pham-pos')
    productList.value = (data || []).map(p => ({
      ...p,
      chiTietSanPhamId: p.chiTietSanPhamId || p.id,
      tenSanPham: p.tenSanPham || p.ten || '',
      gia: p.gia || p.giaBan || p.donGia || 0,
      maCtsp: p.maCtsp || p.ma || '',
      hinhAnh: p.hinhAnh || p.hinhAnhUrl || '',
      tenRam: p.tenRam || '',
      tenRom: p.tenRom || '',
      tenMauSac: p.tenMauSac || '',
      soLuongTon: p.soLuongTon || 0 // Đảm bảo có soLuongTon
    }))
    filteredProductList.value = productList.value

    // Cập nhật soLuongTon cho các sản phẩm trong form nếu chưa có
    updateForm.value.products.forEach((product, index) => {
      if (!product.soLuongTon) {
        const productFromList = productList.value.find(p =>
          p.chiTietSanPhamId === product.chiTietSanPhamId ||
          p.chiTietSanPhamId === product.id ||
          p.id === product.chiTietSanPhamId
        )
        if (productFromList) {
          updateForm.value.products[index].soLuongTon = productFromList.soLuongTon || 0
        }
      }
    })
  } catch (error) {
    console.error('Error loading products:', error)
    productList.value = []
    filteredProductList.value = []
  } finally {
    isLoadingProducts.value = false
  }
}

const searchProducts = () => {
  if (!productSearchText.value.trim()) {
    filteredProductList.value = productList.value
    return
  }

  const searchTerm = productSearchText.value.toLowerCase()
  filteredProductList.value = productList.value.filter(product => {
    return (
      (product.tenSanPham || '').toLowerCase().includes(searchTerm) ||
      (product.maCtsp || '').toLowerCase().includes(searchTerm) ||
      (product.tenRam || '').toLowerCase().includes(searchTerm) ||
      (product.tenRom || '').toLowerCase().includes(searchTerm) ||
      (product.tenMauSac || '').toLowerCase().includes(searchTerm)
    )
  })
}

const isProductSelected = (chiTietSanPhamId) => {
  return selectedProducts.value.some(p => p.chiTietSanPhamId === chiTietSanPhamId)
}

const toggleProduct = (product) => {
  const index = selectedProducts.value.findIndex(p => p.chiTietSanPhamId === product.chiTietSanPhamId)
  if (index >= 0) {
    selectedProducts.value.splice(index, 1)
  } else {
    selectedProducts.value.push({
      ...product,
      soLuong: 1,
      donGia: product.gia || 0
    })
  }
}

const confirmProductSelection = () => {
  // Add selected products to updateForm.products
  selectedProducts.value.forEach(product => {
    // Check if product already exists in updateForm.products
    const existingIndex = updateForm.value.products.findIndex(
      p => p.chiTietSanPhamId === product.chiTietSanPhamId || 
           p.id === product.chiTietSanPhamId ||
           p.chiTietHoaDonId === product.chiTietSanPhamId
    )
    
    if (existingIndex >= 0) {
      // Increase quantity if product already exists (với kiểm tra số lượng tồn kho)
      const existingProduct = updateForm.value.products[existingIndex]
      const currentQuantity = existingProduct.soLuong || 1
      const soLuongTon = existingProduct.soLuongTon || product.soLuongTon || 0

      if (soLuongTon > 0 && currentQuantity >= soLuongTon) {
        if (toastRef.value) {
          toastRef.value.showToast('warning', 'Cảnh báo', `Sản phẩm chỉ còn ${soLuongTon} sản phẩm trong kho`)
        }
        return
      }

      existingProduct.soLuong = currentQuantity + 1
    } else {
      // Add new product
      updateForm.value.products.push({
        chiTietSanPhamId: product.chiTietSanPhamId,
        tenSanPham: product.tenSanPham,
        soLuong: 1,
        donGia: product.gia || 0,
        gia: product.gia || 0,
        hinhAnh: product.hinhAnh || '/placeholder-product.jpg',
        tenRam: product.tenRam || '',
        tenRom: product.tenRom || '',
        tenMauSac: product.tenMauSac || '',
        soLuongTon: product.soLuongTon || 0 // Lưu số lượng tồn kho
      })
    }
  })
  
  showProductModal.value = false
  selectedProducts.value = []
  productSearchText.value = ''
}

// Helper function to get placeholder image
const getPlaceholderImage = () => {
  return 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
}

const getProductImageUrl = (imagePath) => {
  if (!imagePath) {
    return getPlaceholderImage()
  }
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

// Helper function to get product image
const getProductImage = (imagePath) => {
  return getProductImageUrl(imagePath)
}

// Helper functions for order list display
const getOrderItems = (order) => {
  return order.items || order.chiTietHoaDon || order.chiTietDonHang || order.originalOrder?.chiTietHoaDon || order.originalOrder?.chiTietDonHang || []
}

const getFirstProductImage = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) return getPlaceholderImage()

  const firstItem = items[0]
  const imagePath = firstItem.hinhAnh || firstItem.hinh_anh || firstItem.image || firstItem.urlAnh || firstItem.url_anh || null

  return imagePath ? getProductImageUrl(imagePath) : getPlaceholderImage()
}

const getFirstProductName = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) return 'Không có sản phẩm'

  return items[0].tenSanPham || items[0].ten_san_pham || 'Sản phẩm'
}

const getFirstProductVariants = (order) => {
  const items = getOrderItems(order)
  if (items.length === 0) return []

  const firstItem = items[0]
  const variants = []
  if (firstItem.tenRam) variants.push(firstItem.tenRam)
  if (firstItem.tenRom) variants.push(firstItem.tenRom)
  if (firstItem.tenMauSac) variants.push(firstItem.tenMauSac)

  return variants
}

const getOtherProductsCount = (order) => {
  const items = getOrderItems(order)
  return Math.max(0, items.length - 1)
}

const formatOrderDate = (order) => {
  const dateStr = order.date || order.ngayTao || order.originalOrder?.ngayTao
  if (!dateStr) return 'Không có'
  return formatDate(dateStr)
}

const getStatusClassForOrder = (status) => {
  const statusMap = {
    'pending': 'status-pending',
    'completed': 'status-completed',
    'cancelled': 'status-cancelled',
    'return': 'status-return',
    'shipping': 'status-shipping'
  }
  return statusMap[status] || 'status-pending'
}

const getStatusTextForOrder = (status) => {
  const statusMap = {
    'pending': 'Chờ xử lý',
    'completed': 'Hoàn thành',
    'cancelled': 'Đã hủy',
    'return': 'Trả hàng/Hoàn tiền',
    'shipping': 'Đang giao hàng'
  }
  return statusMap[status] || 'Chờ xử lý'
}

// Helper function để lấy tất cả IMEI từ sản phẩm
const getAllImeis = (product) => {
  if (!product) return []
  
  // Nếu có mảng imeis
  if (Array.isArray(product.imeis) && product.imeis.length > 0) {
    return product.imeis
  }
  
  // Nếu có imei đơn lẻ
  if (product.imei) {
    return [product.imei]
  }
  
  return []
}

// Kiểm tra tất cả sản phẩm đã có IMEI chưa (giống admin)
const checkAllProductsHaveImei = () => {
  if (!orderDetails.value?.chiTietDonHang) {
    console.log('🔍 No products found')
    return false
  }

  console.log('🔍 Checking IMEI for all products:', orderDetails.value.chiTietDonHang)

  for (let i = 0; i < orderDetails.value.chiTietDonHang.length; i++) {
    const product = orderDetails.value.chiTietDonHang[i]
    console.log(`🔍 Product ${i}:`, product)

    // Kiểm tra product.imeis (array)
    if (product.imeis && Array.isArray(product.imeis)) {
      if (product.imeis.length === 0) {
        console.log(`❌ Product ${i} has empty imeis array`)
        return false
      }
      // Kiểm tra từng IMEI trong array
      for (let j = 0; j < product.imeis.length; j++) {
        const imei = product.imeis[j]
        const imeiValue = typeof imei === 'object' ? (imei.imei || imei.maImei) : imei
        if (!imeiValue || (typeof imeiValue === 'string' && imeiValue.trim() === '')) {
          console.log(`❌ Product ${i}, IMEI ${j} is empty`)
          return false
        }
      }
    }
    // Kiểm tra product.imei (single IMEI)
    else if (product.imei) {
      const imeiValue = typeof product.imei === 'object' ? (product.imei.imei || product.imei.maImei) : product.imei
      if (!imeiValue || (typeof imeiValue === 'string' && imeiValue.trim() === '')) {
        console.log(`❌ Product ${i} has empty single imei`)
        return false
      }
    }
    // Không có IMEI nào
    else {
      console.log(`❌ Product ${i} has no IMEI at all`)
      return false
    }
  }

  console.log('✅ All products have IMEI')
  return true
}

// Product quantity management
const increaseQuantity = (index) => {
  if (updateForm.value.products[index]) {
    const product = updateForm.value.products[index]
    const currentQuantity = product.soLuong || 1
    const soLuongTon = product.soLuongTon || product.soLuong || 0

    // Kiểm tra số lượng tồn kho
    if (soLuongTon > 0 && currentQuantity >= soLuongTon) {
      // Hiển thị thông báo bằng toast
      if (toastRef.value) {
        toastRef.value.showToast('warning', 'Cảnh báo', `Sản phẩm chỉ còn ${soLuongTon} sản phẩm trong kho`)
      }
      return
    }

    // Tăng số lượng nếu còn hàng
    product.soLuong = currentQuantity + 1
  }
}

const decreaseQuantity = (index) => {
  if (updateForm.value.products[index] && updateForm.value.products[index].soLuong > 1) {
    updateForm.value.products[index].soLuong = updateForm.value.products[index].soLuong - 1
  }
}

const removeProduct = (index) => {
  if (updateForm.value.products.length > 1) {
    updateForm.value.products.splice(index, 1)
  }
}

// Calculate new total
const calculateNewTotal = () => {
  return updateForm.value.products.reduce((total, item) => {
    return total + (item.donGia || item.gia || 0) * (item.soLuong || 1)
  }, 0)
}

// Calculate new total with shipping
const calculateNewTotalWithShipping = () => {
  const newTotal = calculateNewTotal()
  const newShipping = newShippingFee.value !== null ? newShippingFee.value : (orderDetails.value?.phiVanChuyen || 0)
  return newTotal + newShipping
}

// Calculate price difference
const calculatePriceDifference = () => {
  const oldTotal = orderDetails.value?.tongTienHang || orderDetails.value?.tongTien || 0
  const oldShipping = orderDetails.value?.phiVanChuyen || 0
  const oldTotalWithShipping = oldTotal + oldShipping
  
  const newTotal = calculateNewTotal()
  const newShipping = newShippingFee.value !== null ? newShippingFee.value : oldShipping
  const newTotalWithShipping = newTotal + newShipping
  
  return newTotalWithShipping - oldTotalWithShipping
}

// Check if there are changes
const hasChanges = () => {
  const customerInfoChanged = 
    updateForm.value.tenKhachHang !== (orderDetails.value?.tenKhachHang || '') ||
    updateForm.value.soDienThoai !== (orderDetails.value?.soDienThoai || '') ||
    updateForm.value.email !== (orderDetails.value?.email || '') ||
    updateForm.value.ghiChu !== (orderDetails.value?.ghiChu || '')

  const addressChanged = 
    updateForm.value.diaChi !== (orderDetails.value?.diaChi || '') ||
    updateForm.value.tinhThanh !== (orderDetails.value?.tinhThanh || '') ||
    updateForm.value.quanHuyen !== (orderDetails.value?.quanHuyen || '')

  const productsChanged = 
    updateForm.value.products.length !== (orderDetails.value?.chiTietDonHang?.length || 0) ||
    updateForm.value.products.some((item, index) => {
      const originalItem = orderDetails.value?.chiTietDonHang?.[index]
      if (!originalItem) return true
      return item.soLuong !== (originalItem.soLuong || 1) ||
             item.id !== originalItem.id ||
             item.chiTietHoaDonId !== originalItem.chiTietHoaDonId
    })

  return customerInfoChanged || addressChanged || productsChanged
}

// Check if price related fields changed
const hasPriceRelatedChanges = () => {
  const addressChanged = 
    updateForm.value.diaChi !== (orderDetails.value?.diaChi || '') ||
    updateForm.value.tinhThanh !== (orderDetails.value?.tinhThanh || '') ||
    updateForm.value.quanHuyen !== (orderDetails.value?.quanHuyen || '')

  const productsChanged = 
    updateForm.value.products.length !== (orderDetails.value?.chiTietDonHang?.length || 0) ||
    updateForm.value.products.some((item, index) => {
      const originalItem = orderDetails.value?.chiTietDonHang?.[index]
      if (!originalItem) return true
      return item.soLuong !== (originalItem.soLuong || 1) ||
             item.id !== originalItem.id ||
             item.chiTietHoaDonId !== originalItem.chiTietHoaDonId
    })

  return addressChanged || productsChanged
}

// Close update modal
const closeUpdateModal = () => {
  showUpdateModal.value = false
  showPriceConfirmModal.value = false
  updateForm.value = {
    tenKhachHang: '',
    soDienThoai: '',
    email: '',
    diaChi: '',
    tinhThanh: '',
    quanHuyen: '',
    ghiChu: '',
    products: []
  }
  newShippingFee.value = null
  updateError.value = ''
  pendingUpdateRequest.value = null
}

// Prepare update request
const prepareUpdateRequest = () => {
  const newTongTienHang = calculateNewTotal()
  const newPhiVanChuyen = newShippingFee.value !== null ? newShippingFee.value : (orderDetails.value.phiVanChuyen || 0)

  // Get province and district names from selected IDs
  // Nếu là "Lấy tại cửa hàng", không gửi quận/huyện/thành phố
  let tinhThanh = ''
  let quanHuyen = ''
  
  if (updateForm.value.diaChi !== 'Lấy tại cửa hàng' && updateForm.value.diaChi !== 'Tại cửa hàng') {
    tinhThanh = updateForm.value.tinhThanh
    quanHuyen = updateForm.value.quanHuyen
    
    if (selectedProvinceId.value) {
      const province = provinces.value.find(p => p.ProvinceID === selectedProvinceId.value)
      if (province) {
        tinhThanh = province.ProvinceName
      }
    }
    
    if (selectedDistrictId.value) {
      const district = districts.value.find(d => d.DistrictID === selectedDistrictId.value)
      if (district) {
        quanHuyen = district.DistrictName
      }
    }
  }

  // Map products to chiTietDonHang format
  const chiTietDonHang = updateForm.value.products.map(item => {
    // Get id (chiTietHoaDonId) - must be a valid integer or null
    // chiTietHoaDonId là ID của chi tiết hóa đơn (HoaDonCt.id), dùng để update item đã tồn tại
    let itemId = null
    if (item.chiTietHoaDonId != null) {
      itemId = typeof item.chiTietHoaDonId === 'number' ? item.chiTietHoaDonId : parseInt(item.chiTietHoaDonId)
      if (isNaN(itemId)) itemId = null
    }
    
    // Get chiTietSanPhamId - must be a valid integer
    // chiTietSanPhamId là ID của chi tiết sản phẩm (ChiTietSanPham.id)
    let chiTietSanPhamId = null
    if (item.chiTietSanPhamId != null) {
      chiTietSanPhamId = typeof item.chiTietSanPhamId === 'number' ? item.chiTietSanPhamId : parseInt(item.chiTietSanPhamId)
    } else if (item.id != null) {
      // id trong SanPhamTrackingDTO là chiTietSanPhamId
      chiTietSanPhamId = typeof item.id === 'number' ? item.id : parseInt(item.id)
    }
    
    // Validate required fields
    if (!chiTietSanPhamId || isNaN(chiTietSanPhamId)) {
      console.error('Invalid chiTietSanPhamId for item:', item)
      console.error('Item details:', {
        id: item.id,
        chiTietSanPhamId: item.chiTietSanPhamId,
        chiTietHoaDonId: item.chiTietHoaDonId,
        tenSanPham: item.tenSanPham
      })
      throw new Error(`Sản phẩm "${item.tenSanPham || 'Không xác định'}" không có mã sản phẩm hợp lệ`)
    }
    
    const soLuong = item.soLuong || 1
    const donGia = item.donGia || item.gia || 0
    
    if (donGia <= 0) {
      console.error('Invalid donGia for item:', item)
      throw new Error(`Sản phẩm "${item.tenSanPham || 'Không xác định'}" không có giá hợp lệ`)
    }
    
    return {
      id: itemId, // chiTietHoaDonId - Can be null for new items
      chiTietSanPhamId: chiTietSanPhamId,
      soLuong: soLuong,
      donGia: donGia
    }
  })

  const request = {
    tenKhachHang: updateForm.value.tenKhachHang,
    soDienThoai: updateForm.value.soDienThoai,
    email: updateForm.value.email,
    diaChi: updateForm.value.diaChi,
    tinhThanh: tinhThanh,
    quanHuyen: quanHuyen,
    ghiChu: updateForm.value.ghiChu,
    phiVanChuyen: newPhiVanChuyen,
    tongTienHang: newTongTienHang,
    chiTietDonHang: chiTietDonHang
  }
  
  console.log('Prepared update request:', request)
  return request
}

// Handle update with payment (VNPay)
const handleUpdateWithPayment = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  isProcessing.value = true
  updateError.value = ''

  try {
    const priceDifference = calculatePriceDifference()
    
    if (priceDifference <= 0) {
      // Nếu không cần thanh toán thêm, gọi handleUpdateOrder trực tiếp
      await handleUpdateOrder()
      return
    }

    // Lưu request update để dùng sau khi thanh toán
    pendingUpdateRequest.value = prepareUpdateRequest()

    // Tạo VNPay payment URL với số tiền cần thêm
    const paymentRequest = {
      amount: Math.round(priceDifference), // Convert to long (VNPay expects amount in cents, but we use VND)
      orderInfo: `Thanh toán cập nhật đơn hàng ${orderDetails.value.maHoaDon} - Số tiền: ${priceDifference} VND`,
      bankCode: null
    }

    // Gọi API tạo VNPay payment với update request
    const { data: paymentResponse } = await api.post('/api/payments/vnpay/create-with-order-update', {
      paymentRequest,
      orderUpdateRequest: pendingUpdateRequest.value,
      maHoaDon: orderDetails.value.maHoaDon
    })

    if (paymentResponse && paymentResponse.paymentUrl) {
      // Lưu update request vào sessionStorage để dùng sau khi thanh toán
      sessionStorage.setItem('pendingOrderUpdate', JSON.stringify({
        maHoaDon: orderDetails.value.maHoaDon,
        updateRequest: pendingUpdateRequest.value
      }))

      // Redirect to VNPay
      window.location.href = paymentResponse.paymentUrl
    } else {
      throw new Error('Không thể tạo URL thanh toán VNPay')
    }
  } catch (error) {
    console.error('Error creating payment:', error)
    updateError.value = error.response?.data?.message || 'Lỗi khi tạo thanh toán. Vui lòng thử lại.'
    isProcessing.value = false
  }
}

// Handle update order with COD (thanh toán khi nhận hàng)
const handleUpdateOrderWithCOD = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  isProcessing.value = true
  updateError.value = ''

  try {
    // Prepare update request
    const updateRequest = prepareUpdateRequest()

    // Calculate price difference
    const priceDifference = calculatePriceDifference()

    // Call API to update order
    const { data } = await api.put(`/api/hoa-don/update-order/${orderDetails.value.maHoaDon}`, updateRequest)

    if (data && data.success) {
      // If price increased, create additional fee payment record with trangThai = 0 (chưa thanh toán)
      if (priceDifference > 0) {
        try {
          await api.post(`/api/hoa-don/${orderDetails.value.maHoaDon}/additional-fee`, {
            soTienPhuPhi: priceDifference,
            phuongThucThanhToan: 'COD', // Thanh toán khi nhận hàng
            moTa: `Cập nhật đơn hàng: ${hasPriceRelatedChanges() ? 'Thay đổi sản phẩm/địa chỉ' : 'Cập nhật thông tin'}`,
            trangThai: 0 // Chưa thanh toán (sẽ thanh toán khi nhận hàng)
          })
        } catch (feeError) {
          console.error('Error creating additional fee:', feeError)
          // Don't fail the update if fee creation fails
        }
      } else if (priceDifference < 0) {
        // Nếu giá giảm, tạo refund
        try {
          await api.post(`/api/hoa-don/${orderDetails.value.maHoaDon}/refund`, {
            soTienHoan: Math.abs(priceDifference),
            phuongThucThanhToan: orderDetails.value.phuongThucThanhToan || 'Tiền mặt',
            lyDo: `Hoàn tiền do cập nhật đơn hàng - Giảm giá: ${Math.abs(priceDifference)} VND`
          })
        } catch (refundError) {
          console.error('Error creating refund:', refundError)
          // Don't fail the update if refund creation fails
        }
      }

      // Mark as updated once
      hasUpdatedOnce.value = true

      // Close modals
      showUpdateModal.value = false
      showPriceConfirmModal.value = false
      
      if (toastRef.value) {
        toastRef.value.showToast('success', 'Thành công', 'Cập nhật đơn hàng thành công! Số tiền chênh lệch sẽ được thanh toán khi nhận hàng.')
      }

      // Reload order details to get updated data
      await searchOrder()
    } else {
      throw new Error(data?.message || 'Cập nhật đơn hàng thất bại')
    }
  } catch (error) {
    console.error('Error updating order:', error)
    console.error('Error details:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      request: error.config
    })
    updateError.value = error.response?.data?.message || error.message || 'Lỗi khi cập nhật đơn hàng. Vui lòng thử lại.'
    if (toastRef.value) {
      toastRef.value.showToast('error', 'Lỗi', updateError.value)
    }
  } finally {
    isProcessing.value = false
  }
}

// Handle update order (without payment or after payment)
const handleUpdateOrder = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  isProcessing.value = true
  updateError.value = ''

  try {
    // Prepare update request
    const updateRequest = prepareUpdateRequest()

    // Calculate price difference
    const priceDifference = calculatePriceDifference()

    // Call API to update order
    const { data } = await api.put(`/api/hoa-don/update-order/${orderDetails.value.maHoaDon}`, updateRequest)

    if (data && data.success) {
      // If price increased, create additional fee payment record
      if (priceDifference > 0) {
        try {
          await api.post(`/api/hoa-don/${orderDetails.value.maHoaDon}/additional-fee`, {
            soTienPhuPhi: priceDifference,
            phuongThucThanhToan: 'VNPay', // Hoặc có thể là 'Chuyển khoản' nếu đã thanh toán VNPay
            moTa: `Cập nhật đơn hàng: ${hasPriceRelatedChanges() ? 'Thay đổi sản phẩm/địa chỉ' : 'Cập nhật thông tin'}`,
            trangThai: 1 // Đã thanh toán (nếu không có payment thì sẽ được set sau khi thanh toán VNPay)
          })
        } catch (feeError) {
          console.error('Error creating additional fee:', feeError)
          // Don't fail the update if fee creation fails
        }
      } else if (priceDifference < 0) {
        // Nếu giá giảm, tạo refund
        try {
          await api.post(`/api/hoa-don/${orderDetails.value.maHoaDon}/refund`, {
            soTienHoan: Math.abs(priceDifference),
            phuongThucThanhToan: orderDetails.value.phuongThucThanhToan || 'Tiền mặt',
            lyDo: `Hoàn tiền do cập nhật đơn hàng - Giảm giá: ${Math.abs(priceDifference)} VND`
          })
        } catch (refundError) {
          console.error('Error creating refund:', refundError)
          // Don't fail the update if refund creation fails
        }
      }

      // Mark as updated once
      hasUpdatedOnce.value = true

      // Close modals
      showUpdateModal.value = false
      showPriceConfirmModal.value = false
      
      if (toastRef.value) {
        toastRef.value.showToast('success', 'Thành công', 'Cập nhật đơn hàng thành công!')
      }

      // Reload order details to get updated data
      await searchOrder()
    } else {
      throw new Error(data?.message || 'Cập nhật đơn hàng thất bại')
    }
  } catch (error) {
    console.error('Error updating order:', error)
    console.error('Error details:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      request: error.config
    })
    updateError.value = error.response?.data?.message || error.message || 'Lỗi khi cập nhật đơn hàng. Vui lòng thử lại.'
    if (toastRef.value) {
      toastRef.value.showToast('error', 'Lỗi', updateError.value)
    }
  } finally {
    isProcessing.value = false
  }
}

// Payment type helper functions
const getPaymentTypeLabel = (loaiThanhToan) => {
  if (!loaiThanhToan) return 'Thanh toán'
  const typeMap = {
    'PAYMENT': 'Thanh toán',
    'REFUND': 'Hoàn phí',
    'ADDITIONAL_FEE': 'Phụ phí'
  }
  return typeMap[loaiThanhToan] || 'Thanh toán'
}

const getPaymentTypeBadgeClass = (loaiThanhToan) => {
  if (!loaiThanhToan) return 'badge-payment'
  const classMap = {
    'PAYMENT': 'badge-payment',
    'REFUND': 'badge-refund',
    'ADDITIONAL_FEE': 'badge-additional-fee'
  }
  return classMap[loaiThanhToan] || 'badge-payment'
}

const getPaymentAmountClass = (loaiThanhToan) => {
  if (!loaiThanhToan) return 'amount-payment'
  const classMap = {
    'PAYMENT': 'amount-payment',
    'REFUND': 'amount-refund',
    'ADDITIONAL_FEE': 'amount-additional-fee'
  }
  return classMap[loaiThanhToan] || 'amount-payment'
}

// Refund status helper functions
const getRefundStatusLabel = (refundStatus) => {
  if (refundStatus === null || refundStatus === undefined) return 'Không xác định'
  const statusMap = {
    0: 'Chờ chuyển tiền',
    1: 'Đã chuyển tiền - Chờ xác nhận',
    2: 'Hoàn thành hoàn phí'
  }
  return statusMap[refundStatus] || 'Không xác định'
}

const getRefundStatusBadgeClass = (refundStatus) => {
  if (refundStatus === null || refundStatus === undefined) return 'badge-refund-status-unknown'
  const classMap = {
    0: 'badge-refund-status-pending',
    1: 'badge-refund-status-transferred',
    2: 'badge-refund-status-completed'
  }
  return classMap[refundStatus] || 'badge-refund-status-unknown'
}

// Handle confirm refund received
const handleConfirmRefundReceived = async (refundId) => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  if (!confirm('Bạn đã nhận được số tiền hoàn phí chưa? Bấm OK để xác nhận.')) {
    return
  }

  isConfirmingRefund.value = true
  try {
    const { data } = await api.put(`/api/hoa-don/${orderDetails.value.maHoaDon}/refund/${refundId}/confirm`)
    
    if (data && data.success) {
      if (toastRef.value) {
        toastRef.value.showToast('success', 'Thành công', 'Xác nhận nhận hoàn phí thành công!')
      }
      // Reload order details to get updated refund status
      await searchOrder()
    } else {
      throw new Error(data?.message || 'Xác nhận thất bại')
    }
  } catch (error) {
    console.error('Error confirming refund:', error)
    if (toastRef.value) {
      toastRef.value.showToast('error', 'Lỗi', error.response?.data?.message || 'Có lỗi xảy ra khi xác nhận hoàn phí. Vui lòng thử lại.')
    }
  } finally {
    isConfirmingRefund.value = false
  }
}

// Handle confirm received order (customer confirms they received the order)
const handleConfirmReceived = async () => {
  if (!orderDetails.value || !orderDetails.value.maHoaDon) return

  isProcessing.value = true
  updateError.value = ''

  try {
    // Call API to update order status to 3 - HOAN_THANH (Hoàn thành)
    const { data } = await api.put(`/api/hoa-don/update-status/${orderDetails.value.maHoaDon}`, {
      trangThai: 3, // HOAN_THANH
      nguoiThucHien: userInfo.value?.tenKhachHang || 'Khách hàng'
    })

    if (data) {
      // Update order details
      orderDetails.value.trangThai = 3
      orderDetails.value.lichSuTrangThai = data.lichSuTrangThai || orderDetails.value.lichSuTrangThai

      // Set flag để hiển thị nút review
      hasConfirmedReceived.value = true
      showConfirmReceivedModal.value = false

      if (toastRef.value) {
        toastRef.value.showToast('success', 'Thành công', 'Xác nhận đã nhận hàng thành công! Bạn có thể đánh giá và bình luận về sản phẩm.')
      }
      
      // Reload order details to get updated tracking history
      await searchOrder()
    }
  } catch (error) {
    console.error('Error confirming received order:', error)
    updateError.value = error.response?.data?.message || 'Lỗi khi xác nhận đã nhận hàng. Vui lòng thử lại.'
    if (toastRef.value) {
      toastRef.value.showToast('error', 'Lỗi', updateError.value)
    }
  } finally {
    isProcessing.value = false
  }
}

// Open review modal
const openReviewModal = () => {
  if (!orderDetails.value || !orderDetails.value.chiTietDonHang) return
  
  // Initialize review forms for each product
  reviewForms.value = orderDetails.value.chiTietDonHang.map(item => {
    // Lấy sanPhamId từ item (đã có trong API response sau khi backend update)
    const sanPhamId = item.sanPhamId || item.idSanPham
    // Lấy chiTietSanPhamId từ item.id (vì id trong SanPhamTrackingDTO là chiTietSanPhamId)
    const chiTietSanPhamId = item.id || item.chiTietSanPhamId
    
    console.log('Initializing review form for product:', {
      tenSanPham: item.tenSanPham,
      sanPhamId: sanPhamId,
      chiTietSanPhamId: chiTietSanPhamId,
      item: item
    })
    
    return {
      sanPhamId: sanPhamId,
      chiTietSanPhamId: chiTietSanPhamId,
      rating: null,
      comment: '',
      tempRating: null
    }
  })
  
  reviewError.value = ''
  showReviewModal.value = true
}

// Close review modal
const closeReviewModal = () => {
  showReviewModal.value = false
  reviewForms.value = []
  reviewError.value = ''
}

// Check if can submit reviews (at least one rating is selected)
const canSubmitReviews = computed(() => {
  return reviewForms.value.some(form => form.rating !== null && form.rating > 0)
})

// Submit reviews
const submitReviews = async () => {
  if (!canSubmitReviews.value) {
    reviewError.value = 'Vui lòng chọn ít nhất một đánh giá (sao) cho sản phẩm'
    return
  }

  if (!isLoggedIn.value || !userInfo.value) {
    reviewError.value = 'Vui lòng đăng nhập để đánh giá sản phẩm'
    return
  }

  isSubmittingReview.value = true
  reviewError.value = ''

  try {
    const khachHangId = userInfo.value.id || userInfo.value.khachHangId || userInfo.value.userId
    const tenNguoiDung = userInfo.value.hoTen || userInfo.value.tenKhachHang || userInfo.value.taiKhoan || 'Người dùng'

    // Submit reviews for each product
    const reviewsToSubmit = reviewForms.value.filter(form => form.rating !== null && form.rating > 0)
    
    if (reviewsToSubmit.length === 0) {
      reviewError.value = 'Vui lòng chọn ít nhất một đánh giá (sao) cho sản phẩm'
      isSubmittingReview.value = false
      return
    }

    // Validate và chuẩn bị dữ liệu
    const reviewPromises = reviewsToSubmit.map(form => {
      // Đảm bảo idSanPham là số nguyên hợp lệ
      const idSanPham = parseInt(form.sanPhamId)
      if (!idSanPham || isNaN(idSanPham)) {
        throw new Error(`ID sản phẩm không hợp lệ: ${form.sanPhamId}`)
      }

      // Đảm bảo rating là số nguyên từ 1-5
      const rating = parseInt(form.rating)
      if (!rating || rating < 1 || rating > 5) {
        throw new Error(`Đánh giá không hợp lệ: ${form.rating}`)
      }

      // Đảm bảo idChiTietSanPham là số nguyên hoặc null
      let idChiTietSanPham = null
      if (form.chiTietSanPhamId) {
        idChiTietSanPham = parseInt(form.chiTietSanPhamId)
        if (isNaN(idChiTietSanPham)) {
          idChiTietSanPham = null
        }
      }

      const reviewData = {
        idSanPham: idSanPham,
        idChiTietSanPham: idChiTietSanPham,
        idNguoiDung: khachHangId ? parseInt(khachHangId) : null,
        tenNguoiDung: tenNguoiDung || null,
        rating: rating,
        comment: (form.comment && form.comment.trim()) || null
      }

      console.log('Submitting review data:', reviewData)
      return api.post('/api/reviews', reviewData)
    })

    await Promise.all(reviewPromises)

    // Hiển thị toast thay vì alert
    if (toastRef.value) {
      toastRef.value.success('Thành công', 'Đánh giá và bình luận đã được gửi thành công! Cảm ơn bạn đã đánh giá.')
    }
    
    // Đánh dấu đã gửi review
    hasSubmittedReview.value = true
    closeReviewModal()
    
    // Reload order details to refresh data
    await searchOrder()
  } catch (error) {
    console.error('Error submitting reviews:', error)
    console.error('Error response:', error.response?.data)
    const errorMessage = error.response?.data?.error || error.response?.data?.message || error.message || 'Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.'
    reviewError.value = errorMessage
    
    // Hiển thị toast lỗi
    if (toastRef.value) {
      toastRef.value.error('Lỗi', errorMessage)
    }
  } finally {
    isSubmittingReview.value = false
  }
}

// Navigate back to orders page
const goBackToOrders = () => {
  router.push('/tai-khoan')
}

onMounted(async () => {
  // Check user login status
  checkUserLogin()

  if (isLoggedIn.value) {
    // If orderId is in query, load order detail
    if (route.query.orderId) {
      await loadOrderDetail(route.query.orderId)
    } else {
      // Otherwise, load orders list
      await loadOrders()
    }
  } else {
    // Check if order ID is provided in URL params for non-logged in users
    // Load order detail directly (from email link)
    if (route.query.orderId) {
      await loadOrderDetail(route.query.orderId)
    }
  }
})

// Watch for route changes to reload orders when navigating
watch(() => route.query.orderId, async (newOrderId) => {
  if (isLoggedIn.value) {
    if (newOrderId) {
      await loadOrderDetail(newOrderId)
    } else {
      await loadOrders()
    }
  }
})

onBeforeUnmount(() => {
  // Clean up auto-refresh interval
  stopAutoRefresh()
})
</script>

<style scoped>
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
}

.tracking-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
  display: flex;
  flex-direction: column;
  padding-top: 77px; /* Compensate for fixed header */
}

.tracking-main {
  flex: 1;
  padding: 3rem 0;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
  width: 100%;
}

.page-header {
  margin-bottom: 2rem;
  text-align: center;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center;
}

/* Date Range Filter */
.date-range-filter {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.95rem;
  color: #666;
}

.btn-change-date {
  padding: 0.5rem 1rem;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #333;
  transition: all 0.3s ease;
}

.btn-change-date:hover {
  background: #f8f9fa;
  border-color: #FF5500;
  color: #FF5500;
}

/* Date Picker Modal */
.date-picker-modal {
  max-width: 500px;
}

.date-picker-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1rem 0;
}

.date-picker-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.date-picker-form label {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.date-picker-form .form-input {
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.date-picker-form .form-input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 85, 0, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-top: 2px solid #e9ecef;
}

.modal-footer .btn-cancel {
  padding: 0.75rem 1.5rem;
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  color: #666;
  transition: all 0.3s ease;
}

.modal-footer .btn-cancel:hover {
  background: #e9ecef;
  border-color: #ccc;
}

.modal-footer .btn-confirm {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  color: white;
  transition: all 0.3s ease;
}

.modal-footer .btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  color: #495057;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: absolute;
  left: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

.btn-back:hover {
  background: #f8f9fa;
  border-color: #ff5500;
  color: #ff5500;
  transform: translateX(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.btn-back i {
  font-size: 1rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #666;
}

.search-section {
  margin-bottom: 3rem;
}

.search-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

.search-card h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.search-card h2::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.form-label {
  font-weight: 600;
  color: #4a4a4a;
  font-size: 0.95rem;
  white-space: nowrap;
  min-width: 100px;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 0.75rem 1rem;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
  background-color: #e8f4f8;
  color: #333;
}

.search-input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.search-input.error {
  border-color: #DC143C;
}

.error-message {
  color: #DC143C;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  width: 100%;
  order: 3;
}

.btn-search {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  white-space: nowrap;
  height: fit-content;
  font-size: 1rem;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-search:hover:not(:disabled) {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-search:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.order-details {
  margin-bottom: 2rem;
}

/* Order Status Progress Bar */
.order-status-progress {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.progress-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  gap: 1rem;
}

.progress-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9ecef;
  color: #999;
  font-size: 1.5rem;
  margin-bottom: 0.75rem;
  transition: all 0.3s;
}

.progress-step.completed .step-icon,
.progress-step.active .step-icon {
  background: #28a745;
  color: white;
}

.progress-step.active:not(.completed) .step-icon {
  background: #FF5500;
  color: white;
}

.progress-step.cancelled .step-icon {
  background: #dc3545;
  color: white;
}

.step-content {
  text-align: center;
}

.step-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.step-time {
  font-size: 0.75rem;
  color: #666;
}

.progress-step.completed .step-time,
.progress-step.active .step-time {
  color: #28a745;
  font-weight: 500;
}

.step-connector {
  position: absolute;
  top: 25px;
  left: calc(50% + 25px);
  right: calc(-50% + 25px);
  height: 2px;
  background: #e9ecef;
  z-index: 0;
}

.progress-step.completed ~ .progress-step .step-connector {
  background: #28a745;
}

/* Order Details Content - Two Column Layout */
.order-details-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.order-details-left,
.order-details-right {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Detail Card */
.detail-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 500;
  color: #666;
  min-width: 150px;
}

.detail-value {
  font-weight: 600;
  color: #333;
  text-align: right;
  flex: 1;
}

.detail-value.highlight {
  background: #FFD700;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-family: monospace;
  display: inline-block;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.badge-primary {
  background: #007bff;
  color: white;
}

.badge-success {
  background: #28a745;
  color: white;
}

.badge-warning {
  background: #ffc107;
  color: #333;
}

.badge-info {
  background: #17a2b8;
  color: white;
}

.badge-danger {
  background: #dc3545;
  color: white;
}

.badge-secondary {
  background: #6c757d;
  color: white;
}

/* History List */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #007bff;
}

.history-content {
  flex: 1;
}

.history-action {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
}

.history-time {
  font-size: 0.875rem;
  color: #666;
}

.history-status {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.history-status.status-processed {
  background: #d4edda;
  color: #155724;
}

.history-status.status-pending {
  background: #fff3cd;
  color: #856404;
}

.history-empty {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-style: italic;
}

/* Summary List */
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-item.total-item {
  background: #fff;
  border: 2px solid #FF5500;
  border-radius: 8px;
}

.summary-label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.summary-note {
  font-size: 0.875rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.summary-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
}

.summary-value.discount {
  color: #dc3545;
}

.summary-item.total-item .summary-value {
  color: #FF5500;
  font-size: 1.5rem;
}

/* Product List Card */
.product-list-card {
  margin-top: 2rem;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-item {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.product-specs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.spec-badge {
  background: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  color: #666;
}

.product-price {
  font-size: 1.125rem;
  font-weight: 700;
  color: #FF5500;
}

.product-imei {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.imei-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #666;
}

.imei-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.imei-badge {
  background: #FF5500;
  color: white;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: monospace;
}

.order-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.order-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-back-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  color: #495057;
  font-size: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.btn-back-inline:hover {
  background: #f8f9fa;
  border-color: #ff5500;
  color: #ff5500;
  transform: translateX(-2px);
}

.order-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.order-header h2::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.order-id {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-family: monospace;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.status-section {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  font-weight: 600;
  font-size: 1rem;
}

.order-actions-inline {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.status-waiting {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-confirmed {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-shipping {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-delivered {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* Order Status Timeline - giống HoaDonDetailPage.vue */
.order-status-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  border: 1px solid #e9ecef;
  padding: 2.5rem;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.status-header::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.status-timeline {
  background: transparent;
  border-radius: 0;
  padding: 0;
  border: none;
}

.status-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 20px 0;
}

.status-steps::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
}

.status-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
}

.status-step.completed:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  right: -50%;
  height: 2px;
  background: #10b981;
  z-index: 1;
  transform: translateY(-50%);
}

.status-step .step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f97316;
  color: white;
  font-size: 18px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.status-step.active .step-icon {
  background: #f97316;
  color: white;
}

.status-step.completed .step-icon {
  background: #10b981;
  color: white;
}

.status-step.isCancelled .step-icon {
  background: #ef4444;
  color: white;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
}

.status-step .step-content {
  text-align: center;
}

.status-step .step-title {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
  font-size: 14px;
}

.status-step .step-time {
  font-size: 12px;
  color: #718096;
}

.order-info {
  margin-bottom: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.info-item {
  display: block;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: 600;
  color: #666;
  padding-right: 10px;
}

.info-value {
  font-weight: 600;
  color: #333;
  text-align: right;
}

.order-items {
  margin-bottom: 2rem;
}

.order-items h3 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e9ecef;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.order-items h3::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  display: flex;
  align-items: flex-start;
  gap: 1.25rem;
  padding: 1.75rem;
  border: 1px solid #e9ecef;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  position: relative;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.item-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #FF5500;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-right: 140px; /* Space for price on the right */
}

.item-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
}

.item-specs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.25rem;
}

.spec-badge {
  background: #f3f4f6;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #374151;
  font-weight: 500;
  border: 1px solid #e5e7eb;
}

.item-quantity {
  font-size: 0.9rem;
  color: #666;
}

.item-imei {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.imei-item {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: #fef9e7;
  border-radius: 8px;
  border-left: 4px solid #FF5500;
  box-shadow: 0 1px 3px rgba(255, 85, 0, 0.15);
  width: fit-content;
}

.imei-label {
  font-weight: 600;
  color: #d97706;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.imei-value {
  font-family: monospace;
  font-size: 0.9rem;
  color: #d97706;
  font-weight: 600;
}

.imei-tag {
  background: #FF5500;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 600;
  font-family: monospace;
}

.item-price {
  font-weight: 700;
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 1.1rem;
  position: absolute;
  right: 1rem;
  top: 1rem;
}

.no-products {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-style: italic;
}

.discount-text {
  color: #dc3545;
  font-weight: 600;
}

.order-summary {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2.5rem;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  color: #4b5563;
  font-size: 1rem;
}

.summary-row.total {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1f2937;
  border-top: 2px solid #e5e7eb;
  padding-top: 1rem;
  margin-top: 1rem;
  margin-bottom: 0;
}

.summary-row.total span:last-child {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 1.75rem;
}

.no-order {
  margin-bottom: 2rem;
}

.no-order-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 3rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  text-align: center;
}

.no-order-card i {
  font-size: 4rem;
  color: #ffc107;
  margin-bottom: 1rem;
}

.no-order-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.no-order-card p {
  color: #666;
  margin-bottom: 1.5rem;
}

.no-order-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
}

.no-order-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.no-order-info ul {
  margin: 0;
  padding-left: 1.5rem;
}

.no-order-info li {
  color: #666;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.no-order-info strong {
  color: #FF5500;
}

.no-order-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-retry,
.btn-support {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  border: none;
  min-width: 150px;
  justify-content: center;
}

.btn-retry {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-retry:hover {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-support {
  background: #17a2b8;
  color: white;
}

.btn-support:hover {
  background: #138496;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(23, 162, 184, 0.3);
}

.tracking-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
}

/* Responsive */
@media (min-width: 1400px) {
  .tracking-main {
    max-width: 1400px;
  }

  .container {
    max-width: 1400px;
  }
}

@media (max-width: 1399px) {
  .tracking-main {
    max-width: 100%;
    padding: 3rem 2rem;
  }

  .container {
    max-width: 100%;
    padding: 0 2rem;
  }
}

@media (max-width: 768px) {
  .tracking-page {
    padding-top: 67px; /* Adjust for smaller header on mobile */
  }

  .container {
    padding: 0 1rem;
    max-width: 100%;
  }

  .page-header h1 {
    font-size: 1.75rem;
  }

  .search-form {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .order-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .order-header-left {
    width: 100%;
    justify-content: flex-start;
  }

  .order-header h2 {
    font-size: 1.5rem;
  }

  .btn-back-inline {
    width: 32px;
    height: 32px;
    font-size: 1.1rem;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .item-price {
    align-self: flex-end;
  }

  /* Order Details Responsive */
  .order-details-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .progress-container {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .progress-step {
    flex: 0 0 calc(50% - 0.5rem);
    min-width: 120px;
  }

  .step-connector {
    display: none;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .detail-value {
    text-align: left;
  }

  .product-item {
    flex-direction: column;
    gap: 1rem;
  }

  .product-imei {
    align-items: flex-start;
    width: 100%;
  }

  .imei-list {
    align-items: flex-start;
  }
}

/* New Design Styles */
/* Status Tabs */
.status-tabs {
  display: flex;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  padding: 0.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  background: transparent;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  border-radius: 6px;
}

.tab-btn:hover {
  color: #FF5500;
  background: rgba(255, 85, 0, 0.05);
}

.tab-btn.active {
  color: #FF5500;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -0.5rem;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

/* Search Bar */
.search-bar {
  position: relative;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1rem;
}


/* Orders Section */
.orders-section {
  margin-top: 2rem;
}

/* Orders List */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 2rem;
}

.order-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  transition: all 0.3s;
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

.order-body {
  display: flex;
  gap: 1.5rem;
  align-items: center;
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

.order-actions-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.75rem;
  min-width: 200px;
}

.order-price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #FF5500;
}

.btn-view-detail {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-view-detail:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-state .spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #FF5500;
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

.order-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #FF5500;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.order-id-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.order-label {
  font-weight: 500;
  color: #333;
}

.order-id {
  font-weight: 600;
  color: #fff;
  font-family: monospace;
}

.delivery-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: #666;
}

.bullet {
  color: #999;
}

.delivery-text {
  color: #666;
}

.status-text {
  font-weight: 600;
  font-size: 0.9rem;
}

.status-text.status-completed {
  color: #28a745;
}

.status-text.status-cancelled {
  color: #dc3545;
}

.status-text.status-return {
  color: #6c757d;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-shipping {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-return {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.product-section {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
  line-height: 1.4;
}

.product-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.3;
}

.order-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
  min-width: 200px;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.total-label {
  font-weight: 500;
  color: #333;
}

.total-amount {
  font-weight: 600;
  font-size: 1rem;
}

.total-amount.total-normal {
  color: #333;
}

.total-amount.total-cancelled {
  color: #dc3545;
}

.payment-status {
  margin-top: 0.25rem;
}

.payment-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.payment-text.payment-failed {
  color: #dc3545;
}

.payment-text.payment-success {
  color: #28a745;
}

.order-actions {
  margin-top: 0.5rem;
}

.btn-view-detail {
  background: transparent;
  color: #FF5500;
  border: 2px solid #FF5500;
  padding: 0.625rem 1.25rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-view-detail:hover {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border-color: #FF5500;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

/* No Orders State */
.no-orders-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

.no-orders-content {
  text-align: center;
}

.no-orders-illustration {
  margin-bottom: 2rem;
}

.illustration-circle {
  width: 120px;
  height: 120px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  position: relative;
}

.clipboard-icon {
  width: 60px;
  height: 60px;
  position: relative;
}

.clipboard-body {
  width: 40px;
  height: 50px;
  background: #f5d76e;
  border-radius: 4px;
  position: absolute;
  top: 5px;
  left: 10px;
}

.clipboard-clip {
  width: 20px;
  height: 15px;
  background: #d4a574;
  border-radius: 2px;
  position: absolute;
  top: 0;
  left: 15px;
}

.checkmarks {
  position: absolute;
  top: 15px;
  left: 15px;
}

.checkmark {
  width: 8px;
  height: 4px;
  background: #4caf50;
  border-radius: 2px;
  margin-bottom: 3px;
  transform: rotate(-45deg);
}

.checkmark::after {
  content: '';
  position: absolute;
  width: 4px;
  height: 8px;
  background: #4caf50;
  border-radius: 2px;
  top: -2px;
  right: -2px;
  transform: rotate(90deg);
}

.pencil {
  width: 20px;
  height: 3px;
  background: #2196f3;
  border-radius: 2px;
  position: absolute;
  bottom: 10px;
  right: 5px;
  transform: rotate(45deg);
}

.pencil::after {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  background: #ffeb3b;
  border-radius: 50%;
  top: -1px;
  right: -1px;
}

.floating-dots {
  position: absolute;
  top: -10px;
  left: -10px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  position: absolute;
}

.dot-1 {
  background: #ff9800;
  top: 0;
  left: 0;
}

.dot-2 {
  background: #2196f3;
  top: 8px;
  left: 12px;
}

.dot-3 {
  background: #4caf50;
  top: 15px;
  left: 5px;
}

.no-orders-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #666;
  margin: 0;
}

/* Loading State */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

.loading-content {
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #FF5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-content p {
  color: #666;
  font-size: 1rem;
}

/* Product Count */
.product-count {
  font-size: 0.85rem;
  color: #999;
  margin-top: 0.25rem;
}

/* Responsive for new design */
@media (max-width: 768px) {
  .status-tabs {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .tab-btn {
    flex: none;
    min-width: 120px;
  }

  .search-input {
    font-size: 0.9rem;
  }

  .illustration-circle {
    width: 100px;
    height: 100px;
  }

  .clipboard-icon {
    width: 50px;
    height: 50px;
  }

  .order-content {
    flex-direction: column;
    gap: 1rem;
  }

  .order-summary {
    align-items: flex-start;
    min-width: auto;
  }

  .product-section {
    gap: 0.75rem;
  }

  .product-image {
    width: 50px;
    height: 50px;
  }
}

/* Order Actions Section */
.order-actions-section {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e9ecef;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-cancel-order,
.btn-update-order,
.btn-confirm-received {
  padding: 0.875rem 1.75rem;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  min-width: 180px;
  justify-content: center;
}

.btn-cancel-order {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(220, 53, 69, 0.3);
}

.btn-cancel-order:hover:not(:disabled) {
  background: linear-gradient(135deg, #c82333 0%, #dc3545 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.4);
}

.btn-update-order {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-update-order:hover:not(:disabled) {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
}

.btn-confirm-received {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.btn-confirm-received:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
}

.btn-cancel-order:disabled,
.btn-update-order:disabled,
.btn-confirm-received:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.modal-content.update-modal {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.modal-close:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 2rem;
  flex: 1;
  overflow-y: auto;
}

.modal-body p {
  margin: 0 0 1rem;
  color: #666;
  line-height: 1.6;
}

.warning-text {
  color: #dc3545;
  font-weight: 500;
}

.error-message-box {
  background: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  border: 1px solid #f5c6cb;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-top: 2px solid #e9ecef;
}

.btn-cancel,
.btn-confirm-cancel,
.btn-confirm-update {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  min-width: 120px;
}

.btn-cancel {
  background: #e9ecef;
  color: #333;
}

.btn-cancel:hover {
  background: #dee2e6;
}

.btn-confirm-cancel {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
  color: white;
}

.btn-confirm-cancel:hover:not(:disabled) {
  background: linear-gradient(135deg, #c82333 0%, #dc3545 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220, 53, 69, 0.3);
}

.btn-confirm-update {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
}

.btn-confirm-update:hover:not(:disabled) {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-confirm-cancel:disabled,
.btn-confirm-update:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Payment Buttons Group */
.payment-buttons-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

/* Button Thanh toán (màu xanh lá) */
.btn-confirm-payment {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  min-width: 150px;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.btn-confirm-payment:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
}

.btn-confirm-payment:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Button Thanh toán khi nhận hàng (màu xanh dương) */
.btn-pay-on-delivery {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  min-width: 200px;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
}

.btn-pay-on-delivery:hover:not(:disabled) {
  background: linear-gradient(135deg, #0056b3 0%, #007bff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.4);
}

.btn-pay-on-delivery:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Update Form Styles */
.update-section {
  margin-bottom: 2rem;
}

.update-section h4 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e9ecef;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.delivery-type-radio-group {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 500;
  color: #333;
}

.radio-option input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #FF5500;
}

.radio-option span {
  user-select: none;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.shipping-fee-preview {
  background: #e7f3ff;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  color: #0066cc;
  font-weight: 500;
}

/* Product Update List */
.product-update-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.product-update-item {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-image-container {
  flex-shrink: 0;
}

.product-main-image {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  object-fit: cover;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
}

.product-details-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-name-large {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.product-specs-large {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.spec-button {
  padding: 0.5rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 20px;
  background: #fff;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: default;
  transition: all 0.3s;
}

.spec-button:hover {
  border-color: #FF5500;
  background: #fff7f2;
}

.product-price-large {
  font-size: 1.1rem;
  color: #FF5500;
  font-weight: 600;
}

.product-actions-large {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.product-total-large {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  margin-top: 0.5rem;
}

/* Legacy styles for backward compatibility */
.product-info {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.product-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: #fff;
}

.product-info > div {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.product-info .product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

.product-info .product-specs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.product-info .product-price {
  color: #FF5500;
  font-weight: 600;
  font-size: 0.9rem;
}

.product-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-quantity {
  width: 32px;
  height: 32px;
  border: 1px solid #d0d0d0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-quantity:hover:not(:disabled) {
  background: #FF5500;
  color: white;
  border-color: #FF5500;
}

.btn-quantity:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-display {
  min-width: 40px;
  text-align: center;
  font-weight: 600;
  color: #333;
}

.btn-remove-product {
  background: #dc3545;
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-remove-product:hover:not(:disabled) {
  background: #c82333;
  transform: scale(1.1);
}

.btn-remove-product:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.product-total {
  font-weight: 700;
  color: #FF5500;
  min-width: 120px;
  text-align: right;
}

/* Price Summary */
.price-summary {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 12px;
  margin-top: 1rem;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #666;
}

.price-row.difference {
  border-top: 2px solid #e9ecef;
  padding-top: 0.75rem;
  margin-top: 0.75rem;
  font-weight: 600;
  font-size: 1.1rem;
}

.price-row .new-price {
  color: #FF5500;
  font-weight: 600;
}

.price-row .positive {
  color: #28a745;
}

.price-row .negative {
  color: #dc3545;
}

/* Responsive */
@media (max-width: 768px) {
  .status-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-actions-inline {
    width: 100%;
    flex-direction: column;
  }

  .order-actions-inline .btn-cancel-order,
  .order-actions-inline .btn-update-order,
  .order-actions-inline .btn-confirm-received {
    width: 100%;
  }

  .order-actions-section {
    flex-direction: column;
  }

  .btn-cancel-order,
  .btn-update-order,
  .btn-confirm-received {
    width: 100%;
  }

  .modal-content {
    margin: 1rem;
    max-height: 95vh;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .product-update-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-total {
    width: 100%;
    text-align: left;
    margin-top: 0.5rem;
  }
}

/* Payment History Section */
.payment-history-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
}

.payment-history-section h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e9ecef;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.payment-history-section h3::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.payment-history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.payment-history-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1rem;
  background: white;
  border-radius: 12px;
  border-left: 3px solid #007bff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.payment-history-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.payment-history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.payment-amount {
  font-size: 1.25rem;
  font-weight: 700;
}

.amount-payment {
  color: #28a745;
}

.amount-refund {
  color: #dc3545;
}

.amount-additional-fee {
  color: #007bff;
}

.payment-details {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.payment-method {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.payment-type-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  white-space: nowrap;
}

.badge-payment {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.badge-refund {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.badge-additional-fee {
  background: #cce5ff;
  color: #004085;
  border: 1px solid #b3d7ff;
}

.refund-status-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  white-space: nowrap;
}

.badge-refund-status-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.badge-refund-status-transferred {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.badge-refund-status-completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.badge-refund-status-unknown {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.payment-time {
  font-size: 0.85rem;
  color: #999;
}

.payment-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: 1rem;
}

.btn-confirm-refund {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

.btn-confirm-refund:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.4);
}

.btn-confirm-refund:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

/* Add Product Button */
.btn-add-product {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

.btn-add-product:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.4);
}

.btn-add-product:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Product Modal Styles */
.modal-content.product-modal {
  max-width: 800px;
  max-height: 90vh;
}

.modal-body .search-section {
  margin-bottom: 1.5rem;
}

.modal-body .search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.modal-body .search-input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.loading-products {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.loading-products .spinner {
  margin: 0 auto 1rem;
}

.modal-body .products-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 400px;
  overflow-y: auto;
}

.product-checkbox-label {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.product-checkbox-label:hover {
  border-color: #FF5500;
  background: #fff7f2;
}

.product-checkbox-label input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #FF5500;
}

.product-item-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.product-thumb {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  background: #f8f9fa;
}

.product-thumb-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 1.5rem;
}

.product-item-details {
  flex: 1;
}

.product-item-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.25rem 0;
}

.product-item-details .product-meta {
  font-size: 0.875rem;
  color: #666;
  margin: 0 0 0.5rem 0;
}

.product-item-details .product-specs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.modal-body .no-products {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.modal-body .no-products i {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.modal-body .no-products p {
  font-size: 1rem;
  margin: 0;
}

.modal-footer .btn-submit {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 150px;
}

.modal-footer .btn-submit:hover:not(:disabled) {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.modal-footer .btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Rating Section Styles */
.item-rating-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.rating-form-inline {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.rating-form-inline label {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
}

.rating-stars-inline {
  display: flex;
  gap: 4px;
  cursor: pointer;
}

.rating-stars-inline .star {
  font-size: 20px;
  color: #ddd;
  transition: color 0.2s;
  cursor: pointer;
}

.rating-stars-inline .star.filled {
  color: #FFD700;
}

.rating-stars-inline .star:hover {
  color: #FFD700;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.rating-label {
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
}

.rating-stars-display {
  display: flex;
  gap: 4px;
}

.rating-stars-display .star {
  font-size: 18px;
  color: #ddd;
}

.rating-stars-display .star.filled {
  color: #FFD700;
}

.btn-change-rating {
  padding: 0.25rem 0.75rem;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-change-rating:hover {
  background: #e9ecef;
  border-color: #FF5500;
  color: #FF5500;
}

/* Review Modal Styles */
.modal-content.review-modal {
  max-width: 800px;
  max-height: 90vh;
}

.review-products-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.review-product-item {
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  background: #f8f9fa;
}

.review-product-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.review-product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  background: white;
}

.review-product-info {
  flex: 1;
}

.review-product-info h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.review-product-specs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.review-form-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.rating-stars-input {
  display: flex;
  gap: 8px;
  cursor: pointer;
  margin-top: 0.5rem;
}

.rating-stars-input .star {
  font-size: 32px;
  color: #ddd;
  transition: color 0.2s, transform 0.2s;
  cursor: pointer;
}

.rating-stars-input .star:hover {
  transform: scale(1.1);
}

.rating-stars-input .star.filled {
  color: #FFD700;
}

.rating-help-text {
  margin: 0.5rem 0 0 0;
  font-size: 0.875rem;
  color: #dc3545;
}

.char-count {
  text-align: right;
  font-size: 0.875rem;
  color: #666;
  margin-top: 0.25rem;
}

.btn-review-order {
  padding: 0.875rem 1.75rem;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  border: none;
  min-width: 220px;
  justify-content: center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #333;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
}

.btn-review-order:hover:not(:disabled) {
  background: linear-gradient(135deg, #FFA500 0%, #FFD700 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 215, 0, 0.4);
}

.btn-review-order:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-review-completed {
  padding: 0.875rem 1.75rem;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: none;
  min-width: 220px;
  justify-content: center;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
  opacity: 0.8;
}

.info-text {
  color: #17a2b8;
  font-size: 0.95rem;
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: #d1ecf1;
  border-radius: 6px;
  border-left: 3px solid #17a2b8;
}
</style>
