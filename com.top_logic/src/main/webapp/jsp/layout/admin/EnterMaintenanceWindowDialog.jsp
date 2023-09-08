<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.layout.admin.component.MaintenanceWindowComponent.EnterMaintenanceWindowDialog"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form ignoreModel="true">
			<p>
				<form:resource key="dialogMessage"/>
			</p>
			<p>
				<form:resource key="timeMessage"/>
			</p>
			<form:resource key="activate"/>
			&#xA0;&#xA0;&#xA0;
			<form:input name="<%=EnterMaintenanceWindowDialog.MIN_FIELD%>"
				columns="2"
				style="text-align:right"
			/>
			<form:error name="<%=EnterMaintenanceWindowDialog.MIN_FIELD%>"/>
			<form:resource key="minutes"/>
			.
		</form:form>
	</layout:body>
</layout:html>