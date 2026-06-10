package com.neusoft.edu.neullmdev.service.document;

import com.neusoft.edu.neullmdev.dto.document.WordWriteParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class WordWriteService {

    private final String desktopDir;
    private final String documentsDir;

    public WordWriteService() {
        String userHome = System.getProperty("user.home");
        this.desktopDir = userHome + File.separator + "Desktop";
        this.documentsDir = userHome + File.separator + "Documents";
    }

    public ToolResult write(WordWriteParams params) {
        if (params.getFileName() == null || params.getFileName().trim().isEmpty()) {
            params.setFileName(params.getTitle());
        }
        String msg = writeDocument(params);
        return new ToolResult("word_write", msg, msg);
    }

    public String writeDocument(WordWriteParams params) {
        String baseDir = new File(desktopDir).exists() ? desktopDir : documentsDir;
        String fileName = params == null ? null : params.getFileName();
        String safeFileName = (fileName == null || fileName.trim().isEmpty()) ? "未命名文档" : fileName.trim();
        String filePath = baseDir + File.separator + safeFileName + ".docx";

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {
            addTitle(doc, params == null ? null : params.getTitle());
            addContent(doc, params == null ? null : params.getContent());
            doc.write(out);
            return "文档创建成功: " + filePath;
        } catch (Exception e) {
            return "文档创建失败: " + filePath + " — " + e.getMessage();
        }
    }

    private static void addTitle(XWPFDocument doc, String title) {
        if (title == null || title.isEmpty()) {
            return;
        }
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setColor("003366");
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.setFontFamily("宋体");
        titleRun.addBreak(BreakType.TEXT_WRAPPING);
    }

    private static void addContent(XWPFDocument doc, String content) {
        if (content == null || content.isEmpty()) {
            return;
        }
        List<String> paragraphs = Arrays.asList(content.split("\n"));
        for (String paragraphText : paragraphs) {
            if (paragraphText.trim().isEmpty()) {
                addEmptyParagraph(doc);
                continue;
            }
            XWPFParagraph para = doc.createParagraph();
            para.setIndentationFirstLine(600);
            para.setAlignment(ParagraphAlignment.BOTH);
            XWPFRun run = para.createRun();
            run.setText(paragraphText);
            run.setFontSize(12);
            run.setFontFamily("微软雅黑");
            run.addBreak(BreakType.TEXT_WRAPPING);
        }
    }

    private static void addEmptyParagraph(XWPFDocument doc) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.addBreak(BreakType.TEXT_WRAPPING);
    }
}
