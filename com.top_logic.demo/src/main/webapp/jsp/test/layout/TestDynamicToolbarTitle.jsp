<%@page import="com.top_logic.demo.layout.form.demo.TestDynamicToolbarTitle"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<form:resource key="description"/>
			<form:vertical>
				<form:inputCell name="<%= TestDynamicToolbarTitle.TITLE_FIELD %>"/>
			</form:vertical>
		</form:form>
	</layout:body>
</layout:html>