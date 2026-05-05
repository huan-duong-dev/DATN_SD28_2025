-- =====================================================
-- SỬA LỖI BẢNG IMEI_DA_BAN
-- =====================================================

-- 1. Xóa các dòng NULL
DELETE FROM imei_da_ban 
WHERE id_hoa_don_chi_tiet IS NULL 
   OR imei IS NULL 
   OR imei = '';

-- 2. Kiểm tra và xử lý IMEI trùng lặp
-- Lưu ý: Nếu có bảo hành đã tạo cho các IMEI trùng lặp, cần xử lý trước
-- Tìm các IMEI trùng lặp (giữ lại bản ghi đầu tiên, xóa các bản ghi sau)
-- Trước khi xóa, kiểm tra xem có bảo hành nào liên kết không
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'bao_hanh')
BEGIN
    -- Nếu có bảo hành, cần xóa các bảo hành của IMEI trùng lặp (giữ lại bản ghi đầu tiên)
    WITH DuplicateIMEI AS (
        SELECT id, 
               ROW_NUMBER() OVER (PARTITION BY imei ORDER BY id) AS rn
        FROM imei_da_ban
        WHERE imei IS NOT NULL
    )
    DELETE FROM bao_hanh
    WHERE id_imei_da_ban IN (
        SELECT id FROM DuplicateIMEI WHERE rn > 1
    );
END

-- Xóa các IMEI trùng lặp (giữ lại bản ghi đầu tiên)
WITH DuplicateIMEI AS (
    SELECT id, 
           ROW_NUMBER() OVER (PARTITION BY imei ORDER BY id) AS rn
    FROM imei_da_ban
    WHERE imei IS NOT NULL
)
DELETE FROM imei_da_ban
WHERE id IN (
    SELECT id FROM DuplicateIMEI WHERE rn > 1
);

-- 3. Thêm NOT NULL constraint cho các cột quan trọng (phải làm trước khi tạo UNIQUE index)
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('imei_da_ban') 
    AND name = 'id_hoa_don_chi_tiet' 
    AND is_nullable = 1
)
BEGIN
    -- Xóa các dòng có id_hoa_don_chi_tiet NULL trước
    DELETE FROM imei_da_ban WHERE id_hoa_don_chi_tiet IS NULL;
    
    -- Thêm NOT NULL constraint
    ALTER TABLE imei_da_ban
    ALTER COLUMN id_hoa_don_chi_tiet INT NOT NULL;
    PRINT 'Đã thêm NOT NULL constraint cho cột id_hoa_don_chi_tiet';
END

IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('imei_da_ban') 
    AND name = 'imei' 
    AND is_nullable = 1
)
BEGIN
    -- Xóa các dòng có imei NULL trước
    DELETE FROM imei_da_ban WHERE imei IS NULL OR imei = '';
    
    -- Thêm NOT NULL constraint
    ALTER TABLE imei_da_ban
    ALTER COLUMN imei NVARCHAR(50) NOT NULL;
    PRINT 'Đã thêm NOT NULL constraint cho cột imei';
END

-- 4. Thêm UNIQUE constraint cho IMEI (một IMEI chỉ có thể được bán một lần)
-- Phải làm sau khi đã xóa trùng lặp và thêm NOT NULL constraint
IF NOT EXISTS (
    SELECT * FROM sys.indexes 
    WHERE name = 'UQ_imei_da_ban_imei' 
    AND object_id = OBJECT_ID('imei_da_ban')
)
BEGIN
    -- Tạo unique index cho IMEI (sau khi đã đảm bảo không có NULL và không trùng lặp)
    CREATE UNIQUE INDEX UQ_imei_da_ban_imei 
    ON imei_da_ban(imei);
    PRINT 'Đã tạo UNIQUE index cho cột imei trong bảng imei_da_ban';
END
ELSE
BEGIN
    PRINT 'UNIQUE index cho cột imei đã tồn tại';
END

-- 5. Thêm index cho id_hoa_don_chi_tiet (index thông thường, không phải UNIQUE)
IF NOT EXISTS (
    SELECT * FROM sys.indexes 
    WHERE name = 'IX_imei_da_ban_id_hoa_don_chi_tiet' 
    AND object_id = OBJECT_ID('imei_da_ban')
)
BEGIN
    CREATE INDEX IX_imei_da_ban_id_hoa_don_chi_tiet 
    ON imei_da_ban(id_hoa_don_chi_tiet);
    PRINT 'Đã tạo index cho cột id_hoa_don_chi_tiet';
END

PRINT 'Hoàn tất sửa lỗi bảng imei_da_ban';

