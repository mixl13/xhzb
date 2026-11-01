package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日常活动DTO
 */
@Data
@Schema(description = "日常活动DTO")
public class DailyLivingActivitiesDto {

    @Schema(title = "进食")
    private String eating;

    @Schema(title = "洗澡")
    private String bathing;

    @Schema(title = "修饰")
    private String grooming;

    @Schema(title = "穿衣")
    private String dressing;

    @Schema(title = "大便控制")
    private String bowelControl;

    @Schema(title = "小便控制")
    private String bladderControl;

    @Schema(title = "如厕")
    private String toileting;

    @Schema(title = "床椅转椅情况")
    private String chairTransfer;

    @Schema(title = "平地走动")
    private String walkingOnFlatGround;

    @Schema(title = "上下楼梯")
    private String upDownStairs;

    @Schema(title = "总分")
    private Integer totalScore;

    @Schema(title = "能力等级")
    private String abilityRating;
}
