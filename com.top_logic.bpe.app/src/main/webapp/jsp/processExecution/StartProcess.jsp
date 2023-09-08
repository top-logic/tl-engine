<%@page language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase"%><%@page
import="com.top_logic.element.meta.gui.DefaultCreateAttributedComponent"%><%@page
import="com.top_logic.element.layout.formeditor.FormEditorUtil"%><%@page
import="com.top_logic.bpe.app.layout.ProcessExecutionCreateComponent"%><%@page
import="com.top_logic.mig.html.layout.MainLayout"%><%@taglib
uri="layout" prefix="layout"%><%@taglib
uri="ajaxform" prefix="form"%><%@taglib uri="meta" prefix="meta"%><%@taglib uri="basic" prefix="basic"%><%--
Page to create a new attributed element, cooperates with DefaultCreateAttributedComponent.
--%>
<%ProcessExecutionCreateComponent component = (ProcessExecutionCreateComponent) MainLayout
.getComponent(pageContext);
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage
			icon="<%=component.getIcon()%>"
			titleAttribute="collaboration"
			typeField="<%=DefaultCreateAttributedComponent.IMAGE_TYPE_FIELD%>"
		>
			<form:custom name="."/>
		</meta:formPage>
	</layout:body>
</layout:html>