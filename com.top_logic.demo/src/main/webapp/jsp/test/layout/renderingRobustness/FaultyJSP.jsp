<%@page import="com.top_logic.event.infoservice.InfoService"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<%
		if(true) {
			throw new UnsupportedOperationException("Faulty content in <layout:head>");
		}
		%>
	</layout:head>
	<layout:body>
		<%
		if(true) {
			InfoService.showInfo(ResKey.text("Message of jsp view part!"));
			throw new UnsupportedOperationException("Faulty content in <layout:body>");
		}
		%>
	</layout:body>
	<%
	if(true) {
		throw new UnsupportedOperationException("Faulty content in <layout:html>");
	}
	%>
</layout:html>