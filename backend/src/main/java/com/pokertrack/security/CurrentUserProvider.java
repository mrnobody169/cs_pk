package com.pokertrack.security;

import com.pokertrack.mapper.GroupMemberMapper;
import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper bean to get the currently authenticated user from the SecurityContext.
 */
@Component
public class CurrentUserProvider {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }
        String email = authentication.getName();
        return userMapper.findByEmail(email);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Returns the role of the current user in a given group, or null if not a
     * member.
     */
    public String getCurrentUserRole(Long groupId) {
        Long userId = getCurrentUserId();
        return groupMemberMapper.findRole(groupId, userId);
    }

    public boolean isAdminOf(Long groupId) {
        return "ADMIN".equals(getCurrentUserRole(groupId));
    }

    public boolean isMemberOrAbove(Long groupId) {
        String role = getCurrentUserRole(groupId);
        return "ADMIN".equals(role) || "MEMBER".equals(role);
    }

    public boolean hasAccessTo(Long groupId) {
        return getCurrentUserRole(groupId) != null;
    }

    /**
     * Checks if current user is a system-level admin (is_admin flag in users
     * table).
     * Only system admins can perform write operations.
     */
    public boolean isSystemAdmin() {
        User user = getCurrentUser();
        return user != null && Boolean.TRUE.equals(user.getIsAdmin());
    }
}
