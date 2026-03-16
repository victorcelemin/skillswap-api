package com.skillswap.dto.response;

import com.skillswap.entity.Session;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Detalle de una sesión reservada")
public class SessionResponse {

    private Long id;
    private OfferResponse offer;
    private UserSummaryResponse student;
    private LocalDateTime scheduledAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private Integer creditsPaid;
    private Session.SessionStatus status;
    private String studentNotes;
    private String teacherNotes;
    private String meetingUrl;
    private ReviewResponse review;
    private LocalDateTime createdAt;
}
