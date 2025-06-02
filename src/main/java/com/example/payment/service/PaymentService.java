package com.example.payment.service;

import com.example.payment.controller.GetOwnerAdvertisements;
import com.example.payment.kafka.DTO.ReferralCommissionEventDTO;
import com.example.payment.kafka.producers.SendReferralCommission;
import com.example.payment.model.Transaction;
import com.example.payment.model.User;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.service.processor.BuyerProcessor;
import com.example.payment.service.processor.PlatformProcessor;
import com.example.payment.service.processor.SellerProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(BonusService.class);


    private final BuyerProcessor buyerProcessor;
    private final PlatformProcessor platformProcessor;
    private final SellerProcessor sellerProcessor;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final SendReferralCommission sendReferralCommission;
    private final GetOwnerAdvertisements getOwnerAdvertisements;


    public PaymentService(BuyerProcessor buyerProcessor, PlatformProcessor platformProcessor, SellerProcessor sellerProcessor, UserRepository userRepository, TransactionRepository transactionRepository, ObjectMapper objectMapper, SendReferralCommission sendReferralCommission, GetOwnerAdvertisements getOwnerAdvertisements) {
        this.buyerProcessor = buyerProcessor;
        this.platformProcessor = platformProcessor;
        this.sellerProcessor = sellerProcessor;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
        this.sendReferralCommission = sendReferralCommission;
        this.getOwnerAdvertisements = getOwnerAdvertisements;
    }

    @Transactional
    public void processPayment(Long buyersId, Map<Long, BigDecimal> advertisements){


        // создаем список пользователей которых надо будет обновить в бд, делаем так,
        // чтобы обращаться к бд не каждый раз когда хотим записать изменения, а один раз и сразу обновить всех
        List<User> usersToUpdate = new ArrayList<>();
        // аналогично
        List<Transaction> transactionsToCreate = new ArrayList<>();

        /// ----------------------- Работа с покупателем -----------------------

        // добавляем юзера в список(списали сумму товаров со счета)
        usersToUpdate.add(buyerProcessor.processBuyer(buyersId, advertisements));
        transactionsToCreate.add(buyerProcessor.createBuyerTransaction(buyersId, advertisements));

        /// ----------------------- Работа с продавцами -----------------------

        // получаем список id товаров, которые хочет купить покупатель
        List<Long> advertisementIds = buyerProcessor.getAdvertisementsList(advertisements);

        // получаем мапу в которой key=id товара, value=id продавца через рест запрос к адвертайзментСервису
        Map<Long, Long> advertisementToOwnerMap = getOwnerAdvertisements.getOwnerAdvertisements(advertisementIds);

        // создаем Pair Map<key=id продавца, value=сумма продажи>, а второе значение суммарная комиссия
        Pair<Map<Long, BigDecimal>, BigDecimal> sellerTotalAmounts = sellerProcessor.groupSellerAmounts(advertisements, advertisementToOwnerMap);

        // создаем мапу в которой key=id продавца, value=список id товаров
        Map<Long, List<Long>> sellerAdvertisements = sellerProcessor.groupSellerAdvertisements(advertisements,advertisementToOwnerMap);

        // добавляем продавцов в список апдейта
        List<User> sellersUpdate = sellerProcessor.createSellerUpdate(sellerTotalAmounts.getFirst());
        usersToUpdate.addAll(sellersUpdate);

        // Добавляем транзакции продавцов в список
        List<Transaction> sellerTransactions = sellerProcessor.createSellerTransactions(sellerTotalAmounts.getFirst(), sellerAdvertisements);
        transactionsToCreate.addAll(sellerTransactions);

        BigDecimal commissionAmount = sellerTotalAmounts.getSecond();

        // сохранение всех данных в БД
        userRepository.saveAll(usersToUpdate);
        transactionRepository.saveAll(transactionsToCreate);

        /// ----------------------- Реферальный Сервис -----------------------

        // Отправка информации в реферальный сервис
        try {
            ReferralCommissionEventDTO referralEvent = new ReferralCommissionEventDTO(buyersId, commissionAmount);
            String json = objectMapper.writeValueAsString(referralEvent);
            sendReferralCommission.sendReferralCommission(json);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации данных для реферального сервиса: {}", e.getMessage());
        } // Не прерываем выполнение, так как основная операция уже выполнена

        /// ----------------------- Работа с платформой -----------------------

        // начислять комиссию платформе можно только после ответа реферального сервиса,
        // потому что платформе идет все что осталось после раздачи рефералам, а это может быть
        // от 50 до 100% комиссии, в зависимости от размера дерева, которое строится от этого пользователя

    }
}
