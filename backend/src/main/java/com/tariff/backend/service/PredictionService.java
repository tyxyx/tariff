package com.tariff.backend.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PredictionService {
    public String sendPdfToGemini(MultipartFile file) {
        Client client = Client.builder().apiKey("myapikey").build();

        GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash",
            "Explain how AI works in a few words",
            null);
        return "Gemini API response";
    }
}
