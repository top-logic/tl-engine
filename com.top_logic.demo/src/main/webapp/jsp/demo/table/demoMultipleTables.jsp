<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:filterForm>
			<form:resource key="description"/>
			<form:input name="text"/>
		</form:filterForm>
	</layout:body>
</layout:html>