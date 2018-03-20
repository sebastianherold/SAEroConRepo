package teammates.common.datatransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackSessionResponseStatus {
    public List<String> noResponse;
    public List<String> studentsWhoResponded;
    public Map<String, String> emailNameTable;
    public Map<String, String> emailSectionTable;
    public Map<String, String> emailTeamNameTable;

    // Sorts by teamName > studentName
    private Comparator<String> compareByTeamNameStudentName = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            int order = compareByTeamName.compare(s1, s2);
            if (order != 0) {
                // Sorting can be done at team name level
                return order;
            }
            // Both s1, s2 are in the same team
            // Sorting has to be done at name level
            return compareByName.compare(s1, s2);
        }

    };

    // Sorts by teamName
    private Comparator<String> compareByTeamName = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            // Compare between instructor and student
            // Instructor should be at higher order compared to student
            String teamName1 = emailTeamNameTable.get(s1);
            String teamName2 = emailTeamNameTable.get(s2);
            boolean isTeamName1Instructor = teamName1 == null;
            boolean isTeamName2Instructor = teamName2 == null;

            if (isTeamName1Instructor && !isTeamName2Instructor) {
                // Team 1 has higher sorting order when team 1 belongs instructor and team 2 belongs to student
                // -1 represents team 1 is at higher order
                return -1;
            } else if (!isTeamName1Instructor && isTeamName2Instructor) {
                // Team 2 has higher sorting order when team 2 belongs instructor and team 1 belongs to student
                // 1 represents team 2 is at higher order
                return 1;
            } else if (isTeamName1Instructor && isTeamName2Instructor) {
                // Both teams belong to instructor
                // Therefore team name 1 and 2 are the same, which is indicated by 0
                return compareByName.compare(s1, s2);
            } else {
                // Both teams belong to student
                // Compare on team names
                return teamName1.compareToIgnoreCase(teamName2);
            }
        }

    };

    // Sorts by studentName
    private Comparator<String> compareByName = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            // Compare on names
            String name1 = emailNameTable.get(s1);
            String name2 = emailNameTable.get(s2);
            return name1.compareToIgnoreCase(name2);
        }

    };

    public FeedbackSessionResponseStatus() {
        noResponse = new ArrayList<>();
        studentsWhoResponded = new ArrayList<>();
        emailNameTable = new HashMap<>();
        emailSectionTable = new HashMap<>();
        emailTeamNameTable = new HashMap<>();
    }

    /**
     * Returns list of students who did not respond to the feedback session
     * sorted by teamName > studentNamelist.
     */
    public List<String> getStudentsWhoDidNotRespondToAnyQuestion() {
        Collections.sort(noResponse, compareByTeamNameStudentName);
        return noResponse;
    }

    /**
     * Returns list of students who did not respond to the feedback session.
     */
    public List<String> getNoResponse() {
        return noResponse;
    }

    /**
     * Returns list of students who responded to the feedback session.
     */
    public List<String> getStudentsWhoResponded() {
        return studentsWhoResponded;
    }

    public Map<String, String> getEmailNameTable() {
        return emailNameTable;
    }

    public Map<String, String> getEmailTeamNameTable() {
        return emailTeamNameTable;
    }

    public Map<String, String> getEmailSectionTable() {
        return emailSectionTable;
    }
}
