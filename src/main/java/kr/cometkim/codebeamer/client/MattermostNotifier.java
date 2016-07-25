package kr.cometkim.codebeamer.client;

import com.intland.codebeamer.Config;
import com.intland.codebeamer.event.BaseEvent;
import com.intland.codebeamer.event.TrackerItemListener;
import com.intland.codebeamer.event.util.VetoException;
import com.intland.codebeamer.manager.util.ActionData;
import com.intland.codebeamer.persistence.dto.*;
import com.intland.codebeamer.persistence.util.TrackerItemAttachmentGroup;
import com.intland.codebeamer.utils.TemplateRenderer;
import kr.cometkim.codebeamer.util.MarkupParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.eclipse.jgit.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by comet on 2016-07-20.
 */
public class MattermostNotifier extends MattermostClient implements TrackerItemListener{
    private static final Logger logger = Logger.getLogger(MattermostNotifier.class);

    private static final TemplateRenderer templateRenderer = TemplateRenderer.getInstance();

    private static final String DEFAULT_ISSUE_CREATED_TEMPLATE = "IssueCreated.vm";
    private static final String DEFAULT_ISSUE_UPDATED_TEMPLATE = "IssueUpdated.vm";
    private static final String DEFAULT_ISSUE_COMMENTED_TEMPLATE = "IssueCommented.vm";

    @Getter @Setter
    private String trackerIds;

    @Getter @Setter
    private boolean onIssueCreated;

    @Getter @Setter
    private boolean onIssueUpdated;

    @Getter @Setter
    private boolean onIssueCommented;

    @Getter @Setter
    private String issueCreatedTemplate;

    @Getter @Setter
    private String issueUpdatedTemplate;

    @Getter @Setter
    private String issueCommentedTemplate;

    public MattermostNotifier(){
        this.onIssueCreated = true;
        this.onIssueUpdated = true;
        this.onIssueCommented = true;

        this.issueCreatedTemplate = DEFAULT_ISSUE_CREATED_TEMPLATE;
        this.issueUpdatedTemplate = DEFAULT_ISSUE_UPDATED_TEMPLATE;
        this.issueCommentedTemplate = DEFAULT_ISSUE_COMMENTED_TEMPLATE;
    }

    @Override
    public void trackerItemCreated(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {
        if(event.isPostEvent() && this.onIssueCreated){
            TrackerItemDto issue = event.getSource();

            if(StringUtils.isEmptyOrNull(trackerIds) || isTargetIssue(issue)) {
                VelocityContext context = getDefaultVelocityContext(event);
                context.put("issue", issue);

                String message = renderTemplate(issueCreatedTemplate, context, event);
                post(message);
            }
        }
    }

    @Override
    public void trackerItemUpdated(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {
        if(event.isPostEvent() && this.onIssueUpdated){
            TrackerItemDto issue = event.getSource();

            if(StringUtils.isEmptyOrNull(trackerIds) || isTargetIssue(issue)) {
                VelocityContext context = getDefaultVelocityContext(event);
                context.put("issue", event.getSource());

                TrackerItemUpdateDto itemUpdateDto = (TrackerItemUpdateDto) event.getData().getData();
                List<TrackerItemHistoryEntryDto> modifications = itemUpdateDto.getModifications();
                parseAndTruncateModifications(modifications);
                context.put("modifications", modifications);

                String message = renderTemplate(issueUpdatedTemplate, context, event);
                post(message);
            }
        }
    }

    @Override
    public void attachmentAdded(BaseEvent<TrackerItemAttachmentGroup, List<AccessPermissionDto>, ActionData> event) throws VetoException {
        if (event.isPostEvent() && this.onIssueCommented) {
            TrackerItemAttachmentGroup source = event.getSource();
            TrackerItemDto issue = source.getTrackerItem();

            if(StringUtils.isEmptyOrNull(trackerIds) || isTargetIssue(issue)) {
                VelocityContext context = getDefaultVelocityContext(event);

                context.put("issue", issue);

                String description = source.getDto().getDescription();
                String[] commentLines = description.split("\r\n");
                for (int i = 0, len = commentLines.length; i < len; i++) {
                    commentLines[i] = commentLines[i].replace("\\\\", "  ");
                }
                context.put("commentLines", Arrays.asList(commentLines));

                String message = renderTemplate(issueCommentedTemplate, context, event);
                post(message);
            }
        }
    }

    @Override
    public void trackerItemRemoved(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {}

    @Override
    public void trackerItemDeleted(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {}

    @Override
    public void trackerItemEscalated(BaseEvent<TrackerItemDto, TrackerItemEscalationScheduleDto, ActionData> event) throws VetoException {}

    @Override
    public void attachmentUpdated(BaseEvent<TrackerItemAttachmentGroup, List<AccessPermissionDto>, ActionData> event) throws VetoException {}

    @Override
    public void attachmentRemoved(BaseEvent<TrackerItemDto, List<ArtifactDto>, ActionData> event) throws VetoException {}

    protected VelocityContext getDefaultVelocityContext(BaseEvent event){
        HttpServletRequest request = event.getRequest();
        UserDto user = event.getUser();

        VelocityContext context = new VelocityContext();
        context.put("H", "#");
        context.put("event", event);
        context.put("request", request);
        context.put("baseUrl", Config.getCodeBeamerBaseUrl(true));
        context.put("user", user);

        return context;
    }

    protected boolean isTargetIssue(TrackerItemDto issue){
        TrackerDto tracker = issue.getTracker();
        for(String trackerId : trackerIds.split(",")){
            if(String.valueOf(tracker.getId()).equals(trackerId)){
                return true;
            }
        }
        return false;
    }

    protected String renderTemplate(String templateFileName, VelocityContext velocityContext, BaseEvent event){
        Locale locale = getLocaleFromEvent(event);
        String rendered = templateRenderer.renderTemplateOnPath("mattermost/" + templateFileName, velocityContext, new TemplateRenderer.Parameters(locale, false));
        return rendered;
    }

    private Locale getLocaleFromEvent(BaseEvent event){
        HttpServletRequest request = event.getRequest();
        Locale locale = request.getLocale();
        if(locale == null){
            locale = Locale.getDefault();
        }
        return locale;
    }

    private void parseAndTruncateModifications(List<TrackerItemHistoryEntryDto> modifications){
        for(int i = 0, len = modifications.size(); i < len; i++){
            TrackerItemHistoryEntryDto modification = modifications.get(i);

            String oldValue = modification.getOldValue();
            if(oldValue != null) {
                oldValue = parseModificationValue(oldValue);
                oldValue = truncateModificationValue(oldValue);
                oldValue = MarkupParser.replaceLineBreaks(oldValue, "");

                modification.setOldValue(StringEscapeUtils.unescapeHtml(oldValue));
            }

            String newValue = modification.getNewValue();
            if(newValue != null) {
                newValue = parseModificationValue(newValue);
                newValue = truncateModificationValue(newValue);

                newValue = MarkupParser.replaceLineBreaks(newValue, "");

                modification.setNewValue(StringEscapeUtils.unescapeHtml(newValue));
            }
        }
    }

    private String parseModificationValue(String value){
        String parsed = value;
        parsed = MarkupParser.replaceModificationTags(parsed, "`");
        parsed = MarkupParser.replaceHtmlTags(parsed, "");
        return parsed;
    }

    private String truncateModificationValue(String value){
        String[] separated = value.split("\r");
        return separated.length > 1 ? separated[0] + " ..." : separated[0];
    }
}
