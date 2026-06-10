package com.neusoft.edu.neullmdev.service.classroom;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentTextExtractor {

    public String extract(String fileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".docx")) {
            return extractDocx(bytes);
        }
        if (lower.endsWith(".pdf")) {
            return extractPdf(bytes);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extractDocx(byte[] bytes) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    return xml.replaceAll("</w:p>", "\n")
                            .replaceAll("<[^>]+>", "")
                            .replace("&lt;", "<").replace("&gt;", ">")
                            .replace("&amp;", "&").replaceAll("\\n{3,}", "\n\n").trim();
                }
            }
        } catch (IOException e) {
            return "Word 文件读取失败：" + e.getMessage();
        }
        return "";
    }

    private String extractPdf(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc).trim();
        } catch (IOException e) {
            return "PDF 文件读取失败：" + e.getMessage();
        }
    }
}
