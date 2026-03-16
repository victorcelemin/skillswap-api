package com.skillswap.mapper;

import com.skillswap.dto.response.SkillResponse;
import com.skillswap.entity.Skill;
import org.mapstruct.Mapper;

@Mapper
public interface SkillMapper {

    SkillResponse toResponse(Skill skill);
}
