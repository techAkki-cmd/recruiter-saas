package com.platform.job_service.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    // Defaults to the standard Linux Docker path if not provided
    @Value("${app.ocr.tesseract.datapath:/usr/share/tesseract-ocr/4.00/tessdata}")
    private String tessDataPath;

    @Value("${app.ocr.tesseract.language:eng}")
    private String language;

    @Bean
    public ITesseract tesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage(language);
        return tesseract;
    }
}