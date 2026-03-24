package com.pokertrack.mapper;

import com.pokertrack.model.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMapper {
    Group findById(@Param("id") Long id);

    List<Group> findByUserId(@Param("userId") Long userId);

    void insert(Group group);
}
