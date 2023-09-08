<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.event.layout.LogEntryFilterComponent"
%><%@page import="com.top_logic.event.layout.LogEntryFilterComponent.UpdateLogEntriesCommandHandler"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:filterForm cssClass="logEntryFilter">
			<form:vertical>
				<form:columns
					count="1"
					keep="true"
				>
					<form:descriptionCell>
						<form:description>
							<form:label name="<%= LogEntryFilterComponent.START_DATE %>"
								colon="true"
							/>
						</form:description>

						<span class="leftField">
							<form:date name="<%= LogEntryFilterComponent.START_DATE %>"/>
							<form:error name="<%= LogEntryFilterComponent.START_DATE %>"
								icon="true"
							/>
						</span>
						-
						<span class="rightField">
							<form:date name="<%= LogEntryFilterComponent.END_DATE %>"/>
							<form:error name="<%= LogEntryFilterComponent.END_DATE %>"
								icon="true"
							/>
						</span>
					</form:descriptionCell>
				</form:columns>
			</form:vertical>
		</form:filterForm>
	</layout:body>
</layout:html>