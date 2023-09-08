<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="com.top_logic.layout.inspector.InspectorComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:filterForm>
			<form:forEach member="option">
				<form:custom name="${option}"/>
				<form:label name="${option}"/>
				&#xA0;&#xA0;&#xA0;
			</form:forEach>
		</form:filterForm>
	</layout:body>
</layout:html>