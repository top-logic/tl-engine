<%@page session="false" import="com.top_logic.knowledge.service.encryption.pbe.ApplicationPasswordUtil"
%><%
if (!ApplicationPasswordUtil.hasPassword()) {
	%>
	<jsp:forward page="setupApplicationPassword.jsp"/>
	<%
} else {
	%>
	<jsp:forward page="enterApplicationPassword.jsp"/>
	<%
}
%>