-- Thêm cột khach_hang_id vào bảng hoa_don để lưu ID khách hàng
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'khach_hang_id')
BEGIN
    ALTER TABLE hoa_don
    ADD khach_hang_id INT NULL;
    
    -- Thêm foreign key constraint
    ALTER TABLE hoa_don
    ADD CONSTRAINT FK_hoa_don_khach_hang 
    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id);
    
    -- Thêm index cho hiệu suất
    CREATE INDEX IX_hoa_don_khach_hang_id ON hoa_don(khach_hang_id);
    
    PRINT 'Đã thêm cột khach_hang_id vào bảng hoa_don';
END
ELSE
BEGIN
    PRINT 'Cột khach_hang_id đã tồn tại trong bảng hoa_don';
END
