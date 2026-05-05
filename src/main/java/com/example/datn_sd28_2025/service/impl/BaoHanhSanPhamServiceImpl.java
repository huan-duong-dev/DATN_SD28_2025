package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.BaoHanhDTO;
import com.example.datn_sd28_2025.entity.BaoHanh;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.repository.BaoHanhRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.service.BaoHanhSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaoHanhSanPhamServiceImpl implements BaoHanhSanPhamService {

    @Autowired
    private BaoHanhRepository baoHanhRepository;

    @Autowired
    private ImeiDaBanRepository imeiDaBanRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BaoHanhDTO> getAll() {
        try {
            return baoHanhRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách bảo hành: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BaoHanhDTO> getById(Integer id) {
        return baoHanhRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public BaoHanhDTO create(BaoHanhDTO baoHanhDTO) {
        BaoHanh baoHanh = convertToEntity(baoHanhDTO);
        BaoHanh saved = baoHanhRepository.save(baoHanh);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public BaoHanhDTO update(Integer id, BaoHanhDTO baoHanhDTO) {
        BaoHanh existing = baoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bảo hành không tồn tại"));
        
        existing.setThoiHanBaoHanh(baoHanhDTO.getThoiHanBaoHanh());
        existing.setNgayBatDau(baoHanhDTO.getNgayBatDau());
        existing.setNgayKetThuc(baoHanhDTO.getNgayKetThuc());
        existing.setTrangThai(baoHanhDTO.getTrangThai());
        existing.setGhiChu(baoHanhDTO.getGhiChu());
        
        BaoHanh updated = baoHanhRepository.save(existing);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        baoHanhRepository.deleteById(id);
    }

    @Override
    public Optional<BaoHanhDTO> findByImei(String imei) {
        return baoHanhRepository.findByImei(imei)
                .map(this::convertToDto);
    }

    @Override
    public List<BaoHanhDTO> findByIdHoaDon(Integer idHoaDon) {
        return baoHanhRepository.findByIdHoaDon(idHoaDon).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BaoHanhDTO> findByIdKhachHang(Integer idKhachHang) {
        return baoHanhRepository.findByIdKhachHang(idKhachHang).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BaoHanhDTO> findByTrangThai(Integer trangThai) {
        return baoHanhRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BaoHanhDTO taoBaoHanhKhiBanImei(Integer idImeiDaBan, String imei, Integer idHoaDon,
                                            Integer idHoaDonChiTiet, Integer idChiTietSanPham,
                                            Integer idSanPham, Integer idKhachHang, LocalDate ngayBatDau) {
        // Kiểm tra xem đã có bảo hành cho IMEI này chưa
        Optional<BaoHanh> existing = baoHanhRepository.findByIdImeiDaBan(idImeiDaBan);
        if (existing.isPresent()) {
            return convertToDto(existing.get());
        }

        // Lấy thời hạn bảo hành từ chi_tiet_san_pham
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại"));
        
        // Thời hạn bảo hành: mặc định 12 tháng (1 năm) nếu không được chỉ định
        Integer thoiHanBaoHanh = chiTietSanPham.getThoiHanBaoHanh();
        if (thoiHanBaoHanh == null || thoiHanBaoHanh <= 0) {
            thoiHanBaoHanh = 12; // Mặc định 12 tháng (1 năm) kể từ ngày mua
        }

        // Ngày bắt đầu bảo hành: mặc định là ngày tạo hóa đơn (ngày mua hàng)
        if (ngayBatDau == null) {
            if (idHoaDon != null) {
                Optional<HoaDon> hoaDon = hoaDonRepository.findById(idHoaDon);
                if (hoaDon.isPresent() && hoaDon.get().getNgayTao() != null) {
                    // Lấy ngày tạo hóa đơn làm ngày bắt đầu bảo hành (ngày mua)
                    ngayBatDau = hoaDon.get().getNgayTao().toLocalDate();
                } else {
                    ngayBatDau = LocalDate.now();
                }
            } else {
                ngayBatDau = LocalDate.now();
            }
        }

        // Tính ngày kết thúc bảo hành: ngày bắt đầu + thời hạn bảo hành (tháng)
        LocalDate ngayKetThuc = ngayBatDau.plusMonths(thoiHanBaoHanh);

        // Lấy ImeiDaBan
        ImeiDaBan imeiDaBan = imeiDaBanRepository.findById(idImeiDaBan)
                .orElseThrow(() -> new RuntimeException("IMEI đã bán không tồn tại"));

        // Tạo bảo hành mới
        BaoHanh baoHanh = BaoHanh.builder()
                .imeiDaBan(imeiDaBan)
                .imei(imei)
                .idHoaDon(idHoaDon)
                .idHoaDonChiTiet(idHoaDonChiTiet)
                .idChiTietSanPham(idChiTietSanPham)
                .idSanPham(idSanPham)
                .idKhachHang(idKhachHang)
                .thoiHanBaoHanh(thoiHanBaoHanh)
                .ngayBatDau(ngayBatDau)
                .ngayKetThuc(ngayKetThuc)
                .trangThai(0) // Còn hạn
                .build();

        BaoHanh saved = baoHanhRepository.save(baoHanh);
        return convertToDto(saved);
    }

    @Override
    public boolean kiemTraBaoHanhConHan(String imei) {
        Optional<BaoHanh> baoHanh = baoHanhRepository.findBaoHanhConHanByImei(imei, LocalDate.now());
        return baoHanh.isPresent();
    }

    @Override
    public Optional<BaoHanhDTO> getBaoHanhConHanByImei(String imei) {
        return baoHanhRepository.findBaoHanhConHanByImei(imei, LocalDate.now())
                .map(this::convertToDto);
    }

    @Override
    public List<BaoHanhDTO> getBaoHanhConHan() {
        return baoHanhRepository.findBaoHanhConHan(LocalDate.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BaoHanhDTO> getBaoHanhHetHan() {
        return baoHanhRepository.findBaoHanhHetHan(LocalDate.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BaoHanhDTO> getBaoHanhSapHetHan(int soNgay) {
        LocalDate ngayHienTai = LocalDate.now();
        LocalDate ngaySapHetHan = ngayHienTai.plusDays(soNgay);
        return baoHanhRepository.findBaoHanhSapHetHan(ngayHienTai, ngaySapHetHan).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void capNhatTrangThaiHetHan() {
        List<BaoHanh> baoHanhHetHan = baoHanhRepository.findBaoHanhHetHan(LocalDate.now());
        for (BaoHanh bh : baoHanhHetHan) {
            if (bh.getTrangThai() != 2) { // Không phải đã hủy
                bh.setTrangThai(1); // Hết hạn
                baoHanhRepository.save(bh);
            }
        }
    }

    @Override
    public String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 0 -> "Còn hạn";
            case 1 -> "Hết hạn";
            case 2 -> "Đã hủy";
            default -> "Không xác định";
        };
    }

    private BaoHanhDTO convertToDto(BaoHanh baoHanh) {
        if (baoHanh == null) {
            return null;
        }

        try {
            BaoHanhDTO dto = BaoHanhDTO.builder()
                    .id(baoHanh.getId())
                    .imei(baoHanh.getImei())
                    .idHoaDon(baoHanh.getIdHoaDon())
                    .idHoaDonChiTiet(baoHanh.getIdHoaDonChiTiet())
                    .idChiTietSanPham(baoHanh.getIdChiTietSanPham())
                    .idSanPham(baoHanh.getIdSanPham())
                    .idKhachHang(baoHanh.getIdKhachHang())
                    .thoiHanBaoHanh(baoHanh.getThoiHanBaoHanh())
                    .ngayBatDau(baoHanh.getNgayBatDau())
                    .ngayKetThuc(baoHanh.getNgayKetThuc())
                    .trangThai(baoHanh.getTrangThai())
                    .trangThaiText(getTrangThaiText(baoHanh.getTrangThai()))
                    .ghiChu(baoHanh.getGhiChu())
                    .ngayTao(baoHanh.getNgayTao())
                    .ngayCapNhat(baoHanh.getNgayCapNhat())
                    .nguoiTao(baoHanh.getNguoiTao())
                    .nguoiCapNhat(baoHanh.getNguoiCapNhat())
                    .build();

            // Tính số ngày còn lại
            if (baoHanh.getNgayKetThuc() != null) {
                long soNgayConLai = ChronoUnit.DAYS.between(LocalDate.now(), baoHanh.getNgayKetThuc());
                dto.setSoNgayConLai(soNgayConLai);
                dto.setIsHetHan(soNgayConLai < 0);
            }

            // Lấy thông tin từ ImeiDaBan (xử lý lazy loading)
            try {
                if (baoHanh.getImeiDaBan() != null) {
                    dto.setIdImeiDaBan(baoHanh.getImeiDaBan().getId());
                }
            } catch (Exception e) {
                // Nếu gặp lỗi lazy loading, bỏ qua và không set idImeiDaBan
                // Có thể entity đã được detach hoặc session đã đóng
                System.err.println("Warning: Không thể lấy idImeiDaBan: " + e.getMessage());
            }

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi convert BaoHanh sang DTO: " + e.getMessage(), e);
        }
    }

    private BaoHanh convertToEntity(BaoHanhDTO dto) {
        BaoHanh baoHanh = BaoHanh.builder()
                .imei(dto.getImei())
                .idHoaDon(dto.getIdHoaDon())
                .idHoaDonChiTiet(dto.getIdHoaDonChiTiet())
                .idChiTietSanPham(dto.getIdChiTietSanPham())
                .idSanPham(dto.getIdSanPham())
                .idKhachHang(dto.getIdKhachHang())
                .thoiHanBaoHanh(dto.getThoiHanBaoHanh())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .trangThai(dto.getTrangThai())
                .ghiChu(dto.getGhiChu())
                .build();

        if (dto.getIdImeiDaBan() != null) {
            ImeiDaBan imeiDaBan = imeiDaBanRepository.findById(dto.getIdImeiDaBan())
                    .orElse(null);
            baoHanh.setImeiDaBan(imeiDaBan);
        }

        return baoHanh;
    }
}

