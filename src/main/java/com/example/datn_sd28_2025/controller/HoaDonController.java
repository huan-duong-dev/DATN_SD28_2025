package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.HoaDonTrackingDTO;
import com.example.datn_sd28_2025.dto.HoaDonSearchRequestDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.dto.ChiTietThanhToanDTO;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.service.impl.HoaDonServiceImpl;
import com.example.datn_sd28_2025.service.ImeiService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import com.example.datn_sd28_2025.util.SecurityUtil;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.ChiTietHoaDon;
import com.example.datn_sd28_2025.entity.ChiTietThanhToan;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.TrangThaiTracking;
import com.example.datn_sd28_2025.repository.ChiTietHoaDonRepository;
import com.example.datn_sd28_2025.repository.ChiTietThanhToanRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.TrangThaiTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private ImeiDaBanRepository imeiDaBanRepository;

    @Autowired
    private ImeiService imeiService;

    @Autowired
    private HoaDonServiceImpl hoaDonServiceImpl;

    @Autowired
    private ChiTietThanhToanRepository chiTietThanhToanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private TrangThaiTrackingRepository trangThaiTrackingRepository;

    @GetMapping
    public ResponseEntity<List<HoaDonDTO>> getAll() {
        List<HoaDonDTO> hoaDons = hoaDonService.getAll();
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getById(@PathVariable Integer id) {
        HoaDonDTO hoaDon = hoaDonService.getById(id);
        return hoaDon != null ? ResponseEntity.ok(hoaDon) : ResponseEntity.notFound().build();
    }

    @GetMapping("/ma-hoa-don/{maHoaDon}")
    public ResponseEntity<HoaDonDTO> getByMaHoaDon(@PathVariable String maHoaDon) {
        HoaDonDTO hoaDon = hoaDonService.getByMaHoaDon(maHoaDon);
        return hoaDon != null ? ResponseEntity.ok(hoaDon) : ResponseEntity.notFound().build();
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<HoaDonDTO>> getByKhachHangId(@PathVariable Integer khachHangId) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByKhachHangId(khachHangId);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<HoaDonDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByTrangThai(trangThai);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/loai-hoa-don/{loaiHoaDon}")
    public ResponseEntity<List<HoaDonDTO>> getByLoaiHoaDon(@PathVariable String loaiHoaDon) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByLoaiHoaDon(loaiHoaDon);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchHoaDon(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        try {
            // Support both 'keyword' and 'query' parameters for backward compatibility
            String searchTerm = keyword != null ? keyword : query;
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Search term is required"));
            }
            // Trim the search term to remove any whitespace
            searchTerm = searchTerm.trim();
            Page<HoaDonDTO> hoaDons = hoaDonService.searchHoaDon(searchTerm, pageable);
            return ResponseEntity.ok(hoaDons);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error searching invoices", "message", e.getMessage()));
        }
    }

    @PostMapping("/search-advanced")
    public ResponseEntity<Page<HoaDonDTO>> searchHoaDonAdvanced(@RequestBody HoaDonSearchRequestDTO searchRequest) {
        Page<HoaDonDTO> hoaDons = hoaDonService.searchHoaDonAdvanced(searchRequest);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/tracking/{maHoaDon}")
    public ResponseEntity<?> getTrackingInfo(@PathVariable String maHoaDon) {
        try {
            HoaDonTrackingDTO trackingInfo = hoaDonService.getTrackingInfo(maHoaDon);
            if (trackingInfo != null) {
                return ResponseEntity.ok(trackingInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi lấy thông tin tracking",
                    "message", e.getMessage(),
                    "maHoaDon", maHoaDon
            ));
        }
    }

    @GetMapping("/tracking-by-id/{id}")
    public ResponseEntity<?> getTrackingInfoById(@PathVariable Integer id) {
        try {
            HoaDonDTO hoaDon = hoaDonService.getById(id);
            if (hoaDon != null) {
                HoaDonTrackingDTO trackingInfo = hoaDonService.getTrackingInfo(hoaDon.getMaHoaDon());
                if (trackingInfo != null) {
                    return ResponseEntity.ok(trackingInfo);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi lấy thông tin tracking",
                    "message", e.getMessage(),
                    "id", id
            ));
        }
    }

    // Check order by phone and order code (both required)
    @GetMapping("/check")
    public ResponseEntity<?> checkOrderByPhoneAndCode(@RequestParam String phone, @RequestParam String code) {
        try {
            // Simple normalization: trim and keep digits for phone
            String normalizedPhone = phone == null ? null : phone.replaceAll("[^0-9]", "");
            if (normalizedPhone == null || normalizedPhone.isBlank() || code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Thiếu số điện thoại hoặc mã đơn hàng",
                        "phone", phone,
                        "code", code
                ));
            }

            HoaDonDTO hoaDon = hoaDonService.getByMaHoaDon(code.trim());
            if (hoaDon == null) {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "message", "Không tìm thấy đơn hàng với mã: " + code
                ));
            }

            String orderPhone = hoaDon.getSoDienThoai() != null ? hoaDon.getSoDienThoai().replaceAll("[^0-9]", "") : "";
            boolean phoneMatch = orderPhone.endsWith(normalizedPhone) || normalizedPhone.endsWith(orderPhone) || orderPhone.equals(normalizedPhone);

            if (!phoneMatch) {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "message", "Số điện thoại không khớp với đơn hàng"
                ));
            }

            // Return minimal tracking info
            Map<String, Object> result = Map.of(
                    "found", true,
                    "maHoaDon", hoaDon.getMaHoaDon(),
                    "tenKhachHang", hoaDon.getTenKhachHang(),
                    "soDienThoai", hoaDon.getSoDienThoai(),
                    "trangThai", hoaDon.getTrangThai(),
                    "tenTrangThai", com.example.datn_sd28_2025.util.OrderStatusUtil.getStatusName(hoaDon.getTrangThai()),
                    "tongTien", hoaDon.getTongTienSauGiam()
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi kiểm tra đơn hàng",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getHoaDonProducts(@PathVariable Integer id) {
        try {
            List<Map<String, Object>> products = hoaDonService.getHoaDonProducts(id);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi lấy danh sách sản phẩm",
                    "message", e.getMessage(),
                    "hoaDonId", id
            ));
        }
    }

    @PostMapping
    public ResponseEntity<HoaDonDTO> create(@RequestBody HoaDonDTO hoaDonDTO) {
        try {
            // For simple creation without POS order request
            HoaDonDTO created = hoaDonService.updateHoaDon(0, hoaDonDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> updateHoaDon(@PathVariable Integer id, @RequestBody HoaDonDTO hoaDonDTO) {
        try {
            HoaDonDTO updated = hoaDonService.updateHoaDon(id, hoaDonDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<HoaDonDTO> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer newStatus = request.get("trangThai");
            HoaDonDTO updated = hoaDonService.updateTrangThai(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update-status/{maHoaDon}")
    public ResponseEntity<HoaDonDTO> updateStatusByMaHoaDon(@PathVariable String maHoaDon, @RequestBody Map<String, Object> request) {
        try {
            Integer newStatus = (Integer) request.get("trangThai");
            String nguoiThucHien = request.get("nguoiThucHien") != null ? request.get("nguoiThucHien").toString() : null;
            HoaDonDTO updated = hoaDonService.updateTrangThaiByMaHoaDon(maHoaDon, newStatus, nguoiThucHien);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/pos-order")
    public ResponseEntity<?> createPosOrder(@RequestBody PosOrderRequest request) {
        try {
            HoaDonDTO created = hoaDonService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Có lỗi xảy ra khi tạo hóa đơn",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/online-order")
    public ResponseEntity<?> createOnlineOrder(@RequestBody com.example.datn_sd28_2025.dto.OnlineOrderRequest request) {
        try {
            com.example.datn_sd28_2025.dto.OnlineOrderResponse response = hoaDonService.createOnlineOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Có lỗi xảy ra khi tạo đơn hàng online",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<HoaDonDTO> updateTrangThai(@PathVariable Integer id, @RequestParam Integer trangThai) {
        try {
            HoaDonDTO updated = hoaDonService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            hoaDonService.deleteHoaDon(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/statistics/count")
    public ResponseEntity<Long> countByDateRange(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        Long count = hoaDonService.countByDateRange(startDate, endDate);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/revenue")
    public ResponseEntity<Double> getTotalRevenueByDateRange(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        Double revenue = hoaDonService.getTotalRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/status-options")
    public ResponseEntity<Map<String, Object>> getStatusOptions() {
        Map<String, Object> options = Map.of(
                "statuses", Map.of(
                        OrderStatusUtil.CHO_XAC_NHAN, OrderStatusUtil.getStatusName(OrderStatusUtil.CHO_XAC_NHAN),
                        OrderStatusUtil.CHO_GIAO_HANG, OrderStatusUtil.getStatusName(OrderStatusUtil.CHO_GIAO_HANG),
                        OrderStatusUtil.DANG_GIAO, OrderStatusUtil.getStatusName(OrderStatusUtil.DANG_GIAO),
                        OrderStatusUtil.HOAN_THANH, OrderStatusUtil.getStatusName(OrderStatusUtil.HOAN_THANH),
                        OrderStatusUtil.DA_HUY, OrderStatusUtil.getStatusName(OrderStatusUtil.DA_HUY)
                ),
                "orderTypes", Map.of(
                        OrderStatusUtil.NORMAL, "Bán tại quầy",
                        OrderStatusUtil.DELIVERY, "Bán online"
                )
        );
        return ResponseEntity.ok(options);
    }

    @PostMapping("/update-sample-data")
    public ResponseEntity<String> updateSampleData() {
        try {
            // Cập nhật dữ liệu mẫu với các trạng thái đa dạng
            hoaDonService.updateSampleDataWithDiverseStatuses();
            return ResponseEntity.ok("Dữ liệu mẫu đã được cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    @GetMapping("/test-tracking/{maHoaDon}")
    public ResponseEntity<?> testTracking(@PathVariable String maHoaDon) {
        try {
            // Test endpoint đơn giản để debug
            var hoaDon = hoaDonService.getByMaHoaDon(maHoaDon);
            if (hoaDon != null) {
                return ResponseEntity.ok(Map.of(
                        "found", true,
                        "maHoaDon", hoaDon.getMaHoaDon(),
                        "tenKhachHang", hoaDon.getTenKhachHang(),
                        "trangThai", hoaDon.getTrangThai(),
                        "loaiHoaDon", hoaDon.getLoaiHoaDon()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "maHoaDon", maHoaDon,
                        "message", "Không tìm thấy hóa đơn"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi test tracking",
                    "message", e.getMessage(),
                    "maHoaDon", maHoaDon
            ));
        }
    }

    @GetMapping("/test-status")
    public ResponseEntity<?> testStatus() {
        try {
            // Test endpoint để kiểm tra trạng thái của tất cả hóa đơn
            var allHoaDons = hoaDonService.getAll();

            return ResponseEntity.ok(Map.of(
                    "totalHoaDons", allHoaDons.size(),
                    "message", "Test successful"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi test status",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/create-test-data")
    public ResponseEntity<?> createTestData() {
        try {
            // Tạo một số hóa đơn test với trạng thái khác nhau
            hoaDonService.updateSampleDataWithDiverseStatuses();

            return ResponseEntity.ok(Map.of(
                    "message", "Test data created successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi tạo test data",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/create-status-0")
    public ResponseEntity<?> createStatus0() {
        try {
            // Tạo một hóa đơn với trạng thái 0 (Chờ xác nhận)
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHoaDon("TEST_STATUS_0_" + System.currentTimeMillis());
            hoaDon.setTenKhachHang("Test Customer Status 0");
            hoaDon.setSoDienThoai("0900000000");
            hoaDon.setDiaChi("Test Address Status 0");
            hoaDon.setTongTien(1000000.0);
            hoaDon.setTongTienSauGiam(1000000.0);
            hoaDon.setLoaiHoaDon("BAN_THUONG");
            hoaDon.setTrangThai(0); // Chờ xác nhận
            hoaDon.setGhiChu("Test data with status 0");
            hoaDon.setNguoiTao(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            hoaDon.setNgayTao(java.time.LocalDateTime.now());
            hoaDon.setNgayCapNhat(java.time.LocalDateTime.now());

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            return ResponseEntity.ok(Map.of(
                    "message", "Hóa đơn với trạng thái 0 đã được tạo",
                    "maHoaDon", savedHoaDon.getMaHoaDon(),
                    "trangThai", savedHoaDon.getTrangThai(),
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi tạo hóa đơn status 0",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/delete-test-data")
    public ResponseEntity<?> deleteTestData() {
        try {
            // Xóa tất cả hóa đơn test với maHoaDon bắt đầu bằng "TEST_STATUS_0_"
            List<HoaDon> allHoaDons = hoaDonRepository.findAll();
            System.out.println("Total hóa đơn: " + allHoaDons.size());

            List<HoaDon> testHoaDons = allHoaDons.stream()
                    .filter(hd -> hd.getMaHoaDon().startsWith("TEST_STATUS_0_"))
                    .toList();

            System.out.println("Found " + testHoaDons.size() + " test hóa đơn to delete");

            int deletedCount = 0;
            for (HoaDon hoaDon : testHoaDons) {
                try {
                    System.out.println("Attempting to delete: " + hoaDon.getMaHoaDon() + " (ID: " + hoaDon.getId() + ")");
                    hoaDonRepository.deleteById(hoaDon.getId());
                    deletedCount++;
                    System.out.println("Successfully deleted: " + hoaDon.getMaHoaDon());
                } catch (Exception e) {
                    System.out.println("Error deleting " + hoaDon.getMaHoaDon() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Deleted " + deletedCount + " test invoices",
                    "deletedCount", deletedCount,
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error deleting test data",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/save-imei")
    @Transactional
    public ResponseEntity<Map<String, Object>> saveImei(@RequestBody Map<String, Object> request) {
        System.out.println("🚀 ========== API /save-imei CALLED ==========");
        System.out.println("🚀 Request received: " + request);
        try {
            String maHoaDon = (String) request.get("maHoaDon");
            String imei = (String) request.get("imei");
            Integer chiTietHoaDonId = request.get("chiTietHoaDonId") != null ? Integer.valueOf(request.get("chiTietHoaDonId").toString()) : null;
            Integer ctspId = request.get("ctspId") != null ? Integer.valueOf(request.get("ctspId").toString()) : null;
            String maCtsp = request.get("maCtsp") != null ? request.get("maCtsp").toString() : null;
            Integer lineIndex = request.get("lineIndex") != null ? Integer.valueOf(request.get("lineIndex").toString()) : null;
            // Parse nhanVienId từ request
            Integer nhanVienId = null;
            try {
                Object nhanVienIdObj = request.get("nhanVienId");
                if (nhanVienIdObj != null) {
                    if (nhanVienIdObj instanceof Integer) {
                        nhanVienId = (Integer) nhanVienIdObj;
                    } else if (nhanVienIdObj instanceof Number) {
                        nhanVienId = ((Number) nhanVienIdObj).intValue();
                    } else {
                        nhanVienId = Integer.valueOf(nhanVienIdObj.toString());
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error parsing nhanVienId: " + e.getMessage());
            }
            
            // Nếu không có nhanVienId trong request, lấy từ SecurityContext (người đang đăng nhập)
            if (nhanVienId == null || nhanVienId <= 0) {
                Integer currentUserId = SecurityUtil.getCurrentUserId();
                if (currentUserId != null && currentUserId > 0) {
                    nhanVienId = currentUserId;
                    System.out.println("🔍 Lấy nhanVienId từ SecurityContext: " + nhanVienId);
                } else {
                    // Thử lấy từ username nếu không có ID
                    String currentUsername = SecurityUtil.getCurrentUsername();
                    if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
                        // Query nhân viên từ username để lấy ID
                        var nhanVienOpt = nhanVienRepository.findByTaiKhoan(currentUsername);
                        if (nhanVienOpt.isEmpty()) {
                            nhanVienOpt = nhanVienRepository.findByEmail(currentUsername);
                        }
                        if (nhanVienOpt.isPresent()) {
                            nhanVienId = nhanVienOpt.get().getId();
                            System.out.println("🔍 Lấy nhanVienId từ username/email: " + currentUsername + " -> ID: " + nhanVienId);
                        }
                    }
                }
            }
            
            System.out.println("🔍 Received nhanVienId from request: " + request.get("nhanVienId"));
            System.out.println("🔍 Final nhanVienId to use: " + nhanVienId);
            System.out.println("🔍 Full request: " + request);
            
            if (maHoaDon == null || imei == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin maHoaDon hoặc imei"));
            }
            
            HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy hóa đơn với mã: " + maHoaDon));
            }
            
            // LUÔN cập nhật thông tin nhân viên vào hóa đơn nếu có nhanVienId
            // Điều này đảm bảo nhân viên xử lý IMEI được lưu vào database
            boolean nhanVienUpdated = false;
            if (nhanVienId != null && nhanVienId > 0) {
                try {
                    Integer oldNhanVienId = hoaDon.getNhanVienId();
                    System.out.println("🔧 Before update - nhanVienId in entity: " + hoaDon.getNhanVienId());
                    System.out.println("🔧 Setting nhanVienId to: " + nhanVienId);
                    
                    hoaDon.setNhanVienId(nhanVienId);
                    
                    // Cập nhật nguoiCapNhat từ SecurityContext
                    String currentUsername = SecurityUtil.getCurrentUsername();
                    if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
                        hoaDon.setNguoiCapNhat(currentUsername);
                    }
                    
                    // Lưu và flush để đảm bảo dữ liệu được commit ngay
                    System.out.println("🔧 Saving hoaDon with nhanVienId: " + hoaDon.getNhanVienId());
                    hoaDon = hoaDonRepository.save(hoaDon);
                    hoaDonRepository.flush(); // Force flush to database
                    
                    // Cập nhật trực tiếp bằng native query để đảm bảo lưu vào database
                    try {
                        int updatedRows = hoaDonRepository.updateNhanVienId(hoaDon.getId(), nhanVienId);
                        System.out.println("🔧 Updated nhanVienId using native query: " + updatedRows + " row(s) affected");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error updating nhanVienId using native query: " + e.getMessage());
                        // Continue với save() method
                    }
                    
                    // Verify sau khi lưu - query lại từ database
                    HoaDon verifiedHoaDon = hoaDonRepository.findById(hoaDon.getId()).orElse(null);
                    
                    System.out.println("✅ Đã cập nhật nhân viên (ID: " + nhanVienId + ") vào hóa đơn: " + maHoaDon);
                    System.out.println("   - Nhân viên cũ: " + oldNhanVienId);
                    System.out.println("   - Nhân viên mới trong entity: " + hoaDon.getNhanVienId());
                    System.out.println("   - Nhân viên mới trong DB (verified): " + (verifiedHoaDon != null ? verifiedHoaDon.getNhanVienId() : "null"));
                    System.out.println("   - NguoiCapNhat: " + hoaDon.getNguoiCapNhat());
                    
                    if (verifiedHoaDon != null && verifiedHoaDon.getNhanVienId() != null && verifiedHoaDon.getNhanVienId().equals(nhanVienId)) {
                        nhanVienUpdated = true;
                        System.out.println("✅ VERIFIED: nhanVienId đã được lưu thành công vào database");
                    } else {
                        System.err.println("❌ ERROR: nhanVienId KHÔNG được lưu vào database!");
                        System.err.println("   - Expected: " + nhanVienId);
                        System.err.println("   - Actual in DB: " + (verifiedHoaDon != null ? verifiedHoaDon.getNhanVienId() : "null"));
                    }
                } catch (Exception e) {
                    System.err.println("❌ ERROR khi cập nhật nhanVienId: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Không có nhanVienId trong request hoặc giá trị không hợp lệ");
                System.out.println("   - Request keys: " + request.keySet());
                System.out.println("   - nhanVienId value in request: " + request.get("nhanVienId"));
                System.out.println("   - Current user from SecurityContext: " + SecurityUtil.getCurrentUserId());
                System.out.println("   - Current username from SecurityContext: " + SecurityUtil.getCurrentUsername());
            }
            
            HoaDonCt hoaDonCt;
            if (chiTietHoaDonId != null) {
                hoaDonCt = hoaDonCtRepository.findById(chiTietHoaDonId).orElse(null);
                if (hoaDonCt == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy chiTietHoaDonId=" + chiTietHoaDonId));
                }
            } else {
                List<HoaDonCt> hoaDonCts = hoaDonCtRepository.findByIdHoaDon(hoaDon.getId());
                if (hoaDonCts != null) {
                    hoaDonCts.sort(java.util.Comparator.comparing(HoaDonCt::getId));
                }
                if (hoaDonCts == null || hoaDonCts.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Không tìm thấy chi tiết hóa đơn"));
                }

                HoaDonCt selectedHoaDonCt = null;
                if (lineIndex != null && lineIndex >= 0 && lineIndex < hoaDonCts.size()) {
                    selectedHoaDonCt = hoaDonCts.get(lineIndex);
                }
                if (selectedHoaDonCt == null && ctspId != null) {
                    selectedHoaDonCt = hoaDonCts.stream()
                            .filter(ct -> {
                                try {
                                    var field = ct.getClass().getDeclaredField("ctspId");
                                    field.setAccessible(true);
                                    Object val = field.get(ct);
                                    return val != null && Integer.valueOf(val.toString()).equals(ctspId);
                                } catch (Exception ignore) { }
                                return false;
                            })
                            .findFirst().orElse(null);
                }
                if (selectedHoaDonCt == null && maCtsp != null) {
                    selectedHoaDonCt = hoaDonCts.stream()
                            .filter(ct -> {
                                try {
                                    var field = ct.getClass().getDeclaredField("maCtsp");
                                    field.setAccessible(true);
                                    Object val = field.get(ct);
                                    return val != null && maCtsp.equals(val.toString());
                                } catch (Exception ignore) { }
                                return false;
                            })
                            .findFirst().orElse(null);
                }
                if (selectedHoaDonCt == null) {
                    selectedHoaDonCt = hoaDonCts.get(0);
                }
                hoaDonCt = selectedHoaDonCt;
            }
            
            try {
                List<ImeiDaBan> existingByImei = imeiDaBanRepository.findByImei(imei);
                if (existingByImei != null && !existingByImei.isEmpty()) {
                    boolean existsOnSameLine = false;
                    for (ImeiDaBan existed : existingByImei) {
                        try {
                            if (existed.getHoaDonChiTiet() != null && hoaDonCt != null &&
                                    existed.getHoaDonChiTiet().getId() != null &&
                                    existed.getHoaDonChiTiet().getId().equals(hoaDonCt.getId())) {
                                existsOnSameLine = true;
                                break;
                            }
                        } catch (Exception ignore) {}
                    }

                    if (existsOnSameLine) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "IMEI đã tồn tại trên dòng sản phẩm này (idempotent)",
                                "maHoaDon", maHoaDon,
                                "imei", imei,
                                "chiTietHoaDonId", hoaDonCt != null ? hoaDonCt.getId() : -1
                        ));
                    }

                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "IMEI đã tồn tại trong hệ thống",
                            "imei", imei
                    ));
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error checking duplicate IMEI: " + e.getMessage());
            }

            try {
                List<ImeiDaBan> existingAssignments = imeiDaBanRepository.findByImei(imei).stream()
                    .filter(existing -> existing.getHoaDonChiTiet() != null && 
                            existing.getHoaDonChiTiet().getId() != null &&
                            existing.getHoaDonChiTiet().getId().equals(hoaDonCt.getId()))
                    .toList();
                
                if (!existingAssignments.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "IMEI đã được gán cho dòng sản phẩm này (idempotent)",
                        "maHoaDon", maHoaDon,
                        "imei", imei,
                        "chiTietHoaDonId", hoaDonCt.getId(),
                        "existing", true
                    ));
                }
                
                List<ImeiDaBan> assignedToOther = imeiDaBanRepository.findByImei(imei).stream()
                    .filter(existing -> existing.getHoaDonChiTiet() != null && 
                            existing.getHoaDonChiTiet().getId() != null &&
                            !existing.getHoaDonChiTiet().getId().equals(hoaDonCt.getId()))
                    .toList();
                
                if (!assignedToOther.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "IMEI đã được gán cho đơn hàng khác",
                        "imei", imei
                    ));
                }
                
                try {
                    var imeiEntity = imeiService.findByImeiString(imei);
                    if (imeiEntity == null) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "IMEI không tồn tại trong hệ thống",
                            "imei", imei
                        ));
                    }
                    
                    if (imeiEntity.getTrangThai() != 1) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "IMEI không khả dụng (đã bán hoặc bị khóa)",
                            "imei", imei
                        ));
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Lỗi khi kiểm tra IMEI: " + e.getMessage(),
                        "imei", imei
                    ));
                }
                
                ImeiDaBan imeiDaBan = new ImeiDaBan();
                imeiDaBan.setHoaDonChiTiet(hoaDonCt);
                imeiDaBan.setImei(imei);
                imeiDaBan.setTrangThai(0); // 0 = đã bán
                
                ImeiDaBan saved = imeiDaBanRepository.save(imeiDaBan);
                System.out.println("✅ Đã lưu IMEI vào bảng imei_da_ban: " + imei);
                
                // Chuyển IMEI sang trạng thái đã bán (ngừng hoạt động)
                try {
                    var imeiEntity = imeiService.findByImeiString(imei);
                    if (imeiEntity != null) {
                        Integer oldTrangThai = imeiEntity.getTrangThai();
                        imeiEntity.setTrangThai(0); // 0 = đã bán (ngừng hoạt động), 1 = khả dụng
                        imeiService.update(imeiEntity.getId(), imeiService.convertToDTO(imeiEntity));
                        System.out.println("✅ Đã chuyển IMEI sang trạng thái đã bán (ngừng hoạt động)");
                        System.out.println("   - IMEI: " + imei);
                        System.out.println("   - Trạng thái cũ: " + oldTrangThai);
                        System.out.println("   - Trạng thái mới: 0 (đã bán)");
                    } else {
                        System.err.println("⚠️ Không tìm thấy IMEI entity để cập nhật: " + imei);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error updating IMEI status: " + e.getMessage());
                    e.printStackTrace();
                }
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "IMEI đã được gán cho sản phẩm và đánh dấu đã bán");
                responseMap.put("maHoaDon", maHoaDon);
                responseMap.put("imei", imei);
                if (chiTietHoaDonId != null) {
                    responseMap.put("chiTietHoaDonId", chiTietHoaDonId);
                }
                if (ctspId != null) {
                    responseMap.put("ctspId", ctspId);
                }
                if (maCtsp != null) {
                    responseMap.put("maCtsp", maCtsp);
                }
                if (lineIndex != null) {
                    responseMap.put("lineIndex", lineIndex);
                }
                if (nhanVienId != null) {
                    responseMap.put("nhanVienId", nhanVienId);
                }
                
                return ResponseEntity.ok(responseMap);
                
            } catch (Exception e) {
                String errMsg = e.getMessage();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errMsg = e.getCause().getMessage();
                }
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Lỗi khi lưu IMEI: " + (errMsg != null ? errMsg : "unknown"),
                                "maHoaDon", maHoaDon,
                                "imei", imei
                        ));
            }
            
        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi lưu IMEI: " + e.getMessage()));
        }
    }

    @PostMapping("/remove-imei")
    public ResponseEntity<Map<String, Object>> removeImei(@RequestBody Map<String, Object> request) {
        try {
            String maHoaDon = (String) request.get("maHoaDon");
            String imei = (String) request.get("imei");
            
            if (maHoaDon == null || imei == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin maHoaDon hoặc imei"));
            }
            
            HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy hóa đơn với mã: " + maHoaDon));
            }
            
            // Lấy tất cả chi tiết hóa đơn của hóa đơn này
            List<HoaDonCt> hoaDonCts = hoaDonCtRepository.findByIdHoaDon(hoaDon.getId());
            if (hoaDonCts == null || hoaDonCts.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy chi tiết hóa đơn"));
            }
            
            // Tìm IMEI trong các chi tiết hóa đơn
            ImeiDaBan imeiDaBanToRemove = null;
            for (HoaDonCt hoaDonCt : hoaDonCts) {
                List<ImeiDaBan> imeiDaBanList = imeiDaBanRepository.findByIdHoaDonChiTiet(hoaDonCt.getId());
                for (ImeiDaBan imeiDaBan : imeiDaBanList) {
                    if (imei != null && imei.equals(imeiDaBan.getImei())) {
                        imeiDaBanToRemove = imeiDaBan;
                        break;
                    }
                }
                if (imeiDaBanToRemove != null) break;
            }
            
            if (imeiDaBanToRemove == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy IMEI trong hóa đơn này"));
            }
            
            // Xóa bản ghi imei_da_ban
            imeiDaBanRepository.delete(imeiDaBanToRemove);
            
            // Cập nhật trạng thái IMEI về khả dụng (trangThai = 1)
            try {
                var imeiEntity = imeiService.findByImeiString(imei);
                if (imeiEntity != null) {
                    imeiEntity.setTrangThai(1); // Khả dụng
                    imeiService.update(imeiEntity.getId(), imeiService.convertToDTO(imeiEntity));
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error updating IMEI status: " + e.getMessage());
                // Tiếp tục dù có lỗi khi cập nhật trạng thái IMEI
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa IMEI khỏi hóa đơn",
                    "imei", imei,
                    "maHoaDon", maHoaDon
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi xóa IMEI: " + e.getMessage()));
        }
    }

    /**
     * API: Tạo hoàn phí (refund) cho hóa đơn
     * Sử dụng khi hủy đơn hàng hoặc trả hàng
     */
    @PostMapping("/{maHoaDon}/refund")
    public ResponseEntity<?> createRefund(@PathVariable String maHoaDon, @RequestBody Map<String, Object> request) {
        try {
            // Lấy hóa đơn
            HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn với mã: " + maHoaDon
                ));
            }

            // Lấy thông tin từ request
            Double soTienHoan = null;
            if (request.get("soTienHoan") != null) {
                if (request.get("soTienHoan") instanceof Number) {
                    soTienHoan = ((Number) request.get("soTienHoan")).doubleValue();
                } else {
                    soTienHoan = Double.parseDouble(request.get("soTienHoan").toString());
                }
            }

            String phuongThucThanhToan = request.get("phuongThucThanhToan") != null ? 
                    request.get("phuongThucThanhToan").toString() : null;
            String lyDo = request.get("lyDo") != null ? request.get("lyDo").toString() : null;

            // Nếu không có số tiền, lấy từ tổng tiền đã thanh toán
            if (soTienHoan == null || soTienHoan <= 0) {
                soTienHoan = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : hoaDon.getTongTien();
            }

            // Tạo hoàn phí
            var refund = hoaDonServiceImpl.createRefundPayment(hoaDon, soTienHoan, phuongThucThanhToan, lyDo);

            // Convert to DTO
            ChiTietThanhToanDTO refundDto = ChiTietThanhToanDTO.builder()
                    .id(refund.getId())
                    .soTien(refund.getSoTien())
                    .maGiaoDich(refund.getMaGiaoDich())
                    .ngayThanhToan(refund.getNgayThanhToan())
                    .trangThai(refund.getTrangThai())
                    .loaiThanhToan(refund.getLoaiThanhToan())
                    .ngayTao(refund.getNgayTao())
                    .ngayCapNhat(refund.getNgayCapNhat())
                    .build();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Hoàn phí đã được tạo thành công",
                    "refund", refundDto
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo hoàn phí: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * API: Tạo phụ phí (additional fee) cho hóa đơn
     * Sử dụng khi khách hàng cập nhật đơn hàng (thêm sản phẩm hoặc phí vận chuyển)
     */
    @PostMapping("/{maHoaDon}/additional-fee")
    public ResponseEntity<?> createAdditionalFee(@PathVariable String maHoaDon, @RequestBody Map<String, Object> request) {
        try {
            // Lấy hóa đơn
            HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn với mã: " + maHoaDon
                ));
            }

            // Lấy thông tin từ request
            Double soTienPhuPhi = null;
            if (request.get("soTienPhuPhi") != null) {
                if (request.get("soTienPhuPhi") instanceof Number) {
                    soTienPhuPhi = ((Number) request.get("soTienPhuPhi")).doubleValue();
                } else {
                    soTienPhuPhi = Double.parseDouble(request.get("soTienPhuPhi").toString());
                }
            }

            if (soTienPhuPhi == null || soTienPhuPhi <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số tiền phụ phí phải lớn hơn 0"
                ));
            }

            String phuongThucThanhToan = request.get("phuongThucThanhToan") != null ? 
                    request.get("phuongThucThanhToan").toString() : null;
            String moTa = request.get("moTa") != null ? request.get("moTa").toString() : null;
            
            // Lấy trạng thái thanh toán (0 = Chưa thanh toán, 1 = Đã thanh toán)
            Integer trangThai = 1; // Mặc định đã thanh toán
            if (request.get("trangThai") != null) {
                if (request.get("trangThai") instanceof Number) {
                    trangThai = ((Number) request.get("trangThai")).intValue();
                } else {
                    try {
                        trangThai = Integer.parseInt(request.get("trangThai").toString());
                    } catch (NumberFormatException e) {
                        trangThai = 1; // Mặc định nếu parse lỗi
                    }
                }
            }

            // Tạo phụ phí với trạng thái thanh toán
            var additionalFee = hoaDonServiceImpl.createAdditionalFeePayment(hoaDon, soTienPhuPhi, phuongThucThanhToan, moTa, trangThai);

            // Convert to DTO
            ChiTietThanhToanDTO feeDto = ChiTietThanhToanDTO.builder()
                    .id(additionalFee.getId())
                    .soTien(additionalFee.getSoTien())
                    .maGiaoDich(additionalFee.getMaGiaoDich())
                    .ngayThanhToan(additionalFee.getNgayThanhToan())
                    .trangThai(additionalFee.getTrangThai())
                    .loaiThanhToan(additionalFee.getLoaiThanhToan())
                    .ngayTao(additionalFee.getNgayTao())
                    .ngayCapNhat(additionalFee.getNgayCapNhat())
                    .build();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phụ phí đã được tạo thành công",
                    "additionalFee", feeDto
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo phụ phí: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * API: Admin chuyển tiền hoàn phí
     * Chuyển trạng thái refund từ PENDING -> TRANSFERRED
     */
    @PutMapping("/{maHoaDon}/refund/{refundId}/transfer")
    public ResponseEntity<?> transferRefund(@PathVariable String maHoaDon, @PathVariable Integer refundId) {
        try {
            // Lấy refund record
            ChiTietThanhToan refund = chiTietThanhToanRepository.findById(refundId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi hoàn phí với ID: " + refundId));

            // Kiểm tra refund thuộc về hóa đơn này
            if (refund.getHoaDon() == null || !refund.getHoaDon().getMaHoaDon().equals(maHoaDon)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bản ghi hoàn phí không thuộc về hóa đơn này"
                ));
            }

            // Kiểm tra loại thanh toán phải là REFUND
            if (!OrderStatusUtil.REFUND.equals(refund.getLoaiThanhToan())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bản ghi này không phải là hoàn phí"
                ));
            }

            // Kiểm tra trạng thái hiện tại phải là PENDING
            if (refund.getTrangThai() != null && refund.getTrangThai() != OrderStatusUtil.REFUND_PENDING) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể chuyển tiền khi trạng thái là 'Chờ chuyển tiền'"
                ));
            }

            // Cập nhật trạng thái: PENDING -> TRANSFERRED
            refund.setTrangThai(OrderStatusUtil.REFUND_TRANSFERRED);
            refund.setNgayThanhToan(LocalDateTime.now()); // Set ngày chuyển tiền
            refund.setNgayCapNhat(LocalDateTime.now());
            chiTietThanhToanRepository.save(refund);

            System.out.println("✅ Admin đã chuyển tiền hoàn phí cho hóa đơn " + maHoaDon + ", refund ID: " + refundId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã chuyển tiền hoàn phí thành công",
                    "refundStatus", refund.getTrangThai(),
                    "refundStatusName", OrderStatusUtil.getRefundStatusName(refund.getTrangThai())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi chuyển tiền hoàn phí: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * API: Customer xác nhận đã nhận hoàn phí
     * Chuyển trạng thái refund từ TRANSFERRED -> COMPLETED
     */
    @PutMapping("/{maHoaDon}/refund/{refundId}/confirm")
    public ResponseEntity<?> confirmRefundReceived(@PathVariable String maHoaDon, @PathVariable Integer refundId) {
        try {
            // Lấy refund record
            ChiTietThanhToan refund = chiTietThanhToanRepository.findById(refundId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi hoàn phí với ID: " + refundId));

            // Kiểm tra refund thuộc về hóa đơn này
            if (refund.getHoaDon() == null || !refund.getHoaDon().getMaHoaDon().equals(maHoaDon)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bản ghi hoàn phí không thuộc về hóa đơn này"
                ));
            }

            // Kiểm tra loại thanh toán phải là REFUND
            if (!OrderStatusUtil.REFUND.equals(refund.getLoaiThanhToan())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bản ghi này không phải là hoàn phí"
                ));
            }

            // Kiểm tra trạng thái hiện tại phải là TRANSFERRED
            if (refund.getTrangThai() == null || refund.getTrangThai() != OrderStatusUtil.REFUND_TRANSFERRED) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể xác nhận khi trạng thái là 'Đã chuyển tiền - Chờ xác nhận'"
                ));
            }

            // Cập nhật trạng thái: TRANSFERRED -> COMPLETED
            refund.setTrangThai(OrderStatusUtil.REFUND_COMPLETED);
            refund.setNgayCapNhat(LocalDateTime.now());
            chiTietThanhToanRepository.save(refund);

            System.out.println("✅ Customer đã xác nhận nhận hoàn phí cho hóa đơn " + maHoaDon + ", refund ID: " + refundId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xác nhận nhận hoàn phí thành công",
                    "refundStatus", refund.getTrangThai(),
                    "refundStatusName", OrderStatusUtil.getRefundStatusName(refund.getTrangThai())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi xác nhận hoàn phí: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * API: Cập nhật đơn hàng (địa chỉ, sản phẩm) từ website
     * Cho phép khách hàng cập nhật đơn hàng 1 lần
     */
    @PutMapping("/update-order/{maHoaDon}")
    public ResponseEntity<?> updateOrder(@PathVariable String maHoaDon, @RequestBody Map<String, Object> request) {
        try {
            // Lấy hóa đơn
            HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn với mã: " + maHoaDon
                ));
            }

            // Kiểm tra đơn hàng có thể cập nhật không (chỉ cho phép khi ở trạng thái "Chờ xác nhận" - 0)
            if (hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể cập nhật đơn hàng khi đơn hàng ở trạng thái 'Chờ xác nhận'"
                ));
            }

            // Cập nhật thông tin khách hàng (chỉ cho phép các trường: tên, số điện thoại, email, ghi chú)
            if (request.get("tenKhachHang") != null) {
                hoaDon.setTenKhachHang(request.get("tenKhachHang").toString());
            }
            if (request.get("soDienThoai") != null) {
                hoaDon.setSoDienThoai(request.get("soDienThoai").toString());
            }
            if (request.get("email") != null) {
                hoaDon.setEmail(request.get("email").toString());
            }
            if (request.get("ghiChu") != null) {
                hoaDon.setGhiChu(request.get("ghiChu").toString());
            }

            // Cập nhật địa chỉ
            if (request.get("diaChi") != null) {
                hoaDon.setDiaChi(request.get("diaChi").toString());
            }
            if (request.get("tinhThanh") != null) {
                hoaDon.setTinhThanh(request.get("tinhThanh").toString());
            }
            if (request.get("quanHuyen") != null) {
                hoaDon.setQuanHuyen(request.get("quanHuyen").toString());
            }
            if (request.get("phiVanChuyen") != null) {
                hoaDon.setPhiVanChuyen(Double.parseDouble(request.get("phiVanChuyen").toString()));
            }
            if (request.get("phuongThucGiaoHang") != null) {
                hoaDon.setPhuongThucGiaoHang(request.get("phuongThucGiaoHang").toString());
            }
            
            // Recalculate phuongThucNhanHang when updating order
            hoaDon.setPhuongThucNhanHang(OrderStatusUtil.calculatePhuongThucNhanHang(
                hoaDon.getLoaiHoaDon(),
                hoaDon.getPhuongThucGiaoHang()
            ));

            // Cập nhật sản phẩm và tổng tiền
            if (request.get("tongTienHang") != null) {
                Double newTongTienHang = Double.parseDouble(request.get("tongTienHang").toString());
                hoaDon.setTongTien(newTongTienHang);
                
                // Tính lại tổng tiền sau giảm (nếu có giảm giá)
                Double oldTongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0.0;
                Double oldTongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : 0.0;
                Double oldGiamGia = oldTongTien - oldTongTienSauGiam; // Giảm giá cũ
                
                Double phiVanChuyen = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : 0.0;
                Double tongTienSauGiam = newTongTienHang - oldGiamGia + phiVanChuyen;
                hoaDon.setTongTienSauGiam(tongTienSauGiam);
            }

            // Cập nhật chi tiết đơn hàng
            if (request.get("chiTietDonHang") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> chiTietDonHangList = (List<Map<String, Object>>) request.get("chiTietDonHang");
                
                // Xóa tất cả chi tiết hóa đơn hiện tại (sử dụng HoaDonCt thay vì ChiTietHoaDon)
                List<HoaDonCt> existingChiTiet = hoaDonCtRepository.findByIdHoaDon(hoaDon.getId());
                for (HoaDonCt existing : existingChiTiet) {
                    hoaDonCtRepository.delete(existing);
                }
                
                // Tạo lại tất cả chi tiết từ request
                for (Map<String, Object> itemData : chiTietDonHangList) {
                    Integer chiTietSanPhamId = itemData.get("chiTietSanPhamId") != null ?
                            Integer.parseInt(itemData.get("chiTietSanPhamId").toString()) : null;
                    Integer soLuong = itemData.get("soLuong") != null ?
                            Integer.parseInt(itemData.get("soLuong").toString()) : 1;
                    Double donGia = itemData.get("donGia") != null ?
                            Double.parseDouble(itemData.get("donGia").toString()) : 0.0;
                    
                    if (chiTietSanPhamId == null) {
                        throw new RuntimeException("chiTietSanPhamId không được để trống");
                    }
                    
                    // Tìm ChiTietSanPham
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietSanPhamId));
                    
                    // Tạo HoaDonCt records với số lượng tương ứng (mỗi record là 1 sản phẩm)
                    for (int i = 0; i < soLuong; i++) {
                        HoaDonCt newHoaDonCt = HoaDonCt.builder()
                                .hoaDon(hoaDon)
                                .chiTietSanPham(chiTietSanPham)
                                .donGia(java.math.BigDecimal.valueOf(donGia))
                                .thanhTien(java.math.BigDecimal.valueOf(donGia))
                                .trangThai(1)
                                .build();
                        hoaDonCtRepository.save(newHoaDonCt);
                    }
                }
            }

            hoaDon.setNgayCapNhat(LocalDateTime.now());
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // Tạo lịch sử cập nhật đơn hàng
            try {
                // Xác định các thay đổi đã được thực hiện
                StringBuilder moTaBuilder = new StringBuilder("Khách hàng cập nhật đơn hàng: ");
                List<String> changes = new java.util.ArrayList<>();
                
                if (request.get("tenKhachHang") != null || request.get("soDienThoai") != null || 
                    request.get("email") != null || request.get("ghiChu") != null) {
                    changes.add("thông tin khách hàng");
                }
                if (request.get("diaChi") != null || request.get("tinhThanh") != null || 
                    request.get("quanHuyen") != null || request.get("phiVanChuyen") != null) {
                    changes.add("địa chỉ giao hàng");
                }
                if (request.get("chiTietDonHang") != null) {
                    changes.add("sản phẩm");
                }
                if (request.get("tongTienHang") != null) {
                    changes.add("tổng tiền");
                }
                
                if (changes.isEmpty()) {
                    moTaBuilder.append("cập nhật thông tin đơn hàng");
                } else {
                    moTaBuilder.append(String.join(", ", changes));
                }
                
                // Lấy tên khách hàng để lưu vào nguoiThucHien
                String nguoiThucHien = savedHoaDon.getTenKhachHang();
                if (nguoiThucHien == null || nguoiThucHien.trim().isEmpty()) {
                    nguoiThucHien = "Khách hàng";
                }
                
                // Tạo bản ghi lịch sử
                TrangThaiTracking tracking = new TrangThaiTracking(
                    savedHoaDon.getId(),
                    savedHoaDon.getTrangThai(),
                    OrderStatusUtil.getStatusName(savedHoaDon.getTrangThai()),
                    moTaBuilder.toString(),
                    nguoiThucHien
                );
                trangThaiTrackingRepository.save(tracking);
                System.out.println("✅ Created tracking record for order update: " + savedHoaDon.getMaHoaDon() + " by " + nguoiThucHien);
            } catch (Exception e) {
                System.out.println("⚠️ Error creating tracking record for order update: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the update if tracking fails
            }

            // Convert to DTO using service method
            HoaDonDTO updatedDto = hoaDonService.getByMaHoaDon(maHoaDon);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật đơn hàng thành công",
                    "data", updatedDto != null ? updatedDto : new HashMap<>()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi cập nhật đơn hàng: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

}

