package com.pokertrack.controller;

import com.pokertrack.dto.request.AddMemberRequest;
import com.pokertrack.model.Group;
import com.pokertrack.model.GroupMember;
import com.pokertrack.service.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "Groups & Members", description = "Quản lý nhóm và thành viên")
@RestController
@RequestMapping("/api")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getMyGroups() {
        return ResponseEntity.ok(groupService.getGroupsForCurrentUser());
    }

    @PostMapping("/groups")
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(name.trim()));
    }

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getMembers(groupId));
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        groupService.addMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<Void> updateMember(@PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        String role = (String) body.get("role");
        Boolean remove = (Boolean) body.get("remove");
        groupService.updateMember(groupId, userId, role, Boolean.TRUE.equals(remove));
        return ResponseEntity.ok().build();
    }
}
