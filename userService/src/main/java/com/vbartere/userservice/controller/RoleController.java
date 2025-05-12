package com.vbartere.userservice.controller;

import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "http://localhost:4200")
public class RoleController {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Map<String, String> roleDto) {
        String roleName = roleDto.get("name");
        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
        return ResponseEntity.ok(role);
    }
}

