package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaoHanhDTO {
    private Integer id;
    private Integer idImeiDaBan;
    private String imei;
    private Integer idHoaDon;
    private String maHoaDon;
    private Integer idHoaDonChiTiet;
    private Integer idChiTietSanPham;
    private String maCtsp;
    private Integer idSanPham;
    private String tenSanPham;
    private Integer idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private Integer thoiHanBaoHanh; // Số tháng: 12, 18, 24
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private String trangThaiText; // "Còn hạn", "Hết hạn", "Đã hủy"
    private Boolean isHetHan; // true nếu hết hạn
    private Long soNgayConLai; // Số ngày còn lại
    private String ghiChu;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
}

