package com.skillswap.dto.response;

import com.skillswap.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Perfil completo del usuario para el endpoint de perfil.
 */
@Data
@Builder
@Schema(description = "Perfil completo del usuario")
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private Integer creditsBalance;
    private Double averageRating;
    private Integer totalSessionsTaught;
    private Integer totalSessionsLearned;
    private User.Role role;
    private LocalDateTime createdAt;
    private List<UserSkillResponse> skills;
}
