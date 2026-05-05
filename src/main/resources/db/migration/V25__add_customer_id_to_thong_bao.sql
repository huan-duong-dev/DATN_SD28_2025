-- Thêm cột customer_id vào bảng thong_bao để lưu thông báo cho khách hàng
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('thong_bao') AND name = 'customer_id')
BEGIN
    ALTER TABLE thong_bao
    ADD customer_id INT NULL;
    
    -- Thêm foreign key constraint
    ALTER TABLE thong_bao
    ADD CONSTRAINT FK_thong_bao_khach_hang 
    FOREIGN KEY (customer_id) REFERENCES khach_hang(id);
    
    -- Thêm index cho hiệu suất
    CREATE INDEX IX_thong_bao_customer_id ON thong_bao(customer_id);
    
    PRINT 'Đã thêm cột customer_id vào bảng thong_bao';
END
ELSE
BEGIN
    PRINT 'Cột customer_id đã tồn tại trong bảng thong_bao';
END


