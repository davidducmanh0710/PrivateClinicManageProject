package com.spring.privateClinicManage.api;

import java.awt.image.BufferedImage;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.QrCode.QRZXingGenerator;
import com.spring.privateClinicManage.dto.QRDto;

@RestController
@RequestMapping("/api/qr/barcodes")
public class ApiQRRestController {

	@PostMapping(value = "/zxing/qrcode/", produces = MediaType.IMAGE_PNG_VALUE)
	@CrossOrigin
	public ResponseEntity<BufferedImage> barbecueEAN13Barcode(@RequestBody QRDto qrDto)
			throws Exception {
		return new ResponseEntity<>(
				QRZXingGenerator.generateQRCodeImage(qrDto.getText()),
				HttpStatus.OK);
	}
}
