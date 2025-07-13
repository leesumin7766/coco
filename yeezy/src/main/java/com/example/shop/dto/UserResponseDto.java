package com.example.shop.dto;

import com.example.shop.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private String email;
    private String name;
    //private String role; // SELLER 또는 USER

    public UserResponseDto(UserEntity user) {
        this.email = user.getEmail();
        this.name = user.getName();
        //.role = user.getRole(); // 문자열 "SELLER", "USER" 등
    }
}

