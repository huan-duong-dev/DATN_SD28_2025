-- Thêm cột id_chi_tiet_san_pham vào bảng reviews để đánh giá theo chi tiết sản phẩm
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('reviews') AND name = 'id_chi_tiet_san_pham')
BEGIN
    ALTER TABLE reviews
    ADD id_chi_tiet_san_pham INT NULL;
    
    -- Thêm foreign key constraint
    ALTER TABLE reviews
    ADD CONSTRAINT FK_reviews_chi_tiet_san_pham 
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id);
    
    -- Thêm index cho hiệu suất
    CREATE INDEX IX_reviews_chi_tiet_san_pham ON reviews(id_chi_tiet_san_pham);
    
    PRINT 'Đã thêm cột id_chi_tiet_san_pham vào bảng reviews';
END
ELSE
BEGIN
    PRINT 'Cột id_chi_tiet_san_pham đã tồn tại trong bảng reviews';
END






