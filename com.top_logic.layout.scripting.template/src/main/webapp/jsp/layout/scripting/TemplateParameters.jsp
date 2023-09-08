<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form displayWithoutModel="false">
			<form:vertical>
				<form:forEach member="f">
					<form:inputCell name="${f}"/>
				</form:forEach>
			</form:vertical>
		</form:form>
	</layout:body>
</layout:html>