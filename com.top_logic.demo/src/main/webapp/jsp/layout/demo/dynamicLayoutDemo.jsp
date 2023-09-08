<%@page import="com.top_logic.demo.layout.dynamic.demo.DynamicLayoutSelectComponent"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form ignoreModel="true">
			<p>
				Select the layout to display below.
			</p>
			<p>
				<form:select name="<%= DynamicLayoutSelectComponent.SELECT_FIELD %>"/>
			</p>
		</form:form>
	</layout:body>
</layout:html>