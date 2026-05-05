-- Migration: Thêm cột loai_thanh_toan vào bảng chi_tiet_thanh_toan
-- Mô tả: Thêm field để phân biệt loại thanh toán: PAYMENT (Thanh toán), REFUND (Hoàn phí), ADDITIONAL_FEE (Phụ phí)

-- Kiểm tra và thêm cột loai_thanh_toan nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'chi_tiet_thanh_toan' AND COLUMN_NAME = 'loai_thanh_toan'
)
BEGIN
    ALTER TABLE chi_tiet_thanh_toan 
    ADD loai_thanh_toan NVARCHAR(50) DEFAULT 'PAYMENT';
    
    -- Cập nhật giá trị mặc định cho các bản ghi hiện có
    UPDATE chi_tiet_thanh_toan 
    SET loai_thanh_toan = 'PAYMENT' 
    WHERE loai_thanh_toan IS NULL;
    
    PRINT 'Đã thêm cột loai_thanh_toan vào bảng chi_tiet_thanh_toan';
END
ELSE
BEGIN
    PRINT 'Cột loai_thanh_toan đã tồn tại trong bảng chi_tiet_thanh_toan';
END

