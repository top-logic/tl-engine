<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Dialog Demo
		</h1>

		<p>
			Test opening modal dialogs by pressing buttons below.
		</p>

		<p>
			Test that select fields are hidden by dialog.
		</p>

		<form:form>
			<table>
				<tr>
					<td>
						<form:label name="select"/>
					</td>
					<td>
						<form:select name="select"/>
					</td>
				</tr>
				<tr>
					<td>
						<form:label name="selectMulti"/>
					</td>
					<td>
						<form:select name="selectMulti"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>