package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.service.GoogleOAuthService;
import com.example.datn_sd28_2025.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/auth")
@CrossOrigin(origins = "*")
public class CustomerAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Lấy Google OAuth URL để redirect người dùng đến trang đăng nhập Google
     * Hỗ trợ cả Admin (port 5173) và Website (port 5174)
     */
    @GetMapping("/google-url")
    public ResponseEntity<?> getGoogleAuthUrl(@RequestParam(required = false) String redirectUri) {
        try {
            String clientId = googleOAuthService.getClientId();
            
            // Nếu không có redirectUri trong request, sử dụng default từ config
            if (redirectUri == null || redirectUri.trim().isEmpty()) {
                redirectUri = googleOAuthService.getRedirectUri();
            }
            
            // Validate redirect URI (chỉ cho phép localhost với port 5173 hoặc 5174)
            if (!redirectUri.matches("http://localhost:(5173|5174)/google-callback")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Redirect URI không hợp lệ. Chỉ cho phép http://localhost:5173/google-callback hoặc http://localhost:5174/google-callback"));
            }
            
            // URL encode redirect URI để đảm bảo an toàn
            String encodedRedirectUri = java.net.URLEncoder.encode(redirectUri, "UTF-8");
            
            String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + clientId
                    + "&redirect_uri=" + encodedRedirectUri
                    + "&response_type=code"
                    + "&scope=openid%20profile%20email"
                    + "&access_type=offline"
                    + "&prompt=consent";
            
            return ResponseEntity.ok(Map.of("authUrl", googleAuthUrl, "redirectUri", redirectUri));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tạo Google OAuth URL: " + e.getMessage()));
        }
    }

    /**
     * Xử lý Google OAuth callback - nhận code và tạo JWT token
     * Hỗ trợ cả Admin (port 5173) và Website (port 5174)
     */
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String redirectUri = request.get("redirectUri"); // Redirect URI từ frontend
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Authorization code is required"));
            }

            // Nếu không có redirectUri, sử dụng default từ config
            if (redirectUri == null || redirectUri.trim().isEmpty()) {
                redirectUri = googleOAuthService.getRedirectUri();
            }
            
            // Validate redirect URI
            if (!redirectUri.matches("http://localhost:(5173|5174)/google-callback")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Redirect URI không hợp lệ"));
            }

            // Xác thực Google user và lấy thông tin (truyền redirectUri)
            Map<String, Object> authResult = googleOAuthService.authenticateGoogleUser(code, redirectUri);
            
            if (!Boolean.TRUE.equals(authResult.get("success"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", authResult.get("error")));
            }

            // Lấy thông tin khách hàng
            KhachHang customer = (KhachHang) authResult.get("customer");
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "error", "Không thể tạo hoặc tìm thấy tài khoản khách hàng"));
            }

            // Tạo JWT token
            String token = jwtUtil.generateToken(customer.getTaiKhoan() != null ? customer.getTaiKhoan() : customer.getEmail(), "CUSTOMER");

            // Trả về thông tin user và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("user", Map.of(
                    "id", customer.getId(),
                    "email", customer.getEmail() != null ? customer.getEmail() : "",
                    "hoTen", customer.getHoTen() != null ? customer.getHoTen() : "",
                    "taiKhoan", customer.getTaiKhoan() != null ? customer.getTaiKhoan() : "",
                    "soDienThoai", customer.getSoDienThoai() != null ? customer.getSoDienThoai() : "",
                    "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Lỗi khi xử lý đăng nhập Google: " + e.getMessage()));
        }
    }

    /**
     * Đăng nhập khách hàng bằng email và mật khẩu (JWT)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email không được để trống"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Mật khẩu không được để trống"));
            }

            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email không hợp lệ"));
            }

            // Tìm khách hàng theo email
            List<KhachHang> customers = khachHangRepository.findByEmail(email.trim().toLowerCase());

            if (customers == null || customers.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email hoặc mật khẩu không đúng"));
            }

            KhachHang customer = customers.get(0); // Lấy khách hàng đầu tiên

            // Kiểm tra trạng thái tài khoản
            if (customer.getTrangThai() == null || customer.getTrangThai() != 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Tài khoản đã bị khóa"));
            }

            // Kiểm tra mật khẩu
            if (customer.getMatKhau() == null || customer.getMatKhau().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Tài khoản này được đăng nhập bằng Google. Vui lòng sử dụng chức năng đăng nhập Google."));
            }

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(password, customer.getMatKhau())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Tài khoản hoặc mật khẩu không đúng"));
            }

            // Tạo JWT token
            String usernameForToken = customer.getTaiKhoan() != null ? customer.getTaiKhoan() :
                    (customer.getEmail() != null ? customer.getEmail() : String.valueOf(customer.getId()));
            String token = jwtUtil.generateToken(usernameForToken, "CUSTOMER");

            // Trả về thông tin user và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("user", Map.of(
                    "id", customer.getId(),
                    "email", customer.getEmail() != null ? customer.getEmail() : "",
                    "hoTen", customer.getHoTen() != null ? customer.getHoTen() : "",
                    "taiKhoan", customer.getTaiKhoan() != null ? customer.getTaiKhoan() : "",
                    "soDienThoai", customer.getSoDienThoai() != null ? customer.getSoDienThoai() : "",
                    "role", "CUSTOMER"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Lỗi khi xử lý đăng nhập: " + e.getMessage()));
        }
    }

    /**
     * Gửi OTP về email cho chức năng quên mật khẩu (customer)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email không được để trống"));
            }

            // Sử dụng service để gửi OTP
            Map<String, Object> result = googleOAuthService.sendForgotPasswordOTP(email.trim().toLowerCase());
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xác thực OTP (customer)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email không được để trống"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã OTP không được để trống"));
            }
            
            // Sử dụng service để xác thực OTP
            Map<String, Object> result = googleOAuthService.verifyOTP(email.trim().toLowerCase(), otp.trim());
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đặt lại mật khẩu (customer)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");
            
            if (resetToken == null || resetToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Reset token không được để trống"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu mới không được để trống"));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự"));
            }
            
            // Sử dụng service để reset password
            Map<String, Object> result = googleOAuthService.resetPassword(resetToken, newPassword);
            
            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Đăng ký tài khoản mới cho khách hàng
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String hoTen = request.get("hoTen");
            String email = request.get("email");
            String soDienThoai = request.get("soDienThoai");
            String password = request.get("password");
            String confirmPassword = request.get("confirmPassword");
            String gioiTinh = request.get("gioiTinh");
            String ngaySinh = request.get("ngaySinh");

            // Validation
            if (hoTen == null || hoTen.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Họ tên không được để trống"));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email không được để trống"));
            }

            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email không hợp lệ"));
            }

            if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Số điện thoại không được để trống"));
            }

            // Validate phone number format (10 digits, starts with 0)
            if (!soDienThoai.matches("^(0[3|5|7|8|9])+([0-9]{8})$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Mật khẩu không được để trống"));
            }

            if (password.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Mật khẩu phải có ít nhất 6 ký tự"));
            }

            if (confirmPassword == null || !confirmPassword.equals(password)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Mật khẩu xác nhận không khớp"));
            }

            // Validate age (must be at least 18 years old)
            if (ngaySinh != null && !ngaySinh.trim().isEmpty()) {
                try {
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(ngaySinh);
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int age = java.time.Period.between(birthDate, today).getYears();
                    
                    if (age < 18) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("success", false, "error", "Bạn phải đủ 18 tuổi mới được đăng ký tài khoản"));
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "error", "Ngày sinh không hợp lệ"));
                }
            } else {
                // Yêu cầu nhập ngày sinh để kiểm tra tuổi
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Vui lòng nhập ngày sinh để xác minh độ tuổi (phải đủ 18 tuổi)"));
            }

            // Kiểm tra email đã tồn tại chưa
            List<KhachHang> existingByEmail = khachHangRepository.findByEmail(email.trim().toLowerCase());
            if (existingByEmail != null && !existingByEmail.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email đã được sử dụng. Vui lòng sử dụng email khác hoặc đăng nhập."));
            }

            // Kiểm tra số điện thoại đã tồn tại chưa
            List<KhachHang> existingByPhone = khachHangRepository.findBySoDienThoai(soDienThoai.trim());
            if (existingByPhone != null && !existingByPhone.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Số điện thoại đã được sử dụng. Vui lòng sử dụng số điện thoại khác."));
            }

            // Tạo mã khách hàng tự động
            String maKhachHang = generateMaKhachHang();

            // Tạo tài khoản từ email (phần trước @)
            String taiKhoan = email.substring(0, email.indexOf("@"));

            // Mã hóa mật khẩu với {noop} prefix (NoOp - plain text)
            String encodedPassword = ensureNoopPrefix(password);

            // Tạo khách hàng mới
            KhachHang khachHang = new KhachHang();
            khachHang.setMaKhachHang(maKhachHang);
            khachHang.setHoTen(hoTen.trim());
            khachHang.setEmail(email.trim().toLowerCase());
            khachHang.setSoDienThoai(soDienThoai.trim());
            khachHang.setTaiKhoan(taiKhoan);
            khachHang.setMatKhau(encodedPassword);
            khachHang.setTrangThai(1); // Active
            khachHang.setNgayTao(java.time.LocalDateTime.now());
            khachHang.setNgayCapNhat(java.time.LocalDateTime.now());
            khachHang.setNguoiTao("System");
            khachHang.setNguoiCapNhat("System");

            // Set optional fields
            if (gioiTinh != null && !gioiTinh.trim().isEmpty()) {
                khachHang.setGioiTinh(gioiTinh.trim());
            }

            if (ngaySinh != null && !ngaySinh.trim().isEmpty()) {
                try {
                    khachHang.setNgaySinh(java.time.LocalDate.parse(ngaySinh));
                } catch (Exception e) {
                    // Ignore date parsing error
                }
            }

            // Lưu khách hàng
            KhachHang savedKhachHang = khachHangRepository.save(khachHang);

            // Tạo JWT token
            String token = jwtUtil.generateToken(savedKhachHang.getTaiKhoan(), "CUSTOMER");

            // Trả về thông tin user và token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng ký tài khoản thành công!");
            response.put("token", token);
            response.put("user", Map.of(
                    "id", savedKhachHang.getId(),
                    "email", savedKhachHang.getEmail() != null ? savedKhachHang.getEmail() : "",
                    "hoTen", savedKhachHang.getHoTen() != null ? savedKhachHang.getHoTen() : "",
                    "taiKhoan", savedKhachHang.getTaiKhoan() != null ? savedKhachHang.getTaiKhoan() : "",
                    "soDienThoai", savedKhachHang.getSoDienThoai() != null ? savedKhachHang.getSoDienThoai() : "",
                    "role", "CUSTOMER"
            ));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Lỗi khi đăng ký tài khoản: " + e.getMessage()));
        }
    }

    /**
     * Thêm prefix {noop} vào mật khẩu nếu chưa có (NoOp - plain text)
     */
    private String ensureNoopPrefix(String password) {
        if (password == null || password.trim().isEmpty()) {
            return password;
        }
        
        // Nếu đã có prefix {noop}, giữ nguyên
        if (password.startsWith("{noop}")) {
            return password;
        }
        
        // Thêm prefix {noop} cho plain text password
        return "{noop}" + password;
    }

    /**
     * Tạo mã khách hàng tự động
     */
    private String generateMaKhachHang() {
        // Lấy số lượng khách hàng hiện tại
        long count = khachHangRepository.count();
        
        // Lấy danh sách tất cả mã khách hàng hiện có
        Set<String> existingCodes = khachHangRepository.findAll().stream()
                .map(KhachHang::getMaKhachHang)
                .filter(code -> code != null && code.startsWith("KH"))
                .collect(Collectors.toSet());
        
        // Tạo mã: KH + số thứ tự (5 chữ số)
        String maKhachHang;
        int attempts = 0;
        do {
            count++;
            maKhachHang = "KH" + String.format("%05d", count + 1);
            attempts++;
        } while (existingCodes.contains(maKhachHang) && attempts < 100);
        
        return maKhachHang;
    }
}

