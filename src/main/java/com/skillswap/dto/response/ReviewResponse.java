package com.skillswap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Review de una sesión completada")
public class ReviewResponse {

    private Long id;
    private Long sessionId;
    private UserSummaryResponse reviewer;
    private UserSummaryResponse teacher;
    private Integer rating;
    private String comment;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
