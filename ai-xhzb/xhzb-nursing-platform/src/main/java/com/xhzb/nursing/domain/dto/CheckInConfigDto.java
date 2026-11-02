package com.xhzb.nursing.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住配置DTO
 * <p>
 * 说明：bedId / code / floorId / floorName / roomId / startDate / endDate 用于业务校验与入住表落库，
 * check_in_config 表本身只保存费用与护理等级相关字段。
 * </p>
 *
 * @author mixl
 */
@Data
@Schema(description = "入住配置DTO")
public class CheckInConfigDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 床位ID */
    @NotNull(message = "床位ID不能为空")
    @Schema(title = "床位ID")
    private Long bedId;

    /** 房间ID */
    @Schema(title = "房间ID")
    private Long roomId;

    /** 房间编号 */
    @Schema(title = "房间编号")
    private String code;

    /** 楼层ID */
    @Schema(title = "楼层ID")
    private Long floorId;

    /** 楼层名称 */
    @Schema(title = "楼层名称")
    private String floorName;

    /** 护理等级ID */
    @NotNull(message = "护理等级ID不能为空")
    @Schema(title = "护理等级ID")
    private Long nursingLevelId;

    /** 护理等级名称 */
    @Schema(title = "护理等级名称")
    private String nursingLevelName;

    /** 入住开始时间 */
    @Schema(title = "入住开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    /** 入住结束时间 */
    @Schema(title = "入住结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    /** 费用开始时间 */
    @Schema(title = "费用开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feeStartDate;

    /** 费用结束时间 */
    @Schema(title = "费用结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feeEndDate;

    /** 押金（元） */
    @Schema(title = "押金（元）")
    private BigDecimal deposit;

    /** 护理费用（元/月） */
    @Schema(title = "护理费用（元/月）")
    private BigDecimal nursingFee;

    /** 床位费用（元/月） */
    @Schema(title = "床位费用（元/月）")
    private BigDecimal bedFee;

    /** 医保支付（元/月） */
    @Schema(title = "医保支付（元/月）")
    private BigDecimal insurancePayment;

    /** 政府补贴（元/月） */
    @Schema(title = "政府补贴（元/月）")
    private BigDecimal governmentSubsidy;

    /** 其他费用（元/月） */
    @Schema(title = "其他费用（元/月）")
    private BigDecimal otherFees;

}
