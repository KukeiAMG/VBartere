package com.example.Referral.controllers;

import com.example.Referral.model.UserNode;
import com.example.Referral.service.DataService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping
public class refCon {

    private final DataService dataService;

    refCon(DataService dataService){
        this.dataService = dataService;
    }

    @GetMapping("/create")
    public ResponseEntity<String> generateTestData() {

        dataService.registerUser(1L,  null);

        for (long i = 2; i <= 10; i++) {
            dataService.registerUser(i, "ref"+(i-1));
        }

        for (long i = 11; i <= 20; i++) {
            dataService.registerUser(i, "ref"+(i-10));
        }
        return ResponseEntity.ok("Generated 10 test users");
    }

    @GetMapping("/getChildren/{id}")
    public ResponseEntity<String> getChildrenTestData(@PathVariable("id") Long id) {

        List<UserNode> UserList = dataService.getParentsForUserByUID(id);

        return ResponseEntity.ok("received Referrers for user\n" + UserList);
    }
}
