package com.skillswap.controller;

import com.skillswap.dto.response.ApiResponse;
import com.skillswap.dto.response.SkillResponse;
import com.skillswap.entity.Skill;
import com.skillswap.mapper.SkillMapper;
import com.skillswap.repository.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller del catálogo de habilidades.
 * Endpoints públicos — no requieren autenticación.
 */
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
@Tag(name = "Habilidades", description = "Catalogo de habilidades disponibles en la plataforma")
public class SkillController {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @GetMapping
    @Operation(summary = "Listar todas las habilidades activas")
    public ResponseEntity<ApiResponse<Page<SkillResponse>>> getAllSkills(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<SkillResponse> skills = skillRepository.findByIsActiveTrue(pageable)
            .map(skillMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.ok(skills));
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar todas las categorías de habilidades")
    public ResponseEntity<ApiResponse<Skill.SkillCategory[]>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(Skill.SkillCategory.values()));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Listar habilidades por categoría")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getByCategory(
        @PathVariable Skill.SkillCategory category
    ) {
        List<SkillResponse> skills = skillRepository.findByCategory(category)
            .stream()
            .map(skillMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(skills));
    }
}
