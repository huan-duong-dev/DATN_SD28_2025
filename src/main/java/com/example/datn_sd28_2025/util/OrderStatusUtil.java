package com.example.datn_sd28_2025.util;

public class OrderStatusUtil {

    // Order status constants (updated according to new logic)
    public static final int CHO_XAC_NHAN = 0;           // Chờ xác nhận (New order created, not yet confirmed)
    public static final int CHO_GIAO_HANG = 1;          // Chờ giao hàng (Confirmed, waiting for delivery)
    public static final int DANG_GIAO = 2;              // Đang giao (Being transported / In delivery)
    public static final int HOAN_THANH = 3;             // Hoàn thành (Delivered / Completed)
    public static final int DA_HUY = 4;                 // Đã hủy (Order cancelled)
    public static final int DA_TRA_HANG = 5;           // Đã trả hàng (Returned)

    // Payment method constants
    public static final String CASH = "CASH";           // Tiền mặt
    public static final String COD = "COD";             // Thu hộ
    public static final String BANK_TRANSFER = "BANK_TRANSFER"; // Chuyển khoản
    public static final String CREDIT_CARD = "CREDIT_CARD";     // Thẻ tín dụng

    // Order type constants
    public static final String NORMAL = "NORMAL";       // Bán trực tiếp
    public static final String DELIVERY = "DELIVERY";   // Giao hàng

    // Payment type constants
    public static final String PAYMENT = "PAYMENT";           // Thanh toán (Payment)
    public static final String REFUND = "REFUND";             // Hoàn phí (Refund)
    public static final String ADDITIONAL_FEE = "ADDITIONAL_FEE"; // Phụ phí (Additional Fee)

    // Refund status constants (for trangThai when loaiThanhToan = REFUND)
    public static final int REFUND_PENDING = 0;               // Chờ chuyển tiền (mặc định khi tạo refund)
    public static final int REFUND_TRANSFERRED = 1;           // Đã chuyển tiền - Chờ khách xác nhận
    public static final int REFUND_COMPLETED = 2;             // Hoàn thành - Khách đã xác nhận nhận tiền

    /**
     * Get initial status based on order type
     */
    public static int getInitialStatus(String orderType) {
        if (NORMAL.equals(orderType)) {
            return HOAN_THANH; // Thanh toán trực tiếp -> Hoàn thành
        } else if (DELIVERY.equals(orderType)) {
            return CHO_XAC_NHAN; // Giao hàng -> Chờ xác nhận
        } else if ("ONLINE".equals(orderType)) {
            return CHO_XAC_NHAN; // Đơn hàng online -> Chờ xác nhận
        }
        return HOAN_THANH; // Default
    }

    /**
     * Get next status in the workflow
     */
    public static int getNextStatus(int currentStatus) {
        switch (currentStatus) {
            case CHO_XAC_NHAN:
                return CHO_GIAO_HANG; // Đã thanh toán chờ xác nhận -> Chờ giao hàng
            case CHO_GIAO_HANG:
                return DANG_GIAO; // Chờ giao hàng -> Đang giao
            case DANG_GIAO:
                return HOAN_THANH; // Đang giao -> Hoàn thành
            default:
                return currentStatus; // No change
        }
    }

    /**
     * Get status name in Vietnamese
     */
    public static String getStatusName(int status) {
        switch (status) {
            case CHO_XAC_NHAN:
                return "Chờ xác nhận";
            case CHO_GIAO_HANG:
                return "Chờ giao hàng";
            case DANG_GIAO:
                return "Đang giao";
            case HOAN_THANH:
                return "Hoàn thành";
            case DA_HUY:
                return "Đã hủy";
            case DA_TRA_HANG:
                return "Đã trả hàng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Calculate phuongThucNhanHang based on loaiHoaDon and phuongThucGiaoHang
     * 
     * Logic:
     * - POS bán nhanh (BAN_THUONG, phuongThucGiaoHang = null/"Tại cửa hàng") -> Tại cửa hàng
     * - POS bán giao (BAN_THUONG, phuongThucGiaoHang = "Giao hàng") -> Giao hàng
     * - Online giao hàng (ONLINE, phuongThucGiaoHang = "Giao hàng"/"standard") -> Giao hàng
     * - Online lấy tại cửa hàng (ONLINE, phuongThucGiaoHang = "pickup"/"Tại cửa hàng") -> Tại cửa hàng
     */
    public static String calculatePhuongThucNhanHang(String loaiHoaDon, String phuongThucGiaoHang) {
        if (loaiHoaDon == null) {
            return "Tại cửa hàng"; // Default
        }
        
        // POS orders (BAN_THUONG)
        if ("BAN_THUONG".equals(loaiHoaDon)) {
            if (phuongThucGiaoHang == null || phuongThucGiaoHang.trim().isEmpty() || 
                "Tại cửa hàng".equals(phuongThucGiaoHang)) {
                return "Tại cửa hàng"; // POS bán nhanh
            } else if ("Giao hàng".equals(phuongThucGiaoHang) || 
                       "DELIVERY".equals(phuongThucGiaoHang)) {
                return "Giao hàng"; // POS bán giao
            }
        }
        
        // Online orders (ONLINE)
        if ("ONLINE".equals(loaiHoaDon) || "BAN_ONLINE".equals(loaiHoaDon)) {
            if (phuongThucGiaoHang == null || phuongThucGiaoHang.trim().isEmpty()) {
                return "Giao hàng"; // Default for online orders
            }
            
            String phuongThucLower = phuongThucGiaoHang.toLowerCase().trim();
            if ("pickup".equals(phuongThucLower) || 
                "tại cửa hàng".equals(phuongThucLower) ||
                "lấy tại cửa hàng".equals(phuongThucLower)) {
                return "Tại cửa hàng"; // Online lấy tại cửa hàng
            } else if ("giao hàng".equals(phuongThucLower) || 
                       "standard".equals(phuongThucLower) ||
                       "express".equals(phuongThucLower) ||
                       "DELIVERY".equals(phuongThucGiaoHang)) {
                return "Giao hàng"; // Online giao hàng
            }
        }
        
        // Default fallback
        return "Tại cửa hàng";
    }

    /**
     * Get payment method name in Vietnamese
     */
    public static String getPaymentMethodName(String paymentMethod) {
        switch (paymentMethod) {
            case CASH:
                return "Tiền mặt";
            case COD:
                return "Thu hộ (COD)";
            case BANK_TRANSFER:
                return "Chuyển khoản";
            case CREDIT_CARD:
                return "Thẻ tín dụng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Check if status can be updated
     */
    public static boolean canUpdateStatus(int currentStatus, int newStatus) {
        // Can only move forward in workflow or cancel
        if (newStatus == DA_HUY) {
            return currentStatus != HOAN_THANH && currentStatus != DA_HUY;
        }

        // Can only move to next status or stay the same
        return newStatus == currentStatus || newStatus == getNextStatus(currentStatus);
    }

    /**
     * Get payment type name in Vietnamese
     */
    public static String getPaymentTypeName(String paymentType) {
        switch (paymentType) {
            case PAYMENT:
                return "Thanh toán";
            case REFUND:
                return "Hoàn phí";
            case ADDITIONAL_FEE:
                return "Phụ phí";
            default:
                return "Không xác định";
        }
    }

    /**
     * Get refund status name in Vietnamese
     */
    public static String getRefundStatusName(Integer refundStatus) {
        if (refundStatus == null) {
            return "Không xác định";
        }
        switch (refundStatus) {
            case REFUND_PENDING:
                return "Chờ chuyển tiền";
            case REFUND_TRANSFERRED:
                return "Đã chuyển tiền - Chờ xác nhận";
            case REFUND_COMPLETED:
                return "Hoàn thành hoàn phí";
            default:
                return "Không xác định";
        }
    }
}


