package com.xhzb.nursing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 楼层-房间-床位 三级树形VO
 * <p>
 * 用于「根据床位状态查询获取所有楼层数据」接口，前端级联选择器统一使用 value / label / children 结构。
 * </p>
 *
 * @author mixl
 */
@Data
@Schema(description = "楼层-房间-床位树形VO")
public class FloorTreeVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 节点值：楼层ID / 房间ID / 床位ID */
    @Schema(title = "节点值")
    private String value;

    /** 节点名称：楼层名称 / 房间编号 / 床位编号 */
    @Schema(title = "节点名称")
    private String label;

    /** 子节点：楼层下为房间，房间下为床位 */
    @Schema(title = "子节点")
    private List<FloorTreeVo> children;

}
