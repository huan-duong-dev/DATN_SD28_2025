<template>
  <div class="contact-page">
    <!-- Header Layout -->
    <HeaderLayout />

    <!-- Contact Section -->
    <section id="contact" class="contact-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Liên Hệ Với Chúng Tôi</h2>
          <p class="section-description">Chúng tôi luôn sẵn sàng hỗ trợ và giải đáp mọi thắc mắc của bạn</p>
        </div>

        <div class="contact-content">
          <!-- Contact Info Cards -->
          <div class="contact-info-grid">
            <div class="contact-card">
              <div class="contact-icon">
                <i class="bi bi-geo-alt-fill"></i>
              </div>
              <h3>Địa Chỉ</h3>
              <p>123 Đường ABC, Phường XYZ<br>Quận 1, TP. Hồ Chí Minh</p>
            </div>

            <div class="contact-card">
              <div class="contact-icon">
                <i class="bi bi-telephone-fill"></i>
              </div>
              <h3>Điện Thoại</h3>
              <p>Hotline: <a href="tel:19001234">1900 1234</a><br>Mobile: <a href="tel:0912345678">0912 345 678</a></p>
            </div>

            <div class="contact-card">
              <div class="contact-icon">
                <i class="bi bi-envelope-fill"></i>
              </div>
              <h3>Email</h3>
              <p><a href="mailto:phonixstore68@gmail.com">phonixstore68@gmail.com</a></p>
            </div>

            <div class="contact-card">
              <div class="contact-icon">
                <i class="bi bi-clock-fill"></i>
              </div>
              <h3>Giờ Làm Việc</h3>
              <p>Thứ 2 - Thứ 6: 8:00 - 20:00<br>Thứ 7 - CN: 9:00 - 18:00</p>
            </div>
          </div>

          <!-- Contact Form & Map -->
          <div class="contact-form-map">
            <!-- Contact Form -->
            <div class="contact-form-wrapper">
              <h3 class="form-title">Gửi Tin Nhắn Cho Chúng Tôi</h3>
              <form @submit.prevent="handleSubmit" class="contact-form">
                <div class="form-row">
                  <div class="form-group">
                    <label for="name">Họ và Tên *</label>
                    <input type="text" id="name" v-model="form.name" required placeholder="Nhập họ và tên" />
                  </div>
                  <div class="form-group">
                    <label for="email">Email *</label>
                    <input type="email" id="email" v-model="form.email" required placeholder="Nhập email" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label for="phone">Số Điện Thoại</label>
                    <input type="tel" id="phone" v-model="form.phone" placeholder="Nhập số điện thoại" />
                  </div>
                  <div class="form-group">
                    <label for="subject">Chủ Đề *</label>
                    <input type="text" id="subject" v-model="form.subject" required placeholder="Nhập chủ đề" />
                  </div>
                </div>
                <div class="form-group">
                  <label for="message">Nội Dung Tin Nhắn *</label>
                  <textarea id="message" v-model="form.message" required rows="5" placeholder="Nhập nội dung tin nhắn"></textarea>
                </div>
                <button type="submit" class="submit-btn" :disabled="submitting">
                  <span v-if="submitting">Đang gửi...</span>
                  <span v-else>Gửi Tin Nhắn</span>
                </button>
              </form>
            </div>

            <!-- Map -->
            <div class="map-wrapper">
              <h3 class="map-title">Bản Đồ</h3>
              <div class="map-container">
                <iframe
                  src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.319311915234!2d106.66418731480078!3d10.786840292314785!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752ed2392c44df%3A0xbec201c0c8e0b1c0!2zVHLGsOG7nW5nIMSQ4bqhaSBo4buNYyBDw7RuZyBuZ2jhu4cgVGjDtG5nIFRwLjE!5e0!3m2!1svi!2s!4v1234567890123!5m2!1svi!2s"
                  width="100%"
                  height="100%"
                  style="border:0;"
                  allowfullscreen=""
                  loading="lazy"
                  referrerpolicy="no-referrer-when-downgrade"
                ></iframe>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer Layout -->
    <FooterLayout />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'

const submitting = ref(false)

const form = ref({
  name: '',
  email: '',
  phone: '',
  subject: '',
  message: ''
})

const handleSubmit = async () => {
  submitting.value = true
  // Simulate form submission
  setTimeout(() => {
    alert('Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể.')
    form.value = {
      name: '',
      email: '',
      phone: '',
      subject: '',
      message: ''
    }
    submitting.value = false
  }, 1500)
}
</script>

<style scoped>
.contact-page {
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

/* Contact Section */
.contact-section {
  background: white;
  padding: 4rem 0;
}

.contact-content {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* Contact Info Grid */
.contact-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.contact-card {
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  padding: 2rem;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.contact-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(238, 77, 45, 0.15);
}

.contact-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
}

.contact-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.contact-card p {
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.contact-card a {
  color: #ee4d2d;
  text-decoration: none;
  transition: color 0.3s ease;
}

.contact-card a:hover {
  color: #ff6b35;
  text-decoration: underline;
}

/* Contact Form & Map */
.contact-form-map {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-top: 2rem;
}

.contact-form-wrapper,
.map-wrapper {
  background: #f8f9fa;
  padding: 2.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-title,
.map-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #ee4d2d;
}

.contact-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.form-group input,
.form-group textarea {
  padding: 0.875rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #ee4d2d;
  box-shadow: 0 0 0 3px rgba(238, 77, 45, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.submit-btn {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(238, 77, 45, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.map-container {
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.map-container iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* Social Section */
.social-section {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: 4rem 0;
  color: white;
}

.social-section .section-title {
  color: white;
}

.social-section .section-description {
  color: rgba(255, 255, 255, 0.8);
}

.social-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-top: 2rem;
}

.social-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.social-link i {
  font-size: 1.5rem;
}

.social-link:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.2);
}

.social-link.facebook:hover {
  background: #1877f2;
}

.social-link.instagram:hover {
  background: linear-gradient(45deg, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%);
}

.social-link.youtube:hover {
  background: #ff0000;
}

.social-link.zalo:hover {
  background: #0068ff;
}

.social-link.tiktok:hover {
  background: #000000;
}

/* Responsive Design */
@media (max-width: 992px) {
  .contact-form-map {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .section-title {
    font-size: 2rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .contact-info-grid {
    grid-template-columns: 1fr;
  }

  .social-links {
    flex-direction: column;
    align-items: center;
  }

  .social-link {
    width: 100%;
    max-width: 300px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .contact-form-wrapper,
  .map-wrapper {
    padding: 1.5rem;
  }

  .section-header {
    padding-top: 2rem;
  }
}
</style>

