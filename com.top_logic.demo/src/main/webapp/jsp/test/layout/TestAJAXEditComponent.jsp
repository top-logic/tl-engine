<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Test AJAX Edit Component
		</h1>

		<form:form>
			<table>
				<tr>
					<td>
						<form:label name="date"/>
					</td>
					<td>
						<form:label name="date.begin"/>
					</td>
					<td>
						<form:onerror id="date.begin"
							name="date.begin"
						>
							Fehler:
							<form:error name="date.begin"/>
							<br/>
						</form:onerror>
						<form:input name="date.begin"/>
					</td>
				</tr>
				<tr>
					<td>
					</td>
					<td>
						<form:label name="date.end"/>
					</td>
					<td>
						<form:onerror id="date.end"
							name="date.end"
						>
							Fehler:
							<form:error name="date.end"/>
							<br/>
						</form:onerror>
						<form:input name="date.end"/>
					</td>
				</tr>
				<tr>
					<td>
						<form:label name="days"/>
					</td>
					<td>
						<form:label name="days.min"/>
					</td>
					<td>
						<form:onerror id="days.min"
							name="days.min"
						>
							Fehler:
							<form:error name="days.min"/>
							<br/>
						</form:onerror>
						<form:input name="days.min"/>
					</td>
				</tr>
				<tr>
					<td>
					</td>
					<td>
						<form:label name="days.max"/>
					</td>
					<td>
						<form:onerror id="days.max"
							name="days.max"
						>
							Fehler:
							<form:error name="days.max"/>
							<br/>
						</form:onerror>
						<form:input name="days.max"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>