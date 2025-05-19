package com.whj.generate.common.convert;

import com.whj.generate.common.dto.ChromosomeDTO;
import com.whj.generate.common.req.EvolveRequest;
import com.whj.generate.common.req.InitResponse;
import com.whj.generate.common.response.EvolveResponse;
import com.whj.generate.common.response.PopulationResponse;
import com.whj.generate.core.domain.Chromosome;
import com.whj.generate.core.domain.Population;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author whj
 * @date 2025-05-06 上午1:54
 */
public class ChromosomeConvertor {

    public static InitResponse getInitResponse(String sessionId, Population initialPop) {
        return new InitResponse(
                sessionId,
                0,
                initialPop.getCurrentCoverage(),
                initialPop.getChromosomeSet().size()
        );
    }

    public static EvolveResponse getEvolveResponse(EvolveRequest request, int genIndex, Population nextPop, boolean finished) {
        return new EvolveResponse(
                request.getSessionId(),
                genIndex + 1,
                nextPop.getCurrentCoverage(),
                nextPop.getChromosomeSet().size(),
                finished
        );
    }

    public static PopulationResponse getPopulationResponse(String sessionId, int generationIndex, Population pop,Map<Chromosome, Integer> chromosomeSequenceMap) {
        List<ChromosomeDTO> list = pop.getChromosomeSet().stream()
                .map(c -> getChromosomeDTO(chromosomeSequenceMap, c))
                .collect(Collectors.toList());

        return getPopulationResponse(sessionId, generationIndex, pop, list);
    }

    private static PopulationResponse getPopulationResponse(String sessionId, int generationIndex, Population pop, List<ChromosomeDTO> list) {
        return new PopulationResponse(
                sessionId,
                generationIndex,
                pop.getCurrentCoverage(),
                pop.getChromosomeSet().size(),
                list
        );
    }

    private static ChromosomeDTO getChromosomeDTO(Map<Chromosome, Integer> chromosomeSequenceMap, Chromosome c) {
        return new ChromosomeDTO(
                chromosomeSequenceMap.get(c).toString(),
                Arrays.toString(c.getGenes()),
                c.getCoveragePercent(),
                c.getFitness(),
                c.getGenes()
        );
    }
}
