-- =====================================================
-- DATABASE SCHEMA - LUỒNG BẢO HÀNH
-- =====================================================
-- File này chứa định nghĩa đầy đủ các bảng liên quan đến luồng bảo hành:
-- 1. san_pham - Sản phẩm
-- 2. chi_tiet_san_pham - Chi tiết sản phẩm (RAM, ROM, Màu sắc)
-- 3. imei_da_ban - IMEI đã bán (liên kết với hóa đơn)
-- 4. bao_hanh - Bảo hành sản phẩm theo IMEI
-- =====================================================

-- =====================================================
-- 1. BẢNG SAN_PHAM - Sản phẩm
-- =====================================================
-- Bảng này lưu thông tin chung của sản phẩm (điện thoại, tablet, laptop...)
-- Mỗi sản phẩm có nhiều chi tiết sản phẩm (khác nhau về RAM, ROM, màu sắc)

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'san_pham')
BEGIN
    CREATE TABLE san_pham (
        id INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Phân loại sản phẩm
        id_danh_muc INT, -- Danh mục: Điện thoại, Tablet, Laptop...
        id_hang INT, -- Hãng: Apple, Samsung, Xiaomi...
        
        -- Thông số kỹ thuật chung
        id_man_hinh INT, -- Thông số màn hình
        id_camera_truoc INT, -- Camera trước
        id_camera_sau INT, -- Camera sau
        id_chip INT, -- Chip xử lý
        id_gpu INT, -- GPU
        id_sim INT, -- Loại SIM
        id_he_dieu_hanh INT, -- Hệ điều hành
        id_cpu INT, -- CPU
        id_pin INT, -- Pin
        
        -- Thông tin cơ bản
        thiet_ke NVARCHAR(255), -- Thiết kế
        kich_thuoc NVARCHAR(100), -- Kích thước
        ma_san_pham NVARCHAR(50) UNIQUE, -- Mã sản phẩm (unique)
        ten_san_pham NVARCHAR(255) NOT NULL, -- Tên sản phẩm
        mo_ta NVARCHAR(MAX), -- Mô tả chi tiết
        
        -- Trạng thái và quản lý
        trang_thai INT DEFAULT 1, -- 0: Ngừng bán, 1: Đang bán, 2: Tạm ngưng
        
        -- Timestamps
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
        nguoi_tao NVARCHAR(100),
        nguoi_cap_nhat NVARCHAR(100),
        
        -- Foreign keys
        FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id),
        FOREIGN KEY (id_hang) REFERENCES hang(id),
        FOREIGN KEY (id_man_hinh) REFERENCES man_hinh(id),
        FOREIGN KEY (id_camera_truoc) REFERENCES camera_truoc(id),
        FOREIGN KEY (id_camera_sau) REFERENCES camera_sau(id),
        FOREIGN KEY (id_chip) REFERENCES chip(id),
        FOREIGN KEY (id_gpu) REFERENCES gpu(id),
        FOREIGN KEY (id_sim) REFERENCES sim(id),
        FOREIGN KEY (id_he_dieu_hanh) REFERENCES he_dieu_hanh(id),
        FOREIGN KEY (id_cpu) REFERENCES cpu(id),
        FOREIGN KEY (id_pin) REFERENCES pin(id)
    );
    
    PRINT 'Đã tạo bảng san_pham';
END
ELSE
BEGIN
    PRINT 'Bảng san_pham đã tồn tại';
END
GO

-- Index cho bảng san_pham
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_san_pham_ma_san_pham' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    CREATE UNIQUE INDEX IX_san_pham_ma_san_pham ON san_pham(ma_san_pham);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_san_pham_id_hang' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    CREATE INDEX IX_san_pham_id_hang ON san_pham(id_hang);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_san_pham_id_danh_muc' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    CREATE INDEX IX_san_pham_id_danh_muc ON san_pham(id_danh_muc);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_san_pham_trang_thai' AND object_id = OBJECT_ID('san_pham'))
BEGIN
    CREATE INDEX IX_san_pham_trang_thai ON san_pham(trang_thai);
END

-- =====================================================
-- 2. BẢNG CHI_TIET_SAN_PHAM - Chi tiết sản phẩm
-- =====================================================
-- Bảng này lưu các biến thể của sản phẩm (khác nhau về RAM, ROM, màu sắc)
-- Mỗi chi tiết sản phẩm có giá, số lượng, và thời hạn bảo hành riêng
-- Một sản phẩm có thể có nhiều chi tiết sản phẩm

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'chi_tiet_san_pham')
BEGIN
    CREATE TABLE chi_tiet_san_pham (
        id INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Liên kết với sản phẩm
        id_sp INT NOT NULL, -- ID sản phẩm cha
        
        -- Thông số biến thể
        rom_id INT, -- Dung lượng ROM (64GB, 128GB, 256GB...)
        ram_id INT, -- Dung lượng RAM (4GB, 6GB, 8GB...)
        mau_sac_id INT, -- Màu sắc (Đen, Trắng, Xanh...)
        
        -- Thông tin định danh
        ma_ctsp NVARCHAR(50) UNIQUE, -- Mã chi tiết sản phẩm (unique)
        
        -- Giá cả
        gia_nhap DECIMAL(18,2), -- Giá nhập vào
        gia_ban DECIMAL(18,2), -- Giá bán ra
        
        -- Quản lý kho
        so_luong INT DEFAULT 0, -- Số lượng tồn kho
        
        -- Bảo hành
        thoi_han_bao_hanh INT DEFAULT 12, -- Thời hạn bảo hành (số tháng): 12, 18, 24...
        
        -- Ghi chú
        ghi_chu NVARCHAR(MAX), -- Ghi chú thêm
        
        -- Trạng thái
        trang_thai INT DEFAULT 1, -- 0: Ngừng bán, 1: Đang bán, 2: Tạm ngưng
        
        -- Timestamps
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
        nguoi_tao NVARCHAR(100),
        nguoi_cap_nhat NVARCHAR(100),
        
        -- Foreign keys
        FOREIGN KEY (id_sp) REFERENCES san_pham(id) ON DELETE CASCADE,
        FOREIGN KEY (rom_id) REFERENCES rom(id),
        FOREIGN KEY (ram_id) REFERENCES ram(id),
        FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id)
    );
    
    PRINT 'Đã tạo bảng chi_tiet_san_pham';
END
ELSE
BEGIN
    -- Đảm bảo cột thoi_han_bao_hanh tồn tại
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chi_tiet_san_pham') AND name = 'thoi_han_bao_hanh')
    BEGIN
        ALTER TABLE chi_tiet_san_pham
        ADD thoi_han_bao_hanh INT DEFAULT 12;
        PRINT 'Đã thêm cột thoi_han_bao_hanh vào bảng chi_tiet_san_pham';
    END
    
    PRINT 'Bảng chi_tiet_san_pham đã tồn tại';
END
GO

-- Index cho bảng chi_tiet_san_pham
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chi_tiet_san_pham_ma_ctsp' AND object_id = OBJECT_ID('chi_tiet_san_pham'))
BEGIN
    CREATE UNIQUE INDEX IX_chi_tiet_san_pham_ma_ctsp ON chi_tiet_san_pham(ma_ctsp);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chi_tiet_san_pham_id_sp' AND object_id = OBJECT_ID('chi_tiet_san_pham'))
BEGIN
    CREATE INDEX IX_chi_tiet_san_pham_id_sp ON chi_tiet_san_pham(id_sp);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_chi_tiet_san_pham_trang_thai' AND object_id = OBJECT_ID('chi_tiet_san_pham'))
BEGIN
    CREATE INDEX IX_chi_tiet_san_pham_trang_thai ON chi_tiet_san_pham(trang_thai);
END

-- =====================================================
-- 3. BẢNG IMEI_DA_BAN - IMEI đã bán
-- =====================================================
-- Bảng này lưu các IMEI/Serial đã được bán cho khách hàng
-- Mỗi IMEI chỉ có thể được bán một lần (UNIQUE constraint)
-- Liên kết với chi tiết hóa đơn để biết sản phẩm nào đã bán

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'imei_da_ban')
BEGIN
    CREATE TABLE imei_da_ban (
        id INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Liên kết với hóa đơn chi tiết
        id_hoa_don_chi_tiet INT NOT NULL, -- Chi tiết hóa đơn chứa sản phẩm này
        
        -- IMEI/Serial
        imei NVARCHAR(50) NOT NULL UNIQUE, -- IMEI hoặc Serial number (unique)
        
        -- Trạng thái
        trang_thai INT DEFAULT 1, -- 0: Đã hủy, 1: Đã bán, 2: Đã trả hàng
        
        -- Foreign keys
        FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id)
    );
    
    PRINT 'Đã tạo bảng imei_da_ban';
END
ELSE
BEGIN
    -- Đảm bảo các constraint đúng
    -- Kiểm tra và thêm NOT NULL cho id_hoa_don_chi_tiet nếu cần
    IF EXISTS (
        SELECT * FROM sys.columns 
        WHERE object_id = OBJECT_ID('imei_da_ban') 
        AND name = 'id_hoa_don_chi_tiet' 
        AND is_nullable = 1
    )
    BEGIN
        -- Xóa các dòng NULL trước
        DELETE FROM imei_da_ban WHERE id_hoa_don_chi_tiet IS NULL;
        
        ALTER TABLE imei_da_ban
        ALTER COLUMN id_hoa_don_chi_tiet INT NOT NULL;
        PRINT 'Đã thêm NOT NULL constraint cho id_hoa_don_chi_tiet';
    END
    
    -- Kiểm tra và thêm NOT NULL cho imei nếu cần
    IF EXISTS (
        SELECT * FROM sys.columns 
        WHERE object_id = OBJECT_ID('imei_da_ban') 
        AND name = 'imei' 
        AND is_nullable = 1
    )
    BEGIN
        -- Xóa các dòng NULL trước
        DELETE FROM imei_da_ban WHERE imei IS NULL OR imei = '';
        
        ALTER TABLE imei_da_ban
        ALTER COLUMN imei NVARCHAR(50) NOT NULL;
        PRINT 'Đã thêm NOT NULL constraint cho imei';
    END
    
    PRINT 'Bảng imei_da_ban đã tồn tại';
END
GO

-- Index và Unique constraint cho bảng imei_da_ban
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_imei_da_ban_imei' AND object_id = OBJECT_ID('imei_da_ban'))
BEGIN
    -- Xóa các IMEI trùng lặp trước (giữ lại bản ghi đầu tiên)
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
    
    -- Tạo unique index
    CREATE UNIQUE INDEX UQ_imei_da_ban_imei 
    ON imei_da_ban(imei);
    PRINT 'Đã tạo UNIQUE index cho cột imei trong bảng imei_da_ban';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_imei_da_ban_id_hoa_don_chi_tiet' AND object_id = OBJECT_ID('imei_da_ban'))
BEGIN
    CREATE INDEX IX_imei_da_ban_id_hoa_don_chi_tiet 
    ON imei_da_ban(id_hoa_don_chi_tiet);
    PRINT 'Đã tạo index cho cột id_hoa_don_chi_tiet';
END

-- =====================================================
-- 4. BẢNG BAO_HANH - Bảo hành sản phẩm
-- =====================================================
-- Bảng này lưu thông tin bảo hành cho từng IMEI đã bán
-- Tự động tạo khi IMEI được bán (thông qua stored procedure hoặc application logic)
-- Mỗi IMEI chỉ có một bảo hành duy nhất

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bao_hanh')
BEGIN
    CREATE TABLE bao_hanh (
        id INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Liên kết với IMEI đã bán (1-1 relationship)
        id_imei_da_ban INT NOT NULL UNIQUE, -- Mỗi IMEI chỉ có một bảo hành
        
        -- IMEI để dễ tra cứu (duplicate để tối ưu query, không cần join)
        imei NVARCHAR(50) NOT NULL,
        
        -- Thông tin hóa đơn và sản phẩm
        id_hoa_don INT NOT NULL, -- Hóa đơn mua hàng
        id_hoa_don_chi_tiet INT NOT NULL, -- Chi tiết hóa đơn
        id_chi_tiet_san_pham INT NOT NULL, -- Chi tiết sản phẩm đã bán
        id_san_pham INT, -- Sản phẩm (để dễ tra cứu)
        
        -- Thông tin khách hàng
        id_khach_hang INT, -- Khách hàng mua sản phẩm
        
        -- Thời hạn bảo hành
        thoi_han_bao_hanh INT NOT NULL DEFAULT 12, -- Số tháng: 12, 18, 24...
        ngay_bat_dau DATE NOT NULL, -- Ngày bắt đầu bảo hành (thường là ngày mua/ngày tạo hóa đơn)
        ngay_ket_thuc DATE NOT NULL, -- Ngày kết thúc bảo hành (ngay_bat_dau + thoi_han_bao_hanh tháng)
        
        -- Trạng thái bảo hành
        trang_thai INT DEFAULT 0, 
        -- 0: Còn hạn bảo hành
        -- 1: Hết hạn bảo hành
        -- 2: Đã hủy
        
        -- Thông tin bổ sung
        ghi_chu NVARCHAR(500), -- Ghi chú về bảo hành
        
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
GO

-- Index cho bảng bao_hanh
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_imei' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE INDEX IX_bao_hanh_imei ON bao_hanh(imei);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_imei_da_ban' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE UNIQUE INDEX IX_bao_hanh_id_imei_da_ban ON bao_hanh(id_imei_da_ban);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_hoa_don' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE INDEX IX_bao_hanh_id_hoa_don ON bao_hanh(id_hoa_don);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_id_khach_hang' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE INDEX IX_bao_hanh_id_khach_hang ON bao_hanh(id_khach_hang);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_trang_thai' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE INDEX IX_bao_hanh_trang_thai ON bao_hanh(trang_thai);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_bao_hanh_ngay_ket_thuc' AND object_id = OBJECT_ID('bao_hanh'))
BEGIN
    CREATE INDEX IX_bao_hanh_ngay_ket_thuc ON bao_hanh(ngay_ket_thuc);
END

-- =====================================================
-- 5. TRIGGER - Tự động cập nhật ngày kết thúc bảo hành
-- =====================================================
-- Trigger này tự động tính ngày kết thúc bảo hành khi thoi_han_bao_hanh hoặc ngay_bat_dau thay đổi

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
    WHERE id IN (SELECT id FROM inserted)
      AND (ngay_ket_thuc IS NULL OR 
           ngay_ket_thuc <> DATEADD(MONTH, thoi_han_bao_hanh, ngay_bat_dau));
    
    -- Tự động cập nhật trạng thái hết hạn
    UPDATE bao_hanh
    SET trang_thai = CASE 
            WHEN DATEADD(MONTH, thoi_han_bao_hanh, ngay_bat_dau) < CAST(GETDATE() AS DATE) THEN 1 -- Hết hạn
            WHEN trang_thai = 2 THEN 2 -- Giữ nguyên nếu đã hủy
            ELSE 0 -- Còn hạn
        END,
        ngay_cap_nhat = GETDATE()
    WHERE id IN (SELECT id FROM inserted);
END
GO

-- =====================================================
-- 6. STORED PROCEDURE - Tự động tạo bảo hành khi bán IMEI
-- =====================================================
-- Procedure này được gọi khi một IMEI được bán
-- Tự động tạo bảo hành với thời hạn từ chi_tiet_san_pham

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_tao_bao_hanh_khi_ban_imei')
BEGIN
    DROP PROCEDURE sp_tao_bao_hanh_khi_ban_imei;
END
GO

CREATE PROCEDURE sp_tao_bao_hanh_khi_ban_imei
    @id_imei_da_ban INT,
    @imei NVARCHAR(50),
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
    DECLARE @thoi_han_bao_hanh INT;
    SELECT @thoi_han_bao_hanh = ISNULL(thoi_han_bao_hanh, 12) 
    FROM chi_tiet_san_pham 
    WHERE id = @id_chi_tiet_san_pham;
    
    -- Mặc định 12 tháng nếu không có
    IF @thoi_han_bao_hanh IS NULL OR @thoi_han_bao_hanh <= 0
        SET @thoi_han_bao_hanh = 12;
    
    -- Lấy id_san_pham từ chi_tiet_san_pham nếu chưa có
    IF @id_san_pham IS NULL
    BEGIN
        SELECT @id_san_pham = id_sp
        FROM chi_tiet_san_pham
        WHERE id = @id_chi_tiet_san_pham;
    END
    
    -- Ngày bắt đầu bảo hành: mặc định là ngày tạo hóa đơn
    IF @ngay_bat_dau IS NULL
    BEGIN
        SELECT @ngay_bat_dau = CAST(ngay_tao AS DATE)
        FROM hoa_don
        WHERE id = @id_hoa_don;
    END
    
    -- Nếu vẫn không có, dùng ngày hiện tại
    IF @ngay_bat_dau IS NULL
        SET @ngay_bat_dau = CAST(GETDATE() AS DATE);
    
    -- Tính ngày kết thúc bảo hành
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

-- =====================================================
-- 7. VIEW - Xem thông tin bảo hành chi tiết
-- =====================================================
-- View này cung cấp thông tin đầy đủ về bảo hành kèm thông tin sản phẩm, khách hàng

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
    sp.ma_san_pham,
    bh.id_khach_hang,
    kh.ho_ten AS ten_khach_hang,
    kh.so_dien_thoai,
    kh.email,
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

-- =====================================================
-- 8. VIEW - Thống kê bảo hành
-- =====================================================
-- View này cung cấp thống kê về bảo hành

IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_thong_ke_bao_hanh')
BEGIN
    DROP VIEW vw_thong_ke_bao_hanh;
END
GO

CREATE VIEW vw_thong_ke_bao_hanh AS
SELECT 
    COUNT(*) AS tong_so_bao_hanh,
    SUM(CASE WHEN trang_thai = 0 THEN 1 ELSE 0 END) AS bao_hanh_con_han,
    SUM(CASE WHEN trang_thai = 1 THEN 1 ELSE 0 END) AS bao_hanh_het_han,
    SUM(CASE WHEN trang_thai = 2 THEN 1 ELSE 0 END) AS bao_hanh_da_huy,
    SUM(CASE 
        WHEN ngay_ket_thuc >= CAST(GETDATE() AS DATE) 
        AND ngay_ket_thuc <= DATEADD(DAY, 30, CAST(GETDATE() AS DATE))
        THEN 1 ELSE 0 
    END) AS bao_hanh_sap_het_han
FROM bao_hanh;
GO

PRINT '=====================================================';
PRINT 'Đã hoàn tất tạo các bảng và đối tượng liên quan đến luồng bảo hành';
PRINT 'Các bảng đã tạo:';
PRINT '  1. san_pham - Sản phẩm';
PRINT '  2. chi_tiet_san_pham - Chi tiết sản phẩm';
PRINT '  3. imei_da_ban - IMEI đã bán';
PRINT '  4. bao_hanh - Bảo hành sản phẩm';
PRINT 'Các đối tượng đã tạo:';
PRINT '  - Indexes cho tối ưu hiệu suất';
PRINT '  - Trigger tự động cập nhật ngày kết thúc bảo hành';
PRINT '  - Stored Procedure tạo bảo hành tự động';
PRINT '  - Views để xem thông tin bảo hành';
PRINT '=====================================================';

