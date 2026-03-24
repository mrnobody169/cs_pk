package com.pokertrack.service;

import com.pokertrack.dto.request.CreatePlayerRequest;
import com.pokertrack.exception.ForbiddenException;
import com.pokertrack.exception.ResourceNotFoundException;
import com.pokertrack.mapper.PlayerMapper;
import com.pokertrack.model.Player;
import com.pokertrack.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerMapper playerMapper;
    @Autowired
    private CurrentUserProvider currentUser;

    public List<Player> listPlayers(Long groupId) {
        if (!currentUser.hasAccessTo(groupId))
            throw new ForbiddenException("Access denied");
        return playerMapper.findByGroupId(groupId);
    }

    @Transactional
    public Player createPlayer(Long groupId, CreatePlayerRequest req) {
        requireAdmin(groupId);
        Player player = new Player();
        player.setGroupId(groupId);
        player.setName(req.getName());
        player.setAlias(req.getAlias());
        playerMapper.insert(player);
        return playerMapper.findById(player.getId());
    }

    @Transactional
    public Player updatePlayer(Long playerId, String name, String alias) {
        Player player = playerMapper.findById(playerId);
        if (player == null)
            throw new ResourceNotFoundException("Player", playerId);
        requireAdmin(player.getGroupId());
        player.setName(name != null ? name : player.getName());
        player.setAlias(alias != null ? alias : player.getAlias());
        playerMapper.update(player);
        return playerMapper.findById(playerId);
    }

    @Transactional
    public void deactivatePlayer(Long playerId) {
        Player player = playerMapper.findById(playerId);
        if (player == null)
            throw new ResourceNotFoundException("Player", playerId);
        requireAdmin(player.getGroupId());
        playerMapper.deactivate(playerId);
    }

    private void requireAdmin(Long groupId) {
        if (!currentUser.isAdminOf(groupId))
            throw new ForbiddenException("Only ADMIN can manage players");
    }
}
