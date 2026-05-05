-- Add phuong_thuc_nhan_hang column to hoa_don table
ALTER TABLE hoa_don
ADD phuong_thuc_nhan_hang NVARCHAR(50) NULL;

-- Update existing records based on loai_hoa_don and phuong_thuc_giao_hang
-- POS bán nhanh (BAN_THUONG, phuongThucGiaoHang = null/"Tại cửa hàng") -> Tại cửa hàng
UPDATE hoa_don
SET phuong_thuc_nhan_hang = N'Tại cửa hàng'
WHERE loai_hoa_don = N'BAN_THUONG'
  AND (phuong_thuc_giao_hang IS NULL 
       OR phuong_thuc_giao_hang = N'' 
       OR phuong_thuc_giao_hang = N'Tại cửa hàng');

-- POS bán giao (BAN_THUONG, phuongThucGiaoHang = "Giao hàng") -> Giao hàng
UPDATE hoa_don
SET phuong_thuc_nhan_hang = N'Giao hàng'
WHERE loai_hoa_don = N'BAN_THUONG'
  AND (phuong_thuc_giao_hang = N'Giao hàng' 
       OR phuong_thuc_giao_hang = N'DELIVERY');

-- Online giao hàng (ONLINE, phuongThucGiaoHang = "Giao hàng"/"standard") -> Giao hàng
UPDATE hoa_don
SET phuong_thuc_nhan_hang = N'Giao hàng'
WHERE (loai_hoa_don = N'ONLINE' OR loai_hoa_don = N'BAN_ONLINE')
  AND (phuong_thuc_giao_hang = N'Giao hàng' 
       OR phuong_thuc_giao_hang = N'standard'
       OR phuong_thuc_giao_hang = N'express'
       OR phuong_thuc_giao_hang = N'DELIVERY');

-- Online lấy tại cửa hàng (ONLINE, phuongThucGiaoHang = "pickup"/"Tại cửa hàng") -> Tại cửa hàng
UPDATE hoa_don
SET phuong_thuc_nhan_hang = N'Tại cửa hàng'
WHERE (loai_hoa_don = N'ONLINE' OR loai_hoa_don = N'BAN_ONLINE')
  AND (LOWER(phuong_thuc_giao_hang) = N'pickup' 
       OR phuong_thuc_giao_hang = N'Tại cửa hàng'
       OR phuong_thuc_giao_hang = N'Lấy tại cửa hàng');

-- Set default for any remaining NULL values
UPDATE hoa_don
SET phuong_thuc_nhan_hang = N'Tại cửa hàng'
WHERE phuong_thuc_nhan_hang IS NULL;





