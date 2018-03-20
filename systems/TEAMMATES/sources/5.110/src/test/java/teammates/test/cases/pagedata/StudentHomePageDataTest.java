package teammates.test.cases.pagedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import teammates.common.datatransfer.CourseDetailsBundle;
import teammates.common.datatransfer.FeedbackSessionDetailsBundle;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.test.cases.BaseTestCase;
import teammates.test.driver.TimeHelperExtension;
import teammates.ui.pagedata.StudentHomePageData;
import teammates.ui.template.CourseTable;
import teammates.ui.template.ElementTag;
import teammates.ui.template.HomeFeedbackSessionRow;
import teammates.ui.template.StudentFeedbackSessionActions;
import teammates.ui.template.StudentHomeFeedbackSessionRow;

/**
 * SUT: {@link StudentHomePageData}.
 */
public class StudentHomePageDataTest extends BaseTestCase {
    private List<CourseDetailsBundle> courses;

    private FeedbackSessionAttributes submittedSession;
    private FeedbackSessionAttributes pendingSession;
    private FeedbackSessionAttributes awaitingSession;

    private FeedbackSessionAttributes publishedSession;
    private FeedbackSessionAttributes closedSession;
    private FeedbackSessionAttributes submittedClosedSession;

    private Map<FeedbackSessionAttributes, String> tooltipTextMap;
    private Map<FeedbackSessionAttributes, String> buttonTextMap;

    @Test
    public void allTests() {
        StudentHomePageData data = createData();
        testCourseTables(data.getCourseTables());
    }

    private void testCourseTables(List<CourseTable> courseTables) {
        assertEquals(courses.size(), courseTables.size());

        CourseDetailsBundle newCourse = courses.get(0);
        CourseTable newCourseTable = courseTables.get(0);

        testCourseTableMeta(newCourse.course, newCourseTable);
        testNewCourseTable(newCourse, newCourseTable);

        CourseDetailsBundle oldCourse = courses.get(1);
        CourseTable oldCourseTable = courseTables.get(1);

        testCourseTableMeta(oldCourse.course, oldCourseTable);
        testOldCourseTable(oldCourse, oldCourseTable);
    }

    private void testCourseTableMeta(CourseAttributes course, CourseTable table) {
        assertEquals(course.getId(), table.getCourseId());
        assertEquals(course.getName(), table.getCourseName());
        assertEquals(1, table.getButtons().size());
        testViewTeamButton(table.getButtons().get(0), course.getId());
    }

    private void testViewTeamButton(ElementTag tag, String courseId) {
        assertEquals("View Team", tag.getContent());
        assertEquals(2, tag.getAttributes().size());
        assertTrue(tag.getAttributes().get("href").startsWith(Const.ActionURIs.STUDENT_COURSE_DETAILS_PAGE));
        assertTrue(tag.getAttributes().get("href").endsWith(Const.ParamsNames.COURSE_ID + "=" + courseId));
        assertEquals(Const.Tooltips.STUDENT_COURSE_DETAILS, tag.getAttributes().get("title"));
    }

    private void testNewCourseTable(CourseDetailsBundle newCourse, CourseTable courseTable) {
        assertEquals(newCourse.feedbackSessions.size(), courseTable.getRows().size());
        List<HomeFeedbackSessionRow> sessions = courseTable.getRows();
        HomeFeedbackSessionRow submittedRow = sessions.get(0);
        HomeFeedbackSessionRow pendingRow = sessions.get(1);
        HomeFeedbackSessionRow awaitingRow = sessions.get(2);

        int index = 0;

        testFeedbackSession(index++, submittedRow, submittedSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_SUBMITTED, "Submitted");
        testFeedbackSession(index++, pendingRow, pendingSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_PENDING, "Pending");
        testFeedbackSession(index++, awaitingRow, awaitingSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_AWAITING, "Awaiting");
    }

    private void testOldCourseTable(CourseDetailsBundle oldCourse, CourseTable courseTable) {
        // Sessions in old course have multiple messages in tooltip as their end dates have passed.
        assertEquals(oldCourse.feedbackSessions.size(), courseTable.getRows().size());
        List<HomeFeedbackSessionRow> sessions = courseTable.getRows();
        HomeFeedbackSessionRow publishedRow = sessions.get(0);
        HomeFeedbackSessionRow closedRow = sessions.get(1);
        HomeFeedbackSessionRow submittedClosedRow = sessions.get(2);

        int accumlativeOffset = courses.get(0).feedbackSessions.size();
        int index = 0 + accumlativeOffset;

        testFeedbackSession(index++, publishedRow, publishedSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_PENDING
                                + Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_CLOSED
                                + Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_PUBLISHED,
                            "Published");
        testFeedbackSession(index++, closedRow, closedSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_PENDING
                                + Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_CLOSED,
                            "Closed");
        testFeedbackSession(index++, submittedClosedRow, submittedClosedSession,
                            Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_SUBMITTED
                                + Const.Tooltips.STUDENT_FEEDBACK_SESSION_STATUS_CLOSED,
                            "Closed");
    }

    private void testFeedbackSession(int index, HomeFeedbackSessionRow row, FeedbackSessionAttributes session,
            String expectedTooltip, String expectedStatus) {
        StudentHomeFeedbackSessionRow studentRow = (StudentHomeFeedbackSessionRow) row;
        assertEquals(session.getFeedbackSessionName(), studentRow.getName());
        assertEquals(TimeHelper.formatTime12H(session.getEndTime()), studentRow.getEndTime());
        assertEquals(expectedTooltip, studentRow.getTooltip());
        assertEquals(expectedStatus, studentRow.getStatus());
        assertEquals(index, studentRow.getIndex());
        testActions(studentRow.getActions(), session);
    }

    private void testActions(StudentFeedbackSessionActions actions, FeedbackSessionAttributes session) {
        assertEquals(session.isVisible(), actions.isSessionVisible());
        assertEquals(session.isPublished(), actions.isSessionPublished());
        assertEquals(tooltipTextMap.get(session), actions.getTooltipText());
        assertEquals(buttonTextMap.get(session), actions.getButtonText());
    }

    private StudentHomePageData createData() {
        // Courses
        CourseAttributes course1 = new CourseAttributes("course-id-1", "old-course", "UTC");
        CourseAttributes course2 = new CourseAttributes("course-id-2", "new-course", "UTC");

        // Feedback sessions
        submittedSession = createFeedbackSession("submitted session", -1, 1, 1);
        pendingSession = createFeedbackSession("pending session", -1, 1, 1);
        awaitingSession = createFeedbackSession("awaiting session", 1, 2, 1);
        publishedSession = createFeedbackSession("published session", -1, -1, -1);
        closedSession = createFeedbackSession("closed session", -2, -1, 1);
        submittedClosedSession = createFeedbackSession("submitted closed session", -2, -1, 1);

        // Submission status
        Map<FeedbackSessionAttributes, Boolean> sessionSubmissionStatusMap = new HashMap<>();
        sessionSubmissionStatusMap.put(submittedSession, true);
        sessionSubmissionStatusMap.put(pendingSession, false);
        sessionSubmissionStatusMap.put(awaitingSession, false);
        sessionSubmissionStatusMap.put(publishedSession, false);
        sessionSubmissionStatusMap.put(closedSession, false);
        sessionSubmissionStatusMap.put(submittedClosedSession, true);

        // Tooltip and button texts
        tooltipTextMap = new HashMap<>();
        buttonTextMap = new HashMap<>();
        tooltipTextMap.put(submittedSession, Const.Tooltips.FEEDBACK_SESSION_EDIT_SUBMITTED_RESPONSE);
        buttonTextMap.put(submittedSession, "Edit Submission");
        tooltipTextMap.put(pendingSession, Const.Tooltips.FEEDBACK_SESSION_SUBMIT);
        buttonTextMap.put(pendingSession, "Start Submission");
        tooltipTextMap.put(awaitingSession, Const.Tooltips.FEEDBACK_SESSION_AWAITING);
        buttonTextMap.put(awaitingSession, "Start Submission");
        tooltipTextMap.put(publishedSession, Const.Tooltips.FEEDBACK_SESSION_VIEW_SUBMITTED_RESPONSE);
        buttonTextMap.put(publishedSession, "View Submission");
        tooltipTextMap.put(closedSession, Const.Tooltips.FEEDBACK_SESSION_VIEW_SUBMITTED_RESPONSE);
        buttonTextMap.put(closedSession, "View Submission");
        tooltipTextMap.put(submittedClosedSession, Const.Tooltips.FEEDBACK_SESSION_VIEW_SUBMITTED_RESPONSE);
        buttonTextMap.put(submittedClosedSession, "View Submission");

        // Packing into bundles
        CourseDetailsBundle newCourseBundle = createCourseBundle(
                course1, submittedSession, pendingSession, awaitingSession);

        CourseDetailsBundle oldCourseBundle = createCourseBundle(
                course2, publishedSession, closedSession, submittedClosedSession);

        courses = new ArrayList<>();
        courses.add(newCourseBundle);
        courses.add(oldCourseBundle);

        return new StudentHomePageData(new AccountAttributes(), dummySessionToken, courses, sessionSubmissionStatusMap);
    }

    private FeedbackSessionAttributes createFeedbackSession(String name,
            int offsetStart, int offsetEnd, int offsetPublish) {
        FeedbackSessionAttributes session = new FeedbackSessionAttributes();
        session.setFeedbackSessionName(name);
        session.setStartTime(TimeHelperExtension.getHoursOffsetToCurrentTime(offsetStart));
        session.setEndTime(TimeHelperExtension.getHoursOffsetToCurrentTime(offsetEnd));
        session.setResultsVisibleFromTime(TimeHelperExtension.getHoursOffsetToCurrentTime(offsetPublish));
        session.setSessionVisibleFromTime(TimeHelperExtension.getHoursOffsetToCurrentTime(-1));
        return session;
    }

    private CourseDetailsBundle createCourseBundle(CourseAttributes course, FeedbackSessionAttributes... sessions) {
        CourseDetailsBundle courseBundle = new CourseDetailsBundle(course);
        for (FeedbackSessionAttributes session : sessions) {
            courseBundle.feedbackSessions.add(new FeedbackSessionDetailsBundle(session));
        }
        return courseBundle;
    }
}
