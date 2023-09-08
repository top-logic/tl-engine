<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.tool.dataImport.layout.DataImportAssistant"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<br/>
			&#xA0;&#xA0;&#xA0;
			<form:input name="<%=DataImportAssistant.FIELD_MESSAGE%>"/>
			<br/>
			<br/>

			<form:ifExists name="<%=DataImportAssistant.FIELD_ERROR_MESSAGE%>">
				<table style="margin: 5px">
					<tr>
						<td>
							<form:input name="<%=DataImportAssistant.FIELD_ERROR_MESSAGE%>"
								multiLine="true"
							/>
						</td>
					</tr>
				</table>
			</form:ifExists>

			<form:ifExists name="<%=DataImportAssistant.FIELD_WARNING_MESSAGE%>">
				<table style="margin: 5px">
					<tr>
						<td>
							<form:input name="<%=DataImportAssistant.FIELD_WARNING_MESSAGE%>"
								multiLine="true"
							/>
						</td>
					</tr>
				</table>
			</form:ifExists>

			<form:ifExists name="<%=DataImportAssistant.FIELD_INFO_MESSAGE%>">
				<table style="margin: 5px">
					<tr>
						<td>
							<form:input name="<%=DataImportAssistant.FIELD_INFO_MESSAGE%>"
								multiLine="true"
							/>
						</td>
					</tr>
				</table>
			</form:ifExists>
		</form:form>
	</layout:body>
</layout:html>