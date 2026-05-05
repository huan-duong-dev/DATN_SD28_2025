package com.example.datn_sd28_2025.service.impl;

import jakarta.transaction.Transactional;
import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.service.ImeiService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImeiServiceImpl implements ImeiService {

    private final ImeiRepository imeiRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final ImeiDaBanRepository imeiDaBanRepository;

    public ImeiServiceImpl(ImeiRepository imeiRepository, ChiTietSanPhamRepository chiTietSanPhamRepository, ImeiDaBanRepository imeiDaBanRepository) {
        this.imeiRepository = imeiRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.imeiDaBanRepository = imeiDaBanRepository;
    }

    @Override
    public Imei create(Imei imei) {
        // Note: ngay_tao column doesn't exist in the actual database
        // imei.setNgayTao(LocalDateTime.now());
        if (imei.getTrangThai() == null) imei.setTrangThai(1);
        return imeiRepository.save(imei);
    }

    @Override
    @Transactional
    public List<Imei> bulkCreate(Integer chiTietId, List<String> imeis) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findById(chiTietId)
                .orElseThrow(() -> new IllegalArgumentException("ChiTietSanPham not found"));
        List<Imei> saved = new ArrayList<>();
        for (String s : imeis) {
            String code = s.trim();
            if (code.isEmpty()) continue;
            if (imeiRepository.findByImei(code).isPresent()) continue; // skip duplicates
            Imei i = new Imei();
            i.setImei(code);
            i.setChiTietSanPham(ct);
            i.setTrangThai(1);
            // Note: ngay_tao column doesn't exist in the actual database
            // i.setNgayTao(LocalDateTime.now());
            saved.add(imeiRepository.save(i));
        }
        return saved;
    }

    @Override
    public List<Imei> findByChiTiet(Integer chiTietId, Integer status) {
        return imeiRepository.findByChiTietAndStatus(chiTietId, status);
    }

    @Override
    public Imei updateStatus(Integer id, Integer status) {
        Imei i = imeiRepository.findById(id).orElseThrow();
        i.setTrangThai(status);
        return imeiRepository.save(i);
    }

    // DTO methods
    @Override
    public List<ImeiDTO> getAll() {
        return imeiRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public ImeiDTO getById(Integer id) {
        return imeiRepository.findById(id).map(this::convertToDto).orElse(null);
    }

    @Override
    public ImeiDTO save(ImeiDTO imeiDTO) {
        Imei imei = convertToEntity(imeiDTO);
        // Note: ngay_tao column doesn't exist in the actual database
        // imei.setNgayTao(LocalDateTime.now());
        imei.setTrangThai(1);
        return convertToDto(imeiRepository.save(imei));
    }

    @Override
    public ImeiDTO update(Integer id, ImeiDTO imeiDTO) {
        return imeiRepository.findById(id).map(existingImei -> {
            existingImei.setImei(imeiDTO.getImei());
            existingImei.setTrangThai(imeiDTO.getTrangThai());
            // Note: ngay_cap_nhat column doesn't exist in the actual database
            // existingImei.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(imeiRepository.save(existingImei));
        }).orElseThrow(() -> new RuntimeException("Imei not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        imeiRepository.deleteById(id);
    }

    @Override
    public List<String> findDuplicateImeis(List<String> imeis) {
        return imeiRepository.findExistingImeis(imeis);
    }
    
    @Override
    public List<ImeiDTO> getByChiTietSanPham(Integer chiTietSanPhamId) {
        // Only return active IMEIs (trangThai = 1) by default
        List<Imei> imeis = imeiRepository.findByChiTietAndStatus(chiTietSanPhamId, 1);
        
        // Get all IMEIs that are already sold (exist in imei_da_ban table)
        // This ensures data consistency even if imei.trangThai is not updated correctly
        Set<String> soldImeis = imeis.stream()
                .map(Imei::getImei)
                .filter(imeiString -> {
                    List<com.example.datn_sd28_2025.entity.ImeiDaBan> soldRecords = imeiDaBanRepository.findByImei(imeiString);
                    if (soldRecords != null && !soldRecords.isEmpty()) {
                        // IMEI is already sold - update trangThai for consistency
                        Imei imei = imeiRepository.findByImei(imeiString).orElse(null);
                        if (imei != null && imei.getTrangThai() == 1) {
                            imei.setTrangThai(0);
                            imeiRepository.save(imei);
                            System.out.println("⚠️ Fixed data inconsistency: IMEI " + imeiString + " was marked as active but already sold. Updated trangThai to 0.");
                        }
                        return true; // This IMEI is sold
                    }
                    return false;
                })
                .collect(Collectors.toSet());
        
        // Filter out sold IMEIs
        return imeis.stream()
                .filter(imei -> !soldImeis.contains(imei.getImei()))
                .map(this::convertToDto)
                .toList();
    }
    
    @Override
    public Imei findByImeiString(String imei) {
        return imeiRepository.findByImei(imei).orElse(null);
    }
    
    @Override
    public ImeiDTO convertToDTO(Imei imei) {
        return convertToDto(imei);
    }

    @Override
    public java.util.Map<String, Object> searchByImei(String imei) {
        Imei imeiEntity = imeiRepository.findByImei(imei)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy IMEI: " + imei));
        
        ChiTietSanPham chiTiet = imeiEntity.getChiTietSanPham();
        if (chiTiet == null) {
            throw new RuntimeException("IMEI không liên kết với sản phẩm nào");
        }
        
        // Build response map with product details
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("trangThai", imeiEntity.getTrangThai());
        response.put("imei", imeiEntity.getImei());
        
        // Build chiTietSanPham info
        java.util.Map<String, Object> chiTietMap = new java.util.HashMap<>();
        chiTietMap.put("id", chiTiet.getId());
        chiTietMap.put("maCtsp", chiTiet.getMaCtsp());
        chiTietMap.put("giaBan", chiTiet.getGiaBan());
        chiTietMap.put("soLuong", chiTiet.getSoLuong());
        chiTietMap.put("trangThai", chiTiet.getTrangThai());
        
        // Add product info
        if (chiTiet.getSanPham() != null) {
            chiTietMap.put("idSp", chiTiet.getSanPham().getId());
            chiTietMap.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
            chiTietMap.put("maSanPham", chiTiet.getSanPham().getMaSanPham());
            
            if (chiTiet.getSanPham().getHang() != null) {
                chiTietMap.put("hangId", chiTiet.getSanPham().getHang().getId());
                chiTietMap.put("tenHang", chiTiet.getSanPham().getHang().getTen());
            }
        }
        
        // Add variant info
        if (chiTiet.getRam() != null) {
            chiTietMap.put("ramId", chiTiet.getRam().getId());
            chiTietMap.put("tenRam", chiTiet.getRam().getTenRam());
        }
        if (chiTiet.getRom() != null) {
            chiTietMap.put("romId", chiTiet.getRom().getId());
            chiTietMap.put("tenRom", chiTiet.getRom().getDungLuong());
        }
        if (chiTiet.getMauSac() != null) {
            chiTietMap.put("mauSacId", chiTiet.getMauSac().getId());
            chiTietMap.put("tenMauSac", chiTiet.getMauSac().getTenMau());
        }
        
        response.put("chiTietSanPham", chiTietMap);
        
        return response;
    }

    private ImeiDTO convertToDto(Imei imei) {
        return ImeiDTO.builder()
                .id(imei.getId())
                .imei(imei.getImei())
                .trangThai(imei.getTrangThai())
                .idCtsp(imei.getChiTietSanPham() != null ? imei.getChiTietSanPham().getId() : null)
                .build();
    }

    private Imei convertToEntity(ImeiDTO imeiDTO) {
        Imei imei = Imei.builder()
                .id(imeiDTO.getId())
                .imei(imeiDTO.getImei())
                .trangThai(imeiDTO.getTrangThai())
                .build();

        if (imeiDTO.getIdCtsp() != null) {
            chiTietSanPhamRepository.findById(imeiDTO.getIdCtsp()).ifPresent(imei::setChiTietSanPham);
        }
        return imei;
    }
}


