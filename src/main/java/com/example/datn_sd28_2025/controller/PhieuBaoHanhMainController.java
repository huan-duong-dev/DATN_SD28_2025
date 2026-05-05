package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.BaoHanhDTO;
import com.example.datn_sd28_2025.dto.LichSuXuLyBaoHanhDTO;
import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;
import com.example.datn_sd28_2025.entity.LichSuXuLyBaoHanh;
import com.example.datn_sd28_2025.entity.PhieuBaoHanh;
import com.example.datn_sd28_2025.repository.LichSuXuLyBaoHanhRepository;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.repository.PhieuBaoHanhRepository;
import com.example.datn_sd28_2025.service.BaoHanhSanPhamService;
import com.example.datn_sd28_2025.service.EmailService;
import com.example.datn_sd28_2025.service.PDFService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller chính cho PhieuBaoHanh với mapping /api/bao-hanh
 * Để tương thích với frontend đang gọi /api/bao-hanh
 */
@RestController
@RequestMapping("/api/bao-hanh")
@CrossOrigin(origins = "*")
public class PhieuBaoHanhMainController {

    @Autowired
    private PhieuBaoHanhRepository phieuBaoHanhRepository;

    @Autowired
    private BaoHanhSanPhamService baoHanhSanPhamService;

    @Autowired
    private LichSuXuLyBaoHanhRepository lichSuXuLyBaoHanhRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PDFService pdfService;

    @GetMapping
    public ResponseEntity<List<PhieuBaoHanhDTO>> getAll() {
        try {
            List<PhieuBaoHanh> phieuBaoHanhList = phieuBaoHanhRepository.findAll();
            List<PhieuBaoHanhDTO> dtoList = phieuBaoHanhList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuBaoHanhDTO> getById(@PathVariable Integer id) {
        try {
            Optional<PhieuBaoHanh> phieuBaoHanh = phieuBaoHanhRepository.findById(id);
            if (phieuBaoHanh.isPresent()) {
                return ResponseEntity.ok(convertToDto(phieuBaoHanh.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Kiểm tra chi tiết bảo hành theo IMEI
     * Endpoint này kiểm tra bảo hành sản phẩm (BaoHanh), không phải phiếu bảo hành (PhieuBaoHanh)
     */
    @GetMapping("/kiem-tra-chi-tiet/{imei}")
    public ResponseEntity<?> kiemTraBaoHanhChiTiet(@PathVariable String imei) {
        try {
            // Decode IMEI if it contains special characters
            String decodedImei = imei;
            if (imei.contains(":")) {
                // Handle IMEI with colon separator (e.g., "356789012345681:1")
                decodedImei = imei.split(":")[0];
            }
            
            // Tìm bảo hành theo IMEI (bất kể còn hạn hay hết hạn)
            Optional<BaoHanhDTO> baoHanhOpt = baoHanhSanPhamService.findByImei(decodedImei);
            
            if (baoHanhOpt.isPresent()) {
                BaoHanhDTO baoHanh = baoHanhOpt.get();
                // Tính toán lại trạng thái real-time
                LocalDate ngayHienTai = LocalDate.now();
                if (baoHanh.getNgayKetThuc() != null) {
                    long soNgayConLai = java.time.temporal.ChronoUnit.DAYS.between(ngayHienTai, baoHanh.getNgayKetThuc());
                    baoHanh.setSoNgayConLai(soNgayConLai);
                    baoHanh.setIsHetHan(soNgayConLai < 0);
                    
                    // Cập nhật trạng thái text dựa trên tính toán real-time
                    String trangThaiText;
                    if (soNgayConLai < 0) {
                        trangThaiText = "Hết hạn";
                    } else if (soNgayConLai <= 30) {
                        trangThaiText = "Sắp hết hạn (" + soNgayConLai + " ngày)";
                    } else {
                        trangThaiText = "Còn hạn (" + soNgayConLai + " ngày)";
                    }
                    baoHanh.setTrangThaiText(trangThaiText);
                }
                return ResponseEntity.ok(baoHanh);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Không tìm thấy bảo hành cho IMEI: " + decodedImei));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi kiểm tra bảo hành: " + e.getMessage()));
        }
    }

    /**
     * Tiếp nhận yêu cầu bảo hành - Tạo phiếu bảo hành mới
     */
    @PostMapping("/tiep-nhan")
    @Transactional
    public ResponseEntity<?> tiepNhanBaoHanh(@RequestBody PhieuBaoHanhDTO dto) {
        try {
            // Convert DTO to Entity
            PhieuBaoHanh phieu = new PhieuBaoHanh();
            phieu.setKhachHangId(dto.getKhachHangId());
            phieu.setTenKhachHang(dto.getTenKhachHang());
            phieu.setSoDienThoai(dto.getSoDienThoai());
            phieu.setEmailKhachHang(dto.getEmailKhachHang());
            phieu.setSanPhamId(dto.getSanPhamId());
            phieu.setChiTietSanPhamId(dto.getChiTietSanPhamId());
            phieu.setHoaDonId(dto.getHoaDonId());
            phieu.setTenSanPham(dto.getTenSanPham());
            phieu.setImeiSerial(dto.getImeiSerial());
            phieu.setMauSacSku(dto.getMauSacSku());
            phieu.setMoTaLoiKhachHang(dto.getMoTaLoiKhachHang());
            phieu.setMoTaLoiNhanVien(dto.getMoTaLoiNhanVien());
            phieu.setKiemTraNhanh(dto.getKiemTraNhanh());
            phieu.setTinhTrangVatLy(dto.getTinhTrangVatLy());
            phieu.setPhuKienDiKem(dto.getPhuKienDiKem());
            
            // Tình trạng máy chi tiết
            phieu.setTrayXuocNhe(dto.getTrayXuocNhe());
            phieu.setCanMop(dto.getCanMop());
            phieu.setMeVen(dto.getMeVen());
            phieu.setManHinhSocDiemChet(dto.getManHinhSocDiemChet());
            phieu.setVaoNuoc(dto.getVaoNuoc());
            phieu.setTemBaoHanhRachMat(dto.getTemBaoHanhRachMat());
            
            // Phụ kiện đi kèm chi tiết
            phieu.setPhuKienSac(dto.getPhuKienSac());
            phieu.setPhuKienCap(dto.getPhuKienCap());
            phieu.setPhuKienHop(dto.getPhuKienHop());
            phieu.setPhuKienKhac(dto.getPhuKienKhac());
            
            // Xác minh bảo hành
            phieu.setBaoHanhConHan(dto.getBaoHanhConHan());
            phieu.setImeiTrungKhop(dto.getImeiTrungKhop());
            phieu.setTemNguyenVen(dto.getTemNguyenVen());
            
            // Đánh giá điều kiện bảo hành
            phieu.setDuDieuKienBaoHanh(dto.getDuDieuKienBaoHanh());
            phieu.setLyDoKhongDuDieuKien(dto.getLyDoKhongDuDieuKien());
            phieu.setHuongXuLy(dto.getHuongXuLy());
            
            // Biên bản bàn giao
            phieu.setMaVanDon(dto.getMaVanDon());
            phieu.setTinhTrangNiemPhong(dto.getTinhTrangNiemPhong());
            phieu.setNguoiPhuTrach(dto.getNguoiPhuTrach());
            
            phieu.setNhanVienTiepNhanId(dto.getNhanVienTiepNhanId());
            phieu.setNgayNhan(dto.getNgayNhan() != null ? dto.getNgayNhan() : LocalDate.now());
            phieu.setNgayHenTraDuKien(dto.getNgayHenTraDuKien());
            phieu.setTrangThai(0); // Mới tiếp nhận
            
            // Set người tạo
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiTao(currentUsername != null ? currentUsername : "System");
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");
            
            // Save phiếu bảo hành
            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);
            
            // Tạo lịch sử tiếp nhận
            if (savedPhieu.getNhanVienTiepNhanId() != null) {
                String tenNhanVien = nhanVienRepository.findById(savedPhieu.getNhanVienTiepNhanId())
                        .map(com.example.datn_sd28_2025.entity.NhanVien::getHoTen)
                        .orElse("Không xác định");
                
                LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                        .phieuBaoHanh(savedPhieu)
                        .thoiGian(LocalDateTime.now())
                        .nhanVienThucHienId(savedPhieu.getNhanVienTiepNhanId())
                        .tenNhanVienThucHien(tenNhanVien)
                        .hanhDong("TIEP_NHAN")
                        .noiDungXuLy("Tiếp nhận yêu cầu bảo hành")
                        .trangThaiTruoc(null)
                        .trangThaiSau(0)
                        .build();
                
                lichSuXuLyBaoHanhRepository.save(lichSu);
            }
            
            // Convert to DTO để gửi email và tạo PDF
            PhieuBaoHanhDTO savedPhieuDTO = convertToDto(savedPhieu);
            
            // Gửi email với PDF đính kèm cho khách hàng (nếu có email)
            if (savedPhieu.getEmailKhachHang() != null && !savedPhieu.getEmailKhachHang().trim().isEmpty()) {
                try {
                    // Tạo PDF biên bản tiếp nhận
                    byte[] pdfBytes = pdfService.generateBienBanTiepNhan(savedPhieuDTO);
                    
                    // Gửi email với PDF đính kèm
                    emailService.sendBienBanTiepNhanBaoHanh(
                        savedPhieu.getEmailKhachHang(),
                        savedPhieu.getTenKhachHang() != null ? savedPhieu.getTenKhachHang() : "Quý khách",
                        savedPhieu.getMaPhieu() != null ? savedPhieu.getMaPhieu() : "",
                        pdfBytes
                    );
                } catch (Exception e) {
                    // Log lỗi nhưng không làm gián đoạn quá trình tiếp nhận
                    System.err.println("Lỗi khi gửi email biên bản tiếp nhận: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPhieuDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi tiếp nhận bảo hành: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra điều kiện bảo hành
     */
    @PostMapping("/{id}/kiem-tra-dieu-kien")
    @Transactional
    public ResponseEntity<?> kiemTraDieuKien(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin kiểm tra điều kiện
            Boolean duDieuKien = request.get("duDieuKien") != null ? 
                Boolean.parseBoolean(request.get("duDieuKien").toString()) : null;
            String lyDo = request.get("lyDo") != null ? request.get("lyDo").toString() : null;

            phieu.setDuDieuKienBaoHanh(duDieuKien);
            phieu.setLyDoKhongDuDieuKien(lyDo);

            // Cập nhật trạng thái dựa trên kết quả kiểm tra
            if (duDieuKien != null) {
                if (duDieuKien) {
                    phieu.setTrangThai(1); // Đủ điều kiện
                } else {
                    phieu.setTrangThai(2); // Không đủ điều kiện
                }
            }

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("KIEM_TRA_DIEU_KIEN")
                    .noiDungXuLy(duDieuKien != null ? 
                        (duDieuKien ? "Đánh giá: Đủ điều kiện bảo hành" : "Đánh giá: Không đủ điều kiện bảo hành. Lý do: " + (lyDo != null ? lyDo : "")) 
                        : "Cập nhật đánh giá điều kiện bảo hành")
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật đánh giá điều kiện: " + e.getMessage()));
        }
    }

    /**
     * Sửa chữa nội bộ
     */
    @PostMapping("/{id}/sua-noi-bo")
    @Transactional
    public ResponseEntity<?> suaNoiBo(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin sửa chữa nội bộ
            String noiDungSuaChua = request.get("noiDungSuaChua") != null ? request.get("noiDungSuaChua").toString() : null;
            String ghiChu = request.get("ghiChu") != null ? request.get("ghiChu").toString() : null;

            phieu.setNoiDungSuaChua(noiDungSuaChua);
            phieu.setGhiChuKyThuatVien(ghiChu);
            phieu.setHuongXuLy("SUA_TAI_CUA_HANG");
            phieu.setTrangThai(3); // Đang sửa chữa nội bộ

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienKyThuatId() != null ? savedPhieu.getNhanVienKyThuatId() : savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("SUA_NOI_BO")
                    .noiDungXuLy("Cập nhật thông tin sửa chữa nội bộ: " + (noiDungSuaChua != null ? noiDungSuaChua : ""))
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật thông tin sửa chữa nội bộ: " + e.getMessage()));
        }
    }

    /**
     * Gửi đến TTBH hãng
     */
    @PostMapping("/{id}/gui-ttbh")
    @Transactional
    public ResponseEntity<?> guiTTBH(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin gửi TTBH hãng
            String ttbhHang = request.get("ttbhHang") != null ? request.get("ttbhHang").toString() : null;
            String maBaoHanhHang = request.get("maBaoHanhHang") != null ? request.get("maBaoHanhHang").toString() : null;

            phieu.setTtbhHang(ttbhHang);
            phieu.setMaBaoHanhHang(maBaoHanhHang);
            phieu.setHuongXuLy("GUI_TTBH_HANG");
            phieu.setTrangThai(4); // Đã gửi TTBH hãng

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("GUI_TTBH")
                    .noiDungXuLy("Gửi đến TTBH hãng: " + (ttbhHang != null ? ttbhHang : "") + 
                        (maBaoHanhHang != null ? " - Mã BH: " + maBaoHanhHang : ""))
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật thông tin gửi TTBH: " + e.getMessage()));
        }
    }

    /**
     * Nhận từ TTBH hãng
     */
    @PostMapping("/{id}/nhan-tu-ttbh")
    @Transactional
    public ResponseEntity<?> nhanTuTTBH(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin nhận từ TTBH
            String noiDungSuaChua = request.get("noiDungSuaChua") != null ? request.get("noiDungSuaChua").toString() : null;
            String ghiChu = request.get("ghiChu") != null ? request.get("ghiChu").toString() : null;

            phieu.setNoiDungSuaChua(noiDungSuaChua);
            phieu.setGhiChuKyThuatVien(ghiChu);
            phieu.setTrangThai(5); // Đã nhận từ TTBH

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("NHAN_TU_TTBH")
                    .noiDungXuLy("Nhận máy từ TTBH hãng. Nội dung sửa chữa: " + (noiDungSuaChua != null ? noiDungSuaChua : ""))
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật thông tin nhận từ TTBH: " + e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử xử lý bảo hành
     */
    @GetMapping("/{id}/lich-su")
    public ResponseEntity<?> getLichSuXuLy(@PathVariable Integer id) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<LichSuXuLyBaoHanh> lichSuList = lichSuXuLyBaoHanhRepository.findByPhieuBaoHanhId(id);
            List<LichSuXuLyBaoHanhDTO> dtoList = lichSuList.stream()
                    .map(this::convertLichSuToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi lấy lịch sử xử lý: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra QC
     */
    @PostMapping("/{id}/kiem-tra-qc")
    @Transactional
    public ResponseEntity<?> kiemTraQC(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin kiểm tra QC
            Boolean qcPass = request.get("qcPass") != null ? 
                Boolean.parseBoolean(request.get("qcPass").toString()) : null;
            String ghiChu = request.get("ghiChu") != null ? request.get("ghiChu").toString() : null;

            phieu.setGhiChuKyThuatVien(ghiChu);

            // Cập nhật trạng thái dựa trên kết quả QC
            if (qcPass != null) {
                if (qcPass) {
                    phieu.setTrangThai(7); // Đã sửa xong
                } else {
                    // Nếu QC fail, có thể quay lại trạng thái sửa chữa
                    phieu.setTrangThai(3); // Đang sửa chữa nội bộ
                }
            } else {
                phieu.setTrangThai(6); // Đang kiểm tra QC
            }

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienKyThuatId() != null ? savedPhieu.getNhanVienKyThuatId() : savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("KIEM_TRA_QC")
                    .noiDungXuLy("Kiểm tra QC: " + (qcPass != null ? (qcPass ? "Pass - Đã sửa xong" : "Fail - Cần sửa lại") : "Đang kiểm tra") + 
                        (ghiChu != null ? ". Ghi chú: " + ghiChu : ""))
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật kết quả QC: " + e.getMessage()));
        }
    }

    /**
     * Trả máy cho khách hàng
     */
    @PostMapping("/{id}/tra-may")
    @Transactional
    public ResponseEntity<?> traMay(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> request) {
        try {
            Optional<PhieuBaoHanh> phieuOpt = phieuBaoHanhRepository.findById(id);
            if (phieuOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PhieuBaoHanh phieu = phieuOpt.get();
            Integer trangThaiTruoc = phieu.getTrangThai();

            // Cập nhật thông tin trả máy
            String ghiChu = request.get("ghiChu") != null ? request.get("ghiChu").toString() : null;
            phieu.setGhiChuKyThuatVien(ghiChu);
            phieu.setNgayTraThucTe(LocalDate.now());
            phieu.setTrangThai(8); // Đã trả khách

            // Cập nhật người cập nhật
            String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
            phieu.setNguoiCapNhat(currentUsername != null ? currentUsername : "System");

            PhieuBaoHanh savedPhieu = phieuBaoHanhRepository.save(phieu);

            // Tạo lịch sử
            LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                    .phieuBaoHanh(savedPhieu)
                    .thoiGian(LocalDateTime.now())
                    .nhanVienThucHienId(savedPhieu.getNhanVienTraMayId() != null ? savedPhieu.getNhanVienTraMayId() : savedPhieu.getNhanVienTiepNhanId())
                    .tenNhanVienThucHien(currentUsername != null ? currentUsername : "System")
                    .hanhDong("TRA_MAY")
                    .noiDungXuLy("Trả máy cho khách hàng" + (ghiChu != null ? ". Ghi chú: " + ghiChu : ""))
                    .trangThaiTruoc(trangThaiTruoc)
                    .trangThaiSau(savedPhieu.getTrangThai())
                    .build();

            lichSuXuLyBaoHanhRepository.save(lichSu);

            return ResponseEntity.ok(convertToDto(savedPhieu));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Lỗi khi cập nhật trạng thái trả máy: " + e.getMessage()));
        }
    }

    private LichSuXuLyBaoHanhDTO convertLichSuToDto(LichSuXuLyBaoHanh lichSu) {
        if (lichSu == null) {
            return null;
        }

        return LichSuXuLyBaoHanhDTO.builder()
                .id(lichSu.getId())
                .idPhieuBaoHanh(lichSu.getPhieuBaoHanh() != null ? lichSu.getPhieuBaoHanh().getId() : null)
                .thoiGian(lichSu.getThoiGian())
                .nhanVienThucHienId(lichSu.getNhanVienThucHienId())
                .tenNhanVienThucHien(lichSu.getTenNhanVienThucHien())
                .hanhDong(lichSu.getHanhDong())
                .noiDungXuLy(lichSu.getNoiDungXuLy())
                .chiPhiPhatSinh(lichSu.getChiPhiPhatSinh())
                .linhKienThayThe(lichSu.getLinhKienThayThe())
                .ghiChu(lichSu.getGhiChu())
                .trangThaiTruoc(lichSu.getTrangThaiTruoc())
                .trangThaiSau(lichSu.getTrangThaiSau())
                .ngayTao(lichSu.getNgayTao())
                .build();
    }

    private PhieuBaoHanhDTO convertToDto(PhieuBaoHanh phieu) {
        if (phieu == null) {
            return null;
        }

        String trangThaiText = getTrangThaiText(phieu.getTrangThai());

        return PhieuBaoHanhDTO.builder()
                .id(phieu.getId())
                .maPhieu(phieu.getMaPhieu())
                .khachHangId(phieu.getKhachHangId())
                .tenKhachHang(phieu.getTenKhachHang())
                .soDienThoai(phieu.getSoDienThoai())
                .emailKhachHang(phieu.getEmailKhachHang())
                .sanPhamId(phieu.getSanPhamId())
                .chiTietSanPhamId(phieu.getChiTietSanPhamId())
                .hoaDonId(phieu.getHoaDonId())
                .tenSanPham(phieu.getTenSanPham())
                .imeiSerial(phieu.getImeiSerial())
                .mauSacSku(phieu.getMauSacSku())
                .moTaLoiKhachHang(phieu.getMoTaLoiKhachHang())
                .moTaLoiNhanVien(phieu.getMoTaLoiNhanVien())
                .kiemTraNhanh(phieu.getKiemTraNhanh())
                .tinhTrangVatLy(phieu.getTinhTrangVatLy())
                .phuKienDiKem(phieu.getPhuKienDiKem())
                .trayXuocNhe(phieu.getTrayXuocNhe())
                .canMop(phieu.getCanMop())
                .meVen(phieu.getMeVen())
                .manHinhSocDiemChet(phieu.getManHinhSocDiemChet())
                .vaoNuoc(phieu.getVaoNuoc())
                .temBaoHanhRachMat(phieu.getTemBaoHanhRachMat())
                .phuKienSac(phieu.getPhuKienSac())
                .phuKienCap(phieu.getPhuKienCap())
                .phuKienHop(phieu.getPhuKienHop())
                .phuKienKhac(phieu.getPhuKienKhac())
                .baoHanhConHan(phieu.getBaoHanhConHan())
                .imeiTrungKhop(phieu.getImeiTrungKhop())
                .temNguyenVen(phieu.getTemNguyenVen())
                .duDieuKienBaoHanh(phieu.getDuDieuKienBaoHanh())
                .lyDoKhongDuDieuKien(phieu.getLyDoKhongDuDieuKien())
                .huongXuLy(phieu.getHuongXuLy())
                .noiDungSuaChua(phieu.getNoiDungSuaChua())
                .ghiChuKyThuatVien(phieu.getGhiChuKyThuatVien())
                .chiPhiSuaChua(phieu.getChiPhiSuaChua())
                .khachDaThanhToan(phieu.getKhachDaThanhToan())
                .ttbhHang(phieu.getTtbhHang())
                .maBaoHanhHang(phieu.getMaBaoHanhHang())
                .maVanDon(phieu.getMaVanDon())
                .tinhTrangNiemPhong(phieu.getTinhTrangNiemPhong())
                .nguoiPhuTrach(phieu.getNguoiPhuTrach())
                .nhanVienTiepNhanId(phieu.getNhanVienTiepNhanId())
                .nhanVienKyThuatId(phieu.getNhanVienKyThuatId())
                .nhanVienTraMayId(phieu.getNhanVienTraMayId())
                .ngayNhan(phieu.getNgayNhan())
                .ngayHenTraDuKien(phieu.getNgayHenTraDuKien())
                .ngayTraThucTe(phieu.getNgayTraThucTe())
                .trangThai(phieu.getTrangThai())
                .trangThaiText(trangThaiText)
                .ngayTao(phieu.getNgayTao())
                .ngayCapNhat(phieu.getNgayCapNhat())
                .nguoiTao(phieu.getNguoiTao())
                .nguoiCapNhat(phieu.getNguoiCapNhat())
                .build();
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) {
            return "Không xác định";
        }
        return switch (trangThai) {
            case 0 -> "Mới tiếp nhận";
            case 1 -> "Đủ điều kiện";
            case 2 -> "Không đủ điều kiện";
            case 3 -> "Đang sửa chữa nội bộ";
            case 4 -> "Đã gửi TTBH hãng";
            case 5 -> "Đã nhận từ TTBH";
            case 6 -> "Đang kiểm tra QC";
            case 7 -> "Đã sửa xong";
            case 8 -> "Đã trả khách";
            case 9 -> "Hoàn tất";
            default -> "Không xác định";
        };
    }
}

