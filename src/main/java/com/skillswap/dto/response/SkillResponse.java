package com.skillswap.dto.response;

import com.skillswap.entity.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Información de una habilidad del catálogo")
public class SkillResponse {

    private Long id;
    private String name;
    private String description;
    private Skill.SkillCategory category;
    private String iconUrl;
    private String colorHex;
    private Integer totalOffers;
}
