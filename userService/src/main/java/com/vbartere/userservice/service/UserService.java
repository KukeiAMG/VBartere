package com.vbartere.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.DTO.UserReferralDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import com.vbartere.userservice.DTO.UserUpdateDTO;
import com.vbartere.userservice.exceptions.InvalidTokenException;
import com.vbartere.userservice.model.*;
import com.vbartere.userservice.repository.CartRepository;
import com.vbartere.userservice.repository.RefreshTokenRepository;
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

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService, RoleRepository roleRepository,
                       CartRepository cartRepository, KafkaTemplate<String, String> kafkaTemplate,
                       PasswordEncoder passwordEncoder, ObjectMapper objectMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.refreshTokenRepository = refreshTokenRepository;
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

    @Transactional
    public Map<String, String> loginUser(String phoneNumber, String password) throws JsonProcessingException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("wrong phone number or password"));

        String accessToken = jwtService.generateToken(phoneNumber);
        String refreshToken = jwtService.generateRefreshToken(phoneNumber);

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
        kafkaTemplate.send("user-event", objectMapper.writeValueAsString(userEvent));

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

        Cart cart = new Cart();
        cart.setAdvertisementList(new ArrayList<>());

        userRepository.save(user);

        cart.setUserId(user.getId());
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

        kafkaTemplate.send("user.registration.referral", objectMapper.writeValueAsString(userReferralDTO));
        kafkaTemplate.send("user.event", objectMapper.writeValueAsString(userEvent));
        kafkaTemplate.send("administration.user.event", objectMapper.writeValueAsString(adminUserDTO));

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
    public User updateUserDetails(Long id, UserUpdateDTO dto) throws JsonProcessingException {

        User user = getById(id);

        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getSurname() != null) user.setSurname(dto.getSurname());
        if (dto.getInvitedByCode() != null) user.setInvitedByCode(dto.getInvitedByCode());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getBanned() != null) user.setBanned(dto.getBanned());

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
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
        adminUserDTO.setBanned(false);
        adminUserDTO.setEvent(UserEventType.USER_UPDATED);

        kafkaTemplate.send("user-event", objectMapper.writeValueAsString(userEvent));
        kafkaTemplate.send("administration.user.event", objectMapper.writeValueAsString(adminUserDTO));

        return user;
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



