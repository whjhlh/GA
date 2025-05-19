package com.whj.generate.biz.Infrastructure;

import com.whj.generate.core.domain.Nature;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author whj
 * @date 2025-05-19 下午12:28
 */
@Component
public class SessionManager {
    private final Map<String, Nature> sessions = new ConcurrentHashMap<>();

    public String createSession(Nature nature) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, nature);
        return sessionId;
    }

    public Optional<Nature> getNature(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @PreDestroy
    public void cleanUp() {
        sessions.clear();
    }
}
