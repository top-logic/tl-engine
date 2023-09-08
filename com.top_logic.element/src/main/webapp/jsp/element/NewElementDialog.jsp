<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.component.FormComponent"
%><%@page import="com.top_logic.element.layout.structured.StructuredElementCreateComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%--
Page to create a new element.
cooperates with com.top_logic.element.layout.structured.StructuredElementCreateComponent.
--%>
<layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:formPage
			subtitleField="<%=StructuredElementCreateComponent.ELEMENT_TYPE %>"
			titleField="<%=StructuredElementCreateComponent.ELEMENT_NAME %>"
			typeField="<%=StructuredElementCreateComponent.IMAGE_TYPE_FIELD %>"
		>
			<p>
				<form:resource key="message"/>
			</p>
			<table class="frm"
				summary="Create structured element"
			>
				<tr>
				</tr>
			</table>
		</form:formPage>
	</layout:body>
</layout:html>