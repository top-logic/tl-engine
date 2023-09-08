<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<form:input name="SepWinPartnerTestModel"
				columns="70"
			/>
			<form:command name="save_sepwinPartnerTest"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
		</form:form>
	</layout:body>
</layout:html>