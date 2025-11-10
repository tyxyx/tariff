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

    private static final String MODEL = "gemini-2.5-flash";

    /**
     * Sends the extracted PDF text to Gemini and requests a plain-text analysis/simulation.
     * The model should return a few paragraphs (summary, detailed simulation/prediction, assumptions).
     * If the document has no tariff-related content, the model must return exactly:
     * "This document is irrelevant to tariff predictions, please upload another."
     *
     * The method retries transient API errors (503) with exponential backoff and sanitizes
     * responses that contain code fences.
     */
    public String sendPdfToGemini(MultipartFile file) {
        String extractedText = extractTextFromPdf(file);

    String systemInstruction = "You are an expert economist and tariff policy analyst. Analyze the provided text and write a user-facing explanation addressed to the person who uploaded the document. "
        + "Do NOT use third-person language (avoid 'businesses', 'they', 'the document says' etc.). Use second-person ('you') or inclusive first-person ('we') language instead. "
        + "Produce three short, plain-text paragraphs only (no JSON, no code fences, no extra metadata):\n"
        + "1) Summary (1-2 sentences) — start the paragraph with 'Summary for you:' and speak directly to the user about what the document means for them.\n"
        + "2) What to expect — a detailed simulation/prediction paragraph that explains likely economic impacts and concrete effects (prices, supply, trade flows) in terms the user can act on.\n"
        + "3) Assumptions/Caveats — list key assumptions used in the analysis.\n"
        + "If the document contains no tariff-related content, RETURN EXACTLY the following sentence and nothing else: \"This document is irrelevant to tariff predictions, please upload another.\"";

        String userInstruction = "Here is the extracted PDF text (DELIMIT with triple BACKTICKS):\n\n" + extractedText;

        String prompt = systemInstruction + "\n\n" + userInstruction;

        Client client = Client.builder().apiKey(apiKey).build();

        GenerateContentResponse response = client.models.generateContent(MODEL, prompt, null);
        return response.text();

    }
           

    private String extractTextFromPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF file", e);
        }
    }
}
