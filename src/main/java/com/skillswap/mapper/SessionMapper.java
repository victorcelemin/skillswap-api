package com.skillswap.mapper;

import com.skillswap.dto.response.SessionResponse;
import com.skillswap.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {OfferMapper.class, UserMapper.class, ReviewMapper.class})
public interface SessionMapper {

    @Mapping(target = "student", source = "student")
    @Mapping(target = "offer", source = "offer")
    @Mapping(target = "review", source = "review")
    SessionResponse toResponse(Session session);
}
