<%@page import="com.top_logic.knowledge.service.migration.dump.KBDumpHandler"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.7 --%>
<%!
// JSP page configuration:
{
	TITLE = "Export application data";
}
%>

<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>
<layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=UTF-8"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
		<basic:script>
			services.ajax.ignoreTLAttributes = true;
		</basic:script>
	</layout:head>
	<layout:body>
		<basic:access>
			<h1>
				<%=TITLE %>
			</h1>
			<% renderButton(pageContext, KBDumpHandler.Config.class, KBDumpHandler.class, "dumpData"); %>
		</basic:access>
	</layout:body>
</layout:html>