package com.odin.jira.exercise;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


public class IssueCreatedResolvedListener implements InitializingBean, DisposableBean {
    private final EventPublisher eventPublisher;

    /**
     * Constructor.
     * @param eventPublisher injected {@code EventPublisher} implementation.
     */
    public IssueCreatedResolvedListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Called when the plugin has been enabled.
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // register ourselves with the EventPublisher
        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        // unregister ourselves with the EventPublisher
        eventPublisher.unregister(this);
    }

    /**
     * Receives any {@code IssueEvent}s sent by JIRA.
     * @param issueEvent the IssueEvent passed to us
     */
    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        MutableIssue issue = (MutableIssue)issueEvent.getIssue();

        /**
         * When sub-task is created and its 'Fix Version/s' field is empty, then sub-task's 'Fix Version/s' must be set to parent's 'Fix Version/s'. When sub-task is created and its 'Fix Version/s' field is not empty, then sub-task's 'Fix Version/s' must be left intact.
         * When a 'Fix Version/s' value is added to a parent task, the value must be also added to each of the parent's sub-task.
         * When a 'Fix Version/s' value is removed from a parent task, the value must be also removed from each of the parent's sub-task.
         */

        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            Issue parentIssue = ComponentAccessor.getIssueManager().getIssueObject(issue.getParentId());
            // If issue without 'Fix Version' value, update it with parent's value
            if (parentIssue != null && issue.getFixVersions().size() == 0) {
                issue.setFixVersions(parentIssue.getFixVersions());
                issueManager.updateIssue(user, issue, com.atlassian.jira.event.type.EventDispatchOption.ISSUE_UPDATED, false);
            }
        }

        if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
            if (issue.getSubTaskObjects().size() != 0) {
                // Update every sub-task with issue's 'Fix Version' value
                for (Issue item : issue.getSubTaskObjects()) {
                    MutableIssue subtaskIssue = (MutableIssue)item;
                    subtaskIssue.setFixVersions(issue.getFixVersions());
                    issueManager.updateIssue(user, subtaskIssue, com.atlassian.jira.event.type.EventDispatchOption.ISSUE_UPDATED, false);
                }
            }
        }
    }
}