package com.xhzb.nursing.mapper;

import java.util.List;

import com.xhzb.nursing.domain.entity.CheckInConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 入住配置表Mapper接口
 * 
 * @author mixl
 * @date 2026-11-01
 */
@Mapper
public interface CheckInConfigMapper extends BaseMapper<CheckInConfig>
{
    /**
     * 查询入住配置表
     * 
     * @param id 入住配置表主键
     * @return 入住配置表
     */
    public CheckInConfig selectCheckInConfigById(Long id);

    /**
     * 查询入住配置表列表
     * 
     * @param checkInConfig 入住配置表
     * @return 入住配置表集合
     */
    public List<CheckInConfig> selectCheckInConfigList(CheckInConfig checkInConfig);

    /**
     * 新增入住配置表
     * 
     * @param checkInConfig 入住配置表
     * @return 结果
     */
    public int insertCheckInConfig(CheckInConfig checkInConfig);

    /**
     * 修改入住配置表
     * 
     * @param checkInConfig 入住配置表
     * @return 结果
     */
    public int updateCheckInConfig(CheckInConfig checkInConfig);

    /**
     * 删除入住配置表
     * 
     * @param id 入住配置表主键
     * @return 结果
     */
    public int deleteCheckInConfigById(Long id);

    /**
     * 批量删除入住配置表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCheckInConfigByIds(Long[] ids);
}
