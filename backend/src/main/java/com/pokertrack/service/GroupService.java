package com.pokertrack.service;

import com.pokertrack.dto.request.AddMemberRequest;
import com.pokertrack.exception.ConflictException;
import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.mapper.GroupMapper;
import com.pokertrack.mapper.GroupMemberMapper;
import com.pokertrack.mapper.UserMapper;
import com.pokertrack.model.Group;
import com.pokertrack.model.GroupMember;
import com.pokertrack.model.User;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CurrentUserProvider currentUser;

    public List<Group> getGroupsForCurrentUser() {
        return groupMapper.findByUserId(currentUser.getCurrentUserId());
    }

    @Transactional
    public Group createGroup(String name) {
        requireSystemAdmin(); // Only system admin can create groups
        Long userId = currentUser.getCurrentUserId();
        Group group = new Group();
        group.setName(name);
        group.setCreatedBy(userId);
        groupMapper.insert(group);

        // Creator automatically becomes ADMIN
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("ADMIN");
        groupMemberMapper.insert(member);

        return group;
    }

    public List<GroupMember> getMembers(Long groupId) {
        // Any group member can view the member list
        requireMemberAccess(groupId);
        return groupMemberMapper.findByGroupId(groupId);
    }

    @Transactional
    public void addMember(Long groupId, AddMemberRequest request) {
        requireSystemAdmin(); // Only system admin can add members
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null)
            throw new ResourceNotFoundException("User not found with email: " + request.getEmail());

        GroupMember existing = groupMemberMapper.findByGroupAndUser(groupId, user.getId());
        if (existing != null)
            throw new ConflictException("User is already a member of this group");

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(user.getId());
        member.setRole(request.getRole());
        groupMemberMapper.insert(member);
    }

    @Transactional
    public void updateMember(Long groupId, Long userId, String newRole, boolean remove) {
        requireSystemAdmin(); // Only system admin
        GroupMember existing = groupMemberMapper.findByGroupAndUser(groupId, userId);
        if (existing == null)
            throw new ResourceNotFoundException("Member not found in this group");

        if (remove) {
            groupMemberMapper.delete(groupId, userId);
        } else {
            groupMemberMapper.updateRole(groupId, userId, newRole);
        }
    }

    private void requireSystemAdmin() {
        if (!currentUser.isSystemAdmin()) {
            throw new ForbiddenException("Chỉ admin mới có quyền thực hiện");
        }
    }

    private void requireMemberAccess(Long groupId) {
        if (!currentUser.hasAccessTo(groupId)) {
            throw new ForbiddenException("Access denied to this group");
        }
    }
}
