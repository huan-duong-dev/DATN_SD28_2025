package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.BaoHanhDTO;
import com.example.datn_sd28_2025.service.BaoHanhSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bao-hanh-san-pham")
@CrossOrigin(origins = "*")
public class BaoHanhController {

    @Autowired
    private BaoHanhSanPhamService baoHanhService;

    @GetMapping
    public ResponseEntity<List<BaoHanhDTO>> getAll() {
        try {
            List<BaoHanhDTO> baoHanhList = baoHanhService.getAll();
            return ResponseEntity.ok(baoHanhList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bao-hanh-list")
    public ResponseEntity<List<BaoHanhDTO>> getAllList() {
        try {
            List<BaoHanhDTO> baoHanhList = baoHanhService.getAll();
            return ResponseEntity.ok(baoHanhList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bao-hanh-list/{id}")
    public ResponseEntity<BaoHanhDTO> getById(@PathVariable Integer id) {
        Optional<BaoHanhDTO> baoHanh = baoHanhService.getById(id);
        return baoHanh.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bao-hanh-list/imei/{imei}")
    public ResponseEntity<BaoHanhDTO> getByImei(@PathVariable String imei) {
        Optional<BaoHanhDTO> baoHanh = baoHanhService.findByImei(imei);
        return baoHanh.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bao-hanh-list/hoa-don/{idHoaDon}")
    public ResponseEntity<List<BaoHanhDTO>> getByIdHoaDon(@PathVariable Integer idHoaDon) {
        List<BaoHanhDTO> baoHanhList = baoHanhService.findByIdHoaDon(idHoaDon);
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/khach-hang/{idKhachHang}")
    public ResponseEntity<List<BaoHanhDTO>> getByIdKhachHang(@PathVariable Integer idKhachHang) {
        List<BaoHanhDTO> baoHanhList = baoHanhService.findByIdKhachHang(idKhachHang);
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/trang-thai/{trangThai}")
    public ResponseEntity<List<BaoHanhDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        List<BaoHanhDTO> baoHanhList = baoHanhService.findByTrangThai(trangThai);
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/con-han")
    public ResponseEntity<List<BaoHanhDTO>> getBaoHanhConHan() {
        List<BaoHanhDTO> baoHanhList = baoHanhService.getBaoHanhConHan();
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/het-han")
    public ResponseEntity<List<BaoHanhDTO>> getBaoHanhHetHan() {
        List<BaoHanhDTO> baoHanhList = baoHanhService.getBaoHanhHetHan();
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/sap-het-han")
    public ResponseEntity<List<BaoHanhDTO>> getBaoHanhSapHetHan(
            @RequestParam(defaultValue = "30") int soNgay) {
        List<BaoHanhDTO> baoHanhList = baoHanhService.getBaoHanhSapHetHan(soNgay);
        return ResponseEntity.ok(baoHanhList);
    }

    @GetMapping("/bao-hanh-list/kiem-tra/{imei}")
    public ResponseEntity<?> kiemTraBaoHanh(@PathVariable String imei) {
        Optional<BaoHanhDTO> baoHanh = baoHanhService.getBaoHanhConHanByImei(imei);
        if (baoHanh.isPresent()) {
            return ResponseEntity.ok(baoHanh.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("IMEI không có bảo hành còn hạn");
        }
    }

    @GetMapping("/kiem-tra-chi-tiet/{imei}")
    public ResponseEntity<?> kiemTraBaoHanhChiTiet(@PathVariable String imei) {
        // Tìm bảo hành theo IMEI (bất kể còn hạn hay hết hạn)
        Optional<BaoHanhDTO> baoHanhOpt = baoHanhService.findByImei(imei);
        
        if (baoHanhOpt.isPresent()) {
            BaoHanhDTO baoHanh = baoHanhOpt.get();
            // Tính toán lại trạng thái real-time
            java.time.LocalDate ngayHienTai = java.time.LocalDate.now();
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
                    .body(java.util.Map.of("error", "Không tìm thấy bảo hành cho IMEI: " + imei));
        }
    }

    @PostMapping("/bao-hanh-list")
    public ResponseEntity<BaoHanhDTO> create(@RequestBody BaoHanhDTO baoHanhDTO) {
        try {
            BaoHanhDTO created = baoHanhService.create(baoHanhDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/bao-hanh-list/{id}")
    public ResponseEntity<BaoHanhDTO> update(@PathVariable Integer id, 
                                             @RequestBody BaoHanhDTO baoHanhDTO) {
        try {
            BaoHanhDTO updated = baoHanhService.update(id, baoHanhDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/bao-hanh-list/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            baoHanhService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/bao-hanh-list/cap-nhat-trang-thai")
    public ResponseEntity<Void> capNhatTrangThaiHetHan() {
        baoHanhService.capNhatTrangThaiHetHan();
        return ResponseEntity.ok().build();
    }
}
