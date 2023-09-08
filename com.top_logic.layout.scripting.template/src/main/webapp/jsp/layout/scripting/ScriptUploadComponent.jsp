<%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page import="com.top_logic.layout.scripting.template.gui.SaveScriptCommand"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.tool.boundsec.OpenModalDialogCommandHandler"
%><%@page import="com.top_logic.layout.scripting.template.gui.ScriptUploadComponent"
%><%@page language="java" session="false" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<title>
			Script Upload
		</title>
		
	</layout:head>
	<layout:body>
		<form:filterForm
			ignoreModel="true"
			selectFirst="false"
		>
			<div>
				<form:label name="<%= ScriptUploadComponent.FIELD_NAME_FILE_UPLOAD %>"
					colon="true"
				/>
				<form:dataItem name="<%= ScriptUploadComponent.FIELD_NAME_FILE_UPLOAD %>"/>
			</div>
		</form:filterForm>
	</layout:body>
</layout:html>