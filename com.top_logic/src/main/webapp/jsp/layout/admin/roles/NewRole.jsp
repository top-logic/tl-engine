<%@page import="com.top_logic.tool.boundsec.wrap.BoundedRole"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="util"   prefix="tl"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><%@page import="com.top_logic.tool.boundsec.gui.EditRoleComponent"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			titleField="<%=EditRoleComponent.FIELD_NAME %>"
			type="<%= BoundedRole.getRoleType() %>"
		>
			<p>
				<tl:label name="admin.role.edit.new.title"/>
			</p>
		</form:formPage>
	</layout:body>
</layout:html>