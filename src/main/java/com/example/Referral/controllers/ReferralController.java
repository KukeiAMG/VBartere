package com.example.Referral.controllers;


import com.example.Referral.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/referrals")
public class ReferralController {

    private final DataService dataService;

    ReferralController(DataService dataService) {
        this.dataService = dataService;
    }


    @GetMapping("/getChildren/{id}")
    public ResponseEntity<?> getChildren(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            Map<Integer, Integer> referralMap = dataService.getReferralsCountByLevel(id);

            if (referralMap.isEmpty()) {
                return ResponseEntity.ok("No referrals found for user with ID: " + id);
            }

            System.out.println(referralMap);
            // Форматируем вывод в более читаемом виде
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("Referral structure for user ID ").append(id).append(":\n");

            referralMap.forEach((level, count) ->
                    responseBuilder.append("Level ")
                            .append(level)
                            .append(": ")
                            .append(count)
                            .append(count == 1 ? " user" : " users")
                            .append("\n")
            );

            return ResponseEntity.ok(responseBuilder.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing request: " + e.getMessage());
        }
    }



    // отображение реферального кода пользователя можно сделать без обращения к бд,
    // тк он строится на основе userId
}
