<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.base.administration.MaintenanceWindowManager"
%><%@page import="com.top_logic.layout.admin.component.MaintenanceWindowComponent"%>
<%
MaintenanceWindowManager mwMgr = MaintenanceWindowManager.getInstance();
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<h1>
				<form:resource key="pageTitle"/>
			</h1>
			<p>
				<%
				int maintenanceMode = mwMgr.getMaintenanceModeState();
				if (maintenanceMode == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
					%>
					<form:resource key="isInMaintenanceWindow"/>
					<%
				}
				else if (maintenanceMode == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
					%>
					<form:resource key="isAboutToEnterMaintenanceWindow.0"/>
					<span id="<%=MaintenanceWindowComponent.TIME_LEFT_ID%>">
						<form:resource key="isAboutToEnterMaintenanceWindow.1"/>
					</span>
					<form:resource key="isAboutToEnterMaintenanceWindow.2"/>
					<%
				}
				else {
					%>
					<form:resource key="isInNormalMode"/>
					<%
				}
				%>
			</p>
		</form:form>
	</layout:body>
</layout:html>