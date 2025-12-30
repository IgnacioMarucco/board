package com.board.service.impl;

import com.board.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for capturing daily sprint snapshots.
 */
@Component
@RequiredArgsConstructor
public class SprintSnapshotScheduler {

    private final MetricsService metricsService;

    @Scheduled(cron = "0 0 * * * *")
    public void captureDailySnapshots() {
        metricsService.captureDailySnapshots();
    }
}
