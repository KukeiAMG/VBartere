package com.vbartere.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.DTO.UserReferralDTO;
import com.vbartere.Shared.Kafka.DTO.UserService.UserDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.DTO.UserUpdateDTO;
import com.vbartere.userservice.Kafka.Producers.SendAdminRequest;
import com.vbartere.userservice.Kafka.Producers.SendNotificationRequest;
import com.vbartere.userservice.Kafka.Producers.SendReferralRequest;
import com.vbartere.userservice.Mapper.UserMapper;
import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.model.*;
import com.vbartere.userservice.repository.CartRepository;
import com.vbartere.userservice.repository.RefreshTokenRepository;
import com.vbartere.userservice.repository.RoleRepository;
import com.vbartere.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    private final SendAdminRequest sendAdminRequest;
    private final SendNotificationRequest sendNotificationRequest;
    private final SendReferralRequest sendReferralRequest;

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService, RoleRepository roleRepository,
                       CartRepository cartRepository, KafkaTemplate<String, String> kafkaTemplate,
                       PasswordEncoder passwordEncoder, ObjectMapper objectMapper, RefreshTokenRepository refreshTokenRepository, UserMapper userMapper, SendAdminRequest sendAdminRequest, SendNotificationRequest sendNotificationRequest, SendReferralRequest sendReferralRequest) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.sendAdminRequest = sendAdminRequest;
        this.sendNotificationRequest = sendNotificationRequest;
        this.sendReferralRequest = sendReferralRequest;
    }

    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("пользователь не найден в БД")
        );
        return userMapper.userToDTO(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public Map<String, String> loginUser(String phoneNumber, String password) throws JsonProcessingException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("wrong phone number or password"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String hashedToken = passwordEncoder.encode(refreshToken);

        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(hashedToken);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
        tokenEntity.setUser(user);

        refreshTokenRepository.save(tokenEntity);

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_LOGIN
        );
        kafkaTemplate.send("user.event", objectMapper.writeValueAsString(userEvent));

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }

    @Transactional
    public User registerUser(String phoneNumber, String password, String email, String invitedByCode) throws JsonProcessingException {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("user already exists");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("this email is busy");
        }

        User user = new User(
                phoneNumber,
                email,
                passwordEncoder.encode(password),
                invitedByCode
        );
        user.setBanned(false);

        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(new HashSet<>(Collections.singleton(userRole)));

        Cart cart = new Cart();
        cart.setAdvertisementList(new ArrayList<>());

        userRepository.save(user);

        cart.setUser(user);
        cartRepository.save(cart);

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_CREATED
        );

        AdminUserDTO adminUserDTO = new AdminUserDTO();
        adminUserDTO.setId(user.getId());
        adminUserDTO.setPhoneNumber(user.getPhoneNumber());
        adminUserDTO.setEmail(user.getEmail());
        adminUserDTO.setInvitedByCode(user.getInvitedByCode());
        adminUserDTO.setBanned(false);
        adminUserDTO.setEvent(UserEventType.USER_CREATED);

        UserReferralDTO userReferralDTO = new UserReferralDTO(
                user.getId(),
                user.getInvitedByCode()
        );

        sendReferralRequest.sendReferralRequest(objectMapper.writeValueAsString(userReferralDTO));
        sendNotificationRequest.sendNotificationRequest(objectMapper.writeValueAsString(userEvent));
        sendAdminRequest.sendAdminRequest(objectMapper.writeValueAsString(adminUserDTO));

        return user;
    }

    @Transactional
    public User assignRoleToUser(Long userId, String roleName) {
        User user = getUserEntityById(userId);

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("role not found");
        }

        if (user.getRoles().contains(role)) {
            throw new IllegalArgumentException("user already has this role");
        }

        user.getRoles().add(role);

        UserEvent userEvent = new UserEvent();
        userEvent.setId(userId);
        userEvent.setName(user.getName());
        userEvent.setEmail(user.getEmail());
        userEvent.setEvent(UserEventType.USER_UPDATED);
        userEvent.setDescription(
                "Вам назначена новая роль: " + role.getName()
        );

        sendNotificationRequest.updateNotificationAsync(userEvent);

        return userRepository.save(user);
    }

    @Transactional
    public UserDTO updateUserDetails(Long id, UserUpdateDTO dto) throws JsonProcessingException {

        User user = getUserEntityById(id);

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(dto.getEmail());
            if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Email уже занят другим пользователем");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            Optional<User> userByPhone = userRepository.findByPhoneNumber(dto.getPhoneNumber());
            if (userByPhone.isPresent() && !userByPhone.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Телефон уже занят другим пользователем");
            }
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getSurname() != null) user.setSurname(dto.getSurname());
        if (dto.getInvitedByCode() != null) user.setInvitedByCode(dto.getInvitedByCode());
        if (dto.getBanned() != null) user.setBanned(dto.getBanned());

        if (dto.getRoleIds() != null) {
            user.setRoles(user.getRoles());
        }

        userRepository.save(user);

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_UPDATED
        );

        AdminUserDTO adminUserDTO = new AdminUserDTO();
        adminUserDTO.setId(user.getId());
        adminUserDTO.setName(user.getName());
        adminUserDTO.setSurname(user.getSurname());
        adminUserDTO.setPhoneNumber(user.getPhoneNumber());
        adminUserDTO.setEmail(user.getEmail());
        adminUserDTO.setInvitedByCode(user.getInvitedByCode());
        adminUserDTO.setBanned(user.isBanned);
        adminUserDTO.setEvent(UserEventType.USER_UPDATED);

        sendNotificationRequest.updateNotificationAsync(userEvent);
        sendAdminRequest.updateAdminAsync(adminUserDTO);

        return userMapper.userToDTO(user);
    }

    @Transactional
    public void removeUserImage(Long id) {
        User user = getUserEntityById(id);

        Image image = user.getImage();
        if (image != null) {
            try {
                Files.deleteIfExists(Paths.get(image.getFilePath()));
            } catch (IOException e) {
                System.err.println("Ошибка удаления файла изображения: " + image.getFilePath() + ". Сообщение: " + e.getMessage());
            }

            user.setImage(null);
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public Long getUserIdByToken(String token) {
        User user = userRepository.findByPhoneNumber(jwtService.extractPhoneNumber(token))
                .orElseThrow(() -> new InvalidTokenException("Пользователь не найден"));
        return user.getId();
    }

    @Transactional(readOnly = true)
    public boolean isPhoneNumberRegistered(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    @Transactional
    public void deleteUser(Long id) throws JsonProcessingException {
        userRepository.deleteById(id);

        AdminUserDTO adminUserDTO = new AdminUserDTO();
        adminUserDTO.setId(id);
        adminUserDTO.setEvent(UserEventType.USER_DELETED);

        kafkaTemplate.send("administration.user.event", objectMapper.writeValueAsString(adminUserDTO));
    }
}



