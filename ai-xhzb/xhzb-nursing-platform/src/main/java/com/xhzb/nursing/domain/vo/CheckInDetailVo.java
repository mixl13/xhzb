package com.xhzb.nursing.domain.vo;

import com.xhzb.nursing.domain.entity.Contract;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 入住详情VO
 * <p>
 * 由 check_in、elder、check_in_config、contract 多表数据组装而成。
 * </p>
 *
 * @author mixl
 */
@Data
@Schema(description = "入住详情VO")
public class CheckInDetailVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 老人信息 */
    @Schema(title = "老人信息")
    private CheckInElderVo checkInElderVo;

    /** 老人家属信息列表 */
    @Schema(title = "老人家属信息列表")
    private List<ElderFamilyVo> elderFamilyVoList;

    /** 入住配置 */
    @Schema(title = "入住配置")
    private CheckInConfigVo checkInConfigVo;

    /** 签约合同 */
    @Schema(title = "签约合同")
    private Contract contract;

}
