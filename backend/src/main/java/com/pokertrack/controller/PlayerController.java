package com.pokertrack.controller;

import com.pokertrack.dto.request.CreatePlayerRequest;
import com.pokertrack.model.Player;
import com.pokertrack.service.PlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "Players", description = "Quản lý danh sách người chơi trong nhóm")
@RestController
@RequestMapping("/api")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/groups/{groupId}/players")
    public ResponseEntity<List<Player>> listPlayers(@PathVariable Long groupId) {
        return ResponseEntity.ok(playerService.listPlayers(groupId));
    }

    @PostMapping("/groups/{groupId}/players")
    public ResponseEntity<Player> createPlayer(@PathVariable Long groupId,
            @Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(groupId, request));
    }

    @PatchMapping("/players/{playerId}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long playerId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(playerService.updatePlayer(playerId, body.get("name"), body.get("alias")));
    }

    @PostMapping("/players/{playerId}/deactivate")
    public ResponseEntity<Void> deactivatePlayer(@PathVariable Long playerId) {
        playerService.deactivatePlayer(playerId);
        return ResponseEntity.ok().build();
    }
}
