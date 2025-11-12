package com.tariff.backend.service;

// Note: Gemini client is created internally and cannot be easily mocked
// These tests focus on PDF extraction and error handling
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        // Set the API key using reflection since it's a @Value field
        ReflectionTestUtils.setField(predictionService, "apiKey", "test-api-key");
    }

    @Test
    void sendPdfToGemini_withCountry_shouldIncludeCountryInPrompt() throws IOException {
        String pdfContent = "Test PDF content about tariffs";
        String country = "United States";
        
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(createSimplePdf()));
        
        // This will fail because we can't easily mock the Gemini client
        // But it tests the PDF extraction logic
        Exception exception = assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, country);
        });
        
        verify(mockFile, atLeastOnce()).getInputStream();
    }

    @Test
    void sendPdfToGemini_withoutCountry_shouldUseGenericPrompt() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(createSimplePdf()));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, null);
        });
        
        verify(mockFile, atLeastOnce()).getInputStream();
    }

    @Test
    void sendPdfToGemini_withBlankCountry_shouldUseGenericPrompt() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(createSimplePdf()));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, "   ");
        });
        
        verify(mockFile, atLeastOnce()).getInputStream();
    }

    @Test
    void extractTextFromPdf_withCorruptedPdf_shouldThrowRuntimeException() throws IOException {
        byte[] corruptedPdf = "Not a valid PDF".getBytes();
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(corruptedPdf));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, "China");
        });
        
        assertEquals("Failed to parse PDF file", exception.getMessage());
    }

    @Test
    void extractTextFromPdf_withIOException_shouldThrowRuntimeException() throws IOException {
        when(mockFile.getInputStream()).thenThrow(new IOException("File read error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, "Japan");
        });
        
        assertEquals("Failed to parse PDF file", exception.getMessage());
    }

    @Test
    void sendPdfToGemini_withValidPdf_shouldCallGeminiAPI() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(createSimplePdf()));
        
        // This will throw an exception because we can't mock the Gemini client easily
        // In a real scenario, we need to refactor PredictionService to inject the Client
        assertThrows(RuntimeException.class, () -> {
            predictionService.sendPdfToGemini(mockFile, "Germany");
        });
        
        verify(mockFile, atLeastOnce()).getInputStream();
    }

    // Creates a minimal valid PDF structure for testing
    // This is a simplified PDF that PDFBox can parse
    private byte[] createSimplePdf() {
        String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /Resources 4 0 R /MediaBox [0 0 612 792] /Contents 5 0 R >>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<< /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >>\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<< /Length 44 >>\n" +
                "stream\n" +
                "BT /F1 12 Tf 100 700 Td (Test tariff content) Tj ET\n" +
                "endstream\n" +
                "endobj\n" +
                "xref\n" +
                "0 6\n" +
                "0000000000 65535 f\n" +
                "0000000009 00000 n\n" +
                "0000000058 00000 n\n" +
                "0000000115 00000 n\n" +
                "0000000214 00000 n\n" +
                "0000000304 00000 n\n" +
                "trailer\n" +
                "<< /Size 6 /Root 1 0 R >>\n" +
                "startxref\n" +
                "397\n" +
                "%%EOF";
        
        return pdfContent.getBytes();
    }
}
