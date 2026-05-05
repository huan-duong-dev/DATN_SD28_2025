-- =====================================================
-- THÊM CÁC TRƯỜNG CHI TIẾT VÀO BẢNG PHIEU_BAO_HANH
-- Để phù hợp với biên bản tiếp nhận bảo hành
-- =====================================================

-- Thêm email khách hàng
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'email_khach_hang')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD email_khach_hang NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột email_khach_hang vào bảng phieu_bao_hanh';
END

-- Thêm màu/SKU sản phẩm
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'mau_sac_sku')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD mau_sac_sku NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột mau_sac_sku vào bảng phieu_bao_hanh';
END

-- Thêm các trường tình trạng máy chi tiết (checkbox)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'tray_xuoc_nhe')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD tray_xuoc_nhe BIT DEFAULT 0;
    PRINT 'Đã thêm cột tray_xuoc_nhe vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'can_mop')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD can_mop BIT DEFAULT 0;
    PRINT 'Đã thêm cột can_mop vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'me_ven')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD me_ven BIT DEFAULT 0;
    PRINT 'Đã thêm cột me_ven vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'man_hinh_soc_diem_chet')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD man_hinh_soc_diem_chet BIT DEFAULT 0;
    PRINT 'Đã thêm cột man_hinh_soc_diem_chet vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'vao_nuoc')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD vao_nuoc BIT DEFAULT 0;
    PRINT 'Đã thêm cột vao_nuoc vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'tem_bao_hanh_rach_mat')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD tem_bao_hanh_rach_mat BIT DEFAULT 0;
    PRINT 'Đã thêm cột tem_bao_hanh_rach_mat vào bảng phieu_bao_hanh';
END

-- Thêm các trường phụ kiện đi kèm chi tiết (checkbox)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'phu_kien_sac')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD phu_kien_sac BIT DEFAULT 0;
    PRINT 'Đã thêm cột phu_kien_sac vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'phu_kien_cap')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD phu_kien_cap BIT DEFAULT 0;
    PRINT 'Đã thêm cột phu_kien_cap vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'phu_kien_hop')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD phu_kien_hop BIT DEFAULT 0;
    PRINT 'Đã thêm cột phu_kien_hop vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'phu_kien_khac')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD phu_kien_khac NVARCHAR(500) NULL;
    PRINT 'Đã thêm cột phu_kien_khac vào bảng phieu_bao_hanh';
END

-- Thêm kiểm tra nhanh (NV/KTV)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'kiem_tra_nhanh')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD kiem_tra_nhanh NTEXT NULL;
    PRINT 'Đã thêm cột kiem_tra_nhanh vào bảng phieu_bao_hanh';
END

-- Thêm xác minh bảo hành
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'bao_hanh_con_han')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD bao_hanh_con_han BIT NULL;
    PRINT 'Đã thêm cột bao_hanh_con_han vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'imei_trung_khop')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD imei_trung_khop BIT NULL;
    PRINT 'Đã thêm cột imei_trung_khop vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'tem_nguyen_ven')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD tem_nguyen_ven BIT NULL;
    PRINT 'Đã thêm cột tem_nguyen_ven vào bảng phieu_bao_hanh';
END

-- Thêm biên bản bàn giao (nếu gửi đi)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'ma_van_don')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD ma_van_don NVARCHAR(100) NULL;
    PRINT 'Đã thêm cột ma_van_don vào bảng phieu_bao_hanh';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'tinh_trang_niem_phong')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD tinh_trang_niem_phong NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột tinh_trang_niem_phong vào bảng phieu_bao_hanh';
END

-- Thêm người phụ trách
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phieu_bao_hanh') AND name = 'nguoi_phu_trach')
BEGIN
    ALTER TABLE phieu_bao_hanh ADD nguoi_phu_trach NVARCHAR(255) NULL;
    PRINT 'Đã thêm cột nguoi_phu_trach vào bảng phieu_bao_hanh';
END

PRINT 'Hoàn tất thêm các trường chi tiết vào bảng phieu_bao_hanh';

