-- Script tạo bảng phieu_bao_hanh
-- Dựa trên entity PhieuBaoHanh

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[phieu_bao_hanh]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[phieu_bao_hanh] (
        [id] INT IDENTITY(1,1) PRIMARY KEY,
        
        -- Mã phiếu
        [ma_phieu] VARCHAR(50) UNIQUE,
        
        -- Thông tin khách hàng
        [id_khach_hang] INT,
        [ten_khach_hang] NVARCHAR(255),
        [so_dien_thoai] VARCHAR(15),
        
        -- Thông tin sản phẩm
        [id_san_pham] INT,
        [id_chi_tiet_san_pham] INT,
        [id_hoa_don] INT,
        [ten_san_pham] NVARCHAR(255),
        [imei_serial] VARCHAR(50),
        
        -- Tình trạng tiếp nhận
        [mo_ta_loi_khach_hang] NTEXT,
        [mo_ta_loi_nhan_vien] NTEXT,
        [tinh_trang_vat_ly] NTEXT,
        [phu_kien_di_kem] NVARCHAR(500),
        
        -- Đánh giá điều kiện bảo hành
        [du_dieu_kien_bao_hanh] BIT,
        [ly_do_khong_du_dieu_kien] NTEXT,
        
        -- Hướng xử lý
        [huong_xu_ly] VARCHAR(50),
        
        -- Kết quả xử lý
        [noi_dung_sua_chua] NTEXT,
        [ghi_chu_ky_thuat_vien] NTEXT,
        [chi_phi_sua_chua] DECIMAL(18,2),
        [khach_da_thanh_toan] DECIMAL(18,2),
        
        -- Thông tin TTBH hãng
        [ttbh_hang] NVARCHAR(255),
        [ma_bao_hanh_hang] VARCHAR(100),
        
        -- Nhân viên xử lý
        [id_nhan_vien_tiep_nhan] INT,
        [id_nhan_vien_ky_thuat] INT,
        [id_nhan_vien_tra_may] INT,
        
        -- Thời gian
        [ngay_nhan] DATE,
        [ngay_hen_tra_du_kien] DATE,
        [ngay_tra_thuc_te] DATE,
        
        -- Trạng thái
        -- 0: Mới tiếp nhận / Đang kiểm tra điều kiện
        -- 1: Đủ điều kiện bảo hành
        -- 2: Không đủ điều kiện bảo hành
        -- 3: Đang sửa chữa nội bộ
        -- 4: Đã gửi TTBH hãng
        -- 5: Đã nhận từ TTBH
        -- 6: Đang kiểm tra QC
        -- 7: Đã sửa xong
        -- 8: Đã trả khách
        -- 9: Hoàn tất
        [trang_thai] INT DEFAULT 0,
        
        -- Timestamps
        [ngay_tao] DATETIME DEFAULT GETDATE(),
        [ngay_cap_nhat] DATETIME DEFAULT GETDATE(),
        [nguoi_tao] VARCHAR(255),
        [nguoi_cap_nhat] VARCHAR(255)
    );
    
    PRINT 'Table phieu_bao_hanh created successfully';
END
ELSE
BEGIN
    PRINT 'Table phieu_bao_hanh already exists';
END

-- Tạo index cho các trường thường xuyên truy vấn
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_phieu_bao_hanh_ma_phieu' AND object_id = OBJECT_ID('dbo.phieu_bao_hanh'))
BEGIN
    CREATE INDEX idx_phieu_bao_hanh_ma_phieu ON [dbo].[phieu_bao_hanh]([ma_phieu]);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_phieu_bao_hanh_trang_thai' AND object_id = OBJECT_ID('dbo.phieu_bao_hanh'))
BEGIN
    CREATE INDEX idx_phieu_bao_hanh_trang_thai ON [dbo].[phieu_bao_hanh]([trang_thai]);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_phieu_bao_hanh_ngay_nhan' AND object_id = OBJECT_ID('dbo.phieu_bao_hanh'))
BEGIN
    CREATE INDEX idx_phieu_bao_hanh_ngay_nhan ON [dbo].[phieu_bao_hanh]([ngay_nhan]);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_phieu_bao_hanh_khach_hang' AND object_id = OBJECT_ID('dbo.phieu_bao_hanh'))
BEGIN
    CREATE INDEX idx_phieu_bao_hanh_khach_hang ON [dbo].[phieu_bao_hanh]([id_khach_hang]);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_phieu_bao_hanh_imei' AND object_id = OBJECT_ID('dbo.phieu_bao_hanh'))
BEGIN
    CREATE INDEX idx_phieu_bao_hanh_imei ON [dbo].[phieu_bao_hanh]([imei_serial]);
END

PRINT 'Indexes created successfully';

