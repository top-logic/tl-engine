<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form ignoreModel="true">
			<p>
				<form:command name="gotoField"/>
			</p>
		</form:form>
	</layout:body>
</layout:html>