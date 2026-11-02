package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 申请入住DTO
 *
 * @author mixl
 */
@Data
@Schema(description = "申请入住DTO")
public class CheckInApplyDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 健康评估ID */
    @NotNull(message = "评估ID不能为空")
    @Schema(title = "评估ID")
    private Long healthAssessmentId;

    /** 入住老人信息 */
    @Valid
    @NotNull(message = "老人信息不能为空")
    @Schema(title = "入住老人信息")
    private CheckInElderDto checkInElderDto;

    /** 老人家属信息列表（转 JSON 保存到 check_in.remark） */
    @Valid
    @NotEmpty(message = "家属信息不能为空")
    @Schema(title = "老人家属信息列表")
    private List<ElderFamilyDto> elderFamilyDtoList;

    /** 入住配置 */
    @Valid
    @NotNull(message = "入住配置不能为空")
    @Schema(title = "入住配置")
    private CheckInConfigDto checkInConfigDto;

    /** 签约合同 */
    @Valid
    @NotNull(message = "签约合同不能为空")
    @Schema(title = "签约合同")
    private CheckInContractDto checkInContractDto;

}
