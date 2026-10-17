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
				<form:cell width="24%">
					<form:vertical>
						<form:inputCell name="<%= ChangeSetSelector.RANGE_MODE_FIELD %>"/>
					</form:vertical>
				</form:cell>
				<form:cell width="45%">
					<form:vertical>
						<form:inputCell name="<%= ChangeSetSelector.RANGE_START_FIELD %>"/>
						<form:inputCell name="<%= ChangeSetSelector.RANGE_END_FIELD %>"/>
						<form:inputCell name="<%= ChangeSetSelector.REVISION_START_FIELD %>"/>
						<form:inputCell name="<%= ChangeSetSelector.REVISION_END_FIELD %>"/>
					</form:vertical>
				</form:cell>
				<form:cell width="26%">
					<form:vertical>
						<form:inputCell name="<%= ChangeSetSelector.SHOW_TECHNICAL_FIELD %>"
							labelFirst="false"
						/>
						<form:inputCell name="<%= ChangeSetSelector.SHOW_SYSTEM_CHANGES_FIELD %>"
							labelFirst="false"
						/>
					</form:vertical>
				</form:cell>
			</form:columns>
		</form:filterForm>
	</layout:body>
</layout:html>