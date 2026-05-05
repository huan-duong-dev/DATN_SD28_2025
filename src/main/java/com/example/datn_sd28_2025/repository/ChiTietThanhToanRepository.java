package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ChiTietThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietThanhToanRepository extends JpaRepository<ChiTietThanhToan, Integer> {
    
    /**
     * Lấy danh sách chi tiết thanh toán theo id hóa đơn, sắp xếp theo ngày thanh toán tăng dần (cũ đến mới)
     */
    @Query("SELECT ctt FROM ChiTietThanhToan ctt " +
           "WHERE ctt.hoaDon.id = :hoaDonId " +
           "ORDER BY COALESCE(ctt.ngayThanhToan, ctt.ngayTao) ASC, ctt.ngayTao ASC")
    List<ChiTietThanhToan> findByHoaDonIdOrderByNgayThanhToanDesc(@Param("hoaDonId") Integer hoaDonId);
    
    /**
     * Lấy danh sách chi tiết thanh toán theo id hóa đơn với fetch join để tối ưu query
     * Sắp xếp từ cũ đến mới (oldest to newest)
     */
    @Query("SELECT ctt FROM ChiTietThanhToan ctt " +
           "LEFT JOIN FETCH ctt.phuongThucThanhToan " +
           "WHERE ctt.hoaDon.id = :hoaDonId " +
           "ORDER BY COALESCE(ctt.ngayThanhToan, ctt.ngayTao) ASC, ctt.ngayTao ASC")
    List<ChiTietThanhToan> findByHoaDonIdWithPhuongThuc(@Param("hoaDonId") Integer hoaDonId);
    
    /**
     * Tổng số tiền đã thanh toán của một hóa đơn (chỉ tính PAYMENT, không tính REFUND)
     */
    @Query("SELECT COALESCE(SUM(ctt.soTien), 0) FROM ChiTietThanhToan ctt " +
           "WHERE ctt.hoaDon.id = :hoaDonId AND ctt.trangThai = 1 " +
           "AND (ctt.loaiThanhToan = 'PAYMENT' OR ctt.loaiThanhToan IS NULL)")
    java.math.BigDecimal sumTienDaThanhToan(@Param("hoaDonId") Integer hoaDonId);
}

