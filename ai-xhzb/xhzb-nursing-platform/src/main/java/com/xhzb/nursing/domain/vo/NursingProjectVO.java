package com.xhzb.nursing.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 下拉选项VO（通用 label/value 结构）
 *
 * 用于下拉列表数据源，例如：
 * 1. 护理计划新增/编辑页面添加护理项目时的「护理项目」下拉框
 * 2. 护理等级新增/编辑页面选择「护理计划」时的下拉框
 *
 * @author mixl
 * @date 2026-10-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NursingProjectVO implements Serializable {
    /** 选项显示文本 */
    private String label;

    /** 选项值（对应数据ID） */
    private String value;

}
