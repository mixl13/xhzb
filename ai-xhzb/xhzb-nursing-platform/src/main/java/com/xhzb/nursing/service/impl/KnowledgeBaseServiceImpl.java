package com.xhzb.nursing.service.impl;

import java.io.InputStream;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.oss.client.OSSAliyunFileStorageService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.KnowledgeBaseMapper;
import com.xhzb.nursing.domain.entity.KnowledgeBase;
import com.xhzb.nursing.service.IKnowledgeBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 知识库主Service业务层处理
 * 
 * @author mixl
 * @date 2026-10-30
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements IKnowledgeBaseService
{
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private OSSAliyunFileStorageService ossAliyunFileStorageService;
    @Autowired
    private VectorStore vectorStore;

    /**
     * 查询知识库主
     * 
     * @param id 知识库主主键
     * @return 知识库主
     */
    @Override
    public KnowledgeBase selectKnowledgeBaseById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询知识库主列表
     * 
     * @param knowledgeBase 知识库主
     * @return 知识库主
     */
    @Override
    public List<KnowledgeBase> selectKnowledgeBaseList(KnowledgeBase knowledgeBase)
    {
        return knowledgeBaseMapper.selectKnowledgeBaseList(knowledgeBase);
    }

    /**
     * 新增知识库主
     * 
     * @param knowledgeBase 知识库主
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertKnowledgeBase(KnowledgeBase knowledgeBase)
    {
        InputStream download = ossAliyunFileStorageService.download(knowledgeBase.getDocumentUrl());
        InputStreamResource resource = new InputStreamResource(download);
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfBottomTextLinesToDelete(0).build())
                .withPagesPerDocument(1).build());
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withMinChunkSizeChars(200)
                .withChunkSize(500)
                .withKeepSeparator(false)
                .withMaxNumChunks(10000).build();
        List<Document> apply = textSplitter.apply(pagePdfDocumentReader.read());
        vectorStore.add(apply);

        List<String> list = apply.stream().map(Document::getId).toList();
        String IdList =  JSONUtil.toJsonStr(list);
        knowledgeBase.setRemark(IdList);
        knowledgeBase.setCreateTime(DateUtils.getNowDate());
        return this.save(knowledgeBase)? 1 : 0;
    }
    /**
     * 修改知识库主
     * 
     * @param knowledgeBase 知识库主
     * @return 结果
     */
    @Override
    public int updateKnowledgeBase(KnowledgeBase knowledgeBase)
    {
        return updateById(knowledgeBase)? 1 : 0;
    }

    /**
     * 批量删除知识库主
     * 
     * @param ids 需要删除的知识库主主键
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteKnowledgeBaseByIds(Long[] ids)
    {
        List<KnowledgeBase> list = this.listByIds(Arrays.asList(ids));
        if(CollUtil.isEmpty(list)){
            return 0;
        }
        list.forEach(knowledgeBase -> {
            //删除oss
            String documentUrl = knowledgeBase.getDocumentUrl();
            ossAliyunFileStorageService.delete(documentUrl);

            //删除redis
            String remark = knowledgeBase.getRemark();
            List<String> idList = JSONUtil.toList(remark, String.class);
            vectorStore.delete(idList);
        });
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    /**
     * 删除知识库主信息
     * 
     * @param id 知识库主主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeBaseById(Long id)
    {
        return removeById(id)? 1 : 0;
    }

}
