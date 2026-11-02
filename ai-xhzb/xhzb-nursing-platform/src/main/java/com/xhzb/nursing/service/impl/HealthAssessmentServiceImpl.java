package com.xhzb.nursing.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.common.utils.PDFUtil;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.domain.dto.BasicInfoDto;
import com.xhzb.nursing.domain.dto.ElderlyAssessmentDto;
import com.xhzb.nursing.domain.entity.HealthAssessmentDataCollection;
import com.xhzb.nursing.domain.entity.HealthAssessmentReport;
import com.xhzb.nursing.domain.vo.ElderCheckInInfoVO;
import com.xhzb.nursing.domain.vo.ElderLevelResultVO;
import com.xhzb.nursing.domain.vo.HealthReportResultVO;
import com.xhzb.nursing.service.IHealthAssessmentDataCollectionService;
import com.xhzb.nursing.service.IHealthAssessmentReportService;
import com.xhzb.oss.client.OSSAliyunFileStorageService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.HealthAssessmentMapper;
import com.xhzb.nursing.domain.entity.HealthAssessment;
import com.xhzb.nursing.service.IHealthAssessmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 健康评估记录Service业务层处理
 * 
 * @author mixl
 * @date 2026-10-30
 */
@Service
public class HealthAssessmentServiceImpl extends ServiceImpl<HealthAssessmentMapper, HealthAssessment> implements IHealthAssessmentService
{
    @Autowired
    private HealthAssessmentMapper healthAssessmentMapper;
    @Autowired
    private IHealthAssessmentDataCollectionService healthAssessmentDataCollectionService;

    @Autowired
    private ChatClient chatClientDeepseekHealth;

    @Autowired
    private IHealthAssessmentReportService reportService;

    @Autowired
    private OSSAliyunFileStorageService ossAliyunFileStorageService;

    @Autowired
    private IHealthAssessmentReportService  healthAssessmentReportService;


    /**
     * 查询健康评估记录
     * 
     * @param id 健康评估记录主键
     * @return 健康评估记录
     */
    @Override
    public HealthAssessmentDataCollection selectHealthAssessmentById(Long id)
    {
        return healthAssessmentDataCollectionService.getById(id);
    }

    /**
     * 查询健康评估记录列表
     * 
     * @param healthAssessment 健康评估记录
     * @return 健康评估记录
     */
    @Override
    public List<HealthAssessment> selectHealthAssessmentList(HealthAssessment healthAssessment)
    {
        return healthAssessmentMapper.selectHealthAssessmentList(healthAssessment);
    }

    /**
     * 新增健康评估记录
     * 
     * @param dto 健康评估记录
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long insertOrUpdateHealthAssessment(ElderlyAssessmentDto dto)
    {
        HealthAssessment healthAssessment = new HealthAssessment();
        healthAssessment.setElderName(dto.getBasicInfo().getElderName());
        healthAssessment.setIdCard(dto.getBasicInfo().getIdCard());
        //补充新增对象前的额外属性处理
        if (dto.getId() == null){
            healthAssessment.setCheckInStatus(0);
            healthAssessment.setEvaluationProgress(0);
            healthAssessment.setCreateTime(DateUtils.getNowDate());
            this.save(healthAssessment);
        }else{
            //如果存在id记录，直接查询健康评估记录进行修改即可
            HealthAssessment assessment = this.getById(dto.getId());
            if (assessment == null){
                throw new BaseException("健康评估记录不存在，无法更新");
            }
            if(assessment.getEvaluationProgress() != 0){
                throw new BaseException("健康评估记录已取消或已完成，无法更新");
            }
            healthAssessment.setId(dto.getId());
            this.updateById(healthAssessment);
        }

        HealthAssessmentDataCollection  healthAssessmentDataCollection = new HealthAssessmentDataCollection();
        healthAssessmentDataCollection.setBasicInfo(JSONUtil.toJsonStr(dto.getBasicInfo()));
        healthAssessmentDataCollection.setHealthAssessment(JSONUtil.toJsonStr(dto.getHealthAssessmentDto()));
        healthAssessmentDataCollection.setDailyLivingActivities(JSONUtil.toJsonStr(dto.getDailyLivingActivities()));
        healthAssessmentDataCollection.setMentalState(JSONUtil.toJsonStr(dto.getMentalState()));
        healthAssessmentDataCollection.setPerceptionCommunication(JSONUtil.toJsonStr(dto.getPerceptionAndCommunication()));
        healthAssessmentDataCollection.setSocialParticipation(JSONUtil.toJsonStr(dto.getSocialParticipation()));
        healthAssessmentDataCollection.setId(healthAssessment.getId());
        if (dto.getId() == null){
            healthAssessmentDataCollection.setCreateTime(DateUtils.getNowDate());
        }

        healthAssessmentDataCollectionService.saveOrUpdate(healthAssessmentDataCollection);

        return healthAssessment.getId();
    }

    /**
     * 修改健康评估记录
     * 
     * @param healthAssessment 健康评估记录
     * @return 结果
     */
    @Override
    public int updateHealthAssessment(HealthAssessment healthAssessment)
    {
        return updateById(healthAssessment)? 1 : 0;
    }

    /**
     * 批量删除健康评估记录
     * 
     * @param ids 需要删除的健康评估记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAssessmentByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    /**
     * 删除健康评估记录信息
     * 
     * @param id 健康评估记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAssessmentById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
    
    /**
     * AI评估
     *
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long assessmentData(ElderlyAssessmentDto dto) {
        //1.更新保存评估数据：系统首先更新并保存本次评估的基础数据。
        this.insertOrUpdateHealthAssessment(dto);

        //2.提取各项能力：从评估数据中，提取四大维度的能力信息：
        //2.1.日常生活活动等级
        String dailyActivityLevel = this.getCurrentLevel(dto.getDailyLivingActivities().getAbilityRating());

        //2.2.精神状态等级
        String mentalStatusLevel = this.getCurrentLevel(dto.getMentalState().getAbilityRating());

        //2.3.感知觉与沟通等级
        String perceptionCommunicationLevel = this.getCurrentLevel(dto.getPerceptionAndCommunication().getAbilityRating());

        //2.4.社会参与等级
        String socialParticipationLevel = this.getCurrentLevel(dto.getSocialParticipation().getAbilityRating());

        //2.5 补充老人的其他基本能力信息
        // 跌倒次数
        Integer fall = dto.getHealthAssessmentDto().getRecent30Days().getFall();

        // 噎食次数
        Integer choking = dto.getHealthAssessmentDto().getRecent30Days().getChoking();

        // 自杀次数
        Integer suicideAttempt = dto.getHealthAssessmentDto().getRecent30Days().getSuicideAttempt();

        // 走失次数
        Integer lost = dto.getHealthAssessmentDto().getRecent30Days().getLost();

        // 昏迷次数
        Integer coma = dto.getHealthAssessmentDto().getRecent30Days().getComa();

        // 痴呆疾病  0-表示无 1-表示有
        Integer dementia  = "无".equals(dto.getHealthAssessmentDto().getDiseaseDiagnosis().getDementia()) ? 0 : 1;

        // 精神疾病 0-表示无 1-表示有
        Integer mentalillness  = "无".equals(dto.getHealthAssessmentDto().getDiseaseDiagnosis().getMentalIllness()) ? 0 : 1;

        // 是否确诊为认知障碍
        Integer known = dto.getMentalState().getClockDrawingTest().getResult()==2 ? 1 : 0;

        //3.第一阶段 AI 分析（多维度能力评估）：
        //构建老人能力评估Prompt：将四大维度的评估信息和评估规则，组装成 AI 可识别的 Prompt。
        String userPrompt1 = String.format(SystemConstants.USER_PROMPT1,
                dailyActivityLevel,mentalStatusLevel,perceptionCommunicationLevel,socialParticipationLevel,
                fall,choking,suicideAttempt,lost,
                coma,dementia,mentalillness,known
        );

        //ChatClient 分析：调用 AI 客户端（ChatClient）进行分析。
        String content = chatClientDeepseekHealth.prompt()
                .user(userPrompt1)
                .call()
                .content();

        //解析 JSON 结果：解析 AI 返回的 JSON 格式分析结果。
        String json = extractJson(content);
        ElderLevelResultVO elderLevelResultVO = JSONUtil.toBean(json, ElderLevelResultVO.class);

        //构建AI健康评估报告对象
        HealthAssessmentReport report = new HealthAssessmentReport();
        report.setHealthAssessmentId(dto.getId().toString());//健康评估记录的主键ID
        report.setAssessorName(SecurityUtils.getUsername());//评估员名称
        report.setDailyActivityLevel(dailyActivityLevel);
        report.setMentalStatusLevel(mentalStatusLevel);
        report.setPerceptionCommunicationLevel(perceptionCommunicationLevel);
        report.setSocialParticipationLevel(socialParticipationLevel);
        report.setInitialAbilityLevel(elderLevelResultVO.getPreLevel());
        report.setFinalAbilityLevel(elderLevelResultVO.getFinalLevel());
        report.setLevelChangeReason(elderLevelResultVO.getReason());
        report.setCreateTime(DateUtils.getNowDate());
        report.setCreateBy(SecurityUtils.getUsername());
        report.setPsychologicalCare(elderLevelResultVO.getPsychologicalCare());
        reportService.save(report);

        //        4.第二阶段 AI 分析（体检报告评估）：
//        更新健康评估报告：根据上一阶段的分析结果，更新健康评估报告的基础信息。
//        OSS 中下载体检报告：从对象存储（OSS）中下载用户的体检报告 PDF 文件。
//        提取 PDF 内容：解析并提取 PDF 格式体检报告中的文本内容。
//        组装老人健康评估Prompt：将提取的体检报告内容和评估规则，组装成新的 AI 分析 Prompt。
//        AI 分析体检报告：调用 AI 对体检报告内容进行专业分析。
//        解析评估结果：解析 AI 返回的体检报告评估结果。
//        5.结果存储与状态更新：
//        保存到评估结果表：将两阶段的 AI 分析结果，保存到数据库的评估结果表中。
//        更新评估信息状态和建议：更新评估信息的整体状态，并生成最终的评估建议。
//        6.返回评估 ID：流程结束，返回本次评估的唯一ID。

        InputStream inputStream = ossAliyunFileStorageService.download(dto.getHealthAssessmentDto().getRecent30Days().getMedicalReport());

        //4.2 提取PDF内容：解析并提取 PDF 格式体检报告中的文本内容。
        String pdfContent = PDFUtil.pdfToString(inputStream);

        //4.3 组装老人健康评估Prompt：将提取的体检报告内容和评估规则，组装成新的 AI 分析 Prompt。
        String userPrompt2 = String.format(SystemConstants.USER_PROMPT2,pdfContent,elderLevelResultVO.getPsychologicalCare());

        //4.2 AI分析体检报告：调用 AI 对体检报告内容进行专业分析。
        HealthReportResultVO healthReportResultVO = this.chatBigModel(userPrompt2, HealthReportResultVO.class);
        Double healthScore = healthReportResultVO.getHealthScore();//健康评分
        report.setHealthScore(healthScore.toString());//健康评分
        report.setRiskLevel(healthReportResultVO.getRiskLevel());//风险等级
        report.setReportSummary(healthReportResultVO.getSummarize());//报告总结
        report.setAbnormalAnalysis(JSONUtil.toJsonStr(healthReportResultVO.getAbnormalData()));//异常分析
        report.setSystemScore(JSONUtil.toJsonStr(healthReportResultVO.getSystemScore()));//系统评分
        Integer coreSuggestion  = healthScore>=60 ? 1 : 0; // 0-不建议入住 1-建议入住
        report.setCoreSuggestion(coreSuggestion);//是否建议入住
        report.setCheckInStatus(0);//未入住
        String careLevel = this.getCareLevel(healthScore);//建议护理等级
        report.setRecommendedCareLevel(careLevel);
        report.setAssessmentTime(LocalDateTime.now());
        report.setUpdateBy(SecurityUtils.getUsername());
        report.setSuggestionDescription(healthReportResultVO.getSuggestionDescription());
        report.setDietSuggestion(healthReportResultVO.getDietSuggestion());

        // 5.结果存储与状态更新：
        // 5.1 更新AI评估报告信息
        reportService.updateById(report);

        // 5.2 更新健康评估记录表信息
        HealthAssessment healthAssessment = new HealthAssessment();
        healthAssessment.setId(dto.getId());
        healthAssessment.setCoreSuggestion(coreSuggestion);//是否建议入住
        healthAssessment.setEvaluationProgress(1);//评估进度：0-评估中，1-已完成，2-已取消
        this.updateById(healthAssessment);

        // 6.返回评估 ID：流程结束，返回本次评估的唯一ID。
        return dto.getId();
    }


    /**
     * 根据健康评分获取建议护理等级
     * @param healthScore
     * @return
     */
    private String getCareLevel(Double healthScore) {
        String level = "不建议入住";
        if (healthScore>=90) {
            level = "三级护理";
        } else if (healthScore>=80) {
            level = "二级护理";
        } else if (healthScore>=70){
            level = "一级护理";
        } else if (healthScore>=60) {
            level = "特级护理";
        }
        return level;
    }


    /**
     * 与大模型交互对话
     * @param prompt
     * @param type
     * @return
     * @param <T>
     */
    private <T> T chatBigModel(String prompt,Class<T> type) {
        String content = chatClientDeepseekHealth.prompt()
                .user(prompt)
                .call()
                .content();

        //解析 JSON 结果：解析 AI 返回的 JSON 格式分析结果。
        String json = extractJson(content);
        T t = JSONUtil.toBean(json, type);
        return t;
    }




    /**
     * 从 LLM 响应中提取首个 JSON 对象。
     * <p>
     * 处理三种常见情况：纯 JSON、带 markdown ```json 围栏、JSON 前后夹杂说明文字。
     * </p>
     */
    private String extractJson(String content) {
        // 优先匹配 ```json ... ``` 代码块
        Pattern fence = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?\\})\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher fm = fence.matcher(content);
        if (fm.find()) {
            return fm.group(1);
        }
        // 否则取首个 {...} 顶层对象
        Pattern obj = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher om = obj.matcher(content);
        if (om.find()) {
            return om.group();
        }
        return content;
    }


    /**
     * 根据等级中文名称获取等级数值
     * @param ability
     * @return
     */
    private String getCurrentLevel(String ability){
        String level = "";
        switch (ability) {
            case "能力完好":
                level = "0";
                break;
            case "轻度受损":
                level = "1";
                break;
            case "中度受损":
                level = "2";
                break;
            case "重度受损":
                level = "3";
                break;
        }
        return level;
    }
    /**
     * 查看评估详情(评估结果)
     *
     * @param id
     * @return
     */
    @Override
    public HealthAssessmentReport getAssessmentDetail(Long id) {
        //select * from t where health_assessment_id=?

        //第一种写法
        //HealthAssessmentReport detail = reportService.getOne(Wrappers.<HealthAssessmentReport>lambdaQuery().eq(HealthAssessmentReport::getHealthAssessmentId, id));

        //第二种写法
        HealthAssessmentReport detail = reportService.lambdaQuery().eq(HealthAssessmentReport::getHealthAssessmentId,id).one();

        return detail;
    }

    @Override
    public void cancel(Long id) {
        HealthAssessment healthAssessment = this.getById(id);
        healthAssessment.setEvaluationProgress(2);
        this.updateById(healthAssessment);
    }

    @Override
    public ElderCheckInInfoVO getElderInfo(Long id) {
        HealthAssessmentReport healthAssessmentReport = healthAssessmentReportService.lambdaQuery().eq(HealthAssessmentReport::getHealthAssessmentId,id).one();
        HealthAssessmentDataCollection  healthAssessmentDataCollection = healthAssessmentDataCollectionService.getById(id);
        BasicInfoDto info = JSONUtil.toBean(healthAssessmentDataCollection.getBasicInfo(), BasicInfoDto.class);
        ElderCheckInInfoVO elderCheckInInfoVO = new ElderCheckInInfoVO();
        elderCheckInInfoVO.setName(info.getElderName());
        elderCheckInInfoVO.setPhone(info.getElderContact());
        elderCheckInInfoVO.setNation(info.getNation());
        elderCheckInInfoVO.setEducationLevel(info.getEducationLevel());
        elderCheckInInfoVO.setEconomicSource(info.getEconomicSource());
        elderCheckInInfoVO.setIdCardNo(info.getIdCard());
        elderCheckInInfoVO.setLivingSituation(info.getLivingSituation());
        elderCheckInInfoVO.setMaritalStatus(info.getMaritalStatus());
        elderCheckInInfoVO.setReligiousBelief(info.getReligiousBelief());
        elderCheckInInfoVO.setSocialSecurityCard(info.getSocialSecurityCard());
        elderCheckInInfoVO.setMedicalPaymentMethod(info.getMedicalPaymentMethod());
        elderCheckInInfoVO.setCoreSuggestion(healthAssessmentReport.getCoreSuggestion());
        return elderCheckInInfoVO;
    }
}
