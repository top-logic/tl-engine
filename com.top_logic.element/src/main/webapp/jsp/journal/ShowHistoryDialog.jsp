<%@page language="java"
contentType="text/html;charset=ISO-8859-1"
extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<h1>
				<form:label name="title"/>
			</h1>

			<form:tableview name="journal"/>
		</form:form>
	</layout:body>
</layout:html>