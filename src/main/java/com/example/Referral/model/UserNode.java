package com.example.Referral.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
public class UserNode {

    @Id
    private Long userId;
    private  String referrerId;
    private String refId;

    @Relationship(type = "REFERRED", direction = Relationship.Direction.OUTGOING) // "REFERRED" - тип связи между обьектами
    // Relationship.Direction.OUTGOING - означает что пользователь пригласил этих людей
    private List<UserNode> referredUsers; //список приглашенных пользователей

    public UserNode(){}

    public UserNode(Long userId, String referrerId, String refId){
        this.userId = userId;
        this.referrerId = referrerId;
        this.refId = refId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUid(Long userId) {
        this.userId = userId;
    }

    public String getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(String referrerId) {
        this.referrerId = referrerId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public List<UserNode> getReferredUsers() {
        return referredUsers;
    }

    public void setReferredUsers(List<UserNode> referredUsers) {
        this.referredUsers = referredUsers;
    }
}
