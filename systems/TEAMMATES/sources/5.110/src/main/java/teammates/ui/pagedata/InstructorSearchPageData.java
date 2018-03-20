package teammates.ui.pagedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.datatransfer.FeedbackResponseCommentSearchResultBundle;
import teammates.common.datatransfer.SectionDetailsBundle;
import teammates.common.datatransfer.StudentSearchResultBundle;
import teammates.common.datatransfer.TeamDetailsBundle;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseCommentAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;
import teammates.ui.template.FeedbackResponseCommentRow;
import teammates.ui.template.FeedbackSessionRow;
import teammates.ui.template.QuestionTable;
import teammates.ui.template.ResponseRow;
import teammates.ui.template.SearchCommentsForResponsesTable;
import teammates.ui.template.SearchStudentsTable;
import teammates.ui.template.StudentListSectionData;

/**
 * PageData: the data to be used in the InstructorSearchPage.
 */
public class InstructorSearchPageData extends PageData {
    private String searchKey = "";

    /* Whether checkbox is checked for search input */
    private boolean isSearchCommentForResponses;
    private boolean isSearchForStudents;

    /* Whether search results are empty */
    private boolean isCommentsForResponsesEmpty;
    private boolean isStudentsEmpty;

    /* Tables containing search results */
    private List<SearchCommentsForResponsesTable> searchCommentsForResponsesTables;
    private List<SearchStudentsTable> searchStudentsTables;

    public InstructorSearchPageData(AccountAttributes account, String sessionToken) {
        super(account, sessionToken);
    }

    public void init(FeedbackResponseCommentSearchResultBundle frcSearchResultBundle,
                     StudentSearchResultBundle studentSearchResultBundle,
                     String searchKey, boolean isSearchCommentForResponses, boolean isSearchForStudents) {

        this.searchKey = searchKey;

        this.isSearchCommentForResponses = isSearchCommentForResponses;
        this.isSearchForStudents = isSearchForStudents;

        this.isCommentsForResponsesEmpty = frcSearchResultBundle.numberOfResults == 0;
        this.isStudentsEmpty = studentSearchResultBundle.numberOfResults == 0;

        setSearchCommentsForResponsesTables(frcSearchResultBundle);
        setSearchStudentsTables(studentSearchResultBundle);
    }

    public String getSearchKey() {
        return sanitizeForHtml(searchKey);
    }

    public boolean isCommentsForResponsesEmpty() {
        return isCommentsForResponsesEmpty;
    }

    public boolean isStudentsEmpty() {
        return isStudentsEmpty;
    }

    public boolean isSearchCommentForResponses() {
        return isSearchCommentForResponses;
    }

    public boolean isSearchForStudents() {
        return isSearchForStudents;
    }

    public List<SearchCommentsForResponsesTable> getSearchCommentsForResponsesTables() {
        return searchCommentsForResponsesTables;
    }

    public List<SearchStudentsTable> getSearchStudentsTables() {
        return searchStudentsTables;
    }

    private void setSearchCommentsForResponsesTables(
                                    FeedbackResponseCommentSearchResultBundle frcSearchResultBundle) {

        searchCommentsForResponsesTables = new ArrayList<>();
        searchCommentsForResponsesTables.add(new SearchCommentsForResponsesTable(
                                               createFeedbackSessionRows(frcSearchResultBundle)));
    }

    private void setSearchStudentsTables(StudentSearchResultBundle studentSearchResultBundle) {

        searchStudentsTables = new ArrayList<>(); // 1 table for each course
        List<String> courseIdList = getCourseIdsFromStudentSearchResultBundle(studentSearchResultBundle);

        for (String courseId : courseIdList) {
            searchStudentsTables.add(new SearchStudentsTable(
                                       courseId, createStudentRows(courseId, studentSearchResultBundle)));
        }
    }

    private List<FeedbackSessionRow> createFeedbackSessionRows(
                                    FeedbackResponseCommentSearchResultBundle frcSearchResultBundle) {

        List<FeedbackSessionRow> rows = new ArrayList<>();

        for (String fsName : frcSearchResultBundle.questions.keySet()) {
            String courseId = frcSearchResultBundle.sessions.get(fsName).getCourseId();

            rows.add(new FeedbackSessionRow(fsName, courseId, createQuestionTables(
                                                                fsName, frcSearchResultBundle)));
        }
        return rows;
    }

    private List<QuestionTable> createQuestionTables(
                                    String fsName,
                                    FeedbackResponseCommentSearchResultBundle frcSearchResultBundle) {

        List<QuestionTable> questionTables = new ArrayList<>();
        List<FeedbackQuestionAttributes> questionList = frcSearchResultBundle.questions.get(fsName);

        for (FeedbackQuestionAttributes question : questionList) {
            int questionNumber = question.questionNumber;
            String questionText = question.getQuestionDetails().getQuestionText();
            String additionalInfo = question.getQuestionDetails()
                                            .getQuestionAdditionalInfoHtml(questionNumber, "");

            questionTables.add(new QuestionTable(questionNumber, questionText, additionalInfo,
                                            createResponseRows(question, frcSearchResultBundle)));
        }
        return questionTables;
    }

    private List<ResponseRow> createResponseRows(
                                    FeedbackQuestionAttributes question,
                                    FeedbackResponseCommentSearchResultBundle frcSearchResultBundle) {

        List<ResponseRow> rows = new ArrayList<>();
        List<FeedbackResponseAttributes> responseList = frcSearchResultBundle.responses.get(question.getId());

        for (FeedbackResponseAttributes responseEntry : responseList) {
            String giverName = frcSearchResultBundle.responseGiverTable.get(responseEntry.getId());
            String recipientName = frcSearchResultBundle.responseRecipientTable.get(responseEntry.getId());
            String response = responseEntry.getResponseDetails().getAnswerHtmlInstructorView(question.getQuestionDetails());

            rows.add(new ResponseRow(giverName, recipientName, response,
                                       createFeedbackResponseCommentRows(responseEntry, frcSearchResultBundle)));
        }
        return rows;
    }

    private List<FeedbackResponseCommentRow> createFeedbackResponseCommentRows(
                                    FeedbackResponseAttributes responseEntry,
                                    FeedbackResponseCommentSearchResultBundle frcSearchResultBundle) {

        List<FeedbackResponseCommentRow> rows = new ArrayList<>();
        List<FeedbackResponseCommentAttributes> frcList = frcSearchResultBundle
                                                              .comments.get(responseEntry.getId());

        for (FeedbackResponseCommentAttributes frc : frcList) {
            String frCommentGiver = frcSearchResultBundle
                                            .commentGiverTable.get(frc.getId().toString());
            if (!"Anonymous".equals(frCommentGiver)) {
                frCommentGiver = frc.giverEmail;
            }

            double sessionTimeZone = frcSearchResultBundle.sessions.get(responseEntry.feedbackSessionName).getTimeZone();
            FeedbackResponseCommentRow frcDiv = new FeedbackResponseCommentRow(frc, frCommentGiver,
                    frcSearchResultBundle.instructorEmailNameTable, sessionTimeZone);

            rows.add(frcDiv);
        }
        return rows;
    }

    private List<StudentListSectionData> createStudentRows(String courseId,
                                                           StudentSearchResultBundle studentSearchResultBundle) {
        List<StudentListSectionData> rows = new ArrayList<>();
        List<StudentAttributes> studentsInCourse = filterStudentsByCourse(
                                                       courseId, studentSearchResultBundle);
        Map<String, List<String>> sectionNameToTeamNameMap = new HashMap<>();
        Map<String, List<StudentAttributes>> teamNameToStudentsMap = new HashMap<>();
        Map<String, String> emailToPhotoUrlMap = new HashMap<>();
        for (StudentAttributes student : studentsInCourse) {
            String teamName = student.team;
            String sectionName = student.section;
            String viewPhotoLink = addUserIdToUrl(student.getPublicProfilePictureUrl());
            emailToPhotoUrlMap.put(student.email, viewPhotoLink);
            if (!teamNameToStudentsMap.containsKey(teamName)) {
                teamNameToStudentsMap.put(teamName, new ArrayList<StudentAttributes>());
            }
            teamNameToStudentsMap.get(teamName).add(student);
            if (!sectionNameToTeamNameMap.containsKey(sectionName)) {
                sectionNameToTeamNameMap.put(sectionName, new ArrayList<String>());
            }
            if (!sectionNameToTeamNameMap.get(sectionName).contains(teamName)) {
                sectionNameToTeamNameMap.get(sectionName).add(teamName);
            }
        }
        List<SectionDetailsBundle> sections = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : sectionNameToTeamNameMap.entrySet()) {
            SectionDetailsBundle sdb = new SectionDetailsBundle();
            sdb.name = entry.getKey();
            ArrayList<TeamDetailsBundle> teams = new ArrayList<>();
            for (String teamName : entry.getValue()) {
                TeamDetailsBundle tdb = new TeamDetailsBundle();
                tdb.name = teamName;
                tdb.students = teamNameToStudentsMap.get(teamName);
                teams.add(tdb);
            }
            sdb.teams = teams;
            sections.add(sdb);
        }
        for (SectionDetailsBundle section : sections) {
            InstructorAttributes instructor = studentSearchResultBundle.courseIdInstructorMap.get(courseId);
            boolean isAllowedToViewStudentInSection =
                    instructor.isAllowedForPrivilege(
                            section.name, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS);
            boolean isAllowedToModifyStudent =
                    instructor.isAllowedForPrivilege(
                            section.name, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);
            rows.add(new StudentListSectionData(section, isAllowedToViewStudentInSection,
                                                isAllowedToModifyStudent,
                                                emailToPhotoUrlMap, account.googleId, getSessionToken()));
        }
        return rows;
    }

    private List<String> getCourseIdsFromStudentSearchResultBundle(
                                    StudentSearchResultBundle studentSearchResultBundle) {
        List<String> courses = new ArrayList<>();

        for (StudentAttributes student : studentSearchResultBundle.studentList) {
            String course = student.course;
            if (!courses.contains(course)) {
                courses.add(course);
            }
        }
        return courses;
    }

    /**
     * Filters students from studentSearchResultBundle by course ID.
     * @return students whose course ID is equal to the courseId given in the parameter
     */
    private List<StudentAttributes> filterStudentsByCourse(
                                    String courseId,
                                    StudentSearchResultBundle studentSearchResultBundle) {

        List<StudentAttributes> students = new ArrayList<>();

        for (StudentAttributes student : studentSearchResultBundle.studentList) {
            if (courseId.equals(student.course)) {
                students.add(student);
            }
        }
        return students;
    }

}
