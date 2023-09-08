<%@page language="java" session="true"	extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.monitoring.session.SessionSearchComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<style type="text/css">
			.sessionSearch table.frm {
				width: 0px;
			}
			.sessionSearch td.content {
				white-space: nowrap;
			}
		</style>
	</layout:head>

	
	<layout:body formBodyClass="frmBody sessionSearch">
		<form:filterForm>
			<form:columns
				count="4"
				keep="true"
			>
				<form:inputCell name="<%= SessionSearchComponent.TIME_RANGE_FIELD %>"/>
				<form:inputCell name="<%= SessionSearchComponent.RELATIVE_RANGES_FIELD %>"/>
				<form:inputCell name="<%= SessionSearchComponent.START_DATE_FIELD %>"/>
				<form:inputCell name="<%= SessionSearchComponent.END_DATE_FIELD %>"/>
			</form:columns>
		</form:filterForm>
	</layout:body>
</layout:html>