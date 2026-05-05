package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.ChiTietSanPhamDTO;
import com.example.datn_sd28_2025.dto.HoaDonCtDTO;
import com.example.datn_sd28_2025.dto.ImeiDaBanDTO;
import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.service.BaoHanhSanPhamService;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImeiDaBanServiceImpl implements ImeiDaBanService {

    @Autowired
    private ImeiDaBanRepository imeiDaBanRepository;

    @Autowired
    private ImeiRepository imeiRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Autowired
    private BaoHanhSanPhamService baoHanhService;

    @Override
    public ImeiDaBanDTO createImeiDaBan(ImeiDaBanDTO imeiDaBanDTO) {
        ImeiDaBan imeiDaBan = new ImeiDaBan();
        imeiDaBan.setImei(imeiDaBanDTO.getImei());
        imeiDaBan.setTrangThai(1); // Active
        
        if (imeiDaBanDTO.getIdHoaDonChiTiet() != null) {
            HoaDonCt hoaDonCt = hoaDonCtRepository.findById(imeiDaBanDTO.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new RuntimeException("Hóa đơn chi tiết không tồn tại"));
            imeiDaBan.setHoaDonChiTiet(hoaDonCt);
        }
        
        ImeiDaBan saved = imeiDaBanRepository.save(imeiDaBan);
        return convertToDto(saved);
    }

    @Override
    public List<ImeiDaBanDTO> getByHoaDonChiTiet(Integer idHoaDonChiTiet) {
        return imeiDaBanRepository.findByIdHoaDonChiTiet(idHoaDonChiTiet)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ImeiDaBanDTO getByImei(String imei) {
        List<ImeiDaBan> imeiDaBanList = imeiDaBanRepository.findByImei(imei);
        if (imeiDaBanList != null && !imeiDaBanList.isEmpty()) {
            // Trả về bản ghi đầu tiên
            return convertToDto(imeiDaBanList.get(0));
        }
        return null;
    }

    @Override
    public List<ImeiDaBanDTO> getAllActive() {
        return imeiDaBanRepository.findAllActive()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void markImeiAsSold(String imei, Integer idHoaDonChiTiet) {
        // Check if IMEI already exists in imei_da_ban (duplicate check)
        List<ImeiDaBan> existingImeiDaBan = imeiDaBanRepository.findByImei(imei);
        if (existingImeiDaBan != null && !existingImeiDaBan.isEmpty()) {
            // IMEI already sold - check if it's for the same order detail
            ImeiDaBan existing = existingImeiDaBan.get(0);
            if (existing.getHoaDonChiTiet() != null && existing.getHoaDonChiTiet().getId().equals(idHoaDonChiTiet)) {
                // Same order detail - skip, already marked
                System.out.println("⚠️ IMEI " + imei + " already marked as sold for this order detail, skipping...");
                return;
            } else {
                // Different order detail - IMEI already sold in another order
                throw new RuntimeException("IMEI " + imei + " đã được bán trong đơn hàng khác. Không thể bán lại.");
            }
        }
        
        // Find the IMEI in the available list
        Imei imeiEntity = imeiRepository.findByImei(imei)
                .orElseThrow(() -> new RuntimeException("IMEI không tồn tại: " + imei));
        
        if (imeiEntity.getTrangThai() != 1) {
            throw new RuntimeException("IMEI đã được bán hoặc không khả dụng: " + imei);
        }
        
        // Mark IMEI as sold (trangThai = 0)
        imeiEntity.setTrangThai(0);
        imeiRepository.save(imeiEntity);
        
        // Create ImeiDaBan record
        HoaDonCt hoaDonCt = hoaDonCtRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Hóa đơn chi tiết không tồn tại"));
        
        ImeiDaBan imeiDaBan = new ImeiDaBan();
        imeiDaBan.setHoaDonChiTiet(hoaDonCt);
        imeiDaBan.setImei(imei);
        imeiDaBan.setTrangThai(1); // Active
        ImeiDaBan savedImeiDaBan = imeiDaBanRepository.save(imeiDaBan);
        
        // Tự động tạo bảo hành cho IMEI đã bán
        try {
            Integer idHoaDon = hoaDonCt.getHoaDon() != null ? hoaDonCt.getHoaDon().getId() : null;
            Integer idChiTietSanPham = hoaDonCt.getChiTietSanPham() != null ? hoaDonCt.getChiTietSanPham().getId() : null;
            Integer idSanPham = hoaDonCt.getChiTietSanPham() != null && hoaDonCt.getChiTietSanPham().getSanPham() != null 
                    ? hoaDonCt.getChiTietSanPham().getSanPham().getId() : null;
            Integer idKhachHang = hoaDonCt.getHoaDon() != null ? hoaDonCt.getHoaDon().getKhachHangId() : null;
            LocalDate ngayBatDau = hoaDonCt.getHoaDon() != null && hoaDonCt.getHoaDon().getNgayTao() != null
                    ? hoaDonCt.getHoaDon().getNgayTao().toLocalDate() : LocalDate.now();
            
            baoHanhService.taoBaoHanhKhiBanImei(
                    savedImeiDaBan.getId(),
                    imei,
                    idHoaDon,
                    hoaDonCt.getId(),
                    idChiTietSanPham,
                    idSanPham,
                    idKhachHang,
                    ngayBatDau
            );
            System.out.println("✅ Đã tự động tạo bảo hành cho IMEI: " + imei);
        } catch (Exception e) {
            // Log lỗi nhưng không fail transaction - bảo hành có thể tạo sau
            System.err.println("⚠️ Lỗi khi tạo bảo hành cho IMEI " + imei + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ImeiDaBanDTO convertToDto(ImeiDaBan imeiDaBan) {
        ImeiDaBanDTO dto = new ImeiDaBanDTO();
        dto.setId(imeiDaBan.getId());
        dto.setImei(imeiDaBan.getImei());
        dto.setTrangThai(imeiDaBan.getTrangThai());
        
        if (imeiDaBan.getHoaDonChiTiet() != null) {
            HoaDonCt hoaDonCt = imeiDaBan.getHoaDonChiTiet();
            dto.setIdHoaDonChiTiet(hoaDonCt.getId());
            
            // Convert HoaDonCt to DTO
            HoaDonCtDTO hoaDonCtDTO = new HoaDonCtDTO();
            hoaDonCtDTO.setId(hoaDonCt.getId());
            if (hoaDonCt.getHoaDon() != null) {
                hoaDonCtDTO.setIdHoaDon(hoaDonCt.getHoaDon().getId());
            }
            hoaDonCtDTO.setIdCtsp(hoaDonCt.getChiTietSanPham() != null ? hoaDonCt.getChiTietSanPham().getId() : null);
            hoaDonCtDTO.setDonGia(hoaDonCt.getDonGia());
            hoaDonCtDTO.setThanhTien(hoaDonCt.getThanhTien());
            hoaDonCtDTO.setTrangThai(hoaDonCt.getTrangThai());
            
            // Convert ChiTietSanPham to DTO
            if (hoaDonCt.getChiTietSanPham() != null) {
                ChiTietSanPham chiTietSanPham = hoaDonCt.getChiTietSanPham();
                ChiTietSanPhamDTO chiTietSanPhamDTO = new ChiTietSanPhamDTO();
                chiTietSanPhamDTO.setId(chiTietSanPham.getId());
                if (chiTietSanPham.getSanPham() != null) {
                    chiTietSanPhamDTO.setIdSp(chiTietSanPham.getSanPham().getId());
                    // Create SanPhamDTO for nested object
                    SanPhamDTO sanPhamDTO = new SanPhamDTO();
                    sanPhamDTO.setId(chiTietSanPham.getSanPham().getId());
                    sanPhamDTO.setTenSanPham(chiTietSanPham.getSanPham().getTenSanPham());
                    chiTietSanPhamDTO.setSanPham(sanPhamDTO);
                }
                chiTietSanPhamDTO.setMaCtsp(chiTietSanPham.getMaCtsp());
                chiTietSanPhamDTO.setGiaBan(chiTietSanPham.getGiaBan());
                chiTietSanPhamDTO.setThoiHanBaoHanh(chiTietSanPham.getThoiHanBaoHanh());
                hoaDonCtDTO.setChiTietSanPham(chiTietSanPhamDTO);
            }
            
            dto.setHoaDonChiTiet(hoaDonCtDTO);
        }
        
        return dto;
    }
}
