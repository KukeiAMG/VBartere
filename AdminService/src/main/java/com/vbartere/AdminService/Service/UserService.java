package com.vbartere.AdminService.Service;

import com.vbartere.AdminService.Model.User;
import com.vbartere.AdminService.Model.UserDTO;
import com.vbartere.AdminService.Repository.UserRepository;
import com.vbartere.AdminService.Repository.UserServiceFeignClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserServiceFeignClient userServiceFeignClient;
    private final UserRepository userRepository;

    public UserService(UserServiceFeignClient userServiceFeignClient, UserRepository userRepository) {
        this.userServiceFeignClient = userServiceFeignClient;
        this.userRepository = userRepository;
    }

    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> fetchAndSaveUsersFromUserService() {
        List<UserDTO> userDTOList = userServiceFeignClient.getAllUsers();
        List<User> userList = new ArrayList<>();
        for (UserDTO user : userDTOList) {
            User userEntity = new User(
                    user.getId(),
                    user.getPhoneNumber(),
                    user.getPassword(),
                    user.getName(),
                    user.getSurname(),
                    user.getRoles(),
                    user.isBanned()
            );
            userList.add(userEntity);
        }
        return userRepository.saveAll(userList);
    }

    public List<User> getAllUsersFromDB() {
        return userRepository.findAll();
    }

    public void banUser(Long userId, boolean banStatus) {
        User user = getUserById(userId);
        if (user != null) {
            if (user.isBanned()) {
                System.out.println("Вы не можете повторно заблокировать пользователя");
            }
            else {
                user.setBanned(banStatus);
                userRepository.save(user);

                userServiceFeignClient.updateBanStatus(userId, banStatus);
            }
        }
    }
}
