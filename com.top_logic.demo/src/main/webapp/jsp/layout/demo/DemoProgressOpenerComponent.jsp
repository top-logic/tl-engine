<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<h1>
				<form:resource key="pageTitle"/>
			</h1>
			<p>
				<form:resource key="pageText"/>
			</p>
		</form:form>
	</layout:body>
</layout:html>