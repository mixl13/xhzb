package com.xhzb.nursing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 入住详情-老人家属VO
 *
 * @author mixl
 */
@Data
@Schema(description = "入住详情-老人家属VO")
public class ElderFamilyVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 家属姓名 */
    @Schema(title = "家属姓名")
    private String name;

    /** 家属手机号 */
    @Schema(title = "家属手机号")
    private String phone;

    /** 与老人关系 */
    @Schema(title = "与老人关系")
    private String kinship;

}
