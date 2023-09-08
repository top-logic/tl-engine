<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.HTMLConstants"
%><%@page import="com.top_logic.reporting.chart.gantt.component.GanttComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>

	<layout:body formBodyClass="">
		<form:form ignoreModel="true">
			<table style="width:100%; text-align: right">
				<tr>
					<td align="left">
						<form:input name="<%=GanttComponent.FIELD_TIMESTAMP%>"/>
					</td>
					<td align="right">
						<form:command name="<%=GanttComponent.COMMAND_FIRST_PAGE%>"/>
						<form:command name="<%=GanttComponent.COMMAND_PREV_PAGE%>"/>
						<form:resource key="string_page"/>
						<form:input name="<%=GanttComponent.FIELD_CURRENT_PAGE%>"
							columns="1"
						/>
						<form:resource key="string_of"/>
						<form:input name="<%=GanttComponent.FIELD_PAGE_COUNT%>"
							columns="1"
						/>
						<form:command name="<%=GanttComponent.COMMAND_NEXT_PAGE%>"/>
						<form:command name="<%=GanttComponent.COMMAND_LAST_PAGE%>"/>
						<%=HTMLConstants.NBSP%><%=HTMLConstants.NBSP%><%=HTMLConstants.NBSP%><%=HTMLConstants.NBSP%><%=HTMLConstants.NBSP%>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>