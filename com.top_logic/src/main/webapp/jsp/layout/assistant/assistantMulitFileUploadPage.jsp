<%@page language="java" session="true" extends="com.top_logic.knowledge.gui.layout.webfolder.WebFolderJsp"
%><%@page import="com.top_logic.tool.boundsec.assistent.AssistantMultiFileUploadComponent"
%><%@taglib uri="basic"  prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><%@page import="com.top_logic.mig.html.layout.FileUploadComponent"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"%><%
AssistantMultiFileUploadComponent uploadComponent = (AssistantMultiFileUploadComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<basic:fieldset titleKeySuffix="upload">
				<table id="visiblePart"
					align="center"
					border="0"
					summary="Upload table"
					width="100%"
				>
					<tr>
						<td colspan="2">
							&#xA0;
						</td>
					</tr>
					<tr>
						<td class="content"
							style="width:345px;"
							valign="middle"
						>
							<form:custom name="<%= FileUploadComponent.UPLOAD_FIELD_NAME %>"/>
						</td>
						<form:ifExists name="language">
							<td class="content">
								&#xA0;&#xA0;&#xA0;
								<form:label name="language"/>
								:
							</td>
							<td class="content">
								&#xA0;
								<form:select name="language"/>
							</td>
						</form:ifExists>
						<td class="content"
							valign="middle"
						>
							&#xA0;&#xA0;&#xA0;
							<form:command name="<%= AssistantMultiFileUploadComponent.UPLOAD_COMMAND_FIELD %>"
								renderer="<%= DefaultButtonRenderer.INSTANCE %>"
							/>
						</td>
					</tr>
				</table>
			</basic:fieldset>
			<br/>
			<form:tableview name="<%=AssistantMultiFileUploadComponent.FILE_TABLE_FIELD_NAME%>"/>
		</form:form>
	</layout:body>
</layout:html>