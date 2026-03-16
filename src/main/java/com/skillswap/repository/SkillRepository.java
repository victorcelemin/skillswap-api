package com.skillswap.repository;

import com.skillswap.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findByCategory(Skill.SkillCategory category);

    Page<Skill> findByIsActiveTrue(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
