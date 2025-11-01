package com.tariff.backend.service;

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
    public String sendPdfToGemini(MultipartFile file) {
        String extractedText = "Only give responses that are related to tariff prediction. Use only the below context to predict how tariff prices may shift. If the below context is unrelated to tariffs, provide a basic response informing the client that the PDF uploaded is irrelevant";
        extractedText += extractTextFromPdf(file);
        
        Client client = Client.builder().apiKey("myapikey").build();

        GenerateContentResponse response =
            client.models.generateContent(
                "gemini-2.5-flash",
                extractedText,
                null);
        return response != null ? response.toString() : "No response from Gemini API";
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
