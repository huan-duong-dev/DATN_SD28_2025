<template>
  <footer class="main-footer">
    <div class="footer-container">
      <!-- Main Footer Content -->
      <div class="footer-main">
        <div class="footer-grid">
          <!-- Company Info Column -->
          <div class="footer-col footer-col-about">
            <div class="footer-logo-wrapper">
              <img src="/niX%20(1).png" alt="PhoniX" class="footer-logo" />
            </div>
            <p class="footer-desc">
              Hệ thống cửa hàng PhoniX chuyên bán lẻ điện thoại di động và phụ kiện chính hãng.
              Cam kết giá tốt nhất thị trường, giao hàng miễn phí toàn quốc.
            </p>
            <div class="contact-info">
              <div class="contact-item">
                <i class="bi bi-geo-alt-fill"></i>
                <span>Số 49, ngách 230/55, Đồng Me, Mễ Trì Thượng, Nam Từ Liêm, Hà Nội</span>
              </div>
              <div class="contact-item">
                <i class="bi bi-telephone-fill"></i>
                <a href="tel:0919965342">0919 965 342</a>
              </div>
              <div class="contact-item">
                <i class="bi bi-envelope-fill"></i>
                <a href="mailto:phonixstore68@gmail.com">phonixstore68@gmail.com</a>
              </div>
              <div class="contact-item">
                <i class="bi bi-clock-fill"></i>
                <span>Thứ 2 - Thứ 6: 8:00 - 20:00<br>Thứ 7 - CN: 9:00 - 18:00</span>
              </div>
            </div>
          </div>

          <!-- Quick Links Column -->
          <div class="footer-col">
            <h4 class="footer-title">
              <i class="bi bi-link-45deg"></i>
              Liên Kết Nhanh
            </h4>
            <ul class="footer-links">
              <li>
                <router-link to="/">
                  <i class="bi bi-house-door"></i>
                  Trang chủ
                </router-link>
              </li>
              <li>
                <router-link to="/shop">
                  <i class="bi bi-grid"></i>
                  Sản phẩm
                </router-link>
              </li>
              <li>
                <router-link to="/tin-tuc">
                  <i class="bi bi-newspaper"></i>
                  Tin tức
                </router-link>
              </li>
              <li>
                <router-link to="/lien-he">
                  <i class="bi bi-envelope"></i>
                  Liên hệ
                </router-link>
              </li>
              <li>
                <router-link to="/cart">
                  <i class="bi bi-cart"></i>
                  Giỏ hàng
                </router-link>
              </li>
            </ul>
          </div>

          <!-- Policies Column -->
          <div class="footer-col">
            <h4 class="footer-title">
              <i class="bi bi-shield-check"></i>
              Chính Sách
            </h4>
            <ul class="footer-links">
              <li><router-link to="/chinh-sach#mua-hang"><i class="bi bi-file-text"></i> Chính sách mua hàng</router-link></li>
              <li><router-link to="/chinh-sach#doi-tra"><i class="bi bi-arrow-repeat"></i> Chính sách đổi trả</router-link></li>
              <li><router-link to="/chinh-sach#van-chuyen"><i class="bi bi-truck"></i> Chính sách vận chuyển</router-link></li>
              <li><router-link to="/chinh-sach#bao-mat"><i class="bi bi-lock"></i> Chính sách bảo mật</router-link></li>
              <li><router-link to="/chinh-sach#cam-ket"><i class="bi bi-award"></i> Cam kết cửa hàng</router-link></li>
              <li><router-link to="/chinh-sach#bao-hanh"><i class="bi bi-shield-exclamation"></i> Bảo hành</router-link></li>
            </ul>
          </div>

          <!-- Support & Social Column -->
          <div class="footer-col">
            <h4 class="footer-title">
              <i class="bi bi-headset"></i>
              Hỗ Trợ & Kết Nối
            </h4>
            <ul class="footer-links">
              <li><a href="#"><i class="bi bi-question-circle"></i> Hướng dẫn mua hàng</a></li>
              <li><a href="#"><i class="bi bi-arrow-left-right"></i> Hướng dẫn đổi trả</a></li>
              <li><a href="#"><i class="bi bi-bank"></i> Hướng dẫn thanh toán</a></li>
              <li><a href="#"><i class="bi bi-credit-card"></i> Hướng dẫn trả góp</a></li>
              <li><a href="#"><i class="bi bi-search"></i> Tra cứu đơn hàng</a></li>
            </ul>

            <!-- Social Media -->
            <div class="social-section">
              <h5 class="social-title">Theo dõi chúng tôi</h5>
              <div class="social-links">
                <a
                  href="https://www.facebook.com/profile.php?id=61582101347218&locale=vi_VN"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="social-link facebook"
                  title="Facebook"
                >
                  <i class="bi bi-facebook"></i>
                </a>
                <a
                  href="#"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="social-link instagram"
                  title="Instagram"
                >
                  <i class="bi bi-instagram"></i>
                </a>
                <a
                  href="#"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="social-link youtube"
                  title="YouTube"
                >
                  <i class="bi bi-youtube"></i>
                </a>
                <a
                  href="#"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="social-link tiktok"
                  title="TikTok"
                >
                  <i class="bi bi-tiktok"></i>
                </a>
                <a
                  href="#"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="social-link zalo"
                  title="Zalo"
                >
                  <i class="bi bi-chat-dots"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Methods Section -->
      <div class="payment-section">
        <h5 class="payment-title">
          <i class="bi bi-credit-card-2-front"></i>
          Phương Thức Thanh Toán
        </h5>
        <div class="payment-methods">
          <div class="payment-item" title="ZaloPay">
            <img
              src="https://upload.wikimedia.org/wikipedia/vi/7/77/ZaloPay_Logo.png"
              alt="ZaloPay"
              class="payment-logo"
              loading="lazy"
              onerror="this.onerror=null; this.src='https://upload.wikimedia.org/wikipedia/vi/thumb/7/7a/ZaloPay_logo.svg/512px-ZaloPay_logo.svg.png'"
            />
            <span>ZaloPay</span>
          </div>
          <div class="payment-item" title="VNPAY">
            <img
              src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSutP9weqAPNNrV0V616bloZn2fwAdAOHqnFQ&s"
              alt="VNPAY"
              class="payment-logo"
              loading="lazy"
              onerror="this.onerror=null; this.src='https://upload.wikimedia.org/wikipedia/vi/thumb/8/8e/VNPAY_logo.svg/512px-VNPAY_logo.svg.png'"
            />
            <span>VNPAY</span>
          </div>
          <div class="payment-item" title="Thẻ ATM">
            <img
              src="https://cdn-icons-png.flaticon.com/512/179/179249.png"
              alt="ATM"
              class="payment-logo"
              loading="lazy"
            />
            <span>ATM</span>
          </div>
          <div class="payment-item" title="Visa">
            <img
              src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/512px-Visa_Inc._logo.svg.png"
              alt="Visa"
              class="payment-logo"
              loading="lazy"
            />
            <span>Visa</span>
          </div>
          <div class="payment-item" title="MasterCard">
            <img
              src="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Mastercard-logo.svg/512px-Mastercard-logo.svg.png"
              alt="MasterCard"
              class="payment-logo"
              loading="lazy"
            />
            <span>MasterCard</span>
          </div>
          <div class="payment-item" title="Tiền mặt">
            <img
              src="https://e7.pngegg.com/pngimages/898/31/png-clipart-dollar-sign-dollar-sign-united-states-dollar-green-dollar-symbol-text-logo-thumbnail.png"
              alt="Tiền mặt"
              class="payment-logo"
              loading="lazy"
              onerror="this.onerror=null; this.src='https://cdn-icons-png.flaticon.com/512/1828/1828543.png'"
            />
            <span>Tiền mặt</span>
          </div>
        </div>
      </div>

      <!-- Footer Bottom -->
      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <div class="copyright">
            <p>&copy; {{ currentYear }} <strong>PhoniX Store</strong>. Tất cả quyền được bảo lưu.</p>
            <p class="design-note">Được thiết kế với <i class="bi bi-heart-fill"></i> bởi PhoniX Team</p>
          </div>
          <div class="footer-badges">
            <div class="badge">
              <i class="bi bi-shield-check"></i>
              <span>Bảo mật</span>
            </div>
            <div class="badge">
              <i class="bi bi-truck"></i>
              <span>Giao hàng nhanh</span>
            </div>
            <div class="badge">
              <i class="bi bi-arrow-repeat"></i>
              <span>Đổi trả dễ dàng</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating Scroll to Top Button -->
    <button
      class="floating-btn scroll-top"
      @click="scrollToTop"
      v-if="showScrollTop"
      title="Lên đầu trang"
    >
      <i class="bi bi-arrow-up"></i>
    </button>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// State
const showScrollTop = ref(false)

// Computed
const currentYear = computed(() => new Date().getFullYear())

// Methods
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Scroll listener
const handleScroll = () => {
  showScrollTop.value = window.scrollY > 500
}

// Lifecycle
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* Footer Main Styles */
.main-footer {
  background: linear-gradient(180deg, #1a1a1a 0%, #000000 100%);
  color: #ffffff;
  padding: 4rem 0 0;
  margin-top: 4rem;
  position: relative;
  overflow: hidden;
}

.main-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ee4d2d 0%, #ff6b35 50%, #ee4d2d 100%);
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Footer Main Content */
.footer-main {
  padding-bottom: 3rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr 2fr;
  gap: 3rem;
  margin-bottom: 2rem;
}

.footer-col {
  display: flex;
  flex-direction: column;
}

.footer-col-about {
  max-width: 100%;
}

/* Logo */
.footer-logo-wrapper {
  margin-bottom: 1.5rem;
}

.footer-logo {
  height: 60px;
  width: auto;
  filter: brightness(0) invert(1);
  transition: transform 0.3s ease;
}

.footer-logo:hover {
  transform: scale(1.05);
}

/* Description */
.footer-desc {
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 2rem;
  font-size: 0.95rem;
}

/* Contact Info */
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  line-height: 1.6;
}

.contact-item i {
  color: #ff6b35;
  font-size: 1.1rem;
  margin-top: 0.2rem;
  flex-shrink: 0;
}

.contact-item a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.contact-item a:hover {
  color: #ff6b35;
}

/* Footer Title */
.footer-title {
  color: #ffffff;
  font-size: 1.125rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.footer-title i {
  color: #ff6b35;
  font-size: 1.25rem;
}

/* Footer Links */
.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  padding: 0.25rem 0;
}

.footer-links a i {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #ff6b35;
  padding-left: 0.5rem;
}

.footer-links a:hover i {
  color: #ff6b35;
}

/* Social Section */
.social-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.social-title {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.social-links {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.social-link {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.social-link:hover {
  transform: translateY(-3px) scale(1.1);
  border-color: currentColor;
}

.social-link.facebook:hover {
  background: #1877f2;
  color: white;
}

.social-link.instagram:hover {
  background: linear-gradient(45deg, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%);
  color: white;
}

.social-link.youtube:hover {
  background: #ff0000;
  color: white;
}

.social-link.tiktok:hover {
  background: #000000;
  color: white;
}

.social-link.zalo:hover {
  background: #0068ff;
  color: white;
}

/* Payment Section */
.payment-section {
  padding: 2.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.payment-title {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-align: center;
  justify-content: center;
}

.payment-title i {
  color: #ff6b35;
}

.payment-methods {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.payment-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  min-width: 90px;
}

.payment-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #ff6b35;
  transform: translateY(-3px);
}

.payment-item span {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.payment-logo {
  width: 80px;
  height: 50px;
  object-fit: contain;
  background: white;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  filter: brightness(1);
}

.payment-item:hover .payment-logo {
  transform: scale(1.1);
  filter: brightness(1.1);
}

/* Footer Bottom */
.footer-bottom {
  padding: 2rem 0;
}

.footer-bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.copyright {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.875rem;
}

.copyright strong {
  color: #ff6b35;
  font-weight: 600;
}

.design-note {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.design-note i {
  color: #ff6b35;
  animation: heartbeat 1.5s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

.footer-badges {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
}

.badge:hover {
  background: rgba(255, 107, 53, 0.1);
  border-color: #ff6b35;
  color: #ff6b35;
}

.badge i {
  color: #ff6b35;
}

/* Floating Button */
.floating-btn {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.4);
  transition: all 0.3s ease;
  z-index: 999;
}

.floating-btn:hover {
  transform: translateY(-5px) scale(1.1);
  box-shadow: 0 6px 20px rgba(238, 77, 45, 0.6);
}

/* Responsive Design */
@media (max-width: 1200px) {
  .footer-grid {
    grid-template-columns: 2fr 1fr 1fr 1.5fr;
    gap: 2rem;
  }
}

@media (max-width: 992px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 2.5rem;
  }

  .footer-col-about {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .footer-container {
    padding: 0 1.5rem;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .footer-col-about {
    grid-column: 1;
  }

  .footer-bottom-content {
    flex-direction: column;
    text-align: center;
  }

  .payment-methods {
    gap: 0.75rem;
  }

  .payment-item {
    min-width: 80px;
    padding: 0.5rem 0.75rem;
  }

  .floating-btn {
    right: 1rem;
    bottom: 1rem;
    width: 45px;
    height: 45px;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .main-footer {
    padding: 3rem 0 0;
  }

  .footer-container {
    padding: 0 1rem;
  }

  .footer-main {
    padding-bottom: 2rem;
  }

  .payment-section {
    padding: 2rem 0;
  }

  .payment-methods {
    gap: 0.5rem;
  }

  .payment-item {
    min-width: 70px;
    padding: 0.5rem;
  }

  .payment-icon {
    width: 40px;
    height: 25px;
    font-size: 0.6rem;
  }

  .footer-badges {
    justify-content: center;
    width: 100%;
  }

  .badge {
    font-size: 0.75rem;
    padding: 0.4rem 0.75rem;
  }
}
</style>
