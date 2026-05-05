package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.*;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.entity.Ram;
import com.example.datn_sd28_2025.entity.Rom;
import com.example.datn_sd28_2025.entity.MauSac;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.TrangThaiTrackingRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.repository.ChiTietThanhToanRepository;
import com.example.datn_sd28_2025.repository.PhuongThucThanhToanRepository;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.entity.TrangThaiTracking;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.entity.ChiTietThanhToan;
import com.example.datn_sd28_2025.entity.PhuongThucThanhToan;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import com.example.datn_sd28_2025.repository.KhachHangGiamGiaRepository;
import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import com.example.datn_sd28_2025.repository.PhieuGiamGiaRepository;
import com.example.datn_sd28_2025.entity.PhieuGiamGia;
import com.example.datn_sd28_2025.service.PhieuGiamGiaService;
import com.example.datn_sd28_2025.repository.DotGiamGiaSanPhamRepository;
import com.example.datn_sd28_2025.entity.DotGiamGiaSanPham;
import com.example.datn_sd28_2025.entity.KhuyenMai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;
import java.util.Optional;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private ImeiDaBanService imeiDaBanService;

    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @Autowired
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    @Autowired
    private TrangThaiTrackingRepository trangThaiTrackingRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private ChiTietThanhToanRepository chiTietThanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private com.example.datn_sd28_2025.service.NotificationService notificationService;

    @Override
    public List<HoaDonDTO> getAll() {
        return hoaDonRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public HoaDonDTO getById(Integer id) {
        return hoaDonRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public HoaDonDTO getByMaHoaDon(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<HoaDonDTO> getByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangId(khachHangId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByTrangThai(Integer trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByLoaiHoaDon(String loaiHoaDon) {
        return hoaDonRepository.findByLoaiHoaDon(loaiHoaDon)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.findByNgayTaoBetween(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Page<HoaDonDTO> searchHoaDon(String keyword, Pageable pageable) {
        return hoaDonRepository.searchHoaDon(keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public HoaDonDTO createOrder(PosOrderRequest request) {
        // Create the main invoice
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHangId(request.getHoaDon().getKhachHangId());
        hoaDon.setNhanVienId(request.getHoaDon().getNhanVienId());
        hoaDon.setPhieuGiamGiaId(request.getHoaDon().getPhieuGiamGiaId());
        hoaDon.setTongTien(request.getHoaDon().getTongTien());
        hoaDon.setTenKhachHang(request.getHoaDon().getTenKhachHang());
        hoaDon.setSoDienThoai(request.getHoaDon().getSoDienThoai());
        hoaDon.setDiaChi(request.getHoaDon().getDiaChi());
        hoaDon.setTongTienSauGiam(request.getHoaDon().getTongTienSauGiam());
        hoaDon.setNgayThanhToan(request.getHoaDon().getNgayThanhToan());
        hoaDon.setLoaiHoaDon(request.getHoaDon().getLoaiHoaDon());
        hoaDon.setPhuongThucGiaoHang(request.getHoaDon().getPhuongThucGiaoHang());
        
        // Set default payment method for POS orders (BAN_THUONG) if not provided
        String phuongThucThanhToan = request.getHoaDon().getPhuongThucThanhToan();
        if (phuongThucThanhToan == null || phuongThucThanhToan.trim().isEmpty()) {
            if ("BAN_THUONG".equals(request.getHoaDon().getLoaiHoaDon()) || 
                "NORMAL".equals(request.getHoaDon().getLoaiHoaDon())) {
                phuongThucThanhToan = "CASH"; // Tiền mặt
            }
        }
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        
        // Calculate and set phuongThucNhanHang
        hoaDon.setPhuongThucNhanHang(OrderStatusUtil.calculatePhuongThucNhanHang(
            request.getHoaDon().getLoaiHoaDon(),
            request.getHoaDon().getPhuongThucGiaoHang()
        ));

        // Set status based on order type
        hoaDon.setTrangThai(OrderStatusUtil.getInitialStatus(
                request.getHoaDon().getLoaiHoaDon()
        ));
        hoaDon.setGhiChu(request.getHoaDon().getGhiChu());
        // Lấy tên người đăng nhập hiện tại, hoặc "System" nếu không có
        hoaDon.setNguoiTao(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());

        // Save the invoice first to get the ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Create ChiTietHoaDon records
        if (request.getChiTietHoaDon() != null && !request.getChiTietHoaDon().isEmpty()) {
            for (ChiTietHoaDonDTO chiTietDTO : request.getChiTietHoaDon()) {
                HoaDonCt hoaDonCt = new HoaDonCt();
                hoaDonCt.setHoaDon(savedHoaDon);

                // Get ChiTietSanPham
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại: " + chiTietDTO.getSanPhamId()));

                // Validate product status - allow selling products at old price in current session
                // Check if product detail (CTSP) is inactive - chỉ cảnh báo, không chặn
                if (chiTietSanPham.getTrangThai() == null || chiTietSanPham.getTrangThai() != 1) {
                    System.out.println("WARNING: Selling inactive product detail '" +
                            (chiTietSanPham.getSanPham() != null ? chiTietSanPham.getSanPham().getTenSanPham() : "N/A") +
                            "' - allowed because it was added to cart before status change");
                }

                // Check if parent product (SP) is inactive - chỉ cảnh báo, không chặn
                if (chiTietSanPham.getSanPham() != null) {
                    if (chiTietSanPham.getSanPham().getTrangThai() == null || chiTietSanPham.getSanPham().getTrangThai() != 1) {
                        System.out.println("WARNING: Selling inactive product '" + chiTietSanPham.getSanPham().getTenSanPham() +
                                "' - allowed because it was added to cart before status change");
                    }
                }

                hoaDonCt.setChiTietSanPham(chiTietSanPham);

                hoaDonCt.setDonGia(BigDecimal.valueOf(chiTietDTO.getDonGia()));
                hoaDonCt.setThanhTien(BigDecimal.valueOf(chiTietDTO.getThanhTien()));
                hoaDonCt.setTrangThai(1); // Active

                HoaDonCt savedHoaDonCt = hoaDonCtRepository.save(hoaDonCt);

                // Update soLuongDaBan for discount campaigns if product was sold with discount
                // Check if donGia < giaGoc (has discount)
                BigDecimal giaGoc = chiTietSanPham.getGiaBan();
                BigDecimal donGia = BigDecimal.valueOf(chiTietDTO.getDonGia());
                if (giaGoc != null && donGia.compareTo(giaGoc) < 0) {
                    // Product was sold with discount, find active discount campaign and update soLuongDaBan
                    try {
                        LocalDateTime now = LocalDateTime.now();
                        List<DotGiamGiaSanPham> activeCampaigns = dotGiamGiaSanPhamRepository.findActiveByChiTietSanPham(
                                chiTietSanPham.getId(), now);

                        // Find the campaign that matches the discount percentage used
                        BigDecimal discountPercent = giaGoc.subtract(donGia)
                                .divide(giaGoc, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));

                        for (DotGiamGiaSanPham campaign : activeCampaigns) {
                            // Check if this campaign matches the discount
                            BigDecimal campaignPercent = campaign.getPhanTramGiam();
                            if (campaignPercent == null && campaign.getKhuyenMai() != null) {
                                campaignPercent = campaign.getKhuyenMai().getPhanTramGiam();
                            }

                            if (campaignPercent != null) {
                                // Check if discount matches (within 0.01% tolerance)
                                if (Math.abs(campaignPercent.doubleValue() - discountPercent.doubleValue()) < 0.01) {
                                    // Match found - update soLuongDaBan
                                    // Only update if there's remaining quantity
                                    if (campaign.getSoLuongToiDa() == null ||
                                            (campaign.getSoLuongDaBan() != null ? campaign.getSoLuongDaBan() : 0) < campaign.getSoLuongToiDa()) {

                                        int soLuongDaBan = campaign.getSoLuongDaBan() != null ? campaign.getSoLuongDaBan() : 0;
                                        int soLuongToiDa = campaign.getSoLuongToiDa() != null ? campaign.getSoLuongToiDa() : Integer.MAX_VALUE;
                                        int soLuongTrongDon = chiTietDTO.getSoLuong();

                                        // Only count up to remaining quantity
                                        int soLuongConLai = soLuongToiDa - soLuongDaBan;
                                        int soLuongApDung = Math.min(soLuongTrongDon, soLuongConLai);

                                        if (soLuongApDung > 0) {
                                            int newSoLuongDaBan = soLuongDaBan + soLuongApDung;
                                            campaign.setSoLuongDaBan(newSoLuongDaBan);

                                            // Nếu đã hết số lượng (soLuongDaBan >= soLuongToiDa), xóa khỏi đợt giảm giá
                                            if (campaign.getSoLuongToiDa() != null && newSoLuongDaBan >= campaign.getSoLuongToiDa()) {
                                                dotGiamGiaSanPhamRepository.delete(campaign);
                                                System.out.println("Campaign " + campaign.getId() + " reached max quantity (" +
                                                        newSoLuongDaBan + "/" + campaign.getSoLuongToiDa() + "). Removed from promotion.");
                                            } else {
                                                dotGiamGiaSanPhamRepository.save(campaign);
                                                System.out.println("Updated soLuongDaBan for campaign " + campaign.getId() +
                                                        ": " + newSoLuongDaBan + "/" + campaign.getSoLuongToiDa());
                                            }
                                        }
                                        break; // Only update first matching campaign
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Log error but don't fail the order
                        System.err.println("Error updating soLuongDaBan: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Update stock quantity
                Integer currentQuantity = chiTietSanPham.getSoLuong();
                if (currentQuantity == null) {
                    currentQuantity = 0; // Default to 0 if null
                }
                int newQuantity = currentQuantity - chiTietDTO.getSoLuong();
                // Tạm thời cho phép bán âm kho để test
                // if (newQuantity < 0) {
                //     throw new RuntimeException("Không đủ hàng trong kho cho sản phẩm: " + chiTietSanPham.getSanPham().getTenSanPham());
                // }
                chiTietSanPham.setSoLuong(newQuantity);
                chiTietSanPhamRepository.save(chiTietSanPham);

                // Handle IMEI sales if IMEIs are provided
                System.out.println("Processing IMEIs for product: " + chiTietSanPham.getSanPham().getTenSanPham());
                System.out.println("Selected IMEIs: " + chiTietDTO.getSelectedImeis());

                if (chiTietDTO.getSelectedImeis() != null && !chiTietDTO.getSelectedImeis().isEmpty()) {
                    System.out.println("Marking " + chiTietDTO.getSelectedImeis().size() + " IMEIs as sold");
                    for (String imei : chiTietDTO.getSelectedImeis()) {
                        System.out.println("Marking IMEI as sold: " + imei);
                        try {
                            imeiDaBanService.markImeiAsSold(imei, savedHoaDonCt.getId());
                        } catch (RuntimeException e) {
                            // IMEI marking is critical - if it fails, rollback the transaction
                            System.err.println("Error marking IMEI as sold: " + imei + " - " + e.getMessage());
                            throw new RuntimeException("Không thể đánh dấu IMEI " + imei + " đã bán: " + e.getMessage(), e);
                        }
                    }
                    System.out.println("Successfully marked all IMEIs as sold");
                } else {
                    System.out.println("No IMEIs to process for this product");
                }
            }
        }

        // Mark voucher as used if applicable
        System.out.println("=== VOUCHER CHECK ===");
        System.out.println("PhieuGiamGiaId: " + request.getHoaDon().getPhieuGiamGiaId());
        System.out.println("KhachHangId: " + request.getHoaDon().getKhachHangId());

        if (request.getHoaDon().getPhieuGiamGiaId() != null) {
            try {
                if (request.getHoaDon().getKhachHangId() != null) {
                    // Khách hàng có tài khoản - xử lý voucher riêng tư hoặc công khai
                    System.out.println("Attempting to mark voucher as used for customer...");
                    markVoucherAsUsed(request.getHoaDon().getKhachHangId(), request.getHoaDon().getPhieuGiamGiaId());
                    System.out.println("✅ Voucher marked as used for customer: " + request.getHoaDon().getKhachHangId() + ", voucher: " + request.getHoaDon().getPhieuGiamGiaId());
                } else {
                    // Khách vãng lai - chỉ xử lý voucher công khai
                    System.out.println("Processing voucher for walk-in customer (no customer ID)...");
                    markVoucherAsUsedForWalkIn(request.getHoaDon().getPhieuGiamGiaId());
                    System.out.println("✅ Voucher marked as used for walk-in customer, voucher: " + request.getHoaDon().getPhieuGiamGiaId());
                }
            } catch (Exception e) {
                System.out.println("❌ Error marking voucher as used: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the order creation if voucher marking fails
            }
        } else {
            System.out.println("ℹ️ No voucher used in this order");
        }

        // Tạo tracking record cho POS order với thông tin người xử lý
        try {
            String nguoiThucHien = savedHoaDon.getNguoiTao();
            if (nguoiThucHien == null || nguoiThucHien.trim().isEmpty()) {
                nguoiThucHien = "Hệ thống";
            }
            
            TrangThaiTracking tracking = new TrangThaiTracking(
                savedHoaDon.getId(),
                savedHoaDon.getTrangThai(),
                OrderStatusUtil.getStatusName(savedHoaDon.getTrangThai()),
                "Hóa đơn POS được tạo bởi: " + nguoiThucHien,
                nguoiThucHien
            );
            trangThaiTrackingRepository.save(tracking);
            System.out.println("✅ Created tracking record for POS order: " + savedHoaDon.getMaHoaDon() + " by " + nguoiThucHien);
        } catch (Exception e) {
            System.out.println("⚠️ Error creating tracking record for POS order: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the order creation if tracking fails
        }

        // Tự động tạo lịch sử thanh toán
        try {
            System.out.println("💰 Creating payment history for order: " + savedHoaDon.getMaHoaDon());
            System.out.println("💰 Payment method: " + request.getHoaDon().getPhuongThucThanhToan());
            System.out.println("💰 Total amount: " + request.getHoaDon().getTongTienSauGiam());
            createPaymentHistory(savedHoaDon, request.getHoaDon().getPhuongThucThanhToan(), 
                    request.getHoaDon().getTongTienSauGiam(), request.getHoaDon());
        } catch (org.springframework.dao.DataIntegrityViolationException | org.hibernate.exception.ConstraintViolationException e) {
            // Database constraint violations should rollback the transaction
            System.err.println("❌ Database error creating payment history: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo lịch sử thanh toán: " + e.getMessage(), e);
        } catch (Exception e) {
            // Other errors can be logged but don't fail order creation
            System.err.println("⚠️ Non-critical error creating payment history: " + e.getMessage());
            e.printStackTrace();
        }

        return convertToDto(savedHoaDon);
    }

    @Override
    @Transactional
    public OnlineOrderResponse createOnlineOrder(OnlineOrderRequest request) {
        try {
            // Create the main invoice
            HoaDon hoaDon = new HoaDon();
            // Set khachHangId from request (null if customer not logged in, otherwise use provided ID)
            hoaDon.setKhachHangId(request.getIdKhachHang()); // TH1: null nếu chưa đăng nhập, TH2: có ID nếu đã đăng nhập
            hoaDon.setNhanVienId(null); // No staff for online orders
            hoaDon.setPhieuGiamGiaId(request.getPhieuGiamGiaId());
            hoaDon.setTongTien(request.getTongTien() != null ? request.getTongTien().doubleValue() : 0.0);
            hoaDon.setTenKhachHang(request.getTenKhachHang());
            hoaDon.setSoDienThoai(request.getSoDienThoai());
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : null);
            System.out.println("📧 Email from request: '" + request.getEmail() + "' -> Saved: '" + hoaDon.getEmail() + "'");
            hoaDon.setTinhThanh(request.getTinhThanh());
            hoaDon.setQuanHuyen(request.getQuanHuyen());
            hoaDon.setPhuongThucGiaoHang(request.getPhuongThucGiaoHang());
            hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
            hoaDon.setPhiVanChuyen(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen().doubleValue() : 0.0);
            hoaDon.setTongTienSauGiam(request.getTongTienSauGiam() != null ? request.getTongTienSauGiam().doubleValue() : 0.0);
            hoaDon.setLoaiHoaDon("ONLINE");
            
            // Calculate and set phuongThucNhanHang
            hoaDon.setPhuongThucNhanHang(OrderStatusUtil.calculatePhuongThucNhanHang(
                "ONLINE",
                request.getPhuongThucGiaoHang()
            ));
            
            hoaDon.setTrangThai(OrderStatusUtil.getInitialStatus("ONLINE")); // CHO_XAC_NHAN = 0
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setNguoiTao("ONLINE_CUSTOMER");

            // Save the invoice first to get the ID and maHoaDon
            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            // Create ChiTietHoaDon records
            if (request.getChiTietDonHang() != null && !request.getChiTietDonHang().isEmpty()) {
                for (OnlineOrderRequest.ChiTietDonHangRequest chiTietDTO : request.getChiTietDonHang()) {
                    HoaDonCt hoaDonCt = new HoaDonCt();
                    hoaDonCt.setHoaDon(savedHoaDon);

                    // Get ChiTietSanPham
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getChiTietSanPhamId())
                            .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại: " + chiTietDTO.getChiTietSanPhamId()));

                    hoaDonCt.setChiTietSanPham(chiTietSanPham);
                    hoaDonCt.setDonGia(chiTietDTO.getGia());
                    hoaDonCt.setThanhTien(chiTietDTO.getThanhTien());
                    hoaDonCt.setTrangThai(1); // Active
                    // Note: HoaDonCt doesn't have soLuong field, quantity is stored in ChiTietSanPham

                    HoaDonCt savedHoaDonCt = hoaDonCtRepository.save(hoaDonCt);

                    // NOTE: For online orders, DO NOT reduce stock quantity here
                    // Stock will be reduced when admin confirms the order (status changes from 0 to 1)
                    // This allows admin to check inventory and assign IMEIs before confirming
                }
            }

            // Mark voucher as used if applicable
            if (request.getPhieuGiamGiaId() != null) {
                try {
                    // For online orders, mark as used for walk-in (no customer ID)
                    markVoucherAsUsedForWalkIn(request.getPhieuGiamGiaId());
                    System.out.println("✅ Voucher marked as used for online order, voucher: " + request.getPhieuGiamGiaId());
                } catch (Exception e) {
                    System.out.println("❌ Error marking voucher as used: " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the order creation if voucher marking fails
                }
            }

            // Create tracking record
            try {
                TrangThaiTracking tracking = new TrangThaiTracking(
                    savedHoaDon.getId(),
                    savedHoaDon.getTrangThai(),
                    OrderStatusUtil.getStatusName(savedHoaDon.getTrangThai()),
                    "Đơn hàng online được tạo",
                    "Hệ thống"
                );
                trangThaiTrackingRepository.save(tracking);
            } catch (Exception e) {
                System.out.println("⚠️ Error creating tracking record: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the order creation if tracking fails
            }

            // Send notification to admin
            try {
                notificationService.createOrderNotification(
                    savedHoaDon.getMaHoaDon(),
                    savedHoaDon.getTenKhachHang(),
                    savedHoaDon.getTongTienSauGiam() != null ? savedHoaDon.getTongTienSauGiam() : savedHoaDon.getTongTien()
                );
                System.out.println("✅ Notification created for admin - order: " + savedHoaDon.getMaHoaDon());
            } catch (Exception e) {
                System.out.println("⚠️ Error creating admin notification: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the order creation if notification fails
            }
            
            // Send notification to customer (if logged in)
            if (savedHoaDon.getKhachHangId() != null) {
                try {
                    notificationService.createCustomerOrderNotification(
                        savedHoaDon.getMaHoaDon(),
                        savedHoaDon.getKhachHangId(),
                        savedHoaDon.getId()
                    );
                    System.out.println("✅ Customer notification created for order: " + savedHoaDon.getMaHoaDon() + ", customerId: " + savedHoaDon.getKhachHangId());
                } catch (Exception e) {
                    System.out.println("⚠️ Error creating customer notification: " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the order creation if notification fails
                }
            } else {
                System.out.println("ℹ️ No customer notification created - customer not logged in (order: " + savedHoaDon.getMaHoaDon() + ")");
            }

            // Tự động tạo lịch sử thanh toán cho đơn online
            try {
                createPaymentHistory(savedHoaDon, request.getPhuongThucThanhToan(), 
                        request.getTongTienSauGiam() != null ? request.getTongTienSauGiam().doubleValue() : null, null);
            } catch (Exception e) {
                System.err.println("❌ Error creating payment history for online order: " + e.getMessage());
                e.printStackTrace();
                // Không fail order creation nếu tạo lịch sử thanh toán lỗi
            }

            // Return response
            OnlineOrderResponse response = new OnlineOrderResponse();
            response.setMaHoaDon(savedHoaDon.getMaHoaDon());
            response.setMessage("Đơn hàng online đã được tạo thành công");
            response.setStatus("SUCCESS");

            System.out.println("✅ Online order created successfully: " + savedHoaDon.getMaHoaDon());
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error creating online order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo đơn hàng online: " + e.getMessage(), e);
        }
    }

    @Override
    public HoaDonDTO updateHoaDon(Integer id, HoaDonDTO hoaDonDTO) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setKhachHangId(hoaDonDTO.getKhachHangId());
                    existingHoaDon.setNhanVienId(hoaDonDTO.getNhanVienId());
                    existingHoaDon.setPhieuGiamGiaId(hoaDonDTO.getPhieuGiamGiaId());
                    existingHoaDon.setTongTien(hoaDonDTO.getTongTien());
                    existingHoaDon.setTenKhachHang(hoaDonDTO.getTenKhachHang());
                    existingHoaDon.setSoDienThoai(hoaDonDTO.getSoDienThoai());
                    existingHoaDon.setDiaChi(hoaDonDTO.getDiaChi());
                    existingHoaDon.setTongTienSauGiam(hoaDonDTO.getTongTienSauGiam());
                    existingHoaDon.setNgayThanhToan(hoaDonDTO.getNgayThanhToan());
                    existingHoaDon.setLoaiHoaDon(hoaDonDTO.getLoaiHoaDon());
                    existingHoaDon.setTrangThai(hoaDonDTO.getTrangThai());
                    existingHoaDon.setGhiChu(hoaDonDTO.getGhiChu());
                    existingHoaDon.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public HoaDonDTO updateTrangThai(Integer id, Integer trangThai) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setTrangThai(trangThai);
                    existingHoaDon.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public void deleteHoaDon(Integer id) {
        if (!hoaDonRepository.existsById(id)) {
            throw new RuntimeException("Hóa đơn không tồn tại với id: " + id);
        }
        hoaDonRepository.deleteById(id);
    }

    @Override
    public Long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.countByNgayTaoBetween(startDate, endDate);
    }

    @Override
    public Double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Double revenue = hoaDonRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    private HoaDonDTO convertToDto(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setKhachHangId(hoaDon.getKhachHangId());
        dto.setNhanVienId(hoaDon.getNhanVienId());
        dto.setPhieuGiamGiaId(hoaDon.getPhieuGiamGiaId());
        dto.setTongTien(hoaDon.getTongTien());
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setTongTienSauGiam(hoaDon.getTongTienSauGiam());
        dto.setNgayThanhToan(hoaDon.getNgayThanhToan());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setPhuongThucNhanHang(hoaDon.getPhuongThucNhanHang());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setGhiChu(hoaDon.getGhiChu());
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());
        dto.setNguoiTao(hoaDon.getNguoiTao());
        dto.setNguoiCapNhat(hoaDon.getNguoiCapNhat());
        
        // Lấy thông tin nhân viên xử lý đơn hàng
        if (hoaDon.getNhanVienId() != null) {
            System.out.println("🔍 Loading nhanVien for hoaDonId: " + hoaDon.getId() + ", nhanVienId: " + hoaDon.getNhanVienId());
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(hoaDon.getNhanVienId());
            if (nhanVienOpt.isPresent()) {
                NhanVien nhanVien = nhanVienOpt.get();
                dto.setMaNhanVien(nhanVien.getMaNhanVien());
                dto.setTenNhanVien(nhanVien.getHoTen());
                System.out.println("✅ Set maNhanVien: " + nhanVien.getMaNhanVien() + ", tenNhanVien: " + nhanVien.getHoTen());
            } else {
                System.out.println("⚠️ NhanVien not found with id: " + hoaDon.getNhanVienId());
            }
        } else {
            System.out.println("⚠️ HoaDon " + hoaDon.getMaHoaDon() + " has no nhanVienId");
        }
        
        // Lấy lịch sử thanh toán
        dto.setLichSuThanhToan(getLichSuThanhToan(hoaDon.getId()));
        
        // Lấy chi tiết hóa đơn với thông tin sản phẩm đầy đủ
        dto.setChiTietHoaDonList(getChiTietHoaDonList(hoaDon.getId()));
        
        return dto;
    }
    
    /**
     * Lấy danh sách chi tiết hóa đơn với thông tin sản phẩm đầy đủ (hình ảnh và tên)
     */
    private List<ChiTietHoaDonDTO> getChiTietHoaDonList(Integer hoaDonId) {
        List<ChiTietHoaDonDTO> chiTietList = new ArrayList<>();
        
        try {
            List<HoaDonCt> hoaDonChiTiets = hoaDonCtRepository.findByIdHoaDon(hoaDonId);
            
            for (HoaDonCt hoaDonCt : hoaDonChiTiets) {
                ChiTietHoaDonDTO chiTietDto = new ChiTietHoaDonDTO();
                chiTietDto.setId(hoaDonCt.getId());
                chiTietDto.setHoaDonId(hoaDonId);
                chiTietDto.setDonGia(hoaDonCt.getDonGia() != null ? hoaDonCt.getDonGia().doubleValue() : 0.0);
                chiTietDto.setThanhTien(hoaDonCt.getThanhTien() != null ? hoaDonCt.getThanhTien().doubleValue() : 0.0);
                chiTietDto.setSoLuong(1); // Default to 1, can be calculated from IMEI count if needed
                
                // Lấy thông tin sản phẩm
                ChiTietSanPham chiTietSanPham = hoaDonCt.getChiTietSanPham();
                if (chiTietSanPham != null) {
                    SanPham sanPham = chiTietSanPham.getSanPham();
                    if (sanPham != null) {
                        chiTietDto.setTenSanPham(sanPham.getTenSanPham());
                        chiTietDto.setSanPhamId(sanPham.getId());
                    }
                    
                    // Lấy thông tin variant (RAM, ROM, Màu sắc)
                    if (chiTietSanPham.getRam() != null) {
                        chiTietDto.setTenRam(chiTietSanPham.getRam().getTenRam());
                    }
                    if (chiTietSanPham.getRom() != null) {
                        chiTietDto.setTenRom(chiTietSanPham.getRom().getDungLuong());
                    }
                    if (chiTietSanPham.getMauSac() != null) {
                        chiTietDto.setTenMauSac(chiTietSanPham.getMauSac().getTenMau());
                    }
                    
                    // Lấy hình ảnh sản phẩm
                    try {
                        List<HinhAnh> hinhAnhs = hinhAnhRepository.findByChiTietSanPhamId(chiTietSanPham.getId());
                        if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                            // Ưu tiên Cloudinary URL hoặc https://
                            String urlAnh = null;
                            for (HinhAnh hinhAnh : hinhAnhs) {
                                String testUrl = hinhAnh.getUrlAnh();
                                if (testUrl != null && !testUrl.trim().isEmpty()) {
                                    if (testUrl.contains("cloudinary.com") || testUrl.startsWith("https://")) {
                                        urlAnh = testUrl;
                                        break;
                                    }
                                    if (urlAnh == null) {
                                        urlAnh = testUrl;
                                    }
                                }
                            }
                            chiTietDto.setHinhAnh(urlAnh);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading image for product: " + e.getMessage());
                    }
                    
                    // Lấy IMEI đã bán
                    if (hoaDonCt.getImeiDaBans() != null && !hoaDonCt.getImeiDaBans().isEmpty()) {
                        List<String> imeis = hoaDonCt.getImeiDaBans().stream()
                                .map(ImeiDaBan::getImei)
                                .filter(imei -> imei != null && !imei.trim().isEmpty())
                                .collect(Collectors.toList());
                        chiTietDto.setSelectedImeis(imeis);
                        // Set soLuong based on IMEI count
                        chiTietDto.setSoLuong(imeis.size());
                    }
                }
                
                chiTietList.add(chiTietDto);
            }
        } catch (Exception e) {
            System.err.println("Error loading chiTietHoaDonList for hoaDonId " + hoaDonId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return chiTietList;
    }
    
    /**
     * Lấy lịch sử thanh toán của hóa đơn
     */
    private List<ChiTietThanhToanDTO> getLichSuThanhToan(Integer hoaDonId) {
        try {
            System.out.println("🔍 Loading lichSuThanhToan for hoaDonId: " + hoaDonId);
            List<ChiTietThanhToan> chiTietThanhToanList = chiTietThanhToanRepository
                    .findByHoaDonIdWithPhuongThuc(hoaDonId);
            System.out.println("🔍 Found " + chiTietThanhToanList.size() + " payment records");
            
            List<ChiTietThanhToanDTO> result = chiTietThanhToanList.stream()
                    .map(this::convertToChiTietThanhToanDto)
                    .collect(Collectors.toList());
            
            System.out.println("🔍 Converted to " + result.size() + " payment DTOs");
            for (ChiTietThanhToanDTO dto : result) {
                System.out.println("  - Payment: " + dto.getSoTien() + " VND, Method: " + 
                        (dto.getPhuongThucThanhToan() != null ? dto.getPhuongThucThanhToan().getTenPhuongThuc() : "N/A") + 
                        ", Date: " + dto.getNgayThanhToan());
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error loading lịch sử thanh toán for hoaDonId: " + hoaDonId + " - " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Convert ChiTietThanhToan entity sang DTO
     */
    // Constants cho loại thanh toán
    private static final String LOAI_THANH_TOAN_PAYMENT = "PAYMENT"; // Thanh toán
    private static final String LOAI_THANH_TOAN_REFUND = "REFUND"; // Hoàn phí
    private static final String LOAI_THANH_TOAN_ADDITIONAL_FEE = "ADDITIONAL_FEE"; // Phụ phí
    
    private ChiTietThanhToanDTO convertToChiTietThanhToanDto(ChiTietThanhToan ctt) {
        ChiTietThanhToanDTO dto = ChiTietThanhToanDTO.builder()
                .id(ctt.getId())
                .soTien(ctt.getSoTien())
                .maGiaoDich(ctt.getMaGiaoDich())
                .ngayThanhToan(ctt.getNgayThanhToan())
                .trangThai(ctt.getTrangThai())
                .loaiThanhToan(ctt.getLoaiThanhToan() != null ? ctt.getLoaiThanhToan() : LOAI_THANH_TOAN_PAYMENT)
                .ngayTao(ctt.getNgayTao())
                .ngayCapNhat(ctt.getNgayCapNhat())
                .build();
        
        // Thêm thông tin phương thức thanh toán nếu có
        if (ctt.getPhuongThucThanhToan() != null) {
            PhuongThucThanhToan pttt = ctt.getPhuongThucThanhToan();
            PhuongThucThanhToanDTO ptttDto = PhuongThucThanhToanDTO.builder()
                    .id(pttt.getId())
                    .tenPhuongThuc(pttt.getTenPhuongThuc())
                    .loaiHinhThuc(pttt.getLoaiHinhThuc())
                    .trangThai(pttt.getTrangThai())
                    .ngayTao(pttt.getNgayTao())
                    .ngayCapNhat(pttt.getNgayCapNhat())
                    .build();
            dto.setPhuongThucThanhToan(ptttDto);
            dto.setIdPhuongThucThanhToan(pttt.getId());
        }
        
        // Thêm id hóa đơn
        if (ctt.getHoaDon() != null) {
            dto.setIdDonHang(ctt.getHoaDon().getId());
        }
        
        return dto;
    }
    
    /**
     * Tự động tạo lịch sử thanh toán khi hóa đơn được tạo
     * @param hoaDon Hóa đơn đã được lưu
     * @param phuongThucThanhToanString Tên phương thức thanh toán (string)
     * @param tongTienSauGiam Tổng tiền sau giảm giá
     * @param hoaDonDTO HoaDonDTO để lấy thông tin thanh toán kết hợp (nếu có)
     */
    private void createPaymentHistory(HoaDon hoaDon, String phuongThucThanhToanString, Double tongTienSauGiam, HoaDonDTO hoaDonDTO) {
        if (hoaDon == null) {
            System.out.println("⚠️ Skipping payment history creation - hoaDon is null");
            return;
        }
        
        // Nếu không có tongTienSauGiam, thử lấy từ hoaDon
        if (tongTienSauGiam == null || tongTienSauGiam <= 0) {
            tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : hoaDon.getTongTien();
        }
        
        if (tongTienSauGiam == null || tongTienSauGiam <= 0) {
            System.out.println("⚠️ Skipping payment history creation - invalid amount: " + tongTienSauGiam);
            return;
        }
        
        // Xử lý COD (Cash on Delivery) - Thanh toán khi nhận hàng
        if (phuongThucThanhToanString != null && 
            (phuongThucThanhToanString.equalsIgnoreCase("COD") || 
             phuongThucThanhToanString.equalsIgnoreCase("cod") ||
             phuongThucThanhToanString.contains("COD") ||
             phuongThucThanhToanString.contains("Thu hộ") ||
             phuongThucThanhToanString.contains("Thanh toán khi nhận hàng"))) {
            try {
                // Tìm phương thức thanh toán COD hoặc tạo với tên "Thanh toán khi nhận hàng"
                PhuongThucThanhToan phuongThucThanhToan = findPhuongThucThanhToan("COD");
                if (phuongThucThanhToan == null) {
                    // Thử tìm "Thu hộ" hoặc "Thanh toán khi nhận hàng"
                    phuongThucThanhToan = phuongThucThanhToanRepository.findByTenPhuongThucContainingIgnoreCase("Thu hộ").orElse(null);
                    if (phuongThucThanhToan == null) {
                        phuongThucThanhToan = phuongThucThanhToanRepository.findByTenPhuongThucContainingIgnoreCase("Thanh toán khi nhận hàng").orElse(null);
                    }
                }
                
                if (phuongThucThanhToan == null) {
                    System.out.println("⚠️ COD payment method not found in database, using default");
                    // Tìm "Tiền mặt" làm mặc định cho COD
                    phuongThucThanhToan = phuongThucThanhToanRepository.findByTenPhuongThucIgnoreCase("Tiền mặt").orElse(null);
                }
                
                if (phuongThucThanhToan != null) {
                    ChiTietThanhToan chiTietThanhToan = ChiTietThanhToan.builder()
                            .hoaDon(hoaDon)
                            .phuongThucThanhToan(phuongThucThanhToan)
                            .soTien(BigDecimal.valueOf(tongTienSauGiam))
                            .maGiaoDich("COD_" + System.currentTimeMillis())
                            .ngayThanhToan(null) // Chưa thanh toán, sẽ thanh toán khi nhận hàng
                            .trangThai(0) // Chưa thanh toán (0 = chưa thanh toán, 1 = đã thanh toán)
                            .loaiThanhToan(LOAI_THANH_TOAN_PAYMENT) // COD vẫn là thanh toán
                            .ngayTao(LocalDateTime.now())
                            .ngayCapNhat(LocalDateTime.now())
                            .build();
                    
                    chiTietThanhToanRepository.save(chiTietThanhToan);
                    System.out.println("✅ Created COD payment record: Thanh toán khi nhận hàng - " + tongTienSauGiam + " VND");
                }
            } catch (Exception e) {
                System.err.println("❌ Error creating COD payment history: " + e.getMessage());
                e.printStackTrace();
            }
            return; // Đã xử lý COD, không cần xử lý tiếp
        }
        
        // Chỉ tạo lịch sử thanh toán nếu có phương thức thanh toán (không phải COD)
        if (phuongThucThanhToanString == null || phuongThucThanhToanString.trim().isEmpty()) {
            System.out.println("⚠️ Skipping payment history creation - no payment method specified");
            return;
        }
        
        try {
            // Xử lý thanh toán kết hợp (combined payment)
            if ("combined".equalsIgnoreCase(phuongThucThanhToanString) || "Kết hợp".equalsIgnoreCase(phuongThucThanhToanString)) {
                // Lấy thông tin từ ghiChu hoặc từ hoaDonDTO
                Double tienMat = null;
                Double vnpay = null;
                String maGiaoDich = null;
                
                // Tìm thông tin trong ghiChu
                if (hoaDon.getGhiChu() != null) {
                    String ghiChu = hoaDon.getGhiChu();
                    // Parse từ ghiChu: "Thanh toán kết hợp - Tiền mặt: 1000000 VNPay: 500000 (TXN: ...)"
                    // Pattern linh hoạt hơn để bắt nhiều format
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "(?:Thanh toán kết hợp|Kết hợp)[\\s-]*Tiền mặt:\\s*(\\d+(?:\\.\\d+)?)\\s*VNPay:\\s*(\\d+(?:\\.\\d+)?)\\s*\\(TXN:\\s*([^)]+)\\)"
                    );
                    java.util.regex.Matcher matcher = pattern.matcher(ghiChu);
                    if (matcher.find()) {
                        try {
                            tienMat = Double.parseDouble(matcher.group(1).replaceAll("[^0-9.]", ""));
                            vnpay = Double.parseDouble(matcher.group(2).replaceAll("[^0-9.]", ""));
                            maGiaoDich = matcher.group(3).trim();
                            System.out.println("✅ Parsed combined payment: Tiền mặt=" + tienMat + ", VNPay=" + vnpay + ", TXN=" + maGiaoDich);
                        } catch (NumberFormatException e) {
                            System.err.println("❌ Error parsing combined payment amounts: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Could not parse combined payment from ghiChu: " + ghiChu);
                    }
                }
                
                // Nếu không tìm thấy trong ghiChu, thử lấy từ hoaDonDTO (nếu có)
                if (tienMat == null && hoaDonDTO != null) {
                    // Có thể cần thêm logic để lấy từ DTO nếu có field riêng
                }
                
                // Tạo bản ghi cho tiền mặt
                if (tienMat != null && tienMat > 0) {
                    createSinglePaymentRecord(hoaDon, "Tiền mặt", tienMat, null);
                }
                
                // Tạo bản ghi cho VNPay
                if (vnpay != null && vnpay > 0) {
                    createSinglePaymentRecord(hoaDon, "Chuyển khoản", vnpay, maGiaoDich);
                }
            } else {
                // Thanh toán đơn phương thức
                String maGiaoDich = extractMaGiaoDich(hoaDon.getGhiChu());
                createSinglePaymentRecord(hoaDon, phuongThucThanhToanString, tongTienSauGiam, maGiaoDich);
            }
            
            System.out.println("✅ Payment history created for order: " + hoaDon.getMaHoaDon());
        } catch (Exception e) {
            System.err.println("❌ Error creating payment history: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Tạo một bản ghi thanh toán đơn lẻ
     */
    private void createSinglePaymentRecord(HoaDon hoaDon, String phuongThucThanhToanString, Double soTien, String maGiaoDich) {
        if (soTien == null || soTien <= 0) {
            return;
        }
        
        // Map payment method string sang PhuongThucThanhToan entity
        PhuongThucThanhToan phuongThucThanhToan = findPhuongThucThanhToan(phuongThucThanhToanString);
        
        if (phuongThucThanhToan == null) {
            System.out.println("⚠️ Payment method not found: " + phuongThucThanhToanString + ", skipping payment history");
            return;
        }
        
        // Tạo ChiTietThanhToan
        ChiTietThanhToan chiTietThanhToan = ChiTietThanhToan.builder()
                .hoaDon(hoaDon)
                .phuongThucThanhToan(phuongThucThanhToan)
                .soTien(BigDecimal.valueOf(soTien))
                .maGiaoDich(maGiaoDich != null ? maGiaoDich : generateMaGiaoDich(phuongThucThanhToanString))
                .ngayThanhToan(LocalDateTime.now())
                .trangThai(1) // Đã thanh toán
                .loaiThanhToan(OrderStatusUtil.PAYMENT) // Mặc định là thanh toán
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();
        
        chiTietThanhToanRepository.save(chiTietThanhToan);
        System.out.println("✅ Created payment record: " + phuongThucThanhToanString + " - " + soTien + " VND");
    }
    
    /**
     * Tìm PhuongThucThanhToan entity từ tên phương thức (string)
     */
    private PhuongThucThanhToan findPhuongThucThanhToan(String phuongThucString) {
        if (phuongThucString == null || phuongThucString.trim().isEmpty()) {
            return null;
        }
        
        // Map các tên phương thức phổ biến
        String searchName = phuongThucString.trim();
        
        // Map các giá trị từ frontend sang tên trong database
        java.util.Map<String, String> paymentMethodMap = new java.util.HashMap<>();
        paymentMethodMap.put("cash", "Tiền mặt");
        paymentMethodMap.put("tien_mat", "Tiền mặt");
        paymentMethodMap.put("vnpay", "Chuyển khoản");
        paymentMethodMap.put("zalopay", "Ví điện tử ZaloPay");
        paymentMethodMap.put("momo", "Ví điện tử MoMo");
        paymentMethodMap.put("bank", "Chuyển khoản");
        paymentMethodMap.put("card", "Thẻ tín dụng");
        paymentMethodMap.put("cod", "Thu hộ");
        
        // Kiểm tra trong map trước
        String mappedName = paymentMethodMap.get(searchName.toLowerCase());
        if (mappedName != null) {
            searchName = mappedName;
        }
        
        // Tìm trong database
        Optional<PhuongThucThanhToan> result = phuongThucThanhToanRepository.findByTenPhuongThucIgnoreCase(searchName);
        
        if (result.isPresent()) {
            return result.get();
        }
        
        // Thử tìm theo tên chứa
        result = phuongThucThanhToanRepository.findByTenPhuongThucContainingIgnoreCase(searchName);
        if (result.isPresent()) {
            return result.get();
        }
        
        // Nếu không tìm thấy, thử tìm "Tiền mặt" làm mặc định
        result = phuongThucThanhToanRepository.findByTenPhuongThucIgnoreCase("Tiền mặt");
        return result.orElse(null);
    }
    
    /**
     * Trích xuất mã giao dịch từ ghiChu
     */
    private String extractMaGiaoDich(String ghiChu) {
        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            return null;
        }
        
        // Tìm pattern: "VNPay - TXN123" hoặc "ZaloPay - TXN123" hoặc "TXN: TXN123"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:VNPay|ZaloPay|TXN)[\\s-:]+([A-Z0-9]+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(ghiChu);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Tạo mã giao dịch tự động
     */
    private String generateMaGiaoDich(String phuongThucString) {
        String prefix = "TT";
        if (phuongThucString != null) {
            if (phuongThucString.toLowerCase().contains("vnpay") || phuongThucString.contains("Chuyển khoản")) {
                prefix = "CK";
            } else if (phuongThucString.toLowerCase().contains("zalopay")) {
                prefix = "ZP";
            } else if (phuongThucString.toLowerCase().contains("momo")) {
                prefix = "MM";
            } else if (phuongThucString.toLowerCase().contains("cash") || phuongThucString.contains("Tiền mặt")) {
                prefix = "TM";
            }
        }
        return prefix + System.currentTimeMillis();
    }

    @Override
    public Page<HoaDonDTO> searchHoaDonAdvanced(HoaDonSearchRequestDTO searchRequest) {
        System.out.println("🚀 ===== searchHoaDonAdvanced METHOD CALLED =====");
        System.out.println("🔍 Search Request received: " + searchRequest);
        System.out.println("📅 Date Filter - tuNgay: " + searchRequest.getTuNgay() + ", denNgay: " + searchRequest.getDenNgay());
        
        // Get ALL hóa đơn first (without pagination) - no initial sorting, will sort later
        List<HoaDon> allHoaDons = hoaDonRepository.findAll();
        System.out.println("📊 Total hóa đơn in database: " + allHoaDons.size());
        System.out.println("📊 Sample hóa đơn IDs: " + allHoaDons.stream().limit(10).map(h -> h.getId() + ":" + h.getMaHoaDon()).toList());
        
        // Debug: Show sample ngayTao values
        if (!allHoaDons.isEmpty()) {
            System.out.println("📅 Sample ngayTao values:");
            allHoaDons.stream().limit(5).forEach(h -> {
                System.out.println("  - " + h.getMaHoaDon() + ": ngayTao = " + h.getNgayTao());
            });
        }
        
        // Debug: Check if we're getting all data
        if (allHoaDons.size() < 20) {
            System.out.println("⚠️ WARNING: Only " + allHoaDons.size() + " hóa đơn found, expected more!");
        }

        // Apply filters to get filtered list
        List<HoaDon> filteredHoaDons = allHoaDons.stream()
                .filter(hoaDon -> {
                    // Keyword filter
                    if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
                        String keyword = searchRequest.getKeyword().toLowerCase();
                        boolean matchesKeyword =
                                hoaDon.getMaHoaDon().toLowerCase().contains(keyword) ||
                                        (hoaDon.getTenKhachHang() != null && hoaDon.getTenKhachHang().toLowerCase().contains(keyword)) ||
                                        (hoaDon.getSoDienThoai() != null && hoaDon.getSoDienThoai().contains(keyword));
                        if (!matchesKeyword) return false;
                    }

                    // Trạng thái filter
                    if (searchRequest.getTrangThai() != null) {
                        System.out.println("🔍 Filtering by status: " + searchRequest.getTrangThai() + " vs " + hoaDon.getTrangThai());
                        if (!hoaDon.getTrangThai().equals(searchRequest.getTrangThai())) return false;
                    }

                    // Loại hóa đơn filter
                    if (searchRequest.getLoaiHoaDon() != null && !searchRequest.getLoaiHoaDon().trim().isEmpty()) {
                        if (!hoaDon.getLoaiHoaDon().equals(searchRequest.getLoaiHoaDon())) return false;
                    }

                    // Date range filter - So sánh theo ngày (không tính giờ)
                    if (searchRequest.getTuNgay() != null) {
                        // Chuyển ngayTao và tuNgay về LocalDate để so sánh
                        java.time.LocalDate ngayTaoDate = hoaDon.getNgayTao().toLocalDate();
                        java.time.LocalDate tuNgayDate = searchRequest.getTuNgay().toLocalDate();
                        
                        // Loại bỏ nếu ngày tạo TRƯỚC tuNgay
                        if (ngayTaoDate.isBefore(tuNgayDate)) {
                            System.out.println("  ❌ Filtered out " + hoaDon.getMaHoaDon() + " - ngayTao date (" + ngayTaoDate + ") is before tuNgay date (" + tuNgayDate + ")");
                            return false;
                        }
                    }

                    if (searchRequest.getDenNgay() != null) {
                        // Chuyển ngayTao và denNgay về LocalDate để so sánh
                        java.time.LocalDate ngayTaoDate = hoaDon.getNgayTao().toLocalDate();
                        java.time.LocalDate denNgayDate = searchRequest.getDenNgay().toLocalDate();
                        
                        // Loại bỏ nếu ngày tạo SAU denNgay
                        if (ngayTaoDate.isAfter(denNgayDate)) {
                            System.out.println("  ❌ Filtered out " + hoaDon.getMaHoaDon() + " - ngayTao date (" + ngayTaoDate + ") is after denNgay date (" + denNgayDate + ")");
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("📋 Filtered results: " + filteredHoaDons.size() + " items");
        System.out.println("📋 Sample filtered IDs: " + filteredHoaDons.stream().limit(5).map(h -> h.getId() + ":" + h.getMaHoaDon()).toList());
        
        // Log tất cả các hóa đơn đã được filter (nếu có ít hơn 20)
        if (filteredHoaDons.size() > 0 && filteredHoaDons.size() <= 20) {
            System.out.println("📋 All filtered hóa đơn:");
            filteredHoaDons.forEach(h -> {
                System.out.println("  ✅ " + h.getMaHoaDon() + " - ngayTao: " + h.getNgayTao() + " (date: " + h.getNgayTao().toLocalDate() + ")");
            });
        }

        // Debug: Show tongTien values before sorting
        System.out.println("💰 Before sorting - tongTien values:");
        filteredHoaDons.stream().limit(10).forEach(h -> {
            double displayValue = (h.getTongTienSauGiam() != null) ? h.getTongTienSauGiam() : h.getTongTien();
            System.out.println("  " + h.getMaHoaDon() + ": tongTien=" + h.getTongTien() + ", tongTienSauGiam=" + h.getTongTienSauGiam() + ", displayValue=" + displayValue);
        });

        // Apply sorting
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "ngayTao";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "desc";

        System.out.println("🔍 Sorting by: " + sortBy + " direction: " + sortDirection);

        filteredHoaDons.sort((a, b) -> {
            int result = 0;
            switch (sortBy) {
                case "maHoaDon":
                    result = a.getMaHoaDon().compareTo(b.getMaHoaDon());
                    break;
                case "tenKhachHang":
                    result = (a.getTenKhachHang() != null ? a.getTenKhachHang() : "").compareTo(b.getTenKhachHang() != null ? b.getTenKhachHang() : "");
                    break;
                case "tongTien":
                    // Sắp xếp theo giá trị thực tế được hiển thị (tongTienSauGiam hoặc tongTien)
                    double aValue = (a.getTongTienSauGiam() != null) ? a.getTongTienSauGiam() : a.getTongTien();
                    double bValue = (b.getTongTienSauGiam() != null) ? b.getTongTienSauGiam() : b.getTongTien();

                    // Handle null values
                    if (aValue == 0 && a.getTongTien() == null && a.getTongTienSauGiam() == null) aValue = 0.0;
                    if (bValue == 0 && b.getTongTien() == null && b.getTongTienSauGiam() == null) bValue = 0.0;

                    System.out.println("💰 Sorting: " + a.getMaHoaDon() + "(" + aValue + ") vs " + b.getMaHoaDon() + "(" + bValue + ")");
                    result = Double.compare(aValue, bValue);
                    break;
                case "ngayTao":
                default:
                    result = a.getNgayTao().compareTo(b.getNgayTao());
                    break;
            }
            return "desc".equals(sortDirection) ? -result : result;
        });

        // Debug: Show tongTien values after sorting
        System.out.println("💰 After sorting - tongTien values:");
        filteredHoaDons.stream().limit(10).forEach(h -> {
            double displayValue = (h.getTongTienSauGiam() != null) ? h.getTongTienSauGiam() : h.getTongTien();
            System.out.println("  " + h.getMaHoaDon() + ": displayValue=" + displayValue);
        });

        // Apply pagination manually
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        int start = page * size;
        int end = Math.min(start + size, filteredHoaDons.size());

        List<HoaDon> pagedHoaDons = filteredHoaDons.subList(start, end);

        // Convert to DTO
        List<HoaDonDTO> dtoList = pagedHoaDons.stream()
                .map(this::convertToDto)
                .toList();

        System.out.println("📋 Final paginated results: " + dtoList.size() + " items");
        System.out.println("📋 Pagination info: page=" + page + ", size=" + size + ", total=" + filteredHoaDons.size());
        System.out.println("📋 Final response: totalPages=" + (int)Math.ceil((double)filteredHoaDons.size() / size) + ", totalElements=" + filteredHoaDons.size());

        // Create pageable for response
        Pageable pageable = PageRequest.of(page, size);

        return new PageImpl<>(dtoList, pageable, filteredHoaDons.size());
    }

    @Override
    public HoaDonTrackingDTO getTrackingInfo(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .map(this::convertToTrackingDto)
                .orElse(null);
    }

    private HoaDonTrackingDTO convertToTrackingDto(HoaDon hoaDon) {
        HoaDonTrackingDTO dto = new HoaDonTrackingDTO();
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setPhieuGiamGia(hoaDon.getPhieuGiamGiaId() != null ? "VC_" + hoaDon.getPhieuGiamGiaId() : null);
        dto.setNgayDat(hoaDon.getNgayTao());

        // Thông tin nhân viên
        dto.setNhanVienId(hoaDon.getNhanVienId());
        dto.setTenNhanVien(hoaDon.getNguoiTao() != null ? hoaDon.getNguoiTao() : "Không xác định");

        // Thông tin khách hàng
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setEmail(hoaDon.getEmail());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setGhiChu(hoaDon.getGhiChu());
        
        // Thông tin địa chỉ và giao hàng
        dto.setTinhThanh(hoaDon.getTinhThanh());
        dto.setQuanHuyen(hoaDon.getQuanHuyen());
        dto.setPhuongThucGiaoHang(hoaDon.getPhuongThucGiaoHang());
        dto.setPhuongThucNhanHang(hoaDon.getPhuongThucNhanHang());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setPhiVanChuyen(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : 0.0);
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());

        // Tổng kết đơn hàng
        dto.setTongTienHang(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0.0);

        Double tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : hoaDon.getTongTien();
        dto.setGiamGia(hoaDon.getTongTien() != null && tongTienSauGiam != null ?
                hoaDon.getTongTien() - tongTienSauGiam : 0.0);
        dto.setThanhTien(tongTienSauGiam != null ? tongTienSauGiam : hoaDon.getTongTien());
        dto.setTongTien(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0.0);

        // Lấy danh sách sản phẩm
        dto.setDanhSachSanPham(getSanPhamTrackingList(hoaDon.getId()));
        
        // Lấy lịch sử trạng thái từ database
        dto.setLichSuTrangThai(getLichSuTrangThai(hoaDon.getId()));

        // Lấy lịch sử thanh toán từ database
        List<ChiTietThanhToanDTO> lichSuThanhToan = getLichSuThanhToan(hoaDon.getId());
        dto.setLichSuThanhToan(lichSuThanhToan);
        System.out.println("💰 Payment history loaded for order " + hoaDon.getMaHoaDon() + ": " + 
                (lichSuThanhToan != null ? lichSuThanhToan.size() : 0) + " record(s)");

        return dto;
    }
    
    private List<TrangThaiTrackingDTO> getLichSuTrangThai(Integer hoaDonId) {
        try {
            System.out.println("🔍 Loading lichSuTrangThai for hoaDonId: " + hoaDonId);
            List<TrangThaiTracking> trackingList = trangThaiTrackingRepository.findByHoaDonIdOrderByThoiGianAsc(hoaDonId);
            System.out.println("🔍 Found " + trackingList.size() + " tracking records");
            
            List<TrangThaiTrackingDTO> result = trackingList.stream()
                    .map(this::convertToTrangThaiTrackingDto)
                    .collect(Collectors.toList());
            
            System.out.println("🔍 Converted to " + result.size() + " DTOs");
            for (TrangThaiTrackingDTO dto : result) {
                System.out.println("  - TrangThai: " + dto.getTrangThai() + ", TenTrangThai: " + dto.getTenTrangThai() + ", ThoiGian: " + dto.getThoiGian());
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error loading tracking history: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private TrangThaiTrackingDTO convertToTrangThaiTrackingDto(TrangThaiTracking tracking) {
        TrangThaiTrackingDTO dto = new TrangThaiTrackingDTO();
        dto.setTrangThai(tracking.getTrangThai());
        dto.setTenTrangThai(tracking.getTenTrangThai());
        dto.setThoiGian(tracking.getThoiGian());
        dto.setMoTa(tracking.getMoTa());
        dto.setActor(tracking.getNguoiThucHien());
        return dto;
    }
    
    private List<SanPhamTrackingDTO> getSanPhamTrackingList(Integer hoaDonId) {
        List<SanPhamTrackingDTO> products = new ArrayList<>();
        
        try {
            System.out.println("🔍 Loading danhSachSanPham for hoaDonId: " + hoaDonId);
            // Lấy danh sách chi tiết hóa đơn từ database
            List<HoaDonCt> hoaDonChiTiets = hoaDonCtRepository.findByIdHoaDon(hoaDonId);
            if (hoaDonChiTiets == null || hoaDonChiTiets.isEmpty()) {
                System.out.println("⚠️ No HoaDonCt found for hoaDonId: " + hoaDonId);
                return products;
            }
            
            System.out.println("🔍 Found " + hoaDonChiTiets.size() + " HoaDonCt records");
            hoaDonChiTiets.sort(Comparator.comparing(HoaDonCt::getId));
            
            for (HoaDonCt hoaDonChiTiet : hoaDonChiTiets) {
                ChiTietSanPham chiTietSanPham = hoaDonChiTiet.getChiTietSanPham();
                if (chiTietSanPham != null) {
                    SanPham sanPham = chiTietSanPham.getSanPham();
                    if (sanPham != null) {
                        SanPhamTrackingDTO product = new SanPhamTrackingDTO();
                        
                        product.setId(chiTietSanPham.getId()); // chiTietSanPhamId
                        product.setSanPhamId(sanPham.getId()); // sanPhamId (ID sản phẩm chung)
                        product.setChiTietHoaDonId(hoaDonChiTiet.getId()); // chiTietHoaDonId (HoaDonCt.id)
                        // Tạo mã CTSP nếu không có hoặc rỗng
                        String maCtsp = chiTietSanPham.getMaCtsp();
                        if (maCtsp == null || maCtsp.trim().isEmpty()) {
                            maCtsp = "CTSP" + chiTietSanPham.getId();
                        }
                        product.setMaCtsp(maCtsp);
                        product.setTenSanPham(sanPham.getTenSanPham());
                        
                        // Lấy thông tin RAM, ROM, màu sắc
                        if (chiTietSanPham.getRam() != null) {
                            product.setTenRam(chiTietSanPham.getRam().getTenRam());
                        }
                        if (chiTietSanPham.getRom() != null) {
                            product.setTenRom(chiTietSanPham.getRom().getDungLuong());
                        }
                        if (chiTietSanPham.getMauSac() != null) {
                            product.setTenMauSac(chiTietSanPham.getMauSac().getTenMau());
                        }
                        
                        // Lấy giá từ hóa đơn chi tiết
                        if (hoaDonChiTiet.getDonGia() != null) {
                            product.setGia(hoaDonChiTiet.getDonGia().doubleValue());
                        } else {
                            product.setGia(0.0);
                        }
                        
                        // Lấy số lượng (tạm thời để 1)
                        product.setSoLuong(1);
                        
                        // Lấy danh sách IMEI đã bán
                        List<Map<String, Object>> imeis = new ArrayList<>();
                        if (hoaDonChiTiet.getImeiDaBans() != null) {
                            for (ImeiDaBan imeiDaBan : hoaDonChiTiet.getImeiDaBans()) {
                                Map<String, Object> imeiMap = new HashMap<>();
                                imeiMap.put("imei", imeiDaBan.getImei());
                                imeiMap.put("trangThai", imeiDaBan.getTrangThai() != null ? imeiDaBan.getTrangThai() : 0);
                                imeis.add(imeiMap);
                            }
                        }
                        product.setImeis(imeis);
                        
                        // Lấy hình ảnh sản phẩm (nếu có)
                        // Query riêng để lấy hình ảnh vì hinhAnhs là LAZY
                        try {
                            // Query TẤT CẢ hình ảnh (không filter trangThai) để tìm Cloudinary URL
                            List<HinhAnh> hinhAnhs = hinhAnhRepository.findByChiTietSanPhamId(chiTietSanPham.getId());
                            
                            System.out.println("🔍 Loading images for product: " + product.getTenSanPham() + " (ctspId: " + chiTietSanPham.getId() + ")");
                            System.out.println("   Found " + (hinhAnhs != null ? hinhAnhs.size() : 0) + " image(s) in database");
                            
                            if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                                // Log tất cả hình ảnh tìm được
                                for (int i = 0; i < hinhAnhs.size(); i++) {
                                    HinhAnh h = hinhAnhs.get(i);
                                    System.out.println("   Image " + (i + 1) + ": " + h.getUrlAnh() + " (trangThai: " + h.getTrangThai() + ")");
                                }
                                
                                // Ưu tiên: 1) Cloudinary URL, 2) Local file tồn tại, 3) Bất kỳ URL nào
                                String urlAnh = null;
                                String cloudinaryUrl = null;
                                String localFileUrl = null;
                                
                                for (HinhAnh hinhAnh : hinhAnhs) {
                                    String testUrl = hinhAnh.getUrlAnh();
                                    if (testUrl != null && !testUrl.trim().isEmpty()) {
                                        // Ưu tiên Cloudinary URL (https://res.cloudinary.com hoặc bất kỳ https:// nào)
                                        if (testUrl.contains("cloudinary.com") || testUrl.startsWith("https://")) {
                                            cloudinaryUrl = testUrl;
                                            System.out.println("✅ Found Cloudinary URL for product: " + product.getTenSanPham() + " - " + testUrl);
                                            break; // Ưu tiên cao nhất, dùng ngay
                                        }
                                        
                                        // Kiểm tra local file có tồn tại không
                                        if (testUrl.startsWith("/uploads/") || testUrl.startsWith("uploads/")) {
                                            try {
                                                String filePath = testUrl.startsWith("/uploads/") 
                                                    ? testUrl.substring("/uploads/".length())
                                                    : testUrl.substring("uploads/".length());
                                                
                                                java.nio.file.Path fullPath = java.nio.file.Paths.get("uploads", filePath);
                                                if (java.nio.file.Files.exists(fullPath)) {
                                                    localFileUrl = testUrl;
                                                    System.out.println("✅ Found existing local file for product: " + product.getTenSanPham() + " - " + testUrl);
                                                } else {
                                                    System.out.println("⚠️ Local file does not exist: " + fullPath.toAbsolutePath());
                                                }
                                            } catch (Exception e) {
                                                System.err.println("⚠️ Error checking local file existence for: " + testUrl + " - " + e.getMessage());
                                            }
                                        }
                                    }
                                }
                                
                                // Chọn URL: Cloudinary > Local file > Bất kỳ URL nào
                                if (cloudinaryUrl != null) {
                                    urlAnh = cloudinaryUrl;
                                } else if (localFileUrl != null) {
                                    urlAnh = localFileUrl;
                                } else {
                                    // Tìm bất kỳ URL nào (có thể là Cloudinary hoặc local)
                                    for (HinhAnh hinhAnh : hinhAnhs) {
                                        String testUrl = hinhAnh.getUrlAnh();
                                        if (testUrl != null && !testUrl.trim().isEmpty()) {
                                            // Nếu là Cloudinary hoặc https://, dùng ngay
                                            if (testUrl.contains("cloudinary.com") || testUrl.startsWith("https://")) {
                                                urlAnh = testUrl;
                                                System.out.println("✅ Using Cloudinary URL (fallback): " + testUrl);
                                                break;
                                            }
                                        }
                                    }
                                    
                                    // Nếu vẫn chưa có, dùng URL đầu tiên
                                    if (urlAnh == null && hinhAnhs.get(0).getUrlAnh() != null) {
                                        urlAnh = hinhAnhs.get(0).getUrlAnh();
                                        System.out.println("⚠️ Using first available URL (may not exist): " + urlAnh);
                                    }
                                }
                                
                                if (urlAnh != null) {
                                    product.setHinhAnh(urlAnh);
                                    System.out.println("✅ Set hinhAnh for product: " + product.getTenSanPham() + " (ctspId: " + chiTietSanPham.getId() + ") - " + urlAnh);
                                } else {
                                    product.setHinhAnh(null);
                                    System.out.println("⚠️ No valid hinhAnh found for product: " + product.getTenSanPham() + " (ctspId: " + chiTietSanPham.getId() + ")");
                                    System.out.println("   Checked " + hinhAnhs.size() + " image(s)");
                                }
                            } else {
                                product.setHinhAnh(null);
                                System.out.println("⚠️ No hinhAnhs found at all for product: " + product.getTenSanPham() + " (ctspId: " + chiTietSanPham.getId() + ")");
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error loading hinhAnhs for product " + product.getTenSanPham() + " (ctspId: " + chiTietSanPham.getId() + "): " + e.getMessage());
                            e.printStackTrace();
                            product.setHinhAnh(null);
                        }
                        
                        products.add(product);
                        System.out.println("✅ Added product: " + product.getTenSanPham() + " (maCtsp: " + product.getMaCtsp() + ")");
                    }
                }
            }
            
            System.out.println("✅ Total products loaded: " + products.size());
            return products;
        } catch (Exception e) {
            System.err.println("❌ Error loading products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void updateSampleDataWithDiverseStatuses() {
        // Cập nhật loại hóa đơn trước
        List<HoaDon> allHoaDons = hoaDonRepository.findAll();

        // Tạo map để track các mã hóa đơn đã xử lý
        Set<String> processedMaHoaDons = new HashSet<>();

        for (HoaDon hoaDon : allHoaDons) {
            // Bỏ qua nếu đã xử lý mã này rồi (tránh duplicate)
            if (processedMaHoaDons.contains(hoaDon.getMaHoaDon())) {
                continue;
            }

            // Cập nhật loại hóa đơn
            if (hoaDon.getLoaiHoaDon().equals("Bán lẻ") || hoaDon.getLoaiHoaDon().equals("Bán trực tiếp") || hoaDon.getLoaiHoaDon().equals("NORMAL")) {
                hoaDon.setLoaiHoaDon(OrderStatusUtil.NORMAL);
            } else if (hoaDon.getLoaiHoaDon().equals("Online") || hoaDon.getLoaiHoaDon().equals("Giao hàng") || hoaDon.getLoaiHoaDon().equals("DELIVERY")) {
                hoaDon.setLoaiHoaDon(OrderStatusUtil.DELIVERY);
            }

            // Cập nhật trạng thái đa dạng
            Integer currentStatus = hoaDon.getTrangThai();
            Integer newStatus = currentStatus % 6; // 0-5 để có đa dạng trạng thái
            hoaDon.setTrangThai(newStatus);

            hoaDonRepository.save(hoaDon);
            processedMaHoaDons.add(hoaDon.getMaHoaDon());
        }
    }

    @Override
    public List<Map<String, Object>> getHoaDonProducts(Integer hoaDonId) {
        try {
            // Tìm hóa đơn theo ID
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
            if (hoaDonOpt.isEmpty()) {
                return new ArrayList<>();
            }

            HoaDon hoaDon = hoaDonOpt.get();
            List<Map<String, Object>> products = new ArrayList<>();

            // Lấy danh sách chi tiết hóa đơn từ database
            List<HoaDonCt> hoaDonChiTiets = hoaDonCtRepository.findByIdHoaDon(hoaDonId);

            for (HoaDonCt hoaDonChiTiet : hoaDonChiTiets) {
                Map<String, Object> product = new HashMap<>();

                // Lấy thông tin chi tiết sản phẩm
                ChiTietSanPham chiTietSanPham = hoaDonChiTiet.getChiTietSanPham();
                if (chiTietSanPham != null) {
                    SanPham sanPham = chiTietSanPham.getSanPham();
                    if (sanPham != null) {
                        product.put("id", chiTietSanPham.getId());
                        // Tạo mã CTSP nếu không có hoặc rỗng
                        String maCtsp = chiTietSanPham.getMaCtsp();
                        if (maCtsp == null || maCtsp.trim().isEmpty()) {
                            maCtsp = "CTSP" + chiTietSanPham.getId();
                        }
                        product.put("maCtsp", maCtsp);
                        product.put("tenSanPham", sanPham.getTenSanPham());

                        // Lấy thông tin RAM, ROM, màu sắc
                        if (chiTietSanPham.getRam() != null) {
                            product.put("tenRam", chiTietSanPham.getRam().getTenRam());
                        }
                        if (chiTietSanPham.getRom() != null) {
                            product.put("tenRom", chiTietSanPham.getRom().getDungLuong());
                        }
                        if (chiTietSanPham.getMauSac() != null) {
                            product.put("tenMauSac", chiTietSanPham.getMauSac().getTenMau());
                        }

                        // Lấy giá từ hóa đơn chi tiết
                        product.put("donGia", hoaDonChiTiet.getDonGia());
                        product.put("thanhTien", hoaDonChiTiet.getThanhTien());

                        // Lấy số lượng (tạm thời để 1, có thể cần thêm field số lượng vào HoaDonCt)
                        product.put("soLuong", 1);

                        // Lấy danh sách IMEI đã bán
                        List<String> imeis = new ArrayList<>();
                        if (hoaDonChiTiet.getImeiDaBans() != null) {
                            for (ImeiDaBan imeiDaBan : hoaDonChiTiet.getImeiDaBans()) {
                                imeis.add(imeiDaBan.getImei());
                            }
                        }
                        product.put("imeis", imeis);

                        // Lấy hình ảnh sản phẩm (nếu có)
                        if (chiTietSanPham.getHinhAnhs() != null && !chiTietSanPham.getHinhAnhs().isEmpty()) {
                            product.put("hinhAnh", chiTietSanPham.getHinhAnhs().get(0).getUrlAnh());
                        } else {
                            product.put("hinhAnh", "/placeholder-product.png");
                        }

                        products.add(product);
                    }
                }
            }

            return products;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public HoaDonDTO updateTrangThaiByMaHoaDon(String maHoaDon, Integer trangThai) {
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findByMaHoaDon(maHoaDon);
            if (hoaDonOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
            }

            HoaDon hoaDon = hoaDonOpt.get();
            hoaDon.setTrangThai(trangThai);
            hoaDon.setNgayCapNhat(LocalDateTime.now());

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            return convertToDto(savedHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDonDTO updateTrangThaiByMaHoaDon(String maHoaDon, Integer trangThai, String nguoiThucHien) {
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findByMaHoaDon(maHoaDon);
            if (hoaDonOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
            }

            HoaDon hoaDon = hoaDonOpt.get();
            Integer oldStatus = hoaDon.getTrangThai();
            hoaDon.setTrangThai(trangThai);
            hoaDon.setNgayCapNhat(LocalDateTime.now());
            if (nguoiThucHien != null) {
                hoaDon.setNguoiCapNhat(nguoiThucHien);
            }
            
            // Cập nhật nhanVienId từ SecurityContext nếu chưa có
            if (hoaDon.getNhanVienId() == null || hoaDon.getNhanVienId() <= 0) {
                Integer currentUserId = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUserId();
                if (currentUserId != null && currentUserId > 0) {
                    hoaDon.setNhanVienId(currentUserId);
                    System.out.println("✅ Đã cập nhật nhanVienId từ SecurityContext: " + currentUserId + " vào hóa đơn: " + maHoaDon);
                } else if (nguoiThucHien != null && !nguoiThucHien.trim().isEmpty()) {
                    // Thử lấy nhanVienId từ username/email nếu không có từ SecurityContext
                    var nhanVienOpt = nhanVienRepository.findByTaiKhoan(nguoiThucHien);
                    if (nhanVienOpt.isEmpty()) {
                        nhanVienOpt = nhanVienRepository.findByEmail(nguoiThucHien);
                    }
                    if (nhanVienOpt.isPresent()) {
                        hoaDon.setNhanVienId(nhanVienOpt.get().getId());
                        System.out.println("✅ Đã cập nhật nhanVienId từ nguoiThucHien: " + nhanVienOpt.get().getId() + " vào hóa đơn: " + maHoaDon);
                    }
                }
            }

            // If this is an ONLINE order and status changes from 0 (CHO_XAC_NHAN) to 1 (DANG_GIAO_HANG),
            // reduce stock quantity for all items in the order
            if ("ONLINE".equals(hoaDon.getLoaiHoaDon()) && oldStatus != null && oldStatus == 0 && trangThai == 1) {
                System.out.println("🔄 ONLINE order confirmed - reducing stock for order: " + maHoaDon);
                
                // Get all order details
                List<HoaDonCt> chiTietList = hoaDonCtRepository.findByIdHoaDon(hoaDon.getId());
                
                for (HoaDonCt chiTiet : chiTietList) {
                    ChiTietSanPham chiTietSanPham = chiTiet.getChiTietSanPham();
                    if (chiTietSanPham != null) {
                        Integer currentQuantity = chiTietSanPham.getSoLuong();
                        if (currentQuantity == null) {
                            currentQuantity = 0;
                        }
                        
                        // Calculate quantity to reduce (each HoaDonCt represents 1 item)
                        int quantityToReduce = 1; // Each HoaDonCt is 1 item
                        int newQuantity = currentQuantity - quantityToReduce;
                        
                        System.out.println("📦 Reducing stock for product: " + 
                            (chiTietSanPham.getSanPham() != null ? chiTietSanPham.getSanPham().getTenSanPham() : "N/A") +
                            " - Current: " + currentQuantity + ", Reduce: " + quantityToReduce + ", New: " + newQuantity);
                        
                        chiTietSanPham.setSoLuong(newQuantity);
                        chiTietSanPhamRepository.save(chiTietSanPham);
                    }
                }
                
                System.out.println("✅ Stock reduced for all items in order: " + maHoaDon);
            }

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            
            // Nếu hủy đơn hàng (trạng thái = 4 - DA_HUY), tự động tạo refund nếu đã thanh toán
            if (trangThai != null && trangThai == OrderStatusUtil.DA_HUY && 
                (oldStatus == null || oldStatus != OrderStatusUtil.DA_HUY)) {
                try {
                    System.out.println("🔄 Processing refund for cancelled order: " + maHoaDon);
                    
                    // Tính tổng tiền đã thanh toán thực sự (chỉ tính các khoản đã thanh toán, không tính COD)
                    // Lấy tất cả payment records
                    List<ChiTietThanhToan> allPayments = chiTietThanhToanRepository.findByHoaDonIdWithPhuongThuc(savedHoaDon.getId());
                    System.out.println("📋 Found " + allPayments.size() + " payment records");
                    
                    BigDecimal tongTienDaThanhToan = BigDecimal.ZERO;
                    
                    // Chỉ tính các khoản đã thanh toán thực sự:
                    // - PAYMENT với trangThai = 1 (đã thanh toán, không tính COD với trangThai = 0)
                    // - ADDITIONAL_FEE với trangThai = 1 (phụ phí đã thanh toán)
                    // KHÔNG tính:
                    // - PAYMENT với trangThai = 0 (COD - chưa thanh toán)
                    // - ADDITIONAL_FEE với trangThai = 0 (phụ phí chưa thanh toán)
                    // - REFUND (đã hoàn lại rồi)
                    for (ChiTietThanhToan payment : allPayments) {
                        String loaiThanhToan = payment.getLoaiThanhToan();
                        Integer trangThaiPayment = payment.getTrangThai();
                        
                        // Bỏ qua REFUND (đã hoàn lại rồi)
                        if (OrderStatusUtil.REFUND.equals(loaiThanhToan)) {
                            continue;
                        }
                        
                        // Chỉ tính các khoản đã thanh toán (trangThai = 1)
                        if (trangThaiPayment != null && trangThaiPayment == 1) {
                            // Tính PAYMENT đã thanh toán
                            if (loaiThanhToan == null || OrderStatusUtil.PAYMENT.equals(loaiThanhToan)) {
                                if (payment.getSoTien() != null) {
                                    tongTienDaThanhToan = tongTienDaThanhToan.add(payment.getSoTien());
                                    System.out.println("  ✅ Payment (đã thanh toán): " + payment.getSoTien() + " VND");
                                }
                            }
                            // Tính ADDITIONAL_FEE đã thanh toán
                            else if (OrderStatusUtil.ADDITIONAL_FEE.equals(loaiThanhToan)) {
                                if (payment.getSoTien() != null) {
                                    tongTienDaThanhToan = tongTienDaThanhToan.add(payment.getSoTien());
                                    System.out.println("  ✅ Additional Fee (đã thanh toán): " + payment.getSoTien() + " VND");
                                }
                            }
                        } else {
                            // Log các khoản chưa thanh toán (COD, phụ phí chưa thanh toán)
                            if (loaiThanhToan == null || OrderStatusUtil.PAYMENT.equals(loaiThanhToan)) {
                                System.out.println("  ⏸️ Payment (COD - chưa thanh toán): " + 
                                    (payment.getSoTien() != null ? payment.getSoTien() : "0") + " VND - KHÔNG tính vào hoàn phí");
                            } else if (OrderStatusUtil.ADDITIONAL_FEE.equals(loaiThanhToan)) {
                                System.out.println("  ⏸️ Additional Fee (chưa thanh toán): " + 
                                    (payment.getSoTien() != null ? payment.getSoTien() : "0") + " VND - KHÔNG tính vào hoàn phí");
                            }
                        }
                    }
                    
                    System.out.println("💰 Total paid amount (chỉ tính đã thanh toán): " + tongTienDaThanhToan);
                    
                    // Tạo refund nếu có số tiền đã thanh toán > 0
                    if (tongTienDaThanhToan != null && tongTienDaThanhToan.compareTo(BigDecimal.ZERO) > 0) {
                        Double soTienHoan = tongTienDaThanhToan.doubleValue();
                        String lyDo = nguoiThucHien != null ? 
                                "Hủy đơn hàng bởi: " + nguoiThucHien : 
                                "Hủy đơn hàng";
                        
                        // Lấy phương thức thanh toán từ hóa đơn hoặc lịch sử thanh toán
                        String phuongThucThanhToanString = savedHoaDon.getPhuongThucThanhToan();
                        
                        System.out.println("💸 Creating refund: " + soTienHoan + " VND, method: " + phuongThucThanhToanString);
                        createRefundPayment(savedHoaDon, soTienHoan, phuongThucThanhToanString, lyDo);
                        System.out.println("✅ Auto-created refund for cancelled order: " + maHoaDon + " - Amount: " + soTienHoan + " VND");
                    } else {
                        System.out.println("ℹ️ Order " + maHoaDon + " cancelled but no paid amount to refund " +
                                "(COD orders or unpaid additional fees are not refundable). Skipping refund.");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error creating auto-refund for cancelled order: " + e.getMessage());
                    e.printStackTrace();
                    // Không fail việc hủy đơn nếu tạo refund lỗi
                }
            }
            
            // Create tracking record
            try {
                // Xác định người thực hiện cho tracking record
                String nguoiThucHienTracking = nguoiThucHien;
                String moTaTracking = "Cập nhật trạng thái";
                
                // Nếu khách hàng xác nhận đã nhận hàng (trạng thái = 3 và nguoiThucHien là "Khách hàng"),
                // thì lấy nhân viên xử lý đơn hàng làm người thực hiện
                if (trangThai != null && trangThai == OrderStatusUtil.HOAN_THANH && 
                    (nguoiThucHien == null || "Khách hàng".equals(nguoiThucHien) || nguoiThucHien.contains("Khách hàng"))) {
                    // Lấy nhân viên xử lý đơn hàng từ hóa đơn
                    String nhanVienXuLy = savedHoaDon.getNguoiTao();
                    if (nhanVienXuLy != null && !nhanVienXuLy.trim().isEmpty()) {
                        nguoiThucHienTracking = nhanVienXuLy;
                        moTaTracking = "Hoàn thành đơn hàng (Khách hàng đã xác nhận nhận hàng)";
                        System.out.println("✅ Customer confirmed received - Using staff: " + nhanVienXuLy);
                    } else {
                        // Nếu không có nguoiTao, thử lấy từ nhanVienId (nếu có repository)
                        nguoiThucHienTracking = "Nhân viên xử lý";
                        moTaTracking = "Hoàn thành đơn hàng (Khách hàng đã xác nhận nhận hàng)";
                        System.out.println("⚠️ No nguoiTao found, using default staff name");
                    }
                } else if (nguoiThucHien != null) {
                    moTaTracking = "Cập nhật bởi: " + nguoiThucHien;
                } else {
                    nguoiThucHienTracking = "Hệ thống";
                }
                
                TrangThaiTracking tracking = new TrangThaiTracking(
                    savedHoaDon.getId(),
                    savedHoaDon.getTrangThai(),
                    OrderStatusUtil.getStatusName(savedHoaDon.getTrangThai()),
                    moTaTracking,
                    nguoiThucHienTracking
                );
                trangThaiTrackingRepository.save(tracking);
                System.out.println("✅ Created tracking record: Status=" + savedHoaDon.getTrangThai() + 
                    ", NguoiThucHien=" + nguoiThucHienTracking + ", MoTa=" + moTaTracking);
            } catch (Exception e) {
                System.out.println("⚠️ Error creating tracking record: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Send notification to customer when order status is updated (if customer is logged in)
            if (savedHoaDon.getKhachHangId() != null && oldStatus != null && !oldStatus.equals(trangThai)) {
                try {
                    notificationService.createCustomerOrderStatusUpdateNotification(
                        savedHoaDon.getMaHoaDon(),
                        savedHoaDon.getKhachHangId(),
                        savedHoaDon.getId(),
                        trangThai
                    );
                    System.out.println("✅ Customer notification created for status update - order: " + savedHoaDon.getMaHoaDon() + 
                        ", customerId: " + savedHoaDon.getKhachHangId() + ", newStatus: " + trangThai);
                } catch (Exception e) {
                    System.out.println("⚠️ Error creating customer status update notification: " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the status update if notification fails
                }
            } else if (savedHoaDon.getKhachHangId() == null) {
                System.out.println("ℹ️ No customer notification created - customer not logged in (order: " + savedHoaDon.getMaHoaDon() + ")");
            } else if (oldStatus != null && oldStatus.equals(trangThai)) {
                System.out.println("ℹ️ No customer notification created - status unchanged (order: " + savedHoaDon.getMaHoaDon() + ")");
            }

            return convertToDto(savedHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public HoaDon findByMaHoaDon(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon).orElse(null);
    }

    private void markVoucherAsUsed(Integer customerId, Integer voucherId) {
        try {
            System.out.println("=== MARK VOUCHER AS USED ===");
            System.out.println("Customer ID: " + customerId);
            System.out.println("Voucher ID: " + voucherId);

            // Find the relation between customer and voucher
            List<KhachHangGiamGia> allRelations = khachHangGiamGiaRepository.findAll();
            System.out.println("Total relations found: " + allRelations.size());

            Optional<KhachHangGiamGia> relationOpt = allRelations.stream()
                    .filter(r -> {
                        boolean hasCustomer = r.getKhachHang() != null;
                        boolean hasVoucher = r.getPhieuGiamGia() != null;
                        boolean customerMatch = hasCustomer && r.getKhachHang().getId().equals(customerId);
                        boolean voucherMatch = hasVoucher && r.getPhieuGiamGia().getId().equals(voucherId);

                        System.out.println("Relation - Customer: " + (hasCustomer ? r.getKhachHang().getId() : "null") +
                                ", Voucher: " + (hasVoucher ? r.getPhieuGiamGia().getId() : "null") +
                                ", Match: " + (customerMatch && voucherMatch));

                        return customerMatch && voucherMatch;
                    })
                    .findFirst();

            if (relationOpt.isPresent()) {
                KhachHangGiamGia relation = relationOpt.get();
                System.out.println("Found relation, marking as used...");
                relation.setDaSuDung(true);
                relation.setNgaySuDung(LocalDateTime.now());
                khachHangGiamGiaRepository.save(relation);
                System.out.println("✅ Successfully marked voucher as used for customer: " + customerId + ", voucher: " + voucherId);
            } else {
                System.out.println("❌ No voucher relation found for customer: " + customerId + ", voucher: " + voucherId);
            }
        } catch (Exception e) {
            System.out.println("❌ Error marking voucher as used: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Xử lý voucher cho khách vãng lai (không có customerId)
     * Chỉ áp dụng cho voucher công khai (không riêng tư)
     */
    private void markVoucherAsUsedForWalkIn(Integer voucherId) {
        try {
            System.out.println("=== MARKING VOUCHER AS USED FOR WALK-IN CUSTOMER ===");
            System.out.println("Voucher ID: " + voucherId);

            // Tìm voucher trong database để lấy mã voucher
            Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findById(voucherId);

            if (voucherOpt.isEmpty()) {
                System.out.println("❌ Voucher not found with ID: " + voucherId);
                throw new RuntimeException("Voucher not found with ID: " + voucherId);
            }

            PhieuGiamGia voucher = voucherOpt.get();
            String voucherCode = voucher.getMaPhieuGiamGia();

            // Sử dụng PhieuGiamGiaService để xử lý voucher cho khách vãng lai
            boolean success = phieuGiamGiaService.markVoucherAsUsedForWalkIn(voucherCode);

            if (success) {
                System.out.println("✅ Successfully marked voucher as used for walk-in customer: " + voucherId + " (code: " + voucherCode + ")");
            } else {
                System.out.println("❌ Failed to mark voucher as used for walk-in customer: " + voucherId + " (code: " + voucherCode + ")");
                throw new RuntimeException("Failed to mark voucher as used for walk-in customer");
            }

        } catch (Exception e) {
            System.out.println("❌ Error marking voucher as used for walk-in customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Tạo bản ghi hoàn phí (REFUND) khi hủy đơn hàng hoặc trả hàng
     * @param hoaDon Hóa đơn cần hoàn phí
     * @param soTienHoan Số tiền hoàn lại (số dương)
     * @param phuongThucThanhToanString Phương thức hoàn phí (có thể là phương thức thanh toán gốc)
     * @param lyDo Lý do hoàn phí (optional)
     * @return ChiTietThanhToan đã tạo
     */
    @Transactional
    public ChiTietThanhToan createRefundPayment(HoaDon hoaDon, Double soTienHoan, String phuongThucThanhToanString, String lyDo) {
        if (hoaDon == null || soTienHoan == null || soTienHoan <= 0) {
            throw new IllegalArgumentException("Hóa đơn và số tiền hoàn phải hợp lệ");
        }
        
        try {
            // Tìm phương thức thanh toán
            PhuongThucThanhToan phuongThucThanhToan = null;
            if (phuongThucThanhToanString != null && !phuongThucThanhToanString.trim().isEmpty()) {
                phuongThucThanhToan = findPhuongThucThanhToan(phuongThucThanhToanString);
            }
            
            // Nếu không tìm thấy, thử lấy từ hóa đơn (nếu có)
            if (phuongThucThanhToan == null) {
                // Lấy phương thức thanh toán từ lịch sử thanh toán đầu tiên
                List<ChiTietThanhToan> lichSuThanhToan = chiTietThanhToanRepository.findByHoaDonIdWithPhuongThuc(hoaDon.getId());
                if (!lichSuThanhToan.isEmpty()) {
                    ChiTietThanhToan firstPayment = lichSuThanhToan.get(0);
                    if (firstPayment.getPhuongThucThanhToan() != null) {
                        phuongThucThanhToan = firstPayment.getPhuongThucThanhToan();
                    }
                }
            }
            
            // Mặc định là "Tiền mặt" nếu không tìm thấy
            if (phuongThucThanhToan == null) {
                phuongThucThanhToan = phuongThucThanhToanRepository.findByTenPhuongThucIgnoreCase("Tiền mặt").orElse(null);
            }
            
            if (phuongThucThanhToan == null) {
                throw new RuntimeException("Không tìm thấy phương thức thanh toán để hoàn phí");
            }
            
            // Tạo bản ghi hoàn phí
            ChiTietThanhToan refund = ChiTietThanhToan.builder()
                    .hoaDon(hoaDon)
                    .phuongThucThanhToan(phuongThucThanhToan)
                    .soTien(BigDecimal.valueOf(soTienHoan))
                    .maGiaoDich("REFUND_" + hoaDon.getMaHoaDon() + "_" + System.currentTimeMillis())
                    .ngayThanhToan(null) // Chưa thanh toán, sẽ set khi chuyển tiền
                    .trangThai(OrderStatusUtil.REFUND_PENDING) // Chờ chuyển tiền (mặc định)
                    .loaiThanhToan(OrderStatusUtil.REFUND) // Loại: Hoàn phí
                    .ngayTao(LocalDateTime.now())
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            
            chiTietThanhToanRepository.save(refund);
            System.out.println("✅ Created refund record: Hoàn phí " + soTienHoan + " VND cho hóa đơn " + hoaDon.getMaHoaDon() + 
                    (lyDo != null ? " - Lý do: " + lyDo : ""));
            
            return refund;
        } catch (Exception e) {
            System.err.println("❌ Error creating refund payment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Tạo bản ghi phụ phí (ADDITIONAL_FEE) khi khách hàng cập nhật đơn hàng (thêm sản phẩm hoặc phí vận chuyển)
     * @param hoaDon Hóa đơn cần thêm phụ phí
     * @param soTienPhuPhi Số tiền phụ phí (số dương)
     * @param phuongThucThanhToanString Phương thức thanh toán phụ phí
     * @param moTa Mô tả phụ phí (ví dụ: "Phí vận chuyển", "Thêm sản phẩm")
     * @return ChiTietThanhToan đã tạo
     */
    @Transactional
    public ChiTietThanhToan createAdditionalFeePayment(HoaDon hoaDon, Double soTienPhuPhi, String phuongThucThanhToanString, String moTa) {
        return createAdditionalFeePayment(hoaDon, soTienPhuPhi, phuongThucThanhToanString, moTa, 1); // Mặc định đã thanh toán
    }
    
    /**
     * Tạo bản ghi phụ phí (ADDITIONAL_FEE) với trạng thái thanh toán
     * @param hoaDon Hóa đơn cần thêm phụ phí
     * @param soTienPhuPhi Số tiền phụ phí (số dương)
     * @param phuongThucThanhToanString Phương thức thanh toán phụ phí
     * @param moTa Mô tả phụ phí (ví dụ: "Phí vận chuyển", "Thêm sản phẩm")
     * @param trangThai Trạng thái thanh toán (0 = Chưa thanh toán, 1 = Đã thanh toán)
     * @return ChiTietThanhToan đã tạo
     */
    @Transactional
    public ChiTietThanhToan createAdditionalFeePayment(HoaDon hoaDon, Double soTienPhuPhi, String phuongThucThanhToanString, String moTa, Integer trangThai) {
        if (hoaDon == null || soTienPhuPhi == null || soTienPhuPhi <= 0) {
            throw new IllegalArgumentException("Hóa đơn và số tiền phụ phí phải hợp lệ");
        }
        
        try {
            // Tìm phương thức thanh toán
            PhuongThucThanhToan phuongThucThanhToan = null;
            if (phuongThucThanhToanString != null && !phuongThucThanhToanString.trim().isEmpty()) {
                phuongThucThanhToan = findPhuongThucThanhToan(phuongThucThanhToanString);
            }
            
            // Nếu không tìm thấy, thử lấy từ hóa đơn (nếu có)
            if (phuongThucThanhToan == null) {
                // Lấy phương thức thanh toán từ lịch sử thanh toán đầu tiên
                List<ChiTietThanhToan> lichSuThanhToan = chiTietThanhToanRepository.findByHoaDonIdWithPhuongThuc(hoaDon.getId());
                if (!lichSuThanhToan.isEmpty()) {
                    ChiTietThanhToan firstPayment = lichSuThanhToan.get(0);
                    if (firstPayment.getPhuongThucThanhToan() != null) {
                        phuongThucThanhToan = firstPayment.getPhuongThucThanhToan();
                    }
                }
            }
            
            // Mặc định là "Tiền mặt" nếu không tìm thấy
            if (phuongThucThanhToan == null) {
                phuongThucThanhToan = phuongThucThanhToanRepository.findByTenPhuongThucIgnoreCase("Tiền mặt").orElse(null);
            }
            
            if (phuongThucThanhToan == null) {
                throw new RuntimeException("Không tìm thấy phương thức thanh toán để thêm phụ phí");
            }
            
            // Tạo bản ghi phụ phí
            // Nếu trangThai = 0 (chưa thanh toán), ngayThanhToan = null
            // Nếu trangThai = 1 (đã thanh toán), ngayThanhToan = now
            LocalDateTime ngayThanhToanValue = (trangThai != null && trangThai == 1) ? LocalDateTime.now() : null;
            
            ChiTietThanhToan additionalFee = ChiTietThanhToan.builder()
                    .hoaDon(hoaDon)
                    .phuongThucThanhToan(phuongThucThanhToan)
                    .soTien(BigDecimal.valueOf(soTienPhuPhi))
                    .maGiaoDich("FEE_" + hoaDon.getMaHoaDon() + "_" + System.currentTimeMillis())
                    .ngayThanhToan(ngayThanhToanValue)
                    .trangThai(trangThai != null ? trangThai : 1) // Sử dụng trangThai được truyền vào, mặc định là 1 (đã thanh toán)
                    .loaiThanhToan(OrderStatusUtil.ADDITIONAL_FEE) // Loại: Phụ phí
                    .ngayTao(LocalDateTime.now())
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            
            chiTietThanhToanRepository.save(additionalFee);
            
            // Cộng phụ phí vào tổng tiền sau giảm của hóa đơn
            Double currentTongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : 0.0;
            Double newTongTienSauGiam = currentTongTienSauGiam + soTienPhuPhi;
            hoaDon.setTongTienSauGiam(newTongTienSauGiam);
            hoaDon.setNgayCapNhat(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);
            
            System.out.println("✅ Created additional fee record: Phụ phí " + soTienPhuPhi + " VND cho hóa đơn " + hoaDon.getMaHoaDon() + 
                    (moTa != null ? " - Mô tả: " + moTa : "") + ", Trạng thái: " + (trangThai == 0 ? "Chưa thanh toán" : "Đã thanh toán"));
            System.out.println("💰 Updated tongTienSauGiam: " + currentTongTienSauGiam + " + " + soTienPhuPhi + " = " + newTongTienSauGiam);
            
            return additionalFee;
        } catch (Exception e) {
            System.err.println("❌ Error creating additional fee payment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
