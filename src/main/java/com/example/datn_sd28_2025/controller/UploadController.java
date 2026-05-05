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
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private HangRepository hangRepository;

    private static final String UPLOAD_DIR = "uploads/avatar/";
    private static final String IMAGE_DIR = "uploads/images/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ chấp nhận file ảnh");
                return ResponseEntity.badRequest().body(error);
            }

            // Create upload directory if not exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            String filePath = UPLOAD_DIR + filename;

            // Save file
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // Return response
            Map<String, String> response = new HashMap<>();
            response.put("url", "/uploads/avatar/" + filename);
            response.put("filename", filename);
            response.put("message", "Upload thành công");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi lưu file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/avatar/{avatarFilename}")
    public ResponseEntity<?> deleteAvatar(@PathVariable("avatarFilename") String filename) {
        try {
            String filePath = UPLOAD_DIR + filename;
            File file = new File(filePath);
            
            if (file.exists()) {
                if (file.delete()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Xóa file thành công");
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Không thể xóa file");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File không tồn tại");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi xóa file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Upload ảnh cho Danh Mục
     */
    @PostMapping("/image/danh-muc")
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

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "Kích thước file không được vượt quá 5MB"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File phải là hình ảnh"));
            }

            // Tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(IMAGE_DIR + "danh-muc");
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
    @PostMapping("/image/hang")
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

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "Kích thước file không được vượt quá 5MB"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File phải là hình ảnh"));
            }

            // Tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(IMAGE_DIR + "hang");
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
    @DeleteMapping("/image/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteImage(@PathVariable("id") Integer id) {
        try {
            System.out.println("=== Bắt đầu xóa ảnh ID: " + id + " ===");
            
            // Kiểm tra xem ảnh có tồn tại không
            Optional<HinhAnh> hinhAnhOpt = hinhAnhRepository.findById(id);
            if (!hinhAnhOpt.isPresent()) {
                System.out.println("Không tìm thấy ảnh với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hình ảnh với ID: " + id, "id", id));
            }
            
            HinhAnh hinhAnh = hinhAnhOpt.get();
            String urlAnh = hinhAnh.getUrlAnh();
            System.out.println("Tìm thấy ảnh: ID=" + hinhAnh.getId() + ", URL=" + urlAnh);

            // Xóa file vật lý (nếu có) - bỏ qua lỗi nếu không xóa được
            if (urlAnh != null && !urlAnh.isEmpty() && 
                !urlAnh.startsWith("http://") && !urlAnh.startsWith("https://")) {
                try {
                    String filePath = urlAnh.startsWith("/") ? urlAnh.substring(1) : urlAnh;
                    File file = new File(filePath);
                    
                    if (!file.exists()) {
                        String fullPath = System.getProperty("user.dir") + File.separator + filePath;
                        file = new File(fullPath);
                    }
                    
                    if (!file.exists() && urlAnh.contains("uploads/images/")) {
                        String fileName = urlAnh.substring(urlAnh.lastIndexOf("/") + 1);
                        String dir = urlAnh.contains("/hang/") ? "hang" : 
                                   urlAnh.contains("/danh-muc/") ? "danh-muc" : "";
                        if (!dir.isEmpty()) {
                            file = new File(IMAGE_DIR + dir + File.separator + fileName);
                        }
                    }
                    
                    if (file.exists() && file.isFile()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            System.out.println("Đã xóa file: " + file.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    // Bỏ qua lỗi xóa file vật lý
                    System.out.println("Không thể xóa file vật lý (bỏ qua): " + e.getMessage());
                }
            }

            // Xóa record trong database - đơn giản hóa
            System.out.println("Đang xóa record trong database...");
            try {
                hinhAnhRepository.delete(hinhAnh);
                hinhAnhRepository.flush(); // Đảm bảo xóa ngay lập tức
                System.out.println("Đã xóa record thành công trong database");
            } catch (Exception deleteException) {
                System.err.println("Lỗi khi xóa record: " + deleteException.getMessage());
                throw deleteException; // Re-throw để handle ở catch block bên ngoài
            }

            // Bước 4: Verify đã xóa
            Optional<HinhAnh> verifyOpt = hinhAnhRepository.findById(id);
            if (verifyOpt.isPresent()) {
                System.err.println("CẢNH BÁO: Ảnh vẫn còn tồn tại sau khi xóa!");
                throw new RuntimeException("Không thể xóa ảnh ID: " + id);
            } else {
                System.out.println("Xác nhận: Ảnh đã được xóa khỏi database");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Xóa ảnh thành công");
            response.put("id", id);
            
            System.out.println("=== Hoàn thành xóa ảnh ID: " + id + " ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("=== EXCEPTION CHI TIẾT ===");
            System.err.println("Exception khi xóa ảnh ID " + id + ": " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getSimpleName());

            // In stack trace chi tiết
            e.printStackTrace();

            // Kiểm tra cause gốc
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Root cause: " + cause.getMessage());
                System.err.println("Root cause type: " + cause.getClass().getSimpleName());
            }

            // Trả về lỗi chi tiết
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = "Lỗi không xác định";
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi xóa ảnh: " + errorMsg);
            errorResponse.put("id", id);
            errorResponse.put("details", e.getClass().getSimpleName());
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            if (cause != null) {
                errorResponse.put("rootCause", cause.getMessage());
                errorResponse.put("rootCauseType", cause.getClass().getSimpleName());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
