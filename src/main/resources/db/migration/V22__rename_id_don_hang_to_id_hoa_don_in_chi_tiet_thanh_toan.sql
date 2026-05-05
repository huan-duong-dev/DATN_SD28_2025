-- =====================================================
-- ĐỔI TÊN CỘT id_don_hang THÀNH id_hoa_don TRONG BẢNG chi_tiet_thanh_toan
-- =====================================================
-- Cột này cần được đổi tên để khớp với entity ChiTietThanhToan

-- Kiểm tra xem cột id_don_hang có tồn tại không
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chi_tiet_thanh_toan') AND name = 'id_don_hang')
BEGIN
    -- Kiểm tra xem cột id_hoa_don đã tồn tại chưa
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chi_tiet_thanh_toan') AND name = 'id_hoa_don')
    BEGIN
        -- Đổi tên cột từ id_don_hang sang id_hoa_don
        EXEC sp_rename 'chi_tiet_thanh_toan.id_don_hang', 'id_hoa_don', 'COLUMN';
        PRINT 'Đã đổi tên cột id_don_hang thành id_hoa_don trong bảng chi_tiet_thanh_toan';
    END
    ELSE
    BEGIN
        -- Nếu cả hai cột đều tồn tại, cần xử lý dữ liệu trước
        -- Copy dữ liệu từ id_don_hang sang id_hoa_don nếu id_hoa_don NULL
        UPDATE chi_tiet_thanh_toan 
        SET id_hoa_don = id_don_hang 
        WHERE id_hoa_don IS NULL AND id_don_hang IS NOT NULL;
        
        -- Xóa cột id_don_hang sau khi đã copy dữ liệu
        ALTER TABLE chi_tiet_thanh_toan DROP COLUMN id_don_hang;
        PRINT 'Đã copy dữ liệu và xóa cột id_don_hang trong bảng chi_tiet_thanh_toan';
    END
END
ELSE
BEGIN
    -- Nếu cột id_don_hang không tồn tại, kiểm tra xem id_hoa_don đã có chưa
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chi_tiet_thanh_toan') AND name = 'id_hoa_don')
    BEGIN
        -- Nếu cả hai đều không có, tạo cột mới id_hoa_don
        ALTER TABLE chi_tiet_thanh_toan ADD id_hoa_don INT NULL;
        
        -- Thêm foreign key constraint
        ALTER TABLE chi_tiet_thanh_toan
        ADD CONSTRAINT FK_chi_tiet_thanh_toan_hoa_don 
        FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id);
        
        PRINT 'Đã tạo cột id_hoa_don mới trong bảng chi_tiet_thanh_toan';
    END
    ELSE
    BEGIN
        PRINT 'Cột id_hoa_don đã tồn tại trong bảng chi_tiet_thanh_toan';
    END
END

-- Đảm bảo foreign key constraint tồn tại
IF NOT EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE name = 'FK_chi_tiet_thanh_toan_hoa_don' 
    AND parent_object_id = OBJECT_ID('chi_tiet_thanh_toan')
)
BEGIN
    -- Kiểm tra xem cột id_hoa_don có tồn tại không trước khi thêm constraint
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chi_tiet_thanh_toan') AND name = 'id_hoa_don')
    BEGIN
        ALTER TABLE chi_tiet_thanh_toan
        ADD CONSTRAINT FK_chi_tiet_thanh_toan_hoa_don 
        FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id);
        PRINT 'Đã thêm foreign key constraint FK_chi_tiet_thanh_toan_hoa_don';
    END
END

PRINT 'Hoàn tất đổi tên cột trong bảng chi_tiet_thanh_toan';

