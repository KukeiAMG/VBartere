package com.vbartere.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.UserReferralDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.model.Cart;
import com.vbartere.userservice.model.Image;
import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.model.User;
import com.vbartere.userservice.repository.CartRepository;
import com.vbartere.userservice.repository.RoleRepository;
import com.vbartere.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService, RoleRepository roleRepository,
                       CartRepository cartRepository, KafkaTemplate<String, String> kafkaTemplate,
                       PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("пользователь не найден в БД")
        );
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public String loginUser(String phoneNumber, String password) throws JsonProcessingException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("wrong phone number or password"));

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_LOGIN
        );

        kafkaTemplate.send("user-event", objectMapper.writeValueAsString(userEvent));

        // Генерация JWT токена
        return jwtService.generateToken(user.getPhoneNumber());
    }

    @Transactional
    public User registerUser(String phoneNumber, String password, String email, String invitedByCode) throws JsonProcessingException {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("user already exists");
        }

        User user = new User(
                phoneNumber,
                email,
                passwordEncoder.encode(password),
                invitedByCode);

        Cart cart = new Cart();
        cart.setUserId(user.getId());
        cart.setAdvertisementList(new ArrayList<>());

        userRepository.save(user);
        cartRepository.save(cart);

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_CREATED
        );

        UserReferralDTO userReferralDTO = new UserReferralDTO(
                user.getId(),
                user.getInvitedByCode()
        );

        kafkaTemplate.send("user.registration.referral", objectMapper.writeValueAsString(userReferralDTO));
        kafkaTemplate.send("user-event", objectMapper.writeValueAsString(userEvent));

        return user;
    }

    @Transactional
    public User assignRoleToUser(Long userId, String roleName) {
        User user = getById(userId);

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("role not found");
        }

        if (user.getRoles().contains(role)) {
            throw new IllegalArgumentException("user already has this role");
        }

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserDetails(Long userId, String name, String surname) throws JsonProcessingException {
        User user = getById(userId);
        user.setName(name);
        user.setSurname(surname);

        UserEvent userEvent = new UserEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                UserEventType.USER_UPDATED
        );
        kafkaTemplate.send("user-event", objectMapper.writeValueAsString(userEvent));

        return userRepository.save(user);
    }

    @Transactional
    public void uploadUserImage(Long id, MultipartFile file) throws IOException {
        User user = getById(id);

        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        image.setPreviewImage(true);

        user.setImage(image);
        userRepository.save(user);
    }

    @Transactional
    public void removeUserImage(Long id) {
        User user = getById(id);

        user.setImage(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Long getUserIdByPhoneNumber(String token) {
        User user = userRepository.findByPhoneNumber(jwtService.extractPhoneNumber(token))
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return user.getId();
    }

    @Transactional(readOnly = true)
    public boolean isPhoneNumberRegistered(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }
}



