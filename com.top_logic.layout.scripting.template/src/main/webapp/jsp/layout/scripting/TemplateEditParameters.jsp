<%@page import="com.top_logic.layout.scripting.template.gui.templates.TemplateEditModel"
%><%@page import="com.top_logic.layout.form.values.MultiLineText"
%><%@page import="com.top_logic.layout.scripting.template.gui.templates.TemplateEditBuilder"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:filterForm
			displayWithoutModel="false"
			ignoreModel="false"
			selectFirst="false"
		>
			<form:vertical>
				<form:inputCell name="<%= TemplateEditModel.NAME_ATTRIBUTE %>"
					cssClass="scriptRecorderContentColumn"
				/>
				<form:cell>
					<form:custom name="<%= TemplateEditModel.PARAMETERS %>"/>
				</form:cell>
			</form:vertical>
		</form:filterForm>
	</layout:body>
</layout:html>