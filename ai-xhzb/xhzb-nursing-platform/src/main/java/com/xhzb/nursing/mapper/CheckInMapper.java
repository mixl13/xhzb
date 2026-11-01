package com.xhzb.nursing.mapper;

import java.util.List;

import com.xhzb.nursing.domain.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 入住登记Mapper接口
 * 
 * @author mixl
 * @date 2026-11-01
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn>
{
    /**
     * 查询入住登记
     * 
     * @param id 入住登记主键
     * @return 入住登记
     */
    public CheckIn selectCheckInById(Long id);

    /**
     * 查询入住登记列表
     * 
     * @param checkIn 入住登记
     * @return 入住登记集合
     */
    public List<CheckIn> selectCheckInList(CheckIn checkIn);

    /**
     * 新增入住登记
     * 
     * @param checkIn 入住登记
     * @return 结果
     */
    public int insertCheckIn(CheckIn checkIn);

    /**
     * 修改入住登记
     * 
     * @param checkIn 入住登记
     * @return 结果
     */
    public int updateCheckIn(CheckIn checkIn);

    /**
     * 删除入住登记
     * 
     * @param id 入住登记主键
     * @return 结果
     */
    public int deleteCheckInById(Long id);

    /**
     * 批量删除入住登记
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCheckInByIds(Long[] ids);
}
