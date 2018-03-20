package teammates.test.cases.browsertests;

import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.test.pageobjects.InstructorFeedbackEditPage;
import teammates.test.pageobjects.InstructorFeedbackResultsPage;

/**
 * Base class for all Feedback*QuestionUiTest.
 */
public abstract class FeedbackQuestionUiTest extends BaseUiTestCase {

    protected abstract void testNewQuestionFrame();

    protected abstract void testInputValidation();

    protected abstract void testCustomizeOptions();

    protected abstract void testAddQuestionAction() throws Exception;

    protected abstract void testEditQuestionAction() throws Exception;

    protected abstract void testDeleteQuestionAction();

    protected InstructorFeedbackEditPage getFeedbackEditPage(String instructorId, String courseId,
            String feedbackSessionName) {
        AppUrl feedbackPageLink = createUrl(Const.ActionURIs.INSTRUCTOR_FEEDBACK_EDIT_PAGE).withUserId(instructorId)
                .withCourseId(courseId).withSessionName(feedbackSessionName).withEnableSessionEditDetails(true);
        return loginAdminToPage(feedbackPageLink, InstructorFeedbackEditPage.class);
    }

    protected void clickAjaxLoadedPanelAndWaitForExpansion(
            InstructorFeedbackResultsPage resultsPage, String panelId, String ajaxClass) {
        resultsPage.clickElementById(panelId);
        resultsPage.waitForAjaxLoadedPanelToExpand(panelId, ajaxClass);
    }
}
