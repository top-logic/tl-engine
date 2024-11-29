<%@page import="com.top_logic.layout.scripting.template.gui.saveas.SaveScriptAsFormBuilder"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@page language="java" extends="com.top_logic.model.annotate.LabelPosition"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<style type="text/css">
			div {
				margin-bottom: 0px !important;
			}
		</style>
	</layout:head>
	<layout:body>
		<form:form displayWithoutModel="true">
			<form:inputCell name="<%= SaveScriptAsFormBuilder.FIELD_FILE_NAME %>"
				labelPosition="<%=LabelPosition.INLINE%>"
			/>
		</form:form>
	</layout:body>
</layout:html>