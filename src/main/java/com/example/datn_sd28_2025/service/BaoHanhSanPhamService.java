package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.BaoHanhDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BaoHanhSanPhamService {
    // CRUD operations
    List<BaoHanhDTO> getAll();
    Optional<BaoHanhDTO> getById(Integer id);
    BaoHanhDTO create(BaoHanhDTO baoHanhDTO);
    BaoHanhDTO update(Integer id, BaoHanhDTO baoHanhDTO);
    void delete(Integer id);
    
    // Tìm kiếm
    Optional<BaoHanhDTO> findByImei(String imei);
    List<BaoHanhDTO> findByIdHoaDon(Integer idHoaDon);
    List<BaoHanhDTO> findByIdKhachHang(Integer idKhachHang);
    List<BaoHanhDTO> findByTrangThai(Integer trangThai);
    
    // Tự động tạo bảo hành khi bán IMEI
    BaoHanhDTO taoBaoHanhKhiBanImei(Integer idImeiDaBan, String imei, Integer idHoaDon, 
                                     Integer idHoaDonChiTiet, Integer idChiTietSanPham, 
                                     Integer idSanPham, Integer idKhachHang, LocalDate ngayBatDau);
    
    // Kiểm tra bảo hành
    boolean kiemTraBaoHanhConHan(String imei);
    Optional<BaoHanhDTO> getBaoHanhConHanByImei(String imei);
    
    // Báo cáo
    List<BaoHanhDTO> getBaoHanhConHan();
    List<BaoHanhDTO> getBaoHanhHetHan();
    List<BaoHanhDTO> getBaoHanhSapHetHan(int soNgay);
    
    // Cập nhật trạng thái
    void capNhatTrangThaiHetHan();
    String getTrangThaiText(Integer trangThai);
}

