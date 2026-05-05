package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    // Queries theo sản phẩm (tương thích ngược)
    @Query("SELECT r FROM Review r WHERE r.sanPham.id = :idSanPham ORDER BY r.ngayTao DESC")
    List<Review> findBySanPhamIdOrderByNgayTaoDesc(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT r FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai ORDER BY r.ngayTao DESC")
    List<Review> findBySanPhamIdAndTrangThaiOrderByNgayTaoDesc(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sanPham.id = :idSanPham")
    Double getAverageRatingBySanPhamId(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai")
    Double getAverageRatingBySanPhamIdAndTrangThai(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sanPham.id = :idSanPham")
    Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai")
    Long countBySanPhamIdAndTrangThai(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    // Queries theo chi tiết sản phẩm (mới)
    @Query("SELECT r FROM Review r WHERE r.chiTietSanPham.id = :idChiTietSanPham ORDER BY r.ngayTao DESC")
    List<Review> findByChiTietSanPhamIdOrderByNgayTaoDesc(@Param("idChiTietSanPham") Integer idChiTietSanPham);
    
    @Query("SELECT r FROM Review r WHERE r.chiTietSanPham.id = :idChiTietSanPham AND r.trangThai = :trangThai ORDER BY r.ngayTao DESC")
    List<Review> findByChiTietSanPhamIdAndTrangThaiOrderByNgayTaoDesc(@Param("idChiTietSanPham") Integer idChiTietSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.chiTietSanPham.id = :idChiTietSanPham AND r.trangThai = :trangThai")
    Double getAverageRatingByChiTietSanPhamIdAndTrangThai(@Param("idChiTietSanPham") Integer idChiTietSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.chiTietSanPham.id = :idChiTietSanPham AND r.trangThai = :trangThai")
    Long countByChiTietSanPhamIdAndTrangThai(@Param("idChiTietSanPham") Integer idChiTietSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT r FROM Review r WHERE r.trangThai = :trangThai ORDER BY r.ngayTao DESC")
    List<Review> findByTrangThaiOrderByNgayTaoDesc(@Param("trangThai") Integer trangThai);
    
    @Query("SELECT r FROM Review r ORDER BY r.ngayTao DESC")
    List<Review> findAllByOrderByNgayTaoDesc();
}










