package com.xhzb;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@SpringBootTest
public class ETLTest {
    @Test
    public void readTest() throws FileNotFoundException {
        InputStreamResource isr = new InputStreamResource(new FileInputStream("d:\\向量测试数据.txt"));
        TextReader textReader = new TextReader(isr);
        textReader.getCustomMetadata().put("name","张三");
        textReader.getCustomMetadata().put("filename","向量测试数据.txt");
        textReader.getCustomMetadata().put("type","txt");
        List<Document> documents = textReader.read();
        documents.forEach(System.out::println);
    }

    @Test
    public void testPDFByParagraph() throws FileNotFoundException {
        InputStreamResource inputStreamReader = new InputStreamResource(new FileInputStream("D:\\个人信息\\个人信息\\简历\\个人简历.pdf"));
        ParagraphPdfDocumentReader paragraphPdfDocumentReader = new ParagraphPdfDocumentReader(inputStreamReader, PdfDocumentReaderConfig
                .builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter
                        .builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1).build());

        System.out.println(paragraphPdfDocumentReader.get());
    }

    @Autowired
    private VectorStore vectorStore;
    @Test
    public void testByPdf() throws FileNotFoundException {
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream("D:\\个人信息\\个人信息\\简历\\个人简历.pdf"));
        PagePdfDocumentReader reader = new PagePdfDocumentReader(inputStreamResource, PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfBottomTextLinesToDelete(0).build())
                .withPagesPerDocument(1)
                .build());
        //System.out.println(reader.get());

        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(1000)
                .withMaxNumChunks(100000)
                .withMinChunkSizeChars(20)
                .withMinChunkLengthToEmbed(10)
                .withKeepSeparator(true).build();
        List<Document> document = splitter.apply(reader.read());
        vectorStore.add(document);
    }

    @Test
    public void retrieval(){
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .topK(10)
                .query("有哪些护理项目？")
                .similarityThreshold(0.0).build());
        documents.forEach(System.out::println);
    }

}
