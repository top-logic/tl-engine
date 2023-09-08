<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.element.layout.create.CreateFormBuilder"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			subtitleField="<%=CreateFormBuilder.ELEMENT_TYPE %>"
			titleField="<%=CreateFormBuilder.TITLE_FIELD %>"
			typeField="<%=CreateFormBuilder.TYPE_IMAGE_FIELD %>"
		>
			<form:custom name="<%=CreateFormBuilder.ATTRIBUTES_GROUP %>"/>
		</form:formPage>
	</layout:body>
</layout:html>