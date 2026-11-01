package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 时钟绘制测试DTO
 */
@Data
@Schema(description = "时钟绘制测试DTO")
public class ClockDrawingTestDto {

    @Schema(title = "图片地址")
    private String url;

    @Schema(title = "画钟测验结果(0-正确，1-错误，2-确诊认知障碍)")
    private Integer result;
}
