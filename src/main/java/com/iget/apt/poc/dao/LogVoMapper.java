package com.iget.apt.poc.dao;

import com.iget.apt.poc.modal.Vo.LogVo;
import com.iget.apt.poc.modal.Vo.LogVoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface LogVoMapper {
    long countByExample(LogVoExample example);

    int deleteByExample(LogVoExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LogVo record);

    int insertSelective(LogVo record);

    List<LogVo> selectByExample(LogVoExample example);

    LogVo selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") LogVo record, @Param("example") LogVoExample example);

    int updateByExample(@Param("record") LogVo record, @Param("example") LogVoExample example);

    int updateByPrimaryKeySelective(LogVo record);

    int updateByPrimaryKey(LogVo record);
}