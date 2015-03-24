package com.scheerer.stash.merge.checks;

import com.atlassian.stash.build.BuildStats;
import com.atlassian.stash.build.BuildStatusService;
import com.atlassian.stash.hook.repository.RepositoryMergeRequestCheckContext;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.setting.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestMergeRulesCheckTest {

    private static final String LATEST_SOURCE_CHANGESET = "LATEST-FROM-REF-CHANGESET";
    private static final String LATEST_TARGET_CHANGESET = "LATEST-TO-REF-CHANGESET";

    @Mock
    private BuildStatusService buildStatusService;

    @Mock
    private BuildStats buildStats;
    @Mock
    private RepositoryMergeRequestCheckContext repositoryHookContext;
    @Mock
    private Settings settings;
    @Mock
    private MergeRequest mergeRequest;
    @Mock
    private PullRequest pullRequest;
    @Mock
    private PullRequestRef fromRef;
    @Mock
    private PullRequestRef toRef;



    @Before
    public void setup() {
        when(repositoryHookContext.getMergeRequest()).thenReturn(mergeRequest);
        when(repositoryHookContext.getSettings()).thenReturn(settings);
        when(mergeRequest.getPullRequest()).thenReturn(pullRequest);
        when(pullRequest.getFromRef()).thenReturn(fromRef);
        when(pullRequest.getToRef()).thenReturn(toRef);
        when(pullRequest.getTitle()).thenReturn("Pull request title");

        when(fromRef.getLatestChangeset()).thenReturn(LATEST_SOURCE_CHANGESET);
        when(toRef.getLatestChangeset()).thenReturn(LATEST_TARGET_CHANGESET);

        when(settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_BYPASS_KEYWORD, PullRequestMergeRulesCheck.SETTING_KEY_BYPASS_KEYWORD_DEFAULT)).thenReturn("[CRAMIT]");
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(false);
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(false);
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE, PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE_DEFAULT)).thenReturn(false);
    }

    @Test
    public void testBypassKeywordBypassesAllRules() {
        when(pullRequest.getTitle()).thenReturn("Title with [CRAMIT] bypass keyword");

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        // Verify merge request does not get vetoed
        verify(mergeRequest, never()).veto(anyString(), anyString());
    }

    @Test
    public void testValidatingSuccessfulSourceBuild() {
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(true);
        when(buildStatusService.getStats(LATEST_SOURCE_CHANGESET)).thenReturn(buildStats);
        when(buildStats.getFailedCount()).thenReturn(0);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(1);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest, never()).veto(anyString(), anyString());
    }

    @Test
    public void testValidatingFailedSourceBuild() {
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(true);
        when(buildStatusService.getStats(LATEST_SOURCE_CHANGESET)).thenReturn(buildStats);
        when(buildStats.getFailedCount()).thenReturn(1);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(0);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest).veto(contains("Source"), contains("Source"));
    }


    @Test
    public void testValidatingSuccessfulTargetBuild() {
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(true);
        when(buildStatusService.getStats(LATEST_TARGET_CHANGESET)).thenReturn(buildStats);
        when(buildStats.getFailedCount()).thenReturn(0);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(1);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest, never()).veto(anyString(), anyString());
    }

    @Test
    public void testValidatingFailedTargetBuild() {
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS, PullRequestMergeRulesCheck.SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS_DEFAULT)).thenReturn(true);
        when(buildStatusService.getStats(LATEST_TARGET_CHANGESET)).thenReturn(buildStats);
        when(buildStats.getFailedCount()).thenReturn(1);
        when(buildStats.getInProgressCount()).thenReturn(0);
        when(buildStats.getSuccessfulCount()).thenReturn(0);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest).veto(contains("Target"), contains("Target"));
    }

    @Test
    public void testValidatingBuildWithoutDoneKeyword() {
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE, PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE_DEFAULT)).thenReturn(true);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest).veto(contains(PullRequestMergeRulesCheck.SETTING_KEY_DONE_KEYWORD_DEFAULT), contains(
                PullRequestMergeRulesCheck.SETTING_KEY_DONE_KEYWORD_DEFAULT));
    }

    @Test
    public void testValidatingBuildWithDoneKeyword() {
        when(pullRequest.getTitle()).thenReturn("Title with [DONE] keyword");
        when(settings.getBoolean(PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE, PullRequestMergeRulesCheck.SETTING_KEY_MARKED_DONE_DEFAULT)).thenReturn(true);

        PullRequestMergeRulesCheck mergeCheck = new PullRequestMergeRulesCheck(buildStatusService);
        mergeCheck.check(repositoryHookContext);

        verify(mergeRequest, never()).veto(anyString(), anyString());
    }
}
