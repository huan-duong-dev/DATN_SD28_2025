package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.dto.SanPhamEditDTO;
import com.example.datn_sd28_2025.dto.SanPhamViewDTO;
import com.example.datn_sd28_2025.dto.ChiTietSanPhamCreateDTO;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.DanhMuc;
import com.example.datn_sd28_2025.entity.Hang;
import com.example.datn_sd28_2025.entity.MauSac;
import com.example.datn_sd28_2025.entity.Ram;
import com.example.datn_sd28_2025.entity.Rom;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.ManHinh;
import com.example.datn_sd28_2025.entity.CameraTruoc;
import com.example.datn_sd28_2025.entity.CameraSau;
import com.example.datn_sd28_2025.entity.Chip;
import com.example.datn_sd28_2025.entity.Gpu;
import com.example.datn_sd28_2025.entity.Sim;
import com.example.datn_sd28_2025.entity.HeDieuHanh;
import com.example.datn_sd28_2025.entity.Cpu;
import com.example.datn_sd28_2025.entity.Pin;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.DanhMucRepository;
import com.example.datn_sd28_2025.repository.HangRepository;
import com.example.datn_sd28_2025.repository.RamRepository;
import com.example.datn_sd28_2025.repository.RomRepository;
import com.example.datn_sd28_2025.repository.MauSacRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.repository.ManHinhRepository;
import com.example.datn_sd28_2025.repository.CameraTruocRepository;
import com.example.datn_sd28_2025.repository.CameraSauRepository;
import com.example.datn_sd28_2025.repository.ChipRepository;
import com.example.datn_sd28_2025.repository.GpuRepository;
import com.example.datn_sd28_2025.repository.SimRepository;
import com.example.datn_sd28_2025.repository.HeDieuHanhRepository;
import com.example.datn_sd28_2025.repository.CpuRepository;
import com.example.datn_sd28_2025.repository.PinRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.service.SanPhamService;
import com.example.datn_sd28_2025.service.HinhAnhService;
import com.example.datn_sd28_2025.service.NotificationService;
import com.example.datn_sd28_2025.service.dto.SanPhamFullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private HangRepository hangRepository;

    @Autowired
    private RamRepository ramRepository;

    @Autowired
    private RomRepository romRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private ImeiRepository imeiRepository;

    @Autowired
    private ManHinhRepository manHinhRepository;

    @Autowired
    private CameraTruocRepository cameraTruocRepository;

    @Autowired
    private CameraSauRepository cameraSauRepository;

    @Autowired
    private ChipRepository chipRepository;

    @Autowired
    private GpuRepository gpuRepository;

    @Autowired
    private SimRepository simRepository;

    @Autowired
    private HeDieuHanhRepository heDieuHanhRepository;

    @Autowired
    private CpuRepository cpuRepository;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private HinhAnhService hinhAnhService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamDTO> getAllSanPham() {
        return sanPhamRepository.findAll().stream()
                .sorted((s1, s2) -> {
                    // Sắp xếp theo ngày tạo giảm dần (sản phẩm mới nhất lên đầu)
                    if (s1.getNgayTao() == null && s2.getNgayTao() == null) return 0;
                    if (s1.getNgayTao() == null) return 1;
                    if (s2.getNgayTao() == null) return -1;
                    return s2.getNgayTao().compareTo(s1.getNgayTao());
                })
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamDTO> getActiveSanPham() {
        return sanPhamRepository.findAllActive().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamDTO> searchSanPham(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return getActiveSanPham();
        }

        String kw = searchTerm.trim();
        java.util.LinkedHashMap<Integer, SanPham> merged = new java.util.LinkedHashMap<>();

        // Base search
        sanPhamRepository.searchBroad(kw).forEach(sp -> merged.put(sp.getId(), sp));

        // Tokenize and try individual meaningful tokens
        for (String token : kw.toLowerCase().split("[^a-z0-9áàãạăâéèêíìóòôơúùưýđ]+")) {
            if (token != null && token.length() >= 3) {
                sanPhamRepository.searchBroad(token).forEach(sp -> merged.put(sp.getId(), sp));
            }
            // Brand synonyms
            if ("iphone".equals(token) || "ios".equals(token)) {
                sanPhamRepository.searchBroad("apple").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("galaxy".equals(token)) {
                sanPhamRepository.searchBroad("samsung").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("mi".equals(token) || "xiaomi".equals(token) || "redmi".equals(token)) {
                sanPhamRepository.searchBroad("xiaomi").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("realme".equals(token)) {
                sanPhamRepository.searchBroad("realme").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("oppo".equals(token) || "find".equals(token) || "reno".equals(token)) {
                sanPhamRepository.searchBroad("oppo").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("vivo".equals(token) || "x".equals(token)) {
                sanPhamRepository.searchBroad("vivo").forEach(sp -> merged.put(sp.getId(), sp));
            }
            if ("oneplus".equals(token)) {
                sanPhamRepository.searchBroad("oneplus").forEach(sp -> merged.put(sp.getId(), sp));
            }
        }

        return merged.values().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SanPhamDTO> searchAdvanced(
            String kw,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            String chip,
            Integer ramId,
            Integer romId,
            String osName,
            Integer brandId,
            String brandName,
            String gpu,
            String cpuName,
            String simType,
            String minBattery,
            String maxBattery,
            String minScreen,
            String maxScreen,
            String rearCam,
            String frontCam
    ) {
        return sanPhamRepository.searchAdvanced(kw, minPrice, maxPrice, chip, ramId, romId, osName,
                brandId, brandName, gpu, cpuName, simType, minBattery, maxBattery, minScreen, maxScreen, rearCam, frontCam)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SanPhamDTO> getSanPhamById(Integer id) {
        return sanPhamRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SanPhamDTO> getSanPhamByMa(String maSanPham) {
        return sanPhamRepository.findByMaSanPham(maSanPham).map(this::toDTO);
    }

    @Override
    public SanPham createSanPham(SanPham sanPham) {
        if (sanPham.getNgayTao() == null) {
            sanPham.setNgayTao(LocalDateTime.now());
        }
        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(1);
        }

        // Map quan hệ từ id tạm
        if (sanPham.getIdDanhMuc() != null) {
            DanhMuc dm = danhMucRepository.findById(sanPham.getIdDanhMuc())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + sanPham.getIdDanhMuc()));
            sanPham.setDanhMuc(dm);
        }
        if (sanPham.getIdHang() != null) {
            Hang h = hangRepository.findById(sanPham.getIdHang())
                    .orElseThrow(() -> new RuntimeException("Hãng không tồn tại: " + sanPham.getIdHang()));
            sanPham.setHang(h);
        }

        // Map các quan hệ mới
        if (sanPham.getIdManHinh() != null) {
            ManHinh manHinh = manHinhRepository.findById(sanPham.getIdManHinh())
                    .orElseThrow(() -> new RuntimeException("Màn hình không tồn tại: " + sanPham.getIdManHinh()));
            sanPham.setManHinh(manHinh);
        }

        if (sanPham.getIdCameraTruoc() != null) {
            CameraTruoc cameraTruoc = cameraTruocRepository.findById(sanPham.getIdCameraTruoc())
                    .orElseThrow(() -> new RuntimeException("Camera trước không tồn tại: " + sanPham.getIdCameraTruoc()));
            sanPham.setCameraTruoc(cameraTruoc);
        }

        if (sanPham.getIdCameraSau() != null) {
            CameraSau cameraSau = cameraSauRepository.findById(sanPham.getIdCameraSau())
                    .orElseThrow(() -> new RuntimeException("Camera sau không tồn tại: " + sanPham.getIdCameraSau()));
            sanPham.setCameraSau(cameraSau);
        }

        if (sanPham.getIdChip() != null) {
            Chip chip = chipRepository.findById(sanPham.getIdChip())
                    .orElseThrow(() -> new RuntimeException("Chip không tồn tại: " + sanPham.getIdChip()));
            sanPham.setChip(chip);
        }

        if (sanPham.getIdGpu() != null) {
            Gpu gpu = gpuRepository.findById(sanPham.getIdGpu())
                    .orElseThrow(() -> new RuntimeException("GPU không tồn tại: " + sanPham.getIdGpu()));
            sanPham.setGpu(gpu);
        }

        if (sanPham.getIdSim() != null) {
            Sim sim = simRepository.findById(sanPham.getIdSim())
                    .orElseThrow(() -> new RuntimeException("SIM không tồn tại: " + sanPham.getIdSim()));
            sanPham.setSim(sim);
        }

        if (sanPham.getIdHeDieuHanh() != null) {
            HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(sanPham.getIdHeDieuHanh())
                    .orElseThrow(() -> new RuntimeException("Hệ điều hành không tồn tại: " + sanPham.getIdHeDieuHanh()));
            sanPham.setHeDieuHanh(heDieuHanh);
        }

        if (sanPham.getIdCpu() != null) {
            Cpu cpu = cpuRepository.findById(sanPham.getIdCpu())
                    .orElseThrow(() -> new RuntimeException("CPU không tồn tại: " + sanPham.getIdCpu()));
            sanPham.setCpu(cpu);
        }

        if (sanPham.getIdPin() != null) {
            Pin pin = pinRepository.findById(sanPham.getIdPin())
                    .orElseThrow(() -> new RuntimeException("Pin không tồn tại: " + sanPham.getIdPin()));
            sanPham.setPin(pin);
        }
        
        // Tự động tạo mã unique nếu bị trùng
        String originalMa = sanPham.getMaSanPham();
        String uniqueMa = generateUniqueMaSanPham(originalMa);
        sanPham.setMaSanPham(uniqueMa);
        
        return sanPhamRepository.save(sanPham);
    }
    
    private String generateUniqueMaSanPham(String baseMa) {
        String ma = baseMa;
        int counter = 1;
        
        while (sanPhamRepository.findByMaSanPham(ma).isPresent()) {
            ma = baseMa + "-" + counter;
            counter++;
        }
        
        return ma;
    }
    
    private String generateUniqueMaChiTiet(String maSanPham, Integer idRam, Integer idRom, Integer idMauSac) {
        String baseMa = maSanPham + "-" + 
                       (idRam != null ? idRam : "0") + "-" + 
                       (idRom != null ? idRom : "0") + "-" + 
                       (idMauSac != null ? idMauSac : "0");
        
        String ma = baseMa;
        int counter = 1;
        
        // Kiểm tra xem mã chi tiết đã tồn tại chưa
        while (chiTietSanPhamRepository.findByMaCtsp(ma).isPresent()) {
            ma = baseMa + "-" + counter;
            counter++;
        }
        
        return ma;
    }

    @Override
    public SanPham updateSanPham(Integer id, SanPham sanPham) {
        SanPham existingSanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        existingSanPham.setTenSanPham(sanPham.getTenSanPham());
        existingSanPham.setMoTa(sanPham.getMoTa());
        existingSanPham.setThietKe(sanPham.getThietKe());
        existingSanPham.setKichThuoc(sanPham.getKichThuoc());
        existingSanPham.setTrangThai(sanPham.getTrangThai());
        
        // Map quan hệ: ưu tiên idDanhMuc/idHang nếu có; nếu null cho phép bỏ liên kết
        if (sanPham.getIdDanhMuc() != null) {
            DanhMuc dm = danhMucRepository.findById(sanPham.getIdDanhMuc())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + sanPham.getIdDanhMuc()));
            existingSanPham.setDanhMuc(dm);
        } else if (sanPham.getDanhMuc() != null) {
            existingSanPham.setDanhMuc(sanPham.getDanhMuc());
        } else {
            // Không thay đổi danh mục nếu client không gửi thông tin
        }

        if (sanPham.getIdHang() != null) {
            Hang h = hangRepository.findById(sanPham.getIdHang())
                    .orElseThrow(() -> new RuntimeException("Hãng không tồn tại: " + sanPham.getIdHang()));
            existingSanPham.setHang(h);
        } else if (sanPham.getHang() != null) {
            existingSanPham.setHang(sanPham.getHang());
        } else {
            // Không thay đổi hãng nếu client không gửi thông tin
        }
        
        // Xử lý các trường khác
        if (sanPham.getIdManHinh() != null) {
            ManHinh manHinh = manHinhRepository.findById(sanPham.getIdManHinh())
                    .orElseThrow(() -> new RuntimeException("Màn hình không tồn tại: " + sanPham.getIdManHinh()));
            existingSanPham.setManHinh(manHinh);
        } else if (sanPham.getManHinh() != null) {
            existingSanPham.setManHinh(sanPham.getManHinh());
        } else {
            existingSanPham.setManHinh(null);
        }
        
        if (sanPham.getIdCameraTruoc() != null) {
            CameraTruoc cameraTruoc = cameraTruocRepository.findById(sanPham.getIdCameraTruoc())
                    .orElseThrow(() -> new RuntimeException("Camera trước không tồn tại: " + sanPham.getIdCameraTruoc()));
            existingSanPham.setCameraTruoc(cameraTruoc);
        } else if (sanPham.getCameraTruoc() != null) {
            existingSanPham.setCameraTruoc(sanPham.getCameraTruoc());
        } else {
            existingSanPham.setCameraTruoc(null);
        }
        
        if (sanPham.getIdCameraSau() != null) {
            CameraSau cameraSau = cameraSauRepository.findById(sanPham.getIdCameraSau())
                    .orElseThrow(() -> new RuntimeException("Camera sau không tồn tại: " + sanPham.getIdCameraSau()));
            existingSanPham.setCameraSau(cameraSau);
        } else if (sanPham.getCameraSau() != null) {
            existingSanPham.setCameraSau(sanPham.getCameraSau());
        } else {
            existingSanPham.setCameraSau(null);
        }
        
        if (sanPham.getIdChip() != null) {
            Chip chip = chipRepository.findById(sanPham.getIdChip())
                    .orElseThrow(() -> new RuntimeException("Chip không tồn tại: " + sanPham.getIdChip()));
            existingSanPham.setChip(chip);
        } else if (sanPham.getChip() != null) {
            existingSanPham.setChip(sanPham.getChip());
        } else {
            existingSanPham.setChip(null);
        }
        
        if (sanPham.getIdGpu() != null) {
            Gpu gpu = gpuRepository.findById(sanPham.getIdGpu())
                    .orElseThrow(() -> new RuntimeException("GPU không tồn tại: " + sanPham.getIdGpu()));
            existingSanPham.setGpu(gpu);
        } else if (sanPham.getGpu() != null) {
            existingSanPham.setGpu(sanPham.getGpu());
        } else {
            existingSanPham.setGpu(null);
        }
        
        if (sanPham.getIdSim() != null) {
            Sim sim = simRepository.findById(sanPham.getIdSim())
                    .orElseThrow(() -> new RuntimeException("Sim không tồn tại: " + sanPham.getIdSim()));
            existingSanPham.setSim(sim);
        } else if (sanPham.getSim() != null) {
            existingSanPham.setSim(sanPham.getSim());
        } else {
            existingSanPham.setSim(null);
        }
        
        if (sanPham.getIdHeDieuHanh() != null) {
            HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(sanPham.getIdHeDieuHanh())
                    .orElseThrow(() -> new RuntimeException("Hệ điều hành không tồn tại: " + sanPham.getIdHeDieuHanh()));
            existingSanPham.setHeDieuHanh(heDieuHanh);
        } else if (sanPham.getHeDieuHanh() != null) {
            existingSanPham.setHeDieuHanh(sanPham.getHeDieuHanh());
        } else {
            existingSanPham.setHeDieuHanh(null);
        }
        
        if (sanPham.getIdCpu() != null) {
            Cpu cpu = cpuRepository.findById(sanPham.getIdCpu())
                    .orElseThrow(() -> new RuntimeException("CPU không tồn tại: " + sanPham.getIdCpu()));
            existingSanPham.setCpu(cpu);
        } else if (sanPham.getCpu() != null) {
            existingSanPham.setCpu(sanPham.getCpu());
        } else {
            existingSanPham.setCpu(null);
        }
        
        if (sanPham.getIdPin() != null) {
            Pin pin = pinRepository.findById(sanPham.getIdPin())
                    .orElseThrow(() -> new RuntimeException("Pin không tồn tại: " + sanPham.getIdPin()));
            existingSanPham.setPin(pin);
        } else if (sanPham.getPin() != null) {
            existingSanPham.setPin(sanPham.getPin());
        } else {
            existingSanPham.setPin(null);
        }
        
        return sanPhamRepository.save(existingSanPham);
    }

    @Override
    public void deleteSanPham(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        sanPham.setTrangThai(0);
        sanPhamRepository.save(sanPham);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamDTO> getSanPhamByDanhMuc(Integer danhMucId) {
        return sanPhamRepository.findByDanhMucIdAndTrangThaiTrue(danhMucId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamDTO> getSanPhamByHang(Integer hangId) {
        return sanPhamRepository.findByHangIdAndTrangThaiTrue(hangId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public List<ChiTietSanPham> getChiTietSanPhamBySanPhamId(Integer sanPhamId) {
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPhamIdAndTrangThaiTrue(sanPhamId);
        
        // Tự động cập nhật số lượng dựa trên IMEI có trạng thái = 1 và lưu vào DB
        for (ChiTietSanPham chiTiet : chiTietList) {
            int activeImeiCount = (int) imeiRepository.findByChiTietSanPhamId(chiTiet.getId())
                    .stream()
                    .filter(imei -> imei.getTrangThai() == 1)
                    .count();
            chiTiet.setSoLuong(activeImeiCount);
            chiTiet.setNgayCapNhat(LocalDateTime.now());
            chiTietSanPhamRepository.save(chiTiet);
        }
        
        return chiTietList;
    }

    @Override
    @Transactional
    public List<ChiTietSanPham> getAllChiTietSanPhamBySanPhamId(Integer sanPhamId) {
        // Lấy TẤT CẢ chi tiết sản phẩm (kể cả inactive) cho admin
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPhamId(sanPhamId);
        
        // Tự động cập nhật số lượng dựa trên IMEI có trạng thái = 1 và lưu vào DB
        for (ChiTietSanPham chiTiet : chiTietList) {
            int activeImeiCount = (int) imeiRepository.findByChiTietSanPhamId(chiTiet.getId())
                    .stream()
                    .filter(imei -> imei.getTrangThai() == 1)
                    .count();
            chiTiet.setSoLuong(activeImeiCount);
            chiTiet.setNgayCapNhat(LocalDateTime.now());
            chiTietSanPhamRepository.save(chiTiet);
        }
        
        return chiTietList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietSanPham> getAvailableChiTietSanPham() {
        return chiTietSanPhamRepository.findAvailableProducts();
    }

    @Override
    @Transactional
    public List<ChiTietSanPham> getAllChiTietSanPham() {
        // Lấy TẤT CẢ chi tiết sản phẩm (kể cả inactive) cho admin
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findAll();
        
        // Tự động cập nhật số lượng dựa trên IMEI có trạng thái = 1 và lưu vào DB
        for (ChiTietSanPham chiTiet : chiTietList) {
            int activeImeiCount = (int) imeiRepository.findByChiTietSanPhamId(chiTiet.getId())
                    .stream()
                    .filter(imei -> imei.getTrangThai() == 1)
                    .count();
            chiTiet.setSoLuong(activeImeiCount);
            chiTiet.setNgayCapNhat(LocalDateTime.now());
            chiTietSanPhamRepository.save(chiTiet);
        }
        
        return chiTietList;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChiTietSanPham> getChiTietSanPhamById(Integer id) {
        return chiTietSanPhamRepository.findById(id);
    }

    @Override
    public ChiTietSanPham createChiTietSanPham(ChiTietSanPham chiTietSanPham) {
        if (chiTietSanPham.getNgayTao() == null) {
            chiTietSanPham.setNgayTao(LocalDateTime.now());
        }
        if (chiTietSanPham.getTrangThai() == null) {
            chiTietSanPham.setTrangThai(1);
        }
        
        // Xử lý các relationship từ transient fields
        if (chiTietSanPham.getIdSp() != null) {
            SanPham sanPham = sanPhamRepository.findById(chiTietSanPham.getIdSp())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + chiTietSanPham.getIdSp()));
            chiTietSanPham.setSanPham(sanPham);
        }
        
        if (chiTietSanPham.getRamId() != null) {
            Ram ram = ramRepository.findById(chiTietSanPham.getRamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy RAM với ID: " + chiTietSanPham.getRamId()));
            chiTietSanPham.setRam(ram);
        }
        
        if (chiTietSanPham.getRomId() != null) {
            Rom rom = romRepository.findById(chiTietSanPham.getRomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ROM với ID: " + chiTietSanPham.getRomId()));
            chiTietSanPham.setRom(rom);
        }
        
        if (chiTietSanPham.getMauSacId() != null) {
            MauSac mauSac = mauSacRepository.findById(chiTietSanPham.getMauSacId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc với ID: " + chiTietSanPham.getMauSacId()));
            chiTietSanPham.setMauSac(mauSac);
        }
        
        return chiTietSanPhamRepository.save(chiTietSanPham);
    }

    @Override
    public ChiTietSanPham createChiTietSanPhamFromDTO(ChiTietSanPhamCreateDTO dto) {
        ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
        
    
        // Tạo mã chi tiết sản phẩm unique nếu không có hoặc trùng lặp
        String maCtsp = dto.getMaCtsp();
        if (maCtsp == null || maCtsp.trim().isEmpty()) {
            // Lấy mã sản phẩm từ ID
            String maSanPham = "SP";
            if (dto.getIdSp() != null) {
                SanPham sanPham = sanPhamRepository.findById(dto.getIdSp()).orElse(null);
                if (sanPham != null) {
                    maSanPham = sanPham.getMaSanPham();
                }
            }
            maCtsp = generateUniqueMaChiTiet(maSanPham, dto.getRamId(), dto.getRomId(), dto.getMauSacId());
        } else {
            // Kiểm tra xem mã đã tồn tại chưa
            if (chiTietSanPhamRepository.existsByMaCtsp(maCtsp)) {
                String maSanPham = "SP";
                if (dto.getIdSp() != null) {
                    SanPham sanPham = sanPhamRepository.findById(dto.getIdSp()).orElse(null);
                    if (sanPham != null) {
                        maSanPham = sanPham.getMaSanPham();
                    }
                }
                maCtsp = generateUniqueMaChiTiet(maSanPham, dto.getRamId(), dto.getRomId(), dto.getMauSacId());
            }
        }
        chiTietSanPham.setMaCtsp(maCtsp);
        
        chiTietSanPham.setGiaNhap(dto.getGiaNhap());
        chiTietSanPham.setGiaBan(dto.getGiaBan());
        chiTietSanPham.setGhiChu(dto.getGhiChu());
        chiTietSanPham.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);
        chiTietSanPham.setNgayTao(LocalDateTime.now());
        
        // Set relationships
        if (dto.getIdSp() != null) {
            SanPham sanPham = sanPhamRepository.findById(dto.getIdSp())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + dto.getIdSp()));
            chiTietSanPham.setSanPham(sanPham);
        }
        
        if (dto.getRamId() != null) {
            Ram ram = ramRepository.findById(dto.getRamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy RAM với ID: " + dto.getRamId()));
            chiTietSanPham.setRam(ram);
        }
        
        if (dto.getRomId() != null) {
            Rom rom = romRepository.findById(dto.getRomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ROM với ID: " + dto.getRomId()));
            chiTietSanPham.setRom(rom);
        }
        
        if (dto.getMauSacId() != null) {
            MauSac mauSac = mauSacRepository.findById(dto.getMauSacId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc với ID: " + dto.getMauSacId()));
            chiTietSanPham.setMauSac(mauSac);
        }
        
        return chiTietSanPhamRepository.save(chiTietSanPham);
    }

    @Override
    public ChiTietSanPham updateChiTietSanPham(Integer id, ChiTietSanPham chiTietSanPham) {
        ChiTietSanPham existingChiTiet = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        
        existingChiTiet.setMaCtsp(chiTietSanPham.getMaCtsp());
        existingChiTiet.setGiaNhap(chiTietSanPham.getGiaNhap());
        existingChiTiet.setGiaBan(chiTietSanPham.getGiaBan());
        existingChiTiet.setGhiChu(chiTietSanPham.getGhiChu());
        existingChiTiet.setTrangThai(chiTietSanPham.getTrangThai());
        
        return chiTietSanPhamRepository.save(existingChiTiet);
    }

    private SanPhamDTO toDTO(SanPham sp) {
        // Tính tổng số IMEI cho sản phẩm này
        int tongImei = 0;
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPhamIdAndTrangThaiTrue(sp.getId());
        for (ChiTietSanPham chiTiet : chiTietList) {
            List<Imei> imeis = imeiRepository.findByChiTietSanPhamId(chiTiet.getId());
            tongImei += imeis.stream().mapToInt(imei -> imei.getTrangThai() == 1 ? 1 : 0).sum();
        }
        
        // Lấy thông tin từ chi tiết sản phẩm đầu tiên (nếu có)
        ChiTietSanPham firstChiTiet = chiTietList.isEmpty() ? null : chiTietList.get(0);
        
        // Lấy ảnh đầu tiên nếu có
        String hinhAnh = null;
        if (firstChiTiet != null) {
            List<HinhAnh> images = hinhAnhRepository.findByChiTietSanPhamId(firstChiTiet.getId());
            if (!images.isEmpty()) {
                hinhAnh = images.get(0).getUrlAnh();
            }
        }
        
        return SanPhamDTO.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .moTa(sp.getMoTa())
                .thietKe(sp.getThietKe())
                .kichThuoc(sp.getKichThuoc())
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .trangThai(sp.getTrangThai())
                .tenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null)
                .tenHang(sp.getHang() != null ? sp.getHang().getTen() : null)
                .tenManHinh(sp.getManHinh() != null ? sp.getManHinh().getKichThuoc() : null)
                .tenCameraTruoc(sp.getCameraTruoc() != null ? sp.getCameraTruoc().getThongSo() : null)
                .tenCameraSau(sp.getCameraSau() != null ? sp.getCameraSau().getThongSo() : null)
                .tenChip(sp.getChip() != null ? sp.getChip().getTenChip() : null)
                .tenGpu(sp.getGpu() != null ? sp.getGpu().getTenGpu() : null)
                .tenSim(sp.getSim() != null ? sp.getSim().getLoaiSim() : null)
                .tenHeDieuHanh(sp.getHeDieuHanh() != null ? sp.getHeDieuHanh().getTenHeDieuHanh() : null)
                .tenCpu(sp.getCpu() != null ? sp.getCpu().getTenCpu() : null)
                .tenPin(sp.getPin() != null ? sp.getPin().getDungLuongPin() : null)
                .tongImei(tongImei)
                // Thêm thông tin từ chi tiết sản phẩm
                .giaBan(firstChiTiet != null && firstChiTiet.getGiaBan() != null ? firstChiTiet.getGiaBan().doubleValue() : null)
                .hinhAnh(hinhAnh)
                .soLuong(tongImei)
                .tenRam(firstChiTiet != null && firstChiTiet.getRam() != null ? firstChiTiet.getRam().getTenRam() : null)
                .tenRom(firstChiTiet != null && firstChiTiet.getRom() != null ? firstChiTiet.getRom().getDungLuong() : null)
                .build();
    }

    private SanPhamEditDTO toEditDTO(SanPham sp) {
        // Get ONLY ACTIVE variants for editing (trangThai=1)
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPhamId(sp.getId());
        List<SanPhamEditDTO.VariantEditDTO> variants = chiTietList.stream()
                .filter(ct -> ct.getTrangThai() == 1) // Chỉ lấy variant còn hoạt động
                .map(ct -> {
            // Get IMEIs for this variant
            List<String> imeis = imeiRepository.findByChiTietSanPhamId(ct.getId())
                    .stream()
                    .filter(imei -> imei.getTrangThai() == 1)
                    .map(Imei::getImei)
                    .toList();

            // Get image URLs for this variant
            List<String> imageUrls = hinhAnhRepository.findByChiTietSanPhamId(ct.getId())
                    .stream()
                    .map(HinhAnh::getUrlAnh)
                    .toList();

            return SanPhamEditDTO.VariantEditDTO.builder()
                    .id(ct.getId())
                    .idRam(ct.getRam() != null ? ct.getRam().getId() : null)
                    .idRom(ct.getRom() != null ? ct.getRom().getId() : null)
                    .idMauSac(ct.getMauSac() != null ? ct.getMauSac().getId() : null)
                    .soLuong(imeis.size())
                    .donGia(ct.getGiaBan() != null ? ct.getGiaBan().longValue() : null)
                    .giaNhap(ct.getGiaNhap() != null ? ct.getGiaNhap().longValue() : null)
                    .ghiChu(ct.getGhiChu())
                    .trangThai(ct.getTrangThai()) // Include trangThai in the response
                    .imeis(imeis)
                    .imageUrls(imageUrls)
                    .tenRam(ct.getRam() != null ? ct.getRam().getTenRam() : null)
                    .tenRom(ct.getRom() != null ? ct.getRom().getDungLuong() : null)
                    .tenMauSac(ct.getMauSac() != null ? ct.getMauSac().getTenMau() : null)
                    .build();
        }).toList();

        return SanPhamEditDTO.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .moTa(sp.getMoTa())
                .thietKe(sp.getThietKe())
                .kichThuoc(sp.getKichThuoc())
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .trangThai(sp.getTrangThai())
                // ID fields
                .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                .idHang(sp.getHang() != null ? sp.getHang().getId() : null)
                .idManHinh(sp.getManHinh() != null ? sp.getManHinh().getId() : null)
                .idCameraTruoc(sp.getCameraTruoc() != null ? sp.getCameraTruoc().getId() : null)
                .idCameraSau(sp.getCameraSau() != null ? sp.getCameraSau().getId() : null)
                .idChip(sp.getChip() != null ? sp.getChip().getId() : null)
                .idGpu(sp.getGpu() != null ? sp.getGpu().getId() : null)
                .idSim(sp.getSim() != null ? sp.getSim().getId() : null)
                .idHeDieuHanh(sp.getHeDieuHanh() != null ? sp.getHeDieuHanh().getId() : null)
                .idCpu(sp.getCpu() != null ? sp.getCpu().getId() : null)
                .idPin(sp.getPin() != null ? sp.getPin().getId() : null)
                // Name fields
                .tenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null)
                .tenHang(sp.getHang() != null ? sp.getHang().getTen() : null)
                .tenManHinh(sp.getManHinh() != null ? sp.getManHinh().getKichThuoc() : null)
                .tenCameraTruoc(sp.getCameraTruoc() != null ? sp.getCameraTruoc().getThongSo() : null)
                .tenCameraSau(sp.getCameraSau() != null ? sp.getCameraSau().getThongSo() : null)
                .tenChip(sp.getChip() != null ? sp.getChip().getTenChip() : null)
                .tenGpu(sp.getGpu() != null ? sp.getGpu().getTenGpu() : null)
                .tenSim(sp.getSim() != null ? sp.getSim().getLoaiSim() : null)
                .tenHeDieuHanh(sp.getHeDieuHanh() != null ? sp.getHeDieuHanh().getTenHeDieuHanh() : null)
                .tenCpu(sp.getCpu() != null ? sp.getCpu().getTenCpu() : null)
                .tenPin(sp.getPin() != null ? sp.getPin().getDungLuongPin() : null)
                .variants(variants)
                .build();
    }

    private SanPhamViewDTO toViewDTO(SanPham sp) {
        // Tính tổng số IMEI cho sản phẩm này
        int tongImei = 0;
        List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPhamIdAndTrangThaiTrue(sp.getId());
        for (ChiTietSanPham chiTiet : chiTietList) {
            List<Imei> imeis = imeiRepository.findByChiTietSanPhamId(chiTiet.getId());
            tongImei += imeis.stream().mapToInt(imei -> imei.getTrangThai() == 1 ? 1 : 0).sum();
        }

        // Get variants for this product
        List<SanPhamViewDTO.VariantViewDTO> variants = chiTietList.stream().map(ct -> {
            // Get IMEIs for this variant
            List<String> imeis = imeiRepository.findByChiTietSanPhamId(ct.getId())
                    .stream()
                    .filter(imei -> imei.getTrangThai() == 1)
                    .map(Imei::getImei)
                    .toList();

            // Get image URLs for this variant
            List<String> imageUrls = hinhAnhRepository.findByChiTietSanPhamId(ct.getId())
                    .stream()
                    .map(HinhAnh::getUrlAnh)
                    .toList();

            return SanPhamViewDTO.VariantViewDTO.builder()
                    .id(ct.getId())
                    .idRam(ct.getRam() != null ? ct.getRam().getId() : null)
                    .idRom(ct.getRom() != null ? ct.getRom().getId() : null)
                    .idMauSac(ct.getMauSac() != null ? ct.getMauSac().getId() : null)
                    .soLuong(imeis.size())
                    .donGia(ct.getGiaBan() != null ? ct.getGiaBan().longValue() : null)
                    .giaNhap(ct.getGiaNhap() != null ? ct.getGiaNhap().longValue() : null)
                    .ghiChu(ct.getGhiChu())
                    .imeis(imeis)
                    .imageUrls(imageUrls)
                    .tenRam(ct.getRam() != null ? ct.getRam().getTenRam() : null)
                    .tenRom(ct.getRom() != null ? ct.getRom().getDungLuong() : null)
                    .tenMauSac(ct.getMauSac() != null ? ct.getMauSac().getTenMau() : null)
                    .build();
        }).toList();

        return SanPhamViewDTO.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .moTa(sp.getMoTa())
                .thietKe(sp.getThietKe())
                .kichThuoc(sp.getKichThuoc())
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .trangThai(sp.getTrangThai())
                // Name fields
                .tenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null)
                .tenHang(sp.getHang() != null ? sp.getHang().getTen() : null)
                .tenManHinh(sp.getManHinh() != null ? sp.getManHinh().getKichThuoc() : null)
                .tenCameraTruoc(sp.getCameraTruoc() != null ? sp.getCameraTruoc().getThongSo() : null)
                .tenCameraSau(sp.getCameraSau() != null ? sp.getCameraSau().getThongSo() : null)
                .tenChip(sp.getChip() != null ? sp.getChip().getTenChip() : null)
                .tenGpu(sp.getGpu() != null ? sp.getGpu().getTenGpu() : null)
                .tenSim(sp.getSim() != null ? sp.getSim().getLoaiSim() : null)
                .tenHeDieuHanh(sp.getHeDieuHanh() != null ? sp.getHeDieuHanh().getTenHeDieuHanh() : null)
                .tenCpu(sp.getCpu() != null ? sp.getCpu().getTenCpu() : null)
                .tenPin(sp.getPin() != null ? sp.getPin().getDungLuongPin() : null)
                .tongImei(tongImei)
                .variants(variants)
                .build();
    }

    @Override
    public void deleteChiTietSanPham(Integer id) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        chiTietSanPham.setTrangThai(0);
        chiTietSanPhamRepository.save(chiTietSanPham);
    }

    @Override
    public SanPhamDTO createSanPhamFull(SanPhamFullRequest request) {
        try {
            
            SanPham sp = new SanPham();
            sp.setTenSanPham(request.getTenSanPham());
            sp.setMoTa(request.getMoTa());
            sp.setThietKe(request.getThietKe());
            sp.setKichThuoc(request.getKichThuoc());
            sp.setTrangThai(1);
            sp.setNgayTao(java.time.LocalDateTime.now());
            sp.setMaSanPham(generateUniqueMaSanPham(request.getMaSanPham()));

            // Validate danh mục
            if (request.getIdDanhMuc() != null) {
                DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc()).orElse(null);
                if (danhMuc == null) {
                    throw new RuntimeException("Danh mục không tồn tại với ID: " + request.getIdDanhMuc());
                }
                sp.setDanhMuc(danhMuc);
            }
            
            // Validate hãng
            if (request.getIdHang() != null) {
                Hang hang = hangRepository.findById(request.getIdHang()).orElse(null);
                if (hang == null) {
                    throw new RuntimeException("Hãng không tồn tại với ID: " + request.getIdHang());
                }
                sp.setHang(hang);
            }
            
            // Validate và set các trường khác
            if (request.getIdManHinh() != null) {
                ManHinh manHinh = manHinhRepository.findById(request.getIdManHinh()).orElse(null);
                if (manHinh == null) {
                    throw new RuntimeException("Màn hình không tồn tại với ID: " + request.getIdManHinh());
                }
                sp.setManHinh(manHinh);
            }
            
            if (request.getIdCameraTruoc() != null) {
                CameraTruoc cameraTruoc = cameraTruocRepository.findById(request.getIdCameraTruoc()).orElse(null);
                if (cameraTruoc == null) {
                    throw new RuntimeException("Camera trước không tồn tại với ID: " + request.getIdCameraTruoc());
                }
                sp.setCameraTruoc(cameraTruoc);
            }
            
            if (request.getIdCameraSau() != null) {
                CameraSau cameraSau = cameraSauRepository.findById(request.getIdCameraSau()).orElse(null);
                if (cameraSau == null) {
                    throw new RuntimeException("Camera sau không tồn tại với ID: " + request.getIdCameraSau());
                }
                sp.setCameraSau(cameraSau);
            }
            
            if (request.getIdChip() != null) {
                Chip chip = chipRepository.findById(request.getIdChip()).orElse(null);
                if (chip == null) {
                    throw new RuntimeException("Chip không tồn tại với ID: " + request.getIdChip());
                }
                sp.setChip(chip);
            }
            
            if (request.getIdGpu() != null) {
                Gpu gpu = gpuRepository.findById(request.getIdGpu()).orElse(null);
                if (gpu == null) {
                    throw new RuntimeException("GPU không tồn tại với ID: " + request.getIdGpu());
                }
                sp.setGpu(gpu);
            }
            
            if (request.getIdSim() != null) {
                Sim sim = simRepository.findById(request.getIdSim()).orElse(null);
                if (sim == null) {
                    throw new RuntimeException("Sim không tồn tại với ID: " + request.getIdSim());
                }
                sp.setSim(sim);
            }
            
            if (request.getIdHeDieuHanh() != null) {
                HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(request.getIdHeDieuHanh()).orElse(null);
                if (heDieuHanh == null) {
                    throw new RuntimeException("Hệ điều hành không tồn tại với ID: " + request.getIdHeDieuHanh());
                }
                sp.setHeDieuHanh(heDieuHanh);
            }
            
            if (request.getIdCpu() != null) {
                Cpu cpu = cpuRepository.findById(request.getIdCpu()).orElse(null);
                if (cpu == null) {
                    throw new RuntimeException("CPU không tồn tại với ID: " + request.getIdCpu());
                }
                sp.setCpu(cpu);
            }
            
            if (request.getIdPin() != null) {
                Pin pin = pinRepository.findById(request.getIdPin()).orElse(null);
                if (pin == null) {
                    throw new RuntimeException("Pin không tồn tại với ID: " + request.getIdPin());
                }
                sp.setPin(pin);
            }

            SanPham saved = sanPhamRepository.save(sp);

            if (request.getVariants() != null) {
                for (SanPhamFullRequest.Variant v : request.getVariants()) {
                    
                    ChiTietSanPham ct = new ChiTietSanPham();
                    ct.setSanPham(saved);
                    
                    // Tạo mã chi tiết unique
                    String maChiTiet = generateUniqueMaChiTiet(saved.getMaSanPham(), v.getIdRam(), v.getIdRom(), v.getIdMauSac());
                    ct.setMaCtsp(maChiTiet);
                    
                    // Validate RAM
                    if (v.getIdRam() != null) {
                        Ram ram = ramRepository.findById(v.getIdRam()).orElse(null);
                        if (ram == null) {
                            throw new RuntimeException("RAM không tồn tại với ID: " + v.getIdRam());
                        }
                        ct.setRam(ram);
                    }
                    
                    // Validate ROM
                    if (v.getIdRom() != null) {
                        Rom rom = romRepository.findById(v.getIdRom()).orElse(null);
                        if (rom == null) {
                            throw new RuntimeException("ROM không tồn tại với ID: " + v.getIdRom());
                        }
                        ct.setRom(rom);
                    }
                    
                    // Validate màu sắc
                    if (v.getIdMauSac() != null) {
                        MauSac mau = mauSacRepository.findById(v.getIdMauSac()).orElse(null);
                        if (mau == null) {
                            throw new RuntimeException("Màu sắc không tồn tại với ID: " + v.getIdMauSac());
                        }
                        ct.setMauSac(mau);
                    }
                    
                    ct.setGiaBan(v.getDonGia() != null ? java.math.BigDecimal.valueOf(v.getDonGia()) : null);
                    ct.setGiaNhap(v.getGiaNhap() != null ? java.math.BigDecimal.valueOf(v.getGiaNhap()) : null);
                    ct.setGhiChu(v.getGhiChu());
                    ct.setTrangThai(1);
                    ct.setNgayTao(java.time.LocalDateTime.now());
                    
                    // Debug log
                    System.out.println("DEBUG - Saving ChiTietSanPham:");
                    System.out.println("  - GiaBan: " + ct.getGiaBan());
                    System.out.println("  - GiaNhap: " + ct.getGiaNhap());
                    System.out.println("  - GhiChu: " + ct.getGhiChu());
                    
                    ChiTietSanPham savedCt = chiTietSanPhamRepository.save(ct);
                    
                    // Debug log after save
                    System.out.println("DEBUG - Saved ChiTietSanPham ID: " + savedCt.getId());
                    System.out.println("  - GiaBan: " + savedCt.getGiaBan());
                    System.out.println("  - GiaNhap: " + savedCt.getGiaNhap());
                    System.out.println("  - GhiChu: " + savedCt.getGhiChu());
                    
                    // Xử lý IMEI nếu có
                    if (v.getImeis() != null && !v.getImeis().isEmpty()) {
                        for (String imeiCode : v.getImeis()) {
                            if (imeiCode != null && !imeiCode.trim().isEmpty()) {
                                String trimmedImei = imeiCode.trim();
                                
                                // Kiểm tra xem IMEI đã tồn tại chưa
                                Optional<Imei> existingImei = imeiRepository.findByImei(trimmedImei);
                                
                                if (existingImei.isPresent()) {
                                    // Nếu IMEI đã tồn tại, cập nhật chi tiết sản phẩm cho IMEI đó
                                    Imei imei = existingImei.get();
                                    imei.setChiTietSanPham(savedCt);
                                    // Note: ngay_cap_nhat column doesn't exist in the actual database
                                    // imei.setNgayCapNhat(java.time.LocalDateTime.now());
                                    imei.setTrangThai(1);
                                    imeiRepository.save(imei);
                                    System.out.println("DEBUG - Updated existing IMEI: " + trimmedImei);
                                } else {
                                    // Nếu IMEI chưa tồn tại, tạo mới
                                    Imei imei = Imei.builder()
                                        .imei(trimmedImei)
                                        .chiTietSanPham(savedCt)
                                        // Note: ngay_tao column doesn't exist in the actual database
                                        // .ngayTao(java.time.LocalDateTime.now())
                                        .trangThai(1)
                                        .build();
                                    imeiRepository.save(imei);
                                    System.out.println("DEBUG - Created new IMEI: " + trimmedImei);
                                }
                            }
                        }
                    }
                    
                    // Xử lý ảnh nếu có
                    if (v.getImageUrls() != null && !v.getImageUrls().isEmpty()) {
                        for (String imageUrl : v.getImageUrls()) {
                            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                HinhAnh hinhAnh = HinhAnh.builder()
                                    .chiTietSanPham(savedCt)
                                    .urlAnh(imageUrl.trim())
                                    // Note: ngay_tao column doesn't exist in the actual database
                                    // .ngayTao(java.time.LocalDateTime.now())
                                    .trangThai(1)
                                    .build();
                                hinhAnhService.save(hinhAnh);
                            }
                        }
                    }
                }
            }

            return toDTO(saved);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public SanPhamDTO updateSanPhamFull(Integer id, SanPhamFullRequest request) {
        try {
            // Tìm sản phẩm hiện tại
            SanPham existingSanPham = sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
            
            // Cập nhật thông tin sản phẩm chính
            existingSanPham.setTenSanPham(request.getTenSanPham());
            existingSanPham.setMoTa(request.getMoTa());
            existingSanPham.setThietKe(request.getThietKe());
            existingSanPham.setKichThuoc(request.getKichThuoc());
            existingSanPham.setNgayCapNhat(java.time.LocalDateTime.now());
            
            // Cập nhật các mối quan hệ
            if (request.getIdDanhMuc() != null) {
                DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc()).orElse(null);
                if (danhMuc == null) {
                    throw new RuntimeException("Danh mục không tồn tại với ID: " + request.getIdDanhMuc());
                }
                existingSanPham.setDanhMuc(danhMuc);
            }
            
            if (request.getIdHang() != null) {
                Hang hang = hangRepository.findById(request.getIdHang()).orElse(null);
                if (hang == null) {
                    throw new RuntimeException("Hãng không tồn tại với ID: " + request.getIdHang());
                }
                existingSanPham.setHang(hang);
            }
            
            // Cập nhật các trường khác tương tự như createSanPhamFull
            if (request.getIdManHinh() != null) {
                ManHinh manHinh = manHinhRepository.findById(request.getIdManHinh()).orElse(null);
                if (manHinh == null) {
                    throw new RuntimeException("Màn hình không tồn tại với ID: " + request.getIdManHinh());
                }
                existingSanPham.setManHinh(manHinh);
            }
            
            if (request.getIdCameraTruoc() != null) {
                CameraTruoc cameraTruoc = cameraTruocRepository.findById(request.getIdCameraTruoc()).orElse(null);
                if (cameraTruoc == null) {
                    throw new RuntimeException("Camera trước không tồn tại với ID: " + request.getIdCameraTruoc());
                }
                existingSanPham.setCameraTruoc(cameraTruoc);
            }
            
            if (request.getIdCameraSau() != null) {
                CameraSau cameraSau = cameraSauRepository.findById(request.getIdCameraSau()).orElse(null);
                if (cameraSau == null) {
                    throw new RuntimeException("Camera sau không tồn tại với ID: " + request.getIdCameraSau());
                }
                existingSanPham.setCameraSau(cameraSau);
            }
            
            if (request.getIdChip() != null) {
                Chip chip = chipRepository.findById(request.getIdChip()).orElse(null);
                if (chip == null) {
                    throw new RuntimeException("Chip không tồn tại với ID: " + request.getIdChip());
                }
                existingSanPham.setChip(chip);
            }
            
            if (request.getIdGpu() != null) {
                Gpu gpu = gpuRepository.findById(request.getIdGpu()).orElse(null);
                if (gpu == null) {
                    throw new RuntimeException("GPU không tồn tại với ID: " + request.getIdGpu());
                }
                existingSanPham.setGpu(gpu);
            }
            
            if (request.getIdSim() != null) {
                Sim sim = simRepository.findById(request.getIdSim()).orElse(null);
                if (sim == null) {
                    throw new RuntimeException("Sim không tồn tại với ID: " + request.getIdSim());
                }
                existingSanPham.setSim(sim);
            }
            
            if (request.getIdHeDieuHanh() != null) {
                HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(request.getIdHeDieuHanh()).orElse(null);
                if (heDieuHanh == null) {
                    throw new RuntimeException("Hệ điều hành không tồn tại với ID: " + request.getIdHeDieuHanh());
                }
                existingSanPham.setHeDieuHanh(heDieuHanh);
            }
            
            if (request.getIdCpu() != null) {
                Cpu cpu = cpuRepository.findById(request.getIdCpu()).orElse(null);
                if (cpu == null) {
                    throw new RuntimeException("CPU không tồn tại với ID: " + request.getIdCpu());
                }
                existingSanPham.setCpu(cpu);
            }
            
            if (request.getIdPin() != null) {
                Pin pin = pinRepository.findById(request.getIdPin()).orElse(null);
                if (pin == null) {
                    throw new RuntimeException("Pin không tồn tại với ID: " + request.getIdPin());
                }
                existingSanPham.setPin(pin);
            }
            
            // Lưu sản phẩm đã cập nhật
            SanPham savedSanPham = sanPhamRepository.save(existingSanPham);
            
            // Lấy TẤT CẢ chi tiết sản phẩm cũ (kể cả đã vô hiệu hóa) và xử lý
            List<ChiTietSanPham> allExistingChiTiet = chiTietSanPhamRepository.findBySanPhamId(id);
            
            // Thu thập các ID variant từ payload để biết variant nào được giữ lại
            java.util.Set<Integer> variantIdsInPayload = new java.util.HashSet<>();
            if (request.getVariants() != null) {
                for (SanPhamFullRequest.Variant v : request.getVariants()) {
                    if (v.getId() != null) {
                        variantIdsInPayload.add(v.getId());
                    }
                }
            }
            
            System.out.println("=== VARIANT DELETION DEBUG ===");
            System.out.println("DEBUG - Total existing variants: " + allExistingChiTiet.size());
            System.out.println("DEBUG - Variant IDs in payload: " + variantIdsInPayload);
            for (ChiTietSanPham ct : allExistingChiTiet) {
                System.out.println("DEBUG - Existing variant ID " + ct.getId() + " with trangThai=" + ct.getTrangThai());
            }
            
            // Xóa/vô hiệu hóa các variant kowhông còn trong payload
            for (ChiTietSanPham ct : allExistingChiTiet) {
                if (!variantIdsInPayload.contains(ct.getId())) {
                    // Variant này đã bị xóa khỏi frontend, vô hiệu hóa nó
                    System.out.println("DEBUG - Deactivating variant ID: " + ct.getId() + " (not in payload)");
                    ct.setTrangThai(0);
                    ct.setNgayCapNhat(java.time.LocalDateTime.now());
                    chiTietSanPhamRepository.save(ct);
                    System.out.println("DEBUG - Successfully deactivated variant ID: " + ct.getId());
                } else {
                    System.out.println("DEBUG - Keeping variant ID: " + ct.getId() + " (found in payload)");
                }
            }
            System.out.println("=== END VARIANT DELETION DEBUG ===");
            
            // Tạo hoặc cập nhật chi tiết sản phẩm từ variants
            if (request.getVariants() != null) {
                System.out.println("DEBUG - Received variants count: " + request.getVariants().size());
                for (SanPhamFullRequest.Variant v : request.getVariants()) {
                    System.out.println("DEBUG - Processing variant:");
                    System.out.println("  - id: " + v.getId());
                    System.out.println("  - donGia: " + v.getDonGia());
                    System.out.println("  - giaNhap: " + v.getGiaNhap());
                    System.out.println("  - ghiChu: " + v.getGhiChu());
                    System.out.println("  - trangThai: " + v.getTrangThai());
                    
                    ChiTietSanPham ct;
                    
                    // Nếu có ID, tìm và cập nhật variant cũ
                    if (v.getId() != null) {
                        ct = chiTietSanPhamRepository.findById(v.getId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + v.getId()));
                        System.out.println("DEBUG - Updating existing variant ID: " + v.getId());
                        ct.setNgayCapNhat(java.time.LocalDateTime.now());
                    } else {
                        // Tạo mới variant
                        ct = new ChiTietSanPham();
                        ct.setSanPham(savedSanPham);
                        ct.setNgayTao(java.time.LocalDateTime.now());
                        System.out.println("DEBUG - Creating new variant");
                        
                        // Tạo mã chi tiết unique cho variant mới
                        String maChiTiet = generateUniqueMaChiTiet(savedSanPham.getMaSanPham(), v.getIdRam(), v.getIdRom(), v.getIdMauSac());
                        ct.setMaCtsp(maChiTiet);
                    }
                    
                    // Validate và set relationships
                    if (v.getIdRam() != null) {
                        Ram ram = ramRepository.findById(v.getIdRam()).orElse(null);
                        if (ram == null) {
                            throw new RuntimeException("RAM không tồn tại với ID: " + v.getIdRam());
                        }
                        ct.setRam(ram);
                    }
                    
                    if (v.getIdRom() != null) {
                        Rom rom = romRepository.findById(v.getIdRom()).orElse(null);
                        if (rom == null) {
                            throw new RuntimeException("ROM không tồn tại với ID: " + v.getIdRom());
                        }
                        ct.setRom(rom);
                    }
                    
                    if (v.getIdMauSac() != null) {
                        MauSac mau = mauSacRepository.findById(v.getIdMauSac()).orElse(null);
                        if (mau == null) {
                            throw new RuntimeException("Màu sắc không tồn tại với ID: " + v.getIdMauSac());
                        }
                        ct.setMauSac(mau);
                    }
                    
                    ct.setGiaBan(v.getDonGia() != null ? java.math.BigDecimal.valueOf(v.getDonGia()) : null);
                    ct.setGiaNhap(v.getGiaNhap() != null ? java.math.BigDecimal.valueOf(v.getGiaNhap()) : null);
                    ct.setGhiChu(v.getGhiChu());
                    ct.setTrangThai(v.getTrangThai() != null ? v.getTrangThai() : 1);
                    
                    // Debug log
                    System.out.println("DEBUG - Saving ChiTietSanPham:");
                    System.out.println("  - GiaBan: " + ct.getGiaBan());
                    System.out.println("  - GiaNhap: " + ct.getGiaNhap());
                    System.out.println("  - GhiChu: " + ct.getGhiChu());
                    System.out.println("  - TrangThai: " + ct.getTrangThai());
                    
                    ChiTietSanPham savedCt = chiTietSanPhamRepository.save(ct);
                    
                    // Debug log after save
                    System.out.println("DEBUG - Saved ChiTietSanPham ID: " + savedCt.getId());
                    System.out.println("  - GiaBan: " + savedCt.getGiaBan());
                    System.out.println("  - GiaNhap: " + savedCt.getGiaNhap());
                    System.out.println("  - GhiChu: " + savedCt.getGhiChu());
                    
                    // Xử lý IMEI nếu có
                    if (v.getImeis() != null && !v.getImeis().isEmpty()) {
                        for (String imeiCode : v.getImeis()) {
                            if (imeiCode != null && !imeiCode.trim().isEmpty()) {
                                String trimmedImei = imeiCode.trim();
                                
                                // Kiểm tra xem IMEI đã tồn tại chưa
                                Optional<Imei> existingImei = imeiRepository.findByImei(trimmedImei);
                                
                                if (existingImei.isPresent()) {
                                    // Nếu IMEI đã tồn tại, cập nhật chi tiết sản phẩm cho IMEI đó
                                    Imei imei = existingImei.get();
                                    imei.setChiTietSanPham(savedCt);
                                    // Note: ngay_cap_nhat column doesn't exist in the actual database
                                    // imei.setNgayCapNhat(java.time.LocalDateTime.now());
                                    imei.setTrangThai(1);
                                    imeiRepository.save(imei);
                                    System.out.println("DEBUG - Updated existing IMEI: " + trimmedImei);
                                } else {
                                    // Nếu IMEI chưa tồn tại, tạo mới
                                    Imei imei = Imei.builder()
                                        .imei(trimmedImei)
                                        .chiTietSanPham(savedCt)
                                        // Note: ngay_tao column doesn't exist in the actual database
                                        // .ngayTao(java.time.LocalDateTime.now())
                                        .trangThai(1)
                                        .build();
                                    imeiRepository.save(imei);
                                    System.out.println("DEBUG - Created new IMEI: " + trimmedImei);
                                }
                            }
                        }
                    }
                    
                    // Xử lý ảnh nếu có - XÓA ảnh cũ trước khi thêm mới
                    if (v.getImageUrls() != null && !v.getImageUrls().isEmpty()) {
                        // XÓA TẤT CẢ ảnh cũ của variant này trước
                        List<HinhAnh> oldImages = hinhAnhRepository.findByChiTietSanPhamId(savedCt.getId());
                        for (HinhAnh oldImg : oldImages) {
                            hinhAnhService.delete(oldImg.getId());
                        }
                        System.out.println("DEBUG - Deleted " + oldImages.size() + " old images for variant ID: " + savedCt.getId());
                        
                        // Thêm ảnh mới
                        for (String imageUrl : v.getImageUrls()) {
                            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                HinhAnh hinhAnh = HinhAnh.builder()
                                    .chiTietSanPham(savedCt)
                                    .urlAnh(imageUrl.trim())
                                    // Note: ngay_tao column doesn't exist in the actual database
                                    // .ngayTao(java.time.LocalDateTime.now())
                                    .trangThai(1)
                                    .build();
                                hinhAnhService.save(hinhAnh);
                                System.out.println("DEBUG - Saved new image: " + imageUrl.trim());
                            }
                        }
                    }
                }
            }
            
            return toDTO(savedSanPham);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SanPhamEditDTO> getSanPhamForEdit(Integer id) {
        return sanPhamRepository.findById(id).map(this::toEditDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SanPhamViewDTO> getSanPhamForView(Integer id) {
        return sanPhamRepository.findById(id).map(this::toViewDTO);
    }

    @Override
    public int cleanupNullData() {
        try {
            // Xóa dữ liệu NULL trong chi_tiet_san_pham
            int deletedCount = chiTietSanPhamRepository.deleteByMaCtspIsNull();
            return deletedCount;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public HinhAnh addImageToChiTiet(Integer chiTietId, HinhAnh hinhAnh) {
        ChiTietSanPham chiTiet = chiTietSanPhamRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietId));
        
        hinhAnh.setChiTietSanPham(chiTiet);
        hinhAnh.setNgayTao(LocalDateTime.now());
        hinhAnh.setTrangThai(1);
        
        return hinhAnhService.save(hinhAnh);
    }

    @Override
    public List<HinhAnh> getImagesByChiTietId(Integer chiTietId) {
        return hinhAnhService.getByChiTietSanPhamId(chiTietId);
    }

    @Override
    @Transactional
    public void deleteAllImagesFromChiTiet(Integer chiTietId) {
        List<HinhAnh> images = hinhAnhRepository.findByChiTietSanPhamId(chiTietId);
        for (HinhAnh image : images) {
            hinhAnhService.delete(image.getId());
        }
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, Integer trangThai) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        sanPham.setTrangThai(trangThai);
        sanPham.setNgayCapNhat(LocalDateTime.now());
        sanPhamRepository.save(sanPham);
        
        // Nếu vô hiệu hóa sản phẩm (trangThai = 0), thông báo và xóa khỏi giỏ hàng
        if (trangThai != null && trangThai == 0) {
            // Tìm tất cả chi tiết sản phẩm của sản phẩm này
            List<ChiTietSanPham> chiTietList = chiTietSanPhamRepository.findBySanPham(sanPham);
            
            // Gửi WebSocket message cho từng chi tiết sản phẩm
            for (ChiTietSanPham chiTiet : chiTietList) {
                java.util.Map<String, Object> message = new java.util.HashMap<>();
                message.put("type", "PRODUCT_DEACTIVATED");
                message.put("productId", id);
                message.put("chiTietSanPhamId", chiTiet.getId());
                message.put("tenSanPham", sanPham.getTenSanPham());
                message.put("message", "Sản phẩm \"" + sanPham.getTenSanPham() + "\" đã ngừng hoạt động");
                
                // Broadcast đến tất cả clients
                messagingTemplate.convertAndSend("/topic/product-status", message);
            }
            
            // Tạo thông báo hệ thống
            notificationService.createSystemNotification(
                "Sản phẩm đã ngừng hoạt động",
                "Sản phẩm \"" + sanPham.getTenSanPham() + "\" đã được vô hiệu hóa"
            );
        }
    }

    @Override
    @Transactional
    public void updateChiTietStatus(Integer id, Integer trangThai) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        
        chiTietSanPham.setTrangThai(trangThai);
        chiTietSanPhamRepository.save(chiTietSanPham);
        
        // Nếu vô hiệu hóa chi tiết sản phẩm (trangThai = 0), thông báo và xóa khỏi giỏ hàng
        if (trangThai != null && trangThai == 0) {
            SanPham sanPham = chiTietSanPham.getSanPham();
            String tenSanPham = sanPham != null ? sanPham.getTenSanPham() : "Sản phẩm";
            
            // Gửi WebSocket message
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "PRODUCT_DETAIL_DEACTIVATED");
            message.put("chiTietSanPhamId", id);
            message.put("productId", sanPham != null ? sanPham.getId() : null);
            message.put("tenSanPham", tenSanPham);
            message.put("message", "Chi tiết sản phẩm \"" + tenSanPham + "\" đã ngừng hoạt động");
            
            // Broadcast đến tất cả clients
            messagingTemplate.convertAndSend("/topic/product-status", message);
            
            // Tạo thông báo hệ thống
            notificationService.createSystemNotification(
                "Chi tiết sản phẩm đã ngừng hoạt động",
                "Chi tiết sản phẩm \"" + tenSanPham + "\" đã được vô hiệu hóa"
            );
        }
    }
}
