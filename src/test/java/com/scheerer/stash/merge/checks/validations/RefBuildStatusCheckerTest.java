package com.scheerer.stash.merge.checks.validations;

import com.atlassian.stash.build.BuildStats;
import com.atlassian.stash.build.BuildStatusService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RefBuildStatusCheckerTest {

    @Mock
    private BuildStatusService buildStatusService;
    @Mock
    private BuildStats buildStats;

    private RefBuildStatusChecker refBuildStatusChecker;

    @Before
    public void setup() {
        when(buildStatusService.getStats(anyString())).thenReturn(buildStats);
        refBuildStatusChecker = new RefBuildStatusChecker(buildStatusService, "FOO-CHANGESET-ID");
    }

    @Test
    public void testWithoutBuilds() {
        when(buildStats.getFailedCount()).thenReturn(0);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(0);

        MergeCheckValidationResult result = refBuildStatusChecker.validate();
        assertEquals(MergeCheckValidationResult.Status.INVALID, result.getStatus());
        assertTrue(result.getMessage().contains("No builds"));
    }

    @Test
    public void testWithAFailedBuild() {
        when(buildStats.getFailedCount()).thenReturn(1);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(0);

        MergeCheckValidationResult result = refBuildStatusChecker.validate();
        assertEquals(MergeCheckValidationResult.Status.INVALID, result.getStatus());
        assertTrue(result.getMessage().contains("not successful"));
    }

    @Test
    public void testWithASuccessfulBuild() {
        when(buildStats.getFailedCount()).thenReturn(0);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(1);

        MergeCheckValidationResult result = refBuildStatusChecker.validate();
        assertEquals(MergeCheckValidationResult.Status.VALID, result.getStatus());
    }

    @Test
    public void testWithOneFailedAndOneSuccessfulBuild() {
        when(buildStats.getFailedCount()).thenReturn(1);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(1);

        MergeCheckValidationResult result = refBuildStatusChecker.validate();
        assertEquals(MergeCheckValidationResult.Status.INVALID, result.getStatus());
        assertTrue(result.getMessage().contains("not successful"));
    }

    @Test
    public void testWithAllTypesOfBuilds() {
        when(buildStats.getFailedCount()).thenReturn(1);
        when(buildStats.getInProgressCount()).thenReturn(1);
        when(buildStats.getSuccessfulCount()).thenReturn(1);

        MergeCheckValidationResult result = refBuildStatusChecker.validate();
        assertEquals(MergeCheckValidationResult.Status.INVALID, result.getStatus());
        assertTrue(result.getMessage().contains("not successful"));
    }
}
