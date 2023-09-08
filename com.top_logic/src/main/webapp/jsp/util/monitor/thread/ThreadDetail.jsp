<%@page extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.util.monitor.thread.ThreadDetailComponent"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<form:formPage
			subtitleField="<%=ThreadDetailComponent.STATE_FIELD %>"
			titleField="<%=ThreadDetailComponent.NAME_FIELD %>"
		>
			<form:inputCell name="<%=ThreadDetailComponent.KIND_FIELD %>"/>
			<form:inputCell name="<%=ThreadDetailComponent.GROUP_FILED %>"/>
			<form:inputCell name="<%=ThreadDetailComponent.ID_FIELD %>"/>
			<form:inputCell name="<%=ThreadDetailComponent.PRIORITY_FIELD %>"/>
			<form:inputCell name="<%=ThreadDetailComponent.TIME_FIELD %>"/>

			<form:separator/>

			<h3>
				<form:label name="<%=ThreadDetailComponent.STACK_FIELD %>"/>
			</h3>
			<form:cell>
				<form:custom name="<%=ThreadDetailComponent.STACK_FIELD %>"/>
			</form:cell>
		</form:formPage>
	</layout:body>
</layout:html>