package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;
import com.example.datn_sd28_2025.entity.PhieuBaoHanh;
import com.example.datn_sd28_2025.repository.PhieuBaoHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phieu-bao-hanh")
@CrossOrigin(origins = "*")
public class PhieuBaoHanhController {

    @Autowired
    private PhieuBaoHanhRepository phieuBaoHanhRepository;

    // Endpoint chính cho danh sách phiếu bảo hành
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
                .sanPhamId(phieu.getSanPhamId())
                .chiTietSanPhamId(phieu.getChiTietSanPhamId())
                .hoaDonId(phieu.getHoaDonId())
                .tenSanPham(phieu.getTenSanPham())
                .imeiSerial(phieu.getImeiSerial())
                .moTaLoiKhachHang(phieu.getMoTaLoiKhachHang())
                .moTaLoiNhanVien(phieu.getMoTaLoiNhanVien())
                .tinhTrangVatLy(phieu.getTinhTrangVatLy())
                .phuKienDiKem(phieu.getPhuKienDiKem())
                .duDieuKienBaoHanh(phieu.getDuDieuKienBaoHanh())
                .lyDoKhongDuDieuKien(phieu.getLyDoKhongDuDieuKien())
                .huongXuLy(phieu.getHuongXuLy())
                .noiDungSuaChua(phieu.getNoiDungSuaChua())
                .ghiChuKyThuatVien(phieu.getGhiChuKyThuatVien())
                .chiPhiSuaChua(phieu.getChiPhiSuaChua())
                .khachDaThanhToan(phieu.getKhachDaThanhToan())
                .ttbhHang(phieu.getTtbhHang())
                .maBaoHanhHang(phieu.getMaBaoHanhHang())
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

