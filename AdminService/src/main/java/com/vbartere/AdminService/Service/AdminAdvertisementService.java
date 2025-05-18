package com.vbartere.AdminService.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.AdminService.Kafka.Service.Producers.SendAdvertisementServiceEvent;
import com.vbartere.AdminService.Mapper.AdminAdvertisementMapper;
import com.vbartere.AdminService.Model.AdminAdvertisement;
import com.vbartere.AdminService.Repository.AdminAdvertisementRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminAdvertisementDTO;
import com.vbartere.Shared.Kafka.Enum.AdvertisementEventType;
import com.vbartere.Shared.Kafka.Events.AdvertisemenEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAdvertisementService {

    private final AdminAdvertisementRepository adminAdvertisementRepository;
    private final ObjectMapper objectMapper;
    private final SendAdvertisementServiceEvent sendAdvertisementServiceEvent;
    private final AdminAdvertisementMapper adminAdvertisementMapper;

    public AdminAdvertisementService(AdminAdvertisementRepository adminAdvertisementRepository, ObjectMapper objectMapper, SendAdvertisementServiceEvent sendAdvertisementServiceEvent, AdminAdvertisementMapper adminAdvertisementMapper) {
        this.adminAdvertisementRepository = adminAdvertisementRepository;
        this.objectMapper = objectMapper;
        this.sendAdvertisementServiceEvent = sendAdvertisementServiceEvent;
        this.adminAdvertisementMapper = adminAdvertisementMapper;
    }

    @Transactional(readOnly = true)
    public AdminAdvertisementDTO getById(Long id) {
        AdminAdvertisement entity = adminAdvertisementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Объявление не найдено в БД")
        );

        return adminAdvertisementMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<AdminAdvertisementDTO> getAll() {
        List<AdminAdvertisement> entities = adminAdvertisementRepository.findAll();
        List<AdminAdvertisementDTO> dtoList = new ArrayList<>();

        for (AdminAdvertisement entity : entities) {
            AdminAdvertisementDTO dto = adminAdvertisementMapper.toDTO(entity);
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Transactional
    public void banAdvertisement(Long id, String description) throws JsonProcessingException {
        AdminAdvertisement adminAdvertisement = adminAdvertisementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Объявление не найдено в БД")
        );
        adminAdvertisement.setBanStatus(true);

        AdvertisemenEvent advertisemenEvent = new AdvertisemenEvent(
               id,
               adminAdvertisement.getTitle(),
               description,
               AdvertisementEventType.ADVERTISEMENT_BANNED
        );

        sendAdvertisementServiceEvent.sendAdvertisementRequest(objectMapper.writeValueAsString(advertisemenEvent));
    }
}
