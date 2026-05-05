package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.DanhMuc;
import com.example.datn_sd28_2025.entity.Hang;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.repository.DanhMucRepository;
import com.example.datn_sd28_2025.repository.HangRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hinh-anh")
@CrossOrigin(origins = "*")
public class HinhAnhController {

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private HangRepository hangRepository;

    // Đường dẫn lưu ảnh
    private static final String UPLOAD_DIR = "uploads/images/";

    /**
     * Upload ảnh cho Danh Mục
     */
    @PostMapping("/upload-danh-muc")
    public ResponseEntity<?> uploadDanhMucImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idDanhMuc") Integer idDanhMuc) {
        
        try {
            // Kiểm tra danh mục tồn tại
            DanhMuc danhMuc = danhMucRepository.findById(idDanhMuc)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + idDanhMuc));

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File phải là hình ảnh"));
            }

            // Tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(UPLOAD_DIR + "danh-muc");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String fileName = "danh-muc-" + idDanhMuc + "-" + UUID.randomUUID().toString() + extension;
            
            // Lưu file
            Path filePath = Paths.get(uploadDir.getPath(), fileName);
            Files.write(filePath, file.getBytes());

            // Tạo URL
            String fileUrl = "/uploads/images/danh-muc/" + fileName;

            // Lưu vào database
            HinhAnh hinhAnh = HinhAnh.builder()
                    .danhMuc(danhMuc)
                    .urlAnh(fileUrl)
                    .ngayTao(LocalDateTime.now())
                    .trangThai(1)
                    .build();

            hinhAnhRepository.save(hinhAnh);

            Map<String, Object> response = new HashMap<>();
            response.put("id", hinhAnh.getId());
            response.put("urlAnh", fileUrl);
            response.put("message", "Upload thành công");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lưu file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Upload ảnh cho Hãng
     */
    @PostMapping("/upload-hang")
    public ResponseEntity<?> uploadHangImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idHang") Integer idHang) {
        
        try {
            // Kiểm tra hãng tồn tại
            Hang hang = hangRepository.findById(idHang)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng với ID: " + idHang));

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File phải là hình ảnh"));
            }

            // Tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(UPLOAD_DIR + "hang");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String fileName = "hang-" + idHang + "-" + UUID.randomUUID().toString() + extension;
            
            // Lưu file
            Path filePath = Paths.get(uploadDir.getPath(), fileName);
            Files.write(filePath, file.getBytes());

            // Tạo URL
            String fileUrl = "/uploads/images/hang/" + fileName;

            // Lưu vào database
            HinhAnh hinhAnh = HinhAnh.builder()
                    .hang(hang)
                    .urlAnh(fileUrl)
                    .ngayTao(LocalDateTime.now())
                    .trangThai(1)
                    .build();

            hinhAnhRepository.save(hinhAnh);

            Map<String, Object> response = new HashMap<>();
            response.put("id", hinhAnh.getId());
            response.put("urlAnh", fileUrl);
            response.put("message", "Upload thành công");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lưu file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa ảnh theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer id) {
        try {
            HinhAnh hinhAnh = hinhAnhRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh với ID: " + id));

            // Xóa file vật lý
            String urlAnh = hinhAnh.getUrlAnh();
            if (urlAnh != null && !urlAnh.isEmpty()) {
                // Remove leading slash if present
                String filePath = urlAnh.startsWith("/") ? urlAnh.substring(1) : urlAnh;
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }

            // Xóa record trong database
            hinhAnhRepository.delete(hinhAnh);

            return ResponseEntity.ok(Map.of("message", "Xóa ảnh thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi xóa ảnh: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách ảnh theo Danh Mục
     */
    @GetMapping("/danh-muc/{idDanhMuc}")
    public ResponseEntity<?> getImagesByDanhMuc(@PathVariable Integer idDanhMuc) {
        try {
            return ResponseEntity.ok(hinhAnhRepository.findByDanhMucId(idDanhMuc));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách ảnh theo Hãng
     */
    @GetMapping("/hang/{idHang}")
    public ResponseEntity<?> getImagesByHang(@PathVariable Integer idHang) {
        try {
            return ResponseEntity.ok(hinhAnhRepository.findByHangId(idHang));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
