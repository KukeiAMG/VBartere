package com.example.Referral.service;

import com.example.Referral.model.UserNode;
import com.example.Referral.repository.ReferralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // Создаем нового пользователя
        UserNode newUser = new UserNode(userId);

        System.out.println(newUser + "Код пригласившего: " + invitedByCode);

        //если есть код реферала, то нода создается со связью
        if (invitedByCode != null) {
            UserNode referrer = referralRepository.findUserByReferralCode(invitedByCode);

            // если реферала с таким кодом нет, то реф не найден
            if (referrer == null) {
                //TODO: если реферер не найден то пользователь должен ввести код заново
                System.out.println("Реферер не найден: " + invitedByCode);
            }else {
                //добавляем ноду со связью
                referralRepository.saveWithReferral(newUser.getUserId(), newUser.getReferralCode(), invitedByCode);
            }
        }
        else {
            // добавляем нового юзера без связи
            referralRepository.saveWithoutReferral(newUser.getUserId(), newUser.getReferralCode());
        }
    }

    //получение всех родителей для определенного юзера
    @Transactional(readOnly = true)
    public List<UserNode> getParentsForUserByUID(Long userId){
        System.out.println("-------------------getParentsForUserByUID_func-----------------");
        List<UserNode> UserList = referralRepository.getParentsForUserByUID(userId);
        System.out.println(UserList);

        return UserList;
    }

    //получение всех приглашенных пользователей для юзера
    @Transactional(readOnly = true)
    public List<UserNode> getAllChildrenForUserByUID(Long userId){
        System.out.println("-------------------getFullTreeForUserByUID_func-----------------");
        List<UserNode> UserList = referralRepository.getAllChildren(userId);
        System.out.println(UserList);

        return UserList;
    }


}
