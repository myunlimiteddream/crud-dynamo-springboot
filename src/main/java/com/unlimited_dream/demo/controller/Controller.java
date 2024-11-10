package com.unlimited_dream.demo.controller;

import com.unlimited_dream.demo.dto.UserDto;
import com.unlimited_dream.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class Controller {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {

        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> createUser(@PathVariable String id, @RequestBody UserDto userDto) {

        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("scan")
    public ResponseEntity<?> getListUser() {
        return ResponseEntity.ok(userService.getListUser());
    }

    @GetMapping("query")
    public ResponseEntity<?> queryUser(@RequestParam String name, @RequestParam String lastName) {
        return ResponseEntity.ok(userService.queryUser(name, lastName));
    }
}