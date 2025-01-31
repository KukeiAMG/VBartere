package com.example.Referral.service;

import com.example.Referral.model.UserNode;

import java.util.HashMap;
import java.util.Map;

public class ReferralCommission {

    private static Map<Integer, Double> CommissionRate = new HashMap<>(Map.ofEntries(
            Map.entry(1, 0.15),
            Map.entry(2,0.1),
            Map.entry(3, 0.05),
            Map.entry(4, 0.03),
            Map.entry(5, 0.02),
            Map.entry(6, 0.01),
            Map.entry(7, 0.01),
            Map.entry(8, 0.01),
            Map.entry(9, 0.01),
            Map.entry(10, 0.01),
            Map.entry(11, 0.01),
            Map.entry(12, 0.01),
            Map.entry(13, 0.01),
            Map.entry(14, 0.01),
            Map.entry(15, 0.01)
            ));

    // Метод для расчета комиссий
    public Map<Long, Double> calculateCommissions(UserNode user, double commissionAmount) {
        Map<Long, Double> commissions = new HashMap<>();
        calculate(user, commissionAmount, commissions, 1);
        return commissions;
    }

    // Рекурсивный метод для расчета комиссий
    private void calculate(UserNode user, double commissionAmount, Map<Long, Double> commissions, int level) {
        if (user == null || level > CommissionRate.size()) {
            return;
        }

        // Рассчитать комиссию для текущего пользователя
        double commissionRate = CommissionRate.get(level);
        double commission = commissionAmount * commissionRate;

        commissions.put(user.getUserId(), commission);

        // Рекурсивно вызвать для пригласившего пользователя
        UserNode referrer = DataService.getAncByUidDesc(user.getUserId());
        calculate(referrer, commissionAmount, commissions, level + 1);
    }
}
