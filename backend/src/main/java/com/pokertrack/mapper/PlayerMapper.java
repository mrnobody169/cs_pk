package com.pokertrack.mapper;

import com.pokertrack.model.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlayerMapper {
    Player findById(@Param("id") Long id);

    List<Player> findByGroupId(@Param("groupId") Long groupId);

    List<Player> findActiveByGroupId(@Param("groupId") Long groupId);

    void insert(Player player);

    void update(Player player);

    void deactivate(@Param("id") Long id);
}
