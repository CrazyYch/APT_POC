package com.iget.apt.poc.dao;

import com.iget.apt.poc.modal.Vo.RelationshipVoExample;
import com.iget.apt.poc.modal.Vo.RelationshipVoKey;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface RelationshipVoMapper {
    long countByExample(RelationshipVoExample example);

    int deleteByExample(RelationshipVoExample example);

    int deleteByPrimaryKey(RelationshipVoKey key);

    int insert(RelationshipVoKey record);

    int insertSelective(RelationshipVoKey record);

    List<RelationshipVoKey> selectByExample(RelationshipVoExample example);

    int updateByExampleSelective(@Param("record") RelationshipVoKey record, @Param("example") RelationshipVoExample example);

    int updateByExample(@Param("record") RelationshipVoKey record, @Param("example") RelationshipVoExample example);
}