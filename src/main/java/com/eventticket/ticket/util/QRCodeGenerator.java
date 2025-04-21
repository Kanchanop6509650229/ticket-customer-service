package com.eventticket.ticket.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
@Slf4j
public class QRCodeGenerator {

    private static final int QR_CODE_SIZE = 250;

    public String generateQRCodeBase64(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code", e);
            return "";
        }
    }
}