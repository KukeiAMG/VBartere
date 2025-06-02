package com.example.Referral.DTO;

public class ReferralLevelCount {
    private final Integer level;
    private final Long count;


    public ReferralLevelCount(Integer level, Long count) {
        this.level = level;
        this.count = count;
    }

    // Геттеры
    public Integer getLevel() { return level; }
    public Long getCount() { return count; }

    @Override
    public String toString() {
        return "ReferralLevelCount{" +
                "level=" + level +
                ", count=" + count +
                '}';
    }
}
