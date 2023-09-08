<%@page language="java" session="false" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.util.monitor.ApplicationMonitorComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>

	<layout:body>
		<form:form>
			<form:groupCell name="<%=ApplicationMonitorComponent.GROUP_STATUS%>">
				<form:cell wholeLine="true">
					<form:tableview name="<%=ApplicationMonitorComponent.FIELD_STATUS%>"/>
				</form:cell>
			</form:groupCell>

			<form:groupCell name="<%=ApplicationMonitorComponent.GROUP_MONITORS%>">
				<form:cell wholeLine="true">
					<form:tableview name="<%=ApplicationMonitorComponent.FIELD_MONITORS%>"/>
				</form:cell>
			</form:groupCell>

			<form:columns count="2">
				<form:group name="<%=ApplicationMonitorComponent.GROUP_JAVA%>"
					personalizationName="firstRow"
				>
					<form:cell wholeLine="true">
						<form:tableview name="<%=ApplicationMonitorComponent.FIELD_JAVA%>"/>
					</form:cell>
				</form:group>

				<form:groupCell name="<%=ApplicationMonitorComponent.GROUP_SYSTEM%>"
					personalizationName="firstRow"
				>
					<form:cell wholeLine="true">
						<form:tableview name="<%=ApplicationMonitorComponent.FIELD_SYSTEM%>"/>
					</form:cell>
				</form:groupCell>

				<form:groupCell name="<%=ApplicationMonitorComponent.GROUP_MEMORY%>"
					personalizationName="secondRow"
				>
					<form:cell wholeLine="true">
						<form:tableview name="<%=ApplicationMonitorComponent.FIELD_MEMORY%>"/>
					</form:cell>
				</form:groupCell>

				<form:groupCell name="<%=ApplicationMonitorComponent.GROUP_LICENCE%>"
					personalizationName="secondRow"
				>
					<form:cell wholeLine="true">
						<form:tableview name="<%=ApplicationMonitorComponent.FIELD_LICENCE%>"/>
					</form:cell>
				</form:groupCell>
			</form:columns>
		</form:form>
	</layout:body>
</layout:html>