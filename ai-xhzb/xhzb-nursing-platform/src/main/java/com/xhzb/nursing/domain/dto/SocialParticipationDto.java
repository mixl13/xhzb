package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 社会参与DTO
 */
@Data
@Schema(description = "社会参与DTO")
public class SocialParticipationDto {

    @Schema(title = "生活能力")
    private String livingAbility;

    @Schema(title = "工作能力")
    private String workAbility;

    @Schema(title = "时间/空间定向")
    private String timeSpaceOrientation;

    @Schema(title = "人物定向")
    private String personOrientation;

    @Schema(title = "社会技能")
    private String socialSkills;

    @Schema(title = "总分")
    private Integer totalScore;

    @Schema(title = "能力等级")
    private String abilityRating;
}
