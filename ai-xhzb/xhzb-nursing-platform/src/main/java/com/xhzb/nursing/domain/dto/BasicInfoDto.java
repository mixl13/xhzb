package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基本信息DTO
 */
@Data
@Schema(description = "基本信息DTO")
public class BasicInfoDto {

    @Schema(title = "老人姓名")
    private String elderName;

    @Schema(title = "身份证号码")
    private String idCard;

    @Schema(title = "出生日期")
    private String birthDate;

    @Schema(title = "年龄")
    private Integer age;

    @Schema(title = "性别")
    private String gender;

    @Schema(title = "民族")
    private String nation;

    @Schema(title = "社保卡号")
    private String socialSecurityCard;

    @Schema(title = "宗教信仰")
    private String religiousBelief;

    @Schema(title = "文化程度")
    private String educationLevel;

    @Schema(title = "婚姻状况")
    private String maritalStatus;

    @Schema(title = "居住情况")
    private String livingSituation;

    @Schema(title = "医疗付款方式")
    private String medicalPaymentMethod;

    @Schema(title = "经济来源")
    private String economicSource;

    @Schema(title = "老人联系方式")
    private String elderContact;

    @Schema(title = "信息提供者姓名")
    private String providerName;

    @Schema(title = "信息提供者联系方式")
    private String providerContact;

    @Schema(title = "与老人关系")
    private String relationshipWithElder;

    @Schema(title = "知情同意书URL")
    private String informedConsentUrl;
}
