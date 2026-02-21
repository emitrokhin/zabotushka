package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.entity.Platform;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupQualificationOrchestratorTest {

    @Mock
    Instance<PlatformQualificationProcessor> processors;

    @Mock
    PlatformQualificationProcessor telegramProcessor;

    @Mock
    PlatformQualificationProcessor maxProcessor;

    @InjectMocks
    GroupQualificationOrchestrator orchestrator;

    @Test
    @DisplayName("runMonthlyCheck aggregates all processor stats")
    void runMonthlyCheck_ShouldAggregateStats_FromAllProcessors() {
        when(telegramProcessor.platform()).thenReturn(Platform.TELEGRAM);
        when(maxProcessor.platform()).thenReturn(Platform.MAX);
        when(telegramProcessor.processQualifications()).thenReturn(new QualificationProcessStats(3, 1, 1, 0));
        when(maxProcessor.processQualifications()).thenReturn(new QualificationProcessStats(2, 2, 0, 1));
        when(processors.iterator()).thenReturn(List.of(telegramProcessor, maxProcessor).iterator());

        var result = orchestrator.runMonthlyCheck();

        assertThat(result).isEqualTo(new QualificationProcessStats(5, 3, 1, 1));
        verify(telegramProcessor).processQualifications();
        verify(maxProcessor).processQualifications();
    }

    @Test
    @DisplayName("runMonthlyCheck continues when one processor throws")
    void runMonthlyCheck_ShouldContinue_WhenProcessorFails() {
        when(telegramProcessor.platform()).thenReturn(Platform.TELEGRAM);
        when(maxProcessor.platform()).thenReturn(Platform.MAX);
        when(telegramProcessor.processQualifications()).thenThrow(new RuntimeException("boom"));
        when(maxProcessor.processQualifications()).thenReturn(new QualificationProcessStats(4, 1, 0, 0));
        when(processors.iterator()).thenReturn(List.of(telegramProcessor, maxProcessor).iterator());

        var result = orchestrator.runMonthlyCheck();

        assertThat(result).isEqualTo(new QualificationProcessStats(4, 1, 0, 1));
        verify(telegramProcessor).processQualifications();
        verify(maxProcessor).processQualifications();
    }
}
