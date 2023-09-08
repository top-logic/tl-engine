<%@page import="com.top_logic.layout.admin.component.ChangeLogFileBuilder"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form displayWithoutModel="true">
			<form:inputCell name="<%=ChangeLogFileBuilder.CURRENT_LOGGING_FILE%>"/>
			<form:inputCell name="<%=ChangeLogFileBuilder.NEW_LOGGING_FILE%>"/>
		</form:form>
	</layout:body>
</layout:html>