package com.whj.generate.common;

import com.whj.generate.common.config.GeneticAlgorithmConfig;
import com.whj.generate.common.dto.GeneticAlgorithmConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 配置控制器
 * @author whj
 * @date 2025-05-04 下午10:11
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    //http://localhost:8080/genetic/config.html
    @PostMapping
    public ResponseEntity<String> updateConfig(@RequestBody GeneticAlgorithmConfigDTO dto) {
        try {
            fillInGeneticAlgorithmConfig(dto);
            return ResponseEntity.ok("Configuration updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<GeneticAlgorithmConfigDTO> getCurrentConfig() {
        System.out.println("GET /api/config 被调用");
        return ResponseEntity.ok(GeneticAlgorithmConfigDTO.current());
    }

    /**
     * 更新配置
     * @param dto
     */
    private void fillInGeneticAlgorithmConfig(GeneticAlgorithmConfigDTO dto) {
        if (dto.getCrossoverRate() != null) {
            validateProbability(dto.getCrossoverRate(), "Crossover rate");
            GeneticAlgorithmConfig.CROSSOVER_RATE = dto.getCrossoverRate();
        }
        if (dto.getMutationRate() != null) {
            validateProbability(dto.getMutationRate(), "Mutation rate");
            GeneticAlgorithmConfig.MUTATION_RATE = dto.getMutationRate();
        }
        if (dto.getMaxGenerationCount() != null) {
            validatePositive(dto.getMaxGenerationCount(), "Max generation count");
            GeneticAlgorithmConfig.MAX_GENERATION_COUNT = dto.getMaxGenerationCount();
        }
        if (dto.getTargetCoverage() != null) {
            validatePositive(dto.getTargetCoverage(), "Target coverage");
            GeneticAlgorithmConfig.TARGET_COVERAGE = dto.getTargetCoverage();
        }
        if (dto.getNoveltyWeight() != null) {
            validateProbability(dto.getNoveltyWeight(), "Novelty weight");
            GeneticAlgorithmConfig.NOVELTY_WEIGHT = dto.getNoveltyWeight();
        }
        if (dto.getDiversityPenalty() != null) {
            validateProbability(dto.getDiversityPenalty(), "Diversity penalty");
            GeneticAlgorithmConfig.DIVERSITY_PENALTY = dto.getDiversityPenalty();
        }
        if (dto.getBaseWeight() != null) {
            validateProbability(dto.getBaseWeight(), "Base weight");
            GeneticAlgorithmConfig.BASE_WEIGHT = dto.getBaseWeight();
        }
    }

    private void validateProbability(double value, String fieldName) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 1");
        }
    }

    private void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}
