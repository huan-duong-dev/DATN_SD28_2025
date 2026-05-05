-- =====================================================
-- THÊM CÁC CỘT THIẾU VÀO BẢNG HOA_DON
-- =====================================================
-- Các cột này được định nghĩa trong entity HoaDon nhưng chưa có trong database

-- Thêm cột email
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'email')
BEGIN
    ALTER TABLE hoa_don ADD email NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột email vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột email đã tồn tại trong bảng hoa_don';
END

-- Thêm cột tinh_thanh
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'tinh_thanh')
BEGIN
    ALTER TABLE hoa_don ADD tinh_thanh NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột tinh_thanh vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột tinh_thanh đã tồn tại trong bảng hoa_don';
END

-- Thêm cột quan_huyen
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'quan_huyen')
BEGIN
    ALTER TABLE hoa_don ADD quan_huyen NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột quan_huyen vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột quan_huyen đã tồn tại trong bảng hoa_don';
END

-- Thêm cột phuong_thuc_giao_hang
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'phuong_thuc_giao_hang')
BEGIN
    ALTER TABLE hoa_don ADD phuong_thuc_giao_hang NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột phuong_thuc_giao_hang vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột phuong_thuc_giao_hang đã tồn tại trong bảng hoa_don';
END

-- Thêm cột phuong_thuc_thanh_toan
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'phuong_thuc_thanh_toan')
BEGIN
    ALTER TABLE hoa_don ADD phuong_thuc_thanh_toan NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột phuong_thuc_thanh_toan vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột phuong_thuc_thanh_toan đã tồn tại trong bảng hoa_don';
END

-- Thêm cột phi_van_chuyen
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'phi_van_chuyen')
BEGIN
    ALTER TABLE hoa_don ADD phi_van_chuyen DECIMAL(18,2) NULL;
    PRINT 'Đã thêm cột phi_van_chuyen vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột phi_van_chuyen đã tồn tại trong bảng hoa_don';
END

PRINT 'Hoàn tất thêm các cột thiếu vào bảng hoa_don';

