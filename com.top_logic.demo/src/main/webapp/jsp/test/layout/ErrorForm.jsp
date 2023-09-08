<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Test case for throwing an error on a form page
		</h1>
		<form:form>
			<%
			if (true) throw new RuntimeException("Test of form throwing an error from a JSP.");
			%>
		</form:form>
	</layout:body>
</layout:html>