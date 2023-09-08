<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.meta.expression.ExpressionSelectorComponent"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form ignoreModel="true">
			<%
			ExpressionSelectorComponent<?> selectorComponent =
			(ExpressionSelectorComponent<?>) MainLayout.getComponent(pageContext);
			String selectorFieldName = selectorComponent.getExpressionSelectorName();
			%>
			<form:columns
				keep="true"
			>
				<form:inputCell name="<%= selectorFieldName %>"
				/>
				<form:scope name="<%= QueryUtils.FORM_GROUP %>">
					<form:inputCell name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"
					/>
					<form:inputCell name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"
					/>
				</form:scope>
			</form:columns>
		</form:form>
	</layout:body>
</layout:html>