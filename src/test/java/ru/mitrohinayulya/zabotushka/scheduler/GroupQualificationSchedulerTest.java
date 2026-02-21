package ru.mitrohinayulya.zabotushka.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.scheduler.qualification.GroupQualificationOrchestrator;
import ru.mitrohinayulya.zabotushka.scheduler.qualification.QualificationProcessStats;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupQualificationSchedulerTest {

    @Mock
    GroupQualificationOrchestrator orchestrator;

    @InjectMocks
    GroupQualificationScheduler scheduler;

    @Test
    @DisplayName("checkGroupQualifications delegates execution to orchestrator")
    void checkGroupQualifications_ShouldDelegateToOrchestrator() {
        when(orchestrator.runMonthlyCheck()).thenReturn(new QualificationProcessStats(3, 1, 1, 0));

        scheduler.checkGroupQualifications();

        verify(orchestrator, times(1)).runMonthlyCheck();
    }

    @Test
    @DisplayName("checkGroupQualifications catches exceptions and does not throw")
    void checkGroupQualifications_ShouldSwallowException_WhenOrchestratorFails() {
        doThrow(new RuntimeException("Unexpected failure")).when(orchestrator).runMonthlyCheck();

        scheduler.checkGroupQualifications();

        verify(orchestrator, times(1)).runMonthlyCheck();
    }
}
