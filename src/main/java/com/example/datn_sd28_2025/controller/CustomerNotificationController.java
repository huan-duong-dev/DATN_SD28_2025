package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.entity.Notification;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/notifications")
@CrossOrigin(origins = "*")
public class CustomerNotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    /**
     * Lấy customer ID từ authentication context
     */
    private Integer getCustomerIdFromAuth() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                // Không có authentication, user chưa đăng nhập
                return null;
            }
            
            String username = authentication.getName();
            
            // Kiểm tra nếu là anonymous user (khi endpoint là public)
            if ("anonymousUser".equals(username) || !authentication.isAuthenticated() || 
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
                // User chưa đăng nhập, trả về null
                return null;
            }
            
            System.out.println("🔍 getCustomerIdFromAuth: username from token = " + username);
            
            // Tìm customer theo email hoặc taiKhoan
            List<KhachHang> customers = khachHangRepository.findByEmailOrTaiKhoan(username);
            if (customers.isEmpty()) {
                // Thử tìm theo ID nếu username là số
                try {
                    Integer customerId = Integer.parseInt(username);
                    return khachHangRepository.findById(customerId).map(KhachHang::getId).orElse(null);
                } catch (NumberFormatException e) {
                    // Không phải số, không tìm thấy
                    return null;
                }
            }
            
            KhachHang customer = customers.get(0);
            System.out.println("✅ getCustomerIdFromAuth: Found customer ID = " + customer.getId());
            return customer.getId();
        } catch (Exception e) {
            System.err.println("❌ Error in getCustomerIdFromAuth: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách thông báo của customer
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        try {
            Integer customerId = getCustomerIdFromAuth();
            if (customerId == null) {
                // Trả về danh sách rỗng thay vì 401/403 để tránh lỗi trên frontend
                return ResponseEntity.ok(List.of());
            }
            
            List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
            
            // Convert to DTO format
            List<Map<String, Object>> notificationDTOs = notifications.stream()
                .map(n -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", n.getId());
                    dto.put("tieuDe", n.getTieuDe());
                    dto.put("noiDung", n.getNoiDung());
                    dto.put("loai", n.getLoaiThongBao());
                    dto.put("daDoc", n.getTrangThai() == 1);
                    dto.put("thoiGian", n.getNgayTao());
                    dto.put("duLieuId", n.getIdThamChieu());
                    
                    // Lấy trạng thái từ đơn hàng nếu có
                    Integer trangThai = getTrangThaiFromLoai(n.getLoaiThongBao(), n.getIdThamChieu());
                    dto.put("trangThai", trangThai);
                    
                    // Thêm duLieu object nếu có idThamChieu
                    if (n.getIdThamChieu() != null && trangThai != null) {
                        Map<String, Object> duLieu = new HashMap<>();
                        duLieu.put("id", n.getIdThamChieu());
                        duLieu.put("trangThai", trangThai);
                        dto.put("duLieu", duLieu);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu thông báo là đã đọc
     */
    @PutMapping("/{id}/read")
    @Transactional
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Integer customerId = getCustomerIdFromAuth();
            if (customerId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized", "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            int updated = notificationRepository.markAsReadByCustomerId(id, customerId, LocalDateTime.now());
            
            if (updated > 0) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã đánh dấu thông báo là đã đọc"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Thông báo không tồn tại hoặc không thuộc về bạn"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in markAsRead: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    @Transactional
    public ResponseEntity<?> markAllAsRead() {
        try {
            Integer customerId = getCustomerIdFromAuth();
            if (customerId == null) {
                System.err.println("❌ markAllAsRead: customerId is null - Authentication: " + SecurityContextHolder.getContext().getAuthentication());
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized", "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            System.out.println("✅ markAllAsRead: customerId = " + customerId);
            int updated = notificationRepository.markAllAsReadByCustomerId(customerId, LocalDateTime.now());
            System.out.println("✅ markAllAsRead: updated " + updated + " notifications");
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã đánh dấu tất cả thông báo là đã đọc", "updatedCount", updated));
        } catch (Exception e) {
            System.err.println("❌ Error in markAllAsRead: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage(), "details", e.getClass().getSimpleName()));
        }
    }
    
    /**
     * Lấy trạng thái từ loại thông báo và id tham chiếu
     * Nếu là DON_HANG_CAP_NHAT, cần lấy trạng thái từ đơn hàng
     */
    private Integer getTrangThaiFromLoai(String loai, Long idThamChieu) {
        // Nếu là DON_HANG_CAP_NHAT hoặc DON_HANG, lấy trạng thái từ đơn hàng
        if (("DON_HANG_CAP_NHAT".equals(loai) || "DON_HANG".equals(loai)) && idThamChieu != null) {
            try {
                return hoaDonRepository.findById(idThamChieu.intValue())
                    .map(hoaDon -> hoaDon.getTrangThai())
                    .orElse(null);
            } catch (Exception e) {
                // Nếu không tìm thấy hoặc lỗi, trả về null
                return null;
            }
        }
        return null;
    }
}

