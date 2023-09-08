<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.tool.boundsec.assistent.eva.ErrorComponent"
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
			<form:ifExists name="<%=ErrorComponent.ERROR_FIELD %>">
				<p>
					<form:tableview name="<%=ErrorComponent.ERROR_FIELD %>"/>
				</p>
			</form:ifExists>
			<form:ifExists name="<%=ErrorComponent.INFO_FIELD %>">
				<p>
					<form:tableview name="<%=ErrorComponent.INFO_FIELD %>"/>
				</p>
			</form:ifExists>
		</form:form>
	</layout:body>
</layout:html>