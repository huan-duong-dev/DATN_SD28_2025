package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "phieu_bao_hanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuBaoHanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu", unique = true, length = 50)
    private String maPhieu;

    // Thông tin khách hàng
    @Column(name = "id_khach_hang")
    private Integer khachHangId;

    @Column(name = "ten_khach_hang", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "email_khach_hang", length = 255, columnDefinition = "NVARCHAR(255)")
    private String emailKhachHang;

    // Thông tin sản phẩm
    @Column(name = "id_san_pham")
    private Integer sanPhamId;

    @Column(name = "id_chi_tiet_san_pham")
    private Integer chiTietSanPhamId;

    @Column(name = "id_hoa_don")
    private Integer hoaDonId; // Hóa đơn mua hàng gốc

    @Column(name = "ten_san_pham", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenSanPham;

    @Column(name = "imei_serial", length = 50)
    private String imeiSerial;

    @Column(name = "mau_sac_sku", length = 255, columnDefinition = "NVARCHAR(255)")
    private String mauSacSku;

    // Tình trạng tiếp nhận
    @Column(name = "mo_ta_loi_khach_hang", columnDefinition = "NTEXT")
    private String moTaLoiKhachHang;

    @Column(name = "mo_ta_loi_nhan_vien", columnDefinition = "NTEXT")
    private String moTaLoiNhanVien;

    @Column(name = "kiem_tra_nhanh", columnDefinition = "NTEXT")
    private String kiemTraNhanh; // Kiểm tra nhanh (NV/KTV) - mô tả các chức năng đã test

    @Column(name = "tinh_trang_vat_ly", columnDefinition = "NTEXT")
    private String tinhTrangVatLy; // Trầy xước, cấn móp, v.v.

    // Tình trạng máy chi tiết (checkbox)
    @Column(name = "tray_xuoc_nhe")
    private Boolean trayXuocNhe; // Trầy xước nhẹ

    @Column(name = "can_mop")
    private Boolean canMop; // Cấn móp

    @Column(name = "me_ven")
    private Boolean meVen; // Mẻ viền

    @Column(name = "man_hinh_soc_diem_chet")
    private Boolean manHinhSocDiemChet; // Màn hình sọc/điểm chết

    @Column(name = "vao_nuoc")
    private Boolean vaoNuoc; // Vào nước

    @Column(name = "tem_bao_hanh_rach_mat")
    private Boolean temBaoHanhRachMat; // Tem bảo hành rách/mất

    @Column(name = "phu_kien_di_kem", length = 500, columnDefinition = "NVARCHAR(500)")
    private String phuKienDiKem; // Sạc, cáp, hộp, v.v. (deprecated - dùng các trường chi tiết bên dưới)

    // Phụ kiện đi kèm chi tiết (checkbox)
    @Column(name = "phu_kien_sac")
    private Boolean phuKienSac; // Sạc

    @Column(name = "phu_kien_cap")
    private Boolean phuKienCap; // Cáp

    @Column(name = "phu_kien_hop")
    private Boolean phuKienHop; // Hộp

    @Column(name = "phu_kien_khac", length = 500, columnDefinition = "NVARCHAR(500)")
    private String phuKienKhac; // Phụ kiện khác (mô tả)

    // Xác minh bảo hành
    @Column(name = "bao_hanh_con_han")
    private Boolean baoHanhConHan; // Bảo hành còn hạn

    @Column(name = "imei_trung_khop")
    private Boolean imeiTrungKhop; // IMEI/Serial trùng khớp

    @Column(name = "tem_nguyen_ven")
    private Boolean temNguyenVen; // Tem nguyên vẹn

    // Đánh giá điều kiện bảo hành
    @Column(name = "du_dieu_kien_bao_hanh")
    private Boolean duDieuKienBaoHanh; // true: đủ điều kiện, false: không đủ

    @Column(name = "ly_do_khong_du_dieu_kien", columnDefinition = "NTEXT")
    private String lyDoKhongDuDieuKien;

    // Hướng xử lý
    @Column(name = "huong_xu_ly", length = 50)
    private String huongXuLy; // "SUA_TAI_CUA_HANG", "GUI_TTBH_HANG"

    // Kết quả xử lý
    @Column(name = "noi_dung_sua_chua", columnDefinition = "NTEXT")
    private String noiDungSuaChua;

    @Column(name = "ghi_chu_ky_thuat_vien", columnDefinition = "NTEXT")
    private String ghiChuKyThuatVien;

    @Column(name = "chi_phi_sua_chua", precision = 18, scale = 2)
    private BigDecimal chiPhiSuaChua;

    @Column(name = "khach_da_thanh_toan", precision = 18, scale = 2)
    private BigDecimal khachDaThanhToan;

    // Thông tin TTBH hãng (nếu gửi)
    @Column(name = "ttbh_hang", length = 255, columnDefinition = "NVARCHAR(255)")
    private String ttbhHang; // Tên trung tâm bảo hành hãng

    @Column(name = "ma_bao_hanh_hang", length = 100)
    private String maBaoHanhHang; // Mã bảo hành của hãng

    // Biên bản bàn giao (nếu gửi đi)
    @Column(name = "ma_van_don", length = 100, columnDefinition = "NVARCHAR(100)")
    private String maVanDon; // Mã vận đơn

    @Column(name = "tinh_trang_niem_phong", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tinhTrangNiemPhong; // Tình trạng niêm phong

    @Column(name = "nguoi_phu_trach", length = 255, columnDefinition = "NVARCHAR(255)")
    private String nguoiPhuTrach; // Người phụ trách (KTV/NV)

    // Nhân viên xử lý
    @Column(name = "id_nhan_vien_tiep_nhan")
    private Integer nhanVienTiepNhanId; // Nhân viên tiếp nhận

    @Column(name = "id_nhan_vien_ky_thuat")
    private Integer nhanVienKyThuatId; // Kỹ thuật viên xử lý

    @Column(name = "id_nhan_vien_tra_may")
    private Integer nhanVienTraMayId; // Nhân viên trả máy

    // Thời gian
    @Column(name = "ngay_nhan")
    private LocalDate ngayNhan;

    @Column(name = "ngay_hen_tra_du_kien")
    private LocalDate ngayHenTraDuKien;

    @Column(name = "ngay_tra_thuc_te")
    private LocalDate ngayTraThucTe;

    // Trạng thái
    @Column(name = "trang_thai")
    private Integer trangThai; 
    // 0: Mới tiếp nhận / Đang kiểm tra điều kiện
    // 1: Đủ điều kiện bảo hành
    // 2: Không đủ điều kiện bảo hành
    // 3: Đang sửa chữa nội bộ
    // 4: Đã gửi TTBH hãng
    // 5: Đã nhận từ TTBH
    // 6: Đang kiểm tra QC
    // 7: Đã sửa xong
    // 8: Đã trả khách
    // 9: Hoàn tất

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    // Lịch sử xử lý
    @OneToMany(mappedBy = "phieuBaoHanh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LichSuXuLyBaoHanh> lichSuXuLyBaoHanhList;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (maPhieu == null || maPhieu.isEmpty()) {
            maPhieu = generateMaPhieu();
        }
        if (trangThai == null) {
            trangThai = 0; // Mới tiếp nhận
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    private String generateMaPhieu() {
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "BH-" + dateStr + "-" + System.currentTimeMillis() % 10000;
    }
}

