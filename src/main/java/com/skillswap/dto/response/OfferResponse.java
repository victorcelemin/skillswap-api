package com.skillswap.dto.response;

import com.skillswap.entity.Offer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Detalle de una oferta de enseñanza")
public class OfferResponse {

    private Long id;
    private UserSummaryResponse teacher;
    private SkillResponse skill;
    private String title;
    private String description;
    private Integer creditsCostPerHour;
    private Integer durationMinutes;
    private Offer.Modality modality;
    private Offer.OfferStatus status;
    private Integer maxStudentsPerSession;
    private List<String> tags;
    private Integer totalSessionsCompleted;
    private Double averageRating;
    private Integer viewsCount;
    private LocalDateTime createdAt;
}
