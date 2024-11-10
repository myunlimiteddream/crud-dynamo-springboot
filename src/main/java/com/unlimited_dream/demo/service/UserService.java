package com.unlimited_dream.demo.service;

import com.unlimited_dream.demo.dto.UserDto;
import com.unlimited_dream.demo.entity.User;
import com.unlimited_dream.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(UserDto userDto) {
        User user = new User(UUID.randomUUID(), userDto.getName(), userDto.getLastName());
        return userRepository.saveUser(user);
    }

    public User updateUser(String id, UserDto userDto) {
        User user = userRepository.getUserById(id);
        user.setName(userDto.getName());
        user.setLastName(userDto.getLastName());
        return userRepository.saveUser(user);
    }

    public User deleteUser(String id) {
        return userRepository.deleteById(id);
    }

    public List<User> getListUser() {
        return userRepository.scanAllUser();
    }

    public List<User> queryUser(String name, String lastName) {
        return userRepository.queryUser(name, lastName);
    }
}
