package org.senai.dtos;

import org.senai.model.User;

public record UserUpdateResponseDTO(User user, String accessToken, String refreshToken) {
}