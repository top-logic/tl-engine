<%@page language="java" session="true" extends="com.top_logic.knowledge.gui.layout.webfolder.WebFolderJsp"
%><%@page import="com.top_logic.mig.html.layout.FileUploadComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<h2>
				<form:resource key="title"/>
			</h2>
			<p>
				<form:resource key="message"/>
			</p>
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
							valign="middle"
						>
							<form:custom name="<%=FileUploadComponent.UPLOAD_FIELD_NAME %>"/>
						</td>
					</tr>
				</table>
			</basic:fieldset>
		</form:form>
	</layout:body>
</layout:html>