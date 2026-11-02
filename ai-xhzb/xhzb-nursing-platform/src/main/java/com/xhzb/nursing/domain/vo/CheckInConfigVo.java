package com.xhzb.nursing.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhzb.nursing.domain.entity.CheckInConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入住详情-入住配置VO
 * <p>
 * 在 check_in_config 基础上补充入住表的入住床位、入住起止时间。
 * </p>
 *
 * @author mixl
 */
@Data
@Schema(description = "入住详情-入住配置VO")
public class CheckInConfigVo extends CheckInConfig
{
    private static final long serialVersionUID = 1L;

    /** 入住床位编号（来源于 check_in.bed_number） */
    @Schema(title = "入住床位编号")
    private String bedNumber;

    /** 入住开始时间（来源于 check_in.start_date） */
    @Schema(title = "入住开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    /** 入住结束时间（来源于 check_in.end_date） */
    @Schema(title = "入住结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

}
