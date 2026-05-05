-- =====================================================
-- TẠO BẢNG LICH_SU_XU_LY_BAO_HANH NẾU CHƯA TỒN TẠI
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'lich_su_xu_ly_bao_hanh') AND type in (N'U'))
BEGIN
    CREATE TABLE lich_su_xu_ly_bao_hanh (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_phieu_bao_hanh INT NOT NULL,
        thoi_gian DATETIME2 NOT NULL DEFAULT GETDATE(),
        id_nhan_vien_thuc_hien INT,
        ten_nhan_vien_thuc_hien NVARCHAR(255),
        hanh_dong VARCHAR(100),
        -- "TIEP_NHAN", "KIEM_TRA_DIEU_KIEN", "DU_DIEU_KIEN", "KHONG_DU_DIEU_KIEN",
        -- "CHAN_DOAN_LOI", "SUA_NOI_BO", "GUI_TTBH", "NHAN_TU_TTBH", 
        -- "KIEM_TRA_QC", "TRA_MAY", "HOAN_TAT"
        noi_dung_xu_ly NTEXT, -- Mô tả chi tiết hành động
        chi_phi_phat_sinh DECIMAL(18,2),
        linh_kien_thay_the NVARCHAR(500), -- Danh sách linh kiện đã thay thế
        ghi_chu NTEXT,
        trang_thai_truoc INT,
        trang_thai_sau INT,
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        
        FOREIGN KEY (id_phieu_bao_hanh) REFERENCES phieu_bao_hanh(id),
        FOREIGN KEY (id_nhan_vien_thuc_hien) REFERENCES nhan_vien(id)
    );
    
    PRINT 'Đã tạo bảng lich_su_xu_ly_bao_hanh';
    
    -- Tạo index để tối ưu hiệu suất
    CREATE INDEX IX_lich_su_bao_hanh_phieu ON lich_su_xu_ly_bao_hanh(id_phieu_bao_hanh);
    CREATE INDEX IX_lich_su_bao_hanh_thoi_gian ON lich_su_xu_ly_bao_hanh(thoi_gian);
    CREATE INDEX IX_lich_su_bao_hanh_nhan_vien ON lich_su_xu_ly_bao_hanh(id_nhan_vien_thuc_hien);
    
    PRINT 'Đã tạo các index cho bảng lich_su_xu_ly_bao_hanh';
END
ELSE
BEGIN
    PRINT 'Bảng lich_su_xu_ly_bao_hanh đã tồn tại';
    
    -- Kiểm tra và tạo index nếu chưa có
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_lich_su_bao_hanh_phieu' AND object_id = OBJECT_ID('lich_su_xu_ly_bao_hanh'))
    BEGIN
        CREATE INDEX IX_lich_su_bao_hanh_phieu ON lich_su_xu_ly_bao_hanh(id_phieu_bao_hanh);
        PRINT 'Đã tạo index IX_lich_su_bao_hanh_phieu';
    END
    
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_lich_su_bao_hanh_thoi_gian' AND object_id = OBJECT_ID('lich_su_xu_ly_bao_hanh'))
    BEGIN
        CREATE INDEX IX_lich_su_bao_hanh_thoi_gian ON lich_su_xu_ly_bao_hanh(thoi_gian);
        PRINT 'Đã tạo index IX_lich_su_bao_hanh_thoi_gian';
    END
    
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_lich_su_bao_hanh_nhan_vien' AND object_id = OBJECT_ID('lich_su_xu_ly_bao_hanh'))
    BEGIN
        CREATE INDEX IX_lich_su_bao_hanh_nhan_vien ON lich_su_xu_ly_bao_hanh(id_nhan_vien_thuc_hien);
        PRINT 'Đã tạo index IX_lich_su_bao_hanh_nhan_vien';
    END
END

PRINT 'Hoàn tất kiểm tra và tạo bảng lich_su_xu_ly_bao_hanh';

