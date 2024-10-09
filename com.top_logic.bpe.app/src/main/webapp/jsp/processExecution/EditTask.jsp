<%@page import="com.top_logic.element.layout.formeditor.FormEditorUtil"
%><%@page import="com.top_logic.bpe.execution.model.Token"%><%@page
language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase"%><%@page
import="com.top_logic.bpe.layout.execution.ActiveTaskComponent"%><%@page
import="com.top_logic.mig.html.layout.MainLayout"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta" prefix="meta"%>
<%
ActiveTaskComponent component = (ActiveTaskComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage
			icon="<%=component.getIcon()%>"
			subtitleAttribute="<%=Token.PROCESS_EXECUTION_ATTR%>"
		>
			<form:custom name="."/>
		</meta:formPage>
	</layout:body>
</layout:html>