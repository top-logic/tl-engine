<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<h2>
				<form:resource key="title"/>
			</h2>
			<p>
				<form:resource key="message"/>
			</p>
		</form:form>
	</layout:body>
</layout:html>