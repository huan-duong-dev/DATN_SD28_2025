package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ReviewDTO;
import com.example.datn_sd28_2025.dto.ReviewRequestDTO;
import com.example.datn_sd28_2025.entity.Review;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.repository.ReviewRepository;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.ChiTietHoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    
    public ReviewDTO createReview(ReviewRequestDTO requestDTO) {
        // Lấy sản phẩm (bắt buộc)
        SanPham sanPham = sanPhamRepository.findById(requestDTO.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        // Lấy chi tiết sản phẩm nếu có (tùy chọn)
        ChiTietSanPham chiTietSanPham = null;
        if (requestDTO.getIdChiTietSanPham() != null) {
            chiTietSanPham = chiTietSanPhamRepository.findById(requestDTO.getIdChiTietSanPham())
                    .orElse(null); // Không throw exception, chỉ set null nếu không tìm thấy
            // Validate chi tiết sản phẩm thuộc về sản phẩm đúng
            if (chiTietSanPham != null && !chiTietSanPham.getSanPham().getId().equals(sanPham.getId())) {
                throw new RuntimeException("Chi tiết sản phẩm không thuộc về sản phẩm này");
            }
        }
        
        // Validate guest information if idNguoiDung is null
        if (requestDTO.getIdNguoiDung() == null) {
            if (requestDTO.getTenNguoiDung() == null || requestDTO.getTenNguoiDung().trim().isEmpty()) {
                throw new RuntimeException("Tên người dùng không được để trống cho khách");
            }
        }
        
        // Nếu có rating, validate user đã mua sản phẩm (chỉ khi có idNguoiDung)
        if (requestDTO.getRating() != null && requestDTO.getIdNguoiDung() != null) {
            // Rating chỉ được phép khi user đã mua sản phẩm
            // Validation này sẽ được thực hiện ở frontend (TheoDoiDonHangPage)
            // Backend chỉ lưu rating nếu được gửi lên
        }
        
        Review review = Review.builder()
                .sanPham(sanPham)
                .chiTietSanPham(chiTietSanPham)
                .idNguoiDung(requestDTO.getIdNguoiDung())
                .tenNguoiDung(requestDTO.getTenNguoiDung())
                .rating(requestDTO.getRating()) // Có thể null nếu chỉ bình luận
                .comment(requestDTO.getComment())
                .ngayTao(LocalDateTime.now())
                .trangThai(0) // 0: CHO_DUYET (Mặc định chờ duyệt)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview, requestDTO);
    }
    
    public List<ReviewDTO> getReviewsBySanPhamId(Integer idSanPham) {
        // Tạm thời hiển thị tất cả reviews (kể cả chờ duyệt) để test
        // Có thể sửa lại sau để chỉ hiển thị reviews đã duyệt (trangThai = 1)
        List<Review> reviews = reviewRepository.findBySanPhamIdOrderByNgayTaoDesc(idSanPham);
        // Filter: chỉ lấy reviews đã duyệt (1) hoặc chờ duyệt (0), bỏ qua từ chối (2)
        reviews = reviews.stream()
                .filter(r -> r.getTrangThai() == null || r.getTrangThai() == 0 || r.getTrangThai() == 1)
                .collect(Collectors.toList());
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    public Double getAverageRating(Integer idSanPham) {
        return reviewRepository.getAverageRatingBySanPhamIdAndTrangThai(idSanPham, 1); // 1: DA_DUYET
    }
    
    public Long getReviewCount(Integer idSanPham) {
        return reviewRepository.countBySanPhamIdAndTrangThai(idSanPham, 1); // 1: DA_DUYET
    }
    
    // Kiểm tra khách hàng đã mua sản phẩm chưa
    public boolean hasCustomerPurchasedProduct(Integer khachHangId, Integer sanPhamId) {
        if (khachHangId == null || sanPhamId == null) {
            return false;
        }
        return chiTietHoaDonRepository.hasCustomerPurchasedProduct(khachHangId, sanPhamId);
    }
    
    // Lấy đánh giá theo chi tiết sản phẩm
    public List<ReviewDTO> getReviewsByChiTietSanPhamId(Integer idChiTietSanPham) {
        List<Review> reviews = reviewRepository.findByChiTietSanPhamIdAndTrangThaiOrderByNgayTaoDesc(idChiTietSanPham, 1); // 1: DA_DUYET
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    // Lấy điểm trung bình theo chi tiết sản phẩm
    public Double getAverageRatingByChiTietSanPhamId(Integer idChiTietSanPham) {
        return reviewRepository.getAverageRatingByChiTietSanPhamIdAndTrangThai(idChiTietSanPham, 1); // 1: DA_DUYET
    }
    
    // Lấy số lượng đánh giá theo chi tiết sản phẩm
    public Long getReviewCountByChiTietSanPhamId(Integer idChiTietSanPham) {
        return reviewRepository.countByChiTietSanPhamIdAndTrangThai(idChiTietSanPham, 1); // 1: DA_DUYET
    }
    
    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }
    
    // Lấy tất cả đánh giá chờ duyệt
    public List<ReviewDTO> getReviewsChoDuyet() {
        List<Review> reviews = reviewRepository.findByTrangThaiOrderByNgayTaoDesc(0); // 0: CHO_DUYET
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    // Duyệt đánh giá
    public void approveReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(1); // 1: DA_DUYET
        review.setNgayDuyet(LocalDateTime.now());
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Từ chối đánh giá
    public void rejectReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(2); // 2: TU_CHOI
        review.setNgayDuyet(LocalDateTime.now());
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Đặt lại chờ duyệt
    public void resetReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(0); // 0: CHO_DUYET
        review.setNgayDuyet(null);
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Lấy tất cả đánh giá cho admin (bao gồm tất cả trạng thái)
    public List<ReviewDTO> getAllReviewsForAdmin() {
        List<Review> reviews = reviewRepository.findAllByOrderByNgayTaoDesc();
        
        // Update old reviews that don't have trangThai
        for (Review review : reviews) {
            if (review.getTrangThai() == null) {
                review.setTrangThai(0); // 0: CHO_DUYET
                reviewRepository.save(review);
                System.out.println("Updated review " + review.getReviewId() + " with default status CHO_DUYET");
            }
        }
        
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    private ReviewDTO convertToDTO(Review review, ReviewRequestDTO requestDTO) {
        String tenNguoiDung;
        
        // Ưu tiên sử dụng tenNguoiDung từ database nếu có
        if (review.getTenNguoiDung() != null && !review.getTenNguoiDung().trim().isEmpty()) {
            tenNguoiDung = review.getTenNguoiDung();
        } else if (review.getIdNguoiDung() == null) {
            // Guest user - no name provided
            tenNguoiDung = "Khách";
        } else {
            // Registered user - no name in database, use placeholder
            tenNguoiDung = "Người dùng " + review.getIdNguoiDung();
        }
        
        Integer idSanPham = review.getSanPham() != null ? review.getSanPham().getId() : null;
        Integer idChiTietSanPham = review.getChiTietSanPham() != null ? review.getChiTietSanPham().getId() : null;
        
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .idSanPham(idSanPham)
                .idChiTietSanPham(idChiTietSanPham)
                .idNguoiDung(review.getIdNguoiDung())
                .tenNguoiDung(tenNguoiDung)
                .rating(review.getRating())
                .comment(review.getComment())
                .ngayTao(review.getNgayTao())
                .trangThai(review.getTrangThai() != null ? review.getTrangThai() : 0) // 0: CHO_DUYET
                .ngayDuyet(review.getNgayDuyet())
                .ghiChuDuyet(review.getGhiChuDuyet())
                .build();
    }
}










