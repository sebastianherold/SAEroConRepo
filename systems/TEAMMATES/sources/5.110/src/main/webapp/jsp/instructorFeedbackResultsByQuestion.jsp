<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/instructor/results" prefix="results" %>
<c:set var="jsIncludes">
  <script type="text/javascript" src="/js/instructorFeedbackResultsQuestion.js"></script>
</c:set>

<results:resultsPage title="Session Results" jsIncludes="${jsIncludes}">
  <%--
    - Responses are displayed when only a single question is loaded,
    - otherwise it is loaded through ajax. See /js/instructorFeedbackResultsQuestion.js
    --%>
  <results:byQuestionResults questionPanels="${data.questionPanels}" />
</results:resultsPage>
