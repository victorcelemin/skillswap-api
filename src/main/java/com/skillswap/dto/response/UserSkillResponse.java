package com.skillswap.dto.response;

import com.skillswap.entity.UserSkill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Habilidad de un usuario con metadatos")
public class UserSkillResponse {

    private Long id;
    private SkillResponse skill;
    private UserSkill.SkillType type;
    private UserSkill.SkillLevel level;
    private Integer yearsExperience;
    private String description;
}
