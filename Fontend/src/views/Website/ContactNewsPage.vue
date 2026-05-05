<template>
  <div class="contact-news-page">
    <!-- Header Layout -->
    <HeaderLayout />

    <!-- News Section -->
    <section id="news" class="news-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Tin Tức & Sự Kiện</h2>
          <p class="section-description">Cập nhật những tin tức mới nhất về công nghệ và sản phẩm</p>
        </div>

        <div class="news-grid">
          <div v-for="(news, index) in newsList" :key="index" class="news-card">
            <div class="news-image-wrapper">
              <img :src="news.image" :alt="news.title" class="news-image" />
              <div class="news-date-badge">
                <span class="date-day">{{ news.date.day }}</span>
                <span class="date-month">{{ news.date.month }}</span>
              </div>
            </div>
            <div class="news-content">
              <div class="news-category">{{ news.category }}</div>
              <h3 class="news-title">{{ news.title }}</h3>
              <p class="news-excerpt">{{ news.excerpt }}</p>
              <a href="#" class="news-read-more">Đọc thêm <i class="bi bi-arrow-right"></i></a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Social Media Section -->
    <section class="social-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Theo Dõi Chúng Tôi</h2>
          <p class="section-description">Kết nối với chúng tôi trên các mạng xã hội</p>
        </div>
        <div class="social-links">
          <a href="#" class="social-link facebook">
            <i class="bi bi-facebook"></i>
            <span>Facebook</span>
          </a>
          <a href="#" class="social-link instagram">
            <i class="bi bi-instagram"></i>
            <span>Instagram</span>
          </a>
          <a href="#" class="social-link youtube">
            <i class="bi bi-youtube"></i>
            <span>YouTube</span>
          </a>
          <a href="#" class="social-link zalo">
            <i class="bi bi-chat-dots"></i>
            <span>Zalo</span>
          </a>
          <a href="#" class="social-link tiktok">
            <i class="bi bi-tiktok"></i>
            <span>TikTok</span>
          </a>
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

const newsList = ref([
  {
    title: 'Ra Mắt Điện Thoại Mới - Flagship 2024',
    excerpt: 'Khám phá những tính năng đột phá và công nghệ tiên tiến nhất trong dòng sản phẩm mới của chúng tôi.',
    category: 'Sản Phẩm Mới',
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&h=600&fit=crop',
    date: { day: '15', month: 'TH3' }
  },
  {
    title: 'Chương Trình Khuyến Mãi Đặc Biệt Tháng 3',
    excerpt: 'Giảm giá lên đến 30% cho tất cả các sản phẩm điện thoại và phụ kiện. Áp dụng cho khách hàng mới.',
    category: 'Khuyến Mãi',
    image: 'https://images.unsplash.com/photo-1556656793-08538906a9f8?w=800&h=600&fit=crop',
    date: { day: '20', month: 'TH3' }
  },
  {
    title: 'Hướng Dẫn Bảo Quản Điện Thoại Đúng Cách',
    excerpt: 'Những mẹo hay giúp bạn bảo quản và sử dụng điện thoại hiệu quả, kéo dài tuổi thọ sản phẩm.',
    category: 'Mẹo Vặt',
    image: 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=800&h=600&fit=crop',
    date: { day: '25', month: 'TH3' }
  },
  {
    title: 'Công Nghệ AI Mới Trong Điện Thoại',
    excerpt: 'Tìm hiểu về công nghệ trí tuệ nhân tạo đang được tích hợp vào các dòng điện thoại hiện đại.',
    category: 'Công Nghệ',
    image: 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&h=600&fit=crop',
    date: { day: '28', month: 'TH3' }
  },
  {
    title: 'Sự Kiện Trải Nghiệm Sản Phẩm Tại Showroom',
    excerpt: 'Tham gia sự kiện trải nghiệm miễn phí các sản phẩm mới nhất tại showroom của chúng tôi.',
    category: 'Sự Kiện',
    image: 'https://images.unsplash.com/photo-1551434678-e076c223a692?w=800&h=600&fit=crop',
    date: { day: '05', month: 'TH4' }
  },
  {
    title: 'Đánh Giá Chi Tiết: Điện Thoại Flagship 2024',
    excerpt: 'Bài đánh giá toàn diện về hiệu năng, camera, pin và các tính năng nổi bật của flagship mới.',
    category: 'Đánh Giá',
    image: 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800&h=600&fit=crop',
    date: { day: '10', month: 'TH4' }
  }
])

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
.contact-news-page {
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

/* News Section */
.news-section {
  background: #f8f9fa;
  padding: 4rem 0;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.news-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.news-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(238, 77, 45, 0.15);
}

.news-image-wrapper {
  position: relative;
  width: 100%;
  height: 250px;
  overflow: hidden;
}

.news-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.news-card:hover .news-image {
  transform: scale(1.1);
}

.news-date-badge {
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.date-day {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1;
}

.date-month {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-top: 0.25rem;
}

.news-content {
  padding: 2rem;
}

.news-category {
  display: inline-block;
  background: #f0f0f0;
  color: #ee4d2d;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.news-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.news-excerpt {
  color: #666;
  line-height: 1.8;
  margin-bottom: 1.5rem;
}

.news-read-more {
  color: #ee4d2d;
  text-decoration: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.news-read-more:hover {
  color: #ff6b35;
  gap: 0.75rem;
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

  .news-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
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

  .news-grid {
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

