package com.example.payment.controller;

import com.example.payment.DTO.BalanceDTO;
import com.example.payment.DTO.HistoryDTO;
import com.example.payment.service.BonusService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST-контроллер для управления бонусными счетами пользователей.
 * Предоставляет методы для получения баланса, и истории операций.
 */
@RestController
@RequestMapping("/api/bonus")
public class BonusController {
    private final BonusService bonusService;

    public BonusController(BonusService bonusService) {
        this.bonusService = bonusService;
    }

    /**
     * Получает текущий баланс бонусов пользователя.
     *
     * @param userId Long пользователя, для которого запрашивается баланс
     * @return ResponseEntity<BalanceDTO> - ответ, содержащий:
     *         - userId: идентификатор пользователя
     *         - balance: текущий баланс бонусов
     *         - currency: тип валюты (всегда "BONUS")
     * Алгоритм:
     * 1. Принимает Long пользователя из пути запроса
     * 2. Передает запрос в BonusService для получения баланса
     * 3. Возвращает результат в виде BalanceDTO, обернутый в ResponseEntity
     *
     * @throws IllegalArgumentException если пользователь не найден
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceDTO> getBalance(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(bonusService.getBalance(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Получает историю операций пользователя.
     *
     * @param userId Long пользователя (path variable)
     * @param from (optional) дата, начиная с которой возвращать операции
     * @return ResponseEntity<List<HistoryDTO>> - список операций пользователя
     *
     * Алгоритм:
     * 1. Передает запрос в BonusService
     * 2. Возвращает список операций
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<HistoryDTO>> getHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        try {
            return ResponseEntity.ok(bonusService.getHistory(userId, from));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 