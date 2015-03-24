package com.scheerer.stash.merge.checks;

import com.atlassian.stash.build.BuildStatusService;
import com.atlassian.stash.hook.repository.RepositoryMergeRequestCheck;
import com.atlassian.stash.hook.repository.RepositoryMergeRequestCheckContext;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.scheerer.stash.merge.checks.validations.MergeCheckValidationResult;
import com.scheerer.stash.merge.checks.validations.RefBuildStatusChecker;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class PullRequestMergeRulesCheck implements RepositoryMergeRequestCheck {

    public static final String SETTING_KEY_BYPASS_KEYWORD = "bypassKeyword";
    public static final String SETTING_KEY_BYPASS_KEYWORD_DEFAULT = "[FASTTRACK]";
    public static final String SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS = "requireSuccessfulBuilds";
    public static final boolean SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS_DEFAULT = false;
    public static final String SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS = "requireTargetBranchBuildsToBeSuccessful";
    public static final boolean SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS_DEFAULT = false;
//    public static final String SETTING_KEY_REQUIRED_APPROVERS = "numberOfRequiredApprovers";
//    public static final int SETTING_KEY_REQUIRED_APPROVERS_DEFAULT = 0;
    public static final String SETTING_KEY_MARKED_DONE = "mustBeMarkedDone";
    public static final boolean SETTING_KEY_MARKED_DONE_DEFAULT = false;
    public static final String SETTING_KEY_DONE_KEYWORD_DEFAULT = "[DONE]";
    public static final String SETTING_KEY_HIPCHAT_API_TOKEN = "hipChatApiToken";
    public static final String SETTING_KEY_HIPCHAT_ROOM_ID = "hipChatRoomId";

    private final BuildStatusService buildStatusService;

    public PullRequestMergeRulesCheck(BuildStatusService buildStatusService) {
        this.buildStatusService = buildStatusService;
    }

    @Override
    public void check(@Nonnull RepositoryMergeRequestCheckContext context) {
        String bypassKeyword = context.getSettings().getString(SETTING_KEY_BYPASS_KEYWORD, SETTING_KEY_BYPASS_KEYWORD_DEFAULT);
        boolean requireSuccessfulBuilds = context.getSettings().getBoolean(SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS, SETTING_KEY_REQUIRE_SUCCESSFUL_BUILDS_DEFAULT);
        boolean requireTargetBranchBuildsToBeSuccessful = context.getSettings().getBoolean(SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS, SETTING_KEY_REQUIRE_TARGET_SUCCESSFUL_BUILDS_DEFAULT);
//        int numberOfRequiredApprovers = context.getSettings().getInt(SETTING_KEY_REQUIRED_APPROVERS, SETTING_KEY_REQUIRED_APPROVERS_DEFAULT);
        boolean pullRequestMustBeMarkedDone = context.getSettings().getBoolean(SETTING_KEY_MARKED_DONE, SETTING_KEY_MARKED_DONE_DEFAULT);

        MergeRequest request = context.getMergeRequest();
        PullRequest pullRequest = request.getPullRequest();

        if (bypassKeyword != null && StringUtils.containsIgnoreCase(request.getPullRequest().getTitle(), bypassKeyword)) {
            // TODO: Figure out how to notify the team about people bypassing rules
            return;
        }

        if (requireSuccessfulBuilds) {
            String changeset = pullRequest.getFromRef().getLatestChangeset();
            RefBuildStatusChecker sourceBuildStatusChecker = new RefBuildStatusChecker(buildStatusService, changeset);
            MergeCheckValidationResult result = sourceBuildStatusChecker.validate();
            if (result.getStatus() == MergeCheckValidationResult.Status.INVALID) {
                String vetoMessage = "Source Branch: " + result.getMessage();
                request.veto(vetoMessage, vetoMessage);
            }
        }

        if (requireTargetBranchBuildsToBeSuccessful) {
            String changeset = pullRequest.getToRef().getLatestChangeset();
            RefBuildStatusChecker sourceBuildStatusChecker = new RefBuildStatusChecker(buildStatusService, changeset);
            MergeCheckValidationResult result = sourceBuildStatusChecker.validate();
            if (result.getStatus() == MergeCheckValidationResult.Status.INVALID) {
                String vetoMessage = "Target Branch: " + result.getMessage();
                request.veto(vetoMessage, vetoMessage);
            }
        }

//        if (numberOfRequiredApprovers > 0) {
//            Set<PullRequestParticipant> reviewers = pullRequest.getReviewers();
//            if (reviewers.isEmpty()) {
//                String vetoMessage = "Pull requests must have a reviewer";
//                request.veto(vetoMessage, vetoMessage);
//                return;
//            } else {
//                int approvalCount = 0;
//                for (PullRequestParticipant prp : reviewers) {
//                    if (prp.isApproved()) {
//                        approvalCount++;
//                    }
//                }
//                if (approvalCount < numberOfRequiredApprovers) {
//                    String vetoMessage = String.format("Pull requests must have at least %d approval", numberOfRequiredApprovers);
//                    request.veto(vetoMessage, vetoMessage);
//                    return;
//                }
//            }
//        }

        if (pullRequestMustBeMarkedDone && !StringUtils.containsIgnoreCase(pullRequest.getTitle(), SETTING_KEY_DONE_KEYWORD_DEFAULT)) {
            String vetoMessage = "Pull request title is not marked with " + SETTING_KEY_DONE_KEYWORD_DEFAULT;
            request.veto(vetoMessage, vetoMessage);
        }
    }
}
