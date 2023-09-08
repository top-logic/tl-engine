<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@page import="com.top_logic.element.meta.gui.DefaultCreateAttributedComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="meta"   prefix="meta"
%><%--
Page to create a new attributed element, cooperates with DefaultCreateAttributedComponent.
--%>
<layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage
			titleAttribute="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
			typeField="<%=DefaultCreateAttributedComponent.IMAGE_TYPE_FIELD %>"
		>
			<meta:group>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>