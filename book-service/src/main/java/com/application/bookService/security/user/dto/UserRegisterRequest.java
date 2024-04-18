package com.application.bookService.security.user.dto;

import com.application.bookService.security.user.UserEntity;
import java.util.Set;

public record UserRegisterRequest(String username, String password, Set<UserEntity.Role> roles) {}
