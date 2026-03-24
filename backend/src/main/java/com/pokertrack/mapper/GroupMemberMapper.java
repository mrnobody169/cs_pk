package com.pokertrack.mapper;

import com.pokertrack.model.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMemberMapper {
    String findRole(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);

    GroupMember findByGroupAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

    void insert(GroupMember groupMember);

    void updateRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") String role);

    void delete(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
