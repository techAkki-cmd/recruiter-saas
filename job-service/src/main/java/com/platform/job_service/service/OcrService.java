package com.platform.job_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final ITesseract tesseract;

    public String extractTextLocally(File file) {
        log.info("Starting local OCR processing for file: {}", file.getName());

        try {
            String extractedText;

            Tika tika = new Tika();
            String mimeType = tika.detect(file);
            log.info("Tika detected actual file type as: {}", mimeType);

            if (mimeType != null && mimeType.startsWith("image/")) {
                log.info("Recognized true image format. Bypassing PDF parser completely.");
                BufferedImage image = ImageIO.read(file);

                if (image == null) {
                    throw new RuntimeException("ImageIO could not read the image data.");
                }

                extractedText = tesseract.doOCR(image);
            }
            else {
                log.info("Processing as standard document/PDF.");
                extractedText = tesseract.doOCR(file);
            }

            log.info("Successfully extracted text from file.");
            return extractedText;

        } catch (TesseractException e) {
            log.error("Tesseract engine failed to process file: {}", file.getName(), e);
            throw new RuntimeException("OCR Engine failure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error reading file for OCR: {}", file.getName(), e);
            throw new RuntimeException("Unexpected document processing error: " + e.getMessage(), e);
        }
    }
}