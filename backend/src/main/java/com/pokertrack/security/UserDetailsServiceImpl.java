package com.pokertrack.security;

import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Block login for non-approved users
        if (!"APPROVED".equals(user.getStatus())) {
            if ("PENDING".equals(user.getStatus())) {
                throw new DisabledException("Tài khoản đang chờ admin duyệt");
            } else {
                throw new DisabledException("Tài khoản đã bị từ chối");
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList());
    }
}
