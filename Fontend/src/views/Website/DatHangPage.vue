<template>
  <div class="dathang-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="dathang-main" style="margin-top: 80px;">
      <div class="container">


        <!-- Order Success Message -->
        <div v-if="orderSuccess.show" class="order-success-overlay">
          <div class="order-success-card">
            <div class="success-icon">
              <i class="bi bi-check-circle-fill"></i>
            </div>
            <h1 class="success-title">Đặt hàng thành công!</h1>
            <p class="success-message">
              Cảm ơn bạn đã đặt hàng tại PhoniX. Đơn hàng của bạn đang được xử lý.
            </p>
            <div class="order-id">
              Mã đơn hàng: <strong>{{ orderSuccess.orderId }}</strong>
            </div>
            <div class="success-actions">
              <button @click="continueShopping" class="btn-continue">
                <i class="bi bi-arrow-left"></i>
                Tiếp tục mua sắm
              </button>
              <button @click="trackOrder" class="btn-track">
                <i class="bi bi-search"></i>
                Theo dõi đơn hàng
              </button>
            </div>
          </div>
        </div>
        <div class="page-header">
          <h1>Đặt hàng</h1>
          <p>Hoàn tất thông tin để đặt hàng</p>
        </div>

        <!-- Timeline Stepper (after header) -->
        <div class="checkout-steps">
          <div class="step" :class="{ active: currentStep === 1, done: currentStep > 1 }">
            <div class="step-index">1</div>
            <div class="step-info">
              <div class="step-title">Thông tin đơn hàng</div>
              <div class="step-sub">Phương thức + địa chỉ</div>
            </div>
          </div>
          <div class="step-connector"></div>
          <div class="step" :class="{ active: currentStep === 2, done: currentStep > 2 }">
            <div class="step-index">2</div>
            <div class="step-info">
              <div class="step-title">Xác nhận đơn hàng</div>
              <div class="step-sub">Sản phẩm + mã giảm</div>
            </div>
          </div>
          <div class="step-connector"></div>
          <div class="step" :class="{ active: currentStep === 3 }">
            <div class="step-index">3</div>
            <div class="step-info">
              <div class="step-title">Thanh toán</div>
              <div class="step-sub">Phương thức thanh toán</div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 1" class="dathang-layout">
          <!-- Order Form -->
          <div class="order-form">
            <!-- Delivery type selector placed before customer info -->
            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-box-seam"></i>
                Phương thức nhận hàng
              </h2>
              <div class="delivery-type">
                <label :class="['dtype', { active: orderForm.deliveryType === 'delivery' }]">
                  <input type="radio" value="delivery" v-model="orderForm.deliveryType" />
                  <span><i class="bi bi-truck"></i> Giao hàng tận nơi</span>
                </label>
                <label :class="['dtype', { active: orderForm.deliveryType === 'pickup' }]">
                  <input type="radio" value="pickup" v-model="orderForm.deliveryType" />
                  <span><i class="bi bi-shop"></i> Lấy tại cửa hàng</span>
                </label>
              </div>
            </div>
            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-person"></i>
                Thông tin khách hàng
              </h2>

              <!-- Delivery type selector moved above -->

              <div class="form-row">
                <div class="form-group">
                  <label for="fullName">Họ và tên *</label>
                  <input
                    type="text"
                    id="fullName"
                    v-model="orderForm.fullName"
                    placeholder="Nhập họ và tên"
                    :class="{ 'error': errors.fullName }"
                  >
                  <span v-if="errors.fullName" class="error-message">{{ errors.fullName }}</span>
                </div>

                <div class="form-group">
                  <label for="phone">Số điện thoại *</label>
                  <input
                    type="tel"
                    id="phone"
                    v-model="orderForm.phone"
                    placeholder="Nhập số điện thoại"
                    :class="{ 'error': errors.phone }"
                  >
                  <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="email">Email *</label>
                  <input
                    type="email"
                    id="email"
                    v-model="orderForm.email"
                    placeholder="Nhập email"
                    :class="{ 'error': errors.email }"
                  >
                  <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
                </div>
              </div>

              <!-- Address Selection for Logged-in Users -->
              <div v-if="isLoggedIn && orderForm.deliveryType==='delivery'" class="address-selection">
                <h3 class="subsection-title">
                  <i class="bi bi-geo-alt"></i>
                  {{ userAddresses.length > 0 ? 'Chọn địa chỉ giao hàng' : 'Địa chỉ giao hàng' }}
                </h3>

                <div v-if="visibleAddresses.length > 0" class="address-options">
                  <label
                    v-for="address in visibleAddresses"
                    :key="address.id"
                    class="address-option"
                    :class="{ 'selected': selectedAddressId === address.id }"
                  >
                    <input
                      type="radio"
                      :value="address.id"
                      v-model="selectedAddressId"
                      @change="selectUserAddress(address)"
                      name="userAddress"
                    >
                    <div class="address-content">
                      <div class="address-header">
                        <span class="address-type-badge">{{ address.loaiDiaChi || 'Nhà riêng' }}</span>
                        <span v-if="address.macDinh" class="default-badge">Mặc định</span>
                      </div>
                      <div class="address-details">
                        <div class="address-location">
                          <i class="bi bi-geo-alt"></i>
                          {{ formatAddressDisplay(address) }}
                        </div>
                      </div>
                    </div>
                  </label>
                </div>

                <div v-else class="no-address-message">
                  <p>Bạn chưa có địa chỉ nào. Vui lòng thêm địa chỉ mới.</p>
                </div>

                <div class="address-actions">
                  <button type="button" class="btn-add-address" @click="openAddAddressModal">
                    <i class="bi bi-plus-circle"></i>
                    Thêm địa chỉ mới
                  </button>
                  <div v-if="userAddresses.length >= MAX_SAVED_ADDRESSES" class="address-limit-message">
                    Bạn đã đạt giới hạn tạo địa chỉ. Vui lòng xóa bớt để thêm địa chỉ mới.
                  </div>
                </div>
              </div>

              <!-- Address fields -->
              <div v-if="orderForm.deliveryType==='delivery'" class="form-row">
                <!-- Manual Address Input - Only show if not logged in OR logged in but no addresses selected -->
                <template v-if="!isLoggedIn || (isLoggedIn && !selectedAddressId)">
                  <div class="form-group">
                    <label for="province">Tỉnh/Thành phố</label>
                    <select
                      id="province"
                      v-model="orderForm.province"
                      @change="onProvinceChange"
                      :disabled="isLoadingProvinces"
                    >
                      <option value="">Chọn tỉnh/thành phố</option>
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
                    <label for="district">Quận/Huyện</label>
                    <select
                      id="district"
                      v-model="orderForm.district"
                      @change="onDistrictChange"
                      :disabled="!orderForm.province || isLoadingDistricts"
                    >
                      <option value="">Chọn quận/huyện</option>
                      <option
                        v-for="district in districts"
                        :key="district.DistrictID"
                        :value="district.DistrictID"
                      >
                        {{ district.DistrictName }}
                      </option>
                    </select>
                  </div>
                </template>
              </div>

              <div v-if="orderForm.deliveryType==='delivery' && (!isLoggedIn || (isLoggedIn && !selectedAddressId))" class="form-row">
                <div class="form-group full-width">
                  <label for="address">Địa chỉ cụ thể *</label>
                  <FreeMapPicker
                    v-model="orderForm.address"
                    placeholder="Số nhà, tên đường, phường/xã hoặc chọn trên bản đồ"
                    @address-selected="onAddressSelected"
                    @location-updated="onLocationUpdated"
                  />
                  <span v-if="errors.address" class="error-message">{{ errors.address }}</span>
                </div>
              </div>
            </div>

            <div v-if="orderForm.deliveryType==='delivery'" class="form-section">
              <h2 class="section-title">
                <i class="bi bi-truck"></i>
                Phương thức giao hàng
              </h2>

              <div class="delivery-options">
                <label class="delivery-option" v-for="option in deliveryOptions" :key="option.id">
                  <input
                    type="radio"
                    :value="option.id"
                    v-model="orderForm.deliveryMethod"
                    name="delivery"
                  >
                  <div class="option-content">
                    <div class="option-header">
                      <div class="option-name-wrapper">
                        <span class="option-name">{{ option.name }}</span>
                      </div>
                      <span class="option-price">{{ option.price === 0 ? 'Miễn phí' : formatPrice(option.price) }}</span>
                    </div>
                    <p class="option-description">{{ option.description }}</p>
                  </div>
                </label>
              </div>

              <!-- Shipping calculation status -->
              <div v-if="isCalculatingShipping" class="shipping-status">
                <i class="bi bi-hourglass-split"></i>
                <span>Đang tính phí vận chuyển...</span>
              </div>

              <div v-if="shippingError" class="shipping-error">
                <i class="bi bi-exclamation-triangle"></i>
                <span>{{ shippingError }}</span>
              </div>
            </div>

            <!-- Payment options moved to Step 3 -->

            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-chat-text"></i>
                Ghi chú đơn hàng
              </h2>

              <div class="form-group full-width">
                <textarea
                  v-model="orderForm.note"
                  placeholder="Ghi chú thêm cho đơn hàng (không bắt buộc)"
                  rows="3"
                ></textarea>
              </div>
            </div>

            <!-- No internal sub-step actions: whole order-form is a single screen for Step 1 -->
          </div>

          <!-- Order Summary -->
          <div class="order-summary" :class="{ 'buy-now-summary': isBuyNow }">
            <h3 class="summary-title">Tóm tắt đơn hàng</h3>

            <div class="order-items">
              <div v-for="item in selectedItems" :key="item.chiTietSanPhamId" class="order-item">
                <div class="item-image">
                  <img :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham">
                </div>
                <div class="item-info">
                  <h4 class="item-name">{{ item.tenSanPham || 'Sản phẩm không tên' }}</h4>
                  <div class="item-specs">
                    <span v-if="item.tenRam">{{ item.tenRam }}</span>
                    <span v-if="item.tenRom">{{ item.tenRom }}</span>
                    <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                  </div>
                  <div class="item-quantity">
                    <span class="qty-label">Số lượng:</span>
                    <div class="qty-control">
                      <button type="button" class="qty-btn" @click="decreaseItemQuantity(item)" :disabled="(item.quantity || 1) <= 1">-</button>
                      <input
                        class="qty-input"
                        type="number"
                        :min="1"
                        :max="item.soLuongTon || 9999"
                        :value="item.quantity || 1"
                        @input="onQuantityInput($event, item)"
                      />
                      <button type="button" class="qty-btn" @click="increaseItemQuantity(item)" :disabled="item.quantity >= (item.soLuongTon || 1)">+</button>
                    </div>
                  </div>
                </div>
                <div class="item-price">{{ formatPrice((item.gia || 0) * (item.quantity || 1)) }}</div>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row">
              <span>Tạm tính:</span>
              <span class="summary-value">{{ formatPrice(selectedItems.reduce((s,i)=> s + ((i.gia || 0) * (i.quantity || 1)), 0)) }}</span>
            </div>

            <div class="summary-row">
              <span>Phí vận chuyển:</span>
              <span class="summary-value">{{ getDeliveryPrice() }}</span>
            </div>

            <!-- Shipping method info -->
            <div v-if="orderForm.deliveryMethod && deliveryOptions.find(o => o.id === orderForm.deliveryMethod)" class="summary-row shipping-info">
              <span class="shipping-details">
                <i class="bi bi-truck"></i>
                {{ deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.estimatedDays }}
                <img 
                  v-if="orderForm.deliveryMethod === 'ghn' || orderForm.deliveryMethod === 'express'" 
                  src="/GHNLOGO.jpg" 
                  alt="GHN Logo" 
                  class="ghn-logo"
                />
                <span v-if="deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.provider">
                  - {{ deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.provider }}
                </span>
              </span>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row total">
              <span>Tổng cộng:</span>
              <span class="summary-total">{{ formatPrice(getTotalPrice()) }}</span>
            </div>

            <div class="step-actions">
              <button class="btn-next" @click="onContinueFromStep1" :disabled="!isStep1Valid">
                Tiếp tục
                <i class="bi bi-arrow-right"></i>
              </button>
            </div>

            <div class="security-info">
              <i class="bi bi-truck"></i>
              <span>Miễn phí giao hàng cho đơn hàng từ 30 triệu đồng</span>
            </div>
          </div>
        </div>

        <!-- Step 2: Confirm Order -->
        <div v-else-if="currentStep === 2" class="step2-confirm">
          <div class="confirm-layout">
            <div class="confirm-left">
              <div class="summary-header">
                <button class="btn-inline-prev" @click="goToStep(1)"><i class="bi bi-chevron-left"></i></button>
                <h2 class="confirm-title" style="margin:0">Xác nhận đơn hàng</h2>
              </div>
              <div class="confirm-products">
                <div v-for="item in selectedItems" :key="item.chiTietSanPhamId" class="cp-row">
                  <img class="cp-image" :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham" />
                  <div class="cp-details">
                    <div class="cp-name">{{ item.tenSanPham }}</div>
                    <div class="cp-specs" v-if="item.tenRam || item.tenRom || item.tenMauSac">
                      {{ [item.tenRam,item.tenRom,item.tenMauSac].filter(Boolean).join(' / ') }}
                    </div>
                  </div>
                  <div class="cp-qty">x{{ item.quantity || 1 }}</div>
                  <div class="cp-price">{{ formatPrice((item.gia||0) * (item.quantity||1)) }}</div>
                </div>
              </div>

              <div class="coupon-area">
                <label class="coupon-label">Mã giảm giá</label>
                <div class="coupon-actions top">
                  <input
                    class="coupon-display"
                    type="text"
                    :value="selectedCoupon ? ('Đang áp dụng: ' + selectedCoupon) : 'Chưa chọn mã giảm giá'"
                    readonly
                  />
                </div>
                <div v-if="eligibleCoupons.length === 0 && coupons.length === 0" class="coupon-empty">
                  <p>Hiện tại không có mã giảm giá nào khả dụng.</p>
                </div>
                <div v-else class="coupon-grid">
                  <div
                    v-for="c in eligibleCoupons"
                    :key="getVoucherCode(c)"
                    class="voucher-card"
                    :class="[
                      { selected: selectedCoupon === getVoucherCode(c) },
                      isPercentVoucher(c) ? 'percent' : 'amount'
                    ]"
                    @click="onSelectVoucher(c)"
                  >
                    <div class="vc-main">
                      <div class="vc-badge">{{ (c.badge || 'GIFT').toUpperCase() }}<span> VOUCHER</span></div>
                      <div class="vc-title">{{ getVoucherName(c) }}</div>
                      <div class="vc-desc">
                        <template v-if="getVoucherMinOrder(c)">ĐH tối thiểu: {{ formatPrice(getVoucherMinOrder(c)) }}.</template>
                        <template v-if="getVoucherMax(c)"> Giảm tối đa: {{ formatPrice(getVoucherMax(c)) }}.</template>
                      </div>
                      <div class="vc-code">CODE: {{ getVoucherCode(c) }}</div>
                    </div>
                    <div class="vc-right">
                      <div class="vc-currency">{{ isPercentVoucher(c) ? '' : '₫' }}</div>
                      <div class="vc-value">
                        {{ isPercentVoucher(c)
                        ? (getVoucherValue(c) + '%')
                        : (formatPrice(getVoucherValue(c)) || 'FREE') }}
                      </div>
                      <div class="vc-value-label">{{ isPercentVoucher(c) ? 'OFF' : 'value' }}</div>
                    </div>
                  </div>
                </div>

                <div v-if="couponError" class="coupon-error">{{ couponError }}</div>

                <!-- Ineligible vouchers -->
                <div class="more-vouchers">
                  <button type="button" class="btn-more" @click="showIneligible = !showIneligible">
                    {{ showIneligible ? 'Ẩn' : 'Mua thêm để áp dụng mã giảm giá sau' }}
                  </button>
                  <div v-if="showIneligible" class="coupon-grid ineligible">
                    <div
                      v-for="c in ineligibleCoupons"
                      :key="getVoucherCode(c)"
                      class="voucher-card"
                      :class="[ isPercentVoucher(c) ? 'percent' : 'amount' ]"
                    >
                      <div class="vc-main">
                        <div class="vc-badge">{{ (c.badge || 'GIFT').toUpperCase() }}<span> VOUCHER</span></div>
                        <div class="vc-title">{{ getVoucherName(c) }}</div>
                        <div class="vc-desc">
                          <template v-if="getVoucherMinOrder(c)">ĐH tối thiểu: {{ formatPrice(getVoucherMinOrder(c)) }}.</template>
                          <template v-if="getVoucherMax(c)"> Giảm tối đa: {{ formatPrice(getVoucherMax(c)) }}.</template>
                          <div class="vc-hint">Bạn cần mua thêm {{ formatPrice(Math.max(0, getVoucherMinOrder(c) - itemsSubtotal)) }} để dùng mã này.</div>
                        </div>
                        <div class="vc-code">CODE: {{ getVoucherCode(c) }}</div>
                      </div>
                      <div class="vc-right">
                        <div class="vc-currency">{{ isPercentVoucher(c) ? '' : '₫' }}</div>
                        <div class="vc-value">
                          {{ isPercentVoucher(c) ? (getVoucherValue(c) + '%') : (formatPrice(getVoucherValue(c)) || 'FREE') }}
                        </div>
                        <div class="vc-value-label">{{ isPercentVoucher(c) ? 'OFF' : 'value' }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="confirm-right">
              <div class="order-summary">
                <h3 class="summary-title">Tóm tắt đơn hàng</h3>
                <div class="order-items">
                  <div v-for="item in selectedItems" :key="item.chiTietSanPhamId" class="order-item">
                    <div class="item-image">
                      <img :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham">
                    </div>
                    <div class="item-info">
                      <h4 class="item-name">{{ item.tenSanPham || 'Sản phẩm không tên' }}</h4>
                      <div class="item-specs">
                        <span v-if="item.tenRam">{{ item.tenRam }}</span>
                        <span v-if="item.tenRom">{{ item.tenRom }}</span>
                        <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                      </div>
                      <div class="item-quantity"><span class="qty-label">Số lượng:</span> x{{ item.quantity || 1 }}</div>
                    </div>
                    <div class="item-price">{{ formatPrice((item.gia || 0) * (item.quantity || 1)) }}</div>
                  </div>
                </div>

                <div class="summary-divider"></div>

                <div class="summary-row">
                  <span>Tạm tính:</span>
                  <span class="summary-value">{{ formatPrice(itemsSubtotal) }}</span>
                </div>

                <div class="summary-row">
                  <span>Phí vận chuyển:</span>
                  <span class="summary-value">{{ formatPrice(shippingPrice) }}</span>
                </div>

                <div class="summary-row" v-if="discountAmount > 0">
                  <span>Giảm giá:</span>
                  <span class="summary-value">-{{ formatPrice(discountAmount) }}</span>
                </div>

                <div class="summary-divider"></div>

                <div class="summary-row total">
                  <span>Tổng cộng:</span>
                  <span class="summary-total">{{ formatPrice(itemsSubtotal + shippingPrice - discountAmount) }}</span>
                </div>

                <div class="summary-footer">
                  <button class="btn-inline-next" @click="goToStep(3)">Tiếp tục <i class="bi bi-arrow-right"></i></button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Payment -->
        <div v-else-if="currentStep === 3" class="step3-payment">
          <div class="confirm-layout">
            <div class="confirm-left">
              <div class="payment-section">
                <div class="summary-header">
                  <button class="btn-inline-prev" @click="goToStep(2)"><i class="bi bi-chevron-left"></i></button>
                  <h2 style="margin:0">Phương thức thanh toán</h2>
                </div>
                <div class="payment-options">
                  <label
                    class="payment-option"
                    v-for="option in paymentOptions"
                    :key="option.id"
                    :class="{
                    'popular': option.popular,
                    'unavailable': !option.available,
                    'selected': orderForm.paymentMethod === option.id
                  }"
                  >
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="orderForm.paymentMethod"
                      name="payment"
                      :disabled="!option.available"
                    >
                    <div class="option-content">
                      <div class="option-header">
                        <div class="option-main">
                          <i :class="option.icon"></i>
                          <span class="option-name">{{ option.name }}</span>
                        </div>
                        <div class="option-fee">
                          <span v-if="option.fee > 0" class="fee-amount">{{ formatPrice(option.fee) }}</span>
                          <span v-else class="free-text">Miễn phí</span>
                        </div>
                      </div>
                      <div class="option-details">
                        <p class="option-description">{{ option.description }}</p>
                      </div>
                    </div>
                  </label>
                </div>
                <div class="online-pay-actions" v-if="orderForm.paymentMethod==='vnpay' || orderForm.paymentMethod==='zalopay'">
                  <div class="pay-summary">Số tiền thanh toán: <strong>{{ formatPrice(totalAfterDiscount) }}</strong></div>
                  <div class="pay-buttons">
                    <button v-if="orderForm.paymentMethod==='vnpay'" class="btn-next" @click="createVNPayPayment" :disabled="!isFormValid || totalAfterDiscount<=0">Thanh toán VNPay</button>
                    <button v-if="orderForm.paymentMethod==='zalopay'" class="btn-next" @click="createZaloPayPayment" :disabled="!isFormValid || totalAfterDiscount<=0">Thanh toán ZaloPay</button>
                  </div>
                </div>
              </div>
            </div>
            <!-- Right card: Order summary (single instance) -->
            <div class="confirm-right">
              <div class="order-summary">
                <h3 class="summary-title" style="margin:0 0 8px 0">Tóm tắt đơn hàng</h3>
                <div class="order-items">
                  <div v-for="item in selectedItems" :key="item.chiTietSanPhamId" class="order-item">
                    <div class="item-image">
                      <img :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham">
                    </div>
                    <div class="item-info">
                      <h4 class="item-name">{{ item.tenSanPham || 'Sản phẩm không tên' }}</h4>
                      <div class="item-specs">
                        <span v-if="item.tenRam">{{ item.tenRam }}</span>
                        <span v-if="item.tenRom">{{ item.tenRom }}</span>
                        <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                      </div>
                      <div class="item-quantity"><span class="qty-label">Số lượng:</span> x{{ item.quantity || 1 }}</div>
                    </div>
                    <div class="item-price">{{ formatPrice((item.gia || 0) * (item.quantity || 1)) }}</div>
                  </div>
                </div>
                <div class="summary-divider"></div>
                <div class="summary-row">
                  <span>Tạm tính:</span>
                  <span class="summary-value">{{ formatPrice(itemsSubtotal) }}</span>
                </div>
                <div class="summary-row">
                  <span>Phí vận chuyển:</span>
                  <span class="summary-value">{{ formatPrice(shippingPrice) }}</span>
                </div>
                <div class="summary-row" v-if="discountAmount>0">
                  <span>Giảm giá:</span>
                  <span class="summary-value">-{{ formatPrice(discountAmount) }}</span>
                </div>
                <div class="summary-row total">
                  <span>Tổng cộng:</span>
                  <span class="summary-total">{{ formatPrice(itemsSubtotal + shippingPrice - discountAmount) }}</span>
                </div>
                <div class="summary-footer">
                  <button class="btn-inline-next" v-if="orderForm.paymentMethod!=='vnpay' && orderForm.paymentMethod!=='zalopay'" @click="openConfirmModal" :disabled="!isFormValid">
                    Xác nhận đặt hàng
                    <i class="bi bi-check-circle"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="dathang-footer">
      <FooterLayout/>
    </footer>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>

    <!-- Confirm Order Modal -->
    <div v-if="confirmModal.show" class="confirm-overlay" @click.self="closeConfirmModal">
      <div class="confirm-card">
        <div class="confirm-header">
          <h3>Xác nhận đặt hàng</h3>
          <button class="confirm-close" type="button" @click="closeConfirmModal">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <div class="confirm-content">
          <div class="confirm-row">
            <span class="label">Khách hàng</span>
            <span class="value">{{ orderForm.fullName }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Số điện thoại</span>
            <span class="value">{{ orderForm.phone }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Địa chỉ</span>
            <span class="value">{{ orderForm.deliveryType === 'pickup' ? 'Tại cửa hàng' : orderForm.address }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Thanh toán</span>
            <span class="value">{{ paymentOptions.find(p=>p.id===orderForm.paymentMethod)?.name }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Giao hàng</span>
            <span class="value">{{ deliveryOptions.find(d=>d.id===orderForm.deliveryMethod)?.name }}</span>
          </div>
          <div class="confirm-divider"></div>
          <div class="confirm-row">
            <span class="label">Tạm tính</span>
            <span class="value">{{ formatPrice(itemsSubtotal) }}</span>
          </div>
          <div class="confirm-row">
            <span class="label">Phí vận chuyển</span>
            <span class="value">{{ formatPrice(shippingPrice) }}</span>
          </div>
          <div class="confirm-row" v-if="discountAmount>0">
            <span class="label">Giảm giá</span>
            <span class="value">-{{ formatPrice(discountAmount) }}</span>
          </div>
          <div class="confirm-row total">
            <span class="label">Tổng cộng</span>
            <span class="value">{{ formatPrice(itemsSubtotal + shippingPrice - discountAmount) }}</span>
          </div>
        </div>

        <div class="confirm-actions">
          <button type="button" class="btn-cancel" @click="closeConfirmModal" :disabled="confirmModal.loading">Hủy</button>
          <button type="button" class="btn-confirm" @click="confirmPlaceOrder" :disabled="confirmModal.loading">
            <i v-if="confirmModal.loading" class="bi bi-hourglass-split"></i>
            <i v-else class="bi bi-check2-circle"></i>
            {{ confirmModal.loading ? 'Đang xử lý...' : 'Xác nhận đặt hàng' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Add Address Modal -->
    <div v-if="showAddAddressModal" class="confirm-overlay" @click.self="closeAddAddressModal">
      <div class="confirm-card" style="max-width: 600px; max-height: 90vh; overflow-y: auto;">
        <div class="confirm-header">
          <h3>Thêm địa chỉ mới</h3>
          <button class="confirm-close" type="button" @click="closeAddAddressModal"><i class="bi bi-x"></i></button>
        </div>
        <div class="confirm-content">
          <form @submit.prevent="handleSaveNewAddress" class="address-form">
            <div class="form-group">
              <label for="newAddressProvince">Tỉnh/Thành phố <span class="required">*</span></label>
              <select
                id="newAddressProvince"
                v-model="newAddressForm.province"
                @change="onNewAddressProvinceChange"
                :disabled="isLoadingProvinces"
                required
                class="form-input"
              >
                <option value="">Chọn tỉnh/thành phố</option>
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
              <label for="newAddressDistrict">Quận/Huyện <span class="required">*</span></label>
              <select
                id="newAddressDistrict"
                v-model="newAddressForm.district"
                @change="onNewAddressDistrictChange"
                :disabled="!newAddressForm.province || isLoadingDistricts"
                required
                class="form-input"
              >
                <option value="">Chọn quận/huyện</option>
                <option
                  v-for="district in districts"
                  :key="district.DistrictID"
                  :value="district.DistrictID"
                >
                  {{ district.DistrictName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="newAddressDetail">Địa chỉ cụ thể <span class="required">*</span></label>
              <FreeMapPicker
                v-model="newAddressForm.address"
                placeholder="Số nhà, tên đường, phường/xã hoặc chọn trên bản đồ"
                @address-selected="onNewAddressSelected"
                @location-updated="onNewLocationUpdated"
              />
            </div>

            <div class="form-group">
              <label for="newAddressType">Loại địa chỉ</label>
              <select id="newAddressType" v-model="newAddressForm.loaiDiaChi" class="form-input">
                <option value="Nhà riêng">Nhà riêng</option>
                <option value="Công ty">Công ty</option>
                <option value="Khác">Khác</option>
              </select>
            </div>

            <div v-if="newAddressForm.loaiDiaChi === 'Khác'" class="form-group">
              <label for="customAddressType">Nhập loại địa chỉ <span class="required">*</span></label>
              <input
                id="customAddressType"
                type="text"
                v-model="newAddressForm.customLoaiDiaChi"
                placeholder="Ví dụ: Cửa hàng, Văn phòng, ..."
                class="form-input"
                required
              />
            </div>

            <div class="form-group">
              <label>
                <input
                  type="checkbox"
                  v-model="newAddressForm.macDinh"
                  class="checkbox-input"
                />
                <span>Đặt làm địa chỉ mặc định</span>
              </label>
            </div>

            <div v-if="newAddressError" class="error-message" style="margin-top: 10px;">
              {{ newAddressError }}
            </div>

            <div class="confirm-actions" style="margin-top: 20px;">
              <button type="button" class="btn-cancel" @click="closeAddAddressModal">Hủy</button>
              <button type="submit" class="btn-confirm" :disabled="savingNewAddress">
                <span v-if="savingNewAddress">Đang lưu...</span>
                <span v-else>Lưu địa chỉ</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'
import FreeMapPicker from '@/components/FreeMapPicker.vue'
import shippingService from '@/services/shippingService.js'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
// Local list of selected items for checkout (do not mutate full cart)
const selectedItems = ref([])

// Watch selectedItems to ensure it doesn't get lost when switching steps
watch(selectedItems, (newVal) => {
  // If selectedItems becomes empty unexpectedly (and not during order success),
  // reload from cartStore if available
  if (newVal.length === 0 && cartStore.items.length > 0 && !orderSuccess.value.show) {
    console.warn('⚠️ selectedItems became empty, reloading from cartStore')
    selectedItems.value = [...cartStore.items]
    window.purchasedItems = [...cartStore.items]
  }
}, { deep: true })

// Stepper state
const currentStep = ref(1)
const onContinueFromStep1 = () => {
  if (!isStep1Valid.value) return
  goToStep(2)
}
const goToStep = (step) => {
  if (step === 2 && !isStep1Valid.value) return
  currentStep.value = step

  // Ensure selectedItems is preserved when switching steps
  // If selectedItems is empty but cartStore has items, reload from cartStore
  if (selectedItems.value.length === 0 && cartStore.items.length > 0) {
    console.log('🔄 Reloading selectedItems from cartStore when switching to step', step)
    selectedItems.value = [...cartStore.items]
    window.purchasedItems = [...cartStore.items]
  }

  // Save selectedItems to sessionStorage to prevent loss
  try {
    sessionStorage.setItem('checkoutSelectedItems', JSON.stringify(selectedItems.value))
  } catch (e) {
    console.warn('Failed to save selectedItems to sessionStorage:', e)
  }

  // reflect in URL for deep-linking each screen
  router.replace({ query: { ...route.query, step: String(step) } })
  // ensure top
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Xử lý query params từ ProductDetailPage
const isBuyNow = ref(false)
const buyNowProduct = ref(null)

const API_BASE_URL = 'http://localhost:8080'

// Form data - Load from sessionStorage if available
const loadFormFromStorage = () => {
  try {
    const saved = sessionStorage.getItem('checkoutFormData')
    if (saved) {
      const parsed = JSON.parse(saved)
      return {
        fullName: parsed.fullName || '',
        phone: parsed.phone || '',
        email: parsed.email || '',
        province: parsed.province || '',
        district: parsed.district || '',
        address: parsed.address || '',
        deliveryType: parsed.deliveryType || 'delivery',
        deliveryMethod: parsed.deliveryMethod || 'standard',
        paymentMethod: parsed.paymentMethod || 'cod',
        note: parsed.note || ''
      }
    }
  } catch (e) {
    console.warn('Failed to load form from storage:', e)
  }
  return {
    fullName: '',
    phone: '',
    email: '',
    province: '',
    district: '',
    address: '',
    deliveryType: 'delivery',
    deliveryMethod: 'standard',
    paymentMethod: 'cod',
    note: ''
  }
}

const orderForm = ref(loadFormFromStorage())

// Save form to sessionStorage whenever it changes
watch(() => orderForm.value, (newVal) => {
  console.log('📝 Form changed:', {
    fullName: newVal.fullName,
    phone: newVal.phone,
    address: newVal.address,
    deliveryType: newVal.deliveryType,
    paymentMethod: newVal.paymentMethod
  })
  // Save to sessionStorage
  try {
    sessionStorage.setItem('checkoutFormData', JSON.stringify(newVal))
  } catch (e) {
    console.warn('Failed to save form to storage:', e)
  }
}, { deep: true })

// User authentication state
const isLoggedIn = ref(false)
const userInfo = ref(null)
const MAX_SAVED_ADDRESSES = 5
const userAddresses = ref([])
const selectedAddressId = ref(null)
const visibleAddresses = computed(() => userAddresses.value.slice(0, MAX_SAVED_ADDRESSES))

// Form validation errors
const errors = ref({})

// Address selection state
const provinces = ref([])
const districts = ref([])
const isLoadingProvinces = ref(false)
const isLoadingDistricts = ref(false)

// Shipping calculation state
const shippingRates = ref({})
const isCalculatingShipping = ref(false)
const shippingError = ref('')

// Delivery options - will be updated dynamically based on address
const deliveryOptions = ref([
  {
    id: 'standard',
    name: 'Giao hàng tiêu chuẩn',
    description: 'Giao hàng trong 5-7 ngày',
    price: 0,
    estimatedDays: '5-7 ngày'
  },
  {
    id: 'express',
    name: 'Giao hàng nhanh',
    description: 'Giao hàng trong 2-3 ngày làm việc',
    price: 0, // Will be calculated based on address
    estimatedDays: '2-3 ngày'
  },
  {
    id: 'ghn',
    name: 'Ship hỏa tốc',
    description: 'Giao hàng hỏa tốc',
    price: 0, // Will be calculated based on address
    estimatedDays: '1-2 ngày',
    provider: 'GHN'
  }
])

// Payment options
const paymentOptions = ref([
  {
    id: 'cod',
    name: 'Thanh toán khi nhận hàng (COD)',
    description: 'Thanh toán bằng tiền mặt khi nhận hàng',
    icon: 'bi bi-cash',
    available: true,
    fee: 0,
    processingTime: 'Ngay khi nhận hàng',
    security: 'An toàn tuyệt đối',
    popular: true
  },
  // Removed bank transfer & MoMo per request
  {
    id: 'vnpay',
    name: 'VNPay',
    description: 'Thanh toán qua cổng VNPay',
    icon: 'bi bi-credit-card',
    available: true,
    fee: 0,
    processingTime: 'Ngay lập tức',
    security: 'Bảo mật cao',
    popular: false
  }
])

// Toast notification
const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: 'bi-check-circle-fill'
})

// Order success state
const orderSuccess = ref({
  show: false,
  orderId: ''
})

// Confirm modal state
const confirmModal = ref({
  show: false,
  loading: false
})

// Coupons from backend
const coupons = ref([])
const selectedCoupon = ref('')
const couponError = ref('')
const discountAmount = ref(0)
const itemsSubtotal = computed(() => selectedItems.value.reduce((s,i)=> s + ((i.gia||0)*(i.quantity||1)), 0))
const shippingPrice = computed(() => {
  if (orderForm.value.deliveryType === 'pickup') return 0

  // Miễn phí ship cho đơn hàng từ 30 triệu đồng
  if (itemsSubtotal.value >= 30000000) {
    return 0
  }

  const selectedRate = shippingRates.value?.[orderForm.value.deliveryMethod]
  if (selectedRate && typeof selectedRate.price === 'number') {
    return selectedRate.price
  }

  const selectedDelivery = deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)
  return selectedDelivery ? (selectedDelivery.price || 0) : 0
})
// Helpers for voucher fields from backend (support snake_case and camelCase)
const getVoucherId = (c) => c?.id || c?.id_phieu_giam_gia || c?.voucherId || null
const getVoucherCode = (c) => c?.ma_phieu_giam_gia || c?.maPhieuGiamGia || c?.code || ''
const getVoucherName = (c) => c?.ten_phieu_giam_gia || c?.tenPhieuGiamGia || c?.title || 'VOUCHER'
const getVoucherType = (c) => (c?.loai_phieu_giam_gia || c?.loaiPhieuGiamGia || c?.type || '').toString().toLowerCase()
const isPercentVoucher = (c) => {
  const t = getVoucherType(c)
  return t.includes('phần') || t.includes('phan') || t.includes('percent') || t.includes('phan_tram')
}
const getVoucherValue = (c) => c?.gia_tri_giam_gia ?? c?.giaTriGiamGia ?? c?.value ?? 0
const getVoucherMax = (c) => c?.so_tien_giam_toi_da ?? c?.soTienGiamToiDa ?? 0
const getVoucherMinOrder = (c) => c?.hoa_don_toi_thieu ?? c?.hoaDonToiThieu ?? 0
const getVoucherLabel = (c) => `${getVoucherName(c)} • ${getVoucherCode(c)}`
// Helper to get voucher status (trangThai)
const getVoucherStatus = (c) => c?.trang_thai ?? c?.trangThai ?? c?.status ?? 0
// Helper to check if voucher is private (riengTu)
const isPrivateVoucher = (c) => c?.rieng_tu ?? c?.riengTu ?? false
// Helper to check if voucher is active
const isActiveVoucher = (c) => getVoucherStatus(c) === 1

const applyCoupon = () => {
  couponError.value = ''
  discountAmount.value = 0
  const c = coupons.value.find(x => getVoucherCode(x) === selectedCoupon.value)
  if (!c) {
    console.log('⚠️ applyCoupon: No coupon found for', selectedCoupon.value)
    return
  }
  console.log('💳 applyCoupon: Applying coupon', getVoucherCode(c), 'to subtotal', itemsSubtotal.value)
  if (isPercentVoucher(c)) {
    const calc = Math.floor(itemsSubtotal.value * getVoucherValue(c) / 100)
    discountAmount.value = Math.min(calc, getVoucherMax(c) || calc)
  } else if (getVoucherType(c).includes('shipping')) {
    // Make delivery price zero
    const opt = deliveryOptions.value.find(o => o.id === orderForm.value.deliveryMethod)
    if (opt) discountAmount.value = Math.min(opt.price || 0, opt.price || 0)
  } else {
    discountAmount.value = getVoucherValue(c) || 0
  }
  console.log('✅ applyCoupon: Discount calculated:', discountAmount.value)
}

// Re-apply coupon when itemsSubtotal changes (e.g., quantity changes)
watch(itemsSubtotal, () => {
  if (selectedCoupon.value) {
    console.log('📊 itemsSubtotal changed, re-applying coupon')
    applyCoupon()
  }
})

const selectedCouponId = ref(null)

// Save selected coupon to sessionStorage
watch([selectedCoupon, selectedCouponId], ([coupon, couponId]) => {
  console.log('💳 Coupon changed:', { coupon, couponId })
  try {
    sessionStorage.setItem('checkoutSelectedCoupon', coupon || '')
    sessionStorage.setItem('checkoutSelectedCouponId', couponId ? String(couponId) : '')
    // Re-apply coupon if changed
    if (coupon) {
      applyCoupon()
    }
  } catch (e) {
    console.warn('Failed to save coupon to storage:', e)
  }
})

const onSelectVoucher = (c) => {
  selectedCoupon.value = getVoucherCode(c)
  selectedCouponId.value = getVoucherId(c)
  applyCoupon()
}

// Derived voucher lists based on subtotal
// Chỉ hiển thị voucher đang hoạt động (cả công khai và cá nhân được gán cho customer)
const eligibleCoupons = computed(() => {
  return coupons.value.filter(c => {
    const isActive = isActiveVoucher(c)
    const meetsMinOrder = itemsSubtotal.value >= (getVoucherMinOrder(c) || 0)
    return isActive && meetsMinOrder
  })
})
const ineligibleCoupons = computed(() => {
  return coupons.value.filter(c => {
    const isActive = isActiveVoucher(c)
    const meetsMinOrder = itemsSubtotal.value < (getVoucherMinOrder(c) || 0)
    return isActive && meetsMinOrder
  })
})
const showIneligible = ref(false)

// Computed properties
const isFormValid = computed(() => {
  // For pickup: don't require address/delivery method
  if (orderForm.value.deliveryType === 'pickup') {
    const isValid = !!(orderForm.value.fullName && orderForm.value.phone && orderForm.value.paymentMethod)
    if (!isValid) {
      console.log('⚠️ Form validation (pickup):', {
        fullName: !!orderForm.value.fullName,
        phone: !!orderForm.value.phone,
        paymentMethod: !!orderForm.value.paymentMethod,
        fullNameValue: orderForm.value.fullName,
        phoneValue: orderForm.value.phone,
        paymentMethodValue: orderForm.value.paymentMethod
      })
    }
    return isValid
  }
  // For delivery: require address and a selected delivery method
  const isValid = !!(orderForm.value.fullName &&
    orderForm.value.phone &&
    orderForm.value.address &&
    orderForm.value.deliveryMethod &&
    orderForm.value.paymentMethod)
  if (!isValid) {
    console.log('⚠️ Form validation (delivery):', {
      fullName: !!orderForm.value.fullName,
      phone: !!orderForm.value.phone,
      address: !!orderForm.value.address,
      deliveryMethod: !!orderForm.value.deliveryMethod,
      paymentMethod: !!orderForm.value.paymentMethod,
      fullNameValue: orderForm.value.fullName,
      phoneValue: orderForm.value.phone,
      addressValue: orderForm.value.address,
      deliveryMethodValue: orderForm.value.deliveryMethod,
      paymentMethodValue: orderForm.value.paymentMethod
    })
  }
  return isValid
})

// Methods
const increaseItemQuantity = (item) => {
  try {
    cartStore.increaseQuantity(item.chiTietSanPhamId)
    // Update selectedItems to reflect cartStore changes (including price)
    const target = selectedItems.value.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
    if (target) {
      const cartItem = cartStore.items.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
      if (cartItem) {
        target.quantity = cartItem.quantity
        // Ensure price is synced from cartStore (uses discounted price if available)
        target.gia = cartItem.gia
      }
    }
  } catch (e) {
    showToast('error', e?.message || 'Không thể tăng số lượng', 'bi-exclamation-circle-fill')
  }
}

const decreaseItemQuantity = (item) => {
  try {
    cartStore.decreaseQuantity(item.chiTietSanPhamId)
    // Update selectedItems to reflect cartStore changes (including price)
    const target = selectedItems.value.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
    if (target) {
      const cartItem = cartStore.items.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
      if (cartItem) {
        target.quantity = cartItem.quantity
        // Ensure price is synced from cartStore (uses discounted price if available)
        target.gia = cartItem.gia
      } else {
        // Item was removed from cart, remove from selectedItems too
        selectedItems.value = selectedItems.value.filter(i => i.chiTietSanPhamId !== item.chiTietSanPhamId)
      }
    }
  } catch (e) {
    showToast('error', e?.message || 'Không thể giảm số lượng', 'bi-exclamation-circle-fill')
  }
}

const onQuantityInput = (evt, item) => {
  const raw = (evt?.target?.value || '').toString().trim()
  let next = parseInt(raw, 10)
  if (isNaN(next)) next = 1
  if (next < 1) next = 1
  if (item.soLuongTon && next > item.soLuongTon) next = item.soLuongTon

  try {
    cartStore.updateQuantity(item.chiTietSanPhamId, next)
    // Update selectedItems to reflect cartStore changes (including price)
    const target = selectedItems.value.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
    if (target) {
      const cartItem = cartStore.items.find(i => i.chiTietSanPhamId === item.chiTietSanPhamId)
      if (cartItem) {
        target.quantity = cartItem.quantity
        // Ensure price is synced from cartStore (uses discounted price if available)
        target.gia = cartItem.gia
      } else {
        // Item was removed from cart, remove from selectedItems too
        selectedItems.value = selectedItems.value.filter(i => i.chiTietSanPhamId !== item.chiTietSanPhamId)
      }
    }
  } catch (e) {
    showToast('error', e?.message || 'Số lượng không hợp lệ', 'bi-exclamation-circle-fill')
  }
}
const checkUserLogin = () => {
  // Check localStorage for customer token/info (website uses customerToken and customerUser)
  const customerToken = localStorage.getItem('customerToken')
  const customerUserStr = localStorage.getItem('customerUser')

  if (customerToken && customerUserStr) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(customerUserStr)

      // Auto-fill form with customer data
      if (userInfo.value) {
        orderForm.value.fullName = userInfo.value.hoTen || userInfo.value.tenKhachHang || userInfo.value.fullName || ''
        orderForm.value.phone = userInfo.value.soDienThoai || userInfo.value.phone || ''
        orderForm.value.email = userInfo.value.email || userInfo.value.emailKhachHang || ''
      }

      // Load user addresses from API
      loadUserAddresses()

    } catch (error) {
      console.error('Error parsing customer data:', error)
      isLoggedIn.value = false
      userInfo.value = null
    }
  } else {
    isLoggedIn.value = false
    userInfo.value = null
  }
}

// Load user addresses from API
const loadUserAddresses = async () => {
  if (!isLoggedIn.value || !userInfo.value?.id) {
    userAddresses.value = []
    return
  }

  const customerId = userInfo.value.id
  if (!customerId) {
    userAddresses.value = []
    return
  }

  try {
    const response = await api.get(`/api/user-dia-chi/khach-hang/${customerId}`)
    const addressesData = response.data || []

    if (addressesData && Array.isArray(addressesData)) {
      // Process UserDiaChi data structure
      userAddresses.value = addressesData.map((userDiaChi) => {
        const diaChi = userDiaChi.diaChi || {}
        return {
          id: userDiaChi.id,
          fullName: userInfo.value?.hoTen || userInfo.value?.tenKhachHang || '',
          phone: userInfo.value?.soDienThoai || userInfo.value?.phone || '',
          email: userInfo.value?.email || '',
          diaChiChiTiet: diaChi.diaChiChiTiet || '',
          phuongXa: diaChi.phuongXa || '',
          quanHuyen: diaChi.quanHuyen || '',
          tinhThanhPho: diaChi.tinhThanhPho || '',
          province: null, // Will be mapped from province name
          district: null, // Will be mapped from district name
          address: diaChi.diaChiChiTiet || '',
          macDinh: userDiaChi.macDinh || false,
          loaiDiaChi: userDiaChi.loaiDiaChi || 'Nhà riêng', // Get loaiDiaChi from userDiaChi
          diaChi: diaChi
        }
      })

      // Load provinces to map address data
      await loadProvinces()

      // Map province and district IDs
      for (const addr of userAddresses.value) {
        if (addr.tinhThanhPho) {
          const province = provinces.value.find(p =>
            p.ProvinceName === addr.tinhThanhPho ||
            p.ProvinceName.toLowerCase() === addr.tinhThanhPho.toLowerCase()
          )
          if (province) {
            addr.province = province.ProvinceID
            // Load districts for this province
            await loadDistricts(province.ProvinceID)
            if (addr.quanHuyen) {
              const district = districts.value.find(d =>
                d.DistrictName === addr.quanHuyen ||
                d.DistrictName.toLowerCase() === addr.quanHuyen.toLowerCase()
              )
              if (district) {
                addr.district = district.DistrictID
              }
            }
          }
        }
      }

      // Set default address if available
      const defaultAddress = userAddresses.value.find(addr => addr.macDinh)
      if (defaultAddress) {
        selectedAddressId.value = defaultAddress.id
        await selectUserAddress(defaultAddress)
      } else if (userAddresses.value.length > 0) {
        // Use first address if no default
        selectedAddressId.value = userAddresses.value[0].id
        await selectUserAddress(userAddresses.value[0])
      }
    } else {
      userAddresses.value = []
    }
  } catch (error) {
    console.error('Error loading user addresses:', error)
    userAddresses.value = []
  }
}

// Format address for display in dropdown
const formatAddressDisplay = (address) => {
  const parts = []
  if (address.diaChiChiTiet) parts.push(address.diaChiChiTiet)
  if (address.phuongXa) parts.push(address.phuongXa)
  if (address.quanHuyen) parts.push(address.quanHuyen)
  if (address.tinhThanhPho) parts.push(address.tinhThanhPho)
  return parts.join(', ') || 'Địa chỉ'
}

// Handle address selection change
const onAddressSelectChange = () => {
  if (!selectedAddressId.value) return

  const address = userAddresses.value.find(a => a.id === selectedAddressId.value)
  if (address) {
    selectUserAddress(address)
  }
}

// Select user address
const selectUserAddress = async (address) => {
  selectedAddressId.value = address.id

  // Update form with selected address
  orderForm.value.fullName = address.fullName || userInfo.value?.hoTen || ''
  orderForm.value.phone = address.phone || userInfo.value?.soDienThoai || ''
  orderForm.value.email = address.email || userInfo.value?.email || ''

  // Set address details
  if (address.province) {
    orderForm.value.province = address.province
    // Load districts if not already loaded
    if (!districts.value.length || districts.value[0]?.ProvinceID !== address.province) {
      await loadDistricts(address.province)
    }
  } else if (address.tinhThanhPho) {
    // If province ID not mapped, try to find it from province name
    await loadProvinces()
    const province = provinces.value.find(p =>
      p.ProvinceName === address.tinhThanhPho ||
      p.ProvinceName.toLowerCase() === address.tinhThanhPho.toLowerCase()
    )
    if (province) {
      orderForm.value.province = province.ProvinceID
      address.province = province.ProvinceID
      await loadDistricts(province.ProvinceID)
    }
  }

  if (address.district) {
    orderForm.value.district = address.district
  } else if (address.quanHuyen && districts.value.length > 0) {
    // If district ID not mapped, try to find it from district name
    const district = districts.value.find(d =>
      d.DistrictName === address.quanHuyen ||
      d.DistrictName.toLowerCase() === address.quanHuyen.toLowerCase()
    )
    if (district) {
      orderForm.value.district = district.DistrictID
      address.district = district.DistrictID
    }
  }

  // Combine address parts
  const addressParts = []
  if (address.diaChiChiTiet) addressParts.push(address.diaChiChiTiet)
  if (address.phuongXa) addressParts.push(address.phuongXa)
  orderForm.value.address = addressParts.join(', ') || address.address || ''

  // Recalculate shipping - ensure we have province and district before calculating
  if (orderForm.value.province && orderForm.value.district) {
    await calculateShippingRates()
  } else {
    console.warn('⚠️ Cannot calculate shipping: missing province or district', {
      province: orderForm.value.province,
      district: orderForm.value.district,
      address: address
    })
  }
}

const confirmSelectAddress = () => {
  const addr = userAddresses.value.find(a => a.id === selectedAddressId.value)
  if (addr) {
    selectUserAddress(addr)
    closeAddressModal()
  }
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getProductImage = (imagePath) => {
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

  if (!imagePath) return placeholderSVG
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const getDeliveryPrice = () => {
  if (orderForm.value.deliveryType === 'pickup') return 'Miễn phí'

  // Miễn phí ship cho đơn hàng từ 30 triệu đồng
  if (itemsSubtotal.value >= 30000000) {
    return 'Miễn phí'
  }

  const selectedDelivery = deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)

  if (!selectedDelivery) return 'Miễn phí'

  if (isCalculatingShipping.value) {
    return 'Đang tính...'
  }

  if (shippingError.value) {
    return 'Lỗi tính phí'
  }

  return shippingPrice.value === 0 ? 'Miễn phí' : formatPrice(shippingPrice.value)
}

const getTotalPrice = () => {
  const itemsTotal = selectedItems.value.reduce((sum, item) => sum + ((item.gia || 0) * (item.quantity || 1)), 0)
  return itemsTotal + shippingPrice.value
}

// Validation for step 1 minimal info
const isStep1Valid = computed(() => {
  if (orderForm.value.deliveryType === 'pickup') {
    return !!(orderForm.value.fullName && orderForm.value.phone && orderForm.value.email)
  }
  return !!(orderForm.value.fullName && orderForm.value.phone && orderForm.value.email && orderForm.value.address && orderForm.value.deliveryMethod)
})

// Total after discount (for online payments)
const totalAfterDiscount = computed(() => {
  const total = itemsSubtotal.value + shippingPrice.value - discountAmount.value
  return total > 0 ? total : 0
})

// No internal sub-step validation now; whole Step 1 uses isStep1Valid

// Calculate total weight for shipping
const calculateTotalWeight = () => {
  // Estimate weight based on product count (assuming average 200g per item)
  const baseWeight = selectedItems.value.reduce((sum, item) => sum + item.quantity, 0) * 200
  return Math.max(baseWeight, 1000) // Minimum 1kg
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

// Load districts for a province
const loadDistricts = async (provinceId) => {
  if (!provinceId) return

  isLoadingDistricts.value = true
  try {
    const data = await shippingService.loadDistricts(provinceId)
    districts.value = data
  } catch (error) {
    console.error('Error loading districts:', error)
    districts.value = []
  } finally {
    isLoadingDistricts.value = false
  }
}

// Handle province change
const onProvinceChange = async () => {
  orderForm.value.district = '' // Reset district selection
  await loadDistricts(orderForm.value.province)

  // Recalculate shipping when province changes
  if (orderForm.value.address) {
    calculateShippingRates()
  }
}

// Handle district change
const onDistrictChange = () => {
  // Recalculate shipping when district changes
  if (orderForm.value.address) {
    calculateShippingRates()
  }
}

// Handle address selected from map
const onAddressSelected = (addressData) => {
  // Update the address field
  orderForm.value.address = addressData.formatted_address

  // Try to extract province and district from the address
  if (addressData.geometry && addressData.geometry.location) {
    // Use reverse geocoding to get more detailed address info
    reverseGeocodeAddress(addressData.geometry.location)
  }

  // Recalculate shipping
  calculateShippingRates()
}

// Handle location updated from map (with province/district info)
const onLocationUpdated = async (locationData) => {
  // Update the address field
  orderForm.value.address = locationData.address

  let updatedFields = []

  // Try to find and set province
  if (locationData.province) {
    const province = provinces.value.find(p =>
      p.ProvinceName.toLowerCase().includes(locationData.province.toLowerCase()) ||
      locationData.province.toLowerCase().includes(p.ProvinceName.toLowerCase())
    )

    if (province) {
      orderForm.value.province = province.ProvinceID
      updatedFields.push(`Tỉnh/Thành phố: ${province.ProvinceName}`)

      // Load districts for the selected province
      await onProvinceChange()

      // Try to find and set district
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
          orderForm.value.district = district.DistrictID
          updatedFields.push(`Quận/Huyện: ${district.DistrictName}`)
          console.log('Found district:', district.DistrictName) // Debug log
        } else {
          console.log('No district found for:', locationData.district) // Debug log
        }
      }
    }
  }

  // Show success message if fields were updated
  if (updatedFields.length > 0) {
    showToast('success', `Đã tự động cập nhật: ${updatedFields.join(', ')}`, 'bi-geo-alt-fill')
  } else if (locationData.province || locationData.district) {
    // Show debug info if parsing failed
    const debugInfo = []
    if (locationData.province) debugInfo.push(`Tỉnh: ${locationData.province}`)
    if (locationData.district) debugInfo.push(`Quận: ${locationData.district}`)
    showToast('info', `Không tìm thấy phù hợp: ${debugInfo.join(', ')}. Vui lòng chọn thủ công.`, 'bi-info-circle')
  }

  // Recalculate shipping
  calculateShippingRates()
}

// Reverse geocode to get detailed address info
const reverseGeocodeAddress = async (location) => {
  try {
    // This would use Google Geocoding API to get detailed address components
    // For now, we'll just trigger shipping calculation
    calculateShippingRates()
  } catch (error) {
    console.error('Error reverse geocoding:', error)
  }
}

// Calculate shipping rates based on address
const calculateShippingRates = async () => {
  if (!orderForm.value.address || !orderForm.value.province || !orderForm.value.district) {
    return
  }

  isCalculatingShipping.value = true
  shippingError.value = ''

  try {
    const weight = calculateTotalWeight()

    const rates = await shippingService.calculateShippingFee(
      orderForm.value.province,
      orderForm.value.district,
      weight
    )
    shippingRates.value = rates

    // Update delivery options with calculated prices (create new array to ensure reactivity)
    deliveryOptions.value = deliveryOptions.value.map(option => {
      if (rates[option.id]) {
        return {
          ...option,
          price: rates[option.id].price,
          description: rates[option.id].description,
          estimatedDays: rates[option.id].estimatedDays
        }
      }
      return option
    })

  } catch (error) {
    console.error('Error calculating shipping rates:', error)
    shippingError.value = 'Không thể tính phí vận chuyển. Vui lòng thử lại.'
  } finally {
    isCalculatingShipping.value = false
  }
}

// Watch for address changes to recalculate shipping
watch(
  () => [orderForm.value.address, orderForm.value.province, orderForm.value.district],
  () => {
    if (orderForm.value.address && orderForm.value.province && orderForm.value.district) {
      calculateShippingRates()
    }
  },
  { deep: true }
)

// Clear address-related errors when switching to pickup
watch(
  () => orderForm.value.deliveryType,
  (val) => {
    if (val === 'pickup') {
      delete errors.value.address
      delete errors.value.deliveryMethod
    }
  }
)

const validateForm = () => {
  errors.value = {}

  if (!orderForm.value.fullName.trim()) {
    errors.value.fullName = 'Vui lòng nhập họ và tên'
  }

  if (!orderForm.value.phone.trim()) {
    errors.value.phone = 'Vui lòng nhập số điện thoại'
  } else if (!/^[0-9]{10,11}$/.test(orderForm.value.phone.replace(/\s/g, ''))) {
    errors.value.phone = 'Số điện thoại không hợp lệ'
  }

  // Email bắt buộc
  if (!orderForm.value.email.trim()) {
    errors.value.email = 'Vui lòng nhập email'
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(orderForm.value.email.trim())) {
      errors.value.email = 'Email không hợp lệ'
    }
  }

  // Với nhận tại cửa hàng: không bắt buộc địa chỉ
  if (orderForm.value.deliveryType === 'delivery') {
    // For logged-in users with addresses, check if address is selected
    if (isLoggedIn.value && userAddresses.value.length > 0) {
      if (!selectedAddressId.value) {
        errors.value.address = 'Vui lòng chọn địa chỉ giao hàng'
      }
    } else {
      // For non-logged-in or no addresses, check manual address input
      if (!orderForm.value.address.trim()) {
        errors.value.address = 'Vui lòng nhập địa chỉ cụ thể'
      }
    }
    if (!orderForm.value.deliveryMethod) {
      errors.value.deliveryMethod = 'Vui lòng chọn phương thức giao hàng'
    }
  }

  return Object.keys(errors.value).length === 0
}

const openConfirmModal = () => {
  // Validate minimal required fields before opening modal
  if (!validateForm()) {
    showToast('error', 'Vui lòng kiểm tra lại thông tin', 'bi-exclamation-circle-fill')
    return
  }
  if (selectedItems.value.length === 0) {
    showToast('error', 'Giỏ hàng trống', 'bi-exclamation-circle-fill')
    return
  }
  confirmModal.value.show = true
}

const closeConfirmModal = () => {
  if (confirmModal.value.loading) return
  confirmModal.value.show = false
}

const confirmPlaceOrder = async () => {
  confirmModal.value.loading = true
  try {
    await placeOrder()
    confirmModal.value.show = false
  } finally {
    confirmModal.value.loading = false
  }
}

const placeOrder = async () => {
  if (!validateForm()) {
    showToast('error', 'Vui lòng kiểm tra lại thông tin', 'bi-exclamation-circle-fill')
    return
  }

  if (selectedItems.value.length === 0) {
    showToast('error', 'Giỏ hàng trống', 'bi-exclamation-circle-fill')
    return
  }

  try {
    // Prepare order data
    const selectedDelivery = orderForm.value.deliveryType === 'pickup' ? null : deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)

    // Get address from selected address if logged in, otherwise use form input
    let finalAddress = orderForm.value.address || ''
    let finalProvince = ''
    let finalDistrict = ''

    if (orderForm.value.deliveryType === 'delivery') {
      if (isLoggedIn.value && selectedAddressId.value) {
        const selectedAddress = userAddresses.value.find(a => a.id === selectedAddressId.value)
        if (selectedAddress) {
          // Use address from selected address
          const addressParts = []
          if (selectedAddress.diaChiChiTiet) addressParts.push(selectedAddress.diaChiChiTiet)
          if (selectedAddress.phuongXa) addressParts.push(selectedAddress.phuongXa)
          finalAddress = addressParts.join(', ') || selectedAddress.address || ''
          finalProvince = selectedAddress.tinhThanhPho || ''
          finalDistrict = selectedAddress.quanHuyen || ''
        }
      } else {
        // Use manual input
        finalAddress = orderForm.value.address || ''
        finalProvince = provinces.value.find(p => p.ProvinceID === orderForm.value.province)?.ProvinceName || ''
        finalDistrict = districts.value.find(d => d.DistrictID === orderForm.value.district)?.DistrictName || ''
      }
    }

    const orderData = {
      // Customer information
      tenKhachHang: orderForm.value.fullName,
      soDienThoai: orderForm.value.phone,
      email: orderForm.value.email,
      diaChi: orderForm.value.deliveryType === 'pickup' ? 'Tại cửa hàng' : finalAddress,
      tinhThanh: orderForm.value.deliveryType === 'pickup' ? '' : finalProvince,
      quanHuyen: orderForm.value.deliveryType === 'pickup' ? '' : finalDistrict,

      // Order details
      phuongThucGiaoHang: orderForm.value.deliveryType === 'pickup' ? 'pickup' : (orderForm.value.deliveryMethod || 'standard'),
      phuongThucThanhToan: orderForm.value.paymentMethod,
      ghiChu: orderForm.value.note,

      // Pricing
      tongTien: getTotalPrice(),
      phiVanChuyen: shippingPrice.value,

      // Items - Tạo chi tiết riêng biệt cho từng sản phẩm
      chiTietDonHang: selectedItems.value.flatMap(item => {
        // Tạo một mảng chi tiết cho mỗi sản phẩm (số lượng = quantity)
        const details = []
        for (let i = 0; i < item.quantity; i++) {
          details.push({
            chiTietSanPhamId: item.chiTietSanPhamId,
            soLuong: 1, // Mỗi dòng chỉ có 1 sản phẩm
            gia: item.gia,
            thanhTien: item.gia // Thành tiền = giá (vì soLuong = 1)
          })
        }
        return details
      }),

      // Status
      trangThai: 'CHO_XAC_NHAN',

      // Timestamps
      ngayTao: new Date().toISOString(),
      ngayCapNhat: new Date().toISOString()
    }

    console.log('Sending order data:', orderData)

    // Prepare request payload
    const requestPayload = {
      // Customer information
      tenKhachHang: orderData.tenKhachHang,
      soDienThoai: orderData.soDienThoai,
      email: orderData.email || '', // Ensure email is sent even if empty
      diaChi: orderData.diaChi,
      tinhThanh: orderData.tinhThanh,
      quanHuyen: orderData.quanHuyen,

      // Order details
      phuongThucGiaoHang: orderData.phuongThucGiaoHang,
      phuongThucThanhToan: orderData.phuongThucThanhToan,
      ghiChu: orderData.ghiChu || '',
      loaiHoaDon: 'ONLINE', // Đảm bảo loại đơn hàng là ONLINE

      // Pricing
      tongTien: orderData.tongTien,
      tongTienSauGiam: (itemsSubtotal.value + shippingPrice.value - discountAmount.value),
      phiVanChuyen: shippingPrice.value,
      phieuGiamGiaId: selectedCouponId.value || null,

      // Items
      chiTietDonHang: orderData.chiTietDonHang,

      // Status
      trangThai: 'CHO_XAC_NHAN',

      // Timestamps
      ngayTao: new Date().toISOString(),
      ngayCapNhat: new Date().toISOString()
    }

    // Add customer ID if logged in
    if (isLoggedIn.value && userInfo.value?.id) {
      requestPayload.idKhachHang = userInfo.value.id
      console.log('✅ Adding customer ID to order:', userInfo.value.id)
    }

    // Send order to backend API (dedicated online order API)
    const response = await fetch(`${API_BASE_URL}/api/hoa-don/online-order`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestPayload)
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ Server error response:', errorText)
      throw new Error(`HTTP error! status: ${response.status} - ${errorText}`)
    }

    // Check if response has content
    const contentType = response.headers.get('content-type')
    const responseText = await response.text()

    let result = {}
    if (responseText && responseText.trim()) {
      try {
        result = JSON.parse(responseText)
      } catch (e) {
        console.warn('⚠️ Response is not JSON, treating as empty:', responseText)
        // If response is not JSON but status is OK, assume success
        result = { success: true, message: responseText || 'Order created successfully' }
      }
    } else {
      console.warn('⚠️ Empty response, assuming success')
      result = { success: true }
    }

    const orderId = result.maHoaDon || result.id || result.maDonHang || result.orderId
    console.log('✅ Order created successfully:', result)
    console.log('📋 Extracted orderId:', orderId)
    if (!orderId) {
      console.warn('⚠️ No order ID found in response. Full result:', JSON.stringify(result, null, 2))
    }

    // Show success message and clear form
    showOrderSuccess(orderId)

    // Remove only purchased items from cart
    try {
      console.log('🛒 Debug - cartStore.items before:', cartStore.items)
      console.log('🛒 Debug - purchasedItems:', window.purchasedItems)

      // Remove only the items that were purchased
      if (window.purchasedItems && window.purchasedItems.length > 0) {
        window.purchasedItems.forEach(item => {
          console.log(`🛒 Debug - Removing purchased item:`, item)
          cartStore.removeItem(item.chiTietSanPhamId)
          console.log(`✅ Removed purchased item: ${item.tenSanPham}`)
        })
      } else {
        console.log('⚠️ No purchased items found, clearing entire cart')
        cartStore.clearCart()
      }

      console.log('🛒 Debug - cartStore.items after:', cartStore.items)
      console.log('✅ Purchased items removed from cart')
    } catch (e) {
      console.error('Error removing purchased items from cart:', e)
    }

  } catch (error) {
    console.error('Error placing order:', error)
    showToast('error', 'Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.', 'bi-exclamation-circle-fill')
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

const showOrderSuccess = (orderId) => {
  orderSuccess.value = {
    show: true,
    orderId: orderId
  }

  // Reset form
  orderForm.value = {
    fullName: '',
    phone: '',
    email: '',
    province: '',
    district: '',
    address: '',
    deliveryType: 'delivery',
    deliveryMethod: 'standard',
    paymentMethod: 'cod',
    note: ''
  }

  // Reset coupon
  selectedCoupon.value = ''
  selectedCouponId.value = null
  discountAmount.value = 0

  // Clear saved form data, coupon, and selectedItems from sessionStorage
  try {
    sessionStorage.removeItem('checkoutFormData')
    sessionStorage.removeItem('checkoutSelectedCoupon')
    sessionStorage.removeItem('checkoutSelectedCouponId')
    sessionStorage.removeItem('checkoutSelectedItems')
  } catch (e) {}

  // Reset selected items
  selectedItems.value = []
}

// Online payment flows
const buildOrderPayload = () => {
  return {
    hoaDon: {
      tongTien: itemsSubtotal.value + shippingPrice.value,
      tongTienSauGiam: totalAfterDiscount.value,
      loaiHoaDon: 'ONLINE',
      ghiChu: orderForm.value.note || '',
      tenKhachHang: orderForm.value.fullName,
      soDienThoai: orderForm.value.phone,
      diaChi: orderForm.value.deliveryType === 'pickup' ? 'Tại cửa hàng' : (orderForm.value.address || ''),
      phuongThucThanhToan: orderForm.value.paymentMethod,
      phieuGiamGiaId: selectedCouponId.value || null
    },
    chiTietHoaDon: selectedItems.value.map(item => ({
      sanPhamId: item.chiTietSanPhamId,
      soLuong: item.quantity || 1,
      donGia: item.gia || 0,
      thanhTien: (item.gia || 0) * (item.quantity || 1)
    }))
  }
}

const createVNPayPayment = async () => {
  // Log full form state
  console.log('🔍 createVNPayPayment called')
  console.log('🔍 isFormValid:', isFormValid.value)
  console.log('🔍 Full orderForm.value:', JSON.parse(JSON.stringify(orderForm.value)))
  console.log('🔍 Form data:', {
    fullName: orderForm.value.fullName,
    phone: orderForm.value.phone,
    email: orderForm.value.email,
    address: orderForm.value.address,
    deliveryType: orderForm.value.deliveryType,
    deliveryMethod: orderForm.value.deliveryMethod,
    paymentMethod: orderForm.value.paymentMethod,
    province: orderForm.value.province,
    district: orderForm.value.district,
    selectedItems: selectedItems.value.length
  })

  // Validate form before proceeding
  if (!isFormValid.value) {
    console.warn('⚠️ Form validation failed - check validation logs above')
    showToast('error', 'Vui lòng điền đầy đủ thông tin', 'bi-exclamation-circle-fill')
    return
  }

  try {
    // Build OnlineOrderRequest payload
    const selectedDelivery = orderForm.value.deliveryType === 'pickup' ? null : deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)
    const onlineOrder = {
      tenKhachHang: orderForm.value.fullName,
      soDienThoai: orderForm.value.phone,
      email: orderForm.value.email,
      diaChi: orderForm.value.deliveryType === 'pickup' ? 'Tại cửa hàng' : (orderForm.value.address || ''),
      tinhThanh: orderForm.value.deliveryType === 'pickup' ? '' : (provinces.value.find(p => p.ProvinceID === orderForm.value.province)?.ProvinceName || ''),
      quanHuyen: orderForm.value.deliveryType === 'pickup' ? '' : (districts.value.find(d => d.DistrictID === orderForm.value.district)?.DistrictName || ''),
      phuongThucGiaoHang: orderForm.value.deliveryType === 'pickup' ? 'pickup' : (orderForm.value.deliveryMethod || 'standard'),
      phuongThucThanhToan: 'vnpay',
      ghiChu: orderForm.value.note,
      loaiHoaDon: 'ONLINE',
      tongTien: itemsSubtotal.value + shippingPrice.value,
      tongTienSauGiam: totalAfterDiscount.value,
      phiVanChuyen: shippingPrice.value,
      phieuGiamGiaId: selectedCouponId.value || null,
      chiTietDonHang: selectedItems.value.flatMap(item => {
        const details = []
        for (let i = 0; i < (item.quantity || 1); i++) {
          details.push({ chiTietSanPhamId: item.chiTietSanPhamId, soLuong: 1, gia: item.gia, thanhTien: item.gia })
        }
        return details
      })
    }

    // TH1: Chưa đăng nhập -> idKhachHang = null/undefined (không gửi)
    // TH2: Đã đăng nhập -> gửi idKhachHang từ userInfo
    if (isLoggedIn.value && userInfo.value?.id) {
      onlineOrder.idKhachHang = userInfo.value.id
      console.log('✅ [VNPay] Adding customer ID to order:', userInfo.value.id)
    } else {
      console.log('ℹ️ [VNPay] Customer not logged in, idKhachHang will be null')
    }

    console.log('📦 Order payload:', onlineOrder)
    const payload = { amount: Math.round(totalAfterDiscount.value), orderInfo: 'Thanh toan don hang ONLINE', order: onlineOrder }
    console.log('💳 Payment payload:', payload)

    // Persist purchased items for post-payment cleanup
    try { sessionStorage.setItem('purchasedItems', JSON.stringify(selectedItems.value || [])) } catch (e) {}

    console.log('📡 Calling VNPay API...')
    const { data } = await api.post('/api/payments/vnpay/online-create-with-order', payload)
    console.log('✅ VNPay response:', data)

    if (data && data.paymentUrl) {
      console.log('🔗 Redirecting to VNPay:', data.paymentUrl)
      window.location.href = data.paymentUrl
    } else {
      console.error('❌ No payment URL in response')
      showToast('error', 'Không nhận được link thanh toán từ VNPay', 'bi-exclamation-circle-fill')
    }
  } catch (e) {
    console.error('❌ Create VNPay payment error:', e)
    console.error('Error details:', {
      status: e?.response?.status,
      statusText: e?.response?.statusText,
      data: e?.response?.data,
      message: e?.message
    })

    const errorMessage = e?.response?.data?.message || e?.response?.data?.error || e?.message || 'Không tạo được thanh toán VNPay'
    showToast('error', errorMessage, 'bi-exclamation-circle-fill')
  }
}

const createZaloPayPayment = async () => {
  // Validate form before proceeding
  if (!isFormValid.value) {
    showToast('error', 'Vui lòng điền đầy đủ thông tin', 'bi-exclamation-circle-fill')
    return
  }

  try {
    const payload = {
      amount: Math.round(totalAfterDiscount.value),
      orderInfo: 'Thanh toan don hang ONLINE',
      order: buildOrderPayload()
    }
    const { data } = await api.post('/api/payments/zalopay/create-with-order', payload)
    if (data && data.paymentUrl) {
      window.location.href = data.paymentUrl
    }
  } catch (e) {
    console.error('Create ZaloPay payment error', e)
    showToast('error', 'Không tạo được thanh toán ZaloPay', 'bi-exclamation-circle-fill')
  }
}

// Copy to clipboard function
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('success', 'Đã sao chép vào clipboard!', 'bi-check-circle-fill')
  } catch (error) {
    console.error('Error copying to clipboard:', error)
    showToast('error', 'Không thể sao chép', 'bi-exclamation-circle-fill')
  }
}

// Track order function
const trackOrder = () => {
  router.push({
    path: '/theo-doi-don-hang',
    query: {
      orderId: orderSuccess.value.orderId,
      phone: orderForm.value.phone
    }
  })
}

// Continue shopping function - redirect to shop page
const continueShopping = () => {
  orderSuccess.value.show = false
  router.push('/shop')
}

onMounted(async () => {
  // Load form data from sessionStorage (preserve data when navigating)
  const savedForm = loadFormFromStorage()
  if (savedForm.fullName || savedForm.phone) {
    console.log('📋 Restored form from storage:', savedForm)
    orderForm.value = savedForm
  }

  // Check user login status first
  checkUserLogin()

  // Xử lý query params từ ProductDetailPage (mua ngay)
  if (route.query.buyNow === 'true' && route.query.productId) {
    isBuyNow.value = true
    console.log('🛒 Buy Now mode activated:', {
      productId: route.query.productId,
      variantId: route.query.variantId,
      quantity: route.query.quantity
    })

    // Lấy thông tin sản phẩm từ giỏ hàng (đã được thêm bởi ProductDetailPage)
    const cartItems = cartStore.items
    console.log('🔍 Debug - All cart items:', cartItems)
    console.log('🔍 Debug - Looking for productId:', route.query.productId, 'variantId:', route.query.variantId)

    const buyNowItem = cartItems.find(item => {
      console.log('🔍 Debug - Checking item:', {
        itemChiTietSanPhamId: item.chiTietSanPhamId,
        searchVariantId: parseInt(route.query.variantId),
        variantIdMatch: item.chiTietSanPhamId === parseInt(route.query.variantId)
      })
      // Tìm kiếm bằng chiTietSanPhamId (variantId)
      return item.chiTietSanPhamId === parseInt(route.query.variantId)
    })

    if (buyNowItem) {
      buyNowProduct.value = buyNowItem
      // Chỉ hiển thị sản phẩm vừa mua ngay
      selectedItems.value = [buyNowItem]
      console.log('✅ Buy Now item found:', buyNowItem)
      console.log('🔍 Debug - selectedItems after setting:', selectedItems.value)
      console.log('🔍 Debug - Item structure:', {
        tenSanPham: buyNowItem.tenSanPham,
        gia: buyNowItem.gia,
        quantity: buyNowItem.quantity,
        hinhAnh: buyNowItem.hinhAnh,
        tenRam: buyNowItem.tenRam,
        tenRom: buyNowItem.tenRom,
        tenMauSac: buyNowItem.tenMauSac
      })
    } else {
      console.warn('⚠️ Buy Now item not found in cart, trying to find by variantId only')
      // Fallback: tìm sản phẩm chỉ bằng variantId (chiTietSanPhamId)
      const fallbackItem = cartItems.find(item =>
        item.chiTietSanPhamId === parseInt(route.query.variantId)
      )

      if (fallbackItem) {
        buyNowProduct.value = fallbackItem
        selectedItems.value = [fallbackItem]
        console.log('✅ Buy Now item found by variantId only:', fallbackItem)
      } else {
        console.warn('⚠️ Buy Now item not found in cart at all')
        // Nếu không tìm thấy, chuyển về trang giỏ hàng
        router.push('/cart')
        return
      }
    }
  } else {
    // Load selected items from sessionStorage into local list only
    // First try 'selectedItems' (from CartPage), then 'checkoutSelectedItems' (saved during step switching)
    const selected = sessionStorage.getItem('selectedItems') || sessionStorage.getItem('checkoutSelectedItems')
    if (selected) {
      try {
        selectedItems.value = JSON.parse(selected) || []
        // Store a copy for later use in order processing
        window.purchasedItems = [...selectedItems.value]
        // Only remove 'selectedItems' (from CartPage), keep 'checkoutSelectedItems' for step switching
        sessionStorage.removeItem('selectedItems')
        console.log('📦 Loaded selectedItems from sessionStorage:', selectedItems.value.length, 'items')
      } catch (error) {
        console.error('Error loading selected items:', error)
        // Fallback: load from cartStore if parsing fails
        if (cartStore.items.length > 0) {
          selectedItems.value = [...cartStore.items]
          window.purchasedItems = [...cartStore.items]
        }
      }
    } else {
      // No selectedItems in sessionStorage, load from cartStore
      if (cartStore.items.length > 0) {
        selectedItems.value = [...cartStore.items]
        window.purchasedItems = [...cartStore.items]
        console.log('📦 Loaded selectedItems from cartStore:', selectedItems.value.length, 'items')
      } else {
        // No items in cart, redirect to cart page
        router.push('/cart')
        return
      }
    }
  }

  // Load provinces for address selection
  await loadProvinces()

  // Initialize step from URL (?step=1|2|3)
  const stepParam = parseInt(String(route.query.step || ''), 10)
  if ([1,2,3].includes(stepParam)) {
    currentStep.value = stepParam
  }

  // Load vouchers from backend
  // Nếu có user đăng nhập, lấy customer ID từ userInfo (có thể là id hoặc customerId)
  // Nếu không có, chỉ lấy public vouchers
  try {
    const customerId = userInfo.value?.id || userInfo.value?.customerId || null
    console.log('🔍 Loading vouchers - customerId:', customerId, 'userInfo:', userInfo.value)

    // Gọi API với customerId nếu có
    // Backend sẽ tự động filter và chỉ trả về:
    // - Voucher công khai (riengTu === false)
    // - Voucher riêng tư được gán cho customerId này (từ bảng KhachHangGiamGia)
    const url = customerId ? `/api/phieu-giam-gia?customerId=${customerId}` : '/api/phieu-giam-gia'
    console.log('📡 Calling API:', url)
    const response = await api.get(url)
    console.log('📦 API Response:', response)
    console.log('📦 Response data:', response.data)
    console.log('📦 Response data type:', typeof response.data)
    console.log('📦 Is array:', Array.isArray(response.data))

    const data = response.data
    console.log('✅ Vouchers loaded:', Array.isArray(data) ? data.length : 0, 'vouchers')
    if (Array.isArray(data) && data.length > 0) {
      console.log('📋 First voucher:', data[0])
    }
    // Filter: chỉ giữ lại voucher đang hoạt động (trangThai === 1)
    // - Voucher công khai (riengTu === false): hiển thị cho tất cả
    // - Voucher cá nhân (riengTu === true): chỉ hiển thị nếu được gán cho customerId này (backend đã filter)
    // Lưu ý: Backend đã filter voucher riêng tư chỉ trả về những voucher được gán cho customerId này
    const filteredVouchers = Array.isArray(data) ? data.filter(c => {
      const isActive = isActiveVoucher(c)
      const isPrivate = isPrivateVoucher(c)
      
      // Nếu là voucher riêng tư, phải có customerId (backend đã filter, nhưng double-check)
      if (isPrivate && !customerId) {
        console.warn('⚠️ Private voucher found but no customerId:', c)
        return false
      }
      
      // Hiển thị voucher đang hoạt động (cả công khai và cá nhân được gán cho customer)
      return isActive
    }) : []
    console.log('✅ Filtered vouchers (active only):', filteredVouchers.length, 'vouchers')
    if (filteredVouchers.length > 0) {
      console.log('📋 Filtered vouchers sample:', filteredVouchers[0])
    }
    coupons.value = filteredVouchers

    // After vouchers are loaded, restore selected coupon from sessionStorage
    try {
      const savedCoupon = sessionStorage.getItem('checkoutSelectedCoupon')
      const savedCouponId = sessionStorage.getItem('checkoutSelectedCouponId')
      if (savedCoupon && coupons.value.length > 0) {
        console.log('💳 Restored coupon from storage:', { coupon: savedCoupon, couponId: savedCouponId })
        selectedCoupon.value = savedCoupon
        selectedCouponId.value = savedCouponId ? parseInt(savedCouponId) : null
        // Re-apply coupon to calculate discount
        applyCoupon()
        console.log('✅ Coupon applied, discount:', discountAmount.value)
      }
    } catch (e) {
      console.warn('Failed to load coupon from storage:', e)
    }
  } catch (e) {
    console.error('❌ Failed to load vouchers:', e)
    console.error('Error details:', {
      status: e?.response?.status,
      statusText: e?.response?.statusText,
      data: e?.response?.data,
      message: e?.message,
      config: e?.config
    })
    // 401 là bình thường khi người dùng không đăng nhập (website public)
    // Chỉ log lỗi nếu không phải 401
    if (e && e.response && e.response.status !== 401) {
      console.error('Failed to load vouchers', e)
    }
    coupons.value = []
  }

  // If redirected from payment result with success param -> show success overlay
  if (route.query.success === '1') {
    const orderId = String(route.query.orderId || '')
    // Remove purchased items from cart (post-online-payment)
    try {
      const storedPurchased = sessionStorage.getItem('purchasedItems')
      if (storedPurchased) {
        const items = JSON.parse(storedPurchased)
        if (Array.isArray(items)) {
          items.forEach(it => { if (it && it.chiTietSanPhamId) cartStore.removeItem(it.chiTietSanPhamId) })
        }
      }
    } catch (e) { /* ignore */ }
    try { sessionStorage.removeItem('purchasedItems') } catch (e) {}
    showOrderSuccess(orderId)
  }
})

// Address selection modal for logged-in users
const addressModal = ref({ show: false })
const openAddressModal = () => { addressModal.value.show = true }
const closeAddressModal = () => { addressModal.value.show = false }

// Add address modal state
const showAddAddressModal = ref(false)
const savingNewAddress = ref(false)
const newAddressError = ref('')
const newAddressForm = ref({
  province: '',
  district: '',
  address: '',
  loaiDiaChi: 'Nhà riêng',
  customLoaiDiaChi: '',
  macDinh: false
})

// Open add address modal
const openAddAddressModal = async () => {
  // Check address limit
  if (userAddresses.value.length >= MAX_SAVED_ADDRESSES) {
    showToast('error', 'Bạn đã đạt tối đa 5 địa chỉ giao hàng', 'bi-exclamation-circle-fill')
    return
  }

  // Reset form
  newAddressForm.value = {
    province: '',
    district: '',
    address: '',
    loaiDiaChi: 'Nhà riêng',
    customLoaiDiaChi: '',
    macDinh: userAddresses.value.length === 0 // Set as default if no addresses
  }
  newAddressError.value = ''
  showAddAddressModal.value = true

  // Load provinces if not already loaded
  if (provinces.value.length === 0) {
    await loadProvinces()
  }
}

// Close add address modal
const closeAddAddressModal = () => {
  showAddAddressModal.value = false
  newAddressForm.value = {
    province: '',
    district: '',
    address: '',
    loaiDiaChi: 'Nhà riêng',
    customLoaiDiaChi: '',
    macDinh: false
  }
  newAddressError.value = ''
}

// Handle province change in new address form
const onNewAddressProvinceChange = async () => {
  newAddressForm.value.district = '' // Reset district selection
  await loadDistricts(newAddressForm.value.province)
}

// Handle district change in new address form
const onNewAddressDistrictChange = () => {
  // Can add logic here if needed
}

// Handle address selected from map in new address form
const onNewAddressSelected = async (addressData) => {
  newAddressForm.value.address = addressData.formatted_address || addressData.display_name || ''
  if (addressData.geometry && addressData.geometry.location) {
    // Use the location-updated event to parse province and district
    // This will be handled by onNewLocationUpdated
  }
}

// Handle location updated from map in new address form
const onNewLocationUpdated = async (locationData) => {
  console.log('📍 Location updated in new address form:', locationData)
  
  // Try to match province and district from location data
  if (locationData.province) {
    // Ensure provinces are loaded
    if (provinces.value.length === 0) {
      await loadProvinces()
    }
    
    const province = provinces.value.find(p => {
      const provinceName = p.ProvinceName || ''
      const locationProvince = locationData.province || ''
      return provinceName === locationProvince ||
             provinceName.toLowerCase() === locationProvince.toLowerCase() ||
             provinceName.toLowerCase().includes(locationProvince.toLowerCase()) ||
             locationProvince.toLowerCase().includes(provinceName.toLowerCase())
    })
    
    if (province) {
      console.log('✅ Found province:', province.ProvinceName)
      newAddressForm.value.province = province.ProvinceID
      
      // Load districts for this province
      await loadDistricts(province.ProvinceID)
      
      // Wait a bit for districts to load
      await new Promise(resolve => setTimeout(resolve, 300))
      
      // Try to find district
      if (locationData.district && districts.value.length > 0) {
        const district = districts.value.find(d => {
          const districtName = d.DistrictName || ''
          const locationDistrict = locationData.district || ''
          return districtName === locationDistrict ||
                 districtName.toLowerCase() === locationDistrict.toLowerCase() ||
                 districtName.toLowerCase().includes(locationDistrict.toLowerCase()) ||
                 locationDistrict.toLowerCase().includes(districtName.toLowerCase())
        })
        
        if (district) {
          console.log('✅ Found district:', district.DistrictName)
          newAddressForm.value.district = district.DistrictID
        } else {
          console.log('⚠️ District not found:', locationData.district)
          console.log('Available districts:', districts.value.map(d => d.DistrictName))
        }
      } else {
        console.log('⚠️ No district in locationData or districts not loaded yet')
      }
    } else {
      console.log('⚠️ Province not found:', locationData.province)
      console.log('Available provinces:', provinces.value.map(p => p.ProvinceName))
    }
  }
}

// Save new address
const handleSaveNewAddress = async () => {
  if (!isLoggedIn.value || !userInfo.value?.id) {
    showToast('error', 'Vui lòng đăng nhập để thêm địa chỉ', 'bi-exclamation-circle-fill')
    return
  }

  // Validate form
  if (!newAddressForm.value.province || !newAddressForm.value.district || !newAddressForm.value.address.trim()) {
    newAddressError.value = 'Vui lòng điền đầy đủ thông tin địa chỉ'
    return
  }

  // Validate custom address type if "Khác" is selected
  if (newAddressForm.value.loaiDiaChi === 'Khác' && !newAddressForm.value.customLoaiDiaChi?.trim()) {
    newAddressError.value = 'Vui lòng nhập loại địa chỉ'
    return
  }

  savingNewAddress.value = true
  newAddressError.value = ''

  try {
    const customerId = userInfo.value.id
    const provinceName = provinces.value.find(p => p.ProvinceID === newAddressForm.value.province)?.ProvinceName || ''
    const districtName = districts.value.find(d => d.DistrictID === newAddressForm.value.district)?.DistrictName || ''

    // Prepare DiaChi data
    const diaChiData = {
      diaChiChiTiet: newAddressForm.value.address.trim(),
      tinhThanhPho: provinceName,
      quanHuyen: districtName,
      phuongXa: '', // Can be extracted from address if needed
      maBuuDien: '',
      ghiChu: '',
      trangThai: 1
    }

    // Create DiaChi
    const diaChiResponse = await api.post('/api/dia-chi', diaChiData)
    const diaChiId = diaChiResponse.data.id

    // Determine loaiDiaChi - use custom value if "Khác" is selected
    const loaiDiaChi = newAddressForm.value.loaiDiaChi === 'Khác' 
      ? (newAddressForm.value.customLoaiDiaChi || 'Khác')
      : newAddressForm.value.loaiDiaChi

    // Create UserDiaChi
    const userDiaChiData = {
      idDiaChi: diaChiId,
      idUser: customerId,
      loaiDiaChi: loaiDiaChi,
      macDinh: newAddressForm.value.macDinh,
      trangThai: 1
    }

    const userDiaChiResponse = await api.post('/api/user-dia-chi', userDiaChiData)

    // If setting as default, update other addresses
    if (newAddressForm.value.macDinh) {
      await api.put(`/api/user-dia-chi/${userDiaChiResponse.data.id}/mac-dinh`)
    }

    showToast('success', 'Thêm địa chỉ thành công!', 'bi-check-circle-fill')

    // Reload addresses
    await loadUserAddresses()

    // Select the new address
    if (userDiaChiResponse.data?.id) {
      selectedAddressId.value = userDiaChiResponse.data.id
      const newAddress = userAddresses.value.find(a => a.id === userDiaChiResponse.data.id)
      if (newAddress) {
        await selectUserAddress(newAddress)
      }
    }

    // Close modal
    closeAddAddressModal()

  } catch (error) {
    console.error('Error saving new address:', error)
    newAddressError.value = error.response?.data?.message || 'Có lỗi xảy ra khi lưu địa chỉ. Vui lòng thử lại.'
  } finally {
    savingNewAddress.value = false
  }
}
</script>

<style scoped>
/* Online pay block */
.online-pay-actions { margin-top: 12px; padding: 12px; border: 1px dashed #ffd7c2; background: #fff7f2; border-radius: 10px; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.online-pay-actions .pay-summary { color: #333; }
.online-pay-actions .pay-summary strong { color: #FF6B35; }
.online-pay-actions .pay-buttons { display: flex; gap: 8px; }
/* Quantity controls */
.qty-label {
  margin-right: 12px;
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
}

.qty-control {
  display: inline-flex;
  align-items: center;
  gap: 0;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 2px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.qty-control:hover {
  border-color: #ff6b35;
  box-shadow: 0 4px 8px rgba(255, 107, 53, 0.15);
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(255, 107, 53, 0.3);
}

.qty-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(255, 107, 53, 0.4);
}

.qty-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(255, 107, 53, 0.3);
}

.qty-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.qty-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: none;
  background: transparent;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  outline: none;
  border-radius: 8px;
  margin: 0 4px;
}

.qty-input:focus {
  background: #f8f9fa;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.qty-input[type=number] {
  -moz-appearance: textfield;
  appearance: textfield;
}
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
}

.dathang-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* Buy Now Summary */
.buy-now-summary {
  border: 2px solid #ff6b35;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.2);
  position: relative;
}

.buy-now-summary::before {
  content: "⚡ MUA NGAY";
  position: absolute;
  top: -12px;
  left: 20px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.dathang-main {
  flex: 1;
  padding: 3rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #FF5500;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #666;
}

/* User Status Styles */
.user-status,
.guest-status {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-top: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.user-status {
  border-left: 4px solid #28a745;
}

.guest-status {
  border-left: 4px solid #ffc107;
}

.user-info,
.guest-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.user-info i {
  color: #28a745;
  font-size: 1.3rem;
}

.guest-info i {
  color: #ffc107;
  font-size: 1.3rem;
}

.login-benefits,
.login-prompt {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #666;
}

.login-benefits i {
  color: #28a745;
}

.login-prompt i {
  color: #17a2b8;
}

/* Address Selection Styles */
.address-selection {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.subsection-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.subsection-title i {
  color: #FF5500;
}

.address-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.address-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.address-option:hover {
  border-color: #FF5500;
}

.address-option.selected {
  border-color: #FF5500;
  background: rgba(255, 107, 53, 0.05);
}

.address-option input[type="radio"] {
  margin: 0;
  margin-top: 0.25rem;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.address-name {
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.default-badge {
  background: #FF5500;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.address-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.address-phone,
.address-location {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.address-phone i,
.address-location i {
  color: #FF5500;
  font-size: 0.9rem;
}

.address-type-badge {
  display: inline-block;
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 500;
  margin-right: 0.5rem;
}

.address-header .address-type-badge {
  font-size: 1rem;
  padding: 0.5rem 1rem;
  font-weight: 600;
}

.no-address-message {
  padding: 1rem;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
}

.no-address-message p {
  margin: 0;
  color: #856404;
  font-size: 0.9rem;
}

.address-actions {
  display: flex;
  justify-content: center;
}

.btn-add-address {
  background: transparent;
  color: #FF5500;
  border: 2px dashed #FF5500;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.btn-add-address:hover {
  background: #FF5500;
  color: white;
}

.address-limit-message {
  margin-top: 0.75rem;
  color: #DC143C;
  font-size: 0.9rem;
  text-align: center;
}

/* Order Success Overlay */
.order-success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 2rem;
}

.order-success-card {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.success-icon {
  margin-bottom: 1.5rem;
}

.success-icon i {
  font-size: 4rem;
  color: #28a745;
  animation: bounce 1s ease-in-out;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.success-title {
  font-size: 2rem;
  font-weight: 700;
  color: #FF5500;
  margin-bottom: 1rem;
}

.success-message {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.order-id {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 2rem;
  font-size: 1.1rem;
  color: #333;
}

.order-id strong {
  color: #FF5500;
  font-family: monospace;
}

.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-continue,
.btn-track {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  border: none;
  min-width: 150px;
  justify-content: center;
}

.btn-continue {
  background: #6c757d;
  color: white;
}

.btn-continue:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.btn-track {
  background: #FF5500;
  color: white;
}

.btn-track:hover {
  background: #ba2604;
  transform: translateY(-2px);
}

/* Layout */
.dathang-layout {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
}

/* Form Styles */
.order-form {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.form-section {
  margin-bottom: 2.5rem;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: #DC143C;
}

.error-message {
  color: #DC143C;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.address-help {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 8px 12px;
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  border-radius: 6px;
  font-size: 12px;
  color: #1976d2;
}

.address-help i {
  font-size: 14px;
  color: #1976d2;
}

/* Delivery Options */
.delivery-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.delivery-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.delivery-option:hover {
  border-color: #FF5500;
}

.delivery-option input[type="radio"] {
  margin: 0;
}

.delivery-option input[type="radio"]:checked + .option-content {
  color: #FF5500;
}

.delivery-option:has(input:checked) {
  border-color: #FF5500;
  background: rgba(255, 107, 53, 0.05);
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.option-name-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.option-name {
  font-weight: 600;
  font-size: 1rem;
}

.ghn-logo {
  height: 32px;
  width: auto;
  object-fit: contain;
}

.option-price {
  font-weight: 700;
  color: #DC143C;
}

.option-description {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
}

/* Payment Options */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.payment-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  position: relative;
}

.payment-option:hover {
  border-color: #FF5500;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}

.payment-option.selected {
  border-color: #28a745;
  background: rgba(255, 107, 53, 0.05);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
}

.payment-option.popular::before {
  content: "Phổ biến";
  position: absolute;
  top: -8px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.payment-option.unavailable {
  opacity: 0.6;
  cursor: not-allowed;
  background: #f8f9fa;
}

.payment-option input[type="radio"] {
  margin: 0;
  margin-top: 0.25rem;
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.option-main {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.option-main i {
  font-size: 1.5rem;
  color: #FF5500;
}

.option-name {
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.popular-badge {
  background: #28a745;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.option-fee {
  text-align: right;
}

.fee-amount {
  font-weight: 700;
  color: #DC143C;
  font-size: 1rem;
}

.free-text {
  font-weight: 600;
  color: #28a745;
  font-size: 0.9rem;
}

.option-details {
  margin-bottom: 1rem;
}

.option-description {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.75rem;
}

.option-meta {
  display: flex;
  gap: 1.5rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.meta-item i {
  color: #FF5500;
  font-size: 0.9rem;
}

/* Bank Transfer Details */
.bank-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  border: 1px solid #e9ecef;
}

.bank-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

.bank-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.bank-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.bank-label {
  font-weight: 600;
  color: #666;
  min-width: 120px;
  font-size: 0.9rem;
}

.bank-value {
  font-weight: 600;
  color: #333;
  font-family: monospace;
  font-size: 0.9rem;
  flex: 1;
}

.copy-btn {
  background: #FF5500;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background 0.2s;
}

.copy-btn:hover {
  background: #DC143C;
}

.bank-note {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #e3f2fd;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #1976d2;
}

.bank-note i {
  color: #1976d2;
}

/* MoMo QR Code */
.momo-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  border: 1px solid #e9ecef;
  text-align: center;
}

.momo-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.qr-code {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.qr-note {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
}

/* Payment Security */
.payment-security {
  background: #e8f5e8;
  border: 1px solid #c3e6c3;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.payment-security i {
  color: #28a745;
  font-size: 1.5rem;
  margin-top: 0.25rem;
}

.security-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #155724;
  margin-bottom: 0.5rem;
}

.security-content p {
  font-size: 0.9rem;
  color: #155724;
  margin: 0;
  line-height: 1.4;
}

/* Order Summary */
.order-summary {
  background: linear-gradient(180deg,#ffffff 0%, #fffaf5 100%);
  border: 1px solid #f0e7df;
  border-radius: 16px;
  padding: 1.25rem 1.25rem 1.5rem;
  box-shadow: 0 8px 24px rgba(0,0,0,.06);
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.summary-title {
  font-size: 1.2rem;
  font-weight: 800;
  margin: 0 0 1rem 0;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}
.summary-title::before {
  content: "\f46a";
  font-family: bootstrap-icons!important;
  color: #ff6b35;
}

.order-items {
  margin-bottom: 1.5rem;
}

.order-item {
  display: flex;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px dashed #e9e3dd;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.item-specs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 0.25rem;
}

.item-specs span {
  font-size: 0.75rem;
  padding: 0.125rem 0.375rem;
  background: #f0f0f0;
  border-radius: 4px;
  color: #666;
}

.item-quantity {
  font-size: 0.8rem;
  color: #666;
}

.item-price {
  font-weight: 700;
  color: #DC143C;
  font-size: 0.9rem;
}

.summary-divider { height: 1px; background: linear-gradient(90deg, transparent, #f0e7df, transparent); margin: 1rem 0; }

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #666;
}

.summary-value {
  font-weight: 600;
  color: #333;
}

.summary-row.total { font-size: 1.25rem; font-weight: 900; color: #111827; }
.summary-total { font-size: 1.6rem; color: #FF6B35; }

.btn-place-order {
  width: 100%;
  padding: 1rem;
  background: #FF5500;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  transition: all 0.3s;
}

.btn-place-order:hover:not(:disabled) {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220, 20, 60, 0.3);
}

.btn-place-order:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #28a745;
}

.security-info i {
  font-size: 1.2rem;
}

/* Footer */
.dathang-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
}

/* Toast */
.toast-notification {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
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

/* Stepper */
.checkout-steps { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.step { display: flex; align-items: center; gap: 10px; padding: 10px 12px; background: #fff; border: 2px solid #e0e0e0; border-radius: 12px; min-width: 220px; }
.step.active { border-color: #FF6B35; background: rgba(255,107,53,0.05); }
.step.done { border-color: #28a745; }
.step-index { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; background: #9ca3af; }
.step.active .step-index { background: #FF6B35; }
.step.done .step-index { background: #28a745; }
.step-info { display: flex; flex-direction: column; }
.step-title { font-weight: 700; font-size: 14px; }
.step-sub { font-size: 12px; color: #666; }
.step-connector { flex: 1; height: 2px; background: #e0e0e0; }

/* Step actions */
.step-actions { display: flex; justify-content: space-between; gap: 12px; margin-top: 12px; }
.btn-prev, .btn-next, .btn-inline-prev, .btn-inline-next, .btn-confirm, .btn-cancel {
  padding: 10px 16px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  transition: all .2s ease;
}
.btn-prev, .btn-inline-prev, .btn-cancel {
  background: #6c757d;
  color: #fff;
  box-shadow: 0 2px 6px rgba(108,117,125,.3);
}
.btn-prev:hover, .btn-inline-prev:hover, .btn-cancel:hover { filter: brightness(1.05); transform: translateY(-1px); }
.btn-prev:active, .btn-inline-prev:active, .btn-cancel:active { transform: translateY(0); }

.btn-next, .btn-inline-next, .btn-confirm {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  box-shadow: 0 3px 10px rgba(255,107,53,.35);
}
.btn-next:hover, .btn-inline-next:hover, .btn-confirm:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(255,107,53,.45); }
.btn-next:active, .btn-inline-next:active, .btn-confirm:active { transform: translateY(0); }
.btn-next:disabled, .btn-inline-next:disabled, .btn-confirm:disabled { opacity: .6; cursor: not-allowed; box-shadow: none; }

/* Inline back icon-only button */
.btn-inline-prev {
  background: transparent;
  color: #6b7280;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
}
.btn-inline-prev:hover { color: #111827; filter: none; transform: none; }
.btn-inline-prev i { font-size: 20px; }

/* Delivery type toggle */
.delivery-type { display: flex; gap: 12px; margin: 8px 0 16px; }
.delivery-type .dtype { display: inline-flex; align-items: center; gap: 8px; padding: 8px 12px; border: 2px solid #e0e0e0; border-radius: 10px; cursor: pointer; background: #fff; }
.delivery-type .dtype input { display: none; }
.delivery-type .dtype.active { border-color: #FF6B35; background: rgba(255,107,53,0.05); }
.delivery-type .dtype i { color: #FF6B35; }

/* Inline address action */
.address-actions-inline { margin-top: 8px; }
.btn-select-address { background: #f8f9fa; border: 1px dashed #FF5500; color: #FF5500; padding: 6px 10px; border-radius: 8px; cursor: pointer; }

/* Form sub-steps */
.form-steps { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.fstep { display: flex; align-items: center; gap: 6px; padding: 6px 10px; border: 1px solid #e0e0e0; border-radius: 10px; background: #fff; }
.fstep .idx { width: 20px; height: 20px; border-radius: 50%; background: #9ca3af; color: #fff; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.fstep.active { border-color: #FF6B35; background: rgba(255,107,53,0.05); }
.fstep.active .idx { background: #FF6B35; }
.fstep.done { border-color: #28a745; }
.fstep.done .idx { background: #28a745; }
.fstep-connector { flex: 1; height: 1px; background: #e0e0e0; }

/* Step2 */
.confirm-layout { display: grid; grid-template-columns: 1fr 400px; gap: 20px; }
.confirm-left { background: #fff; border-radius: 12px; padding: 16px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.confirm-right { position: sticky; top: 24px; height: fit-content; }
.confirm-title { margin: 0 0 12px 0; font-size: 20px; font-weight: 700; color: #333; }
.confirm-products { border: 1px solid #eee; border-radius: 12px; margin-bottom: 16px; overflow: hidden; }
.cp-row { display: grid; grid-template-columns: 80px 1fr auto auto; align-items: center; gap: 14px; padding: 14px 16px; border-bottom: 1px solid #f0f0f0; }
.cp-row:last-child { border-bottom: none; }
.cp-image { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; border: 1px solid #eee; }
.cp-details { display: flex; flex-direction: column; gap: 6px; }
.cp-name { font-weight: 700; color: #333; font-size: 16px; }
.cp-specs { font-size: 12px; color: #777; }
.cp-qty { color: #666; min-width: 40px; text-align: right; }
.cp-price { font-weight: 800; color: #FF6B35; min-width: 120px; text-align: right; font-size: 15px; }
.coupon-area { margin: 12px 0; }
.coupon-label { font-weight: 600; display: block; margin-bottom: 8px; }
.coupon-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.voucher-card { display: grid; grid-template-columns: 1fr 120px; background: #fff; border-radius: 8px; overflow: hidden; border: 1px solid #eee; box-shadow: 0 2px 10px rgba(0,0,0,0.06); cursor: pointer; }
.voucher-card .vc-main { padding: 14px 16px; position: relative; }
.vc-badge { position: absolute; top: 0; left: 0; background: #ff5a5f; color: #fff; padding: 6px 10px; font-weight: 800; font-size: 12px; clip-path: polygon(0 0, 100% 0, calc(100% - 16px) 100%, 0 100%); }
.vc-badge span { font-weight: 600; margin-left: 6px; }
.vc-title { font-size: 18px; font-weight: 900; color: #ff5a5f; margin-top: 18px; line-height: 1.2; }
.vc-desc { color: #666; font-size: 12px; margin-top: 6px; line-height: 1.4; }
.vc-code { margin-top: 8px; font-size: 12px; color: #999; }
.voucher-card .vc-right { background: #ff5a5f; color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; min-width: 120px; }
.vc-currency { font-size: 12px; font-weight: 700; }
.vc-value { font-size: 22px; font-weight: 900; line-height: 1; }
.vc-value-label { font-size: 10px; letter-spacing: 1px; text-transform: uppercase; opacity: 0.9; }
.voucher-card.selected { outline: 2px solid #FF6B35; }

/* Color schemes by type */
.voucher-card.percent { --voucher-accent: #ff5a5f; }
.voucher-card.amount { --voucher-accent: #14a38b; }
.coupon-actions { display: flex; gap: 8px; margin-top: 10px; }
.coupon-actions.top { margin-bottom: 10px; }
.coupon-select { flex: 1; padding: 8px; border: 2px solid #e0e0e0; border-radius: 8px; }
.btn-apply { display:none; }
.coupon-display { flex: 1; padding: 10px 12px; border: 2px solid #e0e0e0; border-radius: 8px; background: #f8f9fa; color: #555; font-weight: 600; }
.more-vouchers { margin-top: 12px; }
.btn-more { background: #fff7f2; border: 1px solid #ffd7c2; padding: 10px 14px; border-radius: 10px; cursor: pointer; font-weight: 700; color: #ff6b35; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 2px 8px rgba(255,107,53,0.12); transition: all 0.2s; }
.btn-more:hover { background: #ffece3; border-color: #ffb892; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(255,107,53,0.18); }
.btn-more:active { transform: translateY(0); box-shadow: 0 2px 8px rgba(255,107,53,0.12); }
.coupon-grid.ineligible .voucher-card { opacity: 0.7; }
.vc-hint { margin-top: 6px; font-size: 12px; color: #b45309; }
.coupon-error { color: #dc3545; font-size: 0.9rem; margin-top: 6px; }
.coupon-empty { padding: 16px; text-align: center; color: #666; font-size: 0.9rem; background: #f8f9fa; border-radius: 8px; margin-top: 8px; }
.confirm-summary { background: linear-gradient(180deg, #ffffff 0%, #fff8f4 100%); border: 1px solid #ffe0d2; border-radius: 14px; padding: 16px; box-shadow: 0 6px 18px rgba(0,0,0,0.06); }
.srow { display: flex; justify-content: space-between; margin: 12px 0; align-items: center; }
.srow span:first-child { color: #666; }
.srow span:last-child { font-weight: 700; color: #333; }
.srow.total { font-weight: 900; font-size: 20px; }
.srow.total span:last-child { color: #FF6B35; }
.confirm-summary .order-items.mini { margin-bottom: 8px; }
.confirm-summary .order-item { display: flex; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.confirm-summary .order-item:last-child { border-bottom: 0; }
.confirm-summary .item-image img { width: 40px; height: 40px; border-radius: 6px; object-fit: cover; }
.confirm-summary .item-info { flex: 1; }
.confirm-summary .item-name { font-size: 12px; font-weight: 600; color: #333; margin-bottom: 2px; }
.confirm-summary .item-specs span { font-size: 10px; background: #f1f1f1; padding: 0 4px; border-radius: 4px; margin-right: 4px; }
.confirm-summary .item-quantity { font-size: 12px; color: #777; }
.confirm-summary .item-price { font-size: 12px; font-weight: 700; color: #FF6B35; }
.right-actions { margin-top: 12px; display: flex; justify-content: flex-end; gap: 10px; }

/* Saved address list */
.saved-address-list { display: flex; flex-direction: column; gap: 8px; }
.saved-address-item { display: flex; gap: 10px; padding: 10px; border: 1px solid #eee; border-radius: 8px; background: #fff; cursor: pointer; }
.saved-address-item input { margin-top: 4px; }
.saved-address-body .row1 { display: flex; gap: 8px; align-items: center; }

/* Confirm Modal */
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.confirm-card {
  width: 100%;
  max-width: 680px;
  background: linear-gradient(180deg, #ffffff 0%, #fffaf5 100%);
  border: 1px solid #f0e7df;
  border-radius: 16px;
  box-shadow: 0 30px 60px rgba(0,0,0,0.25);
  overflow: hidden;
}

.confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid #f0e7df;
}

.confirm-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}
.confirm-header h3::before {
  content: "\f26a"; /* bi-bag-check */
  font-family: bootstrap-icons!important;
  color: #ff6b35;
}

.confirm-close {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #666;
}

.confirm-content {
  padding: 18px 22px 8px 22px;
}

.confirm-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  color: #374151;
}

.confirm-row .label { font-weight: 700; color: #6b7280; }
.confirm-row .value { font-weight: 700; color: #111827; }

.confirm-row.total { font-size: 1.25rem; color: #111827; }

.confirm-divider { height: 1px; background: linear-gradient(90deg, transparent, #f0e7df, transparent); margin: 10px 0 6px; }

.confirm-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 22px 22px;
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
  background: #FF5500;
  color: #fff;
  border: none;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
/* Responsive */
@media (max-width: 992px) {
  .dathang-layout {
    grid-template-columns: 1fr;
  }

  .order-summary {
    position: static;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .order-form,
  .order-summary {
    padding: 1.5rem;
  }

  .delivery-option,
  .payment-option {
    padding: 0.75rem;
  }
}

/* Shipping Status Styles */
.shipping-status {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
  padding: 12px;
  border-radius: 6px;
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.shipping-status i {
  animation: spin 1s linear infinite;
}

.shipping-error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 12px;
  border-radius: 6px;
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.shipping-info {
  background: #e3f2fd;
  border-left: 3px solid #2196f3;
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 4px;
}

.shipping-details {
  font-size: 14px;
  color: #1976d2;
  display: flex;
  align-items: center;
  gap: 6px;
}

.shipping-details i {
  font-size: 16px;
}

.shipping-details .ghn-logo {
  height: 20px;
  width: auto;
  object-fit: contain;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Address form loading states */
.form-select:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
  opacity: 0.6;
}

.form-select:disabled::after {
  content: "Đang tải...";
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #6c757d;
}
</style>
