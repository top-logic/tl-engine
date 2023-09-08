<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.tool.dataImport.layout.DataImportStatusComponent"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<table style="margin: 5px">
				<tr>
					<td>
						<b>
							<form:label name="<%=DataImportStatusComponent.FIELD_SYSTEM_DATA_TS%>"
								colon="true"
							/>
						</b>
					</td>
					<td>
						&#xA0;&#xA0;&#xA0;
					</td>
					<td>
						<form:input name="<%=DataImportStatusComponent.FIELD_SYSTEM_DATA_TS%>"/>
					</td>
				</tr>
				<tr>
				</tr>
				<tr>
					<td>
						<b>
							<form:label name="<%=DataImportStatusComponent.FIELD_LAST_RUN%>"
								colon="true"
							/>
						</b>
					</td>
					<td>
						&#xA0;&#xA0;&#xA0;
					</td>
					<td>
						<form:input name="<%=DataImportStatusComponent.FIELD_LAST_RUN%>"/>
					</td>
				</tr>
				<tr>
				</tr>
				<tr>
					<td>
						<b>
							<form:label name="<%=DataImportStatusComponent.FIELD_LAST_RESULT%>"
								colon="true"
							/>
						</b>
					</td>
					<td>
						&#xA0;&#xA0;&#xA0;
					</td>
					<td>
						<form:input name="<%=DataImportStatusComponent.FIELD_LAST_RESULT%>"/>
					</td>
				</tr>
			</table>

			<hr/>

			<table style="margin: 5px">
				<tr>
					<td>
						<b>
							<form:label name="<%=DataImportStatusComponent.FIELD_LAST_PROTOCOL%>"
								colon="true"
							/>
						</b>
					</td>
				</tr>
			</table>
			<table style="margin: 5px">
				<tr>
					<td>
						<form:input name="<%=DataImportStatusComponent.FIELD_LAST_PROTOCOL%>"
							multiLine="true"
						/>
					</td>
				</tr>
			</table>
			<br/>
		</form:form>
	</layout:body>
</layout:html>