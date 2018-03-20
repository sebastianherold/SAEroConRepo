package teammates.test.cases.browsertests;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.test.driver.BackDoor;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.StudentHelpPage;
import teammates.test.pageobjects.StudentHomePage;

/**
 * SUT: {@link Const.ActionURIs#STUDENT_HOME_PAGE}.
 */
public class StudentHomePageUiTest extends BaseUiTestCase {
    private StudentHomePage studentHome;

    @Override
    protected void prepareTestData() {
        testData = loadDataBundle("/StudentHomePageUiTest.json");

        // use the 1st student account injected for this test

        String student1GoogleId = TestProperties.TEST_STUDENT1_ACCOUNT;
        String student1Email = student1GoogleId + "@gmail.com";
        testData.accounts.get("alice.tmms").googleId = student1GoogleId;
        testData.accounts.get("alice.tmms").email = student1Email;
        testData.students.get("alice.tmms@SHomeUiT.CS2104").email = student1Email;
        testData.students.get("alice.tmms@SHomeUiT.CS1101").googleId = student1GoogleId;
        testData.students.get("alice.tmms@SHomeUiT.CS1101").email = student1Email;
        testData.students.get("alice.tmms@SHomeUiT.CS4215").googleId = student1GoogleId;
        testData.students.get("alice.tmms@SHomeUiT.CS4215").email = student1Email;
        testData.students.get("alice.tmms@SHomeUiT.CS4221").googleId = student1GoogleId;
        testData.students.get("alice.tmms@SHomeUiT.CS4221").email = student1Email;

        removeAndRestoreDataBundle(testData);

        FeedbackSessionAttributes gracedFeedbackSession =
                BackDoor.getFeedbackSession("SHomeUiT.CS2104", "Graced Feedback Session");
        gracedFeedbackSession.setEndTime(TimeHelper.getDateOffsetToCurrentTime(0));
        BackDoor.editFeedbackSession(gracedFeedbackSession);
    }

    @Test
    public void allTests() throws Exception {
        testContentAndLogin();
        testLinks();
        testLinkAndContentAfterDelete();
    }

    private void testContentAndLogin() throws Exception {

        ______TS("content: no courses, 'welcome stranger' message");

        String unregUserId = TestProperties.TEST_UNREG_ACCOUNT;
        String unregPassword = TestProperties.TEST_UNREG_PASSWORD;
        BackDoor.deleteAccount(unregUserId); //delete account if it exists

        logout();
        studentHome = getHomePage().clickStudentLogin()
                                   .loginAsStudent(unregUserId, unregPassword);

        // this test uses the accounts from test.properties
        // do not do full HTML verification here as the unregistered username is not predictable
        studentHome.verifyHtmlMainContent("/studentHomeHTMLEmpty.html");

        ______TS("persistence check");

        loginWithPersistenceProblem();

        // This is the full HTML verification for Student Home Page, the rest can all be verifyMainHtml
        studentHome.verifyHtml("/studentHomeHTMLPersistenceCheck.html");

        ______TS("login");

        studentHome = getHomePage().clickStudentLogin()
                                   .loginAsStudent(TestProperties.TEST_STUDENT1_ACCOUNT,
                                                   TestProperties.TEST_STUDENT1_PASSWORD);

        ______TS("content: multiple courses");

        // this test uses the accounts from test.properties
        studentHome.verifyHtmlMainContent("/studentHomeHTML.html");

        AppUrl detailsPageUrl = createUrl(Const.ActionURIs.STUDENT_HOME_PAGE)
                             .withUserId(testData.students.get("SHomeUiT.charlie.d@SHomeUiT.CS2104").googleId);

        StudentHomePage studentHomePage = loginAdminToPage(detailsPageUrl, StudentHomePage.class);

        studentHomePage.verifyHtmlMainContent("/studentHomeTypicalHTML.html");

        ______TS("content: requires sanitization");

        detailsPageUrl = createUrl(Const.ActionURIs.STUDENT_HOME_PAGE)
                            .withUserId(testData.students.get("SHomeUiT.student1InTestingSanitizationCourse").googleId);

        studentHomePage = loginAdminToPage(detailsPageUrl, StudentHomePage.class);

        studentHomePage.verifyHtmlMainContent("/studentHomeTypicalTestingSanitization.html");
    }

    private void testLinks() {

        AppUrl homePageUrl = createUrl(Const.ActionURIs.STUDENT_HOME_PAGE)
                .withUserId(testData.students.get("SHomeUiT.charlie.d@SHomeUiT.CS2104").googleId);

        StudentHomePage studentHomePage = loginAdminToPage(homePageUrl, StudentHomePage.class);

        ______TS("link: help page");

        StudentHelpPage helpPage = studentHomePage.loadStudentHelpTab();
        helpPage.closeCurrentWindowAndSwitchToParentWindow();

        ______TS("link: view team link");

        studentHomePage.clickViewTeam();

        AppUrl detailsPageUrl = createUrl(Const.ActionURIs.STUDENT_COURSE_DETAILS_PAGE)
                .withUserId(testData.students.get("SHomeUiT.charlie.d@SHomeUiT.CS1101").googleId)
                .withCourseId(testData.students.get("SHomeUiT.charlie.d@SHomeUiT.CS1101").course);
        assertEquals(detailsPageUrl.toAbsoluteString(), browser.driver.getCurrentUrl());
        studentHomePage.loadStudentHomeTab();

        ______TS("link: link of published feedback");

        studentHomePage.clickViewFeedbackButton("Closed Feedback Session");
        studentHomePage.reloadPage();
        String pageSource = browser.driver.getPageSource();
        assertTrue(pageSource.contains("Feedback Results"));
        assertTrue(pageSource.contains("SHomeUiT.CS2104"));
        assertTrue(pageSource.contains("Closed Feedback Session"));
        studentHomePage.loadStudentHomeTab();

        studentHomePage.clickSubmitFeedbackButton("Closed Feedback Session");
        studentHomePage.reloadPage();
        pageSource = browser.driver.getPageSource();
        assertTrue(pageSource.contains("Submit Feedback"));
        assertTrue(pageSource.contains("SHomeUiT.CS2104"));
        assertTrue(pageSource.contains("Closed Feedback Session"));
        assertTrue(pageSource.contains(Const.StatusMessages.FEEDBACK_SUBMISSIONS_NOT_OPEN));
        studentHomePage.loadStudentHomeTab();

        ______TS("link: link of Grace period feedback");

        assertEquals("true", studentHomePage.getViewFeedbackButton("Graced Feedback Session").getAttribute("disabled"));

        studentHomePage.clickSubmitFeedbackButton("Graced Feedback Session");
        studentHomePage.reloadPage();
        pageSource = browser.driver.getPageSource();
        assertTrue(pageSource.contains("Submit Feedback"));
        assertTrue(pageSource.contains("SHomeUiT.CS2104"));
        assertTrue(pageSource.contains("Graced Feedback Session"));
        assertTrue(pageSource.contains(Const.StatusMessages.FEEDBACK_SUBMISSIONS_NOT_OPEN));
        studentHomePage.loadStudentHomeTab();

        ______TS("link: link of pending feedback");

        assertEquals("true", studentHomePage.getViewFeedbackButton("First Feedback Session").getAttribute("disabled"));

        studentHomePage.clickSubmitFeedbackButton("First Feedback Session");
        studentHomePage.reloadPage();
        pageSource = browser.driver.getPageSource();
        assertTrue(pageSource.contains("Submit Feedback"));
        assertTrue(pageSource.contains("SHomeUiT.CS2104"));
        assertTrue(pageSource.contains("First Feedback Session"));
        studentHomePage.loadStudentHomeTab();
    }

    private void testLinkAndContentAfterDelete() throws Exception {

        AppUrl detailsPageUrl = createUrl(Const.ActionURIs.STUDENT_HOME_PAGE)
                             .withUserId(testData.students.get("SHomeUiT.charlie.d@SHomeUiT.CS2104").googleId);

        StudentHomePage studentHomePage = loginAdminToPage(detailsPageUrl, StudentHomePage.class);

        ______TS("access the feedback session exactly after it is deleted");

        BackDoor.deleteFeedbackSession("First Feedback Session", "SHomeUiT.CS2104");
        studentHomePage.clickSubmitFeedbackButton("First Feedback Session");
        studentHomePage.waitForPageToLoad();
        studentHomePage.verifyHtmlMainContent("/studentHomeFeedbackDeletedHTML.html");

    }

    private void loginWithPersistenceProblem() {
        AppUrl homeUrl = ((AppUrl) createUrl(Const.ActionURIs.STUDENT_HOME_PAGE)
                    .withParam(Const.ParamsNames.CHECK_PERSISTENCE_COURSE, "SHomeUiT.CS2104"))
                    .withUserId("unreg_user");

        studentHome = loginAdminToPage(homeUrl, StudentHomePage.class);

    }

}
