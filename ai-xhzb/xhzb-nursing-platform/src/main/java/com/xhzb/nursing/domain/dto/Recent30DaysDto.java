package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 最近30天意外事件和体检报告DTO
 */
@Data
@Schema(description = "最近30天意外事件和体检报告DTO")
public class Recent30DaysDto {

    @Schema(title = "跌倒")
    private Integer fall;

    @Schema(title = "走失")
    private Integer lost;

    @Schema(title = "噎食")
    private Integer choking;

    @Schema(title = "自杀尝试")
    private Integer suicideAttempt;

    @Schema(title = "昏迷")
    private Integer coma;

    @Schema(title = "体检报告URL")
    private String medicalReport;
}
