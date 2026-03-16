package com.skillswap.controller;

import com.skillswap.dto.request.CreateReviewRequest;
import com.skillswap.dto.response.ApiResponse;
import com.skillswap.dto.response.ReviewResponse;
import com.skillswap.entity.User;
import com.skillswap.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Sistema de valoraciones de sesiones completadas")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
        summary = "Crear review",
        description = "El estudiante valora una sesión completada (1-5 estrellas)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
        @Valid @RequestBody CreateReviewRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        ReviewResponse review = reviewService.createReview(request, currentUser);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(review, "Review publicada exitosamente!"));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Ver reviews de un teacher (público)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getTeacherReviews(
        @PathVariable Long teacherId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(
            reviewService.getTeacherReviews(teacherId, pageable)
        ));
    }

    @GetMapping("/my")
    @Operation(
        summary = "Mis reviews como estudiante",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(
            reviewService.getMyReviews(currentUser, pageable)
        ));
    }
}
