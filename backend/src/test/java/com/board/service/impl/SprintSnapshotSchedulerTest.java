package com.board.service.impl;

import com.board.service.MetricsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for SprintSnapshotScheduler.
 */
@ExtendWith(MockitoExtension.class)
class SprintSnapshotSchedulerTest {

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private SprintSnapshotScheduler scheduler;

    @Test
    @DisplayName("captureDailySnapshots should call metrics service")
    void captureDailySnapshotsShouldCallMetricsService() {
        scheduler.captureDailySnapshots();

        verify(metricsService).captureDailySnapshots();
    }
}
