package teammates.common.datatransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;

public class FeedbackSessionQuestionsBundle {

    public FeedbackSessionAttributes feedbackSession;
    public Map<FeedbackQuestionAttributes, List<FeedbackResponseAttributes>> questionResponseBundle;
    public Map<String, Map<String, String>> recipientList;

    public FeedbackSessionQuestionsBundle(FeedbackSessionAttributes feedbackSession,
            Map<FeedbackQuestionAttributes, List<FeedbackResponseAttributes>> questionResponseBundle,
            Map<String, Map<String, String>> recipientList) {
        this.feedbackSession = feedbackSession;
        this.questionResponseBundle = questionResponseBundle;
        this.recipientList = recipientList;
    }

    public Map<FeedbackQuestionAttributes, List<FeedbackResponseAttributes>> getQuestionResponseBundle() {
        return questionResponseBundle;
    }

    public FeedbackSessionAttributes getFeedbackSession() {
        return feedbackSession;
    }

    /**
     * Gets the list of questions in this bundle, sorted by question number.
     * @return A {@code List} of {@code FeedackQuestionAttributes}.
     */
    public List<FeedbackQuestionAttributes> getSortedQuestions() {
        List<FeedbackQuestionAttributes> sortedQuestions =
                new ArrayList<>(this.questionResponseBundle.keySet());

        Collections.sort(sortedQuestions);

        return sortedQuestions;
    }

    /**
     * Gets the question in the data bundle with id == questionId.
     * @return a FeedbackQuestionAttribute with the specified questionId
     */
    public FeedbackQuestionAttributes getQuestionAttributes(String questionId) {
        List<FeedbackQuestionAttributes> questions =
                new ArrayList<>(this.questionResponseBundle.keySet());

        for (FeedbackQuestionAttributes question : questions) {
            if (question.getId().equals(questionId)) {
                return question;
            }
        }

        return null;
    }

    /**
     * Gets the recipient list for a question, sorted by the recipient's name.
     * @param feedbackQuestionId of the question
     * @return A {@code Map<String key, String value>} where {@code key} is the recipient's email
     *         and {@code value} is the recipients name.
     */
    public Map<String, String> getSortedRecipientList(String feedbackQuestionId) {

        List<Map.Entry<String, String>> sortedList = new ArrayList<>(recipientList.get(feedbackQuestionId).entrySet());

        Collections.sort(sortedList, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                // Sort by value (name).
                int compare = o1.getValue().compareTo(o2.getValue());
                // Sort by key (email) if name is same.
                return compare == 0 ? o1.getKey().compareTo(o2.getKey()) : compare;
            }
        });

        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : sortedList) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public Set<String> getRecipientEmails(String feedbackQuestionId) {
        List<Map.Entry<String, String>> emailList = new ArrayList<>(recipientList.get(feedbackQuestionId).entrySet());

        HashSet<String> result = new HashSet<>();

        for (Map.Entry<String, String> entry : emailList) {
            result.add(entry.getKey());
        }

        return result;
    }

    /**
     * Removes question from the bundle if the question has givers or recipients that are anonymous to the instructor
     * or responses that are hidden from the instructor.
     */
    public void hideUnmoderatableQuestions() {
        List<FeedbackQuestionAttributes> questionsToHide = new ArrayList<>();

        for (FeedbackQuestionAttributes question : questionResponseBundle.keySet()) {
            boolean isGiverVisibleToInstructor = question.showGiverNameTo.contains(FeedbackParticipantType.INSTRUCTORS);
            boolean isRecipientVisibleToInstructor =
                    question.showRecipientNameTo.contains(FeedbackParticipantType.INSTRUCTORS);
            boolean isResponseVisibleToInstructor = question.showResponsesTo.contains(FeedbackParticipantType.INSTRUCTORS);

            if (!isResponseVisibleToInstructor || !isGiverVisibleToInstructor || !isRecipientVisibleToInstructor) {
                questionsToHide.add(question);
                questionResponseBundle.put(question, new ArrayList<FeedbackResponseAttributes>());
            }
        }

        questionResponseBundle.keySet().removeAll(questionsToHide);
    }

    /**
     * Empties responses for all questions in this bundle.
     * Used to not show existing responses when previewing as instructor
     */
    public void resetAllResponses() {
        for (FeedbackQuestionAttributes question : questionResponseBundle.keySet()) {
            questionResponseBundle.put(question, new ArrayList<FeedbackResponseAttributes>());
        }
    }
}
