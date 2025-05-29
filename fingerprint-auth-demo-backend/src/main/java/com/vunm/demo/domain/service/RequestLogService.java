package com.vunm.demo.domain.service;

import com.vunm.demo.domain.model.RequestLog;
import com.vunm.demo.domain.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestLogService {
    private final RequestLogRepository requestLogRepository;

    public void logRequest(RequestLog log) {
        requestLogRepository.save(log);
    }

    public Map<String, Object> getStatistics(Instant from, Instant to) {
        long totalRequests = requestLogRepository.countByTimeRange(from, to);
        long botAttempts = requestLogRepository.countBotAttempts(from, to);
        long failedRequests = requestLogRepository.countFailedRequests(from, to);

        List<String> uniqueIps = requestLogRepository.findDistinctIpAddresses(from, to);
        List<String> uniqueFingerprints = requestLogRepository.findDistinctFingerprints(from, to);

        return Map.of(
            "totalRequests", totalRequests,
            "uniqueIps", uniqueIps.size(),
            "uniqueFingerprints", uniqueFingerprints.size(),
            "botAttempts", botAttempts,
            "failedRequests", failedRequests
        );
    }

    public Page<RequestLog> getRequestLogs(
            Instant from, 
            Instant to, 
            String fingerprint,
            String ipAddress,
            Boolean isSuspectedBot,
            Pageable pageable) {
        
        List<RequestLog> logs = requestLogRepository.findByFilters(
            from, to, fingerprint, ipAddress, isSuspectedBot
        );
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), logs.size());
        
        return new PageImpl<>(
            logs.subList(start, end),
            pageable,
            logs.size()
        );
    }

    public List<Map<String, Object>> getIpFingerprintCorrelation() {
        return requestLogRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                RequestLog::getFingerprint,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    logs -> Map.of(
                        "fingerprint", logs.get(0).getFingerprint(),
                        "ips", logs.stream()
                            .map(RequestLog::getIpAddress)
                            .distinct()
                            .collect(Collectors.toList()),
                        "requestCount", logs.size(),
                        "ipCount", logs.stream()
                            .map(RequestLog::getIpAddress)
                            .distinct()
                            .count()
                    )
                )
            ))
            .values()
            .stream()
            .toList();
    }
} 