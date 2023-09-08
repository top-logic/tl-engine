<%@page import="com.top_logic.demo.layout.form.demo.PersonAjaxForm"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.component.FormComponent"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<table>
				<tr>
					<td class="label">
						<form:label name="<%= PersonAjaxForm.PERSON_GOTO_FIELD %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:popup name="<%= PersonAjaxForm.PERSON_GOTO_FIELD %>"/>
						<form:error name="<%= PersonAjaxForm.PERSON_GOTO_FIELD %>"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="<%= PersonAjaxForm.PERSON_MAILTO_FIELD %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:popup name="<%= PersonAjaxForm.PERSON_MAILTO_FIELD %>"/>
						<form:error name="<%= PersonAjaxForm.PERSON_MAILTO_FIELD %>"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>