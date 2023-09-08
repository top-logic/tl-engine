<%@page import="com.top_logic.model.TLNamed"
%><%@page import="com.top_logic.layout.form.template.SelectionControlProvider"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		
		<form:form>
			<form:vertical>
				<form:inputCell name="<%= TLNamed.NAME_ATTRIBUTE %>"/>
			</form:vertical>
			<form:scope name="<%= QueryUtils.FORM_GROUP %>">
				<form:inputCell name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"/>
				<form:inputCell name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"
					controlProvider="<%=SelectionControlProvider.SELECTION_INSTANCE %>"
				/>
			</form:scope>
		</form:form>
	</layout:body>
</layout:html>