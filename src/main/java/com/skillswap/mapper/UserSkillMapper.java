package com.skillswap.mapper;

import com.skillswap.dto.response.UserSkillResponse;
import com.skillswap.entity.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {SkillMapper.class})
public interface UserSkillMapper {

    @Mapping(target = "skill", source = "skill")
    UserSkillResponse toResponse(UserSkill userSkill);
}
