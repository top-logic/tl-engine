<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		

		<form:form>
			<form:group name="toggleCommands">
				<form:forEach member="command">
					<form:custom name="${command}"/>
					<form:label name="${command}"/>
					<br/>
				</form:forEach>
			</form:group>
		</form:form>
	</layout:body>
</layout:html>