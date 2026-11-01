package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 感知觉及沟通DTO
 */
@Data
@Schema(description = "感知觉及沟通DTO")
public class PerceptionAndCommunicationDto {

    @Schema(title = "意识水平")
    private String consciousLevel;

    @Schema(title = "视力")
    private String vision;

    @Schema(title = "听力")
    private String hearing;

    @Schema(title = "沟通交流")
    private String communication;

    @Schema(title = "感知觉与沟通分级")
    private String perceptionAndCommunicationLevel;

    @Schema(title = "总分")
    private Integer totalScore;

    @Schema(title = "能力等级")
    private String abilityRating;
}
