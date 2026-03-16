package com.skillswap.mapper;

import com.skillswap.dto.response.ReviewResponse;
import com.skillswap.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UserMapper.class})
public interface ReviewMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "reviewer", source = "reviewer")
    @Mapping(target = "teacher", source = "teacher")
    ReviewResponse toResponse(Review review);
}
