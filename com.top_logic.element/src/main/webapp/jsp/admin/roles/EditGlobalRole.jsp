<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.role.GlobalEditRoleComponent"
%><%@page import="com.top_logic.tool.boundsec.wrap.BoundedRole"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><%@page import="com.top_logic.tool.boundsec.gui.EditRoleComponent"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<%
			GlobalEditRoleComponent theComponent = (GlobalEditRoleComponent) MainLayout.getComponent(pageContext);
			BoundedRole             theRole      = (BoundedRole) theComponent.getModel();
			
			if (theRole != null) {
				%>
				<table
					align="center"
					border="0"
				>
					<tr>
						<td class="label"
							width="0%"
						>
							<form:label name="<%=EditRoleComponent.FIELD_NAME%>"
								colon="true"
							/>
						</td>
						<td class="content"
							width="100%"
						>
							<form:input name="<%=EditRoleComponent.FIELD_NAME%>"
								columns="32"
							/>
							<form:error name="<%=EditRoleComponent.FIELD_NAME%>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%=EditRoleComponent.FIELD_SYSTEM%>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:checkbox name="<%=EditRoleComponent.FIELD_SYSTEM%>"/>
							<form:error name="<%=EditRoleComponent.FIELD_SYSTEM%>"/>
						</td>
					</tr>
				</table>
				<%
			}
			%>
		</form:form>
	</layout:body>
</layout:html>