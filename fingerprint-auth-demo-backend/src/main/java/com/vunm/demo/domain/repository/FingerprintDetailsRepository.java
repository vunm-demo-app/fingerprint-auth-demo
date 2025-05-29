package com.vunm.demo.domain.repository;

import com.vunm.demo.domain.model.FingerprintDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FingerprintDetailsRepository extends JpaRepository<FingerprintDetails, String> {
    
    @Query("SELECT f FROM FingerprintDetails f WHERE " +
           "f.userAgent = :userAgent AND " +
           "f.platform = :platform AND " +
           "f.screenResolution = :screenResolution AND " +
           "f.timezone = :timezone AND " +
           "f.language = :language AND " +
           "f.fingerprint <> :fingerprint")
    List<FingerprintDetails> findSimilarFingerprints(
        @Param("fingerprint") String fingerprint,
        @Param("userAgent") String userAgent,
        @Param("platform") String platform,
        @Param("screenResolution") String screenResolution,
        @Param("timezone") String timezone,
        @Param("language") String language
    );
    
    @Query("SELECT f FROM FingerprintDetails f WHERE " +
           "f.canvas = :canvas OR " +
           "f.audio = :audio")
    List<FingerprintDetails> findByCanvasOrAudioFingerprint(
        @Param("canvas") String canvas,
        @Param("audio") String audio
    );
    
    Optional<FingerprintDetails> findByFingerprint(String fingerprint);
} 