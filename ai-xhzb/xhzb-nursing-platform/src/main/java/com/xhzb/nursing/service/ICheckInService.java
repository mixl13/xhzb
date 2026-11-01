package com.xhzb.nursing.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhzb.nursing.domain.dto.CheckInApplyDto;
import com.xhzb.nursing.domain.entity.CheckIn;
import com.xhzb.nursing.domain.vo.CheckInDetailVo;

/**
 * 入住登记Service接口
 * 
 * @author mixl
 * @date 2026-11-01
 */
public interface ICheckInService extends IService<CheckIn>
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
     * 批量删除入住登记
     * 
     * @param ids 需要删除的入住登记主键集合
     * @return 结果
     */
    public int deleteCheckInByIds(Long[] ids);

    /**
     * 删除入住登记信息
     * 
     * @param id 入住登记主键
     * @return 结果
     */
    public int deleteCheckInById(Long id);

    /**
     * 申请入住
     *
     * @param dto 申请入住入参
     */
    public void applyCheckIn(CheckInApplyDto dto);

    /**
     * 查询入住详情
     *
     * @param id 入住id
     * @return 入住详情
     */
    public CheckInDetailVo getCheckInDetail(Long id);
}
