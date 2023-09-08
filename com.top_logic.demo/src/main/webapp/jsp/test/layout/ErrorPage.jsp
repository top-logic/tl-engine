<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Test case for throwing an error on a page
		</h1>
		<%
		if (true) throw new RuntimeException("Test of page throwing an error from a JSP.");
		%>
	</layout:body>
</layout:html>