package com.xhzb;

import org.junit.jupiter.api.Test;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
public class TestPdfByPage {

    @Test
    public void testByPdf() throws FileNotFoundException {
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream("D:\\个人信息\\个人信息\\简历\\个人简历.pdf"));
        PagePdfDocumentReader reader = new PagePdfDocumentReader(inputStreamResource, PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfBottomTextLinesToDelete(0).build())
                .withPagesPerDocument(1)
                .build());
        System.out.println(reader.get());
    }
}
