<%@page import="com.top_logic.tool.boundsec.gui.profile.EditRolesProfileComponent"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<form:custom name="<%=EditRolesProfileComponent.TREE_FIELD %>"/>
		</form:form>
	</layout:body>
</layout:html>