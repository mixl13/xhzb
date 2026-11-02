package com.xhzb.nursing.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入住签约合同DTO
 *
 * @author mixl
 */
@Data
@Schema(description = "入住签约合同DTO")
public class CheckInContractDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 合同名称 */
    @NotBlank(message = "合同名称不能为空")
    @Schema(title = "合同名称")
    private String contractName;

    /** 协议地址（文件路径或URL） */
    @NotBlank(message = "协议地址不能为空")
    @Schema(title = "协议地址")
    private String agreementPath;

    /** 签约日期（用于判断合同是否生效） */
    @NotNull(message = "签约日期不能为空")
    @Schema(title = "签约日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signDate;

    /** 丙方（第三方）姓名 */
    @Schema(title = "丙方姓名")
    private String thirdPartyName;

    /** 丙方（第三方）手机号 */
    @Schema(title = "丙方手机号")
    private String thirdPartyPhone;

}
