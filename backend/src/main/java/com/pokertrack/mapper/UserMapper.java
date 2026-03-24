package com.pokertrack.mapper;

import com.pokertrack.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByEmail(@Param("email") String email);

    User findById(@Param("id") Long id);

    void insert(User user);

    List<User> findByStatus(@Param("status") String status);

    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
