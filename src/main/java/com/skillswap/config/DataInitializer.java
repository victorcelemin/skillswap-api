package com.skillswap.config;

import com.skillswap.entity.Skill;
import com.skillswap.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inicializador de datos de catálogo.
 *
 * Decisión arquitectónica: usar CommandLineRunner para inicializar datos
 * maestros (catálogo de habilidades) al arrancar la aplicación.
 * En producción, esto se haría con Flyway/Liquibase migrations.
 *
 * Cargamos el catálogo de skills con colores y categorías para la UI.
 * Los colores hex hacen que cada categoría tenga identidad visual.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {
        if (skillRepository.count() > 0) {
            log.info("Catalogo de habilidades ya inicializado ({} skills)", skillRepository.count());
            return;
        }

        log.info("Inicializando catalogo de habilidades...");

        List<Skill> skills = List.of(
            // ── TECNOLOGÍA ──
            createSkill("Python", "Programacion con Python: desde scripts hasta data science", Skill.SkillCategory.TECHNOLOGY, "#3776AB"),
            createSkill("JavaScript", "Desarrollo web moderno con JS/ES2024", Skill.SkillCategory.TECHNOLOGY, "#F7DF1E"),
            createSkill("Java", "Programacion orientada a objetos con Java y Spring", Skill.SkillCategory.TECHNOLOGY, "#ED8B00"),
            createSkill("React", "Interfaces modernas con React y hooks", Skill.SkillCategory.TECHNOLOGY, "#61DAFB"),
            createSkill("Machine Learning", "Modelos de ML con scikit-learn y TensorFlow", Skill.SkillCategory.TECHNOLOGY, "#FF6F00"),
            createSkill("DevOps / Docker", "Contenedores, CI/CD y despliegue en la nube", Skill.SkillCategory.TECHNOLOGY, "#2496ED"),
            createSkill("SQL y Bases de Datos", "Diseño y optimizacion de bases de datos", Skill.SkillCategory.TECHNOLOGY, "#336791"),

            // ── IDIOMAS ──
            createSkill("Inglés", "Conversacion y gramatica en ingles para todos los niveles", Skill.SkillCategory.LANGUAGES, "#CF2B36"),
            createSkill("Frances", "Aprende frances con un hablante nativo", Skill.SkillCategory.LANGUAGES, "#0055A4"),
            createSkill("Aleman", "Gramatica y conversacion en aleman", Skill.SkillCategory.LANGUAGES, "#000000"),
            createSkill("Japones", "Hiragana, katakana y conversacion basica", Skill.SkillCategory.LANGUAGES, "#BC002D"),
            createSkill("Portugues", "Portugues europeo y brasileno", Skill.SkillCategory.LANGUAGES, "#009C3B"),
            createSkill("Mandarin", "Pinyin y conversacion en chino mandarin", Skill.SkillCategory.LANGUAGES, "#DE2910"),

            // ── MÚSICA ──
            createSkill("Guitarra Acustica", "Acordes, fingerpicking y teoria musical basica", Skill.SkillCategory.MUSIC, "#8B4513"),
            createSkill("Piano / Teclado", "Lectura de notas y tecnica pianistica", Skill.SkillCategory.MUSIC, "#2C3E50"),
            createSkill("Produccion Musical", "Beats, DAW y mezcla de audio", Skill.SkillCategory.MUSIC, "#9B59B6"),
            createSkill("Canto", "Tecnica vocal, impostacion y repertorio", Skill.SkillCategory.MUSIC, "#E74C3C"),
            createSkill("DJ y Mixing", "Mezcla en vivo y programacion de sets", Skill.SkillCategory.MUSIC, "#1ABC9C"),

            // ── ARTE Y DISEÑO ──
            createSkill("Ilustracion Digital", "Ilustracion con Procreate y Photoshop", Skill.SkillCategory.ART_AND_DESIGN, "#FF6584"),
            createSkill("UI/UX Design", "Diseno de interfaces con Figma", Skill.SkillCategory.ART_AND_DESIGN, "#6C63FF"),
            createSkill("Fotografia", "Composicion, iluminacion y edicion", Skill.SkillCategory.ART_AND_DESIGN, "#2ECC71"),
            createSkill("Acuarela", "Tecnicas de pintura con acuarelas", Skill.SkillCategory.ART_AND_DESIGN, "#3498DB"),

            // ── NEGOCIOS ──
            createSkill("Marketing Digital", "SEO, redes sociales y analitica web", Skill.SkillCategory.BUSINESS, "#F39C12"),
            createSkill("Excel Avanzado", "Tablas dinamicas, macros y VBA", Skill.SkillCategory.BUSINESS, "#217346"),
            createSkill("Emprendimiento", "Modelo de negocio, pitch y startup mindset", Skill.SkillCategory.BUSINESS, "#E67E22"),

            // ── CIENCIAS ──
            createSkill("Matematicas", "Algebra, calculo y estadistica", Skill.SkillCategory.SCIENCE, "#2980B9"),
            createSkill("Fisica", "Mecanica clasica y electromagnetismo", Skill.SkillCategory.SCIENCE, "#8E44AD"),
            createSkill("Quimica", "Quimica organica e inorganica", Skill.SkillCategory.SCIENCE, "#16A085"),

            // ── DEPORTES ──
            createSkill("Yoga", "Posturas, respiracion y meditacion", Skill.SkillCategory.SPORTS_AND_FITNESS, "#F1C40F"),
            createSkill("Ajedrez", "Aperturas, tactica y estrategia", Skill.SkillCategory.SPORTS_AND_FITNESS, "#34495E"),
            createSkill("Natacion", "Tecnicas de natacion y resistencia", Skill.SkillCategory.SPORTS_AND_FITNESS, "#0EA5E9"),

            // ── COCINA ──
            createSkill("Cocina Italiana", "Pastas, risottos y dolci", Skill.SkillCategory.COOKING, "#CE2B37"),
            createSkill("Reposteria", "Pasteles, galletas y decoracion", Skill.SkillCategory.COOKING, "#F97316"),
            createSkill("Cocina Vegana", "Recetas plant-based ricas y nutritivas", Skill.SkillCategory.COOKING, "#22C55E"),

            // ── DESARROLLO PERSONAL ──
            createSkill("Meditacion y Mindfulness", "Tecnicas de atencion plena", Skill.SkillCategory.PERSONAL_DEVELOPMENT, "#A78BFA"),
            createSkill("Oratoria", "Hablar en publico con seguridad y claridad", Skill.SkillCategory.PERSONAL_DEVELOPMENT, "#F472B6"),
            createSkill("Gestion del Tiempo", "Productividad y metodos de organizacion", Skill.SkillCategory.PERSONAL_DEVELOPMENT, "#34D399")
        );

        skillRepository.saveAll(skills);
        log.info("Catalogo inicializado con {} habilidades en {} categorias", skills.size(), 9);
    }

    private Skill createSkill(String name, String description, Skill.SkillCategory category, String colorHex) {
        return Skill.builder()
            .name(name)
            .description(description)
            .category(category)
            .colorHex(colorHex)
            .isActive(true)
            .totalOffers(0)
            .build();
    }
}
