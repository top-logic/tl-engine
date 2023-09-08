<%@page import="com.top_logic.knowledge.service.migration.load.I18NConstants"
%><%@page import="com.top_logic.knowledge.service.migration.load.LoadDataCommand"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<title>
			<% renderText(pageContext, I18NConstants.IMPORT_DATA_JSP_TITLE); %>
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
				<% renderText(pageContext, I18NConstants.IMPORT_DATA_JSP_TITLE); %>
			</h1>
			<% renderHtml(pageContext, I18NConstants.IMPORT_DATA_JSP_DESCRIPTION); %>
			
			<% renderButton(pageContext, LoadDataCommand.Config.class, LoadDataCommand.class, "loadData"); %>
		</basic:access>
	</layout:body>
</layout:html>