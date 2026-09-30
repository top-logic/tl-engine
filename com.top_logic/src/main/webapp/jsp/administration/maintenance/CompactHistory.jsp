<%@page import="com.top_logic.knowledge.service.compact.CompactHistoryCommand"
%><%@page import="com.top_logic.knowledge.service.compact.I18NConstants"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<title>
			<% renderText(pageContext, I18NConstants.COMPACT_HISTORY_JSP_TITLE); %>
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
				<% renderText(pageContext, I18NConstants.COMPACT_HISTORY_JSP_TITLE); %>
			</h1>
			<% renderHtml(pageContext, I18NConstants.COMPACT_HISTORY_JSP_DESCRIPTION); %>
			
			<% renderButton(pageContext, CompactHistoryCommand.Config.class, CompactHistoryCommand.class, CompactHistoryCommand.COMMAND_ID); %>
		</basic:access>
	</layout:body>
</layout:html>