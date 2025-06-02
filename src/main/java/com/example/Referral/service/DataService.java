package com.example.Referral.service;

import com.example.Referral.DTO.ParentChainDTO;
import com.example.Referral.DTO.ReferralLevelCount;
import com.example.Referral.model.UserNode;
import com.example.Referral.repository.ReferralRepository;
import org.apache.kafka.shaded.com.google.protobuf.ServiceException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;  // Добавьте этот импорт
import java.util.stream.Collectors;

/*
  Регистрирует нового пользователя в системе
  @param userId ID пользователя (обязательно)
  @param invitedByCode реферальный код (опционально)
  @throws ReferralCodeNotFoundException если код недействителен
 */

//TODO
// Рассмотреть кеширование для часто запрашиваемых данных
// Добавить метрики выполнения методов
// Реализовать пагинацию для методов возвращающих списки

@Service
public class DataService {
    private final ReferralRepository referralRepository;

    // Конструктор для внедрения зависимости
    public DataService(ReferralRepository ReferralRepository) {
        this.referralRepository = ReferralRepository;
    }

    // Регистрация нового пользователя
    @Transactional
    public void registerUser(Long userId, String invitedByCode) {
        System.out.println("-------------------registerUser_func-----------------");

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Создаем нового пользователя
        UserNode newUser = new UserNode(userId);
        System.out.println(newUser + " Код пригласившего: " + invitedByCode);

        //если есть код реферала, то нода создается со связью
        if (invitedByCode != null) {
            UserNode referrer = referralRepository.findUserByReferralCode(invitedByCode);

            // если реферала с таким кодом нет, то реф не найден
            if (referrer == null) {
                //TODO: если реферер не найден то пользователь должен ввести код заново
                System.out.println("Реферер не найден: " + invitedByCode);
                referralRepository.saveWithReferral(
                        newUser.getUserId(),
                        newUser.getReferralCode(),
                        invitedByCode);

            }else {
                //добавляем ноду со связью
                referralRepository.saveWithReferral(
                        newUser.getUserId(),
                        newUser.getReferralCode(),
                        invitedByCode);
            }
        }
        else {
            // добавляем нового юзера без связи
            referralRepository.saveWithoutReferral(
                    newUser.getUserId(),
                    newUser.getReferralCode());
        }
    }

    //получение всех родителей для определенного юзера
    @Transactional(readOnly = true)
    public List<UserNode> getParentsForUserByUID(Long userId){
        System.out.println("---getParentsForUserByUID_func---");
        List<UserNode> UserList = referralRepository.getParentsForUserByUID(userId);
        System.out.println(UserList);

        return UserList;
    }

    //получение всех приглашенных пользователей для юзера
    @Transactional(readOnly = true)
    public List<UserNode> getAllChildrenForUserByUID(Long userId){
        System.out.println("-------------------getAllChildrenForUserByUID_func-----------------");
        List<UserNode> UserList = referralRepository.getAllChildren(userId);
        System.out.println(UserList);

        return UserList;
    }

    /**
     * Получает количество рефералов по уровням (до 15 уровней глубины)
     * @param userId ID пользователя для которого строится дерево
     * @return Map где ключ - уровень, значение - количество пользователей
     *         В случае ошибки возвращает пустую Map
     */
    @Transactional(readOnly = true)
    public Map<Integer, Integer> getReferralsCountByLevel(Long userId) {
        System.out.println("---getReferralsCountByLevel_func--- for userId: " + userId);

        if (userId == null) {
            return Collections.emptyMap();
        }

        try {
            List<ReferralLevelCount> results = referralRepository.getChildrenCountByLevel(userId);
            System.out.println("Raw query results: " + results);

            Map<Integer, Integer> resultMap = new LinkedHashMap<>();
            for (ReferralLevelCount item : results) {
                if (item.getLevel() != null && item.getCount() != null) {
                    resultMap.put(item.getLevel(), item.getCount().intValue());
                }
            }

            System.out.println("Processed results: " + resultMap);
            return resultMap;

        } catch (Exception e) {
            System.err.println("Error in getReferralsCountByLevel for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * Возвращает цепочку родителей (предков) указанного пользователя до 15 уровней вверх,
     * с указанием их userId и уровня в реферальной иерархии.
     *
     * <p><b>Пример работы:</b></p>
     * Для цепочки {@code A -> B -> C -> D} и вызова метода для {@code userId = D}:
     * <pre>
     * [
     *   {"userId": C, "level": 1},
     *   {"userId": B, "level": 2},
     *   {"userId": A, "level": 3}
     * ]
     * </pre>
     *
     * @param userId ID пользователя, для которого строится цепочка предков (не может быть {@code null}).
     * @return Список карт (Map), где каждая карта содержит:
     *         <ul>
     *           <li>{@code "userId"} (Long) — ID родителя.</li>
     *           <li>{@code "level"} (Integer) — уровень родителя (1 — ближайший, 2 — дед и т.д.).</li>
     *         </ul>
     *         Если {@code userId == null} или произошла ошибка, возвращает {@code Collections.emptyList()}.
     *
     * @throws RuntimeException В случае ошибки выполнения запроса к БД (ошибка логируется, но не пробрасывается).
     *
     * @implSpec Алгоритм:
     * 1. Выполняет Cypher-запрос к Neo4j, находя всех предков пользователя
     *    по связи {@code REFERRED_BY} (до 15 уровней в глубину).
     * 2. Для каждого предка вычисляет уровень как длину пути от исходного пользователя.
     * 3. Сортирует результаты по возрастанию уровня (от ближайшего родителя к самому дальнему).
     * 4. Преобразует результаты в {@code List<Map<String, Object>>} для удобства использования.
     *
     * @see ReferralRepository#getParentChainWithLevels(Long)
     */
    @Transactional(readOnly = true)
    public List<ParentChainDTO> getParentChainWithLevels(Long userId) throws ServiceException {
        System.out.println("---getParentChainWithLevels_func--- for userId: " + userId);


        if (userId == null) {
            System.out.println("Attempt to get parent chain with null user ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            return referralRepository.getParentChainWithLevels(userId);
        } catch (DataAccessException e) {
            System.out.println("Database access error for user " + userId +" "+ e);
            throw new ServiceException("Failed to fetch parent chain ", e);
        }
    }
}
