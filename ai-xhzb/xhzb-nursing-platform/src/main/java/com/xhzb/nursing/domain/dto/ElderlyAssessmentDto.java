package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

/**
 * 老人能力评估总DTO
 */
@Data
@Schema(description = "老人能力评估总DTO")
public class ElderlyAssessmentDto {
    private Long id;

    @Schema(title = "基本信息")
    private BasicInfoDto basicInfo;

    @Schema(title = "健康评估")
    private HealthAssessmentDto healthAssessmentDto;

    @Schema(title = "日常活动")
    private DailyLivingActivitiesDto dailyLivingActivities;

    @Schema(title = "精神状态")
    private MentalStateDto mentalState;

    @Schema(title = "感知觉及沟通")
    private PerceptionAndCommunicationDto perceptionAndCommunication;

    @Schema(title = "社会参与")
    private SocialParticipationDto socialParticipation;
}
