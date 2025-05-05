package com.example.Referral.controllers;

import com.example.Referral.model.UserNode;
import com.example.Referral.service.DataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping
public class ReferralController {

    private final DataService dataService;

    ReferralController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/getChildren/{id}")
    public ResponseEntity<String> getChildrenTestData(@PathVariable("id") Long id) {
        List<UserNode> UserList = dataService.getParentsForUserByUID(id);
        return ResponseEntity.ok("received Referrers for user\n" + UserList);
    }

    // отображение реферального кода пользователя можно сделать без обращения к бд,
    // тк он строится на основе userId
}
