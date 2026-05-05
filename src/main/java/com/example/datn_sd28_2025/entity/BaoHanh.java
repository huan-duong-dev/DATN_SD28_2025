package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bao_hanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaoHanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết với IMEI đã bán
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imei_da_ban", nullable = false)
    private ImeiDaBan imeiDaBan;

    // IMEI để dễ tra cứu
    @Column(name = "imei", length = 50, nullable = false)
    private String imei;

    // Thông tin hóa đơn và sản phẩm
    @Column(name = "id_hoa_don", nullable = false)
    private Integer idHoaDon;

    @Column(name = "id_hoa_don_chi_tiet", nullable = false)
    private Integer idHoaDonChiTiet;

    @Column(name = "id_chi_tiet_san_pham", nullable = false)
    private Integer idChiTietSanPham;

    @Column(name = "id_san_pham")
    private Integer idSanPham;

    // Thông tin khách hàng
    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    // Thời hạn bảo hành
    @Column(name = "thoi_han_bao_hanh", nullable = false)
    private Integer thoiHanBaoHanh; // Số tháng: 12, 18, 24

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau; // Ngày bán/ngày tạo hóa đơn

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc; // Ngày kết thúc bảo hành

    // Trạng thái
    @Column(name = "trang_thai")
    private Integer trangThai;
    // 0: Còn hạn bảo hành
    // 1: Hết hạn bảo hành
    // 2: Đã hủy

    // Thông tin bổ sung
    @Column(name = "ghi_chu", length = 500, columnDefinition = "NVARCHAR(500)")
    private String ghiChu;

    // Timestamps
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = 0; // Còn hạn
        }
        // Tự động tính ngày kết thúc nếu chưa có
        if (ngayKetThuc == null && ngayBatDau != null && thoiHanBaoHanh != null) {
            ngayKetThuc = ngayBatDau.plusMonths(thoiHanBaoHanh);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
        // Tự động cập nhật trạng thái dựa trên ngày kết thúc
        if (ngayKetThuc != null) {
            if (ngayKetThuc.isBefore(LocalDate.now())) {
                trangThai = 1; // Hết hạn
            } else if (trangThai != 2) { // Không phải đã hủy
                trangThai = 0; // Còn hạn
            }
        }
    }
}

