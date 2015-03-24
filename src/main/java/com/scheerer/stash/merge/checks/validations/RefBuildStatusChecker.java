package com.scheerer.stash.merge.checks.validations;

import com.atlassian.stash.build.BuildStats;
import com.atlassian.stash.build.BuildStatusService;

public class RefBuildStatusChecker implements MergeCheckValidator {

    private String changeset;
    private BuildStatusService buildStatusService;

    public RefBuildStatusChecker(BuildStatusService buildStatusService, String changeset) {
        this.buildStatusService = buildStatusService;
        this.changeset = changeset;
    }

    public MergeCheckValidationResult validate() {
        BuildStats buildStats = buildStatusService.getStats(changeset);
        if (totalBuildCount(buildStats) < 1) {
            return new MergeCheckValidationResult("No builds yet, cannot merge.", MergeCheckValidationResult.Status.INVALID);
        } else if (anyUnsuccessfulBuilds(buildStats)) {
            return new MergeCheckValidationResult("All builds are not successful.", MergeCheckValidationResult.Status.INVALID);
        }
        return new MergeCheckValidationResult("All builds for are successful", MergeCheckValidationResult.Status.VALID);
    }

    protected int totalBuildCount(BuildStats buildStats) {
        return buildStats.getFailedCount() + buildStats.getInProgressCount() + buildStats.getSuccessfulCount();
    }

    protected boolean anyUnsuccessfulBuilds(BuildStats buildStats) {
        return buildStats.getFailedCount() > 0 || buildStats.getInProgressCount() > 0;
    }
}
