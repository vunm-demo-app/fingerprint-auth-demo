package com.vunm.demo.domain.repository;

import com.vunm.demo.domain.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.timestamp BETWEEN :from AND :to")
    long countByTimeRange(@Param("from") Instant from, @Param("to") Instant to);
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.isSuspectedBot = true AND r.timestamp BETWEEN :from AND :to")
    long countBotAttempts(@Param("from") Instant from, @Param("to") Instant to);
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.isSuccess = false AND r.timestamp BETWEEN :from AND :to")
    long countFailedRequests(@Param("from") Instant from, @Param("to") Instant to);
    
    @Query("SELECT DISTINCT r.ipAddress FROM RequestLog r WHERE r.timestamp BETWEEN :from AND :to")
    List<String> findDistinctIpAddresses(@Param("from") Instant from, @Param("to") Instant to);
    
    @Query("SELECT DISTINCT r.fingerprint FROM RequestLog r WHERE r.timestamp BETWEEN :from AND :to")
    List<String> findDistinctFingerprints(@Param("from") Instant from, @Param("to") Instant to);
    
    @Query("SELECT r FROM RequestLog r WHERE " +
           "(:from IS NULL OR r.timestamp >= :from) AND " +
           "(:to IS NULL OR r.timestamp <= :to) AND " +
           "(:fingerprint IS NULL OR r.fingerprint = :fingerprint) AND " +
           "(:ipAddress IS NULL OR r.ipAddress = :ipAddress) AND " +
           "(:isSuspectedBot IS NULL OR r.isSuspectedBot = :isSuspectedBot)")
    List<RequestLog> findByFilters(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("fingerprint") String fingerprint,
        @Param("ipAddress") String ipAddress,
        @Param("isSuspectedBot") Boolean isSuspectedBot
    );
} 