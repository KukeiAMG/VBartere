package com.example.payment.kafka.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class ReferralRewardsDistributionDTO {
    private Map<Long, BigDecimal> distribution;
    private BigDecimal platformShare;

    public ReferralRewardsDistributionDTO(Map<Long, BigDecimal> distribution, BigDecimal platformShare) {
        this.distribution = distribution;
        this.platformShare = platformShare;
    }

    public Map<Long, BigDecimal> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<Long, BigDecimal> distribution) {
        this.distribution = distribution;
    }

    public BigDecimal getPlatformShare() {
        return platformShare;
    }

    public void setPlatformShare(BigDecimal platformShare) {
        this.platformShare = platformShare;
    }

    @Override
    public String toString() {
        return "ReferralRewardsDistributionDTO{" +
                "distribution=" + distribution +
                ", platformShare=" + platformShare +
                '}';
    }
}
