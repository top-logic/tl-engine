<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.security.AttributeSecurityEditComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<%
			AttributeSecurityEditComponent theComponent = (AttributeSecurityEditComponent) MainLayout.getComponent(pageContext);
			%>
			<form:groupCell titleKeySuffix="classified.group">
				<form:cell wholeLine="true">
					<form:table name="classified"
						columnNames="<%= theComponent.getColumnDeclaration() %>"
						initialSortColumn="1"
					/>
				</form:cell>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>