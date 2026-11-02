package com.xhzb.nursing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 老人家属信息DTO
 *
 * @author mixl
 */
@Data
@Schema(description = "老人家属信息DTO")
public class ElderFamilyDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 家属姓名 */
    @NotBlank(message = "家属姓名不能为空")
    @Schema(title = "家属姓名")
    private String name;

    /** 家属手机号 */
    @NotBlank(message = "家属手机号不能为空")
    @Schema(title = "家属手机号")
    private String phone;

    /** 与老人关系（亲属关系） */
    @Schema(title = "与老人关系")
    private String kinship;

}
