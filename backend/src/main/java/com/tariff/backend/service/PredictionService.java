package com.tariff.backend.service;
import org.springframework.beans.factory.annotation.Value;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class PredictionService {
    @Value("${GEMINI_API_KEY}")
    private String apiKey;

        
        
    
    public String sendPdfToGemini(MultipartFile file) {
        
        String extractedText = "You are an assistant providing brief tariff predictions based only on the following extracted PDF text. Summarize key tariff changes or trends in 1-2 sentences for an app user. If the text is irrelevant to tariffs, reply politely: 'This document is irrelevant to tariff predictions, please upload another.' Keep the tone clear, simple, and user-friendly. Context:";
        extractedText += extractTextFromPdf(file);


        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response = client.models.generateContent(
                "gemini-2.5-flash",
                extractedText,
                null);
        return response.text();

    }

    private String extractTextFromPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
                PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF file", e);
        }
    }
}
