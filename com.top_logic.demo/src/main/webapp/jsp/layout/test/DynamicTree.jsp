<%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<basic:fieldset>
				<basic:legend>
					Tree1
				</basic:legend>
				<form:custom name="tree"/>
			</basic:fieldset>
		</form:form>
	</layout:body>
</layout:html>