<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.doc.model.Page"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="meta" prefix="meta"
%><layout:html>
	<layout:head>
		<style>
			div.fptBodyContent {
				padding: 0px;
			}
		</style>
	</layout:head>
	<layout:body>
		<meta:formPage
			subtitleAttribute="<%=Page.NAME_ATTR %>"
			titleAttribute="<%=Page.TITLE_ATTR %>"
		>
			<meta:group>
				<div class="structuredTextFullHeight">
					<meta:custom name="<%=Page.CONTENT_ATTR %>"/>
				</div>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>