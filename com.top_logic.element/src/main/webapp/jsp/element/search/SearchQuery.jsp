<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.element.layout.meta.search.AttributedSearchComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="util" prefix="util"
%>
<%
AttributedSearchComponent theComp = (AttributedSearchComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<util:if condition="<%= theComp.isMultiMetaElementSearch() %>">
				&#xA0;&#xA0;&#xA0;
				<form:label name="<%= SearchFieldSupport.META_ELEMENT %>"/>
				:
				<form:select name="<%= SearchFieldSupport.META_ELEMENT %>"/>
			</util:if>
			&#xA0;&#xA0;&#xA0;
			<form:label name="<%=SearchFieldSupport.STORED_QUERY %>"/>
			:
			<form:select name="<%=SearchFieldSupport.STORED_QUERY %>"/>
		</form:form>
	</layout:body>
</layout:html>