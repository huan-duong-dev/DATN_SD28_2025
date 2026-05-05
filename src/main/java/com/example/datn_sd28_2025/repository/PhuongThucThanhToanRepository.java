package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {
    
    /**
     * Tìm phương thức thanh toán theo tên (không phân biệt hoa thường)
     */
    @Query("SELECT p FROM PhuongThucThanhToan p WHERE LOWER(p.tenPhuongThuc) = LOWER(:tenPhuongThuc) AND p.trangThai = 1")
    Optional<PhuongThucThanhToan> findByTenPhuongThucIgnoreCase(@Param("tenPhuongThuc") String tenPhuongThuc);
    
    /**
     * Tìm phương thức thanh toán theo tên chứa (không phân biệt hoa thường)
     */
    @Query("SELECT p FROM PhuongThucThanhToan p WHERE LOWER(p.tenPhuongThuc) LIKE LOWER(CONCAT('%', :tenPhuongThuc, '%')) AND p.trangThai = 1")
    Optional<PhuongThucThanhToan> findByTenPhuongThucContainingIgnoreCase(@Param("tenPhuongThuc") String tenPhuongThuc);
}

