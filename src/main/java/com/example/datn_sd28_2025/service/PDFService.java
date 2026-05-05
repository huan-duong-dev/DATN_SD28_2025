package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PDFService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    public byte[] generateBienBanTiepNhan(PhieuBaoHanhDTO phieu) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // Header
        Paragraph title = new Paragraph("BIÊN BẢN TIẾP NHẬN BẢO HÀNH")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Số phiếu
        Paragraph soPhieu = new Paragraph("Số phiếu: " + (phieu.getMaPhieu() != null ? phieu.getMaPhieu() : ""))
                .setFontSize(12)
                .setBold()
                .setMarginBottom(15);
        document.add(soPhieu);

        // 1. Thông tin cửa hàng
        document.add(createSectionTitle("1. Thông tin cửa hàng"));
        Table storeInfo = new Table(2);
        storeInfo.setWidth(UnitValue.createPercentValue(100));
        addCell(storeInfo, "Tên cửa hàng:", "PhoniX Store", false);
        addCell(storeInfo, "Mã chi nhánh:", "CN001", false);
        addCell(storeInfo, "Địa chỉ:", "123 Đường ABC, Quận XYZ, TP.HCM", false);
        addCell(storeInfo, "SĐT:", "0919965342", false);
        document.add(storeInfo);
        document.add(new Paragraph().setMarginBottom(10));

        // 2. Thông tin khách hàng
        document.add(createSectionTitle("2. Thông tin khách hàng"));
        Table customerInfo = new Table(2);
        customerInfo.setWidth(UnitValue.createPercentValue(100));
        addCell(customerInfo, "Họ tên:", phieu.getTenKhachHang() != null ? phieu.getTenKhachHang() : "", false);
        addCell(customerInfo, "SĐT:", phieu.getSoDienThoai() != null ? phieu.getSoDienThoai() : "", false);
        addCell(customerInfo, "Email:", phieu.getEmailKhachHang() != null ? phieu.getEmailKhachHang() : "", false);
        document.add(customerInfo);
        document.add(new Paragraph().setMarginBottom(10));

        // 3. Thông tin sản phẩm
        document.add(createSectionTitle("3. Thông tin sản phẩm"));
        Table productInfo = new Table(2);
        productInfo.setWidth(UnitValue.createPercentValue(100));
        addCell(productInfo, "Hãng/Model:", phieu.getTenSanPham() != null ? phieu.getTenSanPham() : "", false);
        addCell(productInfo, "Màu/SKU:", phieu.getMauSacSku() != null ? phieu.getMauSacSku() : "", false);
        addCell(productInfo, "IMEI/Serial:", phieu.getImeiSerial() != null ? phieu.getImeiSerial() : "", false);
        document.add(productInfo);
        document.add(new Paragraph().setMarginBottom(10));

        // 4. Tình trạng máy khi tiếp nhận
        document.add(createSectionTitle("4. Tình trạng máy khi tiếp nhận"));
        Paragraph tinhTrang = new Paragraph("Mô tả chi tiết: " + (phieu.getTinhTrangVatLy() != null ? phieu.getTinhTrangVatLy() : ""))
                .setMarginBottom(5);
        document.add(tinhTrang);
        
        Table tinhTrangTable = new Table(2);
        tinhTrangTable.setWidth(UnitValue.createPercentValue(100));
        addCheckboxCell(tinhTrangTable, "Trầy xước nhẹ", phieu.getTrayXuocNhe() != null && phieu.getTrayXuocNhe());
        addCheckboxCell(tinhTrangTable, "Cấn móp", phieu.getCanMop() != null && phieu.getCanMop());
        addCheckboxCell(tinhTrangTable, "Mẻ viền", phieu.getMeVen() != null && phieu.getMeVen());
        addCheckboxCell(tinhTrangTable, "Màn hình sọc/điểm chết", phieu.getManHinhSocDiemChet() != null && phieu.getManHinhSocDiemChet());
        addCheckboxCell(tinhTrangTable, "Vào nước", phieu.getVaoNuoc() != null && phieu.getVaoNuoc());
        addCheckboxCell(tinhTrangTable, "Tem bảo hành rách/mất", phieu.getTemBaoHanhRachMat() != null && phieu.getTemBaoHanhRachMat());
        document.add(tinhTrangTable);
        document.add(new Paragraph().setMarginBottom(10));

        // 5. Phụ kiện đi kèm
        document.add(createSectionTitle("5. Phụ kiện đi kèm"));
        Table phuKienTable = new Table(2);
        phuKienTable.setWidth(UnitValue.createPercentValue(100));
        addCheckboxCell(phuKienTable, "Sạc", phieu.getPhuKienSac() != null && phieu.getPhuKienSac());
        addCheckboxCell(phuKienTable, "Cáp", phieu.getPhuKienCap() != null && phieu.getPhuKienCap());
        addCheckboxCell(phuKienTable, "Hộp", phieu.getPhuKienHop() != null && phieu.getPhuKienHop());
        if (phieu.getPhuKienKhac() != null && !phieu.getPhuKienKhac().isEmpty()) {
            addCell(phuKienTable, "Phụ kiện khác:", phieu.getPhuKienKhac(), false);
        }
        document.add(phuKienTable);
        document.add(new Paragraph().setMarginBottom(10));

        // 6. Mô tả lỗi
        document.add(createSectionTitle("6. Mô tả lỗi"));
        Table loiInfo = new Table(1);
        loiInfo.setWidth(UnitValue.createPercentValue(100));
        addCell(loiInfo, "Khách hàng mô tả: " + (phieu.getMoTaLoiKhachHang() != null ? phieu.getMoTaLoiKhachHang() : ""), "", true);
        addCell(loiInfo, "Nhân viên ghi nhận: " + (phieu.getMoTaLoiNhanVien() != null ? phieu.getMoTaLoiNhanVien() : ""), "", true);
        document.add(loiInfo);
        document.add(new Paragraph().setMarginBottom(10));

        // 7. Kiểm tra nhanh
        document.add(createSectionTitle("7. Kiểm tra nhanh (NV/KTV)"));
        Paragraph kiemTra = new Paragraph("Ghi các chức năng cơ bản đã test và kết quả: " + 
                (phieu.getKiemTraNhanh() != null ? phieu.getKiemTraNhanh() : ""))
                .setMarginBottom(10);
        document.add(kiemTra);

        // 8. Xác minh bảo hành
        document.add(createSectionTitle("8. Xác minh bảo hành"));
        Table xacMinh = new Table(2);
        xacMinh.setWidth(UnitValue.createPercentValue(100));
        String baoHanhConHanText = phieu.getBaoHanhConHan() != null ? 
            (phieu.getBaoHanhConHan() ? "Thời hạn: Còn hạn" : "Thời hạn: Hết hạn") : 
            "Thời hạn: Còn hạn / Hết hạn";
        addCheckboxCell(xacMinh, baoHanhConHanText, phieu.getBaoHanhConHan() != null && phieu.getBaoHanhConHan());
        String imeiTrungKhopText = phieu.getImeiTrungKhop() != null ? 
            (phieu.getImeiTrungKhop() ? "IMEI/Serial trùng khớp: Có" : "IMEI/Serial trùng khớp: Không") : 
            "IMEI/Serial trùng khớp: Có / Không";
        addCheckboxCell(xacMinh, imeiTrungKhopText, phieu.getImeiTrungKhop() != null && phieu.getImeiTrungKhop());
        String temNguyenVenText = phieu.getTemNguyenVen() != null ? 
            (phieu.getTemNguyenVen() ? "Tem nguyên vẹn: Có" : "Tem nguyên vẹn: Không") : 
            "Tem nguyên vẹn: Có / Không";
        addCheckboxCell(xacMinh, temNguyenVenText, phieu.getTemNguyenVen() != null && phieu.getTemNguyenVen());
        document.add(xacMinh);
        document.add(new Paragraph().setMarginBottom(10));

        // 9. Kết luận điều kiện bảo hành
        document.add(createSectionTitle("9. Kết luận điều kiện bảo hành"));
        boolean duDieuKien = phieu.getDuDieuKienBaoHanh() != null && phieu.getDuDieuKienBaoHanh();
        Paragraph ketLuan = new Paragraph((duDieuKien ? "☑ Đủ điều kiện" : "☐ Không đủ điều kiện") + 
                (phieu.getLyDoKhongDuDieuKien() != null && !phieu.getLyDoKhongDuDieuKien().isEmpty() ? 
                        " (Lý do: " + phieu.getLyDoKhongDuDieuKien() + ")" : ""))
                .setMarginBottom(10);
        document.add(ketLuan);

        // 10. Hướng xử lý
        document.add(createSectionTitle("10. Hướng xử lý"));
        String huongXuLy = phieu.getHuongXuLy() != null ? phieu.getHuongXuLy() : "";
        boolean suaTaiCuaHang = "SUA_TAI_CUA_HANG".equals(huongXuLy);
        boolean guiTTBH = "GUI_TTBH_HANG".equals(huongXuLy);
        Paragraph huongXuLyPara = new Paragraph((suaTaiCuaHang ? "☑" : "☐") + " Sửa tại cửa hàng | " +
                (guiTTBH ? "☑" : "☐") + " Gửi TTBH của hãng")
                .setMarginBottom(5);
        document.add(huongXuLyPara);
        
        String tenNhanVien = "";
        if (phieu.getNhanVienTiepNhanId() != null) {
            Optional<NhanVien> nv = nhanVienRepository.findById(phieu.getNhanVienTiepNhanId());
            if (nv.isPresent()) {
                tenNhanVien = nv.get().getHoTen() != null ? nv.get().getHoTen() : "";
            }
        }
        Paragraph nguoiPhuTrach = new Paragraph("Người phụ trách: " + tenNhanVien)
                .setMarginBottom(10);
        document.add(nguoiPhuTrach);

        // 11. Ngày hẹn trả dự kiến
        document.add(createSectionTitle("11. Ngày hẹn trả dự kiến"));
        String ngayHen = phieu.getNgayHenTraDuKien() != null ? 
                phieu.getNgayHenTraDuKien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        Paragraph ngayHenPara = new Paragraph("Ngày: " + ngayHen)
                .setMarginBottom(10);
        document.add(ngayHenPara);

        // 12. Biên bản bàn giao (nếu gửi đi)
        document.add(createSectionTitle("12. Biên bản bàn giao (nếu gửi đi)"));
        Paragraph banGiao = new Paragraph("Mã vận đơn: " + (phieu.getMaBaoHanhHang() != null ? phieu.getMaBaoHanhHang() : "") + 
                " | Tình trạng niêm phong: ")
                .setMarginBottom(10);
        document.add(banGiao);

        // 13. Chữ ký xác nhận
        document.add(createSectionTitle("13. Chữ ký xác nhận"));
        String ngayLap = phieu.getNgayNhan() != null ? 
                phieu.getNgayNhan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph ngayLapPara = new Paragraph("Ngày lập biên bản: " + ngayLap)
                .setMarginBottom(15);
        document.add(ngayLapPara);

        Table signatureTable = new Table(3);
        signatureTable.setWidth(UnitValue.createPercentValue(100));
        addSignatureCell(signatureTable, "Khách hàng\n\n\n(Ký và ghi rõ họ tên)");
        addSignatureCell(signatureTable, "Nhân viên Tiếp nhận\n\n\n(Ký và ghi rõ họ tên)");
        addSignatureCell(signatureTable, "KTV/Quản lý\n\n\n(Ký và ghi rõ họ tên)");
        document.add(signatureTable);

        document.close();
        return baos.toByteArray();
    }

    private Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(12)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5);
    }

    private void addCell(Table table, String label, String value, boolean fullWidth) {
        Cell labelCell;
        if (fullWidth) {
            // Tạo Cell với colspan = 2
            labelCell = new Cell(1, 2).add(new Paragraph(label).setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
        } else {
            labelCell = new Cell().add(new Paragraph(label).setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
        }
        table.addCell(labelCell);
        
        if (!fullWidth) {
            Cell valueCell = new Cell().add(new Paragraph(value != null ? value : ""))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
            table.addCell(valueCell);
        }
    }

    private void addCheckboxCell(Table table, String text, boolean checked) {
        Cell cell = new Cell().add(new Paragraph((checked ? "☑" : "☐") + " " + text))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
        table.addCell(cell);
    }

    private void addSignatureCell(Table table, String text) {
        Cell cell = new Cell().add(new Paragraph(text))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setHeight(100);
        table.addCell(cell);
    }
}

