<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%> <%@taglib uri="layout"   prefix="layout"
%> <%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	
	<layout:body>
		<form:form>
			<table
				align="left"
				border="0"
				cellpadding="0"
				summary=""
			>
				<tr>
					<td class="label">
						<form:label name="name"/>
					</td>
					<td class="content">
						<form:input name="name"/>
					</td>
					<td class="error">
						<form:error name="name"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>