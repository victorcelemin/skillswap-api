package com.skillswap.mapper;

import com.skillswap.dto.response.OfferResponse;
import com.skillswap.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mapper para Offer.
 *
 * Decisión: el campo tags se almacena como CSV en la DB ("python,backend,api")
 * y se devuelve como List<String> en el response.
 * MapStruct no puede hacer esta conversión automáticamente, por eso
 * usamos un @Mapping con expression o un método default.
 */
@Mapper(uses = {UserMapper.class, SkillMapper.class})
public interface OfferMapper {

    @Mapping(target = "tags", expression = "java(splitTags(offer.getTags()))")
    OfferResponse toResponse(Offer offer);

    default List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return Collections.emptyList();
        return Arrays.asList(tags.split(","));
    }
}
