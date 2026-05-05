-- =====================================================
-- TẠO BẢNG BAO_HANH - QUẢN LÝ BẢO HÀNH SẢN PHẨM THEO IMEI
-- =====================================================

-- 1. Thêm trường thoi_han_bao_hanh vào bảng chi_tiet_san_pham
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chi_tiet_san_pham') AND name = 'thoi_han_bao_hanh')
BEGIN
    ALTER TABLE chi_tiet_san_pham
    ADD thoi_han_bao_hanh INT DEFAULT 12; -- Mặc định 12 tháng
    PRINT 'Đã thêm cột thoi_han_bao_hanh vào bảng chi_tiet_san_pham';
END
ELSE
BEGIN
    PRINT 'Cột thoi_han_bao_hanh đã tồn tại trong bảng chi_tiet_san_pham';
END

-- 2. Tạo bảng BAO_HANH
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bao_hanh')
BEGIN
    CREATE TABLE bao_hanh (
        id INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Liên kết với IMEI đã bán
        id_imei_da_ban INT NOT NULL,
        
        -- IMEI để dễ tra cứu (duplicate để tối ưu query)
        imei VARCHAR(50) NOT NULL,
        
        -- Thông tin hóa đơn và sản phẩm
        id_hoa_don INT NOT NULL,
        id_hoa_don_chi_tiet INT NOT NULL,
        id_chi_tiet_san_pham INT NOT NULL,
        id_san_pham INT,
        
        -- Thông tin khách hàng
        id_khach_hang INT,
        
        -- Thời hạn bảo hành
        thoi_han_bao_hanh INT NOT NULL, -- Số tháng: 12, 18, 24
        ngay_bat_dau DATE NOT NULL, -- Ngày bán/ngày tạo hóa đơn
        ngay_ket_thuc DATE NOT NULL, -- Ngày kết thúc bảo hành (ngay_bat_dau + thoi_han_bao_hanh tháng)
        
        -- Trạng thái
        trang_thai INT DEFAULT 0, 
        -- 0: Còn hạn bảo hành
        -- 1: Hết hạn bảo hành
        -- 2: Đã hủy
        
        -- Thông tin bổ sung
        ghi_chu NVARCHAR(500),
        
        -- Timestamps
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
        nguoi_tao NVARCHAR(255),
        nguoi_cap_nhat NVARCHAR(255),
        
        -- Foreign keys
        FOREIGN KEY (id_imei_da_ban) REFERENCES imei_da_ban(id),
        FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
        FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id),
        FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
        FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
        FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
    );
    
    PRINT 'Đã tạo bảng bao_hanh';
END
ELSE
BEGIN
    PRINT 'Bảng bao_hanh đã tồn tại';
END

-- 3. Tạo Index để tối ưu hiệu suất
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_imei')
BEGIN
    CREATE INDEX IX_bao_hanh_imei ON bao_hanh(imei);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_imei_da_ban')
BEGIN
    CREATE INDEX IX_bao_hanh_id_imei_da_ban ON bao_hanh(id_imei_da_ban);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_hoa_don')
BEGIN
    CREATE INDEX IX_bao_hanh_id_hoa_don ON bao_hanh(id_hoa_don);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_khach_hang')
BEGIN
    CREATE INDEX IX_bao_hanh_id_khach_hang ON bao_hanh(id_khach_hang);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_trang_thai')
BEGIN
    CREATE INDEX IX_bao_hanh_trang_thai ON bao_hanh(trang_thai);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_ngay_ket_thuc')
BEGIN
    CREATE INDEX IX_bao_hanh_ngay_ket_thuc ON bao_hanh(ngay_ket_thuc);
END

-- 4. Tạo Trigger để tự động cập nhật ngay_ket_thuc khi thoi_han_bao_hanh thay đổi
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_bao_hanh_update_ngay_ket_thuc')
BEGIN
    DROP TRIGGER trg_bao_hanh_update_ngay_ket_thuc;
END
GO

CREATE TRIGGER trg_bao_hanh_update_ngay_ket_thuc
ON bao_hanh
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Cập nhật ngay_ket_thuc = ngay_bat_dau + thoi_han_bao_hanh tháng
    UPDATE bao_hanh
    SET ngay_ket_thuc = DATEADD(MONTH, thoi_han_bao_hanh, ngay_bat_dau),
        ngay_cap_nhat = GETDATE()
    WHERE id IN (SELECT id FROM inserted);
    
    -- Tự động cập nhật trạng thái hết hạn
    UPDATE bao_hanh
    SET trang_thai = CASE 
            WHEN ngay_ket_thuc < CAST(GETDATE() AS DATE) THEN 1 -- Hết hạn
            ELSE 0 -- Còn hạn
        END,
        ngay_cap_nhat = GETDATE()
    WHERE id IN (SELECT id FROM inserted);
END
GO

-- 5. Tạo Stored Procedure để tự động tạo bảo hành khi IMEI được bán
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_tao_bao_hanh_khi_ban_imei')
BEGIN
    DROP PROCEDURE sp_tao_bao_hanh_khi_ban_imei;
END
GO

CREATE PROCEDURE sp_tao_bao_hanh_khi_ban_imei
    @id_imei_da_ban INT,
    @imei VARCHAR(50),
    @id_hoa_don INT,
    @id_hoa_don_chi_tiet INT,
    @id_chi_tiet_san_pham INT,
    @id_san_pham INT = NULL,
    @id_khach_hang INT = NULL,
    @ngay_bat_dau DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Lấy thời hạn bảo hành từ chi_tiet_san_pham
    -- Mặc định 12 tháng (1 năm) nếu không được chỉ định
    DECLARE @thoi_han_bao_hanh INT;
    SELECT @thoi_han_bao_hanh = ISNULL(thoi_han_bao_hanh, 12) 
    FROM chi_tiet_san_pham 
    WHERE id = @id_chi_tiet_san_pham;
    
    -- Nếu không có thời hạn, mặc định 12 tháng (1 năm)
    IF @thoi_han_bao_hanh IS NULL OR @thoi_han_bao_hanh <= 0
        SET @thoi_han_bao_hanh = 12;
    
    -- Ngày bắt đầu bảo hành: mặc định là ngày tạo hóa đơn (ngày mua hàng)
    IF @ngay_bat_dau IS NULL
    BEGIN
        SELECT @ngay_bat_dau = CAST(ngay_tao AS DATE)
        FROM hoa_don
        WHERE id = @id_hoa_don;
    END
    
    -- Nếu vẫn không có, dùng ngày hiện tại
    IF @ngay_bat_dau IS NULL
        SET @ngay_bat_dau = CAST(GETDATE() AS DATE);
    
    -- Tính ngày kết thúc bảo hành: ngày bắt đầu + thời hạn bảo hành (tháng)
    DECLARE @ngay_ket_thuc DATE = DATEADD(MONTH, @thoi_han_bao_hanh, @ngay_bat_dau);
    
    -- Kiểm tra xem đã có bảo hành cho IMEI này chưa
    IF NOT EXISTS (SELECT 1 FROM bao_hanh WHERE id_imei_da_ban = @id_imei_da_ban)
    BEGIN
        -- Tạo bảo hành mới
        INSERT INTO bao_hanh (
            id_imei_da_ban,
            imei,
            id_hoa_don,
            id_hoa_don_chi_tiet,
            id_chi_tiet_san_pham,
            id_san_pham,
            id_khach_hang,
            thoi_han_bao_hanh,
            ngay_bat_dau,
            ngay_ket_thuc,
            trang_thai,
            ngay_tao,
            ngay_cap_nhat
        )
        VALUES (
            @id_imei_da_ban,
            @imei,
            @id_hoa_don,
            @id_hoa_don_chi_tiet,
            @id_chi_tiet_san_pham,
            @id_san_pham,
            @id_khach_hang,
            @thoi_han_bao_hanh,
            @ngay_bat_dau,
            @ngay_ket_thuc,
            0, -- Còn hạn
            GETDATE(),
            GETDATE()
        );
        
        SELECT SCOPE_IDENTITY() AS bao_hanh_id;
    END
    ELSE
    BEGIN
        SELECT -1 AS bao_hanh_id; -- Đã tồn tại
    END
END
GO

-- 6. Tạo View để xem thông tin bảo hành chi tiết
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_bao_hanh_chi_tiet')
BEGIN
    DROP VIEW vw_bao_hanh_chi_tiet;
END
GO

CREATE VIEW vw_bao_hanh_chi_tiet AS
SELECT 
    bh.id,
    bh.id_imei_da_ban,
    bh.imei,
    bh.id_hoa_don,
    hd.ma_hoa_don,
    bh.id_chi_tiet_san_pham,
    ctsp.ma_ctsp,
    bh.id_san_pham,
    sp.ten_san_pham,
    bh.id_khach_hang,
    kh.ho_ten AS ten_khach_hang,
    kh.so_dien_thoai,
    bh.thoi_han_bao_hanh,
    bh.ngay_bat_dau,
    bh.ngay_ket_thuc,
    bh.trang_thai,
    CASE bh.trang_thai
        WHEN 0 THEN N'Còn hạn'
        WHEN 1 THEN N'Hết hạn'
        WHEN 2 THEN N'Đã hủy'
        ELSE N'Không xác định'
    END AS trang_thai_text,
    CASE 
        WHEN bh.ngay_ket_thuc < CAST(GETDATE() AS DATE) THEN 1
        ELSE 0
    END AS is_het_han,
    DATEDIFF(DAY, CAST(GETDATE() AS DATE), bh.ngay_ket_thuc) AS so_ngay_con_lai,
    bh.ghi_chu,
    bh.ngay_tao,
    bh.ngay_cap_nhat
FROM bao_hanh bh
LEFT JOIN hoa_don hd ON bh.id_hoa_don = hd.id
LEFT JOIN chi_tiet_san_pham ctsp ON bh.id_chi_tiet_san_pham = ctsp.id
LEFT JOIN san_pham sp ON bh.id_san_pham = sp.id
LEFT JOIN khach_hang kh ON bh.id_khach_hang = kh.id;
GO

PRINT 'Đã hoàn tất tạo bảng bao_hanh và các đối tượng liên quan';

