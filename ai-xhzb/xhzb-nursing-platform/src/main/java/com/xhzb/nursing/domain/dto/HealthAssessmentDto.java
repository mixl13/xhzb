package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 健康评估DTO
 */
@Data
@Schema(description = "健康评估DTO")
public class HealthAssessmentDto {

    @Schema(title = "疾病诊断")
    private DiseaseDiagnosisDto diseaseDiagnosis;

    @Schema(title = "最近30天意外事件和体检报告")
    private Recent30DaysDto recent30Days;
}
