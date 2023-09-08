<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform"  prefix="form"
%><%@taglib uri="layout"  prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form displayWithoutModel="true">
			<table>
				<tr>
					<td>
						<form:label name="nodeName"/>
					</td>
					<td>
						<form:input name="nodeName"/>
						<form:error name="nodeName"/>
					</td>
				</tr>

				<tr>
					<td>
						<form:label name="nodeType"/>
					</td>
					<td>
						<form:input name="nodeType"/>
						<form:error name="nodeType"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>