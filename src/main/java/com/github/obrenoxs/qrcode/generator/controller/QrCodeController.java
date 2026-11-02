package com.github.obrenoxs.qrcode.generator.controller;

import com.github.obrenoxs.qrcode.generator.dto.QrCodeGenerateRequest;
import com.github.obrenoxs.qrcode.generator.dto.QrCodeGenerateResponse;
import com.github.obrenoxs.qrcode.generator.service.QrCodeGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qrcode")
public class QrCodeController {

    private final QrCodeGeneratorService qrCodeGeneratorService;

    public QrCodeController(QrCodeGeneratorService qrCodeGeneratorService) {
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    @PostMapping
    public ResponseEntity<QrCodeGenerateResponse> generate(@RequestBody QrCodeGenerateRequest request) {
        try {
            QrCodeGenerateResponse qrCodeGenerateResponse = qrCodeGeneratorService.generateAndUploadQrCode(request.text());
            return ResponseEntity.ok().body(qrCodeGenerateResponse);
        } catch(Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
