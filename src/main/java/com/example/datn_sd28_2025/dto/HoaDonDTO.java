package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {
    private Integer id;
    private String maHoaDon;
    private Integer khachHangId;
    private Integer nhanVienId;
    private String maNhanVien;
    private String tenNhanVien;
    private Integer phieuGiamGiaId;
    private Double tongTien;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private Double tongTienSauGiam;
    private LocalDateTime ngayThanhToan;
    private String loaiHoaDon;
    private Integer trangThai;
    private String ghiChu;
    private String phuongThucThanhToan;
    private String phuongThucGiaoHang;
    private String phuongThucNhanHang; // Tại cửa hàng, Giao hàng
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;

    // Thông tin khách hàng (nếu có) - for additional customer info
    private String soDienThoaiKhachHang;

    // Chi tiết hóa đơn
    private List<ChiTietHoaDonDTO> chiTietHoaDonList;
    
    // Lịch sử thanh toán
    private List<ChiTietThanhToanDTO> lichSuThanhToan;
}