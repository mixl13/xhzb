package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 精神状态DTO
 */
@Data
@Schema(description = "精神状态DTO")
public class MentalStateDto {

    @Schema(title = "回忆词语")
    private String recallWords;

    @Schema(title = "时钟绘制测试")
    private ClockDrawingTestDto clockDrawingTest;

    @Schema(title = "攻击性行为")
    private String aggressiveBehavior;

    @Schema(title = "抑郁症状")
    private String depressiveSymptoms;

    @Schema(title = "选项汇总分数")
    private Integer totalScore;

    @Schema(title = "技能等级")
    private String abilityRating;
}
