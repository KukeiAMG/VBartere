package com.example.Referral.DTO;

import com.example.Referral.model.UserNode;

public class ParentChainDTO {
    private final UserNode userNode;
    private final int level;

    public ParentChainDTO(UserNode userNode, int level) {
        this.userNode = userNode;
        this.level = level;
    }

    // Геттеры
    public UserNode getUserNode() {
        return userNode;
    }

    public int getLevel() {
        return level;
    }
}