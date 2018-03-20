package teammates.test.cases.browsertests;

import java.util.Calendar;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.datatransfer.questions.FeedbackConstantSumResponseDetails;
import teammates.common.datatransfer.questions.FeedbackContributionResponseDetails;
import teammates.common.datatransfer.questions.FeedbackMsqResponseDetails;
import teammates.common.datatransfer.questions.FeedbackNumericalScaleResponseDetails;
import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.test.driver.BackDoor;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.AppPage;
import teammates.test.pageobjects.FeedbackSessionNotVisiblePage;
import teammates.test.pageobjects.FeedbackSubmitPage;

/**
 * SUT: {@link Const.ActionURIs#STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE}.
 *
 * <p>The first team is named "Team >'"< 1" to test cases where a HTML character exists in the team name.
 */
public class StudentFeedbackSubmitPageUiTest extends BaseUiTestCase {
    private FeedbackSubmitPage submitPage;

    @Override
    protected void prepareTestData() {
        testData = loadDataBundle("/StudentFeedbackSubmitPageUiTest.json");
        removeAndRestoreDataBundle(testData);
    }

    @Test
    public void testAll() throws Exception {
        testContent();
        testSubmitAction();
        testInputValidation();
        testLinks();
        testResponsiveSubmission();
        testModifyData();
    }

    private void testLinks() {
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Awaiting Session");
        submitPage.loadStudentHomeTab();
        submitPage = submitPage.goToPreviousPage(FeedbackSubmitPage.class);
        submitPage.loadProfileTab();

        submitPage.logout();
        submitPage = loginToStudentFeedbackSubmitPage(testData.students.get("DropOut"), "Open Session");
        submitPage.clickAndCancel(browser.driver.findElement(By.id("studentHomeNavLink")));
        submitPage.clickAndCancel(browser.driver.findElement(By.id("studentProfileNavLink")));
    }

    private void testContent() throws Exception {

        ______TS("unreg student");

        logout();

        submitPage = loginToStudentFeedbackSubmitPage(testData.students.get("DropOut"), "Open Session");

        // This is the full HTML verification for Unregistered Student Feedback Submit Page,
        // the rest can all be verifyMainHtml
        submitPage.verifyHtml("/unregisteredStudentFeedbackSubmitPageOpen.html");

        ______TS("Awaiting session");

        // this session contains questions to instructors, and since instr3 is not displayed to students,
        // student cannot submit to instr3
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Awaiting Session");

        // This is the full HTML verification for Registered Student Feedback Submit Page, the rest can all be verifyMainHtml
        submitPage.verifyHtml("/studentFeedbackSubmitPageAwaiting.html");

        ______TS("Open session");

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageOpen.html");
        submitPage.verifyVisibilityAndCloseMoreInfoAboutEqualShareModal();

        ______TS("Grace period session");

        FeedbackSessionAttributes fs = BackDoor.getFeedbackSession("SFSubmitUiT.CS2104",
                                                                   "Grace Period Session");

        Calendar endDate = TimeHelper.now(fs.getTimeZone());
        endDate.add(Calendar.MINUTE, -1);
        fs.setGracePeriod(10);
        fs.setEndTime(endDate.getTime());
        BackDoor.editFeedbackSession(fs);
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Grace Period Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageGracePeriod.html");

        ______TS("Closed session");

        // see comment for awaiting session
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Closed Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageClosed.html");

        ______TS("Empty session");

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Empty Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageEmpty.html");

        ______TS("Not yet visible session");

        FeedbackSessionNotVisiblePage fsNotVisiblePage =
                loginToStudentFeedbackSubmitPageFeedbackSessionNotVisible("Alice", "Not Yet Visible Session");
        fsNotVisiblePage.verifyHtmlMainContent("/studentFeedbackSubmitPageNotYetVisible.html");

    }

    private void testSubmitAction() throws Exception {

        ______TS("create new responses");

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.waitForPageToLoad();

        String responseText = "Test Self Feedback";
        submitPage.fillResponseRichTextEditor(1, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(1, 0));

        responseText = "Response to Benny.";
        submitPage.selectRecipient(2, 0, "Benny Charles");
        submitPage.fillResponseRichTextEditor(2, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 0));

        responseText = "Response to student who is going to drop out.";
        submitPage.selectRecipient(2, 1, "Drop out");
        submitPage.fillResponseRichTextEditor(2, 1, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 1));

        responseText = "Response to extra guy.";
        submitPage.selectRecipient(2, 2, "Extra guy");
        submitPage.fillResponseRichTextEditor(2, 2, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 2));
        submitPage.fillResponseTextBox(14, 0, "1");

        // Test partial response for question
        responseText = "Feedback to team 3";
        submitPage.fillResponseRichTextEditor(4, 1, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(4, 1));

        submitPage.verifyOtherOptionTextUnclickable(6, 0);
        submitPage.chooseMcqOption(6, 0, "");
        submitPage.waitForOtherOptionTextToBeClickable(6, 0);
        submitPage.fillMcqOtherOptionTextBox(6, 0, "Features");

        submitPage.chooseMcqOption(7, 0, "Algo");

        submitPage.verifyOtherOptionTextUnclickable(8, 0);
        submitPage.toggleMsqOption(8, 0, "");
        submitPage.waitForOtherOptionTextToBeClickable(8, 0);
        submitPage.fillMsqOtherOptionTextBox(8, 0, "Features");

        submitPage.toggleMsqOption(9, 0, "UI");
        submitPage.toggleMsqOption(9, 0, "Design");

        submitPage.fillResponseTextBox(18, 0, 0, "90");
        submitPage.fillResponseTextBox(18, 0, 1, "10");

        submitPage.chooseContribOption(20, 0, "Equal share");

        // Just check that some of the responses persisted.
        FeedbackQuestionAttributes fq = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                     "First Session", 2);
        FeedbackQuestionAttributes fqPartial = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                            "First Session", 4);
        FeedbackQuestionAttributes fqMcq = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                        "First Session", 8);
        FeedbackQuestionAttributes fqMsq = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                        "First Session", 10);
        FeedbackQuestionAttributes fqNumscale = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                             "First Session", 15);
        FeedbackQuestionAttributes fqConstSum = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                             "First Session", 19);
        FeedbackQuestionAttributes fqConstSum2 = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                              "First Session", 20);
        FeedbackQuestionAttributes fqContrib = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104",
                                                                            "First Session", 21);

        assertNull(BackDoor.getFeedbackResponse(fq.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.benny.c@gmail.tmt"));
        String aliceTeam = testData.students.get("Alice").team;
        assertNull(BackDoor.getFeedbackResponse(fqPartial.getId(),
                                                aliceTeam,
                                                "Team 3"));
        assertNull(BackDoor.getFeedbackResponse(fqMcq.getId(),
                                                aliceTeam,
                                                "Team 2"));
        assertNull(BackDoor.getFeedbackResponse(fqMsq.getId(),
                                                aliceTeam,
                                                "Team 2"));
        assertNull(BackDoor.getFeedbackResponse(fqNumscale.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqConstSum.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqContrib.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqContrib.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.benny.c@gmail.tmt"));
        submitPage.submitWithoutConfirmationEmail();

        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

        assertNotNull(BackDoor.getFeedbackResponse(fq.getId(),
                                                   "SFSubmitUiT.alice.b@gmail.tmt",
                                                   "SFSubmitUiT.benny.c@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqPartial.getId(),
                                                   aliceTeam,
                                                   "Team 3"));
        assertNotNull(BackDoor.getFeedbackResponse(fqMcq.getId(),
                                                    aliceTeam,
                                                   "Team 2"));
        assertNotNull(BackDoor.getFeedbackResponse(fqMsq.getId(),
                                                    aliceTeam,
                                                   "Team 2"));
        assertNotNull(BackDoor.getFeedbackResponse(fqNumscale.getId(),
                                                   "SFSubmitUiT.alice.b@gmail.tmt",
                                                   "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqConstSum.getId(),
                                                   "SFSubmitUiT.alice.b@gmail.tmt",
                                                   "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqContrib.getId(),
                                                   "SFSubmitUiT.alice.b@gmail.tmt",
                                                   "SFSubmitUiT.alice.b@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqContrib.getId(),
                                                "SFSubmitUiT.alice.b@gmail.tmt",
                                                "SFSubmitUiT.benny.c@gmail.tmt"));

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPagePartiallyFilled.html");

        ______TS("test toggle radio button");

        submitPage.chooseMcqOption(7, 1, "UI");
        submitPage.chooseMcqOption(7, 1, "Algo");
        submitPage.chooseMcqOption(7, 1, "Algo"); // toggle 'Algo' radio option

        submitPage.submitWithoutConfirmationEmail();

        assertNull(BackDoor.getFeedbackResponse(fqMcq.getId(), aliceTeam, "Team 3"));

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPagePartiallyFilled.html");

        ______TS("edit existing response");

        // Test editing an existing response
        // + fill up rest of responses at the same time
        String editedResponse = "Edited response to Benny.";
        submitPage.fillResponseRichTextEditor(2, 0, editedResponse);
        assertEquals(editedResponse.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 0));

        responseText = "Feedback to instructors";
        submitPage.fillResponseRichTextEditor(3, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(3, 0));

        responseText = "Feedback to team 2.";
        submitPage.fillResponseRichTextEditor(4, 1, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(4, 1));

        responseText = "Feedback to teammate.";
        submitPage.fillResponseRichTextEditor(5, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(5, 0));

        submitPage.chooseMcqOption(6, 0, "UI");
        submitPage.chooseMcqOption(7, 0, "UI"); // Changed from "Algo" to "UI"
        submitPage.chooseMcqOption(7, 1, "UI");

        submitPage.toggleMsqOption(8, 0, "UI");
        submitPage.toggleMsqOption(8, 0, "Algo");
        submitPage.toggleMsqOption(8, 0, "Design");
        submitPage.toggleMsqOption(9, 0, "UI");
        submitPage.toggleMsqOption(9, 0, "Algo");
        submitPage.toggleMsqOption(9, 0, "Design");
        submitPage.toggleMsqOption(9, 1, "Design");

        submitPage.chooseMcqOption(10, 0, "Drop out (Team 2)");
        submitPage.toggleMsqOption(11, 0, "Alice Betsy</option></td></div>'\" (Team >'\"< 1</td></div>'\")");
        submitPage.toggleMsqOption(11, 0, "Benny Charles (Team >'\"< 1</td></div>'\")");
        submitPage.toggleMsqOption(11, 0, "Charlie Davis (Team 2)");
        submitPage.toggleMsqOption(11, 0, "Extra guy (Team 2)");

        submitPage.chooseMcqOption(12, 0, "Team 2");
        submitPage.toggleMsqOption(13, 0, "Team >'\"< 1</td></div>'\"");
        submitPage.toggleMsqOption(13, 0, "Team 3");

        submitPage.fillResponseTextBox(14, 0, "5");
        submitPage.fillResponseTextBox(15, 0, "1.5");
        submitPage.fillResponseTextBox(15, 1, "2.5");

        submitPage.chooseMcqOption(16, 0, "Teammates Test2");
        submitPage.toggleMsqOption(17, 0, "Teammates Test");
        submitPage.toggleMsqOption(17, 0, "Teammates Test3");

        submitPage.fillResponseTextBox(18, 0, 0, "70");
        submitPage.fillResponseTextBox(18, 0, 1, "30");

        submitPage.fillResponseTextBox(19, 0, 0, "90");
        submitPage.fillResponseTextBox(19, 1, 0, "110");

        submitPage.chooseContribOption(20, 1, "0%");

        // Just check the edited responses, and two new response.
        assertNull(BackDoor.getFeedbackResponse(fqPartial.getId(),
                                                aliceTeam,
                                                "Team 2"));
        assertNull(BackDoor.getFeedbackResponse(fqConstSum2.getId(),
                                                aliceTeam,
                                                "Team 2"));
        assertNull(BackDoor.getFeedbackResponse(fqConstSum2.getId(),
                                                aliceTeam,
                                                "Team 3"));

        submitPage.submitWithoutConfirmationEmail();

        //check new response
        fqPartial = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104", "First Session", 4);
        assertNotNull(BackDoor.getFeedbackResponse(fqPartial.getId(),
                                                   aliceTeam,
                                                   "Team 2"));

        //check edited
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);
        assertEquals("<p>" + editedResponse + "</p>",
                     BackDoor.getFeedbackResponse(fq.getId(), "SFSubmitUiT.alice.b@gmail.tmt",
                                                  "SFSubmitUiT.benny.c@gmail.tmt").responseMetaData.getValue());

        assertEquals("UI", BackDoor.getFeedbackResponse(fqMcq.getId(),
                                                        aliceTeam,
                                                        "Team 2").getResponseDetails().getAnswerString());

        FeedbackMsqResponseDetails frMsq =
                (FeedbackMsqResponseDetails) BackDoor.getFeedbackResponse(fqMsq.getId(),
                                                                          aliceTeam,
                                                                          "Team 2").getResponseDetails();
        assertFalse(frMsq.contains("UI"));
        assertTrue(frMsq.contains("Algo"));
        assertFalse(frMsq.contains("Design"));

        FeedbackNumericalScaleResponseDetails frNumscale = (FeedbackNumericalScaleResponseDetails)
                BackDoor.getFeedbackResponse(fqNumscale.getId(), "SFSubmitUiT.alice.b@gmail.tmt",
                                            "SFSubmitUiT.alice.b@gmail.tmt").getResponseDetails();
        assertEquals("5", frNumscale.getAnswerString());

        FeedbackConstantSumResponseDetails frConstSum = (FeedbackConstantSumResponseDetails)
                BackDoor.getFeedbackResponse(fqConstSum.getId(), "SFSubmitUiT.alice.b@gmail.tmt",
                                             "SFSubmitUiT.alice.b@gmail.tmt").getResponseDetails();
        assertEquals("70, 30", frConstSum.getAnswerString());

        FeedbackConstantSumResponseDetails frConstSum1 = (FeedbackConstantSumResponseDetails)
                BackDoor.getFeedbackResponse(fqConstSum2.getId(),
                                             aliceTeam,
                                             "Team 2").getResponseDetails();
        assertEquals("90", frConstSum1.getAnswerString());

        FeedbackConstantSumResponseDetails frConstSum2 = (FeedbackConstantSumResponseDetails)
                BackDoor.getFeedbackResponse(fqConstSum2.getId(),
                                             aliceTeam,
                                             "Team 3").getResponseDetails();
        assertEquals("110", frConstSum2.getAnswerString());

        FeedbackContributionResponseDetails frContrib = (FeedbackContributionResponseDetails)
                BackDoor.getFeedbackResponse(fqContrib.getId(), "SFSubmitUiT.alice.b@gmail.tmt",
                                             "SFSubmitUiT.alice.b@gmail.tmt").getResponseDetails();
        assertEquals("100", frContrib.getAnswerString());

        FeedbackContributionResponseDetails frContrib1 = (FeedbackContributionResponseDetails)
                BackDoor.getFeedbackResponse(fqContrib.getId(), "SFSubmitUiT.alice.b@gmail.tmt",
                                             "SFSubmitUiT.benny.c@gmail.tmt").getResponseDetails();
        assertEquals("0", frContrib1.getAnswerString());

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");

        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageFullyFilled.html");

        ______TS("create new response for unreg student");
        submitPage.logout();
        submitPage = loginToStudentFeedbackSubmitPage(testData.students.get("DropOut"), "Open Session");

        responseText = "Test Self Feedback";
        submitPage.fillResponseRichTextEditor(1, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(1, 0));

        responseText = "Response to Benny.";
        submitPage.selectRecipient(2, 0, "Benny Charles");
        submitPage.fillResponseRichTextEditor(2, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 0));

        responseText = "Response to student who is number 1.";
        submitPage.selectRecipient(2, 1, "Alice Betsy</option></td></div>'\"");
        submitPage.fillResponseRichTextEditor(2, 1, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 1));

        responseText = "Response to extra guy.";
        submitPage.selectRecipient(2, 2, "Extra guy");
        submitPage.fillResponseRichTextEditor(2, 2, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(2, 2));

        submitPage.fillResponseTextBox(14, 0, "1");

        // Test partial response for question
        responseText = "Feedback to team 3";
        submitPage.fillResponseRichTextEditor(4, 0, responseText);
        assertEquals(responseText.trim().split(" +").length, submitPage.getResponseTextBoxLengthLabelValue(4, 0));

        submitPage.chooseMcqOption(7, 1, "Algo");
        submitPage.toggleMsqOption(9, 1, "UI");
        submitPage.toggleMsqOption(9, 1, "Design");

        submitPage.fillResponseTextBox(18, 0, 0, "90");
        submitPage.fillResponseTextBox(18, 0, 1, "10");

        submitPage.chooseContribOption(20, 0, "Equal share");

        assertNull(BackDoor.getFeedbackResponse(fq.getId(), "drop.out@gmail.tmt", "SFSubmitUiT.benny.c@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqPartial.getId(), "Team 2", "Team 3"));
        assertNull(BackDoor.getFeedbackResponse(fqMcq.getId(), "Team 2", testData.students.get("Alice").team));
        assertNull(BackDoor.getFeedbackResponse(fqMsq.getId(), "Team 2", testData.students.get("Alice").team));
        assertNull(BackDoor.getFeedbackResponse(fqNumscale.getId(), "drop.out@gmail.tmt", "drop.out@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqConstSum.getId(), "drop.out@gmail.tmt", "drop.out@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqContrib.getId(), "drop.out@gmail.tmt", "SFSubmitUiT.charlie.d@gmail.tmt"));

        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);
        submitPage.verifyHtmlMainContent("/unregisteredStudentFeedbackSubmitPagePartiallyFilled.html");

        assertNotNull(BackDoor.getFeedbackResponse(fq.getId(), "drop.out@gmail.tmt", "SFSubmitUiT.benny.c@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqPartial.getId(), "Team 2", "Team 3"));
        assertNotNull(BackDoor.getFeedbackResponse(fqMcq.getId(), "Team 2", testData.students.get("Alice").team));
        assertNotNull(BackDoor.getFeedbackResponse(fqMsq.getId(), "Team 2", testData.students.get("Alice").team));
        assertNotNull(BackDoor.getFeedbackResponse(fqNumscale.getId(), "drop.out@gmail.tmt", "drop.out@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqConstSum.getId(), "drop.out@gmail.tmt", "drop.out@gmail.tmt"));
        assertNotNull(BackDoor.getFeedbackResponse(fqContrib.getId(), "drop.out@gmail.tmt",
                                                   "SFSubmitUiT.charlie.d@gmail.tmt"));
    }

    private void testInputValidation() {
        ______TS("Test InputValidation lower than Min value");

        // this should not give any error since the value will be automatically adjusted before the form is submitted
        // adjusted value should be 1
        logout();
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.fillResponseTextBox(14, 0, "");
        submitPage.fillResponseTextBox(14, 0, "0");
        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

        FeedbackQuestionAttributes fqNumscale = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104", "First Session", 15);

        FeedbackResponseAttributes frNumscale = BackDoor.getFeedbackResponse(fqNumscale.getId(),
                                                                             "SFSubmitUiT.alice.b@gmail.tmt",
                                                                             "SFSubmitUiT.alice.b@gmail.tmt");

        assertEquals("1", frNumscale.getResponseDetails().getAnswerString());

        ______TS("Test InputValidation Over Max value");

        // this should not give any error since the value will be automatically adjusted before the form is submitted
        // adjusted value should be 5
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.fillResponseTextBox(14, 0, "50000");
        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

        fqNumscale = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104", "First Session", 15);

        frNumscale = BackDoor.getFeedbackResponse(fqNumscale.getId(),
                                                  "SFSubmitUiT.alice.b@gmail.tmt",
                                                  "SFSubmitUiT.alice.b@gmail.tmt");

        assertEquals("5", frNumscale.getResponseDetails().getAnswerString());

        ______TS("Test InputValidation extreme negative value");

        /* Attention: in safari or chrome, negative sign "-" can be input so the result will be adjusted to 1
         *            However, in firefox, the sign "-" can not be typed into the text box so no negative
         *            value can be input
         */
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.fillResponseTextBox(14, 0, "-99999");
        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

        fqNumscale = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104", "First Session", 15);

        frNumscale = BackDoor.getFeedbackResponse(fqNumscale.getId(),
                                                  "SFSubmitUiT.alice.b@gmail.tmt",
                                                  "SFSubmitUiT.alice.b@gmail.tmt");

        if ("firefox".equals(TestProperties.BROWSER)) {
            assertEquals("5", frNumscale.getResponseDetails().getAnswerString());
        } else {
            assertEquals("1", frNumscale.getResponseDetails().getAnswerString());

            // We need the final response value for this particular question to be "5"
            submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
            submitPage.fillResponseTextBox(14, 0, "5");
            submitPage.submitWithoutConfirmationEmail();
            submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);
        }

        ______TS("write response without specifying recipient");
        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");

        submitPage.selectRecipient(2, 2, "");
        submitPage.fillResponseRichTextEditor(2, 2, "Response to no recipient");
        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus("You did not specify a recipient for your response in question 2.");
    }

    private void testResponsiveSubmission() {
        ______TS("mobile test");
        submitPage = loginToStudentFeedbackSubmitPage(testData.students.get("DropOut"), "Open Session");

        // Select the first option for the first question for each student
        submitPage.clickRubricRadio(21, 0, 0, 0);
        submitPage.clickRubricRadio(21, 1, 0, 0);
        submitPage.clickRubricRadio(21, 2, 0, 0);
        submitPage.clickRubricRadio(21, 3, 0, 0);

        // Switch to mobile view
        submitPage.changeToMobileView();
        // Test if changes on desktop view persisted to mobile view
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 0, 0, 0));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 1, 0, 0));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 2, 0, 0));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 3, 0, 0));

        // Clear option for the first question for each student
        submitPage.reloadPage();
        submitPage.changeToMobileView();

        // Select the second option for the second question for each student
        submitPage.clickRubricRadioMobile(21, 0, 1, 1);
        submitPage.clickRubricRadioMobile(21, 1, 1, 1);
        submitPage.clickRubricRadioMobile(21, 2, 1, 1);
        submitPage.clickRubricRadioMobile(21, 3, 1, 1);

        // Switch to desktop view
        submitPage.changeToDesktopView();
        // Test if changes on mobile view persisted to desktop view
        assertFalse(submitPage.isRubricRadioMobileChecked(21, 0, 0, 0));
        assertFalse(submitPage.isRubricRadioMobileChecked(21, 1, 0, 0));
        assertFalse(submitPage.isRubricRadioMobileChecked(21, 2, 0, 0));
        assertFalse(submitPage.isRubricRadioMobileChecked(21, 3, 0, 0));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 0, 1, 1));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 1, 1, 1));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 2, 1, 1));
        assertTrue(submitPage.isRubricRadioMobileChecked(21, 3, 1, 1));

        FeedbackQuestionAttributes fqRubric = BackDoor.getFeedbackQuestion("SFSubmitUiT.CS2104", "First Session", 22);
        assertNull(BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "SFSubmitUiT.danny.e@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "extra.guy@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "drop.out@gmail.tmt"));
        assertNull(BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "SFSubmitUiT.charlie.d@gmail.tmt"));
        submitPage.submitWithoutConfirmationEmail();
        assertEquals("[-1, 1]", BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "SFSubmitUiT.danny.e@gmail.tmt").getResponseDetails().getAnswerString());
        assertEquals("[-1, 1]", BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "extra.guy@gmail.tmt").getResponseDetails().getAnswerString());
        assertEquals("[-1, 1]", BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "drop.out@gmail.tmt").getResponseDetails().getAnswerString());
        assertEquals("[-1, 1]", BackDoor.getFeedbackResponse(fqRubric.getId(),
                                        "drop.out@gmail.tmt",
                                        "SFSubmitUiT.charlie.d@gmail.tmt").getResponseDetails().getAnswerString());
    }

    private void testModifyData() throws Exception {
        ______TS("modify data");

        // Next, we edit some student data to cover editing of students
        // after creating the responses.

        // move one student out of Team 2 into a new team
        // This should cause the page to render an extra response box for
        // the team question.
        StudentAttributes extraGuy = testData.students.get("ExtraGuy");
        moveToTeam(extraGuy, "New Team");

        // delete one student
        // This should remove (hide on page render; not deleted) the response made to him,
        // and change the number of options in the recipient dropdown list.
        StudentAttributes dropOutGuy = testData.students.get("DropOut");
        String backDoorOperationStatus = BackDoor.deleteStudent(dropOutGuy.course,
                dropOutGuy.email);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);

        // move Benny out of Team >'"< 1 into team 2 and change her email
        // This should cause the team mates question to disappear completely as
        // no one else is in Team >'"< 1, but other responses to Benny should remain.
        StudentAttributes benny = testData.students.get("Benny");
        moveToTeam(benny, "Team 2");

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");
        submitPage.verifyHtmlMainContent("/studentFeedbackSubmitPageModified.html");

        // verify submission with no-response questions are possible
        submitPage.fillResponseTextBox(19, 2, "100");
        submitPage.submitWithoutConfirmationEmail();
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

        ______TS("Responses with invalid recipients do not prevent submission");
        StudentAttributes alice = testData.students.get("Alice");

        FeedbackQuestionAttributes questionFromDataBundle = testData.feedbackQuestions.get("qn4InSession1");
        FeedbackQuestionAttributes question = BackDoor.getFeedbackQuestion(
                questionFromDataBundle.courseId, questionFromDataBundle.feedbackSessionName,
                questionFromDataBundle.questionNumber);

        FeedbackResponseAttributes existingResponse =
                BackDoor.getFeedbackResponse(question.getId(), alice.team, "Team 2");
        FeedbackResponseAttributes response = new FeedbackResponseAttributes(existingResponse);
        response.recipient = "invalidRecipient";

        String backDoorStatusForCreatingResponse = BackDoor.createFeedbackResponse(response);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorStatusForCreatingResponse);

        submitPage = loginToStudentFeedbackSubmitPage("Alice", "Open Session");

        submitPage.submitWithoutConfirmationEmail();
        // verify that existing responses with invalid recipients do not affect submission
        submitPage.verifyStatus(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED);

    }

    private FeedbackSubmitPage loginToStudentFeedbackSubmitPage(StudentAttributes s, String fsDataId) {
        AppUrl submitUrl = createUrl(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE)
                                             .withCourseId(s.course)
                                             .withStudentEmail(s.email)
                                             .withSessionName(testData.feedbackSessions.get(fsDataId)
                                                                                       .getFeedbackSessionName())
                                             .withRegistrationKey(BackDoor.getEncryptedKeyForStudent(s.course, s.email));

        return AppPage.getNewPageInstance(browser, submitUrl, FeedbackSubmitPage.class);
    }

    private FeedbackSubmitPage loginToStudentFeedbackSubmitPage(String studentName, String fsName) {
        AppUrl editUrl = createUrl(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE)
                                        .withUserId(testData.students.get(studentName).googleId)
                                        .withCourseId(testData.feedbackSessions.get(fsName).getCourseId())
                                        .withSessionName(testData.feedbackSessions.get(fsName).getFeedbackSessionName());

        return loginAdminToPage(editUrl, FeedbackSubmitPage.class);
    }

    private FeedbackSessionNotVisiblePage
            loginToStudentFeedbackSubmitPageFeedbackSessionNotVisible(String studentName, String fsName) {
        AppUrl editUrl = createUrl(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE)
                                        .withUserId(testData.students.get(studentName).googleId)
                                        .withCourseId(testData.feedbackSessions.get(fsName).getCourseId())
                                        .withSessionName(testData.feedbackSessions.get(fsName).getFeedbackSessionName());

        return loginAdminToPage(editUrl, FeedbackSessionNotVisiblePage.class);
    }

    private void moveToTeam(StudentAttributes student, String newTeam) {
        String backDoorOperationStatus;
        student.team = newTeam;
        backDoorOperationStatus = BackDoor.editStudent(student.email, student);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
    }

}
