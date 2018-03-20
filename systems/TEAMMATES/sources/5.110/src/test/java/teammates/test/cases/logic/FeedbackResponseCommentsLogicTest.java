package teammates.test.cases.logic;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.appengine.api.datastore.Text;

import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseCommentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.core.FeedbackQuestionsLogic;
import teammates.logic.core.FeedbackResponseCommentsLogic;
import teammates.logic.core.FeedbackResponsesLogic;

/**
 * SUT: {@link FeedbackResponseCommentsLogic}.
 */
public class FeedbackResponseCommentsLogicTest extends BaseLogicTest {

    private static final FeedbackResponseCommentsLogic frcLogic = FeedbackResponseCommentsLogic.inst();
    private static final FeedbackQuestionsLogic fqLogic = FeedbackQuestionsLogic.inst();
    private static final FeedbackResponsesLogic frLogic = FeedbackResponsesLogic.inst();

    @Override
    protected void prepareTestData() {
        // test data is refreshed before each test case
    }

    @BeforeMethod
    public void refreshTestData() {
        dataBundle = getTypicalDataBundle();
        removeAndRestoreTypicalDataBundle();
    }

    @Test
    public void testCreateFeedbackResponseComment() throws Exception {
        FeedbackResponseCommentAttributes frComment = new FeedbackResponseCommentAttributes();
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: non-existent course");

        frComment.courseId = "no-such-course";

        verifyExceptionThrownFromCreateFrComment(frComment,
                "Trying to create feedback response comments for a course that does not exist.");
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: giver is not instructor");

        frComment.giverEmail = "student2InCourse1@gmail.com";

        verifyExceptionThrownFromCreateFrComment(frComment,
                "User " + frComment.giverEmail + " is not a registered instructor for course "
                + frComment.courseId + ".");
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: giver is not an instructor for the course");

        frComment.giverEmail = "instructor1@course2.com";

        verifyExceptionThrownFromCreateFrComment(frComment,
                "User " + frComment.giverEmail + " is not a registered instructor for course "
                + frComment.courseId + ".");
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: feedback session is not a session for the course");

        frComment.feedbackSessionName = "Private feedback session";

        verifyExceptionThrownFromCreateFrComment(frComment,
                "Feedback session " + frComment.feedbackSessionName + " is not a session for course "
                + frComment.courseId + ".");
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("typical successful case");

        frComment.setId(null);
        frComment.feedbackQuestionId = getQuestionIdInDataBundle("qn1InSession1InCourse1");
        frComment.feedbackResponseId = getResponseIdInDataBundle("response2ForQ1S1C1", "qn1InSession1InCourse1");

        frcLogic.createFeedbackResponseComment(frComment);
        verifyPresentInDatastore(frComment);

        ______TS("typical successful case: frComment already exists");
        frcLogic.createFeedbackResponseComment(frComment);
        List<FeedbackResponseCommentAttributes> actualFrComments =
                frcLogic.getFeedbackResponseCommentForSession(frComment.courseId, frComment.feedbackSessionName);

        FeedbackResponseCommentAttributes actualFrComment = null;
        for (int i = 0; i < actualFrComments.size(); i++) {
            if (actualFrComments.get(i).commentText.equals(frComment.commentText)) {
                actualFrComment = actualFrComments.get(i);
                break;
            }
        }

        assertNotNull(actualFrComment);

        //delete afterwards
        frcLogic.deleteFeedbackResponseComment(frComment);
    }

    @Test
    public void testGetFeedbackResponseComments() {
        FeedbackResponseCommentAttributes frComment = new FeedbackResponseCommentAttributes();
        List<FeedbackResponseCommentAttributes> expectedFrComments = new ArrayList<>();
        this.restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: invalid parameters");

        frComment.courseId = "invalid course id";
        frComment.giverEmail = "invalid giver email";

        verifyNullFromGetFrCommentForSession(frComment);
        verifyNullFromGetFrComment(frComment);
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("Typical successful case");

        List<FeedbackResponseCommentAttributes> actualFrComments =
                frcLogic.getFeedbackResponseCommentForSession(
                                 frComment.courseId, frComment.feedbackSessionName);
        FeedbackResponseCommentAttributes actualFrComment = actualFrComments.get(0);

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(frComment.giverEmail, actualFrComment.giverEmail);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        ______TS("Typical successful case by feedback response comment details");

        actualFrComment =
                frcLogic.getFeedbackResponseComment(
                                 frComment.feedbackResponseId, frComment.giverEmail, frComment.createdAt);

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(frComment.giverEmail, actualFrComment.giverEmail);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        ______TS("Typical successful case by feedback response id");

        actualFrComments = frcLogic.getFeedbackResponseCommentForResponse(frComment.feedbackResponseId);
        actualFrComment = actualFrComments.get(0);

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(frComment.giverEmail, actualFrComment.giverEmail);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        ______TS("Typical successful case by feedback response comment id");

        actualFrComment = frcLogic.getFeedbackResponseComment(frComment.getId());

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(frComment.giverEmail, actualFrComment.giverEmail);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        ______TS("Typical successful case for giver");

        actualFrComments = frcLogic.getFeedbackResponseCommentsForGiver(
                                            frComment.courseId, frComment.giverEmail);
        FeedbackResponseCommentAttributes tempFrComment = new FeedbackResponseCommentAttributes();
        this.restoreFrCommentFromDataBundle(tempFrComment, "comment1FromT1C1ToR1Q1S1C1");
        expectedFrComments.add(tempFrComment);
        tempFrComment = new FeedbackResponseCommentAttributes();
        this.restoreFrCommentFromDataBundle(tempFrComment, "comment1FromT1C1ToR1Q2S1C1");
        expectedFrComments.add(tempFrComment);
        tempFrComment = new FeedbackResponseCommentAttributes();
        this.restoreFrCommentFromDataBundle(tempFrComment, "comment1FromT1C1ToR1Q3S1C1");
        expectedFrComments.add(tempFrComment);

        assertEquals(expectedFrComments.size(), actualFrComments.size());

        for (int i = 0; i < expectedFrComments.size(); i++) {
            assertEquals(expectedFrComments.get(i).courseId, actualFrComments.get(i).courseId);
            assertEquals(expectedFrComments.get(i).giverEmail, actualFrComments.get(i).giverEmail);
            assertEquals(expectedFrComments.get(i).feedbackSessionName,
                         actualFrComments.get(i).feedbackSessionName);
        }

    }

    @Test
    public void testUpdateFeedbackResponseComment() throws Exception {
        FeedbackResponseCommentAttributes frComment = new FeedbackResponseCommentAttributes();
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("fail: invalid params");

        frComment.courseId = "invalid course name";
        String expectedError =
                "\"" + frComment.courseId + "\" is not acceptable to TEAMMATES as a/an course ID "
                + "because it is not in the correct format. A course ID can contain letters, "
                + "numbers, fullstops, hyphens, underscores, and dollar signs. It cannot be longer "
                + "than 40 characters, cannot be empty and cannot contain spaces.";
        verifyExceptionThrownWhenUpdateFrComment(frComment, expectedError);
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");

        ______TS("typical success case");

        frComment.commentText = new Text("Updated feedback response comment");
        frcLogic.updateFeedbackResponseComment(frComment);
        verifyPresentInDatastore(frComment);
        List<FeedbackResponseCommentAttributes> actualFrComments =
                frcLogic.getFeedbackResponseCommentForSession(frComment.courseId, frComment.feedbackSessionName);

        FeedbackResponseCommentAttributes actualFrComment = null;
        for (int i = 0; i < actualFrComments.size(); i++) {
            if (actualFrComments.get(i).commentText.equals(frComment.commentText)) {
                actualFrComment = actualFrComments.get(i);
                break;
            }
        }
        assertNotNull(actualFrComment);

        ______TS("typical success case update feedback response comment giver email");

        String oldEmail = frComment.giverEmail;
        String updatedEmail = "newEmail@gmail.tmt";
        frcLogic.updateFeedbackResponseCommentsEmails(frComment.courseId, oldEmail, updatedEmail);

        actualFrComment = frcLogic.getFeedbackResponseComment(
                                           frComment.feedbackResponseId, updatedEmail, frComment.createdAt);

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(updatedEmail, actualFrComment.giverEmail);
        assertEquals(updatedEmail, actualFrComment.lastEditorEmail);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        // reset email
        frcLogic.updateFeedbackResponseCommentsEmails(frComment.courseId, updatedEmail, oldEmail);

        ______TS("typical success case update feedback response comment feedbackResponseId");

        String oldId = frComment.feedbackResponseId;
        String updatedId = "newResponseId";
        frcLogic.updateFeedbackResponseCommentsForChangingResponseId(oldId, updatedId);

        actualFrComment = frcLogic.getFeedbackResponseComment(
                updatedId, frComment.giverEmail, frComment.createdAt);

        assertEquals(frComment.courseId, actualFrComment.courseId);
        assertEquals(updatedId, actualFrComment.feedbackResponseId);
        assertEquals(frComment.feedbackSessionName, actualFrComment.feedbackSessionName);

        // reset id
        frcLogic.updateFeedbackResponseCommentsForChangingResponseId(updatedId, oldId);
    }

    @Test
    public void testDeleteFeedbackResponseComment() throws Exception {
        //create a frComment to delete
        FeedbackResponseCommentAttributes frComment = new FeedbackResponseCommentAttributes();
        restoreFrCommentFromDataBundle(frComment, "comment1FromT1C1ToR1Q1S1C1");
        frComment.setId(null);
        frComment.feedbackQuestionId = getQuestionIdInDataBundle("qn2InSession1InCourse1");
        frComment.feedbackResponseId = getResponseIdInDataBundle("response2ForQ2S1C1", "qn2InSession1InCourse1");

        frcLogic.createFeedbackResponseComment(frComment);

        ______TS("silent fail nothing to delete");

        frComment.feedbackResponseId = "invalid responseId";
        //without proper frCommentId and its feedbackResponseId,
        //it cannot be deleted
        frcLogic.deleteFeedbackResponseComment(frComment);

        FeedbackResponseCommentAttributes actualFrComment =
                frcLogic.getFeedbackResponseCommentForSession(
                                 frComment.courseId, frComment.feedbackSessionName).get(1);
        verifyPresentInDatastore(actualFrComment);

        ______TS("typical success case");

        frcLogic.deleteFeedbackResponseComment(actualFrComment);
        verifyAbsentInDatastore(actualFrComment);

        ______TS("typical success case for response");

        FeedbackResponseCommentAttributes anotherFrComment = new FeedbackResponseCommentAttributes();
        restoreFrCommentFromDataBundle(anotherFrComment, "comment1FromT1C1ToR1Q2S1C1");
        verifyPresentInDatastore(anotherFrComment);
        frcLogic.deleteFeedbackResponseCommentsForResponse(anotherFrComment.feedbackResponseId);
        verifyAbsentInDatastore(anotherFrComment);

    }

    @Test
    public void testDeleteFeedbackResponseCommentFromCourse() {

        ______TS("typical case");
        String courseId = "idOfTypicalCourse1";

        List<FeedbackResponseCommentAttributes> frcList =
                frcLogic.getFeedbackResponseCommentForSession(courseId, "First feedback session");
        assertFalse(frcList.isEmpty());

        frcLogic.deleteFeedbackResponseCommentsForCourse(courseId);

        frcList = frcLogic.getFeedbackResponseCommentForSession(courseId, "First feedback session");
        assertEquals(0, frcList.size());
    }

    private void verifyExceptionThrownFromCreateFrComment(
            FeedbackResponseCommentAttributes frComment, String expectedMessage)
            throws InvalidParametersException {
        try {
            frcLogic.createFeedbackResponseComment(frComment);
            signalFailureToDetectException();
        } catch (EntityDoesNotExistException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    private void verifyNullFromGetFrCommentForSession(FeedbackResponseCommentAttributes frComment) {
        List<FeedbackResponseCommentAttributes> frCommentsGot =
                frcLogic.getFeedbackResponseCommentForSession(frComment.courseId, frComment.feedbackSessionName);
        assertEquals(0, frCommentsGot.size());
    }

    private void verifyNullFromGetFrComment(FeedbackResponseCommentAttributes frComment) {
        FeedbackResponseCommentAttributes frCommentGot =
                frcLogic.getFeedbackResponseComment(
                                 frComment.feedbackResponseId, frComment.giverEmail, frComment.createdAt);
        assertEquals(null, frCommentGot);
    }

    private void verifyExceptionThrownWhenUpdateFrComment(
            FeedbackResponseCommentAttributes frComment, String expectedString)
            throws EntityDoesNotExistException {
        try {
            frcLogic.updateFeedbackResponseComment(frComment);
            signalFailureToDetectException();
        } catch (InvalidParametersException e) {
            assertEquals(expectedString, e.getMessage());
        }
    }

    private void restoreFrCommentFromDataBundle(
            FeedbackResponseCommentAttributes frComment, String existingFrCommentInDataBundle) {

        FeedbackResponseCommentAttributes existingFrComment =
                dataBundle.feedbackResponseComments.get(existingFrCommentInDataBundle);
        frComment.courseId = existingFrComment.courseId;
        frComment.giverEmail = existingFrComment.giverEmail;
        frComment.feedbackSessionName = existingFrComment.feedbackSessionName;
        frComment.feedbackQuestionId = existingFrComment.feedbackQuestionId;
        frComment.commentText = existingFrComment.commentText;
        frComment.createdAt = existingFrComment.createdAt;
        restoreFrCommentIdFromExistingOne(frComment, existingFrComment);
    }

    private void restoreFrCommentIdFromExistingOne(
            FeedbackResponseCommentAttributes frComment,
            FeedbackResponseCommentAttributes existingFrComment) {

        List<FeedbackResponseCommentAttributes> existingFrComments =
                frcLogic.getFeedbackResponseCommentForSession(
                                 existingFrComment.courseId,
                                 existingFrComment.feedbackSessionName);

        FeedbackResponseCommentAttributes existingFrCommentWithId = null;
        for (FeedbackResponseCommentAttributes c : existingFrComments) {
            if (c.commentText.equals(existingFrComment.commentText)) {
                existingFrCommentWithId = c;
                break;
            }
        }
        if (existingFrCommentWithId != null) {
            frComment.setId(existingFrCommentWithId.getId());
            frComment.feedbackResponseId = existingFrCommentWithId.feedbackResponseId;
        }
    }

    private String getQuestionIdInDataBundle(String questionInDataBundle) {
        FeedbackQuestionAttributes question = dataBundle.feedbackQuestions.get(questionInDataBundle);
        question = fqLogic.getFeedbackQuestion(
                                   question.feedbackSessionName, question.courseId, question.questionNumber);
        return question.getId();
    }

    private String getResponseIdInDataBundle(String responseInDataBundle, String questionInDataBundle) {
        FeedbackResponseAttributes response = dataBundle.feedbackResponses.get(responseInDataBundle);
        response = frLogic.getFeedbackResponse(
                                   getQuestionIdInDataBundle(questionInDataBundle),
                                   response.giver,
                                   response.recipient);
        return response.getId();
    }
}
