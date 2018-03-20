package teammates.test.cases.action;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.datatransfer.questions.FeedbackNumericalScaleQuestionDetails;
import teammates.common.datatransfer.questions.FeedbackQuestionType;
import teammates.common.exception.NullPostParameterException;
import teammates.common.util.Const;
import teammates.common.util.EmailType;
import teammates.common.util.EmailWrapper;
import teammates.common.util.StringHelper;
import teammates.common.util.TimeHelper;
import teammates.logic.core.CoursesLogic;
import teammates.logic.core.StudentsLogic;
import teammates.storage.api.FeedbackQuestionsDb;
import teammates.storage.api.FeedbackResponsesDb;
import teammates.storage.api.FeedbackSessionsDb;
import teammates.ui.controller.RedirectResult;
import teammates.ui.controller.StudentFeedbackSubmissionEditSaveAction;

/**
 * SUT: {@link StudentFeedbackSubmissionEditSaveAction}.
 */
public class StudentFeedbackSubmissionEditSaveActionTest extends BaseActionTest {
    private final CoursesLogic coursesLogic = CoursesLogic.inst();

    @BeforeClass
    public void classSetup() throws Exception {
        addUnregStudentToCourse1();
    }

    @AfterClass
    public void classTearDown() {
        StudentsLogic.inst().deleteStudentCascade("idOfTypicalCourse1", "student6InCourse1@gmail.tmt");
    }

    @Override
    protected String getActionUri() {
        return Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_SAVE;
    }

    @Override
    @Test
    public void testExecuteAndPostProcess() throws Exception {
        ______TS("edit existing answer");

        FeedbackQuestionsDb fqDb = new FeedbackQuestionsDb();
        FeedbackQuestionAttributes fq = fqDb.getFeedbackQuestion("First feedback session", "idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponsesDb frDb = new FeedbackResponsesDb();
        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1ForQ1S1C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        StudentAttributes student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        String[] submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "Edited" + fr.getResponseDetails().getAnswerString()
        };

        StudentFeedbackSubmissionEditSaveAction a = getAction(submissionParams);
        RedirectResult r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("deleted response");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("skipped question");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        // submission confirmation email not sent if parameter does not exist
        verifyNoEmailsSent(a);

        ______TS("new response");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "New " + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.SEND_SUBMISSION_EMAIL, "on"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        // submission confirmation email sent
        verifyNumberOfEmailsSent(a, 1);

        EmailWrapper email = getEmailsSent(a).get(0);
        String courseName = coursesLogic.getCourse(fr.courseId).getName();
        assertEquals(String.format(EmailType.FEEDBACK_SUBMISSION_CONFIRMATION.getSubject(), courseName,
                                   fr.feedbackSessionName),
                     email.getSubject());
        assertEquals(student1InCourse1.email, email.getRecipient());

        ______TS("edit response, did not specify recipient");

        fq = fqDb.getFeedbackQuestion("First feedback session", "idOfTypicalCourse1", 2);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response2ForQ2S1C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-2", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-2-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-2", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-2-0", "",
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-2", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-2-0", "Edited" + fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.SEND_SUBMISSION_EMAIL, "on"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertTrue(r.isError);
        assertEquals("You did not specify a recipient for your response in question 2.", r.getStatusMessage());
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE,
                        r.isError,
                        "student1InCourse1",
                        "idOfTypicalCourse1",
                        "First+feedback+session"),
                r.getDestinationWithParams());

        // submission confirmation email not sent if the action is an error, even with submission parameter "on"
        verifyNoEmailsSent(a);

        ______TS("edit response, empty answer");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-2", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-2-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-2", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-2-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-2", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-2-0", "",
                Const.ParamsNames.SEND_SUBMISSION_EMAIL, "off"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        // submission confirmation email not sent if parameter is not "on"
        verifyNoEmailsSent(a);

        ______TS("new response, did not specify recipient");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-2", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-2", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-2-0", "",
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-2", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-2-0", "Edited" + fr.getResponseDetails().getAnswerString(),
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertTrue(r.isError);
        assertEquals("You did not specify a recipient for your response in question 2.", r.getStatusMessage());
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE,
                        r.isError,
                        "student1InCourse1",
                        "idOfTypicalCourse1",
                        "First+feedback+session"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("mcq");

        DataBundle dataBundle = loadDataBundle("/FeedbackSessionQuestionTypeTest.json");
        removeAndRestoreDataBundle(dataBundle);

        fq = fqDb.getFeedbackQuestion("MCQ Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ1S1C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "It's perfect"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("mcq, question skipped");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString()
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("mcq with other option enabled");

        fq = fqDb.getFeedbackQuestion("MCQ Session", "FSQTT.idOfTypicalCourse1", 3);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ3S1C1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient); // necessary to get the correct responseId
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-2", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-2-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-2", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-2-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-2", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-2-0", "Tutorial",
                Const.ParamsNames.FEEDBACK_QUESTION_MCQ_ISOTHEROPTIONANSWER + "-2-0", "1"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        FeedbackResponseAttributes finalFr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull(finalFr);
        assertEquals("Tutorial", finalFr.getResponseDetails().getAnswerString());

        ______TS("msq");

        fq = fqDb.getFeedbackQuestion("MSQ Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ1S2C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "It's perfect"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("msq, question skipped");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString()
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("msq with other option enabled, student selects predefined options and other option");

        fq = fqDb.getFeedbackQuestion("MSQ Session", "FSQTT.idOfTypicalCourse1", 3);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ3S2C1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient); // necessary to get the correct responseId
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-3", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-3-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-3", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-3-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-3", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-3-0", "Pizza, Pasta, Chicken rice, Hotdog",
                Const.ParamsNames.FEEDBACK_QUESTION_MSQ_ISOTHEROPTIONANSWER + "-3-0", "1"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        finalFr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull(finalFr);
        assertEquals("Pizza, Pasta, Chicken rice, Hotdog", finalFr.getResponseDetails().getAnswerString());

        ______TS("msq with other option enabled, student selects predefined options only, not other option");

        fr = dataBundle.feedbackResponses.get("response2ForQ3S2C1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient); // necessary to get the correct responseId
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-3", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-3-1", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-3", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-3-1", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-3", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-3-1", "Pasta",
                Const.ParamsNames.FEEDBACK_QUESTION_MSQ_ISOTHEROPTIONANSWER + "-3-1", "0"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        finalFr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull(finalFr);
        assertEquals("Pasta", finalFr.getResponseDetails().getAnswerString());

        ______TS("msq with other option enabled, student selects other option only, not any predefined option");

        fr = dataBundle.feedbackResponses.get("response3ForQ3S2C1");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient); // necessary to get the correct responseId
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-3", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-3-2", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-3", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-3-2", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-3", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-3-2", "Hotdog",
                Const.ParamsNames.FEEDBACK_QUESTION_MSQ_ISOTHEROPTIONANSWER + "-3-2", "1"
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        finalFr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull(finalFr);
        assertEquals("Hotdog", finalFr.getResponseDetails().getAnswerString());

        ______TS("numscale");

        fq = fqDb.getFeedbackQuestion("NUMSCALE Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);
        FeedbackNumericalScaleQuestionDetails fqd =
                (FeedbackNumericalScaleQuestionDetails) fq.getQuestionDetails();

        fr = dataBundle.feedbackResponses.get("response1ForQ1S3C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "1",
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MIN + "-1-0", Integer.toString(fqd.getMinScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MAX + "-1-0", Integer.toString(fqd.getMaxScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_STEP + "-1-0", StringHelper.toDecimalFormatString(fqd.getStep())
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertFalse(r.isError);
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("numscale, question skipped");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "",
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MIN + "-1-0", Integer.toString(fqd.getMinScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MAX + "-1-0", Integer.toString(fqd.getMaxScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_STEP + "-1-0", StringHelper.toDecimalFormatString(fqd.getStep())
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Successful case: const sum: typical case");

        fq = fqDb.getFeedbackQuestion("CONSTSUM Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ1S4C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        FeedbackResponseAttributes fr2 = dataBundle.feedbackResponses.get("response2ForQ1S4C1");
        // necessary to get the correct responseId
        fr2 = frDb.getFeedbackResponse(fq.getId(), fr2.giver, fr2.recipient);
        assertNotNull("Feedback response not found in database", fr2);

        student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "50",
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "50",
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertFalse(r.isError);
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Successful case: const sum: question skipped");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Successful case: contrib qn: typical case");

        fq = fqDb.getFeedbackQuestion("CONTRIB Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        fr = dataBundle.feedbackResponses.get("response1ForQ1S5C1");
        // necessary to get the correct responseId
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        student1InCourse1 = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(student1InCourse1.googleId);

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "150",
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertFalse(r.isError);
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, r.isError, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Successful case: contrib qn: question skipped");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertFalse(r.isError);
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "FSQTT.student1InCourse1"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Unsuccessful case: test empty feedback session name parameter");

        submissionParams = new String[]{
                Const.ParamsNames.COURSE_ID, dataBundle.feedbackResponses.get("response1ForQ1S1C1").courseId
        };

        try {
            a = getAction(submissionParams);
            r = getRedirectResult(a);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullPostParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                                       Const.ParamsNames.FEEDBACK_SESSION_NAME), e.getMessage());
        }

        ______TS("Unsuccessful case: test empty course id parameter");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_SESSION_NAME,
                        dataBundle.feedbackResponses.get("response1ForQ1S1C1").feedbackSessionName
        };

        try {
            a = getAction(submissionParams);
            r = getRedirectResult(a);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullPostParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                                       Const.ParamsNames.COURSE_ID), e.getMessage());
        }

        ______TS("Unsuccessful case: modified recipient list to invalid recipient");

        submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", "invalid recipient",
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertTrue(r.isError);
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE, true, "FSQTT.student1InCourse1",
                        "FSQTT.idOfTypicalCourse1", "CONTRIB+Session"),
                r.getDestinationWithParams());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, "invalid recipient"));

        ______TS("Unsuccessful case: modified question type to another type");
        // Response is supposed to be CONTRIB, but submit as RUBRIC
        assertEquals(fq.questionType, FeedbackQuestionType.CONTRIB);
        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", "RUBRIC",
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertTrue(r.isError);
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE, true, "FSQTT.student1InCourse1",
                        "FSQTT.idOfTypicalCourse1", "CONTRIB+Session"),
                r.getDestinationWithParams());
        assertEquals(String.format(Const.StatusMessages.FEEDBACK_RESPONSES_WRONG_QUESTION_TYPE, "1"), r.getStatusMessage());
        assertNull(frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient));

        ______TS("Unsuccessful case: try to delete response not belonging to the student");

        fq = fqDb.getFeedbackQuestion("MCQ Session", "FSQTT.idOfTypicalCourse1", 1);
        assertNotNull("Feedback question not found in database", fq);

        FeedbackResponseAttributes otherFr = dataBundle.feedbackResponses.get("response2ForQ1S1C1");
        List<FeedbackResponseAttributes> responsesToAdd = new ArrayList<>();
        responsesToAdd.add(fr);
        frDb.createFeedbackResponses(responsesToAdd);

        // necessary to get the correct responseId
        otherFr = frDb.getFeedbackResponse(fq.getId(), otherFr.giver, otherFr.recipient);
        assertNotNull("Feedback response not found in database", fr);

        submissionParams = new String[] {
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", otherFr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, otherFr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, otherFr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", otherFr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", otherFr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", otherFr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", ""
        };

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertTrue(r.isError);

        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE, true, "FSQTT.student1InCourse1",
                        "FSQTT.idOfTypicalCourse1", "MCQ+Session"),
                r.getDestinationWithParams());
        assertNotNull(frDb.getFeedbackResponse(fq.getId(), otherFr.giver, otherFr.recipient));

        gaeSimulation.logoutUser();

        ______TS("Unregistered student with valid submission of response remains at submission page");

        StudentAttributes unregisteredStudent = dataBundle.students.get("unregisteredStudentInCourse1");

        fq = fqDb.getFeedbackQuestion("Unregistered Student Session", "FSQTT.idOfTypicalCourse2", 1);
        assertNotNull("Feedback question not found in database", fq);
        fqd = (FeedbackNumericalScaleQuestionDetails) fq.getQuestionDetails();

        fr = dataBundle.feedbackResponses.get("response1ForQ1S1C2");
        fr = frDb.getFeedbackResponse(fq.getId(), fr.giver, fr.recipient);
        // ensure correct response id is retrieved
        assertNotNull("Feedback response not found in database", fr);

        FeedbackSessionsDb fsDb = new FeedbackSessionsDb();
        FeedbackSessionAttributes fsa = dataBundle.feedbackSessions.get("unregisteredStudentSession");
        fsa = fsDb.getFeedbackSession(unregisteredStudent.course, fsa.getFeedbackSessionName());
        assertNotNull("Feedback session not found in database", fsa);

        String studentKey = StudentsLogic.inst().getEncryptedKeyForStudent(unregisteredStudent.course,
                                                                           unregisteredStudent.email);

        // Valid response from unregistered student
        String[] validSubmissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", fr.getResponseDetails().getAnswerString(),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MIN + "-1-0", Integer.toString(fqd.getMinScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MAX + "-1-0", Integer.toString(fqd.getMaxScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_STEP + "-1-0",
                        StringHelper.toDecimalFormatString(fqd.getStep()),
                Const.ParamsNames.SEND_SUBMISSION_EMAIL, "on",

                Const.ParamsNames.REGKEY, studentKey,
                Const.ParamsNames.STUDENT_EMAIL, unregisteredStudent.email
        };

        StudentFeedbackSubmissionEditSaveAction submissionAction = getAction(validSubmissionParams);
        RedirectResult redirectResult = getRedirectResult(submissionAction);

        assertFalse(redirectResult.isError);
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE,
                        "unregisteredStudentInCourse2%40gmail.tmt",
                        redirectResult.isError, unregisteredStudent.course,
                        "Unregistered+Student+Session", studentKey),
                redirectResult.getDestinationWithParams());
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, redirectResult.getStatusMessage());
        gaeSimulation.logoutUser();

        // submission confirmation email sent also for unregistered student
        verifyNumberOfEmailsSent(submissionAction, 1);

        email = getEmailsSent(submissionAction).get(0);
        courseName = coursesLogic.getCourse(fr.courseId).getName();
        assertEquals(String.format(EmailType.FEEDBACK_SUBMISSION_CONFIRMATION.getSubject(), courseName,
                                   fr.feedbackSessionName),
                     email.getSubject());
        assertEquals(unregisteredStudent.email, email.getRecipient());

        ______TS("Unregistered student with invalid submission of response remains at submission page");

        studentKey = StudentsLogic.inst().getEncryptedKeyForStudent(unregisteredStudent.course, unregisteredStudent.email);

        // Invalid response from unregistered student
        String[] invalidSubmissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_QUESTION_RESPONSETOTAL + "-1", "1",
                Const.ParamsNames.FEEDBACK_RESPONSE_ID + "-1-0", fr.getId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", "100",
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MIN + "-1-0", Integer.toString(fqd.getMinScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_MAX + "-1-0", Integer.toString(fqd.getMaxScale()),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMSCALE_STEP + "-1-0",
                        StringHelper.toDecimalFormatString(fqd.getStep()),

                Const.ParamsNames.REGKEY, studentKey,
                Const.ParamsNames.STUDENT_EMAIL, unregisteredStudent.email
        };

        submissionAction = getAction(invalidSubmissionParams);
        redirectResult = getRedirectResult(submissionAction);

        assertTrue(redirectResult.isError);
        assertEquals(
                getPageResultDestination(
                        Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_PAGE,
                        "unregisteredStudentInCourse2%40gmail.tmt",
                        redirectResult.isError, unregisteredStudent.course,
                        "Unregistered+Student+Session", studentKey),
                redirectResult.getDestinationWithParams());
        assertEquals("100 is out of the range for Numerical-scale question.(min=1, max=5)",
                     redirectResult.getStatusMessage());
        gaeSimulation.logoutUser();
    }

    @Test
    public void testGracePeriodExecuteAndPostProcess() throws Exception {
        FeedbackSessionsDb feedbackSessionDb = new FeedbackSessionsDb();
        FeedbackSessionAttributes fs = dataBundle.feedbackSessions.get("gracePeriodSession");
        StudentAttributes studentInGracePeriod = dataBundle.students.get("student1InCourse1");
        gaeSimulation.loginAsStudent(studentInGracePeriod.googleId);

        String[] submissionParams = new String[]{
                Const.ParamsNames.COURSE_ID, fs.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.getFeedbackSessionName()
        };

        ______TS("opened");

        fs.setEndTime(TimeHelper.getDateOffsetToCurrentTime(1));
        feedbackSessionDb.updateFeedbackSession(fs);

        assertTrue(fs.isOpened());
        assertFalse(fs.isInGracePeriod());

        StudentFeedbackSubmissionEditSaveAction a = getAction(submissionParams);
        RedirectResult r = getRedirectResult(a);

        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "student1InCourse1"),
                r.getDestinationWithParams());
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertFalse(r.isError);

        ______TS("during grace period");

        fs.setEndTime(TimeHelper.getDateOffsetToCurrentTime(0));
        feedbackSessionDb.updateFeedbackSession(fs);

        assertFalse(fs.isOpened());
        assertTrue(fs.isInGracePeriod());

        a = getAction(submissionParams);
        r = getRedirectResult(a);
        assertEquals(
                getPageResultDestination(Const.ActionURIs.STUDENT_HOME_PAGE, false, "student1InCourse1"),
                r.getDestinationWithParams());
        assertEquals(Const.StatusMessages.FEEDBACK_RESPONSES_SAVED, r.getStatusMessage());
        assertFalse(r.isError);

        ______TS("after grace period");

        fs.setEndTime(TimeHelper.getDateOffsetToCurrentTime(-10));
        feedbackSessionDb.updateFeedbackSession(fs);

        assertFalse(fs.isOpened());
        assertFalse(fs.isInGracePeriod());

        a = getAction(submissionParams);
        r = getRedirectResult(a);

        assertEquals(Const.StatusMessages.FEEDBACK_SUBMISSIONS_NOT_OPEN, r.getStatusMessage());
    }

    @Override
    protected StudentFeedbackSubmissionEditSaveAction getAction(String... params) {
        return (StudentFeedbackSubmissionEditSaveAction) gaeSimulation.getActionObject(getActionUri(), params);
    }

    protected String getPageResultDestination(
            String parentUri, boolean isError, String userId, String courseId, String fsname) {
        String pageDestination = parentUri;
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.ERROR, Boolean.toString(isError));
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.USER_ID, userId);
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.COURSE_ID, courseId);
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.FEEDBACK_SESSION_NAME, fsname);
        return pageDestination;
    }

    protected String getPageResultDestination(
            String parentUri, String studentEmail, boolean isError, String courseId, String fsname, String key) {
        String pageDestination = parentUri;
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.STUDENT_EMAIL, studentEmail);
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.ERROR, Boolean.toString(isError));
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.COURSE_ID, courseId);
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.FEEDBACK_SESSION_NAME, fsname);
        pageDestination = addParamToUrl(pageDestination, Const.ParamsNames.REGKEY, key);
        return pageDestination;
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1ForQ1S1C1");

        String[] submissionParams = new String[]{
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fr.feedbackSessionName,
                Const.ParamsNames.COURSE_ID, fr.courseId,
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", fr.getResponseDetails().getAnswerString()
        };

        verifyOnlyStudentsOfTheSameCourseCanAccess(submissionParams);
        testGracePeriodAccessControlForStudents();
    }

    private void testGracePeriodAccessControlForStudents() {
        FeedbackSessionAttributes fs = dataBundle.feedbackSessions.get("gracePeriodSession");
        fs.setEndTime(TimeHelper.getDateOffsetToCurrentTime(0));
        dataBundle.feedbackSessions.put("gracePeriodSession", fs);

        assertFalse(fs.isOpened());
        assertTrue(fs.isInGracePeriod());
        assertFalse(fs.isClosed());

        FeedbackResponseAttributes fr = dataBundle.feedbackResponses.get("response1GracePeriodFeedback");

        String[] submissionParams = new String[]{
                Const.ParamsNames.COURSE_ID, fs.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.getFeedbackSessionName(),
                Const.ParamsNames.FEEDBACK_QUESTION_ID + "-1", fr.feedbackQuestionId,
                Const.ParamsNames.FEEDBACK_RESPONSE_RECIPIENT + "-1-0", fr.recipient,
                Const.ParamsNames.FEEDBACK_QUESTION_TYPE + "-1", fr.feedbackQuestionType.toString(),
                Const.ParamsNames.FEEDBACK_RESPONSE_TEXT + "-1-0", fr.getResponseDetails().getAnswerString()
        };

        verifyOnlyStudentsOfTheSameCourseCanAccess(submissionParams);
    }
}
