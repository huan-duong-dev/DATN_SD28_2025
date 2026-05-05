package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.BaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaoHanhRepository extends JpaRepository<BaoHanh, Integer> {
    
    // Tìm bảo hành theo IMEI
    Optional<BaoHanh> findByImei(String imei);
    
    // Tìm bảo hành theo id_imei_da_ban
    @Query("SELECT b FROM BaoHanh b WHERE b.imeiDaBan.id = :idImeiDaBan")
    Optional<BaoHanh> findByIdImeiDaBan(@Param("idImeiDaBan") Integer idImeiDaBan);
    
    // Tìm bảo hành theo hóa đơn
    List<BaoHanh> findByIdHoaDon(Integer idHoaDon);
    
    // Tìm bảo hành theo khách hàng
    List<BaoHanh> findByIdKhachHang(Integer idKhachHang);
    
    // Tìm bảo hành theo trạng thái
    List<BaoHanh> findByTrangThai(Integer trangThai);
    
    // Tìm bảo hành còn hạn
    @Query("SELECT b FROM BaoHanh b WHERE b.trangThai = 0 AND b.ngayKetThuc >= :ngayHienTai")
    List<BaoHanh> findBaoHanhConHan(@Param("ngayHienTai") LocalDate ngayHienTai);
    
    // Tìm bảo hành hết hạn
    @Query("SELECT b FROM BaoHanh b WHERE b.ngayKetThuc < :ngayHienTai AND b.trangThai != 2")
    List<BaoHanh> findBaoHanhHetHan(@Param("ngayHienTai") LocalDate ngayHienTai);
    
    // Tìm bảo hành sắp hết hạn (trong vòng X ngày)
    @Query("SELECT b FROM BaoHanh b WHERE b.trangThai = 0 AND b.ngayKetThuc BETWEEN :ngayHienTai AND :ngaySapHetHan")
    List<BaoHanh> findBaoHanhSapHetHan(@Param("ngayHienTai") LocalDate ngayHienTai, 
                                       @Param("ngaySapHetHan") LocalDate ngaySapHetHan);
    
    // Kiểm tra IMEI có bảo hành còn hạn không
    @Query("SELECT b FROM BaoHanh b WHERE b.imei = :imei AND b.trangThai = 0 AND b.ngayKetThuc >= :ngayHienTai")
    Optional<BaoHanh> findBaoHanhConHanByImei(@Param("imei") String imei, 
                                               @Param("ngayHienTai") LocalDate ngayHienTai);
}

