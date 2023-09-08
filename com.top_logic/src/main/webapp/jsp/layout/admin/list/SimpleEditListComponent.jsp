<%@page import="com.top_logic.knowledge.wrap.list.FastList"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.admin.component.EditListComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
EditListComponent theComponent = (EditListComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			displayWithoutModel="true"
			titleKey="<%=theComponent.getListname()%>"
			type="<%= FastList.getFastListType() %>"
		>
			<form:tablelist name="<%=EditListComponent.FIELD_TABLELIST%>"
				rowObjectCreator="<%=theComponent%>"
				rowObjectRemover="<%=theComponent%>"
				sortable="true"
			/>
			<form:error name="<%=EditListComponent.FIELD_TABLELIST%>"/>
		</form:formPage>
	</layout:body>
</layout:html>