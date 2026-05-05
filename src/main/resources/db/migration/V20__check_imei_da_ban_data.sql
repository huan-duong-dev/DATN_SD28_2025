-- =====================================================
-- KIỂM TRA DỮ LIỆU BẢNG IMEI_DA_BAN
-- =====================================================

-- 1. Xem tất cả dữ liệu
SELECT * FROM imei_da_ban ORDER BY id;

-- 2. Kiểm tra các dòng NULL
SELECT * FROM imei_da_ban 
WHERE id_hoa_don_chi_tiet IS NULL 
   OR imei IS NULL 
   OR imei = '';

-- 3. Kiểm tra IMEI trùng lặp
SELECT imei, COUNT(*) as so_lan_xuat_hien
FROM imei_da_ban
WHERE imei IS NOT NULL
GROUP BY imei
HAVING COUNT(*) > 1
ORDER BY so_lan_xuat_hien DESC;

-- 4. Xem chi tiết các IMEI trùng lặp
SELECT id, id_hoa_don_chi_tiet, imei, trang_thai
FROM imei_da_ban
WHERE imei IN (
    SELECT imei
    FROM imei_da_ban
    WHERE imei IS NOT NULL
    GROUP BY imei
    HAVING COUNT(*) > 1
)
ORDER BY imei, id;

-- 5. Kiểm tra foreign key constraints
SELECT 
    id,
    id_hoa_don_chi_tiet,
    imei,
    CASE 
        WHEN EXISTS (SELECT 1 FROM hoa_don_chi_tiet hdct WHERE hdct.id = imei_da_ban.id_hoa_don_chi_tiet) 
        THEN 'Valid' 
        ELSE 'Invalid FK' 
    END as fk_status
FROM imei_da_ban
WHERE id_hoa_don_chi_tiet IS NOT NULL;

