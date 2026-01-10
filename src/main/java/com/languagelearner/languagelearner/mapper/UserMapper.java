package com.languagelearner.languagelearner.mapper;

import com.languagelearner.languagelearner.dto.UserDTO;
import com.languagelearner.languagelearner.model.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setVerified(user.isVerified());
        return dto;
    }
}
