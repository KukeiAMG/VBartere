package com.example.Referral.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;


import java.util.List;

@Node("UserNode")
public class UserNode {

    //TODO:
    @Id
    private Long userId;
    private String referralCode;

    public UserNode(){}

    public UserNode(Long userId){
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
}
