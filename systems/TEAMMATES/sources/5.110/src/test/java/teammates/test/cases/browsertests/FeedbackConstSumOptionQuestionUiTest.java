package teammates.test.cases.browsertests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.util.Const;
import teammates.test.driver.BackDoor;
import teammates.test.pageobjects.InstructorFeedbackEditPage;

/**
 * SUT: {@link Const.ActionURIs#INSTRUCTOR_FEEDBACK_EDIT_PAGE},
 *      specifically for constant sum (options) questions.
 */
public class FeedbackConstSumOptionQuestionUiTest extends FeedbackQuestionUiTest {
    private InstructorFeedbackEditPage feedbackEditPage;

    private String courseId;
    private String feedbackSessionName;
    private String instructorId;

    @Override
    protected void prepareTestData() {
        testData = loadDataBundle("/FeedbackConstSumOptionQuestionUiTest.json");
        removeAndRestoreDataBundle(testData);

        instructorId = testData.accounts.get("instructor1").googleId;
        courseId = testData.courses.get("course").getId();
        feedbackSessionName = testData.feedbackSessions.get("openSession").getFeedbackSessionName();
    }

    @BeforeClass
    public void classSetup() {
        feedbackEditPage = getFeedbackEditPage(instructorId, courseId, feedbackSessionName);
    }

    @Test
    public void allTests() throws Exception {
        testEditPage();

        //TODO: move/create other ConstSumOption question related UI tests here.
        //i.e. results page, submit page.
    }

    private void testEditPage() throws Exception {
        testNewQuestionFrame();
        testInputValidation();
        testCustomizeOptions();
        testAddQuestionAction();
        testEditQuestionAction();
        testDeleteQuestionAction();
    }

    @Override
    public void testNewQuestionFrame() {
        ______TS("CONSTSUM-option: new question (frame) link");

        feedbackEditPage.clickNewQuestionButton();
        feedbackEditPage.selectNewQuestionType("CONSTSUM_OPTION");
        assertTrue(feedbackEditPage.verifyNewConstSumQuestionFormIsDisplayed());
    }

    @Override
    public void testInputValidation() {

        ______TS("empty options");

        feedbackEditPage.fillQuestionTextBoxForNewQuestion("ConstSum-option qn");
        feedbackEditPage.fillQuestionDescriptionForNewQuestion("more details");

        feedbackEditPage.fillConstSumPointsBoxForNewQuestion("");
        assertEquals("1", feedbackEditPage.getConstSumPointsBoxForNewQuestion());

        feedbackEditPage.fillConstSumPointsForEachOptionBoxForNewQuestion("");
        assertEquals("1", feedbackEditPage.getConstSumPointsForEachOptionBoxForNewQuestion());

        feedbackEditPage.clickAddQuestionButton();

        feedbackEditPage.verifyStatus("Too little options for Distribute points (among options) question. "
                                      + "Minimum number of options is: 2.");

        ______TS("remove when 1 left");

        feedbackEditPage.clickNewQuestionButton();
        feedbackEditPage.selectNewQuestionType("CONSTSUM_OPTION");
        feedbackEditPage.fillQuestionTextBoxForNewQuestion("Test const sum question");
        feedbackEditPage.fillQuestionDescriptionForNewQuestion("more details");
        assertTrue(feedbackEditPage.verifyNewConstSumQuestionFormIsDisplayed());

        feedbackEditPage.clickRemoveConstSumOptionLinkForNewQuestion(1);
        assertFalse(feedbackEditPage.isElementPresent("constSumOptionRow-1--1"));

        // TODO: Check that after deleting, the value is cleared
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-0--1"));
        feedbackEditPage.clickRemoveConstSumOptionLinkForNewQuestion(0);
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-0--1"));
        feedbackEditPage.clickAddQuestionButton();
        feedbackEditPage.verifyStatus("Too little options for Distribute points (among options) question. "
                                      + "Minimum number of options is: 2.");

        ______TS("duplicate options");

        feedbackEditPage.clickNewQuestionButton();
        feedbackEditPage.selectNewQuestionType("CONSTSUM_OPTION");
        feedbackEditPage.fillQuestionTextBoxForNewQuestion("Test duplicate options");
        feedbackEditPage.fillQuestionDescriptionForNewQuestion("more details");

        feedbackEditPage.fillConstSumOptionForNewQuestion(0, "duplicate option");
        feedbackEditPage.fillConstSumOptionForNewQuestion(1, "duplicate option");

        feedbackEditPage.clickAddQuestionButton();
        feedbackEditPage.verifyStatus(Const.FeedbackQuestion.CONST_SUM_ERROR_DUPLICATE_OPTIONS);
    }

    @Override
    public void testCustomizeOptions() {
        feedbackEditPage.clickNewQuestionButton();
        feedbackEditPage.selectNewQuestionType("CONSTSUM_OPTION");

        feedbackEditPage.fillConstSumOptionForNewQuestion(0, "Option 1");
        feedbackEditPage.fillConstSumOptionForNewQuestion(1, "Option 2");

        ______TS("CONST SUM: add option");

        assertFalse(feedbackEditPage.isElementPresent("constSumOptionRow-2--1"));
        feedbackEditPage.clickAddMoreConstSumOptionLinkForNewQuestion();
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-2--1"));

        ______TS("CONST SUM: remove option");

        feedbackEditPage.fillConstSumOptionForNewQuestion(2, "Option 3");
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-1--1"));
        feedbackEditPage.clickRemoveConstSumOptionLinkForNewQuestion(1);
        assertFalse(feedbackEditPage.isElementPresent("constSumOptionRow-1--1"));

        ______TS("CONST SUM: add option after remove");

        feedbackEditPage.clickAddMoreConstSumOptionLinkForNewQuestion();
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-3--1"));
        feedbackEditPage.clickAddMoreConstSumOptionLinkForNewQuestion();
        feedbackEditPage.fillConstSumOptionForNewQuestion(4, "Option 5");
        assertTrue(feedbackEditPage.isElementPresent("constSumOptionRow-4--1"));
    }

    @Override
    public void testAddQuestionAction() throws Exception {
        ______TS("CONST SUM: add question action success");

        feedbackEditPage.fillQuestionTextBoxForNewQuestion("const sum qn");
        feedbackEditPage.fillQuestionDescriptionForNewQuestion("more details");
        feedbackEditPage.enableOtherFeedbackPathOptionsForNewQuestion();
        feedbackEditPage.selectRecipientsToBeStudents();
        feedbackEditPage.fillConstSumPointsBoxForNewQuestion("30");
        assertNull(BackDoor.getFeedbackQuestion(courseId, feedbackSessionName, 1));
        feedbackEditPage.clickAddQuestionButton();
        feedbackEditPage.verifyStatus(Const.StatusMessages.FEEDBACK_QUESTION_ADDED);
        assertNotNull(BackDoor.getFeedbackQuestion(courseId, feedbackSessionName, 1));

        assertEquals("30", feedbackEditPage.getConstSumPointsBox(1));
        assertEquals("30", feedbackEditPage.getConstSumPointsForEachOptionBox(1));
        feedbackEditPage.verifyHtmlMainContent("/instructorFeedbackConstSumOptionQuestionAddSuccess.html");
    }

    @Override
    public void testEditQuestionAction() throws Exception {
        ______TS("CONST SUM: edit question success");

        feedbackEditPage.clickEditQuestionButton(1);
        feedbackEditPage.fillQuestionTextBox("edited const sum qn text", 1);
        feedbackEditPage.fillQuestionDescription("more details", 1);
        feedbackEditPage.selectConstSumPointsOptions("PerOption", 1);
        feedbackEditPage.fillConstSumPointsForEachOptionBox("200", 1);

        feedbackEditPage.clickSaveExistingQuestionButton(1);
        feedbackEditPage.verifyStatus(Const.StatusMessages.FEEDBACK_QUESTION_EDITED);

        assertEquals("200", feedbackEditPage.getConstSumPointsBox(1));
        assertEquals("200", feedbackEditPage.getConstSumPointsForEachOptionBox(1));

        feedbackEditPage.verifyHtmlMainContent("/instructorFeedbackConstSumOptionQuestionEditSuccess.html");

        ______TS("CONST SUM: edit question failure due to duplicate options");

        feedbackEditPage.clickEditQuestionButton(1);
        feedbackEditPage.fillConstSumOption(0, "duplicate option", 1);
        feedbackEditPage.fillConstSumOption(1, "duplicate option", 1);

        feedbackEditPage.clickSaveExistingQuestionButton(1);
        feedbackEditPage.verifyStatus(Const.FeedbackQuestion.CONST_SUM_ERROR_DUPLICATE_OPTIONS);
    }

    @Override
    public void testDeleteQuestionAction() {
        ______TS("CONSTSUM: qn delete then cancel");

        feedbackEditPage.clickDeleteQuestionLink(1);
        feedbackEditPage.waitForConfirmationModalAndClickCancel();
        assertNotNull(BackDoor.getFeedbackQuestion(courseId, feedbackSessionName, 1));

        ______TS("CONSTSUM: qn delete then accept");

        feedbackEditPage.clickDeleteQuestionLink(1);
        feedbackEditPage.waitForConfirmationModalAndClickOk();
        feedbackEditPage.verifyStatus(Const.StatusMessages.FEEDBACK_QUESTION_DELETED);
        assertNull(BackDoor.getFeedbackQuestion(courseId, feedbackSessionName, 1));
    }

}
