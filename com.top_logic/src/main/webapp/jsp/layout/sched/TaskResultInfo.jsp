<%@page import="com.top_logic.layout.ResPrefix"
%><%@page import="com.top_logic.util.sched.layout.TaskResultComponent"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.util.sched.layout.table.results.TaskResultAccessor"
%><%@page import="com.top_logic.layout.task.EditTaskWrapperComponent"
%><%@page import="com.top_logic.util.sched.layout.EditTaskComponent"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.util.sched.layout.table.TaskAccessor"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
Resources i18n = Resources.getInstance();
LayoutComponent component = MainLayout.getComponent(pageContext);
ResPrefix i18nPrefix = component.getResPrefix();
%><layout:html>
	<layout:head>
		<title>
			<%= i18n.getString(i18nPrefix.key("title")) %>
		</title>
	</layout:head>
	<layout:body>
		<form:form>
			<form:groupCell titleKeySuffix="title">
				<form:cell wholeLine="true">
					<form:columns count="2">
						<form:inputCell name="<%= TaskResultAccessor.START_DATE %>"/>
						<form:inputCell name="<%= TaskResultAccessor.END_DATE %>"/>
						<form:inputCell name="<%= TaskResultAccessor.DURATION %>"/>
						<form:inputCell name="<%= TaskResultAccessor.RESULT %>"/>
						<form:inputCell name="<%= TaskResultAccessor.EXCEPTION %>"/>
						<form:cell wholeLine="true">
							<form:inputCell name="<%= TaskResultAccessor.WARNINGS %>"/>
						</form:cell>
						<form:cell wholeLine="true">
							<form:inputCell name="<%= TaskResultAccessor.MESSAGE %>"/>
						</form:cell>
						<form:cell wholeLine="true">
							<form:inputCell name="<%= TaskResultAccessor.LOG_FILE %>"/>
						</form:cell>
					</form:columns>
				</form:cell>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>