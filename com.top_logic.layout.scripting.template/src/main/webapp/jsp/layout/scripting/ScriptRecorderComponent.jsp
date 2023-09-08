<%@page language="java" session="false" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.layout.scripting.template.gui.ScriptRecorderComponent"
%><layout:html>
	<layout:head/>
	<layout:body formBodyClass="">
		<form:form
			ignoreModel="true"
			selectFirst="false"
		>
			<div class="ScriptRecorder fullSize">
				<form:custom name="<%= ScriptRecorderComponent.FIELD_NAME_EDIT_ACTION %>"/>
				<span class="scriptRecorderActionXmlError">
					<form:error name="<%= ScriptRecorderComponent.FIELD_NAME_EDIT_ACTION %>"/>
				</span>
			</div>
		</form:form>
	</layout:body>
</layout:html>