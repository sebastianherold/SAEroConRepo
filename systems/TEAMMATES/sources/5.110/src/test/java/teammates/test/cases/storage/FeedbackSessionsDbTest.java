package teammates.test.cases.storage;

import static teammates.common.util.FieldValidator.SESSION_END_TIME_FIELD_NAME;
import static teammates.common.util.FieldValidator.SESSION_START_TIME_FIELD_NAME;
import static teammates.common.util.FieldValidator.TIME_FRAME_ERROR_MESSAGE;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.appengine.api.datastore.Text;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackSessionType;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.storage.api.FeedbackSessionsDb;
import teammates.test.cases.BaseComponentTestCase;
import teammates.test.driver.AssertHelper;

/**
 * SUT: {@link FeedbackSessionsDb}.
 */
public class FeedbackSessionsDbTest extends BaseComponentTestCase {

    private static final FeedbackSessionsDb fsDb = new FeedbackSessionsDb();
    private DataBundle dataBundle = getTypicalDataBundle();

    @BeforeClass
    public void classSetup() throws Exception {
        addSessionsToDb();
    }

    private void addSessionsToDb() throws Exception {
        Set<String> keys = dataBundle.feedbackSessions.keySet();
        for (String i : keys) {
            try {
                fsDb.createEntity(dataBundle.feedbackSessions.get(i));
            } catch (EntityAlreadyExistsException e) {
                fsDb.updateFeedbackSession(dataBundle.feedbackSessions.get(i));
            }
        }
    }

    @Test
    public void testCreateDeleteFeedbackSession()
            throws Exception {

        ______TS("standard success case");

        FeedbackSessionAttributes fsa = getNewFeedbackSession();
        fsDb.createEntity(fsa);
        verifyPresentInDatastore(fsa);

        ______TS("duplicate");
        try {
            fsDb.createEntity(fsa);
            signalFailureToDetectException();
        } catch (EntityAlreadyExistsException e) {
            AssertHelper.assertContains(String.format(FeedbackSessionsDb.ERROR_CREATE_ENTITY_ALREADY_EXISTS,
                                                      fsa.getEntityTypeAsString())
                                            + fsa.getIdentificationString(),
                                        e.getMessage());
        }

        fsDb.deleteEntity(fsa);
        verifyAbsentInDatastore(fsa);

        ______TS("null params");

        try {
            fsDb.createEntity(null);
            signalFailureToDetectException();
        } catch (AssertionError e) {
            AssertHelper.assertContains(Const.StatusCodes.DBLEVEL_NULL_INPUT, e.getLocalizedMessage());
        }

        ______TS("invalid params");

        try {
            fsa.setStartTime(new Date());
            fsDb.createEntity(fsa);
            signalFailureToDetectException();
        } catch (InvalidParametersException e) {
            // start time is now after end time
            AssertHelper.assertContains("start time", e.getLocalizedMessage());
        }

    }

    @Test
    public void testAllGetFeedbackSessions() {

        testGetFeedbackSessions();
        testGetFeedbackSessionsForCourse();
        testGetFeedbackSessionsPossiblyNeedingOpenEmail();
        testGetFeedbackSessionsPossiblyNeedingClosingEmail();
        testGetFeedbackSessionsPossiblyNeedingClosedEmail();
        testGetFeedbackSessionsPossiblyNeedingPublishedEmail();
    }

    private void testGetFeedbackSessions() {

        ______TS("standard success case");

        FeedbackSessionAttributes expected =
                dataBundle.feedbackSessions.get("session1InCourse2");
        FeedbackSessionAttributes actual =
                fsDb.getFeedbackSession("idOfTypicalCourse2", "Private feedback session");

        assertEquals(expected.toString(), actual.toString());

        ______TS("non-existant session");

        assertNull(fsDb.getFeedbackSession("non-course", "Non-existant feedback session"));

        ______TS("null fsName");

        try {
            fsDb.getFeedbackSession("idOfTypicalCourse1", null);
            signalFailureToDetectException();
        } catch (AssertionError e) {
            AssertHelper.assertContains(Const.StatusCodes.DBLEVEL_NULL_INPUT, e.getLocalizedMessage());
        }

        ______TS("null courseId");

        try {
            fsDb.getFeedbackSession(null, "First feedback session");
            signalFailureToDetectException();
        } catch (AssertionError e) {
            AssertHelper.assertContains(Const.StatusCodes.DBLEVEL_NULL_INPUT, e.getLocalizedMessage());
        }

    }

    private void testGetFeedbackSessionsForCourse() {

        ______TS("standard success case");

        List<FeedbackSessionAttributes> sessions = fsDb.getFeedbackSessionsForCourse("idOfTypicalCourse1");

        String expected =
                dataBundle.feedbackSessions.get("session1InCourse1").toString() + Const.EOL
                + dataBundle.feedbackSessions.get("session2InCourse1").toString() + Const.EOL
                + dataBundle.feedbackSessions.get("empty.session").toString() + Const.EOL
                + dataBundle.feedbackSessions.get("awaiting.session").toString() + Const.EOL
                + dataBundle.feedbackSessions.get("closedSession").toString() + Const.EOL
                + dataBundle.feedbackSessions.get("gracePeriodSession").toString() + Const.EOL;

        for (FeedbackSessionAttributes session : sessions) {
            AssertHelper.assertContains(session.toString(), expected);
        }
        assertEquals(6, sessions.size());

        ______TS("null params");

        try {
            fsDb.getFeedbackSessionsForCourse(null);
            signalFailureToDetectException();
        } catch (AssertionError e) {
            AssertHelper.assertContains(Const.StatusCodes.DBLEVEL_NULL_INPUT, e.getLocalizedMessage());
        }

        ______TS("non-existant course");

        assertTrue(fsDb.getFeedbackSessionsForCourse("non-existant course").isEmpty());

        ______TS("no sessions in course");

        assertTrue(fsDb.getFeedbackSessionsForCourse("idOfCourseNoEvals").isEmpty());
    }

    private void testGetFeedbackSessionsPossiblyNeedingOpenEmail() {

        ______TS("standard success case");

        List<FeedbackSessionAttributes> fsaList = fsDb.getFeedbackSessionsPossiblyNeedingOpenEmail();

        assertEquals(1, fsaList.size());
        for (FeedbackSessionAttributes fsa : fsaList) {
            assertFalse(fsa.isSentOpenEmail());
        }

    }

    private void testGetFeedbackSessionsPossiblyNeedingClosingEmail() {

        ______TS("standard success case");

        List<FeedbackSessionAttributes> fsaList = fsDb.getFeedbackSessionsPossiblyNeedingClosingEmail();

        assertEquals(7, fsaList.size());
        for (FeedbackSessionAttributes fsa : fsaList) {
            assertFalse(fsa.isSentClosingEmail());
            assertTrue(fsa.isClosingEmailEnabled());
        }

    }

    private void testGetFeedbackSessionsPossiblyNeedingClosedEmail() {

        ______TS("standard success case");

        List<FeedbackSessionAttributes> fsaList = fsDb.getFeedbackSessionsPossiblyNeedingClosedEmail();

        assertEquals(7, fsaList.size());
        for (FeedbackSessionAttributes fsa : fsaList) {
            assertFalse(fsa.isSentClosedEmail());
            assertTrue(fsa.isClosingEmailEnabled());
        }

    }

    private void testGetFeedbackSessionsPossiblyNeedingPublishedEmail() {

        ______TS("standard success case");

        List<FeedbackSessionAttributes> fsaList = fsDb.getFeedbackSessionsPossiblyNeedingPublishedEmail();

        assertEquals(9, fsaList.size());
        for (FeedbackSessionAttributes fsa : fsaList) {
            assertFalse(fsa.isSentPublishedEmail());
            assertTrue(fsa.isPublishedEmailEnabled());
        }

    }

    @Test
    public void testUpdateFeedbackSession() throws Exception {

        ______TS("null params");
        try {
            fsDb.updateFeedbackSession(null);
            signalFailureToDetectException();
        } catch (AssertionError e) {
            AssertHelper.assertContains(Const.StatusCodes.DBLEVEL_NULL_INPUT, e.getLocalizedMessage());
        }
        ______TS("invalid feedback sesion attributes");
        FeedbackSessionAttributes invalidFs = getNewFeedbackSession();
        fsDb.deleteEntity(invalidFs);
        fsDb.createEntity(invalidFs);
        Calendar calendar = TimeHelper.dateToCalendar(invalidFs.getEndTime());
        calendar.add(Calendar.MONTH, 1);
        invalidFs.setStartTime(calendar.getTime());
        invalidFs.setResultsVisibleFromTime(calendar.getTime());
        try {
            fsDb.updateFeedbackSession(invalidFs);
            signalFailureToDetectException();
        } catch (InvalidParametersException e) {
            assertEquals(
                    String.format(TIME_FRAME_ERROR_MESSAGE, SESSION_END_TIME_FIELD_NAME,
                                  SESSION_START_TIME_FIELD_NAME),
                    e.getLocalizedMessage());
        }
        ______TS("feedback session does not exist");
        FeedbackSessionAttributes nonexistantFs = getNewFeedbackSession();
        nonexistantFs.setFeedbackSessionName("non existant fs");
        nonexistantFs.setCourseId("non.existant.course");
        try {
            fsDb.updateFeedbackSession(nonexistantFs);
            signalFailureToDetectException();
        } catch (EntityDoesNotExistException e) {
            AssertHelper.assertContains(FeedbackSessionsDb.ERROR_UPDATE_NON_EXISTENT, e.getLocalizedMessage());
        }
        ______TS("standard success case");
        FeedbackSessionAttributes modifiedSession = getNewFeedbackSession();
        fsDb.deleteEntity(modifiedSession);
        fsDb.createEntity(modifiedSession);
        verifyPresentInDatastore(modifiedSession);
        modifiedSession.setInstructions(new Text("new instructions"));
        modifiedSession.setGracePeriod(0);
        modifiedSession.setSentOpenEmail(false);
        fsDb.updateFeedbackSession(modifiedSession);
        verifyPresentInDatastore(modifiedSession);
    }

    private FeedbackSessionAttributes getNewFeedbackSession() {
        FeedbackSessionAttributes fsa = new FeedbackSessionAttributes();
        fsa.setFeedbackSessionType(FeedbackSessionType.STANDARD);
        fsa.setFeedbackSessionName("fsTest1");
        fsa.setCourseId("testCourse");
        fsa.setCreatorEmail("valid@email.com");
        fsa.setCreatedTime(new Date());
        fsa.setStartTime(new Date());
        fsa.setEndTime(new Date());
        fsa.setSessionVisibleFromTime(new Date());
        fsa.setResultsVisibleFromTime(new Date());
        fsa.setGracePeriod(5);
        fsa.setSentOpenEmail(true);
        fsa.setSentPublishedEmail(true);
        fsa.setInstructions(new Text("Give feedback."));
        return fsa;
    }

    @AfterClass
    public void classTearDown() {
        deleteSessionsFromDb();
    }

    private void deleteSessionsFromDb() {
        Set<String> keys = dataBundle.feedbackSessions.keySet();
        for (String i : keys) {
            fsDb.deleteEntity(dataBundle.feedbackSessions.get(i));
        }
        fsDb.deleteEntity(getNewFeedbackSession());
    }

}
