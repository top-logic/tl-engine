<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.tool.boundsec.gui.EditRoleComponent"
%><%@page import="com.top_logic.tool.boundsec.wrap.BoundedRole"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			titleField="<%=EditRoleComponent.FIELD_NAME%>"
			type="<%= BoundedRole.getRoleType() %>"
		>
			<form:groupCell titleKeySuffix="basic">
				<form:inputCell name="<%=EditRoleComponent.FIELD_SYSTEM %>"/>

				<form:descriptionCell wholeLine="true">
					<form:description>
						<form:label name="<%=EditRoleComponent.FIELD_DESC %>"/>
						<form:error name="<%=EditRoleComponent.FIELD_DESC%>"/>
					</form:description>
					<form:input name="<%=EditRoleComponent.FIELD_DESC%>"
						columns="128"
						multiLine="true"
						rows="4"
					/>
				</form:descriptionCell>
			</form:groupCell>
		</form:formPage>
	</layout:body>
</layout:html>