<%@ tag description="studentFeedbackResults.jsp - Student feedback results question with responses" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/student/feedbackResults" prefix="feedbackResults" %>
<%@ attribute name="questionWithResponses" type="teammates.ui.template.StudentFeedbackResultsQuestionWithResponses" required="true" %>

<div class="panel panel-default">
  <div class="panel-heading">
    <%-- Note: When an element has class text-preserve-space, do not insert HTML spaces --%>
    <h4>Question ${questionWithResponses.questionDetails.questionIndex}: <span class="text-preserve-space"><c:out value="${questionWithResponses.questionDetails.questionText}"/>${questionWithResponses.questionDetails.additionalInfo}</span></h4>

    ${questionWithResponses.questionDetails.questionResultStatistics}

    <c:if test="${questionWithResponses.questionDetails.individualResponsesShownToStudents}">

      <c:forEach items="${questionWithResponses.responseTables}" var="responseTable">
        <feedbackResults:responseTable responseTable="${responseTable}"/>
      </c:forEach>

    </c:if>
  </div>
</div>
<br>
