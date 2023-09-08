<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@page import="com.top_logic.element.layout.structured.CreateAttributedStructuredElementComponent"
%><%@page import="com.top_logic.element.meta.gui.DefaultCreateAttributedComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="meta" prefix="meta"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage
			subtitleField="<%=CreateAttributedStructuredElementComponent.PARAMETER_TYPE %>"
			titleAttribute="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
		>
			<meta:group>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>