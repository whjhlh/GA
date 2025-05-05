package com.whj.generate.common.req;

/**
 * @author whj
 * @date 2025-05-06 上午12:50
 */
public class EvolveRequest {
    private String sessionId;
    private int generationIndex;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getGenerationIndex() {
        return generationIndex;
    }

    public void setGenerationIndex(int generationIndex) {
        this.generationIndex = generationIndex;
    }
// getters & setters omitted
}
