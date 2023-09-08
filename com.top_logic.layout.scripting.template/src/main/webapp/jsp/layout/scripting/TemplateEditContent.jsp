<%@page import="com.top_logic.layout.scripting.template.gui.templates.TemplateEditModel"
%><%@page import="com.top_logic.layout.form.values.MultiLineText"
%><%@page import="com.top_logic.layout.scripting.template.gui.templates.TemplateEditBuilder"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body formBodyClass="">
		<form:form
			displayWithoutModel="false"
			selectFirst="false"
		>
			<form:custom name="<%= TemplateEditModel.CONTENT %>"/>
			<span class="scriptRecorderActionXmlError">
				<form:error name="<%= TemplateEditModel.CONTENT %>"/>
			</span>
		</form:form>
	</layout:body>
</layout:html>