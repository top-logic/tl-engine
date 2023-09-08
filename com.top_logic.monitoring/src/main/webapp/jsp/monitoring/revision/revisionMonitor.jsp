<%@page import="com.top_logic.monitoring.revision.ChangeSetSelector"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	
	<layout:body>
		<form:filterForm>
			<form:columns
				count="3"
				keep="true"
			>
				<form:inputCell name="<%= ChangeSetSelector.RANGE_START_FIELD %>"/>
				<form:inputCell name="<%= ChangeSetSelector.SHOW_TECHNICAL_FIELD %>"/>
				<form:inputCell name="<%= ChangeSetSelector.SHOW_SYSTEM_CHANGES_FIELD %>"/>
			</form:columns>
		</form:filterForm>
	</layout:body>
</layout:html>