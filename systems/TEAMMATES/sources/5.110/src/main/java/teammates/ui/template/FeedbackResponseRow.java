package teammates.ui.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teammates.common.datatransfer.FeedbackParticipantType;
import teammates.common.datatransfer.FeedbackSessionResultsBundle;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseCommentAttributes;
import teammates.common.datatransfer.questions.FeedbackQuestionDetails;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StringHelper;

public class FeedbackResponseRow {

    private int questionNumber;
    private String questionText;
    private String questionMoreInfo;
    private String responseText;
    private List<FeedbackResponseCommentRow> responseComments;

    public FeedbackResponseRow(int fbIndex, int personIndex, String personType,
                               FeedbackResponseAttributes response, FeedbackSessionResultsBundle results) {
        String questionId = response.feedbackQuestionId;
        FeedbackQuestionAttributes question = results.questions.get(questionId);
        FeedbackQuestionDetails questionDetails = question.getQuestionDetails();
        this.questionNumber = question.questionNumber;
        this.questionText = results.getQuestionText(questionId);
        this.questionMoreInfo = questionDetails.getQuestionAdditionalInfoHtml(this.questionNumber,
                                                                              personType + "-" + personIndex
                                                                                         + "-session-" + fbIndex);
        if ("recipient".equals(personType)) {
            this.responseText = response.getResponseDetails().getAnswerHtmlInstructorView(questionDetails);
        } else if ("giver".equals(personType)) {
            this.responseText = results.getResponseAnswerHtml(response, question);
        }
        this.responseComments = new ArrayList<>();
        List<FeedbackResponseCommentAttributes> frcs = results.responseComments.get(response.getId());

        Map<FeedbackParticipantType, Boolean> responseVisibilities = new HashMap<>();

        for (FeedbackParticipantType participant : question.showResponsesTo) {
            responseVisibilities.put(participant, true);
        }
        String giverName = results.getNameForEmail(response.giver);
        if (frcs != null) {
            for (FeedbackResponseCommentAttributes frc : frcs) {
                String showCommentTo = StringHelper.removeEnclosingSquareBrackets(frc.showCommentTo.toString());
                String showGiverNameToString = StringHelper.removeEnclosingSquareBrackets(frc.showGiverNameTo.toString());
                String recipientName = results.getNameForEmail(response.recipient);
                String giverEmail = frc.giverEmail;
                Map<String, String> instructorEmailNameTable = results.instructorEmailNameTable;
                FeedbackResponseCommentRow responseRow = new FeedbackResponseCommentRow(frc,
                        giverEmail, giverName, recipientName, showCommentTo, showGiverNameToString, responseVisibilities,
                        instructorEmailNameTable, results.getTimeZone());
                String whoCanSeeComment = null;
                boolean isVisibilityIconShown = false;
                if (results.feedbackSession.isPublished()) {
                    boolean isResponseCommentPublicToRecipient = !frc.showCommentTo.isEmpty();
                    isVisibilityIconShown = isResponseCommentPublicToRecipient;

                    if (isVisibilityIconShown) {
                        whoCanSeeComment = getTypeOfPeopleCanViewComment(frc, question);
                    }
                }
                responseRow.setVisibilityIcon(isVisibilityIconShown, whoCanSeeComment);
                responseRow.enableEditDelete();
                this.responseComments.add(responseRow);
            }
        }
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionText() {
        return SanitizationHelper.sanitizeForHtml(questionText);
    }

    public String getQuestionMoreInfo() {
        return questionMoreInfo;
    }

    public String getResponseText() {
        return responseText;
    }

    public List<FeedbackResponseCommentRow> getResponseComments() {
        return responseComments;
    }

    /**
     * Returns the type of people that can view the response comment.
     */
    public String getTypeOfPeopleCanViewComment(FeedbackResponseCommentAttributes comment,
                                                FeedbackQuestionAttributes relatedQuestion) {
        StringBuilder peopleCanView = new StringBuilder(100);
        List<FeedbackParticipantType> showCommentTo;
        if (comment.isVisibilityFollowingFeedbackQuestion) {
            showCommentTo = relatedQuestion.showResponsesTo;
        } else {
            showCommentTo = comment.showCommentTo;
        }
        for (int i = 0; i < showCommentTo.size(); i++) {
            FeedbackParticipantType commentViewer = showCommentTo.get(i);
            if (i == showCommentTo.size() - 1 && showCommentTo.size() > 1) {
                peopleCanView.append("and ");
            }

            switch (commentViewer) {
            case GIVER:
                peopleCanView.append("response giver, ");
                break;
            case RECEIVER:
                peopleCanView.append("response recipient, ");
                break;
            case OWN_TEAM:
                peopleCanView.append("response giver's team, ");
                break;
            case RECEIVER_TEAM_MEMBERS:
                peopleCanView.append("response recipient's team, ");
                break;
            case STUDENTS:
                peopleCanView.append("other students in this course, ");
                break;
            case INSTRUCTORS:
                peopleCanView.append("instructors, ");
                break;
            default:
                break;
            }
        }
        String peopleCanViewString = peopleCanView.toString();
        return removeEndComma(peopleCanViewString);
    }

    private String removeEndComma(String str) {
        return str.substring(0, str.length() - 2);
    }
}
