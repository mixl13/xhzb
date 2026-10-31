package com.xhzb.nursing.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xhzb.nursing.domain.entity.NursingLevel;
import com.xhzb.nursing.mapper.NursingLevelMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NursingLevelTool {
    @Autowired
    private NursingLevelMapper nursingLevelMapper;

    @Tool(name = "find_level_tool",description = "查询所有的护理等级")
    public List<NursingLevel> findNursingLevels(){
        return nursingLevelMapper.selectList(Wrappers.lambdaQuery(NursingLevel.class).eq(NursingLevel::getStatus,1));
    }
}
