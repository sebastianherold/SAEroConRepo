<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="teammates.common.util.Const" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib tagdir="/WEB-INF/tags/instructor" prefix="ti" %>

<c:set var="cssIncludes">
  <link rel="stylesheet" href="/stylesheets/instructorCourseEnroll.css" type="text/css">
</c:set>
<c:set var="jsIncludes">
  <script type="text/javascript" src="/js/instructorCourseEnrollPage.js"></script>
</c:set>
<c:set var="SESSION_TOKEN">
  <%=Const.ParamsNames.SESSION_TOKEN%>
</c:set>

<ti:instructorPage title="Enroll Students for ${data.courseId}" cssIncludes="${cssIncludes}" jsIncludes="${jsIncludes}">
  <div class="instructionImg">
    <img src="/images/enrollInstructions.gif" class="img-responsive" border="0" >
  </div>
  <br>
  <div class="panel panel-primary">
    <div class="panel-body fill-plain">
      <div class="text-muted">
        <span class="glyphicon glyphicon-exclamation-sign glyphicon-primary"></span> If you want to enroll more then <strong>100</strong> students into one course, divide students into sections containing no more than <strong>100</strong> students.
      </div>
      <br>
      <form action="${data.instructorCourseEnrollSaveLink}" method="post" class="form-horizontal" role="form">
        <input type="hidden" name="${SESSION_TOKEN}" value="${data.sessionToken}">
        <div class="col-md-12">
          <div class="form-group">
            <label for="instructions" class="col-sm-1 control-label">Student data:</label>
            <div class="col-sm-11">
              <textarea class="form-control" id="enrollstudents" name="enrollstudents" rows="6" cols="120" style="max-width:100%;" placeholder="Paste student data here ...">${fn:escapeXml(data.enrollStudents)}</textarea>
              <br>

              <t:statusMessage statusMessagesToUser="${data.statusMessagesToUser}" />

              <button type="submit" title="Enroll" id="button_enroll" name="button_enroll" class="btn btn-primary btn-md">
                Enroll students
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>

  <br>

  <div class="moreInfo">
    <h2> More info </h2>
    <hr style="width: 80%; margin-left: 0px;">
    <ul>
      <li>
        <span class="moreInfoPointTitle">Sample spreadsheet</span>
        <div class="moreInfoPointDetails">
          If you are not sure about the format of the spreadsheet,
          <a id="spreadsheet-link" href="https://docs.google.com/spreadsheets/d/1jODYiO_TcSoQ82t4E9rX-kfn1l7sEhb810rdlVJxHo0/edit?usp=sharing">here</a> is a sample file.
        </div>
      </li>
      <li>
        <span class="moreInfoPointTitle">Column headings</span>
        <div class="moreInfoPointDetails">
          The column order is not important.
          Column headings are <u>not</u> case sensitive. e.g. Team, TEAM, team are all acceptable.
        </div>
      </li>
      <li>
        <span class="moreInfoPointTitle">Columns to include</span>
        <div class="moreInfoPointDetails">
          <ul>
            <li class="moreInfoColumnInfo">
              <samp>Section</samp> [Compulsory for courses having more than 100 students]: Section name/ID
            </li>
            <li class="moreInfoColumnInfo">
              <samp>Team</samp> [Compulsory]: Team name/ID
              <div class="moreInfoPointDetails">
                <ul>
                  <li class="moreInfoEmailDetails">
                    Team must be unique within a course. A team cannot be in 2 different sections.
                  </li>
                  <li class="moreInfoEmailDetails">
                    If you do not have teams in your course, use “N/A” as the team name for all students.
                  </li>
                </ul>
              </div>
            </li>
            <li class="moreInfoColumnInfo">
              <samp>Name</samp> [Compulsory]: Student name
            </li>
            <li class="moreInfoColumnInfo">
              <samp>Email</samp> [Compulsory]: The email address used to contact the student.<br>
              <div class="moreInfoPointDetails">
                <ul>
                  <li class="moreInfoEmailDetails">
                    This need not be a Gmail address.
                  </li>
                  <li class="moreInfoEmailDetails">
                    It should be unique for each student.
                    If two students are given the same email, they will be considered the same student.
                  </li>
                </ul>
              </div>
            </li>
            <li class="moreInfoColumnInfo">
              <samp>Comments</samp> [Optional]: Any other information you want to record about a student.
            </li>
          </ul>
        </div>
      </li>
      <li>
        <span class="moreInfoPointTitle">Mass editing enrolled students</span>
        <div class="moreInfoPointDetails">
          <ul>
            <li class="moreInfoMassEditInfo">
              The text box above can be used for mass-editing details (except email address) of students already enrolled.
              To edit, simply enroll students using the updated data and existing data will be updated accordingly.</li>
            <li class="moreInfoMassEditInfo">To DELETE students or to UPDATE EMAIL address of a student, please go to the ‘courses’ page and click the 'view' link of the course.</li>
          </ul>
        </div>
      </li>
      <li>
        <span class="moreInfoPointTitle">Enrolling without spreadsheets</span>
        <div class="moreInfoPointDetails">
          The alternative is to type student data in the text box, using the pipe symbol (also called the vertical bar,
          not to be confused with upper case i or lower case L) to separate values.
          <br> Here is an example.
          <br><br>
          <div id="moreInfoEnrollWOSpreadSheetEg">
            <samp>
              <span class="enrollLines"><b>Section   |   Team   |   Name   |   Email   |   Comments</b></span>
              <br><span class="enrollLines">Tut Group 1   |   Team 1   |   Tom Jacobs  |  tom@email.com</span>
              <br><span class="enrollLines">Tut Group 1   |   Team 1  |   Jean Wong   |   jean@email.com   |   Exchange Student</span>
              <br><span class="enrollLines">Tut Group 1   |   Team 2   |   Jack Wayne  |  jack@email.com</span>
              <br><span class="enrollLines">Tut Group 2   |   Team 3   |   Thora Parker  |  thora@email.com</span>
            </samp>
          </div>
        </div>
      </li>
    </ul>
  </div>
</ti:instructorPage>
