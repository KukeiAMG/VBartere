package com.example.Referral.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;


import java.util.List;
import java.util.Objects;

@Node("UserNode")
public class UserNode {

    //TODO:
    @Id
    @NotNull
    private Long userId;
    private String referralCode;

    public UserNode(){}

    public UserNode(Long userId){
        if (userId == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        }
        this.userId = userId;
        this.referralCode = "ref"+userId;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReferralCode() {
        return referralCode;
    }
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }


    @Override
    public String toString() {
        return "\nUserID:" + userId + "   ReferralCode: "+ referralCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNode userNode = (UserNode) o;
        return Objects.equals(userId, userNode.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}