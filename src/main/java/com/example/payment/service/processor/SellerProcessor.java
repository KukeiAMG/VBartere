package com.example.payment.service.processor;

import com.example.payment.model.Transaction;
import com.example.payment.model.TransactionType;
import com.example.payment.model.User;
import com.example.payment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class SellerProcessor {
    private static final Logger log = LoggerFactory.getLogger(SellerProcessor.class);
    private static final BigDecimal COMMISSION_PERCENT = new BigDecimal("0.1");
    private final UserRepository userRepository;

    public SellerProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Группирует суммы платежей по продавцам на основе карты объявлений и соответствия объявлений продавцам.
     *
     * <p>Алгоритм работы метода:
     * <ol>
     *   <li>Создается пустая карта для хранения итоговых сумм по продавцам</li>
     *   <li>Для каждого объявления из входной карты:
     *     <ul>
     *       <li>Получаем ID объявления и его цену</li>
     *       <li>Находим ID продавца по ID объявления</li>
     *       <li>Если продавец уже есть в результирующей карте:
     *         <br>- Добавляем цену объявления к его текущей сумме</li>
     *       <li>Если продавца нет в карте:
     *         <br>- Добавляем новую запись с ID продавца и ценой объявления</li>
     *     </ul>
     *   </li>
     *   <li>Возвращается карта с суммарными значениями по каждому продавцу</li>
     * </ol>
     *
     * @param advertisements карта объявлений, где:
     *                       <br>Ключ - ID объявления (Long)
     *                       <br>Значение - цена объявления (BigDecimal)
     * @param advertisementToOwnerMap карта соответствия объявлений продавцам, где:
     *                       <br>Ключ - ID объявления (Long)
     *                       <br>Значение - ID продавца (Long)
     * @return карта сгруппированных сумм по продавцам, где:
     *                       <br>Ключ - ID продавца (Long)
     *                       <br>Значение - общая сумма всех его объявлений (BigDecimal)
     * @throws NullPointerException если один из параметров равен null
     *
     * <p>Пример использования:
     * <pre>{@code
     * Map<Long, BigDecimal> ads = Map.of(
     *     1L, new BigDecimal("100.50"),
     *     2L, new BigDecimal("200.75"),
     *     3L, new BigDecimal("150.00")
     * );
     *
     * Map<Long, Long> owners = Map.of(
     *     1L, 101L,
     *     2L, 102L,
     *     3L, 101L
     * );
     *
     * Map<Long, BigDecimal> result = groupSellerAmounts(ads, owners);
     * // Результат: {101=250.50, 102=200.75}
     * }</pre>
     */
    public Pair<Map<Long, BigDecimal>, BigDecimal> groupSellerAmounts(Map<Long, BigDecimal> advertisements,
                                                                      Map<Long, Long> advertisementToOwnerMap) {
        Map<Long, BigDecimal> sellerTotalAmounts = new HashMap<>();
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : advertisements.entrySet()) {
            Long advertisementId = entry.getKey();
            //вычисляем прибыль продавца с учетом комиссии
            BigDecimal price = entry.getValue().subtract(entry.getValue().multiply(COMMISSION_PERCENT));
            totalCommission = totalCommission.add(entry.getValue().multiply(COMMISSION_PERCENT));
            Long sellerId = advertisementToOwnerMap.get(advertisementId);

            if (sellerTotalAmounts.containsKey(sellerId)) {
                // Если продавец уже есть в мапе, добавляем к его сумме
                BigDecimal currentAmount = sellerTotalAmounts.get(sellerId);
                sellerTotalAmounts.put(sellerId, currentAmount.add(price));
            } else {
                // Если продавца нет в мапе, добавляем новую запись
                sellerTotalAmounts.put(sellerId, price);
            }
        }

        return Pair.of(sellerTotalAmounts, totalCommission);
    }

    /**
     * Группирует идентификаторы объявлений по продавцам, создавая карту "продавец → список его объявлений".
     *
     * <p><b>Алгоритм работы:</b>
     * <ol>
     *   <li>Создается пустая карта для хранения результатов (HashMap)</li>
     *   <li>Для каждого объявления из входной карты:
     *     <ul>
     *       <li>Определяется ID продавца через lookup в advertisementToOwnerMap</li>
     *       <li>Если продавец отсутствует в результирующей карте:
     *         <br>- Создается новая запись с пустым списком объявлений</li>
     *       <li>ID текущего объявления добавляется в список объявлений продавца</li>
     *     </ul>
     *   </li>
     *   <li>Возвращается сформированная карта группировки</li>
     * </ol>
     *
     * <p><b>Особенности реализации:</b>
     * <ul>
     *   <li>Гарантирует, что каждому продавцу соответствует non-null список</li>
     *   <li>Списки объявлений сохраняют порядок добавления (ArrayList)</li>
     *   <li>Не модифицирует входные параметры</li>
     * </ul>
     *
     * @param advertisements карта объявлений, где:
     *                       <br>Ключ - ID объявления (Long)
     *                       <br>Значение - цена объявления (BigDecimal, не используется в этом методе)
     * @param advertisementToOwnerMap карта соответствия "объявление → продавец", где:
     *                       <br>Ключ - ID объявления (Long)
     *                       <br>Значение - ID продавца (Long)
     * @return карта группировки, где:
     *                       <br>Ключ - ID продавца (Long)
     *                       <br>Значение - список ID его объявлений (List<Long>)
     * @throws NullPointerException если:
     *                       <br>- advertisements = null
     *                       <br>- advertisementToOwnerMap = null
     *                       <br>- advertisementToOwnerMap не содержит ключа из advertisements
     *
     * <p><b>Пример использования:</b>
     * <pre>{@code
     * Map<Long, BigDecimal> ads = new HashMap<>();
     * ads.put(1L, new BigDecimal("100"));
     * ads.put(2L, new BigDecimal("200"));
     * ads.put(3L, new BigDecimal("300"));
     *
     * Map<Long, Long> owners = new HashMap<>();
     * owners.put(1L, 10L); // объявление 1 → продавец 10
     * owners.put(2L, 20L); // объявление 2 → продавец 20
     * owners.put(3L, 10L); // объявление 3 → продавец 10
     *
     * Map<Long, List<Long>> result = groupSellerAdvertisements(ads, owners);
     * // Результат: {10=[1, 3], 20=[2]}
     * }</pre>
     *
     * <p><b>Применение в системе:</b>
     * Метод используется в платежной системе для:
     * <ul>
     *   <li>Определения всех объявлений каждого продавца</li>
     *   <li>Группировки платежей по продавцам</li>
     *   <li>Формирования отчетности по продажам</li>
     * </ul>
     */
    public Map<Long, List<Long>> groupSellerAdvertisements(Map<Long, BigDecimal> advertisements,
                                                           Map<Long, Long> advertisementToOwnerMap) {
        Map<Long, List<Long>> sellerAdvertisements = new HashMap<>();

        for (Long advertisementId : advertisements.keySet()) {
            Long sellerId = advertisementToOwnerMap.get(advertisementId);

            // Проверяем есть ли уже такой продавец в мапе
            if (!sellerAdvertisements.containsKey(sellerId)) {
                // Если нет - создаем новый список
                sellerAdvertisements.put(sellerId, new ArrayList<>());
            }
            // Добавляем advertisementId в список продавца
            sellerAdvertisements.get(sellerId).add(advertisementId);
        }

        return sellerAdvertisements;
    }

    /**
     * Создает список транзакций для продавцов на основе их доходов от продаж
     * @param sellerTotalAmounts карта с суммами по продавцам (key=id продавца, value=сумма продажи)
     * @param sellerAdvertisements карта с объявлениями по продавцам (key=id продавца, value=список id объявлений)
     * @return список транзакций для всех продавцов
     */
    public List<Transaction> createSellerTransactions(Map<Long, BigDecimal> sellerTotalAmounts,
                                                      Map<Long, List<Long>> sellerAdvertisements) {
        List<Transaction> transactions = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : sellerTotalAmounts.entrySet()) {
            Long sellerId = entry.getKey();
            BigDecimal amount = entry.getValue();

            Transaction transaction = new Transaction();
            transaction.setUserId(sellerId);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.CREDIT);
            transaction.setReason("Income from advertisements: " + sellerAdvertisements.get(sellerId));
            transaction.setOperationId(UUID.randomUUID().toString());
            transaction.setCreatedAt(LocalDateTime.now());

            transactions.add(transaction);
        }

        return transactions;
    }

    /**
     * Создает список объектов User для обновления баланса продавцов
     * @param sellerTotalAmounts карта с суммами для начисления (key=id продавца, value=сумма для добавления)
     * @return список пользователей для обновления в БД
     * @throws IllegalArgumentException если продавец не найден
     */
    public List<User> createSellerUpdate(Map<Long, BigDecimal> sellerTotalAmounts){
        List<User> usersToUpdate = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : sellerTotalAmounts.entrySet()) {
            Long sellerId = entry.getKey();
            BigDecimal amountToAdd = entry.getValue();
            // Получаем аккаунт продавца (может выбросить IllegalArgumentException)
            User sellerAccount = getSellerAccount(sellerId);
            // Обновляем баланс
            sellerAccount.setBalance(sellerAccount.getBalance().add(amountToAdd));
            // Добавляем в список для обновления
            usersToUpdate.add(sellerAccount);
        }
        return usersToUpdate;
    }
    @Transactional(readOnly = true)
    private User getSellerAccount(Long sellerId) {
        return userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт продавца не найден ID: "  + sellerId));
    }
}
