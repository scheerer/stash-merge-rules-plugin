package com.scheerer.stash.pullrequest.listeners;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergeActivityEvent;
import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestAction;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.scheerer.stash.hipchat.HipChatClient;
import com.scheerer.stash.hipchat.HipChatNotification;
import com.scheerer.stash.merge.checks.PullRequestMergeRulesCheck;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeCheckBypassListener {

    public static final String REPO_HOOK_KEY = "com.scheerer:mergeRulesCheck";

    private static final Logger log = LoggerFactory.getLogger(MergeCheckBypassListener.class);

    private final RepositoryHookService repositoryHookService;

    public MergeCheckBypassListener(RepositoryHookService repositoryHookService) {
        this.repositoryHookService = repositoryHookService;
    }

    @EventListener
    public void pullRequestListener(PullRequestMergeActivityEvent pullRequestMergeActivityEvent) {
        Repository targetRepo = pullRequestMergeActivityEvent.getPullRequest().getToRef().getRepository();
        PullRequest pullRequest = pullRequestMergeActivityEvent.getPullRequest();

        RepositoryHook repositoryHook = repositoryHookService.getByKey(targetRepo, REPO_HOOK_KEY);
        if (repositoryHook.isEnabled() && pullRequestMergeActivityEvent.getActivity().getAction().equals(PullRequestAction.MERGED)) {
            Settings settings = repositoryHookService.getSettings(targetRepo, REPO_HOOK_KEY);
            String bypassKeyword = settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_BYPASS_KEYWORD);
            String apiToken = settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_HIPCHAT_API_TOKEN);
            String roomId = settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_HIPCHAT_ROOM_ID);

            if (StringUtils.containsIgnoreCase(pullRequest.getTitle(), bypassKeyword)
                    && isHipChatConfigured(settings)) {
                String message = String.format("<b>%s</b> bypassed merge checks for pull request <b>#%d</b> in <b>%s/%s</b> <br/>%s",
                        pullRequestMergeActivityEvent.getUser().getDisplayName(),
                        pullRequest.getId(),
                        targetRepo.getProject().getKey(),
                        targetRepo.getSlug(),
                        pullRequest.getTitle());

                HipChatClient client = new HipChatClient(apiToken);
                client.sendRoomNotification(new HipChatNotification(message, roomId, HipChatNotification.Color.RED));
            }
        }
    }

    protected boolean isHipChatConfigured(Settings settings) {
        String apiToken = settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_HIPCHAT_API_TOKEN);
        String roomId = settings.getString(PullRequestMergeRulesCheck.SETTING_KEY_HIPCHAT_ROOM_ID);
        return StringUtils.isNotBlank(apiToken) && StringUtils.isNotBlank(roomId);
    }
}
