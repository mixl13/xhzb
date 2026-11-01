package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 疾病诊断DTO
 */
@Data
@Schema(description = "疾病诊断DTO")
public class DiseaseDiagnosisDto {

    @Schema(title = "痴呆")
    private String dementia;

    @Schema(title = "精神疾病")
    private String mentalIllness;

    @Schema(title = "慢性病")
    private String chronicDiseases;
}
