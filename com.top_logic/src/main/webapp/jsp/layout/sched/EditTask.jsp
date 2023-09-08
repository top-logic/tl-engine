<%@page import="com.top_logic.util.sched.layout.table.results.TaskResultAccessor"
%><%@page import="com.top_logic.layout.task.EditTaskWrapperComponent"
%><%@page import="com.top_logic.util.sched.layout.EditTaskComponent"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.util.sched.layout.table.TaskAccessor"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<title>
			Edit Task
		</title>

		<style type="text/css">
			table.schedulingAlgorithm {
				width: 100%;
			}
		</style>
	</layout:head>
	<layout:body>
		<form:form>
			<form:ifExists name="<%= EditTaskComponent.NAME_GROUP_BASIC %>">
				<form:groupCell name="<%= EditTaskComponent.NAME_GROUP_BASIC %>"
					firstColumnWidth="13em"
				>
					<form:cell wholeLine="true">
						<form:inputCell name="<%=TaskAccessor.NAME%>"/>
					</form:cell>

					<form:cell wholeLine="true">
						<form:inputCell name="<%=TaskAccessor.CLASS_NAME%>"/>
					</form:cell>

					<form:cell wholeLine="true">
						<form:columns count="2">
							<form:inputCell name="<%=TaskAccessor.IS_ENABLED%>"/>
							<form:inputCell name="<%=TaskAccessor.RUN_ON_STARTUP%>"/>
							<form:inputCell name="<%=TaskAccessor.PER_NODE%>"/>
							<form:inputCell name="<%=TaskAccessor.PERSISTENT%>"/>
							<form:inputCell name="<%=TaskAccessor.NEEDS_MAINTENANCE_MODE%>"/>
							<form:inputCell name="<%=TaskAccessor.IS_BLOCKED%>"/>
							<form:inputCell name="<%=TaskAccessor.IS_BLOCKING_ALLOWED%>"/>
						</form:columns>
					</form:cell>
				</form:groupCell>
			</form:ifExists>

			<form:ifExists name="<%= EditTaskComponent.NAME_GROUP_SCHEDULE_BORDER %>">
				<form:groupCell name="<%= EditTaskComponent.NAME_GROUP_SCHEDULE_BORDER %>">
					<form:custom name="<%= EditTaskComponent.NAME_GROUP_SCHEDULE_CONTENT %>"/>
				</form:groupCell>
			</form:ifExists>

			<form:ifExists name="<%= EditTaskComponent.NAME_GROUP_STATE %>">
				<form:groupCell name="<%= EditTaskComponent.NAME_GROUP_STATE %>">
					<form:cell wholeLine="true">
						<form:columns count="2">
							<form:inputCell name="<%=TaskResultAccessor.START_DATE%>"/>
							<form:inputCell name="<%=TaskResultAccessor.END_DATE%>"/>
							<form:inputCell name="<%=TaskResultAccessor.DURATION%>"/>
							<form:inputCell name="<%=TaskAccessor.COMBINED_STATE%>"/>
							<form:inputCell name="<%=TaskAccessor.CLUSTER_LOCK%>"/>
							<form:inputCell name="<%=TaskResultAccessor.EXCEPTION%>"/>
							<form:inputCell name="<%=TaskResultAccessor.MESSAGE%>"/>
							<form:inputCell name="<%=TaskAccessor.LAST_SCHEDULE%>"/>
							<form:inputCell name="<%=TaskAccessor.NEXT_SCHEDULE%>"/>

							<form:descriptionCell>
								<form:description>
									<form:label name="<%= TaskResultAccessor.LOG_FILE %>"
										colon="true"
									/>
								</form:description>
								<form:custom name="<%= TaskResultAccessor.LOG_FILE %>"/>
							</form:descriptionCell>
						</form:columns>
					</form:cell>
				</form:groupCell>
			</form:ifExists>

			<form:ifExists name="<%= EditTaskWrapperComponent.NAME_GROUP_WRAPPER %>">
				<form:groupCell name="<%= EditTaskWrapperComponent.NAME_GROUP_WRAPPER %>"
					titleKeySuffix="<%= EditTaskWrapperComponent.NAME_GROUP_WRAPPER %>"
					width="small"
				>
					<form:inputCell name="<%=EditTaskWrapperComponent.TIME%>"/>
				</form:groupCell>
			</form:ifExists>
		</form:form>
	</layout:body>
</layout:html>