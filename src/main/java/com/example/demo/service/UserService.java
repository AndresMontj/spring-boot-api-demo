package com.example.demo.service;

import com.example.demo.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);
}