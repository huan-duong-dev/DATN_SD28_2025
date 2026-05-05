<template>
  <div class="policies-page">
    <!-- Header Layout -->
    <HeaderLayout />

    <!-- Policies Navigation -->
    <section class="policies-nav-section">
      <div class="container">
        <div class="policies-nav">
          <button
            v-for="policy in policies"
            :key="policy.id"
            @click="scrollToPolicy(policy.id)"
            :class="['nav-btn', { active: activePolicy === policy.id }]"
          >
            <i :class="policy.icon"></i>
            <span>{{ policy.title }}</span>
          </button>
        </div>
      </div>
    </section>

    <!-- Policies Content -->
    <section class="policies-content-section">
      <div class="container">
        <!-- Chính sách mua hàng -->
        <div :id="policies[0].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[0].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[0].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-check-circle"></i> Quy trình đặt hàng</h3>
              <ul>
                <li>Khách hàng có thể đặt hàng trực tuyến qua website hoặc gọi điện đến hotline: <strong>0919 965 342</strong></li>
                <li>Sau khi đặt hàng thành công, chúng tôi sẽ gửi email xác nhận đơn hàng trong vòng 30 phút</li>
                <li>Khách hàng vui lòng kiểm tra thông tin đơn hàng và thông tin giao hàng trước khi xác nhận</li>
                <li>Đơn hàng sẽ được xử lý trong vòng 24 giờ kể từ khi xác nhận thanh toán</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-currency-dollar"></i> Giá cả và thanh toán</h3>
              <ul>
                <li>Tất cả giá sản phẩm trên website đã bao gồm VAT (nếu có)</li>
                <li>Giá sản phẩm có thể thay đổi theo thời gian, giá tại thời điểm thanh toán là giá áp dụng</li>
                <li>Chúng tôi chấp nhận thanh toán qua: ZaloPay, VNPAY, Thẻ ATM, Visa, MasterCard, và tiền mặt khi nhận hàng</li>
                <li>Đối với đơn hàng trả góp, khách hàng vui lòng liên hệ trực tiếp để được tư vấn</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-shield-check"></i> Cam kết chất lượng</h3>
              <ul>
                <li>Tất cả sản phẩm đều là hàng chính hãng, có đầy đủ tem bảo hành và hóa đơn</li>
                <li>Sản phẩm được kiểm tra kỹ lưỡng trước khi giao hàng</li>
                <li>Cam kết giá tốt nhất thị trường, nếu phát hiện giá rẻ hơn ở nơi khác, chúng tôi sẽ hoàn tiền chênh lệch</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Chính sách đổi trả -->
        <div :id="policies[1].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[1].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[1].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-clock-history"></i> Thời gian đổi trả</h3>
              <ul>
                <li>Khách hàng có quyền đổi/trả hàng trong vòng <strong>7 ngày</strong> kể từ ngày nhận hàng</li>
                <li>Đối với sản phẩm lỗi do nhà sản xuất: đổi trả miễn phí trong vòng <strong>30 ngày</strong></li>
                <li>Thời gian xử lý đổi trả: <strong>3-5 ngày làm việc</strong> sau khi nhận được hàng</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-check2-square"></i> Điều kiện đổi trả</h3>
              <ul>
                <li>Sản phẩm còn nguyên vẹn, chưa sử dụng, còn đầy đủ phụ kiện và hộp đựng</li>
                <li>Còn tem bảo hành, tem niêm phong (nếu có) và hóa đơn mua hàng</li>
                <li>Sản phẩm không bị trầy xước, hư hỏng do tác động bên ngoài</li>
                <li>Không áp dụng đổi trả cho các sản phẩm đã kích hoạt bảo hành hoặc đã sử dụng dịch vụ</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-arrow-repeat"></i> Quy trình đổi trả</h3>
              <ul>
                <li>Khách hàng liên hệ hotline <strong>0919 965 342</strong> hoặc email <strong>phonixstore68@gmail.com</strong></li>
                <li>Chúng tôi sẽ kiểm tra và xác nhận yêu cầu đổi trả trong vòng 24 giờ</li>
                <li>Khách hàng gửi hàng về địa chỉ: <strong>Số 49, ngách 230/55, Đồng Me, Mễ Trì Thượng, Nam Từ Liêm, Hà Nội</strong></li>
                <li>Sau khi kiểm tra, chúng tôi sẽ tiến hành đổi trả hoặc hoàn tiền</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-cash-coin"></i> Chi phí đổi trả</h3>
              <ul>
                <li>Đổi trả do lỗi sản phẩm: <strong>Miễn phí</strong></li>
                <li>Đổi trả do khách hàng không hài lòng: Khách hàng chịu phí vận chuyển</li>
                <li>Hoàn tiền sẽ được thực hiện qua phương thức thanh toán ban đầu</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Chính sách vận chuyển -->
        <div :id="policies[2].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[2].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[2].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-truck"></i> Phạm vi giao hàng</h3>
              <ul>
                <li>Giao hàng toàn quốc, miễn phí vận chuyển cho đơn hàng từ <strong>500.000đ</strong> trở lên</li>
                <li>Đơn hàng dưới 500.000đ: phí vận chuyển từ <strong>30.000đ - 50.000đ</strong> tùy khu vực</li>
                <li>Giao hàng nội thành Hà Nội: <strong>Miễn phí</strong> cho mọi đơn hàng</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-clock"></i> Thời gian giao hàng</h3>
              <ul>
                <li>Nội thành Hà Nội: <strong>1-2 ngày</strong> làm việc</li>
                <li>Các tỉnh/thành phố khác: <strong>3-5 ngày</strong> làm việc</li>
                <li>Vùng sâu, vùng xa: <strong>5-7 ngày</strong> làm việc</li>
                <li>Thời gian giao hàng có thể thay đổi trong các dịp lễ, Tết hoặc do thời tiết</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-box-seam"></i> Đóng gói và bảo quản</h3>
              <ul>
                <li>Tất cả sản phẩm được đóng gói cẩn thận, đảm bảo an toàn trong quá trình vận chuyển</li>
                <li>Sử dụng hộp chuyên dụng và vật liệu chống sốc</li>
                <li>Khách hàng vui lòng kiểm tra hàng hóa trước khi ký nhận</li>
                <li>Nếu phát hiện hư hỏng do vận chuyển, vui lòng từ chối nhận hàng và liên hệ ngay với chúng tôi</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-geo-alt"></i> Địa điểm giao hàng</h3>
              <ul>
                <li>Giao hàng tận nơi theo địa chỉ khách hàng cung cấp</li>
                <li>Khách hàng có thể yêu cầu giao hàng tại văn phòng, công ty hoặc địa điểm khác</li>
                <li>Vui lòng cung cấp địa chỉ chính xác và số điện thoại liên hệ để đảm bảo giao hàng thành công</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Chính sách bảo mật -->
        <div :id="policies[3].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[3].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[3].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-shield-lock"></i> Bảo mật thông tin cá nhân</h3>
              <ul>
                <li>Chúng tôi cam kết bảo mật tuyệt đối thông tin cá nhân của khách hàng</li>
                <li>Thông tin chỉ được sử dụng cho mục đích xử lý đơn hàng và cải thiện dịch vụ</li>
                <li>Không chia sẻ, bán hoặc tiết lộ thông tin cho bên thứ ba mà không có sự đồng ý của khách hàng</li>
                <li>Áp dụng công nghệ mã hóa SSL để bảo vệ thông tin thanh toán</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-credit-card"></i> Bảo mật thanh toán</h3>
              <ul>
                <li>Tất cả giao dịch thanh toán được xử lý qua các cổng thanh toán uy tín (ZaloPay, VNPAY)</li>
                <li>Thông tin thẻ tín dụng được mã hóa và không được lưu trữ trên hệ thống của chúng tôi</li>
                <li>Tuân thủ các tiêu chuẩn bảo mật quốc tế PCI DSS</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-cookie"></i> Cookie và công nghệ theo dõi</h3>
              <ul>
                <li>Website sử dụng cookie để cải thiện trải nghiệm người dùng</li>
                <li>Khách hàng có thể tắt cookie trong cài đặt trình duyệt</li>
                <li>Chúng tôi không sử dụng cookie để thu thập thông tin cá nhân nhạy cảm</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Cam kết cửa hàng -->
        <div :id="policies[4].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[4].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[4].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-award"></i> Cam kết về sản phẩm</h3>
              <ul>
                <li><strong>100% hàng chính hãng:</strong> Tất cả sản phẩm đều có nguồn gốc rõ ràng, tem bảo hành chính hãng</li>
                <li><strong>Giá tốt nhất:</strong> Cam kết giá tốt nhất thị trường, hoàn tiền chênh lệch nếu tìm thấy giá rẻ hơn</li>
                <li><strong>Chất lượng đảm bảo:</strong> Sản phẩm được kiểm tra kỹ lưỡng trước khi giao hàng</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-people"></i> Cam kết về dịch vụ</h3>
              <ul>
                <li><strong>Tư vấn chuyên nghiệp:</strong> Đội ngũ nhân viên được đào tạo bài bản, tư vấn nhiệt tình</li>
                <li><strong>Giao hàng nhanh:</strong> Cam kết giao hàng đúng thời gian đã hẹn</li>
                <li><strong>Hỗ trợ 24/7:</strong> Hotline và email luôn sẵn sàng hỗ trợ khách hàng</li>
                <li><strong>Bảo hành chính hãng:</strong> Tất cả sản phẩm đều có chế độ bảo hành từ nhà sản xuất</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-heart"></i> Cam kết về khách hàng</h3>
              <ul>
                <li>Đặt khách hàng làm trung tâm, luôn lắng nghe và cải thiện dịch vụ</li>
                <li>Xử lý khiếu nại nhanh chóng, công bằng và minh bạch</li>
                <li>Chính sách đổi trả linh hoạt, tạo điều kiện tối đa cho khách hàng</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Bảo hành -->
        <div :id="policies[5].id" class="policy-section">
          <div class="policy-header">
            <div class="policy-icon">
              <i :class="policies[5].icon"></i>
            </div>
            <h2 class="policy-title">{{ policies[5].title }}</h2>
          </div>
          <div class="policy-content">
            <div class="policy-item">
              <h3><i class="bi bi-calendar-check"></i> Thời gian bảo hành</h3>
              <ul>
                <li>Thời gian bảo hành theo quy định của nhà sản xuất (thường từ 12-24 tháng)</li>
                <li>Bảo hành được tính từ ngày mua hàng, có hóa đơn mua hàng</li>
                <li>Một số sản phẩm có thể có chế độ bảo hành mở rộng (có phí)</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-check-circle"></i> Điều kiện bảo hành</h3>
              <ul>
                <li>Sản phẩm còn trong thời hạn bảo hành</li>
                <li>Còn tem bảo hành và hóa đơn mua hàng</li>
                <li>Lỗi do nhà sản xuất, không phải do người dùng</li>
                <li>Sản phẩm không bị hư hỏng do va đập, rơi vỡ, ngấm nước, hoặc sửa chữa không chính hãng</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-tools"></i> Quy trình bảo hành</h3>
              <ul>
                <li>Khách hàng liên hệ hotline <strong>0919 965 342</strong> hoặc mang sản phẩm đến cửa hàng</li>
                <li>Nhân viên kiểm tra và xác nhận điều kiện bảo hành</li>
                <li>Gửi sản phẩm đến trung tâm bảo hành chính hãng</li>
                <li>Thời gian xử lý: <strong>7-15 ngày</strong> làm việc tùy theo tình trạng sản phẩm</li>
                <li>Khách hàng sẽ được thông báo khi sản phẩm đã được sửa chữa xong</li>
              </ul>
            </div>

            <div class="policy-item">
              <h3><i class="bi bi-x-circle"></i> Trường hợp không được bảo hành</h3>
              <ul>
                <li>Sản phẩm hết thời hạn bảo hành</li>
                <li>Mất tem bảo hành hoặc hóa đơn</li>
                <li>Hư hỏng do người dùng: rơi vỡ, ngấm nước, cháy nổ, sửa chữa không chính hãng</li>
                <li>Sản phẩm bị thay đổi, can thiệp phần cứng/phần mềm không được phép</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact CTA Section -->
    <section class="contact-cta-section">
      <div class="container">
        <div class="cta-content">
          <h2>Có thắc mắc về chính sách?</h2>
          <p>Đội ngũ hỗ trợ của chúng tôi luôn sẵn sàng giải đáp mọi thắc mắc của bạn</p>
          <div class="cta-buttons">
            <a href="tel:0919965342" class="cta-btn primary">
              <i class="bi bi-telephone-fill"></i>
              Gọi ngay: 0919 965 342
            </a>
            <router-link to="/lien-he" class="cta-btn secondary">
              <i class="bi bi-envelope-fill"></i>
              Liên hệ qua form
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer Layout -->
    <FooterLayout />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'

const activePolicy = ref('mua-hang')

const policies = ref([
  {
    id: 'mua-hang',
    title: 'Chính Sách Mua Hàng',
    icon: 'bi bi-file-text'
  },
  {
    id: 'doi-tra',
    title: 'Chính Sách Đổi Trả',
    icon: 'bi bi-arrow-repeat'
  },
  {
    id: 'van-chuyen',
    title: 'Chính Sách Vận Chuyển',
    icon: 'bi bi-truck'
  },
  {
    id: 'bao-mat',
    title: 'Chính Sách Bảo Mật',
    icon: 'bi bi-lock'
  },
  {
    id: 'cam-ket',
    title: 'Cam Kết Cửa Hàng',
    icon: 'bi bi-award'
  },
  {
    id: 'bao-hanh',
    title: 'Bảo Hành',
    icon: 'bi bi-shield-exclamation'
  }
])

const scrollToPolicy = (policyId) => {
  activePolicy.value = policyId
  const element = document.getElementById(policyId)
  if (element) {
    const offset = 200 // Offset for fixed header
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

const handleScroll = () => {
  const sections = policies.value.map(p => document.getElementById(p.id))
  const scrollPosition = window.scrollY + 250

  sections.forEach((section, index) => {
    if (section) {
      const sectionTop = section.offsetTop
      const sectionBottom = sectionTop + section.offsetHeight

      if (scrollPosition >= sectionTop && scrollPosition < sectionBottom) {
        activePolicy.value = policies.value[index].id
      }
    }
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  // Handle hash in URL (when coming from footer links)
  const hash = window.location.hash.replace('#', '')
  if (hash && policies.value.find(p => p.id === hash)) {
    setTimeout(() => {
      scrollToPolicy(hash)
    }, 300)
  } else {
    // Check initial position
    handleScroll()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.policies-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Policies Navigation */
.policies-nav-section {
  background: white;
  padding: 2rem 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 150px;
  z-index: 100;
}

.policies-nav {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  padding: 0.5rem 0;
  scrollbar-width: thin;
}

.policies-nav::-webkit-scrollbar {
  height: 4px;
}

.policies-nav::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.policies-nav::-webkit-scrollbar-thumb {
  background: #ee4d2d;
  border-radius: 2px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #f8f9fa;
  border: 2px solid transparent;
  border-radius: 25px;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-size: 0.9rem;
}

.nav-btn:hover {
  background: #ee4d2d;
  color: white;
  transform: translateY(-2px);
}

.nav-btn.active {
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  border-color: #ee4d2d;
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.3);
}

.nav-btn i {
  font-size: 1.1rem;
}

/* Policies Content */
.policies-content-section {
  padding: 4rem 0;
}

.policy-section {
  background: white;
  border-radius: 16px;
  padding: 3rem;
  margin-bottom: 3rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  scroll-margin-top: 200px;
}

.policy-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 3px solid #ee4d2d;
}

.policy-icon {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.3);
}

.policy-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.policy-content {
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.policy-item {
  background: #f8f9fa;
  padding: 2rem;
  border-radius: 12px;
  border-left: 4px solid #ee4d2d;
}

.policy-item h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.policy-item h3 i {
  color: #ee4d2d;
  font-size: 1.5rem;
}

.policy-item ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.policy-item ul li {
  padding-left: 1.5rem;
  position: relative;
  line-height: 1.8;
  color: #555;
}

.policy-item ul li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #ee4d2d;
  font-weight: bold;
  font-size: 1.1rem;
}

.policy-item strong {
  color: #ee4d2d;
  font-weight: 600;
}

/* Contact CTA Section */
.contact-cta-section {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  padding: 4rem 0;
  color: white;
}

.cta-content {
  text-align: center;
  max-width: 700px;
  margin: 0 auto;
}

.cta-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.cta-content p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.cta-btn.primary {
  background: linear-gradient(135deg, #ee4d2d 0%, #ff6b35 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(238, 77, 45, 0.3);
}

.cta-btn.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(238, 77, 45, 0.4);
}

.cta-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.cta-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: white;
}

/* Responsive Design */
@media (max-width: 992px) {
  .policies-nav {
    gap: 0.75rem;
  }

  .nav-btn {
    padding: 0.65rem 1.25rem;
    font-size: 0.85rem;
  }

  .policy-section {
    padding: 2rem;
  }

  .policy-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.25rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .policies-nav-section {
    top: 150px;
  }

  .policies-nav {
    gap: 0.5rem;
  }

  .nav-btn {
    padding: 0.6rem 1rem;
    font-size: 0.8rem;
  }

  .nav-btn span {
    display: none;
  }

  .policy-section {
    padding: 1.5rem;
    margin-bottom: 2rem;
  }

  .policy-title {
    font-size: 1.5rem;
  }

  .policy-item {
    padding: 1.5rem;
  }

  .cta-content h2 {
    font-size: 2rem;
  }

  .cta-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .cta-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .policy-icon {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }

  .policy-title {
    font-size: 1.25rem;
  }

  .policy-item h3 {
    font-size: 1.1rem;
  }
}
</style>

