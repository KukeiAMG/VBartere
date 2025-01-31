package com.example.Referral.service;

import com.example.Referral.model.UserNode;
import com.example.Referral.repository.ReferralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataService {

    private final ReferralRepository ReferralRepository;

    // Конструктор для внедрения зависимости
    public DataService(ReferralRepository ReferralRepository) {
        this.ReferralRepository = ReferralRepository;
    }

    // Регистрация нового пользователя
    @Transactional
    public void registerUser(Long userId, String referrerRefId, String refId) {
        // Найти пригласившего пользователя по refId
        UserNode referrer = ReferralRepository.getUserByRefId(referrerRefId);

        // Создать нового пользователя
        UserNode newUser = new UserNode();
        newUser.setUid(userId);
        newUser.setReferrerId(String.valueOf(referrer != null ? referrer.getUserId() : null));
        newUser.setRefId(refId);

        if (referrer != null) {
            // Добавить связь с пригласившим пользователем
            referrer.getReferredUsers().add(newUser);
            ReferralRepository.save(referrer);
        } else {
            // Сохранить пользователя без связи, если нет пригласившего
            ReferralRepository.save(newUser);
        }
    }

    // Получение всех потомков для заданного пользователя
    public List<UserNode> getReferralTreeDown(Long userId) {
        return ReferralRepository.getDescs(userId);
    }

    // Получение всех предков для заданного пользователя
    public List<UserNode> getReferralTreeUp(Long userId) {
        return ReferralRepository.getAncs(userId);
    }

    // Получение полного дерева
    public List<UserNode> getFullTree() {
        return ReferralRepository.getFullTree();
    }

    // Получение пользователя по его реф. коду
    public UserNode getUserByRefId(String RefId){
        return ReferralRepository.getUserByRefId(RefId);
    }

    // Получение Предка по UID потомка
    public UserNode getAncByUidDesc (Long userId){
        return ReferralRepository.getAncByUidDesc(userId);
    }



}
