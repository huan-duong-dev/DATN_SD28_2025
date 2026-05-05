-- Thêm cột id_danh_muc và id_hang vào bảng hinh_anh (SQL Server syntax)
ALTER TABLE hinh_anh 
ADD id_danh_muc INT NULL,
    id_hang INT NULL;

-- Thêm foreign key constraints
ALTER TABLE hinh_anh
ADD CONSTRAINT fk_hinh_anh_danh_muc 
FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE CASCADE;

ALTER TABLE hinh_anh
ADD CONSTRAINT fk_hinh_anh_hang 
FOREIGN KEY (id_hang) REFERENCES hang(id) ON DELETE CASCADE;

-- Tạo index cho performance
CREATE INDEX idx_hinh_anh_danh_muc ON hinh_anh(id_danh_muc);
CREATE INDEX idx_hinh_anh_hang ON hinh_anh(id_hang);
