-- Câu lệnh INSERT INTO 5 dữ liệu mẫu cho bảng bao_hanh

-- Bảo hành 1: iPhone 15 Pro Max - Còn hạn bảo hành (12 tháng)
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
    ghi_chu,
    ngay_tao,
    ngay_cap_nhat,
    nguoi_tao,
    nguoi_cap_nhat
) VALUES (
    1,                              -- id_imei_da_ban
    '354623456789012',              -- imei
    1,                              -- id_hoa_don
    1,                              -- id_hoa_don_chi_tiet
    1,                              -- id_chi_tiet_san_pham
    1,                              -- id_san_pham
    1,                              -- id_khach_hang
    12,                             -- thoi_han_bao_hanh (12 tháng)
    '2024-01-15',                   -- ngay_bat_dau
    '2025-01-15',                   -- ngay_ket_thuc
    0,                              -- trang_thai (0: Còn hạn)
    'Bảo hành chính hãng Apple',    -- ghi_chu
    '2024-01-15 10:30:00',          -- ngay_tao
    '2024-01-15 10:30:00',          -- ngay_cap_nhat
    'ADMIN',                        -- nguoi_tao
    'ADMIN'                         -- nguoi_cap_nhat
);

-- Bảo hành 2: Samsung Galaxy S24 Ultra - Còn hạn bảo hành (18 tháng)
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
    ghi_chu,
    ngay_tao,
    ngay_cap_nhat,
    nguoi_tao,
    nguoi_cap_nhat
) VALUES (
    2,                              -- id_imei_da_ban
    '354623456789013',              -- imei
    2,                              -- id_hoa_don
    2,                              -- id_hoa_don_chi_tiet
    2,                              -- id_chi_tiet_san_pham
    2,                              -- id_san_pham
    2,                              -- id_khach_hang
    18,                             -- thoi_han_bao_hanh (18 tháng)
    '2024-02-20',                   -- ngay_bat_dau
    '2025-08-20',                   -- ngay_ket_thuc
    0,                              -- trang_thai (0: Còn hạn)
    'Bảo hành chính hãng Samsung',  -- ghi_chu
    '2024-02-20 14:15:00',          -- ngay_tao
    '2024-02-20 14:15:00',          -- ngay_cap_nhat
    'ADMIN',                        -- nguoi_tao
    'ADMIN'                         -- nguoi_cap_nhat
);

-- Bảo hành 3: Xiaomi 14 Pro - Hết hạn bảo hành (12 tháng)
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
    ghi_chu,
    ngay_tao,
    ngay_cap_nhat,
    nguoi_tao,
    nguoi_cap_nhat
) VALUES (
    3,                              -- id_imei_da_ban
    '354623456789014',              -- imei
    3,                              -- id_hoa_don
    3,                              -- id_hoa_don_chi_tiet
    3,                              -- id_chi_tiet_san_pham
    3,                              -- id_san_pham
    3,                              -- id_khach_hang
    12,                             -- thoi_han_bao_hanh (12 tháng)
    '2023-06-10',                   -- ngay_bat_dau
    '2024-06-10',                   -- ngay_ket_thuc (đã hết hạn)
    1,                              -- trang_thai (1: Hết hạn)
    'Bảo hành chính hãng Xiaomi',   -- ghi_chu
    '2023-06-10 09:00:00',          -- ngay_tao
    '2024-06-10 00:00:00',          -- ngay_cap_nhat
    'ADMIN',                        -- nguoi_tao
    'SYSTEM'                        -- nguoi_cap_nhat
);

-- Bảo hành 4: Oppo Find X7 Ultra - Còn hạn bảo hành (24 tháng)
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
    ghi_chu,
    ngay_tao,
    ngay_cap_nhat,
    nguoi_tao,
    nguoi_cap_nhat
) VALUES (
    4,                              -- id_imei_da_ban
    '354623456789015',              -- imei
    4,                              -- id_hoa_don
    4,                              -- id_hoa_don_chi_tiet
    4,                              -- id_chi_tiet_san_pham
    4,                              -- id_san_pham
    4,                              -- id_khach_hang
    24,                             -- thoi_han_bao_hanh (24 tháng)
    '2024-03-05',                   -- ngay_bat_dau
    '2026-03-05',                   -- ngay_ket_thuc
    0,                              -- trang_thai (0: Còn hạn)
    'Bảo hành chính hãng Oppo - Gói bảo hành mở rộng', -- ghi_chu
    '2024-03-05 16:45:00',          -- ngay_tao
    '2024-03-05 16:45:00',          -- ngay_cap_nhat
    'NV001',                        -- nguoi_tao
    'NV001'                         -- nguoi_cap_nhat
);

-- Bảo hành 5: Vivo X100 Pro - Còn hạn bảo hành (12 tháng) - Sắp hết hạn
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
    ghi_chu,
    ngay_tao,
    ngay_cap_nhat,
    nguoi_tao,
    nguoi_cap_nhat
) VALUES (
    5,                              -- id_imei_da_ban
    '354623456789016',              -- imei
    5,                              -- id_hoa_don
    5,                              -- id_hoa_don_chi_tiet
    5,                              -- id_chi_tiet_san_pham
    5,                              -- id_san_pham
    5,                              -- id_khach_hang
    12,                             -- thoi_han_bao_hanh (12 tháng)
    '2023-12-20',                   -- ngay_bat_dau
    '2024-12-20',                   -- ngay_ket_thuc (sắp hết hạn)
    0,                              -- trang_thai (0: Còn hạn nhưng sắp hết)
    'Bảo hành chính hãng Vivo - Cần kiểm tra trước khi hết hạn', -- ghi_chu
    '2023-12-20 11:20:00',          -- ngay_tao
    '2023-12-20 11:20:00',          -- ngay_cap_nhat
    'NV002',                        -- nguoi_tao
    'NV002'                         -- nguoi_cap_nhat
);

