<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/shared/feedbackSubmissionEdit" prefix="tsfse" %>
<tsfse:feedbackSubmissionEdit isInstructor="${true}" moderatedPersonEmail="${data.previewInstructor.email}"
    moderatedPersonName="${data.previewInstructor.name}" />
