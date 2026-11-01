package com.xhzb.nursing.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhzb.common.exception.ServiceException;
import com.xhzb.common.utils.StringUtils;
import com.xhzb.common.utils.bean.BeanUtils;
import com.xhzb.nursing.domain.dto.CheckInApplyDto;
import com.xhzb.nursing.domain.dto.CheckInConfigDto;
import com.xhzb.nursing.domain.dto.CheckInContractDto;
import com.xhzb.nursing.domain.dto.CheckInElderDto;
import com.xhzb.nursing.domain.entity.Bed;
import com.xhzb.nursing.domain.entity.CheckIn;
import com.xhzb.nursing.domain.entity.CheckInConfig;
import com.xhzb.nursing.domain.entity.Contract;
import com.xhzb.nursing.domain.entity.Elder;
import com.xhzb.nursing.domain.entity.HealthAssessment;
import com.xhzb.nursing.domain.entity.HealthAssessmentReport;
import com.xhzb.nursing.domain.vo.CheckInConfigVo;
import com.xhzb.nursing.domain.vo.CheckInDetailVo;
import com.xhzb.nursing.domain.vo.CheckInElderVo;
import com.xhzb.nursing.domain.vo.ElderFamilyVo;
import com.xhzb.nursing.mapper.CheckInMapper;
import com.xhzb.nursing.service.IBedService;
import com.xhzb.nursing.service.ICheckInConfigService;
import com.xhzb.nursing.service.ICheckInService;
import com.xhzb.nursing.service.IContractService;
import com.xhzb.nursing.service.IElderService;
import com.xhzb.nursing.service.IHealthAssessmentReportService;
import com.xhzb.nursing.service.IHealthAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 入住登记Service业务层处理
 * 
 * @author mixl
 * @date 2026-11-01
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService
{
    /** 合同编号时间部分格式：yyyyMMddHHmmss */
    private static final DateTimeFormatter CONTRACT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 评估进度：1-已完成 */
    private static final Integer EVALUATION_PROGRESS_FINISHED = 1;

    /** 入住状态：0-已入住（check_in.status） */
    private static final Integer CHECK_IN_STATUS_IN = 0;

    /** 是否已入住：1-是（health_assessment.check_in_status / health_assessment_report.check_in_status） */
    private static final Integer CHECKED_IN_FLAG_YES = 1;

    /** 床位状态：1-已入住 */
    private static final Integer BED_STATUS_OCCUPIED = 1;

    /** 老人状态：1-已入住 */
    private static final Integer ELDER_STATUS_CHECKED_IN = 1;

    /** 合同状态：0-未生效，1-已生效 */
    private static final Integer CONTRACT_STATUS_NOT_EFFECTIVE = 0;
    private static final Integer CONTRACT_STATUS_EFFECTIVE = 1;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private IHealthAssessmentService healthAssessmentService;

    @Autowired
    private IHealthAssessmentReportService healthAssessmentReportService;

    @Autowired
    private IElderService elderService;

    @Autowired
    private IBedService bedService;

    @Autowired
    private IContractService contractService;

    @Autowired
    private ICheckInConfigService checkInConfigService;

    /**
     * 查询入住登记
     * 
     * @param id 入住登记主键
     * @return 入住登记
     */
    @Override
    public CheckIn selectCheckInById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询入住登记列表
     * 
     * @param checkIn 入住登记
     * @return 入住登记
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住登记
     * 
     * @param checkIn 入住登记
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        return save(checkIn)? 1 : 0;
    }

    /**
     * 修改入住登记
     * 
     * @param checkIn 入住登记
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        return updateById(checkIn)? 1 : 0;
    }

    /**
     * 批量删除入住登记
     * 
     * @param ids 需要删除的入住登记主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    /**
     * 删除入住登记信息
     * 
     * @param id 入住登记主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id)
    {
        return removeById(id)? 1 : 0;
    }

    /**
     * 申请入住
     *
     * @param dto 申请入住入参
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyCheckIn(CheckInApplyDto dto)
    {
        CheckInElderDto elderDto = dto.getCheckInElderDto();
        CheckInConfigDto configDto = dto.getCheckInConfigDto();
        CheckInContractDto contractDto = dto.getCheckInContractDto();

        // 1、判断评估是否完成，未完成不允许办理入住
        HealthAssessment assessment = healthAssessmentService.getById(dto.getHealthAssessmentId());
        if (assessment == null)
        {
            throw new ServiceException("评估记录不存在");
        }
        if (!EVALUATION_PROGRESS_FINISHED.equals(assessment.getEvaluationProgress()))
        {
            throw new ServiceException("未完成评估不能入住");
        }

        // 2、校验老人是否已入住：同一身份证在 check_in 中存在「已入住」记录则不允许重复入住
        long checkedInCount = this.lambdaQuery()
                .eq(CheckIn::getIdCardNo, elderDto.getIdCardNo())
                .eq(CheckIn::getStatus, CHECK_IN_STATUS_IN)
                .count();
        if (checkedInCount > 0)
        {
            throw new ServiceException("老人已入住");
        }

        // 3、判断床位是否已被占用：bed 表中该床位已是「已入住」状态则不能分配给当前老人
        Bed bed = bedService.getById(configDto.getBedId());
        if (bed == null)
        {
            throw new ServiceException("床位不存在");
        }
        if (BED_STATUS_OCCUPIED.equals(bed.getBedStatus()))
        {
            throw new ServiceException("有其他老人已入住");
        }

        // 4、更新床位状态为已入住
        bed.setBedStatus(BED_STATUS_OCCUPIED);
        bedService.updateById(bed);

        // 5、新增或更新老人：老人此前入住过时表中已存在记录，此时更新其信息
        Elder elder = elderService.lambdaQuery().eq(Elder::getIdCardNo, elderDto.getIdCardNo()).last("limit 1").one();
        boolean elderExists = elder != null;
        if (!elderExists)
        {
            elder = new Elder();
        }
        elder.setName(elderDto.getName());
        elder.setIdCardNo(elderDto.getIdCardNo());
        elder.setPhone(elderDto.getPhone());
        elder.setSex(elderDto.getSex());
        elder.setBirthday(elderDto.getBirthday());
        elder.setAddress(elderDto.getAddress());
        elder.setImage(elderDto.getImage());
        elder.setIdCardNationalEmblemImg(elderDto.getIdCardNationalEmblemImg());
        elder.setIdCardPortraitImg(elderDto.getIdCardPortraitImg());
        elder.setNation(elderDto.getNation());
        elder.setEducationLevel(elderDto.getEducationLevel());
        elder.setSocialSecurityCard(elderDto.getSocialSecurityCard());
        elder.setLivingSituation(elderDto.getLivingSituation());
        elder.setReligiousBelief(elderDto.getReligiousBelief());
        elder.setEconomicSource(elderDto.getEconomicSource());
        elder.setMaritalStatus(elderDto.getMaritalStatus());
        elder.setMedicalPaymentMethod(elderDto.getMedicalPaymentMethod());
        elder.setCoreSuggestion(elderDto.getCoreSuggestion());
        elder.setBedId(configDto.getBedId());
        elder.setBedNumber(bed.getBedNumber());
        elder.setStatus(ELDER_STATUS_CHECKED_IN);
        if (elderExists)
        {
            elderService.updateById(elder);
        }
        else
        {
            elderService.save(elder);
        }

        // 6、新增签约合同：签约日期不晚于当前时间即为已生效，否则未生效
        Contract contract = new Contract();
        // contract.elder_id 在表结构中为 int，此处按实体类型做收窄
        contract.setElderId(elder.getId().intValue());
        contract.setElderName(elder.getName());
        contract.setContractName(contractDto.getContractName());
        contract.setContractNumber(generateContractNumber());
        contract.setAgreementPath(contractDto.getAgreementPath());
        contract.setThirdPartyName(contractDto.getThirdPartyName());
        contract.setThirdPartyPhone(contractDto.getThirdPartyPhone());
        contract.setStartDate(configDto.getStartDate());
        contract.setEndDate(configDto.getEndDate());
        contract.setSignDate(contractDto.getSignDate());
        contract.setSortOrder(0);
        contract.setStatus(contractDto.getSignDate() != null && !contractDto.getSignDate().isAfter(LocalDateTime.now())
                ? CONTRACT_STATUS_EFFECTIVE : CONTRACT_STATUS_NOT_EFFECTIVE);
        contractService.save(contract);

        // 7、新增入住记录：家属列表转 JSON 保存到 remark 字段
        CheckIn checkIn = new CheckIn();
        checkIn.setElderId(elder.getId());
        checkIn.setElderName(elder.getName());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setStartDate(configDto.getStartDate());
        checkIn.setEndDate(configDto.getEndDate());
        checkIn.setNursingLevelName(configDto.getNursingLevelName());
        checkIn.setBedNumber(bed.getBedNumber());
        checkIn.setStatus(CHECK_IN_STATUS_IN);
        checkIn.setSortOrder(0);
        checkIn.setRemark(JSONUtil.toJsonStr(dto.getElderFamilyDtoList()));
        this.save(checkIn);

        // 8、新增入住配置
        CheckInConfig checkInConfig = new CheckInConfig();
        checkInConfig.setCheckInId(checkIn.getId());
        checkInConfig.setNursingLevelId(configDto.getNursingLevelId());
        checkInConfig.setNursingLevelName(configDto.getNursingLevelName());
        checkInConfig.setFeeStartDate(configDto.getFeeStartDate());
        checkInConfig.setFeeEndDate(configDto.getFeeEndDate());
        checkInConfig.setDeposit(configDto.getDeposit());
        checkInConfig.setNursingFee(configDto.getNursingFee());
        checkInConfig.setBedFee(configDto.getBedFee());
        checkInConfig.setInsurancePayment(configDto.getInsurancePayment());
        checkInConfig.setGovernmentSubsidy(configDto.getGovernmentSubsidy());
        checkInConfig.setOtherFees(configDto.getOtherFees());
        checkInConfig.setSortOrder(0);
        checkInConfigService.save(checkInConfig);

        // 9、修改入住评估表，并做老人关联
        HealthAssessment assessmentUpdate = new HealthAssessment();
        assessmentUpdate.setId(assessment.getId());
        assessmentUpdate.setElderId(elder.getId());
        assessmentUpdate.setCheckInStatus(CHECKED_IN_FLAG_YES);
        healthAssessmentService.updateById(assessmentUpdate);

        // 10、修改入住评估结果表，状态改为已入住
        healthAssessmentReportService.lambdaUpdate()
                .eq(HealthAssessmentReport::getHealthAssessmentId, String.valueOf(assessment.getId()))
                .set(HealthAssessmentReport::getCheckInStatus, CHECKED_IN_FLAG_YES)
                .update();
    }

    /**
     * 查询入住详情
     *
     * @param id 入住id
     * @return 入住详情
     */
    @Override
    public CheckInDetailVo getCheckInDetail(Long id)
    {
        CheckIn checkIn = this.getById(id);
        if (checkIn == null)
        {
            throw new ServiceException("入住记录不存在");
        }

        CheckInDetailVo detailVo = new CheckInDetailVo();

        // 老人信息
        Elder elder = elderService.getById(checkIn.getElderId());
        if (elder != null)
        {
            CheckInElderVo elderVo = new CheckInElderVo();
            BeanUtils.copyProperties(elder, elderVo);
            // elder 表无年龄字段，按出生日期计算后返回
            elderVo.setAge(calculateAge(elder.getBirthday()));
            detailVo.setCheckInElderVo(elderVo);
        }

        // 家属信息：入住时以 JSON 形式保存在 check_in.remark 中
        detailVo.setElderFamilyVoList(parseFamilyList(checkIn.getRemark()));

        // 入住配置：补充入住床位与入住起止时间
        CheckInConfig checkInConfig = checkInConfigService.lambdaQuery()
                .eq(CheckInConfig::getCheckInId, id)
                .last("limit 1")
                .one();
        if (checkInConfig != null)
        {
            CheckInConfigVo configVo = new CheckInConfigVo();
            BeanUtils.copyProperties(checkInConfig, configVo);
            configVo.setBedNumber(checkIn.getBedNumber());
            configVo.setStartDate(checkIn.getStartDate());
            configVo.setEndDate(checkIn.getEndDate());
            detailVo.setCheckInConfigVo(configVo);
        }

        // 签约合同：取该老人最近一次签订的合同
        detailVo.setContract(contractService.lambdaQuery()
                .eq(Contract::getElderId, checkIn.getElderId())
                .orderByDesc(Contract::getId)
                .last("limit 1")
                .one());

        return detailVo;
    }

    /**
     * 生成合同编号：HT + yyyyMMddHHmmss + 4 位递增值
     *
     * @return 合同编号
     */
    private String generateContractNumber()
    {
        // 递增序号取当前合同总数 + 1，不足 4 位左侧补 0
        long sequence = contractService.count() + 1;
        return "HT" + LocalDateTime.now().format(CONTRACT_TIME_FORMATTER) + String.format("%04d", sequence);
    }

    /**
     * 解析入住时保存到 remark 的家属信息 JSON
     *
     * @param remark 备注（家属信息 JSON）
     * @return 家属信息集合
     */
    private List<ElderFamilyVo> parseFamilyList(String remark)
    {
        if (StringUtils.isEmpty(remark))
        {
            return new ArrayList<>();
        }
        return JSONUtil.parseArray(remark).toList(ElderFamilyVo.class);
    }

    /**
     * 根据出生日期计算年龄
     *
     * @param birthday 出生日期
     * @return 年龄
     */
    private Integer calculateAge(String birthday)
    {
        if (StringUtils.isEmpty(birthday))
        {
            return null;
        }
        try
        {
            LocalDate birthDate = LocalDate.parse(birthday.trim().substring(0, 10));
            return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
