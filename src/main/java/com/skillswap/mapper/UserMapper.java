package com.skillswap.mapper;

import com.skillswap.dto.response.UserProfileResponse;
import com.skillswap.dto.response.UserSummaryResponse;
import com.skillswap.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para User.
 *
 * MapStruct genera el código de mapeo en compile-time.
 * El LSP puede mostrar errores de resolución de propiedades Lombok
 * antes de compilar — esto es esperado y se resuelve al compilar con mvn compile.
 */
@Mapper(uses = {UserSkillMapper.class})
public interface UserMapper {

    @Mapping(target = "skills", source = "userSkills")
    UserProfileResponse toProfileResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);
}
