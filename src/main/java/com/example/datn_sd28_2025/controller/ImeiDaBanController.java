package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ImeiDaBanDTO;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imei-da-ban")
@CrossOrigin(origins = "*")
public class ImeiDaBanController {

    @Autowired
    private ImeiDaBanService imeiDaBanService;

    @GetMapping("/imei/{imei}")
    public ResponseEntity<ImeiDaBanDTO> getByImei(@PathVariable String imei) {
        ImeiDaBanDTO imeiDaBan = imeiDaBanService.getByImei(imei);
        if (imeiDaBan != null) {
            return ResponseEntity.ok(imeiDaBan);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/hoa-don-chi-tiet/{idHoaDonChiTiet}")
    public ResponseEntity<List<ImeiDaBanDTO>> getByHoaDonChiTiet(@PathVariable Integer idHoaDonChiTiet) {
        List<ImeiDaBanDTO> imeiDaBans = imeiDaBanService.getByHoaDonChiTiet(idHoaDonChiTiet);
        return ResponseEntity.ok(imeiDaBans);
    }

    @GetMapping("/all-active")
    public ResponseEntity<List<ImeiDaBanDTO>> getAllActive() {
        List<ImeiDaBanDTO> imeiDaBans = imeiDaBanService.getAllActive();
        return ResponseEntity.ok(imeiDaBans);
    }
}

