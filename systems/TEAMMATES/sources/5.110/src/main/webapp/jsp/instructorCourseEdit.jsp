<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="teammates.common.util.FrontEndLibrary" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib tagdir="/WEB-INF/tags/instructor" prefix="ti" %>
<%@ taglib tagdir="/WEB-INF/tags/instructor/course" prefix="course" %>

<c:set var="jsIncludes">
  <script type="text/javascript" src="<%= FrontEndLibrary.MOMENT %>"></script>
  <script type="text/javascript" src="<%= FrontEndLibrary.MOMENT_TIMEZONE %>"></script>
  <script type="text/javascript" src="/js/instructorCourseEdit.js"></script>
</c:set>

<ti:instructorPage title="Edit Course Details" jsIncludes="${jsIncludes}">
  <input type="hidden" id="course-time-zone" value="${data.course.timeZone}">
  <course:courseEditCourseInfo
      editCourseButton="${data.editCourseButton}"
      deleteCourseButton="${data.deleteCourseButton}"
      course="${data.course}" />
  <br>
  <t:statusMessage statusMessagesToUser="${data.statusMessagesToUser}" />
  <div class="pull-right">
    <a href="/instructorHelp.jsp#editCourse" class="small" target="_blank" rel="noopener noreferrer">
      <span class="glyphicon glyphicon-info-sign"></span>
      More about configuring access permissions
    </a>
  </div>
  <br>
  <br>
  <course:courseEditInstructorList instructorPanelList="${data.instructorPanelList}" />
  <course:courseEditAddInstructorPanel
      addInstructorButton="${data.addInstructorButton}"
      courseId="${data.course.id}"
      addInstructorPanel="${data.addInstructorPanel}"
  />
  <course:courseEditInstructorRoleModal />
  <br>
  <br>
</ti:instructorPage>
