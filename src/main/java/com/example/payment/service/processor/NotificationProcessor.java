//package com.example.payment.service.processor;
//
//import com.example.payment.dto.NotificationPurchaseDTO;
//import com.example.payment.dto.ReferralCommissionEventDTO;
//import com.example.payment.kafka.SendNotificationRequest;
//import com.example.payment.kafka.SendReferralCommission;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Component
//public class NotificationProcessor {
//    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);
//    private final SendNotificationRequest notificationSender;
//    private final SendReferralCommission referralCommissionSender;
//
//    public NotificationProcessor(SendNotificationRequest notificationSender,
//                               SendReferralCommission referralCommissionSender) {
//        this.notificationSender = notificationSender;
//        this.referralCommissionSender = referralCommissionSender;
//    }
//
//    public void sendPurchaseNotification(Long buyerId, BigDecimal totalAmount,
//                                       BigDecimal remainingBalance, Map<Long, Long> advertisementToOwnerMap) {
//        try {
//            NotificationPurchaseDTO notification = new NotificationPurchaseDTO();
//            notification.setBuyerId(buyerId);
//            notification.setTransactionType("PURCHASE");
//            notification.setAmount(totalAmount);
//            notification.setRemainingBalance(remainingBalance);
//            notification.setPurchaseTime(LocalDateTime.now());
//            notification.setSellerInfo(advertisementToOwnerMap);
//
//            notificationSender.sendNotification(notification);
//            log.info("Successfully sent purchase notification for buyer {}", buyerId);
//        } catch (Exception e) {
//            log.error("Failed to send purchase notification: {}", e.getMessage());
//            // Don't throw exception as notification failure shouldn't affect the main flow
//        }
//    }
//
//    public void sendReferralCommission(Long userId, BigDecimal commission) {
//        try {
//            ReferralCommissionEventDTO event = new ReferralCommissionEventDTO(userId, commission);
//            referralCommissionSender.sendReferralCommission(event);
//            log.info("Successfully sent referral commission notification for user {}", userId);
//        } catch (Exception e) {
//            log.error("Failed to send referral commission notification: {}", e.getMessage());
//            // Don't throw exception as notification failure shouldn't affect the main flow
//        }
//    }
//}
