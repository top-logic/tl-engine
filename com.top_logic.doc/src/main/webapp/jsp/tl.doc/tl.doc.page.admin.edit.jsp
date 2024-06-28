<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.form.component.EditMode"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.doc.model.Page"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="meta" prefix="meta"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<style>
			div.fptBodyContent {
				padding: 0px;
			}
		</style>
	</layout:head>
	<layout:body>
		<meta:formPage titleAttribute="<%=Page.TITLE_ATTR %>">
			<form:subtitle>
				<meta:group>
					<meta:attribute name="<%=Page.NAME_ATTR%>"/>
					<% if (((EditMode) MainLayout.getComponent(pageContext)).isInViewMode()) {%>
						(UUID:
							<meta:attribute name="<%=Page.UUID_ATTR%>"/>
						)
					<%} %>
				</meta:group>
			</form:subtitle>

			<meta:group>
				<div class="structuredTextFullHeight">
					<meta:custom name="<%=Page.CONTENT_ATTR %>"/>
				</div>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>